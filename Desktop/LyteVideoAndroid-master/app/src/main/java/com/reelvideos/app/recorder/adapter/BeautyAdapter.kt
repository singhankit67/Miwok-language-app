
package com.reelvideos.app.recorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reelvideos.app.R

class BeautyAdapter(val callback: (position: Int) -> Unit) : RecyclerView.Adapter<BeautyAdapter.ViewHolder>() {

  private val mImages = arrayOf(R.mipmap.ic_smoother)
  private val mTexts = arrayOf(R.string.smoother)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_beauty, parent, false)
    return ViewHolder(view)
  }

  override fun getItemCount(): Int {
    return mImages.size
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.imageView.setImageResource(mImages[position])
    holder.textView.text = holder.itemView.resources.getText(mTexts[position])
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.item_beauty_image)
    val textView: TextView = itemView.findViewById(R.id.item_beauty_text)
  }
}