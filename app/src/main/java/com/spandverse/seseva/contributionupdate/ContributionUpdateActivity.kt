package com.spandverse.seseva.contributionupdate

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.spandverse.seseva.MainActivity
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.ActivityContributionUpdateBinding
import com.spandverse.seseva.foregroundnnotifications.PermissionViewModel
import com.spandverse.seseva.foregroundnnotifications.PermissionViewModelFactory
import com.spandverse.seseva.home.HomeActivity
import com.spandverse.seseva.home.PassageViewModel
import com.spandverse.seseva.home.PassageViewModelFactory
import kotlinx.android.synthetic.main.blocking_screen.view.*
import kotlinx.android.synthetic.main.fragment_rules2.*

class ContributionUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContributionUpdateBinding
    private lateinit var viewModel: ContributionUpdateViewModel
    private lateinit var viewModelFactory: ContributionUpdateViewModelFactory
    private val cloudImagesReference = Firebase.storage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contribution_update)
        viewModelFactory = ContributionUpdateViewModelFactory(application)
        val sharedPref=getSharedPreferences((R.string.shared_pref).toString(), Context.MODE_PRIVATE)
        val showWeeklyReward=sharedPref.getInt((R.string.days_after_installation).toString(),1)==0
        val missionNumber=intent.getIntExtra("missionNumber",0)
        val savedMissionNumber=sharedPref.getInt((R.string.chosen_mission_number).toString(),0)
        if(savedMissionNumber!=0 && missionNumber==savedMissionNumber){
            viewModel = ViewModelProvider(this, viewModelFactory).get(ContributionUpdateViewModel::class.java)
            binding.lifecycleOwner = this
            binding.contributionUpdateViewModel = viewModel
            val missionImgRef = cloudImagesReference.getReferenceFromUrl("gs://unslave-0.appspot.com/missionImages/mission${missionNumber}Image.jpg")
            Glide.with(this)
                .load(missionImgRef)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_imageplaceholder)
                        .error(R.drawable.ic_imageplaceholder)
                        .fallback(R.drawable.ic_imageplaceholder)
                )
                .into(binding.imageView)
            viewModel.showRewardDetails.observe(this, Observer<Boolean> {expand->
                if(expand){
                    binding.rewardECIcon.setImageResource(R.drawable.ic_collapse_vector)
                    binding.dailyReward.visibility=View.VISIBLE
                    binding.dailyRewardText.visibility=View.VISIBLE
                    if(showWeeklyReward){
                        binding.weeklyReward.visibility=View.VISIBLE
                        binding.weeklyRewardText.visibility=View.VISIBLE
                    }
                }
                else{
                    binding.rewardECIcon.setImageResource(R.drawable.ic_expand_vector)
                    binding.dailyReward.visibility=View.GONE
                    binding.dailyRewardText.visibility=View.GONE
                    binding.weeklyReward.visibility=View.GONE
                    binding.weeklyRewardText.visibility=View.GONE
                }
            })
            viewModel.showPenaltyDetails.observe(this, Observer<Boolean> {expand->
                if(expand){
                    binding.penaltyECIcon.setImageResource(R.drawable.ic_collapse_vector)
                    binding.penaltyItemsCL.visibility=View.VISIBLE
                }
                else{
                    binding.penaltyECIcon.setImageResource(R.drawable.ic_expand_vector)
                    binding.penaltyItemsCL.visibility=View.GONE
                }
            })
            viewModel.showUsageDetails.observe(this, Observer<Boolean> {expand->
                if(expand){
                    binding.usageECIcon.setImageResource(R.drawable.ic_collapse_vector)
                    binding.usageDetailsCL.visibility=View.VISIBLE
                }
                else{
                    binding.usageECIcon.setImageResource(R.drawable.ic_expand_vector)
                    binding.usageDetailsCL.visibility=View.GONE
                }
            })
            viewModel.nonZeroMoneyRaised.observe(this, Observer<Boolean> {nonZero->
                if(!nonZero){
                    binding.youRaised.visibility=View.GONE
                    binding.youRaisedText.visibility=View.GONE
                    binding.raisedNothingText.visibility=View.VISIBLE
                }

            })
            viewModel.goToHome.observe(this, Observer<Boolean> {toHome->
                if(toHome){
                    goToHome()
                }

            })
            viewModel.penaltiesExpandContractInvisible.observe(this, Observer<Boolean> {invisible->
                if(invisible){
                    binding.penaltyECIcon.visibility= View.GONE
                }
            })
            viewModel.socialPenalty.observe(this, Observer<String> {
                binding.socialPenaltyText.visibility=View.VISIBLE
                binding.socialPenalty.visibility=View.VISIBLE
            })
            viewModel.comPenalty.observe(this, Observer<String> {
                binding.comPenaltyText.visibility=View.VISIBLE
                binding.comPenalty.visibility=View.VISIBLE
            })
            viewModel.gamesPenalty.observe(this, Observer<String> {
                binding.gamesPenaltyText.visibility=View.VISIBLE
                binding.gamesPenalty.visibility=View.VISIBLE
            })
            viewModel.videoPenalty.observe(this, Observer<String> {
                binding.videoPenaltyText.visibility=View.VISIBLE
                binding.videoPenalty.visibility=View.VISIBLE
            })
            viewModel.othersPenalty.observe(this, Observer<String> {
                binding.othersPenaltyText.visibility=View.VISIBLE
                binding.othersPenalty.visibility=View.VISIBLE
            })
            viewModel.msnbsPenalty.observe(this, Observer<String> {
                binding.msnbsPenaltyText.visibility=View.VISIBLE
                binding.msnbsPenalty.visibility=View.VISIBLE
            })
            viewModel.entertainmentPenalty.observe(this, Observer<String> {
                binding.entertainmentPenaltyText.visibility=View.VISIBLE
                binding.entertainmentPenalty.visibility=View.VISIBLE
            })
        }
        else{
            goToHome()
        }

    }
    private fun goToHome() {
        val intent= Intent(this, MainActivity::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        finish()
    }


}