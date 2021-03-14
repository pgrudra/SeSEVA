package com.example.us0.home.rules

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.us0.CategoryRefreshWorker
import com.example.us0.R
import com.example.us0.allotGroup
import com.example.us0.data.apps.AppAndCategory
import com.example.us0.data.apps.AppDataBaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class Rules2ViewModel(
    private val database: AppDataBaseDao,
    application: Application,
    private val pm: PackageManager
) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref =
        context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val _toHomeFragment = MutableLiveData<Boolean>()
    val toHomeFragment: LiveData<Boolean>
        get() = _toHomeFragment

    private val _social = MutableLiveData<Boolean>()
    val social: LiveData<Boolean>
        get() = _social
    private val _communication = MutableLiveData<Boolean>()
    val communication: LiveData<Boolean>
        get() = _communication
    private val _games = MutableLiveData<Boolean>()
    val games: LiveData<Boolean>
        get() = _games
    private val _video = MutableLiveData<Boolean>()
    val video: LiveData<Boolean>
        get() = _video
    private val _msnbs = MutableLiveData<Boolean>()
    val msnbs: LiveData<Boolean>
        get() = _msnbs
    private val _whitelisted = MutableLiveData<Boolean>()
    val whitelisted: LiveData<Boolean>
        get() = _whitelisted
    private val _others = MutableLiveData<Boolean>()
    val others: LiveData<Boolean>
        get() = _others
    private val _entertainment = MutableLiveData<Boolean>()
    val entertainment: LiveData<Boolean>
        get() = _entertainment
    private val _contributeSentence=MutableLiveData<SpannableString>()
    val contributeSentence:LiveData<SpannableString>
        get()=_contributeSentence

    private val spannable1=SpannableString("Raise upto Rs 6 per day by adhering to below rules")
    private val spannable2=SpannableString("Raise upto Rs 4 per week by adhering to below rules")
    private val _daily = MutableLiveData<Boolean>()
    val daily: LiveData<Boolean>
        get() = _daily
    private val _weekly = MutableLiveData<Boolean>()
    val weekly: LiveData<Boolean>
        get() = _weekly
    private val _scrimVisible = MutableLiveData<Boolean>()
    val scrimVisible: LiveData<Boolean>
        get() = _scrimVisible
    private val _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean>
        get() = _noInternet
    val socialApps=database.getAll("SOCIAL")
    val communicationApps=database.getAll("COMMUNICATION")
    val gamesApps=database.getAll("GAMES")
    val entertainmentApps=database.getAll("ENTERTAINMENT")
    val videoApps=database.getAll("VIDEO_PLAYERS_N_COMICS")
    val msnbsApps=database.getAll("MSNBS")
    val whitelistedApps=database.getAll("WHITELISTED")
    val otherApps=database.getAll("OTHERS")
    init {

        spannable1.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable1.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        _contributeSentence.value= spannable1

        when {
            checkIfRulesShown() -> {
                _scrimVisible.value=false
            }
            checkInternet() -> {

                getApps()
            }
            else -> {
                _noInternet.value=true
            }
        }

        _social.value=false
        _communication.value=false
        _games.value=false
        _whitelisted.value=false
        _video.value=false
        _entertainment.value=false
        _msnbs.value=false
        _others.value=false

    }

    fun goToHome() {
        with(sharedPref?.edit()) {
            this?.putBoolean((R.string.rules_shown).toString(), true)
            this?.apply()
        }
        _toHomeFragment.value = true
    }

    fun goToHomeComplete() {
        _toHomeFragment.value = false
    }
    fun socialDropdown(){
        _social.value = _social.value != true
    }
    fun communicationDropdown(){
        _communication.value = _communication.value != true
    }
    fun gamesDropdown(){
        _games.value = _games.value != true
    }
    fun entertainmentDropdown(){
        _entertainment.value = _entertainment.value != true
    }
    fun videoDropdown(){
        _video.value = _video.value != true
    }
    fun msnbsDropdown(){
        _msnbs.value=_msnbs.value !=true
    }
    fun whitelistedDropdown(){
        _whitelisted.value=_whitelisted.value !=true
    }
    fun otherAppsDropdown(){
        _others.value=_others.value !=true
    }
    fun dailyRulesChosen(){
        _contributeSentence.value=spannable1
        _daily.value=true
        _weekly.value=false
    }
    fun weeklyRulesChosen(){
        _contributeSentence.value=spannable2
        _weekly.value=true
        _daily.value=false
    }
    private fun checkIfRulesShown():Boolean=sharedPref?.getBoolean((R.string.rules_shown).toString(),false)?:false

    private fun checkInternet():Boolean{
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (connectivityManager != null) {
            val capabilities =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                } else {
                    null
                }
            return if (capabilities != null) {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                false
            }
        }
        else return false
    }
    fun scrimGoneOrNoInternet(){
        if(checkInternet()){
            _scrimVisible.value=false
            if(_noInternet.value==false)
                {getApps()}

        }
        else{
            _noInternet.value=true
        }

    }
    fun makeNoInternetFalse(){
        _noInternet.value=false
    }


    private fun getApps() {
        viewModelScope.launch {
            insertIntoDatabase()
        }
    }

    private suspend fun insertIntoDatabase() {
        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val launchables = pm.queryIntentActivities(main, 0)
        Collections.sort(
            launchables,
            ResolveInfo.DisplayNameComparator(pm)
        )
        val appNameList = ArrayList<String>()
        val appPackageList = ArrayList<String>()

        for (item in launchables) {
            try {
                val nameOfPackage: String = item.activityInfo.packageName
                val nameOfApp = pm.getApplicationLabel(
                    pm.getApplicationInfo(
                        nameOfPackage, PackageManager.GET_META_DATA
                    )
                ) as String
                var copy = false
                for (i in appNameList.indices) {
                    if (nameOfPackage == appPackageList[i]) copy = true
                }
                if (!copy) {
                    appNameList.add(nameOfApp)
                    appPackageList.add(nameOfPackage)
                }
            } catch (e: Exception) {
            }
        }

        for (i in appNameList.indices) {
            val app = AppAndCategory()
            app.appName = appNameList[i]
            app.packageName = appPackageList[i]
            val checkApp = database.isAppExist(appPackageList[i])
            if (checkApp == null) {
                val queryUrl = GOOGLE_URL + app.packageName + "&hl=en"
                val category= try {

                    val document = withContext(Dispatchers.IO) {
                        Jsoup.connect(queryUrl).get()
                    }
                    val text = document?.select("a[itemprop=genre]")
                    if (text == null) {
                        "OTHERS"
                    }
                    val href = text?.attr("abs:href")
                    if (href != null) {

                        if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                            href.substring(
                                href.indexOf(CATEGORY_STRING) +CAT_SIZE,
                                href.length
                            )
                        } else {
                            "OTHERS"
                        }
                    } else {
                        "OTHERS"
                    }
                } catch (e: Exception) {
                    "OTHERS"
                }
                app.appCategory=allotGroup(category)
                Log.i("PaP","${app.appCategory}")
                database.insert(app)
            } /*else if (checkApp.appCategory=="OTHERS") {
                val queryUrl = GOOGLE_URL + checkApp.packageName + "&hl=en"
                val category = try {

                    val document = withContext(Dispatchers.IO) {
                        Jsoup.connect(queryUrl).get()
                    }
                    val text = document?.select("a[itemprop=genre]")
                    if (text == null) {
                        "OTHERS"
                    }
                    val href = text?.attr("abs:href")
                    if (href != null) {

                        if (href.length > 4 && href.contains(CATEGORY_STRING)) {
                            href.substring(
                                href.indexOf(CATEGORY_STRING) + CAT_SIZE,
                                href.length
                            )
                        } else {
                            "OTHERS"
                        }
                    } else {
                        "OTHERS"
                    }
                } catch (e: Exception) {
                    "OTHERS"
                }
                if(category!="OTHERS"){
                    checkApp.appCategory=allotGroup(category)
                    database.update(checkApp)
                }
            }*/
        }
    }


    companion object {
        private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
        private const val CAT_SIZE = 9
        private const val CATEGORY_STRING = "category/"

    }

}
