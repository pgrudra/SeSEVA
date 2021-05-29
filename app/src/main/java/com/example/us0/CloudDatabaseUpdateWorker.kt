package com.example.us0

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.us0.data.AllDatabase
import com.example.us0.data.missions.PartialMission
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CloudDatabaseUpdateWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                Log.i("CDUWT","started")
                val sharedPref = applicationContext.getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
                val cloudReference = Firebase.database.reference
                val user = Firebase.auth.currentUser
                val dao = AllDatabase.getInstance(applicationContext).MissionsDatabaseDao
                val chosenMission=sharedPref.getInt((R.string.chosen_mission_number).toString(), 0)
                Log.i("CDUWT","$chosenMission")
                if(chosenMission!=0){
                    //checkk if chosen mission is completed. If yes, then set shosen mission to 0, else continue
//maybe services closes when this works..check
                    val userId = user!!.uid

                    cloudReference.child("users").child(userId).child("chosenMission")
                        .setValue(chosenMission)
                    Log.i("CDUWT","missionNumberSaved")

                    val moneyToBeUpdated=sharedPref.getInt((R.string.money_to_be_updated).toString(), 0)
                    Log.i("CDUWT","$moneyToBeUpdated")
                    if(moneyToBeUpdated!=0){
                        val referenceToUserMissionContribution=cloudReference.child("users").child(userId).child("contributions").child("$chosenMission")
                        referenceToUserMissionContribution.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var missionContribution:Int?=snapshot.getValue<Int>()
                                Log.i("CDUWT","$missionContribution")
                                if(missionContribution==null){
                                    missionContribution=moneyToBeUpdated
                                    val referenceMissionContributors=cloudReference.child("Users Active").child("$chosenMission")
                                    referenceMissionContributors.runTransaction(object : Transaction.Handler{
                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                            var contributors=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                            Log.i("CDUWT","$contributors")
                                            contributors+=1
                                            Log.i("CDUWT","$contributors"+"latest")
                                            currentData.value=contributors
                                            return Transaction.success(currentData)
                                        }

                                        override fun onComplete(
                                            error: DatabaseError?,
                                            committed: Boolean,
                                            currentData: DataSnapshot?
                                        ) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                /*referenceMissionContributors.addListenerForSingleValueEvent(object: ValueEventListener{
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

                                    })*/
                                }
                                else{
                                    missionContribution+=moneyToBeUpdated
                                }
                                referenceToUserMissionContribution.setValue(missionContribution)
                                    .addOnSuccessListener {
                                        Log.i("CDUWT","missionContriSet")
                                        val referenceToMissionMoneyRaised=cloudReference.child("Money Raised").child("$chosenMission")
                                        referenceToMissionMoneyRaised.runTransaction(object : Transaction.Handler{
                                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                var contribution=currentData.getValue<Int>()?: return Transaction.success(currentData)
                                                Log.i("CDUWT","$contribution")
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
                                                    Log.i("CDUWT","noError")
                                                    with(sharedPref.edit()) {
                                                        this?.putInt((R.string.money_to_be_updated).toString(), 0)
                                                        this?.apply()
                                                    }
                                                    //update mission Contribution Database
                                                    launch (Dispatchers.IO) {

                                                        val mC = dao.doesMissionExist(chosenMission)
                                                        if(mC!=null){
                                                            mC.contribution=missionContribution
                                                            dao.update(mC)
                                                        }
                                                    }
                                                }
                                            }

                                        })
                                    }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })


                    }
                }
                val nowMinusOneDay= Calendar.getInstance().timeInMillis-24*60*60*1000
                val list=dao.getMissionNumbersForReport(nowMinusOneDay,false)
                list?.forEach { i ->
                    run {
                        var missionDescription:String?=null
                        if (!i.onAccomplishDataUpdated) {
                            cloudReference.child("accomplishedMissions").child("${i.missionNumber}")
                                .child("missionDescription")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        missionDescription = snapshot.value.toString()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                        }
                        cloudReference.child("accomplishedMissions").child("${i.missionNumber}")
                            .child("reportAvailable")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val reportAvailable=snapshot.getValue<Boolean>()?:false
                                    missionDescription?.let {
                                        launch(Dispatchers.IO) {
                                            dao.partialUpdate(PartialMission(i.missionNumber, missionDescription!!,true,reportAvailable))
                                        }
                                        }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                }
                //call accomplished missions with reportAvailable=false
                //{if(!onAccomplishDataUpdated)
                //{update mission}
                //check if reportAvailable
                // update(mission)}
                try{

                    Result.success()
                }catch (e: Exception){
                    Result.failure()
                }

            } catch (e: Exception) {

                Log.i("CDUWT","failed")
                Result.failure()
            }


        }
    }
}