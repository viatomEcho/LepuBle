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
    public static final String BT_NAME_ER1_N = "VBeat";
    public static final String BT_NAME_DUOEK = "DuoEK";
    public static final String BT_NAME_PULSEBIT_EX = "Pulsebit";
    public static final String BT_NAME_OXY_LINK = "Oxylink";
    public static final String BT_NAME_KIDS_O2 = "KidsO2";
    public static final String FETAL_DEVICE_NAME = "MD1000AF4";//7 OEM
    public static final String BT_NAME_BABY_O2 = "BabyO2";
    public static final String BT_NAME_OXY_SMART = "OxySmart";
    public static final String BT_NAME_TV221U = "VTM 20F";//4 OEM
    public static final String BT_NAME_PC100 = "PC-100:";//5 OEM   小企鹅后面 5位sn
    public static final String BT_NAME_PC60FW = "PC-60F_SN";//6 OEM
    public static final String BT_NAME_AOJ20A = "AOJ-20A";//2 OEM
    public static final String BT_NAME_BP2 = "BP2";
    public static final String BT_NAME_OXYFIT = "Oxyfit";
    public static final String BT_NAME_VCOMIN = "VCOMIN";//3 OEM
    public static final String BT_NAME_CHECK_POD = "Checkme Pod";
    public static final String BT_NAME_BP2A = "BP2A";
    public static final String BT_NAME_BODY_FAT = "Viatom";//1 OEM
    private static final String BABYTONE = "Babytone";
    public static final String DEVICE_NAME_BODY_FAT = "Body Fat";
    public static final String BT_NAME_O2M = "O2M"; // O2 Max
    public static final String BT_NAME_CHECKME_O2M = "Checkme O2 Max";
    public static final String BT_NAME_BPM = "BPM-188"; // 捷美瑞血压计
    public static final String BT_NAME_BPM_B02 = "BPM-B02";
    //    private static final String BPM_PRODUCT_NAME = " B02T";
    public static final String BPM_PRODUCT_NAME = " B02T/B02S";
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
    public static final String BT_NAME_PC_60NW = "PC-60NW-1_SN";//【蓝牙名】 + SN后六位
    public static final String BT_NAME_PC_60NW_W = "PC-60NW-1";//显示名
    public static final String BT_NAME_POD_1W = "POD-1_SN";//【蓝牙名】 + SN后四位
    public static final String BT_NAME_POD_1W_W = "POD-1W";//显示名
    public static final String BT_NAME_PC_60B = "PC-60B_SN";//【蓝牙名】 + SN后六位 PC-60B_SN000007
    public static final String BT_NAME_PC_60B_B = "PC-60B";//显示名

    public static final String BT_NAME_RINGO2 = "O2NCI";
    public static final String BT_NAME_KCA = "KCA"; // 康康血压计
    public static final String BT_NAME_PC80B = "PC80B";
    public static final String BT_NAME_BPW1 = "BPW1"; // 金亿帝血压手表
    public static final String BT_NAME_F4_SCALE = "MY_SCALE"; // F4体脂秤
    public static final String BT_NAME_F5_SCALE = "MY_SCALE"; // F5体脂秤

    public static final String BT_NAME_PC66A = "PC-66A";
    public static final String BT_NAME_PC66B = "PC-66B";
    public static final String BT_NAME_PC68A = "PC-68A";
    public static final String BT_NAME_PC68B = "PC-68B";

    public static final String BT_NAME_AP20 = "AP-20";


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
    public static final int MODEL_BABYO2 = 13;
    public static final int MODEL_OXYSMART = 14;
    public static final int MODEL_TV221U = 15;
    public static final int MODEL_ER1_N = 16;
    public static final int MODEL_PC100 = 17;
    public static final int MODEL_AOJ20A = 18;
    public static final int MODEL_BP2 = 19;
    public static final int MODEL_OXYFIT = 20;
    public static final int MODEL_VCOMIN = 21;
    public static final int MODEL_CHECK_POD = 22;
    public static final int MODEL_BP2A = 23;
    public static final int MODEL_BODY_FAT = 24;
    //O2plus
    public static final int MODEL_O2M = 25;
    public static final int MODEL_BPM = 26;
    public static final int MODEL_LEM = 27;
    public static final int MODEL_FHR = 28;
    public static final int MODEL_BABYO2N = 29;
    public static final int MODEL_PC60FW = 30;
    public static final int MODEL_BP2T = 31;
    public static final int MODEL_BP2W = 32;
    public static final int MODEL_ER2 = 33;
    public static final int MODEL_STATION = 34;
    public static final int MODEL_POD2B = 35;

    public static final int MODEL_PC_60NW= 36;
    public static final int MODEL_POD_1W= 37;
    public static final int MODEL_PC_60B= 38;

    public static final int MODEL_RINGO2 = 41;
    public static final int MODEL_KCA = 42;
    public static final int MODEL_PC80B = 43;
    public static final int MODEL_BPW1 = 44;
    public static final int MODEL_F4_SCALE = 45;
    public static final int MODEL_F5_SCALE = 46;

    public static final int MODEL_PC_6N = 47;
    public static final int MODEL_AP20 = 48;


    @IntDef({MODEL_CHECKO2, MODEL_SNOREO2, MODEL_SLEEPO2, MODEL_O2RING, MODEL_WEARO2, MODEL_SLEEPU, MODEL_ER1, MODEL_ER1_N,
            MODEL_DUOEK, MODEL_ER2, MODEL_PULSEBITEX, MODEL_OXYLINK, MODEL_KIDSO2, MODEL_FETAL, MODEL_BABYO2, MODEL_OXYSMART,
            MODEL_TV221U, MODEL_PC100, MODEL_AOJ20A, MODEL_OXYFIT, MODEL_VCOMIN, MODEL_CHECK_POD, MODEL_BODY_FAT, MODEL_LEM,
            MODEL_BABYO2N, MODEL_BP2T, MODEL_BP2W, MODEL_STATION, MODEL_POD2B, MODEL_PC_60NW, MODEL_POD_1W, MODEL_PC_60B,
            MODEL_BP2, MODEL_RINGO2, MODEL_KCA, MODEL_O2M, MODEL_BPM,MODEL_BP2A, MODEL_PC60FW, MODEL_PC80B, MODEL_FHR, MODEL_BPW1,
            MODEL_F4_SCALE, MODEL_F5_SCALE, MODEL_PC_6N, MODEL_AP20})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MODEL {

    }

    public @MODEL
    static int getDeviceModel(String deviceName) {

        if (deviceName == null || deviceName.length() == 0) {
            return MODEL_UNRECOGNIZED;
        }

        if (deviceName.contains(BT_NAME_PC60FW)) {
            return Bluetooth.MODEL_PC60FW;
        } else if (FETAL_DEVICE_NAME.equals(deviceName)) {
            return MODEL_FETAL;
        } else if (BT_NAME_TV221U.equals(deviceName)) {
            return MODEL_TV221U;
        } else if (deviceName.contains(BT_NAME_PC100)) {
            return MODEL_PC100;
        } else if (BT_NAME_AOJ20A.equals(deviceName)) {
            return MODEL_AOJ20A;
        } else if (deviceName.contains(BT_NAME_VCOMIN)) {
            return MODEL_VCOMIN;
        } else if (deviceName.contains(BT_NAME_CHECK_POD)) {
            return MODEL_CHECK_POD;
        } else if (BT_NAME_BODY_FAT.equals(deviceName)) {
            return MODEL_BODY_FAT;
        } else if (deviceName.contains(BT_NAME_POD2B)) {
            return MODEL_POD2B;
        } else if (deviceName.contains(BT_NAME_PC_60NW)){
            return MODEL_PC_60NW;
        } else if (deviceName.contains(BT_NAME_POD_1W)) {
            return MODEL_POD_1W;
        } else if (deviceName.contains(BT_NAME_PC_60B)){
            return MODEL_PC_60B;
        } else if (deviceName.contains(BT_NAME_KCA)) {
            return MODEL_KCA;
        } else if (deviceName.contains(BT_NAME_PC80B)) {
            return MODEL_PC80B;
        } else if (deviceName.contains(BT_NAME_PC66A) || deviceName.contains(BT_NAME_PC66B)
                || deviceName.contains(BT_NAME_PC68A) || deviceName.contains(BT_NAME_PC68B)) {
            return MODEL_PC_6N;
        } else if (deviceName.contains(BT_NAME_AP20)) {
            return MODEL_AP20;
        }

        if (deviceName.split(" ").length == 0) {
            return MODEL_UNRECOGNIZED;
        }

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
            case BT_NAME_ER1_N:
                return MODEL_ER1_N;
            case BT_NAME_DUOEK:
                return MODEL_DUOEK;
            case BT_NAME_DEVICES_ER2:
                return MODEL_ER2; // vihealth MODEL_DEVICE_ER2
            case BT_NAME_PULSEBIT_EX:
                return MODEL_PULSEBITEX;
            case BT_NAME_OXY_LINK:
                return MODEL_OXYLINK;
            case BT_NAME_KIDS_O2:
                return MODEL_KIDSO2;
            case BT_NAME_BABY_O2:
                return MODEL_BABYO2;
            case BT_NAME_OXY_SMART:
                return MODEL_OXYSMART;
            case BT_NAME_OXYFIT:
                return MODEL_OXYFIT;
            case BT_NAME_BP2:
                return MODEL_BP2;
            case BT_NAME_BP2A:
                return MODEL_BP2A;
            case BT_NAME_BP2T:
                return MODEL_BP2T;
            case BT_NAME_BP2W:
                return MODEL_BP2W;
            case BT_NAME_RINGO2:
                return MODEL_RINGO2;
            case BT_NAME_O2M:
                return MODEL_O2M;
            case BT_NAME_LEM:
                return MODEL_LEM;
            case BT_NAME_BPM:
                return MODEL_BPM;
            case BT_NAME_FHR:
                return MODEL_FHR; // vihealth MODEL_P600L
            case BT_NAME_BABYO2N:
                return MODEL_BABYO2N;
            case BT_NAME_STATION:
                return MODEL_STATION;
            case BT_NAME_BPW1:
                return MODEL_BPW1;
//            case BT_NAME_F4_SCALE:
//                return MODEL_F4_SCALE;
//            case BT_NAME_F5_SCALE:
//                return MODEL_F5_SCALE;
            default:
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

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(model);
        out.writeString(name);
        out.writeParcelable(device, flags);
        out.writeString(macAddr);
        out.writeInt(rssi);
    }
    public static final Creator<Bluetooth> CREATOR = new Creator<Bluetooth>() {
        @Override
        public Bluetooth createFromParcel(Parcel in) {
            return new Bluetooth(in);
        }
        @Override
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
