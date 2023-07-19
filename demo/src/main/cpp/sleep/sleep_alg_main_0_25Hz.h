//��//*************************************************************************
//�ļ����ƣ�sleep_alg_main.h
//
//�ļ�˵����˯�߷����㷨��Ҫ�ӿ�
//
//������ʷ��Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

#ifndef _SLEEP_ALG_MAIN_0_25_HZ_H_
#define _SLEEP_ALG_MAIN_0_25_HZ_H_

#include "sleep_alg_filter.h"
#include <string.h>
#include "math.h"

#define			SLEEP_ALG_SAMPLE_0_25_HZ				(0.25)

#define			SLEEP_ALG_AWAKE_COUNT					(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 10 * 60)			//���ڼ�������ʱ���ʵĵ�����10���ӣ�
#define			SLEEP_ALG_STATUS_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 5)			//״̬����ʱ��ĵ�����5���ӣ�

#define			SLEEP_ALG_PR_BUFF_INTERVAL				(int)(60 * SLEEP_ALG_SAMPLE_0_25_HZ * 1)						//���ʻ���������
#define			SLEEP_ALG_ACC_BUFF_INTERVAL				(1)						//���Ỻ��������

#define			SLEEP_ALG_PR_LOW_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 3)			//״̬����ʱ��ĵ�����3���ӣ�
#define			SLEEP_ALG_ACC_HIGH_COUNTINUE_COUNT		(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 3)			//״̬����ʱ��ĵ�����3���ӣ�


#define			SLEEP_ALG_ACC_BUFF_SIZE					(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 10)			//���Ỻ�����
#define			SLEEP_ALG_BASE_PR_BUFF_SIZE				(5)						//���ʻ��߻���

#define			SLEEP_ALG_STD_PR_BUFF_INTERVAL			(int)(4 * SLEEP_ALG_SAMPLE_0_25_HZ)						//���ʻ���������
#define			SLEEP_ALG_STD_PR_BUFF_SIZE				(60)						//��׼���

#define			SLEEP_ALG_REM_COUNTINUE_COUNT			(int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 4)

#define         SLEEP_STATE_BUFF_LEN                    (int)(SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 60 * 10)          // ����˯��״̬������󳤶�

/*
typedef enum
{
	SLEEP_ALG_STATUS_NONE = 0,			//δ�ó����
	SLEEP_ALG_STATUS_REM,				//�����۶�
	SLEEP_ALG_STATUS_DEEP,				//��˯��
	SLEEP_ALG_STATUS_LIGHT,				//ǳ˯��
	SLEEP_ALG_STATUS_AWAKE,				//����
}SLEEP_ALG_STATUS;
*/
//
typedef enum
{
	SLEEP_ALG_STATUS_DEEP = 0,		    //��˯��
	SLEEP_ALG_STATUS_LIGHT,				//ǳ˯��
	SLEEP_ALG_STATUS_REM,				//�����۶�
	SLEEP_ALG_STATUS_AWAKE,				//����
	SLEEP_ALG_STATUS_PREPARATION,       //׼��˯�߽׶Σ��������Դ�ʱ�����ûʹ�ã�
	SLEEP_ALG_STATUS_NONE,			    //δ�ó����
}SLEEP_ALG_STATUS;



//������ṹ��
typedef struct
{
	short             pr;				//���ʣ���λ1bpm��
	int				  acc;				//����ֵ
}sleep_alg_input_t;

typedef struct
{
	unsigned int	sleep_time;		//��˯��ʱ�䣨��λ��s��
	unsigned int	deep_time;		//��˯ʱ�䣨��λ��s��
	unsigned int	light_time;		//ǳ˯ʱ�䣨��λ��s��
	unsigned int	rem_time;		//�����۶�ʱ�䣨��λ��s��
	unsigned int	awake_cnt;		//���Ѵ���
	unsigned int    preparation_time; //׼��˯��ʱ�䣨��λ��s��(��ʱûʹ��)
	unsigned int    falling_asleep;   //��˯ʱ��� (ʱ���)
	SLEEP_ALG_STATUS   sleep_state_buff[SLEEP_STATE_BUFF_LEN];  // ����˯�߷��ڽ��
	unsigned int    sleep_state_buff_len;       // ����˯�߷��ڽ�����鳤��
	unsigned int    awake_time;       // ��˯ʱ��� ��ʱ�����

	unsigned int    deep_time_percent;  // ��˯��������λ��%��
	unsigned int    light_time_percent; // ǳ˯��������λ��%��
	unsigned int    rem_time_percent;   // �����۶���������λ��%��
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