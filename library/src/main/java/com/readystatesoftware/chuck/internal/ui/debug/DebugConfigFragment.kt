package com.readystatesoftware.chuck.internal.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.api.ChuckInterceptor
import com.readystatesoftware.chuck.api.NetworkThrottling

class DebugConfigFragment : Fragment() {
    companion object {
        fun newInstance() = DebugConfigFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_debug, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.throttlingDelayValue).text = NetworkThrottling.humanReadable(ChuckInterceptor.processor.throttlingDelay)
        view.findViewById<SeekBar>(R.id.throttlingDelayEditor).progress = NetworkThrottling.indexOf(ChuckInterceptor.processor.throttlingDelay)
        view.findViewById<SeekBar>(R.id.throttlingDelayEditor).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ChuckInterceptor.processor.throttlingDelay = NetworkThrottling.toDelay(progress)
                view.findViewById<TextView>(R.id.throttlingDelayValue).text = NetworkThrottling.humanReadable(ChuckInterceptor.processor.throttlingDelay)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }
}