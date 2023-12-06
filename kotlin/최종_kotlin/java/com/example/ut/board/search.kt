package com.example.ut.board

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.board_num
import com.example.ut.a_data.search_userid
import com.example.ut.a_data.userid
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class search  : AppCompatActivity()  {
    var mode:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search)

        mode = intent.getStringExtra("mode")

        val IP = IP()
        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","")

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val send_data = userid(userid!!)

        val call = apiService.search(send_data)

        call.enqueue(object:Callback<List<search_userid>>{
            override fun onResponse(call: Call<List<search_userid>>, response: Response<List<search_userid>>) {
                if(response.isSuccessful){
                    val responsebody = response.body()

                    val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                    val linearLayout = scrollView.getChildAt(0) as ViewGroup//ScrollView 내에 있는 LinearLayout을 찾고 이를 linearLayout 변수로 가져옴.'

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                        ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
                    )

                    for(i in 0 until responsebody!!.size){
                        val newTextView = TextView(this@search)

                        println(i)

                        newTextView.text = responsebody[i].search
                        newTextView.setTextColor(Color.BLACK)
                        newTextView.textSize = 20f
                        newTextView.layoutParams = layoutParams

                        newTextView.setOnClickListener{
                            intent(responsebody[i].search)
                        }

                        linearLayout.addView(newTextView)
                    }
                }else{
                    println("요청 실패")
                }
            }

            override fun onFailure(call: Call<List<search_userid>>, t: Throwable) {
                println("연결 실패")
            }
        })

        val search = findViewById<EditText>(R.id.search)
        search.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH  ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                // 여기에 "완료" 버튼을 눌렀을 때 실행할 코드를 작성
                if(!search.text.isNullOrBlank()){
                    val intent = Intent(this,search_board::class.java)
                    intent.putExtra("search", search.text.toString())
                    intent.putExtra("mode" , mode)
                    startActivity(intent)
                }

                return@setOnEditorActionListener true
            }
            false
        }

        val search_icon = findViewById<ImageView>(R.id.search_icon)
        search_icon.setOnClickListener {
            if(!search.text.isNullOrBlank()){
                val intent = Intent(this,search_board::class.java)
                intent.putExtra("search", search.text.toString())
                intent.putExtra("mode" , mode)
                startActivity(intent)
            }
        }
    }

    fun intent(text:String){
        val intent = Intent(this,search_board::class.java)
        intent.putExtra("search",text)
        intent.putExtra("mode",mode)
        startActivity(intent)
    }
}