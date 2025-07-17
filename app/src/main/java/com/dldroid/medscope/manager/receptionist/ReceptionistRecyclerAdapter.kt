package com.dldroid.medscope.manager.receptionist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.ItemReceptionistBinding
import com.dldroid.medscope.manager.RecyclerInterface
import java.util.Collections

class ReceptionistRecyclerAdapter(private val context: Context,
    private val recyclerModels: ArrayList<ReceptionistRecyclerModel>,
    private val recyclerInterface: RecyclerInterface) : RecyclerView.Adapter<ReceptionistRecyclerAdapter.ViewHolder>() {

    fun sort(comparator: Comparator<ReceptionistRecyclerModel>?) {
        Collections.sort(recyclerModels, comparator)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_receptionist, parent, false)
        return ViewHolder(view, recyclerInterface)
    }

    override fun getItemCount(): Int {
        return recyclerModels.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.id.text = recyclerModels[position].getNumber()
        holder.binding.name.text = recyclerModels[position].getName()
    }


    class ViewHolder(itemView: View, recyclerInterface: RecyclerInterface) : RecyclerView.ViewHolder(itemView) {
        private var _binding : ItemReceptionistBinding? = null
        val binding get() = _binding!!

        init {
            _binding = ItemReceptionistBinding.bind(itemView)
            binding.name.setSelected(true)
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