package com.chuckerteam.chucker.sample

import android.content.Context
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.chuckerteam.chucker.sample.HttpBinApi.Data
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class HttpBinClient(
    context: Context
) {

    private val collector = ChuckerCollector(
        context = context,
        showNotification = true,
        retentionPeriod = RetentionManager.Period.ONE_HOUR
    )

    private val chuckerInterceptor = ChuckerInterceptor(
        context = context,
        collector = collector,
        maxContentLength = 250000L
    )

private val httpClient = HttpClient(OkHttp) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
    engine {
        addInterceptor(chuckerInterceptor)
        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        config {
            followRedirects(true)
            readTimeout(30, TimeUnit.SECONDS)
            connectTimeout(30, TimeUnit.SECONDS)
            callTimeout(30, TimeUnit.SECONDS)
        }
    }
}

    @Suppress("MagicNumber")
    internal fun doHttpActivity() = GlobalScope.launch {
        val api = HttpBinApi(httpClient)

        async { api.get("/get") }
        async { api.post("/post", Data("A String")) }
        async { api.patch("/patch", Data("patched")) }
        async { api.put("/put", Data("put")) }
        async { api.delete("/delete") }
        async { api.status(201) }
        async { api.status(401) }
        async { api.status(500) }
        async { api.delay(9) }
        async { api.delay(15) }
        async { api.redirectTo("https://http2.akamai.com") }
        async { api.redirect(3) }
        async { api.redirectRelative(2) }
        async { api.redirectAbsolute(4) }
        async { api.stream(500) }
        async { api.streamBytes(2048) }
        async { api.image("image/png") }
        async { api.gzip() }
        async { api.xml() }
        async { api.utf8() }
        async { api.deflate() }
        async { api.cookieSet("v") }
        async { api.basicAuth("me", "pass") }
        async { api.drip(512, 5, 1, 200) }
        async { api.deny() }
        async { api.cache("Mon") }
        async { api.cache(30) }
    }

    internal fun initializeCrashHandler() {
        Chucker.registerDefaultCrashHandler(collector)
    }

    internal fun recordException() {
        collector.onError("Example button pressed", RuntimeException("User triggered the button"))
        // You can also throw exception, it will be caught thanks to "Chucker.registerDefaultCrashHandler"
        // throw new RuntimeException("User triggered the button");
    }

}
