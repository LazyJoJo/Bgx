package com.stock.fund.domain.exception;

/**
 * 数据采集异常 用于封装数据采集过程中的错误信息
 */
public class DataCollectionException extends BusinessException {

    public DataCollectionException(String message) {
        super("DATA_COLLECTION_ERROR", message);
    }

    public DataCollectionException(String message, Throwable cause) {
        super("DATA_COLLECTION_ERROR", message);
        initCause(cause);
    }
}
