package com.example.us0.data.missions

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface MissionsDatabaseDao{
    @Insert
    suspend fun insert(mission:Mission)

    @Update
    suspend fun update(mission:Mission)

    @Query("SELECT * FROM list_of_missions WHERE missionNumber=:key")
    suspend fun doesMissionExist(key:Int): Mission?

    @Query("SELECT * FROM list_of_missions WHERE deadline>:now")
    fun getAllActiveMissions(now:Long): LiveData<List<Mission>>

    @Query("DELETE FROM list_of_missions")
    suspend fun clear()

    @Query("SELECT * FROM list_of_missions WHERE deadline<:now")
    fun getAllClosedMissions(now:Long): LiveData<List<Mission>>
}
