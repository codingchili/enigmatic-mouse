package com.codingchili.mouse.enigma.secret

import android.util.Log
import org.spongycastle.crypto.generators.SCrypt

/**
 * @author Robin Duda
 */
class CredentialBank {
    private val list: ArrayList<Credential> = ArrayList()
    private val master: String = "kaputt"

    init {
        list.add(Credential("youtube.com", "rduda@kth.se", "********"))
        list.add(Credential("google.com", "rduda@kth.se", "********"))
    }

    fun store(credential: Credential) {
        // 1. kdf on input password to verify master password.
        // 2. kdf to generate symmetric key.
        var key : String =
                String(SCrypt.generate(credential.password.toByteArray(), "".toByteArray(), 256, 32, 2, 256))

        Log.w("", "generated = " + key)

        list.add(credential)

        list.sortBy {
            it.url
        }
    }

    fun remove(credential: Credential) {
        list.remove(credential);
    }

    fun retrieve(): List<Credential> {
        return list
    }
}