package com.example.ut.board

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ut.MainActivity
import com.example.ut.R
import com.example.ut.a_data.board
import com.example.ut.a_data.category_all
import com.example.ut.a_data.filter_search
import com.example.ut.a_data.login_member_data
import com.example.ut.server.ApiServer
import com.example.ut.server.IP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat

class search_board : AppCompatActivity() {
    var mode:String? = null
    var small = false

    var classification:String? = null
    var category:ArrayList<String> = arrayListOf()
    var new_distance = 10
    var min_credit = -1
    var max_credit = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_board)

        val intent = intent
        val search = intent.getStringExtra("search")

        classification = intent.getStringExtra("ca")
        category = intent.getStringArrayListExtra("category") ?: arrayListOf()
        new_distance = intent.getIntExtra("distance", 10)
        min_credit = intent.getIntExtra("min_credit", -1)
        max_credit = intent.getIntExtra("max_credit", -1)

        mode = intent.getStringExtra("mode")

        val search_ed = findViewById<EditText>(R.id.search)

        search_ed.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH  ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                // 여기에 "완료" 버튼을 눌렀을 때 실행할 코드를 작성
                if(search_ed.text.toString() != "" && search_ed.text.toString() != null){
                    val intent = Intent(this, search_board::class.java)
                    intent.putExtra("search", search_ed.text.toString())
                    intent.putExtra("ca", classification)
                    intent.putStringArrayListExtra("category", category)
                    intent.putExtra("distance", new_distance)
                    intent.putExtra("min_credit", min_credit)
                    intent.putExtra("max_credit", max_credit)
                    startActivity(intent)
                }

                return@setOnEditorActionListener true
            }
            false
        }

        search_ed.setText(search)

        val preferences = getSharedPreferences("session", MODE_PRIVATE)
        val userid = preferences.getString("userid","")

        val IP = IP()

        val retrofit = Retrofit.Builder()
            .baseUrl(IP.ip()) // 실제 엔드포인트 URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService =  retrofit.create(ApiServer::class.java)
        val search_data = filter_search(userid, search, null, classification, category, new_distance, min_credit, max_credit)

        val call = apiService.filter_board(search_data)

        val smallLayout = findViewById<LinearLayout>(R.id.smallLayout)
        val main2 = findViewById<LinearLayout>(R.id.main2)

        val category = findViewById<Button>(R.id.category)
        val distance = findViewById<Button>(R.id.distance)
        val credit = findViewById<Button>(R.id.credit)

        val category2 = findViewById<Button>(R.id.category2)
        val distance2 = findViewById<Button>(R.id.distance2)
        val credit2 = findViewById<Button>(R.id.credit2)

        val submit = findViewById<Button>(R.id.submit)
        val reset = findViewById<Button>(R.id.reset)

        if(!this.category.isEmpty()){
            if(this.category.size > 1){
                category.text = this.category[0]+" 외"
            } else{
                category.text = this.category[0]
            }
        } else if(this.classification != null){
            category.text = this.classification
        }

        if(new_distance != 10){
            distance.text = "거리:"+new_distance+"km"
        }

        if(min_credit != -1){
            if(max_credit != -1){
                credit.text = min_credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원~"+
                        max_credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원"
            } else{
                credit.text = min_credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원"
            }
        } else if(max_credit != -1) {
            if (min_credit != -1) {
                credit.text = min_credit.toString().let { "%,d".format(it.toLongOrNull()) } +
                        "원~" + max_credit.toString().let { "%,d".format(it.toLongOrNull()) } + "원"
            } else {
                credit.text = "~${max_credit.toString().let { "%,d".format(it.toLongOrNull()) }}원"
            }
        }

        category.setOnClickListener {
            if (smallLayout.getVisibility() == View.GONE) {
                small = true
                // 작은 레이아웃이 숨겨져 있을 때
                category2.setBackgroundResource(R.drawable.square_dark_blue)
                distance2.setBackgroundResource(R.drawable.square_lite_blue)
                credit2.setBackgroundResource(R.drawable.square_lite_blue)

                smallLayout.setVisibility(View.VISIBLE)
                main2.setVisibility(View.VISIBLE)

                main2.bringToFront()
                smallLayout.bringToFront()

                val linear = findViewById<LinearLayout>(R.id.linear)
                linear.orientation = LinearLayout.VERTICAL
                linear.gravity = Gravity.NO_GRAVITY
                linear.removeAllViews()

                val data = login_member_data("", "")
                val call = apiService.carto(data)

                call.enqueue(object : Callback<category_all> {
                    override fun onResponse(call: Call<category_all>, response: Response<category_all>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            // 서버 응답 처리
                            val classification = responseBody!!.data1.map { it.classification }
                            val category_list = responseBody!!.data1.map { it.category }

                            createlinear(classification, category_list)

                            reset.setOnClickListener{
                                this@search_board.classification = null
                                this@search_board.category = arrayListOf()
                                linear.removeAllViews()
                                createlinear(classification, category_list)
                            }
                        } else {
                            // 서버 요청 실패
                            showAlertDialog("서버 요청에 실패하였습니다.")
                        }
                    }

                    override fun onFailure(call: Call<category_all>, t: Throwable) {
                        // 네트워크 오류 처리
                        showAlertDialog("서버 연결에 실패하였습니다.")
                        println(t.message)
                    }
                })
            } else {
                // 작은 레이아웃이 보여져 있을 때
                smallLayout.setVisibility(View.GONE)
                main2.setVisibility(View.GONE)
            }
        }

        distance.setOnClickListener {
            if (smallLayout.getVisibility() == View.GONE) {
                small = true
                // 작은 레이아웃이 숨겨져 있을 때
                category2.setBackgroundResource(R.drawable.square_lite_blue)
                distance2.setBackgroundResource(R.drawable.square_dark_blue)
                credit2.setBackgroundResource(R.drawable.square_lite_blue)

                smallLayout.setVisibility(View.VISIBLE)
                main2.setVisibility(View.VISIBLE)

                main2.bringToFront()
                smallLayout.bringToFront()

                val linear = findViewById<LinearLayout>(R.id.linear)
                linear.orientation = LinearLayout.HORIZONTAL
                linear.gravity = Gravity.CENTER_HORIZONTAL
                linear.removeAllViews()

                val newText = EditText(this)

                val layoutParams = LinearLayout.LayoutParams(
                    (200 * resources.displayMetrics.density).toInt(),
                    (40 * resources.displayMetrics.density).toInt()
                )
                layoutParams.topMargin = (10 * resources.displayMetrics.density).toInt()
                layoutParams.gravity = Gravity.CENTER

                newText.setPadding((10 * resources.displayMetrics.density).toInt())
                newText.setBackgroundResource(R.color.dark_blue)
                newText.layoutParams = layoutParams
                newText.inputType = InputType.TYPE_CLASS_NUMBER
                newText.gravity = Gravity.RIGHT

                newText.filters = arrayOf(InputFilter.LengthFilter(4))

                if(new_distance != 10){
                    newText.setText(this.new_distance.toString())
                } else{
                    newText.hint = "10"
                }

                newText.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // 텍스트가 변경된 후 호출되는 부분
                        this@search_board.new_distance = s?.toString()?.toIntOrNull() ?: 10
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // 텍스트가 변경되기 전에 호출되는 부분
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 텍스트가 변경되면서 호출되는 부분
                    }
                })

                val newText2 = TextView(this)
                newText2.text = "km"
                newText2.textSize = 20f
                newText2.setBackgroundResource(R.color.dark_blue)

                val layoutParams2 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (40 * resources.displayMetrics.density).toInt()
                )
                layoutParams2.topMargin = (10 * resources.displayMetrics.density).toInt()
                layoutParams2.gravity = Gravity.CENTER

                newText2.layoutParams = layoutParams2
                newText2.gravity = Gravity.CENTER

                linear.addView(newText)
                linear.addView(newText2)

                reset.setOnClickListener{
                    this@search_board.new_distance = 10
                    newText.setText((10).toString())
                }
            } else {
                // 작은 레이아웃이 보여져 있을 때
                smallLayout.setVisibility(View.GONE)
                main2.setVisibility(View.GONE)
            }
        }

        credit.setOnClickListener {
            if (smallLayout.getVisibility() == View.GONE) {
                small = true
                // 작은 레이아웃이 숨겨져 있을 때
                category2.setBackgroundResource(R.drawable.square_lite_blue)
                distance2.setBackgroundResource(R.drawable.square_lite_blue)
                credit2.setBackgroundResource(R.drawable.square_dark_blue)

                smallLayout.setVisibility(View.VISIBLE)
                main2.setVisibility(View.VISIBLE)

                main2.bringToFront()
                smallLayout.bringToFront()

                val linear = findViewById<LinearLayout>(R.id.linear)
                linear.orientation = LinearLayout.VERTICAL
                linear.gravity = Gravity.NO_GRAVITY
                linear.removeAllViews()

                val newlinear = LinearLayout(this)
                newlinear.orientation = LinearLayout.HORIZONTAL
                newlinear.gravity = Gravity.CENTER_HORIZONTAL

                val layoutParams = LinearLayout.LayoutParams(
                    (160 * resources.displayMetrics.density).toInt(),
                    (40 * resources.displayMetrics.density).toInt()
                )
                layoutParams.topMargin = (10 * resources.displayMetrics.density).toInt()

                val newText_min = EditText(this)

                newText_min.hint = "최소 가격"
                newText_min.setBackgroundResource(R.color.dark_blue)
                newText_min.setPadding((10 * resources.displayMetrics.density).toInt())
                newText_min.inputType = InputType.TYPE_CLASS_NUMBER
                newText_min.layoutParams = layoutParams

                if(min_credit != -1){
                    val formattedValue = NumberFormat.getNumberInstance().format(this.min_credit)
                    newText_min.setText(formattedValue)
                }

                val newText_max = EditText(this)

                newText_max.hint = "최대 가격"
                newText_max.setBackgroundResource(R.color.dark_blue)
                newText_max.setPadding((10 * resources.displayMetrics.density).toInt())
                newText_max.inputType = InputType.TYPE_CLASS_NUMBER
                newText_max.layoutParams = layoutParams

                if(max_credit != -1){
                    val formattedValue = NumberFormat.getNumberInstance().format(this.max_credit)
                    newText_max.setText(formattedValue)
                }

                newText_min.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // 텍스트가 변경된 후 호출되는 부분
                        if (s != null && s.isNotEmpty()) {
                            val creditValue = s.toString().replace(",", "").toLong()

                            val formattedValue = NumberFormat.getNumberInstance().format(creditValue)
                            newText_min.removeTextChangedListener(this)
                            newText_min.setText(formattedValue)
                            newText_min.setSelection(formattedValue.length)
                            newText_min.addTextChangedListener(this)
                        }
                        this@search_board.min_credit = s.toString().replace(",","").toIntOrNull() ?: -1
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // 텍스트가 변경되기 전에 호출되는 부분
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 텍스트가 변경되면서 호출되는 부분
                    }
                })

                newText_max.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        // 텍스트가 변경된 후 호출되는 부분
                        if (s != null && s.isNotEmpty()) {
                            val creditValue = s.toString().replace(",", "").toLong()

                            val formattedValue = NumberFormat.getNumberInstance().format(creditValue)
                            newText_max.removeTextChangedListener(this)
                            newText_max.setText(formattedValue)
                            newText_max.setSelection(formattedValue.length)
                            newText_max.addTextChangedListener(this)
                        }
                        this@search_board.max_credit = s.toString().replace(",","").toIntOrNull() ?: -1
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // 텍스트가 변경되기 전에 호출되는 부분

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // 텍스트가 변경되면서 호출되는 부분

                    }
                })

                val newtext2 = TextView(this)
                newtext2.text = "   ~   "

                val layoutParams2 = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams2.topMargin = (10 * resources.displayMetrics.density).toInt()
                newtext2.layoutParams = layoutParams2
                newtext2.gravity = Gravity.CENTER

                newlinear.addView(newText_min)
                newlinear.addView(newtext2)
                newlinear.addView(newText_max)

                linear.addView(newlinear)

                reset.setOnClickListener{
                    this@search_board.min_credit = 0
                    this@search_board.max_credit = -1
                    newText_min.setText(null)
                    newText_max.setText(null)
                }
            } else {
                // 작은 레이아웃이 보여져 있을 때
                smallLayout.setVisibility(View.GONE)
                main2.setVisibility(View.GONE)
            }
        }

        category2.setOnClickListener {
            small = true

            category2.setBackgroundResource(R.drawable.square_dark_blue)
            distance2.setBackgroundResource(R.drawable.square_lite_blue)
            credit2.setBackgroundResource(R.drawable.square_lite_blue)

            val linear = findViewById<LinearLayout>(R.id.linear)
            linear.orientation = LinearLayout.VERTICAL
            linear.gravity = Gravity.NO_GRAVITY
            linear.removeAllViews()

            val data = login_member_data("", "")
            val call = apiService.carto(data)

            call.enqueue(object : Callback<category_all> {
                override fun onResponse(call: Call<category_all>, response: Response<category_all>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        // 서버 응답 처리
                        val classification = responseBody!!.data1.map { it.classification }
                        val category_list = responseBody!!.data1.map { it.category }

                        createlinear(classification, category_list)

                        reset.setOnClickListener{
                            this@search_board.classification = null
                            this@search_board.category = arrayListOf()
                            linear.removeAllViews()
                            createlinear(classification, category_list)
                        }
                    } else {
                        // 서버 요청 실패
                        showAlertDialog("서버 요청에 실패하였습니다.")
                    }
                }

                override fun onFailure(call: Call<category_all>, t: Throwable) {
                    // 네트워크 오류 처리
                    showAlertDialog("서버 연결에 실패하였습니다.")
                    println(t.message)
                }
            })
        }

        distance2.setOnClickListener {
            small = true

            category2.setBackgroundResource(R.drawable.square_lite_blue)
            distance2.setBackgroundResource(R.drawable.square_dark_blue)
            credit2.setBackgroundResource(R.drawable.square_lite_blue)

            val linear = findViewById<LinearLayout>(R.id.linear)
            linear.orientation = LinearLayout.HORIZONTAL
            linear.gravity = Gravity.CENTER_HORIZONTAL
            linear.removeAllViews()

            val newText = EditText(this)

            val layoutParams = LinearLayout.LayoutParams(
                (200 * resources.displayMetrics.density).toInt(),
                (40 * resources.displayMetrics.density).toInt()
            )
            layoutParams.topMargin = (10 * resources.displayMetrics.density).toInt()
            layoutParams.gravity = Gravity.CENTER

            newText.setPadding((10 * resources.displayMetrics.density).toInt())
            newText.setBackgroundResource(R.color.dark_blue)
            newText.layoutParams = layoutParams
            newText.inputType = InputType.TYPE_CLASS_NUMBER
            newText.gravity = Gravity.RIGHT

            newText.filters = arrayOf(InputFilter.LengthFilter(4))

            if(new_distance != 10){
                newText.setText(this.new_distance.toString())
            } else{
                newText.hint = "10"
            }

            newText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // 텍스트가 변경된 후 호출되는 부분
                    this@search_board.new_distance = s?.toString()?.toIntOrNull() ?: 10
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트가 변경되기 전에 호출되는 부분
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경되면서 호출되는 부분
                }
            })

            val newText2 = TextView(this)
            newText2.text = "km"
            newText2.textSize = 20f
            newText2.setBackgroundResource(R.color.dark_blue)

            val layoutParams2 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                (40 * resources.displayMetrics.density).toInt()
            )
            layoutParams2.topMargin = (10 * resources.displayMetrics.density).toInt()
            layoutParams2.gravity = Gravity.CENTER

            newText2.layoutParams = layoutParams2
            newText2.gravity = Gravity.CENTER

            linear.addView(newText)
            linear.addView(newText2)

            reset.setOnClickListener{
                this@search_board.new_distance = 10
                newText.setText((10).toString())
            }
        }

        credit2.setOnClickListener {
            small = true

            category2.setBackgroundResource(R.drawable.square_lite_blue)
            distance2.setBackgroundResource(R.drawable.square_lite_blue)
            credit2.setBackgroundResource(R.drawable.square_dark_blue)

            val linear = findViewById<LinearLayout>(R.id.linear)
            linear.orientation = LinearLayout.VERTICAL
            linear.gravity = Gravity.NO_GRAVITY
            linear.removeAllViews()

            val newlinear = LinearLayout(this)
            newlinear.orientation = LinearLayout.HORIZONTAL
            newlinear.gravity = Gravity.CENTER_HORIZONTAL

            val layoutParams = LinearLayout.LayoutParams(
                (160 * resources.displayMetrics.density).toInt(),
                (40 * resources.displayMetrics.density).toInt()
            )
            layoutParams.topMargin = (10 * resources.displayMetrics.density).toInt()

            val newText_min = EditText(this)

            newText_min.hint = "최소 가격"
            newText_min.setBackgroundResource(R.color.dark_blue)
            newText_min.setPadding((10 * resources.displayMetrics.density).toInt())
            newText_min.inputType = InputType.TYPE_CLASS_NUMBER
            newText_min.layoutParams = layoutParams

            if(min_credit != -1){
                val formattedValue = NumberFormat.getNumberInstance().format(this.min_credit)
                newText_min.setText(formattedValue)
            }

            val newText_max = EditText(this)

            newText_max.hint = "최대 가격"
            newText_max.setBackgroundResource(R.color.dark_blue)
            newText_max.setPadding((10 * resources.displayMetrics.density).toInt())
            newText_max.inputType = InputType.TYPE_CLASS_NUMBER
            newText_max.layoutParams = layoutParams

            if(max_credit != -1){
                val formattedValue = NumberFormat.getNumberInstance().format(this.max_credit)
                newText_max.setText(formattedValue)
            }

            newText_min.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // 텍스트가 변경된 후 호출되는 부분
                    if (s != null && s.isNotEmpty()) {
                        val creditValue = s.toString().replace(",", "").toLong()

                        val formattedValue = NumberFormat.getNumberInstance().format(creditValue)
                        newText_min.removeTextChangedListener(this)
                        newText_min.setText(formattedValue)
                        newText_min.setSelection(formattedValue.length)
                        newText_min.addTextChangedListener(this)
                    }
                    this@search_board.min_credit = s.toString().replace(",","").toIntOrNull() ?: -1
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트가 변경되기 전에 호출되는 부분
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경되면서 호출되는 부분
                }
            })

            newText_max.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    // 텍스트가 변경된 후 호출되는 부분
                    if (s != null && s.isNotEmpty()) {
                        val creditValue = s.toString().replace(",", "").toLong()

                        val formattedValue = NumberFormat.getNumberInstance().format(creditValue)
                        newText_max.removeTextChangedListener(this)
                        newText_max.setText(formattedValue)
                        newText_max.setSelection(formattedValue.length)
                        newText_max.addTextChangedListener(this)
                    }
                    this@search_board.max_credit = s.toString().replace(",","").toIntOrNull() ?: -1
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 텍스트가 변경되기 전에 호출되는 부분

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 텍스트가 변경되면서 호출되는 부분

                }
            })

            val newtext2 = TextView(this)
            newtext2.text = "   ~   "

            val layoutParams2 = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutParams2.topMargin = (10 * resources.displayMetrics.density).toInt()
            newtext2.layoutParams = layoutParams2
            newtext2.gravity = Gravity.CENTER

            newlinear.addView(newText_min)
            newlinear.addView(newtext2)
            newlinear.addView(newText_max)

            linear.addView(newlinear)

            reset.setOnClickListener{
                this@search_board.min_credit = 0
                this@search_board.max_credit = -1
                newText_min.setText(null)
                newText_max.setText(null)
            }
        }

        submit.setOnClickListener {
            val intent = Intent(this, search_board::class.java)
            intent.putExtra("search", search_ed.text.toString())
            intent.putExtra("ca", classification)
            intent.putStringArrayListExtra("category", this.category)
            intent.putExtra("distance", new_distance)
            intent.putExtra("min_credit", min_credit)
            intent.putExtra("max_credit", max_credit)
            startActivity(intent)
        }

        findViewById<View>(R.id.main2).setOnTouchListener { v, event ->
            // smallLayout 이외의 영역을 터치했을 때 smallLayout을 숨김
            if (event.action == MotionEvent.ACTION_DOWN && smallLayout.visibility == View.VISIBLE) {
                val x = event.x
                val y = event.y

                val location = IntArray(2)
                smallLayout.getLocationOnScreen(location)

                val left = location[0]
                val top = location[1]
                val right = left + smallLayout.width
                val bottom = top + smallLayout.height

                if (!(x >= left && x <= right && y >= top && y <= bottom)) {
                    small = false
                    smallLayout.visibility = View.GONE
                    main2.visibility = View.GONE

                    return@setOnTouchListener true
                }
            }
            false
        }

        call.enqueue(object : Callback<board> {
            override fun onResponse(call: Call<board>, response: Response<board>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val scrollView = findViewById<ScrollView>(R.id.scroll)
                    val linearLayout = scrollView.getChildAt(0) as ViewGroup

                    // 서버 응답 처리
                    if(responseBody!!.items.isNotEmpty()){
                        linearLayout.removeAllViews()

                        for(i in 0 until responseBody!!.items.size){
                            val linearLayout1 = LinearLayout(this@search_board)
                            linearLayout1.orientation = LinearLayout.HORIZONTAL

                            var layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                                LinearLayout.LayoutParams.WRAP_CONTENT// 높이는 내용에 맞게
                            )

                            linearLayout1.layoutParams = layoutParams
                            linearLayout1.setPadding((10 * resources.displayMetrics.density).toInt())
                            linearLayout1.setOnClickListener {
                                intent(responseBody.items[i].num)
                            }

                            val linear = LinearLayout(this@search_board)
                            layoutParams = LinearLayout.LayoutParams(
                                (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )
                            layoutParams.marginStart =
                                (10 * resources.displayMetrics.density).toInt()
                            layoutParams.topMargin =
                                (10 * resources.displayMetrics.density).toInt()
                            linear.layoutParams = layoutParams

                            val view = ImageView(this@search_board)
                            view.setBackgroundResource(R.drawable.rounded_corner_border_2)

                            view.clipToOutline = true //둥글게 만들기
                            view.scaleType = ImageView.ScaleType.CENTER_CROP

                            layoutParams = LinearLayout.LayoutParams(
                                (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                            )
                            layoutParams.gravity = Gravity.CENTER
                            view.layoutParams = layoutParams

                            Glide.with(this@search_board)
                                .load(IP.ip()+"image/"+responseBody.items[i].num)
                                .skipMemoryCache(true) // 메모리 캐시 사용 안 함
                                .override(
                                    (100 * resources.displayMetrics.density).toInt(),
                                    (100 * resources.displayMetrics.density).toInt()
                                )
                                .into(view)

                            linear.addView(view)

                            view.setOnClickListener {
                                linear.removeAllViews()
                                val webView = WebView(this@search_board)
                                video(webView, IP, responseBody.items[i].num)
                                layoutParams = LinearLayout.LayoutParams(
                                    (100 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                                    (100 * resources.displayMetrics.density).toInt()// 높이는 내용에 맞게
                                )
                                layoutParams.gravity = Gravity.CENTER
                                webView.layoutParams = layoutParams

                                webView.setBackgroundResource(R.drawable.rounded_corner_border_2)
                                webView.clipToOutline = true //둥글게 만들기

                                linear.addView(webView)
                            }

                            val innerLayout = LinearLayout(this@search_board)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (100 * resources.displayMetrics.density).toInt()
                            )
                            layoutParams.marginStart = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
                            layoutParams.gravity = Gravity.CENTER
                            innerLayout.layoutParams = layoutParams

                            innerLayout.orientation = LinearLayout.VERTICAL

                            val titleTextView = TextView(this@search_board)
                            titleTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            titleTextView.text = responseBody.items[i].title
                            titleTextView.textSize = 25f
                            titleTextView.ellipsize = TextUtils.TruncateAt.END
                            titleTextView.maxLines = 1

                            val addressTextView = TextView(this@search_board)
                            addressTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            val displayMetrics = resources.displayMetrics
                            val halfScreenWidth = displayMetrics.widthPixels / 2.6

                            addressTextView.maxWidth = halfScreenWidth.toInt()
                            addressTextView.ellipsize = TextUtils.TruncateAt.END
                            addressTextView.maxLines = 1
                            addressTextView.text = responseBody.items[i].address
                            addressTextView.textSize = 20f

                            val creditTextView = TextView(this@search_board)
                            creditTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            creditTextView.text = responseBody.items[i].credit.toString().let { "%,d".format(it.toLongOrNull()) }+"원"
                            creditTextView.textSize = 25f

                            val viewsTextView = TextView(this@search_board)
                            viewsTextView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            viewsTextView.gravity = Gravity.END
                            viewsTextView.text = responseBody.items[i].views.toString()
                            viewsTextView.textSize = 20f

                            // innerLayout에 하위 뷰들을 추가
                            innerLayout.gravity = Gravity.CENTER_VERTICAL
                            innerLayout.addView(titleTextView)
                            innerLayout.addView(addressTextView)

                            val newlin = LinearLayout(this@search_board)
                            newlin.orientation = LinearLayout.HORIZONTAL
                            newlin.addView(creditTextView)
                            newlin.addView(viewsTextView)

                            innerLayout.addView(newlin)

                            // 부모 레이아웃에 하위 뷰들을 추가
                            linearLayout1.addView(linear)
                            linearLayout1.addView(innerLayout)

                            linearLayout.addView(linearLayout1,0)
                        }
                    }
                } else {
                    // 서버 요청 실패
                    showAlertDialog("서버 요청에 실패하였습니다.")
                }
            }

            override fun onFailure(call: Call<board>, t: Throwable) {
                // 네트워크 오류 처리
                showAlertDialog("서버 연결에 실패하였습니다.")
                println(t.message)
            }
        })
    }

    fun createlinear(classification:List<String>, category_list:List<String>){
        val linear = findViewById<LinearLayout>(R.id.linear)
        val category = classification.distinct()

        if(this@search_board.classification != null){
            val this_count = classification.count { it == category[category.indexOf(this@search_board.classification)] }
            val index = classification.indexOf(category[category.indexOf(this@search_board.classification)])
            val sublist = category_list.subList(index, category_list.size)

            val linear2 = LinearLayout(this@search_board)
            linear2.orientation = LinearLayout.HORIZONTAL

            var layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // 너비는 부모와 일치
                LinearLayout.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
            )
            layoutParams.marginEnd = (10 * resources.displayMetrics.density).toInt()
            layoutParams.marginStart = (20 * resources.displayMetrics.density).toInt()
            linear2.layoutParams = layoutParams

            val newText = Button(this@search_board)
            newText.gravity = Gravity.LEFT
            newText.setTextColor(Color.BLUE)
            newText.setBackgroundResource(android.R.color.transparent)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
                LinearLayout.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
            )
            layoutParams.weight = 1f
            newText.layoutParams = layoutParams
            newText.text = this@search_board.classification

            val button = TextView(this@search_board)
            layoutParams = LinearLayout.LayoutParams(
                (30 * resources.displayMetrics.density).toInt(), // 너비는 부모와 일치
                (30 * resources.displayMetrics.density).toInt() // 높이는 내용에 맞게
            )
            button.layoutParams = layoutParams
            button.setBackgroundResource(R.drawable.rounded_corner_border)
            button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.dark_blue2)
            button.gravity = Gravity.CENTER
            button.text = "X"
            button.textSize = 20f
            button.setTextColor(Color.WHITE)

            button.setOnClickListener {
                this@search_board.classification = null
                linear.removeAllViews()
                createlinear(classification,category_list)
            }

            linear2.addView(newText)
            linear2.addView(button)

            linear.addView(linear2)

            for(i in 0 until this_count){
                val newText = Button(this@search_board)
                newText.setBackgroundResource(android.R.color.transparent)
                newText.gravity = Gravity.LEFT
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
                    ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
                )

                layoutParams.marginStart = (40 * resources.displayMetrics.density).toInt()

                newText.text = sublist[i]
                newText.layoutParams = layoutParams

                if(this@search_board.category.indexOf(sublist[i])!=-1){
                    newText.setTextColor(Color.BLUE)
                }

                newText.setOnClickListener {
                    if(newText.getCurrentTextColor() == Color.BLUE){
                        newText.setTextColor(Color.BLACK)
                        this@search_board.category.remove(sublist[i])
                    } else{
                        newText.setTextColor(Color.BLUE)
                        this@search_board.category.add(sublist[i])
                    }
                }

                linear.addView(newText)
            }
        } else{
            for(i in 0 until category.size){
                val newText = Button(this@search_board)
                newText.gravity = Gravity.LEFT

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, // 너비는 부모와 일치
                    ViewGroup.LayoutParams.WRAP_CONTENT // 높이는 내용에 맞게
                )
                layoutParams.topMargin = (5 * resources.displayMetrics.density).toInt()
                layoutParams.marginStart = (20 * resources.displayMetrics.density).toInt()
                newText.layoutParams = layoutParams

                newText.setBackgroundResource(android.R.color.transparent)
                newText.text = category[i]

                newText.setOnClickListener {
                    this@search_board.classification = category[i]
                    linear.removeAllViews()
                    createlinear(classification,category_list)
                }

                linear.addView(newText)
            }
        }
    }

    fun showAlertDialog(text:String) {
        // AlertDialog 빌더 생성
        val builder = AlertDialog.Builder(this)

        // 다이얼로그 메시지 설정
        builder.setMessage(text)

        // 다이얼로그 생성 및 표시
        val dialog = builder.create()
        dialog.show()
    }

    private fun intent(num:Int){
        val intent = Intent(this, board_info::class.java)
        intent.putExtra("num",num)
        intent.putExtra("search",1)
        startActivity(intent)
    }

    private fun video(webView: WebView, adress: IP, num:Int){
        // 동영상 URL 설정
        val videoUrl = adress.ip()+"video/"+num
        val imageUrl = adress.ip()+"image/"+num

        // WebView 설정
        val settings = webView.settings
        settings.javaScriptEnabled = true

        webView.webViewClient = object: WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val javascript = """
                    javascript:(function() {
                        var video = document.querySelector('video');
                        
                        if (video) {
                            video.removeAttribute('autoplay');
                            video.removeAttribute('controls');
                            video.setAttribute('poster','$imageUrl');
                            
                            video.addEventListener('click', function() {
                                if (video.paused) {
                                    // 비디오가 일시 정지 상태인 경우, 'play()' 메서드를 호출하여 재생합니다.
                                    video.play();
                                } else {
                                    // 비디오가 재생 중인 경우, 'pause()' 메서드를 호출하여 정지합니다.
                                    video.pause();
                                }
                            });
                        }
                    })()
                """

                webView.evaluateJavascript(javascript, null)
            }
        }
        // 웹 페이지 로드
        webView.loadUrl(videoUrl)
    }

    override fun onBackPressed() {
        if(small){
            small = false

            val smallLayout = findViewById<LinearLayout>(R.id.smallLayout)
            val main2 = findViewById<LinearLayout>(R.id.main2)

            smallLayout.visibility = View.GONE
            main2.visibility = View.GONE
        } else if(mode != null){
            val intent = Intent(this, filter::class.java)
            startActivity(intent)
        } else{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}