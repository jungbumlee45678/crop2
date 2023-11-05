package com.example.ut.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.R
import com.example.ut.a_data.member_data
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)

        val IP = IP()

        val preferences = getSharedPreferences("session", MODE_PRIVATE) // "session"이라는 이름의 SharedPreferences 파일을 가져옴(없으면 자동으로 생성)
        val editor = preferences.edit()

        val sent_SignUp_info = findViewById<Button>(R.id.SingUp_Button)
        sent_SignUp_info.setOnClickListener(View.OnClickListener { //버튼 누를 경우
            val gson = GsonBuilder().setLenient().create()// 사용해서 [json 검사 통과] -> 일반적인 데이터도 통신 가능
            val retrofit = Retrofit.Builder()//[json 형식]으로 데이터 검사
                .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService =  retrofit.create(ApiServer::class.java)

            val userid = findViewById<TextView>(R.id.SignUp_id).text.toString()
            val userpw = findViewById<TextView>(R.id.SignUp_pw).text.toString()
            val userpwr = findViewById<TextView>(R.id.SignUp_pw_r).text.toString()
            val email = findViewById<TextView>(R.id.SignUp_Email).text.toString()
            val address = findViewById<TextView>(R.id.Address).text.toString()
            val username = findViewById<TextView>(R.id.SignUp_NiKname).text.toString()

            val memberData = member_data(userid, userpw, username, email, address)

            val call = apiService.getsignup(memberData)

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
                                showAlertDialog("회원가입 성공했습니다.",true)
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


        })
    }
    fun showAlertDialog(text:String,bool:Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        builder.setPositiveButton("확인") { _, _ ->
            if(bool) {
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
}

