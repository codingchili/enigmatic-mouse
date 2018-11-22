package com.codingchili.mouse.enigma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.Credential
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.FaviconLoader

class CredentialListFragment : Fragment() {
    private val bank: CredentialBank = CredentialBank()

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<ListView>(R.id.list_pw)
        val adapter = object : ArrayAdapter<Credential>(activity?.applicationContext!!, R.layout.list_item_user, bank.retrieve()) {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater.inflate(R.layout.fragment_user_list, container,false)
    }

}
