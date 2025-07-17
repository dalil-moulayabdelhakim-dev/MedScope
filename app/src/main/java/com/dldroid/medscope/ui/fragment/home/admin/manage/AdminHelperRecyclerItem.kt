package com.dldroid.medscope.ui.fragment.home.admin.manage

class AdminHelperRecyclerItem(private var id: String?, private var userName: String?,
                              private var lastSeen: String?, private var online: Int
) {

    companion object{
        val BY_TITLE_ASCENDING: java.util.Comparator<AdminHelperRecyclerItem> =
            Comparator { model1, model2 ->
                model2.getUserName()?.let { model1.getUserName()?.compareTo(it, ignoreCase = true) }!!
            }
    }

    fun getId(): String? {
        return id
    }

    fun getUserName(): String? {
        return userName
    }

    fun getLastSeen(): String? {
        return lastSeen
    }

    fun getOnline(): Int {
        return online
    }
}