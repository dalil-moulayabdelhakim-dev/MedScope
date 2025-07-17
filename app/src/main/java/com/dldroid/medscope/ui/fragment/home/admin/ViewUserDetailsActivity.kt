package com.dldroid.medscope.ui.fragment.home.admin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.DATA
import com.dldroid.medscope.database.DATE_OF_BIRTH
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERRORS
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GENDER
import com.dldroid.medscope.database.GET_USER_BY_ID_URL
import com.dldroid.medscope.database.ID
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.PASSWORD
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SPECIALTY
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.UPDATE_USER_URL
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.ActivityViewUserDetailsBinding
import com.dldroid.medscope.manager.AuthManager
import com.dldroid.medscope.manager.time.Calendar
import com.dldroid.medscope.manager.ToolsManager
import com.dldroid.medscope.ui.LoginActivity
import com.dldroid.medscope.ui.dialog.SimpleProgressDialog
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class ViewUserDetailsActivity : AppCompatActivity() {

    private var _binding: ActivityViewUserDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var id: String
    private lateinit var userRoles: ArrayList<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityViewUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        loadInfo()
    }

    private fun init() {
        id = intent.getStringExtra(ID).toString()

        Log.i("id", id)
        userRoles = ArrayList()
        userRoles.addAll(resources.getStringArray(R.array.roles))
        spinnerAdapter =
            ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, userRoles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.userRoleSpinner.adapter = spinnerAdapter
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                submitForm(it)
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadInfo()
        }

        binding.removeButton.setOnClickListener {
            val builder = AlertDialog.Builder(this, R.style.dialog)
            val dialog = builder.setTitle("Remove user")
                .setIcon(R.drawable.ic_about)
                .setMessage(
                    "Are you sure you want to remove this user?\n\n This action can not be undone"
                )
                .setPositiveButton(
                    getString(R.string.yes)
                ) { _, _ ->
                    ToolsManager.removeUser(this , supportFragmentManager, binding.swipeRefreshLayout, id)
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

        binding.showCalendarButton.setOnClickListener {
            val calendar = Calendar(applicationContext, binding.dateOfBirthInput)
            calendar.showCalendar()
        }
        binding.generatePasswordButton.setOnClickListener {
            binding.passwordInput.setText(AuthManager.generatePassword())
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun loadInfo() {
        val simpleProgressDialog = SimpleProgressDialog()
        simpleProgressDialog.show(supportFragmentManager, "loading")
        val request = object : StringRequest(
            Method.POST, GET_USER_BY_ID_URL,
            Response.Listener { response ->
                simpleProgressDialog.dismiss()
                var jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)
                jsonObject = jsonObject.getJSONObject(DATA)


                if (success) {
                    Log.d("Response", jsonObject.toString())
                    AuthManager.generateCode(
                        jsonObject.getString(ID_CARD_NUMBER),
                        binding.qrCodeView
                    )
                    binding.fullNameInput.setText(jsonObject.getString(FULL_NAME))
                    binding.identityCardNumberInput.setText(jsonObject.getString(ID_CARD_NUMBER))
                    binding.dateOfBirthInput.setText(jsonObject.getString(DATE_OF_BIRTH))
                    binding.emailInput.setText(jsonObject.getString(EMAIL))
                    binding.phoneNumberInput.setText(jsonObject.getString(PHONE))
                    binding.userRoleSpinner.setSelection(jsonObject.getInt(USER_TYPE))
                    binding.passwordInput.setText("Create new password")

                    if (jsonObject.getString(USER_TYPE) == "2") {
                        binding.specialtyInput.setText(jsonObject.getString(SPECIALTY))
                        binding.specialtyLayout.isVisible = true
                    }

                    if(jsonObject.getString(GENDER).equals("1")){
                        binding.maleRadioButton.isChecked = true
                        binding.femalRadioButton.isChecked = false
                    }else{
                        binding.maleRadioButton.isChecked = false
                        binding.femalRadioButton.isChecked = true
                    }
                    setListeners()
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }, Response.ErrorListener { error ->
                simpleProgressDialog.dismiss()
                val networkResponse = error.networkResponse
                val statusCode = networkResponse.statusCode
                //Log.d("ProfileFragment.loadProfile.error", String(networkResponse.data))
                val jsonObject = JSONObject(String(networkResponse.data))
                val title = jsonObject.getString(TITLE)
                val message = jsonObject.getString(MESSAGE)
                val builder = AlertDialog.Builder(
                    applicationContext, R.style.dialog
                )
                lateinit var dialog: AlertDialog
                builder.setTitle("$statusCode $title")
                    .setIcon(R.drawable.ic_logout)
                    .setMessage(message)
                if (statusCode == 401) {
                    dialog = builder.setPositiveButton(
                        getString(R.string.ok)
                    ) { _, _ ->
                        val sh: SharedPreferences =
                            applicationContext.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                        val e: SharedPreferences.Editor = sh.edit()
                        e.clear()

                        //Manager(getApplicationContext()).dontBack()
                        e.apply()
                        applicationContext.startActivity(
                            Intent(
                                applicationContext,
                                LoginActivity::class.java
                            )
                        )
                        finish()
                    }.create()
                } else {
                    dialog = builder.setPositiveButton(
                        getString(R.string.ok)
                    ) { _, _ ->
                        dialog.dismiss()
                    }.create()
                }

                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.blue))
                }
                dialog.show()
                binding.swipeRefreshLayout.isRefreshing = false
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences =
                    applicationContext.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                params[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                params[ID] = id
                return params
            }
        }
        Volley.newRequestQueue(applicationContext).add(request)
    }

    private fun validateForm(): Boolean {
        var correctCounter = 0
        if (binding.userRoleSpinner.selectedItemId.toInt() != 0) {
            correctCounter++
            binding.userRoleError.isVisible = false
        } else {
            binding.userRoleError.text = getString(R.string.invalid_choice)
            binding.userRoleError.isVisible = true
        }

        if (binding.fullNameInput.text.toString().isNotEmpty()) {
            if (binding.fullNameInput.text.toString().length <= 50) {
                correctCounter++
                binding.fullNameLayout.error = null
            } else {
                binding.fullNameLayout.error =
                    getString(R.string.the_full_name_must_be_less_than_50_characters)
            }
        } else
            binding.fullNameLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.identityCardNumberInput.text.toString().isNotEmpty()) {
            if (binding.identityCardNumberInput.text.toString().length >= 10) {
                correctCounter++
                binding.identityCardNumberLayout.error = null
            } else {
                binding.identityCardNumberLayout.error =
                    getString(R.string.the_identity_card_number_must_be_10_digits)
            }
        } else
            binding.identityCardNumberLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.dateOfBirthInput.text.toString().isNotEmpty()) {
            correctCounter++
            binding.dateOfBirthLayout.error = null
        } else
            binding.dateOfBirthLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.emailInput.text.toString().isNotEmpty())
            if (Patterns.EMAIL_ADDRESS.matcher(binding.emailInput.text.toString()).matches()) {
                correctCounter++
                binding.emailLayout.error = null
            } else
                binding.emailLayout.error = getString(R.string.this_email_not_valid)
        else
            binding.emailLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.phoneNumberInput.text.toString()
                .isNotEmpty()
        ) {
            if (binding.phoneNumberInput.text.toString().length == 10) {
                correctCounter++
                binding.phoneNumberLayout.error = null
            } else {

                binding.phoneNumberLayout.error =
                    getString(R.string.the_phone_number_must_be_10_digits)
            }
        } else
            binding.phoneNumberLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.passwordInput.text.toString().isNotEmpty()) {
            correctCounter++
            binding.passwordLayout.error = null
        } else
            binding.passwordLayout.error = getString(R.string.this_feild_canted_be_empty)

        if (binding.specialtyLayout.isVisible) {

            if (binding.specialtyInput.text.toString().isNotEmpty()) {
                correctCounter++
                binding.specialtyLayout.error = null
            } else
                binding.specialtyLayout.error = getString(R.string.this_feild_canted_be_empty)

            return correctCounter == 8
        } else {
            return correctCounter == 7

        }
    }

    private fun submitForm(it: View) {
        val simpleProgressDialog = SimpleProgressDialog()
        simpleProgressDialog.show(supportFragmentManager, "loading")
        val request = object : StringRequest(
            Method.POST, UPDATE_USER_URL,
            Response.Listener { response ->
                simpleProgressDialog.dismiss()
                val jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)

                if (success) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.user_updated_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener { error ->
                simpleProgressDialog.dismiss()
                val networkResponse = error.networkResponse
                val statusCode = networkResponse.statusCode
                val jsonObject = JSONObject(String(networkResponse.data))
                if (statusCode == 401) {
                    val title = jsonObject.getString(TITLE)
                    val message = jsonObject.getString(MESSAGE)
                    val builder = AlertDialog.Builder(
                        applicationContext, R.style.dialog
                    )
                    val dialog = builder.setTitle(title)
                        .setIcon(R.drawable.ic_logout)
                        .setMessage(message)
                        .setPositiveButton(
                            getString(R.string.ok)
                        ) { _, _ ->
                            val sh: SharedPreferences =
                                applicationContext.getSharedPreferences(AUTH, 0x0000)
                            val e: SharedPreferences.Editor = sh.edit()
                            e.clear()

                            //Manager(getApplicationContext()).dontBack()
                            e.apply()
                            applicationContext.startActivity(
                                Intent(
                                    applicationContext,
                                    LoginActivity::class.java
                                )
                            )
                            finish()
                        }
                        .create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(resources.getColor(R.color.blue))
                    }
                    dialog.show()
                } else {

                    val snackbar = Snackbar.make(
                        /* view = */ it,
                        /* text = */ jsonObject.getString(ERRORS),
                        /* duration = */ Snackbar.LENGTH_SHORT
                    )

                    snackbar.show()
                }

            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                val sharedPreferences =
                    applicationContext.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                params[ID] = id
                params[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                params[FULL_NAME] = binding.fullNameInput.text.toString()
                params[ID_CARD_NUMBER] = binding.identityCardNumberInput.text.toString()
                params[DATE_OF_BIRTH] = binding.dateOfBirthInput.text.toString()
                params[EMAIL] = binding.emailInput.text.toString()
                params[PHONE] = binding.phoneNumberInput.text.toString()
                params[PASSWORD] = binding.passwordInput.text.toString()
                params[GENDER] = if (binding.maleRadioButton.isChecked) "1" else "0"
                params[USER_TYPE] = binding.userRoleSpinner.selectedItemId.toString()
                if (binding.specialtyLayout.isVisible)
                    params[SPECIALTY] = binding.specialtyInput.text.toString()

                return params
            }
        }
        Volley.newRequestQueue(applicationContext).add(request)

    }

}


