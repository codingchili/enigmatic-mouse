package com.codingchili.mouse.enigma.model

import android.util.Log

/**
 * Utility class for logging performance metrics.
 */
class Performance(val name: String) {
    private var start: Long = 0L

    fun async(block: (() -> Unit) -> Unit) {
        sync({
            block.invoke {
                complete()
            }
        }, true)
    }

    fun sync(block: () -> Unit, async: Boolean = false) {
        start = System.currentTimeMillis()

        block.invoke()
        if (!async) {
            complete()
        }
    }

    fun complete() {
        Log.w(javaClass.name, "$name completed in ${System.currentTimeMillis() - start} ms.")
    }
}
