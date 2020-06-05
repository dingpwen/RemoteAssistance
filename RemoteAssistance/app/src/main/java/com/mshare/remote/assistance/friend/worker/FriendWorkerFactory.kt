package com.mshare.remote.assistance.friend.worker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.mshare.remote.assistance.Constants

object FriendWorkerFactory {
    fun startFriendListWorker(context: Context): LiveData<WorkInfo> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()
        val token = Constants.getUserToken(context, true)
        val data = Data.Builder()
            .putString(Constants.WS_MSG_TOKEN_SELF, token)
            .putString(BaseWorker.HTTP_URL, Constants.getFriendListUrl())
            .build()
        val friendRequest =  OneTimeWorkRequest.Builder(FriendListWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("FriendListWorker", ExistingWorkPolicy.REPLACE, friendRequest)
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(friendRequest.id)
    }

    fun startAddOrRemoveWorker(context: Context, token: String, friendToken: String, type:Int): LiveData<WorkInfo> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()
        val data = Data.Builder()
            .putString(Constants.WS_MSG_TOKEN_SELF, token)
            .putString(Constants.WS_MSG_TOKEN_GOAL, friendToken)
            .putString(BaseWorker.HTTP_METHOD, BaseWorker.HTTP_POST)
            .putString(BaseWorker.HTTP_URL, if(type == 1) Constants.getFriendAddUrl() else Constants.getFriendDelUrl())
            .build()
        val friendRequest =  OneTimeWorkRequest.Builder(FriendAddWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(friendRequest)
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(friendRequest.id)
    }

    fun startAddUserWorker(context: Context, token: String, category:String): LiveData<WorkInfo> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .build()
        val data = Data.Builder()
            .putString(Constants.WS_MSG_TOKEN_SELF, token)
            .putString(Constants.USER_TOKEN_CATEGORY, category)
            .putString(BaseWorker.HTTP_METHOD, BaseWorker.HTTP_POST)
            .putString(BaseWorker.HTTP_URL, Constants.getUserAddUrl())
            .build()
        val addRequest =  OneTimeWorkRequest.Builder(UserAddWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("UserAddWorker", ExistingWorkPolicy.REPLACE, addRequest)
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(addRequest.id)
    }

    fun startCheckVersionWorker(context: Context, curVersion: Long): LiveData<WorkInfo> {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()
        val data = Data.Builder()
            .putString("project", Constants.PROJECT)
            .putLong("version", curVersion)
            .putString(BaseWorker.HTTP_URL, Constants.getVersionUrl())
            .build()
        val checkRequest =  OneTimeWorkRequest.Builder(CheckVersionWorker::class.java)
            .setConstraints(constraints)
            .setInputData(data)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("CheckVersionWorker", ExistingWorkPolicy.REPLACE, checkRequest)
        return WorkManager.getInstance(context).getWorkInfoByIdLiveData(checkRequest.id)
    }
}