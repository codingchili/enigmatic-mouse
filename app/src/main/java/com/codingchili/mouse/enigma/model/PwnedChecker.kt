package com.codingchili.mouse.enigma.model

import android.app.Application
import android.util.Log
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import java.time.ZonedDateTime

private const val API = "https://haveibeenpwned.com/api/v2/breaches"

/**
 * Downloads the haveibeenpwned list of domains to check if any breaches
 * has occurred since last time scanning.
 */
class PwnedChecker(application: Application) {
    private var preferences = MousePreferences(application)

    fun check(sites: List<String>, callback: (Map<String, List<PwnedSite>>) -> Unit, error: (Throwable) -> Unit) {
        val client = AsyncHttpClient()
        val params = RequestParams()

        try {
            client.get(API, params, object : AsyncHttpResponseHandler() {

                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                    if (responseBody != null) {
                        val pwned = HashMap<String, ArrayList<PwnedSite>>(sites.size)
                        val json = JSONArray(String(responseBody))
                        val add = { site: PwnedSite ->
                            if (sites.contains(site.domain)) {
                                pwned.computeIfAbsent(site.domain) { ArrayList() }
                                pwned[site.domain]!!.add(site)
                            }
                        }

                        for (index in 0 until json.length()) {
                            val site = PwnedSite(json.getJSONObject(index))

                            add.invoke(site)
                            Log.w("PwnedChecker", "Detected breach on domain: " + site.domain)
                        }
                        preferences.setLastPwnedCheck(ZonedDateTime.now())
                        callback.invoke(pwned)
                    }
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, exception: Throwable?) {
                    if (exception == null) {
                        error.invoke(Error("HTTP " + statusCode.toString()))
                    } else {
                        error.invoke(exception)
                    }
                }
            })
        } catch (e: Exception) {
            error.invoke(e)
        }
    }
}
