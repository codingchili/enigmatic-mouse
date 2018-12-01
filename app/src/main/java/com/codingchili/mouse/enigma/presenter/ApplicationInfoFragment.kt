package com.codingchili.mouse.enigma.presenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.google.android.material.bottomappbar.BottomAppBar

/**
 * Shows information about the application.
 */
class ApplicationInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(false)

        val view = inflater.inflate(R.layout.fragment_application_info, container, false)

        view.findViewById<BottomAppBar>(R.id.bottom_app_bar).navigationIcon = null

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            FragmentSelector.back()
        }

        return view
    }
}