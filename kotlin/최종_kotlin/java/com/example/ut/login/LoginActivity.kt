package com.example.ut.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.login_member_data
import com.example.ut.board.search_board
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {
    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // 액티비티 설정

        val pm = this.packageManager.getPackageInfo(this.packageName,0)
        findViewById<TextView>(R.id.version).text = pm.versionName

        val Sign_up_button = findViewById<TextView>(R.id.Sign_up) // 설정한 액티비티에서 id가 Sign_up인 <버튼>을 변수로 선언
        val findid = findViewById<TextView>(R.id.findid)
        val findpw = findViewById<TextView>(R.id.findpw)

        val sent_Login_info = findViewById<Button>(R.id.Login)

        preferences = getSharedPreferences("session", MODE_PRIVATE) // "session"이라는 이름의 SharedPreferences 파일을 가져옴(없으면 자동으로 생성)
        editor = preferences?.edit()

        var userid = preferences?.getString("userid", "") //쿠키 안에 있는 userid의 문자열을 가져옴, 없을 시 빈 칸
        var username = preferences?.getString("username", "")
        val userid0 = preferences?.getString("0userid", "")

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        if(userid.equals("")||username.equals("")){
            editor?.clear()?.apply()
        }

        if (!userid.equals("")) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }else if(!userid0.equals("")){
            val intent = Intent(this, email_cheack::class.java)
            startActivity(intent)
        }

        if (!isInternetConnected(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("알림")
            builder.setMessage("네트워크 연결에 실패하였습니다.")

            builder.setCancelable(false)

            builder.setPositiveButton("확인") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            val dialog = builder.create()
            dialog.show()
        }

        Sign_up_button.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Sing_up_Activity::class.java)
            startActivity(intent)
        })

        findid.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, show_find::class.java)
            intent.putExtra("find","id")
            startActivity(intent)
        })

        findpw.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, show_find::class.java)
            startActivity(intent)
        })

        val user_pw = findViewById<EditText>(R.id.User_pw)

        user_pw.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE  ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                // 여기에 "완료" 버튼을 눌렀을 때 실행할 코드를 작성
                userid = findViewById<TextView>(R.id.User_id).text.toString()
                val userpw = user_pw.text.toString()
                login(userid!!, userpw)

                return@setOnEditorActionListener true
            }
            false
        }

        sent_Login_info.setOnClickListener(View.OnClickListener { //버튼 누를 경우
            userid = findViewById<TextView>(R.id.User_id).text.toString()
            val userpw = findViewById<TextView>(R.id.User_pw).text.toString()
            login(userid!!, userpw)
        })
    }

    fun login(userid:String, userpw:String){
        val IP = IP()

        if (userid.equals("")){
            showAlertDialog("아이디를 입력하세요.")
        } else if(userpw.equals("")) {
            showAlertDialog("비밀번호를 입력하세요.")
        } else {
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService =  retrofit.create(ApiServer::class.java)

            val login_data = login_member_data(
                findViewById<TextView>(R.id.User_id).text.toString().trim(),
                findViewById<TextView>(R.id.User_pw).text.toString().trim()
            )

            val call = apiService.getlogin(login_data)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        // 서버 응답 처리
                        if(responseBody.equals("0")){
                            showAlertDialog("아이디 또는 비밀번호를 잘못 입력했습니다.")
                        }else if(responseBody.equals("auth")) {
                            editor?.putString("0userid", findViewById<TextView>(R.id.User_id).text.toString().trim())
                            editor?.apply()

                            intent(1)
                        }else{
                            editor?.putString("userid", findViewById<TextView>(R.id.User_id).text.toString().trim())
                            editor?.putString("username", responseBody)
                            editor?.apply()

                            intent()
                        }
                    } else {
                        // 서버 요청 실패
                        showAlertDialog("서버 요청에 실패하였습니다.")
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    // 네트워크 오류 처리
                    showAlertDialog("서버 연결에 실패하였습니다.")
                    println(t.message)
                }
            })
        }
    }

    fun showAlertDialog(text:String) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    fun intent(int:Int=0){
        if(int==0){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else{
            val intent = Intent(this, email_cheack::class.java)
            startActivity(intent)
        }
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

    private fun requestPermissions() { //권한 확인하는 함수
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

        if (permissionsToRequest.isNotEmpty()) { //요청!!
            // 권한이 필요한 경우에만 권한 요청을 시작
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("앱 종료")
        builder.setMessage("앱을 종료하시겠습니까?")

        builder.setPositiveButton("종료") { _, _ ->
            finishAffinity() // 앱 종료
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss() // 다이얼로그 닫기
        }

        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}