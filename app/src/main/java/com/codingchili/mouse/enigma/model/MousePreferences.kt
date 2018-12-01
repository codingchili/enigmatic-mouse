package com.codingchili.mouse.enigma.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.util.*

private const val KEY_INSTALLED = "KEY_INSTALLED"
private const val TEE_IV = "TEE_IV"
private const val MASTER_SALT = "MASTER_SALT"
private const val MASTER_KEY = "MASTER_KEY"
private const val CLIPBOARD_WARNING = "CLIPBOARD_WARNING"
private const val FP_SUPPORTED = "FP_SUPPORTED"
private const val fileName = "mouse.prefs"

class MousePreferences(application: Application) {
    private var preferences : SharedPreferences =
            application.getSharedPreferences(fileName, MODE_PRIVATE)

    fun getTeeIv(): ByteArray {
        val iv: String = preferences.getString(TEE_IV, "")!!

        if (!iv.isBlank()) {
            return Base64.getDecoder().decode(iv)
        } else {
            throw Error("No IV is present.")
        }
    }

    fun getMasterSalt(): ByteArray {
        val salt: String = preferences.getString(MASTER_SALT, "")!!

        if (!salt.isBlank()) {
            return Base64.getDecoder().decode(salt)
        } else {
            throw Error("No master salt is present.")
        }
    }

    fun isKeyInstalled(): Boolean {
        return preferences.getBoolean(KEY_INSTALLED, false)
    }

    fun getEncryptedMaster(): ByteArray {
        val key: String = preferences.getString(MASTER_KEY, "")!!

        if (key.isBlank()) {
            throw Error("No master key in shared prefs.")
        } else {
            return Base64.getDecoder().decode(key)
        }
    }

    fun isClipboardWarningShown(): Boolean {
        return preferences.getBoolean(CLIPBOARD_WARNING, false)
    }

    fun setClipboardWarned(isWarned: Boolean): MousePreferences {
        preferences.edit()
                .putBoolean(CLIPBOARD_WARNING, isWarned)
                .apply()
        return this
    }

    fun setTeeIV(iv: ByteArray): MousePreferences {
        preferences.edit()
                .putString(TEE_IV, Base64.getEncoder().encodeToString(iv))
                .apply()
        return this
    }

    fun setMasterSalt(salt: ByteArray): MousePreferences {
        preferences.edit().putString(MASTER_SALT, Base64.getEncoder().encodeToString(salt)).apply()
        return this
    }

    fun setInstalled(): MousePreferences {
        preferences.edit().putBoolean(KEY_INSTALLED, true).apply()
        return this
    }

    fun reset(): MousePreferences {
        preferences.edit()
                .putBoolean(KEY_INSTALLED, false)
                .putBoolean(FP_SUPPORTED, true)
                .apply()
        return this
    }

    fun setEncryptedMaster(encryptedKey: ByteArray): MousePreferences {
        preferences.edit()
                .putString(MASTER_KEY, Base64.getEncoder().encodeToString(encryptedKey))
                .apply()
        return this
    }

    fun setFPSupported(supported: Boolean): MousePreferences {
        preferences.edit()
                .putBoolean(FP_SUPPORTED, supported)
                .apply()
        return this
    }

    fun isSupportingFP(): Boolean {
        return preferences.getBoolean(FP_SUPPORTED, true)
    }
}