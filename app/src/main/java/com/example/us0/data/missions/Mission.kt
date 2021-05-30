package com.example.us0.data.missions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_of_missions")
data class Mission(
    @PrimaryKey
    var missionNumber:Int=0,

    @ColumnInfo(name="mission_name")
    var missionName:String="",

    @ColumnInfo(name="deadline")
    var deadline:Long=0L,

    @ColumnInfo(name="users_active")
    var usersActive:Int=0,

    @ColumnInfo(name="rules_number")
    var rulesNumber:Int=0,

    @ColumnInfo(name="on_accomplish_data_updated")
    var onAccomplishDataUpdated:Boolean=false,

    @ColumnInfo(name="report_available")
    var reportAvailable:Boolean=false,

    @ColumnInfo(name="sponsor_name")
    var sponsorName:String="",

    @ColumnInfo(name = "sponsor_Description")
    var sponsorDescription:String="",

    @ColumnInfo(name="mission_description")
    var missionDescription:String="",

    @ColumnInfo(name="mission_category")
    var missionCategory:String="",

    @ColumnInfo(name="mission_active")
    var missionActive:Boolean=true,

    @ColumnInfo(name="total_money_raised")
    var totalMoneyRaised:Int=0,


    @ColumnInfo(name = "contribution")
    var contribution: Int = 0,

    @ColumnInfo(name = "sponsor_number")
    var sponsorNumber:Int=0,

    @ColumnInfo(name="goal")
    var goal:String="",

)

fun List<Mission>.asActiveDomainModel(): List<DomainActiveMission> {
    return map {
        DomainActiveMission(
            missionNumber = it.missionNumber,
            missionName = it.missionName,
            deadline =it.deadline,
            usersActive = it.usersActive,
            rulesNumber = it.rulesNumber,
            sponsorName = it.sponsorName,
            sponsorNumber = it.sponsorNumber,
            sponsorDescription = it.sponsorDescription,
            missionDescription = it.missionDescription,
            totalMoneyRaised = it.totalMoneyRaised,
            category=it.missionCategory,
            contribution = it.contribution,
            goal = it.goal,
            reportAvailable = it.reportAvailable,
            onAccomplishDataUpdated = it.onAccomplishDataUpdated
        )
    }
}
fun Mission.asActiveDomainModel(): DomainActiveMission {
    return DomainActiveMission(
            missionNumber = missionNumber,
            missionName = missionName,
            deadline =deadline,
            usersActive = usersActive,
            rulesNumber = rulesNumber,
            sponsorName = sponsorName,
            sponsorNumber = sponsorNumber,
            sponsorDescription = sponsorDescription,
            missionDescription = missionDescription,
            totalMoneyRaised = totalMoneyRaised,
            category=missionCategory,
            contribution = contribution,
            goal=goal,
            reportAvailable = reportAvailable,
            onAccomplishDataUpdated = onAccomplishDataUpdated
        )

}
fun Mission.asClosedDomainModel():DomainClosedMission{
        return DomainClosedMission(
            missionNumber = missionNumber,
            missionName = missionName,
            deadline =deadline,
            usersActive = usersActive,
            rulesNumber=rulesNumber,
            sponsorName = sponsorName,
            sponsorNumber = sponsorNumber,
            sponsorDescription = sponsorDescription,
            missionDescription = missionDescription,
            totalMoneyRaised = totalMoneyRaised,
            category=missionCategory,
            contribution = contribution,
            goal=goal
        )

}

@Entity
data class PartialMission(
    @PrimaryKey
    val missionNumber:Int,

    @ColumnInfo(name="mission_description")
    val missionDescription:String,

    @ColumnInfo(name="on_accomplish_data_updated")
    val onAccomplishDataUpdated:Boolean,

    @ColumnInfo(name="report_available")
    val reportAvailable:Boolean,
)