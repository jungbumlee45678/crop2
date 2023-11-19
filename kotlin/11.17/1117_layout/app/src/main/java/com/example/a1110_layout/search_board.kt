package com.example.a1110_layout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog

class search_board : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_board)

        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        findViewById<Button>(R.id.filter).setOnClickListener {
            bottomSheetDialog.show()
        }
    }
}