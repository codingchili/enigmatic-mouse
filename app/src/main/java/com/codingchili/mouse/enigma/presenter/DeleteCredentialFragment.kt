package com.codingchili.mouse.enigma.presenter

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential
import com.codingchili.mouse.enigma.model.CredentialBank

/**
 * Dialog to delete a credential.
 */
class DeleteCredentialFragment : DialogFragment() {
    private val actionBlockMS = 4000L
    private lateinit var credential: Credential

    fun setCredential(credential: Credential): DeleteCredentialFragment {
        this.credential = credential
        return this
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { context ->
            val builder = AlertDialog.Builder(context)
                    .setMessage(R.string.delete_credential)
                    .setPositiveButton(R.string.delete_ok) { _, _ ->
                        CredentialBank.remove(credential)
                        FragmentSelector.back()

                        val text = "${getString(R.string.removed_toaster)} ${credential.username}@${credential.site}"
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton(R.string.delete_cancel) { _, _ -> }

            val dialog = builder.create()
            dialog.setOnShowListener {
                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

                button.isEnabled = false

                Handler().postDelayed({
                    button.setTextColor(context.getColor(R.color.accent))
                    button.isEnabled = true
                }, actionBlockMS)
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}