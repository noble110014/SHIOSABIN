package com.example.shiosabin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private val FINE_PERMISSION_CODE = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // FusedLocationProviderClientの初期化
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<FloatingActionButton>(R.id.current_location_Button).setOnClickListener {
            enableMyLocation() // 現在地を取得してズーム
        }

        val seekBar:Slider = view.findViewById(R.id.seekBar)
        seekBar.addOnChangeListener{seekBar,value,fromUser ->
            onMapReady(mMap)
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // ProgressBarを非表示にする
        view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE

        // 他のマーカーを表示
        viewLifecycleOwner.lifecycleScope.launch {
            val mitinoeki = LatLng(26.552155585587087, 127.96844407573552)
            val kosen = LatLng(26.5, 128.0)

            addCircleAroundMarker(googleMap, kosen, "沖縄高専", 1500.0, R.color.s_level1_color)
            addCircleAroundMarker(googleMap, mitinoeki, "道の駅", 2000.0, R.color.s_level3_color)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(kosen,15f))
        }
    }

    // 現在地のマイロケーションレイヤーを有効にする
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // パーミッションがない場合はリクエスト
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_PERMISSION_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true // マイロケーションレイヤーを有効にする

        // 現在地を取得
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                // カメラを現在地に移動
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Toast.makeText(requireContext(), "現在地が取得できません", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 円とマーカーの追加
    private fun addCircleAroundMarker(
        googleMap: GoogleMap, latlng: LatLng, title: String,
        _radius: Double,
        rangeColor: Int
    ) {
        if (isAdded) {
            googleMap.addCircle(
                CircleOptions()
                    .center(latlng)
                    .radius(_radius)
                    .fillColor(ContextCompat.getColor(requireContext(), rangeColor))
            )

            googleMap.addMarker(
                MarkerOptions()
                    .title(title)
                    .position(latlng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.lader_icon))
            )
        }
    }

    // パーミッションのリクエスト結果を受け取る
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == FINE_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), "位置情報のパーミッションが必要です", Toast.LENGTH_SHORT).show()
        }
    }
}
