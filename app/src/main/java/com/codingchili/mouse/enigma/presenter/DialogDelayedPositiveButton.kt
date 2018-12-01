package com.codingchili.mouse.enigma.presenter

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.MousePreferences

/**
 * Dialog to delete a credential.
 */
class DialogDelayedPositiveButton : DialogFragment() {
    private val actionBlockMS = 4000L
    var positiveText : Int = 0
    var negativeText: Int = 0
    var message: Int = 0
    lateinit var negativeHandler: () -> Unit
    lateinit var positiveHandler: () -> Unit

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val preferences = MousePreferences(activity!!.application)
        val block = if (preferences.delayedActions()) actionBlockMS else 1
        val blocked = if (preferences.delayedActions()) " (${block / 1000})" else ""

        return activity?.let { context ->
            val builder = AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton("${getString(positiveText)}$blocked") { _, _ -> positiveHandler.invoke() }
                    .setNegativeButton(negativeText) { _, _ -> negativeHandler.invoke() }

            val dialog = builder.create()
            dialog.setOnShowListener {
                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

                button.isEnabled = false

                Handler().postDelayed({
                    button.setTextColor(context.getColor(R.color.accent))
                    button.isEnabled = true
                }, block)
            }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}