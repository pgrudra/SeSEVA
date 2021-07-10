package com.spandverse.seseva

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.data.missions.PartialMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.spandverse.seseva.contributionupdate.ContributionUpdateActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CloudDatabaseUpdateWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val sharedPref = applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
                val cloudReference = Firebase.database.reference
                val user = Firebase.auth.currentUser
                val dao = AllDatabase.getInstance(applicationContext).MissionsDatabaseDao
                val chosenMission=sharedPref.getInt((R.string.chosen_mission_number).toString(), 0)
                if(chosenMission!=0){
                    //checkk if chosen mission is completed. If yes, then set chosen mission to 0, else continue
//maybe services closes when this works..check
                    val userId = user!!.uid
                    cloudReference.child("users").child(userId).child("chosenMission")
                        .setValue(chosenMission)
                    val moneyToBeUpdated=sharedPref.getInt((R.string.money_to_be_updated).toString(), 0)
                    if(moneyToBeUpdated!=0){
                        val missionContributionReference=cloudReference.child("users").child(userId).child("contributions").child("$chosenMission")
                        when(val missionContributionResult=missionContributionReference.singleValueEvent()){
                            is EventResponse.Changed->{
                                var missionContribution:Int?=missionContributionResult.snapshot.getValue<Int>()
                                if(missionContribution==null){
                                    missionContribution=moneyToBeUpdated
                                    val referenceMissionContributors=cloudReference.child("contributors").child("$chosenMission")
                                    referenceMissionContributors.runTransaction(object : Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            var contributors=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                            contributors+=1
                                            currentData.value=contributors
                                            return Transaction.success(currentData)
                                        }
                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                        }
                                    })
                                   }
                                else{
                                    missionContribution+=moneyToBeUpdated
                                }
                                //update mission Contribution Database
                                val mC = dao.doesMissionExist(chosenMission)
                                if(mC!=null){
                                    mC.contribution=missionContribution
                                    dao.update(mC)
                                }
                                missionContributionReference.setValue(missionContribution) .addOnSuccessListener {

                                    val referenceToMissionMoneyRaised=cloudReference.child("moneyRaised").child("$chosenMission")
                                    referenceToMissionMoneyRaised.runTransaction(object : Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            var contribution=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                            contribution+=moneyToBeUpdated
                                            currentData.value=contribution
                                            return Transaction.success(currentData)
                                        }
                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                            if(error==null){
                                                createContributionNotificationChannel()
                                                val intent=Intent(applicationContext,
                                                    ContributionUpdateActivity::class.java).apply{
                                                    flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                                intent.putExtra("missionNumber",chosenMission)
                                                val pendingIntent:PendingIntent= PendingIntent.getActivity(applicationContext,0,intent,0)
                                                val builder=NotificationCompat.Builder(applicationContext,applicationContext.getString(R.string.contribution_update_notification_channel_id))
                                                    .setSmallIcon(R.drawable.ic_seseva_notification_icon)
                                                    .setContentTitle("Your Yesterday's Stats")
                                                    .setContentText("Congos! You raised Rs $moneyToBeUpdated, but missed your chance to raise...")
                                                    .setPriority(NotificationCompat.PRIORITY_MIN)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true)
                                                with(NotificationManagerCompat.from(applicationContext)){
                                                    notify(2,builder.build())
                                                }
                                                with(sharedPref.edit()) {
                                                    this?.putInt((R.string.money_to_be_updated).toString(), 0)
                                                    this?.apply()
                                                }

                                            }
                                        }

                                    })
                                }
                            }
                            is EventResponse.Cancelled->{

                            }
                        }
                        /*val referenceToUserMissionContribution=cloudReference.child("users").child(userId).child("contributions").child("$chosenMission")
                        referenceToUserMissionContribution.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var missionContribution:Int?=snapshot.getValue<Int>()
                                if(missionContribution==null){
                                    missionContribution=moneyToBeUpdated
                                    val referenceMissionContributors=cloudReference.child("Users Active").child("$chosenMission")
                                    referenceMissionContributors.runTransaction(object : Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            var contributors=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                            contributors+=1
                                            currentData.value=contributors
                                            return Transaction.success(currentData)
                                        }
                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                        }
                                    })
                                *//*referenceMissionContributors.addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            var contributors=snapshot.getValue<Int>()
                                            if(contributors!=null){
                                            contributors+=1}
                                            else{
                                                contributors=1
                                            }
                                            referenceMissionContributors.setValue(contributors)
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }

                                    })*//*
                                }
                                else{
                                    missionContribution+=moneyToBeUpdated
                                }
                                referenceToUserMissionContribution.setValue(missionContribution)
                                    .addOnSuccessListener {

                                        val referenceToMissionMoneyRaised=cloudReference.child("Money Raised").child("$chosenMission")
                                        referenceToMissionMoneyRaised.runTransaction(object : Transaction.Handler{
                                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                var contribution=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                                    contribution+=moneyToBeUpdated
                                                currentData.value=contribution

                                                return Transaction.success(currentData)
                                            }
                                            override fun onComplete(
                                                error: DatabaseError?,
                                                committed: Boolean,
                                                currentData: DataSnapshot?
                                            ) {
                                                if(error==null){
                                                    with(sharedPref.edit()) {
                                                        this?.putInt((R.string.money_to_be_updated).toString(), 0)
                                                        this?.apply()
                                                    }

                                                }
                                            }

                                        })
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })*/
                    }
                    else{
                        createContributionNotificationChannel()
                        val intent=Intent(applicationContext, ContributionUpdateActivity::class.java).apply{
                            flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        intent.putExtra("missionNumber",chosenMission)
                        val pendingIntent:PendingIntent= PendingIntent.getActivity(applicationContext,0,intent,0)
                        val builder=NotificationCompat.Builder(applicationContext,applicationContext.getString(R.string.contribution_update_notification_channel_id))
                            .setSmallIcon(R.drawable.ic_seseva_notification_icon)
                            .setContentTitle("Your Yesterday's Stats")
                            .setContentText("You entirely lost your chance to rarise money towards your mission!")
                            .setPriority(NotificationCompat.PRIORITY_MIN)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                        with(NotificationManagerCompat.from(applicationContext)){
                            notify(2,builder.build())
                        }
                    }
                }
                else{
                    //plz choose a mission
                    //intent to home activity
                }
                val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
                val list=dao.getMissionNumbersForReport(nowMinusOneDay,false)
                list?.forEach { i ->
                    run {
                        var missionDescription:String?=null
                        var reportAvailable=false
                        if (!i.onAccomplishDataUpdated) {
                            val missionDescriptionResult=cloudReference.child("accomplishedMissions").child("${i.missionNumber}")
                                .child("missionDescription").singleValueEvent()
                            when(missionDescriptionResult){
                                is EventResponse.Changed->{missionDescription = missionDescriptionResult.snapshot.value.toString()}
                                is EventResponse.Cancelled->{}
                            }
                        }
                        val reportAvailableResult=cloudReference.child("accomplishedMissions").child("${i.missionNumber}")
                            .child("reportAvailable").singleValueEvent()
                        when(reportAvailableResult){
                            is EventResponse.Changed->{reportAvailable = reportAvailableResult.snapshot.getValue<Boolean>()?:false}
                            is EventResponse.Cancelled->{}
                        }
                        missionDescription?.let{
                            dao.partialUpdate(PartialMission(i.missionNumber, it, true, reportAvailable))
                            }

                        }
                }
                //call accomplished missions with reportAvailable=false
                //{if(!onAccomplishDataUpdated)
                //{update mission}
                //check if reportAvailable
                // update(mission)}

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }


        }
    }

    private fun createContributionNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val contributionUpdateNotificationChannel = NotificationChannel(
                applicationContext.getString(R.string.contribution_update_notification_channel_id),
                applicationContext.getString(R.string.contribution_update_notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            )
            val notificationManager:NotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(contributionUpdateNotificationChannel)
        }
    }
}