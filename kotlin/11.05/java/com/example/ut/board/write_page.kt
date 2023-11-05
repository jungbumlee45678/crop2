package com.example.ut.board

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.R

class write_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_page)

        val recording = findViewById<Button>(R.id.recording)
        val title = findViewById<TextView>(R.id.title)

        recording.setOnClickListener{
            val intent = Intent(this, video_start::class.java)
            intent.putExtra("title", title.text.toString())
            startActivity(intent)
        }
    }
}
