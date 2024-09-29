package com.example.shiosabin

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.shiosabin.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding
    private val urlGetText = "http://10.0.2.2:5556/api/hello?name=a"  // エミュレータの場合*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "SHIOSABIN"  // Set the toolbar title

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

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
        binding.navigationDrawer.setCheckedItem(R.id.bottom_sensor)
        binding.bottomNavigation.selectedItemId = R.id.bottom_sensor

        // 非同期でAPI通信を開始
        CoroutineScope(Dispatchers.IO).launch {
            fetchData()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_notification -> openFragment(S_NotificationFragment())
            R.id.nav_sharing -> openFragment(S_SharingFragment())
            R.id.nav_gps -> openFragment(S_GPSFragment())
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

    // suspend 関数にして非同期通信を行う
   private suspend fun fetchData() {
        val result = fetchFromApi(urlGetText)

        // メインスレッドでUIを更新
        withContext(Dispatchers.Main) {
            Log.d("MainActivity", result)
        }
    }

    // API通信を行う関数
    private suspend fun fetchFromApi(urlString: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MainActivity", "Sending request to: $urlString")
                // URL 設定
                val url = URL(urlString)
                val con = url.openConnection() as HttpURLConnection

                // 接続設定
                con.connectTimeout = 30_000 // 30 秒
                con.readTimeout = 30_000    // 30 秒
                con.requestMethod = "GET"   // GETの場合は省略可

                // 接続を確立
                con.connect()

                val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { br ->
                    br.readLines().joinToString("")
                }

                // JSON変換
                val json = JSONObject(str)

                // "message" フィールドが JSONArray かどうか確認して取得
                if (json.has("message") && !json.isNull("message")) {
                    val messageArray = json.getJSONArray("message")
                    Log.d("MainActivity", "Message Array: $messageArray")

                    // 配列の内容をログに出力する（デバッグ用）
                    for (i in 0 until messageArray.length()) {
                        Log.d("MainActivity", "Item $i: ${messageArray.get(i)}")
                    }

                    messageArray.toString() // 配列全体を文字列として返す
                } else {
                    /*Log.e("MainActivity", "No message field or message is null")
                    "No message field or message is null"*/
                    Log.d("MainActivity",json.toString())
                    json.toString()
                }
            } catch (e: Exception) {
                // エラーログの詳細を出力
                Log.e("MainActivity", "Exception: ${e.message}")
                e.printStackTrace()
                "Error fetching data"
            }
        }
    }

}
