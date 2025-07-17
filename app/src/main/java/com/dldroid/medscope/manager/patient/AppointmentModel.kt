package com.dldroid.medscope.manager.patient

class AppointmentModel(
    private val name: String,
    private val dateTime: String,
    private val with: String,
    private val color: Int
) {

    fun getName(): String {
        return name
    }

    fun getDateTime(): String {
        return dateTime
    }

    fun getWith(): String {
        return with
    }

    fun getColor(): Int {
        return color
    }
}