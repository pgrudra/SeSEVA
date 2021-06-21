package com.spandverse.seseva.choosemission

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.spandverse.seseva.checkInternetConnectivity
import com.spandverse.seseva.data.sponsors.Sponsor
import com.spandverse.seseva.data.sponsors.SponsorDatabaseDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch


class SponsorDetailsViewModel(sponsorNumber:Int,sponsorDatabaseDao: SponsorDatabaseDao, application: Application): AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val cloudImagesReference = Firebase.storage
    private val cloudReference = Firebase.database.reference.child("sponsors")
    private val _showEntireMissionsList= MutableLiveData<Boolean>()
    val showEntireMissionsList: LiveData<Boolean>
        get()=_showEntireMissionsList
    private val _showNoInternetDialog= MutableLiveData<Boolean>()
    val showNoInternetDialog: LiveData<Boolean>
        get()=_showNoInternetDialog
    private val _expandContractVisibility= MutableLiveData<Boolean>()
    val expandContractVisibility: LiveData<Boolean>
        get()=_expandContractVisibility
    private val _missionsSponsoredVisibility= MutableLiveData<Boolean>()
    val missionsSponsoredVisibility: LiveData<Boolean>
        get()=_missionsSponsoredVisibility
    private val _hideProgress= MutableLiveData<Boolean>()
    val hideProgress: LiveData<Boolean>
        get()=_hideProgress
    private val _sponsorName=MutableLiveData<String>()
    val sponsorName:LiveData<String>
        get()=_sponsorName
    private val _sponsorSite=MutableLiveData<String?>()
    val sponsorSite:LiveData<String?>
        get()=_sponsorSite
    private val _logoReference=MutableLiveData<StorageReference>()
    val logoReference:LiveData<StorageReference>
        get()=_logoReference
    private val _sponsorDescription=MutableLiveData<String>()
    val sponsorDescription:LiveData<String>
        get()=_sponsorDescription
    private val _sponsorAddress=MutableLiveData<String>()
    val sponsorAddress:LiveData<String>
        get()=_sponsorAddress
    private val _shortList=MutableLiveData<String>()
    val shortList:LiveData<String>
        get()=_shortList
    private val _entireList=MutableLiveData<String>()
    val entireList:LiveData<String>
        get()=_entireList
    private val _displayList=MutableLiveData<String>()
    val displayList:LiveData<String>
        get()=_displayList
    init {
        _showEntireMissionsList.value=false
        _shortList.value=""
        _entireList.value=""
        viewModelScope.launch {
            val sponsor: Sponsor? = sponsorDatabaseDao.doesSponsorExist(sponsorNumber)
            if (sponsor == null) {
                cloudReference.child(sponsorNumber.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val missionAmounts = snapshot.child("missionAmounts").value
                            val sponsoredAmount =
                                missionAmounts?.toString()?.split(",")?.map { it.trim().toInt() }
                                    ?.sum() ?: 0
                            val mSponsor = Sponsor(
                                sponsorNumber = sponsorNumber,
                                sponsorName = snapshot.child("sponsorName").value.toString(),
                                sponsorDescription = snapshot.child("sponsorDescription").value.toString(),
                                sponsorAddress = snapshot.child("sponsorAddress").value.toString(),
                                sponsorSite = snapshot.child("sponsorSite").value.toString(),
                                missionsSponsored = if(snapshot.child("missionsSponsored").value==null) "" else snapshot.child("missionsSponsored").value.toString(),
                                missionAmounts = missionAmounts?.toString() ?: "",
                                sponsoredAmount = sponsoredAmount
                            )
                            _sponsorName.value = mSponsor.sponsorName
                            _sponsorDescription.value = mSponsor.sponsorDescription
                            _logoReference.value = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${mSponsor.sponsorNumber}Logo.png")
                            _sponsorSite.value=mSponsor.sponsorSite

                            mSponsor.sponsorAddress?.let { _sponsorAddress.value = it }
                            if(missionAmounts!=null){
                                val missionNamesList = mSponsor.missionsSponsored.split(",").map { it.trim() }
                                val missionAmountsList = mSponsor.missionAmounts.split(",").map { it.trim().toInt() }
                                display(missionNamesList, missionAmountsList)
                            }
                            else{
                                _expandContractVisibility.value=false
                                _missionsSponsoredVisibility.value=false
                            }

                            /*val adapter =
                                SponsoredMissionsAdapter(SponsoredMissionsAdapter.OnClickListener {
                                    //download report
                                })
                            adapter.submitList(list)
                            binding.list.adapter = adapter*/
                            viewModelScope.launch {
                                sponsorDatabaseDao.insert(mSponsor)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (!checkInternetConnectivity(context!!)) {
                                _showNoInternetDialog.value=true
                            }
                        }
                    })
            } else {
                _sponsorName.value = sponsor.sponsorName
                _sponsorDescription.value = sponsor.sponsorDescription
                _logoReference.value = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/sponsorLogos/sponsor${sponsor.sponsorNumber}Logo.png")
                _sponsorSite.value=sponsor.sponsorSite
                sponsor.sponsorAddress?.let { _sponsorAddress.value = it }
                if(sponsor.missionAmounts!=""){
                    val missionNamesList = sponsor.missionsSponsored.split(",").map { it.trim() }
                    val missionAmountsList = sponsor.missionAmounts.split(",").map { it.trim().toInt() }
                    display(missionNamesList, missionAmountsList)
                }
                else{
                    _expandContractVisibility.value=false
                    _missionsSponsoredVisibility.value=false
                }
                /*val adapter=SponsoredMissionsAdapter(SponsoredMissionsAdapter.OnClickListener{
                    //download report
                })
                adapter.submitList(list)
                binding.list.adapter=adapter*/
            }

        }
    }
    fun expandOrContract(){
        _showEntireMissionsList.value = _showEntireMissionsList.value != true
    }
    private fun display(missionNamesList: List<String>?, missionAmountsList: List<Int>?) {
        if (missionAmountsList!!.size < 4) {
            for (i in missionNamesList!!.indices) {
                _shortList.value += missionNamesList[i] + "\nRs " + missionAmountsList[i] + "\n\n"
            }
            _displayList.value=_shortList.value
            _expandContractVisibility.value=false
        } else {
            for (i in missionNamesList!!.indices) {
                _entireList.value += missionNamesList[i] + "\nRs " + missionAmountsList[i] + "\n\n"
                if(i<4){
                    _shortList.value += missionNamesList[i] + "\nRs " + missionAmountsList[i] + "\n\n"
                }
            }
            _displayList.value=_shortList.value
            _expandContractVisibility.value=true
        }
        _hideProgress.value=true
    }

    fun displayEntireList() {
        _displayList.value=_entireList.value
    }

    fun displayShortList() {
        _displayList.value=_shortList.value
    }
}
