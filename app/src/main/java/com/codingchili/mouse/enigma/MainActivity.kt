package com.codingchili.mouse.enigma

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.Security


/**
 * @author Robin Duda
 */
class MainActivity : AppCompatActivity() {

    init {
        Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext, "stack " + supportFragmentManager.backStackEntryCount, Toast.LENGTH_SHORT).show()
        if (supportFragmentManager?.backStackEntryCount!! > 1) {
            supportFragmentManager?.popBackStack()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.root, CredentialListFragment())
                .addToBackStack("list")
                .commit()
    }
}
