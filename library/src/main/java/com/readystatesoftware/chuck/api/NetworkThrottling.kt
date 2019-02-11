package com.readystatesoftware.chuck.api

import androidx.annotation.IntDef

class NetworkThrottling {
    companion object {
        fun indexOf(@ThrottlingDelay throttlingDelay: Int): Int = all.indexOf(throttlingDelay)

        @ThrottlingDelay
        fun toDelay(index: Int): Int = all[index]

        fun humanReadable(@ThrottlingDelay throttlingDelay: Int) = desc[indexOf(throttlingDelay)]

        const val None = 0
        const val OneSecond = 1
        const val TwoSeconds = 2
        const val FiveSeconds = 5
        const val TenSeconds = 10
        const val FifteenSeconds = 15
        const val TwentySeconds = 20
        const val ThirtySeconds = 30
        const val OneMinute = 60
        const val TenMinutes = 600

        private val all: List<Int> = listOf(None, OneSecond, TwoSeconds, FiveSeconds,
                TenSeconds, FifteenSeconds, TwentySeconds, ThirtySeconds,
                OneMinute, TenMinutes)
        private val desc: List<String> = listOf("None", "1 Second", "2 Seconds", "5 Seconds",
                "10 Seconds", "15 Seconds", "20 Seconds", "30 Seconds",
                "1 Minute", "10 Minutes")
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(None, OneSecond, TwoSeconds, FiveSeconds, TenSeconds, FifteenSeconds,
            TwentySeconds, ThirtySeconds, OneMinute, TenMinutes)
    annotation class ThrottlingDelay
}