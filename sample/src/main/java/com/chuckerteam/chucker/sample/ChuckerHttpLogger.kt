package com.chuckerteam.chucker.sample

import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.internal.data.entity.HttpHeader
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.response.HttpResponse
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.util.AttributeKey
import java.util.concurrent.TimeUnit

class ChuckerHttpLogger(config: ChuckerFeatureConfig) {

    private val collector = ChuckerCollector(
        context = config.chuckerContext!!,
        showNotification = config.showNotification,
        retentionPeriod = config.retentionPeriod
    )

    private fun bodyHasSupportedEncoding(contentEncoding: String?) =
        contentEncoding == null ||
                contentEncoding.isEmpty() ||
                contentEncoding.equals("identity", ignoreCase = true) ||
                contentEncoding.equals("gzip", ignoreCase = true)

    private fun bodyIsGzipped(contentEncoding: String?) =
        contentEncoding?.equals("gzip", ignoreCase = true) ?: false

    fun logRequest(build: HttpRequestBuilder) {
        val value = HttpTransaction().apply {
            requestDate = System.currentTimeMillis()
            method = build.method.value
            populateUrl(build.url.build().toString())
            val headers = build.headers.build()
            setRequestHeaders(mutableListOf<HttpHeader>().apply {
                headers.forEach { name, values ->
                    add(HttpHeader(name, values[0]))
                }
            })
            requestContentType = build.contentType()?.contentType
            requestContentLength = build.contentLength()
            isRequestBodyPlainText = bodyHasSupportedEncoding(headers["Content-Encoding"])

            // Need to pull the full request body if there is one...
        }
        build.attributes.put(AttributeKey("TransactionKey"), value)
        build.attributes.put(AttributeKey("TransactionStart"), System.nanoTime())
        collector.onRequestSent(value)
    }

    @Suppress("UNCHECKED_CAST")
    fun logResponse(response: HttpResponse) {
        val key = response.call.attributes.allKeys.firstOrNull { it.name == "TransactionKey" }
        val took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() -
                (response.call.attributes[response.call.attributes.allKeys.firstOrNull { it.name == "TransactionStart" } as AttributeKey<Long>]))
        key?.let {
            val transaction = response.call.attributes[it as AttributeKey<HttpTransaction>].apply {
                responseDate = System.currentTimeMillis()
                tookMs = took
                protocol = response.call.request.url.protocol.name
                responseCode = response.status.value
                responseMessage = response.status.description

                // Need to pull the full response body if there is one...
//                responseContentType = responseBody?.contentType()?.toString()
//                responseContentLength = responseBody?.contentLength() ?: 0L
//                setResponseHeaders(filterHeaders(response.headers()))
            }
            collector.onResponseReceived(transaction)
        }
    }

    fun logRequestException(builder: HttpRequestBuilder, cause: Throwable) {

    }

    fun logResponseException(clientCall: HttpClientCall, cause: Throwable) {

    }

}