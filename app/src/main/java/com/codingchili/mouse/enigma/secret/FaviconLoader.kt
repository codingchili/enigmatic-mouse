package com.codingchili.mouse.enigma.secret

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.TypedValue
import com.jakewharton.disklrucache.DiskLruCache
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import cz.msebera.android.httpclient.Header
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.OutputStream
import kotlin.math.roundToInt


/**
 * Loads the favicon of the given url.
 */
class FaviconLoader {
    private var cache: DiskLruCache
    private val context: Context

    constructor(_context: Context) {
        context = _context
        cache = DiskLruCache.open(context.cacheDir, 2, 1, 512_000_000) // 64MB disk cache.
    }

    fun load(site: String, callback: (Bitmap) -> Unit, error: (Throwable) -> Unit) {
        val cached: DiskLruCache.Snapshot? = cache.get(site.hashCode().toString())

        if (cached != null) {
            Log.w("FaviconLoader", "IS IN CACHE: " + site.hashCode().toString())
            callback.invoke(BitmapFactory.decodeStream(cached.getInputStream(0)))
        } else {
            Log.w("FaviconLoader", "IS NOT IN CACHE: " + site.hashCode().toString())
            loadIconReferences(site, callback, error)
        }
    }

    private fun loadIconReferences(site: String, callback: (Bitmap) -> Unit, error: (Throwable) -> Unit) {
        val client = AsyncHttpClient()
        val params = RequestParams()

        client.get(site, params, object : AsyncHttpResponseHandler() {

            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {
                if (responseBody != null) {
                    val document: Document = Jsoup.parse(String(responseBody))

                    // as loaded by browsers if no link-rel icon present.
                    var largestLogoHref = "$site/favicon.ico"
                    var largestIconSize = 0

                    // find the biggest logo in the index HTML document.
                    document.getElementsByTag("link").forEach { element ->
                        val rel = element.attr("rel")

                        Log.w("FaviconLoader", "element: " + element.toString())

                        if (rel.contains("icon") || rel.contains("shortcut") || rel.contains("apple-touch-icon")) {
                            var size = 1

                            if (element.hasAttr("sizes")) {
                                size = Integer.parseInt(element.attr("sizes").split("x")[0])
                            }

                            if (size > largestIconSize) {
                                largestLogoHref = element.attr("href")
                                largestIconSize = size
                            }
                        }
                    }

                    Log.w("FaviconLoader", "biggest logo chosen from $largestLogoHref size was $largestIconSize")

                    // support use-current-protocol type of links, but always default to https!
                    if (largestLogoHref!!.startsWith("//")) {
                        largestLogoHref = largestLogoHref!!.replace("//", "https://")
                    }

                    // prepend hostname if absolute url.
                    if (largestLogoHref!!.startsWith("/")) {
                        largestLogoHref = "$site$largestLogoHref"
                    }

                    // prepend protocol and hostname if relative url.
                    if (!largestLogoHref!!.startsWith("https://")) {
                        largestLogoHref = "$site/$largestLogoHref"
                    }

                    loadImageFromNetwork(site, largestLogoHref!!, callback, error)
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, exception: Throwable?) {
                if (exception == null) {
                    // do nothing: site may not be a valid url.
                } else {
                    error.invoke(exception)
                }

            }

        })
    }

    private fun loadImageFromNetwork(site: String, imageUrl: String, callback: (Bitmap) -> Unit, error: (Throwable) -> Unit) {
        val client = AsyncHttpClient()

        client.get(imageUrl, RequestParams(), object : AsyncHttpResponseHandler() {

            override fun onStart() {
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray) {
                // should match the layout.
                val dip = 96f
                val r = context.resources
                val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                ).roundToInt()

                // decode response into a bitmap and scale it.
                var logo: Bitmap = BitmapFactory.decodeByteArray(response, 0, response.size)
                logo = Bitmap.createScaledBitmap(logo, px, px, true)

                // callback before writing to disk.
                callback.invoke(logo)

                // compress to webp and store on disk.
                val editor: DiskLruCache.Editor = cache.edit(site.hashCode().toString())
                val out: OutputStream = editor.newOutputStream(0)
                logo.compress(Bitmap.CompressFormat.WEBP, 100, out)
                editor.commit()
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, errorResponse: ByteArray, e: Throwable) {
                error.invoke(e)
            }

            override fun onRetry(retryNo: Int) {
                // called when request is retried
            }
        })
    }

}