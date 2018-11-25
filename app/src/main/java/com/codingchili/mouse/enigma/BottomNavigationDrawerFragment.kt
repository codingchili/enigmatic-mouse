package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.FaviconLoader
import com.codingchili.mouse.enigma.secret.MousePreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView


class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {
    private lateinit var icons: FaviconLoader

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        icons = FaviconLoader(context!!)
        val nav: NavigationView = view?.findViewById(R.id.navigation_view)!!

        nav.setNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.clear_logo_cache -> {
                    icons.clear()
                    Toast.makeText(activity?.applicationContext, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()

                    CredentialBank.retrieve().forEach { credential ->
                        icons.load(credential.url, { bitmap ->
                            CredentialBank.onCacheUpdated(credential)
                        }, { exception ->
                            // failed to update icon..
                        })
                    }
                }
                R.id.clean_all_dev -> {
                    MousePreferences(activity!!.application)
                            .unsetTeeGenerated()

                    activity?.finish()
                }
            }

            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }
}
