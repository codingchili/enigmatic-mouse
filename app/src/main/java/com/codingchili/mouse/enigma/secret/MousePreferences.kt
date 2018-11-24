package com.codingchili.mouse.enigma.secret

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class MousePreferences {
    private val TEE_GEN : String = "TEE_GEN"
    private val TEE_IV : String = "TEE_IV"
    private val MASTER_SALT : String = "MASTER_SALT"

    private val fileName = "mouse.prefs"
    private var preferences : SharedPreferences

    constructor(application: Application) {
        preferences = application.getSharedPreferences(fileName, MODE_PRIVATE)
    }

    public fun getTeeIv(): ByteArray {
        return preferences.getString(TEE_IV, "")!!.toByteArray()
    }

    public fun getMasterSalt(): ByteArray {
        return preferences.getString(MASTER_SALT, "")!!.toByteArray()
    }

    public fun isTeeGenerated(): Boolean {
        return preferences.getBoolean(TEE_GEN, false)
    }

    public fun setTeeIV(iv: ByteArray) {
        preferences.edit()
                .putString(TEE_IV, String(iv))
                .apply()
    }

    public fun setMasterSalt(salt: ByteArray) {
        preferences.edit()
                .putString(MASTER_SALT, String(salt))
                .apply()
    }

    public fun setTeeGenerated() {
        preferences.edit()
                .putBoolean(TEE_GEN, true)
                .apply()
    }

    public fun unsetTeeGenerated() {
        preferences.edit()
                .putBoolean(TEE_GEN, false)
                .apply()
    }
}
