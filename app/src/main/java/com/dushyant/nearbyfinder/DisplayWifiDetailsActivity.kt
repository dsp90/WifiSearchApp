package com.dushyant.nearbyfinder

import android.net.wifi.ScanResult
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dushyant.nearbyfinder.databinding.ActivityWifiDetailsBinding

class DisplayWifiDetailsActivity: AppCompatActivity() {

    private lateinit var binding:  ActivityWifiDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding = ActivityWifiDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val wifiDetails = this.intent.getParcelableArrayListExtra<Parcelable>("WIFI_DET")
        val foo: ArrayList<ScanResult> = wifiDetails as ArrayList<ScanResult>
        val adapter = WifiDetailsAdapter(foo)
        binding.rvWifiDetails.adapter = adapter
        binding.rvWifiDetails.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}