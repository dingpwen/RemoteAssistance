package com.mshare.remote.assistance

import android.app.Application
import com.mshare.remote.assistance.db.AppDB

class AssistanceApplication: Application() {
    fun getDataBase():AppDB = AppDB.getInstance(this)
}