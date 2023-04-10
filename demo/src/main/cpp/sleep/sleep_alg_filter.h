//*************************************************************************
//�ļ����ƣ�sleep_alg_filter.h
//
//�ļ�˵�����˲��ӿں���
//
//������ʷ��Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

#ifndef _SLEEP_ALG_FILTER_H_
#define _SLEEP_ALG_FILTER_H_

#define     SLEEP_ALG_FILTER_COEFFICIENT_GAIN          (1 << 30)

//IIR�˲��ṹ��
typedef struct
{
	int             *numerator_buff;        //��������
	int             *denominator_buff;      //��ĸ����
	int             gain;                   //��������
	int             *x;                     //���㻺��
	int             *y;                     //���㻺��
	unsigned char   num;                    //���Ӹ���
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
