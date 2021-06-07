package com.spandverse.seseva.data.stats

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_of_stats")
data class Stat(
    @PrimaryKey(autoGenerate = true)
    var statId:Long=0L,
    @ColumnInfo(name="package_name")
    val packageName:String?,
    @ColumnInfo(name="app_name")
    val appName:String?,
    @ColumnInfo(name="app_category")
    val appCategory:String?,
    @ColumnInfo(name="time_spent")
    val timeSpent:Int?,
    @ColumnInfo(name ="app_launches")
    val appLaunches:Int?,
    @ColumnInfo(name="date")
    val date:Long?
)