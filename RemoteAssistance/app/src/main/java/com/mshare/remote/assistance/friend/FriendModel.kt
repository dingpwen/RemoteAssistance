package com.mshare.remote.assistance.friend
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.util.OkHttpUtil
import okhttp3.*

class FriendModel:Contact.IModel {
    override fun loadData(params: Map<*, *>, callback: Callback) {
        OkHttpUtil.baseGet(Constants.getFriendListUrl(), params, callback)
    }

    override fun addOrRemoveFriend(params: Map<*, *>, callback: Callback, type: Int) {
        when(type) {
            1 -> OkHttpUtil.basePost(Constants.getFriendAddUrl(), params, callback)
            2 -> OkHttpUtil.basePost(Constants.getFriendDelUrl(), params, callback)
            else -> {
                return
            }
        }
    }

    override fun addUser(params: Map<*, *>, callback: Callback, type:Int) {
        when(type) {
            1 -> OkHttpUtil.basePost(Constants.getUserRegisterUrl(), params, callback)
            2 -> OkHttpUtil.basePost(Constants.getUserAddUrl(), params, callback)
            else -> {
                return
            }
        }
    }
}