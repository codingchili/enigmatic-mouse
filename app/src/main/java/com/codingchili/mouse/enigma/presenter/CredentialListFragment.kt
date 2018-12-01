package com.codingchili.mouse.enigma.presenter

import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.FaviconLoader
import com.codingchili.mouse.enigma.model.PwnedChecker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.atomic.AtomicInteger

/**
 * Fragment that displays a list of all available credentials.
 */
class CredentialListFragment : Fragment() {
    private lateinit var adapter: ArrayAdapter<Credential>
    private lateinit var icons: FaviconLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        icons = FaviconLoader(context!!)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_credential_list, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.credential_list, menu)
    }

    private fun performLogoClear() {
        icons.clear()
        CredentialBank.onCacheUpdated()
        Toast.makeText(activity?.applicationContext, getString(R.string.icons_reloading), Toast.LENGTH_SHORT).show()

        val credentials = CredentialBank.retrieve()
        val count = AtomicInteger(credentials.size)
        val progress = {
            if (count.decrementAndGet() == 0) {
                Toast.makeText(activity?.applicationContext, getString(R.string.icons_reloaded), Toast.LENGTH_SHORT).show()
            }
        }

        CredentialBank.retrieve().forEach { credential ->
            icons.load(credential.domain, { _ ->
                CredentialBank.onCacheUpdated()
                progress.invoke()
            }, { _ ->
                progress.invoke()
            })
        }
    }

    private fun performPwnedCheck() {
        val checker = PwnedChecker(activity!!.application)
        val sites = ArrayList<String>()
        val activity = activity

        CredentialBank.retrieve().forEach {
            sites.add(it.domain)
        }

        checker.check(sites, { pwned ->
            Toast.makeText(activity, getString(R.string.pwned_updating_lists), Toast.LENGTH_SHORT).show()

            CredentialBank.setPwnedList(pwned)

            activity!!.runOnUiThread {
                Toast.makeText(activity, getString(R.string.pwned_list_updated), Toast.LENGTH_SHORT).show()
            }

        }, {
            Toast.makeText(activity, R.string.pwned_check_error, Toast.LENGTH_LONG).show()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        val toolbar = view.findViewById<Toolbar>(R.id.bottom_app_bar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(activity?.supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_logo_cache -> {
                    performLogoClear()
                }
                R.id.clean_all_dev -> {
                    CredentialBank.uninstall()
                    activity?.finish()
                }
                R.id.pwned_checker -> {
                    performPwnedCheck()
                }
            }
            true
        }

        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<FloatingActionButton>(R.id.add_pw)

        button.setOnClickListener {
            view.findViewById<FloatingActionButton>(R.id.add_pw).setImageResource(R.drawable.add_icon_simple)
            FragmentSelector.addCredential()
        }

        val list = view.findViewById<ListView>(R.id.list_pw)
        adapter = object : ArrayAdapter<Credential>(activity?.applicationContext!!, R.layout.list_item_credential, CredentialBank.retrieve()) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var item: View? = convertView
                if (convertView == null) {
                    item = layoutInflater.inflate(R.layout.list_item_credential, parent, false) as View
                }

                val imageView: ImageView = item?.findViewById(R.id.site_logo) as ImageView

                FaviconLoader(context).get(CredentialBank.retrieve()[position].domain, { bitmap ->
                    imageView.setImageBitmap(bitmap)
                }, {
                    // image not loaded to cache - unload current.
                    imageView.setImageDrawable(null)
                })

                val credential = CredentialBank.retrieve()[position]
                val username = item.findViewById<TextView>(R.id.username)
                item.findViewById<ImageView>(R.id.favorite_icon)?.visibility =
                        if (credential.favorite) View.VISIBLE else View.GONE

                username.text = credential.username

                val domain = item.findViewById<TextView>(R.id.url)
                domain.text = credential.domain

                val pwned = CredentialBank.pwnsByDomain(credential.domain)

                pwned.forEach { pwn ->
                    domain.setTextColor(context.getColor(R.color.text))
                    if (!pwn.acknowledged) {
                        domain.setTextColor(context.getColor(R.color.accent))
                    }
                }
                return item
            }
        }

        val activity = activity
        CredentialBank.onChangeListener {
            activity!!.runOnUiThread {
                adapter.notifyDataSetChanged()
            }
        }

        list?.adapter = adapter

        list?.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            FragmentSelector.credentialInfo(CredentialBank.retrieve()[position])
        }

        list?.setOnItemLongClickListener { _, _, position, _ ->
            val credential: Credential = CredentialBank.retrieve()[position]
            credential.favorite = !credential.favorite
            CredentialBank.store(credential)
            adapter.notifyDataSetChanged()
            true
        }
    }
}
