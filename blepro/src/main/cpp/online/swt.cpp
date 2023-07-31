//
// Created by zhenghuimin on 2019/4/28.
// this program is intended to denoise ecg signal
//

#include "swt.h"
#include <math.h>
#include <algorithm>
#include "commalgorithm.h"

using namespace std;

#define FILTER_INI_LEN 12

#define SWT_FLOOR_NUM 3
#define SWT_MAX_SAMPLE_CNT 256


static double swd[SWT_FLOOR_NUM][SWT_MAX_SAMPLE_CNT];
static double swa[SWT_FLOOR_NUM][SWT_MAX_SAMPLE_CNT];


static double Lo_R[] = {-0.000720549445364512,-0.00182320887070299,0.00561143481939450,0.0236801719463341,-0.0594344186464569,-0.0764885990783064,0.417005184421693,0.812723635445542,0.386110066821162,-0.0673725547219630,-0.0414649367817592,0.0163873364635221};
static double Hi_R[] = {-0.0163873364635221,-0.0414649367817592,0.0673725547219630,0.386110066821162,-0.812723635445542,0.417005184421693,0.0764885990783064,-0.0594344186464569,-0.0236801719463341,0.00561143481939450,0.00182320887070299,-0.000720549445364512};

static double Lo_RR[] = {0.0163873364635221,-0.0414649367817592,-0.0673725547219630,0.386110066821162,0.812723635445542,0.417005184421693,-0.0764885990783064,-0.0594344186464569,0.0236801719463341,0.00561143481939450,-0.00182320887070299,-0.000720549445364512};
static double Hi_RR[] = {-0.000720549445364512,0.00182320887070299,0.00561143481939450,-0.0236801719463341,-0.0594344186464569,0.0764885990783064,0.417005184421693,-0.812723635445542,0.386110066821162,0.0673725547219630,-0.0414649367817592,-0.0163873364635221};


double SWT::GetExtendedR(int index, int exponent2, double * tableR)
{
    int i;
    for (i = 0; i < FILTER_INI_LEN; i++)
    {
        if (index == i * exponent2)
        {
            return tableR[i];
        }
    }

    return 0;
}

int SWT::IntMatlabMod(int x, int y)
{
    if (y == 0)
    {
        return x;
    }

    if (x == y)
    {
        return 0;
    }

    return (int)((double)x - floor((double)x / (double)y) * (double)y);
}


int SWT::MapExtendedIndex(int index, int extendLen, int sampleCnt)
{
    int tempInt = 1;

    if (index < extendLen / 2)
    {
        tempInt = sampleCnt - extendLen / 2 + index + 1;
    }
    else if (index >= extendLen / 2 && index < extendLen / 2 + sampleCnt)
    {
        tempInt = index - extendLen / 2 + 1;
    }
    else if (index >= extendLen / 2 + sampleCnt)
    {
        tempInt = index - extendLen / 2 - sampleCnt + 1;
    }

    if (extendLen/2 > sampleCnt)
    {
        tempInt = IntMatlabMod(tempInt, sampleCnt);
        if (tempInt == 0)
        {
            tempInt = sampleCnt;
        }
    }

    return tempInt - 1;

}


void SWT::idwtLOC(double a[], double d[], int lon, int lo_Rl,int flag,double y[], int step_RR, int step_RRj)
{
    int j;
    int k;
    double tp1;
    double tp2;
    int index;
//    double tmpSwap;

    for (j = lo_Rl-1; j < lon+lo_Rl-1; j++)
    {
        tp1 = 0;
        tp2 = 0;
        for (k = 0; k < lon+lo_Rl; k++)
        {
            if ((j - k) >= 0 && (j - k) < lo_Rl)
            {
                index = MapExtendedIndex(k, lo_Rl, lon);
                if (index % 2 == 0)
                {
                    if (flag == -1)
                    {
                        tp1 += a[step_RRj + step_RR*(1+index)] * Lo_RR[j - k];
                        tp2 += d[step_RRj + step_RR*(1+index)] * Hi_RR[j - k];
                    }
                    else
                    {
                        tp1 += a[step_RRj + step_RR*index] * Lo_RR[j - k];
                        tp2 += d[step_RRj + step_RR*index] * Hi_RR[j - k];
                    }
                }


            }
        }

        if (flag == -1)
        {
            if (j - lo_Rl+1 == lon-1)
            {
                y[0] = tp1 + tp2;
            }
            else
            {
                y[j - lo_Rl+1+1] = tp1 + tp2;
            }
        }
        else
        {
            y[j - lo_Rl+1] = tp1 + tp2;
        }
    }


}


