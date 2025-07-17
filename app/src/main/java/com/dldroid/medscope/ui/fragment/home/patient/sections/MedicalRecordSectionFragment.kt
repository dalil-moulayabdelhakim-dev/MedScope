package com.dldroid.medscope.ui.fragment.home.patient.sections

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.CURRENT_MEDICATION
import com.dldroid.medscope.database.DOWNLOAD_FILES
import com.dldroid.medscope.database.GET_FILES_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.MEDICAL_HISTORY
import com.dldroid.medscope.database.MEDICATIONS
import com.dldroid.medscope.database.NAME
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.URLL
import com.dldroid.medscope.databinding.FragmentMedicalRecordSectionBinding
import com.dldroid.medscope.manager.FileDownloader
import com.dldroid.medscope.manager.MedicalRecordsAdapter
import com.dldroid.medscope.manager.MedicalRecordsModel
import com.dldroid.medscope.manager.PermissionsManager
import com.dldroid.medscope.manager.RecyclerInterface
import com.dldroid.medscope.ui.dialog.RequestPermissionDialog
import org.json.JSONException
import org.json.JSONObject
import java.io.File

class MedicalRecordSectionFragment : Fragment() {


    private var _binding: FragmentMedicalRecordSectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyUrls: ArrayList<String>
    private lateinit var historyNames: ArrayList<String>
    private lateinit var currentMedicationList: ArrayList<MedicalRecordsModel>
    private lateinit var historyList: ArrayList<MedicalRecordsModel>
    private lateinit var currentMedicationAdapter: MedicalRecordsAdapter
    private lateinit var historyAdapter: MedicalRecordsAdapter

    private lateinit var DOWNLOAD_DIR: String

