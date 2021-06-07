package com.spandverse.seseva.data.missions

import androidx.lifecycle.LiveData
import androidx.room.*
import com.spandverse.seseva.MissionNumberUpdateReport


@Dao
interface MissionsDatabaseDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission:Mission)

    @Update
    suspend fun update(mission:Mission)

    @Update(entity = Mission::class)
    suspend fun partialUpdate(partialMission:PartialMission)

    @Query("SELECT * FROM list_of_missions WHERE missionNumber=:key")
    suspend fun doesMissionExist(key:Int): Mission?

    @Query("SELECT * FROM list_of_missions WHERE deadline>:now AND contribution>:contribution")
    fun getAllActiveMissions(now:Long,contribution:Int): LiveData<List<Mission>>

    @Query("SELECT * FROM list_of_missions")
    fun getAllMissions(): LiveData<List<Mission>>

    @Query("DELETE FROM list_of_missions")
    suspend fun clear()

    @Query("SELECT * FROM list_of_missions WHERE deadline<:now AND contribution>:contribution")
    fun getAllAccomplishedMissions(now:Long,contribution:Int): LiveData<List<Mission>>

    /*@Query("SELECT * FROM list_of_missions WHERE mission_complete_notification=:t")
    suspend fun notifyIfClosed(t:Boolean):Mission?*/
    @Query("SELECT missionNumber,on_accomplish_data_updated,report_available FROM list_of_missions where deadline<:now AND report_available=:key")
    suspend fun getMissionNumbersForReport(now:Long,key:Boolean):List<MissionNumberUpdateReport>?

    @Query("SELECT missionNumber FROM list_of_missions")
    suspend fun getDownloadedMissions(): List<Int>?

    @Query("SELECT COUNT(*) FROM list_of_missions WHERE contribution>:n")
    fun getMissionsCount(n:Int):LiveData<Int?>

    @Query("SELECT COUNT(*) FROM list_of_missions WHERE deadline>:now")
    fun getActiveMissionsCount(now:Long):LiveData<Int?>

    @Query("SELECT SUM(total_money_raised) FROM list_of_missions")
    fun getTotalMoneyRaised():LiveData<Int?>

    @Query("SELECT SUM(contribution) FROM list_of_missions")
    fun getTotalContribution():LiveData<Int?>

}
