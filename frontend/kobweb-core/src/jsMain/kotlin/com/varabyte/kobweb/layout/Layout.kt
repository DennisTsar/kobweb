package com.varabyte.kobweb.layout

import androidx.compose.runtime.*


sealed interface ContentWithLayout<T> {
    val content: T
    val layout: BuiltLayout<*>?
}

// TODO: add a Layout that takes no data
interface Layout<T> : ContentWithLayout<@Composable (T, @Composable () -> Unit) -> Unit> {
    operator fun invoke(data: T) = object : BuiltLayout<T> by this {
        override val data = data
    }

    // lazy version to allow reactivity with MutableState
    operator fun invoke(data: () -> T) = object : BuiltLayout<T> by this {
        override val data get() = data()
    }

//    operator fun invoke(data: MutableState<T>) = object : BuiltLayout<T> by this {
//        override val data by data
//    }
}

// TODO: maybe this is the thing that should be named "Layout"?
interface BuiltLayout<T> : Layout<T> {
    val data: T
}

interface Page : ContentWithLayout<@Composable () -> Unit>

// Not named "Page" to not overlap with @Page
fun createPage(layout: BuiltLayout<*>? = null, content: @Composable () -> Unit) = object : Page {
    override val content = content
    override val layout = layout
}

fun <T> createLayout(layout: BuiltLayout<*>? = null, content: @Composable (T, @Composable () -> Unit) -> Unit) =
    object : Layout<T> {
        override val content = content
        override val layout = layout
    }
