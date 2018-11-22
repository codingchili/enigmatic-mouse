package com.codingchili.mouse.enigma

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        findViewById<Toolbar>(R.id.bottom_app_bar).setOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment, CredentialListFragment())
                .addToBackStack("list")
                .commit()

        val button = findViewById<FloatingActionButton>(R.id.add_pw)

        button.setOnClickListener {

            findViewById<FloatingActionButton>(R.id.add_pw).setImageResource(R.drawable.add_icon_simple)



            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment, AddCredentialFragment())
                    .addToBackStack("add")
                    .commit()
        }
    }
}
