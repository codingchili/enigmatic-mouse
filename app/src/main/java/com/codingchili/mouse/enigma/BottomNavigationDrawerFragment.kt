package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView


class BottomNavigationDrawerFragment: BottomSheetDialogFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val nav: NavigationView = view?.findViewById(R.id.navigation_view)!!

        nav.setNavigationItemSelectedListener { menuItem ->

            // todo something on menu click :)

            Toast.makeText(activity?.applicationContext, "xxx", Toast.LENGTH_SHORT).show()
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
