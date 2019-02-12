package com.readystatesoftware.chuck.internal.debug

import com.readystatesoftware.chuck.api.MockedResponse
import com.readystatesoftware.chuck.api.NetworkThrottling
import okhttp3.Interceptor
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class DebuggingChainProcessor {
    @NetworkThrottling.ThrottlingDelay
    var throttlingDelay = NetworkThrottling.None

    val allMockedResponses = mutableListOf<MockedResponse>()
    val activeMockedResponses = mutableListOf<MockedResponse>()

    fun registerMockedResponses(vararg responses: MockedResponse) {
        allMockedResponses.addAll(responses)
    }

    fun activateMockResponse(response: MockedResponse) {
        if (!activeMockedResponses.contains(response)) {
            activeMockedResponses.add(response)
        }
    }

    fun disableMockResponse(response: MockedResponse) {
        if (activeMockedResponses.contains(response)) {
            activeMockedResponses.add(response)
        }
    }

    fun processChain(chain: Interceptor.Chain, request: Request): WrappedResult {
        doDelay()
        return try {
            attemptToMock(request) ?: processRealCall(chain, request)
        } catch (e: IOException) {
            WrappedResult(throttlingDelay = throttlingDelay, error = e)
        }
    }

    private fun doDelay() {
        if (throttlingDelay > 0) {
            try {
                Thread.sleep(throttlingDelay * 1000L)
            } catch (ignored: InterruptedException) {
            }
        }
    }

    private fun attemptToMock(request: Request): WrappedResult? = activeMockedResponses.asSequence()
            .filter { it.isMatch(request.url().encodedPath()) }
            .map { it.toResponse(request) }
            .map { WrappedResult(success = it, throttlingDelay = throttlingDelay, mocked = true) }
            .firstOrNull()

    private fun processRealCall(chain: Interceptor.Chain, request: Request): WrappedResult {
        val startNs = System.nanoTime()
        val proceed = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        return WrappedResult(elapsedTime = tookMs, success = proceed,
                throttlingDelay = throttlingDelay)
    }
}

