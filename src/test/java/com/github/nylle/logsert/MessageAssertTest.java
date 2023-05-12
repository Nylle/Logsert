package com.github.nylle.logsert;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import ch.qos.logback.classic.Level;

import java.util.Map;

import static com.github.nylle.logsert.LogAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MessageAssertTest {
    @RegisterExtension
    LoggerExtension sut = new LoggerExtension(SomethingThatLogs.class);

    @Nested
    class WithLevel {

        @Test
        void hasLevel() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThat(sut).containsMessage("message").withLevel(Level.INFO);
        }

        @Test
        void levelNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withLevel(Level.WARN))
                    .withMessageContaining("Expecting log:\n  [[INFO, message]]\n")
                    .withMessageContaining("to contain:\n  [[WARN, message]]\n")
                    .withMessageContaining("but could not find the following:\n  [[WARN, message]]");
        }
    }

    @Nested
    class WithMdc {

        @Test
        void hasMdc() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).containsMessage("message").withMdcEntry("key", "value");
        }

        @Test
        void mdcEntryNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "otherKey", "otherValue");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withMdcEntry("key", "value"))
                    .withMessageContaining("Expecting log:\n  [[message, {otherKey=otherValue}]]\n")
                    .withMessageContaining("to contain:\n  [[message, {key=value}]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message, {key=value}]]");
        }
    }

    @Nested
    class WithException {

        @Test
        void hasException() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThat(sut).containsMessage("message").withException(RuntimeException.class);
        }

        @Test
        void exceptionNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withException(Exception.class))
                    .withMessageContaining("Expecting log:\n  [[message, java.lang.RuntimeException]]\n")
                    .withMessageContaining("to contain:\n  [[message, java.lang.Exception]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message, java.lang.Exception]]");
        }

        @Test
        void hasExceptionAndMessage() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThat(sut).containsMessage("message").withException(RuntimeException.class, "expected for test");
        }

        @Test
        void exceptionDoesNotMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withException(Exception.class, "expected for test"))
                    .withMessageContaining("Expecting log:\n  [[message, java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[message, java.lang.Exception: expected for test]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message, java.lang.Exception: expected for test]]");
        }

        @Test
        void exceptionMessageDoesNotMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withException(RuntimeException.class, "other message"))
                    .withMessageContaining("Expecting log:\n  [[message, java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[message, java.lang.RuntimeException: other message]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message, java.lang.RuntimeException: other message]]");
        }

        @Test
        void hasExceptionInstance() {
            var exception = new RuntimeException("expected for test");

            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), exception);

            assertThat(sut).containsMessage("message").withException(exception);
        }

        @Test
        void exceptionInstanceNotFound() {
            var exception = new RuntimeException("expected for test");

            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), exception);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsMessage("message").withException(new IllegalStateException("expected for test")))
                    .withMessageContaining("Expecting log:\n  [[message, java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[message, java.lang.IllegalStateException: expected for test]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message, java.lang.IllegalStateException: expected for test]]");
        }

    }

    @Test
    void end2end() {
        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value", "foo", "bar"), new RuntimeException("expected for test"));
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key2", "value2"), new RuntimeException("expected for test"));
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test2"));

        assertThat(sut).containsMessage("message")
                .withMdcEntry("foo", "bar")
                .withMdcEntry("key", "value")
                .withException(RuntimeException.class, "expected for test")
                .withLevel(Level.INFO);

//        assertThat(sut).containsMessage("message")
//                .withMdcEntry("key2", "value2")
//                .withException(RuntimeException.class, "expected for test")
//                .withLevel(Level.WARN);
    }

}
