//文//*************************************************************************
//文件名称：sleep_alg_main.h
//
//文件说明：睡眠分期算法主要接口
//
//更改历史：Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

#ifndef _SLEEP_ALG_MAIN_0_25_HZ_H_
#define _SLEEP_ALG_MAIN_0_25_HZ_H_

#include "sleep_alg_filter.h"
#include <string.h>
#include "math.h"

#define			SLEEP_ALG_SAMPLE_0_25_HZ				(0.25)

#define			SLEEP_ALG_AWAKE_COUNT					(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 10 * 60)			//用于计算清醒时心率的点数（10分钟）
#define			SLEEP_ALG_STATUS_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 5)			//状态持续时间的点数（5分钟）

#define			SLEEP_ALG_PR_BUFF_INTERVAL				(int)(60 * SLEEP_ALG_SAMPLE_0_25_HZ * 1)						//心率缓存采样间隔
#define			SLEEP_ALG_ACC_BUFF_INTERVAL				(1)						//三轴缓存采样间隔

#define			SLEEP_ALG_PR_LOW_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 3)			//状态持续时间的点数（3分钟）
#define			SLEEP_ALG_ACC_HIGH_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 3)			//状态持续时间的点数（3分钟）


#define			SLEEP_ALG_ACC_BUFF_SIZE					(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 10)			//三轴缓存个数
#define			SLEEP_ALG_BASE_PR_BUFF_SIZE				(5)						//心率基线缓存

#define			SLEEP_ALG_STD_PR_BUFF_INTERVAL			(int)(4 * SLEEP_ALG_SAMPLE_0_25_HZ)						//心率缓存采样间隔
#define			SLEEP_ALG_STD_PR_BUFF_SIZE				(60)						//标准差缓存

#define			SLEEP_ALG_REM_COUNTINUE_COUNT			(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 4)

#define         SLEEP_STATE_BUFF_LEN                    (int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 60 * 10)          // 保存睡眠状态数组最大长度

/*
typedef enum
{
	SLEEP_ALG_STATUS_NONE = 0,			//未得出结果
	SLEEP_ALG_STATUS_REM,				//快速眼动
	SLEEP_ALG_STATUS_DEEP,				//深睡眠
	SLEEP_ALG_STATUS_LIGHT,				//浅睡眠
	SLEEP_ALG_STATUS_AWAKE,				//清醒
}SLEEP_ALG_STATUS;
*/
//
typedef enum
{
	SLEEP_ALG_STATUS_DEEP = 0,		    //深睡眠
	SLEEP_ALG_STATUS_LIGHT,				//浅睡眠
	SLEEP_ALG_STATUS_REM,				//快速眼动
	SLEEP_ALG_STATUS_AWAKE,				//清醒
	SLEEP_ALG_STATUS_PREPARATION,       //准备睡眠阶段（可算在卧床时间里）（没使用）
	SLEEP_ALG_STATUS_NONE,			    //未得出结果
}SLEEP_ALG_STATUS;



//主输入结构体
typedef struct
{
	short             pr;				//脉率（单位1bpm）
	int				  acc;				//三轴值
}sleep_alg_input_t;

typedef struct
{
	unsigned int	sleep_time;		//总睡眠时间（单位：s）
	unsigned int	deep_time;		//深睡时间（单位：s）
	unsigned int	light_time;		//浅睡时间（单位：s）
	unsigned int	rem_time;		//快速眼动时间（单位：s）
	unsigned int	awake_cnt;		//清醒次数
	unsigned int    preparation_time; //准备睡眠时间（单位：s）(暂时没使用)
	unsigned int    falling_asleep;   //入睡时间点 (时间戳)
	SLEEP_ALG_STATUS   sleep_state_buff[SLEEP_STATE_BUFF_LEN];  // 保存睡眠分期结果
	unsigned int    sleep_state_buff_len;       // 保存睡眠分期结果数组长度
	unsigned int    awake_time;       // 出睡时间点 （时间戳）

	unsigned int    deep_time_percent;  // 深睡比例（单位：%）
	unsigned int    light_time_percent; // 浅睡比例（单位：%）
	unsigned int    rem_time_percent;   // 快速眼动比例（单位：%）
}sleep_alg_result;

typedef struct
{
	short			pr;
	short			pr_base;
}sleep_alg_rem_pr;


void sleep_alg_init_0_25Hz(unsigned int input_start_timestamp);
SLEEP_ALG_STATUS sleep_alg_main_pro_0_25Hz(sleep_alg_input_t  *input);
sleep_alg_result* sleep_alg_get_res_0_25Hz();

int sleep_alg_get_pr_base();

int sleep_alg_get_pr();

int sleep_alg_get_acc();


SLEEP_ALG_STATUS sleep_alg_get_status();

#endif