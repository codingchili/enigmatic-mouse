package com.codingchili.mouse.enigma.secret

import java.time.ZonedDateTime

/**
 * @author Robin Duda
 */
data class Credential(val url: String, val username: String, val password: String) {
    val created : ZonedDateTime = ZonedDateTime.now()
}