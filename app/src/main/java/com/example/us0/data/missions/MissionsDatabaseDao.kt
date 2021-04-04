package com.example.us0.data.missions

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface MissionsDatabaseDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("SELECT * FROM list_of_missions WHERE mission_complete_notification=:t")
    suspend fun notifyIfClosed(t:Boolean):Mission?

    @Query("SELECT missionNumber FROM list_of_missions")
    suspend fun getDownloadedMissions(): List<Int>?
}
