package com.codingchili.mouse.enigma.secret

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Robin Duda
 */
open class Credential(): RealmObject() {

    constructor(site: String, username: String, password: String) : this() {
        this.site = site
        this.username = username
        this.password = password
    }

    @PrimaryKey
    var id: String = ""
    var created : String = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE)
    var site: String = ""
    var username: String = ""
    var password: String = ""

    companion object {
        fun getPrimaryKey() : String {
            return UUID.randomUUID().toString()
        }
    }
}