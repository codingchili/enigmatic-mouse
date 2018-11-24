package com.codingchili.mouse.enigma.secret

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import org.spongycastle.crypto.generators.SCrypt
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

/**
 * @author Robin Duda
 */
class CredentialBank {
    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
    private val keyName = "bank_mouse"
    private val list: ArrayList<Credential> = ArrayList()
    private val random = SecureRandom()

    private lateinit var teeIV : ByteArray

    init {
        list.add(Credential("https://youtube.com/", "rduda@kth.se", "********"))
        list.add(Credential("https://google.com/", "rduda@kth.se", "********"))
    }

    fun setIv(teeIV: ByteArray) {
        this.teeIV = teeIV
    }

    fun masterKey(encrypt: Boolean): Cipher {
        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)

        keyStore.load(null)

        //key still cannot be used yet until authentication!
        val secretKey = keyStore.getKey(keyName, null)

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } else {
            Log.w("CredentialBankIV", "TEE_IV = " + String(teeIV))
            Log.w("CredentialBankTEE", "KEY = " + secretKey.algorithm)
            Log.w("CredentialBankTEE", "KEY = " + secretKey.format)
            //Log.w("CredentialBankTEE", "KEY = " + String(secretKey.encoded))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(teeIV))
        }

        return cipher
    }

    fun store(credential: Credential) {
        // 1. kdf on input password to verify master password.
        // 2. kdf to generate symmetric key.
        val key = generateKDFKey(credential.password.toByteArray())

        Log.w("", "generated = " + key)

        list.add(credential)

        list.sortBy {
            it.url
        }
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return salt
    }

    fun generateTEEKey() {
        // creates the key in the android keystore :)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(keyName,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun generateKDFKey(secret: ByteArray): ByteArray {
        return generateKDFKey(secret, generateSalt())
    }

    fun generateKDFKey(secret: ByteArray, salt: ByteArray): ByteArray {
        val start = System.currentTimeMillis()
        val bytes = SCrypt.generate(secret, salt, 256, 32, 2, 16)

        Log.w("CredentialsBank", "Generated KFF in " + (System.currentTimeMillis() - start) + "ms")
        return bytes
    }

    fun remove(credential: Credential) {
        list.remove(credential);
    }

    fun retrieve(): List<Credential> {
        return list
    }
}