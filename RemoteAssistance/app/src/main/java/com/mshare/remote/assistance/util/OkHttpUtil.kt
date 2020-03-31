package com.mshare.remote.assistance.util

import com.mshare.remote.assistance.Constants
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request

object OkHttpUtil {
    fun baseGet(url:String, params: Map<*, *>, callback: Callback) {
        val client = Constants.httpClient
        val httpUrl: HttpUrl.Builder = url.toHttpUrlOrNull()?.newBuilder()
                ?: return
        for(entry in params.entries){
            httpUrl.addQueryParameter(entry.key as String,entry.value as String)
        }
        val request: Request = Request.Builder()
                .url(httpUrl.build())
                .build()
        client.newCall(request).enqueue(callback)

    }

    fun basePost(url:String, params: Map<*, *>, callback: Callback) {
        val client = Constants.httpClient
        /*val obj = JSONObject()
        for(entry in params.entries){
            obj.put(entry.key as String,entry.value as String)
        }
        val requestBody = obj.toString().toRequestBody(Constants.MEDIA_TYPE_JSON.toMediaType())*/
        val formBody = FormBody.Builder()
        for(entry in params.entries){
            formBody.add(entry.key as String,entry.value as String)
        }
        val requestBody = formBody.build()
        val request: Request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        client.newCall(request).enqueue(callback)
    }
}