package com.example.ut.info

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.ut.R

class sale_history : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_history)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","").toString()

        val list = listOf<String>("제목","구매자","가격","날짜") //제목 구매자 가격 날짜
        val data = listOf<List<String>>(list,list,list)

        createText(0, data.size, data)
    }

    fun createText(location: Int, count: Int, text:List<List<String>>) {
        val scrollView  = findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
        val linearLayout = scrollView.getChildAt(0) as ViewGroup

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
            ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
        )

        for (i in 0 until count){
            val newTextView = TextView(this)
            var text_all = ""

            for(j in 0 until text[i].size){
                text_all += text[i][j]
                if(j!=text[i].size-1){
                    text_all += "|"
                }
            }

            newTextView.text = text_all

            newTextView.textSize = 24f

            layoutParams.marginStart= 80
            layoutParams.topMargin = 20
            newTextView.layoutParams = layoutParams

            newTextView.setTextColor(Color.BLACK)

            linearLayout.addView(newTextView,location+i)
        }
    }
}