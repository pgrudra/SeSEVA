package com.example.us0.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDataBaseDao{

    @Insert
    suspend fun insert(app:AppAndCategory)

    @Update
    suspend fun update(app:AppAndCategory)

    @Query("SELECT * FROM list_of_installed_apps_and_their_category WHERE package_name=:pk")
    suspend fun isAppExist(pk:String):AppAndCategory?

    @Query("SELECT * FROM list_of_installed_apps_and_their_category")
    fun getAll(): LiveData<List<AppAndCategory>>

    @Query("DELETE FROM list_of_installed_apps_and_their_category")
    suspend fun clear()

    @Query("SELECT package_name FROM list_of_installed_apps_and_their_category")
    fun getLaunchablesList():List<String>
}