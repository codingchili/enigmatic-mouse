package com.codingchili.mouse.enigma.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Contains information about a domain that has been pwned.
 */
private const val DOMAIN = "Domain"
private const val ADDED_DATE = "AddedDate"
private const val BREACH_DATE = "BreachDate"
private const val DESCRIPTION = "Description"

open class PwnedSite: RealmObject {
    lateinit var domain: String
    lateinit var description: String
    lateinit var added: String
    lateinit var discovered: String
    var acknowledged = false

    constructor() {
        // no-args Realm constructor.
    }

    constructor(json: JSONObject) {
        domain = json.getString(DOMAIN)
        description = json.getString(DESCRIPTION)
        added = json.getString(ADDED_DATE)
        discovered = json.getString(BREACH_DATE)
    }

    fun addedAfter(date: ZonedDateTime): Boolean {
        return ZonedDateTime.parse(added, DateTimeFormatter.ISO_DATE_TIME).isAfter(date)
    }

    override fun equals(other: Any?): Boolean {
        val casted = (other as PwnedSite)
        return casted.domain == domain && casted.discovered == discovered
    }

    override fun hashCode(): Int {
        return ("$domain.$discovered").hashCode()
    }

    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
}
