package app.ui.home

import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import app.R
import app.data.model.Danger
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class RetrieveMapActivity : AppCompatActivity(), OnMapReadyCallback, KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()

    private lateinit var viewModel: HomeViewModel

    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private lateinit var dangers: ArrayList<Danger>
    private lateinit var location: LatLng
    var markersList: ArrayList<Marker> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrieve_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mapReady = true
            updateMap()
        }

        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        viewModel.init()
        viewModel.getLiveData().observe(this, androidx.lifecycle.Observer { it ->

            this.dangers = it
            //if(mapReady){mMap.clear()}
            updateMap()
            //showOrHideInfoWindows(true)
        })
    }

    private fun updateMap() {

        if (mapReady && dangers != null) {
            mMap.clear()
            dangers.forEach { it ->
                this.location = LatLng(it.getLocation().getAlt(), it.getLocation().getLong())
                var marker: Marker = mMap.addMarker(MarkerOptions().position(location).title(it.getType()))
                //marker.showInfoWindow()

                if (it.getNumber() == 1 ) { marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.animals))}
                if (it.getNumber() == 2 ) { marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.work))}
                if (it.getNumber() == 3 ) { marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.trafic))}

                markersList.add(marker)

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(36.7508896,5.0567333), 8f))
            showOrHideInfoWindows(true)
            this.dangers.clear()
        }
    }

    private fun showOrHideInfoWindows(shouldShow: Boolean) {
        println("ya  "+markersList.size+ "  dangers")
        for (marker in markersList) {
            if (shouldShow) marker.showInfoWindow() else marker.hideInfoWindow()
        }
    }

//    private fun initMap() {
//
//        if (mapReady && dangers != null) {
//
//            dangers.forEach { it ->
//                val location = LatLng(it.getLocation().getAlt(), it.getLocation().getLong())
//               this.marker = mMap.addMarker(MarkerOptions().position(location).title(it.getType()))
//                marker.showInfoWindow()
//
//                if (it.getNumber() == 1 ) { marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.animals))}
//                else {
//                    if (it.getNumber() == 2) {
//                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.work))
//                    }
//                }
//
//            }
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(36.752887,3.042048), 7f))
//            marker.showInfoWindow()
//            this.dangers.clear()
//
//        }
//    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //mMap = googleMap
        mapReady = true

    }


    private fun getCOmpleteAddress(Latitude: Double, Longtitude: Double): String? {
        var address = ""
        val geocoder = Geocoder(this@RetrieveMapActivity, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(Latitude, Longtitude, 1)
            if (address != null) {
                val returnAddress = addresses[0]
                val stringBuilderReturnAddress = StringBuilder("")
                for (i in 0..returnAddress.maxAddressLineIndex) {
                    stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n")
                }
                address = stringBuilderReturnAddress.toString()
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
        return address
    }


}
