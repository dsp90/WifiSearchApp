package com.dushyant.nearbyfinder

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import com.dushyant.nearbyfinder.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val LOCATION_REQUEST_CODE = 1
private const val REQUEST_CHECK_SETTINGS = 3

class MainActivity : AppCompatActivity(), ISelectedItem {

    private lateinit var wifiManager: WifiManager
    private lateinit var binding: ActivityMainBinding

    private lateinit var selectedWifi: List<ScanResult>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        selectedWifi = ArrayList()

        checkLocationPermission()

        binding.swipeContainer.setOnRefreshListener {
            scanForWifi()
        }


        binding.btnNext.isEnabled = selectedWifi.isNotEmpty()

        binding.btnNext.setOnClickListener {
            val intent = Intent(this@MainActivity, DisplayWifiDetailsActivity::class.java)
            intent.putParcelableArrayListExtra(
                "WIFI_DET", selectedWifi as java.util.ArrayList<out Parcelable>
            )
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        selectedWifi = ArrayList()
        binding.btnNext.isEnabled = selectedWifi.isNotEmpty()
        scanForWifi()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                //
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showInContextUI()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun showInContextUI() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission needed")
            .setMessage(
                "This application needs location permission in order to search for nearest" +
                        "wifi networks"
            )
            .setPositiveButton("OK") { _, _ ->
                requestLocationPermission()
            }.create()
            .show()
    }


    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            applicationContext,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        createLocationRequest()
                    } else {
                        // show snackbar
                        Toast.makeText(
                            applicationContext, "Please allow location permission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            REQUEST_CHECK_SETTINGS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanForWifi()
                } else {
                    Toast.makeText(
                        applicationContext, "Please allow gps for searching wifi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun scanForWifi() {
        showProgressBar()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                binding.swipeContainer.isRefreshing = false
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        this.applicationContext.registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
    }

    private fun showProgressBar() {
        if (!binding.progressBarStatus.isShown) {
            binding.progressBarStatus.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        if (binding.progressBarStatus.isShown) {
            binding.progressBarStatus.visibility = View.GONE
        }
    }

    private fun toggleEmptyPlaceHolder(isVisibile: Boolean) {
        if (isVisibile) {
            hideProgressBar()
            hideRecyclerView()
            binding.emptyPlaceholder.visibility = View.VISIBLE
        } else {
            binding.emptyPlaceholder.visibility = View.GONE
        }
    }

    private fun hideRecyclerView() {
        binding.rvWifiList.visibility = View.GONE
    }

    private fun scanSuccess() {
        val results: List<ScanResult> = wifiManager.scanResults
        populateData(results)
        Log.d(TAG, "SUCCESS. scan results: $results")
    }

    private fun populateData(results: List<ScanResult>) {
        hideProgressBar()
        if (results.isNotEmpty()) {
            toggleEmptyPlaceHolder(false)
            val adapter = WifiListAdapter(results, this)
            val layoutManager = LinearLayoutManager(this)
            binding.rvWifiList.adapter = adapter
            binding.rvWifiList.layoutManager = layoutManager
        } else {
            toggleEmptyPlaceHolder(true)
        }
    }

    private fun scanFailure() {
        val results = wifiManager.scanResults
        populateData(results)
        Log.d(TAG, "FAILURE. scan results: $results")
    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            scanForWifi()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this@MainActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // ignore
                }
            }
        }
    }

    override fun getSelectedItem(items: List<ScanResult>) {
        btn_next.isEnabled = items.isNotEmpty()
        selectedWifi = items
    }
}