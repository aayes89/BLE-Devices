package mx.slam.myzetrack;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;

public class BLEDevice {
    BluetoothDevice ble;
    private String name;
    private String alias;
    private String address;
    private ParcelUuid[] uuids;
    private int type;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @SuppressLint("NewApi")
    public BLEDevice(BluetoothDevice ble) {
        this.ble = ble;
        try {
            name = ble.getName();
            alias = ble.getAlias();
            address = ble.getAddress();
            type = ble.getType();
            uuids = ble.getUuids();
        }catch (SecurityException e){
            Log.e("BLEDevice", e.getMessage(), e);
        }
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getAddress() {
        return address;
    }

    public ParcelUuid[] getUuids() {
        return uuids;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BLEDevice{" +
                "ble=" + ble +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", address='" + address + '\'' +
                ", uuids=" + Arrays.toString(uuids) +
                ", type=" + type +
                ", Standard UUID=" + BTMODULEUUID +
                '}';
    }
}