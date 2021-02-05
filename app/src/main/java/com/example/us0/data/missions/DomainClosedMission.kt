package com.example.us0.data.missions

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
@Parcelize
data class DomainClosedMission(
    val missionNumber: Int,
    val missionName: String,
    val deadline: Long,
    val usersActive: Int,
    val sponsorName: String,
    val sponsorDescription: String,
    val missionDescription: String,
    val totalMoneyRaised: Int,
    val category:String,
    val sponsorSite:String
): Parcelable {
    val deadlineAsDate:String
        get()=SimpleDateFormat("dd/MM/yyyy").format(deadline)
}