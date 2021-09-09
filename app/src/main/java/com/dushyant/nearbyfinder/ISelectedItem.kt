package com.dushyant.nearbyfinder

import android.net.wifi.ScanResult

interface ISelectedItem {
    fun getSelectedItem(items: List<ScanResult>)
}