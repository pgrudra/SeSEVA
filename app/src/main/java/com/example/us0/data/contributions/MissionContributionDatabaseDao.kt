package com.example.us0.data.contributions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MissionContributionDatabaseDao {
    @Insert
    suspend fun insert(contribution:MissionContribution)

    @Update
    suspend fun update(contribution:MissionContribution)

    @Query("SELECT * FROM list_of_contributions WHERE missionNumber =:n")
    suspend fun getContribution(n:Int): MissionContribution?
}
