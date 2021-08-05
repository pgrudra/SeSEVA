package com.spandverse.seseva.data.sponsors

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface SponsorDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sponsor: Sponsor)

    @Update(entity = Sponsor::class)
    suspend fun update(partialSponsor:PartialSponsor)

    @Query("SELECT * FROM list_of_sponsors WHERE sponsorNumber=:key")
    suspend fun doesSponsorExist(key:Int): Sponsor?

    @Query("SELECT * FROM list_of_sponsors")
    fun getAllSponsors(): LiveData<List<Sponsor>>

    @Query("SELECT sponsorNumber FROM list_of_sponsors")
    suspend fun getDownloadedSponsors(): List<Int>?

    @Query("DELETE FROM list_of_sponsors")
    suspend fun clear()
}

