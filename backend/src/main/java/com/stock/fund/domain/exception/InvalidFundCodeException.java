package com.stock.fund.domain.exception;

/**
 * 无效基金代码异常 当基金代码无法获取有效的基金数据时抛出 与网络错误导致的 DataCollectionException
 * 不同，此类表示用户输入的代码格式无效或基金不存在
 */
public class InvalidFundCodeException extends BusinessException {

    public static final String CODE = "INVALID_FUND_CODE";

    public InvalidFundCodeException(String fundCode) {
        super(CODE, "无效的基金代码: " + fundCode + "，无法获取有效的基金数据，请确认是否为有效的基金代码");
    }

    public InvalidFundCodeException(String fundCode, String reason) {
        super(CODE, "无效的基金代码: " + fundCode + "，" + reason);
    }
}