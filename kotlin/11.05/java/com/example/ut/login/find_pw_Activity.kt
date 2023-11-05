package com.example.ut.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.ut.R
import com.example.ut.a_data.findpw
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class find_pw_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pw)

        val findpw = findViewById<Button>(R.id.findpw)
        val findid = findViewById<TextView>(R.id.findid)

        findid.setOnClickListener(View.OnClickListener{
            val intent = Intent(this, find_id_Activity::class.java)
            startActivity(intent)
        })

        val IP = IP()

        findpw.setOnClickListener(View.OnClickListener{
            val userid = findViewById<TextView>(R.id.userid).text.toString()
            val username = findViewById<TextView>(R.id.username).text.toString()
            val email = findViewById<TextView>(R.id.email).text.toString()

            if (userid.equals("")){
                showAlertDialog("아이디를 입력하세요.")
            } else if(email.equals("")) {
                showAlertDialog("이메일을 입력하세요.")
            }else if(email.equals("")) {
                showAlertDialog("이메일을 입력하세요.")
            } else {
                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val apiService =  retrofit.create(ApiServer::class.java)

                val findpw = findpw(userid,username, email)

                val call = apiService.findpw(findpw)

                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            // 서버 응답 처리
                            if(responseBody.equals("0")){
                                showAlertDialog("입력한 정보에 맞는 정보가 없습니다.")
                                //findViewById<TextView>(R.id.userid).text = null // userid textview 빈칸만들기
                                //findViewById<TextView>(R.id.username).text = null // username textview 빈칸만들기
                                //findViewById<TextView>(R.id.email).text = null // email textview 빈칸만들기
                            }else{
                                if(responseBody!=null){
                                    showAlertDialog("임시 비밀번호를 이메일로 전송했습니다.",true)
                                }
                            }
                        } else {
                            // 서버 요청 실패
                            showAlertDialog("서버 요청에 실패하였습니다.")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        // 네트워크 오류 처리
                        showAlertDialog("서버 연결에 실패하였습니다.")
                    }
                })
            }
        })
    }
    fun showAlertDialog(text:String, responseBody:Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        builder.setCancelable(false)

        builder.setPositiveButton("확인") { _, _ ->
            if(responseBody) {
                intent()
            }
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }
    fun intent(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    override fun onBackPressed() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}