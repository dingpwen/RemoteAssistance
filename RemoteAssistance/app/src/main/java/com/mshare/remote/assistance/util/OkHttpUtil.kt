package com.mshare.remote.assistance.util

import android.content.Context
import android.text.TextUtils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkHttpUtil {
    private val httpBuilder = OkHttpClient.Builder()
    private var initial = false
    private const val READ_TIMEOUT: Long = 5
    private const val WRITE_TIMEOUT: Long = 5
    private const val CONN_TIMEOUT: Long = 5
    private const val CACHE_SIZE: Long = 10 * 1024 * 1024
    private val noCacheSet = mutableSetOf<String>()
    private val defaultCacheControl =
        CacheControl.Builder().maxAge(360, TimeUnit.SECONDS).build()

    @Synchronized fun initHttpClientCache(context: Context) {
        if(!initial) {
            val interceptor = CacheInterceptor(context)
            httpBuilder.cache(Cache(context.cacheDir, CACHE_SIZE))
                .addNetworkInterceptor(interceptor)
            initial = true
        }
    }

    @Synchronized fun addToNoCacheSet(url: String) {
        noCacheSet.add(url)
    }

    @Synchronized fun removeFromNoCacheSet(url: String) {
        noCacheSet.remove(url)
    }

    fun addParamToUrl(url: String, params: Map<*, *>?): String {
        if(params == null) {
            return url
        }
        val httpUrl: HttpUrl.Builder = url.toHttpUrlOrNull()?.newBuilder()
            ?: return url
        for(entry in params.entries){
            httpUrl.addQueryParameter(entry.key as String,entry.value as String)
        }
        return httpUrl.build().toString()
    }

    fun baseGet(url:String, params: Map<*, *>, callback: Callback) {
        val httpClient = httpBuilder.build()
        val noCache = noCacheSet.contains(url)
        val request: Request = Request.Builder()
            .url(addParamToUrl(url, params))
            .cacheControl(if(noCache) CacheControl.FORCE_NETWORK else defaultCacheControl)
            .build()
        httpClient.newCall(request).enqueue(callback)
    }

    fun basePost(url:String, params: Map<*, *>, callback: Callback) {
        val httpClient = httpBuilder.build()
        val formBody = FormBody.Builder()
        for(entry in params.entries){
            formBody.add(entry.key as String,entry.value as String)
        }
        val requestBody = formBody.build()
        val request: Request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        httpClient.newCall(request).enqueue(callback)
    }

    fun baseSyncGet(url: String, params: Map<*, *>?): String? {
        val httpClient = httpBuilder.build()
        val noCache = noCacheSet.contains(url)
        var result: String? = null
        val httpUrl: String = addParamToUrl(url, params)
        val request = Request.Builder()
            .url(httpUrl)
            .cacheControl(if(noCache) CacheControl.FORCE_NETWORK else defaultCacheControl)
            .build()
        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                noCacheSet.remove(url)
                result = response.body!!.string()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun baseSyncPost(url:String, params: Map<*, *>): String? {
        val httpClient = httpBuilder.build()
        var result: String? = null
        val formBody = FormBody.Builder()
        for(entry in params.entries){
            formBody.add(entry.key as String,entry.value as String)
        }
        val requestBody = formBody.build()
        val request: Request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                result = response.body!!.string()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    fun createWebSocket(webUrl: String?, wsl: WebSocketListener?, params: Map<*, *>?) {
        if (TextUtils.isEmpty(webUrl)) {
            return
        }
        val httpClient = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
            .build()
        var url = addParamToUrl("http://localhost/test", params)
        url = url.replace("http://localhost/test", webUrl!!, true)
        val request = Request.Builder().url(url).build()
        httpClient.newWebSocket(request, wsl!!)
        httpClient.dispatcher.executorService.shutdown()
    }
}