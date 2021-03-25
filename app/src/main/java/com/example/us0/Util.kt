package com.example.us0

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LifecycleService

enum class Actions{
    START,
    STOP
}
enum class ServiceState{
    STARTED,
    STOPPED
}
fun setServiceState(context: Context, state:ServiceState){
    val sharedPref = getPreferences(context)
    with(sharedPref.edit()) {
        this?.putString((R.string.service_state).toString(), state.name)
        this?.apply()}
}
fun getServiceState(context:Context):ServiceState{
    val sharedPref = getPreferences(context)
    val value=sharedPref.getString((R.string.service_state).toString(), ServiceState.STOPPED.name)?:ServiceState.STOPPED.name
    return ServiceState.valueOf(value)
}

private fun getPreferences(context:Context):SharedPreferences{
    return context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
}

fun allotGroup(cat:String): String {
    return when (cat) {
        "WEATHER" -> "WHITELISTED"
        "BUSINESS" -> "WHITELISTED"
        "TRAVEL_AND_LOCAL" -> "WHITELISTED"
        "TOOLS" -> "WHITELISTED"
        "PRODUCTIVITY" -> "WHITELISTED"
        "PHOTOGRAPHY" -> "WHITELISTED"
        "PERSONALIZATION" -> "WHITELISTED"
        "PARENTING" -> "WHITELISTED"
        "MEDICAL" -> "WHITELISTED"
        "MAPS_AND_NAVIGATION" -> "WHITELISTED"
        "LIBRARIES_AND_DEMO" -> "WHITELISTED"
        "HOUSE_AND_HOME" -> "WHITELISTED"
        "HEALTH_AND_FITNESS" -> "WHITELISTED"
        "FOOD_AND_DRINK" -> "WHITELISTED"
        "FINANCE" -> "WHITELISTED"
        "EVENTS" -> "WHITELISTED"
        "EDUCATION" -> "WHITELISTED"
        "BOOKS_AND_REFERENCE" -> "WHITELISTED"
        "AUTO_AND_VEHICLES" -> "WHITELISTED"
        "ART_AND_DESIGN" -> "WHITELISTED"
        "GAME_EDUCATIONAL" -> "WHITELISTED"
        "GAME_PUZZLE" -> "WHITELISTED"
        "GAME_TRIVIA" -> "WHITELISTED"
        "GAME_WORD" -> "WHITELISTED"
        "OTHERS" -> "OTHERS"
        "COMMUNICATION" -> "COMMUNICATION"
        "GAME_ACTION" -> "GAMES"
        "GAME_ADVENTURE" -> "GAMES"
        "GAME_ARCADE" -> "GAMES"
        "GAME_BOARD" -> "GAMES"
        "GAME_CARD" -> "GAMES"
        "GAME_CASINO" -> "GAMES"
        "GAME_CASUAL" -> "GAMES"
        "GAME_MUSIC" -> "GAMES"
        "GAME_RACING" -> "GAMES"
        "GAME_ROLE_PLAYING" -> "GAMES"
        "GAME_SIMULATION" -> "GAMES"
        "GAME_SPORTS" -> "GAMES"
        "GAME_STRATEGY" -> "GAMES"
        "DATING" -> "SOCIAL"
        "LIFESTYLE" -> "SOCIAL"
        "SOCIAL" -> "SOCIAL"
        "VIDEO_PLAYERS" -> "VIDEO_PLAYERS_N_COMICS"
        "COMICS" -> "VIDEO_PLAYERS_N_COMICS"
        "MUSIC_AND_AUDIO" -> "MSNBS"
        "SHOPPING" -> "MSNBS"
        "NEWS_AND_MAGAZINES" -> "MSNBS"
        "BEAUTY" -> "MSNBS"
        "SPORTS" -> "MSNBS"
        "ENTERTAINMENT" -> "ENTERTAINMENT"
        else -> "OTHERS"
    }
}
