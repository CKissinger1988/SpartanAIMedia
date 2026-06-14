package com.spartanai.spartanaimedia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spartanai.spartanaimedia.data.local.dao.MediaDao
import com.spartanai.spartanaimedia.data.local.dao.UserDao
import com.spartanai.spartanaimedia.data.local.entity.MediaEntity
import com.spartanai.spartanaimedia.data.local.entity.UserProfileEntity
import com.spartanai.spartanaimedia.data.local.entity.WatchlistEntity

@Database(
    entities = [
        MediaEntity::class,
        UserProfileEntity::class,
        WatchlistEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {
    abstract val mediaDao: MediaDao
    abstract val userDao: UserDao
}
