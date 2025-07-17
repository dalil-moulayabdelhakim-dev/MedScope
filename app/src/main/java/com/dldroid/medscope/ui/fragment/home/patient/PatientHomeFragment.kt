package com.dldroid.medscope.ui.fragment.home.patient

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.ActivityHomeBinding
import com.dldroid.medscope.databinding.FragmentPatientHomeBinding
import com.dldroid.medscope.ui.HomeActivity

class PatientHomeFragment : Fragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.home_section, R.id.medical_record_section
        ), ActivityHomeBinding.inflate(layoutInflater).drawer)
        val navController = activity?.findNavController(R.id.fragment)
        if (navController != null) {
            setupActionBarWithNavController(activity as HomeActivity, navController, appBarConfiguration)
        }

        // Set the item active indicator color from resources
        val colorStateList = ResourcesCompat.getColorStateList(resources, R.color.blue_light2, null)
        binding.bottomNavigationView.itemActiveIndicatorColor = colorStateList
        if (navController != null) {
            binding.bottomNavigationView.setupWithNavController(navController)
        }
    }

}