package playground.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.layout.BuiltLayout
import com.varabyte.kobweb.layout.createLayout
import com.varabyte.kobweb.layout.createPage
import com.varabyte.kobweb.silk.components.forms.Button
import com.varabyte.kobweb.silk.components.forms.TextInput
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import playground.components.sections.NavHeader
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

// Accepting State probably isn't the right way to do this, instead use a lazy value like in `NestedLayout`
class PageLayoutData(val title: String, val subHead: State<String?>) {
    constructor(title: String, subHead: String?) : this(title, mutableStateOf(subHead))
}

val PageLayout = createLayout { data: PageLayoutData, content ->
    val title = data.title
    LaunchedEffect(title) {
        document.title = title
    }

    Column(
        modifier = Modifier.fillMaxSize().textAlign(TextAlign.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHeader()
        H1 { Text(title) }
        data.subHead.value?.let { H2 { Text(it) } }

        run {
            val num = remember { Random.nextInt().toString() }
            Text(num)
            Div(Modifier.size(200.px, 100.px).overflow(Overflow.Auto).toAttrs()) {
                repeat(100) {
                    Div {
                        Text("Hello $it")
                    }
                }
            }
        }

        content()
    }
}

private var name = mutableStateOf("")
val Page1 = createPage(PageLayout(PageLayoutData("Page 1", name))) {
    var name by name
    Text("Please enter your name")
    TextInput(name, onTextChange = { name = it })
    P()
    Text("Hello ${name.takeIf { it.isNotBlank() } ?: "World"}!")
}

val Page2 = createPage(PageLayout(PageLayoutData("Page 2", "sub head"))) {
    Text("This is Page 2")
}


class NestedLayoutData(val index: Int, val title: String)

private var myData by mutableStateOf(PageLayoutData("Nested", null))

val NestedLayout = createLayout(PageLayout { myData }) { data: NestedLayoutData, content ->
    LaunchedEffect(Unit) {
        var num = 0
        while (true) {
            num++
            myData = PageLayoutData("${data.title} (Nested) $num", data.index.toString())
            delay(1.seconds)
        }
    }
    Box(Modifier.backgroundColor(Colors.Red)) {
        content()
    }
}

val Page3 = createPage(NestedLayout(NestedLayoutData(1, "Page 3"))) {
    Text("This is Page 3 (nested)")
}

val Page4 = createPage(PageLayout(PageLayoutData("Page 4", "sub head"))) {
    Text("This is Page 4")
}


@Page
@Composable
fun HomePage() {
    val pages by remember { mutableStateOf(listOf(Page1, Page2, Page3, Page4)) }
    var pageIndex by remember { mutableStateOf(0) }
    val page = pages[pageIndex]
    var layoutMethod = page.layout
    val layouts: List<BuiltLayout<*>> =
        buildList {
            while (layoutMethod != null) {
                add(0, layoutMethod)
                layoutMethod = layoutMethod.layout
            }
        }

    Div {
        Button({ pageIndex = (pageIndex + 1) % pages.size }) { Text("Next Page") }
        Div {
            val x = layouts.foldRight(page.content) { layout, accum ->
                {
                    val content = layout.content as @Composable (Any, @Composable () -> Unit) -> Unit
                    content.invoke(layout.data as Any, accum)
                }
            }.invoke()
        }
    }
}
