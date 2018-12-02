package com.codingchili.mouse.enigma.presenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton


/**
 * Fragment that shows information about a single credential.
 */
class CredentialInfoFragment : Fragment() {
    private lateinit var credential: Credential

    override fun onCreate(savedInstanceState: Bundle?) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_credential_info, container, false)

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

                    AuditLogger.onCopiedToClipboard(context!!, credential)
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
                    AuditLogger.onPasswordDisplayed(context!!, credential)
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
            FragmentSelector.removeCredentialDialog(credential)
            AuditLogger.onRemovedCredential(context!!, credential)
        }

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<TextView>(R.id.created_at_date).text = credential.created
        view.findViewById<TextView>(R.id.header).text = String.format(
                getString(R.string.credential_header,
                        credential.username,
                        credential.domain))

        val list = view.findViewById<ListView>(R.id.breach_list)
        val adapter = object : ArrayAdapter<PwnedSite>(activity?.applicationContext!!, R.layout.list_item_credential, CredentialBank.pwnsByDomain(credential.domain)) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var item: View? = convertView
                if (convertView == null) {
                    item = layoutInflater.inflate(R.layout.list_domain_pwn, parent, false) as View
                }

                val header = item!!.findViewById<TextView>(R.id.breach_header)
                val description = item.findViewById<TextView>(R.id.breach_description)

                val pwn = CredentialBank.pwnsByDomain(credential.domain)[position]

                header.text = String.format(header.text.toString(), pwn.discovered)
                description.text = Html.fromHtml(pwn.description, Html.FROM_HTML_MODE_COMPACT)

                if (!pwn.acknowledged) {
                    header.setTextColor(context.getColor(R.color.accent))
                } else {
                    header.setTextColor(context.getColor(R.color.text))
                }

                item.setOnClickListener {
                    CredentialBank.acknowledge(pwn)
                    CredentialBank.store(credential)
                    Toast.makeText(context, getString(R.string.credential_unmark_pwned), Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
                return item
            }
        }

        list.adapter = adapter

        FaviconLoader(activity!!.applicationContext).get(credential.domain, { bitmap ->
            view.findViewById<ImageView>(R.id.logo).setImageBitmap(bitmap)
        }, {
            // no image available.
        })

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager? = (context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)
        if (clipboard != null) {
            val clip = ClipData.newPlainText("pwd", text)
            clipboard.primaryClip = clip
            Toast.makeText(context, getString(R.string.clipboard_success), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, getString(R.string.clipboard_failed), Toast.LENGTH_SHORT).show()
        }
    }
}
