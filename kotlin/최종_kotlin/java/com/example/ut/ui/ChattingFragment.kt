package com.example.ut.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ut.R
import com.example.ut.a_data.chat_board
import com.example.ut.a_data.userid
import com.example.ut.alarm.alarm
import com.example.ut.chatting.chatting
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChattingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chatting, container, false)

        //_binding = FragmentChattingBinding.inflate(inflater, container, false)
        //_binding = FragmentChattingBinding.bind(view)

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

                        if(data!!.isNotEmpty()) {
                            linearLayout.removeAllViews()

                            for (i in 0 until data!!.size) {
                                val linearLayout1 = LinearLayout(requireContext())
                                linearLayout1.orientation = LinearLayout.HORIZONTAL

                                linearLayout1.gravity = Gravity.CENTER

                                val Linear_Params = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                    LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                                )

                                linearLayout1.setPadding((10 * resources.displayMetrics.density).toInt())
                                linearLayout1.layoutParams =Linear_Params

                                val linear = LinearLayout(requireContext())
                                var layoutParams = LinearLayout.LayoutParams(
                                    (80 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                    (80 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                )

                                linear.layoutParams = layoutParams

                                val view = ImageView(requireContext())
                                view.setBackgroundResource(R.drawable.rounded_corner_border_2)

                                layoutParams = LinearLayout.LayoutParams(
                                    (80 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                    (80 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                )

                                layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
                                layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                                view.layoutParams = layoutParams

                                Glide.with(requireContext())
                                    .load(IP.ip() + "image/" + data!![i].boardid)
                                    .placeholder(R.color.black) // 기본 이미지 설정
                                    .override(
                                        (80 * resources.displayMetrics.density).toInt(),
                                        (80 * resources.displayMetrics.density).toInt()
                                    )
                                    .into(view)

                                view.clipToOutline = true //둥글게 만들기
                                view.scaleType = ScaleType.CENTER_CROP

                                linear.addView(view)

                                val newLinear = LinearLayout(requireContext())
                                newLinear.orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                    LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                                )
                                layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()

                                newLinear.layoutParams = layoutParams

                                val title = TextView(requireContext())

                                title.text = data[i].title
                                title.textSize = 20f
                                title.ellipsize = TextUtils.TruncateAt.END
                                title.maxLines = 1

                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
                                    LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                                )
                                title.layoutParams = layoutParams

                                val newlinear2 = LinearLayout(requireContext())
                                newlinear2.orientation = LinearLayout.HORIZONTAL

                                val di_username = TextView(requireContext())
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )

                                layoutParams.weight = 1f

                                val displayMetrics = resources.displayMetrics
                                val halfScreenWidth = displayMetrics.widthPixels / 2.6

                                di_username.maxWidth = halfScreenWidth.toInt()
                                di_username.layoutParams = layoutParams
                                di_username.text = data[i].di_username
                                di_username.textSize = 20f
                                di_username.ellipsize = TextUtils.TruncateAt.END
                                di_username.maxLines = 1

                                val count = TextView(requireContext())
                                count.setBackgroundResource(R.drawable.rounded_corner_border)
                                count.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_blue2)
                                layoutParams = LinearLayout.LayoutParams(
                                    (30 * resources.displayMetrics.density).toInt(),
                                    (30 * resources.displayMetrics.density).toInt()
                                )

                                count.layoutParams = layoutParams
                                count.text = (data[i].max_num - data[i].min_num).toString()
                                count.setTextColor(Color.WHITE)
                                count.gravity = Gravity.CENTER
                                count.textSize = 20f

                                newlinear2.addView(di_username)
                                newlinear2.addView(count)

                                linearLayout1.setOnClickListener {
                                    val intent = Intent(requireContext(), chatting::class.java)
                                    intent.putExtra("num", data[i].conid)
                                    intent.putExtra("boardid", data[i].boardid)
                                    intent.putExtra("di_username", data[i].di_username)
                                    intent.putExtra("title", data[i].title)
                                    intent.putExtra("bo_userid", data[i].bo_userid)
                                    intent.putExtra("state", data[i].state)
                                    startActivity(intent)
                                }

                                newLinear.addView(title)
                                newLinear.addView(newlinear2)

                                linearLayout1.addView(linear)
                                linearLayout1.addView(newLinear)

                                linearLayout.addView(linearLayout1)
                            }
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

                    if(data!!.isNotEmpty()) {
                        linearLayout.removeAllViews()

                        for (i in 0 until data!!.size) {
                            val linearLayout1 = LinearLayout(requireContext())
                            linearLayout1.orientation = LinearLayout.HORIZONTAL

                            linearLayout1.gravity = Gravity.CENTER

                            val Linear_Params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                            )

                            linearLayout1.setPadding((10 * resources.displayMetrics.density).toInt())
                            linearLayout1.layoutParams =Linear_Params

                            val linear = LinearLayout(requireContext())
                            var layoutParams = LinearLayout.LayoutParams(
                                (80 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                (80 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )

                            linear.layoutParams = layoutParams

                            val view = ImageView(requireContext())
                            view.setBackgroundResource(R.drawable.rounded_corner_border_2)

                            layoutParams = LinearLayout.LayoutParams(
                                (80 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                (80 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )

                            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.gravity = Gravity.CENTER_HORIZONTAL
                            view.layoutParams = layoutParams

                            Glide.with(requireContext())
                                .load(IP.ip() + "image/" + data!![i].boardid)
                                .placeholder(R.color.black) // 기본 이미지 설정
                                .override(
                                    (80 * resources.displayMetrics.density).toInt(),
                                    (80 * resources.displayMetrics.density).toInt()
                                )
                                .into(view)

                            view.clipToOutline = true //둥글게 만들기
                            view.scaleType = ScaleType.CENTER_CROP

                            linear.addView(view)

                            val newLinear = LinearLayout(requireContext())
                            newLinear.orientation = LinearLayout.VERTICAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                            )
                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()

                            newLinear.layoutParams = layoutParams

                            val title = TextView(requireContext())

                            title.text = data[i].title
                            title.textSize = 20f
                            title.ellipsize = TextUtils.TruncateAt.END
                            title.maxLines = 1

                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
                                LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                            )
                            title.layoutParams = layoutParams

                            val newlinear2 = LinearLayout(requireContext())
                            newlinear2.orientation = LinearLayout.HORIZONTAL

                            val di_username = TextView(requireContext())
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            layoutParams.weight = 1f

                            val displayMetrics = resources.displayMetrics
                            val halfScreenWidth = displayMetrics.widthPixels / 2.6

                            di_username.maxWidth = halfScreenWidth.toInt()
                            di_username.layoutParams = layoutParams
                            di_username.text = data[i].di_username
                            di_username.textSize = 20f
                            di_username.ellipsize = TextUtils.TruncateAt.END
                            di_username.maxLines = 1

                            val count = TextView(requireContext())
                            count.setBackgroundResource(R.drawable.rounded_corner_border)
                            count.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.dark_blue2)
                            layoutParams = LinearLayout.LayoutParams(
                                (30 * resources.displayMetrics.density).toInt(),
                                (30 * resources.displayMetrics.density).toInt()
                            )

                            count.layoutParams = layoutParams
                            count.text = (data[i].max_num - data[i].min_num).toString()
                            count.setTextColor(Color.WHITE)
                            count.gravity = Gravity.CENTER
                            count.textSize = 20f

                            newlinear2.addView(di_username)
                            newlinear2.addView(count)

                            linearLayout1.setOnClickListener {
                                val intent = Intent(requireContext(), chatting::class.java)
                                intent.putExtra("num", data[i].conid)
                                intent.putExtra("boardid", data[i].boardid)
                                intent.putExtra("di_username", data[i].di_username)
                                intent.putExtra("title", data[i].title)
                                intent.putExtra("bo_userid", data[i].bo_userid)
                                intent.putExtra("state", data[i].state)
                                startActivity(intent)
                            }

                            newLinear.addView(title)
                            newLinear.addView(newlinear2)

                            linearLayout1.addView(linear)
                            linearLayout1.addView(newLinear)

                            linearLayout.addView(linearLayout1)
                        }
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
}