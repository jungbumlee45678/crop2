package com.example.stfrag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.a1110_layout.R
import com.example.a1110_layout.frag_sale
import com.example.a1110_layout.frag_sale_complete

class sales_history : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        프래그먼트 화면 변환때 뒤로가기 정상 작동을 위한 속성
        C>사용자>(이름)>AndroidStudioProjects>(프로젝트이름)>app>src>main>AndroidManifest에서
        application의 속성에서 android:enableOnBackInvokedCallback="true"를 추가해야 정상작동
        */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_history)

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
                Sale_button.setBackgroundResource(R.drawable.press_square_lite_blue_button) //background 속성 변경
                Sale_completed_button.setBackgroundResource(R.drawable.square_lite_blue)
            }
            1 -> {
                ft.replace(R.id.main_frag, frag_sale_complete()).commit()
                Sale_button.setBackgroundResource(R.drawable.square_lite_blue)
                Sale_completed_button.setBackgroundResource(R.drawable.press_square_lite_blue_button)
            }
        }
    }
}