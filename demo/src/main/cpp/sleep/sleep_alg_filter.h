//*************************************************************************
//文件名称：sleep_alg_filter.h
//
//文件说明：滤波接口函数
//
//更改历史：Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

#ifndef _SLEEP_ALG_FILTER_H_
#define _SLEEP_ALG_FILTER_H_

#define     SLEEP_ALG_FILTER_COEFFICIENT_GAIN          (1 << 30)

//IIR滤波结构体
typedef struct
{
	int             *numerator_buff;        //分子数组
	int             *denominator_buff;      //分母数组
	int             gain;                   //函数增益
	int             *x;                     //运算缓存
	int             *y;                     //运算缓存
	unsigned char   num;                    //分子个数
}IIR_filter_t;


extern IIR_filter_t IIR_acc;
extern IIR_filter_t IIR_pulse;
extern IIR_filter_t IIR_pr_base;
extern IIR_filter_t IIR_acc_0_25Hz;
extern IIR_filter_t IIR_pulse_0_25Hz;
extern IIR_filter_t IIR_pr_base_0_25Hz;
extern IIR_filter_t IIR_acc_base_0_25Hz;
void sleep_alg_filter_init();
int IIR_filter(IIR_filter_t filter, int data, unsigned char reset);
#endif
