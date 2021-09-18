package app.ui.home


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import app.R
import app.data.model.LocationHelper
import app.databinding.ActivityHomeBinding
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import org.tensorflow.lite.examples.detection.DetectorActivity


class HomeActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: HomeViewModelFactory by instance()

    private lateinit var viewModel: HomeViewModel

    // globally declare LocationRequest
    private lateinit var locationRequest: LocationRequest

    // globally declare LocationCallback
    private lateinit var locationCallback: LocationCallback

    // declare a global variable of FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar : Toolbar
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private lateinit var navigationView: NavigationView
    private val btnToggleDark: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        drawerLayout = findViewById(R.id.drawer_layout)
//        toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,R.string.app_name,R.string.app_name)
        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle!!.syncState();
        actionBarDrawerToggle!!.drawerArrowDrawable.color = resources.getColor(R.color.blue_color_txt);
        navigationView =  findViewById(R.id.navigation_menu);

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_home -> {
                }
                R.id.nav_profile -> {
                }
                R.id.nav_settings -> {
                }
                R.id.nav_map -> {
                }
                R.id.nav_about -> {
                }

            }
            true
        }

        val binding: ActivityHomeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        viewModel = ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.sendRegistrationToServer()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.map.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, RetrieveMapActivity::class.java)
            startActivity(intent);
        }

        binding.videoNav.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, DetectorActivity::class.java)
            startActivity(intent);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates();
        } else {
            askLocationPermission();
        }


    }
    fun btnToggleDark(view: View?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    /**
     * call this method in onCreate
     * onLocationResult call when location is changed
     */
    private fun getLocationUpdates()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                            locationResult.lastLocation
                    Log.d("onSuccess2", "onSuccess2: $location")

                    // use your location object
                    // get latitude , longitude and other info from this
                    viewModel.setLocation(LocationHelper(location.latitude, location.longitude))
                }


            }
        }
    }

    //start location updates
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null /* Looper */
        )
    }
    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)


    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }
//    private fun getLastLocation() {
//        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//        val locationTask = fusedLocationProviderClient.lastLocation
//        locationTask.addOnSuccessListener { location ->
//            if (location != null) {
//                locat = LocationHelper(location.latitude, location.longitude)
//                viewModel.setLocation(locat)
//
//                //We have a location
//                Log.d("onSuccess", "onSuccess: $locat")
//                Log.d("onSuccess", "onSuccess: " + location.latitude)
//                Log.d("onSuccess", "onSuccess: " + location.longitude)
//                Log.d("place 1", "vous etes arrivé ici ")
//            } else {
//                Log.d("onSuccess", "onSuccess: Location was null...")
//
//            }
//        }
//
//        locationTask.addOnFailureListener { e -> Log.e("onFailure", "onFailure: " + e.localizedMessage) }
//    }

    private fun askLocationPermission() {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) !==
                PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) ===
                                    PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        getLocationUpdates()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}

