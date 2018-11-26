package com.codingchili.mouse.enigma

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.FaviconLoader
import java.time.format.DateTimeFormatter


class CredentialInfoFragment: Fragment() {
    private lateinit var credential : Credential

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    fun setCredential(credential: Credential): Fragment {
        this.credential = credential
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?): View?{
        val view: View = inflater.inflate(R.layout.fragment_credential_info,container,false)

        view.findViewById<View>(R.id.open_website).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(credential.site)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<View>(R.id.password_copy).setOnClickListener {
            copyToClipboard(credential.password)
            // todo: expire clipboard?
        }

        view.findViewById<View>(R.id.password_show).setOnClickListener {
            Toast.makeText(context, credential.password, Toast.LENGTH_SHORT).show()
        }

        view.findViewById<TextView>(R.id.created_at_date).text = credential.created
        view.findViewById<TextView>(R.id.username).text = credential.username
        view.findViewById<TextView>(R.id.website).text = credential.site

        FaviconLoader(activity!!.applicationContext).get(credential.site, { bitmap ->
            view.findViewById<ImageView>(R.id.logo).setImageBitmap(bitmap)
        }, {
            // no image available.
        })

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboard : ClipboardManager? = (context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)
        if (clipboard != null) {
            val clip = ClipData.newPlainText("pwd", text)
            clipboard.primaryClip = clip
            Toast.makeText(context, getString(R.string.clipboard_success), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, getString(R.string.clipboard_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
