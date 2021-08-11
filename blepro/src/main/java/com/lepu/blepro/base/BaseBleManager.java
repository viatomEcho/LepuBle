package com.lepu.blepro.base;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.util.Log;

import androidx.annotation.NonNull;

import com.lepu.blepro.ble.data.LepuDevice;
import com.lepu.blepro.utils.ByteArrayKt;
import com.lepu.blepro.utils.LepuBleLog;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.RequestQueue;

/**
 * author: wujuan
 * created on: 2021/1/26 17:32
 * description:
 */
public abstract class BaseBleManager extends BleManager {
    public final static String TAG = "BaseBleManager";
    public UUID service_uuid;
    public UUID write_uuid;
    public UUID notify_uuid;

    public BluetoothGattCharacteristic write_char, notify_char;

    private NotifyListener listener;

    boolean isUpdater = false;

    public void setNotifyListener(NotifyListener listener) {
        this.listener = listener;
    }

    public BaseBleManager(@NonNull final Context context) {
        super(context);
        initUUID();

    }

    public abstract void initUUID();

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

            LepuBleLog.d(TAG, "id:" + service_uuid +",,,,,"+ write_uuid +",,,,"+ notify_uuid + ",,,,isUpdater="+ isUpdater);


//            final BluetoothGattService service = gatt.getService(service_uuid);
//            if (service != null) {
//                write_char = service.getCharacteristic(write_uuid);
//                notify_char = service.getCharacteristic(notify_uuid);
//            }
//            // Validate properties
//            boolean notify = false;
//            if (notify_char != null) {
//                final int properties = notify_char.getProperties();
//                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
//            }
//            boolean writeRequest = false;
//            if (write_char != null) {
//                final int properties = write_char.getProperties();
//                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
//                write_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//            }
//            // Return true if all required services have been found
//            return write_char != null && notify_char != null
//                    && notify && writeRequest;

            final BluetoothGattService service = gatt.getService(service_uuid);
            LepuBleLog.d(TAG, "service ==  " + service);

            if (isUpdater && service == null ) return true;
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid);
                notify_char = service.getCharacteristic(notify_uuid);
            }
            LepuBleLog.d(TAG, "writeChar ==  " + write_char);
            LepuBleLog.d(TAG, "notifyChar ==  " + notify_char);
            // Validate properties
            boolean notify = false;
            if (notify_char != null) {
                final int properties = notify_char.getProperties();
                LepuBleLog.d(TAG, "notifyChar properties ==  " + properties);
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                LepuBleLog.d(TAG, "notifyChar notify ==  " + notify);
            }
            boolean writeRequest = false;
            if (write_char != null) {
                final int properties = write_char.getProperties();
                LepuBleLog.d(TAG, "writeChar properties ==  " + properties);
                int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
                if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                    writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
                }
                write_char.setWriteType(writeType);
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 || (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
                LepuBleLog.d(TAG, "writeChar writeRequest ==  " + writeRequest);

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
//            beginAtomicRequestQueue()
//                    .add(requestMtu(23) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
//                            .with((device, mtu) -> LepuBleLog.d(TAG, "MTU set to " + mtu))
//                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
////                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
////                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
////                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
////                    .add(sleep(500))
//                    .add(enableNotifications(notify_char))
//                    .done(device -> LepuBleLog.d(TAG, "Target initialized"))
//                    .enqueue();
            LepuBleLog.d(TAG, "initialize");

            initReqQueue();
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
                    LepuBleLog.d(TAG,device.getName() + "received==" + ByteArrayKt.bytesToHex(data.getValue()) + "size =" + ByteArrayKt.bytesToHex(data.getValue()).length());
                    listener.onNotify(device, data);
                });
    }

    public abstract  void initReqQueue();

    public void sendCmd(byte[] bytes) {
        LepuBleLog.d(TAG,"BaseBleManager send: " + ByteArrayKt.bytesToHex(bytes));
        writeCharacteristic(write_char, bytes)
                .split()
                .done(device -> {
//                    LogUtils.d(device.getName() + " send: " + ByteArrayKt.bytesToHex(bytes));
                })
                .enqueue();
    }


    @Override
    public void log(final int priority, @NonNull final String message) {
        LepuBleLog.d(TAG, message);
    }

    public boolean isUpdater() {
        return isUpdater;
    }

    public void setUpdater(boolean updater) {
        isUpdater = updater;
    }
}