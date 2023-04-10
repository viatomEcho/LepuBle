#include <jni.h>

//
// Created by chenyongfeng on 2023/4/7.
//
#include "sleep_alg_filter.h"
#include "sleep_alg_filter.c"
#include "sleep_alg_main_0_25Hz.h"
#include "sleep_alg_main_0_25Hz.c"

extern "C"
JNIEXPORT void JNICALL
Java_com_lepu_demo_util_DataConvert_sleep_1alg_1init_10_125Hz(JNIEnv *env, jclass clazz) {
    sleep_alg_init_0_25Hz();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lepu_demo_util_DataConvert_sleep_1alg_1main_1pro_10_125Hz(JNIEnv *env, jclass clazz, jshort pr, jint acc) {
    sleep_alg_input_t i;
    i.pr = pr;
    i.acc = acc;
    sleep_alg_input_t  *input = &i;
    int result = sleep_alg_main_pro_0_25Hz(input);
    return result;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_lepu_demo_util_DataConvert_sleep_1alg_1get_1res_10_125Hz(JNIEnv *env, jclass clazz) {
    int *arrays = 0;
    arrays = (int *)malloc(sizeof(int)*5);
    memset(arrays,'\0',sizeof(arrays));
    sleep_alg_result *result = sleep_alg_get_res_0_25Hz();
    arrays[0] = result->sleep_time;
    arrays[1] = result->deep_time;
    arrays[2] = result->light_time;
    arrays[3] = result->rem_time;
    arrays[4] = result->awake_time;
    jintArray data = (*env).NewIntArray(5);
    (*env).SetIntArrayRegion(data, 0, 5, arrays);
    free(arrays);
    return data;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }
    // Get jclass with env->FindClass.
    // Register methods with env->RegisterNatives.

    return JNI_VERSION_1_6;
}