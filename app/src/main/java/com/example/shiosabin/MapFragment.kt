package com.example.shiosabin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import com.example.shiosabin.BuildConfig.MAP_DATA_FETCH_NETWORK_ADDRESS
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
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

    private lateinit var group1Data: List<Pair<String,String>>
    private lateinit var group2Data: List<Pair<String,String>>
    private lateinit var group3Data: List<Pair<String,String>>
    private lateinit var group4Data: List<Pair<String,String>>
    private lateinit var group5Data: List<Pair<String,String>>
    private lateinit var seekBar: Slider

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = DataHandler.fetchFromMapApi(requireContext())
                withContext(Dispatchers.Main) {

                    group1Data = result.getOrNull(0)?.map { Pair(it.first.toString(), it.second) } ?: emptyList()
                    group2Data = result.getOrNull(1)?.map { Pair(it.first.toString(), it.second) } ?: emptyList()
                    group3Data = result.getOrNull(2)?.map { Pair(it.first.toString(), it.second) } ?: emptyList()
                    group4Data = result.getOrNull(3)?.map { Pair(it.first.toString(), it.second) } ?: emptyList()
                    group5Data = result.getOrNull(4)?.map { Pair(it.first.toString(), it.second) } ?: emptyList()

                    onMapReady(mMap) // データ取得後に地図描画を行う
                }
            } catch (e: Exception) {
                Log.e("DataFetchError", "Error fetching data from API: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "データの取得に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // FusedLocationProviderClientの初期化
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<FloatingActionButton>(R.id.current_location_Button).setOnClickListener {
            enableMyLocation() // 現在地を取得してズーム
        }

        seekBar = view.findViewById(R.id.seekBar)

        seekBar.addOnChangeListener { slider, value, fromUser ->
            updateMapMarkers(value)
            Log.d("CHECKMARKERVALUE",value.toString())
        }

        return view
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // ProgressBarを非表示にする
        view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE

        // 他のマーカーを表示
        updateMapMarkers(0f)
    }

    private fun updateMapMarkers(value: Float) {
        viewLifecycleOwner.lifecycleScope.launch {
            when (value) {
                0f -> {
                    if(::group1Data.isInitialized)
                    {
                        val coordinatesList1 = group1Data.map{ it.first }
                        val colorKeysList1 = group1Data.map { it.second }
                        addCirclesAndMarkers(mMap, coordinatesList1, "センサー", 1000.0, colorKeysList1)
                    }

                }
                0.25f -> {
                    if(::group2Data.isInitialized)
                    {
                        val coordinatesList2 = group2Data.map { it.first }
                        val colorKeysList2 = group2Data.map { it.second }
                        addCirclesAndMarkers(mMap, coordinatesList2, "センサー", 1000.0, colorKeysList2)
                    }
                }
                0.5f -> {
                    if(::group3Data.isInitialized)
                    {
                        val coordinatesList3 = group3Data.map { it.first }
                        val colorKeysList3 = group3Data.map { it.second }
                        addCirclesAndMarkers(mMap, coordinatesList3, "センサー", 1000.0, colorKeysList3)
                    }
                }
                0.75f -> {
                    if(::group4Data.isInitialized)
                    {
                        val coordinatesList4 = group4Data.map { it.first }
                        val colorKeysList4 = group4Data.map { it.second }
                        addCirclesAndMarkers(mMap, coordinatesList4, "センサー", 1000.0, colorKeysList4)
                    }
                }
                1f -> {
                    if(::group5Data.isInitialized)
                    {
                        val coordinatesList5 = group5Data.map { it.first }
                        val colorKeysList5 = group5Data.map { it.second }
                        addCirclesAndMarkers(mMap, coordinatesList5, "センサー", 1000.0, colorKeysList5)
                    }
                }
                // ... (他のグループに対する処理)
        }
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
                Toast.makeText(requireContext(), "現在地が取得できません", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // 円とマーカーの追加
    private fun addCirclesAndMarkers(
        googleMap: GoogleMap,
        coordinates: List<String>,  // 文字列のリスト
        title: String,
        _radius: Double,
        rangeColorKeys: List<String>  // 色を指定するキーのリスト
    ) {
        if (isAdded) {
            // キーと色をマッピング (例: "1" -> 赤, "2" -> 青など)
            val colorMap = mapOf(
                "1" to R.color.map_s_level1_color,    // "1" -> 赤色
                "2" to R.color.map_s_level2_color,   // "2" -> 青色
                "3" to R.color.map_s_level3_color,  // "3" -> 緑色
                "4" to R.color.map_s_level4_color, // "4" -> 黄色
                "5" to R.color.map_s_level5_color
            )

            coordinates.forEachIndexed { index, coordinate ->
                // "lat,lng" 形式の座標を LatLng に変換
                val latLngParts = coordinate.split(" ")
                if (latLngParts.size == 2) {
                    try {
                        val lat = latLngParts[0].toDouble()
                        val lng = latLngParts[1].toDouble()
                        val latLng = LatLng(lat, lng)

                        // 対応する色を取得 (リストのインデックスに基づいて)
                        val rangeColorKey =
                            rangeColorKeys.getOrNull(index) ?: "1"  // キーがない場合はデフォルトで "1"
                        val rangeColorResId = colorMap[rangeColorKey] ?: R.color.black

                        // 円を追加
                        googleMap.addCircle(
                            CircleOptions()
                                .center(latLng)
                                .radius(_radius)
                                .fillColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        rangeColorResId
                                    )
                                )
                        )

                        // マーカーを追加
                        googleMap.addMarker(
                            MarkerOptions()
                                .title(title)
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lader_icon))
                        )
                    } catch (e: NumberFormatException) {
                        // 座標が数値に変換できなかった場合のエラーハンドリング
                        Log.e("CoordinateError", "Invalid coordinate format: $coordinate")
                    }
                } else {
                    Log.e(
                        "CoordinateError",
                        "Coordinate should be in 'lat,lng' format: $coordinate"
                    )
                }
            }
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
