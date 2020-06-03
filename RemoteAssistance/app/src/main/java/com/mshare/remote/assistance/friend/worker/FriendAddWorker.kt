package com.mshare.remote.assistance.friend.worker

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mshare.remote.assistance.Constants

class FriendAddWorker(context: Context, workerParams: WorkerParameters):
    BaseWorker(context, workerParams) {
    override fun getParams(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val token = inputData.getString(Constants.WS_MSG_TOKEN_SELF) ?: return map
        val friendToken = inputData.getString(Constants.WS_MSG_TOKEN_GOAL) ?: return map
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map[Constants.WS_MSG_TOKEN_GOAL] = friendToken
        return map
    }
}