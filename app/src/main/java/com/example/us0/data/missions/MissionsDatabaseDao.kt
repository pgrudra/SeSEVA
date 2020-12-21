package com.example.us0.data.missions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface MissionsDatabaseDao{
    @Insert
    suspend fun insert(mission:Mission)

    @Update
    suspend fun update(mission:Mission)


}
