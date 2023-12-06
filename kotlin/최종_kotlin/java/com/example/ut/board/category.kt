package com.example.ut.board

import android.content.Intent
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

class category : AppCompatActivity() {
    var classification = listOf<String>()
    var category_list = listOf<String>()

    var select_text: TextView? = null

    var main_index = 0

    var title:String? = null
    var text:String? = null
    var credit:String? = null
    var category:String? = null
    var mode:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","").toString()

        title = intent.getStringExtra("title")
        text = intent.getStringExtra("text")
        credit = intent.getStringExtra("credit")
        category = intent.getStringExtra("category")
        mode = intent.getStringExtra("mode")

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiserver = retrofit.create(ApiServer::class.java)
        val data = login_member_data("", "")

        val call = apiserver.carto(data)

        call.enqueue(object : Callback<category_all> {
            override fun onResponse(call: Call<category_all>, response: Response<category_all>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // 서버 응답 처리
                    classification = responseBody!!.data1.map { it.classification }
                    category_list = responseBody!!.data1.map { it.category }

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

    fun createText(location: Int, count: Int, text:List<String>, bool:Boolean=true) {
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

                    val sublist = category_list.subList(index, category_list.size)

                    if(main_index!=0){
                        deleteText(main_index)
                    }
                    createText(i+1, this_count, sublist,false)

                    main_index = i+1; select_text = newTextView
                }
                linearLayout.addView(newTextView,location+i)
            }else{
                newTextView.text = "- "+text[i]

                newTextView.textSize = 24f

                layoutParams.marginStart= 100
                layoutParams.topMargin = 20
                newTextView.layoutParams = layoutParams

                newTextView.setTextColor(Color.BLACK)

                newTextView.setOnClickListener {
                    category = text[i]
                    intent()
                }

                list.add(newTextView)
            }
        }

        if(list.isNotEmpty()){ //영역 표시로 의한 리스트 한개씩 넣기 부분
            for(i in 0 until list.size){
                linear.addView(list[i])
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
        val intent = Intent(this, write_page::class.java)
        intent.putExtra("title",title)
        intent.putExtra("text",text)
        intent.putExtra("credit",credit)
        intent.putExtra("category",category)
        intent.putExtra("mode",mode)
        startActivity(intent)
    }

    override fun onBackPressed(){
        intent()
    }
}