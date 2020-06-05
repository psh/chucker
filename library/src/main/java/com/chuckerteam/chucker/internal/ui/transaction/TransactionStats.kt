package com.chuckerteam.chucker.internal.ui.transaction

import com.chuckerteam.chucker.internal.data.entity.HttpTransaction
import com.chuckerteam.chucker.internal.data.entity.HttpTransactionTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import kotlin.math.sqrt

internal class TransactionStats {
    private var stats: Map<HttpUrl, StatsHolder> = mutableMapOf()

    fun setData(httpTransactions: List<HttpTransactionTuple>) {
        CoroutineScope(Dispatchers.IO).launch {
            async {
                stats = computeStats(httpTransactions)
            }
        }
    }

    fun statsFor(tuple: HttpTransactionTuple): StatsHolder? =
        stats[urlKey(tuple.scheme, tuple.host, tuple.path)]

    fun statsFor(transaction: HttpTransaction): StatsHolder? =
        stats[urlKey(transaction.scheme, transaction.host, transaction.path)]

    private fun computeStats(data: List<HttpTransactionTuple>) : Map<HttpUrl, StatsHolder> {
        return data.groupBy { urlKey(it.scheme, it.host, it.path) }
            .mapValues { StatsHolder(it.value) }
            .toSortedMap(compareBy { it.toString() })
    }

    private fun urlKey(scheme: String?, host: String?, path: String?): HttpUrl {
        val partial = if (true == path?.contains("?")) path.split("?")[0] else path
        return HttpUrl.parse("$scheme://$host$partial")!!
    }

    fun isOutlier(tuple: HttpTransactionTuple): Boolean {
        return statsFor(tuple)?.isOutlier(tuple.tookMs) == true
    }

    class StatsHolder(points: List<HttpTransactionTuple>) {
        val min: Long?
        val max: Long?
        val q1: Long?
        val median: Long?
        val q3: Long?
        val iqr: Long?
        val average: Double?
        val sd: Double?
        val durations: List<Long>

        init {
            durations = points.mapNotNull { it.tookMs }.sorted()
            min = durations.min()
            max = durations.max()

            if (durations.size < 10) {
                average = null
                sd = null
                q1 = null
                median = null
                q3 = null
                iqr = null
            } else {
                average = durations.average()
                sd = durations.stdDev()
                q1 = durations.medianOf(0, durations.size / 2)
                median = durations.medianOf(0, durations.size)
                q3 = durations.medianOf(durations.size / 2, durations.size)
                iqr = if (q1 != null && q3 != null) q3 - q1 else null
            }
        }

        fun isOutlier(tookMs: Long?): Boolean =
            if (durations.isEmpty() || average == null || sd == null || tookMs == null) {
                false
            } else {
                // Calculate the Z-score for this observation.  A value > 2 represents
                // an outlier, as (0.0 < Z-score < 2.0) represents 95% of the
                // population if normally distributed.
                (tookMs.toDouble() - average) / sd > 2.0
            }

        private fun List<Long>.stdDev(): Double {
            val sum = this.map { it.toDouble() }.sum()
            val sumSq = this.map { it.toDouble() * it.toDouble() }.sum()
            val count = this.count().toDouble()
            return sqrt(sumSq / count - sum * sum / count / count)
        }

        private fun List<Long>.medianOf(lowerBound: Int, upperBound: Int): Long? {
            val sampleSize = upperBound - lowerBound
            val middle = sampleSize / 2
            return when {
                this.isEmpty() -> null
                sampleSize % 2 == 1 -> this[lowerBound + middle]
                else -> ((this[lowerBound + middle - 1] + this[lowerBound + middle]) / 2.0).toLong()
            }
        }
    }
}
