package mx.slam.myzetrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    BluetoothGatt bluetoothGatt;
    Handler bluetoothHandler;
    SwitchCompat aSwitch;
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    List<BLEDevice> bluetoothDevices = new ArrayList<>();
    boolean oneTimeScan;
    private int pos;
    private static final String TAG = "MainActivity";

    @SuppressLint({"MissingPermission", "HandlerLeak", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        oneTimeScan = true;
        aSwitch = findViewById(R.id.slider);
        recyclerView = findViewById(R.id.recyclerV);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothHandler = new Handler();

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent();
                    enableIntent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    MainActivity.this.startActivity(enableIntent);
                }
                startScanning();
            }else{
                oneTimeScan = false;
                stopScanning();
                adapter = new RecyclerAdapter(bluetoothDevices, (view, position) -> {
                    pos = position;
                    connectToDeviceSelected();
                });
                recyclerView.setAdapter(adapter);
            }
        });

        // have access coarse location or fine location permission enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This needs location access");
            builder.setMessage("Please grant location access to detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1));
            builder.show();
        }
    }

    // Scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                BluetoothDevice nDev = result.getDevice();
                BLEDevice newDevice = new BLEDevice(nDev);
                if(!isDeviceOnList(newDevice))
                    bluetoothDevices.add(newDevice);
                Log.v(TAG, "Fetching scanning result and updating adapter list");
            }catch (SecurityException e) {
                Log.e(TAG, "Something wrong happens: "+e.getMessage(), e);
            }
        }
    };

    private boolean isDeviceOnList(BLEDevice ble){
        for(BLEDevice dev: bluetoothDevices){
            if(dev==ble)    //rarely occurs
                return true;
            if(ble.getName() == null) // do not add this device
                return true;
            if(dev.getName()!=null)
                if(dev.getName().equalsIgnoreCase(ble.getName()))   // when same name occurs
                    return true;
            if(dev.getAddress().equals(ble.getAddress()))   // having same address
                return true;
        }
        return false;
    }

    // Device connection call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // show read or write characteristic operation
            MainActivity.this.runOnUiThread(() -> Log.v(TAG,"device read or wrote to "+gatt.getDevice().getAddress()+"\n"));
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int state) {
            // show when a device connects or disconnects
            Log.v(TAG,"New state: "+ state);
            switch (state) {
                case 0:
                    MainActivity.this.runOnUiThread(() -> {
                        Log.v(TAG,"device disconnected\n");
                        aSwitch.setChecked(false);
                    });
                    break;
                case 2:
                    MainActivity.this.runOnUiThread(() -> {
                        Log.v(TAG,"device connected\n");
                        aSwitch.setChecked(true);
                    });

                    // discover services and characteristics for a device
                    bluetoothGatt.discoverServices();

                    break;
                default:
                    MainActivity.this.runOnUiThread(() -> Log.v(TAG,"unknown state!\n"));
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // when a service is discovered
            MainActivity.this.runOnUiThread(() -> Log.v(TAG,"device service have been discovered\n"));
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Broadcast a characteristic when read operation occurs
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                informUpdate(characteristic);
            }
        }
    };

    private void informUpdate(final BluetoothGattCharacteristic characteristic) {
        Log.v(TAG,"UUID: "+characteristic.getUuid());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"coarse location permission granted",Toast.LENGTH_SHORT).show();
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> {
                });
                builder.show();
            }
        }
    }

    public void startScanning() {
        Log.v(TAG,"Starting scanning");
        aSwitch.setChecked(true);
        AsyncTask.execute(() -> {
            try {
                bluetoothLeScanner.startScan(leScanCallback);
            }catch (SecurityException e){
                Log.e(TAG,e.getMessage(),e);
            }
        });
        if(oneTimeScan) {
            oneTimeScan = false;

        }
        Log.v(TAG,"Scanning started\n");
    }

    public void stopScanning() {
        Log.v(TAG,"Stopping scanning");
        aSwitch.setChecked(false);
        AsyncTask.execute(() -> {
            try {
                bluetoothLeScanner.stopScan(leScanCallback);
            }catch (SecurityException e){
                Log.e(TAG,e.getMessage(),e);
            }
        });
        Log.v(TAG,"Stopped Scanning\n");
    }

    @SuppressLint("MissingPermission")
    public void connectToDeviceSelected() {
        StringBuilder dev = new StringBuilder();
        Log.v(TAG, "Trying to connect to selected device on recycler adapter pos: "+pos);
        if(!bluetoothDevices.isEmpty() && pos>=0) {
            BLEDevice devices = bluetoothDevices.get(pos);
            dev.append("Name: ").append(devices.getName());
            dev.append("Address: ").append(devices.getAddress());
            dev.append("Type: ").append(devices.getType());
            dev.append("\n");
            Log.v(TAG,dev.toString());
            bluetoothGatt = devices.ble.connectGatt(getApplicationContext(), false, btleGattCallback);
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnectDeviceSelected() {
        if(bluetoothGatt!=null) {
            Log.v(TAG, "Disconnecting from selected device\n");
            bluetoothGatt.disconnect();
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            Log.v(TAG,"Service discovered: " + uuid);
            //MainActivity.this.runOnUiThread(() -> Log.v(TAG,"Service discovered: "+uuid+"\n"));
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                Log.v(TAG,"Characteristic discovered for service: " + charUuid);
                //MainActivity.this.runOnUiThread(() -> Log.v(TAG,"Characteristic discovered for service: "+charUuid+"\n"));

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
        disconnectDeviceSelected();
    }

}