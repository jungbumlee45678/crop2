package com.example.ut.info

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.ut.R
import com.example.ut.a_data.info
import com.example.ut.a_data.login_member_data
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class my_info : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","")

        val my_name =  findViewById<TextView>(R.id.my_name)
        val my_id =  findViewById<TextView>(R.id.my_id)
        val my_email =  findViewById<TextView>(R.id.my_email)
        val my_address =  findViewById<TextView>(R.id.my_address)

        val IP = IP()

        val profile = findViewById<ImageView>(R.id.profileImageView)

        Glide.with(this)
            .load(IP.ip()+"profile/"+userid)
            .placeholder(R.drawable.baseline_person_24) // 기본 이미지 설정
            .override(
                (100 * resources.displayMetrics.density).toInt(),
                (100 * resources.displayMetrics.density).toInt()
            )
            .into(profile)

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)

        val data = login_member_data(userid!!,"")

        val call = apiService.info(data)

        call.enqueue(object : Callback<List<info>> {
            override fun onResponse(call: Call<List<info>>, response: Response<List<info>>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if(responseBody!=null){
                        my_name.text =responseBody[0].username
                        my_id.text = userid
                        my_email.text = responseBody[0].email
                        my_address.text = responseBody[0].address
                    }
                } else {

                }
            }

            override fun onFailure(call: Call<List<info>>, t: Throwable) {
                println(t.message)
            }
        })

        val change_pw_button = findViewById<Button>(R.id.change_pw)
        change_pw_button.setOnClickListener{
            val intent = Intent(this, change_info_before::class.java)
            startActivity(intent)
        }

        val change_name_button = findViewById<Button>(R.id.change_name)
        change_name_button.setOnClickListener{
            val intent = Intent(this, change_name::class.java)
            startActivity(intent)
        }

        val change_email_button = findViewById<Button>(R.id.change_email)
        change_email_button.setOnClickListener{
            val intent = Intent(this, change_email::class.java)
            startActivity(intent)
        }

        val change_address_button = findViewById<Button>(R.id.change_address)
        change_address_button.setOnClickListener{
            val intent = Intent(this, change_address::class.java)
            startActivity(intent)
        }


    }
}