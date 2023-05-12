package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static com.github.nylle.logsert.LogAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;

class LogAssertTest {
    @RegisterExtension
    LoggerExtension sut = new LoggerExtension(SomethingThatLogs.class);

    @Test
    void containsLogs() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("message");

        assertThat(sut).containsLogs();
        assertThat(sut).containsLogs(1);
    }

    @Test
    void logsNotFound() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> assertThat(sut).containsLogs())
                .withMessageContaining("Expecting log:\n  []\n")
                .withMessageContaining("to contain entries\n")
                .withMessageContaining("but could not find any entry");
    }

    @Test
    void logCountMismatch() {
        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> assertThat(sut).containsLogs(1))
                .withMessageContaining("Expecting log:\n  []\n")
                .withMessageContaining("to contain 1 entries\n")
                .withMessageContaining("but found 0 entries");
    }

    @Test
    void standardListAssertionIsSupported() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

        assertThat(sut.getLogEvents()).extracting("level", "message", "MDCPropertyMap", "throwableProxy.className", "throwableProxy.message")
                .contains(tuple(Level.INFO, "message", Map.of("key", "value"), RuntimeException.class.getName(), "expected for test"));
    }
}
