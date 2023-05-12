package com.github.nylle.logsert;

import org.assertj.core.api.AbstractAssert;

public class LogAssert extends AbstractAssert<LogAssert, LoggerExtension> {

    public LogAssert(LoggerExtension actual) {
        super(actual, LogAssert.class);
    }

    public static LogAssert assertThat(LoggerExtension actual) {
        return new LogAssert(actual);
    }

    public MessageAssert containsLogs() {
        isNotNull();

        var candidates = actual.getLogEvents();

        if(candidates.isEmpty()) {
            failWithMessage("\nExpecting log:\n  %s\nto contain entries\nbut could not find any entry",
                    actual.getLogEvents());
        }
        return new MessageAssert(candidates);
    }

    public MessageAssert containsLogs(int count) {
        isNotNull();

        var candidates = actual.getLogEvents();

        if(candidates.size() != count) {
            failWithMessage("\nExpecting log:\n  %s\nto contain %s entries\nbut found %s entries",
                    actual.getLogEvents(),
                    count,
                    candidates.size());
        }
        return new MessageAssert(candidates);
    }
}
