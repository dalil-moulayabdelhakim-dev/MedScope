package com.dldroid.medscope.database

private const val HOST = "http://192.168.8.100:8000"
private const val URL = "$HOST/api"

// urls -----------------------------
const val CHECK_ACCESS_TOKEN = "$URL/auth/check_access_token"
const val LOGIN_URL = "$URL/user/login"
const val LOGOUT_URL = "$URL/user/logout"
const val CHECK_CONNECTION_URL = "$URL/connection/check"
const val USER_PROFILE_URL = "$URL/user/profile"
const val USER_ID_CARD_NUMBER_URL = "$URL/user/id_card_number"
const val GET_ALL_BY_TYPE_URL = "$URL/user/get_all_by_type"
const val GET_ALL_USER_TYPES_URL = "$URL/user/get_all_types"
const val CREATE_USER_URL = "$URL/admin/create_user"
const val GET_USER_BY_ID_URL = "$URL/admin/get_user_by_id"
const val UPDATE_USER_URL = "$URL/admin/update_user"
const val REMOVE_USER_URL = "$URL/admin/remove_user"
const val GET_ALL_APPOINTMENT_URL = "$URL/appointment/get_all"
const val GET_DOCTORS_BY_SPECIALTY_URL = "$URL/doctor/specialty/get_all"
const val GET_DOCTOR_BY_ID_URL = "$URL/doctor/id/get"
const val CREATE_APPOINTMENT_URL = "$URL/appointment/create"
const val ACCEPT_APPOINTMENT_URL = "$URL/appointment/accept"
const val CANCEL_APPOINTMENT_URL = "$URL/appointment/cancel"
const val GET_PATIENT_URL = "$URL/patient/get"
const val GET_ALL_APPOINTMENTS_BY_PATIENT = "$URL/appointment/patient/get_all"
const val GET_FILES_URL = "$URL/medication/patient/get_all"
const val GET_APPOINTMENT_BY_ID_FOR_PATIENT_URL = "$URL/appointment/id/patient/get"
const val DOCTOR_DATA_URL = "$URL/doctor/data"

const val DOWNLOAD_FILES: String = "$HOST/storage/"

//keys -----------------------------
const val SUCCESS = "success"
const val ERROR = "error"
const val ID = "id"
const val ACCESS_TOKEN = "access_token"
const val USER_TYPE = "user_type"

/* 1: patient
 * 2: doctor
 * 3: admin
 * 4: laboratorian
 * 5: receptionists */

const val AUTH = "auth"
const val FULL_NAME = "full_name"
const val ID_CARD_NUMBER = "identity_card_number"
const val GENDER = "gender"
const val PHONE = "phone"
const val SPECIALTY = "specialty"
const val DATA = "data"
const val DATE_OF_BIRTH = "date_of_birth"
const val EMAIL = "email"
const val PASSWORD = "password"
const val MALE = "male"
const val FEMALE = "female"
const val USER = "user"
const val USERS = "users"
const val ACTIVE = "active"
const val UPDATED_AT = "updated_at"
const val ALL_USER_TYPES = "all_user_types"
const val NAME = "name"
const val TITLE = "title"
const val MESSAGE = "message"
const val ERRORS = "errors"
const val DOCTOR_ID = "doctor_id"
const val PATIENT_ID = "patient_id"
const val DATE = "date"
const val TIME = "time"
const val PATIENT_NAME = "patient_name"
const val DOCTOR_NAME = "doctor_name"
const val DOCTORS = "doctors"
const val DOCTOR = "doctor"
const val LOCATION = "location"
const val REASON = "reason"
const val STATUS = "status"
const val SPECIALTY_ID = "specialty_id"
const val RECEPTOR_ID = "receptor_id"
const val APPOINTMENTS = "appointments"
const val APPOINTMENT = "appointment"
const val WITH = "with"
const val CURRENT_MEDICATION = "current_medication"
const val MEDICAL_HISTORY = "medical_history"
const val PDF_CHANNEL_ID = 123
const val MEDICATIONS = "medications"
const val URLL = "url"
const val RECEPTOR_NAME = "receptor_name"
const val DEVICE_MODEL = "device_model"
const val RECEPTOR_PHONE = "receptor_phone"
const val RECEPTOR_EMAIL = "receptor_email"
const val RECEPTOR_SPECIALTY = "receptor_specialty"
const val PARIENT_NUMBER = "patient_number"
const val APPOINTMENT_NUMBER = "appointment_number"

//system key -----------------------------
const val PERMISSION_REQUEST = 1001
const val PASSWORD_LENGTH = 8

