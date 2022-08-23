//
// Created by zhenghuimin on 2019/4/29.
//

#ifndef DENOISE_COMMALGORITHM_H
#define DENOISE_COMMALGORITHM_H

#include <vector>
#include <algorithm>
#include <string>
#include <deque>


using namespace std;
//#define PI 3.1515926

//int GetSystemTime();

/* 0:doubleNum1 == doubleNum2 */
/* 1:doubleNum1 > doubleNum2 */
/* -1:doubleNum1 < doubleNum2 */
int DoubleCompares(double doubleNum1, double doubleNum2);

//int FloatCompares(float floatNum1, float floatNum2);

double DoubleTripToZero(double doubleNum);

double StringToDouble(string str);

//float StringToFloat(string str);
//
//int StringToInt(string str);

void DoubleToString(double doubleNum, string & str);

//void StringDoubleToIntString(string & str);
//
//void Deque2String(deque<double> & dequeInstance, string & strigInstance);
//
//void String2Deque(const string & strigInstance, deque<double> & dequeInstance);

int DoubleIsTooNear(double doubleNum1, double doubleNum2);

//int AnnIsNoise(int ann);
//
//int GetOneNoiseAnn();
//
//int PosIsInLostPosRange(deque<int> & lostPos, int pos);
//
//double Weightedmean(deque<int> & x, deque<double> & y);
//
//void FabsThresholdPulse(deque<double> & y, double threshold, deque<int>& mp);
//
//void MultiplySignalsDoubleIntDouble(deque<double> & x, deque<int> & y, deque<double> & z);
//
//double MeanofDoubledeque(deque<int> & x, deque<int> & y);
////double MeanofINtdeque(deque<int>&x);
//
//void MultiplySignalsint3(deque<int> & x, deque<int> & y, deque<int> & z);
//
//void GetvalidSignal(deque<double> & x, deque<int> & structelemt, deque<double> & output);
//void JiaoyanSignal(deque<double >&x,deque<int>&loc1,deque<int>&loc2);


class DataStreamStatistic
{
public:
    void ResetMe();

    double DssMeanAdd91PercentStd(double data);

    double DssMeanDec25PercentStd(double data);

    double DssDataDivideMean(double data);

    double DssMean(double data);

    double DssStd(double data);

private:
    double mean;
    double squareMean;
    double sampleCnt;
public:
    DataStreamStatistic();
};




#endif //DENOISE_COMMALGORITHM_H