int SWT::MyPow(int di, int zhi)
{
    int i;
    int res = 1;

    for (i = 0; i < zhi; i++)
    {
        res *= di;
    }

    return res;
}

int SWT::GetSenarios(double x1, double x2, double x3)
{
    if (1 == DoubleIsTooNear(x1, x2))
    {
        return 1;
    }

    if (1 == DoubleIsTooNear(x2, x3))
    {
        return 2;
    }

    return 3;

}

void SWT::SamplesToSwdSwa(double * samples, int sampleCnt)
{
    int i = 0;
    int j = 0;
    int k = 0;
    double tp1;
    double tp2;
    int exponent2;
    int exponent2Multi8;

    //the process of swt
    for (i = 0; i < SWT_FLOOR_NUM; i++)
    {
//        exponent2 = (int) pow(2, i);
        exponent2 = MyPow(2, i);
        exponent2Multi8 = FILTER_INI_LEN * exponent2;

        for (j = exponent2Multi8; j < sampleCnt + exponent2Multi8; j++)
        {
            tp1 = 0;
            tp2 = 0;
            for (k = 0; k < sampleCnt + exponent2Multi8; k++)
            {
                if ((j - k) >= 0 && (j - k) < exponent2Multi8)
                {
                    tp1 += samples[MapExtendedIndex(k, exponent2Multi8, sampleCnt)] * GetExtendedR(j - k, exponent2, Hi_R);
                    tp2 += samples[MapExtendedIndex(k, exponent2Multi8, sampleCnt)] * GetExtendedR(j - k, exponent2, Lo_R);
                }
            }
            swd[i][j - exponent2Multi8] = tp1;
            swa[i][j - exponent2Multi8] = tp2;
        }

        for (j = 0; j < sampleCnt; j++)
        {
            samples[j] = swa[i][j];
        }

    }
}


void SWT::CutSwd(int sampleCnt)
{

//    double xPercent[SWT_FLOOR_NUM];
//    double wPercent[SWT_FLOOR_NUM];
//    double curveBend[SWT_FLOOR_NUM];
//
//    xPercent[2] = 4;
//    xPercent[3] = 3.5;
//    xPercent[4] = 0.0001;
//
//    wPercent[2] = 0.00005;
//    wPercent[3] = 0.05;
//    wPercent[4] = 0.7;
//
//    curveBend[2] = 0.01;
//    curveBend[3] = 0.95;
//    curveBend[4] = 0.9;
//
    double lamda;
//    double maxX;
//    double retainPoint;
//
//    double x1,y1;
//    double x2,y2;
//    double x3,y3;

    double absX;
//    double denoisePercent;


    int i = 0;
    int j = 0;
    vector<double> arrayForOrder;

//    int senarios;

    arrayForOrder.resize(sampleCnt);


    for (i = 0; i < SWT_FLOOR_NUM; i++)
    {
        for (j = 0; j < sampleCnt; j++)
        {
            absX = fabs(swd[i][j]);
            arrayForOrder[j] = absX;
        }

        sort(arrayForOrder.begin(), arrayForOrder.end());

        if(sampleCnt % 2 == 0)
        {
            lamda = (arrayForOrder[sampleCnt / 2-1] + arrayForOrder[sampleCnt / 2]) / 2;
        }
        else
        {
            lamda = arrayForOrder[sampleCnt / 2];
        }

//        maxX = arrayForOrder[sampleCnt-1];

        lamda = lamda / 0.6745;
        lamda = lamda * sqrt(2 * log(sampleCnt));
        lamda = lamda / log(i + 2);


//        retainPoint = lamda + (maxX - lamda) * xPercent[i];
//
//        x1 = lamda;
//        y1 = wPercent[i];
//
//        x2 = (curveBend[i] - 1) * (retainPoint - lamda) / (wPercent[i] - 1) + lamda;
//        y2 = curveBend[i];
//
//        x3 = retainPoint;
//        y3 = 1;
//
//        senarios = GetSenarios(x1, x2, x3);

        for (j = 0; j < sampleCnt; j++)
        {
            absX = fabs(swd[i][j]);
            if (-1 != DoubleCompares(absX, lamda) )
            {
                continue;
            }
            else
            {
                swd[i][j] = 0;

            }
//            denoisePercent = 0;

//            if (senarios == 1)
//            {
//                if (-1 != DoubleCompares(absX, 0) && -1 == DoubleCompares(absX, x1))
//                {
//                    denoisePercent = wPercent[i];
//                }
//                else if (-1 != DoubleCompares(absX, x1))
//                {
//                    denoisePercent = 1;
//                }
//
//                swd[i][j] *= denoisePercent;
//                continue;
//            }

//            if (senarios == 2)
//            {
//                if (-1 != DoubleCompares(absX, 0) && -1 == DoubleCompares(absX, x2))
//                {
//                    denoisePercent = wPercent[i];
//                }
//                else if (-1 != DoubleCompares(absX, x2))
//                {
//                    denoisePercent = 1;
//                }
//
//                swd[i][j] *= denoisePercent;
//                continue;
//            }

//            if (senarios == 3)
//            {
//                if (-1 != DoubleCompares(absX, 0) && -1 == DoubleCompares(absX, x1))
//                {
//                    denoisePercent = wPercent[i];
//                }
//                else if (-1 != DoubleCompares(absX, x1) && -1 == DoubleCompares(absX, x2))
//                {
//                    denoisePercent = (y2 - y1) * (absX - x1) / (x2 - x1) + y1;
//                }
//                else if (-1 != DoubleCompares(absX, x2) && -1 == DoubleCompares(absX, x3))
//                {
//                    denoisePercent = (y3 - y2) * (absX - x2) / (x3 - x2) + y2;
//                }
//                else if (-1 != DoubleCompares(absX, x3))
//                {
//                    denoisePercent = 1;
//                }
//
//                swd[i][j] *= denoisePercent;
//                continue;
//            }


        }

    }


//    for (j = 0; j < sampleCnt; j++)
//    {
//        swd[0][j] = 0;
//    }
//
//    for (j = 0; j < sampleCnt; j++)
//    {
//        swd[1][j] = 0;
//    }



    return;
}

