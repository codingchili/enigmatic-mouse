package com.codingchili.mouse.enigma.presenter

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.CredentialBank


/**
 * @author Robin Duda
 *
 * Helper class to manage fragments.
 */
object FragmentSelector {
    private lateinit var manager: FragmentManager
    private lateinit var activity: AppCompatActivity

    fun master() {
        manager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.root, MasterSetupFragment())
                .commit()
    }

    fun init(activity: AppCompatActivity) {
        this.manager = activity.supportFragmentManager
        this.activity = activity
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

    fun removeCredentialDialog(credential: Credential) {
        val dialog = DialogDelayedPositiveButton()
                .setMessage(R.string.delete_credential)
                .setPositiveText(R.string.delete_positive)
                .setNegativeText(R.string.delete_negative)
                .setPositiveHandler {
                    CredentialBank.remove(credential)
                    FragmentSelector.back()

                    val text = "${activity.getString(R.string.removed_toaster)} ${credential.username}@${credential.site}"
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
                }
                .setNegativeHandler {

                }
        dialog.show(manager, "dialog")
    }

    fun clipboardWarningDialog(callback: () -> Unit) {
        val dialog = DialogDelayedPositiveButton()

        dialog.setMessage(R.string.clipboard_warning)
                .setPositiveText(R.string.clipboard_positive)
                .setNegativeText(R.string.clipboard_negative)
                .setPositiveHandler {
                    callback.invoke()
                }
                .setNegativeHandler {
                    Toast.makeText(activity.applicationContext,
                            activity.getString(R.string.clipboard_warning_heeded),
                            Toast.LENGTH_LONG).show()
                }
        dialog.show(manager, "dialog")
    }
}
