package com.lepu.blepro.ble.data;

import android.util.Log;

import com.lepu.blepro.ble.cmd.Er2RTParam;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Watch4gRtData {
    Er2RTParam parameters;

    // reserved 17 bytes
    private WaveData waveData;

    public Watch4gRtData(byte[] bytes) {
        if (bytes == null || bytes.length < Er2RTParam.LENGTH) {
            return;
        }

        byte[] paraData = Arrays.copyOfRange(bytes, 0, Er2RTParam.LENGTH);
        parameters = new Er2RTParam(paraData);

        byte[] waveBuf = Arrays.copyOfRange(bytes, Er2RTParam.LENGTH, bytes.length);
        this.waveData = new WaveData(waveBuf);
    }

    public int getHr() {
        if(parameters != null) {
            return parameters.getHr();
        } else {
            return 0;
        }
    }

    public boolean isrFlag() {
        if(parameters != null) {
            return parameters.isrFlag();
        }
        return false;
    }

    public int getBatteryState() {
        if(parameters != null) {
            return parameters.getBatteryState();
        } else {
            return 0;
        }
    }

    public int getBatteryVoltage() {
        if(parameters != null) {
            return parameters.getPercent();
        } else {
            return 0;
        }
    }

    public boolean isSignalPoor() {
        if(parameters != null) {
            return parameters.isSignalPoor();
        } else {
            return false;
        }
    }

    public byte getPercent() {
        if(parameters != null) {
            return parameters.getPercent();
        } else {
            return 0;
        }
    }

    public int getRecordTime() {
        if(parameters != null) {
            return parameters.getRecordTime();
        } else {
            return 0;
        }
    }

    public int getCurrentState() {
        if(parameters != null) {
            return parameters.getCurrentState();
        } else {
            return 0;
        }
    }

    public int getLastState() {
        if(parameters != null) {
            return parameters.getLastState();
        } else {
            return 0;
        }
    }

    public Er2RTParam getRtParam() {
        return parameters;
    }

    public WaveData getWaveData() {
        return waveData;
    }

    public class WaveData {
        private int size;
        private float[] datas;
        private byte[] bytes;

        public WaveData(byte[] bytes) {
            if (bytes == null || bytes.length < 2) {
//            datas = null;
                return;
            }
            this.bytes = ArrayUtils.clone(bytes);

            this.size = bytes[0] & 0xFF + ((bytes[1] & 0xFF) << 8);

            if ((size * 2) != (bytes.length - 2)) {
                size = (bytes.length - 2) / 2;
            }
            datas = new float[size];
            for (int i = 0; i < size; i++) {
//            if((2*i + 2 < bytes.length) && (2*i + 3 < bytes.length)) {
//                datas[i] = byteTomV(bytes[2*i + 2], bytes[2*i + 3]);
//            }
                datas[i] = byteTomV(bytes[2 * i + 2], bytes[2 * i + 3]);
            }
        }

        public float[] getDatas() {
            return datas;
        }

        private float byteTomV(byte a, byte b) {
            if (a == (byte) 0xff && b == (byte) 0x7f)
                return 0f;

            int n = ((a & 0xFF) | (short) (b << 8));

//        float mv = (float) (n*12.7*1800*1.03)/(10*227*4096);
//            float mv = (float) (n * (1.0035 * 1800) / (4096 * 178.74));

            float mv = (float) (n*0.00275*2);

//        float mv = (float) (n * 0.002467);

            Log.d("test12345", "----------n------------" + n);
            Log.d("test12345", "----------mv------------" + mv);


            return mv;
        }

        public int getSize() {
            return size;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

    }
}
