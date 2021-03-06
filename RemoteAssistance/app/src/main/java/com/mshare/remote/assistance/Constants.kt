package com.mshare.remote.assistance
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import com.mshare.remote.assistance.util.OkHttpUtil
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Constants{
    private var DEBUG_MODE = false
    private const val SHARES_FILE = "assistance"
    //private const val SERVER_LOCAL_BASE = "://192.168.0.109:5000/"
    private const val SERVER_LOCAL_BASE = "://172.16.200.206:5000/"
    private const val SERVER_REMOTE_BASE = "://124.70.140.183:5000/"
    private const val WS_SERVER_LOCAL:String = "ws"+ SERVER_LOCAL_BASE + "socket"
    private const val WS_SERVER_REMOTE:String = "ws"+ SERVER_REMOTE_BASE + "socket"
    const val WS_MSG_COMMAND_SELF:String = "command"
    const val WS_MSG_COMMAND_HELP:String = "start_help"
    const val WS_MSG_COMMAND_END:String = "end_help"
    const val WS_MSG_COMMAND_INVALID:String = "invalid"
    const val WS_MSG_TOKEN_SELF:String = "token"
    const val WS_MSG_TOKEN_GOAL:String = "goal_token"
    const val USER_TOKEN_CATEGORY:String = "category"
    const val DATA_TYPE_IMAGE:Byte = 0
    const val DATA_TYPE_AUDIO:Byte = 1
    const val WS_CONN_RETRY_TIMES = 5

    const val PROJECT = "Assistance"
    const val PACKAGE = "com.mshare.remote.assistance"

    fun getWSServerHost():String {
        return if(DEBUG_MODE) WS_SERVER_LOCAL else WS_SERVER_REMOTE
    }

    private const val HTTP_SERVER_LOCAL = "http$SERVER_LOCAL_BASE"
    private const val HTTP_SERVER_REMOTE = "http$SERVER_REMOTE_BASE"
    private const val URL_FRIEND_BASE = "friend"
    private const val URL_USER_BASE = "user"
    private const val URL_IMAGE_BASE = "image"
    private const val USER_TOKEN_FIX = "wveinwpadlakm@I"
    private const val USER_TOKEN_POS = 10
    const val ERROR_TYPE_NET = -1
    const val ERROR_TYPE_JSON = -2
    const val ERROR_TYPE_ADD = -3
    const val ERROR_TYPE_DEL = -4
    const val ERROR_TYPE_COMM = -5
    const val ERROR_INVALID_TOKEN = -6

    private fun getServerHost():String{
        return if(DEBUG_MODE) HTTP_SERVER_LOCAL else HTTP_SERVER_REMOTE
    }

    fun getFriendListUrl():String {
        return getServerHost() + URL_FRIEND_BASE + "/list"
    }

    fun getFriendAddUrl():String {
        return getServerHost() + URL_FRIEND_BASE + "/add"
    }

    fun getFriendDelUrl():String {
        return getServerHost() + URL_FRIEND_BASE + "/delete"
    }

    fun getUserLoginUrl():String {
        return getServerHost() + URL_USER_BASE + "/login"
    }

    fun getUserAddUrl():String {
        return getServerHost() + URL_USER_BASE + "/add"
    }

    fun getUserRegisterUrl():String {
        return getServerHost() + URL_USER_BASE + "/register"
    }

    fun getUserUpdateUrl():String {
        return getServerHost() + URL_USER_BASE + "/update"
    }


    fun getUserInfoUrl():String {
        return getServerHost() + URL_USER_BASE + "/info"
    }

    fun getImageUploadUrl():String {
        return getServerHost() + URL_IMAGE_BASE + "/upload"
    }

    fun getImageUrl(context: Context, url:String):String {
        val token = getUserToken(context)
        val hostUrl = getServerHost() + URL_IMAGE_BASE + "/get"
        val map = HashMap<String, String>()
        map["token"] = token
        map["image"] = url
        return OkHttpUtil.addParamToUrl(hostUrl, map)
    }

    fun encodeString(content:String):String {
        var encodeStr:String? = null
        try{
            val md = MessageDigest.getInstance("md5")
            val md5 = md.digest(content.toByteArray())
            encodeStr = Base64.encodeToString(md5, 0)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return encodeStr?:""
    }

    private fun generateUserToken():String{
        val token:String = System.currentTimeMillis().toString()
        var md5Token:String? = null
        try{
            val md = MessageDigest.getInstance("md5")
            val md5 = md.digest(token.toByteArray())
            val fix = md.digest(USER_TOKEN_FIX.toByteArray())
            val data = ByteArray(md5.size + fix.size)
            System.arraycopy(md5, 0, data, 0, USER_TOKEN_POS)
            System.arraycopy(fix, 0, data, USER_TOKEN_POS, fix.size)
            System.arraycopy(md5, USER_TOKEN_POS, data, fix.size + USER_TOKEN_POS, md5.size - USER_TOKEN_POS)
            md5Token = Base64.encodeToString(data, 0)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return md5Token?:""
    }

    fun saveUserToken(context:Context, token:String) {
        context.getSharedPreferences(SHARES_FILE, Context.MODE_PRIVATE).edit()
                .putString(WS_MSG_TOKEN_SELF, token)
                .apply()
    }

    fun getUserToken(context:Context, generate:Boolean = true):String {
        val sp: SharedPreferences = context.getSharedPreferences(SHARES_FILE, Context.MODE_PRIVATE)
        var token = sp.getString(WS_MSG_TOKEN_SELF, "")
        if(generate && token == "") {
            token = generateUserToken()
        }
        return token?:return ""
    }

    fun checkUserToken(token:String):Boolean {
        if(token == "") {
            return false
        }
        val data = Base64.decode(token, 0)
        val md = MessageDigest.getInstance("md5")
        val fix = md.digest(USER_TOKEN_FIX.toByteArray())
        if(data.size < fix.size + USER_TOKEN_POS) {
            return false
        }
        for(i in fix.indices) {
            if(data[i + USER_TOKEN_POS] != fix[i]) {
                return false
            }
        }
        return true
    }

    const val QRCODE_IMG_SIZE = 560
    const val USER_IMG_SIZE = 520
    const val PWD_MIN_LENGTH = 8
    const val PWD_MAX_LENGTH = 15
    const val USER_LOGIN_STATUS = "login_state"
    const val USER_LOGIN_STATUS_OFF = 0
    const val USER_LOGIN_STATUS_ON = 1
    private const val URL_VERSION_BASE = "version"
    const val REQUEST_CODE_VERSION = 0x124

    fun setLoginStatus(context:Context, status:Int) {
        context.getSharedPreferences(SHARES_FILE, Context.MODE_PRIVATE).edit()
                .putInt(USER_LOGIN_STATUS, status)
                .apply()
    }

    fun getLoginStatus(context:Context):Int {
        return context.getSharedPreferences(SHARES_FILE, Context.MODE_PRIVATE).getInt(USER_LOGIN_STATUS, USER_LOGIN_STATUS_OFF)
    }

    fun getNewApkPath(context: Context): String {
        return context.applicationContext.getExternalFilesDir(null).toString() + File.separator + "viwalk_new.apk"
    }

    fun getPatchPath(context: Context): String {
        return context.applicationContext.getExternalFilesDir(null).toString() + File.separator + "apk.patch"
    }

    fun getVersionUrl(): String {
        return getServerHost() + URL_VERSION_BASE + "/newest"
    }

    fun getVersionDataUrl(): String {
        return getServerHost() + URL_VERSION_BASE + "/patch"
    }

    fun isWifiConnected(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cap = manager.getNetworkCapabilities(manager.activeNetwork)
        return cap != null && cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun isNetConnected(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val cap = manager.getNetworkCapabilities(manager.activeNetwork)
        return cap != null && (cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
}