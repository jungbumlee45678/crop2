package com.example.ut.board

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import com.example.ut.server.ProgressRequestBody
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class video_start : AppCompatActivity() {
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var currentRecording: Recording? = null

    private var bool = true

    fun bindPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        var preview : Preview = Preview.Builder()
            .build()

        cameraProviderFuture.addListener({
            // 카메라 프로바이더 가져오기
            val cameraProvider = cameraProviderFuture.get()

            // 프리뷰 구성
            val preview = Preview.Builder()
                .build()
            val previewView:PreviewView = findViewById(R.id.previewView)

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val qualitySelector = QualitySelector.fromOrderedList(
                listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // 카메라 바인딩
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA // 뒷 카메라 선택
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("MissingPermission")
    fun startvideo(){
        // Create MediaStoreOutputOptions for our recorder
        val name = "UT-" +
                SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(this.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

// 2. Configure Recorder and Start recording to the mediaStoreOutput.
        currentRecording = videoCapture.output
            .prepareRecording(this, mediaStoreOutput)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(this), captureListener)
    }

    var time_ex = false
    private val captureListener = Consumer<VideoRecordEvent> { event -> //넘어가는 거 제작 [뒤로가기 없애고/바로 진행]
        if (event is VideoRecordEvent.Start) {
            // 녹화가 시작되면 녹화 시간 업데이트 시작
            time_ex = true
        }

        if(time_ex){
            val stats = event.recordingStats
            //val size = stats.numBytesRecorded / 1000
            val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)

            val hours = time / 3600
            val minutes = (time % 3600) / 60
            val seconds = time % 60
            val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            //var text = "recorded ${size}KB, in ${time}second"

            runOnUiThread {
                val time = findViewById<TextView>(R.id.time)
                time.setVisibility(View.VISIBLE)
                time.text = formattedTime
            }
        }

        if(event is VideoRecordEvent.Finalize){
            time_ex = false

            val IP = IP()
            val uri = uri()

            runOnUiThread {
                val smallLayout = findViewById<LinearLayout>(R.id.smallLayout)
                val main2 = findViewById<LinearLayout>(R.id.main2)

                smallLayout.setVisibility(View.VISIBLE)
                main2.setVisibility(View.VISIBLE)

                main2.bringToFront()
                smallLayout.bringToFront()
            }

            val gson = GsonBuilder().setLenient().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(IP.ip()) // 서버 기본 URL을 여기에 적어주세요
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            val apiService = retrofit.create(ApiServer::class.java)
            val file = File(uri.getRealPathFromURI(this, event.outputResults.outputUri))

            val videoRequestBody = ProgressRequestBody(file, "video/*".toMediaType()) { progress ->
                // 업로드 진행률 처리
                val percentage = (100 * progress).toInt()
                runOnUiThread {
                    val text = findViewById<TextView>(R.id.text)
                    text.text = "업로드 진행률 : "+percentage
                }
            }

            val videoPart = MultipartBody.Part.createFormData("video", file.name, videoRequestBody)

            val intent = intent
            val title = intent.getStringExtra("title")
            val category = intent.getStringExtra("category")
            val text = intent.getStringExtra("text")
            val credit = intent.getStringExtra("credit")

            val preferences = getSharedPreferences("session", MODE_PRIVATE)
            val username = preferences.getString("username","")
            val userid = preferences.getString("userid","")

            apiService.uploadVideo(userid!!, videoPart, username!!, title.toString(),category.toString(),text.toString(),credit.toString()).enqueue(object :
                Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        intent()
                    } else {
                        println(0)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    println(t.message)
                }
            })
        }
    }

    fun stopvideo(){
        currentRecording!!.stop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_start)

        if (allPermissionsGranted()) {
            bindPreview()
        } else {
            requestPermissions()
        }

        val button = findViewById<Button>(R.id.button)
        val time = findViewById<TextView>(R.id.time)

        button.setOnClickListener {
            if(bool){
                time.setVisibility(View.VISIBLE)
                startvideo()
                bool = false
                button.text = "완료"
            }else{
                time.setVisibility(View.GONE)
                stopvideo()
                bool = true
                button.text = "녹화"
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        ActivityCompat.requestPermissions(
            this,
            permissionsToRequest.toTypedArray(),
            REQUEST_CODE_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // 권한 거부 처리 또는 오류 처리를 수행하세요.
                }
            }

            if (allPermissionsGranted()) {
                bindPreview()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // 이 부분을 추가합니다.
    }

    fun intent(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}