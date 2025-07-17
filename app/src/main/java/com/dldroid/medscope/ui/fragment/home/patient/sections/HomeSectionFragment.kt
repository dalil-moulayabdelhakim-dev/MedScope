package com.dldroid.medscope.ui.fragment.home.patient.sections

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.APPOINTMENTS
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATE
import com.dldroid.medscope.database.DOCTOR_NAME
import com.dldroid.medscope.database.GET_ALL_APPOINTMENTS_BY_PATIENT
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.SPECIALTY
import com.dldroid.medscope.database.SPECIALTY_ID
import com.dldroid.medscope.database.STATUS
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TIME
import com.dldroid.medscope.database.WITH
import com.dldroid.medscope.databinding.FragmentHomeSectionBinding
import com.dldroid.medscope.manager.RecyclerInterface
import com.dldroid.medscope.manager.patient.AdviceAdapter
import com.dldroid.medscope.manager.patient.AdviceModel
import com.dldroid.medscope.manager.patient.AppointmentAdapter
import com.dldroid.medscope.manager.patient.AppointmentModel
import com.dldroid.medscope.manager.patient.SpecialtyAdapter
import com.dldroid.medscope.manager.patient.SpecialtyModel
import com.dldroid.medscope.manager.patient.view.ViewAppointmentForPatientActivity
import com.dldroid.medscope.manager.patient.view.ViewDoctorSpecialityActivity
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs

class HomeSectionFragment : Fragment() {

    private var _binding: FragmentHomeSectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var listItems: ArrayList<AdviceModel>
    private lateinit var compositePageTransformer: CompositePageTransformer
    private val sH = Handler()
    private lateinit var adviceAdapter: AdviceAdapter
    private lateinit var specialtyModelArrayList: ArrayList<SpecialtyModel>
    private lateinit var specialtyAdapter: SpecialtyAdapter
    private lateinit var specialtyModel: SpecialtyModel

    private lateinit var names: ArrayList<String>
    private lateinit var dateTime: java.util.ArrayList<String>
    private lateinit var ids: java.util.ArrayList<String>
    private lateinit var statuses: java.util.ArrayList<Int>
    private lateinit var withs: java.util.ArrayList<String>
    private lateinit var appointmentModelArrayList : ArrayList<AppointmentModel>
    private lateinit var appointmentAdapter : AppointmentAdapter


    companion object{
        private const val DELAY: Int = 5000
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeSectionBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        prepareViewPager()
        setListeners()
        refresh()

    }

