package com.example.ut.chatting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.conid
import com.example.ut.a_data.content
import com.example.ut.a_data.message
import com.example.ut.a_data.state
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.example.ut.server.MyWebSocketListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit

class chatting : AppCompatActivity() {
    private val webSocketScope = CoroutineScope(Dispatchers.IO)
    private lateinit var webSocket: WebSocket
    var message_num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        val intent = intent
        val preferences = getSharedPreferences("session", MODE_PRIVATE)

        val num = intent.getIntExtra("num",0)
        val title = intent.getStringExtra("title")
        val di_username = intent.getStringExtra("di_username")
        val bo_userid = intent.getStringExtra("bo_userid")
        val bo_state = intent.getIntExtra("state",0)

        val userid = preferences.getString("userid","")

        println("$num, $title, $di_username, $bo_userid, $userid")

        if(num==0){
            println("잘못된 요청입니다.")
            onBackPressed()
        }

        val state = findViewById<Button>(R.id.state)

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val call = apiService.message_load(conid(num))

        val scrollView  = findViewById<ScrollView>(R.id.chat) // 부모 레이아웃 ID로 변경
        val linearLayout = scrollView.getChildAt(0) as ViewGroup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title+"|"+di_username

        var layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        if(bo_state==0){
            state.setOnClickListener {
                if(userid==bo_userid){
                    val call = apiService.state(state(num))
                    call.enqueue(object:Callback<String>{
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if(response.isSuccessful){
                                showAlertDialog("처리되었습니다",0)
                            }else{
                                println("요청 실패")
                            }
                        }
                        override fun onFailure(call: Call<String>, t: Throwable) {
                            println("연결 실패")
                        }
                    })
                }else{
                    showAlertDialog("판매자가 아닙니다.")
                }
            }
        }

        call.enqueue(object:Callback<List<content>>{
            override fun onResponse(call: Call<List<content>>, response: Response<List<content>>) {
                if(response.isSuccessful){
                    val data = response.body()
                    println(data)

                    if(data!=null){
                        if(data.isEmpty()){
                            val dataObject = JSONObject()
                            dataObject.put("userid", userid)
                            dataObject.put("num", message_num)
                            dataObject.put("conid", num)

                            startWebSocket(dataObject, linearLayout, scrollView)
                        } else{
                            for(i in 0 until data!!.size) {
                                val layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                val newText = TextView(this@chatting)

                                newText.text = data!![i].content
                                newText.textSize = 20f
                                newText.setPadding((10 * resources.displayMetrics.density).toInt())

                                if (data!![i].userid == userid) {
                                    layoutParams.gravity = Gravity.RIGHT
                                } else {
                                    layoutParams.gravity = Gravity.LEFT
                                    message_num = data!![i].num
                                }

                                layoutParams.setMargins((10 * resources.displayMetrics.density).toInt())

                                newText.layoutParams = layoutParams
                                newText.setBackgroundResource(R.drawable.text_bar)

                                linearLayout.addView(newText)

                                if (i == data!!.size - 1) {
                                    val dataObject = JSONObject()
                                    dataObject.put("userid", userid)
                                    dataObject.put("num", message_num)
                                    dataObject.put("conid", num)
                                    startWebSocket(dataObject, linearLayout, scrollView)

                                    scrollView.post {
                                        scrollView.fullScroll(View.FOCUS_DOWN)
                                    }
                                }
                            }
                        }
                    }
                } else{
                    println("요청 실패")
                }
            }

            override fun onFailure(call: Call<List<content>>, t: Throwable) {
                println("연결 실패")
            }
        })

        val chat_input = findViewById<LinearLayout>(R.id.chat_input)
        val message = findViewById<EditText>(R.id.message)

        var send_ex = false

        val handler = Handler()
        val runnable = object : Runnable {
            override fun run() {
                if (!message.text.trim().isNullOrEmpty()) {
                    if(!send_ex){
                        val newButton = Button(this@chatting)
                        newButton.layoutParams = layoutParams
                        newButton.text = "Send"
                        newButton.isAllCaps = false

                        newButton.setOnClickListener {
                            val message_data = message(num,userid!!,message.text.toString())
                            val call = apiService.message(message_data)

                            call.enqueue(object : Callback<String> {
                                override fun onResponse(call: Call<String>, response: Response<String>) {
                                    if (response.isSuccessful) {

                                    }else{
                                        println("요청 실패")
                                    }
                                }
                                override fun onFailure(call: Call<String>, t: Throwable) {
                                    println("연결 실패")
                                }
                            })

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            val newText = TextView(this@chatting)

                            newText.text = message.text.toString()
                            newText.textSize = 20f
                            newText.setPadding((10 * resources.displayMetrics.density).toInt())

                            layoutParams.gravity = Gravity.RIGHT
                            layoutParams.setMargins((10 * resources.displayMetrics.density).toInt())

                            newText.layoutParams = layoutParams
                            newText.setBackgroundResource(R.drawable.text_bar)

                            linearLayout.addView(newText)

                            message.text = null

                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_DOWN)
                            }
                        }

                        chat_input.addView(newButton)
                        send_ex = true
                    }
                } else {
                    try {
                        chat_input.removeViewAt(2)
                        send_ex = false
                    } catch (e: NullPointerException) {

                    }
                }
                handler.postDelayed(this, 200)
            }
        }

        if(bo_state==0){
            handler.post(runnable)
        } else{
            message.setEnabled(false)
            message.setBackgroundResource(R.color.gray)
        }
    }

    fun intent(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun startWebSocket(dataObject: JSONObject, linearLayout:ViewGroup, scrollView:ScrollView){
        webSocketScope.launch {
            val client = OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS) // 읽기 타임아웃 설정
                .retryOnConnectionFailure(true) // 연결 실패 시 재시도
                .build()

            val IP = IP()

            val request = Request.Builder()
                .url(IP.ip10()) // WebSocket 서버 URL을 입력하세요.
                .build()

            val webSocketListener = MyWebSocketListener()
            webSocket = client.newWebSocket(request, webSocketListener)

            // 5초마다 서버로 데이터를 보냅니다.
            while (isActive) {
                // 서버로 보낼 데이터를 여기에 작성합니다.
                webSocket.send(dataObject.toString())
                val json = webSocketListener.json()
                if(json!=null){
                    for (i in 0 until json.length()) {
                        val jsonObject = json.getJSONObject(i)
                        val num = jsonObject.getInt("num")
                        val content = jsonObject.getString("content")
                        val time = jsonObject.getString("time")

                        if(num>message_num){
                            runOnUiThread {
                                val layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT
                                )
                                val newText = TextView(this@chatting)

                                newText.text = content
                                newText.textSize = 20f
                                newText.setPadding((10 * resources.displayMetrics.density).toInt())

                                layoutParams.gravity = Gravity.LEFT

                                layoutParams.setMargins((10 * resources.displayMetrics.density).toInt())

                                newText.layoutParams = layoutParams
                                newText.setBackgroundResource(R.drawable.text_bar)

                                linearLayout.addView(newText)

                                scrollView.post {
                                    scrollView.fullScroll(View.FOCUS_DOWN)
                                }
                            }

                            dataObject.put("num", num)
                            message_num = num
                        }
                    }
                }
                delay(100) // 0.1초 대기
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

    fun showAlertDialog(text:String,num:Int) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        builder.setCancelable(false)

        builder.setPositiveButton("확인") { _, _ ->
            intent()
        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        webSocket.close(1000, "User closed the connection")

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("tag","chat")
        startActivity(intent)
    }
}