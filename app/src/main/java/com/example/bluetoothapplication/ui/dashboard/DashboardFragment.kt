package com.example.bluetoothapplication.ui.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bluetoothapplication.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val notification: TextView = binding.notificationText
        val startscan: Button = binding.startscan
        val stopscan: Button = binding.stopscan

        startscan.setOnClickListener {
            Log.i("BluetoothScan", "Launch Background Scan in Wellness Clicked")
            val startBackgroundScan = Intent(requireContext(), SmartWellnessBackgroundScan::class.java)
            startBackgroundScan.putExtra("service_uuid", SERVICE_UUID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(startBackgroundScan)
            }else{
                requireContext().startService(startBackgroundScan)
            }
        }

        dashboardViewModel.text.observe(viewLifecycleOwner) {
            notification.text = it
        }
        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}