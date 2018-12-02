package com.codingchili.mouse.enigma.model

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import org.spongycastle.crypto.generators.SCrypt
import java.security.KeyStore
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    private const val REALM_SCHEMA_VERSION = 13L
    private const val REALM_NAME = "credentials_$REALM_SCHEMA_VERSION" // skip migration support for now.

    private val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE)
    private val listeners = ArrayList<() -> Unit>()
    private val random = SecureRandom()
    private var cache: MutableList<Credential> = ArrayList()
    private var vault: Vault = Vault()

    private lateinit var cipher: Cipher
    private lateinit var preferences: MousePreferences
    private lateinit var key: ByteArray

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
        var bytes = ByteArray(0)

        Performance("CredentialBank:generateKey").sync({
            bytes = SCrypt.generate(secret, salt, ITERATIONS, 8, 1, KDF_OUTPUT_BYTES)
        })
        return bytes
    }

    fun store(credential: Credential) {
        cache.remove(credential)
        cache.add(credential)
        sortCache()
        onCacheUpdated()

        vault.credentials.remove(credential)
        vault.credentials.add(credential)
        save()
    }

    fun auditLog(): List<String> {
        return vault.log
    }

    fun log(line: String) {
        val timestamp: String = ZonedDateTime.now()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        vault.log.add(0, "$timestamp: $line")

        if (vault.log.size > MAX_LOG_BUFFER) {
            vault.log.removeAt(vault.log.size - 1)
        }

        save()
    }

    fun retrieve(): List<Credential> {
        return cache
    }

    fun remove(credential: Credential) {
        cache.remove(credential)
        onCacheUpdated()

        Realm.getDefaultInstance().use {
            it.executeTransactionAsync { realm ->
                realm.where(credential.javaClass).equalTo(ID_FIELD, credential.id)
                        .findAll()
                        .deleteAllFromRealm()
            }
        }
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

    /**
     * Connect to the realm instance - requires calling any of the install
     * or unlock methods first. Must be called from the UI thread.
     */
    fun connect(): Boolean {
        Realm.setDefaultConfiguration(RealmConfiguration.Builder()
                .encryptionKey(key)
                .schemaVersion(REALM_SCHEMA_VERSION)
                .name(REALM_NAME)
                .build())
        try {
            cache.clear()

            Realm.getDefaultInstance().use {
                val found = it.where(Vault::class.java)
                        .equalTo(
                                NAME_FIELD,
                                DEFAULT_NAME)
                        .findFirst()

                if (found == null) {
                    vault = Vault()
                } else {
                    this.vault = it.copyFromRealm(found)
                }

                cache.addAll(vault.credentials)
            }
            sortCache()
            return true
        } catch (e: Exception) {
            Log.wtf(javaClass.name, e)
            return false
        }
    }

    fun installWithFingerprint(password: String) {
        installWithPassword(password)

        val spec = SecretKeySpec(key, "AES")
        val encryptedKey = cipher.doFinal(spec.encoded)

        preferences.setEncryptedMaster(encryptedKey)
                .setTeeIV(cipher.iv)
                .setFPSupported(true)
    }

    fun installWithPassword(password: String) {
        val salt = CredentialBank.generateSalt()
        key = CredentialBank.generateKDFKey(password.toByteArray(), salt)

        preferences.setMasterSalt(salt)
                .setFPSupported(false)
                .setInstalled()
    }

    fun unlockWithFingerprint() {
        key = cipher.doFinal(preferences.getEncryptedMaster())
    }

    fun unlockWithPassword(password: String) {
        key = generateKDFKey(password.toByteArray(), preferences.getMasterSalt())
    }

    fun getCipher(): Cipher {
        return cipher
    }

    fun uninstall() {
        preferences.reset()
        try {
            if (!Realm.deleteRealm(RealmConfiguration.Builder().name(REALM_NAME).build())) {
                Log.w(javaClass.name, "Failed to delete realm.")
            }
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


    private fun save() {
        Realm.getDefaultInstance().use {it ->
            it.executeTransactionAsync {
                it.copyToRealmOrUpdate(vault)
            }
        }
    }

    fun setPwnedList(pwned: Map<String, List<PwnedSite>>) {
        pwned.values.forEach { list ->
            list.forEach { domain ->
                if (!vault.pwned.contains(domain)) {
                    vault.pwned.add(domain)
                }
            }
        }
        save()
    }

    fun acknowledge(pwn: PwnedSite) {
        pwn.acknowledged = true
        save()
    }
}