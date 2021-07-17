package com.spandverse.seseva

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.data.apps.AppAndCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.ArrayList

class CategoryRefreshWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val dao = AllDatabase.getInstance(applicationContext).AppDatabaseDao
                val pm = requireNotNull(applicationContext.packageManager)
                val main = Intent(Intent.ACTION_MAIN, null)
                main.addCategory(Intent.CATEGORY_LAUNCHER)
                val launchables = pm.queryIntentActivities(main, 0)
                val appPackageList = ArrayList<String>()
                val otherCategory:List<AppAndCategory>? =dao.getCatApps("OTHERS")
                if(otherCategory!=null){
                    for(i in otherCategory){
                        val nameOfPackage: String =i.packageName
                        val queryUrl = "$GOOGLE_URL$nameOfPackage&hl=en"
                        val category = try {
                            val document = Jsoup.connect(queryUrl).get()

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
                                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
                                    i.appCategory=getSystemCats(ai.category)
                                }
                                else{
                                    i.appCategory="WHITELISTED"
                                }
                                dao.update(i)
                            }
                        }
                        else{
                            i.appCategory = allotedCat
                            dao.update(i)
                        }
                    }
                }
                for (item in launchables) {
                    val nameOfPackage: String = item.activityInfo.packageName
                    appPackageList.add(nameOfPackage)
                    val checkApp = dao.isAppExist(nameOfPackage)
                    if (checkApp == null) {
                        val queryUrl = "$GOOGLE_URL$nameOfPackage&hl=en"
                        val category = try {
                            val document = Jsoup.connect(queryUrl).get()

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
                        val app = AppAndCategory()
                        app.packageName = nameOfPackage
                        app.appName = pm.getApplicationLabel(
                            pm.getApplicationInfo(
                                nameOfPackage, PackageManager.GET_META_DATA
                            )
                        ) as String
                        val allotedCat=allotGroup(category)
                        if(allotedCat=="OTHERS"){
                            val ai=pm.getApplicationInfo(app.packageName,0)
                            if((ai.flags and ApplicationInfo.FLAG_SYSTEM)!=0){
                                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                                    app.appCategory=getSystemCats(ai.category)
                                }
                                else{
                                    app.appCategory="WHITELISTED"
                                }
                                dao.insert(app)
                            }
                            else{
                                app.appCategory = allotedCat
                                dao.insert(app)
                            }
                        }
                        else{
                            app.appCategory = allotedCat
                            dao.insert(app)
                        }
                    }
                }
                val databaseList=dao.getLaunchablesList()
                val deletedApps=databaseList.minus(appPackageList)
                if(deletedApps.isNotEmpty()){
                    for( i in deletedApps){
                        dao.deleteApp(i)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

    companion object {
        private const val GOOGLE_URL = "https://play.google.com/store/apps/details?id="
        private const val CAT_SIZE = 9
        private const val CATEGORY_STRING = "category/"
    }

}