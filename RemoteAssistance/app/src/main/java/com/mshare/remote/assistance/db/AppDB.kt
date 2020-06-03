package com.mshare.remote.assistance.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mshare.remote.assistance.friend.model.FriendDao
import com.mshare.remote.assistance.friend.model.FriendEntity

@Database(entities = [FriendEntity::class], version = 1, exportSchema = true)
abstract class AppDB: RoomDatabase() {
    public abstract fun friendDao(): FriendDao
    companion object{
        private val DATABASE_NAME = "assistance.db"
        private var instance:AppDB? = null
        public fun getInstance(context: Context):AppDB{
            if(instance == null) {
                synchronized(AppDB::class) {
                    if(instance == null) {
                        instance = buildDatabase(context.getApplicationContext());
                    }
                }
            }
            return instance!!;
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDB::class.java, DATABASE_NAME).build()
    }
}