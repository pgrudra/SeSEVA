package com.example.us0.data.sponsors

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.us0.data.missions.Mission

@Dao
interface SponsorDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sponsor: Sponsor)

    @Update
    suspend fun update(sponsor: Sponsor)

    @Query("SELECT * FROM list_of_sponsors WHERE sponsorNumber=:key")
    suspend fun doesSponsorExist(key:Int): Sponsor?

    @Query("SELECT * FROM list_of_sponsors")
    fun getAllSponsors(): LiveData<List<Sponsor>>

    @Query("DELETE FROM list_of_sponsors")
    suspend fun clear()
}