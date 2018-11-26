package com.codingchili.mouse.enigma.presenter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential


/**
 * @author Robin Duda
 *
 * Helper class to manage fragments.
 */
object FragmentSelector {
    private lateinit var manager : FragmentManager

    fun master() {
        manager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.root, MasterSetupFragment())
                .commit()
    }

    fun init(manager: FragmentManager) {
        this.manager = manager
    }

    fun back() {
        manager.popBackStack()
    }

    fun remove(fragment: Fragment) {
        manager.beginTransaction().remove(fragment).commit()
    }

    fun addCredential() {
        manager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out)
                .replace(R.id.root, AddCredentialFragment())
                .addToBackStack("add")
                .commit()
    }

    fun credentialInfo(credential: Credential) {
        manager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out)
                .replace(R.id.root, CredentialInfoFragment().setCredential(credential))
                .addToBackStack("info")
                .commit()
    }

    fun list() {
        manager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.root, CredentialListFragment())
                .addToBackStack("list")
                .commit()
    }

    fun removeCredential(credential: Credential) {
        DeleteCredentialFragment().setCredential(credential).show(manager, "dialog")
    }

}
