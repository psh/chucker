package com.readystatesoftware.chuck.internal.ui.debug

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.internal.debug.DebuggingChainProcessor

class DebugConfigAdapter : ListAdapter<ConfigItem, ConfigItemViewHolder>(ConfigItemDiff()) {

    fun initFrom(chainProcessor: DebuggingChainProcessor) {
        val data = mutableListOf<ConfigItem>()
        data.add(ThrottlingHeaderItem())
        data.add(ThrottlingModel(chainProcessor))
        if (chainProcessor.activeMockedResponses.isNotEmpty()) {
            data.add(ActiveMocksHeader())
            chainProcessor.activeMockedResponses.forEach { data.add(ActiveMock(it)) }
        }
        val inactive = chainProcessor.allMockedResponses.subtract(chainProcessor.activeMockedResponses)
        if (inactive.isNotEmpty()) {
            data.add(InactiveMocksHeader())
            inactive.forEach { data.add(InactiveMock(it)) }
        }
        submitList(data)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            THROTTLING_HEADER_TYPE -> ThrottlingHeaderViewHolder(inflater.inflate(R.layout.debug_config_throttling_header, parent, false))
            THROTTLING_EDITOR_TYPE -> ThrottlingEditorViewHolder(inflater.inflate(R.layout.debug_config_throttling_editor, parent, false))
            ACTIVE_MOCKS_HEADER_TYPE -> MockHeaderViewHolder(inflater.inflate(R.layout.debug_config_mock_header, parent, false), true)
            ACTIVE_MOCKS_TYPE -> MockResponseViewHolder(inflater.inflate(R.layout.debug_config_mock_response, parent, false), true)
            INACTIVE_MOCKS_HEADER_TYPE -> MockHeaderViewHolder(inflater.inflate(R.layout.debug_config_mock_header, parent, false), false)
            else -> MockResponseViewHolder(inflater.inflate(R.layout.debug_config_mock_response, parent, false), false)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onBindViewHolder(holder: ConfigItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ConfigItemDiff : DiffUtil.ItemCallback<ConfigItem>() {
    override fun areItemsTheSame(oldItem: ConfigItem, newItem: ConfigItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ConfigItem, newItem: ConfigItem): Boolean = oldItem == newItem
}

