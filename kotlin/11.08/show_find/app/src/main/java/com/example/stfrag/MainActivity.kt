package com.example.stfrag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 액티비티 설정

        val find_id_pw_button = findViewById<TextView>(R.id.find_id_pw)

        find_id_pw_button.setOnClickListener{
            val intent = Intent(this, show_find_id_pw::class.java)
            startActivity(intent)
        }

        val Sign_up_button = findViewById<TextView>(R.id.sign_up)
    }
}