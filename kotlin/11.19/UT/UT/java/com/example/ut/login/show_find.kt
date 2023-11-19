package com.example.ut.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.ut.R

class show_find : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        C>사용자>(이름)>AndroidStudioProjects>(프로젝트이름)>app>src>main>AndroidManifest에서
        application의 속성에서 android:enableOnBackInvokedCallback="true"를 추가해야 정상작동
        사용자가 뒤로가기 버튼을 눌렀을 때 백 스택이 변경될 때 콜백을 활성화하도록 하는 데 사용됩니다.
        */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_find)

        val intent = intent
        val find = intent.getStringExtra("find")

        if(find.equals("id")){
            setFrag(0)
        }else{
            setFrag(1)
            findViewById<TextView>(R.id.find).text = "비밀번호 찾기"
        }

        val find_id_button = findViewById<Button>(R.id.find_id)
        val find_pw_button = findViewById<Button>(R.id.find_pw)

        find_id_button.setOnClickListener {
            setFrag(0)
            findViewById<TextView>(R.id.find).text = "아이디 찾기"
        }
        find_pw_button.setOnClickListener {
            setFrag(1)
            findViewById<TextView>(R.id.find).text = "비밀번호 찾기"
        }
    }

    private fun setFrag(fragNum : Int) { //프래그먼트 바꾸는 함수
        val ft = supportFragmentManager.beginTransaction() //프래그먼트 관리(변수)

        val find_id_button = findViewById<Button>(R.id.find_id)
        val find_pw_button = findViewById<Button>(R.id.find_pw)

        when(fragNum){
            0 -> { //fragNum이 0일 경우
                ft.replace(R.id.main_frag, find_id_Activity()).commit() //프래그먼트를 교체한다(activity_maind안에 FrameLayoutdml의 id가 main_frag임), 바꾸려는 액티비티
                find_id_button.setBackgroundResource(R.drawable.square_dark_blue) //background 속성 변경
                find_pw_button.setBackgroundResource(R.drawable.square_lite_blue)
            }
            1 -> {
                ft.replace(R.id.main_frag, find_pw_Activity()).commit()
                find_id_button.setBackgroundResource(R.drawable.square_lite_blue)
                find_pw_button.setBackgroundResource(R.drawable.square_dark_blue)
            }
        }
    }
}