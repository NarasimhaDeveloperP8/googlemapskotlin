package com.example.googemapskotlin

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.directions.route.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList

class GMActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
    RoutingListener {

    lateinit var mMap: GoogleMap
    lateinit var myLocation: Location
    lateinit var destinationLocation: Location
    lateinit  var start: LatLng
    lateinit var end: LatLng
    var locationPermission = false
    lateinit var polylines: MutableList<Polyline>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_g_m)

        requestPermision()

       /* val  map_fragment:SupportMapFragment? = SupportMapFragment.newInstance(findViewById(R.id.map_fragment))
        map_fragment?.getMapAsync(this)*/

        val mapFragment: SupportMapFragment? = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun requestPermision() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE)
        } else {
            locationPermission = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission = true
                    getMyLocation()
                } else {
                    // permission denied  boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }

    //to get user location
    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap.setMyLocationEnabled(true)
        mMap.setOnMyLocationChangeListener(object : GoogleMap.OnMyLocationChangeListener {
            override fun onMyLocationChange(location: Location) {
                myLocation = location
                val ltlng = LatLng(location.latitude, location.longitude)
                val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    ltlng, 16f)
                mMap.animateCamera(cameraUpdate)
            }
        })

        //get destination location when user click on map
        mMap.setOnMapClickListener(object : GoogleMap.OnMapClickListener{
            override fun onMapClick(latLng: LatLng?) {
                end = latLng!!
                mMap.clear()
                start = LatLng(myLocation!!.latitude, myLocation!!.longitude)
                //start route finding
                Findroutes(start, end)
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        getMyLocation()
    }

    // find Routes.
    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {
            val routing: Routing = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("AIzaSyCGs1ZT7FQpshDpgzrdIrEIlv9S1jmF-qY") // our api key .
                .build()
            routing.execute()
        }
    }



    //Routing call back functions.
    override fun onRoutingFailure(e: RouteException) {
        val parentLayout = findViewById<View>(android.R.id.content)
        System.out.println("asdggfagshdfg+++" + e.toString())
        val snackbar: Snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onRoutingStart() {
        Toast.makeText(this, "Finding Route...", Toast.LENGTH_LONG).show()
    }

    //If Route finding success
    override fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
        val center: CameraUpdate = CameraUpdateFactory.newLatLng(start)
        val zoom: CameraUpdate = CameraUpdateFactory.zoomTo(16F)
        if (polylines != null) {
            polylines!!.clear()
        }
        val polyOptions = PolylineOptions()
        var polylineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        polylines = ArrayList<Polyline>()
        //add route(s) to the map using polyline
        for (i in route.indices) {
            if (i == shortestRouteIndex) {
                polyOptions.color(resources.getColor(android.R.color.background_dark))
                polyOptions.width(7F)
                polyOptions.addAll(route[shortestRouteIndex].getPoints())
                val polyline: Polyline = mMap!!.addPolyline(polyOptions)
                polylineStartLatLng = polyline.getPoints().get(0)
                val k: Int = polyline.getPoints().size
                polylineEndLatLng = polyline.getPoints().get(k - 1)
                polylines!!.add(polyline)
            } else {
            }
        }

        // Marker on route starting position
        val startMarker = MarkerOptions()
        polylineStartLatLng?.let { startMarker.position(it) }
        startMarker.title("My Location")
        mMap?.addMarker(startMarker)

        // Marker on route ending position
        val endMarker = MarkerOptions()
        polylineEndLatLng?.let { endMarker.position(it) }
        endMarker.title("Destination")
        mMap.addMarker(endMarker)
    }

    override fun onRoutingCancelled(){
        Findroutes(start, end)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Findroutes(start, end)
    }

    companion object {
        //to get location permissions.
        private const val LOCATION_REQUEST_CODE = 23
    }
}