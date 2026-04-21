package com.stock.fund.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * 数字工具类 提供数字格式化、计算、转换等常用功能
 */
@Slf4j
public class NumberUtils {

    // 默认精度
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    // 数字格式化器
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("#,##0.00%");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("¥#,##0.00");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.CHINA);

    /**
     * 保留指定小数位数（四舍五入）
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        if (value == null) {
            return null;
        }
        return value.setScale(scale, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 保留指定小数位数（指定舍入模式）
     */
    public static BigDecimal round(BigDecimal value, int scale, RoundingMode roundingMode) {
        if (value == null) {
            return null;
        }
        return value.setScale(scale, roundingMode);
    }

    /**
     * 保留两位小数（默认）
     */
    public static BigDecimal round(BigDecimal value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * 保留指定小数位数（double类型）
     */
    public static double round(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return round(bd, scale).doubleValue();
    }

    /**
     * 保留两位小数（double类型）
     */
    public static double round(double value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * 格化数字为千分位字符串
     */
    public static String formatNumber(Number number) {
        if (number == null) {
            return null;
        }
        return NUMBER_FORMAT.format(number);
    }

    /**
     * 格化为带两位小数的字符串
     */
    public static String formatDecimal(Number number) {
        if (number == null) {
            return null;
        }
        return DECIMAL_FORMAT.format(number);
    }

    /**
     * 格化为百分比字符串
     */
    public static String formatPercent(Number number) {
        if (number == null) {
            return null;
        }
        return PERCENT_FORMAT.format(number);
    }

    /**
     * 格化为百分比字符串（指定小数位数）
     */
    public static String formatPercent(Number number, int scale) {
        if (number == null) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#,##0." + "0".repeat(scale) + "%");
        return df.format(number);
    }

    /**
     * 格式化为货币字符串
     */
    public static String formatCurrency(Number amount) {
        if (amount == null) {
            return null;
        }
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * 安全的加法运算
     */
    public static BigDecimal add(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result.add(value);
            }
        }
        return result;
    }

    /**
     * 安全的减法运算
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend) {
        if (minuend == null) {
            minuend = BigDecimal.ZERO;
        }
        if (subtrahend == null) {
            subtrahend = BigDecimal.ZERO;
        }
        return minuend.subtract(subtrahend);
    }

    /**
     * 安全的乘法运算
     */
    public static BigDecimal multiply(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = BigDecimal.ONE;
        for (BigDecimal value : values) {
            if (value != null) {
                result = result.multiply(value);
            }
        }
        return result;
    }

    /**
     * 安全的除法运算
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return divide(dividend, divisor, DEFAULT_SCALE);
    }

    /**
     * 安全的除法运算（指定精度）
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        if (dividend == null) {
            dividend = BigDecimal.ZERO;
        }
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Divisor is null or zero, returning 0");
            return BigDecimal.ZERO;
        }
        return dividend.divide(divisor, scale, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 计算百分比变化
     */
    public static BigDecimal calculatePercentChange(BigDecimal currentValue, BigDecimal previousValue) {
        if (currentValue == null || previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal change = currentValue.subtract(previousValue);
        return change.multiply(BigDecimal.valueOf(100)).divide(previousValue, 2, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 计算增长率
     */
    public static double calculateGrowthRate(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return 0;
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    /**
     * 判断是否为正数
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否为负数
     */
    public static boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断是否为零
     */
    public static boolean isZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 两个BigDecimal值
     */
    public static int compare(BigDecimal value1, BigDecimal value2) {
        if (value1 == null && value2 == null) {
            return 0;
        }
        if (value1 == null) {
            return -1;
        }
        if (value2 == null) {
            return 1;
        }
        return value1.compareTo(value2);
    }

    /**
     * 获取最大值
     */
    public static BigDecimal max(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        BigDecimal max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null && (max == null || values[i].compareTo(max) > 0)) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * 获取最小值
     */
    public static BigDecimal min(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        BigDecimal min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null && (min == null || values[i].compareTo(min) < 0)) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * 将字符串转换为BigDecimal
     */
    public static BigDecimal toBigDecimal(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(str.trim());
        } catch (NumberFormatException e) {
            log.warn("Failed to convert string to BigDecimal: {}", str);
            return null;
        }
    }

    /**
     * 将Object转换为BigDecimal
     */
    public static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        return toBigDecimal(obj.toString());
    }
}