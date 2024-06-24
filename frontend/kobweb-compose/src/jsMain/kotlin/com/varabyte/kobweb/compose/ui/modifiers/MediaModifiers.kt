package com.varabyte.kobweb.compose.ui.modifiers

import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.styleModifier

fun Modifier.objectFit(objectFit: ObjectFit) = styleModifier {
    objectFit(objectFit)
}

fun Modifier.mixBlendMode(blendMode: MixBlendMode) = styleModifier {
    mixBlendMode(blendMode)
}
