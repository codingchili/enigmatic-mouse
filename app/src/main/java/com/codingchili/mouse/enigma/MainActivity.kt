package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
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
        /*val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)*/

        val button = findViewById<FloatingActionButton>(R.id.add_pw)
        val list = findViewById<ListView>(R.id.list_pw)
        val adapter = object : ArrayAdapter<Credential>(this, android.R.layout.simple_list_item_2, bank.retrieve()) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var view: TwoLineListItem? = convertView as TwoLineListItem?

                if (convertView == null) {
                    view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false) as TwoLineListItem
                }

                view?.findViewById<TextView>(android.R.id.text1)?.text = bank.retrieve()[position].url
                view?.findViewById<TextView>(android.R.id.text2)?.text = bank.retrieve()[position].username

                return view
            }
        }

        list.adapter = adapter

        button.setOnClickListener {

            // todo: show dialog here.
            bank.store(Credential("some_url", "some_user", "some_pass"))
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "added user to bank.", Toast.LENGTH_SHORT).show()
        }

        list.setOnItemClickListener{ _: AdapterView<*>?, _: View?, position: Int, _: Long ->

            // todo: show dialog here for master password.
            // on master password OK - show pass details and store master password until app closes.
            // display last read, created, username, url as clickable link - open browser,
            // password as clickable - copy to clipboard !!INSECURE!!

            Toast.makeText(this, bank.retrieve()[position].username, Toast.LENGTH_SHORT).show()
        }
    }
}
