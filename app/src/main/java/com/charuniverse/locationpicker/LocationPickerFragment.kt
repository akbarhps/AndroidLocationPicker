package com.charuniverse.locationpicker

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.charuniverse.locationpicker.databinding.FragmentLocationPickerBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.util.*

class LocationPickerFragment : Fragment(R.layout.fragment_location_picker) {

    private lateinit var googleMap: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var binding: FragmentLocationPickerBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLocationPickerBinding.bind(view)
        geocoder = Geocoder(context, Locale.getDefault())

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)
            ?.getMapAsync(mapReadyCallback)

        binding.layoutSearchLocation.setEndIconOnClickListener {
            val address = binding.inputSearchLocation.text.toString()
            val coordinate = getCoordinateByAddress(address) ?: return@setEndIconOnClickListener
            updateCamera(coordinate)
        }
    }

    private val mapReadyCallback = OnMapReadyCallback {
        googleMap = it

        // indonesia coordinate
        updateCamera(LatLng(-2.3932797, 108.8507139), 4f)

        googleMap.setOnCameraMoveStartedListener {
            toggleProgressBar(true)
        }

        googleMap.setOnCameraIdleListener {
            val newLocation = googleMap.cameraPosition.target
            val locationAddress = getAddressByCoordinate(newLocation)
                ?: return@setOnCameraIdleListener

            binding.addressShortName.text = locationAddress.substringBefore(',')
            binding.addressFullName.text = locationAddress

            toggleProgressBar(false)
        }
    }

    private fun toggleProgressBar(isVisible: Boolean) {
        binding.markerPinProgressBar.visibility = if (isVisible) {
            startAnimation(binding.markerPinContainer, R.anim.map_marker_pin_loading)
            View.VISIBLE
        } else {
            startAnimation(binding.markerPinContainer, R.anim.map_marker_pin_idle)
            View.GONE
        }
    }

    private fun getAddressByCoordinate(coordinate: LatLng): String? {
        val result = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1)
        if (result == null || result.size == 0) {
            return null
        }

        return result[0].getAddressLine(0)
    }

    private fun getCoordinateByAddress(address: String): LatLng? {
        if (address.isEmpty()) {
            return null
        }

        val result = geocoder.getFromLocationName(address, 1)
        if (result == null || result.size == 0) {
            return null
        }

        return LatLng(result[0].latitude, result[0].longitude)
    }

    private fun updateCamera(target: LatLng, zoom: Float = 20f) {
        val cameraPosition = CameraPosition.Builder()
            .target(target).zoom(zoom)
            .build()

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun startAnimation(view: View, animationId: Int) = view.startAnimation(
        AnimationUtils.loadAnimation(requireContext(), animationId)
    )

}