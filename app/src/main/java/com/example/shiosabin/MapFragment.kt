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
import com.example.shiosabin.BuildConfig.MAP_SALINITY_DATA_FETCH_NETWORK_ADDRESS
import com.example.shiosabin.BuildConfig.MAP_SENSOR_LOCATION_DATA_FETCH_NETWORK_ADDRESS
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
    private lateinit var positions: List<String>
    private lateinit var salinities: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                positions = DataHandler.fetchFromApi(MAP_SENSOR_LOCATION_DATA_FETCH_NETWORK_ADDRESS, requireContext())
                val locationData = listOf(
                    listOf(1, "POINT(10.00 10.00)"),
                    listOf(2, "POINT(20.00 20.00)"),
                    listOf(3, "POINT(30.00 30.00)"),
                    listOf(4, "POINT(40.00 40.00)"),
                    listOf(5, "POINT(50.00 50.00)")
                )

                // 新しい配列に座標を格納する
                val coordinatesList = locationData.map { pointData ->
                    // "POINT(10.00 10.00)" の形式から "10.00, 10.00" を取得
                    val pointString = pointData[1] as String
                    val coordinates = pointString.removePrefix("POINT(").removeSuffix(")").split(" ")
                    coordinates.joinToString(", ")
                }
                positions = coordinatesList

                salinities = DataHandler.fetchFromApi(MAP_SALINITY_DATA_FETCH_NETWORK_ADDRESS, requireContext())
                val salinityData = listOf(
                    listOf(
                        listOf(1, "POINT(50.00 50.00)", "Group 1"),
                        listOf(2, "POINT(40.00 40.00)", "Group 1"),
                        listOf(5, "POINT(30.00 30.00)", "Group 1"),
                        listOf(4, "POINT(20.00 20.00)", "Group 1"),
                        listOf(3, "POINT(10.00 10.00)", "Group 1")
                    ),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    emptyList()
                )

                // 先頭の数字を取り出して結合する
                val firstNumbers: List<String> = salinityData[0].map { (it[0] as Int).toString() }

                salinities = firstNumbers

                withContext(Dispatchers.Main) {
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

        val seekBar: Slider = view.findViewById(R.id.seekBar)
        seekBar.addOnChangeListener { seekBar, value, fromUser ->
            onMapReady(mMap)
        }

        // 非同期でデータを取得し、取得後に地図上に描画


        return view
    }


    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        if (!::positions.isInitialized) {
            Log.e("MapFragment", "Positions not initialized!")
            return
        }


        // ProgressBarを非表示にする
        view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE


        // 他のマーカーを表示
        viewLifecycleOwner.lifecycleScope.launch {
            /*val mitinoeki = LatLng(26.552155585587087, 127.96844407573552)
            val kosen = LatLng(26.5, 128.0)

            addCircleAroundMarker(googleMap, kosen, "沖縄高専", 1500.0, R.color.s_level1_color)
            addCircleAroundMarker(googleMap, mitinoeki, "道の駅", 2000.0, R.color.s_level3_color)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(kosen,15f))*/
            addCirclesAndMarkers(googleMap, positions, "センサー", 1000000.0, salinities)
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
                "1" to R.color.s_level1_color,    // "1" -> 赤色
                "2" to R.color.s_level2_color,   // "2" -> 青色
                "3" to R.color.s_level3_color,  // "3" -> 緑色
                "4" to R.color.s_level4_color, // "4" -> 黄色
                "5" to R.color.s_level5_color
            )

            coordinates.forEachIndexed { index, coordinate ->
                // "lat,lng" 形式の座標を LatLng に変換
                val latLngParts = coordinate.split(",")
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
