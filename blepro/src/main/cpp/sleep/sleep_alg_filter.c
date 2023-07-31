//*************************************************************************
//文件名称：sleep_alg_filter.c
//
//文件说明：睡眠分期算法滤波相关
//
//更改历史：Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************
//#include "stdafx.h"
#include "sleep_alg_filter.h"


//心率值滤波函数(Fs=10hz,Fc=0.005)
int m_coefficient_pulse_b[2] = { 1,1 };
int m_coefficient_pulse_a[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.99686333183343789343* SLEEP_ALG_FILTER_COEFFICIENT_GAIN)};
int m_coefficient_pulse_x[2] = { 0 };
int m_coefficient_pulse_y[2] = { 0 };
IIR_filter_t IIR_pulse = { m_coefficient_pulse_b,m_coefficient_pulse_a,(int)(0.001568334083280984543* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_pulse_x,m_coefficient_pulse_y,2 };
//心率基线滤波函数(Fs=10hz,Fc=0.0005)
int m_coefficient_pr_base_b[2] = { 1,1 };
int m_coefficient_pr_base_a[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.999685890072329663702532798197353258729* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_pr_base_x[2] = { 0 };
int m_coefficient_pr_base_y[2] = { 0 };
IIR_filter_t IIR_pr_base = { m_coefficient_pr_base_b,m_coefficient_pr_base_a,(int)(0.000157054963835185252022871860155817103* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_pr_base_x,m_coefficient_pr_base_y,2 };

//加速度滤波函数(Fs=10hz,Fc=0.005)
int m_coefficient_acc_b[2] = { 1,1 };
int m_coefficient_acc_a[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.99686333183343789343* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_acc_x[2] = { 0 };
int m_coefficient_acc_y[2] = { 0 };
IIR_filter_t IIR_acc = { m_coefficient_acc_b,m_coefficient_acc_a,(int)(0.001568334083280984543 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_acc_x,m_coefficient_acc_y,2 };


//心率值滤波函数(Fs=0.25hz,Fc=0.005)
int m_coefficient_pulse_b_0_25Hz[2] = { 1,1 };
int m_coefficient_pulse_a_0_25Hz[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.927307768331003257067379763611825183034* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_pulse_x_0_25Hz[2] = { 0 };
int m_coefficient_pulse_y_0_25Hz[2] = { 0 };
IIR_filter_t IIR_pulse_0_25Hz = { m_coefficient_pulse_b_0_25Hz,m_coefficient_pulse_a_0_25Hz,(int)(0.036346115834498420038567445544686052017   * SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_pulse_x_0_25Hz,m_coefficient_pulse_y_0_25Hz,2 };
//心率基线滤波函数(Fs=0.25hz,Fc=0.0005)
int m_coefficient_pr_base_b_0_25Hz[2] = { 1,1 };
int m_coefficient_pr_base_a_0_25Hz[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.987511929907314289778241800377145409584* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_pr_base_x_0_25Hz[2] = { 0 };
int m_coefficient_pr_base_y_0_25Hz[2] = { 0 };
IIR_filter_t IIR_pr_base_0_25Hz = { m_coefficient_pr_base_b_0_25Hz,m_coefficient_pr_base_a_0_25Hz,(int)(0.006244035046342847304623457915795370354 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_pr_base_x_0_25Hz,m_coefficient_pr_base_y_0_25Hz,2 };

//加速度滤波函数(Fs=0.25hz,Fc=0.005)
int m_coefficient_acc_b_0_25Hz[2] = { 1,1 };
int m_coefficient_acc_a_0_25Hz[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.881618592363189068628059885668335482478* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_acc_x_0_25Hz[2] = { 0 };
int m_coefficient_acc_y_0_25Hz[2] = { 0 };
IIR_filter_t IIR_acc_0_25Hz = { m_coefficient_acc_b_0_25Hz,m_coefficient_acc_a_0_25Hz,(int)(0.059190703818405444869288345444147125818  * SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_acc_x_0_25Hz,m_coefficient_acc_y_0_25Hz,2 };

//加速度去基线滤波函数(Fs=0.25hz,Fc=0.001)
int m_coefficient_acc_b_base_0_25Hz[2] = { 1,1 };
int m_coefficient_acc_a_base_0_25Hz[2] = { 1 * SLEEP_ALG_FILTER_COEFFICIENT_GAIN,(int)(-0.975177876180649105819497890479397028685* SLEEP_ALG_FILTER_COEFFICIENT_GAIN) };
int m_coefficient_acc_x_base_0_25Hz[2] = { 0 };
int m_coefficient_acc_y_base_0_25Hz[2] = { 0 };
IIR_filter_t IIR_acc_base_0_25Hz = { m_coefficient_acc_b_base_0_25Hz,m_coefficient_acc_a_base_0_25Hz,(int)(0.012411061909675434947186722922651824774   * SLEEP_ALG_FILTER_COEFFICIENT_GAIN) ,m_coefficient_acc_x_base_0_25Hz,m_coefficient_acc_y_base_0_25Hz,2 };


//滤波初始化
void sleep_alg_filter_init()
{
	IIR_filter(IIR_pulse, 0, 1);
	IIR_filter(IIR_acc, 0, 1);
	IIR_filter(IIR_pr_base, 0, 1);
	IIR_filter(IIR_pulse_0_25Hz, 0, 1);
	IIR_filter(IIR_acc_0_25Hz, 0, 1);
	IIR_filter(IIR_pr_base_0_25Hz, 0, 1);
	IIR_filter(IIR_acc_base_0_25Hz, 0, 1);
}

int IIR_filter(IIR_filter_t filter, int data, unsigned char reset) {
	int denominator_sum = 0;
	int numerator_sum = 0;
	int i = 0;
	if (reset)
	{
		for (i = 0; i < filter.num; i++)
		{
			filter.x[i] = data;
			filter.y[i] = data;
		}
		return data;
	}

	if (filter.num < 2) {
		return 0;
	}
	// 更新x(n)
	filter.x[0] = data;
	// 更新y(n)
	numerator_sum = 0;
	for (i = 0; i < filter.num; i++)
	{
		numerator_sum += (int)((filter.numerator_buff[i] * filter.gain * (long long)filter.x[i]) / SLEEP_ALG_FILTER_COEFFICIENT_GAIN);
	}
	denominator_sum = 0;
	for (i = 0; i < (filter.num - 1); i++)
	{
		denominator_sum += (int)((filter.denominator_buff[i + 1] * (long long)filter.y[i + 1]) / SLEEP_ALG_FILTER_COEFFICIENT_GAIN);
	}
	filter.y[0] = (numerator_sum - denominator_sum);

	//循环
	for (i = filter.num - 1; i > 0; i--) {
		filter.x[i] = filter.x[i - 1];
		filter.y[i] = filter.y[i - 1];
	}

	return filter.y[0];
}

