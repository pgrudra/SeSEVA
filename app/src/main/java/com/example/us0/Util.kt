package com.example.us0

import android.content.Context
import android.content.SharedPreferences

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