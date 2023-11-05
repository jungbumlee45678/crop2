package com.example.ut.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.ut.databinding.FragmentChattingBinding
import com.example.ut.R
import com.example.ut.alarm.alarm
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
import java.util.concurrent.TimeUnit

class ChattingFragment : Fragment() {
    private var _binding: FragmentChattingBinding? = null
    private val binding get() = _binding!!
    private val webSocketScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chatting, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        val preferences = requireActivity().getSharedPreferences("session", AppCompatActivity.MODE_PRIVATE)
        val userid = preferences.getString("userid", "")

        val dataObject = JSONObject()
        dataObject.put("userid",userid)
        startWebSocket(dataObject)

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

    private fun startWebSocket(dataObject: JSONObject) {
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
            val webSocket = client.newWebSocket(request, webSocketListener)

            // 5초마다 서버로 데이터를 보냅니다.
            while (isActive) {
                // 서버로 보낼 데이터를 여기에 작성합니다.
                webSocket.send(dataObject.toString())
                val json = webSocketListener.json()
                if(json!=null){
                    for (i in 0 until json.length()) {
                        val jsonObject = json.getJSONObject(i)
                        val conid = jsonObject.getInt("conid")
                    }
                }
                delay(5000) // 5초 대기
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}