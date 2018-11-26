package com.codingchili.mouse.enigma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.FaviconLoader
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CredentialListFragment : Fragment() {
    private lateinit var adapter: ArrayAdapter<Credential>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.bottom_app_bar).setOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(activity?.supportFragmentManager, bottomNavDrawerFragment.tag)
        }

        val button = view.findViewById<FloatingActionButton>(R.id.add_pw)

        button.setOnClickListener {

            view.findViewById<FloatingActionButton>(R.id.add_pw).setImageResource(R.drawable.add_icon_simple)

            activity?.supportFragmentManager?.beginTransaction()
                    ?.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out)
                    ?.replace(R.id.root, AddCredentialFragment())
                    ?.addToBackStack("add")
                    ?.commit()
        }

        val list = view.findViewById<ListView>(R.id.list_pw)
        adapter = object : ArrayAdapter<Credential>(activity?.applicationContext!!, R.layout.list_item_user, CredentialBank.retrieve()) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var view: View? = convertView
                if (convertView == null) {
                    view = layoutInflater.inflate(R.layout.list_item_user, parent, false) as View
                }

                val imageView: ImageView = view?.findViewById(R.id.site_logo) as ImageView

                FaviconLoader(context).get(CredentialBank.retrieve()[position].site, { bitmap ->
                    //imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imageView.setImageBitmap(bitmap)
                }, {
                    // image not loaded to cache - unload current.
                    imageView.setImageDrawable(null)
                })

                view.findViewById<TextView>(R.id.url)?.text = CredentialBank.retrieve()[position].site
                view.findViewById<TextView>(R.id.username)?.text = CredentialBank.retrieve()[position].username

                return view
            }
        }

        CredentialBank.onChangeListener {
            adapter.notifyDataSetChanged()
        }

        list?.adapter = adapter

        list?.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val credential = CredentialBank.retrieve()[position]

            activity?.supportFragmentManager?.beginTransaction()
                    ?.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out)
                    ?.replace(R.id.root, CredentialInfoFragment().setCredential(credential))
                    ?.addToBackStack("info")
                    ?.commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_user_list, container,false)
    }

}
