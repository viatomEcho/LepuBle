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
Java_com_lepu_blepro_utils_AlgorithmUtil_sleep_1alg_1init_10_125Hz(JNIEnv *env, jclass clazz, jint timestamp) {
    sleep_alg_init_0_25Hz(timestamp);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_lepu_blepro_utils_AlgorithmUtil_sleep_1alg_1main_1pro_10_125Hz(JNIEnv *env, jclass clazz, jshort pr, jint acc) {
    sleep_alg_input_t i;
    i.pr = pr;
    i.acc = acc;
    sleep_alg_input_t  *input = &i;
    int result = sleep_alg_main_pro_0_25Hz(input);
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_lepu_blepro_utils_AlgorithmUtil_sleep_1alg_1main(JNIEnv *env, jclass clazz, jintArray pr, jintArray acc) {
    int len = (*env).GetArrayLength(pr);
    int *p = (*env).GetIntArrayElements(pr, NULL);
    int *a = (*env).GetIntArrayElements(acc, NULL);
    sleep_alg_main(p, a, len);
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_lepu_blepro_utils_AlgorithmUtil_sleep_1alg_1get_1res_10_125Hz(JNIEnv *env, jclass clazz) {
    sleep_alg_result *result = sleep_alg_get_res_0_25Hz();
    int len = result->sleep_state_buff_len;
    int *arrays = 0;
    arrays = (int *)malloc(sizeof(int)*(12+len));
    memset(arrays,'\0',sizeof(arrays));
    arrays[0] = result->sleep_time;
    arrays[1] = result->deep_time;
    arrays[2] = result->light_time;
    arrays[3] = result->rem_time;
    arrays[4] = result->awake_cnt;
    arrays[5] = result->preparation_time;
    arrays[6] = result->falling_asleep;
    arrays[7] = result->sleep_state_buff_len;
    for (int i = 0; i < len; i++) {
        arrays[8+i] = result->sleep_state_buff[i];
    }
    arrays[8+len] = result->awake_time;
    arrays[9+len] = result->deep_time_percent;
    arrays[10+len] = result->light_time_percent;
    arrays[11+len] = result->rem_time_percent;
    jintArray data = (*env).NewIntArray(12+len);
    (*env).SetIntArrayRegion(data, 0, (12+len), arrays);
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