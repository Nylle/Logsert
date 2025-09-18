package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static com.github.nylle.logsert.LogAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LogAssertionsTest {

    @RegisterExtension
    LogRecorder sut = new LogRecorder(SomethingThatLogs.class);

    @Nested
    class ContainsLogs {

        @Test
        void succeedsWhenAnyNumberOfLogsMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");
            somethingThatLogs.logInfo("message");

            assertThat(sut).withMessage("message").containsLogs();
        }

        @Test
        void failsWhenNoLogsMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithArguments("other {}", "message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMessage("message").containsLogs())
                    .withMessageContaining("Expecting log:\n  [[other message]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("at least once but was not found");
        }

        @Test
        void succeedsWhenExpectedNumberOfLogsMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");
            somethingThatLogs.logInfo("message");

            assertThat(sut).withMessage("message").containsLogs(2);
        }

        @Test
        void failsWhenExpectedNumberOfLogsIsHigher() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");
            somethingThatLogs.logInfo("message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMessage("message").containsLogs(3))
                    .withMessageContaining("Expecting log:\n  [[message],\n   [message]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("3 times but found 2 times");
        }

        @Test
        void failsWhenExpectedNumberOfLogsIsLower() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");
            somethingThatLogs.logInfo("message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMessage("message").containsLogs(1))
                    .withMessageContaining("Expecting log:\n  [[message],\n   [message]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("1 times but found 2 times");
        }
    }

    @Nested
    class WithMessage {

        @Test
        void succeedsWhenMessageMatches() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThat(sut).withMessage("message").containsLogs();
        }

        @Test
        void inlinesArguments() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithArguments("Hello {}!", "world");

            assertThat(sut).withMessage("Hello world!").containsLogs();
        }

        @Test
        void failsWhenMessageWasNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMessage("message").containsLogs())
                    .withMessageContaining("Expecting log:\n  [[other message]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Nested
    class WithMessageContaining {

        @Test
        void succeedsWhenStringIsContainedInMessage() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other message, but long");

            assertThat(sut).withMessageContaining("message").containsLogs();
        }

        @Test
        void inlinesArguments() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithArguments("Hello {}!", "world");

            assertThat(sut).withMessageContaining("lo wor").containsLogs();
        }

        @Test
        void failsWhenMessageIsNotContained() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other text");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMessageContaining("message").containsLogs())
                    .withMessageContaining("Expecting log:\n  [[other text]]\n")
                    .withMessageContaining("to contain:\n  [[*message*]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Nested
    class WithLevel {

        @Test
        void succeedsWhenLevelMatches() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThat(sut).withLevel(Level.INFO).containsLogs();
        }

        @Test
        void failsWhenLevelNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withLevel(Level.WARN).containsLogs())
                    .withMessageContaining("Expecting log:\n  [[INFO]]\n")
                    .withMessageContaining("to contain:\n  [[WARN]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Nested
    class WithMdcEntry {

        @Test
        void succeedsWhenKeyValuePairIsFoundInMdc() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).withMdcEntry("key", "value").containsLogs();
        }

        @Test
        void failsWhenKeyValuePairNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "otherKey", "otherValue");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMdcEntry("key", "value").containsLogs())
                    .withMessageContaining("Expecting log:\n  [[{otherKey=otherValue}]]\n")
                    .withMessageContaining("to contain:\n  [[{key=value}]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Nested
    class WithMdcEntries {

        @Test
        void succeedsWhenAllEntriesAreFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).withMdcEntries(Map.of("key", "value")).containsLogs();
        }

        @Test
        void failsWhenNotAllEntriesAreFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "otherKey", "otherValue");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMdcEntries(Map.of("key", "value")).containsLogs())
                    .withMessageContaining("Expecting log:\n  [[{otherKey=otherValue}]]\n")
                    .withMessageContaining("to contain:\n  [[{key=value}]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Nested
    class WithMdcEntriesExactly {

        @Test
        void succeedsWhenOnlyEntriesAreFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).withMdcEntriesExactly(Map.of("key", "value")).containsLogs();
        }

        @Test
        void failsWhenMoreEntriesAreFound() {
            var somethingThatLogs = new SomethingThatLogs();
            var mdcMap = Map.of("key", "value", "otherKey", "otherValue");
            somethingThatLogs.logInfoWithMdc("message", mdcMap);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).withMdcEntriesExactly(Map.of("key", "value")).containsLogs())
                    .withMessageContaining("Expecting log:\n  [[" + mdcMap + "]]\n")
                    .withMessageContaining("to contain exactly:\n  [[{key=value}]]\n")
                    .withMessageContaining("at least once but was not found");
        }
    }

    @Test
    void demonstrateAllAssertions() {
        var expectedException = new RuntimeException("expected for test");

        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdcAndException("message 1", Map.of("key", "value", "foo", "bar"), expectedException);
        somethingThatLogs.logInfo("message 2");

        assertThat(sut)
                .withMessage("message 1")
                .withMessageContaining("essa")
                .withLevel(Level.INFO)
                .withMdcEntry("foo", "bar")
                .withMdcEntry("key", "value")
                .withMdcEntries(Map.of("key", "value"))
                .withMdcEntries(Map.of("foo", "bar"))
                .withMdcEntries(Map.of("key", "value", "foo", "bar"))
                .withMdcEntriesExactly(Map.of("key", "value", "foo", "bar"))
                .withException(expectedException)
                .withException(RuntimeException.class)
                .withException(RuntimeException.class, "expected for test")
                .containsLogs()
                .withMessage("message 2")
                .containsLogs(1)
                .withMessageContaining("message")
                .withLevel(Level.INFO)
                .containsLogs(2)
                .withMessage("message 3")
                .containsLogs(0);
    }
}