void SWT::SwdSwaToSamples(double * samples, int sampleCnt)
{
    int i = 0;
    int j = 0;
    int k = 0;
    int step_RR;


    //the process of iswt

    for (i = 0; i < sampleCnt; i++)
        samples[i] = swa[SWT_FLOOR_NUM - 1][i];

    for (i = SWT_FLOOR_NUM - 1; i >= 0; i--)
    {
#if 0
        sprintf(input, "f:\\123\\filt_%d.data", i);
        FILE *fpfilt123 = fopen(input, "wb+");

        for (int xxx = 0; xxx < sampleCnt; xxx++)
        {
            sprintf(input, "%lf\r\n", samples[xxx]);
            fwrite(input, strlen(input), 0x01, fpfilt123);
        }

        fclose(fpfilt123);

#endif

//        step_RR = (int)pow(2, i);
        step_RR = MyPow(2, i);
        for (j = 0; j < step_RR; j++)
        {
            int lon_RR = sampleCnt / step_RR;

            idwtLOC(samples, swd[i], lon_RR, FILTER_INI_LEN,1, swa[0], step_RR, j);

            idwtLOC(samples, swd[i], lon_RR, FILTER_INI_LEN,-1, swa[1], step_RR, j);

            for (k = 0; k < lon_RR; k++)
            {
                samples[j + step_RR * k] = 0.5*(swa[0][k] + swa[1][k]);
            }
        }
    }


}



void SWT::EcgSwt(double * samples, int sampleCnt)
{

    if (sampleCnt > SWT_MAX_SAMPLE_CNT)
    {
        return;
    }

    SamplesToSwdSwa(samples, sampleCnt);


    CutSwd(sampleCnt);


    SwdSwaToSamples(samples, sampleCnt);

//    vector<double> pri;
//
//    vector<double> pri_bef;
//
//    for (int i = 0; i < sampleCnt; i++)
//    {
//        if (samples[i] == 2361)
//        {
//            int x =1;
//        }
//        pri_bef.push_back(samples[i]);
//        samples[i] *= 6.9905;
//        pri.push_back(samples[i]);
//    }

    return;
}


void SWT::AnalyzeSwt(deque <double> & samples)
{
    int i;

    if (samples.size() != SWT_MAX_SAMPLE_CNT)
    {
        return;
    }

    double * samplesPtr = new double[samples.size()];

    for (i = 0; i < samples.size(); ++i)
    {
        samplesPtr[i] = samples[i];
    }

    EcgSwt(samplesPtr, samples.size());

    for (i = 0; i < samples.size(); ++i)
    {
        samples[i] = samplesPtr[i];
    }

    delete [] samplesPtr;
}







