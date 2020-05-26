package com.example.locationtrackfire;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; //google map activity

    //for database related
    private DatabaseReference databaseReference;


    private LocationListener locationListener;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;

    //declre text boxes to store latitude dna longitude data
    private EditText editTextLatitude;
    private EditText editTextLongitude;
    int count=0;
    //declaring a strings to store previous locaitons (used to plot grapht)
    String previouslatitude="";
    String previouslongitude="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and gets notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //giving permission to the user to access
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);


        editTextLatitude = findViewById(R.id.editText);
        editTextLongitude = findViewById(R.id.editText2);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //giving the firebase "location" node reference to the datareference
        databaseReference = FirebaseDatabase.getInstance().getReference("Location");


        //creating a value listener.. called for changes in db
       databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    //taking the value stored in the db and store in string variable.

                        String latitude=dataSnapshot.child("latitude").child(String.valueOf(count)).getValue().toString();
                        String longitude=dataSnapshot.child("longitude").child(String.valueOf(count)).getValue().toString();

                        if(count!=0){//checking if the previous data is present or not.


                             previouslatitude=dataSnapshot.child("latitude").child(String.valueOf(count-1)).getValue().toString();
                             previouslongitude=dataSnapshot.child("longitude").child(String.valueOf(count-1)).getValue().toString();

                             count++;

                        }
                        else {
                            count++;
                        }


                   // Log.d("latitude", latitude);
                    //Log.d("longitude", longitude);
                    //Log.d("count", String.valueOf(count));











                    // convert the string to double and create Latlng .

                    LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));


                    //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.me2);


                    mMap.addMarker(new MarkerOptions().position(latLng).title("you are here"));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,4.2f));

                    mMap.addPolyline(new PolylineOptions()
                            .clickable(true)
                            .add(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)), new LatLng(Double.parseDouble(previouslatitude),Double.parseDouble(previouslongitude)))
                   );





                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
       });

        // creating a location listener ..
       locationListener = new LocationListener() {
           @Override
           public void onLocationChanged(Location location) {

                                //when location changed ...called

                try {
                    //change to string and store in text box

                    editTextLatitude.setText(Double.toString(location.getLatitude()));
                    editTextLongitude.setText(Double.toString(location.getLongitude()));

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
         //location manager service
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        try {
            //allowing gps and network to location manager
            //location provider , min distant and time to change the location, invoke the location listener


            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateButtonOnclick(View view) {


            // when button is clicked .updates the child



            databaseReference.child("latitude").child(String.valueOf(count)).setValue(editTextLatitude.getText().toString());
            databaseReference.child("longitude").child(String.valueOf(count)).setValue(editTextLongitude.getText().toString());

            Toast.makeText(MapsActivity.this,"location saved",Toast.LENGTH_SHORT).show();




    }

}