package com.example.us0.home.usagestats

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.us0.*
import com.example.us0.data.AllDatabase
import com.example.us0.databinding.FragmentAppUsageBinding
import com.example.us0.databinding.FragmentCategoryUsageBinding
import com.example.us0.home.DrawerLocker
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.util.*


class AppUsageFragment : Fragment() {
    private lateinit var binding: FragmentAppUsageBinding
    private val viewModel:UsageOverViewViewModel by activityViewModels()
    //private lateinit var viewModelFactory: AppUsageViewModelFactory
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_usage, container, false)
        var noChartsToDisplay=false
        val application = requireNotNull(this.activity).application
        val statDatabaseDao = AllDatabase.getInstance(application).StatDataBaseDao
        val appPackageName: String = AppUsageFragmentArgs.fromBundle(requireArguments()).appPackageName
        binding.usageOverViewViewModel=viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.appPackageNameForAppScreen.observe(viewLifecycleOwner, Observer {
            binding.appIcon.setImageDrawable(context?.packageManager?.getApplicationIcon(it))
        })
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_navdrawer_icon)
        binding.toolbar.setNavigationOnClickListener { v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v) }
        drawerLocker!!.setDrawerEnabled(true)
        drawerLocker.displayBottomNavigation(true)

        val timeWeekChart=binding.timeSpentBarChart
        timeWeekChart.setNoDataText("Loading")
        timeWeekChart.xAxis.position= XAxis.XAxisPosition.BOTTOM
        timeWeekChart.xAxis.setDrawGridLines(false)
        timeWeekChart.setDrawGridBackground(false)
        timeWeekChart.animateY(500)
        timeWeekChart.legend.isEnabled=false
        timeWeekChart.axisRight.isEnabled=false
        timeWeekChart.axisLeft.setDrawGridLines(false)
        timeWeekChart.description.isEnabled=false
        timeWeekChart.xAxis.textColor=R.color.disabled_text
        timeWeekChart.axisLeft.textColor=R.color.disabled_text
        timeWeekChart.invalidate()
        val launchesWeekChart=binding.appLaunchesBarChart
        launchesWeekChart.setNoDataText("Loading")
        launchesWeekChart.xAxis.position= XAxis.XAxisPosition.BOTTOM
        launchesWeekChart.xAxis.setDrawGridLines(false)
        launchesWeekChart.setDrawGridBackground(false)
        launchesWeekChart.animateY(500)
        launchesWeekChart.legend.isEnabled=false
        launchesWeekChart.axisRight.isEnabled=false
        launchesWeekChart.axisLeft.setDrawGridLines(false)
        launchesWeekChart.description.isEnabled=false
        launchesWeekChart.xAxis.textColor=R.color.disabled_text
        launchesWeekChart.axisLeft.textColor=R.color.disabled_text
        launchesWeekChart.invalidate()

        val timeMonthChart=binding.timeSpentMonthLineChart
        timeMonthChart.setNoDataText("Loading")
        timeMonthChart.xAxis.position=XAxis.XAxisPosition.BOTTOM
        timeMonthChart.xAxis.setDrawGridLines(false)
        timeMonthChart.setDrawGridBackground(false)
        timeMonthChart.animateY(500)
        timeMonthChart.legend.isEnabled=false
        timeMonthChart.axisLeft.isEnabled=false
        timeMonthChart.axisRight.setDrawGridLines(false)
        timeMonthChart.description.isEnabled=false
        timeMonthChart.xAxis.textColor= Color.WHITE
        timeMonthChart.axisRight.textColor= Color.WHITE
        timeMonthChart.invalidate()

        val launchesMonthChart=binding.appLaunchesMonthLineChart
        launchesMonthChart.setNoDataText("Loading")
        launchesMonthChart.xAxis.position=XAxis.XAxisPosition.BOTTOM
        launchesMonthChart.xAxis.setDrawGridLines(false)
        launchesMonthChart.setDrawGridBackground(false)
        launchesMonthChart.animateY(500)
        launchesMonthChart.legend.isEnabled=false
        launchesMonthChart.axisLeft.isEnabled=false
        launchesMonthChart.axisRight.setDrawGridLines(false)
        launchesMonthChart.description.isEnabled=false
        launchesMonthChart.xAxis.textColor= Color.WHITE
        launchesMonthChart.axisRight.textColor= Color.WHITE
        launchesMonthChart.invalidate()

        val timeYearChart=binding.timeSpentYearLineChart
        timeYearChart.setNoDataText("Loading")
        timeYearChart.xAxis.position=XAxis.XAxisPosition.BOTTOM
        timeYearChart.xAxis.setDrawGridLines(false)
        timeYearChart.setDrawGridBackground(false)
        timeYearChart.animateY(500)
        timeYearChart.legend.isEnabled=false
        timeYearChart.axisLeft.isEnabled=false
        timeYearChart.axisRight.setDrawGridLines(false)
        timeYearChart.description.isEnabled=false
        timeYearChart.xAxis.textColor= Color.WHITE
        timeYearChart.axisRight.textColor= Color.WHITE
        timeYearChart.invalidate()

        val launchesYearChart=binding.appLaunchesYearLineChart
        launchesYearChart.setNoDataText("Loading")
        launchesYearChart.xAxis.position=XAxis.XAxisPosition.BOTTOM
        launchesYearChart.xAxis.setDrawGridLines(false)
        launchesYearChart.setDrawGridBackground(false)
        launchesYearChart.animateY(500)
        launchesYearChart.legend.isEnabled=false
        launchesYearChart.axisLeft.isEnabled=false
        launchesYearChart.axisRight.setDrawGridLines(false)
        launchesYearChart.description.isEnabled=false
        launchesYearChart.xAxis.textColor= Color.WHITE
        launchesYearChart.axisRight.textColor= Color.WHITE
        launchesYearChart.invalidate()
        viewLifecycleOwner.lifecycleScope.launch {
            val lastWeekStart: Calendar = Calendar.getInstance()
            lastWeekStart.add(Calendar.DATE, -8)
            val pastWeekData=statDatabaseDao.getTimeLaunchesDate(
                pkg = appPackageName,
                d = lastWeekStart.timeInMillis
            )
            if(pastWeekData.isEmpty()){
                noChartsToDisplay=true
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.GONE
                binding.noChartsText.visibility=View.VISIBLE
            }
            else{
                noChartsToDisplay=false
                var timeWeekAggregate=0
                var launchesWeekAggregate=0
                val weekDataIterator=pastWeekData.iterator()
                var weekData=weekDataIterator.next()
                timeWeekAggregate+=weekData.time?:0
                launchesWeekAggregate+=weekData.launches?:0
                val weekTimeEntries: ArrayList<BarEntry> = ArrayList()
                val weekLaunchesEntries: ArrayList<BarEntry> = ArrayList()
                val weekLabels = ArrayList<String>()
                for(i in 0..6){
                    lastWeekStart.add(Calendar.DATE, 1)
                    weekLabels.add(getDay(lastWeekStart.get(Calendar.DAY_OF_WEEK)))
                    val calender= Calendar.getInstance()
                    calender.timeInMillis=weekData.date!!
                    if(lastWeekStart.get(Calendar.DATE)==calender.get(Calendar.DATE)){
                        weekTimeEntries.add(BarEntry(i.toFloat(), ((weekData.time ?: 0) / 60).toFloat()))
                        weekLaunchesEntries.add(BarEntry(i.toFloat(),(weekData.launches?:0).toFloat()))
                        //made data entry
                        if(weekDataIterator.hasNext()){
                            weekData=weekDataIterator.next()
                            timeWeekAggregate+=weekData.time?:0
                            launchesWeekAggregate+=weekData.launches?:0
                        }
                    }
                    else{
                        weekTimeEntries.add(BarEntry(i.toFloat(), 0.toFloat()))
                        weekLaunchesEntries.add(BarEntry(i.toFloat(), 0.toFloat()))
                        //made an 0 entry
                    }
                }
                val weekTimeDataSet= BarDataSet(weekTimeEntries, "Time in mins")
                val weekTimeData= BarData(weekTimeDataSet)
                timeWeekChart.data=weekTimeData
                val xAxisWeekTimeFormatter: ValueFormatter = WeekAxisValueFormatter(binding.timeSpentBarChart, weekLabels)
                timeWeekChart.xAxis.valueFormatter=xAxisWeekTimeFormatter

                val weekLaunchesDataSet= BarDataSet(weekLaunchesEntries, "Launches")
                val weekLaunchesData= BarData(weekLaunchesDataSet)
                launchesWeekChart.data=weekLaunchesData
                val xAxisWeekLaunchesFormatter: ValueFormatter = WeekAxisValueFormatter(binding.appLaunchesBarChart, weekLabels)
                launchesWeekChart.xAxis.valueFormatter=xAxisWeekLaunchesFormatter
                binding.timeSpentWeekAggregate.text= secToHrMin(timeWeekAggregate)
                binding.appLaunchesWeekAggregate.text=launchesWeekAggregate.toString()

                timeWeekChart.invalidate()
                launchesWeekChart.invalidate()

                val lastMonthStart: Calendar = Calendar.getInstance()
                lastMonthStart.add(Calendar.DATE, -30)
                val pastMonthData=statDatabaseDao.getTimeLaunchesDate(
                    pkg = appPackageName,
                    d = lastMonthStart.timeInMillis
                )
                var timeMonthAggregate=0
                var launchesMonthAggregate=0
                val monthDataIterator=pastMonthData.iterator()
                var monthData=monthDataIterator.next()
                timeMonthAggregate+=monthData.time?:0
                launchesMonthAggregate+=monthData.launches?:0
                val monthTimeEntries: ArrayList<Entry> = ArrayList()
                val monthLaunchesEntries: ArrayList<Entry> = ArrayList()
                val monthLabels = ArrayList<String>()
                for(i in 0..28){
                    lastMonthStart.add(Calendar.DATE, 1)
                    monthLabels.add(getDayAndMonth(lastMonthStart))
                    val calender=Calendar.getInstance()
                    calender.timeInMillis=monthData.date!!
                    if(checkIfSameDay(lastMonthStart,calender)){
                        monthTimeEntries.add(Entry(i.toFloat(), ((monthData.time ?: 0) / 60).toFloat()))
                        monthLaunchesEntries.add(Entry(i.toFloat(),(monthData.launches?:0).toFloat()))
                        //made data entry
                        if(monthDataIterator.hasNext()){
                            monthData=monthDataIterator.next()
                            timeMonthAggregate+=monthData.time?:0
                            launchesMonthAggregate+=monthData.launches?:0
                        }
                    }
                    else{
                        monthTimeEntries.add(Entry(i.toFloat(), 0.toFloat()))
                        monthLaunchesEntries.add(Entry(i.toFloat(), 0.toFloat()))
                        //made an 0 entry
                    }
                }
                val monthTimeDataSet= LineDataSet(monthTimeEntries, "Time in mins")
                val monthTimeData= LineData(monthTimeDataSet)
                timeMonthChart.data=monthTimeData
                val xAxisMonthTimeFormatter: ValueFormatter = MonthAxisValueFormatter(timeMonthChart, monthLabels)
                timeMonthChart.xAxis.valueFormatter=xAxisMonthTimeFormatter

                val monthLaunchesDataSet= LineDataSet(monthLaunchesEntries, "Launches")
                val monthLaunchesData= LineData(monthLaunchesDataSet)
                launchesMonthChart.data=monthLaunchesData
                val xAxisMonthLaunchesFormatter: ValueFormatter = MonthAxisValueFormatter(launchesMonthChart, monthLabels)
                launchesMonthChart.xAxis.valueFormatter=xAxisMonthLaunchesFormatter
                binding.timeSpentMonthAggregate.text=secToHrMin(timeMonthAggregate)
                binding.appLaunchesMonthAggregate.text=launchesMonthAggregate.toString()
                timeMonthChart.invalidate()
                launchesMonthChart.invalidate()

                val lastYearStart: Calendar = Calendar.getInstance()
                lastYearStart.add(Calendar.DATE, -365)
                val pastYearData=statDatabaseDao.getTimeLaunchesDate(
                    pkg = appPackageName,
                    d = lastYearStart.timeInMillis
                )
                var timeYearAggregate=0
                var launchesYearAggregate=0
                val yearDataIterator=pastYearData.iterator()
                var yearData=yearDataIterator.next()
                timeYearAggregate+=yearData.time?:0
                launchesYearAggregate+=yearData.launches?:0
                val yearTimeEntries: ArrayList<Entry> = ArrayList()
                val yearLaunchesEntries: ArrayList<Entry> = ArrayList()
                val yearLabels = ArrayList<String>()
                for(i in 0..363){
                    lastYearStart.add(Calendar.DATE, 1)
                    yearLabels.add(getDayAndMonth(lastYearStart))
                    val calender=Calendar.getInstance()
                    calender.timeInMillis=yearData.date!!
                    if(checkIfSameDay(lastYearStart,calender)){
                        yearTimeEntries.add(Entry(i.toFloat(), ((yearData.time ?: 0) / 60).toFloat()))
                        yearLaunchesEntries.add(Entry(i.toFloat(),(yearData.launches?:0).toFloat()))
                        //made data entry
                        if(yearDataIterator.hasNext()){
                            yearData=yearDataIterator.next()
                            timeYearAggregate+=yearData.time?:0
                            launchesYearAggregate+=yearData.launches?:0
                        }
                    }
                    else{
                        yearTimeEntries.add(Entry(i.toFloat(), 0.toFloat()))
                        yearLaunchesEntries.add(Entry(i.toFloat(), 0.toFloat()))
                        //made an 0 entry
                    }
                }
                val yearTimeDataSet= LineDataSet(yearTimeEntries, "Time in mins")
                val yearTimeData= LineData(yearTimeDataSet)
                timeYearChart.data=yearTimeData
                val xAxisYearTimeFormatter: ValueFormatter = YearAxisValueFormatter(timeYearChart, yearLabels)
                timeYearChart.xAxis.valueFormatter=xAxisYearTimeFormatter

                val yearLaunchesDataSet= LineDataSet(yearLaunchesEntries, "Launches")
                val yearLaunchesData= LineData(yearLaunchesDataSet)
                launchesYearChart.data=yearLaunchesData
                val xAxisYearLaunchesFormatter: ValueFormatter = YearAxisValueFormatter(launchesYearChart, yearLabels)
                launchesYearChart.xAxis.valueFormatter=xAxisYearLaunchesFormatter
                binding.timeSpentYearAggregate.text=secToHrMin(timeYearAggregate)
                binding.appLaunchesYearAggregate.text=launchesYearAggregate.toString()
                timeYearChart.invalidate()
                launchesYearChart.invalidate()
            }


        }
        binding.thisWeekButton.setOnClickListener {
            binding.thisWeekButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            binding.thisMonthButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            binding.thisYearButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            if(noChartsToDisplay){
                binding.noChartsText.visibility=View.VISIBLE
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.GONE
            }
            else{
                binding.noChartsText.visibility=View.GONE
                binding.weekChartsConstraintLayout.visibility=View.VISIBLE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.GONE
            }
        }
        binding.thisMonthButton.setOnClickListener {
            binding.thisMonthButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            binding.thisWeekButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            binding.thisYearButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)

            if(noChartsToDisplay){
                binding.noChartsText.visibility=View.VISIBLE
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.GONE
            }
            else{
                binding.noChartsText.visibility=View.GONE
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.VISIBLE
                binding.yearChartsConstraintLayout.visibility=View.GONE
            }
        }
        binding.thisYearButton.setOnClickListener {
            binding.thisYearButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            binding.thisMonthButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            binding.thisWeekButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)

            if(noChartsToDisplay){
                binding.noChartsText.visibility=View.VISIBLE
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.GONE
            }
            else{
                binding.noChartsText.visibility=View.GONE
                binding.weekChartsConstraintLayout.visibility=View.GONE
                binding.monthChartsConstraintLayout.visibility=View.GONE
                binding.yearChartsConstraintLayout.visibility=View.VISIBLE
            }
        }
        return binding.root
    }
}