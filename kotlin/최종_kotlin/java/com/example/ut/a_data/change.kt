package com.example.ut.a_data

data class change(
    val userid:String,
    val userpw:String,
    val change:String
)

data class change_address(
    val userid:String,
    val userpw:String,
    val change:String,
    val latitude:Double,
    val longitude:Double
)
