package com.example.us0.data.missions

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class NetworkMission(
    val missionName: String = "",
    val deadline: String = "",
    val description: String = ""
){
    private fun deadlineStringToLong():Long{
        var l:Long=0L
        val ONE_DAY=24*60*60*1000
        val f = SimpleDateFormat("dd-MMM-yyyy")
        try {
            val d: Date = f.parse(deadline)
            l= d.time+ONE_DAY
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return l
    }
fun asDatabaseModel(): Mission {
    return Mission(
        missionName = missionName,
        description = description,
        deadline = deadlineStringToLong()
        )
}
}
data class Deadline(
    val day: Int = 0,
    val month: Int = 0,
    val year: Int = 0
)