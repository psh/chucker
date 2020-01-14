package com.chuckerteam.chucker.sample

import android.content.Context
import com.chuckerteam.chucker.api.RetentionManager

class ChuckerFeatureConfig {
    var chuckerContext: Context? = null
    var showNotification: Boolean = true
    var retentionPeriod: RetentionManager.Period =
        RetentionManager.Period.ONE_WEEK
    var maxContentLength: Long = 250000L
    var headersToRedact: MutableSet<String> = mutableSetOf()
}