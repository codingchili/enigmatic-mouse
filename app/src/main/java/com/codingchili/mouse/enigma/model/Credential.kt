package com.codingchili.mouse.enigma.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Data model for Credential information.
 */
open class Credential(): RealmObject() {

    constructor(domain: String, username: String, password: String) : this() {
        this.domain = domain
        this.username = username
        this.password = password
    }

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var created : String = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE)
    var domain: String = ""
    var username: String = ""
    var password: String = ""
    var favorite: Boolean = false
    var pwns = RealmList<PwnedSite>()

    override fun equals(other: Any?): Boolean {
        return other != null && id == (other as Credential).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}