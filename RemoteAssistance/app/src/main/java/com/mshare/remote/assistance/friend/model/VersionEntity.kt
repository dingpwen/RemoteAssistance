package com.mshare.remote.assistance.friend.model

data class VersionEntity (
    val apkType:Int,
    val apkVersion: Long,
    val apkChecksum: String,
    val patchChecksum: String
)