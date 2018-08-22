package com.poly

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.math.BigDecimal
import kotlin.math.round

//Curent address..........
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {



    private lateinit var mMap: GoogleMap
    private lateinit var instance: GetLocation
    var ischeck = false

    private  var arrayPoints: MutableList<LatLng> = mutableListOf()
    private  var list: MutableList<Model> = mutableListOf()
    private lateinit var polylineOptions: PolylineOptions

    private var checkClick = false

    /* recevie Broadcast lattude and longtude*/
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent != null && !ischeck) {
                val speed_ = intent.getFloatExtra("speed",0.0f)
                val lat = intent.getDoubleExtra("lat", 0.0)
                val lng = intent.getDoubleExtra("lng", 0.0)
                val address = intent.getStringExtra("add")
                val sydney = LatLng(lat, lng)


                val speed = round(speed_.toLong(), 2)
                val speedInKmph = mpsToKmph(speed)
                val speedInKmphUpTo2DecimalPlaces = round(speedInKmph.toLong(), 2)
                val stringBuilder = StringBuilder()
                stringBuilder.append("$speed m/s")
                stringBuilder.append("\n$speedInKmphUpTo2DecimalPlaces km/hr")
                tv.text=stringBuilder.toString()


              //  tv.text="Speed Check: $speed"
               // mapMaker(address, sydney, R.drawable.pick_location)
            }
        }

    }

    fun round(d: Long, decimalPlace: Int): Float {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).toFloat()
    }


    /**
     * @param mps
     * @return
     */
    fun mpsToKmph(mps: Float): Float {
        return mps * 18 / 5
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        list.add(Model(LatLng(28.6815697, 77.1194608),"Delhi"))
        list.add(Model(LatLng(19.100610, 72.905479),"Mumbai"))
        list.add(Model(LatLng(28.41422, 77.27821),"Faridabad"))
        list.add(Model(LatLng(20.421326, 84.384067),"odisa"))
        list.add(Model(LatLng(9.460084, 77.461157),"Kerla"))
        list.add(Model(LatLng(29.998887, 68.938494),"pakistan"))

        initBroadCastMap()
        instance.startLocation()


    }

    private fun initBroadCastMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        instance = GetLocation(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("key_action"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            GetLocation.REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.e("TAG", "User agreed to make required location settings changes.")
                Activity.RESULT_CANCELED -> {
                    Log.e("TAG", "User chose not to make required location settings changes.")
                    instance.mRequestingLocationUpdates = false
                }
            }// Nothing to do. startLocationupdates() gets called in onResume again.
        }
    }


    public override fun onResume() {
        super.onResume()
        if (instance.mRequestingLocationUpdates!! && instance.checkPermissions()) {
            instance.startLocationUpdates()
        }
        instance.updateLocationUI()
    }

    override fun onPause() {
        super.onPause()
        if (instance.mRequestingLocationUpdates!!) {
            instance.stopLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap;
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        this.mMap.setMapStyle(style);
        mMap.isMyLocationEnabled = true

        mMap.setOnMapLoadedCallback {
            // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30))
            run()
        }


    }



    fun run(){

        val bulder=LatLngBounds.Builder()
       // bulder.include()
       // bulder.include()

        for(l in list){
            bulder.include(l.latlng)
            val markarOption=MarkerOptions().position(l.latlng).title(l.name).snippet(l.name)
            mMap.addMarker(markarOption)
        }

        val bound =bulder.build()
        val cameraUpdate=CameraUpdateFactory.newLatLngBounds(bound,100)
        mMap.moveCamera(cameraUpdate)
    }
}
