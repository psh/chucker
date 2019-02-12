package com.readystatesoftware.chuck.api

import okhttp3.*

class MockedResponse(
        internal val id: String,
        private val description: String,
        private val responseCode: Int = 200,
        private val mediaType: MediaType? = MediaType.parse("application/json"),
        private val body: String = "",
        private val urlParts: List<String> = listOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MockedResponse

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return description
    }

    fun isMatch(encodedPath: String) =
            urlParts.isEmpty() || urlParts.count { encodedPath.contains(it) } > 0

    fun toResponse(request: Request): Response = Response.Builder()
            .body(ResponseBody.create(mediaType, body))
            .code(responseCode)
            .request(request)
            .message("MOCKED: $description")
            .protocol(Protocol.HTTP_2)
            .build()
}