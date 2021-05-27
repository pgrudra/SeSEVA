package com.example.us0.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.us0.data.appcategories.CategoryStat
import com.example.us0.data.appcategories.CategoryStatDatabaseDao
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.data.apps.AppDataBaseDao
import com.example.us0.data.missions.Mission
import com.example.us0.data.missions.MissionsDatabaseDao
import com.example.us0.data.sponsors.Sponsor
import com.example.us0.data.sponsors.SponsorDatabaseDao
import com.example.us0.data.stats.Stat
import com.example.us0.data.stats.StatDataBaseDao

@Database(entities = [AppAndCategory::class, Mission::class, Stat::class, CategoryStat::class, Sponsor::class], version = 12, exportSchema = false)
abstract class AllDatabase : RoomDatabase() {

    abstract val AppDatabaseDao: AppDataBaseDao
    abstract val MissionsDatabaseDao:MissionsDatabaseDao
    abstract val StatDataBaseDao:StatDataBaseDao
    abstract val CategoryStatDatabaseDao:CategoryStatDatabaseDao
    abstract val SponsorDatabaseDao: SponsorDatabaseDao

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
                        "database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}