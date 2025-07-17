package com.dldroid.medscope.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.DialogRequestPermissionBinding
import com.dldroid.medscope.manager.PermissionsManager

class RequestPermissionDialog(private val permission : String): DialogFragment(){

    private var _binding: DialogRequestPermissionBinding? = null
    private val binding get() = _binding!!


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogRequestPermissionBinding.inflate(layoutInflater)

        // Create an alert dialog builder object
        val builder = AlertDialog.Builder(activity, R.style.dialog)
        // Inflate the dialog layout from the XML file
        val view: View = binding.root
        // Set the dialog view to the inflated layout
        builder.setView(view)
        setListeners()


        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // Check if Bluetooth connect permission is granted
        if (PermissionsManager.checkPermission(context, permission)) {
            // If granted, dismiss the dialog
            dismiss()
        }
    }

    private fun setListeners() {
        // Set click listener for "Later" button
        binding.laterButton.setOnClickListener { // Dismiss the dialog
            dismiss()
        }

        // Set click listener for "Grant Permissions" button
        binding.grantPermissionsButton.setOnClickListener(View.OnClickListener { // Redirect to app details settings to grant necessary permissions
            redirectToSettings()
        })
    }

    private fun redirectToSettings() {
        val i = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context?.packageName)
        )
        // Start activity and catch potential exception
        try {
            startActivity(i)
            activity?.finish()
        } catch (e: ActivityNotFoundException) {
            // Log the exception if the activity is not found
            Log.e("redirectToSettings", e.toString())
        }
    }
}