package com.readystatesoftware.chuck.internal.debug

import okhttp3.Response
import java.io.IOException

data class WrappedResult @JvmOverloads constructor(
        val elapsedTime: Long = 0,
        val success: Response? = null,
        val throttlingDelay: Int = 0,
        val error: IOException? = null,
        val mocked: Boolean = false
)