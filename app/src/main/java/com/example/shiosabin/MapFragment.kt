package com.example.shiosabin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback {

    private var locations: List<Pair<Double, Double>>? = null
    private var FINE_PERMISSION_CODE:Int = 1;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {

        view?.findViewById<ProgressBar>(R.id.progress_bar)?.visibility = View.GONE
        // 非同期でマーカーや円を追加
        CoroutineScope(Dispatchers.IO).launch {
            val mitinoeki = LatLng(26.552155585587087, 127.96844407573552)
            val kosen = LatLng(26.5, 128.0)

            // メインスレッドでUI更新
            withContext(Dispatchers.Main) {
                addCircleAroundMarker(googleMap, kosen, "沖縄高専", 1500.0, R.color.s_level1_color)
                addCircleAroundMarker(googleMap, mitinoeki, "道の駅", 2000.0, R.color.s_level3_color)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kosen, 13f))
            }
        }
    }


    private fun addCircleAroundMarker(
        googleMap: GoogleMap, latlng: LatLng, title: String,
        _radius: Double,
        rangeColor: Int
    ) {
        googleMap.addCircle(
                    CircleOptions()
                        .center(latlng)
                        .radius(_radius)
                        .fillColor(ContextCompat.getColor(requireContext(), rangeColor)) // requireContext() を使うことで null を避ける
                )

        googleMap.addMarker(
            MarkerOptions()
                .title(title)
                .position(latlng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.lader_icon))
        )
    }
}
