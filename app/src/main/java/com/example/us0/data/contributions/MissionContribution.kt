package com.example.us0.data.contributions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_of_contributions")
data class MissionContribution(
    @PrimaryKey
    var missionNumber: Int = 0,

    @ColumnInfo(name = "contribution")
    var contribution: Int = 0

)