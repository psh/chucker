package com.chuckerteam.chucker.internal.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.ViewModelProvider
import com.chuckerteam.chucker.api.Chucker
import com.chuckerteam.chucker.databinding.ChuckerActivityMainBinding
import com.chuckerteam.chucker.internal.ui.throwable.ThrowableActivity
import com.chuckerteam.chucker.internal.ui.throwable.ThrowableAdapter
import com.chuckerteam.chucker.internal.ui.transaction.TransactionActivity
import com.chuckerteam.chucker.internal.ui.transaction.TransactionAdapter
import com.google.android.material.tabs.TabLayout

internal class MainActivity :
    BaseChuckerActivity(),
    TransactionAdapter.TransactionClickListListener,
    ThrowableAdapter.ThrowableClickListListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var mainBinding: ChuckerActivityMainBinding

    private val applicationName: CharSequence
        get() = applicationInfo.loadLabel(packageManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mainBinding = ChuckerActivityMainBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            setSupportActionBar(toolbar)
            toolbar.subtitle = applicationName
            viewPager.adapter = HomePageAdapter(this@MainActivity, supportFragmentManager)
            tabLayout.setupWithViewPager(viewPager)
            viewPager.addOnPageChangeListener(
                object : TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        if (position == HomePageAdapter.SCREEN_HTTP_INDEX) {
                            Chucker.dismissTransactionsNotification(this@MainActivity)
                        } else {
                            Chucker.dismissErrorsNotification(this@MainActivity)
                        }
                    }
                }
            )
        }

        consumeIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeIntent(intent)
    }

    private var lastKeyTime = 0L
    private val keycodes: Map<Int, String> = mapOf(
        KeyEvent.KEYCODE_MINUS to "-",
        KeyEvent.KEYCODE_PERIOD to ".",
        KeyEvent.KEYCODE_0 to "0",
        KeyEvent.KEYCODE_1 to "1",
        KeyEvent.KEYCODE_2 to "2",
        KeyEvent.KEYCODE_3 to "3",
        KeyEvent.KEYCODE_4 to "4",
        KeyEvent.KEYCODE_5 to "5",
        KeyEvent.KEYCODE_6 to "6",
        KeyEvent.KEYCODE_7 to "7",
        KeyEvent.KEYCODE_8 to "8",
        KeyEvent.KEYCODE_9 to "9",
        KeyEvent.KEYCODE_A to "A",
        KeyEvent.KEYCODE_B to "B",
        KeyEvent.KEYCODE_C to "C",
        KeyEvent.KEYCODE_D to "D",
        KeyEvent.KEYCODE_E to "E",
        KeyEvent.KEYCODE_F to "F",
        KeyEvent.KEYCODE_G to "G",
        KeyEvent.KEYCODE_H to "H",
        KeyEvent.KEYCODE_I to "I",
        KeyEvent.KEYCODE_J to "J",
        KeyEvent.KEYCODE_K to "K",
        KeyEvent.KEYCODE_L to "L",
        KeyEvent.KEYCODE_M to "M",
        KeyEvent.KEYCODE_N to "N",
        KeyEvent.KEYCODE_O to "O",
        KeyEvent.KEYCODE_P to "P",
        KeyEvent.KEYCODE_Q to "Q",
        KeyEvent.KEYCODE_R to "R",
        KeyEvent.KEYCODE_S to "S",
        KeyEvent.KEYCODE_T to "T",
        KeyEvent.KEYCODE_U to "U",
        KeyEvent.KEYCODE_V to "V",
        KeyEvent.KEYCODE_W to "W",
        KeyEvent.KEYCODE_X to "X",
        KeyEvent.KEYCODE_Y to "Y",
        KeyEvent.KEYCODE_Z to "Z"
    )
    private val input: StringBuilder = StringBuilder()

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        input.append(keycodes[keyCode])
        lastKeyTime = System.currentTimeMillis()
        return true
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastKeyTime < 250 && event?.keyCode == KeyEvent.KEYCODE_ENTER) {
            if (input.isNotEmpty()) {
                Log.d("@@@@", "Barcode scanner input = $input")
                input.setLength(0)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Scroll to the right tab.
     */
    private fun consumeIntent(intent: Intent) {
        // Get the screen to show, by default => HTTP
        val screenToShow = intent.getIntExtra(EXTRA_SCREEN, Chucker.SCREEN_HTTP)
        mainBinding.viewPager.currentItem = if (screenToShow == Chucker.SCREEN_HTTP) {
            HomePageAdapter.SCREEN_HTTP_INDEX
        } else {
            HomePageAdapter.SCREEN_THROWABLE_INDEX
        }
    }

    override fun onThrowableClick(throwableId: Long, position: Int) {
        ThrowableActivity.start(this, throwableId)
    }

    override fun onTransactionClick(transactionId: Long, position: Int) {
        TransactionActivity.start(this, transactionId)
    }

    companion object {
        const val EXTRA_SCREEN = "EXTRA_SCREEN"
    }
}
