package com.codingchili.mouse.enigma.model

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmFileException
import org.spongycastle.crypto.generators.SCrypt
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Manages the secure storage of credentials.
 */
object CredentialBank {
    private const val KEY_NAME = "bank_mouse"
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ITERATIONS = 65536
    private const val SALT_BYTES = 32
    private const val KDF_OUTPUT_BYTES = 64
    private const val REALM_SCHEMA_VERSION = 8L
    private const val REALM_NAME = "credentials_$REALM_SCHEMA_VERSION" // skip migration support for now.

    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE)
    private val listeners = ArrayList<() -> Unit>()
    private val random = SecureRandom()
    private var cache: MutableList<Credential> = ArrayList()

    private lateinit var cipher: Cipher
    private lateinit var preferences: MousePreferences

    fun initCipher(encrypt: Boolean) {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7, "AndroidKeyStoreBCWorkaround")

        if (encrypt) {
            generateTEEKey()
        }

        keyStore.load(null)

        // key cannot be used until after authentication.
        val secretKey = keyStore.getKey(KEY_NAME, null)

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(preferences.getTeeIv()))
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return salt
    }

    fun generateTEEKey() {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun generateKDFKey(secret: ByteArray, salt: ByteArray): ByteArray {
        val start = System.currentTimeMillis()
        val bytes = SCrypt.generate(secret, salt, ITERATIONS, 8, 1, KDF_OUTPUT_BYTES)

        Log.w(javaClass.name, "Generated derived key in " + (System.currentTimeMillis() - start) + "ms")
        return bytes
    }

    fun store(credential: Credential) {
        cache.remove(credential)
        cache.add(credential)

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(credential)
        realm.commitTransaction()
        realm.close()

        sortCache()
        onCacheUpdated()
    }

    fun retrieve(): List<Credential> {
        return cache
    }

    fun remove(credential: Credential) {
        cache.remove(credential)

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.where(credential.javaClass).equalTo("id", credential.id)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()
        realm.close()

        onCacheUpdated()
    }

    private fun sortCache() {
        cache = cache.asSequence()
                .sortedWith(compareBy({ !it.favorite }, { it.domain }))
                .toMutableList()
    }

    fun onChangeListener(callback: () -> Unit) {
        listeners.add(callback)
    }

    fun onCacheUpdated() {
        listeners.forEach { callback ->
            callback.invoke()
        }
    }

    fun setPreferences(preferences: MousePreferences) {
        this.preferences = preferences
    }

    fun decryptMasterKeyWithFingerprint() {
        val decryptedKey = cipher.doFinal(preferences.getEncryptedMaster())
        configureRealm(decryptedKey)
    }

    private fun configureRealm(key: ByteArray) {
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .encryptionKey(key)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .name(REALM_NAME)
                .build())

        cache.clear()

        val realm = Realm.getDefaultInstance()
        realm.where(Credential::class.java).findAll().forEach { credential ->
            cache.add(realm.copyFromRealm(credential))
        }
        sortCache()
        realm.close()
    }

    fun installWithFingerprint(password: String) {
        val key = installWithPassword(password)

        val spec = SecretKeySpec(key, "AES")
        val encryptedKey = cipher.doFinal(spec.encoded)

        preferences.setEncryptedMaster(encryptedKey)
                .setTeeIV(cipher.iv)
                .setFPSupported(true)
    }

    fun installWithPassword(password: String): ByteArray {
        val salt = CredentialBank.generateSalt()
        val key = CredentialBank.generateKDFKey(password.toByteArray(), salt)

        configureRealm(key)

        preferences.setMasterSalt(salt)
                .setFPSupported(false)
                .setInstalled()

        return key
    }

    fun unlockWithPassword(password: String): Boolean {
        return try {
            configureRealm(generateKDFKey(password.toByteArray(), preferences.getMasterSalt()))
            true
        } catch (e: RealmFileException) {
            if (e.kind == RealmFileException.Kind.ACCESS_ERROR) {
                false
            } else {
                throw e
            }
        }
    }

    fun getCipher(): Cipher {
        return cipher
    }

    fun uninstall() {
        preferences.reset()
        preferences.setClipboardWarned(false)
        try {
            Realm.deleteRealm(RealmConfiguration.Builder().name(REALM_NAME).build())
        } catch (e: Exception) {
            Log.w(javaClass.name, e.message)
        }
    }
}