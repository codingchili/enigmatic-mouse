package com.codingchili.mouse.enigma.presenter

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.MousePreferences
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.Realm


/**
 * Fragment used to setup the master password and generate a TEE key
 * protected by fingerprint.
 */
class MasterSetupFragment : Fragment() {
    private lateinit var fingerprints: FingerprintManagerCompat
    private lateinit var preferences: MousePreferences

    private lateinit var icon: ImageView
    private lateinit var subheading: TextView
    private lateinit var password: TextInputEditText
    private lateinit var passwordContainer: TextInputLayout
    private lateinit var header: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        preferences = MousePreferences(activity!!.application)
        fingerprints = FingerprintManagerCompat.from(context!!)

        CredentialBank.setPreferences(MousePreferences(activity!!.application))
        Realm.init(context!!.applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_master_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        header = view.findViewById(R.id.master_password_header)
        password = view.findViewById(R.id.master_password)
        passwordContainer = view.findViewById(R.id.master_password_layout)
        subheading = view.findViewById(R.id.fp_header)
        icon = view.findViewById(R.id.fp_icon)

        adaptViewForFingerprintSensorAvailability()

        if (preferences.isKeyInstalled()) {
            password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            header.visibility = View.GONE
        } else {
            subheading.text = if (fingerprintSupported()) getString(R.string.master_scan_fp_text)
            else getString(R.string.master_authenticate)
        }

        if (fingerprintSupported()) {
            CredentialBank.initCipher(!preferences.isKeyInstalled())
            authenticateWithFingerprint()
        }

        view.findViewById<ImageView>(R.id.fp_icon).setOnClickListener {
            hideKeyboard()

            if (preferences.isKeyInstalled()) {

                if (password.text!!.isEmpty()) {
                    // user taps the login button when in FP mode - no text entered.
                    subheading.text = getString(R.string.master_authenticate)
                    showPasswordLogin()
                } else {
                    subheading.text = getString(R.string.master_crypto_verifying)
                    animatedFeedback()
                    AsyncTask.execute {
                        if (CredentialBank.unlockWithPassword(password.text.toString())) {
                            activity!!.runOnUiThread {
                                finish()
                            }
                        } else {
                            onAuthenticationFailed()
                        }
                    }
                }
            } else {
                install(false)
            }
        }
    }

    private fun finish() {
        hideKeyboard()
        FragmentSelector.list()
    }

    private fun hideKeyboard() {
        if (activity != null) {
            val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(password.windowToken, 0)
        }
    }

    private fun authenticateWithFingerprint() {
        fingerprints.authenticate(
                FingerprintManagerCompat.CryptoObject(CredentialBank.getCipher()),
                0,
                androidx.core.os.CancellationSignal(),
                object : FingerprintManagerCompat.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        hideKeyboard()
                        if (preferences.isKeyInstalled()) {
                            CredentialBank.decryptMasterKeyWithFingerprint()
                            finish()
                        } else {
                            install(true)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        this@MasterSetupFragment.onAuthenticationFailed()
                        this@MasterSetupFragment.authenticateWithFingerprint()
                    }
                },
                null)

    }

    private fun install(fingerprint: Boolean) {
        subheading.text = getString(R.string.master_crypto_in_progress)
        animatedFeedback()
        AsyncTask.execute {

            if (fingerprint) {
                CredentialBank.installWithFingerprint(password.text.toString())
            } else {
                CredentialBank.installWithPassword(password.text.toString())
            }

            activity!!.runOnUiThread {
                finish()
            }
        }
    }

    private fun adaptViewForFingerprintSensorAvailability() {
        if (fingerprintSupported()) {
            subheading.text = getString(R.string.fp_authenticate)

            if (preferences.isKeyInstalled()) {
                passwordContainer.visibility = View.GONE
                header.visibility = View.GONE
            }

            icon.setImageResource(R.drawable.baseline_fingerprint_24)
        } else {
            subheading.text = getString(R.string.master_authenticate)
            passwordContainer.visibility = View.VISIBLE
            icon.setImageResource(R.drawable.baseline_launch_24)
        }
    }

    private fun onAuthenticationFailed() {
        activity!!.runOnUiThread {
            subheading.text = getString(R.string.fp_authenticate_fail)
            val icon = view!!.findViewById<ImageView>(R.id.fp_icon)
            icon.clearAnimation()

            icon.setColorFilter(
                    ContextCompat.getColor(context!!, R.color.accent),
                    android.graphics.PorterDuff.Mode.MULTIPLY)

            showPasswordLogin()
        }
    }

    private fun showPasswordLogin() {
        passwordContainer.visibility = View.VISIBLE
        icon.setImageResource(R.drawable.baseline_launch_24)
    }

    private fun fingerprintSupported(): Boolean {
        return (fingerprints.isHardwareDetected && preferences.isSupportingFP())
    }

    private fun animatedFeedback() {
        val animation = RotateAnimation(0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        )
        icon.setImageResource(R.drawable.baseline_sync_24)
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.duration = 700
        icon.startAnimation(animation)

        header.visibility = View.GONE
        passwordContainer.visibility = View.GONE
    }
}