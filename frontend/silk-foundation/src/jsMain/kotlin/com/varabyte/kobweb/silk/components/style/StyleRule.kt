package com.varabyte.kobweb.silk.components.style

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.attributes.ComparableAttrsScope
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.compose.ui.toStyles
import com.varabyte.kobweb.compose.util.kebabCaseToTitleCamelCase
import com.varabyte.kobweb.silk.theme.SilkTheme
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.suffixedWith
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.w3c.dom.Element

// TODO: constructor visibility
abstract class StyleRule(
    internal val init: ComponentModifiers.() -> Unit,
    internal val extraModifiers: @Composable () -> Modifier,
)

/**
 * A [ComponentStyle] pared down to read-only data only, which should happen shortly after Silk initializes.
 *
 * @param stylesheetInfo The information needed to apply this style to a stylesheet and to generate a modifier.
 * @param extraModifiers Additional modifiers that can be tacked onto this component style, convenient for including
 *   non-style attributes whenever this style is applied.
 */
internal class ImmutableStyleRule(
    private val stylesheetInfo: StylesheetInfo,
    internal val extraModifiers: @Composable () -> Modifier
) {
    private val classNames = stylesheetInfo.classSelectors.classNames.toSet()

    @Composable
    fun toModifier(): Modifier {
        val currentClassNames = classNames.filterNot { it.endsWith(ColorMode.current.opposite.name.lowercase()) }
        return (if (currentClassNames.isNotEmpty()) Modifier.classNames(*currentClassNames.toTypedArray()) else Modifier)
            .then(extraModifiers())
    }

    internal fun addStylesInto(styleSheet: StyleSheet) {
        stylesheetInfo.cssStyleRules.forEach { group ->
            if (group.mediaQuery == null) {
                styleSheet.addStyles(group.cssRule, group.styles)
            } else {
                styleSheet.media(group.mediaQuery) {
                    addStyles(group.cssRule, group.styles)
                }
            }
        }
    }

    /**
     * @param cssRule A selector plus an optional pseudo keyword (e.g. "a", "a:link", and "a::selection")
     */
    private fun <T : StyleScope> GenericStyleSheetBuilder<T>.addStyles(cssRule: String, styles: ComparableStyleScope) {
        cssRule style {
            styles.properties.forEach { entry -> property(entry.key, entry.value) }
            styles.variables.forEach { entry -> variable(entry.key, entry.value) }
        }
    }
}

/**
 * Holds information about CSS class selectors and style rules associated with a `ComponentStyle`.
 *
 * @param classSelectors A list of class selectors associated with the component style.
 * @param cssStyleRules A list of CSS style rules that will need to be added to the stylesheet.
 */
// TODO: name this better
internal class StylesheetInfo(val classSelectors: ClassSelectors, val cssStyleRules: List<CssStyleRule>)

/**
 * Represents an individual CSS style rule, containing all the information needed to add the styles to a StyleSheet.
 *
 * @param cssRule The CSS rule string, including selector and optional pseudo-elements.
 * @param styles A scope containing the CSS properties and variables associated with the rule.
 * @param mediaQuery An optional media query string defining the conditions under which this rule applies.
 */
internal class CssStyleRule(val cssRule: String, val styles: ComparableStyleScope, val mediaQuery: String? = null)

/** Represents the class selectors associated with a [ComponentStyle]. */
internal value class ClassSelectors(private val value: List<String>) {
    // Selectors may be ".someStyle" or ".someStyle.someVariant" - only the last part is relevant to the specific style
    val classNames get() = value.map { it.substringAfterLast('.') }
    operator fun plus(other: ClassSelectors) = ClassSelectors(value + other.value)
}

