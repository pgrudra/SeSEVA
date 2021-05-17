package com.example.us0.data.appcategories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.us0.TimeLaunchesDate

@Dao
interface CategoryStatDatabaseDao{

    @Insert
    suspend fun insert(stat: CategoryStat)

    @Query("SELECT * FROM list_of_category_stats WHERE category_name=:cat")
    suspend fun getCategoryStats(cat:String): List<CategoryStat>?

    @Query("SELECT * FROM list_of_category_stats WHERE rule_violated=:t")
    suspend fun getCategoryStats(t:Boolean): List<CategoryStat>?

    //may not be necessary
    @Query("SELECT * FROM list_of_category_stats WHERE date=:chosenDate")
    suspend fun getCategoryStatsOfChosenDate(chosenDate:Long): List<CategoryStat>?
    //may not be necessary till here

    @Query("SELECT time_spent,app_launches,date FROM list_of_category_stats WHERE category_name=:cat AND date>:d ORDER BY date ASC")
    suspend fun getTimeLaunchesDate(cat:String,d:Long): List<TimeLaunchesDate>

    @Query("DELETE FROM list_of_category_stats")
    suspend fun clear()

    @Query("DELETE FROM list_of_stats WHERE date>:max")
    suspend fun deleteStats(max:Long)
}