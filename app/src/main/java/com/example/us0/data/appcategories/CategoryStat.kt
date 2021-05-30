package com.example.us0.data.appcategories

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_of_category_stats")
data class CategoryStat(
    @PrimaryKey(autoGenerate = true)
    var categoryStatId:Long=0L,
    @ColumnInfo(name="category_name")
    val categoryName:String?,
    @ColumnInfo(name="time_spent")
    val timeSpent:Int?,
    @ColumnInfo(name ="app_launches")
    val appLaunches:Int?,
    @ColumnInfo(name="date")
    val date:Long?,
    @ColumnInfo(name = "rule_violated")
    val ruleViolated:Boolean?
)