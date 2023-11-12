package com.example.ut.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ut.R
import com.example.ut.login.LoginActivity
import com.example.ut.alarm.alarm_keyword
import com.example.ut.databinding.FragmentInformationBinding
import com.example.ut.info.interest_thing
import com.example.ut.info.my_info
import com.example.ut.info.purchase_history
import com.example.ut.info.sale_history
import com.example.ut.server.IP

class InformationFragment : Fragment() {
    private var _binding: FragmentInformationBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_information, container, false)

        val preferences = requireActivity().getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        // "session"이라는 이름의 SharedPreferences 파일을 가져옴(없으면 자동으로 생성)
        val username = preferences.getString("username", "")
        val userid = preferences.getString("userid", "")

        view.findViewById<TextView>(R.id.Nikname).text = username

        val IP = IP()

        val profile = view.findViewById<ImageView>(R.id.profileImageView)
        Glide.with(requireContext())
            .load(IP.ip()+"profile/"+userid)
            .placeholder(R.drawable.baseline_person_24) // 기본 이미지 설정
            .into(profile)

        val logout_button = view.findViewById<Button>(R.id.logout)
        logout_button.setOnClickListener{
            showAlertDialog("로그아웃하시겠습니까?")
        }

        val change_info_button = view.findViewById<Button>(R.id.my_info)
        change_info_button.setOnClickListener{
            intent(my_info::class.java)
        }

        val gs_thing_button = view.findViewById<Button>(R.id.gs_thing)
        gs_thing_button.setOnClickListener{
            intent(interest_thing::class.java, true)
        }

        val sell_thing_button = view.findViewById<Button>(R.id.sell_thing)
        sell_thing_button.setOnClickListener{
            intent(sale_history::class.java)
        }

        val buy_thing_button = view.findViewById<Button>(R.id.buy_thing)
        buy_thing_button.setOnClickListener{
            intent(purchase_history::class.java)
        }

        val key_word_button = view.findViewById<Button>(R.id.key_word)
        key_word_button.setOnClickListener{
            intent(alarm_keyword::class.java)
        }

        return view


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun intent(page:Class<*>,bool:Boolean=false){
        val intent = Intent(requireContext(), page)
        if(bool){
            intent.putExtra("start",0)
        }
        startActivity(intent)
    }

    fun showAlertDialog(text:String) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(requireContext())

        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        builder.setNegativeButton("확인") { _, _ ->
            val preferences: SharedPreferences = requireContext().getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = preferences.edit()
            preferences.edit().clear().apply()
            editor.apply()

            // MainActivity로 이동합니다.
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        builder.setPositiveButton("취소") { _, _ ->

        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }
}