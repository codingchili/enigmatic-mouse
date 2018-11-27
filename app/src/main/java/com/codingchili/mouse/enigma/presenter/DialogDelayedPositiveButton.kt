package com.codingchili.mouse.enigma.presenter

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.Credential

/**
 * Dialog to delete a credential.
 */
class DialogDelayedPositiveButton : DialogFragment() {
    val actionBlockMS = 4000L
    var positiveText : Int = 0
    var negativeText: Int = 0
    var message: Int = 0
    lateinit var negativeHandler: () -> Unit
    lateinit var positiveHandler: () -> Unit

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { context ->
            val builder = AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(positiveText) { _, _ -> positiveHandler.invoke() }
                    .setNegativeButton(negativeText) { _, _ -> negativeHandler.invoke() }

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