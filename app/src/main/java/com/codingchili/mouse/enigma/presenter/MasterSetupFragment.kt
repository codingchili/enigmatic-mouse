package com.codingchili.mouse.enigma.presenter

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.CredentialBank
import com.codingchili.mouse.enigma.model.MousePreferences
import com.google.android.material.textfield.TextInputEditText
import io.realm.Realm


/**
 * @author Robin Duda
 *
 * Fragment used to setup the master password and generate a TEE key
 * protected by fingerprint.
 */
class MasterSetupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        Realm.init(context!!.applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_master_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val header = view.findViewById<TextView>(R.id.fp_header)
        val preferences = MousePreferences(activity!!.application)
        val password = view.findViewById<TextInputEditText>(R.id.master_password)
        password.onEditorAction(EditorInfo.IME_ACTION_DONE)

        CredentialBank.setPreferences(preferences)

        if (preferences.isTeeGenerated()) {
            header.text = getString(R.string.fp_authenticate)
            view.findViewById<View>(R.id.master_password).visibility = View.GONE
            view.findViewById<View>(R.id.master_password_header).visibility = View.GONE
            CredentialBank.initCipher(false)
        } else {
            header.text = getString(R.string.master_scan_fp_text)
            CredentialBank.initCipher(true)
        }

        val fingerprints: FingerprintManagerCompat = FingerprintManagerCompat.from(context!!)
        fingerprints.authenticate(
                FingerprintManagerCompat.CryptoObject(CredentialBank.getCipher()),
                0,
                androidx.core.os.CancellationSignal(),
                object : FingerprintManagerCompat.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        CredentialBank.setPreferences(MousePreferences(activity!!.application))

                        if (preferences.isTeeGenerated()) {
                            CredentialBank.load()
                            FragmentSelector.list()
                        } else {
                            header.text = getString(R.string.master_crypto_in_progress)
                            animatedFeedback(view)

                            AsyncTask.execute {
                                CredentialBank.install(password.text.toString())
                                activity!!.runOnUiThread {
                                    FragmentSelector.list()
                                }
                            }
                        }
                    }

                    override fun onAuthenticationFailed() {
                        view.findViewById<TextView>(R.id.fp_header).text = getString(R.string.fp_authenticate_fail)
                        view.findViewById<ImageView>(R.id.fp_icon)
                                .setColorFilter(
                                        ContextCompat.getColor(context!!, R.color.accent),
                                        android.graphics.PorterDuff.Mode.MULTIPLY)
                    }
                },
                null)
    }

    private fun animatedFeedback(view: View) {
        val icon = view.findViewById<ImageView>(R.id.fp_icon)
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

        view.findViewById<View>(R.id.master_password_header).visibility = View.GONE
        view.findViewById<View>(R.id.master_password_layout).visibility = View.GONE
    }
}