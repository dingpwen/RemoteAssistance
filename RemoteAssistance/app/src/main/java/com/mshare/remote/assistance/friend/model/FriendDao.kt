package com.mshare.remote.assistance.friend.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FriendDao {
    @Query("delete from friends")
    fun deleteAll()

    @Query("select * from friends")
    fun loadAll(): LiveData<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveFriendItems(friendsList: List<FriendEntity>)

    @Query("delete from friends where token = :token")
    fun deleteByToken(token: String);
}