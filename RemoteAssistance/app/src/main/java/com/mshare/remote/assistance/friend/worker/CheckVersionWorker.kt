package com.mshare.remote.assistance.friend.worker

import android.content.Context
import androidx.work.WorkerParameters
import com.mshare.remote.assistance.Constants

class CheckVersionWorker (context: Context, workerParams: WorkerParameters):
    BaseWorker(context, workerParams) {
    override fun getParams(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val project = inputData.getString("project") ?: Constants.PROJECT
        val version = inputData.getLong("version", 1)
        map["project"] = project
        map["version"] = "" + version
        return map
    }
}