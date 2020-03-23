package com.mshare.remote.assistance
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

object Constants{
    const val DEBUG_MODE = true
    const val SHARES_FILE = "assistance"
    //private const val SERVER_LOCAL_BASE = "://192.168.0.109:5000/"
    private const val SERVER_LOCAL_BASE = "://172.16.200.114:5000/"
    private const val SERVER_REMOTE_BASE = "://mshare.frp1.chuantou.org:58000/"
    private const val WS_SERVER_LOCAL:String = "ws"+ SERVER_LOCAL_BASE + "socket"
    private const val WS_SERVER_REMOTE:String = "ws"+ SERVER_REMOTE_BASE + "socket"
    const val WS_MSG_COMMAND_SELF:String = "command"
    const val WS_MSG_COMMAND_HELP:String = "start_help"
    const val WS_MSG_COMMAND_END:String = "end_help"
    const val WS_MSG_COMMAND_INVALID:String = "invalid"
    const val WS_MSG_TOKEN_SELF:String = "token"
    const val WS_MSG_TOKEN_GOAL:String = "goal_token"
    const val DATA_TYPE_IMAGE:Byte = 0
    const val DATA_TYPE_AUDIO:Byte = 1
    const val WS_CONN_RETRY_TIMES = 5;
    private const val READ_TIMEOUT:Long = 5
    private const val CONN_TIMEOUT:Long = 5

    fun getWSServerHost():String {
        return if(DEBUG_MODE) WS_SERVER_LOCAL else WS_SERVER_REMOTE
    }

    fun createWebSocket(webUrl: String, wsl: WebSocketListener, params: Map<String, String>)  {
        if(TextUtils.isEmpty(webUrl)) {
            return
        }
        val httpClient = OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONN_TIMEOUT, TimeUnit.SECONDS)
            .build()
        var url:String? = null
        val map = HashMap<String, String>(params)
        for(entry in map.entries){
            if(url == null) {
                url = "?" + entry.key + "=" + entry.value
            } else {
                url = "&" + entry.key + "=" + entry.value
            }
        }
        url = webUrl + url
        Log.d("wenpd", "createWebSocket:" + url)
        val request = Request.Builder().url(url).build()
        httpClient.newWebSocket(request, wsl)
        httpClient.dispatcher.executorService.shutdown()
    }

    val httpClient = OkHttpClient()
    private const val HTTP_SERVER_LOCAL = "http"+ SERVER_LOCAL_BASE + "friend"
    private const val HTTP_SERVER_REMOTE = "http"+ SERVER_REMOTE_BASE + "friend"
    const val MEDIA_TYPE_JSON = "application/json"
    const val ERROR_TYPE_NET = -1
    const val ERROR_TYPE_JSON = -2
    const val ERROR_TYPE_ADD = -3
    const val ERROR_TYPE_DEL = -4

    private fun getServerHost():String{
        return if(DEBUG_MODE) HTTP_SERVER_LOCAL else HTTP_SERVER_REMOTE
    }

    fun getFriendListUrl():String {
        return getServerHost() + "/list"
    }

    fun getFriendAddUrl():String {
        return getServerHost() + "/add"
    }

    fun getFriendDelUrl():String {
        return getServerHost() + "/delete"
    }

    private fun generateUserToken():String{
        val token:String = System.currentTimeMillis().toString()
        var md5Token:String? = null
        try{
            val md = MessageDigest.getInstance("md5");
            val md5 = md.digest(token.toByteArray());
            md5Token = Base64.encodeToString(md5, 0)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return md5Token?:""
    }

    private fun saveUserToken(context:Context, token:String) {
        context.getSharedPreferences(Constants.SHARES_FILE, Context.MODE_PRIVATE).edit()
                .putString(Constants.WS_MSG_TOKEN_SELF, token)
                .apply()
    }

    fun getUserToken(context:Context):String {
        val sp: SharedPreferences = context.getSharedPreferences(Constants.SHARES_FILE, Context.MODE_PRIVATE)
        var token = sp.getString(Constants.WS_MSG_TOKEN_SELF, "")
        if(token == "") {
            token = Constants.generateUserToken()
            saveUserToken(context, token)
        }
        return token?:return ""
    }

    fun checkUserToken(token:String):Boolean {
        return true
    }
}