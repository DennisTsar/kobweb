package com.varabyte.kobweb.silk

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.KobwebComposeStyles
import com.varabyte.kobweb.core.KobwebApp
import com.varabyte.kobweb.silk.init.SilkWidgetVariables
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass

@Composable
fun SilkApp(content: @Composable () -> Unit) {
    KobwebApp {
        KobwebComposeStyles()
        SilkFoundationStyles()
        val colorMode = ColorMode.current
        DisposableEffect(colorMode) {
            document.documentElement?.addClass(if (colorMode.isLight) "silk-light" else "silk-dark")
            document.documentElement?.removeClass(if (colorMode.isLight) "silk-dark" else "silk-light")
            onDispose { }
        }
        SilkWidgetVariables()
        content()
    }
}
