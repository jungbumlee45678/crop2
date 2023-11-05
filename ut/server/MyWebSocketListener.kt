package com.example.ut.server

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray

class MyWebSocketListener() : WebSocketListener() {
    private var webSocket: WebSocket? = null
    private var jsonArray: JSONArray? = null

    override fun onOpen(webSocket: WebSocket, response: Response) {
        this.webSocket = webSocket
        println("WebSocket 연결이 열렸습니다.")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // 웹소켓으로부터 메시지를 수신할 때 호출됨
        // 수신한 JSON 문자열을 파싱하여 데이터 추출
        try {
            jsonArray = JSONArray(text)
            //num,title,username

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("서버로부터 바이트 데이터를 수신했습니다.")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        println("WebSocket 연결이 종료되었습니다.")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("WebSocket 연결 실패 또는 오류 발생: ${t.message}")
    }

    fun json():JSONArray?{
        return jsonArray
    }
}