    private fun setListeners() {
        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun init() {

        names = ArrayList()
        dateTime = ArrayList()
        ids = ArrayList()
        statuses = ArrayList()
        withs = ArrayList()
        listItems = ArrayList()
        specialtyModelArrayList = ArrayList()
        appointmentModelArrayList = ArrayList()
        appointmentAdapter = AppointmentAdapter(requireContext(), appointmentModelArrayList, object : RecyclerInterface{
            override fun onItemClick(position: Int) {
                val i = Intent(
                    context,
                    ViewAppointmentForPatientActivity::class.java
                )
                i.putExtra(ID, ids[position])
                startActivity(i)
            }

            override fun onItemHold(position: Int) {
                TODO("Not yet implemented")
            }
        })
        binding.appointmentsList.adapter = appointmentAdapter
        binding.appointmentsList.layoutManager = LinearLayoutManager(requireContext())

        binding.specialtyRecyclerView.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        val speT = arrayOf(
            "Dentist", "Cardiologist", "Dermatologist", "Ayurveda", "Eye Care", "Orthopedic",
            "Urologist", "Gynecologist"
        )
        val speI = intArrayOf(
            R.drawable.img_dentist,
            R.drawable.img_cardiologist,
            R.drawable.img_dermatologist,
            R.drawable.img_ayurveda,
            R.drawable.img_eye_care,
            R.drawable.img_orthopedic,
            R.drawable.img_urologist,
            R.drawable.img_gynecologist
        )

        for (i in speT.indices) {
            specialtyModelArrayList.add(SpecialtyModel(speT[i], speI[i]))
        }

        specialtyAdapter =
            SpecialtyAdapter(requireContext(), specialtyModelArrayList, object : RecyclerInterface {
                override fun onItemClick(position: Int) {
                    val i = Intent(context, ViewDoctorSpecialityActivity::class.java)
                    i.putExtra(
                        SPECIALTY_ID,
                        (position + 1).toString()
                    )
                    i.putExtra(SPECIALTY, specialtyModelArrayList[position].getName())
                    startActivity(i)
                }

                override fun onItemHold(position: Int) {
                }
            })
        compositePageTransformer = CompositePageTransformer()
        val devices = arrayOf(
            "Health: is the balance and harmony of all forces together",
            "Make sure to eat vegetables and fruits",
            "Avoid anxiety and stress to maintain blood pressure",
            "Carrying weights helps prevent osteoporosis",
            "It is recommended to drink 3.5 liters of water daily",
            "Sleep keeps the skin fresh"
        )
        val images = intArrayOf(
            R.drawable.img_health_power,
            R.drawable.img_eat_food,
            R.drawable.img_pression,
            R.drawable.img_power,
            R.drawable.img_water,
            R.drawable.img_sleep
        )

        for ((i, image) in images.withIndex()) {
            listItems.add(AdviceModel("- " + devices[i], image))
        }
        adviceAdapter = AdviceAdapter(listItems, binding.viewPager2)
        binding.specialtyRecyclerView.setAdapter(specialtyAdapter)
    }


    private fun prepareViewPager() {
        compositePageTransformer.addTransformer { page, position ->
            val r = (1 - abs(position.toDouble())).toFloat()
            page.scaleY = 0.85f + r * 0.15f
        }

        binding.viewPager2.setPageTransformer(compositePageTransformer)
        binding.viewPager2.setAdapter(adviceAdapter)

        binding.viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sH.removeCallbacks(runnable)
                sH.postDelayed(
                    runnable,
                    DELAY.toLong()
                )
            }
        })
        binding.dotsIndicator.setViewPager2(binding.viewPager2)

    }

    private val runnable = Runnable {
        var next: Int = binding.viewPager2.currentItem + 1
        if (next == listItems.size) {
            next = 0
        }
        binding.viewPager2.currentItem = next
    }

    private fun getAppointments() {
        val r: StringRequest = object : StringRequest(
            Method.POST, GET_ALL_APPOINTMENTS_BY_PATIENT,
            Response.Listener<String?> { response ->
                try {
                    binding.refreshLayout.isRefreshing = false
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        val ja = j.getJSONArray(APPOINTMENTS)
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
                                withs.add(j.getString(WITH))
                                appointmentModelArrayList.add(
                                    AppointmentModel(
                                        j.getString(DOCTOR_NAME),
                                        "date: " + j.getString(DATE) + " time: " + builder.toString(),
                                        withs[i],
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

                    Toast.makeText(context, getString(R.string.something_went_wrong_please_try_again), Toast.LENGTH_SHORT)
                        .show()
                }
            },
            Response.ErrorListener { error ->
//                val j = error?.networkResponse?.data?.toString(Charsets.UTF_8)
//                    ?.let { JSONObject(it) }
//                val title = j?.getString(TITLE)
//                val message = j?.getString(MESSAGE)
                binding.refreshLayout.isRefreshing = false
                binding.appointmentsList.visibility = View.VISIBLE
                Log.d("errorC", error.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val p: MutableMap<String, String> = HashMap()
                val sh = requireContext().getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                p[ACCESS_TOKEN] = sh.getString(ACCESS_TOKEN, "")!!
                return p
            }
        }
        val q = Volley.newRequestQueue(requireContext())
        q.add(r)
    }
    private fun refresh() {
        binding.refreshLayout.isRefreshing = true
        binding.appointmentsList.visibility = View.GONE
        reset()
        getAppointments()
    }

    private fun reset() {
        appointmentModelArrayList.clear()
        names.clear()
        dateTime.clear()
        statuses.clear()
        ids.clear()
        withs.clear()
    }

}