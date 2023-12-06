package com.example.ut.info

import  android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.cate
import com.example.ut.a_data.category_all
import com.example.ut.a_data.login_member_data
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class interest_thing : AppCompatActivity() {
    var classification = listOf<String>()
    var category = listOf<String>()

    val select_data = mutableListOf<String>()
    val select_ca = mutableListOf<String>()

    var select_ca_0 = mutableListOf<String>()

    var select_text:TextView? = null

    var main_index = 0

    var start = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest_thing)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","").toString()

        val intent = intent
        start = intent.getIntExtra("start",1)

        val sendb = findViewById<Button>(R.id.send_gs)

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiserver = retrofit.create(ApiServer::class.java)
        val data = login_member_data(
            userid,
            ""
        )

        val call = apiserver.carto(data)

        call.enqueue(object : Callback<category_all> {
            override fun onResponse(call: Call<category_all>, response: Response<category_all>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // 서버 응답 처리
                    classification = responseBody!!.data1.map { it.classification }
                    category = responseBody!!.data1.map { it.category }

                    select_ca.addAll(responseBody!!.data2.map { it.classification })
                    select_data.addAll(responseBody!!.data2.map { it.category })

                    select_ca_0 = select_ca.distinct().toMutableList()
                    val category = classification.distinct()
                    createText(0, category.size, category)
                } else {
                    // 서버 요청 실패
                    showAlertDialog("서버 요청에 실패하였습니다.")
                }
            }

            override fun onFailure(call: Call<category_all>, t: Throwable) {
                // 네트워크 오류 처리
                showAlertDialog("서버 연결에 실패하였습니다.")
                println(t.message)
            }
        })

        sendb.setOnClickListener {
            val data = cate(
                select_data,
                userid
            )
            val call2 = apiserver.carto_input(data)

            call2.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        intent()
                    } else {
                        // 서버 요청 실패
                        showAlertDialog("서버 요청에 실패하였습니다.")
                    }
                }
                override fun onFailure(call: Call<String>, t: Throwable) {
                    // 네트워크 오류 처리
                    showAlertDialog("서버 연결에 실패하였습니다.")
                    println(t.message)
                }
            })
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

    fun createText(location: Int, count: Int, text:List<String>, TextView: TextView?=null ,bool:Boolean=true,index:Int=0) {
        val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
        val linearLayout = scrollView.getChildAt(0) as ViewGroup

        val list = mutableListOf<TextView>()
        val linear = LinearLayout(this)
        linear.orientation = LinearLayout.VERTICAL
        linear.setBackgroundResource(R.drawable.text_bar2);

        val layoutParam0 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
            ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
        )

        layoutParam0.marginStart = 40
        layoutParam0.marginEnd = 40
        layoutParam0.topMargin = 20

        linear.layoutParams = layoutParam0

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
            ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
        )

        for (i in 0 until count){
            val newTextView = TextView(this)

            if(bool){
                newTextView.text = "○ "+text[i]

                newTextView.textSize = 30f
                newTextView.setTextColor(Color.BLACK)

                layoutParams.marginStart= 40
                layoutParams.marginEnd= 40
                layoutParams.topMargin = 20

                newTextView.layoutParams = layoutParams

                newTextView.setOnClickListener {
                    if(select_text!=null){
                        select_text?.text = "○ " + select_text!!.text.substring(1)
                        select_text?.setBackground(null)
                    }
                    newTextView.text = "● " + text[i]
                    newTextView.setBackgroundResource(R.drawable.text_bar2);

                    val this_count = classification.count { it == text[i] }
                    val index = classification.indexOf(text[i])

                    val sublist = category.subList(index, category.size)

                    if(main_index!=0){
                        deleteText(main_index)
                    }
                    createText(i+1,this_count, sublist,newTextView,false, index)

                    main_index = i+1; select_text = newTextView
                }
                linearLayout.addView(newTextView,location+i)
            }else{
                newTextView.textSize = 24f

                layoutParams.marginStart= 100
                layoutParams.topMargin = 20
                newTextView.layoutParams = layoutParams

                if(select_data.contains(text[i])){
                    newTextView.text = "○"+text[i]
                    newTextView.setTextColor(Color.BLACK)
                }else{
                    newTextView.text = "●"+text[i]
                    newTextView.setTextColor(Color.BLUE)
                }

                newTextView.setOnClickListener {
                    if(select_data.contains(text[i])){
                        newTextView.text = "●"+text[i]
                        newTextView.setTextColor(Color.BLUE)
                        select_ca.remove(classification[index])
                        select_data.remove(text[i])
                    }else{
                        newTextView.text = "○"+text[i]
                        newTextView.setTextColor(Color.BLACK)
                        select_ca.add(classification[index])
                        select_data.add(text[i])
                    }
                }
                list.add(newTextView)
            }
        }

        if(list.isNotEmpty()){
            for(i in 0 until list.size){
                linear.addView(list[i])
                println(list[i].text)
            }
            linearLayout.addView(linear,location)
        }
    }

    fun deleteText(location: Int) {
        val scrollView = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
        val linearLayout = scrollView.getChildAt(0) as ViewGroup

        linearLayout.removeViewAt(location)
    }

    private fun intent(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed(){
        if(start==1){
            intent()
        }else{
            super.onBackPressed()
        }
    }
}