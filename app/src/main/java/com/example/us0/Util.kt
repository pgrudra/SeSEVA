package com.example.us0

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleService
import androidx.room.ColumnInfo
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*

enum class Actions{
    START,
    STOP
}
enum class ServiceState{
    STARTED,
    STOPPED
}
enum class AppsCategoryType{
    DAILY,
    WEEKLY
}
enum class CategoryRuleStatus{
    BROKEN,
    WARNING,
    SAFE
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

data class TimeLaunchesDate(
    @ColumnInfo(name="time_spent") val time: Int?,
    @ColumnInfo(name="app_launches") val launches: Int?,
    @ColumnInfo(name="date") val date: Long?
)

fun getDay(i: Int): String {
    return when(i){
        Calendar.SUNDAY -> "Sun."
        Calendar.MONDAY -> "Mon."
        Calendar.TUESDAY -> "Tue."
        Calendar.WEDNESDAY -> "Wed."
        Calendar.THURSDAY -> "Thu."
        Calendar.FRIDAY -> "Fri."
        else->"Sat."
    }
}
fun getMonth(i: Int): String {
    return when(i){
        0->"Jan."
        1->"Feb."
        2->"Mar."
        3->"Apr."
        4->"May"
        5->"Jun."
        6->"Jul."
        7->"Aug."
        8->"Sept."
        9->"Oct."
        10->"Nov."
        else->"Dec."
    }
}

class WeekAxisValueFormatter(private val chart: BarLineChartBase<*>, private val labels: ArrayList<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return  if(value==6f){ "YDA" }
        else{labels[value.toInt()]}
    }
}
class MonthAxisValueFormatter(private val chart: BarLineChartBase<*>, private val labels: ArrayList<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return  if(value==28f){ "YDA" }
        else{labels[value.toInt()]}
    }
}
class YearAxisValueFormatter(private val chart: BarLineChartBase<*>, private val labels: ArrayList<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return  if(value==363f){ "YDA" }
        else{labels[value.toInt()]}
    }
}
fun secToHrMin(timeWeekAggregate: Int): CharSequence {
    val hrs=(timeWeekAggregate/3600)
    val mins=(timeWeekAggregate%3600)/60
    return "$hrs hrs $mins mins "
}
fun getDayAndMonth(lastMonthStart: Calendar): String {
    return lastMonthStart.get(Calendar.DATE).toString()+" "+getMonth(lastMonthStart.get(Calendar.MONTH))
}
fun checkIfSameDay(lastMonthStart: Calendar, calender: Calendar): Boolean {
    if(lastMonthStart.get(Calendar.DATE)==calender.get(Calendar.DATE)){
        if(lastMonthStart.get(Calendar.MONTH)==calender.get((Calendar.MONTH))){
            if(lastMonthStart.get(Calendar.YEAR)==calender.get(Calendar.YEAR)){
                return true
            }
        }
    }
    return false
}

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()