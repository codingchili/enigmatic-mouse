package com.codingchili.mouse.enigma.presenter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.FaviconLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import org.spongycastle.util.encoders.Hex
import java.security.SecureRandom
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.AuditLogger


/**
 * Fragment for adding new credentials.
 */
internal class AddCredentialFragment: Fragment() {
    private val random : SecureRandom = SecureRandom()

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?): View?{
        val view: View = inflater.inflate(R.layout.fragment_add_credential,container,false)

        view.findViewById<FloatingActionButton>(R.id.cancel).setOnClickListener {
            Toast.makeText(context, "cancelled", Toast.LENGTH_SHORT).show()
            FragmentSelector.back()
        }

        view.findViewById<ImageView>(R.id.generate).setOnClickListener {
            val generated : String = generate()
            Toast.makeText(context, generated, Toast.LENGTH_SHORT).show()
            val password = view.findViewById<TextInputEditText>(R.id.password)

            password.setText(generated)

            if (activity != null) {
                val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.hideSoftInputFromWindow(password.rootView.windowToken, 0)
            }
        }

        view.findViewById<TextInputEditText>(R.id.website).setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                val edit : TextInputEditText = v as TextInputEditText

                val activity = activity
                FaviconLoader(context!!).load(edit.text.toString(), { bitmap ->
                    view.findViewById<ImageView>(R.id.logo).setImageBitmap(bitmap)

                    activity!!.runOnUiThread {
                        CredentialBank.onCacheUpdated()
                    }
                }, { exception ->
                    Log.w("AddCredentialFragment", exception?.message)
                })
            }
        }

        view.findViewById<TextInputEditText>(R.id.password).addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                val input = view.findViewById(R.id.password) as TextInputEditText
                val password = input.text.toString()
                val strength : TextView = view.findViewById(R.id.strength)

                when (password.length) {
                    in 1..10 -> {
                        strength.setText(R.string.password_weak)
                        strength.setTextColor(color(R.color.password_weak))
                    }
                    in 10..14 -> {
                        strength.setText(R.string.password_ok)
                        strength.setTextColor(color(R.color.password_ok))
                    }
                    in 14..999 -> {
                        strength.setTextColor(color(R.color.password_strong))
                        strength.setText(R.string.password_strong)
                    }
                    else -> {
                        strength.setText(R.string.password_generate_tap)
                        strength.setTextColor(color(R.color.text))
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        view.findViewById<Button>(R.id.save).setOnClickListener {
            var website : String  = view.findViewById<TextInputEditText>(R.id.website).text.toString()
            val username : String = view.findViewById<TextInputEditText>(R.id.username).text.toString()
            val password : String = view.findViewById<TextInputEditText>(R.id.password).text.toString()

            website = website.replace(Regex("https://|http://"), "")

            val credential = Credential(website, username, password)
            CredentialBank.store(credential)
            Toast.makeText(super.getContext(), "credentials saved.", Toast.LENGTH_SHORT).show()
            AuditLogger.onAddedCredential(context!!, credential)
            FragmentSelector.back()
        }
        return view
    }

    private fun color(res: Int): Int{
        return ContextCompat.getColor(context!!, res)
    }

    private fun generate() : String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        val generated : String = Hex.toHexString(bytes)
        val builder = StringBuilder()

        for (i in 0..3) {
            builder.append(generated.substring(i*4, (i*4)+4))
            if (i < 3) {
                builder.append("-")
            }
        }
        return builder.toString()
    }
}