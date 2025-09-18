package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ExpectedLoggingEvent {
    private Level level;
    private String message;
    private String throwableClass;
    private String throwableMessage;
    private Map<String, String> mdc;
    private boolean mdcExactly = false;

    public ExpectedLoggingEvent() {
    }

    public ExpectedLoggingEvent(Level level, String message, String throwableClass, String throwableMessage, Map<String, String> mdc) {
        this.level = level;
        this.message = message;
        this.throwableClass = throwableClass;
        this.throwableMessage = throwableMessage;
        this.mdc = mdc;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setThrowableClass(String throwableClass) {
        this.throwableClass = throwableClass;
    }

    public void setThrowableMessage(String throwableMessage) {
        this.throwableMessage = throwableMessage;
    }

    public void setMdc(Map<String, String> mdc) {
        this.mdc = mdc;
        this.mdcExactly = true;
    }

    public boolean isMdcExactly() {
        return mdcExactly;
    }

    public void putMdc(Map<String, String> mdc) {
        if (this.mdc == null) {
            this.mdc = new HashMap<>(mdc);
        } else {
            this.mdc.putAll(mdc);
        }
    }

    public String format() {
        var exceptionString = Optional.ofNullable(throwableClass)
                .map(x -> throwableMessage != null ? throwableClass + ": " + throwableMessage : throwableClass)
                .orElse(null);

        return "[" + Stream.of(level, message, exceptionString, mdc)
                .filter(x -> x != null)
                .map(x -> x.toString())
                .collect(Collectors.joining(", ")) + "]";
    }

    public String format(ILoggingEvent event) {
        return new ExpectedLoggingEvent(
                level == null ? null : event.getLevel(),
                message == null ? null : event.getMessage(),
                throwableClass == null ? null : event.getThrowableProxy().getClassName(),
                throwableMessage == null ? null : event.getThrowableProxy().getMessage(),
                mdc == null ? null : event.getMDCPropertyMap()).format();
    }
}
