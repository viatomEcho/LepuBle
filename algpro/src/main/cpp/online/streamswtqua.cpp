//
// Created by zhenghuimin on 2019/5/7.
//

#include "commalgorithm.h"
#include "streamswtqua.h"


//void StreamSwtQua::SetQuaMatrix(const deque <deque <double>> & leftMatrix)
//{
//    quadraticVariation.SetLinearTransMatrixOnTheLeft(leftMatrix);
//}

void StreamSwtQua::GetEcgData(double sample, deque <double> & outputPoints)
{
    int i;

    outputPoints.clear();

    buf[bufLen] = sample;

    if (bufLen >= SWT_MAX_SAMPLE_CNT_ONCE / 2)
    {
        bufMid[bufLen - (SWT_MAX_SAMPLE_CNT_ONCE / 2)] = sample;
    }

    bufLen++;

    if (bufLen == SWT_MAX_SAMPLE_CNT_ONCE)
    {

        AnalyzeOurBuf();

        if (isFirstBuf == 1)
        {
            isFirstBuf = 0;

            for (i = 0; i < SWT_MAX_SAMPLE_CNT_ONCE - SWT_MAX_SAMPLE_CNT_ONCE / 4; i++)
            {
                outputPoints.push_back(buf[i]);
            }

//            EcgSwtDataOutput(SWT_MAX_SAMPLE_CNT_ONCE - SWT_MAX_SAMPLE_CNT_ONCE/4, buf);
        }
        else
        {
            for (i = 0; i < SWT_MAX_SAMPLE_CNT_ONCE - SWT_MAX_SAMPLE_CNT_ONCE / 2; i++)
            {
                outputPoints.push_back(buf[i + SWT_MAX_SAMPLE_CNT_ONCE / 4]);
            }

//            EcgSwtDataOutput(SWT_MAX_SAMPLE_CNT_ONCE - SWT_MAX_SAMPLE_CNT_ONCE/2, buf + SWT_MAX_SAMPLE_CNT_ONCE/4);
        }

        for (i = 0; i < SWT_MAX_SAMPLE_CNT_ONCE / 2; i++)
        {
            buf[i] = bufMid[i];
        }

        bufLen = SWT_MAX_SAMPLE_CNT_ONCE / 2;
    }

    return;
}

void StreamSwtQua::AnalyzeOurBuf()
{
    int i;
    deque <double> samples;

    samples.resize(SWT_MAX_SAMPLE_CNT_ONCE);

    for (i = 0; i < SWT_MAX_SAMPLE_CNT_ONCE; i++)
    {
        samples[i] = buf[i];
    }

    swtCollection.AnalyzeSwt(samples);
//    quadraticVariation.MatrixBaseLineWander(samples);

    for (i = 0; i < SWT_MAX_SAMPLE_CNT_ONCE; i++)
    {
        buf[i] = samples[i];
    }

}

void StreamSwtQua::ResetMe()
{
    bufLen = 0;
    isFirstBuf = 1;
}

StreamSwtQua::StreamSwtQua()
{
    ResetMe();
}

StreamSwtQua::~StreamSwtQua()
{
    ResetMe();
}
