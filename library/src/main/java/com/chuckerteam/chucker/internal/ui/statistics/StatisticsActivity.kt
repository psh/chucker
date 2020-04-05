package com.chuckerteam.chucker.internal.ui.statistics

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.databinding.ChuckerActivityStatisticsBinding
import com.chuckerteam.chucker.internal.ui.BaseChuckerActivity

internal class StatisticsActivity : BaseChuckerActivity() {
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var statisticsBinding: ChuckerActivityStatisticsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statisticsBinding = ChuckerActivityStatisticsBinding.inflate(layoutInflater)

        viewModel = ViewModelProvider(this).get(StatisticsViewModel::class.java)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
}