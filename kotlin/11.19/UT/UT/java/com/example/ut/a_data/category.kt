package com.example.ut.a_data

data class category(
    val classification:String,
    val category:String
)

data class category_all(
    val data1:List<category>,
    val data2:List<category>
)

data class class_ca(
    val classification:String,
    val image:String
)