package com.dldroid.medscope.ui.fragment.home.admin.manage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.ItemRecyclerAdminHelperBinding
import com.dldroid.medscope.manager.RecyclerInterface
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

class AdminHelperRecyclerAdapter(
    val context: Context?,
    private val usersList: ArrayList<AdminHelperRecyclerItem>,
    private val adminHelperRecyclerInterface: RecyclerInterface
) : RecyclerView.Adapter<AdminHelperRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_recycler_admin_helper, parent, false)
        return AdminHelperRecyclerAdapter.ViewHolder(
            view,
            adminHelperRecyclerInterface
        )
    }

    fun sort(comparator: Comparator<AdminHelperRecyclerItem>) {
        Collections.sort(usersList, comparator)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = usersList[position].getUserName()
        holder.binding.lastSeen.text = usersList[position].getLastSeen()
        holder.binding.onlign.setImageResource(usersList[position].getOnline())
    }

    class ViewHolder(itemView: View, adminHelperRecyclerInterface: RecyclerInterface) :
        RecyclerView.ViewHolder(itemView) {
            private var _binding : ItemRecyclerAdminHelperBinding? = null
        val binding get() = _binding!!

        init {
            _binding = ItemRecyclerAdminHelperBinding.bind(itemView)
            binding.name.isSelected = true
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    adminHelperRecyclerInterface.onItemClick(position)
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    adminHelperRecyclerInterface.onItemHold(position)
                }
                true
            }
        }

    }
}