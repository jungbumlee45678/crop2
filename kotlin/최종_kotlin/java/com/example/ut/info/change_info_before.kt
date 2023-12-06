package com.example.ut.info

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.change
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class change_info_before : AppCompatActivity() {
    fun isValidPw(pw: String): Boolean {
        val regex = Regex("^(?=.*[a-zA-Z])(?=.*[^가-힣])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{6,12}$")
        return regex.matches(pw)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_info_before)

        val IP = IP()

        val change_address_button = findViewById<Button>(R.id.change_pw_button)
        change_address_button.setOnClickListener{
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService =  retrofit.create(ApiServer::class.java)



            val preferences = getSharedPreferences("session", MODE_PRIVATE)
            val UserID = preferences.getString("userid","").toString()

            val data = change(
                UserID,
                findViewById<TextView>(R.id.change_pw_old).text.toString(),
                findViewById<TextView>(R.id.change_pw).text.toString()
                )
            
            val oldpw = findViewById<TextView>(R.id.change_pw_old).text.toString()
            val changepw = findViewById<TextView>(R.id.change_pw).text.toString()
            val changepwr = findViewById<TextView>(R.id.change_pw_r).text.toString()

            val call = apiService.ch_pw(data)

            if(oldpw.equals("")) {
                showAlertDialog("현재 비밀번호를 입력해주세요.")
            } else if(!isValidPw(changepw)) {
                showAlertDialog("새 비밀번호를 형식에 맞게 입력해주세요.")
            } else if(!changepw.equals(changepwr)) {
                showAlertDialog("새 비밀번호가 일치하지 않습니다.")
            } else {
                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            // 서버 응답 처리
                            println(responseBody)
                            if(responseBody.equals("nothing")){
                                showAlertDialog("비밀번호를 잘못 입력했습니다.")
                            }else{
                                showAlertDialog("비밀번호 변경 완료.",true)
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
    }
    fun showAlertDialog(text:String, boolean: Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        if(boolean) {
            builder.setCancelable(false) //다이얼로그 밖 클릭해도 안꺼짐

            builder.setPositiveButton("확인") { _, _ ->
                Intent()
            }
        }
        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }
    fun Intent(){
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}