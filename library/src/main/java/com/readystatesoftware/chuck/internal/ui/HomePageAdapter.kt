package com.readystatesoftware.chuck.internal.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.readystatesoftware.chuck.R
import com.readystatesoftware.chuck.internal.ui.debug.DebugConfigFragment
import com.readystatesoftware.chuck.internal.ui.error.ErrorListFragment
import com.readystatesoftware.chuck.internal.ui.transaction.TransactionListFragment

/**
 * @author Olivier Perez
 */
internal class HomePageAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int = 3

    override fun getItem(position: Int): Fragment? = when (position) {
        SCREEN_HTTP_INDEX -> TransactionListFragment.newInstance()
        SCREEN_DEBUG_INDEX -> DebugConfigFragment.newInstance()
        else -> ErrorListFragment.newInstance()
    }

    override fun getPageTitle(position: Int): CharSequence? = context.getString(when (position) {
        SCREEN_HTTP_INDEX -> R.string.chuck_tab_network
        SCREEN_DEBUG_INDEX -> R.string.chuck_tab_debug
        else -> R.string.chuck_tab_errors
    })

    companion object {
        const val SCREEN_HTTP_INDEX = 0
        const val SCREEN_ERROR_INDEX = 1
        const val SCREEN_DEBUG_INDEX = 2
    }
}
