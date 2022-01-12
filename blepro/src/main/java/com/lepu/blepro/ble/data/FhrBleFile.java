package com.lepu.blepro.ble.data;

import com.lepu.blepro.utils.PCMCovWavUtil;

/**
 * @author chenyongfeng
 */
public class FhrBleFile {

    private static int[] indexTable={
            -1, -1, -1, -1, 2, 4, 6, 8,
            -1, -1, -1, -1, 2, 4, 6, 8};

    private static int[] stepsizeTable = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};

    private static short[] rsSample8Coff1 = {
            0, 0, 0, 32767, 1, 0, 0, 0,
            -22, 347, -2263, 31759, 3482, -584, 50, -1,
            -25, 484, -3379, 28865, 8069, -1378, 134, -2,
            -20, 472, -3559, 24465, 13443, -2275, 248, -6,
            -12, 374, -3090, 19112, 19112, -3090, 374, -12,
            -6, 248, -2275, 13443, 24465, -3559, 472, -20,
            -2, 134, -1378, 8069, 28865, -3379, 484, -25,
            -1, 50, -584, 3482, 31759, -2263, 347, -22,
            0, 0, 0, 0, 32767, 1, 0, 0
    };

    private static RSP8_CONTEXT RspCont_obj8 = null;

    public static byte[] decode(byte[] aFileBytes) {
        byte[] pcmFileBytes = new byte[aFileBytes.length*4];
        byte[] delta = new byte[2];
        int index = 0;
        int valpred = 0;
        for (int i=0; i<aFileBytes.length; i++) {
            delta[1] = (byte) (aFileBytes[i] & 0x0f);
            delta[0] = (byte) ((aFileBytes[i] >> 4) & 0x0f);
            for (int j=0; j<2; j++) {
                int sign;
                int step;
                int vpdiff;
                int tmp;
                step = stepsizeTable[index];
                vpdiff = 0;
                if ((delta[j] & 0x04) != 0) {
                    vpdiff += step;
                }
                if ((delta[j] & 0x02) != 0) {
                    vpdiff += (step >> 1);
                }
                if ((delta[j] & 0x01) != 0) {
                    vpdiff += (step >> 2);
                }
                vpdiff += (step >> 3);
                sign = delta[j] & 0x08;
                if (sign != 0) {
                    tmp = valpred - vpdiff;
                } else {
                    tmp = valpred + vpdiff;
                }

                if (tmp > 32767) {
                    tmp = 32767;
                } else if (tmp < -32768) {
                    tmp = -32768;
                }

                valpred = tmp;
                index += indexTable[delta[j]];
                if (index < 0) {
                    index = 0;
                }
                if (index > 88) {
                    index = 88;
                }
                pcmFileBytes[i*4 + j*2] = Hex2ByteDTX(tmp)[0];
                pcmFileBytes[i*4 + j*2+1] = Hex2ByteDTX(tmp)[1];
            }
        }
        return pcmFileBytes;
    }

    public static byte[] resample(byte[] pcmFileBytes) {
        byte[] tempFileBytes = new byte[pcmFileBytes.length*5];
        try {
            resample8_init();
            int len = pcmFileBytes.length/64;
            int remain = pcmFileBytes.length % 64;
            byte[] inData = new byte[64];
            int outlen;
            int index = 0;
            for (int i=0; i<=len; i++) {
                if (i == len) {
                    System.arraycopy(pcmFileBytes, i*64, inData, 0, remain);
                    short[] reData = toShortArray(inData);

                    RspCont_obj8.obuf_cnt = 0;
                    RspCont_obj8.in_cnt = (short)(32 * RspCont_obj8.nch);

                    if (RspCont_obj8.insample < RspCont_obj8.outsample) {
                        Resample8Up(reData, remain/2);
                    }

                    if (RspCont_obj8.nch == 1) {
                        outlen = RspCont_obj8.obuf_cnt;
                    } else {
                        outlen = (RspCont_obj8.obuf_cnt >> 1);
                    }
                    for (int j=0; j<outlen*RspCont_obj8.nch; j++) {
                        tempFileBytes[index + j*2] = Hex2ByteDTX(RspCont_obj8.obuf[j])[0];
                        tempFileBytes[index + j*2+1] = Hex2ByteDTX(RspCont_obj8.obuf[j])[1];
                    }
                } else {
                    System.arraycopy(pcmFileBytes, i*64, inData, 0, 64);
                    short[] reData = toShortArray(inData);

                    RspCont_obj8.obuf_cnt = 0;
                    RspCont_obj8.in_cnt = (short)(32 * RspCont_obj8.nch);

                    if (RspCont_obj8.insample < RspCont_obj8.outsample) {
                        Resample8Up(reData, 32);
                    }

                    if (RspCont_obj8.nch == 1) {
                        outlen = RspCont_obj8.obuf_cnt;
                    } else {
                        outlen = (RspCont_obj8.obuf_cnt >> 1);
                    }
                    for (int j=0; j<outlen*RspCont_obj8.nch; j++) {
                        tempFileBytes[index + j*2] = Hex2ByteDTX(RspCont_obj8.obuf[j])[0];
                        tempFileBytes[index + j*2+1] = Hex2ByteDTX(RspCont_obj8.obuf[j])[1];
                    }
                }
                index += outlen*RspCont_obj8.nch*2;
            }
            byte[] resFileBytes = new byte[index];
            for (int i=0; i<resFileBytes.length; i++) {
                resFileBytes[i] = tempFileBytes[i];
            }
            return resFileBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void resample8_init() {
        RspCont_obj8 = new RSP8_CONTEXT();
        RspCont_obj8.nch = 1;
        RspCont_obj8.insample = 2000;
        RspCont_obj8.outsample = 8000;
        RspCont_obj8.sample_index = 0;
        RspCont_obj8.LpScl = (134217728 / 8000) + 1;
    }

    private static void Resample8Up(short[] inData, int len) {
        int i, nch, coff, div_ou, out_data;

        nch = RspCont_obj8.nch - 1;
        div_ou = RspCont_obj8.LpScl;
        RspCont_obj8.in_cnt = (short)(len * RspCont_obj8.nch);
        while (true) {
            int[] tmp_data = {0, 0};
            while (RspCont_obj8.sample_index >= RspCont_obj8.outsample) {

                if (RspCont_obj8.in_cnt <= 0) {
                    return;
                }
                for (i = 0; i < 8 - 1; i++) {
                    RspCont_obj8.bufL[i] = RspCont_obj8.bufL[i + 1];
                }
                RspCont_obj8.bufL[8 - 1] = inData[len*RspCont_obj8.nch - RspCont_obj8.in_cnt];
                RspCont_obj8.in_cnt--;
                if (nch!=0) {
                    for (i = 0; i < 8 - 1; i++) {
                        RspCont_obj8.bufR[i] = RspCont_obj8.bufR[i + 1];
                    }
                    RspCont_obj8.bufR[8 - 1] = inData[len*RspCont_obj8.nch - RspCont_obj8.in_cnt];
                    RspCont_obj8.in_cnt--;
                }
                RspCont_obj8.sample_index -= RspCont_obj8.outsample;
            }
            RspCont_obj8.coff_index = (RspCont_obj8.sample_index*div_ou) >> 24;
            RspCont_obj8.phase = ((RspCont_obj8.sample_index*div_ou) >> 16) & 0xff;

            tmp_data[0] = 0;
            tmp_data[1] = 0;
            for (i = 0; i < 8; i++) {
                coff = (rsSample8Coff1[RspCont_obj8.coff_index*8 + i] * (0x100 - RspCont_obj8.phase) +
                        rsSample8Coff1[(RspCont_obj8.coff_index + 1)*8 + i] * RspCont_obj8.phase + (1 << 7)) >> 8;
                tmp_data[0] += RspCont_obj8.bufL[i] * coff;
                if (nch!=0) {
                    tmp_data[1] += RspCont_obj8.bufR[i] * coff;
                }
            }

            tmp_data[0] >>= 15;
            out_data = clip_8(tmp_data[0], -32768, 32767);
            RspCont_obj8.obuf[RspCont_obj8.obuf_cnt++] = (short)out_data;
            if (nch!=0) {
                tmp_data[1] >>= 15;
                out_data = clip_8(tmp_data[1], -32768, 32767);
                RspCont_obj8.obuf[RspCont_obj8.obuf_cnt++] = (short)out_data;
            }

            RspCont_obj8.sample_index += RspCont_obj8.insample;
        }
    }

    private static int clip_8(int a, int amin, int amax) {
        if (a < amin) {
            return amin;
        } else if (a > amax) {
            return amax;
        } else {
            return a;
        }
    }

    static class RSP8_CONTEXT {
        short[] bufL = new short[24+1];
        short[] bufR = new short[24+1];

        short insample;
        short outsample;

        short LpScl;

        int coff_index;
        int phase;
        int sample_index;

        short nch;

        short[] obuf = new short[32*6*2];
        short obuf_cnt;

        short in_cnt;
    }

    /**
     * 转换音频文件
     */
    public static byte[] convertWav(byte[] pcmFileBytes) {
        try {
            int size = pcmFileBytes.length;

            //填入参数，比特率等等。这里用的是16位单声道 8000 hz
            PCMCovWavUtil header = new PCMCovWavUtil();
            //长度字段 = 内容的大小（PCMSize) + 头部字段的大小(不包括前面4字节的标识符RIFF以及fileLength本身的4字节)
            header.fileLength = size + (44 - 8);
            header.FmtHdrLeth = 16;
            header.BitsPerSample = 16;
            header.Channels = 1;
            header.FormatTag = 0x0001;
            header.SamplesPerSec = 8000;
            header.BlockAlign = (short)(header.Channels * header.BitsPerSample / 8);
            header.AvgBytesPerSec = header.BlockAlign * header.SamplesPerSec;
            header.DataHdrLeth = size;

            byte[] h = header.getHeader();
            //计算长度
            byte[] wavFileBytes = new byte[h.length + size];
            for (int i=0; i<h.length; i++) {
                wavFileBytes[i] = h[i];
            }
            for (int i=0; i<wavFileBytes.length - h.length; i++) {
                wavFileBytes[h.length+i] = pcmFileBytes[i];
            }
            return wavFileBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] Hex2ByteDTX(int l) {
        int len = 2;
        byte[] b = new byte[len];
        for (int i=0; i<len; i++) {
            b[i] = (byte) (l >> (i * 8) & 0x00ff);
        }
        return b;
    }

    private static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i=0; i<count; i++) {
            dest[i] = (short) (src[i * 2 + 1] << 8 | src[2 * i] & 0xff);
        }
        return dest;
    }

}
