package com.codingchili.mouse.enigma.model

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import org.spongycastle.crypto.generators.SCrypt
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * @author Robin Duda
 *
 * Manages the secure storage of credentials.
 */
object CredentialBank {
    private const val KEY_NAME = "bank_mouse"
    private const val KEYSTORE = "AndroidKeyStore"
    private const val ITERATIONS = 1024
    private const val SALT_BYTES = 32
    private const val KDF_OUTPUT_BITS = 512
    private const val REALM_SCHEMA_VERSION = 6L
    private const val REALM_NAME = "credentials_$REALM_SCHEMA_VERSION" // skip migration support for now.

    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE)
    private val listeners = ArrayList<() -> Unit>()
    private val random = SecureRandom()
    private var list: MutableList<Credential> = ArrayList()

    private lateinit var cipher: Cipher
    private lateinit var preferences: MousePreferences

    fun initCipher(encrypt: Boolean) {
        cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7, "AndroidKeyStoreBCWorkaround")

        keyStore.load(null)

        // key cannot be used until after authentication.
        val secretKey = keyStore.getKey(KEY_NAME, null)

        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(preferences.getTeeIv()))
        }
    }

    fun store(credential: Credential) {
        list.remove(credential)
        list.add(credential)

        // realms are cached so we can look it up here android warns when set as a member field.
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(credential)
        realm.commitTransaction()
        realm.close()

        list = list.asSequence()
                .sortedWith(compareBy({ !it.favorite }, { it.site }))
                .toMutableList()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        random.nextBytes(salt)
        return salt
    }

    fun generateTEEKey() {
        // creates the key in the android keystore :)
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
        val bytes = SCrypt.generate(secret, salt, ITERATIONS, 32, 2, KDF_OUTPUT_BITS)

        Log.w(javaClass.name, "Generated derived key in " + (System.currentTimeMillis() - start) + "ms")
        return bytes
    }

    fun retrieve(): List<Credential> {
        return list
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

    fun load() {
        val decryptedKey = cipher.doFinal(preferences.getEncryptedMaster())
        configureRealm(decryptedKey)
    }

    private fun configureRealm(key: ByteArray) {
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .encryptionKey(key)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .name(REALM_NAME)
                .build())

        list.clear()

        // todo: implement migration here.
        val realm = Realm.getDefaultInstance()
        realm.where(Credential::class.java).findAll().forEach { credential ->
            list.add(realm.copyFromRealm(credential))
        }
        realm.close()
    }

    fun install(password: String) {
        val salt = CredentialBank.generateSalt()
        val key = CredentialBank.generateKDFKey(password.toByteArray(), salt)
        val spec = SecretKeySpec(key, "AES")

        val encryptedKey = cipher.doFinal(spec.encoded)
        configureRealm(key)

        preferences.setMasterSalt(salt)
                .setTeeIV(cipher.iv)
                .setEncryptedMaster(encryptedKey)
                .setTeeGenerated()
    }

    fun getCipher(): Cipher {
        return cipher
    }

    fun uninstall() {
        preferences.unsetTeeGenerated()
        try {
            Realm.deleteRealm(RealmConfiguration.Builder().name(REALM_NAME).build())
        } catch (e: Exception) {
            Log.w(javaClass.name, e.message)
        }
    }
}