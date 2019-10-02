package com.chuckerteam.chucker.api

import com.chuckerteam.chucker.internal.data.entity.WebsocketOperation.*
import com.chuckerteam.chucker.internal.data.entity.asWebsocketTraffic
import okhttp3.*
import okio.ByteString

fun OkHttpClient.newLoggedWebSocket(request: Request, listener: WebSocketListener): WebSocket {
    val interceptor =
        interceptors().firstOrNull { it is ChuckerInterceptor } as ChuckerInterceptor? ?:
        networkInterceptors().firstOrNull { it is ChuckerInterceptor } as ChuckerInterceptor?

    return when (interceptor) {
        null -> newWebSocket(request, listener)
        else -> LoggedWebsocket(
            newWebSocket(request, LoggedWebSocketListener(listener, interceptor.collector)),
            interceptor.collector
        )
    }
}

private class LoggedWebsocket(val wrapped: WebSocket, val collector: ChuckerCollector) : WebSocket {
    override fun queueSize(): Long {
        return wrapped.queueSize()
    }

    override fun send(text: String): Boolean {
        collector.onWebsocketTraffic(
            wrapped.request().asWebsocketTraffic(SEND).apply {
                contentText = text
            })
        return wrapped.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        return wrapped.send(bytes)
    }

    override fun close(code: Int, reason: String?): Boolean {
        return wrapped.close(code, reason)
    }

    override fun cancel() {
        wrapped.cancel()
    }

    override fun request(): Request {
        return wrapped.request()
    }
}

private class LoggedWebSocketListener(
    val wrapped: WebSocketListener,
    val collector: ChuckerCollector
) : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        wrapped.onOpen(webSocket, response)
        collector.onWebsocketTraffic(
            webSocket.request().asWebsocketTraffic(OPEN)
        )
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        wrapped.onFailure(webSocket, t, response)
        collector.onWebsocketTraffic(
            webSocket.request().asWebsocketTraffic(FAILURE).apply {
                error = t.toString()
            })
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        wrapped.onClosing(webSocket, code, reason)
        collector.onWebsocketTraffic(
            webSocket.request().asWebsocketTraffic(CLOSING).apply {
                this.code = code
                this.reason = reason
            })
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        wrapped.onMessage(webSocket, text)
        collector.onWebsocketTraffic(
            webSocket.request().asWebsocketTraffic(MESSAGE).apply {
                this.contentText = text
            })
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        wrapped.onMessage(webSocket, bytes)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        wrapped.onClosed(webSocket, code, reason)
        collector.onWebsocketTraffic(
            webSocket.request().asWebsocketTraffic(CLOSED).apply {
                this.code = code
                this.reason = reason
            })
    }
}

fun Request.isWebsocketNegotiation(): Boolean {
    val upgradeHeader = headers().values("Upgrade")
        .firstOrNull { it == "websocket" } != null
    val connectionHeader = headers().values("Connection")
        .firstOrNull { it == "Upgrade" } != null
    return upgradeHeader && connectionHeader
}
