package com.codingchili.mouse.enigma.model

import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Contains information about a domain that has been pwned.
 */
private const val DOMAIN = "Domain"
private const val ADDED_DATE = "AddedDate"
private const val BREACH_DATE = "BreachDate"
private const val DESCRIPTION = "Description"

data class PwnedSite(val json: JSONObject) {
    var domain: String = json.getString(DOMAIN)
    var description: String = json.getString(DESCRIPTION)
    var added: ZonedDateTime = ZonedDateTime.parse(json.getString(ADDED_DATE),
            DateTimeFormatter.ISO_DATE_TIME)
    var discovered: LocalDate = LocalDate.parse(json.getString(BREACH_DATE),
            DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()))
}
