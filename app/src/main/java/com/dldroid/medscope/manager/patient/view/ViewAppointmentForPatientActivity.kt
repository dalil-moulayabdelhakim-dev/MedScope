package com.dldroid.medscope.manager.patient.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.APPOINTMENT
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATE
import com.dldroid.medscope.database.GET_APPOINTMENT_BY_ID_FOR_PATIENT_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.LOCATION
import com.dldroid.medscope.database.REASON
import com.dldroid.medscope.database.RECEPTOR_EMAIL
import com.dldroid.medscope.database.RECEPTOR_NAME
import com.dldroid.medscope.database.RECEPTOR_PHONE
import com.dldroid.medscope.database.RECEPTOR_SPECIALTY
import com.dldroid.medscope.database.STATUS
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TIME
import com.dldroid.medscope.databinding.ActivityViewAppointmentForPatientBinding
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import org.json.JSONException
import org.json.JSONObject

class ViewAppointmentForPatientActivity : AppCompatActivity() {

    private lateinit var _binding : ActivityViewAppointmentForPatientBinding
    private val binding get() = _binding

    private lateinit var appointmentID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityViewAppointmentForPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setListeners()
        loadInfo()

    }


    private fun init() {
        binding.name.setSelected(true)

        val bundle = intent.extras
        if (bundle != null) {
            appointmentID = bundle.getString(ID).toString()
        }
    }


    override fun onResume() {
        loadInfo()
        super.onResume()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener{ onBackPressed() }
        binding.refreshLayout.setOnRefreshListener {
            loadInfo()
        }
    }


    private fun loadInfo() {
        binding.refreshLayout.isRefreshing = true
        val r: StringRequest = object : StringRequest(
            Method.POST, GET_APPOINTMENT_BY_ID_FOR_PATIENT_URL,
            Response.Listener { response ->
                try {
                    binding.refreshLayout.isRefreshing = false
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        val ja = j.getJSONArray(APPOINTMENT)
                        j = ja.getJSONObject(0)
                        binding.dateTextView.text = j.getString(DATE)
                        binding.timeTextView.text = j.getString(TIME)
                        binding.locationTextView.text = j.getString(LOCATION)
                        binding.reasonTextView.text = j.getString(REASON)

                        when (j.getInt(STATUS)) {
                            0 -> {
                                binding.statusTextView.text = getString(R.string.pending)
                                binding.statusIndicator.setBackgroundColor(getColor(R.color.orange))
                            } //pending
                            1 -> {
                                binding.statusTextView.text = getString(R.string.accepted)
                                binding.statusIndicator.setBackgroundColor(getColor(R.color.blue))
                            }  //accepted
                            2 -> {
                                binding.statusTextView.text = getString(R.string.completed)
                                binding.statusIndicator.setBackgroundColor(getColor(R.color.green))
                            } //completed
                            else -> {
                                binding.statusTextView.text = getString(R.string.canceled)
                                binding.statusIndicator.setBackgroundColor(getColor(R.color.red))
                            } //canceled
                        }

                        binding.name.text = j.getString(RECEPTOR_NAME)
                        binding.specialtyTextView.text = j.getString(RECEPTOR_SPECIALTY)
                        binding.phoneTextView.text = j.getString(RECEPTOR_PHONE)
                        binding.emailTextView.text = j.getString(RECEPTOR_EMAIL)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("MY_ERROR_AT_VIEWAPP_IN_ONRESPONSE", e.toString())
                }
            },
            Response.ErrorListener { error ->
                binding.refreshLayout.isRefreshing = false
                Log.e("MY_ERROR_AT_VIEWAPP_IN_ONERRORRESPONSE", error.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val p: MutableMap<String, String> = HashMap()
                val sh = getSharedPreferences(AUTH, MODE_PRIVATE)
                val accessToken = sh.getString(ACCESS_TOKEN, "")
                p[ACCESS_TOKEN] = accessToken!!
                p[ID] = appointmentID
                return p
            }
        }

        val q = Volley.newRequestQueue(applicationContext)
        q.add(r)
    }




}