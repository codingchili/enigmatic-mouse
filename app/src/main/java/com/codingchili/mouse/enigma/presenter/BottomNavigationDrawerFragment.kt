package com.codingchili.mouse.enigma.presenter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.codingchili.mouse.enigma.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView


/**
 * Fragment for the application menu.
 */
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val nav: NavigationView = view?.findViewById(R.id.navigation_view)!!

        nav.setNavigationItemSelectedListener {
            FragmentSelector.remove(this)

            when (it.itemId) {
                R.id.navigation_settings -> FragmentSelector.settings()
                R.id.navigation_info -> FragmentSelector.info()
                R.id.navigation_rate -> openAppInPlayStore()
                R.id.navigation_website -> openWebsite()
            }
            true
        }
    }

    private fun openAppInPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.app_url))
        startActivity(intent)
    }

    private fun openWebsite() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(getString(R.string.website_url))
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }
}