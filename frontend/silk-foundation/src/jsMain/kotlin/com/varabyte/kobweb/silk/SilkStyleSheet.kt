package com.varabyte.kobweb.silk

import com.varabyte.kobweb.compose.css.*
import org.jetbrains.compose.web.css.*

internal val SILK_LIGHT_SELECTOR = ".silk-light"
internal val SILK_DARK_SELECTOR = ".silk-dark"

/** A stylesheet that will be used for configuring Silk components. */
object SilkStyleSheet : StyleSheet() {
    init {
        // TODO: should these be in a layer?
        SILK_LIGHT_SELECTOR style {
            colorScheme(ColorScheme.Light)
        }

        SILK_DARK_SELECTOR style {
            colorScheme(ColorScheme.Dark)
        }
    }
}
