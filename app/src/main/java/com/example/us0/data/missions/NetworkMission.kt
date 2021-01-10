package com.example.us0.data.missions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class NetworkMission(
    val missionName: String = "",
    val deadline: String = "",
    val missionDescription: String = "",
    val sponsorName:String="",
    val sponsorDescription:String="",
    val category:String="",
    val sponsorSite:String=""
){
    private fun deadlineStringToLong():Long{
        var l:Long=0L
        val f = SimpleDateFormat("dd-MMM-yyyy")
        try {
            val d: Date = f.parse(deadline)
            l= d.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return l
    }
fun asDatabaseModel(): Mission {
    return Mission(
        missionName = missionName,
        missionDescription = missionDescription,
        deadline = deadlineStringToLong(),
        sponsorName=sponsorName,
        sponsorDescription = sponsorDescription,
        missionCategory = category,
        sponsorSite = sponsorSite
        )
}
}
data class Deadline(
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0
)