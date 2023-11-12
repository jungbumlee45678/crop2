package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MainActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var image: ImageView? = null

    var imageuri = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermissions(this)

        val SingUp_Button = findViewById<Button>(R.id.SingUp_Button)

        image = findViewById(R.id.profileImageView)
        var image_b = false

        image?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
            image_b = true
        }

        SingUp_Button.setOnClickListener {
            if(image_b){
                val gson = GsonBuilder().setLenient().create()// 사용해서 [json 검사 통과] -> 일반적인 데이터도 통신 가능
                val retrofit = Retrofit.Builder()//[json 형식]으로 데이터 검사
                    .baseUrl("http://172.31.58.80:3000/") // 실제 엔드포인트 URL
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()

                val apiService =  retrofit.create(MyApi::class.java)

                val imageFile = File(absolutelyPath(Uri.parse(imageuri),this))
                val requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
                val imageBody = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                println(imageFile)
                println(requestFile)
                println(imageBody)

                val call2 = apiService.sendImage(imageBody)


                call2.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            showAlertDialog("회원가입 성공했습니다.",true)
                        } else {
                            // 서버 요청 실패
                            showAlertDialog("프로필 사진 첨부가 실패 하였습니다.",true)
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        println("Network Error "+t.message ?: "Unknown error")
                        showAlertDialog("프로필 사진 첨부가 실패 하였습니다.",true)
                    }
                })
            }else{
                showAlertDialog("프로필 사진을 첨부해주세요.")
            }
        }
    }

    fun absolutelyPath(path: Uri?, context : Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)

        return result!!
    }


    fun showAlertDialog(text:String,bool:Boolean=false) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)
        if(bool){
            builder.setCancelable(false)
        }

        builder.setPositiveButton("확인") { _, _ ->

        }

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // 이미지 선택 후 결과 처리
            val selectedImage = data.data
            image?.setImageURI(selectedImage)
            imageuri = selectedImage.toString()
        }
    }

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf<String>(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun verifyStoragePermissions(activity: Activity?) {
        val permission = ActivityCompat.checkSelfPermission(
            activity!!,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            println(1)
            ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
        }else{
            println(0)
        }
    }
}