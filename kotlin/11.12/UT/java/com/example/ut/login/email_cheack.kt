package com.example.ut.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.R
import com.example.ut.a_data.login_member_data
import com.example.ut.info.interest_thing
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class email_cheack : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.email_cheack)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val editor = preferences.edit()

        val userid = preferences.getString("0userid","")

        val IP = IP()

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService = retrofit.create(ApiServer::class.java)

        var Data = login_member_data(userid!!,"")

        var call = apiService.email(Data)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    findViewById<TextView>(R.id.email).text = responseBody
                } else {

                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                println("Network Error "+t.message ?: "Unknown error")
            }
        })

        val button = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button4 = findViewById<Button>(R.id.button4)

        button4.setOnClickListener{
            preferences.edit().clear().apply()

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            Data = login_member_data(userid!!,findViewById<TextView>(R.id.email).text.toString())
            call = apiService.emailsend(Data)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if(responseBody!!.equals("0")){
                            button.text = "재전송"
                            showAlertDialog("메일 전송 했습니다.")
                        }
                    } else {

                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    println("Network Error "+t.message ?: "Unknown error")
                }
            })
        }
        button2.setOnClickListener {
            Data = login_member_data(userid!!,findViewById<TextView>(R.id.email).text.toString())
            call = apiService.emailch(Data)

            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if(!responseBody!!.equals("0")){
                            preferences.edit().clear().apply()

                            editor.putString("userid", userid)
                            editor.putString("username",responseBody)
                            editor.apply()

                            intent()
                        }else{
                            showAlertDialog("인증되지 않았습니다.")
                        }
                    } else {

                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    println("Network Error "+t.message ?: "Unknown error")
                }
            })
        }
    }
    fun intent(){
        val intent = Intent(this, interest_thing::class.java)
        intent.putExtra("first",1)
        startActivity(intent)
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
}