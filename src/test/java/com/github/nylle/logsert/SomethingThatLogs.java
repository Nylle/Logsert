package com.github.nylle.logsert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public class SomethingThatLogs {
    private static final Logger logger = LoggerFactory.getLogger(SomethingThatLogs.class);

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logInfoWithArguments(String message, Object argument) {
        logger.info(message, argument);
    }

    public void logInfoWithMdc(String message, String key, String value) {
        MDC.put(key, value);
        logger.info(message);
        MDC.clear();
    }

    public void logInfoWithMdc(String message, Map<String, String> mdc) {
        mdc.forEach((key, value) -> MDC.put(key, value));
        logger.info(message);
        MDC.clear();
    }

    public void logInfoWithMdcAndException(String message, Map<String, String> mdc, Exception exception) {
        mdc.forEach((key, value) -> MDC.put(key, value));
        logger.info(message, exception);
        MDC.clear();
    }
}
