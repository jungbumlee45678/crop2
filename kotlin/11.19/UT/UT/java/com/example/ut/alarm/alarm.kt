package com.example.ut.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.ut.MainActivity
import com.example.ut.a_data.userid
import com.example.ut.a_data.keywrod
import com.example.ut.R
import com.example.ut.a_data.board
import com.example.ut.board.board_info
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class alarm : AppCompatActivity() {
    private var webSocketJob: Job? = null // 코루틴 Job을 추적하기 위한 변수
    val IP = IP()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","").toString()

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)

        val call = apiService.keyword(userid(userid))

        call.enqueue(object : Callback<List<keywrod>> {
            override fun onResponse(call: Call<List<keywrod>>, response: Response<List<keywrod>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    setContentView(R.layout.activity_alarm)
                    // 서버 응답 처리
                    if(responseBody != null){
                        val alarm_keyword = findViewById<TextView>(R.id.alarm_keyword)
                        val keyword_go = findViewById<Button>(R.id.keyword_go)

                        alarm_keyword.text = "알림 받는 키워드 "+responseBody.size+"개"

                        keyword_go.setOnClickListener{
                            intent()
                        }
                        
                        val call = apiService.alert(userid(userid))
                        call.enqueue(object:Callback<board>{
                            override fun onResponse(call: Call<board>, response: Response<board>) {
                                if (response.isSuccessful) {
                                    val responseBody = response.body()
                                    if(responseBody!=null){
                                        if (responseBody.items.isNotEmpty()) {
                                            val scrollView = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                                            val linearLayout = scrollView.getChildAt(0) as ViewGroup

                                            val layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                                ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
                                            )

                                            for (i in 0 until responseBody!!.items.size) {
                                                val newTextView = TextView(this@alarm)
                                                newTextView.text = "제목: " + responseBody.items[i].title

                                                newTextView.textSize = 30f
                                                layoutParams.topMargin = 20
                                                newTextView.layoutParams = layoutParams

                                                newTextView.setOnClickListener {
                                                    intent(
                                                        responseBody.items[i].title,
                                                        responseBody.items[i].username,
                                                        responseBody.items[i].num
                                                    )
                                                }

                                                linearLayout.addView(newTextView, 0)
                                            }
                                        }
                                    }
                                } else {
                                    println("요청 실패")
                                }
                            }override fun onFailure(call: Call<board>, t: Throwable) {
                                println("연결 실패")
                            }
                        })
                    }
                } else {
                    // 서버 요청 실패
                    showAlertDialog("서버 요청에 실패하였습니다.")
                    setContentView(R.layout.activity_alarm)
                }
            }

            override fun onFailure(call: Call<List<keywrod>>, t: Throwable) {
                // 네트워크 오류 처리
                setContentView(R.layout.activity_alarm)
                showAlertDialog("서버 연결에 실패하였습니다.")
                println(t.message)
            }
        })
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

    private fun intent(){
        val intent = Intent(this, alarm_keyword::class.java)
        intent.putExtra("alarm_keyword",1)
        startActivity(intent)
    }

    private fun intent(title:String, username:String, num:Int){
        val intent = Intent(this, board_info::class.java)
        intent.putExtra("num",num)
        intent.putExtra("title",title)
        intent.putExtra("username",username)
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}