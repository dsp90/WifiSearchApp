package com.dushyant.nearbyfinder

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiListAdapter(
    private val wifiNames: List<ScanResult>,
    private val listener: ISelectedItem
) :
    RecyclerView.Adapter<WifiListAdapter.CustomViewHolder>() {

    private val selectedItems: MutableList<ScanResult> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_layout_wifi_names, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        if (wifiNames[position].SSID.isNotEmpty()) {
            holder.textWifiName.text = wifiNames[position].SSID
        } else {
            holder.textWifiName.text = "Unknown"
        }
        holder.checkBoxWifi.isChecked = false
        holder.checkBoxWifi.setOnCheckedChangeListener { _, b ->
            if (b) {
                selectedItems.add(wifiNames[position])
                listener.getSelectedItem(selectedItems)
            } else {
                selectedItems.remove(wifiNames[position])
                listener.getSelectedItem(selectedItems)
            }
        }
    }

    override fun getItemCount(): Int {
        return wifiNames.size
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var view: View = itemView

        val textWifiName: TextView = view.findViewById(R.id.tv_wifi_name)
        val checkBoxWifi: CheckBox = view.findViewById(R.id.chk_wifi)
    }
}