package com.github.nylle.logsert;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;

public class LoggerExtension implements BeforeEachCallback, AfterEachCallback {

    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    private final Logger logger;

    public LoggerExtension(Class<?> type) {
        this.logger = LoggerFactory.getLogger(type);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        listAppender.stop();
        listAppender.list.clear();
        ((ch.qos.logback.classic.Logger)logger).detachAppender(listAppender);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        ((ch.qos.logback.classic.Logger)logger).addAppender(listAppender);
        listAppender.start();
    }

    public List<ILoggingEvent> getLogEvents() {
        return listAppender.list;
    }
}
