package com.example.ut.a_data

import java.sql.Timestamp

data class board(val items: List<Item>)

data class re_board(
    val num:Int,
    val title:String,
    val credit:Int,
    val content:String,
    val category:String
)

data class Item(
    val num: Int,
    val title: String,
    val username: String,
    val credit:Int,
    val views:Int,
    val address:String,
    val distance:Double
 )

data class board_info(
    val state:Int,
    val userid: String,
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
    val num:Int,
    val userid:String
)

data class search(
    val mode:String?,
    val userid:String?,
    val search:String?,
    val sale:Int?
)

data class filter_search(
    val userid:String?,
    val search:String?,
    val sale:Int?,
    val classification:String?,
    val category:ArrayList<String>?,
    val distance:Int,
    val min_credit:Int?,
    val max_credit:Int?
)

data class search_userid(
    val search:String
)