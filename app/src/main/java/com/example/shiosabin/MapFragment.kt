package com.example.shiosabin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

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
        // 地図が準備できたときに呼び出されます
        val kosen = LatLng(26.526821661793992, 128.03037129598053)
        addCircleAroundMarker(googleMap,kosen,"沖縄高専",1000.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kosen, 13f))
    }

    private fun addCircleAroundMarker(googleMap: GoogleMap, latlng: LatLng, title: String,_radius: Double) {
        googleMap.addCircle(
            CircleOptions()
                .center(latlng)
                .radius(_radius)
                .fillColor(resources.getColor(R.color.blue, context?.theme))

        )
        googleMap.addMarker(
            MarkerOptions()
                .title(title)
                .position(latlng)
        )
    }
}