    private lateinit var fileName: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalRecordSectionBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PermissionsManager.readExternalStoragePermission(context)
        PermissionsManager.writeExternalStoragePermission(context)
        init()
        setLiseners()
        refresh()
    }

    private fun setLiseners() {
        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }
    }


    private fun init() {
        historyUrls = ArrayList()
        historyNames = ArrayList()
        currentMedicationList = ArrayList()
        historyList = ArrayList()
        currentMedicationAdapter =
            MedicalRecordsAdapter(
                requireContext(),
                currentMedicationList,
                object : RecyclerInterface {
                    override fun onItemClick(position: Int) {
                        //Log.d("url", DOWNLOAD_FILES + currentMedicationList[position].getUrl())
                       // if (checkPermission()) {
                            fileName = currentMedicationList[position].getName()
                            val file = File("$DOWNLOAD_DIR/$fileName")
                            if (file.exists()) {
                                FileDownloader(requireActivity(), openPdfLauncher).openPdf(
                                    requireActivity(),
                                    file
                                )
                            } else {
                                FileDownloader(requireActivity(), openPdfLauncher).execute(
                                    DOWNLOAD_FILES + currentMedicationList[position].getUrl()
                                )
                            }
                        //}


                    }

                    override fun onItemHold(position: Int) {
                    }
                })
        historyAdapter =
            MedicalRecordsAdapter(requireContext(), historyList, object : RecyclerInterface {
                override fun onItemClick(position: Int) {
                    //if (checkPermission())
                        fileName = historyList[position].getName()
                    val file = File("$DOWNLOAD_DIR/$fileName")
                    if (file.exists()) {
                        FileDownloader(requireActivity(), openPdfLauncher).openPdf(
                            requireActivity(),
                            file
                        )
                    } else {
                        FileDownloader(requireActivity(), openPdfLauncher).execute(
                            DOWNLOAD_FILES + historyList[position].getUrl()
                        )
                    }
                }

                override fun onItemHold(position: Int) {
                }
            })
        binding.recyclerCurrentMedication.setAdapter(currentMedicationAdapter)
        binding.recyclerMedicationHistory.setAdapter(historyAdapter)
        binding.recyclerCurrentMedication.setLayoutManager(LinearLayoutManager(context))
        binding.recyclerMedicationHistory.setLayoutManager(LinearLayoutManager(context))

        binding.recyclerCurrentMedication.visibility = View.GONE
        binding.recyclerMedicationHistory.visibility = View.GONE
        DOWNLOAD_DIR = requireContext().filesDir.absolutePath
    }

    private val openPdfLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            val file = File(requireContext().filesDir, fileName)
            if (file.exists()) {
                file.delete()
                Log.d("FileDelete", "File deleted")
            }
        }

    private fun refresh() {
        binding.notFoundCurrentMedication.visibility = View.GONE
        binding.notFoundMedicationHistory.visibility = View.GONE
        binding.recyclerCurrentMedication.visibility = View.VISIBLE
        binding.recyclerMedicationHistory.visibility = View.VISIBLE
        historyUrls.clear()
        historyNames.clear()
        currentMedicationList.clear()
        historyList.clear()
        getFiles()
    }

    private fun getFiles() {
        binding.refreshLayout.isRefreshing = true
        val r: StringRequest = object : StringRequest(
            Method.POST, GET_FILES_URL,
            Response.Listener { response ->
                try {
                    binding.refreshLayout.isRefreshing = false
                    var j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {
                        val ja = j.getJSONArray(MEDICATIONS)
                        if (ja.length() > 0) {
                            j = ja.getJSONObject(ja.length() - 1)
                            val name = j.getString(NAME)
                            val url = j.getString(URLL)
                            currentMedicationList.add(MedicalRecordsModel(name, url))

                            if (ja.length() > 1) {
                                for (i in 0 until ja.length() - 1) {
                                    j = ja.getJSONObject(i)
                                    historyUrls.add(j.getString(URLL))
                                    historyNames.add(j.getString(NAME))
                                    historyList.add(
                                        MedicalRecordsModel(
                                            historyNames[i],
                                            historyUrls[i]
                                        )
                                    )
                                }
                                historyAdapter.notifyDataSetChanged()
                                binding.recyclerMedicationHistory.visibility = View.VISIBLE
                            }
                            currentMedicationAdapter.notifyDataSetChanged()
                            binding.recyclerCurrentMedication.visibility = View.VISIBLE
                        } else {
                            binding.notFoundMedicationHistory.visibility = View.VISIBLE
                            binding.notFoundCurrentMedication.visibility = View.VISIBLE
                            binding.recyclerCurrentMedication.visibility = View.GONE
                            binding.recyclerMedicationHistory.visibility = View.GONE
                        }
                    } else {
                        binding.notFoundMedicationHistory.visibility = View.VISIBLE
                        binding.notFoundCurrentMedication.visibility = View.VISIBLE
                        binding.recyclerCurrentMedication.visibility = View.GONE
                        binding.recyclerMedicationHistory.visibility = View.GONE
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    binding.notFoundMedicationHistory.visibility = View.VISIBLE
                    binding.notFoundCurrentMedication.visibility = View.VISIBLE
                    binding.recyclerCurrentMedication.visibility = View.GONE
                    binding.recyclerMedicationHistory.visibility = View.GONE
                    Log.e("MY_ERROR_AT_VIEWDOC_IN_ONRESPONSE", e.toString())
                }
            },
            Response.ErrorListener { error ->
                binding.refreshLayout.isRefreshing = false
                binding.notFoundMedicationHistory.visibility = View.VISIBLE
                binding.notFoundCurrentMedication.visibility = View.VISIBLE
                binding.recyclerCurrentMedication.visibility = View.GONE
                binding.recyclerMedicationHistory.visibility = View.GONE
                Log.e("MY_ERROR_AT_VIEWDOC_IN_ONERRORRESPONSE", error.toString())
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val p: MutableMap<String, String> = HashMap()
                val accessToken =
                    context?.getSharedPreferences(AUTH, Context.MODE_PRIVATE)?.getString(
                        ACCESS_TOKEN, ""
                    )
                p[ACCESS_TOKEN] = accessToken.toString()
                return p
            }
        }
        val q = Volley.newRequestQueue(context)
        q.add(r)
    }


    /*private fun checkPermission(): Boolean {
        if (!PermissionsManager.checkPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) && !PermissionsManager.checkPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            RequestPermissionDialog(Manifest.permission.READ_EXTERNAL_STORAGE).show(
                requireActivity().supportFragmentManager,
                "request read permission"
            )
            RequestPermissionDialog(Manifest.permission.WRITE_EXTERNAL_STORAGE).show(
                requireActivity().supportFragmentManager,
                "request write permission"
            )
            return false
        }
        return true
    }*/
}