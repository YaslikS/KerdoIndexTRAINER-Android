package com.amed.kerdoindextrainer.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.adapters.SportsmanAdapter
import com.amed.kerdoindextrainer.databinding.FragmentDetailsBinding
import com.amed.kerdoindextrainer.databinding.FragmentProfileBinding
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseCloudManager
import com.amed.kerdoindextrainer.fireBaseManagers.hasConnection
import com.amed.kerdoindextrainer.model.Measure
import com.amed.kerdoindextrainer.model.MeasureJsonManager
import com.amed.kerdoindextrainer.model.SharedPreferencesManager
import com.amed.kerdoindextrainer.model.User
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.util.ArrayList

class DetailsFragment : Fragment(), OnChartValueSelectedListener {

    private var binding: FragmentDetailsBinding? = null
    private val TAG = "kiTRAINER.DetailsFragment"
    var idSportsman = String()
    var nameSportsman: String? = null
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var sportsman: User? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null
    private var checkingReachability = CoroutineScope(Dispatchers.IO)
    private var measureJsonManager: MeasureJsonManager? = null
    private var measures1 = mutableListOf<Measure>()
    private var measures2 = mutableListOf<Measure>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentDetailsBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        measureJsonManager = MeasureJsonManager()
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())
        settingsViews()

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    private fun displayData(sportsman: User) {
        displayLastDate(sportsman.lastDate.toString())
        displayMeasure(sportsman.json.toString())
        displayEmail(sportsman?.email.toString())
    }

    private fun displayEmail(email: String) {
        Log.i(TAG, "displayEmail: entrance")
        if (email.isNotEmpty()) {
            binding?.sportsmanEmailTV?.visibility = TextView.VISIBLE
            binding?.sportsmanEmailTV?.text = "Sportsman's email address: $email"
        } else {
            //binding?.sportsmanEmailTV?.visibility = TextView.INVISIBLE
        }
    }

    private fun displayMeasure(json: String) {
        Log.i(TAG, "displayMeasure: entrance")
        if (json.isNotEmpty()) {
            Log.i(TAG, "displayMeasure: jsonIsNotEmpty")
            binding?.graphKedro?.visibility = BarChart.VISIBLE
            binding?.graphKedro?.layoutParams?.height = 700
            binding?.graphDAD?.visibility = BarChart.VISIBLE
            binding?.graphDAD?.layoutParams?.height = 700
            binding?.graphPulse?.visibility = BarChart.VISIBLE
            binding?.graphPulse?.layoutParams?.height = 700
            binding?.lastUpdateTV?.visibility = TextView.VISIBLE
            binding?.jsonIsEmptyCL?.visibility = ConstraintLayout.INVISIBLE
            var js = measureJsonManager?.parcingJson(json)
            measures1 = js?.measures1!!
            measures2 = js?.measures2!!
            createKedroChart()  // график кедро
            createPulseChart()  // график пульса
            createDadChart()    // график ДАД
        } else {
            Log.i(TAG, "displayMeasure: jsonIsEmpty")
            binding?.lastUpdateTV?.visibility = TextView.INVISIBLE
            binding?.graphKedro?.visibility = BarChart.INVISIBLE
            binding?.graphKedro?.layoutParams?.height = 100
            binding?.graphDAD?.visibility = BarChart.INVISIBLE
            binding?.graphDAD?.layoutParams?.height = 1
            binding?.graphPulse?.visibility = BarChart.INVISIBLE
            binding?.graphPulse?.layoutParams?.height = 1
            binding?.jsonIsEmptyCL?.visibility = ConstraintLayout.VISIBLE
        }
    }

    // заполнение графика кедро
    private fun createKedroChart() {
        Log.i(TAG, "createKedroChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.KerdoIndex?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.KerdoIndex?.toFloat()!!))
        }
        //  заполнение массива с цветами
        val colors = ArrayList<Int>()
        for (index in values) {
            if (index.y < -15.0) {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.greenGraph))
                Log.i(TAG, "createKedroChart: R.color.greenGraph")
            } else if (15.0 < index.y) {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.redGraph))
                Log.i(TAG, "createKedroChart: R.color.redGraph")
            } else {
                colors.add(ContextCompat.getColor(requireActivity(), R.color.yellowGraph))
                Log.i(TAG, "createKedroChart: R.color.yellowGraph")
            }
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        set.colors = colors
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f
        binding?.graphKedro?.isDragYEnabled = false
        binding?.graphKedro?.legend?.isEnabled = false
        binding?.graphKedro?.description?.isEnabled = false
        binding?.graphKedro?.isDoubleTapToZoomEnabled = false
        binding?.graphKedro?.data = data
        binding?.graphKedro?.setTouchEnabled(true)
        binding?.graphKedro?.setOnChartValueSelectedListener(this)
        binding?.graphKedro?.xAxis?.isGranularityEnabled = true
        binding?.graphKedro?.xAxis?.granularity = 1f
        binding?.graphKedro?.invalidate()
        binding?.graphKedro?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphKedro?.moveViewToX(measures1.size.toFloat())
            binding?.graphKedro?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createKedroChart: exit")
    }

    // заполнение графика пульса
    private fun createPulseChart() {
        Log.i(TAG, "createPulseChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.Pulse?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.Pulse?.toFloat()!!))
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f

        binding?.graphPulse?.isDragYEnabled = false
        binding?.graphPulse?.legend?.isEnabled = false
        binding?.graphPulse?.description?.isEnabled = false
        binding?.graphPulse?.isDoubleTapToZoomEnabled = false
        binding?.graphPulse?.data = data
        binding?.graphPulse?.setTouchEnabled(false)
        binding?.graphPulse?.xAxis?.isGranularityEnabled = true
        binding?.graphPulse?.xAxis?.granularity = 1f
        binding?.graphPulse?.invalidate()
        binding?.graphPulse?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphPulse?.moveViewToX(measures1.size.toFloat())
            binding?.graphPulse?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createPulseChart: exit")
    }

    // заполнение графика дад
    private fun createDadChart() {
        Log.i(TAG, "createDadChart: entrance")
        val values = ArrayList<BarEntry>()
        //  заполнение массива с данными для графика
        for (index in measures1.indices) {
            val entry = measures1[index]
            val entry2 = measures2[index]
            values.add(BarEntry((index + 1).toFloat(), entry.DAD?.toFloat()!!))
            values.add(BarEntry((index + 1).toFloat(), entry2.DAD?.toFloat()!!))
        }
        //  настройка данных графика
        val set = BarDataSet(values, "Values")
        //  настройка отображения графика
        val data = BarData(set)
        data.setValueTextSize(12f)
        data.barWidth = 0.8f
        binding?.graphDAD?.isDragYEnabled = false
        binding?.graphDAD?.legend?.isEnabled = false
        binding?.graphDAD?.description?.isEnabled = false
        binding?.graphDAD?.isDoubleTapToZoomEnabled = false
        binding?.graphDAD?.data = data
        binding?.graphDAD?.setTouchEnabled(false)
        binding?.graphDAD?.xAxis?.isGranularityEnabled = true
        binding?.graphDAD?.xAxis?.granularity = 1f
        binding?.graphDAD?.invalidate()
        binding?.graphDAD?.barData?.barWidth = 0.5F
        if (measures1.isNotEmpty()) {
            binding?.graphDAD?.moveViewToX(measures1.size.toFloat())
            binding?.graphDAD?.setVisibleXRangeMaximum(12F)
        }
        Log.i(TAG, "createDadChart: exit")
    }

    private fun displayLastDate(lastDate: String) {
        if (lastDate.isNotEmpty()) {
            Log.i(TAG, "displayLastDate: lastDate.isNotEmpty")
            binding?.lastUpdateTV?.text = "Last Update:$lastDate"
        } else {
            Log.i(TAG, "displayLastDate: lastDate.isEmpty")
            binding?.lastUpdateTV?.text = "Sportsman not uploaded measurements yet"
        }
    }

    // настройка view
    private fun settingsViews() {
        Log.i(TAG, "settingsViews: entrance")

        clickListeners()
        binding?.topTV?.text = nameSportsman

        CoroutineScope(Dispatchers.IO).launch {
            if (idSportsman != null && idSportsman != "") {
                Log.i(TAG, "settingsViews: idSportsman != null && idSportsman != \"\"")
                binding?.progressCL?.visibility = ConstraintLayout.VISIBLE
                fireBaseCloudManager?.getSportsmanData(idSportsman, ::resultGettingSportsman)
                    ?.collect { gettedSportsman ->
                        sportsman = gettedSportsman
                        Log.i(TAG, "settingsViews: CoroutineScope: $sportsman")
                        launch(Dispatchers.Main) {
                            displayData(sportsman!!)
                            binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                        }
                    }
            }
        }

        Log.i(TAG, "settingsViews: exit")
    }

    private fun resultGettingSportsman(state: Int) {
        when (state) {
            0 -> {  //  неудачное получение
                Log.i(TAG, "resultGettingSportsman: state = $state")

            }
            1 -> {  //  удачное получение
                Log.i(TAG, "resultGettingSportsman: state = $state")

            }
        }
    }

    private fun clickListeners() {
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "clickListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
        binding?.closeInfoMeasuringButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: closeInfoMeasuringButton: entrance")
            binding?.dateMeasuringCardView?.visibility = CardView.INVISIBLE
        }
        binding?.deleteSportsmanButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: deleteSportsmanButton: entrance")
            AlertDialog.Builder(requireActivity())
                .setTitle("Delete sportsman?")
                .setMessage("Are you sure you want to remove the sportsman?")
                .setNegativeButton("Delete") { _, _ ->
                    Log.i(TAG, "clickListeners: deleteSportsmanButton: Delete: entrance")
                    fireBaseCloudManager?.deleteSportsman(idSportsman, ::resultDeleteSportsman)
                }.setPositiveButton("Cancel", null).show()
        }
    }

    private fun resultDeleteSportsman(state: Int) {
        Log.i(TAG, "resultDeleteSportsman: entrance")
        when (state) {
            0 -> {  //  неудачное удаление
                Log.i(TAG, "resultDeleteSportsman: state = $state")
                AlertDialog.Builder(requireActivity())
                    .setTitle("Failed to delete sportsman")
                    .setMessage("Check your internet connection")
                    .setPositiveButton("OK", null).show()
            }
            1 -> {  //  удачное удаление
                Log.i(TAG, "resultDeleteSportsman: state = $state")
                binding?.deleteSuccessCardView?.visibility = CardView.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding?.deleteSuccessCardView?.visibility = CardView.INVISIBLE
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }
    }

    // состояние интернета
    // наблюдение за ним
    private fun startCheckingReachability() {
        Log.i(TAG, "startCheckingReachability: entrance")
        checkingReachability = CoroutineScope(Dispatchers.IO)
        checkingReachability?.launch(Dispatchers.IO) {
            while (true) {
                when (hasConnection(requireActivity())) {
                    true -> {
                        //Log.i(TAG, "startCheckingReachability: true")
                        launch(Dispatchers.Main) {
                            binding?.offlineModeButton?.visibility = Button.INVISIBLE
                        }
                    }
                    false -> {
                        //Log.i(TAG, "startCheckingReachability: false")
                        launch(Dispatchers.Main) {
                            binding?.offlineModeButton?.visibility = Button.VISIBLE
                        }
                    }
                }
                delay(500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCheckingReachability()
    }

    override fun onPause() {
        super.onPause()
        binding?.nestedScrollView?.scrollTo(0, 0)
        checkingReachability.cancel()
    }

    // нажатие на столбец - отображение окна с инфо столбца
    private fun visibleInfoMeasuring(X: Int) {
        Log.i(TAG, "visibleInfoMeasuring: entrance")
        //  получение данных, которые будут выведен
        val Y1 = measures1?.get(X - 1)?.KerdoIndex?.toFloat()
        val Y2 = measures2?.get(X - 1)?.KerdoIndex?.toFloat()
        binding?.dateMeasuringTV?.text = measures1[X - 1].date
        binding?.indexMeasuringTV?.text = String.format("%.2f", Y1)
        binding?.indexMeasuringTV2?.text = String.format("%.2f", Y2)
        // лог
        Log.i(TAG, "visibleInfoMeasuring: kerdoIndex1: $Y1")
        Log.i(TAG, "visibleInfoMeasuring: kerdoIndex2: $Y2")
        Log.i(TAG, "visibleInfoMeasuring: date: " + measures1[X - 1]?.date)

        if (-15.0 <= Y1!! && Y1 <= 15.0) {
            binding?.cardView?.setCardBackgroundColor(
                ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
            )
            Log.i(TAG, "visibleInfoMeasuring: R.color.yellowGraph for Y1")
        } else
            if (Y1 < -15.0) {
                binding?.cardView?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.greenGraph for Y1")
            } else {
                binding?.cardView?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.redGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.redGraph for Y1")
            }

        if (-15.0 <= Y2!! && Y2 <= 15.0) {
            binding?.cardView2?.setCardBackgroundColor(
                ContextCompat.getColor(requireActivity(), R.color.yellowGraph)
            )
            Log.i(TAG, "visibleInfoMeasuring: R.color.yellowGraph for Y2")
        } else
            if (Y2 < -15.0) {
                binding?.cardView2?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.greenGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.greenGraph for Y2")
            } else {
                binding?.cardView2?.setCardBackgroundColor(
                    ContextCompat.getColor(requireActivity(), R.color.redGraph)
                )
                Log.i(TAG, "visibleInfoMeasuring: R.color.redGraph for Y2")
            }

        binding?.dateMeasuringCardView?.visibility = CardView.VISIBLE
        Log.i(TAG, "visibleInfoMeasuring: exit")
    }

    // нажатие на столбец
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i(TAG, "onValueSelected: column: " + e!!.x)
        // отображение панели добавления измерения
        visibleInfoMeasuring(e.x.toInt())
    }

    override fun onNothingSelected() {
        Log.i(TAG, "onNothingSelected ")
    }
}