package com.example.ut.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.board
import com.example.ut.a_data.board_info
import com.example.ut.a_data.board_num
import com.example.ut.a_data.chat
import com.example.ut.a_data.chat_num
import com.example.ut.chatting.chatting
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class board_info : AppCompatActivity() {
    var search = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_info)

        val IP = IP()

        val intent = intent
        val num =  intent.getIntExtra("num",0)
        search = intent.getIntExtra("search",0)

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val board_num = board_num(num)

        val call = apiService.board_info(board_num)

        val preferences = getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        val myname = preferences.getString("username", "")

        val webView = findViewById<WebView>(R.id.WebView)
        video(webView,IP,num)

        val chat = findViewById<Button>(R.id.chat)
        var username = ""
        var title = "'"
        var bo_userid = ""

        call.enqueue(object : Callback<board_info> {
            override fun onResponse(call: Call<board_info>, response: Response<board_info>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    username = responseBody!!.username
                    title = responseBody!!.title
                    bo_userid = responseBody!!.userid

                    val imageView = findViewById<ImageView>(R.id.profileImageView)

                    Glide.with(this@board_info)
                        .load(IP.ip()+"profile/"+responseBody!!.userid)
                        .placeholder(R.drawable.baseline_person_24)
                        .override(
                            (100 * resources.displayMetrics.density).toInt(),
                            (100 * resources.displayMetrics.density).toInt()
                        )
                        .into(imageView)

                    findViewById<TextView>(R.id.username).text = responseBody!!.username
                    findViewById<TextView>(R.id.address).text = responseBody?.address
                    findViewById<TextView>(R.id.title).text = responseBody?.title
                    findViewById<TextView>(R.id.category).text = responseBody?.category

                    findViewById<TextView>(R.id.detail).text = responseBody!!.content.replace("\\n", "\n")

                    findViewById<TextView>(R.id.views).text = responseBody?.views.toString()
                    findViewById<TextView>(R.id.credit).text = responseBody?.credit.toString()
                    findViewById<TextView>(R.id.date).text = convertTimestampToKoreanTime(responseBody!!.time)
                } else {
                    println("요청 실패")
                }
            }

            override fun onFailure(call: Call<board_info>, t: Throwable) {
                println("연결 실패")
            }
        })

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
                                intent(data!!.num,username,title,bo_userid)
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
    fun intent(num:Int,username:String,title:String,bo_userid:String){
        val intent = Intent(this,chatting::class.java)
        intent.putExtra("num",num)
        intent.putExtra("di_username",username)
        intent.putExtra("title",title)
        intent.putExtra("bo_userid",bo_userid)
        startActivity(intent)
    }

    fun convertTimestampToKoreanTime(timestamp: Timestamp): String {
        // 시간대를 한국 시간대로 설정
        val koreanTimeZone = TimeZone.getTimeZone("Asia/Seoul")

        // Timestamp를 Date로 변환
        val date = Date(timestamp.time)

        // SimpleDateFormat을 사용하여 한국 시간 형식으로 포맷
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = koreanTimeZone

        // 포맷된 날짜 및 시간 반환
        return sdf.format(date)
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

    override fun onBackPressed() {
        if(search == 1){
            super.onBackPressed()
        } else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}