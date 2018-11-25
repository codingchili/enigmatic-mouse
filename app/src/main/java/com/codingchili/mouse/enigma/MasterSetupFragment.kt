package com.codingchili.mouse.enigma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import com.codingchili.mouse.enigma.secret.CredentialBank
import com.codingchili.mouse.enigma.secret.MousePreferences
import com.google.android.material.textfield.TextInputEditText
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MasterSetupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_master_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = MousePreferences(activity!!.application)
        val password = view.findViewById<TextInputEditText>(R.id.master_password)
        val cipher: Cipher
        password.onEditorAction(EditorInfo.IME_ACTION_DONE)

        if (preferences.isTeeGenerated()) {
            Log.w("MasterSetupFragment", String(preferences.getTeeIv()))
            CredentialBank.setIv(preferences.getTeeIv())

            view.findViewById<TextView>(R.id.fp_header).text = getString(R.string.fp_authenticate)
            view.findViewById<View>(R.id.master_password).visibility = View.GONE
            view.findViewById<View>(R.id.master_password_header).visibility = View.GONE
            cipher = CredentialBank.masterKey(false)
        } else {
            Log.w("MasterSetupFragment", "NO tee GENERATED!")
            CredentialBank.generateTEEKey()
            cipher = CredentialBank.masterKey(true)
        }

        val fingerprints: FingerprintManagerCompat = FingerprintManagerCompat.from(context!!)
        fingerprints.authenticate(
                FingerprintManagerCompat.CryptoObject(cipher),
                0,
                androidx.core.os.CancellationSignal(),
                object : FingerprintManagerCompat.AuthenticationCallback() {

                    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence?) {
                        super.onAuthenticationError(errMsgId, errString)
                        Log.w("MasterSetupFragment", "onAuthenticationError")
                    }

                    override fun onAuthenticationSucceeded(result: FingerprintManagerCompat.AuthenticationResult?) {
                        super.onAuthenticationSucceeded(result)
                        Log.w("MasterSetupFragment", "onSucceeded")

                        if (preferences.isTeeGenerated()) {
                            // decrypt the key stored in the database some.. how..
                        } else {
                            // we use the KDF key to encrypt credentials.
                            val salt = CredentialBank.generateSalt()
                            val key = CredentialBank.generateKDFKey(password.text.toString().toByteArray(), salt)
                            val spec = SecretKeySpec(key, "AES")

                            // we use the fingerprint protected TEE key to decrypt the encrypted KDF key.
                            // we can also retrieve the KDF key using the master password.
                            val encryptedKey = cipher.doFinal(spec.encoded)

                            preferences.setMasterSalt(salt)
                                    .setTeeIV(cipher.iv)
                                    .setTeeGenerated()

                            Log.w("MasterSetupFragment", String(encryptedKey))
                        }

                        fragmentManager?.beginTransaction()
                                ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                ?.replace(R.id.root, CredentialListFragment())
                                ?.addToBackStack("list")
                                ?.commit()
                    }

                    override fun onAuthenticationFailed() {
                        Log.w("MasterSetupFragment", "onFailed")
                        super.onAuthenticationFailed()
                    }
                },
                null)

        view.findViewById<ImageView>(R.id.fp_tap).setOnClickListener {
            // i think we can remove this because its not proper ui.
        }

        /*val info : BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.fp_title))
                .setSubtitle(getString(R.string.fp_subtitle))
                .setDescription(getString(R.string.fp_description))
                .setNegativeButtonText(getString(R.string.fp_cancel))
                .build()

        // we need to fake a fragment activity, we have a regular activity and
        // a fragment. Combine them and wazaa!
        val fakeFragmentActivity : FragmentActivity  = object: FragmentActivity() {

            override fun getSupportFragmentManager(): FragmentManager {
                return activity!!.supportFragmentManager
            }

            // we need to track the lifecycle of the main activity because the
            // biometric prompt will listen to the state and cancel itself due
            // to security reasons.
            override fun getLifecycle(): Lifecycle {
                return object: Lifecycle() {
                    override fun addObserver(observer: LifecycleObserver) {
                        activity?.lifecycle?.addObserver(observer)
                    }

                    override fun removeObserver(observer: LifecycleObserver) {
                        activity?.lifecycle?.removeObserver(observer)
                    }

                    override fun getCurrentState(): State {
                        // fake the state to match the main activity state.
                        return activity!!.lifecycle.currentState
                    }
                }
            }
        }*/

        /*BiometricPrompt(fakeFragmentActivity, Executors.newSingleThreadExecutor(), object: BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.w("MasterSetupFragment", "onAuthenticationError")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.w("MasterSetupFragment", "onSucceeded")

                fragmentManager?.beginTransaction()
                        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        ?.add(R.id.root, CredentialListFragment())
                        ?.addToBackStack("list")
                        ?.commit()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w("MasterSetupFragment", "onFailed")
            }
        }).authenticate(info)*/
    }
}