package com.lepu.blepro.ble.cmd.er2;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Er2RtData {
    DeviceRunParameters parameters;

    // reserved 17 bytes
    private WaveData waveData;

    public Er2RtData(byte[] bytes) {
        if (bytes == null || bytes.length < DeviceRunParameters.LENGTH) {
            return;
        }

        byte[] paraData = Arrays.copyOfRange(bytes, 0, DeviceRunParameters.LENGTH);
        parameters = new DeviceRunParameters(paraData);

        byte[] waveBuf = Arrays.copyOfRange(bytes, DeviceRunParameters.LENGTH, bytes.length);
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

    public WaveData getWaveData() {
        return waveData;
    }

    class WaveData {
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
            float mv = (float) (n * (1.0035 * 1800) / (4096 * 178.74));
//        float mv = (float) (n * 0.002467);

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
