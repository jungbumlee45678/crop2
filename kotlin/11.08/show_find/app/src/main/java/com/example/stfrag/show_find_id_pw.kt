package com.example.stfrag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class show_find_id_pw : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        C>사용자>(이름)>AndroidStudioProjects>(프로젝트이름)>app>src>main>AndroidManifest에서
        application의 속성에서 android:enableOnBackInvokedCallback="true"를 추가해야 정상작동
        */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_find_id_pw)

        setFrag(0)

        val find_id_button = findViewById<Button>(R.id.find_id)
        val find_pw_button = findViewById<Button>(R.id.find_pw)

        find_id_button.setOnClickListener {
            setFrag(0)
        }
        find_pw_button.setOnClickListener {
            setFrag(1)
        }
    }

    private fun setFrag(fragNum : Int) { //프래그먼트 바꾸는 함수
        val ft = supportFragmentManager.beginTransaction() //프래그먼트 관리(변수)

        val find_id_button = findViewById<Button>(R.id.find_id)
        val find_pw_button = findViewById<Button>(R.id.find_pw)

        when(fragNum){
            0 -> { //fragNum이 0일 경우
                ft.replace(R.id.main_frag, frag_find_id()).commit() //프래그먼트를 교체한다(activity_maind안에 FrameLayoutdml의 id가 main_frag임), 바꾸려는 액티비티
                find_id_button.setBackgroundResource(R.drawable.press_square_lite_blue_button) //background 속성 변경
                find_pw_button.setBackgroundResource(R.drawable.square_lite_blue_button)
            }
            1 -> {
                ft.replace(R.id.main_frag, frag_find_pw()).commit()
                find_id_button.setBackgroundResource(R.drawable.square_lite_blue_button)
                find_pw_button.setBackgroundResource(R.drawable.press_square_lite_blue_button)
            }
        }
    }
}