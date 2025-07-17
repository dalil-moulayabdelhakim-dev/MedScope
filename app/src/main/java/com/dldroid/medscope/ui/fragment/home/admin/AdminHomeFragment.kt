package com.dldroid.medscope.ui.fragment.home.admin

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
import com.dldroid.medscope.databinding.FragmentHomeAdminBinding
import com.dldroid.medscope.ui.HomeActivity
import com.dldroid.medscope.ui.fragment.home.admin.dialog.AddUserDialog

class AdminHomeFragment : Fragment() {
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.adminSection, R.id.doctorSection, R.id.patientSection,
            R.id.laboratorianSection, R.id.receptionistSection
        ), ActivityHomeBinding.inflate(layoutInflater).drawer)
        val navController = activity?.findNavController(R.id.fragment)
        if (navController != null) {
            setupActionBarWithNavController(activity as HomeActivity, navController, appBarConfiguration)
        }
        binding.addUserButton.setOnClickListener {
            val dialog = AddUserDialog()
            dialog.show(childFragmentManager, "AddUserDialog")
        }

        // Set the item active indicator color from resources
        val colorStateList = ResourcesCompat.getColorStateList(resources, R.color.blue_light2, null)
        binding.bottomNavigationView.itemActiveIndicatorColor = colorStateList
        if (navController != null) {
            binding.bottomNavigationView.setupWithNavController(navController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}