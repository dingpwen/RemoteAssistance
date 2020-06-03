package com.mshare.remote.assistance.friend.worker

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.mshare.remote.assistance.Constants

class UserAddWorker (context: Context, workerParams: WorkerParameters):
    BaseWorker(context, workerParams) {
    override fun getParams(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val token = inputData.getString(Constants.WS_MSG_TOKEN_SELF) ?: return map
        val category = inputData.getString(Constants.USER_TOKEN_CATEGORY) ?: return map
        map[Constants.WS_MSG_TOKEN_SELF] = token
        map[Constants.USER_TOKEN_CATEGORY] = category
        return map
    }
}