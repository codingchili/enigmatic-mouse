package com.codingchili.mouse.enigma.presenter

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.CredentialBank


/**
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
        show(AddCredentialFragment(), "add")
    }

    fun credentialInfo(credential: Credential) {
        show(CredentialInfoFragment().setCredential(credential), "info")
    }

    fun info() {
        show(ApplicationInfoFragment(), "app_info")
    }

    fun settings() {
        show(ApplicationSettingsFragment(), "app_settings")
    }

    fun list() {
        show(CredentialListFragment(), "list", android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun show(fragment: Fragment, tag: String) {
        show(fragment, tag, android.R.anim.slide_in_left, android.R.anim.fade_out)
    }

    private fun show(fragment: Fragment, tag: String, inAnimation: Int, outAnimation: Int) {
        manager.beginTransaction()
                .setCustomAnimations(inAnimation, outAnimation)
                .replace(R.id.root, fragment)
                .addToBackStack(tag)
                .commit()
    }

    fun removeCredentialDialog(credential: Credential) {
        showDialog(R.string.delete_credential,
                R.string.delete_positive,
                R.string.delete_negative, {
            CredentialBank.remove(credential)
            FragmentSelector.back()

            val text = "${activity.getString(R.string.removed_toaster)} " +
                    "${credential.username}@${credential.domain}"

            Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
        }, {})
    }

    fun clipboardWarningDialog(callback: () -> Unit) {
        showDialog(R.string.clipboard_warning,
                R.string.clipboard_positive,
                R.string.clipboard_negative,
                callback, {
            Toast.makeText(activity.applicationContext,
                    activity.getString(R.string.clipboard_warning_heeded),
                    Toast.LENGTH_LONG).show()
        })
    }

    private fun showDialog(message: Int, positiveText: Int, negativeText: Int,
                           positiveHandler: () -> Unit, negativeHandler: () -> Unit) {
        val dialog = DialogDelayedPositiveButton()
        dialog.message = message
        dialog.positiveText = positiveText
        dialog.negativeText= negativeText
        dialog.positiveHandler= positiveHandler
        dialog.negativeHandler = negativeHandler
        dialog.show(manager, "dialog")
    }
}