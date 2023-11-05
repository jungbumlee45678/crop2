package com.example.ut.a_data

data class board(val items: List<Item>)

data class Item(
    val num: Int,
    val title: String,
    val username: String
 )
