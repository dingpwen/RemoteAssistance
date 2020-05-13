package com.mshare.remote.assistance.util

import android.text.TextUtils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkHttpUtil {
    private var httpClient = OkHttpClient()
    private const val READ_TIMEOUT: Long = 5
    private const val WRITE_TIMEOUT: Long = 5
    private const val CONN_TIMEOUT: Long = 5

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
        val request: Request = Request.Builder()
                .url(addParamToUrl(url, params))
                .build()
        httpClient.newCall(request).enqueue(callback)

    }

    fun basePost(url:String, params: Map<*, *>, callback: Callback) {
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
        var result: String? = null
        val httpUrl: String = addParamToUrl(url, params)
        val request = Request.Builder()
            .url(httpUrl)
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