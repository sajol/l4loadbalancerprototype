package com.example.l4loadbalancer.application.service

import com.example.l4loadbalancer.util.suspendAccept
import com.example.l4loadbalancer.util.suspendConnect
import com.example.l4loadbalancer.util.suspendRead
import com.example.l4loadbalancer.util.suspendWrite
import kotlinx.coroutines.*
import mu.KotlinLogging
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Executors

class L4LoadBalancerService(
    private val backendConfigService: BackendConfigService,
) {
    private val logger = KotlinLogging.logger {}
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var server: AsynchronousServerSocketChannel
    private val acceptThreadPool = Executors.newFixedThreadPool(8).asCoroutineDispatcher()


    init {
        ConnectionTracker.initialize(backendConfigService.getBackendAddresses())
    }

    fun startServer() = runBlocking {
        // Initialize the server socket
        server = AsynchronousServerSocketChannel.open()
            .bind(InetSocketAddress("localhost", 8080), 1024)
        logger.info { "L4 Load Balancer started on port 8080" }

        // Accept connections in a loop
        while (true) {
            withContext(acceptThreadPool) {
                val client = server.suspendAccept()
                logger.info { "New client connected from ${client.remoteAddress}" }
                scope.launch {
                    try {
                        handleClient(client)
                    } catch (e: Exception) {
                        logger.error(e) { "Error while handling connection from ${client.remoteAddress}" }
                        sendErrorResponse(client, "Internal Server Error")
                    } finally {
                        closeConnection(client)
                    }
                }
            }
        }
    }

    private suspend fun closeConnection(client: AsynchronousSocketChannel) {
        try {
            withContext(Dispatchers.IO) {
                client.close()
            }
            logger.info { "Connection closed for ${client.remoteAddress}" }
        } catch (e: Exception) {
            logger.error(e) { "Error while closing connection from ${client.remoteAddress}" }
        }
    }

    private suspend fun handleClient(client: AsynchronousSocketChannel) {
        val bufferToBackend = ByteBuffer.allocate(1024)
        val bufferToClient = ByteBuffer.allocate(1024)

        val backendAddress = backendConfigService.selectBackend()
        val backendServer = connectToBackend(backendAddress)
        ConnectionTracker.incrementConnection(backendAddress)

        try {
            coroutineScope {
                // Handle client-to-backend communication
                launch(Dispatchers.IO) {
                    proxyClientRequestToBaceknd(bufferToBackend, client, backendServer)
                }

                // Handle backend-to-client communication
                launch(Dispatchers.IO) {
                    proxyBackendResponseToClient(bufferToClient, backendServer, client)
                }
            }
        } finally {
            logger.info { "Closing connections for client: ${client.remoteAddress}" }
            closeConnection(client)
            closeConnection(backendServer)
            ConnectionTracker.decrementConnection(backendAddress)
        }
    }

    private suspend fun proxyBackendResponseToClient(
        bufferToClient: ByteBuffer, backendServer: AsynchronousSocketChannel, client: AsynchronousSocketChannel
    ) {
        while (true) {
            bufferToClient.clear()
            val backendBytesRead = backendServer.suspendRead(bufferToClient)
            if (backendBytesRead == -1) {
                logger.info { "Backend server closed connection: ${backendServer.remoteAddress}" }
                break
            }
            bufferToClient.flip()
            client.suspendWrite(bufferToClient)
        }
    }

    private suspend fun proxyClientRequestToBaceknd(
        bufferToBackend: ByteBuffer, client: AsynchronousSocketChannel, backendServer: AsynchronousSocketChannel
    ) {
        while (true) {
            bufferToBackend.clear()
            val clientBytesRead = client.suspendRead(bufferToBackend)
            if (clientBytesRead == -1) {
                logger.info { "Client disconnected: ${client.remoteAddress}" }
                break
            }
            bufferToBackend.flip()
            backendServer.suspendWrite(bufferToBackend)
        }
    }

    private suspend fun connectToBackend(backendAddress: InetSocketAddress): AsynchronousSocketChannel {
        val backend = withContext(Dispatchers.IO) {
            AsynchronousSocketChannel.open()
        }
        backend.suspendConnect(backendAddress)
        logger.info { "Connected to backend server at $backendAddress" }
        return backend
    }

    private suspend fun sendErrorResponse(client: AsynchronousSocketChannel, message: String) {
        val response = """
            HTTP/1.1 500 Internal Server Error
            Content-Length: ${message.toByteArray().size}
            Content-Type: text/plain
            
            $message
        """.trimIndent()

        val buffer = ByteBuffer.wrap(response.toByteArray())
        try {
            client.suspendWrite(buffer)
        } catch (e: Exception) {
            logger.error(e) { "Error while sending error response to ${client.remoteAddress}" }
        }
    }
}