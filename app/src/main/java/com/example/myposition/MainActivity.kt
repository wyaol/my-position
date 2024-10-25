package com.example.myposition

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private lateinit var ipEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var locationTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var locationTracker: LocationTracker
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // 初始化视图和 SharedPreferences
        ipEditText = findViewById(R.id.ipEditText)
        portEditText = findViewById(R.id.portEditText)
        saveButton = findViewById(R.id.saveButton)
        locationTextView = findViewById(R.id.locationTextView)
        sharedPreferences = getSharedPreferences("ServerConfig", Context.MODE_PRIVATE)

        // 从 SharedPreferences 获取 IP 和端口，并设置到输入框
        ipEditText.setText(sharedPreferences.getString("server_ip", ""))
        portEditText.setText(sharedPreferences.getString("server_port", ""))

        // 点击保存按钮时保存 IP 和端口到 SharedPreferences
        saveButton.setOnClickListener {
            val ip = ipEditText.text.toString()
            val port = portEditText.text.toString()

            // 保存 IP 和端口
            sharedPreferences.edit().apply {
                putString("server_ip", ip)
                putString("server_port", port)
                apply()
            }

            Toast.makeText(this, "Server IP and Port Saved", Toast.LENGTH_SHORT).show()
        }

        requestAccess()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
//            locationTracker = LocationTracker(this) { location ->
//                updateUI(location)
//            }
//            locationTracker.startLocationUpdates()
        } else {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(intent);
        }

        // 启动前台服务
        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun requestAccess() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
        if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE)
            }
        }
    }

    // 更新 UI 上的位置
    @SuppressLint("SetTextI18n")
    private fun updateUI(location: Location) {
        locationTextView.text = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
