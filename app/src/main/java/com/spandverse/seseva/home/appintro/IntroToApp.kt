package com.spandverse.seseva.home.appintro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.spandverse.seseva.R
import com.spandverse.seseva.databinding.FragmentIntroToAppBinding


class IntroToApp : Fragment() {
    private lateinit var binding: FragmentIntroToAppBinding
    private lateinit var viewModel: IntroToAppViewModel
    private lateinit var viewModelFactory: IntroToAppViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro_to_app, container, false)
        val application = requireNotNull(this.activity).application
        viewModelFactory = IntroToAppViewModelFactory(application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(IntroToAppViewModel::class.java)
        binding.introViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
/*binding.continueButton.setOnClickListener {
    NavHostFragment.findNavController(this).navigate(IntroToAppDirections.actionIntroToApp3ToPassageFragment())
}*/
        viewModel.goToNextScreen.observe(viewLifecycleOwner, Observer<Boolean> { go ->
            if (go) {
                NavHostFragment.findNavController(this).navigate(IntroToAppDirections.actionIntroToApp3ToPassageFragment())
                viewModel.nextScreenComplete()
            }
        })
        viewModel.slideNumber.observe(viewLifecycleOwner, Observer<Int> { i ->
            when (i) {
                0 -> {
                    binding.rightButton.visibility=View.VISIBLE
                    binding.leftButton.visibility=View.GONE
                    binding.continueButton.visibility=View.GONE
                    binding.t0.startAnimation(AlphaAnimation(0F,1F).apply{
                        duration=300
                        fillAfter=true
                    })
                    binding.t1.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                    binding.t2.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                }
                1 -> {
                    binding.rightButton.visibility=View.VISIBLE
                    binding.leftButton.visibility=View.VISIBLE
                    binding.continueButton.visibility=View.GONE
                    binding.t0.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                    binding.t1.startAnimation(AlphaAnimation(0F,1F).apply{
                        duration=300
                        fillAfter=true
                    })
                    binding.t2.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                }
                2 -> {
                    binding.rightButton.visibility=View.GONE
                    binding.leftButton.visibility=View.VISIBLE
                    binding.continueButton.visibility=View.VISIBLE
                    binding.t0.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                    binding.t1.startAnimation(AlphaAnimation(1F,0F).apply{
                        duration=0
                        fillAfter=true
                    })
                    binding.t2.startAnimation(AlphaAnimation(0F,1F).apply{
                        duration=300
                        fillAfter=true
                    })
                }
            }
        })
        return binding.root
    }


}