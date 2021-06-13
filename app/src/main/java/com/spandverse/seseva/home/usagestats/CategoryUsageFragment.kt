package com.spandverse.seseva.home.usagestats

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.spandverse.seseva.*
import com.spandverse.seseva.adapters.PieChartLegendAdapter
import com.spandverse.seseva.adapters.StatAdapter
import com.spandverse.seseva.data.AllDatabase
import com.spandverse.seseva.databinding.FragmentCategoryUsageBinding
import com.spandverse.seseva.foregroundnnotifications.InfoPopUpWindow
import com.spandverse.seseva.home.DrawerLocker
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.*


class CategoryUsageFragment : Fragment() {
    private lateinit var binding:FragmentCategoryUsageBinding
    private val viewModel:UsageOverViewViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category_usage, container, false)

        var noChartsToDisplay=false

        val application = requireNotNull(this.activity).application
        val catDatabaseDao = AllDatabase.getInstance(application).CategoryStatDatabaseDao
        val catName: String = CategoryUsageFragmentArgs.fromBundle(requireArguments()).catName
        binding.usageOverViewViewModel= viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val adapter= StatAdapter(StatAdapter.OnClickListener{
            viewModel.toAppUsageScreen(it)
            })
        viewModel.navigateToSelectedApp.observe(viewLifecycleOwner, Observer {navigate->
            if(navigate) {
                findNavController().navigate(CategoryUsageFragmentDirections.actionCategoryUsageFragmentToAppUsageFragment(viewModel.appPackageNameForAppScreen.value!!))
                viewModel.navigateToSelectedAppComplete()
            }
        })
        viewModel.appsInCatList.observe(viewLifecycleOwner, Observer {it->
            if(it.isEmpty()){
                binding.comparativeAnalysisLayout.visibility=View.GONE
                binding.tapToKnowText.visibility=View.GONE
            }
            else{
                binding.comparativeAnalysisLayout.visibility=View.VISIBLE
                binding.tapToKnowText.visibility=View.VISIBLE
            }

        })
        viewModel.screenHeading.observe(viewLifecycleOwner, Observer {heading->
            if(heading=="Today's statistics") {
                binding.i.visibility=View.GONE
            }
            else{
                binding.i.visibility=View.VISIBLE
            }
        })
        viewModel.headsUpDisappear.observe(viewLifecycleOwner, Observer {disappear->
            if(disappear) {
                binding.headsUpCL.visibility=View.GONE
                binding.whitelistedDescription.visibility=View.VISIBLE
            }
            else{
                binding.headsUpCL.visibility=View.VISIBLE
                binding.whitelistedDescription.visibility=View.GONE
            }
        })
        viewModel.countdownColor.observe(viewLifecycleOwner, Observer {status->
            when (status) {
                CategoryRuleStatus.BROKEN -> {
                    binding.headsUpText.text="Oye!!"
                    context?.let {
                        binding.headsUpText.setTextColor(ContextCompat.getColor(it, R.color.colorError))
                        binding.timeCountdown.setTextColor(ContextCompat.getColor(it, R.color.colorError))
                        binding.launchesCountdown.setTextColor(ContextCompat.getColor(it, R.color.colorError))}
                    binding.timeCountdown.setBackgroundResource(R.drawable.ic_countdown_red)
                    binding.launchesCountdown.setBackgroundResource(R.drawable.ic_counts_countdown_red)

                }
                CategoryRuleStatus.WARNING -> {
                    binding.headsUpText.text="Heads Up!"
                    context?.let {
                        binding.headsUpText.setTextColor(ContextCompat.getColor(it, R.color.yellow))
                        binding.timeCountdown.setTextColor(ContextCompat.getColor(it, R.color.yellow))
                        binding.launchesCountdown.setTextColor(ContextCompat.getColor(it, R.color.yellow))}
                    binding.timeCountdown.setBackgroundResource(R.drawable.ic_countdown_yellow)
                    binding.launchesCountdown.setBackgroundResource(R.drawable.ic_counts_countdown_yellow)
                }
                else -> {
                    binding.headsUpText.text="Hey!"
                    context?.let {
                        binding.headsUpText.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary))
                        binding.timeCountdown.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary))
                        binding.launchesCountdown.setTextColor(ContextCompat.getColor(it, R.color.colorPrimary)) }
                    binding.timeCountdown.setBackgroundResource(R.drawable.ic_countdown_green)
                    binding.launchesCountdown.setBackgroundResource(R.drawable.ic_counts_countdown_green)
                }
            }
        })
        val manager2 = GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false)
        val manager1 = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)
        viewModel.appsInCatList.observe(viewLifecycleOwner, Observer {
            if(it.size<7)
                binding.appRecyclerView.layoutManager=manager1
            else
                binding.appRecyclerView.layoutManager=manager2
            it?.let { adapter.submitList(it) }
        })
        binding.appRecyclerView.adapter=adapter
        binding.i.setOnClickListener {
            val popUpClass = InfoPopUpWindow()
            it?.let {
                popUpClass.showPopupWindow(it) }
        }
        val drawerLocker=(activity as DrawerLocker?)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_left)
        binding.toolbar.setNavigationOnClickListener {
                //v -> (activity as DrawerLocker?)!!.openCloseNavigationDrawer(v)
            activity?.onBackPressed()}
        drawerLocker!!.setDrawerEnabled(true)
        drawerLocker.displayBottomNavigation(true)

        viewModel.catTimeSpentPieChartVisible.observe(viewLifecycleOwner,Observer{visible->
            if(visible){
                binding.timePieChart.visibility=View.VISIBLE
                binding.timePieChartLegend.visibility=View.VISIBLE
                binding.timeSpentDropdownIcon.setImageResource(R.drawable.ic_collapse_vector)
            }
            else{
                binding.timePieChart.visibility=View.GONE
                binding.timePieChartLegend.visibility=View.GONE
                binding.timeSpentDropdownIcon.setImageResource(R.drawable.ic_expand_vector)
            }

        })
        viewModel.catAppLaunchesPieChartVisible.observe(viewLifecycleOwner,Observer{visible->
            if(visible){
                binding.appLaunchesPieChart.visibility=View.VISIBLE
                binding.launchesPieChartLegend.visibility=View.VISIBLE
                binding.appLaunchesDropdownIcon.setImageResource(R.drawable.ic_collapse_vector)
            }
            else{
                binding.appLaunchesPieChart.visibility=View.GONE
                binding.launchesPieChartLegend.visibility=View.GONE
                binding.appLaunchesDropdownIcon.setImageResource(R.drawable.ic_expand_vector)
            }
        })

        val timePieChart=binding.timePieChart
        val timeLegend=timePieChart.legend
        /*timeLegend.form= Legend.LegendForm.CIRCLE
        timeLegend.textColor=R.color.disabled_text
        timeLegend.textSize=12f
        timeLegend.formSize=12f
        timeLegend.formToTextSpace=4f
        timeLegend.orientation=Legend.LegendOrientation.VERTICAL
        timeLegend.verticalAlignment=Legend.LegendVerticalAlignment.BOTTOM
        timeLegend.horizontalAlignment=Legend.LegendHorizontalAlignment.CENTER*/
        timeLegend.isEnabled=false
        timePieChart.description.isEnabled=false
        timePieChart.setDrawEntryLabels(false)
        timePieChart.transparentCircleRadius=0f
        val launchesPieChart=binding.appLaunchesPieChart
        val launchesLegend=launchesPieChart.legend
        /*launchesLegend.form=Legend.LegendForm.CIRCLE
        launchesLegend.textColor=R.color.disabled_text
        launchesLegend.textSize=12f
        launchesLegend.formSize=12f
        launchesLegend.formToTextSpace=4f
        launchesLegend.orientation=Legend.LegendOrientation.VERTICAL*/
        launchesLegend.isEnabled=false
        launchesPieChart.description.isEnabled=false
        launchesPieChart.setDrawEntryLabels(false)
        launchesPieChart.transparentCircleRadius=0f
        context?.let {
            timePieChart.setHoleColor(ContextCompat.getColor(it,R.color.rules_card))
            launchesPieChart.setHoleColor(ContextCompat.getColor(it,R.color.rules_card)) }
        viewModel.processingDataForPieChartDone.observe(viewLifecycleOwner, Observer { done ->
            if (done) {
                val catTimePieChartEntries: ArrayList<PieEntry> = ArrayList()
                val catLaunchesPieChartEntries: ArrayList<PieEntry> = ArrayList()
                val stats = viewModel.appsInCatList.value
                if (stats != null && stats.isNotEmpty()) {
                    if (stats.size > 6) {
                        for (i in 0..4) {
                            stats[i].timeSpent?.let {
                                catTimePieChartEntries.add(
                                    PieEntry(
                                        it.toFloat(),
                                        stats[i].appName
                                    )
                                )
                            }
                            stats[i].appLaunches?.let {
                                catLaunchesPieChartEntries.add(
                                    PieEntry(
                                        it.toFloat(),
                                        stats[i].appName
                                    )
                                )
                            }
                        }
                        var othersTime = 0
                        var othersLaunches = 0
                        for (i in 5..stats.lastIndex) {
                            othersTime += stats[i].timeSpent ?: 0
                            othersLaunches += stats[i].appLaunches ?: 0
                        }
                        if (othersTime != 0)
                            catTimePieChartEntries.add(PieEntry(othersTime.toFloat(), "Others"))
                        if (othersLaunches != 0)
                            catLaunchesPieChartEntries.add(
                                PieEntry(
                                    othersLaunches.toFloat(),
                                    "Others"
                                )
                            )
                    } else {
                        for (stat in stats) {
                            stat.timeSpent?.let {
                                catTimePieChartEntries.add(
                                    PieEntry(
                                        it.toFloat(),
                                        stat.appName
                                    )
                                )
                            }
                            stat.appLaunches?.let {
                                catLaunchesPieChartEntries.add(
                                    PieEntry(
                                        it.toFloat(),
                                        stat.appName
                                    )
                                )
                            }
                        }
                    }
                    val colorsList: MutableList<Int> = mutableListOf()
                    for (c in ColorTemplate.MATERIAL_COLORS) {
                        colorsList.add(c)
                    }
                    for (c in ColorTemplate.COLORFUL_COLORS) {
                        colorsList.add(c)
                    }
                    val catTimePieDataSet = PieDataSet(catTimePieChartEntries, "")
                    catTimePieDataSet.colors = colorsList
                    val catTimePieData = PieData(catTimePieDataSet)
                    timePieChart.data = catTimePieData
                    val timeLegendManager =
                        GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                    val timeEntries = timeLegend.entries
                    val timeLegendList: MutableList<PieChartLegendItem> = mutableListOf()
                    for (i in timeEntries.indices) {
                        timeLegendList.add(
                            PieChartLegendItem(
                                timeEntries[i].formColor,
                                timeEntries[i].label
                            )
                        )
                    }
                    val timePieChartAdapter = PieChartLegendAdapter()
                    timePieChartAdapter.submitList(timeLegendList)
                    binding.timePieChartLegend.adapter = timePieChartAdapter
                    binding.timePieChartLegend.layoutManager = timeLegendManager
                    timePieChart.invalidate()

                    val catLaunchesPieDataSet = PieDataSet(catLaunchesPieChartEntries, "")
                    catLaunchesPieDataSet.colors = colorsList
                    val catLaunchesPieData = PieData(catLaunchesPieDataSet)
                    launchesPieChart.data = catLaunchesPieData
                    val launchesLegendManager =
                        GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
                    val launchesEntries = launchesLegend.entries
                    val launchesLegendList: MutableList<PieChartLegendItem> = mutableListOf()
                    for (i in launchesEntries.indices) {
                        launchesLegendList.add(
                            PieChartLegendItem(
                                launchesEntries[i].formColor,
                                launchesEntries[i].label
                            )
                        )
                    }
                    val launchesPieChartAdapter = PieChartLegendAdapter()
                    launchesPieChartAdapter.submitList(launchesLegendList)
                    binding.launchesPieChartLegend.adapter = launchesPieChartAdapter
                    binding.launchesPieChartLegend.layoutManager = launchesLegendManager
                    launchesPieChart.invalidate()
                }
            }
        })

        for(key in viewModel.categoryTimes.keys){
            if(key!="TOTAL"){

                Log.i("UOF3","$key")
            }
        }
        /*val totalTimePieDataSet= PieDataSet(catTimePieChartEntries, "")
        //totalTimePieDataSet.setColors(ColorTemplate.MATERIAL_COLORS)
        val totalTimePieTimeData= PieData(totalTimePieDataSet)*/

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
        timeWeekChart.axisLeft.axisMinimum=0f
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
        launchesWeekChart.axisLeft.axisMinimum=0f
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
        timeMonthChart.axisLeft.axisMinimum=0f
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
        launchesMonthChart.axisLeft.axisMinimum=0f
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
        timeYearChart.axisLeft.axisMinimum=0f
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
        launchesYearChart.axisLeft.axisMinimum=0f
        launchesYearChart.invalidate()
        viewLifecycleOwner.lifecycleScope.launch {
            val lastWeekStart: Calendar = Calendar.getInstance()
            lastWeekStart.add(Calendar.DATE, -8)
            val pastWeekData=catDatabaseDao.getTimeLaunchesDate(
                cat = catName,
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
                binding.timeSpentWeekAggregate.text=secToHrMin(timeWeekAggregate)
                binding.appLaunchesWeekAggregate.text=launchesWeekAggregate.toString()

                timeWeekChart.invalidate()
                launchesWeekChart.invalidate()

                val lastMonthStart: Calendar = Calendar.getInstance()
                lastMonthStart.add(Calendar.DATE, -30)
                val pastMonthData=catDatabaseDao.getTimeLaunchesDate(
                    cat = "TOTAL",
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
                val pastYearData=catDatabaseDao.getTimeLaunchesDate(
                    cat = catName,
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
        val weekButton=binding.thisWeekButton
        val monthButton=binding.thisMonthButton
        val yearButton=binding.thisYearButton
        weekButton.setOnClickListener {
            weekButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            monthButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            yearButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            context?.let{
                weekButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))
                monthButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))
                yearButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text)) }

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
        monthButton.setOnClickListener {
            monthButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            weekButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            yearButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            context?.let{
                weekButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))
                monthButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))
                yearButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text)) }

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
        yearButton.setOnClickListener {
            yearButton.setBackgroundResource(R.drawable.primary_60p_rounded_corner_4)
            monthButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            weekButton.setBackgroundResource(R.drawable.all_corner_rounded_4dp_2e)
            context?.let{
                weekButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))
                monthButton.setTextColor(ContextCompat.getColor(it,R.color.secondary_text))
                yearButton.setTextColor(ContextCompat.getColor(it,R.color.primary_text))}
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
    override fun onResume() {
        super.onResume()
        viewModel.stopHandler()
        viewModel.runHandler()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopHandler()
    }
}