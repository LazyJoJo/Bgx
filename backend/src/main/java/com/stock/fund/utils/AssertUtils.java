package com.stock.fund.utils;

import java.util.function.Supplier;

import org.springframework.util.Assert;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 断工具类 提供参数验证和业务逻辑断言功能
 */
@Slf4j
public class AssertUtils extends Assert {

    /**
     * 断言对象不为null
     */
    public static void notNull(Object object, String message) {
        Assert.notNull(object, message);
    }

    /**
     * 断言对象不为null（带自定义异常）
     */
    public static void notNull(Object object, RuntimeException exception) {
        if (object == null) {
            throw exception;
        }
    }

    /**
     * 断言字符串不为空
     */
    public static void notEmpty(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串不为空（带自定义异常）
     */
    public static void notEmpty(String text, RuntimeException exception) {
        if (text == null || text.trim().isEmpty()) {
            throw exception;
        }
    }

    /**
     * 断言集合不为空
     */
    public static void notEmpty(java.util.Collection<?> collection, String message) {
        if (CollUtil.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言集合不为空（带自定义异常）
     */
    public static void notEmpty(java.util.Collection<?> collection, RuntimeException exception) {
        if (CollUtil.isEmpty(collection)) {
            throw exception;
        }
    }

    /**
     * 断言Map不为空
     */
    public static void notEmpty(java.util.Map<?, ?> map, String message) {
        if (CollUtil.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言Map不为空（带自定义异常）
     */
    public static void notEmpty(java.util.Map<?, ?> map, RuntimeException exception) {
        if (CollUtil.isEmpty(map)) {
            throw exception;
        }
    }

    /**
     * 断言表达式为true
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言表达式为true（带自定义异常）
     */
    public static void isTrue(boolean expression, RuntimeException exception) {
        if (!expression) {
            throw exception;
        }
    }

    /**
     * 断言两个对象相等
     */
    public static void equals(Object obj1, Object obj2, String message) {
        if (obj1 == null ? obj2 != null : !obj1.equals(obj2)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言两个对象不相等
     */
    public static void notEquals(Object obj1, Object obj2, String message) {
        if (obj1 == null ? obj2 == null : obj1.equals(obj2)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数值大于0
     */
    public static void positive(Number number, String message) {
        if (number == null || number.doubleValue() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数值大于等于0
     */
    public static void nonNegative(Number number, String message) {
        if (number == null || number.doubleValue() < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数值在指定范围内
     */
    public static void inRange(Number number, Number min, Number max, String message) {
        if (number == null) {
            throw new IllegalArgumentException(message);
        }
        if (min != null && number.doubleValue() < min.doubleValue()) {
            throw new IllegalArgumentException(message);
        }
        if (max != null && number.doubleValue() > max.doubleValue()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串长度在指定范围内
     */
    public static void lengthInRange(String str, int min, int max, String message) {
        if (str == null) {
            throw new IllegalArgumentException(message);
        }
        int length = str.length();
        if (length < min || length > max) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言邮箱格式正确
     */
    public static void isEmail(String email, String message) {
        // 使用简单验证替代Hutool的isEmail方法
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言手机号格式正确
     */
    public static void isPhone(String phone, String message) {
        // 使用简单验证替代Hutool的isMobile方法
        if (phone == null || phone.length() != 11) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言为正数（BigDecimal）
     */
    public static void positive(java.math.BigDecimal number, String message) {
        if (number == null || number.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言为非负数（BigDecimal）
     */
    public static void nonNegative(java.math.BigDecimal number, String message) {
        if (number == null || number.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言日期时间不为null且不早于指定时间
     */
    public static void notBefore(java.time.LocalDateTime dateTime, java.time.LocalDateTime minDateTime,
            String message) {
        if (dateTime == null || dateTime.isBefore(minDateTime)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言日期时间不为null且不晚于指定时间
     */
    public static void notAfter(java.time.LocalDateTime dateTime, java.time.LocalDateTime maxDateTime, String message) {
        if (dateTime == null || dateTime.isAfter(maxDateTime)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言日期不为null且不在指定范围外
     */
    public static void dateInRange(java.time.LocalDate date, java.time.LocalDate minDate, java.time.LocalDate maxDate,
            String message) {
        if (date == null) {
            throw new IllegalArgumentException(message);
        }
        if (minDate != null && date.isBefore(minDate)) {
            throw new IllegalArgumentException(message);
        }
        if (maxDate != null && date.isAfter(maxDate)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言业务状态正确
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * 断言业务状态正确（带自定义异常）
     */
    public static void state(boolean expression, RuntimeException exception) {
        if (!expression) {
            throw exception;
        }
    }

    /**
     * 安全执行，捕获异常并返回默认值
     */
    public static <T> T safeExecute(Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Safe execution failed, returning default value", e);
            return defaultValue;
        }
    }

    /**
     * 安全执行，捕获异常并返回null
     */
    public static <T> T safeExecute(Supplier<T> supplier) {
        return safeExecute(supplier, null);
    }

    /**
     * 断言并抛出指定异常类型
     */
    public static void throwIf(boolean condition, Supplier<? extends RuntimeException> exceptionSupplier) {
        if (condition) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 断言并抛出指定异常
     */
    public static void throwException(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }
}