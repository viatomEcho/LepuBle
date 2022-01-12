package com.lepu.blepro.ble.data;

import com.icomon.icbodyfatalgorithms.ICBodyFatAlgorithmsType;

/**
 * @author chenyongfeng
 */
public class FscaleWeightData implements Cloneable {
    public boolean isStabilized;
    public int weight_g;
    public double weight_kg;
    public double weight_lb;
    public int weight_st;
    public double weight_st_lb;
    public boolean isSupportHR;
    public int hr;
    public long time;
    public double bmi;
    public double bodyFatPercent;
    public double subcutaneousFatPercent;
    public double visceralFat;
    public double musclePercent;
    public int bmr;
    public double boneMass;
    public double moisturePercent;
    public double physicalAge;
    public double proteinPercent;
    public double smPercent;
    public int electrode = 4;
    public double bodyScore;
    public int bodyType;
    public double targetWeight;
    public int state;
    public double imp;
    public double imp2;
    public double imp3;
    public double imp4;
    public double imp5;
    public ICBodyFatAlgorithmsType bfa_type;

    public FscaleWeightData() {
    }

    public boolean isStabilized() {
        return this.isStabilized;
    }

    public void setStabilized(boolean stabilized) {
        this.isStabilized = stabilized;
    }

    public int getWeight_g() {
        return this.weight_g;
    }

    public void setWeight_g(int weight_g) {
        this.weight_g = weight_g;
    }

    public double getWeight_kg() {
        return this.weight_kg;
    }

    public void setWeight_kg(double weight_kg) {
        this.weight_kg = weight_kg;
    }

    public double getWeight_lb() {
        return this.weight_lb;
    }

    public void setWeight_lb(double weight_lb) {
        this.weight_lb = weight_lb;
    }

    public int getWeight_st() {
        return this.weight_st;
    }

    public void setWeight_st(int weight_st) {
        this.weight_st = weight_st;
    }

    public double getWeight_st_lb() {
        return this.weight_st_lb;
    }

    public void setWeight_st_lb(double weight_st_lb) {
        this.weight_st_lb = weight_st_lb;
    }

    public boolean isSupportHR() {
        return this.isSupportHR;
    }

    public void setSupportHR(boolean supportHR) {
        this.isSupportHR = supportHR;
    }

    public int getHr() {
        return this.hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getBmi() {
        return this.bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getBodyFatPercent() {
        return this.bodyFatPercent;
    }

    public void setBodyFatPercent(double bodyFatPercent) {
        this.bodyFatPercent = bodyFatPercent;
    }

    public double getSubcutaneousFatPercent() {
        return this.subcutaneousFatPercent;
    }

    public void setSubcutaneousFatPercent(double subcutaneousFatPercent) {
        this.subcutaneousFatPercent = subcutaneousFatPercent;
    }

    public double getVisceralFat() {
        return this.visceralFat;
    }

    public void setVisceralFat(double visceralFat) {
        this.visceralFat = visceralFat;
    }

    public double getMusclePercent() {
        return this.musclePercent;
    }

    public void setMusclePercent(double musclePercent) {
        this.musclePercent = musclePercent;
    }

    public int getBmr() {
        return this.bmr;
    }

    public void setBmr(int bmr) {
        this.bmr = bmr;
    }

    public double getBoneMass() {
        return this.boneMass;
    }

    public void setBoneMass(double boneMass) {
        this.boneMass = boneMass;
    }

    public double getMoisturePercent() {
        return this.moisturePercent;
    }

    public void setMoisturePercent(double moisturePercent) {
        this.moisturePercent = moisturePercent;
    }

    public double getPhysicalAge() {
        return this.physicalAge;
    }

    public void setPhysicalAge(double physicalAge) {
        this.physicalAge = physicalAge;
    }

    public double getProteinPercent() {
        return this.proteinPercent;
    }

    public void setProteinPercent(double proteinPercent) {
        this.proteinPercent = proteinPercent;
    }

    public double getSmPercent() {
        return this.smPercent;
    }

    public void setSmPercent(double smPercent) {
        this.smPercent = smPercent;
    }

    public int getElectrode() {
        return this.electrode;
    }

    public void setElectrode(int electrode) {
        this.electrode = electrode;
    }

    public double getImp() {
        return this.imp;
    }

    public void setImp(double imp) {
        this.imp = imp;
    }

    public double getImp2() {
        return this.imp2;
    }

    public void setImp2(double imp2) {
        this.imp2 = imp2;
    }

    public double getImp3() {
        return this.imp3;
    }

    public void setImp3(double imp3) {
        this.imp3 = imp3;
    }

    public double getImp4() {
        return this.imp4;
    }

    public void setImp4(double imp4) {
        this.imp4 = imp4;
    }

    public double getImp5() {
        return this.imp5;
    }

    public void setImp5(double imp5) {
        this.imp5 = imp5;
    }

    public ICBodyFatAlgorithmsType getBfa_type() {
        return this.bfa_type;
    }

    public void setBfa_type(ICBodyFatAlgorithmsType bfa_type) {
        this.bfa_type = bfa_type;
    }

    public double getBodyScore() {
        return this.bodyScore;
    }

    public void setBodyScore(double bodyScore) {
        this.bodyScore = bodyScore;
    }

    public int getBodyType() {
        return this.bodyType;
    }

    public void setBodyType(int bodyType) {
        this.bodyType = bodyType;
    }

    public double getTargetWeight() {
        return this.targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public FscaleWeightData clone() {
        try {
            return (FscaleWeightData)super.clone();
        } catch (CloneNotSupportedException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "FscaleWeightData{" +
                "isStabilized=" + isStabilized +
                ", weight_g=" + weight_g +
                ", weight_kg=" + weight_kg +
                ", weight_lb=" + weight_lb +
                ", weight_st=" + weight_st +
                ", weight_st_lb=" + weight_st_lb +
                ", isSupportHR=" + isSupportHR +
                ", hr=" + hr +
                ", time=" + time +
                ", bmi=" + bmi +
                ", bodyFatPercent=" + bodyFatPercent +
                ", subcutaneousFatPercent=" + subcutaneousFatPercent +
                ", visceralFat=" + visceralFat +
                ", musclePercent=" + musclePercent +
                ", bmr=" + bmr +
                ", boneMass=" + boneMass +
                ", moisturePercent=" + moisturePercent +
                ", physicalAge=" + physicalAge +
                ", proteinPercent=" + proteinPercent +
                ", smPercent=" + smPercent +
                ", electrode=" + electrode +
                ", bodyScore=" + bodyScore +
                ", bodyType=" + bodyType +
                ", targetWeight=" + targetWeight +
                ", state=" + state +
                ", imp=" + imp +
                ", imp2=" + imp2 +
                ", imp3=" + imp3 +
                ", imp4=" + imp4 +
                ", imp5=" + imp5 +
                ", bfa_type=" + bfa_type +
                '}';
    }
}
