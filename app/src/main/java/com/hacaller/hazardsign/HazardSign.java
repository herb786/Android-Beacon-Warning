package com.hacaller.hazardsign;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class HazardSign extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ImageView imgWarningSign;
    TextView txtWarningMessage;
    TextView txtSignalIntensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hazard_sign);

        imgWarningSign = findViewById(R.id.imgWarningSign);
        txtWarningMessage = findViewById(R.id.txtWarningMessage);
        txtSignalIntensity = findViewById(R.id.txtSignalIntensity);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 998);
        } else {
            initBleManager();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(myScanCallback);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 998 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initBleManager();
        }
    }


    private void initBleManager(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 999);
        } else {
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(myScanCallback);
        }
    }

    ScanCallback myScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d("LEBRSSI", String.valueOf(result.getRssi()));
            txtSignalIntensity.setText(String.format("RSSI: %d db",result.getRssi()));
            if (result.getRssi() > -40){
                imgWarningSign.setImageResource(R.drawable.biohazard);
                txtWarningMessage.setText("Your life is in danger!");
            } else if (result.getRssi() > -70) {
                imgWarningSign.setImageResource(R.drawable.stop);
                txtWarningMessage.setText("Too close!");
            } else {
                imgWarningSign.setImageResource(R.drawable.warning);
                txtWarningMessage.setText("Please stay away!!!");
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

}
