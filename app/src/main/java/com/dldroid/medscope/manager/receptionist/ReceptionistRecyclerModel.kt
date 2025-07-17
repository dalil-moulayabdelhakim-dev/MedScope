package com.dldroid.medscope.manager.receptionist

class ReceptionistRecyclerModel(private val name: String?, private val number: String?, private val id: String?) {

    companion object{
        val BY_TITLE_ASCENDING: Comparator<ReceptionistRecyclerModel> =
            Comparator { model1, model2 ->
                model2.getName()?.let { model1.getName()?.compareTo(it, ignoreCase = true) }!!
            }
    }

    fun getNumber(): String? {
        return number
    }

    fun getName(): String? {
        return name
    }

    fun getId(): String? {
        return id
    }
}