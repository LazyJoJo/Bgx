package com.stock.fund.application.scheduler;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * 风险提醒调度器测试
 * TDD RED阶段：验证调度器在11:30和14:30正确触发风险提醒检测
 */
@ExtendWith(MockitoExtension.class)
class RiskAlertSchedulerTest {

    @Mock
    private RiskAlertAppService riskAlertAppService;

    @Mock
    private SchedulerConfig schedulerConfig;

    @InjectMocks
    private RiskAlertScheduler riskAlertScheduler;

    @Test
    @DisplayName("11:30触发 - 应调用风险提醒检测并传入11:30时间点")
    void triggerAt1130_shouldCallCheckRiskAlertsWith1130() {
        // given
        doNothing().when(riskAlertAppService).checkAndCreateRiskAlerts(anyString());

        // when
        riskAlertScheduler.triggerRiskAlertCheckAt1130();

        // then
        verify(riskAlertAppService, times(1)).checkAndCreateRiskAlerts("11:30");
    }

    @Test
    @DisplayName("14:30触发 - 应调用风险提醒检测并传入14:30时间点")
    void triggerAt1430_shouldCallCheckRiskAlertsWith1430() {
        // given
        doNothing().when(riskAlertAppService).checkAndCreateRiskAlerts(anyString());

        // when
        riskAlertScheduler.triggerRiskAlertCheckAt1430();

        // then
        verify(riskAlertAppService, times(1)).checkAndCreateRiskAlerts("14:30");
    }

    @Test
    @DisplayName("触发检测时异常 - 应捕获异常不影响调度")
    void triggerRiskAlertCheck_exception_shouldNotThrow() {
        // given: 风险提醒检测抛出异常
        doThrow(new RuntimeException("Database error")).when(riskAlertAppService).checkAndCreateRiskAlerts(anyString());

        // when & then: 不应抛出异常，每个方法都能正常执行（虽然内部异常被捕获）
        riskAlertScheduler.triggerRiskAlertCheckAt1130();
        riskAlertScheduler.triggerRiskAlertCheckAt1430();

        // 验证两个方法都尝试调用了风险提醒检测
        verify(riskAlertAppService, times(2)).checkAndCreateRiskAlerts(anyString());
    }
}
