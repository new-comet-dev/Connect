package com.jarrebnnee.connect;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jarrebnnee.connect.Service.GetSet;
import com.jarrebnnee.connect.Service.SaveSharedPrefrence;
import com.jarrebnnee.connect.Service.ServiceHandler;
import com.jarrebnnee.connect.Service.TrackGPS;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SellerMapActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener,OnMapReadyCallback {

    GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    MarkerOptions marker;
    private GoogleApiClient mGoogleApiClient;
    FusedLocationProviderApi fusedLocationProviderApi;
    private LocationRequest mLocationRequest;
    ImageView ivlv;
    Toolbar toolbar;
    ImageView ivBack;
    TextView tvTitle;
    GetSet getSet;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    String u_latitute1,u_longitute1,c_id1,lang,u_type,u_id;
    ArrayList<HashMap<String, String>> map_list;
    HashMap<String, String> resultp;
    SaveSharedPrefrence prefrence;
    TrackGPS gps;
    private ArrayList<MapData> datas;
    ImageView ivLocation;
    ArrayAdapter<String> arrad;
    ArrayList<String> loc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_map);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        ivlv = (ImageView)findViewById(R.id.ivlv);
        ivLocation = (ImageView)findViewById(R.id.ivLocation);
        setSupportActionBar(toolbar);
        loc = new ArrayList<String>();
        loc.add("Current Location");
        loc.add("Your Location");

        arrad = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_spblack, loc);

        getSet = GetSet.getInstance();

        prefrence = new SaveSharedPrefrence();
        u_type = prefrence.getUserType(getApplicationContext());
        lang = prefrence.getlang(getApplicationContext());
        u_id = prefrence.getUserID(getApplicationContext());
        c_id1 = getSet.getsubc_id();
        checkLocationPermission();

        gps = new TrackGPS(SellerMapActivity.this);
        if (gps.canGetLocation()) {

            u_longitute1 = String.valueOf(gps.getLongitude());
            u_latitute1 = String.valueOf(gps.getLatitude());

          //  Toast.makeText(getApplicationContext(), "Longitude:" + u_longitute1 + "\nLatitude:" + u_latitute1, Toast.LENGTH_SHORT).show();
            Log.e("latlong", "Longitude:" + u_longitute1 + "\nLatitude:" + u_latitute1);
        } else {
            gps.showSettingsAlert();
        }


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + "" + "</font>")));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tvTitle.setText("Near By "+getSet.getsubc_name());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //  googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10 * 1000);
        mLocationRequest.setFastestInterval(1 * 1000);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        ivlv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SellerJobListActivity.class);
                startActivity(i);
                finish();
            }
        });
        ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeLocationDialog();
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        new MapList(getApplicationContext()).execute();

    }
    public void  ChangeLocationDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SellerMapActivity.this);
        dialogBuilder.setAdapter(arrad, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s = loc.get(which);
                Log.e("language",s);
                if (s.equals("Current Location")) {
                    u_longitute1 = String.valueOf(gps.getLongitude());
                    u_latitute1 = String.valueOf(gps.getLatitude());
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Your Location");
                    markerOptions.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.map1));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    map_list.clear();
                    datas.clear();
                    new MapList(getApplicationContext()).execute();
                }else if (s.equals("Your Location")) {
                    u_longitute1 = prefrence.getUserLatitute(getApplicationContext());//getUserLongitute(getApplicationContext());
                    u_latitute1 = prefrence.getUserLongitute(getApplicationContext());//getUserLatitute(getApplicationContext());
                    if (mCurrLocationMarker != null) {
                        mCurrLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(Double.parseDouble(u_longitute1), Double.parseDouble(u_latitute1));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("Your Location");
                    markerOptions.icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.map1));
                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


                    map_list.clear();
                    datas.clear();
                    Log.e("datas.size",""+datas.size());
                    new MapList(getApplicationContext()).execute();
                }
                dialog.dismiss();
            }
        });

        dialogBuilder.setTitle(getApplicationContext().getResources().getString(R.string.selectloc));
        AlertDialog b = dialogBuilder.create();
        b.show();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Your Location");
        markerOptions.icon(BitmapDescriptorFactory
                .fromResource(R.drawable.map1));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }
    public void init(){

        if (datas.size() != 0) {
            for (int i = 0; i < datas.size(); i++) {

                //temp2 = maplist.get(i);
                //String str = temp2.get("artisan_sur_name");
                Log.d("datas.size", ""+datas.size());
                Log.d("latitute", ""+datas.get(i).latitude);
                Log.d("longitude", ""+datas.get(i).longitude);
                resultp = map_list.get(i);
                String Name = resultp.get("vendor_name");
                marker = new MarkerOptions()
                        .position(
                                new LatLng(datas.get(i).latitude, datas
                                        .get(i).longitude))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.service_map_icon)).title(""+i);

                mMap.addMarker(marker);


            }
        } else {
            mMap.clear();
            Toast.makeText(getApplicationContext(), "No Jobs found nearby", Toast.LENGTH_SHORT).show();

        }
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public View getInfoContents(Marker x) {
                // TODO Auto-generated method stub

                View v = getLayoutInflater().inflate(R.layout.map_dialog, null);
                LinearLayout seller_map_info_box = (LinearLayout) v.findViewById(R.id.seller_map_info_box);
                LinearLayout layout_seller_info_box= (LinearLayout)v.findViewById(R.id.layout_seller_info_box);
                Log.e("marker servece", "" + x);
                if (x.getTitle().equals("Your Location")) {
                    Log.e("marker servece", "null");
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText("Your Location");
                    textView.setTextColor(Color.parseColor("#000000"));
                    seller_map_info_box.addView(textView);
                    layout_seller_info_box.setVisibility(View.GONE);
                    return v;
                } else {
                    layout_seller_info_box.setVisibility(View.VISIBLE);
                }

                int index = Integer.valueOf(x.getTitle());

                resultp = map_list.get(index);
                String js_title = resultp.get("js_title");
                String c_title = resultp.get("c_title");
                String js_description = resultp.get("js_description");
                String js_status = resultp.get("js_status");
                String js_image = resultp.get("js_image");
               /* String v_id = temp2.get("vendor_id");
                getSet.setvendorId(v_id);
                getSet.setV_name(temp2.get("vendor_name"));
                getSet.setV_price(temp2.get("price"));
                getSet.setV_rat(temp2.get("reputation"));
                getSet.setV_location(temp2.get("area"));
                getSet.setV_min_qty(temp2.get("min_qty"));
                getSet.setV_phone(temp2.get("vendor_phone"));
                String shipping_cost =temp2.get("shipping_cost");
                shipping_cost = shipping_cost.substring(0, shipping_cost.length()-3);
                getSet.setV_Shipping(shipping_cost);

                String rat = temp2.get("reputation");
                String vendor_name = temp2.get("vendor_name");
                String vendor_phone = temp2.get("vendor_phone");
                String area = temp2.get("area");*/

                ImageView iv1 = (ImageView)v.findViewById(R.id.iv1);
                TextView txtView = (TextView) v.findViewById(R.id.tvSName);
                TextView tvCategory = (TextView) v.findViewById(R.id.tvCategory);
                TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);
                TextView tvdes = (TextView) v.findViewById(R.id.tvdes);

                Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/tt0142m.ttf");
                tvStatus.setTypeface(custom_font);
                tvCategory.setTypeface(custom_font);
                Typeface custom_fontbold = Typeface.createFromAsset(getAssets(),  "fonts/tt0144m.ttf");
                txtView.setTypeface(custom_fontbold);

                txtView.setText(js_title);
                tvCategory.setText("Category: "+c_title);
                tvdes.setText(js_description);

               Picasso.with(getApplicationContext()).load(js_image).into(iv1);

                if (js_status.equals("3")){
                    tvStatus.setText("Status: Completed");
                }else if (js_status.equals("2")){
                    tvStatus.setText("Status: Awarded");
                }else if (js_status.equals("1")){
                    tvStatus.setText("Status: Applied");
                }else if (js_status.equals("4")){
                    tvStatus.setText("Status: Accept");
                }else if (js_status.equals("5")){
                    tvStatus.setText("Status: Rejected");
                }else if (js_status.equals("0")){
                    tvStatus.setText("Status: Pending");
                }



                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker x) {
                // TODO Auto-generated method stub
					/*int index = Integer.valueOf(marker.getTitle());
					  temp2 = maplist.get(index);

					  final String id = temp2.get("id");

					  getSet.setid1(id);
						Intent i = new Intent(getApplicationContext(), FindArtisanDetailActivity.class);
						startActivity(i);
						finish();*/
                int index = Integer.valueOf(x.getTitle());
                resultp = map_list.get(index);
                getSet.setjs_id(resultp.get("js_id"));
                getSet.setjs_title(resultp.get("js_title"));
                getSet.setjsr_posted_user_id(resultp.get("js_user_id"));
                Intent i = new Intent(getApplicationContext(),
                        ProjectDetailActivity.class);
                startActivity(i);
                finish();
            }
        });


    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        // mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()) , 10.0f) );

        // Enable / Disable my location button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Enable / Disable Compass icon
        mMap.getUiSettings().setCompassEnabled(true);

        // Enable / Disable Rotate gesture
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        // Enable / Disable zooming functionality
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

       /* LatLng TutorialsPoint = new LatLng(21, 57);
        mMap.addMarker(new
                MarkerOptions().position(TutorialsPoint).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TutorialsPoint));*/
    }
    class MapList extends AsyncTask<Void, Void, Void> {
        boolean st = false;
        String result;
        Context context;
        ProgressDialog pDialog;
        String status, msg,js_id,js_user_id,js_category_id,js_title,js_description,js_price,js_appointment_date,js_image,js_radius,js_status,js_created,js_modified,u_id,u_first_name,u_last_name,u_email,u_password,u_address,u_gender,u_latitute,u_longitute,u_city,u_phone,u_postcode,u_country,u_img,u_description,u_status,u_type,u_is_notification_sound,u_seller_services,u_created,u_modified,c_id,c_images,c_type,c_title,c_is_parent_id,c_total_services,c_created,distance,u_city_name;

        MapList(Context context) {

            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SellerMapActivity.this);
            pDialog.setMessage(getResources().getString(R.string.pdialog));
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            map_list = new ArrayList<HashMap<String, String>>();
            datas = new ArrayList<MapData>();
            String uri =  Urlcollection.url;//"http://cp3767.veba.co/~shubantech/Ebay_clone/ebay_clone_api/?";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("action", "searchJobs"));
            nameValuePairs.add(new BasicNameValuePair("category_id", c_id1));
            nameValuePairs.add(new BasicNameValuePair("u_latitute",u_latitute1));
            nameValuePairs.add(new BasicNameValuePair("u_longitute",u_longitute1));
            nameValuePairs.add(new BasicNameValuePair("lang",lang));
          /*  if (u_type.equals("2")) {
                nameValuePairs.add(new BasicNameValuePair("u_id", u_id));
            }*/

            ServiceHandler handler = new ServiceHandler();
            String jsonSt = handler.makeServiceCall(
                    uri, ServiceHandler.POST, nameValuePairs);
            Log.d("nameValuePairs: ", "> " + nameValuePairs);
            Log.d("Response: ", "> " + jsonSt);

            if (jsonSt != null) {

                try {

                    JSONObject jsonObj = new JSONObject(jsonSt);
                    status = jsonObj.getString("status");
                    msg = jsonObj.getString("message");
                    Log.e("msg", msg);
                    JSONArray data = jsonObj.getJSONArray("data");
                    for (int i = 0;i<data.length();i++){
                        JSONObject object = data.getJSONObject(i);
                        js_id = object.getString("js_id");
                        js_user_id  = object.getString("js_user_id");
                        js_category_id  = object.getString("js_category_id");
                        js_title  = object.getString("js_title");
                        js_description  = object.getString("js_description");
                        js_price  = object.getString("js_price");
                        js_appointment_date  = object.getString("js_appointment_date");
                        js_image  = object.getString("js_image");
                        js_radius  = object.getString("js_radius");
                        js_status  = object.getString("js_status");
                        js_created  = object.getString("js_created");
                        js_modified  = object.getString("js_modified");
                        u_id  = object.getString("u_id");
                        u_first_name  = object.getString("u_first_name");
                        u_last_name  = object.getString("u_last_name");
                        u_email  = object.getString("u_email");
                        u_password  = object.getString("u_password");
                        u_address  = object.getString("u_address");
                        u_gender  = object.getString("u_gender");
                        u_latitute  = object.getString("u_latitute");
                        u_longitute  = object.getString("u_longitute");
                        u_city  = object.getString("u_city");
                        u_phone  = object.getString("u_phone");
                        u_postcode  = object.getString("u_postcode");
                        u_country  = object.getString("u_country");
                        u_img  = object.getString("u_img");
                        u_description  = object.getString("u_description");
                        u_status  = object.getString("u_status");
                        u_type  = object.getString("u_type");
                        u_is_notification_sound  = object.getString("u_is_notification_sound");
                        u_seller_services  = object.getString("u_seller_services");
                        u_created  = object.getString("u_created");
                        u_modified  = object.getString("u_modified");
                        c_title  = object.getString("c_title");

                       /* c_id  = object.getString("c_id");
                        c_images  = object.getString("c_images");
                        c_type  = object.getString("c_type");
                        c_title  = object.getString("c_title");
                        c_is_parent_id  = object.getString("c_is_parent_id");
                        c_total_services  = object.getString("c_total_services");
                        c_created  = object.getString("c_created");*/
                        distance  = object.getString("distance");
                        u_city_name  = object.getString("u_city_name");

                        resultp = new HashMap<String, String>();
                      /*  if(u_latitute.equals("null")||u_longitute.equals("null")){
                            u_latitute="0.0";
                            u_longitute="0.0";
                        }else if(u_latitute.equals("null")){
                            u_latitute="0.0";
                        }else if (u_longitute.equals("null")) {
                            u_longitute="0.0";
                        }else{*/
                        MapData data1 = new MapData();

                        data1.latitude = Double.parseDouble(u_latitute);
                        data1.longitude = Double.parseDouble(u_longitute);
                        datas.add(data1);
                        resultp.put("js_id",js_id);
                        resultp.put("js_user_id",js_user_id);
                        resultp.put("js_category_id",js_category_id);
                        resultp.put("js_title",js_title);
                        resultp.put("js_description",js_description);
                        resultp.put("js_price",js_price);
                        resultp.put("js_appointment_date",js_appointment_date);
                        resultp.put("js_image",js_image);
                        resultp.put("js_radius",js_radius);
                        resultp.put("js_status",js_status);
                        resultp.put("js_created",js_created);
                        resultp.put("js_modified",js_modified);
                        resultp.put("u_id",u_id);
                        resultp.put("u_first_name",u_first_name);
                        resultp.put("u_last_name",u_last_name);
                        resultp.put("u_email",u_email);
                        resultp.put("u_password",u_password);
                        resultp.put("u_address",u_address);
                        resultp.put("u_gender",u_gender);
                        resultp.put("u_latitute",u_latitute);
                        resultp.put("u_longitute",u_longitute);
                        resultp.put("u_city",u_city);
                        resultp.put("u_phone",u_phone);
                        resultp.put("u_postcode",u_postcode);
                        resultp.put("u_country",u_country);
                        resultp.put("u_img",u_img);
                        resultp.put("u_description",u_description);
                        resultp.put("u_status",u_status);
                        resultp.put("u_type",u_type);
                        resultp.put("u_is_notification_sound",u_is_notification_sound);
                        resultp.put("u_seller_services",u_seller_services);
                        resultp.put("u_created",u_created);
                        resultp.put("u_modified",u_modified);
                        resultp.put("c_title",c_title);
                       /* resultp.put("c_id",c_id);
                        resultp.put("c_images",c_images);
                        resultp.put("c_type",c_type);
                        resultp.put("c_title",c_title);
                        resultp.put("c_is_parent_id",c_is_parent_id);
                        resultp.put("c_total_services",c_total_services);
                        resultp.put("c_created",c_created);*/
                        resultp.put("distance",distance);
                        resultp.put("u_city_name",u_city_name);

                        map_list.add(resultp);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            pDialog.dismiss();
            if (status.equals("0")) {
                init();
                Toast.makeText(getApplicationContext(),"sucess",Toast.LENGTH_LONG).show();
            }else {
                init();
            }
        }
    }
    private class MapData {

        double latitude = 0;
        double longitude = 0;

    }

}

