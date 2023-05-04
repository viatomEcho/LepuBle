//*************************************************************************
//�ļ����ƣ�sleep_alg_main.c
//
//�ļ�˵����˯�߷����㷨���ļ�
//
//������ʷ��Created by xuyixuan [2022 / 11 / 28]
//*************************************************************************

//#include "stdafx.h"

/*==============================================================================
	������ʵ�ַ���
	1����˯�жϣ����������½���һ��ʱ�������
	2�������۶�������ֵͻ��
	3����˯������û��ͻ����ƽ��
	4��ǳ˯������û��ͻ�䵫��С������;С�����嶯
	5�����ѣ��嶯���Ի����ʴ������
	��ע��������ÿ���׶ζ����������³���ʱ�䣬��ֹ�׶�Ƶ���䶯
==============================================================================*/


#include "sleep_alg_main_0_25Hz.h"

static unsigned int		m_sleep_alg_count = 0;									//�㷨ִ�м���
static SLEEP_ALG_STATUS	m_sleep_alg_status = SLEEP_ALG_STATUS_NONE;				//��ǰ˯��״̬
static SLEEP_ALG_STATUS	m_sleep_alg_pre_status = SLEEP_ALG_STATUS_NONE;			//��һ������˯��״̬
static int					m_sleep_alg_pr = 0;										//�˲��������ֵ
static int					m_sleep_alg_pre_pr = 0;										//�˲��������ֵ
static int					m_sleep_alg_acc = 0;									//�˲���ļ��ٶ�ֵ
static int					m_sleep_alg_pre_acc = 0;								//��һ�����ڵļ��ٶ�ֵ
static unsigned int		m_sleep_alg_acc_high_count = 0;							//�߼��ٶ�ֵ����
static unsigned char		m_sleep_alg_acc_high_sign = 0;							//���ٶ�ֵ�߱�ǣ��ñ�־��1���淢��������
static unsigned int		m_sleep_alg_status_change_time = 0;						//˯��״̬�����仯��ʱ��
static int					m_sleep_alg_pr_base = 0;								//���ʻ���
static int					m_sleep_alg_pr_base_buff[SLEEP_ALG_BASE_PR_BUFF_SIZE] = { 0 };//���ʻ��߻���
//static sleep_alg_rem_pr					m_sleep_alg_pr_buff[SLEEP_ALG_STD_PR_BUFF_SIZE] = { 0 };//���ʻ���
static int					m_sleep_alg_pr_buff[SLEEP_ALG_STD_PR_BUFF_SIZE] = { 0 };//���ʻ���
static unsigned int		m_sleep_alg_pr_base_buff_count = 0;						//���ʻ��߻������
static unsigned int		m_sleep_alg_pr_buff_count = 0;						//���ʻ������
static unsigned char		m_sleep_alg_fall_sleep_flag = 0;						//����˯�ߵı�־
static unsigned int		m_sleep_alg_fall_sleep_time = 0;						//����˯�ߵ�ʱ��
static int					m_sleep_alg_pr_base_std = 0;							//���߱�׼��
static int					m_sleep_alg_pr_std = 0;
static unsigned char		m_sleep_alg_rem_flag = 0;								//��������۶��ı�־
static unsigned int		m_sleep_alg_rem_count = 0;								//��������۶�����
static unsigned int		m_sleep_alg_awake_count = 0;							//���ѳ����׶μ���
static int					m_sleep_alg_pr_buff_high_count = 0;				
static sleep_alg_result	m_sleep_alg_res = { 0 };
//�㷨��ʼ��
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
//�㷨���ӿ�
SLEEP_ALG_STATUS sleep_alg_main_pro_0_25Hz(sleep_alg_input_t  *input) {

	//==============��ֹ0�����˲������㣬0������������һ���ϳ�ʱ�������ʱ��==================
	if (input->pr != 0 && m_sleep_alg_pre_pr == 0) {
		IIR_filter(IIR_pulse_0_25Hz, input->pr * 1000, 1);
		IIR_filter(IIR_pr_base_0_25Hz, input->pr * 10000, 1);
	}
	if (m_sleep_alg_count == 0) {
		IIR_filter(IIR_acc_base_0_25Hz, input->acc * 100, 1);
	}
	//=======================�����˲�=======================
	if (input->pr != 0) {
		m_sleep_alg_pr = IIR_filter(IIR_pulse_0_25Hz, input->pr * 1000, 0) / 1000;
		m_sleep_alg_pr_base = IIR_filter(IIR_pr_base_0_25Hz, input->pr * 10000, 0) / 10000;
	}
	else {
		m_sleep_alg_pr_base_buff_count = 0;
	}
	//m_sleep_alg_acc = IIR_filter(IIR_acc_base_0_25Hz, input->acc * 100, 0) / 100;
	m_sleep_alg_acc = IIR_filter(IIR_acc_0_25Hz, (input->acc - m_sleep_alg_acc) * 100, 0) / 100;
	//m_sleep_alg_acc = input->acc;//��ʱ����
	if (m_sleep_alg_acc > 100) {
		m_sleep_alg_acc = 100;
	}
	//=======================�����ж�=======================
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
	if (m_sleep_alg_acc >= 23) {//����������Ϊ����  16
		m_sleep_alg_rem_count = 0;
		m_sleep_alg_rem_flag = 0;
		m_sleep_alg_fall_sleep_flag = 0;
		m_sleep_alg_awake_count = 0;
	}
	//=======================δ����˯��=======================
	if (m_sleep_alg_fall_sleep_flag == 0) {
		m_sleep_alg_awake_count++;
	}
	//==========================���ʲ����ж�============================
	if (m_sleep_alg_count % SLEEP_ALG_STD_PR_BUFF_INTERVAL == 0 && input->pr != 0 && m_sleep_alg_fall_sleep_flag) {
		m_sleep_alg_pr_buff[m_sleep_alg_pr_buff_count % SLEEP_ALG_STD_PR_BUFF_SIZE] = m_sleep_alg_pr;
		m_sleep_alg_pr_buff_high_count = 0;
		//ƽ�������ж�
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
	//=======================���������½��׶��ж�=======================
	unsigned char pr_down_count = 0;
	//unsigned char pr_equal_count = 0;
	if (m_sleep_alg_count % SLEEP_ALG_PR_BUFF_INTERVAL == 0 && input->pr != 0) {
		//д�뻺��
		m_sleep_alg_pr_base_buff[m_sleep_alg_pr_base_buff_count %SLEEP_ALG_BASE_PR_BUFF_SIZE] = m_sleep_alg_pr_base;
		if (m_sleep_alg_pr_base_buff_count >= SLEEP_ALG_BASE_PR_BUFF_SIZE) {
			//�½������ж�
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
			//ƽ�������ж�
			int	sum = 0, avge = 0, diff_sum = 0;
			for (int i = 0; i < SLEEP_ALG_BASE_PR_BUFF_SIZE; i++) {
				sum += m_sleep_alg_pr_base_buff[i];
			}
			avge = sum / SLEEP_ALG_BASE_PR_BUFF_SIZE;
			for (int i = 0; i < SLEEP_ALG_BASE_PR_BUFF_SIZE; i++) {
				diff_sum += abs(m_sleep_alg_pr_base_buff[i] - avge);
			}
			m_sleep_alg_pr_base_std = diff_sum / SLEEP_ALG_BASE_PR_BUFF_SIZE;



			//��˯�ж�
			if (pr_down_count >= 4					//����4��ֵ�½� 
				&& m_sleep_alg_awake_count > SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 10		//�������10���ӣ��˵�������˯ʱ��Ϊ10-30���ӣ�
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


	//=======================�����۶��ж�=======================
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

	//��ʱ����
	if (m_sleep_alg_count == 5580) {
		m_sleep_alg_count = m_sleep_alg_count;
	}

	//=======================��������������Ϊ����=======================
	if (m_sleep_alg_pr >= m_sleep_alg_pr_base * 117 / 100  // 115
		&& m_sleep_alg_acc_high_sign
		//&& m_sleep_alg_count > SLEEP_ALG_SAMPLE_0_25_HZ * 60 * 30	//��Сʱǰ������������Ϊ�ǿ����۶�
		&& m_sleep_alg_pr >= 75) {
		m_sleep_alg_rem_count = 0;
		m_sleep_alg_rem_flag = 0;
		m_sleep_alg_fall_sleep_flag = 0;
		m_sleep_alg_awake_count = 0;
	}
	
	//=======================˯��״̬�ж�=======================
	if (m_sleep_alg_fall_sleep_flag != 0/* && m_sleep_alg_count - m_sleep_alg_status_change_time > SLEEP_ALG_STATUS_COUNTINUE_COUNT*/) {//���������
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
	//=======================˯��״̬�ı�=======================
	if (m_sleep_alg_pre_status != m_sleep_alg_status && m_sleep_alg_count - m_sleep_alg_status_change_time > SLEEP_ALG_STATUS_COUNTINUE_COUNT) {
		if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT && m_sleep_alg_pre_status == SLEEP_ALG_STATUS_REM) {//�����۶���ǳ˯�׶εĹ����ڳ�����һЩ����ֹ��˯ǳ˯Ƶ���䶯
			m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE_0_25_HZ * 5 * 60;
		}
		else if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT) {//ǳ˯ʱ������һЩ
			m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE_0_25_HZ * 2 * 60;
		}
		//else if (m_sleep_alg_status == SLEEP_ALG_STATUS_LIGHT && m_sleep_alg_pre_status == SLEEP_ALG_STATUS_AEAKE) {//���ѵ���˯��ʱ����Ҫһ��ʱ��Ĺ���
		//	m_sleep_alg_status_change_time = m_sleep_alg_count + SLEEP_ALG_SAMPLE * 4 * 60;
		//}
		else {
			m_sleep_alg_status_change_time = m_sleep_alg_count;
		}

	}
	//=======================˯��״̬����ͳ��=======================
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
	//return m_sleep_alg_pr_buff_high_count;//��ʱ����
}

SLEEP_ALG_STATUS sleep_alg_get_status() {
	return m_sleep_alg_status;
}


