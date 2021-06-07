package com.spandverse.seseva.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spandverse.seseva.data.appcategories.CategoryStat
import com.spandverse.seseva.data.appcategories.CategoryStatDatabaseDao
import com.spandverse.seseva.data.apps.AppAndCategory
import com.spandverse.seseva.data.apps.AppDataBaseDao
import com.spandverse.seseva.data.missions.Mission
import com.spandverse.seseva.data.missions.MissionsDatabaseDao
import com.spandverse.seseva.data.missions.PartialMission
import com.spandverse.seseva.data.sponsors.PartialSponsor
import com.spandverse.seseva.data.sponsors.Sponsor
import com.spandverse.seseva.data.sponsors.SponsorDatabaseDao
import com.spandverse.seseva.data.stats.Stat
import com.spandverse.seseva.data.stats.StatDataBaseDao

@Database(entities = [AppAndCategory::class, Mission::class, Stat::class, CategoryStat::class, Sponsor::class, PartialSponsor::class, PartialMission::class], version = 12, exportSchema = false)
abstract class AllDatabase : RoomDatabase() {

    abstract val AppDatabaseDao: AppDataBaseDao
    abstract val MissionsDatabaseDao:MissionsDatabaseDao
    abstract val StatDataBaseDao:StatDataBaseDao
    abstract val CategoryStatDatabaseDao:CategoryStatDatabaseDao
    abstract val SponsorDatabaseDao: SponsorDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: com.spandverse.seseva.data.AllDatabase? = null
        fun getInstance(context: Context): com.spandverse.seseva.data.AllDatabase {
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