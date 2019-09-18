package view


import Util.GpsUtils
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sportsplayer.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.user_location.*
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.Locale

@Suppress("DEPRECATION")
class CurrentLocationDetector : AppCompatActivity() {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private var myLatitude = 0.0
    private var myLongitude = 0.0
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var isContinue = false
    private var isGPS = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.user_location)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // locationRequest!!.interval = (10 * 1000).toLong() // 10 seconds
        // locationRequest!!.fastestInterval = (5 * 1000).toLong() // 5 seconds

        GpsUtils(this).turnGPSOn(object : GpsUtils.onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                isGPS = isGPSEnable
            }
        })
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        myLatitude = location.latitude
                        myLongitude = location.longitude
                        if (!isContinue) {
                            locationCoordinates(myLatitude,myLongitude)
                        }
                    }
                }
            }
        }


    }

    override fun onStart() {
        super.onStart()
        if (!isGPS) {

            toast("Please turn on GPS")
            finish()
        }else {
            getLocation()
        }
    }

    private fun getLocation() {
        val progressDialog: ProgressDialog = ProgressDialog.show(this, "getting location", "please wait...")
        progressDialog.show()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                AppConstants.LOCATION_REQUEST
            )

        } else {
            mFusedLocationClient!!.lastLocation.addOnSuccessListener(this)
            { location ->
                if (location != null) {
                    myLatitude = location.latitude
                    myLongitude = location.longitude
                    locationCoordinates(myLatitude,myLongitude)
                    progressDialog.dismiss()
                } else {
                    toast("Location is null")
                    mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
                }
            }



        }
    }

    @SuppressLint("HardwareIds")
    fun locationCoordinates(latitude:Double, longitude:Double)
    {
        val progressDialog: ProgressDialog = ProgressDialog.show(this, "Setting location", "Hold on...")
        progressDialog.show()
        try {

            val geoCoder = Geocoder(applicationContext, Locale.getDefault())
            val address: List<Address> = geoCoder.getFromLocation(latitude,longitude, 1)
            toast("data is fetched")
            if (address.isNotEmpty()) {
                val myaddress = address[0].getAddressLine(0)
                val city = address[0].locality
                val intent = Intent()
                intent.putExtra("city",city)
                intent.putExtra("address",myaddress)
                setResult(Activity.RESULT_OK, intent)
                progressDialog.dismiss()
                finish()

            }else{
                toast("List is Null")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            progressDialog.dismiss()
            finish()

        }

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient!!.lastLocation.addOnSuccessListener(this)
                    { location ->
                        if (location != null) {
                            myLatitude = location.latitude
                            myLongitude = location.longitude
                        }
                        else {
                            mFusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
                        }
                    }

                } else {
                    toast("Permission denied")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true // flag maintain before get location
            }
        }
    }




}