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
    void containsMessage() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("message");

        assertThat(sut).containsMessage("message");
    }

    @Test
    void messageNotFound() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("other");

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> assertThat(sut).containsMessage("message"))
                .withMessageContaining("Expecting log:\n  [other]\n")
                .withMessageContaining("to contain:\n  [message]\n")
                .withMessageContaining("but could not find the following:\n  [message]");
    }

    @Test
    void standardListAssertionIsSupported() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

        assertThat(sut.getLogEvents()).extracting("level", "message", "MDCPropertyMap", "throwableProxy.className", "throwableProxy.message")
                .contains(tuple(Level.INFO, "message", Map.of("key", "value"), RuntimeException.class.getName(), "expected for test"));
    }
}
