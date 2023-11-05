package com.example.ut

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ut.databinding.ActivityMainBinding
import com.example.ut.ui.InformationFragment
import com.example.ut.ui.DashboardFragment
import com.example.ut.ui.HomeFragment
import com.example.ut.ui.ChattingFragment

private const val TAG_HOME = "home_fragment"
private const val TAG_DASHBOARD = "dashboard_fragment"
private const val TAG_NOTIFICATIONS = "notifications_fragment"
private const val TAG_ANOTHER = "another_fragment"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragment(TAG_HOME, HomeFragment())                          //화면 나오는거
        //binding.navigationView.selectedItemId = R.id.navigation_home //하단바 체크되어있는거

        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> setFragment(TAG_HOME, HomeFragment())
                R.id.navigation_dashboard -> setFragment(TAG_DASHBOARD, DashboardFragment())
                R.id.navigation_chat -> setFragment(TAG_NOTIFICATIONS, ChattingFragment())
                R.id.navigation_person -> setFragment(TAG_ANOTHER, InformationFragment())
            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) { //프레그먼트 함수
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val homeFragment = manager.findFragmentByTag(TAG_HOME) // 프레그먼트 변수 설정
        val dashboardFragment = manager.findFragmentByTag(TAG_DASHBOARD)
        val notificationsFragment = manager.findFragmentByTag(TAG_NOTIFICATIONS)
        val anotherFragment = manager.findFragmentByTag(TAG_ANOTHER)

        hideFragments(fragTransaction, homeFragment, dashboardFragment, notificationsFragment, anotherFragment)

        if (tag == TAG_HOME && homeFragment != null) {
            fragTransaction.show(homeFragment) // 보여줄 화면 설정
        } else if (tag == TAG_DASHBOARD && dashboardFragment != null) {
            fragTransaction.show(dashboardFragment)
        } else if (tag == TAG_NOTIFICATIONS && notificationsFragment != null) {
            fragTransaction.show(notificationsFragment)
        } else if (tag == TAG_ANOTHER && anotherFragment != null) {
            fragTransaction.show(anotherFragment)
        }

        fragTransaction.commitAllowingStateLoss()
    }

    private fun hideFragments(
        fragTransaction: androidx.fragment.app.FragmentTransaction,
        vararg fragments: Fragment?
    ) {
        for (fragment in fragments) {
            if (fragment != null) {
                fragTransaction.hide(fragment)
            }
        }
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("앱 종료")
        builder.setMessage("앱을 종료하시겠습니까?")

        builder.setPositiveButton("종료") { _, _ ->
            finishAffinity() // 앱 종료
        }

        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss() // 다이얼로그 닫기
        }

        val dialog = builder.create()
        dialog.show()
    }
}