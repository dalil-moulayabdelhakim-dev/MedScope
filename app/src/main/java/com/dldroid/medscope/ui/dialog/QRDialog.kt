package com.dldroid.medscope.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATE_OF_BIRTH
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERROR
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GENDER
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.USER_ID_CARD_NUMBER_URL
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.DialogQrBinding
import com.dldroid.medscope.manager.AuthManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject

class QRDialog : DialogFragment() {
    private var _binding: DialogQrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.dialog)
        _binding = DialogQrBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = builder.setView(binding.root)
            .setTitle(getString(R.string.scan_the_code))
            .setIcon(R.drawable.ic_scan_code)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                dismiss()
            }.create()

        dialog.setOnShowListener {
            val button = (it as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        }

        init()
        loadData()
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun init() {
        binding.progressBarLayout.visibility = View.VISIBLE
        binding.imageLayout.visibility = View.GONE

    }

    private fun loadData() {
        val request = object : StringRequest(
            Method.POST, USER_ID_CARD_NUMBER_URL,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)
                if (success) {
                    val id = jsonObject.getString(ID_CARD_NUMBER)
                    AuthManager.generateCode(id, binding.qrCode)
                    binding.qrText.text = id
                    binding.progressBarLayout.visibility = View.GONE
                    binding.imageLayout.visibility = View.VISIBLE
                } else {
                    val title = jsonObject.getString(TITLE)
                    val message = jsonObject.getString(MESSAGE)
                    val builder = AlertDialog.Builder(
                        context, R.style.dialog
                    )
                    val dialog = builder.setTitle(title)
                        .setIcon(R.drawable.ic_logout)
                        .setMessage(message)
                        .setPositiveButton(
                            getString(R.string.ok)
                        ) { _, _ ->
                            AuthManager.logout(requireContext(), requireActivity().supportFragmentManager)
                        }
                        .create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(resources.getColor(R.color.blue))
                    }
                    dialog.show()
                    AuthManager.logout(
                        requireContext(),
                        requireActivity().supportFragmentManager
                    )
                    binding.progressBarLayout.visibility = View.GONE
                }
            }, Response.ErrorListener { e ->
                Toast.makeText(
                    context,
                    getString(R.string.something_went_wrong_please_try_again),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("ProfileFragment.loadProfile.error", e.toString())
                binding.progressBarLayout.visibility = View.GONE
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences =
                    requireContext().getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                val accessToken = sharedPreferences.getString(ACCESS_TOKEN, "")
                params[ACCESS_TOKEN] = accessToken!!
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)

    }
}