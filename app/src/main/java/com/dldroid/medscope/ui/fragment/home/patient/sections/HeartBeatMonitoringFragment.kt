package com.dldroid.medscope.ui.fragment.home.patient.sections

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dldroid.medscope.databinding.FragmentHeartBeatMonitoringBinding
import com.dldroid.medscope.manager.BluetoothManager
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class HeartBeatMonitoringFragment : Fragment() {

    private lateinit var _binding: FragmentHeartBeatMonitoringBinding
    private val binding get() = _binding

    private lateinit var series: LineGraphSeries<DataPoint>
    private lateinit var bluetoothManager : BluetoothManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHeartBeatMonitoringBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupGraph()
    }

    override fun onPause() {
        super.onPause()
        bluetoothManager.stopChecking()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.stopReading()
    }

    @SuppressLint("NewApi")
    private fun init() {
        series = LineGraphSeries()
        binding.ecgGraphView.addSeries(series)
        bluetoothManager = BluetoothManager(requireContext(), requireActivity(), binding, series)
        bluetoothManager.startChecking()


    }

    private fun setupGraph() {
        binding.ecgGraphView.viewport.isXAxisBoundsManual = true
        binding.ecgGraphView.viewport.setMinX(0.0)
        binding.ecgGraphView.viewport.setMaxX(187.0)

        // ✅ تمكين التمرير التلقائي
        binding.ecgGraphView.viewport.isScrollable = true
        binding.ecgGraphView.viewport.isScalable = true
    }

}