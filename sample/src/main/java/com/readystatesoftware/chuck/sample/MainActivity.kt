package com.readystatesoftware.chuck.sample

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.readystatesoftware.chuck.api.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var collector: ChuckCollector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        do_http.setOnClickListener { doHttpActivity() }
        trigger_exception.setOnClickListener { triggerException() }

        with(launch_chucker_directly) {
            visibility = if (Chuck.isOp()) View.VISIBLE else View.GONE
            setOnClickListener { launchChuckDirectly() }
        }

        val badRequest = MockedResponse("bad_request", "Faked bad request", 400, MediaType.parse("application/json"), "{\"bad\": true}", listOf("bad/request"))
        val missing = MockedResponse("missing", "Fake missing", 404, MediaType.parse("application/json"), "", listOf("something"))
        ChuckInterceptor.registerMockedResponses(badRequest, missing)
        ChuckInterceptor.activateMockResponse(badRequest)
        ChuckInterceptor.throttlingDelay(NetworkThrottling.FiveSeconds)

        collector = ChuckCollector(this)
                .showNotification(true)
                .retentionManager(RetentionManager(this, ChuckCollector.Period.ONE_HOUR))

        Chuck.registerDefaultCrashHanlder(collector)
    }

    private fun getClient(context: Context): OkHttpClient {
        val chuckInterceptor = ChuckInterceptor(context, collector)
                .maxContentLength(250000L);

        return OkHttpClient.Builder()
                // Add a ChuckInterceptor instance to your OkHttp client
                .addInterceptor(chuckInterceptor)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build()
    }

    private fun launchChuckDirectly() {
        // Optionally launch Chuck directly from your own app UI
        startActivity(Chuck.getLaunchIntent(this, Chuck.SCREEN_HTTP))
    }

    private fun doHttpActivity() {
        val cb = object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {}
            override fun onFailure(call: Call<Void>, t: Throwable) {
                t.printStackTrace()
            }
        }

        with(SampleApiService.getInstance(getClient(this))) {
            get().enqueue(cb)
            bad().enqueue(cb)
            post(SampleApiService.Data(null)).enqueue(cb)
            post(SampleApiService.Data("posted")).enqueue(cb)
            patch(SampleApiService.Data("patched")).enqueue(cb)
            put(SampleApiService.Data("put")).enqueue(cb)
            delete().enqueue(cb)
            status(201).enqueue(cb)
            status(401).enqueue(cb)
            status(500).enqueue(cb)
            delay(9).enqueue(cb)
            delay(15).enqueue(cb)
            redirectTo("https://http2.akamai.com").enqueue(cb)
            redirect(3).enqueue(cb)
            redirectRelative(2).enqueue(cb)
            redirectAbsolute(4).enqueue(cb)
            stream(500).enqueue(cb)
            streamBytes(2048).enqueue(cb)
            image("image/png").enqueue(cb)
            gzip().enqueue(cb)
            xml().enqueue(cb)
            utf8().enqueue(cb)
            deflate().enqueue(cb)
            cookieSet("v").enqueue(cb)
            basicAuth("me", "pass").enqueue(cb)
            drip(512, 5, 1, 200).enqueue(cb)
            deny().enqueue(cb)
            cache("Mon").enqueue(cb)
            cache(30).enqueue(cb)
        }
    }

    private fun triggerException() {
        collector.onError("Example button pressed", RuntimeException("User triggered the button"))
        // You can also throw exception, it will be caught thanks to "Chuck.registerDefaultCrashHanlder"
        // throw new RuntimeException("User triggered the button");
    }
}