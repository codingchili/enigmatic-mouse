package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.FaviconLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.security.Security


/**
 * @author Robin Duda
 */
class MainActivity : AppCompatActivity() {
    private val bank: CredentialBank = CredentialBank()

    init {
        Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Toolbar>(R.id.bottom_app_bar).setOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        val button = findViewById<FloatingActionButton>(R.id.add_pw)
        val list = findViewById<ListView>(R.id.list_pw)
        val adapter = object : ArrayAdapter<Credential>(this, R.layout.user_list, bank.retrieve()) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var view: View? = convertView

                if (convertView == null) {
                    view = layoutInflater.inflate(R.layout.user_list, parent, false) as View
                }

                FaviconLoader(applicationContext).load(bank.retrieve()[position].url, { bitmap ->
                    val imageView: ImageView = view?.findViewById(R.id.site_logo) as ImageView
                    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imageView.setImageBitmap(bitmap)
                }, { exception ->
                    Toast.makeText(super.getContext(), exception.message, Toast.LENGTH_SHORT).show()
                })

                view?.findViewById<TextView>(R.id.url)?.text = bank.retrieve()[position].url
                view?.findViewById<TextView>(R.id.username)?.text = bank.retrieve()[position].username

                return view
            }
        }

        list.adapter = adapter

        button.setOnClickListener {
            bank.store(Credential("https://facebook.com/", "some_user", "some_pass"))
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "added user to bank.", Toast.LENGTH_SHORT).show()
        }

        list.setOnItemClickListener { _: AdapterView<*>?, view: View?, position: Int, _: Long ->
            // do something.
        }
    }
}
