package com.example.ut.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setMargins
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.example.ut.R
import com.example.ut.a_data.keywrod
import com.example.ut.a_data.keywrod_input
import com.example.ut.a_data.userid
import com.google.gson.GsonBuilder
import org.apmem.tools.layouts.FlowLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class alarm_keyword : AppCompatActivity() {
    var alarm_num = 0
    val all_keyword = mutableListOf<String>()
    var goto = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm_keyword)

        val intent = intent
        goto = intent.getIntExtra("alarm_keyword",0)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","").toString()

        val send_button = findViewById<Button>(R.id.keyword_send)
        val alarm_num_text = findViewById<TextView>(R.id.alarm_num)

        val IP = IP()

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
                    // 서버 응답 처리
                    for(i in 0 until responseBody!!.size){
                        all_keyword.add(responseBody[i].keyword)
                    }
                    createText(alarm_num_text,userid)
                } else {
                    // 서버 요청 실패
                    showAlertDialog("서버 요청에 실패하였습니다.")
                }
            }

            override fun onFailure(call: Call<List<keywrod>>, t: Throwable) {
                // 네트워크 오류 처리
                showAlertDialog("서버 연결에 실패하였습니다.")
                println(t.message)
            }
        })

        val alarm_keyword = findViewById<EditText>(R.id.alarm_keyword)
        alarm_keyword.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE  ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    // 여기에 "완료" 버튼을 눌렀을 때 실행할 코드를 작성
                    val keyword = findViewById<TextView>(R.id.alarm_keyword)
                    val keyword_text = keyword.text.toString()

                    if (keyword_text.equals("")) {
                        showAlertDialog("키워드가 빈칸입니다.")
                    }else if(alarm_num >= 30){
                        showAlertDialog("키워드는 최대 30개까지 등록할 수 있습니다.")
                    }else {
                        val data = keywrod_input(
                            keyword_text,
                            userid,
                            1
                        )

                        val call = apiService.keyword_input(data)

                        call.enqueue(object : Callback<String>{
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if(response.isSuccessful){
                                    all_keyword.add(keyword_text)

                                    alarm_num += 1
                                    alarm_num_text.text = "추가한 키워드 : "+alarm_num+"/30"

                                    val layoutParams = FlowLayout.LayoutParams(
                                        FlowLayout.LayoutParams.WRAP_CONTENT,
                                        (40 * resources.displayMetrics.density).toInt()
                                    )
                                    layoutParams.setMargins((5 * resources.displayMetrics.density).toInt())

                                    val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                                    val linearLayout = scrollView.getChildAt(0) as ViewGroup//ScrollView 내에 있는 LinearLayout을 찾고 이를 linearLayout 변수로 가져옴.'

                                    val newTextView = Button(this@alarm_keyword) // 텍스트뷰 변수 생성
                                    newTextView.layoutParams = layoutParams
                                    newTextView.setBackgroundResource(R.drawable.square_lite_blue)

                                    newTextView.setOnClickListener {
                                        val data = keywrod_input(
                                            keyword_text,
                                            userid,
                                            0
                                        )
                                        val call = apiService.keyword_input(data)

                                        call.enqueue(object : Callback<String> {
                                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                                if (response.isSuccessful) {
                                                    alarm_num -= 1

                                                    if(alarm_num == 0){
                                                        alarm_num_text.text = ""
                                                    }else{
                                                        alarm_num_text.text = "추가한 키워드 : "+alarm_num+"/30"
                                                    }

                                                    linearLayout.removeView(newTextView)
                                                    all_keyword.remove(keyword_text)
                                                } else {
                                                    showAlertDialog("서버 요청에 실패하였습니다.")
                                                }
                                            }

                                            override fun onFailure(call: Call<String>, t: Throwable) {
                                                showAlertDialog("서버 연결에 실패하였습니다.")
                                                println(t.message)
                                            }
                                        })
                                    }

                                    keyword.text = ""
                                    newTextView.text = keyword_text
                                    linearLayout.addView(newTextView,0)
                                }else{
                                    showAlertDialog("서버 요청에 실패하였습니다.")
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                showAlertDialog("서버 연결에 실패하였습니다.")
                                println(t.message)
                            }
                        })
                    }

                    return@setOnEditorActionListener true
                }
                false
            }

        send_button.setOnClickListener {
            val keyword = findViewById<TextView>(R.id.alarm_keyword)
            val keyword_text = keyword.text.toString()

            if (keyword_text.equals("")) {
                showAlertDialog("키워드가 빈칸입니다.")
            }else if(alarm_num >= 30){
                showAlertDialog("키워드는 최대 30개까지 등록할 수 있습니다.")
            }else {
                val data = keywrod_input(
                    keyword_text,
                    userid,
                    1
                )

                val call = apiService.keyword_input(data)

                call.enqueue(object : Callback<String>{
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if(response.isSuccessful){
                            all_keyword.add(keyword_text)

                            val layoutParams = FlowLayout.LayoutParams(
                                FlowLayout.LayoutParams.WRAP_CONTENT,
                                (40 * resources.displayMetrics.density).toInt()
                            )
                            layoutParams.setMargins((5 * resources.displayMetrics.density).toInt())

                            alarm_num += 1
                            alarm_num_text.text = "추가한 키워드 : "+alarm_num+"/30"

                            val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                            val linearLayout = scrollView.getChildAt(0) as ViewGroup//ScrollView 내에 있는 LinearLayout을 찾고 이를 linearLayout 변수로 가져옴.'

                            val newTextView = Button(this@alarm_keyword) // 텍스트뷰 변수 생성
                            newTextView.layoutParams = layoutParams

                            newTextView.setOnClickListener {
                                val data = keywrod_input(
                                    keyword_text,
                                    userid,
                                    0
                                )
                                val call = apiService.keyword_input(data)

                                call.enqueue(object : Callback<String> {
                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            alarm_num -= 1

                                            if(alarm_num == 0){
                                                alarm_num_text.text = ""
                                            }else{
                                                alarm_num_text.text = "추가한 키워드 : "+alarm_num+"/30"
                                            }

                                            linearLayout.removeView(newTextView)
                                            all_keyword.remove(keyword_text)
                                        } else {
                                            showAlertDialog("서버 요청에 실패하였습니다.")
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                        showAlertDialog("서버 연결에 실패하였습니다.")
                                        println(t.message)
                                    }
                                })
                            }

                            keyword.text = ""
                            newTextView.text = keyword_text
                            newTextView.setBackgroundResource(R.drawable.square_lite_blue)
                            linearLayout.addView(newTextView,0)
                        }else{
                            showAlertDialog("서버 요청에 실패하였습니다.")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        showAlertDialog("서버 연결에 실패하였습니다.")
                        println(t.message)
                    }
                })
            }
        }
    }

    fun createText(alarm_num_text:TextView,userid:String){
        alarm_num = all_keyword.size
        if(alarm_num!=0){
            alarm_num_text.text = "추가한 키워드 : "+all_keyword.size+"/30"
        }

        for(i in 0 until all_keyword.size){
            val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
            val linearLayout = scrollView.getChildAt(0) as ViewGroup//ScrollView 내에 있는 LinearLayout을 찾고 이를 linearLayout 변수로 가져옴.'

            val newTextView = Button(this) // 텍스트뷰 변수 생성

            val layoutParams = FlowLayout.LayoutParams(
                FlowLayout.LayoutParams.WRAP_CONTENT,
                (40 * resources.displayMetrics.density).toInt()
            )
            layoutParams.setMargins((5 * resources.displayMetrics.density).toInt())

            newTextView.layoutParams = layoutParams

            val IP = IP()
            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(IP.ip()) // 실제 엔드포인트 URL
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService =  retrofit.create(ApiServer::class.java)

            newTextView.setOnClickListener {
                val data = keywrod_input(
                    newTextView.text.toString(),
                    userid,
                    0
                )

                val call = apiService.keyword_input(data)

                call.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            alarm_num -= 1

                            if(alarm_num == 0){
                                alarm_num_text.text = ""
                            }else{
                                alarm_num_text.text = "추가한 키워드 : "+alarm_num+"/30"
                            }

                            linearLayout.removeView(newTextView)
                            all_keyword.remove(all_keyword[i])
                        } else {
                            showAlertDialog("서버 요청에 실패하였습니다.")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        showAlertDialog("서버 연결에 실패하였습니다.")
                        println(t.message)
                    }
                })
            }
            newTextView.text =all_keyword[i]
            newTextView.setBackgroundResource(R.drawable.square_lite_blue)
            linearLayout.addView(newTextView,0)
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

    override fun onBackPressed() {
        if (goto == 1) {
            val intent = Intent(this, alarm::class.java)
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }
}