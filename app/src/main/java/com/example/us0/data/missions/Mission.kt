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

    @ColumnInfo(name="img")
    var img:String="",

    @ColumnInfo(name="users_active")
    var usersActive:Int=0,

    @ColumnInfo(name="mission_complete_notification")
    var missionCompleteNotification:Boolean=false,

    @ColumnInfo(name="intro")
    var intro:String="",

    @ColumnInfo(name="description")
    var description:String=""
)