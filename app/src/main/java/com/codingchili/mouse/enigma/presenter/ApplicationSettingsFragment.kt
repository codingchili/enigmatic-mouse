package com.codingchili.mouse.enigma.presenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.MousePreferences
import com.google.android.material.bottomappbar.BottomAppBar
import java.time.format.DateTimeFormatter

/**
 * Shows application settings.
 */
class ApplicationSettingsFragment : Fragment() {
    private lateinit var preferences: MousePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = MousePreferences(activity!!.application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(false)
        val view = inflater.inflate(R.layout.fragment_application_settings, container, false)

        view.findViewById<BottomAppBar>(R.id.bottom_app_bar).navigationIcon = null

        view.findViewById<View>(R.id.cancel).setOnClickListener {
            FragmentSelector.back()
        }

        val date = view.findViewById<TextView>(R.id.security_list_date)
        val status = view.findViewById<TextView>(R.id.fingerprint_security_status)
        val log = view.findViewById<TextView>(R.id.audit_log)
        val delay = view.findViewById<Switch>(R.id.delay_sensitive_operations)
        val developer = view.findViewById<Switch>(R.id.developer_options)
        val resume = view.findViewById<Switch>(R.id.lock_on_resume)

        date.text = preferences.lastPwnedCheck()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        status.text = if (preferences.isSupportingFP())
            getString(R.string.fp_enabled)
        else getString(R.string.fp_not_enabled)

        delay.isChecked = preferences.delayedActions()
        developer.isChecked = preferences.developerOptions()
        resume.isChecked = preferences.lockOnresume()

        delay.setOnCheckedChangeListener { _, isChecked ->
            preferences.setDelayActions(isChecked)
        }

        developer.setOnCheckedChangeListener { _, isChecked ->
            preferences.setDeveloperOptions(isChecked)
        }

        resume.setOnCheckedChangeListener { _, isChecked ->
            preferences.setLockOnResume(isChecked)
        }

        log.text = CredentialBank.auditLog().joinToString(separator = "\n")

        return view
    }

}
