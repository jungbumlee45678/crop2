package com.example.ut.alarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
                                    val scrollView = findViewById<ScrollView>(R.id.main2)
                                    val linearLayout = scrollView.getChildAt(0) as ViewGroup

                                    // 서버 응답 처리
                                    if(responseBody!!.items.isNotEmpty()){
                                        linearLayout.removeAllViews()

                                        for(i in 0 until responseBody!!.items.size){
                                            val linearLayout1 = LinearLayout(this@alarm)
                                            linearLayout1.orientation = LinearLayout.HORIZONTAL

                                            var layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                                LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                                            )

                                            linearLayout1.layoutParams = layoutParams
                                            linearLayout1.setPadding((10 * resources.displayMetrics.density).toInt())
                                            linearLayout1.setOnClickListener {
                                                intent(responseBody.items[i].num)
                                            }

                                            val linear = LinearLayout(this@alarm)
                                            layoutParams = LinearLayout.LayoutParams(
                                                (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                                (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                            )
                                            layoutParams.marginStart =
                                                (10 * resources.displayMetrics.density).toInt()
                                            layoutParams.topMargin =
                                                (10 * resources.displayMetrics.density).toInt()
                                            linear.layoutParams = layoutParams

                                            val view = ImageView(this@alarm)
                                            view.setBackgroundResource(R.drawable.rounded_corner_border_2)

                                            view.clipToOutline = true //둥글게 만들기
                                            view.scaleType = ImageView.ScaleType.CENTER_CROP

                                            layoutParams = LinearLayout.LayoutParams(
                                                (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                                (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                            )
                                            layoutParams.gravity = Gravity.CENTER
                                            view.layoutParams = layoutParams

                                            Glide.with(this@alarm)
                                                .load(IP.ip()+"image/"+responseBody.items[i].num)
                                                .placeholder(R.color.black) // 기본 이미지 설정
                                                .override(
                                                    (100 * resources.displayMetrics.density).toInt(),
                                                    (100 * resources.displayMetrics.density).toInt()
                                                )
                                                .into(view)

                                            linear.addView(view)

                                            view.setOnClickListener {
                                                linear.removeAllViews()
                                                val webView = WebView(this@alarm)
                                                video(webView, IP, responseBody.items[i].num)
                                                layoutParams = LinearLayout.LayoutParams(
                                                    (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                                    (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                                )
                                                layoutParams.gravity = Gravity.CENTER
                                                webView.layoutParams = layoutParams

                                                webView.setBackgroundResource(R.drawable.rounded_corner_border_2)
                                                webView.clipToOutline = true //둥글게 만들기

                                                linear.addView(webView)
                                            }

                                            val innerLayout = LinearLayout(this@alarm)
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                (100 * resources.displayMetrics.density).toInt()
                                            )
                                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()
                                            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
                                            layoutParams.gravity = Gravity.CENTER
                                            innerLayout.layoutParams = layoutParams

                                            innerLayout.orientation = LinearLayout.VERTICAL

                                            val titleTextView = TextView(this@alarm)
                                            titleTextView.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            titleTextView.text = responseBody.items[i].title
                                            titleTextView.textSize = 25f
                                            titleTextView.ellipsize = TextUtils.TruncateAt.END
                                            titleTextView.maxLines = 1

                                            val addressTextView = TextView(this@alarm)
                                            addressTextView.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            val displayMetrics = resources.displayMetrics
                                            val halfScreenWidth = displayMetrics.widthPixels / 2.6

                                            addressTextView.maxWidth = halfScreenWidth.toInt()
                                            addressTextView.ellipsize = TextUtils.TruncateAt.END
                                            addressTextView.maxLines = 1
                                            addressTextView.text = responseBody.items[i].address
                                            addressTextView.textSize = 20f

                                            val creditTextView = TextView(this@alarm)
                                            creditTextView.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            creditTextView.text = responseBody.items[i].credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원"
                                            creditTextView.textSize = 25f

                                            val viewsTextView = TextView(this@alarm)
                                            viewsTextView.layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            viewsTextView.gravity = Gravity.END
                                            viewsTextView.text = responseBody.items[i].views.toString()
                                            viewsTextView.textSize = 20f

                                            // innerLayout에 하위 뷰들을 추가
                                            innerLayout.gravity = Gravity.CENTER_VERTICAL
                                            innerLayout.addView(titleTextView)
                                            innerLayout.addView(addressTextView)

                                            val newlin = LinearLayout(this@alarm)
                                            newlin.orientation = LinearLayout.HORIZONTAL
                                            newlin.addView(creditTextView)
                                            newlin.addView(viewsTextView)

                                            innerLayout.addView(newlin)

                                            // 부모 레이아웃에 하위 뷰들을 추가
                                            linearLayout1.addView(linear)
                                            linearLayout1.addView(innerLayout)

                                            linearLayout.addView(linearLayout1,0)
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

    private fun intent(num:Int){
        val intent = Intent(this, board_info::class.java)
        intent.putExtra("num",num)
        intent.putExtra("search",1)
        startActivity(intent)
    }

    private fun video(webView: WebView, adress: IP, num:Int){
        // 동영상 URL 설정
        val videoUrl = adress.ip()+"video/"+num
        val imageUrl = adress.ip()+"image/"+num

        // WebView 설정
        val settings = webView.settings
        settings.javaScriptEnabled = true

        webView.webViewClient = object: WebViewClient(){
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
                            video.removeAttribute('controls');
                            video.setAttribute('poster','$imageUrl');
                            
                            video.addEventListener('click', function() {
                                if (video.paused) {
                                    // 비디오가 일시 정지 상태인 경우, 'play()' 메서드를 호출하여 재생합니다.
                                    video.play();
                                } else {
                                    // 비디오가 재생 중인 경우, 'pause()' 메서드를 호출하여 정지합니다.
                                    video.pause();
                                }
                            });
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}