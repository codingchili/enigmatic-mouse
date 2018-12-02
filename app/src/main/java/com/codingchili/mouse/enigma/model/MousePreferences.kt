package com.codingchili.mouse.enigma.model

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val KEY_INSTALLED = "KEY_INSTALLED"
private const val TEE_IV = "TEE_IV"
private const val MASTER_SALT = "MASTER_SALT"
private const val MASTER_KEY = "MASTER_KEY"
private const val CLIPBOARD_WARNING = "CLIPBOARD_WARNING"
private const val FP_SUPPORTED = "FP_SUPPORTED"
private const val PWNED_CHECK = "PWNED_CHECK"
private const val LOCK_RESUME = "LOCK_RESUME"
private const val DEVELOPER_OPTIONS = "DEV_OPTIONS"
private const val DELAY_ACTIONS = "DELAY_ACTIONS"
private const val fileName = "mouse.prefs"

/**
 * Preferences wrapper.
 */
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
                .putString(PWNED_CHECK, null)
                .putBoolean(CLIPBOARD_WARNING, false)
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

    fun setLastPwnedCheck(date: ZonedDateTime): MousePreferences {
        preferences.edit()
                .putString(PWNED_CHECK, date.format(DateTimeFormatter.ISO_DATE_TIME))
                .apply()
        return this
    }

    fun setLockOnResume(enabled: Boolean) {
        preferences.edit()
                .putBoolean(LOCK_RESUME, enabled)
                .apply()
    }

    fun setDeveloperOptions(enabled: Boolean) {
        preferences.edit()
                .putBoolean(DEVELOPER_OPTIONS, enabled)
                .apply()
    }

    fun setDelayActions(enabled: Boolean) {
        preferences.edit()
                .putBoolean(DELAY_ACTIONS, enabled)
                .apply()
    }

    fun lockOnresume(): Boolean {
        return preferences.getBoolean(LOCK_RESUME, true)
    }

    fun developerOptions(): Boolean {
        return preferences.getBoolean(DEVELOPER_OPTIONS, false)
    }

    fun delayedActions(): Boolean {
        return preferences.getBoolean(DELAY_ACTIONS, true)
    }

    fun lastPwnedCheck(): ZonedDateTime {
        val date = Optional.ofNullable(preferences.getString(PWNED_CHECK, null))

        val parsed = date.map { string ->
            ZonedDateTime.parse(string, DateTimeFormatter.ISO_DATE_TIME)
        }
        // if unset scan breaches in the last 6 months.
        return parsed.orElse(ZonedDateTime.now().minusMonths(6))
    }
}