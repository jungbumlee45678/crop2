package com.example.stfrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class frag_find_id : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //여기 내용 실행
        val view = inflater.inflate(R.layout.frag_find_id, container, false) // setContentView의 프래그먼트 버전
        return view
    }
}