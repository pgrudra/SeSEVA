package com.spandverse.seseva.data.missions

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
@Parcelize
data class DomainClosedMission(
    val missionNumber: Int,
    val missionName: String,
    val deadline: Long,
    val contributors: Int,
    val rulesNumber: Int,
    val sponsorName: String,
    val sponsorNumber:Int,
    val missionDescription: String,
    val totalMoneyRaised: Int,
    val category:String,
    val contribution:Int,
    val goal:String
): Parcelable {
    val deadlineAsDate:String
        get()=SimpleDateFormat("dd/MM/yyyy").format(deadline)
}