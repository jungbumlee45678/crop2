package com.example.ut.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.Item
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.zip.GZIPInputStream

class board_info : AppCompatActivity() {
    var search = 0
    var tag:String? = null
    var isSubitem1Visible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_info)

        val IP = IP()

        val intent = intent
        val num =  intent.getIntExtra("num",0)
        search = intent.getIntExtra("search",0)
        tag = intent.getStringExtra("tag")

        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid", "")
        val myname = preferences.getString("username", "")

        val apiService =  retrofit.create(ApiServer::class.java)
        val board_num = board_num(num, userid.toString())

        val call = apiService.board_info(board_num)

        val webView = findViewById<WebView>(R.id.WebView)
        video(webView,IP,num)

        val chat = findViewById<Button>(R.id.chat)
        var username = ""
        var title = "'"
        var bo_userid = ""
        var boardid = num
        var state = 0

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        call.enqueue(object : Callback<board_info> {
            override fun onResponse(call: Call<board_info>, response: Response<board_info>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    val correction = toolbar.menu.findItem(R.id.correction)
                    val delete = toolbar.menu.findItem(R.id.delete)

                    username = responseBody!!.username
                    title = responseBody.title
                    bo_userid = responseBody.userid
                    state = responseBody.state

                    if(userid!=responseBody!!.userid){
                        /*
                        correction.isVisible = false
                        delete.isVisible = false
                         */
                        toolbar.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.correction -> {
                                    showAlertDialog("자신의 게시물이 아닙니다.")
                                    return@setOnMenuItemClickListener true
                                }

                                R.id.delete -> {
                                    showAlertDialog("자신의 게시물이 아닙니다.")
                                    return@setOnMenuItemClickListener true

                                }

                                else -> return@setOnMenuItemClickListener false
                            }
                        }
                    } else if(state==1){
                        toolbar.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.correction -> {
                                    showAlertDialog("거래가 완료된 게시물을 수정할 수 없습니다.")
                                    return@setOnMenuItemClickListener true
                                }

                                R.id.delete -> {
                                    showAlertDialog("거래가 완료된 게시물을 삭제할 수 없습니다.")
                                    return@setOnMenuItemClickListener true

                                }

                                else -> return@setOnMenuItemClickListener false
                            }
                        }
                    }else {
                        toolbar.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.correction -> {
                                    // 메뉴 아이템 1 클릭 시 처리
                                    val intent = Intent(this@board_info, write_page::class.java)
                                    intent.putExtra("num",num)
                                    intent.putExtra("title",title)
                                    intent.putExtra("text",responseBody!!.content)
                                    intent.putExtra("credit", responseBody!!.credit.toString().let { "%,d".format(it.toLongOrNull()) })
                                    intent.putExtra("category",responseBody!!.category)
                                    startActivity(intent)
                                    return@setOnMenuItemClickListener true
                                }

                                R.id.delete -> {
                                    // 메뉴 아이템 2 클릭 시 처리
                                    val call = apiService.board_delete(board_num)
                                    call.enqueue(object:Callback<String>{
                                        override fun onResponse(call: Call<String>, response: Response<String>) {
                                            if(response.isSuccessful){
                                                intent()
                                            }else{
                                                println("요청 실패")
                                            }
                                        }
                                        override fun onFailure(call: Call<String>, t: Throwable) {
                                            println("연결 실패")
                                        }
                                    })
                                    return@setOnMenuItemClickListener true
                                }

                                else -> return@setOnMenuItemClickListener false
                            }
                        }
                    }

                    val imageView = findViewById<ImageView>(R.id.profileImageView)

                    Glide.with(this@board_info)
                        .load(IP.ip()+"profile/"+responseBody!!.userid)
                        .placeholder(R.drawable.baseline_person_24_blue)
                        .override(
                            (100 * resources.displayMetrics.density).toInt(),
                            (100 * resources.displayMetrics.density).toInt()
                        )
                        .into(imageView)

                    imageView.clipToOutline = true //둥글게 만들기

                    findViewById<TextView>(R.id.username).text = responseBody!!.username
                    findViewById<TextView>(R.id.address).text = responseBody?.address
                    findViewById<TextView>(R.id.title).text = responseBody?.title
                    findViewById<TextView>(R.id.category).text = responseBody?.category

                    findViewById<TextView>(R.id.detail).text = responseBody!!.content.replace("\\n", "\n")

                    findViewById<TextView>(R.id.views).text = responseBody!!.views.toString()
                    findViewById<TextView>(R.id.credit).text = responseBody!!.credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원"
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
                    boardid,
                    userid!!
                )
                val call = apiService.chat(data)
                call.enqueue(object:Callback<chat_num>{
                    override fun onResponse(call: Call<chat_num>, response: Response<chat_num>) {
                        if(response.isSuccessful){
                            val data = response.body()

                            if(data!=null){
                                intent(data.num, boardid, username, title, bo_userid, state)
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
    fun intent(num:Int,boardid:Int,username:String,title:String,bo_userid:String,state:Int){
        val intent = Intent(this,chatting::class.java)
        intent.putExtra("num",num)
        intent.putExtra("boardid", boardid)
        intent.putExtra("di_username",username)
        intent.putExtra("title",title)
        intent.putExtra("bo_userid",bo_userid)
        intent.putExtra("state", state)
        startActivity(intent)
    }

    fun intent(){
        val intent = Intent(this, MainActivity::class.java)
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
        val imageUrl = adress.ip()+"image/"+num

        // WebView 설정
        val settings = webView.settings
        settings.javaScriptEnabled = true

        webView.webViewClient = object:WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // 오류 로깅 또는 디버깅을 위한 작업 수행
                val errorMessage = "WebViewError Error: ${error?.description}"
                println(errorMessage)
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
        if(search == 1 || tag=="info"){
            super.onBackPressed()
        } else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}