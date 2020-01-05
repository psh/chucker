package com.chuckerteam.chucker.sample

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom

private const val BASE_URL = "https://httpbin.org"

@Suppress("TooManyFunctions")
class HttpBinApi(private val api: HttpClient) {
    suspend fun status(code: Int) = get("/status/$code")
    suspend fun stream(lines: Int) = get("/stream/${lines}")
    suspend fun streamBytes(bytes: Int) = get("/stream-bytes/${bytes}")
    suspend fun delay(seconds: Int) = get("/delay/${seconds}")
    suspend fun redirect(times: Int) = get("/redirect/${times}")
    suspend fun redirectRelative(times: Int) = get("/relative-redirect/${times}")
    suspend fun redirectAbsolute(times: Int) = get("/absolute-redirect/${times}")
    suspend fun gzip() = get("/gzip")
    suspend fun xml() = get("/xml")
    suspend fun utf8() = get("/encoding/utf8")
    suspend fun deflate() = get("/deflate")
    suspend fun deny() = get("/deny")
    suspend fun cache(seconds: Int) = get("/cache/${seconds}")
    suspend fun basicAuth(user: String, passwd: String) = get("/basic-auth/${user}/${passwd}")

    suspend fun bearer(token: String) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/bearer")
            }
            header("Authorization", token)
        }

    suspend fun redirectTo(value: String) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/redirect-to")
            }
            parameter("url", value)
        }

    suspend fun image(accept: String) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/image")
            }
            header("Accept", accept)
        }

    suspend fun cookieSet(value: String) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/cookies/set")
            }
            parameter("k1", value)
        }

    suspend fun drip(bytes: Int, seconds: Int, delay: Int, code: Int) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/drip")
            }
            parameter("numbytes", bytes)
            parameter("duration", seconds)
            parameter("delay", delay)
            parameter("code", code)
        }

    suspend fun cache(ifModifiedSince: String) =
        api.get<HttpResponse> {
            url {
                takeFrom("$BASE_URL/cache")
            }
            header("If-Modified-Since", ifModifiedSince)
        }

    suspend fun get(path: String) =
        api.get<HttpResponse>("$BASE_URL${path}") {}

    suspend fun post(path: String, data: Data) =
        api.post<HttpResponse> {
            url.takeFrom("$BASE_URL${path}")
            contentType(ContentType.Application.Json)
            body = data
        }

    suspend fun patch(path: String, data: Data) =
        api.patch<HttpResponse> {
            url.takeFrom("$BASE_URL${path}")
            contentType(ContentType.Application.Json)
            body = data
        }

    suspend fun put(path: String, data: Data) =
        api.put<HttpResponse> {
            url.takeFrom("$BASE_URL${path}")
            contentType(ContentType.Application.Json)
            body = data
        }

    suspend fun delete(path: String) =
        api.delete<HttpResponse>("$BASE_URL${path}") {}

    class Data(val thing: String)
}

