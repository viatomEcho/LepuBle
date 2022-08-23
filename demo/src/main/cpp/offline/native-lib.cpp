//
// Created by gongguopei on 2019/5/31.
//

//
// Created by wangjiang on 2019/4/8.
//

#include <jni.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <stdlib.h>
#include <fcntl.h>

#include <string>
#include <deque>
#include <cstdlib>
#include <string.h>
// #include "filter_16_v2.cpp"
#include "streamswtqua.h"
#include "streamswtqua.cpp"
#include "commalgorithm.h"
#include "commalgorithm.cpp"
#include "swt.h"
#include "swt.cpp"
#include <android/log.h>
#include <assert.h>

#define APPNAME "MyApp"
extern "C"

JNIEXPORT jshortArray JNICALL
Java_com_lepu_demo_util_DataConvert_shortfilter(JNIEnv *env, jobject thiz, jshortArray inShorts) {

    short *shortArray;
    jsize arraySize;
    arraySize = (*env).GetArrayLength(inShorts);

    deque <double > inputt;
	deque <double > realInput;		// Panjie: 实际进行分析的数据

    inputt.clear();

    jboolean *isCopy = (jboolean *)malloc(sizeof(jboolean));
    shortArray =(*env).GetShortArrayElements(inShorts, isCopy);
    for(int j = 0; j < arraySize; j++ )
    {
        inputt.push_back((jdouble) shortArray[j]);
    }
    int inputLength = (int) inputt.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "doubleArraySize == %d", inputLength);

	int i = 0;
	int j = 0;
	int flag = 0;

    StreamSwtQua streamSwtQua;
    deque <double> outputPoints;
    deque <double> allSig;
    deque <double> outputsize;

	int lenthOfData		= 0;
	int ReduntLength	= 0;
	int MultipleSize		= 0;
	int padDataLen		= 0;		// Panjie: 需要补充的数据点数

	lenthOfData = inputt.size();
	MultipleSize = lenthOfData / 256;
	ReduntLength = lenthOfData - 256 * MultipleSize;

	padDataLen = (MultipleSize + 1) * 256 - lenthOfData;

	for (j = 0; j < lenthOfData; ++j)
	{
		realInput.push_back(inputt[j]);
	}

	// 数据补零
	if (0 != ReduntLength)
	{
		if (padDataLen < 64)
		{
			flag = 1;

			for (j = lenthOfData - 1; j >= lenthOfData - padDataLen; j--)
			{
				realInput.push_back(inputt[j]);
			}

			for (j = 256 * (MultipleSize + 1) - 128; j < 256 * (MultipleSize + 1); j++)
			{
				realInput.push_back(realInput[j]);
			}
		}
		else
		{
			for (j = lenthOfData - 1; j >= lenthOfData - padDataLen; j--)
			{
				realInput.push_back(inputt[j]);
			}
		}
	}

	if (0 == ReduntLength)
	{
		for (i = 0; i < 256 * MultipleSize; ++i)
		{
			streamSwtQua.GetEcgData(realInput[i], outputPoints);

			for (j = 0; j < outputPoints.size(); ++j)
			{
				allSig.push_back(outputPoints[j]);
			}
		}

		for (i = 256 * MultipleSize - 128; i < 256 * MultipleSize; ++i)
		{
			streamSwtQua.GetEcgData(inputt[i], outputPoints);
		}

		for (j = 0; j < 64; j++)
		{
			allSig.push_back(outputPoints[j]);
		}
	}
	else
	{
		for (i = 0; i < realInput.size(); i++)
		{
			streamSwtQua.GetEcgData(realInput[i], outputPoints);

			for (j = 0; j < outputPoints.size(); ++j)
			{
				allSig.push_back(outputPoints[j]);
			}
		}

		if (ReduntLength < 192)
		{
			for (i = 0; i < 192 - ReduntLength; i++)
			{
				allSig.pop_back();
			}
		}

		if (1 == flag)
		{
			for (i = 0; i < 64 + padDataLen; i++)
			{
				allSig.pop_back();
			}
		}
	}


    long length = allSig.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "length == %ld", length);
    short array[length];
    for(i = 0; i < length; i++)
    {
        array[i] = (short) allSig[i];
    }

    jsize size = (jsize) allSig.size();
    jshortArray result = (*env).NewShortArray(size);
    (*env).SetShortArrayRegion(result, 0, size, array);

    return result;
}

extern "C" JNIEXPORT jdoubleArray JNICALL
Java_com_lepu_filterdemo_MainActivity_offlineFilter(JNIEnv *env, jobject thiz, jdoubleArray doubles) {

    double *doubleArray;
    jsize doubleArraySize;
    doubleArraySize = (*env).GetArrayLength(doubles);

    deque <double > inputt;
    inputt.clear();

    jboolean *isCopy = (jboolean *)malloc(sizeof(jboolean));
    doubleArray =(*env).GetDoubleArrayElements(doubles, isCopy);
    for(int j = 0; j < doubleArraySize; j++ )
    {
        inputt.push_back(doubleArray[j]);
    }
    int inputLength = (int) inputt.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "doubleArraySize == %d", inputLength);

    int i;
    StreamSwtQua streamSwtQua;
    deque <double> outputPoints;
    deque <double> allSig;
    deque <double> outputsize;
    int ReduntLength;
    int MultipleSize;
    MultipleSize = inputLength/256;
    ReduntLength = inputLength - 256 * MultipleSize;

    if(ReduntLength != 0)
    {
        for(i = inputLength; i < (MultipleSize+1)*256; i++)
        {
            inputt.push_back(0);
        }
    }



    if(ReduntLength == 0)
    {
        for (i = 0; i < 256 * MultipleSize; ++i)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }

    }
    else{
        for(i = 0; i < inputt.size(); i++)
        {
            streamSwtQua.GetEcgData(inputt[i], outputPoints);

            for (int j = 0; j < outputPoints.size(); ++j)
            {
                allSig.push_back(outputPoints[j]);
            }
        }
        if(ReduntLength < 192)
        {
            for(i = 0; i < 192 -ReduntLength; i++)
            {
                allSig.pop_back();
            }
        }
    }

    long length = allSig.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "length == %ld", length);
    double array[length];
    for(i = 0; i < length; i++)
    {
        array[i] = allSig[i];
    }

    jsize size = (jsize) allSig.size();
    jdoubleArray result = (*env).NewDoubleArray(size);
    (*env).SetDoubleArrayRegion(result, 0, size, array);

    return result;
}