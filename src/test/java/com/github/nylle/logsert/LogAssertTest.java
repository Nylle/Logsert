package com.github.nylle.logsert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import ch.qos.logback.classic.Level;

import static com.github.nylle.logsert.LogAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LogAssertTest {
    @RegisterExtension
    LoggerExtension sut = new LoggerExtension(SomethingThatLogs.class);

    @Test
    void containsMessage() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("message");

        assertThat(sut).containsMessage("message").withLevel(Level.INFO);
    }

    @Test
    void messageNotFound() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("other");

        assertThatExceptionOfType(AssertionError.class)
                .isThrownBy(() -> assertThat(sut).containsMessage("message").withLevel(Level.INFO))
                .withMessageContaining("Expecting log:\n  [other]\n")
                .withMessageContaining("to contain:\n  [message]\n")
                .withMessageContaining("but could not find the following:\n  [message]");
    }
}
