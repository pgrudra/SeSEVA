package com.example.us0.data.sponsors

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_of_sponsors")
data class Sponsor(
    @PrimaryKey
    var sponsorNumber:Int=0,

    @ColumnInfo(name="sponsor_name")
    var sponsorName:String="",

    @ColumnInfo(name="sponsor_description")
    var sponsorDescription:String="",

    @ColumnInfo(name="sponsor_address")
    var sponsorAddress:String?=null,

    @ColumnInfo(name="sponsor_site")
    var sponsorSite:String?=null,

    @ColumnInfo(name="missions_sponsored")
    var missionsSponsored:String="",

    @ColumnInfo(name="mission_amounts")
    var missionAmounts:String="",

    @ColumnInfo(name="sponsored_amount")
    var sponsoredAmount:Int=0

)
@Entity
data class PartialSponsor(
    @PrimaryKey
    val sponsorNumber:Int,
    @ColumnInfo(name="missions_sponsored")
    var missionsSponsored:String="",

    @ColumnInfo(name="mission_amounts")
    var missionAmounts:String="",

    @ColumnInfo(name="sponsored_amount")
    var sponsoredAmount:Int=0
)