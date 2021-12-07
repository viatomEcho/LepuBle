package com.lepu.blepro.objs;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Bluetooth implements Parcelable {

    public static final String BT_NAME_O2 = "O2";
    public static final String BT_NAME_SNO2 = "O2BAND";
    public static final String BT_NAME_SPO2 = "SleepO2";
    public static final String BT_NAME_O2RING = "O2Ring";
    public static final String BT_NAME_WEARO2 = "WearO2";
    public static final String BT_NAME_SLEEPU = "SleepU";
    public static final String BT_NAME_ER1 = "ER1";
    public static final String BT_NAME_DUOEK = "DuoEK";
    public static final String BT_NAME_PULSEBIT_EX = "Pulsebit";
    public static final String BT_NAME_OXY_LINK = "Oxylink";
    public static final String BT_NAME_KIDS_O2 = "KidsO2";
    public static final String BT_NAME_FETAL = "MD1000AF4";
    public static final String BT_NAME_BP2 = "BP2";
    public static final String BT_NAME_BP2A = "BP2A";

    public static final String BT_NAME_RINGO2 = "O2NCI";
    public static final String BT_NAME_KCA = "KCA"; // 康康血压计
    public static final String BT_NAME_O2M = "O2M"; // O2 Max
    public static final String BT_NAME_BPM = "BPM-188"; // 捷美瑞血压计
    public static final String BT_NAME_ER2 = "ER2";
    public static final String BT_NAME_PC100 = "PC-100"; //小企鹅后面 5位sn
    public static final String BT_NAME_PC60FW = "PC-60F_SN";//6 OEM


    public static final String BT_NAME_ER1_N = "ER1";
    public static final String FETAL_DEVICE_NAME = "MD1000AF4";//7 OEM
    public static final String BT_NAME_BABY_O2 = "BabyO2";
    public static final String BT_NAME_OXY_SMART = "OxySmart";
    public static final String BT_NAME_TV221U = "VTM 20F";//4 OEM
    public static final String BT_NAME_PC60FW_SN = "PC-60F_SN";//6 OEM
    public static final String BT_NAME_AOJ20A = "AOJ-20A";//2 OEM
    public static final String BT_NAME_OXYFIT = "Oxyfit";
    public static final String BT_NAME_VCOMIN = "VCOMIN";//3 OEM
    public static final String BT_NAME_CHECK_POD = "Checkme Pod";
    public static final String BT_NAME_BODY_FAT = "Viatom";//1 OEM
    private static final String BT_NAME_BABYTONE = "Babytone";
    public static final String BT_NAME__BODY_FAT = "Body Fat";
    public static final String BT_NAME_CHECKME_O2M = "Checkme O2 Max";
    public static final String BT_NAME_BPM_B02 = "BPM-B02";
    private static final String BT_NAME_BPM_PRODUCT = " B02T";
    public static final String BT_NAME_LEM = "LEM1";
    public static final String BT_NAME_LEM_M1 = "M1";
    public static final String BT_NAME_FHR = "FHR-666(BLE)";//OEM
    public static final String BT_NAME_FHR_P600L = "Babytone";//OEM
    public static final String BT_NAME_BABYO2N = "BabyO2N";//盒子版BabyO2
    public static final String BT_NAME_BABYO2S2 = "BabyO2 S2";//盒子版BabyO2N S2
    public static final String BT_NAME_PC60FW_NEW = "PC-60FW";//显示名
    public static final String BT_NAME_BP2T = "BP2T";
    public static final String BT_NAME_BP2W = "BP2W";
    public static final String BT_NAME_DEVICES_ER2 = "ER2";//新增ER2【产线用】
    public static final String BT_NAME_STATION = "Station";//BabyO2盒子升级专用
    public static final String BT_NAME_POD2B = "POD-2B_SN";//POD-2B_SN7295【蓝牙名】
    public static final String BT_NAME_POD2W = "POD-2W";//【显示名】
    public static final String BT_NAME_PC80B = "PC80B";
    public static final String BT_NAME_BPW1 = "BPW1"; // 金亿帝血压手表
    public static final String BT_NAME_MY_SCALE = "MY_SCALE"; // F5体脂秤


    public static final int MODEL_UNRECOGNIZED = 0;
    public static final int MODEL_CHECKO2 = 1;
    public static final int MODEL_SNOREO2 = 2;
    public static final int MODEL_SLEEPO2 = 3;
    public static final int MODEL_O2RING = 4;
    public static final int MODEL_WEARO2 = 5;
    public static final int MODEL_SLEEPU = 6;
    public static final int MODEL_ER1 = 7;
    public static final int MODEL_DUOEK = 8;
    public static final int MODEL_PULSEBITEX = 9;
    public static final int MODEL_OXYLINK = 10;
    public static final int MODEL_KIDSO2 = 11;
    public static final int MODEL_FETAL = 12;
    public static final int MODEL_BP2 = 13;
    public static final int MODEL_RINGO2 = 14;
    public static final int MODEL_KCA = 15;
    public static final int MODEL_O2MAX = 16;
    public static final int MODEL_BPM = 17;
    public static final int MODEL_ER2 = 18;
    public static final int MODEL_PC100 = 19;
    public static final int MODEL_BP2A = 20;
    public static final int MODEL_PC60FW = 21;
    public static final int MODEL_PC80B = 22;
    public static final int MODEL_FHR = 23;
    public static final int MODEL_BPW1 = 24;
    public static final int MODEL_MY_SCALE = 25;


    @IntDef({MODEL_CHECKO2, MODEL_SNOREO2, MODEL_SLEEPO2, MODEL_O2RING, MODEL_WEARO2, MODEL_SLEEPU,
            MODEL_ER1,MODEL_DUOEK, MODEL_ER2, MODEL_PULSEBITEX, MODEL_OXYLINK, MODEL_KIDSO2, MODEL_FETAL,
            MODEL_BP2, MODEL_RINGO2, MODEL_KCA, MODEL_O2MAX, MODEL_BPM,MODEL_BP2A,MODEL_PC60FW,MODEL_PC80B,MODEL_FHR,MODEL_BPW1,MODEL_MY_SCALE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MODEL {

    }

    public @MODEL
    static int getDeviceModel(String deviceName) {

        if (deviceName == null || deviceName.length() == 0) {
            return MODEL_UNRECOGNIZED;
        }

        if (deviceName.contains(BT_NAME_MY_SCALE)) {
            return Bluetooth.MODEL_MY_SCALE;
        }

        if (deviceName.contains(BT_NAME_PC60FW)) {
            return Bluetooth.MODEL_PC60FW;
        }

        if (deviceName.split(" ").length == 0)
            return MODEL_UNRECOGNIZED;

        String deviceNamePrefix = deviceName.split(" ")[0];
        switch (deviceNamePrefix) {
            case BT_NAME_O2:
                return MODEL_CHECKO2;
            case BT_NAME_SNO2:
                return MODEL_SNOREO2;
            case BT_NAME_SPO2:
                return MODEL_SLEEPO2;
            case BT_NAME_O2RING:
                return MODEL_O2RING;
            case BT_NAME_WEARO2:
                return MODEL_WEARO2;
            case BT_NAME_SLEEPU:
                return MODEL_SLEEPU;
            case BT_NAME_ER1:
                return MODEL_ER1;
            case BT_NAME_DUOEK:
                return MODEL_DUOEK;
            case BT_NAME_ER2:
                return MODEL_ER2;
            case BT_NAME_PULSEBIT_EX:
                return MODEL_PULSEBITEX;
            case BT_NAME_OXY_LINK:
                return MODEL_OXYLINK;
            case BT_NAME_KIDS_O2:
                return MODEL_KIDSO2;
            case BT_NAME_FETAL:
                return MODEL_FETAL;
            case BT_NAME_BP2:
                return MODEL_BP2;
            case BT_NAME_BP2A:
                return MODEL_BP2;
            case BT_NAME_RINGO2:
                return MODEL_RINGO2;
            case BT_NAME_O2M:
                return MODEL_O2MAX;
            case BT_NAME_BPM:
                return MODEL_BPM;
                case BT_NAME_BPW1:
                return MODEL_BPW1;
            default:
                if (deviceNamePrefix.startsWith(BT_NAME_KCA))
                    return MODEL_KCA;
                else if (deviceNamePrefix.startsWith(BT_NAME_PC80B))
                    return MODEL_PC80B;
                else if (deviceNamePrefix.startsWith(BT_NAME_FHR))
                    return MODEL_FHR;
                return MODEL_UNRECOGNIZED;
        }
    }


    @MODEL
    private int model;
    private String name;
    private BluetoothDevice device;
    private String macAddr;
    private int rssi;

    public Bluetooth(@MODEL int model, String name, BluetoothDevice device, int rssi) {
        this.model = model;
        this.name = name == null ? "" : name;
        this.device = device;
        this.macAddr = device.getAddress();
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bluetooth) {
            Bluetooth b = (Bluetooth) obj;
            return (this.macAddr.equals(b.getMacAddr()));
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(model);
        out.writeString(name);
        out.writeParcelable(device, flags);
        out.writeString(macAddr);
        out.writeInt(rssi);
    }
    public static final Creator<Bluetooth> CREATOR = new Creator<Bluetooth>() {
        public Bluetooth createFromParcel(Parcel in) {
            return new Bluetooth(in);
        }
        public Bluetooth[] newArray(int size) {
            return new Bluetooth[size];
        }
    };

    private Bluetooth(Parcel in) {
        model = in.readInt();
        name = in.readString();
        device = in.readParcelable(Bluetooth.class.getClassLoader());
        macAddr = in.readString();
        rssi = in.readInt();
    }

    @Override
    public String toString() {
        return "Bluetooth{" +
                "model=" + model +
                ", name='" + name + '\'' +
                ", device=" + device +
                ", macAddr='" + macAddr + '\'' +
                ", rssi=" + rssi +
                '}';
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @MODEL
    public int getModel() {
        return model;
    }

    public void setModel(@MODEL int model) {
        this.model = model;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
