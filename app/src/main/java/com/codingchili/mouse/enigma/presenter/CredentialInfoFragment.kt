package com.codingchili.mouse.enigma.presenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.FaviconLoader
import com.codingchili.mouse.enigma.model.MousePreferences
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * Fragment that shows information about a single credential.
 */
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

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.credential_info, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?): View?{
        val view: View = inflater.inflate(R.layout.fragment_credential_info, container,false)

        val toolbar = view.findViewById<Toolbar>(R.id.bottom_app_bar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(activity?.supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.copy -> {
                    val preferences = MousePreferences(activity!!.application)

                    if (preferences.isClipboardWarningShown()) {
                        copyToClipboard(credential.password) // todo: expire clipboard?
                    } else {
                        FragmentSelector.clipboardWarningDialog {
                            copyToClipboard(credential.password)
                            preferences.setClipboardWarned(true)
                        }
                    }
                }
                R.id.show -> {
                    Toast.makeText(context, credential.password, Toast.LENGTH_SHORT).show()
                }
                R.id.launch -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://${credential.domain}")
                    startActivity(intent)
                }
            }
            true
        }

        view.findViewById<FloatingActionButton>(R.id.remove_credential).setOnClickListener {
            Log.w("wow", "HEY HEY CANCEL THIS !")
            FragmentSelector.removeCredentialDialog(credential)
        }

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<TextView>(R.id.created_at_date).text = credential.created
        view.findViewById<TextView>(R.id.header).text = String.format(
                getString(R.string.credential_header,
                        credential.username,
                        credential.domain))

        if (credential.pwned) {
            view.findViewById<TextView>(R.id.breach_info).text = Html.fromHtml(credential.pwnedDescription, Html.FROM_HTML_MODE_COMPACT)
            val header = view.findViewById<TextView>(R.id.breach_header)

            header.text = String.format(header.text.toString(), credential.pwnedAt)
            header.setOnClickListener {
                //FragmentSelector.dismissBreachWarning()
            }
        } else {
            view.findViewById<View>(R.id.breach_layout).visibility = View.GONE
        }

        FaviconLoader(activity!!.applicationContext).get(credential.domain, { bitmap ->
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
