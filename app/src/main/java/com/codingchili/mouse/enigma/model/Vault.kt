package com.codingchili.mouse.enigma.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Wraps a set of credentials inside a vault. We can allow users
 * to switch between vaults later - and we can set and store
 * metadata securely - like an audit log for example.
 */
open class Vault: RealmObject() {

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name =  "default"
    var credentials = RealmList<Credential>()
    var log = RealmList<String>()
    var pwned = RealmList<PwnedSite>()
}
