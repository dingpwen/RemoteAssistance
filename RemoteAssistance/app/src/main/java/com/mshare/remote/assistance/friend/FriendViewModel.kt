package com.mshare.remote.assistance.friend

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.friend.model.FriendEntity
import com.mshare.remote.assistance.friend.model.FriendRepository
import com.mshare.remote.assistance.friend.model.VersionEntity
import com.mshare.remote.assistance.util.OkHttpUtil
import wen.mmvm.arch.Result

class FriendViewModel(application: Application):AndroidViewModel(application) {
    val friendRepository = FriendRepository(application)
    private var mCache: MediatorLiveData<Result<List<FriendEntity>>>? = null
    private var mAddObserver:MediatorLiveData<Result<Int>>? = null
    val mErrorType = MutableLiveData(0)

    public fun getAllFriends():LiveData<Result<List<FriendEntity>>> {
        if(mCache == null) {
            mCache = friendRepository.loadFriendsData()
        }
        return mCache as LiveData<Result<List<FriendEntity>>>
    }

    public fun updateCache() {
        val update = friendRepository.update()
        mCache?.addSource(update) {
            mCache?.value = it
        }
    }

    fun addOrRemoveFriend(token: String, friendToken: String, type:Int) {
        val addOrRemove = friendRepository.addOrRemoveFriend(token, friendToken, type)
        mCache?.addSource(addOrRemove) {
            mCache?.removeSource(addOrRemove)
            if(it is Result.Success) {
                OkHttpUtil.addToNoCacheSet(Constants.getFriendListUrl())
                val load = friendRepository.loadFriendsData()
                mCache?.addSource(load) {loadData ->
                    mCache?.value = loadData
                }
            } else if(it is Result.Error) {
                mErrorType.value = it.data
            }
        }
    }

    fun addUser(token: String, category:String):LiveData<Result<Int>> {
        if(mAddObserver == null) {
            mAddObserver = friendRepository.addUser(token, category)
        } else {
            val addResult = friendRepository.addUser(token, category)
            mAddObserver?.addSource(addResult) {
                mAddObserver?.removeSource(addResult)
                mAddObserver?.value = it
            }
        }
        return mAddObserver as LiveData<Result<Int>>
    }

    fun checkVersion(curVersion: Long):LiveData<Result<VersionEntity>> {
        return friendRepository.checkVersion(curVersion);
    }
}