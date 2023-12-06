package com.example.ut.board

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.category_all
import com.example.ut.a_data.login_member_data
import com.example.ut.a_data.re_board
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.util.prefs.Preferences

class write_page : AppCompatActivity() {
    private var num:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_page)

        num = intent.getIntExtra("num",0)
        val recording = findViewById<Button>(R.id.recording)

        val title = findViewById<TextView>(R.id.title)
        val category = findViewById<EditText>(R.id.category)
        val text = findViewById<TextView>(R.id.text)
        val credit: EditText = findViewById(R.id.credit)

        if(num != 0){
            val last_line = findViewById<LinearLayout>(R.id.last_line)
            val new_button = Button(this)

            new_button.text = "수정"
            new_button.setBackgroundResource(R.drawable.square_lite_blue)

            new_button.setOnClickListener {
                val IP = IP()

                val retrofit = Retrofit.Builder()
                    .baseUrl(IP.ip())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiserver = retrofit.create(ApiServer::class.java)

                val re_board = re_board(
                    num!!,
                    title.text.toString(),
                    credit.text.toString().replace(",","").toInt(),
                    text.text.toString(),
                    category.text.toString()
                )

                val call = apiserver.re_board(re_board)
                
                call.enqueue(object:Callback<String>{
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if(response.isSuccessful){
                            val intent = Intent(this@write_page, board_info::class.java)
                            intent.putExtra("num", num)
                            startActivity(intent)
                        }else{
                            println("요청 실패")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("연결 실패")
                    }
                })
            }

            last_line.addView(new_button,0)
        }

        val intent = intent
        title.text = intent.getStringExtra("title")
        text.text = intent.getStringExtra("text")

        if(intent.getStringExtra("credit")!=null){
            credit.setText(intent.getStringExtra("credit"))
        }

        category.setText(intent.getStringExtra("category"))

        category.setOnClickListener {
            val intent = Intent(this, com.example.ut.board.category::class.java)
            intent.putExtra("num", num)
            intent.putExtra("title",title.text.toString())
            intent.putExtra("text",text.text.toString())
            intent.putExtra("credit",credit.text.toString())
            intent.putExtra("category",category.text.toString())
            startActivity(intent)
        }

        credit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                if (editable != null && editable.isNotEmpty()) {
                    val creditValue = editable.toString().replace(",", "")

                    val formattedValue = NumberFormat.getNumberInstance().format(creditValue.toLong())
                    credit.removeTextChangedListener(this)
                    credit.setText(formattedValue)
                    credit.setSelection(formattedValue.length)
                    credit.addTextChangedListener(this)
                }
            }
        })

        recording.setOnClickListener{
            if(title.text.toString() == ""){
                showAlertDialog("제목을 입력해주세요")
            } else if(category.text.toString() == ""){
                showAlertDialog("카테고리를 선택해주세요")
            } else if(text.text.toString() == ""){
                showAlertDialog("내용을 입력해주세요")
            } else if(credit.text.toString() == ""){
                showAlertDialog("가격을 입력해주세요")
            } else{
                val intent = Intent(this, video_start::class.java)
                intent.putExtra("num", num)
                intent.putExtra("title", title.text.toString())
                intent.putExtra("category", category.text.toString())
                intent.putExtra("text", text.text.toString())
                intent.putExtra("credit", credit.text.toString().replace(",",""))
                startActivity(intent)
            }
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

    override fun onBackPressed() {
        if(num != 0){
            super.onBackPressed()
        }else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
