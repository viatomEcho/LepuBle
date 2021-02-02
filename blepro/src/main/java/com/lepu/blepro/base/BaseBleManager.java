package com.lepu.blepro.base;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lepu.blepro.utils.ByteArrayKt;
import com.lepu.blepro.utils.LepuBleLog;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

/**
 * author: wujuan
 * created on: 2021/1/26 17:32
 * description:
 */
public abstract class BaseBleManager extends BleManager {

    public final static UUID service_uuid =
            UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339");
    public final static UUID write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    public final static UUID notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private BluetoothGattCharacteristic write_char, notify_char;

    private NotifyListener listener;

    public void setNotifyListener(NotifyListener listener) {
        this.listener = listener;
    }

    public BaseBleManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private class MyManagerGattCallback extends BleManagerGattCallback {

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(service_uuid);
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid);
                notify_char = service.getCharacteristic(notify_uuid);
            }
            // Validate properties
            boolean notify = false;
            if (notify_char != null) {
                final int properties = notify_char.getProperties();
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            }
            boolean writeRequest = false;
            if (write_char != null) {
                final int properties = write_char.getProperties();
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
                write_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            }
            // Return true if all required services have been found
            return write_char != null && notify_char != null
                    && notify && writeRequest;
        }


        // If you have any optional services, allocate them here. Return true only if
        // they are found.
        @Override
        protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
            return super.isOptionalServiceSupported(gatt);
        }

        // Initialize your device here. Often you need to enable notifications and set required
        // MTU or write some initial data. Do it here.
        @Override
        protected void initialize() {
            beginAtomicRequestQueue()
                    .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
//                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
//                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
//                    .add(sleep(500))
                    .add(enableNotifications(notify_char))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();
            setNotify();
            BaseBleManager.this.init();

        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            write_char = null;
            notify_char = null;
        }
    }
    protected abstract void init();

    public void setNotify() {
        setNotificationCallback(notify_char)
                .with((device, data) -> {
//                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
                    listener.onNotify(device, data);
                });
    }

    public void sendCmd(byte[] bytes) {

        LepuBleLog.d("BaseBleManager send: " + ByteArrayKt.bytesToHex(bytes));

        writeCharacteristic(write_char, bytes)
                .split()
                .done(device -> {
//                    LogUtils.d(device.getName() + " send: " + ByteArrayKt.bytesToHex(bytes));
                })
                .enqueue();
    }


    @Override
    public void log(final int priority, @NonNull final String message) {
//        if (Build.DEBUG || priority == Log.ERROR) {
//            Log.println(priority, "MyBleManager", message);
//        }
//        LepuBleLog.d(message);
    }

}