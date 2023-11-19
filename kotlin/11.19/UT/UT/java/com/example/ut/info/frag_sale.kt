package com.example.ut.info

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ut.R
import com.example.ut.a_data.board
import com.example.ut.board.board_info
import com.example.ut.board.write_page
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class frag_sale : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //여기 내용 실행
        val view = inflater.inflate(R.layout.activity_frag_sale, container, false) // setContentView의 프래그먼트 버전

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val preferences = requireActivity().getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        val userid = preferences.getString("userid","")

        val apiService =  retrofit.create(ApiServer::class.java)
        val search = com.example.ut.a_data.search(userid, "", 0)
        val call = apiService.board(search)

        call.enqueue(object : Callback<board> {
            override fun onResponse(call: Call<board>, response: Response<board>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // 서버 응답 처리

                    if(responseBody!!.items.isNotEmpty()){
                        val scrollView  = view.findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                        val linearLayout = scrollView.getChildAt(0) as ViewGroup

                        for(i in 0 until responseBody!!.items.size){
                            val linearLayout1 = LinearLayout(requireContext())
                            linearLayout1.orientation = LinearLayout.HORIZONTAL
                            linearLayout1.setBackgroundResource(R.drawable.text_bar);

                            var layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                (120 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )

                            layoutParams.topMargin = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()

                            linearLayout1.layoutParams = layoutParams

                            linearLayout1.setOnClickListener {
                                intent(responseBody.items[i].num)
                            }

                            val webView = WebView(requireContext())
                            video(webView,IP,responseBody.items[i].num)
                            layoutParams = LinearLayout.LayoutParams(
                                (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )

                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.gravity = Gravity.CENTER
                            webView.layoutParams = layoutParams

                            val innerLayout = LinearLayout(requireContext())
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (100 * resources.displayMetrics.density).toInt()
                            )
                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.gravity = Gravity.CENTER
                            innerLayout.layoutParams = layoutParams

                            innerLayout.orientation = LinearLayout.VERTICAL

                            val titleTextView = TextView(requireContext())
                            titleTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            titleTextView.text = responseBody.items[i].title
                            titleTextView.textSize = 20f

                            val addressTextView = TextView(requireContext())
                            addressTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            addressTextView.text = responseBody.items[i].address
                            addressTextView.textSize = 14f

                            val creditTextView = TextView(requireContext())
                            creditTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            creditTextView.text = responseBody.items[i].credit.toString()
                            creditTextView.textSize = 20f

                            val viewsTextView = TextView(requireContext())
                            viewsTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )

                            viewsTextView.gravity = Gravity.END
                            viewsTextView.text = responseBody.items[i].views.toString()
                            viewsTextView.textSize = 14f

                            // 부모 레이아웃에 하위 뷰들을 추가
                            linearLayout1.addView(webView)
                            linearLayout1.addView(innerLayout)

                            // innerLayout에 하위 뷰들을 추가
                            innerLayout.addView(titleTextView)
                            innerLayout.addView(addressTextView)
                            innerLayout.addView(creditTextView)
                            innerLayout.addView(viewsTextView)

                            linearLayout.addView(linearLayout1,0)
                        }
                    }
                } else {
                    // 서버 요청 실패
                    showAlertDialog("서버 요청에 실패하였습니다.")
                }
            }

            override fun onFailure(call: Call<board>, t: Throwable) {
                // 네트워크 오류 처리
                showAlertDialog("서버 연결에 실패하였습니다.")
                println(t.message)
            }
        })

        return view
    }

    fun showAlertDialog(text:String) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(requireContext())

        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    private fun intent(num:Int){
        val intent = Intent(requireContext(), board_info::class.java)
        intent.putExtra("num",num)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_top, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun video(webView: WebView, adress: IP, num:Int){
        // 동영상 URL 설정
        val videoUrl = adress.ip()+"video/"+num
        val imageUrl = adress.ip()+"image"

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
}