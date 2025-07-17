package com.dldroid.medscope.ui

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.CHECK_ACCESS_TOKEN
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.USER_PROFILE_URL
import com.dldroid.medscope.databinding.ActivityHomeBinding
import com.dldroid.medscope.manager.AuthManager
import com.dldroid.medscope.manager.PermissionsManager
import com.dldroid.medscope.manager.SimpleDialog
import com.dldroid.medscope.manager.services.AppointmentNotificationService
import com.dldroid.medscope.ui.dialog.QRDialog
import com.dldroid.medscope.ui.fragment.ProfileFragment
import com.dldroid.medscope.ui.fragment.home.admin.AdminHomeFragment
import com.dldroid.medscope.ui.fragment.home.doctor.DoctorHomeFragment
import com.dldroid.medscope.ui.fragment.home.patient.PatientHomeFragment
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private var menuSelector = 0
    private var userType = ""
    private var isDoubleTap = false
    private lateinit var headerNavigation: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        init()
        checkAccess()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PermissionsManager.postNotificationPermission(this)) {
            PermissionsManager.postNotificationPermission(this)
        }else{
            createNotificationChannel()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !PermissionsManager.foregroundSpecialUsePermission(this)){
            PermissionsManager.foregroundSpecialUsePermission(this)
        }
        screenLoader()
        val toggle = ActionBarDrawerToggle(
            this@HomeActivity, binding.drawer, binding.toolbar, R.string.open_navigatiion_drawer,
            R.string.close_navigation_drawer
        )
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()
        setListeners()
        getUser()
    }

    override fun onResume() {
        super.onResume()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1) // "1" is the same ID used in notify()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val channel1 = NotificationChannel("Channel1", "Appointment Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            val channel2 = NotificationChannel("Channel2", "Service Notifications", NotificationManager.IMPORTANCE_LOW)

            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
        }
    }

    private fun checkAccess() {
        val request = @SuppressLint("NotifyDataSetChanged")
        object : StringRequest(
            Method.POST, CHECK_ACCESS_TOKEN,
            Response.Listener { _ ->

            }, Response.ErrorListener { error ->
                /*val jsonObject = JSONObject(error.networkResponse.data.toString(Charsets.UTF_8))
                val title = jsonObject.getString(TITLE)
                val message = jsonObject.getString(MESSAGE)
                SimpleDialog.showDialog(this, title, message)

                Log.e("ProfileFragment.loadProfile.error", error.toString())
*/
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences = applicationContext?.getSharedPreferences(
                    AUTH, Context.MODE_PRIVATE
                )

                params[ACCESS_TOKEN] = sharedPreferences?.getString(ACCESS_TOKEN, "").toString()
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(request)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onBackPressed() {
        // Manager(applicationContext).dontBack()
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else if (isDoubleTap) {
            super.onBackPressed()
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.click_back_again),
                Toast.LENGTH_SHORT
            )
                .show()
            isDoubleTap = true
            Handler().postDelayed({ isDoubleTap = false }, 2000)
        }
    }

    private fun init() {
        userType = AuthManager.getUserType(this)

         headerNavigation = binding.navigationView.getHeaderView(0)

    }

    private fun setListeners() {
        binding.navigationView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->

            return@OnNavigationItemSelectedListener when (item.itemId) {
                R.id.home_menu_button -> {
                    if (menuSelector != 0) {
                        binding.drawer.closeDrawer(GravityCompat.START)
                        menuSelector = 0
                        screenLoader()
                    }

                    true
                }

                R.id.profile_menu_button -> {
                    if (menuSelector != 1) {
                        binding.drawer.closeDrawer(GravityCompat.START)
                        menuSelector = 1
                        binding.toolbar.title = getString(R.string.profile)
                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.fragment_container_view,
                                ProfileFragment::class.java,
                                null
                            )
                            .commit()
                    }
                    binding.comingSoon.visibility = GONE
                    true
                }

                R.id.about_menu_button -> {
                    if (menuSelector != 3) {
                        binding.drawer.closeDrawer(GravityCompat.START)
                        menuSelector = 3
                    }
                    true
                }

                R.id.logout_menu_button -> {
                    binding.drawer.closeDrawer(GravityCompat.START)
                    menuSelector = 2
                    val builder = AlertDialog.Builder(this, R.style.dialog)
                    val dialog = builder.setTitle("Logout")
                        .setIcon(R.drawable.ic_logout)
                        .setMessage(getString(R.string.do_you_want_to_logout))
                        .setPositiveButton(getString(R.string.yes)
                        ) { _, _ ->
                            //Manager(this@HomeScreen).dontBack()
                            AuthManager.logout(this, supportFragmentManager)
                        }
                        .setNegativeButton(getString(R.string.no)
                        ) { dialogInterface, i -> dialogInterface.cancel() }
                        .create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.blue))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.blue))
                    }
                    dialog.show()
                    false
                }

                else -> {
                    false
                }
            }
        })
    }


    private fun screenLoader() {
        Log.i("ScreenLoader", "userType: $userType")
        when (userType) {
            "3" -> { //admin
                binding.comingSoon.visibility = GONE
                binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.userPic)
                    .setImageResource(R.drawable.tp_pic_admin)

                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, AdminHomeFragment::class.java, null)
                    .commit()
            }

            "2" -> { //doctor
                binding.comingSoon.visibility = GONE
                startAppointmentNotificationService()
                binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.userPic)
                    .setImageResource(R.drawable.tp_pic_doctor)

                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, DoctorHomeFragment::class.java, null)
                    .commit()
            }

            "4" -> { //laboratory
                binding.comingSoon.visibility = VISIBLE
                startAppointmentNotificationService()
                binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.userPic)
                    .setImageResource(R.drawable.tp_pic_laboratory)
            }

            "5" -> { //receptionist
                binding.comingSoon.visibility = VISIBLE
                binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.userPic)
                    .setImageResource(R.drawable.tp_rec_ic)
            }

            else -> { //patient
                startAppointmentNotificationService()
                binding.comingSoon.visibility = GONE
                binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.userPic)
                    .setImageResource(R.drawable.tp_pic_patient)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_view, PatientHomeFragment::class.java, null)
                    .commit()
            }
        }
    }

    private fun startAppointmentNotificationService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isNotificationServiceRunning() && PermissionsManager.foregroundSpecialUsePermission(this) && PermissionsManager.postNotificationPermission(this)) {
            val i = Intent(
                this,
                AppointmentNotificationService::class.java
            )
            startForegroundService(i)
        } else {
            Log.e("API EXCEPTION", "need api from oreo(api 26)+")
        }
    }

    private fun isNotificationServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (AppointmentNotificationService::class.java.name == service.service.className) {
                return true
            }
        }
        Log.i("NotificationService", "not running")
        return false
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_qr_code -> {
                val add = QRDialog()
                add.show(supportFragmentManager, "qr scan")
            }
            R.id.action_exit -> {
                exitProcess(0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getUser() {
        val request: StringRequest = object : StringRequest(
            Method.POST, USER_PROFILE_URL,
            Response.Listener<String?> { response ->
                try {
                    val j = JSONObject(response)
                    val b = j.getBoolean(SUCCESS)
                    if (b) {

                        headerNavigation.findViewById<TextView>(R.id.user_name_header).text = j.getString(FULL_NAME)
                        headerNavigation.findViewById<TextView>(R.id.id_card_number_header).text = "ID N: ${j.getString(ID_CARD_NUMBER)}"

                    } else {
                        headerNavigation.findViewById<TextView>(R.id.user_name_header).text = "UNKNOWN"
                        headerNavigation.findViewById<TextView>(R.id.id_card_number_header).text = "UNKNOWN"
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("MY_ERROR_AT_HOMESCR_IN_ONRESPONSE", e.toString())
                }
            },
            Response.ErrorListener { error ->
                Log.e(
                    "MY_ERROR_AT_HOMESCR_IN_ONERRORRESPONSE",
                    error.toString()
                )
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val param: MutableMap<String, String> = HashMap()
                val sharedPreferences = getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                param[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                return param
            }
        }

        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(request)
    }
}