package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import org.assertj.core.api.AbstractAssert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class LogAssertions extends AbstractAssert<LogAssertions, LogRecorder> {

    private Stream<ILoggingEvent> candidates;
    private final ExpectedLoggingEvent expected = new ExpectedLoggingEvent();

    private LogAssertions(LogRecorder actual) {
        super(actual, LogAssertions.class);
        this.candidates = actual.getLogEvents().stream();
    }

    public static LogAssertions assertThat(LogRecorder actual) {
        return new LogAssertions(actual);
    }

    public LogAssertions withMessage(String message) {
        this.candidates = this.candidates.filter(x -> x.getFormattedMessage().equals(message));
        this.expected.setMessage(message);
        return this;
    }

    public LogAssertions withMessageContaining(String message) {
        this.candidates = this.candidates.filter(x -> x.getFormattedMessage().contains(message));
        this.expected.setMessage("*" + message + "*");
        return this;
    }

    public LogAssertions withLevel(Level level) {
        this.candidates = this.candidates.filter(x -> x.getLevel().equals(level));
        this.expected.setLevel(level);
        return this;
    }

    public LogAssertions withMdcEntry(String key, String value) {
        return withMdcEntries(Map.of(key, value));
    }

    public LogAssertions withMdcEntries(Map<String, String> mdcMap) {
        this.candidates = this.candidates.filter(x -> x.getMDCPropertyMap().entrySet().containsAll(mdcMap.entrySet()));
        this.expected.putMdc(mdcMap);
        return this;
    }

    public LogAssertions withMdcEntriesExactly(Map<String, String> mdcMap) {
        this.candidates = this.candidates.filter(x -> x.getMDCPropertyMap().size() == mdcMap.size() && x.getMDCPropertyMap().entrySet().containsAll(mdcMap.entrySet()));
        this.expected.setMdc(new HashMap<>(mdcMap));
        return this;
    }

    public LogAssertions withException(Class<? extends Throwable> throwableClass) {
        this.candidates = this.candidates.filter(x -> x.getThrowableProxy().getClassName().equals(throwableClass.getName()));
        this.expected.setThrowableClass(throwableClass.getName());
        return this;
    }

    public LogAssertions withException(Class<? extends Throwable> throwableClass, String message) {
        this.candidates = this.candidates.filter(x -> x.getThrowableProxy().getClassName().equals(throwableClass.getName()) && x.getThrowableProxy().getMessage().equals(message));
        this.expected.setThrowableClass(throwableClass.getName());
        this.expected.setThrowableMessage(message);
        return this;
    }

    public LogAssertions withException(Throwable throwable) {
        this.candidates = this.candidates.filter(x -> reflectThrowable(x.getThrowableProxy()).equals(throwable));
        this.expected.setThrowableClass(throwable.getClass().getName());
        this.expected.setThrowableMessage(throwable.getMessage());
        return this;
    }

    public LogAssertions containsLogs() {
        isNotNull();

        if (this.candidates.findAny().isEmpty()) {
            failWithMessage("\nExpecting log:\n  %s\nto contain%s:\n  %s\nat least once but was not found",
                    actual.getLogEvents().stream().map(x -> expected.format(x)).collect(toList()).toString().replace("], ", "],\n   "),
                    expected.isMdcExactly() ? " exactly" : "",
                    List.of(expected.format()));
        }

        return new LogAssertions(actual);
    }

    public LogAssertions containsLogs(int count) {
        isNotNull();

        var candidateCount = this.candidates.count();
        if (candidateCount != count) {
            failWithMessage("\nExpecting log:\n  %s\nto contain%s:\n  %s\n%s times but found %s times",
                    actual.getLogEvents().stream().map(x -> expected.format(x)).collect(toList()).toString().replace("], ", "],\n   "),
                    expected.isMdcExactly() ? " exactly" : "",
                    List.of(expected.format()),
                    count,
                    candidateCount);
        }

        return new LogAssertions(actual);
    }

    @SuppressWarnings({"java:S3011", "java:S112"})
    private static Throwable reflectThrowable(IThrowableProxy throwableProxy) {
        try {
            var throwable = Arrays.stream(throwableProxy.getClass().getDeclaredFields()).filter(x -> x.getName().equals("throwable")).findFirst().orElseThrow();
            throwable.setAccessible(true);
            return (Throwable) throwable.get(throwableProxy);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}