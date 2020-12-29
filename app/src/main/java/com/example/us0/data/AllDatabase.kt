package com.example.us0.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.data.apps.AppDataBaseDao
import com.example.us0.data.missions.Mission
import com.example.us0.data.missions.MissionsDatabaseDao

@Database(entities = [AppAndCategory::class, Mission::class], version = 2, exportSchema = false)
abstract class AllDatabase : RoomDatabase() {

    abstract val AppDatabaseDao: AppDataBaseDao
    abstract val MissionsDatabaseDao:MissionsDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: com.example.us0.data.AllDatabase? = null
        fun getInstance(context: Context): com.example.us0.data.AllDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AllDatabase::class.java,
                        "apps_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}