package com.varabyte.kobweb.silk.components.icons.fa

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.toAttrs
import org.jetbrains.compose.web.dom.TagElement
import org.w3c.dom.HTMLCollection
import org.w3c.dom.asList

enum class IconStyle {
    FILLED,
    OUTLINE;
}

// See: https://docs.fontawesome.com/web/style/size
enum class IconSize(internal val className: String) {
    // Relative sizes
    XXS("fa-2xs"),
    XS("fa-xs"),
    SM("fa-sm"),
    LG("fa-lg"),
    XL("fa-xl"),
    XXL("fa-2xl"),

    // Literal sizes
    X1("fa-1x"),
    X2("fa-2x"),
    X3("fa-3x"),
    X4("fa-4x"),
    X5("fa-5x"),
    X6("fa-6x"),
    X7("fa-7x"),
    X8("fa-8x"),
    X9("fa-9x"),
    X10("fa-10x");
}

@JsModule("@fortawesome/fontawesome-svg-core")
private external object FaSvgApi {
    // https://docs.fontawesome.com/apis/javascript/methods#iconicondefinition-params
    fun icon(icon: dynamic): dynamic
}

@Composable
internal fun FaIcon(
    icon: dynamic,
    modifier: Modifier,
    size: IconSize? = null,
) {
    val elements = FaSvgApi.icon(icon).node.unsafeCast<HTMLCollection>()
    elements.asList().forEach { element ->
        TagElement(
            { element },
            applyAttrs = modifier.toAttrs {
                element.attributes.asList().forEach { attr -> attr(attr.name, attr.value) }
                if (size != null) {
                    classes(size.className)
                }
            },
            content = null,
        )
    }
}
