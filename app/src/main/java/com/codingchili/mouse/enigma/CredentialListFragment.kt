package com.codingchili.mouse.enigma

import android.os.Bundle
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
    private val bank: CredentialBank = CredentialBank()
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
                    ?.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    ?.replace(R.id.root, AddCredentialFragment().setBank(bank))
                    ?.addToBackStack("add")
                    ?.commit()
        }

        val list = view.findViewById<ListView>(R.id.list_pw)
        adapter = object : ArrayAdapter<Credential>(activity?.applicationContext!!, R.layout.list_item_user, bank.retrieve()) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var view: View? = convertView

                if (convertView == null) {
                    view = layoutInflater.inflate(R.layout.list_item_user, parent, false) as View
                }

                FaviconLoader(context).load(bank.retrieve()[position].url, { bitmap ->
                    val imageView: ImageView = view?.findViewById(R.id.site_logo) as ImageView
                    imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    imageView.setImageBitmap(bitmap)
                }, { exception ->
                    Toast.makeText(super.getContext(), exception.message, Toast.LENGTH_SHORT).show()
                })

                view?.findViewById<TextView>(R.id.url)?.text = bank.retrieve()[position].url
                view?.findViewById<TextView>(R.id.username)?.text = bank.retrieve()[position].username

                return view
            }
        }

        list?.adapter = adapter

        list?.setOnItemClickListener { _: AdapterView<*>?, view: View?, position: Int, _: Long ->
            // do something.
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
