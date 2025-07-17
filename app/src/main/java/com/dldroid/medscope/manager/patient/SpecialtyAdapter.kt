package com.dldroid.medscope.manager.patient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dldroid.medscope.R
import com.dldroid.medscope.manager.RecyclerInterface

class SpecialtyAdapter(private val context: Context,
    private val specialtyModels: ArrayList<SpecialtyModel>,
    private val recyclerInterface: RecyclerInterface):
    RecyclerView.Adapter<SpecialtyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.item_specialty, parent, false)
        return ViewHolder(v, recyclerInterface)
    }

    override fun getItemCount(): Int {
        return specialtyModels.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = specialtyModels[position].getName()
        holder.image.setImageResource(specialtyModels[position].getImage())
    }

    class ViewHolder(itemView: View, recyclerterface: RecyclerInterface) :
        RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById<ImageView>(R.id.spImage)
        var text: TextView = itemView.findViewById<TextView>(R.id.spText)

        init {
            text.isSelected = true
            itemView.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    recyclerterface.onItemClick(pos)
                }
            }
        }
    }
}