package com.example.l4loadbalancer.util

import kotlinx.coroutines.CancellableContinuation
import java.nio.channels.CompletionHandler
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CoroutineCompletionHandler<T>(
    private val continuation: CancellableContinuation<T>,
) : CompletionHandler<T, Void?> {
    override fun completed(result: T, attachment: Void?) {
        continuation.resume(result)
    }

    override fun failed(exc: Throwable, attachment: Void?) {
        continuation.resumeWithException(exc)
    }

}