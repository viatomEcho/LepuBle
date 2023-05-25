package com.lepu.demo.data;

public class AnalysisFile {
    private String fileName;
    private boolean motion;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isMotion() {
        return motion;
    }

    public void setMotion(boolean motion) {
        this.motion = motion;
    }

    @Override
    public String toString() {
        return "AnalysisFile{" +
                "fileName='" + fileName + '\'' +
                ", motion=" + motion +
                '}';
    }
}
