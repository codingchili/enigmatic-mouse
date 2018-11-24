package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.FaviconLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

internal class AddCredentialFragment: Fragment() {
    private lateinit var bank: CredentialBank

    fun setBank(bank: CredentialBank): AddCredentialFragment {
        this.bank = bank
        return this
    }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?): View?{
        val view: View = inflater.inflate(R.layout.fragment_add_credential,container,false)

        view.findViewById<FloatingActionButton>(R.id.cancel).setOnClickListener {
            Toast.makeText(context, "cancelled", Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<TextInputEditText>(R.id.website).setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val edit : TextInputEditText = v as TextInputEditText
                val loader = FaviconLoader(context!!)

                loader.load(toUrl(edit.text.toString()), { bitmap ->
                    view.findViewById<ImageView>(R.id.logo).setImageBitmap(bitmap)
                }, { exception ->
                    // failed to load image - don't care.
                })
            }
        }

        view.findViewById<Button>(R.id.save).setOnClickListener {
            var website : String  = view.findViewById<TextInputEditText>(R.id.website).text.toString()
            val username : String = view.findViewById<TextInputEditText>(R.id.username).text.toString()
            val password : String = view.findViewById<TextInputEditText>(R.id.password).text.toString()

            website = toUrl(website)

            bank.store(Credential(website, username, password))
            Toast.makeText(super.getContext(), "credentials saved.", Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

    private fun toUrl(hostname: String): String {
        return if (!hostname.startsWith("http")) {
            "https://$hostname/"
        } else {
            hostname
        }
    }

}

