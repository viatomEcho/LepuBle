//*************************************************************************
//文件名称：sleep_alg_main.c
//
//文件说明：睡眠分期算法主文件
//
//更改历史：Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

//#include "stdafx.h"

/*==============================================================================
	各功能实现方案
	1、入睡判断：出现心率下降的一段时间的现象
	2、快速眼动：心率值突变
	3、深睡：心率没有突变且平缓
	4、浅睡：心率没有突变但有小幅波动;小幅度体动
	5、清醒：体动明显或心率大幅跳动
	备注：程序在每个阶段都会设置最新持续时间，防止阶段频繁变动
==============================================================================*/


#include "sleep_alg_main_0_25Hz.h"

static unsigned int		m_sleep_alg_count = 0;									//算法执行计数
static SLEEP_ALG_STATUS	m_sleep_alg_status = SLEEP_ALG_STATUS_NONE;				//当前睡眠状态
static SLEEP_ALG_STATUS	m_sleep_alg_pre_status = SLEEP_ALG_STATUS_NONE;			//上一个周期睡眠状态
static int					m_sleep_alg_pr = 0;										//滤波后的心率值
static int					m_sleep_alg_pre_pr = 0;										//滤波后的心率值
static int					m_sleep_alg_acc = 0;									//滤波后的加速度值
static int					m_sleep_alg_pre_acc = 0;								//上一个周期的加速度值
static unsigned int		m_sleep_alg_acc_high_count = 0;							//高加速度值计数
static unsigned char		m_sleep_alg_acc_high_sign = 0;							//加速度值高标记（该标志置1表面发生动作）
static unsigned int		m_sleep_alg_status_change_time = 0;						//睡眠状态发生变化的时刻
static int					m_sleep_alg_pr_base = 0;								//心率基线
static int					m_sleep_alg_pr_base_buff[SLEEP_ALG_BASE_PR_BUFF_SIZE] = { 0 };//心率基线缓存
//static sleep_alg_rem_pr					m_sleep_alg_pr_buff[SLEEP_ALG_STD_PR_BUFF_SIZE] = { 0 };//心率缓存
static int					m_sleep_alg_pr_buff[SLEEP_ALG_STD_PR_BUFF_SIZE] = { 0 };//心率缓存
static unsigned int		m_sleep_alg_pr_base_buff_count = 0;						//心率基线缓存计数
static unsigned int		m_sleep_alg_pr_buff_count = 0;						//心率缓存计数
static unsigned char		m_sleep_alg_fall_sleep_flag = 0;						//进入睡眠的标志
static unsigned int		m_sleep_alg_fall_sleep_time = 0;						//进入睡眠的时间
static int					m_sleep_alg_pr_base_std = 0;							//基线标准差
static int					m_sleep_alg_pr_std = 0;
static unsigned char		m_sleep_alg_rem_flag = 0;								//进入快速眼动的标志
static unsigned int		m_sleep_alg_rem_count = 0;								//进入快速眼动计数
static unsigned int		m_sleep_alg_awake_count = 0;							//清醒持续阶段计数
static int					m_sleep_alg_pr_buff_high_count = 0;				
static sleep_alg_result	m_sleep_alg_res = { 0 };
//算法初始化
void sleep_alg_init_0_25Hz() {
	sleep_alg_filter_init();
	m_sleep_alg_status = SLEEP_ALG_STATUS_AWAKE;
	m_sleep_alg_pre_status = SLEEP_ALG_STATUS_AWAKE;
	m_sleep_alg_acc_high_count = 0;
	m_sleep_alg_fall_sleep_flag = 0;
	memset(&m_sleep_alg_res, 0, sizeof(m_sleep_alg_res));
	memset(&m_sleep_alg_pr_buff, 0, sizeof(m_sleep_alg_pr_buff));
	m_sleep_alg_pr_buff_high_count = 0;
	m_sleep_alg_pr_buff_count = 0;
}
//算法主接口
SLEEP_ALG_STATUS sleep_alg_main_pro_0_25Hz(sleep_alg_input_t  *input) {

	//==============防止0进入滤波器运算，0进入运算会出现一个较长时间的上升时间==================
	if (input->pr != 0 && m_sleep_alg_pre_pr == 0) {
		IIR_filter(IIR_pulse_0_25Hz, input->pr * 1000, 1);
		IIR_filter(IIR_pr_base_0_25Hz, input->pr * 10000, 1);
	}
	if (m_sleep_alg_count == 0) {
		IIR_filter(IIR_acc_base_0_25Hz, input->acc * 100, 1);
	}
	//=======================数据滤波=======================
	if (input->pr != 0) {
		m_sleep_alg_pr = IIR_filter(IIR_pulse_0_25Hz, input->pr * 1000, 0) / 1000;
		m_sleep_alg_pr_base = IIR_filter(IIR_pr_base_0_25Hz, input->pr * 10000, 0) / 10000;
	}
	else {
		m_sleep_alg_pr_base_buff_count = 0;
	}
	//m_sleep_alg_acc = IIR_filter(IIR_acc_base_0_25Hz, input->acc * 100, 0) / 100;
	m_sleep_alg_acc = IIR_filter(IIR_acc_0_25Hz, (input->acc - m_sleep_alg_acc) * 100, 0) / 100;
	//m_sleep_alg_acc = input->acc;//临时测试
	if (m_sleep_alg_acc > 100) {
		m_sleep_alg_acc = 100;
	}
	//=======================动作判断=======================
	//if (m_sleep_alg_count % SLEEP_ALG_ACC_BUFF_INTERVAL == 0) {
	if (m_sleep_alg_acc >= 1 && m_sleep_alg_pre_acc < 3) {
		m_sleep_alg_acc_high_sign = 1;
		m_sleep_alg_acc_high_count = 0;
	}
	m_sleep_alg_pre_acc = m_sleep_alg_acc;
	//}
	if (m_sleep_alg_acc_high_sign) {
		m_sleep_alg_acc_high_count++;
	}
	if (m_sleep_alg_acc_high_count > SLEEP_ALG_ACC_HIGH_COUNTINUE_COUNT) {
		m_sleep_alg_acc_high_sign = 0;
		m_sleep_alg_acc_high_count = 0;
	}
	if (m_sleep_alg_acc >= 23) {//动作过大认为清醒  16
		m_sleep_alg_rem_count = 0;
		m_sleep_alg_rem_flag = 0;
		m_sleep_alg_fall_sleep_flag = 0;
		m_sleep_alg_awake_count = 0;
	}
	//=======================未进入睡眠=======================
	if (m_sleep_alg_fall_sleep_flag == 0) {
		m_sleep_alg_awake_count++;
	}
	//==========================心率波动判断============================
	if (m_sleep_alg_count % SLEEP_ALG_STD_PR_BUFF_INTERVAL == 0 && input->pr != 0 && m_sleep_alg_fall_sleep_flag) {
		m_sleep_alg_pr_buff[m_sleep_alg_pr_buff_count % SLEEP_ALG_STD_PR_BUFF_SIZE] = m_sleep_alg_pr;
		m_sleep_alg_pr_buff_high_count = 0;
		//平缓趋势判断
		int	sum = 0, avge = 0, diff_sum = 0;
		for (int i = 0; i < SLEEP_ALG_STD_PR_BUFF_SIZE; i++) {
			sum += m_sleep_alg_pr_buff[i];
			if (m_sleep_alg_pr > m_sleep_alg_pr_base * 102 / 100) {
				m_sleep_alg_pr_buff_high_count++;
			}
		}
		avge = sum / SLEEP_ALG_STD_PR_BUFF_SIZE;
		for (int i = 0; i < SLEEP_ALG_STD_PR_BUFF_SIZE; i++) {
			diff_sum += abs(m_sleep_alg_pr_buff[i] - avge);
		}
		m_sleep_alg_pr_std = diff_sum / SLEEP_ALG_STD_PR_BUFF_SIZE;
		
		m_sleep_alg_pr_buff_count++;
	}
	//=======================心率连续下降阶段判断=======================
	unsigned char pr_down_count = 0;
	//unsigned char pr_equal_count = 0;
	if (m_sleep_alg_count % SLEEP_ALG_PR_BUFF_INTERVAL == 0 && input->pr != 0) {
		//写入缓存
		m_sleep_alg_pr_base_buff[m_sleep_alg_pr_base_buff_count %SLEEP_ALG_BASE_PR_BUFF_SIZE] = m_sleep_alg_pr_base;
		if (m_sleep_alg_pr_base_buff_count >= SLEEP_ALG_BASE_PR_BUFF_SIZE) {
			//下降趋势判断
			for (int i = 0; i < SLEEP_ALG_BASE_PR_BUFF_SIZE - 1; i++) {
				if (m_sleep_alg_pr_base_buff[(m_sleep_alg_pr_base_buff_count - i) % SLEEP_ALG_BASE_PR_BUFF_SIZE] <= m_sleep_alg_pr_base_buff[(m_sleep_alg_pr_base_buff_count - i - 1) % SLEEP_ALG_BASE_PR_BUFF_SIZE]) {
					pr_down_count++;
				}
				else {
					pr_down_count = 0;
				}
				//if (m_sleep_alg_pr_base_buff[(m_sleep_alg_pr_base_buff_count - i) % SLEEP_ALG_BASE_PR_BUFF_SIZE] == m_sleep_alg_pr_base_buff[(m_sleep_alg_pr_base_buff_count - i - 1) % SLEEP_ALG_BASE_PR_BUFF_SIZE]) {
				//	pr_equal_count++;
				//}
			}
			//平缓趋势判断
			int	sum = 0, avge = 0, diff_sum = 0;
			for (int i = 0; i < SLEEP_ALG_BASE_PR_BUFF_SIZE; i++) {
				sum += m_sleep_alg_pr_base_buff[i];
			}
			avge = sum / SLEEP_ALG_BASE_PR_BUFF_SIZE;
			for (int i = 0; i < SLEEP_ALG_BASE_PR_BUFF_SIZE; i++) {
				diff_sum += abs(m_sleep_alg_pr_base_buff[i] - avge);
			}
			m_sleep_alg_pr_base_std = diff_sum / SLEEP_ALG_BASE_PR_BUFF_SIZE;



			//入睡判断
			if (pr_down_count >= 4					//连续4个值下降 
				&& m_sleep_alg_awake_count > SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 10		//佩戴超过10分钟（人的正常入睡时间为10-30分钟）
				&& avge < 75
				&& m_sleep_alg_fall_sleep_flag == 0
				&& (!m_sleep_alg_acc_high_sign)) {
				m_sleep_alg_fall_sleep_flag = 1;
				m_sleep_alg_fall_sleep_time = m_sleep_alg_count;
			}

			if (pr_down_count >= 3) {
				m_sleep_alg_pr_base_std = 0;
			}


		}
		m_sleep_alg_pr_base_buff_count++;
	}


	//=======================快速眼动判断=======================
	if (m_sleep_alg_pr > m_sleep_alg_pr_base * 106 / 100) {
		m_sleep_alg_rem_flag = 1;
		m_sleep_alg_rem_count = 0;
	}
	//else {
	//	m_sleep_alg_rem_flag = 0;
	//}
	if (m_sleep_alg_rem_flag) {
		m_sleep_alg_rem_count++;
	}
	if (m_sleep_alg_rem_count > SLEEP_ALG_REM_COUNTINUE_COUNT) {
		m_sleep_alg_rem_count = 0;
		m_sleep_alg_rem_flag = 0;
	}
	if (m_sleep_alg_pr_std > 1) {
		m_sleep_alg_rem_flag = 1;
	}

	//临时测试
	if (m_sleep_alg_count == 5580) {
		m_sleep_alg_count = m_sleep_alg_count;
	}

	//=======================心率跳动过大认为清醒=======================
	if (m_sleep_alg_pr >= m_sleep_alg_pr_base * 117 / 100  // 115
		&& m_sleep_alg_acc_high_sign
		//&& m_sleep_alg_count > SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 30	//半小时前的心率跳动认为是快速眼动
		&& m_sleep_alg_pr >= 75) {
		m_sleep_alg_rem_count = 0;
		m_sleep_alg_rem_flag = 0;
		m_sleep_alg_fall_sleep_flag = 0;
		m_sleep_alg_awake_count = 0;
	}
	
	//=======================睡眠状态判断=======================
	if (m_sleep_alg_fall_sleep_flag != 0/* && m_sleep_alg_count - m_sleep_alg_status_change_time > SLEEP_ALG_STATUS_COUNTINUE_COUNT*/) {//已求出基线
		if ((int)(m_sleep_alg_count - m_sleep_alg_status_change_time) > SLEEP_ALG_STATUS_COUNTINUE_COUNT) {
			if (!m_sleep_alg_acc_high_sign
				&&	m_sleep_alg_pr_std < 1) {
				if (m_sleep_alg_pre_status == SLEEP_ALG_STATUS_AWAKE) {
					m_sleep_alg_status = SLEEP_ALG_STATUS_LIGHT;
				}
				else {
					m_sleep_alg_status =  SLEEP_ALG_STATUS_DEEP;
				}

			}
			//else if (m_sleep_alg_pr_low_count <= SLEEP_ALG_PR_LOW_COUNTINUE_COUNT
			//	&& m_sleep_alg_acc_high_count >= 32) {
			//	if (m_sleep_alg_pre_status == SLEEP_ALG_STATUS_LIGHT) {
			//		m_sleep_alg_status = SLEEP_ALG_STATUS_AEAKE;
			//	}
			//	else {
			//		m_sleep_alg_pre_status = SLEEP_ALG_STATUS_LIGHT;
			//	}
			//}
			else {
				m_sleep_alg_status = SLEEP_ALG_STATUS_LIGHT;
			}

		}
		if (m_sleep_alg_rem_flag && (int)(m_sleep_alg_count - m_sleep_alg_fall_sleep_time) > SLEEP_ALG_SAMPLE_0_25_HZ * 15 * 60) {
			m_sleep_alg_status = SLEEP_ALG_STATUS_REM;
		}

	}
	else {
		m_sleep_alg_status = SLEEP_ALG_STATUS_AWAKE;
	}
	//=======================睡眠状态改变=======================
	if (m_sleep_alg_pre_status != m_sleep_alg_status && m_sleep_alg_count - m_sleep_alg_status_change_time > SLEEP_ALG_STATUS_COUNTINUE_COUNT) {
		if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT && m_sleep_alg_pre_status == SLEEP_ALG_STATUS_REM) {//快速眼动到浅睡阶段的过渡期持续长一些，防止深睡浅睡频繁变动
			m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE_0_25_HZ * 5 * 60;
		}
		else if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT) {//浅睡时间拉长一些
			m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE_0_25_HZ * 2 * 60;
		}
		//else if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT && m_sleep_alg_pre_status == SLEEP_ALG_STATUS_AEAKE) {//清醒到入睡的时间需要一定时间的过渡
		//	m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE * 4 * 60;
		//}
		else {
			m_sleep_alg_status_change_time = m_sleep_alg_count;
		}

	}
	//=======================睡眠状态数据统计=======================
	switch (m_sleep_alg_status)
	{
	case SLEEP_ALG_STATUS_REM:
		m_sleep_alg_res.rem_time++;
		break;
	case SLEEP_ALG_STATUS_DEEP:
		m_sleep_alg_res.deep_time++;
		break;
	case SLEEP_ALG_STATUS_LIGHT:
		m_sleep_alg_res.light_time++;
		break;
	case SLEEP_ALG_STATUS_AWAKE:
		if (m_sleep_alg_pre_status != SLEEP_ALG_STATUS_AWAKE) {
			m_sleep_alg_res.awake_time++;
		}
		break;
	default:
		break;
	}
	m_sleep_alg_res.sleep_time = m_sleep_alg_res.rem_time + m_sleep_alg_res.deep_time + m_sleep_alg_res.light_time;

	m_sleep_alg_pre_status = m_sleep_alg_status;
	m_sleep_alg_pre_pr = input->pr;

	m_sleep_alg_count++;
	return m_sleep_alg_status;
}


sleep_alg_result* sleep_alg_get_res_0_25Hz() {
	return (&m_sleep_alg_res);
}


int sleep_alg_get_pr_base() {
	return m_sleep_alg_pr_base;
}

int sleep_alg_get_pr() {
	return m_sleep_alg_pr;
}

int sleep_alg_get_acc() {
	return m_sleep_alg_acc;
	//return m_sleep_alg_pr_std;
	//return m_sleep_alg_pr_buff_high_count;//临时测试
}

SLEEP_ALG_STATUS sleep_alg_get_status() {
	return m_sleep_alg_status;
}


