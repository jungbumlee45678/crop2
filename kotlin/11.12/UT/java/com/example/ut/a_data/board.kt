package com.example.ut.a_data

import java.sql.Timestamp

data class board(val items: List<Item>)

data class Item(
    val num: Int,
    val title: String,
    val username: String,
    val credit:Int,
    val views:Int,
    val address:String
 )

data class board_info(
    val username: String,
    val address: String,
    val title: String,
    val category: String,
    val credit:Int,
    val views:Int,
    val content:String,
    val time:Timestamp
)

data class board_num(
    val num:Int
)

data class search(
    val userid:String?,
    val search:String?
)

data class search_userid(
    val search:String
)