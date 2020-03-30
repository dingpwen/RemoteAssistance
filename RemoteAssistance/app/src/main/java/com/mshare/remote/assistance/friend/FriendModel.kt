package com.mshare.remote.assistance.friend
import com.mshare.remote.assistance.Constants
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject

class FriendModel:Contact.IModel {
    private fun baseGet(url:String, params: Map<*, *>, callback: Callback) {
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

    private fun basePost(url:String, params: Map<*, *>, callback: Callback) {
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

    override fun loadData(params: Map<*, *>, callback: Callback) {
        baseGet(Constants.getFriendListUrl(), params, callback)
    }

    override fun addOrRemoveFriend(params: Map<*, *>, callback: Callback, type: Int) {
        when(type) {
            1 -> basePost(Constants.getFriendAddUrl(), params, callback)
            2 -> basePost(Constants.getFriendDelUrl(), params, callback)
            else -> {
                return
            }
        }
    }

    override fun addUser(params: Map<*, *>, callback: Callback, type:Int) {
        when(type) {
            1 -> basePost(Constants.getUserRegisterUrl(), params, callback)
            2 -> basePost(Constants.getUserAddUrl(), params, callback)
            else -> {
                return
            }
        }
    }
}