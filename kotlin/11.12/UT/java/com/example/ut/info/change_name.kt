package com.example.ut.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.ut.R
import com.example.ut.a_data.change
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class change_name : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_name)

        val change_name_button = findViewById<Button>(R.id.change_name)
        change_name_button.setOnClickListener{

            val IP = IP()
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService =  retrofit.create(ApiServer::class.java)

            val preferences = getSharedPreferences("session", MODE_PRIVATE)
            val userid = preferences.getString("userid","").toString()

            val cheak_pw = findViewById<TextView>(R.id.cheak_pw).text.toString()
            val change_name_info = findViewById<TextView>(R.id.change_name_info).text.toString()

            val data = change(userid,cheak_pw,change_name_info)

            val call = apiService.ch_name(data)

            if(change_name_info.equals("")) {
                showAlertDialog("닉네임을 입력해주세요.")
            } else {
                call.enqueue(object : retrofit2.Callback<String> {
                    override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            println(responseBody)
                            if(responseBody.equals("exis")) {
                                showAlertDialog("이미 존재하는 닉네임입니다.")
                            } else if(responseBody.equals("nothing")) {
                                showAlertDialog("비밀번호가 틀렸습니다.")
                            } else {
                                showAlertDialog("닉네임 변경 완료", true)
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
        }

    }
    fun showAlertDialog(text:String,boolean: Boolean = false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        builder.setCancelable(false) //다이얼로그 밖 클릭해도 안꺼짐

        builder.setPositiveButton("확인") { _, _ ->
            if(boolean){
                val intent = Intent(this, my_info::class.java)
                startActivity(intent)
            }
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }
}