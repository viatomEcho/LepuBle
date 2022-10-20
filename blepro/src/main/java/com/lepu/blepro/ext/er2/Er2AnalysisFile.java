package com.lepu.blepro.ext.er2;

import java.util.ArrayList;

public class Er2AnalysisFile {

    private int fileVersion;     // 文件版本 e.g.  0x01 :  V1
    private int recordingTime;   // 记录时长 e.g. 3600 :  3600s
    private ArrayList<AnalysisResult> resultList = new ArrayList<>();  // 每1分钟分析结果

    public Er2AnalysisFile(byte[] bytes) {
        com.lepu.blepro.ble.data.Er2AnalysisFile data = new com.lepu.blepro.ble.data.Er2AnalysisFile(bytes);
        fileVersion = data.getFileVersion();
        recordingTime = data.getRecordingTime();
        int len = data.getResultList().size();
        for (int i=0; i<len; i++) {
            AnalysisResult result = new AnalysisResult();
            result.result = data.getResultList().get(i).getResult();
            result.hr = data.getResultList().get(i).getHr();
            result.qrs = data.getResultList().get(i).getQrs();
            result.pvcs = data.getResultList().get(i).getPvcs();
            result.qtc = data.getResultList().get(i).getQtc();
            result.st = data.getResultList().get(i).getSt();
            resultList.add(result);
        }
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(int recordingTime) {
        this.recordingTime = recordingTime;
    }

    public ArrayList<AnalysisResult> getResultList() {
        return resultList;
    }

    public void setResultList(ArrayList<AnalysisResult> resultList) {
        this.resultList = resultList;
    }

    public class AnalysisResult {
        private int result;
        private Er2EcgDiagnosis diagnosis;  // 诊断结果
        private int hr;                     // 心率 单位：bpm
        private int qrs;                    // QRS 单位：ms
        private int pvcs;                   // PVC个数
        private int qtc;                    // QTc 单位：ms
        private short st;                   // ST（以ST*100存储），单位为mV

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public Er2EcgDiagnosis getDiagnosis() {
            if (diagnosis == null) {
                diagnosis = new Er2EcgDiagnosis(result);
            }
            return diagnosis;
        }

        public void setDiagnosis(Er2EcgDiagnosis diagnosis) {
            this.diagnosis = diagnosis;
        }

        public int getHr() {
            return hr;
        }

        public void setHr(int hr) {
            this.hr = hr;
        }

        public int getQrs() {
            return qrs;
        }

        public void setQrs(int qrs) {
            this.qrs = qrs;
        }

        public int getPvcs() {
            return pvcs;
        }

        public void setPvcs(int pvcs) {
            this.pvcs = pvcs;
        }

        public int getQtc() {
            return qtc;
        }

        public void setQtc(int qtc) {
            this.qtc = qtc;
        }

        public short getSt() {
            return st;
        }

        public void setSt(short st) {
            this.st = st;
        }
    }

}
