package com.lepu.blepro.objs;

import android.bluetooth.BluetoothDevice;
import com.lepu.blepro.utils.LepuBleLog;
import java.util.ArrayList;

/**
 * @author wujuan
 */
public class BluetoothController {


    public static ArrayList<Integer> getModelList() {
        return modelList;
    }

    public static void setModelList(ArrayList<Integer> modelList) {
        BluetoothController.modelList = modelList;
    }

    private static ArrayList<String> connectedList = new ArrayList<String>();
    private static ArrayList<Bluetooth> bleDevices = new ArrayList<Bluetooth>();
    private static ArrayList<Bluetooth> connectedDevices = new ArrayList<Bluetooth>();
    private static ArrayList<Integer> modelList = new ArrayList<Integer>();

    public void setConnectedList(ArrayList<String> list) {
        connectedList = list;
    }

    synchronized public static boolean addDevice(Bluetooth b) {
        boolean needNotify = false;

        if (!bleDevices.contains(b)) {
            bleDevices.add(b);
            needNotify = true;
        }
        if (!modelList.contains(b.getModel())) {
            modelList.add(b.getModel());
            needNotify = true;
        }
        LepuBleLog.d("addDevice => " + b.getName() + " macAddr:" + b.getMacAddr() + " needNotify:" + needNotify);


        return needNotify;
    }

    synchronized public static boolean checkO2Device(String deviceName) {
        return checkO2Device(Bluetooth.getDeviceModel(deviceName));
    }

    synchronized public static boolean checkO2Device(int model) {
        boolean isO2 = false;
        if (model == Bluetooth.MODEL_O2RING
                || model == Bluetooth.MODEL_AI_S100
                || model == Bluetooth.MODEL_OXYRING
                || model == Bluetooth.MODEL_CMRING
                || model == Bluetooth.MODEL_O2M
                || model == Bluetooth.MODEL_OXYLINK
                || model == Bluetooth.MODEL_BABYO2
                || model == Bluetooth.MODEL_BABYO2N
                || model == Bluetooth.MODEL_BBSM_S1
                || model == Bluetooth.MODEL_BBSM_S2
                || model == Bluetooth.MODEL_CHECKO2
                || model == Bluetooth.MODEL_SLEEPO2
                || model == Bluetooth.MODEL_SNOREO2
                || model == Bluetooth.MODEL_WEARO2
                || model == Bluetooth.MODEL_SLEEPU
                || model == Bluetooth.MODEL_KIDSO2
                || model == Bluetooth.MODEL_OXYFIT
                || model == Bluetooth.MODEL_OXYU) {
            isO2 = true;
        }
        return isO2;
    }


    synchronized static public void clear() {
        bleDevices = new ArrayList<Bluetooth>();
        connectedDevices = new ArrayList<Bluetooth>();
        modelList = new ArrayList<Integer>();
    }

    synchronized public static ArrayList<Bluetooth> getDevices() {
        return bleDevices;
    }

    synchronized public static ArrayList<Bluetooth> getDevices(@Bluetooth.MODEL int model) {
        ArrayList<Bluetooth> list = new ArrayList<Bluetooth>();
        for (Bluetooth b : bleDevices) {
            if (b.getModel() == model) {
                list.add(b);
            }
        }
        LepuBleLog.d("get device: " + model + " -> " + list.size());
        return list;
    }

    synchronized public static ArrayList<Bluetooth> getConnectedDevices() {
        return connectedDevices;
    }

    synchronized public static String getDeviceName(String address) {
//        Optional<Bluetooth> optional = bleDevices.stream().filter(b -> b.getMacAddr().equals(address))
//                .findFirst();
////        if(optional.isPresent()) {
////            return optional.get().getName();
////        } else {
////            return null;
////        }
//
//        return optional.map(Bluetooth::getName).orElse(null);
        for (Bluetooth b : bleDevices) {
            if (b.getMacAddr().equals(address)) {
                return b.getName();
            }
        }
        return null;
    }

    synchronized public static Bluetooth getCurrentBluetooth(BluetoothDevice device) {
        for (Bluetooth b : bleDevices) {
            if (b.getMacAddr().equals(device.getAddress())) {
                return b;
            }
        }
        return null;
    }
}
