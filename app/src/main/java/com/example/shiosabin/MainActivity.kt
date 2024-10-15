package com.example.shiosabin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.shiosabin.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences_S: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "SHIOSABIN"

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = binding.navigationDrawer
        navigationView.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_sensor -> openFragment(SensorFragment())
                R.id.bottom_map -> openFragment(MapFragment())
                R.id.bottom_prediction -> openFragment(PredictionFragment())
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(SensorFragment())
        navigationView.setCheckedItem(R.id.bottom_sensor)
        binding.bottomNavigation.selectedItemId = R.id.bottom_sensor

        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        sharedPreferences_S = getSharedPreferences("SensorPrefs", Context.MODE_PRIVATE)

        // 初期状態としてログイン状態を設定
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val username = sharedPreferences.getString("username", "") ?: ""
        Log.d("MainActivity", "isLoggedIn: $isLoggedIn, username: $username")
        setNavigationView(isLoggedIn, username)

        // LoginDialogFragmentからの結果を受け取るリスナー
        supportFragmentManager.setFragmentResultListener("loginRequestKey", this) { _, bundle ->
            val success = bundle.getBoolean("loginSuccess", false)
            val username = bundle.getString("username")
            if (success && username != null) {
                onLoginSuccess(username) // ログイン成功時の処理
            }
        }

        supportFragmentManager.setFragmentResultListener("RegisterSensorIDKey",this){ _, bundle ->
            SetSensorID()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_notification -> openFragment(S_NotificationFragment())
            R.id.nav_sharing -> openFragment(S_SharingFragment())
            R.id.nav_gps -> openFragment(S_GPSFragment())
            R.id.nav_login -> {
                val loginDialog = LoginDialogFragment()
                loginDialog.show(supportFragmentManager, "LoginDialogFragment")
            }

            R.id.nav_logout -> {
                val editor = sharedPreferences.edit()
                editor.putBoolean("isLoggedIn", false)
                editor.putString("username", "") // ログアウト時にusernameをクリア
                editor.apply()

                // NavigationViewのヘッダーを更新
                setNavigationView(false, "")

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("ログアウトに成功しました")
                    .setCancelable(false)
                    .setPositiveButton("閉じる") { dialog, _ ->
                        dialog.dismiss()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("成功")
                alert.show()
            }

            R.id.nav_sensor_register -> {
                val sensorDialog = SensorIDDialogFragment()
                sensorDialog.show(supportFragmentManager, "SensorIDDialogFragment")
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun onLoginSuccess(username: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("username", username) // ログイン時にusernameを保存
        editor.apply()

        setNavigationView(true, username) // ヘッダーを更新
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val tag = fragment::class.java.simpleName
        val existingFragment = fragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag)
            fragmentTransaction.commit()
        }
    }


    private fun setNavigationView(isLoggedIn: Boolean, username: String) {
        // 既存のヘッダーを削除

        if (isLoggedIn) {
            // ログイン後のメニューを設定
            navigationView.menu.clear()  // 現在のメニューをクリア
            navigationView.inflateMenu(R.menu.logged_nav_menu)  // ログイン後のメニューを読み込み
        } else {
            // ログイン前のメニューを設定
            navigationView.menu.clear()  // 現在のメニューをクリア
            navigationView.inflateMenu(R.menu.non_logged_nav_menu)  // ログイン前のメニューを読み込み
        }

        if (navigationView.headerCount > 0) {
            navigationView.removeHeaderView(navigationView.getHeaderView(0))
        }

        // 新しいヘッダーを追加
        val headerLayout = if (isLoggedIn) {
            R.layout.registered_nav_header
        } else {
            R.layout.login_nav_header
        }

        val headerView = layoutInflater.inflate(headerLayout, navigationView, false)
        navigationView.addHeaderView(headerView)

        val header = navigationView.getHeaderView(0) // ヘッダーの0番目を取得
        val userTextView = header.findViewById<TextView>(R.id.userID) // ヘッダー内のTextViewにアクセス
        userTextView.text = "USER ID: " + username

        SetSensorID()
    }

    private fun SetSensorID() {
        val header = navigationView.getHeaderView(0) // ヘッダーの0番目を取得
        val sensorID = sharedPreferences_S.getString("SENSOR_ID", "")
        val sensorTextView = header.findViewById<TextView>(R.id.sensorID)
        sensorTextView.text = "SENSOR ID: $sensorID" // センサーIDをヘッダーに表示
    }


}
