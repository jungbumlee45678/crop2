package com.example.ut.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.chat
import com.example.ut.a_data.chat_num
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class board_info : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_info)

        val IP = IP()

        val data = intent

        val title = data.getStringExtra("title")
        val username = data.getStringExtra("username")
        val num =  data.getIntExtra("num",0)

        val preferences = getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        val myname = preferences.getString("username", "")

        findViewById<TextView>(R.id.title).text = title
        findViewById<TextView>(R.id.username).text = username

        val webView = findViewById<WebView>(R.id.WebView)
        video(webView,IP,num)

        val chat = findViewById<Button>(R.id.chat)

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService = retrofit.create(ApiServer::class.java)

        chat.setOnClickListener {
            if(myname.equals(username)){
                showAlertDialog("자신과 채팅 할 수 없습니다.")
            }else{
                val data = chat(
                    num,
                    myname!!
                )
                val call = apiService.chat(data)
                call.enqueue(object:Callback<chat_num>{
                    override fun onResponse(call: Call<chat_num>, response: Response<chat_num>) {
                        if(response.isSuccessful){
                            val data = response.body()
                            if(data!=null){
                                println(data.num)
                            }
                        }else{
                            println("요청 실패")
                        }
                    }

                    override fun onFailure(call: Call<chat_num>, t: Throwable) {
                        println("연결 실패")
                    }
                })
            }
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

    private fun video(webView:WebView, adress: IP, num:Int){
        // 동영상 URL 설정
        val videoUrl = adress.ip()+"video/"+num
        val imageUrl = adress.ip()+"image"

        // WebView 설정
        val settings = webView.settings
        settings.javaScriptEnabled = true

        webView.webViewClient = object:WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val javascript = """
                    javascript:(function() {
                        var video = document.querySelector('video');
                        if (video) {
                            video.removeAttribute('autoplay');
                            video.setAttribute('poster','$imageUrl');
                        }
                    })()
                """

                webView.evaluateJavascript(javascript, null)
            }
        }
        // 웹 페이지 로드
        webView.loadUrl(videoUrl)
    }
}