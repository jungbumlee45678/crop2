package com.example.ut.server

class IP {
    var co7 = "172.31.57.164"
    var co8 = "172.31.58.80"

    var co7_2 = "172.31.57.158"

    val co = "192.168.0.21"
    val server = "13.124.55.241"

    val address = co

    fun ip():String{
        var IP = "http://" + address + ":3000/"
        return IP
    }

    fun ip10():String{
        var IP = "ws://" + address + ":3010/"
        return IP
    }
    //172.31.57.164 -> 7층 서버 (3번째 줄)
    //172.31.58.80 -> 8층 서버 (2번째 줄)

}