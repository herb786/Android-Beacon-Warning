package com.hacaller.hazardsign;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

public class DeprecatedHazardSign extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
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
            mBluetoothAdapter.startLeScan(myLeScanCallback);
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
            mBluetoothAdapter.startLeScan(myLeScanCallback);
        }
    }

    BluetoothAdapter.LeScanCallback myLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("LEBRSSI", String.valueOf(rssi));
            txtSignalIntensity.setText(String.format("RSSI: %d db",rssi));
            if (rssi > -40){
                imgWarningSign.setImageResource(R.drawable.biohazard);
                txtWarningMessage.setText("Your life is in danger!");
            } else if (rssi > -70) {
                imgWarningSign.setImageResource(R.drawable.stop);
                txtWarningMessage.setText("Too close!");
            } else {
                imgWarningSign.setImageResource(R.drawable.warning);
                txtWarningMessage.setText("Please stay away!!!");
            }
        }
    };
}
