package com.github.nylle.logsert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import ch.qos.logback.classic.Level;

import static org.assertj.core.api.Assertions.assertThat;


class LoggerExtensionTest {

    @RegisterExtension
    LoggerExtension sut = new LoggerExtension(SomethingThatLogs.class);

    @Test
    void getEvents() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfo("message");

        assertThat(sut.getLogEvents()).hasSize(1);
        assertThat(sut.getLogEvents().get(0).getMessage()).isEqualTo("message");
        assertThat(sut.getLogEvents().get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(sut.getLogEvents().get(0).getMDCPropertyMap()).isEmpty();
    }

    @Test
    void getEventsWithMdc() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdc("message", "key", "value");

        assertThat(sut.getLogEvents()).hasSize(1);
        assertThat(sut.getLogEvents().get(0).getMessage()).isEqualTo("message");
        assertThat(sut.getLogEvents().get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(sut.getLogEvents().get(0).getMDCPropertyMap()).containsEntry("key", "value");
    }

}
