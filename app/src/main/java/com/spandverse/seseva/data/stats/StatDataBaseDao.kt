package com.spandverse.seseva.data.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.spandverse.seseva.TimeLaunchesDate

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

    @Query("SELECT time_spent,app_launches,date FROM list_of_stats WHERE package_name=:pkg AND date>:d ORDER BY date ASC")
    suspend fun getTimeLaunchesDate(pkg:String,d:Long): List<TimeLaunchesDate>

    @Query("SELECT SUM(time_spent) FROM list_of_stats WHERE package_name=:pkg AND date>:d")
    suspend fun getWeekTimeSpent(pkg:String,d:Long): Int?

    @Query("SELECT SUM(app_launches) FROM list_of_stats WHERE package_name=:pkg AND date>:d")
    suspend fun getWeekLaunches(pkg:String,d:Long): Int?

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