package com.dldroid.medscope.ui.fragment.home.doctor.sections

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.APPOINTMENTS
import com.dldroid.medscope.database.APPOINTMENT_NUMBER
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATE
import com.dldroid.medscope.database.DOCTOR_DATA_URL
import com.dldroid.medscope.database.DOCTOR_NAME
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.PARIENT_NUMBER
import com.dldroid.medscope.database.PATIENT_NAME
import com.dldroid.medscope.database.STATUS
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TIME
import com.dldroid.medscope.database.WITH
import com.dldroid.medscope.databinding.FragmentDoctorHomeSectionBinding
import com.dldroid.medscope.manager.RecyclerInterface
import com.dldroid.medscope.manager.patient.AppointmentAdapter
import com.dldroid.medscope.manager.patient.AppointmentModel
import com.dldroid.medscope.manager.patient.view.ViewAppointmentForPatientActivity
import org.json.JSONException
import org.json.JSONObject


class DoctorHomeSectionFragment : Fragment() {

    private lateinit var _binding: FragmentDoctorHomeSectionBinding
    private val binding get() = _binding!!


    private lateinit var names: ArrayList<String>
    private lateinit var dateTime: java.util.ArrayList<String>
    private lateinit var ids: java.util.ArrayList<String>
    private lateinit var statuses: java.util.ArrayList<Int>


    private lateinit var appointmentModelArrayList: ArrayList<AppointmentModel>
    private lateinit var appointmentAdapter: AppointmentAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDoctorHomeSectionBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
        loadData()
    }

    private fun init() {
        names = ArrayList()
        dateTime = ArrayList()
        ids = ArrayList()
        statuses = ArrayList()
        appointmentModelArrayList = ArrayList()

        appointmentAdapter =
            AppointmentAdapter(requireContext(), appointmentModelArrayList, object :
                RecyclerInterface {
                override fun onItemClick(position: Int) {
                    TODO("Not yet implemented")
                }

                override fun onItemHold(position: Int) {
                    TODO("Not yet implemented")
                }
            })


        binding.appointmentsList.adapter = appointmentAdapter
        binding.appointmentsList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setListeners() {
        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun refresh() {
        appointmentModelArrayList.clear()
        names.clear()
        dateTime.clear()
        statuses.clear()
        ids.clear()
    }

    private fun loadData() {
        val r: StringRequest = object : StringRequest(
            Method.POST, DOCTOR_DATA_URL,
            Response.Listener { response ->
                try {
                    binding.refreshLayout.isRefreshing = false
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        binding.patientsNumber.text = j.getString(PARIENT_NUMBER)
                        val ja = j.getJSONArray(APPOINTMENTS)
                        binding.appointmentsNumber.text = ja.length().toString()
                        if (ja.length() != 0) {
                            for (i in 0 until ja.length()) {
                                j = ja.getJSONObject(i)
                                ids.add(j.getString(ID))
                                val builder: StringBuilder = StringBuilder(j.getString(TIME))
                                builder.replace(
                                    j.getString(TIME).lastIndexOf(":"),
                                    j.getString(TIME).length,
                                    ""
                                )
                                when (j.getInt(STATUS)) {
                                    0 -> statuses.add(R.color.orange) //pending
                                    1 -> statuses.add(R.color.blue)  //accepted
                                    2 -> statuses.add(R.color.green) //completed
                                    else -> statuses.add(R.color.red) //canceled
                                }
                                appointmentModelArrayList.add(
                                    AppointmentModel(
                                        j.getString(PATIENT_NAME),
                                        "date: " + j.getString(DATE) + " time: " + builder.toString(),
                                        "",
                                        statuses[i]
                                    )
                                )

                                //Log.d("appointment_details", "id: ${ids[i]}, name: ${j.getString(DOCTOR_NAME)}, with: ${withs[i]}, date: ${j.getString(DATE)}, time: ${j.getString(TIME)}, status: ${j.getInt(STATUS)}")
                            }
                            appointmentAdapter.notifyDataSetChanged()
                            binding.appointmentsList.visibility = View.VISIBLE
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("MY_ERRORaa", e.message!!)

                    Toast.makeText(
                        context,
                        getString(R.string.something_went_wrong_please_try_again),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            },
            { error ->

            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val p: MutableMap<String, String> = HashMap()
                val sh = requireContext().getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                p[ACCESS_TOKEN] = sh.getString(ACCESS_TOKEN, "")!!
                return p
            }
        }
        val q: RequestQueue = Volley.newRequestQueue(requireContext())
        q.add(r)
    }


}