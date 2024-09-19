package com.example.shiosabin

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
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
    private val urlGetText = "http://192.168.0.10:5000/api/get"

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

        // Use the ApiHandler to fetch data
        val apiHandler = ApiHandler(urlGetText)
        apiHandler.fetchApiData(object : ApiHandler.ApiCallback {
            override fun onSuccess(result: String) {
                PredictionFragment().SetTodaySaltLevel(result)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                // Handle the error appropriately, e.g., show a toast or log it
            }
        })
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
}
