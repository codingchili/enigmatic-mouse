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
import java.io.OutputStream
import kotlin.math.roundToInt


/**
 * Loads the favicon of the given url.
 */
class FaviconLoader {
    private val api : String = "https://realfavicongenerator.p.mashape.com/favicon/icon"
    private var cache: DiskLruCache
    private val context: Context

    constructor(_context: Context) {
        context = _context
        cache = DiskLruCache.open(context.cacheDir, 1, 1, 256_000_000) // 32MB disk cache.
    }

    fun load(site: String, callback: (Bitmap) -> Unit, error: (Throwable) -> Unit) {
        val cached: DiskLruCache.Snapshot? = cache.get(site.hashCode().toString())

        if (cached != null) {
            Log.w("asd", "IS IN CACHE: " + site.hashCode().toString())
            callback.invoke(BitmapFactory.decodeStream(cached.getInputStream(0)))
        } else {
            Log.w("asd", "IS NOT IN CACHE: " + site.hashCode().toString())
            loadFromNetwork(site, callback, error)
        }
    }

    private fun loadFromNetwork(site: String, callback: (Bitmap) -> Unit, error: (Throwable) -> Unit) {
        val client = AsyncHttpClient()

        client.addHeader("X-Mashape-Key", "<your api key>")
        val params = RequestParams()
        params.add("site", site)

        client.get(api, params, object : AsyncHttpResponseHandler() {

            override fun onStart() {
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: ByteArray) {
                // should match the layout.
                val dip = 46f
                val r = context.resources
                val px = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        dip,
                        r.displayMetrics
                ).roundToInt()

                // decode response into a bitmap and scale it.
                var logo : Bitmap = BitmapFactory.decodeByteArray(response, 0, response.size)
                logo = Bitmap.createScaledBitmap(logo, px, px, true)

                // callback before writing to disk.
                callback.invoke(logo)

                // compress to webp and store on disk.
                val editor: DiskLruCache.Editor = cache.edit(site.hashCode().toString())
                val out : OutputStream = editor.newOutputStream(0)
                logo.compress(Bitmap.CompressFormat.WEBP, 100, out)
                editor.commit()
                Log.w("asd", "commit called: " + site.hashCode().toString())
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