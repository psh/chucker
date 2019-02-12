package com.readystatesoftware.chuck.internal.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.api.ChuckInterceptor
import com.readystatesoftware.chuck.internal.debug.DebuggingChainProcessor

class DebugConfigFragment : Fragment() {
    companion object {
        fun newInstance() = DebugConfigFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.debug_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.config_recycler)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = DebugConfigAdapter().apply { initFrom(ChuckInterceptor.processor) }
    }
}