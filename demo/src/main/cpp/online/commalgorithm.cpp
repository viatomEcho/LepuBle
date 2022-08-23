//
// Created by zhenghuimin on 2019/4/29.
//
#include "commalgorithm.h"

#include <math.h>

#include <iostream>
#include <sstream>
//#include <mycross.h>

using namespace std;

//int GetSystemTime()
//{
//    struct timeval tp;
//    gettimeofday(&tp, NULL);
//    int ms = tp.tv_sec * 1000 + tp.tv_usec / 1000;
//
//    return ms;
//}

int DoubleCompares(double doubleNum1, double doubleNum2)
{
    if (fabs(doubleNum1 - doubleNum2) < 1e-9)
    {
        return 0;
    }

    if (doubleNum1 > doubleNum2)
    {
        return 1;
    }

    return -1;
}


double DoubleTripToZero(double doubleNum)
{
    if (fabs(doubleNum) < 1e-4)
    {
        return 0;
    }

    return doubleNum;
}

double StringToDouble(string str)
{
    if (str == "")
    {
        return 0;
    }
    istringstream iss(str);
    double num;
    iss >> num;
    return num;
}


int DoubleIsTooNear(double doubleNum1, double doubleNum2)
{
    if (fabs(doubleNum1 - doubleNum2) < 1e-8)
    {
        return 1;
    }

    return 0;
}


void DataStreamStatistic::ResetMe()
{
    mean = 0;
    squareMean = 0;
    sampleCnt = 0;
}

double DataStreamStatistic::DssMeanAdd91PercentStd(double data)
{
    double sd;
    double std;

    if (sampleCnt == 0)
    {
        mean = data;
        squareMean = data * data;
        sd = 0;
    }
    else
    {
        mean /= (sampleCnt + 1);
        mean *= sampleCnt;
        mean += (data / (sampleCnt + 1));

        squareMean /= (sampleCnt + 1);
        squareMean *= sampleCnt;
        squareMean += (data * data / (sampleCnt + 1));

        sd = squareMean - (mean * mean);
        sd /= sampleCnt;
        sd *= (sampleCnt + 1);
    }

    sampleCnt++;

    std = sqrt(sd);

//    double meanPercent = 31.3507999072571;
//    double stdPercent = -7.76025968003710;//(sqrt(sd) - mean - 538.9) * 0.37 / 870.7 + 0.55;
//    double cPercent = -1222.83978669140;

    double meanPercent = 1.1;
    double stdPercent = 0.8;//(sqrt(sd) - mean - 538.9) * 0.37 / 870.7 + 0.55;
    double cPercent = 0;

    if (0 != DoubleCompares(mean, 0) && 0 != DoubleCompares(std, 0))
    {
        meanPercent *= fabs(std / mean);
        stdPercent *= fabs(std / mean);
    }

//    if (-1 != DoubleCompares(percent, 1))
//    {
//        percent = 0.93;
//    }
//    else if (1 != DoubleCompares(percent, 0))
//    {
//        percent = 0;
//    }

    double res = mean * meanPercent + std * stdPercent + cPercent;

//    QPrint("DssMeanAdd91PercentStd:mean:%lf, standard:%lf, res:%lf\n", mean, sqrt(sd), res);

    return res;
}

double DataStreamStatistic::DssMeanDec25PercentStd(double data)
{
    double sd;
    double std;

    if (sampleCnt == 0)
    {
        mean = data;
        squareMean = data * data;
        sd = 0;
    }
    else
    {
        mean /= (sampleCnt + 1);
        mean *= sampleCnt;
        mean += (data / (sampleCnt + 1));

        squareMean /= (sampleCnt + 1);
        squareMean *= sampleCnt;
        squareMean += (data * data / (sampleCnt + 1));

        sd = squareMean - (mean * mean);
        sd /= sampleCnt;
        sd *= (sampleCnt + 1);
    }

    sampleCnt++;

    std = sqrt(sd);

//    double meanPercent = -11.9392343060465;
//    double stdPercent = -5.09932764954599;//(sqrt(sd) + mean - 717.14) * 0.33 / 265.9 + 0.4;
//    double cPercent = -1342.40789260877;

    double meanPercent = 0.7;
    double stdPercent = -0.4;//(sqrt(sd) + mean - 717.14) * 0.33 / 265.9 + 0.4;
    double cPercent = 0;

    if (0 != DoubleCompares(mean, 0) && 0 != DoubleCompares(std, 0))
    {
        meanPercent *= fabs(std / mean);
        stdPercent *= fabs(std / mean);
    }

//    if (-1 != DoubleCompares(percent, 1))
//    {
//        percent = 0.80;
//    }
//    else if (1 != DoubleCompares(percent, 0))
//    {
//        percent = 0;
//    }

    double res = mean * meanPercent + std * stdPercent + cPercent;

//    QPrint("DssMeanDec25PercentStd:mean:%lf, standard:%lf, stdPercent:%lf,res:%lf\n", mean, std,stdPercent, res);

    return res;
}

double DataStreamStatistic::DssDataDivideMean(double data)
{
    mean /= (sampleCnt + 1);
    mean *= sampleCnt;
    mean += (data / (sampleCnt + 1));

    sampleCnt++;

    if (mean == 0)
    {
        return 0;
    }
    else
    {
        return (data / mean);
    }

}


double DataStreamStatistic::DssMean(double data)
{

    mean /= (sampleCnt + 1);
    mean *= sampleCnt;
    mean += (data / (sampleCnt + 1));

    sampleCnt++;

    return mean;
}


double DataStreamStatistic::DssStd(double data)
{
    double sd;
    double res;

    if (sampleCnt == 0)
    {
        mean = data;
        squareMean = data * data;
        sd = 0;
    }
    else
    {
        mean /= (sampleCnt + 1);
        mean *= sampleCnt;
        mean += (data / (sampleCnt + 1));

        squareMean /= (sampleCnt + 1);
        squareMean *= sampleCnt;
        squareMean += (data * data / (sampleCnt + 1));

        sd = squareMean - (mean * mean);
        sd /= sampleCnt;
        sd *= (sampleCnt + 1);
    }

    sampleCnt++;

    if (0 == DoubleCompares(sd, 0))
    {
        sd = 0;
    }

    res = (sqrt(DoubleTripToZero(sd)));

    return res;

}


DataStreamStatistic::DataStreamStatistic()
{
    ResetMe();
}