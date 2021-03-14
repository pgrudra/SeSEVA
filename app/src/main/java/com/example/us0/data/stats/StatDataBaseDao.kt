package com.example.us0.data.stats

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.us0.data.apps.AppAndCategory

@Dao
interface StatDataBaseDao{

    @Insert
    suspend fun insert(stat: Stat)

    @Query("SELECT * FROM list_of_stats WHERE package_name=:pk")
    suspend fun getAppStats(pk:String): List<Stat>?
    //may not be necessary
    @Query("SELECT * FROM list_of_stats WHERE app_category=:cat")
    suspend fun getAppStatsOfChosenCat(cat:String): List<Stat>?

    @Query("SELECT * FROM list_of_stats WHERE date=:chosenDate")
    suspend fun getAppsStatsOnChosenDate(chosenDate:Long): List<Stat>?

    @Query("DELETE FROM list_of_stats WHERE package_name=:pk")
    suspend fun deleteAppStats(pk:String)

    @Query("DELETE FROM list_of_stats WHERE app_category=:cat")
    suspend fun deleteCatStats(cat:String)
    //may not be necessary till here
    @Query("DELETE FROM list_of_stats")
    suspend fun clear()

    @Query("DELETE FROM list_of_stats WHERE date>:max")
    suspend fun deleteStats(max:Long)
}