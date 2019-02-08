package com.readystatesoftware.chuck.api

import androidx.annotation.IntDef

class NetworkThrottling {
    companion object {
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
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(None, OneSecond, TwoSeconds, FiveSeconds, TenSeconds, FifteenSeconds,
            TwentySeconds, ThirtySeconds, OneMinute, TenMinutes)
    annotation class ThrottlingDelay
}