package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.google.android.material.floatingactionbutton.FloatingActionButton

internal class AddCredentialFragment: Fragment() {
    private val bank: CredentialBank = CredentialBank()

    override fun onActivityCreated(savedInstanceState:Bundle?){
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:Bundle?): View?{
        val view: View = inflater.inflate(R.layout.fragment_add_credential,container,false)

        view.findViewById<FloatingActionButton>(R.id.cancel).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        view.findViewById<Button>(R.id.save).setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        return view
    }

}

