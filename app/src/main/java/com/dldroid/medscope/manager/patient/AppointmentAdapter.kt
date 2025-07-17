package com.dldroid.medscope.manager.patient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dldroid.medscope.databinding.ItemAppointmentBinding
import com.dldroid.medscope.manager.RecyclerInterface
import java.util.Collections

class AppointmentAdapter(
    private val context: Context,
    private val appointmentModels: ArrayList<AppointmentModel>,
    private val recyclerInterface: RecyclerInterface
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    fun sort(comparator: Comparator<AppointmentModel>?) {
        Collections.sort(appointmentModels, comparator)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemAppointmentBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view.root, recyclerInterface)

    }

    override fun getItemCount(): Int {
        return appointmentModels.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = appointmentModels[position].getName()
        holder.binding.dateTime.text = appointmentModels[position].getDateTime()
        holder.binding.witht.visibility = View.GONE
        if (appointmentModels[position].getWith().isNotEmpty()){
            holder.binding.witht.visibility = View.VISIBLE
            holder.binding.witht.text = appointmentModels[position].getWith()
        }
        holder.binding.status.setBackgroundColor(context.resources.getColor(appointmentModels[position].getColor()))
    }


    class ViewHolder(itemView: View, recyclerInterface: RecyclerInterface) :
        RecyclerView.ViewHolder(itemView) {
        var _binding: ItemAppointmentBinding = ItemAppointmentBinding.bind(itemView)
        val binding get() = _binding

        init {
            binding.name.isSelected = true
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    recyclerInterface.onItemClick(position)
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    recyclerInterface.onItemHold(position)
                }
                true
            }
        }
    }

}