package com.example.us0.data.missions

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
@Parcelize
data class DomainActiveMission(
    val missionNumber: Int,
    val missionName: String,
    val deadline: Long,
    val usersActive: Int,
    val rulesNumber: Int,
    val sponsorName: String,
    val sponsorNumber:Int,
    val sponsorDescription: String,
    val missionDescription: String,
    val totalMoneyRaised: Int,
    val category:String,
    val contribution:Int,
    val goal:String,
    val reportAvailable:Boolean,
    val onAccomplishDataUpdated:Boolean
): Parcelable {
    val deadlineAsDate:String
        get()="Available till "+SimpleDateFormat("dd/MM/yyyy").format(deadline)
    val deadlineAsDateShort:String
        get()= SimpleDateFormat("dd/MM/yyyy").format(deadline)
}