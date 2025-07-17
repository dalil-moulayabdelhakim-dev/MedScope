package com.dldroid.medscope.ui.fragment.home.patient

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DOCTOR
import com.dldroid.medscope.database.DOCTOR_ID
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERROR
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GET_DOCTOR_BY_ID_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SPECIALTY_ID
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.manager.time.Calendar
import com.dldroid.medscope.manager.time.Time
import com.dldroid.medscope.databinding.ActivityAppointmentBinding
import com.dldroid.medscope.manager.PermissionsManager
import com.dldroid.medscope.manager.appointment.AppointmentManager
import com.dldroid.medscope.ui.dialog.RequestPermissionDialog
import org.json.JSONException
import org.json.JSONObject

class CreateAppointmentActivity : AppCompatActivity() {
    private var _binding: ActivityAppointmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var doctorID: String
    private lateinit var sharedAuth: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setListeners()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.showDateChooserButton.setOnClickListener {
            Calendar(this, null).showCalendar(this, binding.dateTextView)
        }

        binding.showTimeChooserButton.setOnClickListener {
            Time(this, null).showTimePicker(this, binding.timeTextView)
        }

        binding.submitButton.setOnClickListener {
            submit()
        }
    }

    private fun init() {
        val bundle = intent.extras
        doctorID = bundle!!.getString(ID, "")
        sharedAuth = getSharedPreferences(AUTH, MODE_PRIVATE)

        binding.doctorName.setSelected(true)


        binding.progressBar.visibility = View.VISIBLE
        binding.container.visibility = View.GONE
    }

    override fun onResume() {
        loadInfos()
        super.onResume()
    }

    private fun loadInfos() {
        val r: StringRequest = object : StringRequest(
            Method.POST, GET_DOCTOR_BY_ID_URL,
            Response.Listener { response ->
                try {
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        j = j.getJSONObject(DOCTOR)
                        binding.doctorName.text = j.getString(FULL_NAME)
                        binding.doctorEmail.text = j.getString(EMAIL)
                        binding.doctorPhone.text = j.getString(PHONE)
                        binding.doctorSpecialty.text = j.getString(SPECIALTY_ID)
                        binding.progressBar.visibility = View.GONE
                        binding.container.visibility = View.VISIBLE
                    } else {
                        Log.d("errorD", j.getString(ERROR))
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("MY_ERROR_AT_VIEWDR_IN_ONRESPONSE", e.toString())
                }
            },
            Response.ErrorListener { error ->
                Log.e(
                    "MY_ERROR_AT_viewdr_IN_ONERRORRESPONSE",
                    error.toString()
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val p: MutableMap<String, String> = HashMap()
                val sh = getSharedPreferences(AUTH, MODE_PRIVATE)
                val accessToken = sh.getString(ACCESS_TOKEN, "")
                p[ACCESS_TOKEN] = accessToken.toString()
                p[DOCTOR_ID] = doctorID
                return p
            }
        }

        val q = Volley.newRequestQueue(applicationContext)
        q.add(r)
    }

    private fun submit() {
        val location = "clinic location"
        val dateT: String = binding.dateTextView.getText().toString()
        val timeT: String = binding.timeTextView.getText().toString()
        val resT: String = binding.reasonTextView.getText().toString()
        if (resT.isNotEmpty()) {
            if(PermissionsManager.sendSMSPermission(this)){
                AppointmentManager(
                    this,
                    doctorID,
                    dateT,
                    timeT,
                    resT,
                    location
                ).createAppointment(supportFragmentManager)
                /* Manager(this).sendSMS(
                 binding.doctorPhone.getText().toString(),
                 "Med Scope:\nMr/Ms $fullNameT you have a new appointment request from patient $patientName, please enter the app to check it."
             )*/
            }else{
                RequestPermissionDialog(Manifest.permission.SEND_SMS).show(supportFragmentManager, "request sms permission")
            }

        } else {
            Toast.makeText(applicationContext, "Some fields are missing", Toast.LENGTH_LONG).show()
        }
    }
}