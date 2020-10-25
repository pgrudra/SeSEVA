package com.example.us0.data
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "list_of_installed_apps_and_their_category")
data class AppAndCategory(
    @PrimaryKey(autoGenerate = true)
    var appId:Long=0L,
    @ColumnInfo(name="app_name")
    var appName:String="",
    @ColumnInfo(name="app_category")
    var appCategory:String=""
)


