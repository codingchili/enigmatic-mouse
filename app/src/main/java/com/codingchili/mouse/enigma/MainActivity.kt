package com.codingchili.mouse.enigma

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codingchili.mouse.enigma.presenter.FragmentSelector
import java.security.Security


/**
 * The Main activity.
 */
class MainActivity : AppCompatActivity() {

    init {
        Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
    }

    override fun onResume() {
        FragmentSelector.master()
        super.onResume()
    }

    override fun onBackPressed() {
        if (supportFragmentManager?.backStackEntryCount!! > 1) {
            supportFragmentManager?.popBackStack()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.bottom_app_bar))

        FragmentSelector.init(this)
        FragmentSelector.master()
    }
}
