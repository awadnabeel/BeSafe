package com.example.wifi_test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.IOException;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.*;

public class MainActivity extends AppCompatActivity {

    Button btnScanBarcode;
    Button btnScanOffline;
    WifiManager wifiManager;
//    TelephonyManager telephonyManager;
    TextView hello;
    String macAddress;
    //    String imei ;
    String deviceID;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        Context context = getApplicationContext();

        hello = (TextView) findViewById(R.id.hello);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnScanBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, ScannedBarcodeActivity.class), 555);
            }
        });

//        btnScanOffline = findViewById(R.id.btnScanOffline);
//        btnScanOffline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivityForResult(new Intent(MainActivity.this, ScanOfflineActivity.class), 555);
//            }
//        });

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        macAddress = wifiInfo.getMacAddress();
//        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//         imei = telephonyManager.getDeviceSoftwareVersion() ;
        deviceID = Build.FINGERPRINT;

        Toast.makeText(getApplicationContext(), "MAC = " + macAddress + " , ID = (" + deviceID + ")", Toast.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getApplicationContext(), "PHONE STATE PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
        }

        hello.setText(macAddress);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_LONG).show();

        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 555 && resultCode == 666) {
            hello.setText(data.getStringExtra("QR-code"));
            String url = "http://www.google.com?";
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(), response.substring(1, 100), Toast.LENGTH_SHORT).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
            queue.add(request);
        }
    }

    private void scanSuccess() {
        Toast.makeText(getApplicationContext(), "Sucess", Toast.LENGTH_LONG).show();
        printNetworkNames();

//  ... use new scan results ...

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
        printNetworkNames();

//  ... potentially use older scan results ...

    }

    private void printNetworkNames() {
        List<ScanResult> results = wifiManager.getScanResults();
//        Toast.makeText(getApplicationContext(),"printing",Toast.LENGTH_LONG).show();
//        Toast.makeText(getApplicationContext(),results.size()+"",Toast.LENGTH_LONG).show() ;

        StringBuilder networkInfo = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            ScanResult network = results.get(i);
            networkInfo.append(network.SSID + "|" + network.level + "\n");
        }
        Toast.makeText(getApplicationContext(), networkInfo, Toast.LENGTH_LONG).show();

        StringBuilder networkInfo2 = new StringBuilder();

        for (int i = 0; i < results.size(); i++) {
            ScanResult network = results.get(i);
            networkInfo2.append(network.BSSID + "|" + network.level + "\n");
        }

        Toast.makeText(getApplicationContext(), networkInfo2.toString(), Toast.LENGTH_LONG).show();

//        hello.setText(networkInfo2.toString() + "\n" + networkInfo.toString());
    }

}
