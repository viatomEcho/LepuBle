package com.lepu.blepro.ble.data;

import android.util.Log;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * @author chenyongfeng
 */
public class Lew3RtData {
    private RtParam parameters;

    // reserved 17 bytes
    private WaveData waveData;

    public Lew3RtData(byte[] bytes) {
        if (bytes == null || bytes.length < RtParam.LENGTH) {
            return;
        }

        byte[] paraData = Arrays.copyOfRange(bytes, 0, RtParam.LENGTH);
        this.parameters = new RtParam(paraData);

        byte[] waveBuf = Arrays.copyOfRange(bytes, RtParam.LENGTH, bytes.length);
        this.waveData = new WaveData(waveBuf);
    }

    public RtParam getRtParam() {
        return parameters;
    }

    public WaveData getWaveData() {
        return waveData;
    }

    public class RtParam {

        public static final int LENGTH = 20;

        private int hr;
        private byte sysFlag;
        private byte percent;
        private int recordTime;
        private byte runStatus;

        public RtParam(byte[] data) {
            if (data == null || data.length != LENGTH) {
                return;
            }

            hr = data[0] & 0xFF + ((data[1] & 0xFF) << 8);
            sysFlag = data[2];
            percent = data[3];

            recordTime = (data[4] & 0xFF) + ((data[5] & 0xFF) << 8)
                    + ((data[6] & 0xFF) << 16) + ((data[7] & 0xFF) << 24);

            runStatus = data[8];

        }

        public int getHr() {
            return hr;
        }

        public byte getSysFlag() {
            return sysFlag;
        }

        public boolean isrFlag() {
            byte sysFlag = getSysFlag();
            return (sysFlag & 0x01) > 0;
        }

        public boolean isSignalPoor() {
            byte sysFlag = getSysFlag();
            return (sysFlag & 0x04) > 0;
        }

        public int getBatteryState() {
            byte sysFlag = getSysFlag();
            return (sysFlag >> 6) & 0x03;
        }

        public byte getPercent() {
            return percent;
        }

        public int getRecordTime() {
            return recordTime;
        }

        public byte getRunStatus() {
            return runStatus;
        }

        public int getCurrentState() {
            byte runStatus = getRunStatus();
            return runStatus & 0x0F;
        }

        public int getLastState() {
            byte runStatus = getRunStatus();
            return (runStatus >> 4) & 0x0F;
        }

        @Override
        public String toString() {
            return "RtParam{" +
                    "hr=" + hr +
                    ", sysFlag=" + sysFlag +
                    ", percent=" + percent +
                    ", recordTime=" + recordTime +
                    ", runStatus=" + runStatus +
                    '}';
        }
    }

    public class WaveData {
        private int size;
        private float[] datas;
        private byte[] bytes;
        int n1 = 0;
        float mv1 = 0;

        public int getN1() {
            return n1;
        }

        public void setN1(int n1) {
            this.n1 = n1;
        }

        public float getMv1() {
            return mv1;
        }

        public void setMv1(float mv1) {
            this.mv1 = mv1;
        }

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
            if (a == (byte) 0xff && b == (byte) 0x7f) {
                return 0f;
            }

            int n = ((a & 0xFF) | (short) (b << 8));

//        float mv = (float) (n*12.7*1800*1.03)/(10*227*4096);
//            float mv = (float) (n * (1.0035 * 1800) / (4096 * 178.74));

//            float mv = (float) (n*0.00275);
            float mv = (float) (n*0.0028985507246377);

//        float mv = (float) (n * 0.002467);
            n1 = n;
            mv1 = mv;
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
