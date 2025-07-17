package com.dldroid.medscope.ui.fragment.home.admin.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.CREATE_USER_URL
import com.dldroid.medscope.database.DATE_OF_BIRTH
import com.dldroid.medscope.database.EMAIL
import com.dldroid.medscope.database.ERRORS
import com.dldroid.medscope.database.FULL_NAME
import com.dldroid.medscope.database.GENDER
import com.dldroid.medscope.database.ID_CARD_NUMBER
import com.dldroid.medscope.database.MESSAGE
import com.dldroid.medscope.database.PASSWORD
import com.dldroid.medscope.database.PHONE
import com.dldroid.medscope.database.SPECIALTY
import com.dldroid.medscope.database.SUCCESS
import com.dldroid.medscope.database.TITLE
import com.dldroid.medscope.database.USER_TYPE
import com.dldroid.medscope.databinding.DialogAddUserBinding
import com.dldroid.medscope.manager.AuthManager
import com.dldroid.medscope.manager.time.Calendar
import com.dldroid.medscope.ui.LoginActivity
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

class AddUserDialog : DialogFragment() {

    private var _binding: DialogAddUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.dialog)
        _binding = DialogAddUserBinding.inflate(layoutInflater)
        builder.setView(binding.root)
            .setTitle(getString(R.string.add_user))
            .setIcon(R.drawable.ic_profile)
        init()
        AuthManager.loadUserTypes(requireContext(), binding.userRoleSpinner)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListeners() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                submitForm(it)
            }
        }

        binding.showCalendarButton.setOnClickListener {
            val calendar = Calendar(requireContext(), binding.dateOfBirthInput)
            calendar.showCalendar()
        }

        binding.generatePasswordButton.setOnClickListener {
            binding.passwordInput.setText((AuthManager.generatePassword()))
        }

        binding.userRoleSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position != 0) {
                        binding.spinnerError.text = ""
                        binding.spinnerError.visibility = View.GONE
                        binding.userRoleSpinner.background =
                            context?.let {
                                getDrawable(
                                    it,
                                    R.drawable.shape_spinner
                                )
                            }
                    }

                    if (position == 2) {
                        binding.specialtyLayout.visibility = View.VISIBLE
                    } else {
                        binding.specialtyLayout.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
    }

    private fun init() {
        binding.userRoleSpinner.setPopupBackgroundDrawable(context?.let {
            getDrawable(
                it,
                R.drawable.shape_dialog
            )
        })
    }


    private fun validateForm(): Boolean {
        var correctCounter = 0
        if (binding.userRoleSpinner.selectedItemPosition != 0) {
            correctCounter++
            binding.spinnerError.text = ""
            binding.spinnerError.visibility = View.GONE
            binding.userRoleSpinner.background =
                context?.let { getDrawable(it, R.drawable.shape_spinner) }
        } else {
            binding.spinnerError.text = getString(R.string.this_feild_canted_be_empty)
            binding.spinnerError.visibility = View.VISIBLE
            binding.userRoleSpinner.background =
                context?.let { getDrawable(it, R.drawable.shape_spinner_error) }
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
        val request = object : StringRequest(
            Method.POST, CREATE_USER_URL,
            Response.Listener { response ->
                val jsonObject = JSONObject(response)
                val success = jsonObject.getBoolean(SUCCESS)

                if (success) {
                    Toast.makeText(
                        context,
                        getString(R.string.user_added_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                val statusCode = networkResponse.statusCode
                val jsonObject = JSONObject(String(networkResponse.data))
                if (statusCode == 401) {
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
                            requireContext().startActivity(
                                Intent(
                                    context,
                                    LoginActivity::class.java
                                )
                            )
                            requireActivity().finish()
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
                    requireActivity().getSharedPreferences(AUTH, Context.MODE_PRIVATE)
                params[ACCESS_TOKEN] = sharedPreferences.getString(ACCESS_TOKEN, "").toString()
                params[FULL_NAME] = binding.fullNameInput.text.toString()
                params[ID_CARD_NUMBER] = binding.identityCardNumberInput.text.toString()
                params[DATE_OF_BIRTH] = binding.dateOfBirthInput.text.toString()
                params[EMAIL] = binding.emailInput.text.toString()
                params[PHONE] = binding.phoneNumberInput.text.toString()
                params[PASSWORD] = binding.passwordInput.text.toString()
                params[GENDER] = if (binding.maleRadioButton.isChecked) "1" else "0"
                params[USER_TYPE] = binding.userRoleSpinner.selectedItemPosition.toString()
                if (binding.specialtyLayout.isVisible)
                    params[SPECIALTY] = binding.specialtyInput.text.toString()

                return params
            }
        }
        Volley.newRequestQueue(activity).add(request)

    }
}
