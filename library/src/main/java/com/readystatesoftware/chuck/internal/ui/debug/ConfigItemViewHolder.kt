package com.readystatesoftware.chuck.internal.ui.debug

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ConfigItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
    abstract fun bind(item: ConfigItem)
}

class MockResponseViewHolder(itemView: View?, private val active: Boolean) : ConfigItemViewHolder(itemView) {
    override fun bind(item: ConfigItem) {
    }
}

class MockHeaderViewHolder(itemView: View?, private val active: Boolean) : ConfigItemViewHolder(itemView) {
    override fun bind(item: ConfigItem) {
    }
}

class ThrottlingEditorViewHolder(itemView: View?) : ConfigItemViewHolder(itemView) {
    override fun bind(item: ConfigItem) {
    }
}

class ThrottlingHeaderViewHolder(itemView: View?) : ConfigItemViewHolder(itemView) {
    override fun bind(item: ConfigItem) {
    }
}

