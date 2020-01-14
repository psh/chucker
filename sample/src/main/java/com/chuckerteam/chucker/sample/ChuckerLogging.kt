package com.chuckerteam.chucker.sample

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.features.observer.ResponseHandler
import io.ktor.client.features.observer.ResponseObserver
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.HttpResponsePipeline
import io.ktor.util.AttributeKey

object ChuckerLogging : HttpClientFeature<ChuckerFeatureConfig, ChuckerHttpLogger> {
    override val key: AttributeKey<ChuckerHttpLogger> = AttributeKey("ChuckerLogging")

    override fun prepare(block: ChuckerFeatureConfig.() -> Unit): ChuckerHttpLogger {
        return ChuckerHttpLogger(ChuckerFeatureConfig().apply(block))
    }

    override fun install(feature: ChuckerHttpLogger, scope: HttpClient) {
        scope.sendPipeline.intercept(HttpSendPipeline.Before) {
            try {
                feature.logRequest(context)
            } catch (_: Throwable) {
            }

            try {
                proceedWith(subject)
            } catch (cause: Throwable) {
                feature.logRequestException(context, cause)
                throw cause
            }
        }

        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) {
            try {
                proceedWith(subject)
            } catch (cause: Throwable) {
                feature.logResponseException(context, cause)
                throw cause
            }
        }

        val observer: ResponseHandler = { it: HttpResponse ->
            try {
                feature.logResponse(it)
            } catch (_: Throwable) {
            }
        }

        ResponseObserver.install(ResponseObserver(observer), scope)
    }
}