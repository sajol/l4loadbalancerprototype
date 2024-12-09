package com.example.l4loadbalancer.util

import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel

suspend fun AsynchronousSocketChannel.suspendRead(buffer: ByteBuffer): Int =
    suspendCancellableCoroutine { cont ->
        this.read(buffer, null, CoroutineCompletionHandler(cont))
    }

suspend fun AsynchronousSocketChannel.suspendWrite(buffer: ByteBuffer): Int =
    suspendCancellableCoroutine { cont ->
        this.write(buffer, null, CoroutineCompletionHandler(cont))
    }

suspend fun AsynchronousSocketChannel.suspendConnect(address: InetSocketAddress): Void =
    suspendCancellableCoroutine { cont ->
        this.connect(address, null, CoroutineCompletionHandler(cont))
    }

suspend fun AsynchronousServerSocketChannel.suspendAccept(): AsynchronousSocketChannel =
    suspendCancellableCoroutine { cont ->
        this.accept(null, CoroutineCompletionHandler(cont))
    }