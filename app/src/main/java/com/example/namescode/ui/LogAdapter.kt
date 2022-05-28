package com.example.namescode.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.namescode.R
import com.example.namescode.model.Log

class LogAdapter : RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    private val tmpList = arrayListOf<Log>()

    fun setData(logList:ArrayList<Log>){
        tmpList.clear()
        tmpList.addAll(logList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var log: Log

        private val logText = itemView.findViewById<TextView>(R.id.logTV)
        fun bind(log: Log) {
            this.log = log
            logText.text = log.log
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_lv, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tmpList[position])
    }

    override fun getItemCount() = tmpList.size
}