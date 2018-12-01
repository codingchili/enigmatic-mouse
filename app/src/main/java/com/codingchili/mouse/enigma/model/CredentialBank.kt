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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
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
    private const val MAX_LOG_BUFFER = 256
    private const val SALT_BYTES = 32
    private const val KDF_OUTPUT_BYTES = 64
    private const val REALM_SCHEMA_VERSION = 10L
    private const val REALM_NAME = "credentials_$REALM_SCHEMA_VERSION" // skip migration support for now.

    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE)
    private val listeners = ArrayList<() -> Unit>()
    private val random = SecureRandom()
    private var cache : MutableList<Credential> = ArrayList()
    private var vault: Vault = Vault()

    private lateinit var cipher: Cipher
    private lateinit var preferences: MousePreferences
    private lateinit var realm : Realm

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

        realm.beginTransaction()
        vault.credentials.clear()
        vault.credentials.addAll(cache)
        realm.copyToRealmOrUpdate(vault)
        realm.commitTransaction()

        sortCache()
        onCacheUpdated()
    }

    private fun save() {
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(vault)
        realm.commitTransaction()
    }

    fun onFingerprintAuthenticated() {
        log("Authenticated using fingerprint.")
    }

    fun auditLog(): List<String> {
        return vault.log
    }

    fun onPasswordAuthenticate() {
        log("Authenticated using password.")
    }

    private fun log(line: String) {
        val timestamp: String = ZonedDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        if (vault.log.size > MAX_LOG_BUFFER) {
            vault.log.removeAt(vault.log.size -1)
        }


        realm.beginTransaction()
        vault.log.add(0, "$timestamp: $line")
        realm.commitTransaction()

        save()
    }

    fun retrieve(): List<Credential> {
        return cache
    }

    fun remove(credential: Credential) {
        cache.remove(credential)

        realm.beginTransaction()
        realm.where(credential.javaClass).equalTo("id", credential.id)
                .findAll()
                .deleteAllFromRealm()
        realm.commitTransaction()

        onCacheUpdated()
    }

    private fun sortCache() {
        cache = cache.asSequence()
                .sortedWith(compareBy({ !it.favorite }, { it.domain }))
                .toMutableList() as ArrayList
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

        realm = Realm.getDefaultInstance()
        realm.where(Vault::class.java).findAll().forEach { vault ->
            this.vault = vault

            this.vault.credentials.forEach { credential ->
                // todo: should be managed?
                cache.add(realm.copyFromRealm(credential))
            }
        }
        sortCache()
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

    fun pwnsByDomain(domain: String): List<PwnedSite> {
        val matches = ArrayList<PwnedSite>()

        for (pwnedSite in vault.pwned) {
            if (domain == pwnedSite.domain) {
                matches.add(pwnedSite)
            }
        }

        return matches
    }

    fun setPwnedList(pwned: Map<String, List<PwnedSite>>) {
        vault.pwned.clear()

        pwned.values.forEach { list ->
            vault.pwned.addAll(list)
        }
        save()
    }
}