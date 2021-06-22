package com.spandverse.seseva.home.rules

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Process
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
import com.spandverse.seseva.*
import com.spandverse.seseva.data.apps.AppAndCategory
import com.spandverse.seseva.data.apps.AppDataBaseDao
import com.spandverse.seseva.foregroundnnotifications.TestService
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.*

class Rules2ViewModel(
    private val database: AppDataBaseDao,
    application: Application,
    private val pm: PackageManager
) :
    AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val sharedPref = context.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
    private val cloudReference = Firebase.database.reference

    private val _toHomeFragment = MutableLiveData<Boolean>()
    val toHomeFragment: LiveData<Boolean>
        get() = _toHomeFragment

    private val _toolBarNDrawer = MutableLiveData<Boolean>()
    val toolBarNDrawer: LiveData<Boolean>
        get() = _toolBarNDrawer

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

    private val _socialMaxTime=MutableLiveData<String>()
    val socialMaxTime:LiveData<String>
        get()=_socialMaxTime
    private val _socialMaxLaunches=MutableLiveData<String>()
    val socialMaxLaunches:LiveData<String>
        get()=_socialMaxLaunches
    private val _socialPenalty=MutableLiveData<String>()
    val socialPenalty:LiveData<String>
        get()=_socialPenalty
    private val _communicationMaxTime=MutableLiveData<String>()
    val communicationMaxTime:LiveData<String>
        get()=_communicationMaxTime
    private val _communicationMaxLaunches=MutableLiveData<String>()
    val communicationMaxLaunches:LiveData<String>
        get()=_communicationMaxLaunches
    private val _communicationPenalty=MutableLiveData<String>()
    val communicationPenalty:LiveData<String>
        get()=_communicationPenalty
    private val _gamesMaxTime=MutableLiveData<String>()
    val gamesMaxTime:LiveData<String>
        get()=_gamesMaxTime
    private val _gamesMaxLaunches=MutableLiveData<String>()
    val gamesMaxLaunches:LiveData<String>
        get()=_gamesMaxLaunches
    private val _gamesPenalty=MutableLiveData<String>()
    val gamesPenalty:LiveData<String>
        get()=_gamesPenalty
    private val _entertainmentMaxTime=MutableLiveData<String>()
    val entertainmentMaxTime:LiveData<String>
        get()=_entertainmentMaxTime
    private val _entertainmentMaxLaunches=MutableLiveData<String>()
    val entertainmentMaxLaunches:LiveData<String>
        get()=_entertainmentMaxLaunches
    private val _entertainmentPenalty=MutableLiveData<String>()
    val entertainmentPenalty:LiveData<String>
        get()=_entertainmentPenalty
    private val _videoMaxTime=MutableLiveData<String>()
    val videoMaxTime:LiveData<String>
        get()=_videoMaxTime
    private val _videoMaxLaunches=MutableLiveData<String>()
    val videoMaxLaunches:LiveData<String>
        get()=_videoMaxLaunches
    private val _videoPenalty=MutableLiveData<String>()
    val videoPenalty:LiveData<String>
        get()=_videoPenalty
    private val _msnbsMaxTime=MutableLiveData<String>()
    val msnbsMaxTime:LiveData<String>
        get()=_msnbsMaxTime
    private val _msnbsMaxLaunches=MutableLiveData<String>()
    val msnbsMaxLaunches:LiveData<String>
        get()=_msnbsMaxLaunches
    private val _msnbsPenalty=MutableLiveData<String>()
    val msnbsPenalty:LiveData<String>
        get()=_msnbsPenalty
    private val _othersMaxTime=MutableLiveData<String>()
    val othersMaxTime:LiveData<String>
        get()=_othersMaxTime
    private val _othersMaxLaunches=MutableLiveData<String>()
    val othersMaxLaunches:LiveData<String>
        get()=_othersMaxLaunches
    private val _othersPenalty=MutableLiveData<String>()
    val othersPenalty:LiveData<String>
        get()=_othersPenalty


    private var spannable1=SpannableString("Raise upto Rs... per day by adhering to below rules")
    private var spannable2=SpannableString("Raise upto Rs..  per week by adhering to below rules")
    private val _daily = MutableLiveData<Boolean>()
    val daily: LiveData<Boolean>
        get() = _daily
    private val _weekly = MutableLiveData<Boolean>()
    val weekly: LiveData<Boolean>
        get() = _weekly
    private val _scrimVisible = MutableLiveData<Boolean>()
    val scrimVisible: LiveData<Boolean>
        get() = _scrimVisible
    private val _iUnderstandRulesVisible = MutableLiveData<Boolean>()
    val iUnderstandRulesVisible: LiveData<Boolean>
        get() = _iUnderstandRulesVisible
    private val _noInternet = MutableLiveData<Boolean>()
    val noInternet: LiveData<Boolean>
        get() = _noInternet
    val socialApps=database.getAll("SOCIAL")
    val communicationApps=database.getAll("COMM. & BROWSING")
    val gamesApps=database.getAll("GAMES")
    val entertainmentApps=database.getAll("ENTERTAINMENT")
    val videoApps=database.getAll("VIDEO & COMICS")
    val msnbsApps=database.getAll("MSNBS")
    val whitelistedApps=database.getAll("WHITELISTED")
    val otherApps=database.getAll("OTHERS")
    init {
        val serviceRestart=checkUsageAccessPermission()
         when {
            checkIfRulesShown() -> {

                _scrimVisible.value=false
                //_toolBarNDrawer.value=true
                loadRules()
                _iUnderstandRulesVisible.value=false
            }
            checkInternet() -> {
                getAndLoadRules(serviceRestart)
                getApps()
                _iUnderstandRulesVisible.value=true
               // _toolBarNDrawer.value=false
            }
            else -> {
                _noInternet.value=true
                //_toolBarNDrawer.value=false
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
    private fun checkUsageAccessPermission():Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.packageName?.let {
                appOps.unsafeCheckOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        } else {
            context?.packageName?.let {
                appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(), it
                )
            }
        }
        return if (mode == AppOpsManager.MODE_ALLOWED){
            _toolBarNDrawer.value=true
            true
        } else{
            _toolBarNDrawer.value=false
            false
        }

    }

    private fun loadRules() {
        _socialMaxTime.value=sharedPref.getInt((R.string.social_max_time).toString(),0).toString()+" min"
        _socialMaxLaunches.value=sharedPref.getInt((R.string.social_max_launches).toString(),0).toString()
        _socialPenalty.value="Rs "+sharedPref.getInt((R.string.social_penalty).toString(),0).toString()
        _communicationMaxTime.value=sharedPref.getInt((R.string.communication_max_time).toString(),0).toString()+" min"
        _communicationMaxLaunches.value=sharedPref.getInt((R.string.communication_max_launches).toString(),0).toString()
        _communicationPenalty.value="Rs "+sharedPref.getInt((R.string.communication_penalty).toString(),0).toString()
        _gamesMaxTime.value=sharedPref.getInt((R.string.games_max_time).toString(),0).toString()+" min"
        _gamesMaxLaunches.value=sharedPref.getInt((R.string.games_max_launches).toString(),0).toString()
        _gamesPenalty.value="Rs "+sharedPref.getInt((R.string.games_penalty).toString(),0).toString()
        _othersMaxTime.value=sharedPref.getInt((R.string.others_max_time).toString(),0).toString()+" min"
        _othersMaxLaunches.value=sharedPref.getInt((R.string.others_max_launches).toString(),0).toString()
        _othersPenalty.value="Rs "+sharedPref.getInt((R.string.others_penalty).toString(),0).toString()
        _entertainmentMaxTime.value=sharedPref.getInt((R.string.entertainment_max_time).toString(),0).toString()+" min"
        _entertainmentMaxLaunches.value=sharedPref.getInt((R.string.entertainment_max_launches).toString(),0).toString()
        _entertainmentPenalty.value="Rs "+sharedPref.getInt((R.string.entertainment_penalty).toString(),0).toString()
        _msnbsMaxTime.value=sharedPref.getInt((R.string.msnbs_max_time).toString(),0).toString()+" min"
        _msnbsMaxLaunches.value=sharedPref.getInt((R.string.msnbs_max_launches).toString(),0).toString()
        _msnbsPenalty.value="Rs "+sharedPref.getInt((R.string.msnbs_penalty).toString(),0).toString()
        _videoMaxTime.value=sharedPref.getInt((R.string.video_max_time).toString(),0).toString()+" min"
        _videoMaxLaunches.value=sharedPref.getInt((R.string.video_max_launches).toString(),0).toString()
        _videoPenalty.value="Rs "+sharedPref.getInt((R.string.video_penalty).toString(),0).toString()
        spannable1=SpannableString("Raise upto Rs ${sharedPref.getInt((R.string.daily_reward).toString(),0)} per day by adhering to below rules")
        spannable2= SpannableString("Raise upto Rs ${sharedPref.getInt((R.string.weekly_reward).toString(),0)} per week by adhering to below rules")
        spannable1.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable1.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable2.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        _contributeSentence.value=spannable1
    }

    private fun getAndLoadRules(serviceRestart: Boolean) {
        val rulesNumber=sharedPref.getInt((R.string.rules_number).toString(), 0)
        cloudReference.child("rules").child(rulesNumber.toString()).get().addOnSuccessListener {
            val sMT=it.child("socialMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.social_max_time).toString(),sMT.toInt())
                this?.apply() }
            val sML=it.child("socialMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.social_max_launches).toString(), sML.toInt())
                this?.apply() }
            val sP=it.child("socialPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.social_penalty).toString(), sP.toInt())
                this?.apply() }
            val cMT=it.child("communicationMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.communication_max_time).toString(), cMT.toInt())
                this?.apply() }
            val cML=it.child("communicationMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.communication_max_launches).toString(), cML.toInt())
                this?.apply() }
            val cP=it.child("communicationPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.communication_penalty).toString(), cP.toInt())
                this?.apply() }
            val gMT=it.child("gamesMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.games_max_time).toString(), gMT.toInt())
                this?.apply() }
            val gML=it.child("gamesMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.games_max_launches).toString(), gML.toInt())
                this?.apply() }
            val gP=it.child("gamesPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.games_penalty).toString(), gP.toInt())
                this?.apply() }
            val vMT=it.child("videoMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.video_max_time).toString(), vMT.toInt())
                this?.apply() }
            val vML=it.child("videoMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.video_max_launches).toString(), vML.toInt())
                this?.apply() }
            val vP=it.child("videoPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.video_penalty).toString(), vP.toInt())
                this?.apply() }
            val mMT=it.child("msnbsMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.msnbs_max_time).toString(), mMT.toInt())
                this?.apply() }
            val mML=it.child("msnbsMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.msnbs_max_launches).toString(), mML.toInt())
                this?.apply() }
            val mP=it.child("msnbsPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.msnbs_penalty).toString(), mP.toInt())
                this?.apply() }
            val oMT=it.child("othersMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.others_max_time).toString(), oMT.toInt())
                this?.apply() }
            val oML=it.child("othersMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.others_max_launches).toString(), oML.toInt())
                this?.apply() }
            val oP=it.child("othersPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.others_penalty).toString(), oP.toInt())
                this?.apply() }
            val eMT=it.child("entertainmentMaxTime").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.entertainment_max_time).toString(), eMT.toInt())
                this?.apply() }
            val eML=it.child("entertainmentMaxLaunches").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.entertainment_max_launches).toString(), eML.toInt())
                this?.apply() }
            val eP= it.child("entertainmentPenalty").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.entertainment_penalty).toString(),eP.toInt())
                this?.apply() }
            val dR=it.child("dailyReward").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.daily_reward).toString(),dR.toInt())
                this?.apply() }
            val wR=it.child("weeklyReward").value.toString()
            with(sharedPref?.edit()) { this?.putInt((R.string.weekly_reward).toString(),wR.toInt())
                this?.apply() }
            spannable1=SpannableString("Raise upto Rs $dR per day by adhering to below rules")
            spannable2= SpannableString("Raise upto Rs $wR per week by adhering to below rules")
            spannable1.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable1.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable2.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),11,16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable2.setSpan(RelativeSizeSpan(1.333f),11,16,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            _contributeSentence.value=spannable1
            _socialMaxTime.value= "$sMT min"
            _socialMaxLaunches.value=sML
            _socialPenalty.value= "Rs $sP"
            _communicationMaxTime.value= "$cMT min"
            _communicationMaxLaunches.value=cML
            _communicationPenalty.value= "Rs $cP"
            _gamesMaxTime.value= "$gMT min"
            _gamesMaxLaunches.value=gML
            _gamesPenalty.value= "Rs $gP"
            _othersMaxTime.value= "$oMT min"
            _othersMaxLaunches.value=oML
            _othersPenalty.value= "Rs $oP"
            _entertainmentMaxTime.value= "$eMT min"
            _entertainmentMaxLaunches.value=eML
            _entertainmentPenalty.value= "Rs $eP"
            _msnbsMaxTime.value= "$mMT min"
            _msnbsMaxLaunches.value=mML
            _msnbsPenalty.value= "Rs $mP"
            _videoMaxTime.value= "$vMT min"
            _videoMaxLaunches.value=vML
            _videoPenalty.value= "Rs $vP"
            with(sharedPref?.edit()) {
                this?.putInt((R.string.saved_rules_number).toString(), rulesNumber)
                this?.apply()
            }
            if(sharedPref.getBoolean((R.string.onboarding_done).toString(),false)) {
                if (serviceRestart) {
                    actionOnService(Actions.STOP)
                    actionOnService(Actions.START)
                }
            }
        }
    }

    private fun actionOnService(action: Actions) {
        if(getServiceState(context)==ServiceState.STOPPED && action==Actions.STOP) return
        Intent(context, TestService::class.java).also{
            it.action= Actions.START.name
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                context?.startForegroundService(it)
            }
            else{
                context?.startService(it)
            }
        }
    }

    fun goToHome() {
        /*with(sharedPref?.edit()) {
            this?.putBoolean((R.string.rules_shown).toString(), true)
            this?.apply()
        }*/
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
    private fun checkIfRulesShown():Boolean{
        val rN=sharedPref.getInt((R.string.rules_number).toString(),-1)
        val sRN=sharedPref.getInt((R.string.saved_rules_number).toString(),-2)
        return rN==sRN
    }

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
                    val k=database.isAppExist(nameOfPackage)
                    if(k==null){
                        val app = AppAndCategory()
                        app.appName = nameOfApp
                        app.packageName = nameOfPackage
                        database.insert(app)
                    }

                }
            } catch (e: Exception) {
            }
        }
        val appsList: List<AppAndCategory>? = database.getList()
        if (appsList != null) {
            for (i in appsList) {
                val queryUrl = GOOGLE_URL + i.packageName + "&hl=en"
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
                val allotedCat=allotGroup(category)
                if(allotedCat=="OTHERS"){
                    val ai=pm.getApplicationInfo(i.packageName,0)
                    if((ai.flags and ApplicationInfo.FLAG_SYSTEM)!=0){
                        i.appCategory="WHITELISTED"
                        database.update(i)
                    }
                }
                else{
                    i.appCategory = allotedCat
                    database.update(i)
                }

            }
        }
    }
        /*for (i in appNameList.indices) {
            val app = AppAndCategory()
            app.appName = appNameList[i]
            app.packageName = appPackageList[i]
            val checkApp = database.isAppExist(appPackageList[i])
            if (checkApp == null) {
                val queryUrl = GOOGLE_URL + app.packageName + "&hl=en"
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
                app.appCategory = allotGroup(category)
                Log.i("PaP", "${app.appCategory}")
                database.insert(app)
            } else if (checkApp.appCategory == "OTHERS") {
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
                if (category != "OTHERS") {
                    checkApp.appCategory = allotGroup(category)
                    database.update(checkApp)
                }
            }
        }*/


        companion object {
            private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
            private const val CAT_SIZE = 9
            private const val CATEGORY_STRING = "category/"

        }

}
