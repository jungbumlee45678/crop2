package com.example.ut.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ut.R
import com.example.ut.a_data.member_data
import com.example.ut.board.uri
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Locale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException

class Sing_up_Activity : AppCompatActivity() {
    fun isValidId(id: String): Boolean {
        val regex = Regex("^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d]{6,12}$")
        return regex.matches(id)
    }

    fun isValidPw(pw: String): Boolean {
        val regex = Regex("^(?=.*[a-zA-Z])(?=.*[^가-힣])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,12}$")
        return regex.matches(pw)
    }

    fun isValidEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return regex.matches(email)
    }
    //^[A-Za-z0-9]: 이메일 주소는 문자 또는 숫자로 시작해야 합니다.
    //(.*)([@]{1})(.{1,})(\\.)(.{1,}): @를 기준으로 이메일 주소를 나누고, 각 부분이 최소한 하나의 문자를 포함해야 합니다.

    var userid = ""
    var userpw = ""
    var userpwr = ""
    var email = ""
    var address = ""
    var username = ""

    var latitude = 0.0
    var longitude = 0.0

    private val PICK_IMAGE_REQUEST = 1
    private var image: ImageView? = null

    var imageuri = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sign_up1()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val find_address = findViewById<Button>(R.id.find_address)
        find_address.setOnClickListener {
            if(checkLocationPermission()){
                requestLocation()
            } else{
                requestLocationPermission()
            }
        }
    }

    fun sign_up1(){
        setContentView(R.layout.activity_sing_up)
        val sent_SignUp_info = findViewById<Button>(R.id.SingUp_Button)

        image = findViewById(R.id.profileImageView)
        var image_b = false

        image?.setOnClickListener {
            if (!allPermissionsGranted()) {
                requestPermissions()
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE_REQUEST)
                image_b = true

                image?.clipToOutline = true //둥글게 만들기
            }
        }

        if(userid!=""){
            findViewById<TextView>(R.id.SignUp_id).text = userid
            findViewById<TextView>(R.id.SignUp_pw).text = userpw
            findViewById<TextView>(R.id.SignUp_pw_r).text = userpwr
            findViewById<TextView>(R.id.SignUp_Email).text = email
            findViewById<TextView>(R.id.Address).text = address
            findViewById<TextView>(R.id.SignUp_NiKname).text = username
        }

        sent_SignUp_info.setOnClickListener{ //버튼 누를 경우
            userid = findViewById<TextView>(R.id.SignUp_id).text.toString()
            userpw = findViewById<TextView>(R.id.SignUp_pw).text.toString()
            userpwr = findViewById<TextView>(R.id.SignUp_pw_r).text.toString()
            email = findViewById<TextView>(R.id.SignUp_Email).text.toString()
            address = findViewById<TextView>(R.id.Address).text.toString()
            username = findViewById<TextView>(R.id.SignUp_NiKname).text.toString()

            if(userid.equals("")){
                showAlertDialog("아이디를 입력하세요.")
            }else if(userpw.equals("")){
                showAlertDialog("비밀번호를 입력하세요.")
            }
            else if(!userpw.equals(userpwr)){
                showAlertDialog("비밀번호가 일치하지 않습니다.")
            }else if(username.equals("")){
                showAlertDialog("닉네임을 입력하세요.")
            }else if(email.equals("")){
                showAlertDialog("이메일을 입력하세요.")
            }else if(address.equals("")){
                showAlertDialog("주소를 입력하세요.")
            }
            /*
            else if(!isValidId(userid)){
                showAlertDialog("아이디는 영문자,숫자를 포함한 6~12자 사이로 입력하세요.")
            }else if(!isValidPw(userpw)){
                showAlertDialog("비밀번호는 특수문자,영문자,숫자를 포함한 6~12자 사이로 입력하세요.")
            }
            */
            else if(!isValidEmail(email)){
                showAlertDialog("이메일 형식에 맞게 작성해주세요.")
            }
            else {
                val memberData = member_data(userid, userpw, username, email, address, latitude, longitude)

                println(latitude)

                val IP = IP()

                val gson = GsonBuilder().setLenient().create()// 사용해서 [json 검사 통과] -> 일반적인 데이터도 통신 가능
                val retrofit = Retrofit.Builder()//[json 형식]으로 데이터 검사
                    .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val apiService =  retrofit.create(ApiServer::class.java)

                val call = apiService.getsignup(memberData)

                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            // 서버 응답 처리
                            if(responseBody.equals("userid")){
                                showAlertDialog("존재하는 아이디입니다.")
                            }else if(responseBody.equals("username")){
                                showAlertDialog("존재하는 닉네임입니다.")
                            } else {
                                if(imageuri != ""){
                                    val uri = uri()
                                    val imageFile = File(uri.getRealPathFromURI(this@Sing_up_Activity, Uri.parse(imageuri)))
                                    val requestFile = RequestBody.create("image/*".toMediaType(), imageFile)
                                    val imageBody = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                                    apiService.profile(userid, imageBody).enqueue(object : Callback<String> {
                                        override fun onResponse(call: Call<String>, response2: Response<String>) {
                                            if (response2.isSuccessful) {
                                                val responseBody = response2.body()
                                                showAlertDialog("회원가입 성공했습니다.",true)
                                            } else {
                                                // 서버 요청 실패
                                                showAlertDialog("프로필 사진 첨부가 실패 하였습니다.",true)
                                            }
                                        }

                                        override fun onFailure(call: Call<String>, t: Throwable) {
                                            println("Network Error "+t.message ?: "Unknown error")
                                            showAlertDialog("프로필 사진 첨부가 실패 하였습니다.",true)
                                        }
                                    })
                                }else{
                                    showAlertDialog("회원가입 성공했습니다.",true)
                                }
                            }
                        } else {
                            // 서버 요청 실패
                            showAlertDialog("서버 요청에 실패하였습니다.")
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        // 네트워크 오류 처리
                        println("Network Error "+t.message ?: "Unknown error")
                        showAlertDialog("서버 연결에 실패하였습니다.")
                    }
                })
            }
        }
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

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            // 권한이 필요한 경우에만 권한 요청을 시작
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            REQUEST_CODE_PERMISSIONS->{
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        showPermissionSettingsDialog()
                    }
                }
            }

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

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.KOREA)
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address: Address = addresses[0]

                    val Address = findViewById<TextView>(R.id.Address)
                    Address.text = address.getAddressLine(0).substring(5)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // 주소를 가져오는 동안 오류가 발생한 경우 처리를 수행할 수 있습니다.
        }
    }

    fun showAlertDialog(text:String,bool:Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        if(bool){
            builder.setCancelable(false)
        }

        builder.setPositiveButton("확인") { _, _ ->
            if(bool) {
                intent()
            }
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
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

    fun intent(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("경고")
        builder.setMessage("뒤로 갈 경우 회원가입을 다시 해야합니다.")
        builder.setCancelable(false)

        builder.setPositiveButton("확인") { _, _ ->
            super.onBackPressed()
        }

        builder.setNegativeButton("취소") { _, _ ->

        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 이미지 선택 후 결과 처리
            val selectedImage = data.data
            image?.setImageURI(selectedImage)
            imageuri = selectedImage.toString()
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1
        private const val PERMISSIONS_REQUEST_LOCATION = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    }
}

