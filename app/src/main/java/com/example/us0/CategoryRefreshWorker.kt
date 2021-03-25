package com.example.us0

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.us0.data.AllDatabase
import com.example.us0.data.apps.AppAndCategory
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
                        if(category!="OTHERS") {
                            i.appCategory = allotGroup(category)
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
                        app.appCategory = allotGroup(category)
                        dao.insert(app)
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