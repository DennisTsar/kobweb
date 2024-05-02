package com.varabyte.kobweb.compose

import androidx.compose.runtime.*
import com.varabyte.kobweb.browser.util.invokeLater
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Promise
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// IMPORTANT NOTE: for ths to consistently work (main inconsistency is view-transition-name not working), the
// coroutineContext's MonotonicFrameClock must not be based on window.requestAnimationFrame. This is because view
// transitions freeze the dom, which means that requestAnimationFrame doesn't get called, which means that recomposition
// doesn't happen. So we need to use a different MonotonicFrameClock implementation, current using window.invokeLater

// Another note: due to the "freezing the dom" behavior, clicking a button causes it to lose the hover effect & then
// regain it after the transition is done. This might be intended behavior? But worth looking into.

// `action` must return a Promise so that startViewTransition awaits it (and thus awaits the recomposition)
fun startViewTransition(action: () -> Promise<*>): dynamic {
    val document = document.asDynamic()
    return if (document.startViewTransition != undefined) document.startViewTransition(action) else action()
}


class MyMonotonicClockImpl : MonotonicFrameClock {
    override suspend fun <R> withFrameNanos(
        onFrame: (Long) -> R
    ): R = suspendCoroutine { continuation ->
        val time = window.performance.now().toDuration(DurationUnit.MILLISECONDS)
        window.invokeLater {
            val duration = window.performance.now().toDuration(DurationUnit.MILLISECONDS) - time
            val result = onFrame(duration.inWholeNanoseconds) // not sure this time is even used
            continuation.resume(result)
        }
        // Default Clock:
//        window.requestAnimationFrame {
//            val duration = it.toDuration(DurationUnit.MILLISECONDS)
//            val result = onFrame(duration.inWholeNanoseconds)
//            continuation.resume(result)
//        }
    }
}
