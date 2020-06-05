package com.mshare.remote.assistance.friend.model

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.work.*
import com.mshare.remote.assistance.AssistanceApplication
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.friend.worker.BaseWorker
import com.mshare.remote.assistance.friend.worker.FriendWorkerFactory
import org.json.JSONException
import org.json.JSONObject
import wen.mmvm.arch.AbsDataSource
import wen.mmvm.arch.Result
import java.lang.Exception

class FriendRepository(application: Application) {
    val friendDao = (application as AssistanceApplication).getDataBase().friendDao()
    val app = application

    fun update() = loadFriendsData()

    fun loadFriendsData() = object:AbsDataSource<List<FriendEntity>>(){
        override fun loadFromDb(): LiveData<List<FriendEntity>> = friendDao.loadAll()

        override fun onFetchFailed() {
            //do nothing
        }

        override suspend fun saveNetResult(data: List<FriendEntity>?) = friendDao.saveFriendItems(data!!)

        override fun shouldFetch(data: List<FriendEntity>?): Boolean = true

        override fun createNetCall(): LiveData<Result<List<FriendEntity>>> {
            val result = MediatorLiveData<Result<List<FriendEntity>>>()
            val workerInfo = FriendWorkerFactory.startFriendListWorker(app)
            result.addSource(workerInfo) {
                if (it != null && it.state == WorkInfo.State.SUCCEEDED) {
                    val response = it.outputData.getString(BaseWorker.HTTP_RESPONSE)
                    val parseResult = parseData(response)
                    if(parseResult is Result.Success) {
                        result.postValue(parseResult)
                    } else {
                        onFetchFailed()
                    }
                    result.removeSource(workerInfo)
                } else if(it != null && (it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED)){
                    result.removeSource(workerInfo)
                }
            }

            return result
        }
    }.getAsLiveData()

    private fun parseData(data: String?):Result<List<FriendEntity>>{
        val friendList:MutableList<FriendEntity> = arrayListOf()
        if(data == null || data.isEmpty()) {
            return Result.Error(Exception("Network unavailable"), friendList)
        }
        try {
            val obj = JSONObject(data)
            val status = obj.getInt("status")
            if(status == 200) {
                val friends = obj.getJSONArray("friends")
                for(i in 0 until friends.length()) {
                    val friend:JSONObject = friends.get(i) as JSONObject
                    val name = friend.getString("name")
                    val userToken = friend.getString("user_token")
                    val imgUrl = friend.getString("imageUrl")
                    friendList.add(FriendEntity(userToken, name, imgUrl))
                }
            }
        } catch (e:JSONException) {
            return Result.Error(e, friendList)
        }
        return Result.Success(friendList)
    }

    fun addOrRemoveFriend(token: String, friendToken: String, type:Int): LiveData<Result<Int>> {
        val result = MediatorLiveData<Result<Int>>()
        val workerInfo = FriendWorkerFactory.startAddOrRemoveWorker(app, token, friendToken, type)
        result.addSource(workerInfo) {
            if (it != null && it.state == WorkInfo.State.SUCCEEDED) {
                val response = it.outputData.getString(BaseWorker.HTTP_RESPONSE)
                result.postValue(parsePostData(response, type))
                result.removeSource(workerInfo)
            } else if(it != null && (it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED)){
                result.removeSource(workerInfo)
            }
        }
        return result
    }

    private fun parsePostData(result: String?, type: Int):Result<Int>{
        if(result == null || result.isEmpty()) {
            return Result.Error(Exception("Network unavailable"), Constants.ERROR_TYPE_NET)
        }
        try {
            val obj = JSONObject(result)
            val status = obj.getInt("status")
            if(status != 200) {
                if(type == 1) {
                    return Result.Error(Exception("Failed to add"), Constants.ERROR_TYPE_ADD)
                }
                return Result.Error(Exception("Failed to delete"), Constants.ERROR_TYPE_DEL)
            }
        } catch (e: JSONException) {
            return Result.Error(e, Constants.ERROR_TYPE_JSON)
        }
        return Result.Success(200)
    }

    fun addUser(token: String, category:String):MediatorLiveData<Result<Int>> {
        val result = MediatorLiveData<Result<Int>>()
        val workerInfo = FriendWorkerFactory.startAddUserWorker(app, token, category)
        result.addSource(workerInfo) {
            if (it != null && it.state == WorkInfo.State.SUCCEEDED) {
                result.removeSource(workerInfo)
                val response = it.outputData.getString(BaseWorker.HTTP_RESPONSE)
                result.postValue(parsePostData(response, 1))
            } else if(it != null && (it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED)){
                result.removeSource(workerInfo)
                result.value = Result.Error(Exception("Failed to add user"), Constants.ERROR_TYPE_ADD)
            }
        }
        return result
    }

    fun checkVersion(curVersion: Long):LiveData<Result<VersionEntity>> {
        val result = MediatorLiveData<Result<VersionEntity>>()
        val workerInfo = FriendWorkerFactory.startCheckVersionWorker(app, curVersion)
        result.addSource(workerInfo) {
            if (it != null && it.state == WorkInfo.State.SUCCEEDED) {
                result.removeSource(workerInfo)
                val response = it.outputData.getString(BaseWorker.HTTP_RESPONSE)
                if(response == null) {
                    return@addSource
                }
                try{
                    val obj = JSONObject(response)
                    if(obj.getInt("status") == 200) {
                        val apkVersion = obj.getLong("version")
                        if(apkVersion > curVersion) {
                            val apkType = obj.getInt("type")
                            val apkChecksum = obj.getString("checksum1")
                            val patchChecksum = obj.getString("checksum2")
                            result.value = Result.Success(
                                VersionEntity(apkType, apkVersion, apkChecksum, patchChecksum))
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else if(it != null && (it.state == WorkInfo.State.FAILED || it.state == WorkInfo.State.CANCELLED)){
                result.removeSource(workerInfo)
            }
        }
        return result
    }
}