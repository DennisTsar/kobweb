package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.layout.HorizontalDivider
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import playground.components.layouts.PageLayout
import playground.components.sections.NavHeader

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        Surface(Modifier.padding(1.cssRem), colorModeOverride = ColorMode.current.opposite) {
            Div {
                Text("Mode: ${ColorMode.current}")
                NavHeader()
                HorizontalDivider()
                Surface(Modifier.padding(1.cssRem), colorModeOverride = ColorMode.current.opposite) {
                    Div {
                        Text("Mode2: ${ColorMode.current}")
                        NavHeader()
                        HorizontalDivider()
                        Surface(Modifier.padding(1.cssRem), colorModeOverride = ColorMode.current.opposite) {
                            Div {
                                Text("Mode3: ${ColorMode.current}")
                                NavHeader()
                            }
                        }
                    }
                }
            }
        }
    }
}
