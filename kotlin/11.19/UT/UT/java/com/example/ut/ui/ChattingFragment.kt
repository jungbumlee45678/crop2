package com.example.ut.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ut.databinding.FragmentChattingBinding
import com.example.ut.R
import com.example.ut.a_data.chat_board
import com.example.ut.a_data.conid
import com.example.ut.a_data.userid
import com.example.ut.alarm.alarm
import com.example.ut.chatting.chatting
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChattingFragment : Fragment() {
    private var _binding: FragmentChattingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chatting, container, false)

        //_binding = FragmentChattingBinding.inflate(inflater, container, false)
        _binding = FragmentChattingBinding.bind(view)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        val preferences = requireActivity().getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        val userid = preferences.getString("userid", "")

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val call = apiService.chat_board(userid(userid!!))

        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipe)
        swipe.setOnRefreshListener {
            val call = apiService.chat_board(userid(userid!!))
            call.enqueue(object:Callback<List<chat_board>>{
                override fun onResponse(call: Call<List<chat_board>>, response: Response<List<chat_board>>) {
                    if(response.isSuccessful){
                        val data = response.body()

                        val scrollView  = view.findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                        val linearLayout = scrollView.getChildAt(0) as ViewGroup

                        linearLayout.removeAllViews()

                        for (i in 0 until data!!.size){
                            var layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            val newTextView = TextView(requireContext())

                            var num = 0
                            if(data!![i].min_num<=data!![i].num){
                                num = data!![i].num
                            }else{
                                num = data!![i].min_num
                            }


                            newTextView.text = data!![i].title+"|"+data!![i].di_username+"|"+(data!![i].max_num - num)
                            newTextView.textSize = 20f

                            newTextView.layoutParams = layoutParams
                            newTextView.setPadding((20 * resources.displayMetrics.density).toInt())

                            newTextView.setBackgroundResource(R.color.gray)

                            newTextView.setOnClickListener {
                                val intent = Intent(requireContext(),chatting::class.java)
                                intent.putExtra("num",data!![i].conid)
                                intent.putExtra("di_username",data!![i].di_username)
                                intent.putExtra("title",data!![i].title)
                                intent.putExtra("bo_userid",data!![i].bo_userid)
                                intent.putExtra("state",data!![i].state)
                                startActivity(intent)
                            }

                            linearLayout.addView(newTextView)
                        }
                    }else{
                        println("요청 실패")
                    }
                }
                override fun onFailure(call: Call<List<chat_board>>, t: Throwable) {
                    println("연결 실패")
                }
            })
            swipe.isRefreshing = false
        }

        call.enqueue(object:Callback<List<chat_board>>{
            override fun onResponse(call: Call<List<chat_board>>, response: Response<List<chat_board>>) {
                if(response.isSuccessful){
                    val data = response.body()

                    val scrollView  = view.findViewById<ScrollView>(R.id.main2) // 부모 레이아웃 ID로 변경
                    val linearLayout = scrollView.getChildAt(0) as ViewGroup

                    for (i in 0 until data!!.size){
                        var layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )

                        val newTextView = TextView(requireContext())

                        var num = 0
                        if(data!![i].min_num<=data!![i].num){
                            num = data!![i].num
                        }else{
                            num = data!![i].min_num
                        }


                        newTextView.text = data!![i].title+"|"+data!![i].di_username+"|"+(data!![i].max_num - num)
                        newTextView.textSize = 20f

                        newTextView.layoutParams = layoutParams
                        newTextView.setPadding((20 * resources.displayMetrics.density).toInt())

                        newTextView.setBackgroundResource(R.color.gray)

                        newTextView.setOnClickListener {
                            val intent = Intent(requireContext(),chatting::class.java)
                            intent.putExtra("num",data!![i].conid)
                            intent.putExtra("di_username",data!![i].di_username)
                            intent.putExtra("title",data!![i].title)
                            intent.putExtra("bo_userid",data!![i].bo_userid)
                            intent.putExtra("state",data!![i].state)
                            startActivity(intent)
                        }

                        linearLayout.addView(newTextView)
                    }
                }else{
                    println("요청 실패")
                }
            }
            override fun onFailure(call: Call<List<chat_board>>, t: Throwable) {
                println("연결 실패")
            }
        })

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_alarm -> {
                    val intent = Intent(requireContext(), alarm::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}