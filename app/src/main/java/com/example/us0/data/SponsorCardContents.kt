package com.example.us0.data

data class SponsorCardContents(
    val sponsorName:String,
    val totalMoneySponsored:Int,
    val sponsoredMissions:List<String>,
    val sponsoredMissionNumbers:List<Int>
)