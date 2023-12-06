package com.example.ut.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.ut.R
import com.example.ut.a_data.findid
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class find_id_Activity : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //여기 내용 실행
        val view = inflater.inflate(R.layout.activity_find_id, container, false) // setContentView의 프래그먼트 버전

        val findid = view.findViewById<Button>(R.id.findid)

        val IP = IP()

        findid.setOnClickListener(View.OnClickListener{
            var username = view.findViewById<TextView>(R.id.username).text.toString()
            var email = view.findViewById<TextView>(R.id.email).text.toString()

            if (username.equals("")){
                showAlertDialog("닉네임을 입력하세요.")
            } else if(email.equals("")) {
                showAlertDialog("이메일을 입력하세요.")
            } else {
                val gson = GsonBuilder().setLenient().create()
                val retrofit = Retrofit.Builder()
                    .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val apiService =  retrofit.create(ApiServer::class.java)

                val findid = findid(username, email)

                val call = apiService.findid(findid)

                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            // 서버 응답 처리
                            if(responseBody.equals("0")){
                                showAlertDialog("입력한 정보에 맞는 아이디가 없습니다.")
                                //findViewById<TextView>(R.id.username).text = null // username textview 빈칸만들기
                                //findViewById<TextView>(R.id.email).text = null // email textview 빈칸만들기
                            }else{
                                if(responseBody!=null){
                                    showAlertDialog("아이디 : "+responseBody, true)
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

        return view
    }

    fun showAlertDialog(text:String, responseBody:Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(requireContext())

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        builder.setCancelable(false) //다이얼로그 밖 클릭해도 안꺼짐

        builder.setPositiveButton("확인") { _, _ ->
            if(responseBody){
                intent()
            }
        }

        if(responseBody){
            // 비밀번호 찾기 버튼
            builder.setNegativeButton("비밀번호 찾기") { _, _ ->
                val intent = Intent(requireContext(), find_pw_Activity::class.java)
                startActivity(intent)
            }
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }
    fun intent(){
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}