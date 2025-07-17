package com.dldroid.medscope.manager

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.ItemMedicationBinding

class MedicalRecordsAdapter(
    private val context: Context,
    private val medicationModels: ArrayList<MedicalRecordsModel>,
    private val recyclerInterface: RecyclerInterface
) : RecyclerView.Adapter<MedicalRecordsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, recyclerInterface: RecyclerInterface) :
        RecyclerView.ViewHolder(itemView) {

        private var _binding: ItemMedicationBinding = ItemMedicationBinding.bind(itemView)
        val binding get() = _binding

        init {
            binding.medicationName.setSelected(true)
            binding.downloadButton.paintFlags = binding.medicationName.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            itemView.setOnClickListener {
                val p = adapterPosition
                if (p != RecyclerView.NO_POSITION) {
                    recyclerInterface.onItemClick(p)
                }
            }

            itemView.setOnLongClickListener {
                val p = adapterPosition
                if (p != RecyclerView.NO_POSITION) {
                    recyclerInterface.onItemHold(p)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false)
        return ViewHolder(view, recyclerInterface)
    }

    override fun getItemCount(): Int {
        return medicationModels.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.medicationName.text = medicationModels[position].getName()
    }
}