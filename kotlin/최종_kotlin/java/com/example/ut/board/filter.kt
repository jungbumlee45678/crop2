package com.example.ut.board

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.class_ca
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class filter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val call = apiService.class_ca()

        val search = findViewById<EditText>(R.id.search)
        search.setOnClickListener{
            val intent = Intent(this, com.example.ut.board.search::class.java)
            intent.putExtra("mode","search")
            startActivity(intent)
        }

        call.enqueue(object : Callback<List<class_ca>>{
            override fun onResponse(call: Call<List<class_ca>>, response: Response<List<class_ca>>) {
                if(response.isSuccessful){
                    val responsebody = response.body()

                    val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경

                    lifecycleScope.launch {
                        val scrollViewWidthDeferred = async {
                            delay(100) // 비동기 작업을 여기에 추가할 수 있음
                            scrollView.width - (40 * resources.displayMetrics.density).toInt()
                        }

                        // 이제 scrollView의 너비를 얻을 수 있습니다.
                        val scrollViewWidth = scrollViewWidthDeferred.await()/3

                        val linearLayout = scrollView.getChildAt(0) as ViewGroup//ScrollView 내에 있는 LinearLayout을 찾고 이를 linearLayout 변수로 가져옴.'
                        var newlin = LinearLayout(this@filter)

                        if (responsebody != null) {
                            for (i in 0 until responsebody.size) {
                                val newlin2 = LinearLayout(this@filter)
                                newlin2.orientation = LinearLayout.VERTICAL

                                val newText = TextView(this@filter)
                                newText.text = responsebody[i].classification
                                newText.gravity = Gravity.CENTER_HORIZONTAL

                                val layoutParams = LinearLayout.LayoutParams(
                                    scrollViewWidth, // 너비는 부모와 일치
                                    scrollViewWidth // 높이는 내용에 맞게
                                )

                                val layoutParams2 = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                    LinearLayout.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
                                )

                                if (i != 0 && i % 3 == 0) {
                                    linearLayout.addView(newlin)
                                    newlin = LinearLayout(this@filter)
                                } else if (i == responsebody.size - 1) {
                                    linearLayout.addView(newlin)
                                }

                                if (i % 3 != 0) {
                                    layoutParams.leftMargin = (10 * resources.displayMetrics.density).toInt()
                                    layoutParams2.leftMargin = (10 * resources.displayMetrics.density).toInt()
                                }
                                if (i % 3 != 2) {
                                    layoutParams.rightMargin = (10 * resources.displayMetrics.density).toInt()
                                    layoutParams2.rightMargin = (10 * resources.displayMetrics.density).toInt()
                                }
                                layoutParams.bottomMargin = (20 * resources.displayMetrics.density).toInt()
                                layoutParams2.bottomMargin = (40 * resources.displayMetrics.density).toInt()

                                val newimageview = ImageView(this@filter)
                                newimageview.setBackgroundResource(R.drawable.rounded_corner_border)
                                //newimageview.backgroundTintList = ContextCompat.getColorStateList(this@filter, R.color.white)
                                newimageview.clipToOutline = true //둥글게 만들기
                                newimageview.scaleType = ImageView.ScaleType.CENTER_CROP

                                newimageview.layoutParams = layoutParams
                                newText.layoutParams =layoutParams2

                                Glide.with(this@filter)
                                    .load(IP.ip() + "image/server/" + responsebody[i].image)
                                    .into(newimageview)

                                newlin2.addView(newimageview)
                                newlin2.addView(newText)

                                newlin2.setOnClickListener{
                                    intent(newText.text.toString())
                                }

                                newlin.addView(newlin2)
                            }
                        }
                    }
                }else{
                    println("요청 실패")
                }
            }

            override fun onFailure(call: Call<List<class_ca>>, t: Throwable) {
                println("연결 실패")
            }
        })
    }

    fun intent(ca:String){
        val intent = Intent(this, search_board::class.java)
        intent.putExtra("ca",ca)
        intent.putExtra("mode","search")
        startActivity(intent)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}