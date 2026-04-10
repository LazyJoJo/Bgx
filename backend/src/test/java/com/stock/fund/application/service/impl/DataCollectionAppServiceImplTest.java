package com.stock.fund.application.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataCollectionAppServiceImpl 单元测试
 * 测试 isMarketOpenTime 开盘时间判断逻辑
 */
class DataCollectionAppServiceImplTest {

    /**
     * 通过测试辅助类调用 isMarketOpenTime
     */
    private boolean isMarketOpenTime(LocalDate date, LocalTime time) {
        return MarketTimeChecker.isMarketOpenTime(date, time);
    }

    @Test
    @DisplayName("周一9:30 - 应该返回true（开盘时间内）")
    void testMarketOpen_Monday930() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("周一14:30 - 应该返回true（开盘时间内）")
    void testMarketOpen_Monday1430() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(14, 30)));
    }

    @Test
    @DisplayName("周一15:00 - 应该返回true（收盘时刻）")
    void testMarketOpen_Monday1500() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(15, 0)));
    }

    @Test
    @DisplayName("周一8:00 - 应该返回false（开盘前）")
    void testMarketOpen_Monday0800() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("周一9:00 - 应该返回false（开盘前）")
    void testMarketOpen_Monday0900() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(9, 0)));
    }

    @Test
    @DisplayName("周一15:30 - 应该返回false（收盘后）")
    void testMarketOpen_Monday1530() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(15, 30)));
    }

    @Test
    @DisplayName("周一18:00 - 应该返回false（收盘后）")
    void testMarketOpen_Monday1800() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(18, 0)));
    }

    @Test
    @DisplayName("周六9:30 - 应该返回false（周末）")
    void testMarketOpen_Saturday930() {
        LocalDate saturday = getNextSaturday();
        assertFalse(isMarketOpenTime(saturday, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("周六14:00 - 应该返回false（周末）")
    void testMarketOpen_Saturday1400() {
        LocalDate saturday = getNextSaturday();
        assertFalse(isMarketOpenTime(saturday, LocalTime.of(14, 0)));
    }

    @Test
    @DisplayName("周日9:30 - 应该返回false（周末）")
    void testMarketOpen_Sunday930() {
        LocalDate sunday = getNextSunday();
        assertFalse(isMarketOpenTime(sunday, LocalTime.of(9, 30)));
    }

    @Test
    @DisplayName("周日14:00 - 应该返回false（周末）")
    void testMarketOpen_Sunday1400() {
        LocalDate sunday = getNextSunday();
        assertFalse(isMarketOpenTime(sunday, LocalTime.of(14, 0)));
    }

    @Test
    @DisplayName("周五15:00 - 应该返回true（交易日内）")
    void testMarketOpen_Friday1500() {
        LocalDate friday = getNextFriday();
        assertTrue(isMarketOpenTime(friday, LocalTime.of(15, 0)));
    }

    @Test
    @DisplayName("周五15:01 - 应该返回false（收盘后）")
    void testMarketOpen_Friday1501() {
        LocalDate friday = getNextFriday();
        assertFalse(isMarketOpenTime(friday, LocalTime.of(15, 1)));
    }

    @Test
    @DisplayName("周一12:00 - 应该返回true（午间交易时间）")
    void testMarketOpen_Monday1200() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(12, 0)));
    }

    @Test
    @DisplayName("边界测试 - 9:29:59 应该返回false（开盘前1秒）")
    void testMarketOpen_JustBeforeOpen() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(9, 29, 59)));
    }

    @Test
    @DisplayName("边界测试 - 9:30:01 应该返回true（开盘后1秒）")
    void testMarketOpen_JustAfterOpen() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(9, 30, 1)));
    }

    @Test
    @DisplayName("边界测试 - 14:59:59 应该返回true（收盘前1秒）")
    void testMarketOpen_JustBeforeClose() {
        LocalDate monday = getNextMonday();
        assertTrue(isMarketOpenTime(monday, LocalTime.of(14, 59, 59)));
    }

    @Test
    @DisplayName("边界测试 - 15:00:01 应该返回false（收盘后1秒）")
    void testMarketOpen_JustAfterClose() {
        LocalDate monday = getNextMonday();
        assertFalse(isMarketOpenTime(monday, LocalTime.of(15, 0, 1)));
    }

    // ========== 辅助方法 ==========

    /**
     * 获取下一个周一（如果今天就是周一，返回今天的下周一的日期用于测试会有问题，这里获取未来的周一）
     */
    private LocalDate getNextMonday() {
        LocalDate today = LocalDate.now();
        int daysUntilMonday = (DayOfWeek.MONDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilMonday == 0) {
            daysUntilMonday = 7; // 如果今天是周一，返回下周一
        }
        return today.plusDays(daysUntilMonday);
    }

    private LocalDate getNextSaturday() {
        LocalDate today = LocalDate.now();
        int daysUntilSaturday = (DayOfWeek.SATURDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilSaturday == 0) {
            daysUntilSaturday = 7; // 如果今天是周六，返回下周六
        }
        return today.plusDays(daysUntilSaturday);
    }

    private LocalDate getNextSunday() {
        LocalDate today = LocalDate.now();
        int daysUntilSunday = (DayOfWeek.SUNDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilSunday == 0) {
            daysUntilSunday = 7; // 如果今天是周日，返回下周日
        }
        return today.plusDays(daysUntilSunday);
    }

    private LocalDate getNextFriday() {
        LocalDate today = LocalDate.now();
        int daysUntilFriday = (DayOfWeek.FRIDAY.getValue() - today.getDayOfWeek().getValue() + 7) % 7;
        if (daysUntilFriday == 0) {
            daysUntilFriday = 7; // 如果今天是周五，返回下周五
        }
        return today.plusDays(daysUntilFriday);
    }
}
