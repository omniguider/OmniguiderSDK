package com.omni.navisdk.network

import android.graphics.Bitmap
import android.util.LruCache
import com.android.volley.toolbox.ImageLoader

class LruImageCache private constructor() : ImageLoader.ImageCache {
    override fun getBitmap(arg0: String): Bitmap? {
        return mMemoryCache!![arg0]
    }

    override fun putBitmap(arg0: String, arg1: Bitmap) {
        if (getBitmap(arg0) == null) {
            mMemoryCache!!.put(arg0, arg1)
        }
    }

    companion object {
        private var mMemoryCache: LruCache<String, Bitmap>? = null
        private var lruImageCache: LruImageCache? = null
        val instance: LruImageCache?
            get() {
                if (lruImageCache == null) {
                    lruImageCache = LruImageCache()
                }
                return lruImageCache!!
            }
    }

    init {
        // Get the Max available memory
        val maxMemory = Runtime.getRuntime().maxMemory().toInt()
        val cacheSize = maxMemory / 8
        if (mMemoryCache == null) {
            mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(key: String?, bitmap: Bitmap): Int {
                    return bitmap.rowBytes * bitmap.height
                }
            }
        }
    }
}