internal class SimpleStyleRule(
    init: ComponentModifiers.() -> Unit,
    val selector: String,
    extraModifiers: @Composable () -> Modifier,
) : StyleRule(init, extraModifiers) {
    @Composable
    fun toModifier(): Modifier {
        return SilkTheme.componentStyles.getValue(selector).toModifier()
    }

    /**
     * Shared logic for using an initial selector name and triggering a callback with the final selector name and
     * CSS styles to be associated with it.
     */
    private fun withFinalSelectorName(
        selectorBaseName: String,
        group: StyleGroup,
        handler: (String, ComparableStyleScope) -> Unit
    ) {
        when (group) {
            is StyleGroup.Light -> handler(selectorBaseName.suffixedWith(ColorMode.LIGHT), group.styles)
            is StyleGroup.Dark -> handler(selectorBaseName.suffixedWith(ColorMode.DARK), group.styles)
            is StyleGroup.ColorAgnostic -> handler(selectorBaseName, group.styles)
            is StyleGroup.ColorAware -> {
                handler(selectorBaseName.suffixedWith(ColorMode.LIGHT), group.lightStyles)
                handler(selectorBaseName.suffixedWith(ColorMode.DARK), group.darkStyles)
            }
        }
    }

    // Collect all CSS selectors (e.g. all base, hover, breakpoints, etc. modifiers) and, if we ever find multiple
    // definitions for the same selector, just combine them together. One way this is useful is you can use
    // `MutableSilkTheme.modifyComponentStyle` to layer additional styles on top of a base style. In almost all
    // practical cases, however, there will only ever be a single selector of each type per component style.
    private fun ComponentModifiers.mergeCssModifiers(init: ComponentModifiers.() -> Unit): Map<CssModifier.Key, CssModifier> {
        return apply(init).cssModifiers
            .groupBy { it.key }
            .mapValues { (_, group) ->
                group.reduce { acc, curr -> acc.mergeWith(curr) }
            }
    }

    private fun Map<CssModifier.Key, CssModifier>.assertNoAttributeModifiers(selectorName: String): Map<CssModifier.Key, CssModifier> {
        return this.onEach { (_, cssModifier) ->
            val attrsScope = ComparableAttrsScope<Element>()
            cssModifier.modifier.toAttrs<AttrsScope<Element>>().invoke(attrsScope)
            if (attrsScope.attributes.isEmpty()) return@onEach

            error(buildString {
                appendLine("ComponentStyle declarations cannot contain Modifiers that specify attributes. Please move Modifiers associated with attributes to the ComponentStyle's `extraModifiers` parameter.")
                appendLine()
                appendLine("Details:")

                append("\tCSS rule: ")
                append("\"$selectorName")
                if (cssModifier.mediaQuery != null) append(cssModifier.mediaQuery)
                if (cssModifier.suffix != null) append(cssModifier.suffix)
                append("\"")

                append(" (do you declare a property called ")
                // ".example" likely comes from `ExampleStyle` while ".example.example-outlined" likely
                // comes from ExampleOutlinedVariant or OutlinedExampleVariant
                val isStyle = selectorName.count { it == '.' } == 1// "Variant" else "Style"
                val styleName = selectorName.substringAfter(".").substringBefore(".")

                if (isStyle) {
                    append("`${styleName.kebabCaseToTitleCamelCase()}Style`")
                } else {
                    // Convert ".example.example-outlined" to "outlined". This could come from a variant
                    // property called OutlinedExampleVariant or ExampleOutlinedVariant
                    val variantPart = selectorName.substringAfterLast(".").removePrefix("$styleName-")
                    append("`${"$styleName-$variantPart".kebabCaseToTitleCamelCase()}Variant`")
                    append(" or ")
                    append("`${"$variantPart-$styleName".kebabCaseToTitleCamelCase()}Variant`")
                }
                appendLine("?)")
                appendLine("\tAttribute(s): ${attrsScope.attributes.keys.joinToString(", ") { "\"$it\"" }}")
                appendLine()
                appendLine("An example of how to fix this:")
                appendLine(
                    """
                    |   // Before
                    |   val ExampleStyle by ComponentStyle {
                    |       base {
                    |          Modifier
                    |              .backgroundColor(Colors.Magenta))
                    |              .tabIndex(0) // <-- The offending attribute modifier
                    |       }
                    |   }
                    |   
                    |   // After
                    |   val ExampleStyle by ComponentStyle(extraModifiers = Modifier.tabIndex(0)) {
                    |       base {
                    |           Modifier.backgroundColor(Colors.Magenta)
                    |       }
                    |   }
                    """.trimMargin()
                )
            })
        }
    }


    /**
     * Processes and validates the Style, returning data related to the usage of the style.
     *
     * @return A `StylesheetInfo` object containing the class selectors and the CSS style rules.
     *  - The CSS class selectors associated with the style, always including the base class, and
     *  potentially additional classes if the style is color mode aware. This lets us avoid applying unnecessary
     *  classnames, making it easier to debug CSS issues in the browser.
     *  - A list of CSS style rules describing how it should be applied to the stylesheet.
     */
    private fun getStyleSheetInfo(selectorName: String): StylesheetInfo {
        // Always add the base selector name, even if the ComponentStyle is empty. Callers may use empty
        // component styles as classnames, which can still be useful for targeting one element from another, or
        // searching for all elements tagged with a certain class.
        val classNames = mutableListOf(selectorName)
        val styleGroupThings = mutableListOf<CssStyleRule>()

        val lightModifiers = ComponentModifiers(ColorMode.LIGHT).mergeCssModifiers(init)
            .assertNoAttributeModifiers(selectorName)
        val darkModifiers = ComponentModifiers(ColorMode.DARK).mergeCssModifiers(init)
            .assertNoAttributeModifiers(selectorName)

        StyleGroup.from(lightModifiers[CssModifier.BaseKey]?.modifier, darkModifiers[CssModifier.BaseKey]?.modifier)
            ?.let { group ->
                withFinalSelectorName(selectorName, group) { name, styles ->
                    if (styles.isNotEmpty()) {
                        classNames.add(name)
                        styleGroupThings.add(CssStyleRule(name, styles))
                    }
                }
            }

        val allCssRuleKeys = (lightModifiers.keys + darkModifiers.keys).filter { it != CssModifier.BaseKey }
        for (cssRuleKey in allCssRuleKeys) {
            val group = StyleGroup.from(lightModifiers[cssRuleKey]?.modifier, darkModifiers[cssRuleKey]?.modifier)
                ?: continue
            withFinalSelectorName(selectorName, group) { name, styles ->
                if (styles.isNotEmpty()) {
                    classNames.add(name)

                    val cssRule = "$name${cssRuleKey.suffix.orEmpty()}"
                    val styleGroupThing = if (cssRuleKey.mediaQuery != null) {
                        CssStyleRule(cssRule, styles, cssRuleKey.mediaQuery)
                    } else {
                        CssStyleRule(cssRule, styles)
                    }
                    styleGroupThings.add(styleGroupThing)
                }
            }
        }
        return StylesheetInfo(ClassSelectors(classNames), styleGroupThings)
    }

    internal fun intoImmutableStyle(): ImmutableStyleRule {
        return ImmutableStyleRule(getStyleSheetInfo(selector), extraModifiers)
    }
}

