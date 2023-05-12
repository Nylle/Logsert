package com.github.nylle.logsert;

import org.assertj.core.api.AbstractAssert;

import ch.qos.logback.classic.Level;

import java.util.List;
import java.util.stream.Collectors;

public class LogAssert extends AbstractAssert<LogAssert, LoggerExtension> {
    private static final String FAIL_MESSAGE = "\nExpecting log:\n  %s\nto contain:\n  %s\nbut could not find the following:\n  %s";

    public LogAssert(LoggerExtension actual) {
        super(actual, LogAssert.class);
    }

    public static LogAssert assertThat(LoggerExtension actual) {
        return new LogAssert(actual);
    }

    public MessageAssert containsMessage(String message) {
        isNotNull();

        var candidates = actual.getLogEvents().stream().filter(x -> x.getMessage().equals(message)).collect(Collectors.toList());

        if(candidates.isEmpty()) {
            failWithMessage(FAIL_MESSAGE,
                    actual.getLogEvents().stream().map(x -> x.getMessage()).collect(Collectors.toList()),
                    List.of(message),
                    List.of(message));
        }
        return new MessageAssert(candidates, message);
    }

    private static String formatMessageAndLevel(String message, Level level) {
        return "[" + level + ", " + message + "]";
    }
}

