package com.dldroid.medscope.manager.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.dldroid.medscope.R

class AdviceAdapter(private var adviceModels: List<AdviceModel>?, viewPager2: ViewPager2?) : RecyclerView.Adapter<AdviceAdapter.SliderViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return SliderViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_advice, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.setImageView(adviceModels!![position])
    }

    override fun getItemCount(): Int {
        return adviceModels!!.size
    }

    class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private val textView: TextView = itemView.findViewById(R.id.text)

        fun setImageView(itemModel: AdviceModel) {
            imageView.setImageResource(itemModel.getImage())
            textView.text = itemModel.getText()
        }
    }
}