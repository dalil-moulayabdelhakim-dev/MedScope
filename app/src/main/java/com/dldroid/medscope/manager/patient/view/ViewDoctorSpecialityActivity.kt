package com.dldroid.medscope.manager.patient.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.DOCTORS
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GET_DOCTORS_BY_SPECIALTY_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SPECIALTY
import com.dldroid.medscope.database.SPECIALTY_ID
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.databinding.ActivityViewDoctorSpecialityBinding
import com.dldroid.medscope.manager.RecyclerInterface
import com.dldroid.medscope.ui.fragment.home.patient.CreateAppointmentActivity
import com.dldroid.medscope.manager.receptionist.ReceptionistRecyclerAdapter
import com.dldroid.medscope.manager.receptionist.ReceptionistRecyclerModel
import org.json.JSONException
import org.json.JSONObject

class ViewDoctorSpecialityActivity : AppCompatActivity() {
    private lateinit var _binding : ActivityViewDoctorSpecialityBinding
    private val binding get() = _binding

    private var recRecyclerModelArrayList: java.util.ArrayList<ReceptionistRecyclerModel>? = null
    private var adapter: ReceptionistRecyclerAdapter? = null
    private var ids: java.util.ArrayList<String>? = null
    private var names: java.util.ArrayList<String?>? = null
    private var phones: java.util.ArrayList<String?>? = null
    private var specialtyID: String? = null
    private var specialtyName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityViewDoctorSpecialityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setListeners()
    }

    private fun setListeners() {
        binding.refreshLayout.setOnRefreshListener {
            refresh()
            binding.refreshLayout.isRefreshing = true
        }
        binding.back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun refresh() {
        binding.progressBar.visibility = View.VISIBLE
        binding.doctorsListSpecialty.visibility = View.GONE
        binding.nothingToShow.visibility = View.GONE
        reset()
        loadInfos()
    }

    private fun reset() {
        recRecyclerModelArrayList?.clear()
        ids?.clear()
        names?.clear()
        phones?.clear()
    }

    private fun init() {
        val b = intent.extras
        specialtyID = b!!.getString(SPECIALTY_ID, "")
        specialtyName = b!!.getString(SPECIALTY, "")
        binding.specialtyName.text = specialtyName + "s"
        recRecyclerModelArrayList = ArrayList()
        ids = ArrayList()
        names = ArrayList()
        phones = ArrayList()
        adapter = ReceptionistRecyclerAdapter(this, recRecyclerModelArrayList!!, object : RecyclerInterface {
            override fun onItemClick(position: Int) {
                val i = Intent(
                    applicationContext,
                    CreateAppointmentActivity::class.java
                )
                i.putExtra(ID, recRecyclerModelArrayList!![position].getId())
                startActivity(i)
            }

            override fun onItemHold(position: Int) {
            }
        })
        binding.doctorsListSpecialty.setAdapter(adapter)
        binding.doctorsListSpecialty.setLayoutManager(LinearLayoutManager(this))

        binding.progressBar.visibility = View.VISIBLE
        binding.doctorsListSpecialty.visibility = View.GONE
        binding.nothingToShow.visibility = View.GONE
    }


    override fun onResume() {
        //Manager(this).canBack("11")
        refresh()
        super.onResume()
    }

    private fun loadInfos() {
        binding.doctorsListSpecialty.visibility = View.GONE
        val r: StringRequest = object : StringRequest(
            Method.POST, GET_DOCTORS_BY_SPECIALTY_URL,
            Response.Listener{ response ->
                try {
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        val ja = j.getJSONArray(DOCTORS)

                        if (ja.length() != 0) {
                            for (i in 0 until ja.length()) {
                                j = ja.getJSONObject(i)
                                names?.add(j.getString(FULL_NAME))
                                ids?.add(j.getString(ID))
                                phones?.add("Phone N: " + j.getString(PHONE))
                                Log.d("getAllBySpecialty", j.getString(FULL_NAME))
                                recRecyclerModelArrayList?.add(
                                    ReceptionistRecyclerModel(
                                        names?.get(i),
                                        phones?.get(i),
                                        ids?.get(i)
                                    )
                                )
                            }

                            binding.progressBar.visibility = View.GONE
                            binding.doctorsListSpecialty.visibility = View.VISIBLE
                            binding.nothingToShow.visibility = View.GONE
                            adapter?.sort(ReceptionistRecyclerModel.BY_TITLE_ASCENDING)
                            adapter?.notifyDataSetChanged()
                        } else {
                            binding.progressBar.visibility = View.GONE
                            binding.doctorsListSpecialty.visibility = View.GONE
                            binding.nothingToShow.visibility = View.VISIBLE
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.doctorsListSpecialty.visibility = View.GONE
                        binding.nothingToShow.visibility = View.VISIBLE
                    }
                    binding.refreshLayout.isRefreshing = false
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("getAllBySpecialty", e.toString())
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.something_went_wrong_please_try_again),
                        Toast.LENGTH_SHORT
                    ).show()

                    binding.progressBar.visibility = View.GONE
                    binding.doctorsListSpecialty.visibility = View.GONE
                    binding.nothingToShow.visibility = View.VISIBLE
                    binding.refreshLayout.isRefreshing = false
                }
            },
            Response.ErrorListener { error ->
                Log.e("getAllBySpecialtyError", error.toString())
                binding.progressBar.visibility = View.GONE
                binding.doctorsListSpecialty.visibility = View.GONE
                binding.nothingToShow.visibility = View.VISIBLE
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val p: MutableMap<String, String> = HashMap()
                p[SPECIALTY_ID] = specialtyID.toString()
                return p
            }
        }
        val q = Volley.newRequestQueue(applicationContext)
        q.add(r)
    }

}