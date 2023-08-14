//
// Created by zhenghuimin on 2019/4/28.
//

#ifndef DENOISE_SWT_H
#define DENOISE_SWT_H

#include <deque>
using namespace std;

class SWT
{
public:

    void AnalyzeSwt(deque <double> & samples);

private:
    double GetExtendedR(int index, int exponent2, double * tableR);

    int IntMatlabMod(int x, int y);

    int MapExtendedIndex(int index, int extendLen, int sampleCnt);

    void idwtLOC(double a[], double d[], int lon, int lo_Rl, int flag, double y[], int step_RR, int step_RRj);

    int MyPow(int di, int zhi);

    int GetSenarios(double x1, double x2, double x3);

    void SamplesToSwdSwa(double * samples, int sampleCnt);

    void CutSwd(int sampleCnt);

    void SwdSwaToSamples(double * samples, int sampleCnt);

    void EcgSwt(double * samples, int sampleCnt);


};

#endif //DENOISE_SWT_H
