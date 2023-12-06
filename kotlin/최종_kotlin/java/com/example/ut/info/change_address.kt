package com.example.ut.info

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ut.R
import com.example.ut.a_data.change
import com.example.ut.a_data.change_address
import com.example.ut.login.Sing_up_Activity
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Locale

class change_address : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var latitude = -1.0
    var longitude = -1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_address)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val address = intent.getStringExtra("address")
        findViewById<TextView>(R.id.change_address_info).text = address

        val address_find = findViewById<TextView>(R.id.change_address_find)
        address_find.setOnClickListener {
            if(checkLocationPermission()){
                requestLocation()
            } else{
                requestLocationPermission()
            }
        }

        val change_name_button = findViewById<Button>(R.id.change_address)
        change_name_button.setOnClickListener {
            if(latitude!=-1.0){
                val IP = IP()
                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val apiService = retrofit.create(ApiServer::class.java)

                val preferences = getSharedPreferences("session", MODE_PRIVATE)
                val userid = preferences.getString("userid", "").toString()
                val cheak_pw = findViewById<TextView>(R.id.cheak_pw).text.toString()
                val change_address_info = findViewById<TextView>(R.id.change_address_info).text.toString()

                val data = change_address(userid, cheak_pw, change_address_info, latitude, longitude)

                val call = apiService.ch_address(data)

                if (change_address_info.equals("")) {
                    showAlertDialog("주소를 입력해주세요.")
                } else {
                    call.enqueue(object : retrofit2.Callback<String> {
                        override fun onResponse(
                            call: Call<String>,
                            response: retrofit2.Response<String>
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                println(responseBody)
                                if (responseBody.equals("nothing")) {
                                    showAlertDialog("비밀번호가 틀렸습니다.")
                                } else {
                                    showAlertDialog("주소 변경 완료", true)
                                }
                            } else {
                                // 서버 요청 실패
                                showAlertDialog("서버 요청에 실패하였습니다.")
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            // 네트워크 오류 처리
                            showAlertDialog("서버 연결에 실패하였습니다.")
                            println(t)
                        }
                    })
                }
            } else{
                showAlertDialog("같은 주소 입니다.")
            }
        }

    }

    fun showAlertDialog(text: String, boolean: Boolean = false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        builder.setCancelable(false) //다이얼로그 밖 클릭해도 안꺼짐

        builder.setPositiveButton("확인") { _, _ ->
            if (boolean) {
                val intent = Intent(this, my_info::class.java)
                startActivity(intent)
            }
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun requestLocation() {
        // 위치 정보 요청
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // 위치 정보를 성공적으로 가져온 경우
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        // 여기에서 위도와 경도를 사용할 수 있습니다.

                        getAddressFromLocation(latitude, longitude)
                    }
                }
                .addOnFailureListener { e ->
                    // 위치 정보를 가져오는 데 실패한 경우
                    // 실패 처리를 수행할 수 있습니다.
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            // 권한이 거부된 경우 처리를 수행할 수 있습니다.
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_LOCATION -> {
                // 사용자가 권한 부여를 허용한 경우 위치 정보 요청
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation()
                } else {
                    // 사용자가 권한 부여를 거부한 경우 처리를 수행할 수 있습니다.
                    showPermissionSettingsDialog()
                }
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.KOREA)
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address: Address = addresses[0]

                    val Address = findViewById<TextView>(R.id.change_address_info)
                    Address.text = address.getAddressLine(0).substring(5)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 주소를 가져오는 동안 오류가 발생한 경우 처리를 수행할 수 있습니다.
        }
    }

    private fun showPermissionSettingsDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("권한 설정")
        builder.setMessage("앱을 사용하려면 권한이 필요합니다. 설정으로 이동하여 권한을 활성화하세요.")
        builder.setPositiveButton("설정으로 이동") { _, _ ->
            // 시스템 설정으로 이동
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 100
    }
}