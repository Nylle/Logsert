package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;


class LogRecorderTest {

    @Nested
    class ByClass {

        @RegisterExtension
        LogRecorder sut = new LogRecorder(SomethingThatLogs.class);

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

    @Nested
    class ByName {

        @RegisterExtension
        LogRecorder sut = new LogRecorder(SomethingThatLogs.class.getName());

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
}
