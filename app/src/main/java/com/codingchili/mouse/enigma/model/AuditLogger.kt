package com.codingchili.mouse.enigma.model

import android.content.Context
import com.codingchili.mouse.enigma.R
import com.codingchili.mouse.enigma.model.CredentialBank.log

/**
 * Utility class to handle audit logging.
 */
object AuditLogger {

    fun onFingerprintAuthenticated(context: Context) {
        log(context.getString(R.string.audit_fingerprint_authenticated))
    }

    fun onPasswordAuthenticate(context: Context) {
        log(context.getString(R.string.audit_password_authenticated))
    }

    fun onPwnedListUpdated(context: Context) {
        log(context.getString(R.string.audit_security_list_update))
    }

    fun onPasswordDisplayed(context: Context, credential: Credential) {
        log(context.getString(R.string.audit_displayed_password)
                .format(credential.username, credential.domain))
    }

    fun onCopiedToClipboard(context: Context, credential: Credential) {
        log(context.getString(R.string.audit_coped_password)
                .format(credential.username, credential.domain))
    }

    fun onAddedCredential(context: Context, credential: Credential) {
        log(context.getString(R.string.audit_added_credential)
                .format(credential.username, credential.domain))
    }

    fun onRemovedCredential(context: Context, credential: Credential) {
        log(context.getString(R.string.audit_removed_credential)
                .format(credential.username, credential.domain))
    }
}
