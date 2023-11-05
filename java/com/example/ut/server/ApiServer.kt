package com.example.ut.server

import com.example.ut.a_data.board
import com.example.ut.a_data.cate
import com.example.ut.a_data.category_all
import com.example.ut.a_data.change
import com.example.ut.a_data.chat
import com.example.ut.a_data.chat_num
import com.example.ut.a_data.findid
import com.example.ut.a_data.findpw
import com.example.ut.a_data.info
import com.example.ut.a_data.keywrod
import com.example.ut.a_data.keywrod_input
import com.example.ut.a_data.login_member_data
import com.example.ut.a_data.member_data
import com.example.ut.a_data.userid
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiServer {
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
        @Part("username") username: String,
        @Part userData: MultipartBody.Part,
        @Part("title") title: String
    ): Call<String>

    @POST("/board")
    fun board(): Call<board>

    @POST("/carto")
    fun carto(@Body userData : login_member_data): Call<category_all>

    @POST("/carto_input")
    fun carto_input(@Body userData : cate): Call<String>

    @POST("/keyword")
    fun keyword(@Body userData : userid): Call<List<keywrod>>

    @POST("/keyword_input")
    fun keyword_input(@Body userData : keywrod_input): Call<String>

    @POST("/alert")
    fun alert(@Body userData : userid): Call<board>

    @POST("/chat")
    fun chat(@Body userData : chat): Call<chat_num>
}