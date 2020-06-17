package com.example.getlocationgps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationClient;

    Button btn_getLocation, btn_goToMaps;
    TextView tx_setLocation;

    LocationManager locationManager;
    String latitude=null, longitude=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //add permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        tx_setLocation = (TextView)findViewById(R.id.tx_setlocation);
        btn_getLocation = (Button) findViewById(R.id.btn_getLocation);
        btn_goToMaps = (Button) findViewById(R.id.btn_goToMaps);

        btn_getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                //Check gps is enable or not
                if (!locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)) {
                    //function to enable gps
                    onGPS();
                } else {
                    //GPS already On
                    //getLocation();
                    getLocGServices();
                }
            }
        });

        btn_goToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((latitude!=null) && (longitude!=null)) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q="+latitude+","+longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Harap Ambil Lokasi Anda Terlebih Dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLocation() {
        //Check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                tx_setLocation.setText("Lokasi Anda \n Latitude : "+latitude+"\n Longitude : "+longitude);
            } else if (locationNetwork != null) {
                double lat = locationNetwork.getLatitude();
                double longi = locationNetwork.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                tx_setLocation.setText("Lokasi Anda \n Latitude : "+latitude+"\n Longitude : "+longitude);
            } else if (locationPassive != null) {
                double lat = locationPassive.getLatitude();
                double longi = locationPassive.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                tx_setLocation.setText("Lokasi Anda \n Latitude : "+latitude+"\n Longitude : "+longitude);
            } else {
                Toast.makeText(this, "Tidak Dapat Menggambil Lokasi Anda", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Mengaktifkan GPS").setCancelable(false).setPositiveButton("YA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @OnClick(R.id.fab1)
    public void fab1_action() {
        startActivity(new Intent(this, MainActivityWithGoogleServices.class));
        finish();
    }

    // TODO:
    //  -------------------- get last location menggunakan google apis services --------------------

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getLocGServices() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            openLocation();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        System.err.println("error onDexterPermission = " + error);
                    }
                })
                .onSameThread()
                .check();
    }

    private void openLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat   = location.getLatitude();
                double longi = location.getLongitude();

                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                tx_setLocation.setText("Lokasi Anda \n Latitude : "+latitude+"\n Longitude : "+longitude);
            }
        });
    }
}
