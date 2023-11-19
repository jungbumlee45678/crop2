package com.example.ut.board

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ut.R
import com.example.ut.a_data.category_all
import com.example.ut.a_data.login_member_data
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat

class write_page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_page)

        val recording = findViewById<Button>(R.id.recording)
        val title = findViewById<TextView>(R.id.title)
        val category = findViewById<TextView>(R.id.category)
        val text = findViewById<TextView>(R.id.text)

        val credit: EditText = findViewById(R.id.credit)
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

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiserver = retrofit.create(ApiServer::class.java)
        val data = login_member_data(
            "",
            ""
        )

        val call = apiserver.carto(data)

        call.enqueue(object : Callback<category_all> {
            override fun onResponse(call: Call<category_all>, response: Response<category_all>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    // 서버 응답 처리
                    val data = responseBody!!.data1.map { it.classification }.distinct()

                    val classification = responseBody!!.data1.map { it.classification }
                    val category = responseBody!!.data1.map { it.category }
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
}
