package com.codingchili.mouse.enigma.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Robin Duda
 *
 * Data model for Credential information.
 */
open class Credential(): RealmObject() {

    constructor(site: String, username: String, password: String) : this() {
        this.site = site
        this.username = username
        this.password = password
    }

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var created : String = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE)
    var site: String = ""
    var username: String = ""
    var password: String = ""
    var favorite: Boolean = false

    override fun equals(other: Any?): Boolean {
        return other != null && id == (other as Credential).id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}