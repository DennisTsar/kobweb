package com.varabyte.kobweb.compose.ui.modifiers

import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.styleModifier
import org.jetbrains.compose.web.css.*

class OutlineScope internal constructor(private val styleScope: StyleScope) {
    fun color(color: CSSColorValue) = styleScope.outlineColor(color)
    fun style(style: LineStyle) = styleScope.outlineStyle(style)
    fun width(width: CSSLengthNumericValue) = styleScope.outlineWidth(width)
    fun width(width: OutlineWidth) = styleScope.outlineWidth(width)
}

fun Modifier.outline(outline: Outline) = styleModifier {
    outline(outline)
}

fun Modifier.outline(scope: OutlineScope.() -> Unit) = styleModifier {
    OutlineScope(this).apply(scope)
}

fun Modifier.outline(width: CSSLengthNumericValue? = null, style: LineStyle? = null, color: CSSColorValue? = null) = styleModifier {
    outline(Outline.of(width?.let { OutlineWidth.of(it) }, style, color))
}

fun Modifier.outlineOffset(value: CSSLengthNumericValue) = styleModifier {
    outlineOffset(value)
}
