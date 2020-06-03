package com.mshare.remote.assistance.friend.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey
    @ColumnInfo(name = "token")
    val user_token:String,
    @ColumnInfo(name = "name")
    val name:String,
    @ColumnInfo(name = "image")
    val imageUrl:String
)