package com.example.ut.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.ui.InformationFragment

class sale_history : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_history)

        setFrag(0)

        val Sale_button = findViewById<Button>(R.id.Sale)
        val Sale_completed_button = findViewById<Button>(R.id.Sale_completed)

        Sale_button.setOnClickListener {
            setFrag(0)
        }
        Sale_completed_button.setOnClickListener {
            setFrag(1)
        }
    }

    private fun setFrag(fragNum : Int) { //프래그먼트 바꾸는 함수
        val ft = supportFragmentManager.beginTransaction() //프래그먼트 관리(변수)

        val Sale_button = findViewById<Button>(R.id.Sale)
        val Sale_completed_button = findViewById<Button>(R.id.Sale_completed)

        when(fragNum){
            0 -> { //fragNum이 0일 경우
                ft.replace(R.id.main_frag, frag_sale()).commit() //프래그먼트를 교체한다(activity_maind안에 FrameLayoutdml의 id가 main_frag임), 바꾸려는 액티비티
                Sale_button.setBackgroundResource(R.drawable.square_dark_blue) //background 속성 변경
                Sale_completed_button.setBackgroundResource(R.drawable.square_lite_blue)
            }
            1 -> {
                ft.replace(R.id.main_frag, frag_sale_complete()).commit()
                Sale_button.setBackgroundResource(R.drawable.square_lite_blue)
                Sale_completed_button.setBackgroundResource(R.drawable.square_dark_blue)
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("tag","info")
        startActivity(intent)
    }
}