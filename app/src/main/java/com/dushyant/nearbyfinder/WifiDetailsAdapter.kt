package com.dushyant.nearbyfinder

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiDetailsAdapter(private val wifiDetails: ArrayList<ScanResult>): RecyclerView.Adapter<WifiDetailsAdapter.CustomViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WifiDetailsAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_selected_wifi_details_layout, parent, false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: WifiDetailsAdapter.CustomViewHolder, position: Int) {
        val details = "BSSID: ${wifiDetails[position].BSSID} \n" +
                "SSID: ${wifiDetails[position].SSID} \n" +
                "Capabilities: ${wifiDetails[position].capabilities} \n" +
                "CenterFreq0: ${wifiDetails[position].centerFreq0} \n" +
                "CenterFreq1: ${wifiDetails[position].centerFreq1} \n" +
                "ChannelWidth: ${wifiDetails[position].channelWidth} \n" +
                "Frequency: ${wifiDetails[position].frequency} \n" +
                "Level: ${wifiDetails[position].level} \n" +
                "Timestamp: ${wifiDetails[position].timestamp}"
        holder.tvDetails.text = details
    }

    override fun getItemCount(): Int = wifiDetails.size

    inner class CustomViewHolder(v: View): RecyclerView.ViewHolder(v){
        val tvDetails: TextView = v.findViewById(R.id.tv_wifi_details)
    }
}