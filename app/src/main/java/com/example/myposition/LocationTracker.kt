package com.example.myposition

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationTracker(
    private val context: Context,
    private val locationUpdateCallback: (Location) -> Unit,
) {
    private lateinit var sharedPreferences: SharedPreferences

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isTracking = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            // 当位置更新时调用
            locationUpdateCallback(location)
            updateLocationToServer(location)
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(context, "$provider 已启用", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(context, "$provider 已禁用", Toast.LENGTH_SHORT).show()
        }
    }

    fun startLocationUpdates() {
        sharedPreferences = context.getSharedPreferences("ServerConfig", Context.MODE_PRIVATE)
        val crt = Criteria()
        crt.horizontalAccuracy = Criteria.ACCURACY_HIGH // 水平精度高
        crt.isAltitudeRequired = true // 需要高度
        val provider = locationManager.getBestProvider(crt, true)

        // 请求位置更新
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "位置权限未打开", Toast.LENGTH_SHORT).show()
            return
        }

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 5000, 5.0f, locationListener)
        } else {
            Toast.makeText(context, "请求定位失败", Toast.LENGTH_SHORT).show()
        }
        isTracking = true
    }

    fun stopLocationUpdates() {
        if (isTracking) {
            locationManager.removeUpdates(locationListener)
            isTracking = false
        }
    }

    // 更新位置信息到服务器
    private fun updateLocationToServer(location: Location) {
        val serverUrl = getServerUrl()

        val retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(LocationApiService::class.java)

        val locationData = LocationData(location.latitude, location.longitude)
        service.uploadLocation(locationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // 上传成功
//                Toast.makeText(context, "Location Uploaded", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 上传失败
                Toast.makeText(context, "Failed to Upload Location", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    // 获取服务器 URL
    private fun getServerUrl(): String {
        val ip = sharedPreferences.getString("server_ip", "127.0.0.1") ?: ""
        val port = sharedPreferences.getString("server_port", "8081") ?: ""
        return "http://$ip:$port/"
    }
}
