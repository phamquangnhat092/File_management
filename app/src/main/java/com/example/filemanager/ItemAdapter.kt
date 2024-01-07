package com.example.filemanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.io.File

class ItemAdapter(var items: Array<File>):BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemView: View
        var itemViewHolder: ItemViewHolder
        val selectedFile = items[position]
        if (convertView == null) {
            itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.recycler_item, parent, false)

            itemViewHolder = ItemViewHolder(itemView)
            itemView.tag = itemViewHolder
        } else {
            itemView = convertView
            itemViewHolder = itemView.tag as ItemViewHolder
        }
        itemViewHolder.textView.text = selectedFile.name
        if (selectedFile.isDirectory) {
            itemViewHolder.imageView.setImageResource(R.drawable.ic_baseline_folder_24)
        } else {
            itemViewHolder.imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24)
        }


        return itemView
    }

    inner class ItemViewHolder(itemView: View) {

        val imageView= itemView.findViewById<ImageView>(R.id.icon_view)
        val textView= itemView.findViewById<TextView>(R.id.file_name_text_view)




    }
}