package com.example.ut.server

import com.example.ut.a_data.board
import com.example.ut.a_data.board_info
import com.example.ut.a_data.board_num
import com.example.ut.a_data.cate
import com.example.ut.a_data.category_all
import com.example.ut.a_data.change
import com.example.ut.a_data.chat
import com.example.ut.a_data.chat_board
import com.example.ut.a_data.chat_num
import com.example.ut.a_data.class_ca
import com.example.ut.a_data.conid
import com.example.ut.a_data.content
import com.example.ut.a_data.findid
import com.example.ut.a_data.findpw
import com.example.ut.a_data.info
import com.example.ut.a_data.keywrod
import com.example.ut.a_data.keywrod_input
import com.example.ut.a_data.login_member_data
import com.example.ut.a_data.member_data
import com.example.ut.a_data.message
import com.example.ut.a_data.search
import com.example.ut.a_data.search_userid
import com.example.ut.a_data.state
import com.example.ut.a_data.userid
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiServer {
    @Multipart
    @POST("/profile")
    fun profile(
        @Part("userid") userid:String,
        @Part image: MultipartBody.Part
    ): Call<String>

    @POST("/signup")
    fun getsignup(@Body userData: member_data): Call<String>

    @POST("/login")
    fun getlogin(@Body userData: login_member_data): Call<String>

    @POST("/findid")
    fun findid(@Body userData: findid): Call<String>

    @POST("/findpw")
    fun findpw(@Body userData: findpw): Call<String>

    @POST("/email")
    fun email(@Body userData: login_member_data): Call<String>

    @POST("/emailsend")
    fun emailsend(@Body userData: login_member_data): Call<String>

    @POST("/emailch")
    fun emailch(@Body userData: login_member_data): Call<String>

    @POST("/info")
    fun info(@Body userData: login_member_data): Call<List<info>>

    @POST("/change_username")
    fun ch_name(@Body userData: change): Call<String>

    @POST("/change_address")
    fun ch_address(@Body userData: change): Call<String>

    @POST("/change_email")
    fun ch_email(@Body userData: change): Call<String>

    @POST("/change_pw")
    fun ch_pw(@Body userData: change): Call<String>

    @Multipart
    @POST("/video")
    fun uploadVideo(
        @Part("userid") userid: String,
        @Part userData: MultipartBody.Part,
        @Part("username") username: String,
        @Part("title") title: String,
        @Part("category") category: String,
        @Part("content") content: String,
        @Part("credit") credit: String
    ): Call<String>

    @POST("/board")
    fun board(@Body userData : search): Call<board>

    @POST("/search")
    fun search(@Body userData : userid): Call<List<search_userid>>

    @POST("/board_info")
    fun board_info(@Body userData : board_num): Call<board_info>

    @POST("/carto")
    fun carto(@Body userData : login_member_data): Call<category_all>

    @POST("/carto_input")
    fun carto_input(@Body userData : cate): Call<String>

    @POST("/class_ca")
    fun class_ca(): Call<List<class_ca>>

    @POST("/keyword")
    fun keyword(@Body userData : userid): Call<List<keywrod>>

    @POST("/keyword_input")
    fun keyword_input(@Body userData : keywrod_input): Call<String>

    @POST("/alert")
    fun alert(@Body userData : userid): Call<board>

    @POST("/chat")
    fun chat(@Body userData : chat): Call<chat_num>

    @POST("/chat_board")
    fun chat_board(@Body userData : userid): Call<List<chat_board>>

    @POST("/state")
    fun state(@Body userData : state): Call<String>

    @POST("/message")
    fun message(@Body userData : message): Call<String>

    @POST("/message_load")
    fun message_load(@Body userData : conid): Call<List<content>>
}