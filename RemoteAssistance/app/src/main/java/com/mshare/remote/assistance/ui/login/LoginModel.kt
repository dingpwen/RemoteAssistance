package com.mshare.remote.assistance.ui.login

import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.util.OkHttpUtil
import okhttp3.Callback

class LoginModel {
    fun login(params: Map<*, *>, callback: Callback) {
        OkHttpUtil.baseGet(Constants.getUserLoginUrl(), params, callback)
    }

    fun logon(params: Map<*, *>, callback: Callback) {
        OkHttpUtil.basePost(Constants.getUserRegisterUrl(), params, callback)
    }
}