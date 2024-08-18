package com.varabyte.kobweb.silk.defer

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.clear
import org.jetbrains.compose.web.renderComposable

private class DeferredComposablesState {
    private var timeoutHandle = -1
    private val batchedCommands = mutableListOf<() -> Unit>()
    private val entries = mutableStateListOf<DeferredComposablesState.Entry>()

    // By not running some commands immediately, instead delaying and batching them together, this prevents a bunch of
    // intermediate recompositions.
    private fun invokeLater(block: () -> Unit) {
        batchedCommands.add(block)
        if (timeoutHandle == -1) {
            timeoutHandle = window.setTimeout({
                batchedCommands.forEach { it.invoke() }
                batchedCommands.clear()

                timeoutHandle = -1
            })
        }
    }

    fun append(id: String?): Entry = Entry(id).also {
        invokeLater {
            entries.add(it)
        }
    }

    @Composable
    fun forEach(render: @Composable (Entry) -> Unit) {
        entries.forEach { render(it) }
    }

    inner class Entry(val id: String?) {
        var content: (@Composable () -> Unit)? = null
        fun dismiss() {
            invokeLater {
                entries.remove(this)
            }
        }
    }
}

private val LocalDeferred = staticCompositionLocalOf<DeferredComposablesState> {
    error("Attempting to defer rendering without calling `renderWithDeferred`, a required pre-requisite.")
}

/**
 * Defer the target [content] from rendering until the main content is finished.
 *
 * This has the (often wanted) side effects of making sure the content always appears on top of the main content
 * (without needing to use z-index tricks) while also de-parenting the target being rendered (thereby avoiding
 * inheriting unexpected styles from element you want to appear beside, not within).
 *
 * Render deferral is particularly useful for overlays, like modals and tooltips.
 */
@Composable
fun deferRender(id: String? = null, content: @Composable () -> Unit) {
    val state = LocalDeferred.current
    val deferredEntry = remember(state) { state.append(id) }
    deferredEntry.content = content
    DisposableEffect(deferredEntry) { onDispose { deferredEntry.dismiss() } }
}

/**
 * Wraps a target composable with support for allowing deferred render calls.
 *
 * With this method called, any of the children Composables in [content] can trigger [deferRender], which will append
 * a render request which only gets run *after* the main content is finished rendering.
 *
 * You should only have to call this method once. Putting it near the root of your compose hierarchy is suggested.
 */
@Composable
fun renderWithDeferred(content: @Composable () -> Unit) {
    val state = DeferredComposablesState()
    CompositionLocalProvider(LocalDeferred provides state) {
        content()
        state.forEach { entry ->
            DisposableEffect(entry.id) {
                if (entry.id == null) return@DisposableEffect onDispose { }
                renderComposable(entry.id) {
                    entry.content?.invoke()
                }
                onDispose {
                    // Note: this resets the state when navigating between a page with & without this deferred content
                    document.getElementById(entry.id)?.clear()
                }
            }
            // Deferred content itself may defer more content! Like showing a tooltip within an overlay
            // If we don't do this, we end up with the deferred list constantly getting modified and causing
            // recompositions as a result.
            entry.content?.let { renderWithDeferred(it) }
        }
    }
}
