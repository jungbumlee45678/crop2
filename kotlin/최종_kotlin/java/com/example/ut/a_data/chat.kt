package com.example.ut.a_data

import java.sql.Timestamp

data class chat(
    val boardid:Int,
    val userid:String
)

data class chat_num(
    val num:Int
)

data class chat_board(
    val conid:Int,
    val boardid:Int,
    val title:String,
    val max_num:Int,
    val min_num:Int,
    val di_username:String,
    val bo_userid:String,
    val state:Int
)

data class message(
    val conid:Int,
    val userid:String,
    val content:String
)

data class conid(
    val conid: Int,
    val userid:String
)

data class content(
    val num:Int,
    val content: String,
    val userid:String,
    val time:Timestamp
)

data class state(
    val num:Int,
    val chat_num:Int,
    val userid:String
)