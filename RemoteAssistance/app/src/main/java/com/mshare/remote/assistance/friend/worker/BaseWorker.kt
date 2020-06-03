package com.mshare.remote.assistance.friend.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mshare.remote.assistance.util.OkHttpUtil

abstract class BaseWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    companion object{
        const val HTTP_URL:String = "url"
        const val HTTP_METHOD:String = "method"
        const val HTTP_POST:String = "POST"
        const val HTTP_GET:String = "GET"
        const val HTTP_RESPONSE:String = "response"
    }
    abstract fun getParams():HashMap<String, String>

    override fun doWork(): Result {
        val url = inputData.getString(HTTP_URL) ?: return Result.failure()
        val method = inputData.getString(HTTP_METHOD) ?: HTTP_GET
        val map = getParams()
        var response:String? = null
        if(method.equals(HTTP_GET)) {
            response = OkHttpUtil.baseSyncGet(url, map)
        } else if(method.equals(HTTP_POST)){
            response = OkHttpUtil.baseSyncPost(url, map)
        }
        val data = Data.Builder().putString(HTTP_RESPONSE, response).build()
        return Result.success(data)
    }
}