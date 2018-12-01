package com.codingchili.mouse.enigma.presenter

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

        nav.setNavigationItemSelectedListener { item ->
            // when..
            FragmentSelector.remove(this)
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }
}