package com.example.shiosabin

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.shiosabin.BuildConfig.DATA_FETCH_NETWORK_ADDRESS
import com.example.shiosabin.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private val urlGetText = DATA_FETCH_NETWORK_ADDRESS // エミュレータの場合
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "SHIOSABIN"

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close)
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

        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)  // アプリ起動時は必ずfalseにリセット
        editor.apply()
        // 初期状態としてログイン状態を設定
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        Log.d("MainActivity", "isLoggedIn: $isLoggedIn")
        setNavigationView(isLoggedIn)

        // LoginDialogFragmentからの結果を受け取るリスナー
        supportFragmentManager.setFragmentResultListener("loginRequestKey", this) { _, bundle ->
            val success = bundle.getBoolean("loginSuccess", false)
            if (success) {
                onLoginSuccess() // ログイン成功時の処理
            }
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
                editor.apply()

                // NavigationViewのヘッダーを更新
                setNavigationView(false)
                Toast.makeText(this, "ログアウトしました", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_sensor_register ->{
                val sensorDialog = SensorIDDialogFragment()
                sensorDialog.show(supportFragmentManager,"SensorIDDialogFragment")
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
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


    private fun setNavigationView(isLoggedIn: Boolean) {
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
    }

    // ログインダイアログでのログイン成功時に呼び出されるメソッド
    fun onLoginSuccess() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.apply()

        setNavigationView(true) // ヘッダーを更新
    }
}
