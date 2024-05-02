package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.dom.disposableRef
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.startViewTransition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.toModifier
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.promise
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Text
import playground.components.layouts.PageLayout
import kotlin.time.Duration.Companion.milliseconds


val TestStyle by ComponentStyle {
    base { Modifier.styleModifier { property("view-transition-name", "test-1") } }
}

val TestStyle2 by ComponentStyle {
    base { Modifier.styleModifier { property("view-transition-name", "test-2") } }
}

@Page
@Composable
fun HomePage() {
    PageLayout("Welcome to Kobweb!") {
        Box(TestStyle.toModifier().size(200.px).backgroundColor(Colors.Green))
        var state by remember { mutableStateOf(0) }
        val ctx = rememberPageContext()
        Button(
            onClick = {
                startViewTransition {
                    MainScope().promise {
                        ctx.router.navigateTo("/widgets")
                        delay(1.milliseconds)
                    }
                }
                Unit
            }
        ) {
            Text("Click me!")
        }
        Button(
            onClick = {
                startViewTransition {
                    state = if (state == 0) 1 else 0
                }
                Unit
            }
        ) {
            Text("Click me2!")
        }
        when (state) {
            0 -> {
                Box(
                    TestStyle2.toModifier()
                        .position(Position.Absolute)
                        .top(100.px)
                        .left(100.px)
                        .width(100.px)
                        .height(100.px)
                        .backgroundColor(Color("red")),
                    ref = disposableRef {
                        println("created0")
                        onDispose {
                            println("disposed0")
                        }
                    }
                )
            }

            1 -> {
                Box(
                    TestStyle2.toModifier()
                        .position(Position.Absolute)
                        .top(100.px)
                        .left(100.px)
                        .width(100.px)
                        .height(100.px)
                        .backgroundColor(Color("blue")),
                    ref = disposableRef {
                        println("created1")
                        onDispose {
                            println("disposed1")
                        }
                    }
                )
            }
        }
    }
}
