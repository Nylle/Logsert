package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MessageAssert extends AbstractAssert<MessageAssert, List<ILoggingEvent>> {

    private final String logMessage;

    private List<ILoggingEvent> candidates;

    private Level level;
    private Map<String, String> mdc;
    private String throwableClass;
    private String throwableMessage;

    public MessageAssert(List<ILoggingEvent> actual, String message) {
        super(actual, MessageAssert.class);
        this.logMessage = message;
        this.candidates = actual;
    }

    public MessageAssert withLevel(Level level) {
        isNotNull();

        this.level = level;
        this.candidates = this.candidates.stream()
                .filter(x -> x.getLevel().equals(level))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    public MessageAssert withMdcEntry(String key, String value) {
        isNotNull();

        if (this.mdc == null) {
            this.mdc = new HashMap<>();
        }
        this.mdc.put(key, value);
        this.candidates = this.candidates.stream()
                .filter(x -> x.getMDCPropertyMap().containsKey(key) && x.getMDCPropertyMap().get(key).equals(value))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    public MessageAssert withMdcEntries(Map<String, String> mdcMap) {
        isNotNull();

        if (this.mdc == null) {
            this.mdc = new HashMap<>();
        }
        this.mdc.putAll(mdcMap);
        this.candidates = this.candidates.stream()
                .filter(x -> x.getMDCPropertyMap().entrySet().containsAll(mdcMap.entrySet()))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    public MessageAssert withMdcEntriesExactly(Map<String, String> mdcMap) {
        isNotNull();

        this.mdc = new HashMap<>(mdcMap);
        this.candidates = this.candidates.stream()
                .filter(x -> x.getMDCPropertyMap().size() == mdcMap.size() && x.getMDCPropertyMap().entrySet().containsAll(mdcMap.entrySet()))
                .collect(toList());

        if (candidates.isEmpty()) {
            failExactly();
        }
        return this;
    }

    public MessageAssert withException(Class<? extends Throwable> throwableClass) {
        isNotNull();

        this.throwableClass = throwableClass.getName();
        this.candidates = this.candidates.stream()
                .filter(x -> x.getThrowableProxy().getClassName().equals(throwableClass.getName()))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    public MessageAssert withException(Class<? extends Throwable> throwableClass, String message) {
        isNotNull();

        this.throwableClass = throwableClass.getName();
        this.throwableMessage = message;
        this.candidates = this.candidates.stream()
                .filter(x -> x.getThrowableProxy().getClassName().equals(throwableClass.getName()) && x.getThrowableProxy().getMessage().equals(message))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    public MessageAssert withException(Throwable throwable) {
        isNotNull();

        this.throwableClass = throwable.getClass().getName();
        this.throwableMessage = throwable.getMessage();
        this.candidates = this.candidates.stream()
                .filter(x -> reflectThrowable(x.getThrowableProxy()).equals(throwable))
                .collect(toList());

        if (candidates.isEmpty()) {
            fail();
        }
        return this;
    }

    private void fail() {
        var expected = format(level, logMessage, throwableClass, throwableMessage, mdc);
        failWithMessage("\nExpecting log:\n  %s\nto contain:\n  %s\nbut could not find the following:\n  %s",
                actual.stream().map(x -> format(x)).collect(toList()).toString().replace("], ", "],\n   "),
                List.of(expected),
                List.of(expected));
    }

    private void failExactly() {
        var expected = format(level, logMessage, throwableClass, throwableMessage, mdc);
        failWithMessage("\nExpecting log:\n  %s\nto contain exactly:\n  %s\nbut could not find the following:\n  %s",
                actual.stream().map(x -> format(x)).collect(toList()).toString().replace("], ", "],\n   "),
                List.of(expected),
                List.of(expected));
    }

    private String format(ILoggingEvent event) {
        return format(
                level == null ? null : event.getLevel(),
                logMessage == null ? null : event.getMessage(),
                throwableClass == null ? null : event.getThrowableProxy().getClassName(),
                throwableMessage == null ? null : event.getThrowableProxy().getMessage(),
                mdc == null ? null : event.getMDCPropertyMap()
        );
    }

    private static String format(Level level, String message, String exceptionClass, String exceptionMessage, Map<String, String> mdc) {
        String exceptionString = null;

        if(exceptionClass != null) {
            exceptionString = exceptionMessage != null
                    ? exceptionClass + ": " + exceptionMessage
                    : exceptionClass;
        }

        return "[" + Stream.of(level, message, exceptionString, mdc)
                .filter(x -> x != null)
                .map(x -> x.toString())
                .collect(Collectors.joining(", ")) + "]";
    }

    @SuppressWarnings("java:S3011")
    private static Throwable reflectThrowable(IThrowableProxy throwableProxy) {
        try {
            var throwable = Arrays.stream(throwableProxy.getClass().getDeclaredFields()).filter(x -> x.getName().equals("throwable")).findFirst().orElseThrow();
            throwable.setAccessible(true);
            return (Throwable) throwable.get(throwableProxy);
        } catch (Exception ex) {
            throw new LogAssertException(ex);
        }
    }
}
