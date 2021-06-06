package com.example.us0.home.navscreens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.us0.R
import com.example.us0.databinding.FragmentAboutBinding
import com.example.us0.home.DrawerLocker


class AboutFragment : Fragment() {
   private lateinit var binding:FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        val drawerLocker=(activity as DrawerLocker?)
        drawerLocker!!.setDrawerEnabled(false)
        drawerLocker.displayBottomNavigation(false)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding.versionName.text=getString(R.string.version_name,getString(R.string.app_version))
        binding.gmailIcon.setOnClickListener {
            val intent= Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL,"pgr19990109@gmail.com" )
                putExtra(Intent.EXTRA_SUBJECT, "Connecting with SeSEVA")
            }
            val pkgMng=activity?.packageManager
            if(pkgMng!=null){
                if (intent.resolveActivity(pkgMng) != null) {
                    startActivity(intent)
                }
            }

        }
        return binding.root
    }


}