private sealed interface StyleGroup {
    class Light(val styles: ComparableStyleScope) : StyleGroup
    class Dark(val styles: ComparableStyleScope) : StyleGroup
    class ColorAgnostic(val styles: ComparableStyleScope) : StyleGroup
    class ColorAware(val lightStyles: ComparableStyleScope, val darkStyles: ComparableStyleScope) : StyleGroup

    companion object {
        @Suppress("NAME_SHADOWING") // Shadowing used to turn nullable into non-null
        fun from(lightModifiers: Modifier?, darkModifiers: Modifier?): StyleGroup? {
            val lightStyles = lightModifiers?.let { lightModifiers ->
                ComparableStyleScope().apply { lightModifiers.toStyles().invoke(this) }
            }
            val darkStyles = darkModifiers?.let { darkModifiers ->
                ComparableStyleScope().apply { darkModifiers.toStyles().invoke(this) }
            }

            if (lightStyles == null && darkStyles == null) return null
            if (lightStyles != null && darkStyles == null) return Light(lightStyles)
            if (lightStyles == null && darkStyles != null) return Dark(darkStyles)
            check(lightStyles != null && darkStyles != null)
            return if (lightStyles == darkStyles) {
                ColorAgnostic(lightStyles)
            } else {
                ColorAware(lightStyles, darkStyles)
            }
        }
    }
}
