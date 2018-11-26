package com.codingchili.mouse.enigma.presenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.FaviconLoader
import com.codingchili.mouse.enigma.model.MousePreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView


/**
 * @author Robin Duda
 *
 * Fragment for the application menu.
 */
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
                    CredentialBank.onCacheUpdated()
                    Toast.makeText(activity?.applicationContext, getString(R.string.cache_cleared), Toast.LENGTH_SHORT).show()

                    CredentialBank.retrieve().forEach { credential ->
                        icons.load(credential.site, { _ ->
                            CredentialBank.onCacheUpdated()
                        }, { _ ->  }) // failed to update icon..
                    }
                }
                R.id.clean_all_dev -> {
                    CredentialBank.uninstall()
                    activity?.finish()
                }
            }
            FragmentSelector.remove(this)
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