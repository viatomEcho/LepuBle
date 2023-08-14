//
// Created by zhenghuimin on 2019/5/7.
//

#ifndef DENOISE_STREAMSWTQUA_H
#define DENOISE_STREAMSWTQUA_H

#include <string>
#include <deque>
#include "swt.h"


using namespace std;

class StreamSwtQua
{
public:

    void GetEcgData(double sample, deque <double> & outputPoints);
private:
    SWT swtCollection;

private:
#define SWT_MAX_SAMPLE_CNT_ONCE 256
    int bufLen;
    double buf[SWT_MAX_SAMPLE_CNT_ONCE];

    double bufMid[SWT_MAX_SAMPLE_CNT_ONCE/2];
    int isFirstBuf;
private:
    void AnalyzeOurBuf();
public:
    void ResetMe();
public:
    StreamSwtQua();
    ~StreamSwtQua();

};



#endif //DENOISE_STREAMSWTQUA_H
