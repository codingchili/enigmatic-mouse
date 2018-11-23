package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
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
            Toast.makeText(super.getContext(), "cancelled", Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<Button>(R.id.save).setOnClickListener {
            var website : String  = view.findViewById<TextInputEditText>(R.id.website).text.toString()
            val username : String = view.findViewById<TextInputEditText>(R.id.username).text.toString()
            val password : String = view.findViewById<TextInputEditText>(R.id.password).text.toString()

            if (!website.startsWith("http")) {
                website = "https://$website/"
            }

            bank.store(Credential(website, username, password))
            Toast.makeText(super.getContext(), "credentials saved.", Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

}

