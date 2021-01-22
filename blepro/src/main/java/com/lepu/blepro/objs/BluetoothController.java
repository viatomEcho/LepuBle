package com.lepu.blepro.objs;

import com.lepu.blepro.utils.LepuBleLog;

import java.util.ArrayList;

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
        LepuBleLog.d("addDevice => " + b.getName() + "macAddr:" + b.getMacAddr());

        if (!bleDevices.contains(b)) {
            bleDevices.add(b);
            needNotify = true;
        }
        if (!modelList.contains(b.getModel())) {
            modelList.add(b.getModel());
            needNotify = true;
        }

        return needNotify;
    }

    synchronized public static boolean checkO2Device(String deviceName) {
        return checkO2Device(Bluetooth.getDeviceModel(deviceName));
    }

    synchronized public static boolean checkO2Device(int model) {
        boolean isO2 = false;
        if (model == Bluetooth.MODEL_O2RING || model == Bluetooth.MODEL_O2MAX || model == Bluetooth.MODEL_OXYLINK)
            isO2 = true;
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
}
