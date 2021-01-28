package com.lepu.demo.ble;

import android.content.Context;
import android.text.TextUtils;


/**
 *  blepro--BleServiceHelper 和 app---BindDeviceUtils 的管理帮手
 *  意图让有关视图的的绑定设备、连接、断开连接只使用此唯一工具
 *
 *  （将来应该存在于独立的module中）
 */
public class DeviceStateHelper {

//
//    public static void reconnect(MyDevice device){
//        if (device == null)return;
//        switch (Integer.parseInt(device.getDeviceType())){
//            case O2RING:
//                BleServiceHelper.Companion.getBleServiceHelper().reconnectO2();
//                break;
//        }
//
//    }
//
//    public static void reconnect(int deviceType){
//        switch (deviceType){
//            case O2RING:
//                BleServiceHelper.Companion.getBleServiceHelper().reconnectO2();
//                break;
//        }
//
//    }
//
//    public static void connectO2(Context context, Bluetooth bluetoothDevice){
//        if (bluetoothDevice == null){
//            LogUtils.d("connectO2 Bluetooth is null");
//            return;
//        }
//        BleServiceHelper.Companion.getBleServiceHelper().connectO2(context, bluetoothDevice.getDevice());
//
//    }
//
//    public static int getDeviceState(MyDevice device){
//        if (device == null)return -1;
//        return getDeviceState(Integer.parseInt(device.getDeviceType()));
//
//    }
//
    /**
     * 先判断是否连接，如果连接了，就是已连接，没连接在判断是否绑定
     */
//    private static int getDeviceState(int type){
//        switch (type){
//            case WATCH_TYPE:
//            case BAND_TYPE:
//
//                break;
//            case ER1_TYPE:
//            case ER2_TYPE:
//
//                break;
//            case SCALE_TYPE:
//
//                break;
//            case O2RING:
//                BleServiceHelper helper = BleServiceHelper.Companion.getBleServiceHelper();
//                int o2ConnectState = helper.getO2ConnectState();
//                LogUtils.d("getO2ConnectState", o2ConnectState);
//
//                if (o2ConnectState == BleConst.DeviceState.CONNECTED || o2ConnectState == BleConst.DeviceState.CONNECTING){
//                    return o2ConnectState;
//                }else{
//                    // 不满足已连接 连接中 检查是否绑定
//                    String deviceName = SPUtils.getInstance(Const.CONFIG_USER).getString(ACTION_O2RING_DEVICE_NAME);
//                    return TextUtils.isEmpty(deviceName) ? BleConst.DeviceState.UNBOUND : BleConst.DeviceState.DISCONNECTED;
//                }
//
//            default:
//                break;
//
//        }
//
//        return -1;
//
//    }
//
//    /**
//     * 收到执行绑定设置的通知（EventOxyInfo）
//     *
//     * @param event
//     */
//    public static void bindO2(BleProEvent event, int currentModel, Bluetooth currentBluetooth){
//        if (currentBluetooth == null || currentBluetooth.getModel() != currentModel)
//            return;
//        LogUtils.d("start binding...");
//        OxyBleResponse.OxyInfo oxyInfo = (OxyBleResponse.OxyInfo) event.getData();
//        switch (currentModel){
//            case Bluetooth.MODEL_O2RING:
//                //真正绑定执行
//                com.lepu.lepucare.util.DeviceStateHelper.bindO2Ring(currentBluetooth, oxyInfo);
//                LogUtils.d("to bind ".concat(currentBluetooth.getName()));
//                O2RingEvent.post(O2RingEvent.O2UIBindFinish, currentBluetooth);
//                MyDevice curDevice = BindDeviceUtils.getCurrentDevice();
//                O2Realm.Companion.saveDeviceInfo(curDevice.DeviceName, Integer.parseInt(curDevice.DeviceType),oxyInfo.getBtlVersion(),oxyInfo.getSwVersion(),oxyInfo.getHwVersion(),oxyInfo.getSn(),curDevice.MacAddress);
//                break;
//        }
//
//    }
//
//
//
//    public static void bindO2Ring(Bluetooth b, OxyBleResponse.OxyInfo oxyInfo){
//        LogUtils.d(b.toString());
//        MyDevice m = new MyDevice();
//
//        m.setDeviceName(b.getName());
//        m.setMacAddress(b.getMacAddr());
//        m.setSerialNumber(oxyInfo.getSn());
//        m.setDeviceType(String.valueOf(Constants.DeviceType.O2RING));
//
//        BindDeviceUtils.bindNewDevice(m);
//
//        SPTool.saveO2RingDeviceName(m.getDeviceName());
//        SPTool.saveO2RingDeviceAddress(m.getMacAddress());
////        SPTool.setGlobalDeviceType(Constants.DeviceType.O2RING, true);
//
//        RunVarsKt.setOxyName(m.getDeviceName());
//        RunVarsKt.setOxySn(m.SerialNumber);
//        RunVarsKt.setHasOxy(true);
//    }
//
//    public static void disconnectO2(boolean autoReconnect) {
//        BleServiceHelper bleServiceHelper = BleServiceHelper.Companion.getBleServiceHelper();
//        bleServiceHelper.disconnectO2(autoReconnect);
//        LogUtils.d("disconnectO2 yes!!!");
//    }
//
//    /**
//     * disconnect and unbound
//     */
//    public static void unboundO2(){
//        disconnectO2(false);
//        unboundO2Ring();
//    }
//
//    /**
//     *
//     */
//    public static void unboundO2Ring(){
//        SPUtils.getInstance(Const.CONFIG_USER).remove(ACTION_O2RING_DEVICE_NAME);
//        SPUtils.getInstance(Const.CONFIG_USER).remove(ACTION_O2RING_DEVICE_ADDRESS);
//    }




}
