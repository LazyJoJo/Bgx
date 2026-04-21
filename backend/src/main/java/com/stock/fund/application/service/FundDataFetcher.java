package com.stock.fund.application.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.stock.fund.domain.entity.FundQuote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 基金数据获取器服务 封装基金实时数据的获取逻辑，支持重试机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundDataFetcher {

    private final DataCollectionAppService dataCollectionAppService;

    /**
     * 使用 Spring Retry 获取基金实时数据 最多重试 3 次，初始间隔 1 秒，失败后进入恢复流程
     */
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public FundQuote fetchFundDataWithRetry(String code) {
        try {
            FundQuote quote = dataCollectionAppService.fetchFundRealTimeData(code);
            if (quote == null) {
                throw new RuntimeException("Fund " + code + " data is empty");
            }
            return quote;
        } catch (Exception e) {
            log.warn("Failed to fetch fund {} info: {}", code, e.getMessage());
            throw e; // 触发重试
        }
    }

    /**
     * 重试失败后的恢复方法，返回 null 表示最终失败
     */
    @Recover
    public FundQuote recoverFromFetchFailure(Exception e, String code) {
        log.error("Fund {} fetch ultimately failed, max retries reached: {}", code, e.getMessage());
        return null;
    }
}
