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
static StreamSwtQua streamSwtQua;
extern "C"

JNIEXPORT jdoubleArray JNICALL
Java_com_lepu_blepro_utils_AlgorithmUtil_filter(JNIEnv *env,jobject thiz, jdouble f, jboolean reset) {

    if (reset) {
        streamSwtQua.ResetMe();
        return (*env).NewDoubleArray(0);
    }
//
//
//    jfloat result = Smooth(Filter_Low16_ECG(f, 0));
//
//    return result;


    deque <double> outputPoints;
    streamSwtQua.GetEcgData(f, outputPoints);
    double *arrays = 0;
    if(outputPoints.empty())
    {
        arrays = (double *)malloc(sizeof(double)*7);
        memset(arrays,'\0',sizeof(arrays));
    } else
    {
        arrays = (double *)malloc(sizeof(double)*outputPoints.size());
        for(int i = 0; i < outputPoints.size(); i++ )
        {
            arrays[i] = outputPoints[i];
        }
    }

    long length = outputPoints.size();
//    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "length == %ld", length);

    jsize size = (jsize) outputPoints.size();
    jdoubleArray result = (*env).NewDoubleArray(size);
    (*env).SetDoubleArrayRegion(result, 0, size, arrays);

    return result;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    streamSwtQua = StreamSwtQua();
    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.

    return JNI_VERSION_1_6;
}

