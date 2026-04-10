package com.stock.fund.application.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 开盘时间判断工具类
 * 提取为package-private以便测试访问
 */
class MarketTimeChecker {

    /**
     * 判断当前时间是否为开盘时间段
     * A股开盘时间：9:30 - 15:00
     * @param date 查询日期
     * @param time 查询时间
     * @return 是否在开盘时间段内
     */
    static boolean isMarketOpenTime(LocalDate date, LocalTime time) {
        // 判断是否为工作日（周一至周五）
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // 判断是否在开盘时间段内（9:30 - 15:00）
        LocalTime marketOpenStart = LocalTime.of(9, 30);
        LocalTime marketOpenEnd = LocalTime.of(15, 0);

        return !time.isBefore(marketOpenStart) && !time.isAfter(marketOpenEnd);
    }
}
