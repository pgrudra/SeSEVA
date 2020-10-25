package com.example.us0.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDataBaseDao{

    @Insert
    fun insert(app:AppAndCategory)

    @Update
    fun update(app:AppAndCategory)

    @Query("SELECT * FROM list_of_installed_apps_and_their_category")
    fun getAll(): LiveData<List<AppAndCategory>>

    @Query("DELETE FROM list_of_installed_apps_and_their_category")
    fun clear()
}