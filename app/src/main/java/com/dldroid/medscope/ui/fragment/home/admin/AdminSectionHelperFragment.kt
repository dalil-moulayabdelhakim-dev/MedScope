package com.dldroid.medscope.ui.fragment.home.admin

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.ACTIVE
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GET_ALL_BY_TYPE_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.UPDATED_AT
import com.dldroid.medscope.database.USERS
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.FragmentAdminSectionHelperBinding
import com.dldroid.medscope.manager.AuthManager
import com.dldroid.medscope.manager.MyCaptureActivity
import com.dldroid.medscope.manager.PermissionsManager
import com.dldroid.medscope.manager.RecyclerInterface
import com.dldroid.medscope.manager.ToolsManager
import com.dldroid.medscope.ui.LoginActivity
import com.dldroid.medscope.ui.dialog.RequestPermissionDialog
import com.dldroid.medscope.ui.fragment.home.admin.manage.AdminHelperRecyclerAdapter
import com.dldroid.medscope.ui.fragment.home.admin.manage.AdminHelperRecyclerItem
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

open class AdminSectionHelperFragment : Fragment() {
    private var _binding: FragmentAdminSectionHelperBinding? = null
    open val binding get() = _binding!!
    open lateinit var userList: ArrayList<AdminHelperRecyclerItem>
    open lateinit var adapter: AdminHelperRecyclerAdapter
    open lateinit var userType: String
    open lateinit var names: ArrayList<String>
    open lateinit var ids: ArrayList<String>
    open lateinit var identitys: ArrayList<String>
    open lateinit var online: ArrayList<String>
    open lateinit var lastSees: ArrayList<String>
    open var image = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAdminSectionHelperBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListeners()
        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        reset()
        _binding = null
    }

    private fun setListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadData()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    search(query)
                } else {
                    reset()
                    loadData()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        binding.scanCodeButton.setOnClickListener {
            scanner()
        }
    }

    private fun init() {
        userList = ArrayList()
        names = ArrayList()
        ids = ArrayList()
        identitys = ArrayList()
        online = ArrayList()
        lastSees = ArrayList()
        adapter = AdminHelperRecyclerAdapter(context, userList, object :
            RecyclerInterface {
            override fun onItemClick(position: Int) {
                val intent = Intent(context, ViewUserDetailsActivity::class.java)
                intent.putExtra(ID, userList[position].getId())
                startActivity(intent)
            }

            override fun onItemHold(position: Int) {
                val builder = AlertDialog.Builder(requireContext(), R.style.dialog)
                val dialog = builder.setTitle("Remove user")
                    .setIcon(R.drawable.ic_about)
                    .setMessage(
                        "Are you sure you want to remove this user '${userList[position].getUserName()}'?\n\n This action can not be undone"
                    )
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ ->
                        ToolsManager.removeUser(requireContext() , requireActivity().supportFragmentManager, binding.swipeRefreshLayout, userList[position].getId().toString())
                    }
                    .setNegativeButton(
                        getString(R.string.no)
                    ) { dialogInterface, i -> dialogInterface.cancel() }
                    .create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                }
                dialog.show()
            }

        })
        binding.helperRecyclerView.adapter = adapter
        binding.helperRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.progressBar.visibility = VISIBLE
        binding.helperRecyclerView.visibility = GONE
        binding.emptyLayout.visibility = GONE
    }

    private fun reset() {
        userList.clear()
        names.clear()
        ids.clear()
        identitys.clear()
        online.clear()
        lastSees.clear()
        adapter.notifyDataSetChanged() // Notify adapter after clearing
    }

    protected open fun loadData() {
        reset()
        val request = @SuppressLint("NotifyDataSetChanged")
        object : StringRequest(
            Method.POST, GET_ALL_BY_TYPE_URL,
            Response.Listener { response ->
                var jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)
                if (success) {
                    val jsonArray: JSONArray = jsonObject.getJSONArray(USERS)

                    if (jsonArray.length() != 0) {
                        for (i in 0 until jsonArray.length()) {
                            jsonObject = jsonArray.getJSONObject(i)
                            names.add(jsonObject.getString(FULL_NAME))
                            ids.add(jsonObject.getString(ID))
                            Log.i("id-load", jsonObject.getString(ID))
                            identitys.add(jsonObject.getString(ID_CARD_NUMBER))
                            online.add(jsonObject.getString(ACTIVE))
                            if (online[i] == "1") {
                                image = R.drawable.shape_circle_online
                                lastSees.add("Online")
                            } else {
                                image = R.drawable.shape_circle_offline
                                var t: String = jsonObject.getString(UPDATED_AT)
                                t = t.replace("T", " ")
                                val builder = StringBuilder(t)
                                builder.replace(t.lastIndexOf(":"), t.length, "")
                                t = builder.toString()
                                lastSees.add("last seen $t")
                            }

                            userList.add(
                                AdminHelperRecyclerItem(
                                    ids[i],
                                    names[i],
                                    lastSees[i],
                                    image
                                )
                            )
                        }

                        binding.progressBar.visibility = GONE
                        binding.helperRecyclerView.visibility = VISIBLE
                        adapter.sort(AdminHelperRecyclerItem.BY_TITLE_ASCENDING)
                        adapter.notifyDataSetChanged()
                    }else{
                        binding.progressBar.visibility = GONE
                        binding.helperRecyclerView.visibility = GONE
                        binding.emptyLayout.visibility = VISIBLE
                    }
                    if (binding.swipeRefreshLayout.isRefreshing)
                        binding.swipeRefreshLayout.isRefreshing = false
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
                }
            }, Response.ErrorListener { error ->
                val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
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
                        val sh: SharedPreferences =
                            requireContext().getSharedPreferences(AUTH, 0x0000)
                        val e: SharedPreferences.Editor = sh.edit()
                        e.clear()

                        //Manager(getApplicationContext()).dontBack()
                        e.apply()
                        requireContext().startActivity(Intent(context, LoginActivity::class.java))
                        requireActivity().finish()
                    }
                    .create()
                dialog.setOnShowListener {
                    context?.resources?.getColor(R.color.blue)?.let { it1 ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(it1)
                    }
                }
                dialog.show()
                Log.e("ProfileFragment.loadProfile.error", error.toString())

                if (binding.swipeRefreshLayout.isRefreshing)
                    binding.swipeRefreshLayout.isRefreshing = false
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences = context?.getSharedPreferences(
                    AUTH, Context.MODE_PRIVATE
                )

                params[ACCESS_TOKEN] = sharedPreferences?.getString(ACCESS_TOKEN, "").toString()
                params[USER_TYPE] = userType
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    private fun search(text: String) {
        userList.clear()
        for (i in identitys.indices) {
            if (identitys[i].uppercase(Locale.getDefault()).trim { it <= ' ' }
                    .contains(
                        text.uppercase(Locale.getDefault())
                            .trim { it <= ' ' }) || names[i].uppercase(
                    Locale.getDefault()
                ).trim { it <= ' ' }
                    .contains(text.uppercase(Locale.getDefault()).trim { it <= ' ' })
            ) {
                if (online[i] == "1") {
                    image = R.drawable.shape_circle_online
                } else {
                    image = R.drawable.shape_circle_offline
                }
                userList.add(AdminHelperRecyclerItem(ids[i], names[i], lastSees[i], image))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun scanner() {
        if (PermissionsManager.cameraPermission(requireContext())) {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                .setPrompt(getString(R.string.va_vd))
                .setBeepEnabled(false)
                .setBarcodeImageEnabled(true)
                .setCaptureActivity(MyCaptureActivity::class.java)
            barcodeLauncher.launch(options)
        } else {
            RequestPermissionDialog(Manifest.permission.CAMERA).show(requireActivity().supportFragmentManager, "request camera permission")
            Toast.makeText(context, getString(R.string.camera_permission_is_required), Toast.LENGTH_LONG).show()
        }
    }

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(context, getString(R.string.cancelled), Toast.LENGTH_LONG).show()
        } else {
            binding.searchView.setQuery(result.contents, true)
        }
    }
}