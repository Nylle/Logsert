package com.github.nylle.logsert;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static com.github.nylle.logsert.LogAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MessageAssertTest {
    @RegisterExtension
    LogRecorder sut = new LogRecorder(SomethingThatLogs.class);

    @Nested
    class WithMessage {

        @Test
        void hasMessage() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThat(sut).containsLogs().withMessage("message");
        }

        @Test
        void inlinesArguments() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithArguments("Hello {}!", "world");

            assertThat(sut).containsLogs().withMessage("Hello world!");
        }

        @Test
        void messageNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withMessage("message"))
                    .withMessageContaining("Expecting log:\n  [[other message]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message]]");
        }
    }

    @Nested
    class WithMessageContaining {

        @Test
        void hasMessageContaining() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other message, but long");

            assertThat(sut).containsLogs().withMessageContaining("message");
        }

        @Test
        void inlinesArguments() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithArguments("Hello {}!", "world");

            assertThat(sut).containsLogs().withMessageContaining("lo wor");
        }

        @Test
        void messageNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("other text");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withMessageContaining("message"))
                    .withMessageContaining("Expecting log:\n  [[other text]]\n")
                    .withMessageContaining("to contain:\n  [[message]]\n")
                    .withMessageContaining("but could not find the following:\n  [[message]]");
        }
    }

    @Nested
    class WithLevel {

        @Test
        void hasLevel() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThat(sut).containsLogs().withLevel(Level.INFO);
        }

        @Test
        void levelNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfo("message");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withLevel(Level.WARN))
                    .withMessageContaining("Expecting log:\n  [[INFO]]\n")
                    .withMessageContaining("to contain:\n  [[WARN]]\n")
                    .withMessageContaining("but could not find the following:\n  [[WARN]]");
        }
    }

    @Nested
    class WithMdcEntry {

        @Test
        void hasMdc() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).containsLogs().withMdcEntry("key", "value");
        }

        @Test
        void mdcEntryNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "otherKey", "otherValue");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withMdcEntry("key", "value"))
                    .withMessageContaining("Expecting log:\n  [[{otherKey=otherValue}]]\n")
                    .withMessageContaining("to contain:\n  [[{key=value}]]\n")
                    .withMessageContaining("but could not find the following:\n  [[{key=value}]]");
        }
    }

    @Nested
    class WithMdcEntries {

        @Test
        void hasMdcEntries() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).containsLogs().withMdcEntries(Map.of("key", "value"));
        }

        @Test
        void mdcEntriesNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "otherKey", "otherValue");

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withMdcEntries(Map.of("key", "value")))
                    .withMessageContaining("Expecting log:\n  [[{otherKey=otherValue}]]\n")
                    .withMessageContaining("to contain:\n  [[{key=value}]]\n")
                    .withMessageContaining("but could not find the following:\n  [[{key=value}]]");
        }
    }

    @Nested
    class WithMdcEntriesExactly {

        @Test
        void hasExactlyMdcEntries() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdc("message", "key", "value");

            assertThat(sut).containsLogs().withMdcEntriesExactly(Map.of("key", "value"));
        }

        @Test
        void mdcEntriesNotExactlyMatched() {
            var somethingThatLogs = new SomethingThatLogs();
            var mdcMap = Map.of("key", "value", "otherKey", "otherValue");
            somethingThatLogs.logInfoWithMdc("message", mdcMap);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withMdcEntriesExactly(Map.of("key", "value")))
                    .withMessageContaining("Expecting log:\n  [[" + mdcMap + "]]\n")
                    .withMessageContaining("to contain exactly:\n  [[{key=value}]]\n")
                    .withMessageContaining("but could not find the following:\n  [[{key=value}]]");
        }
    }

    @Nested
    class WithException {

        @Test
        void hasException() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThat(sut).containsLogs().withException(RuntimeException.class);
        }

        @Test
        void exceptionNotFound() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withException(Exception.class))
                    .withMessageContaining("Expecting log:\n  [[java.lang.RuntimeException]]\n")
                    .withMessageContaining("to contain:\n  [[java.lang.Exception]]\n")
                    .withMessageContaining("but could not find the following:\n  [[java.lang.Exception]]");
        }

        @Test
        void hasExceptionAndMessage() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThat(sut).containsLogs().withException(RuntimeException.class, "expected for test");
        }

        @Test
        void exceptionDoesNotMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withException(Exception.class, "expected for test"))
                    .withMessageContaining("Expecting log:\n  [[java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[java.lang.Exception: expected for test]]\n")
                    .withMessageContaining("but could not find the following:\n  [[java.lang.Exception: expected for test]]");
        }

        @Test
        void exceptionMessageDoesNotMatch() {
            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withException(RuntimeException.class, "other message"))
                    .withMessageContaining("Expecting log:\n  [[java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[java.lang.RuntimeException: other message]]\n")
                    .withMessageContaining("but could not find the following:\n  [[java.lang.RuntimeException: other message]]");
        }

        @Test
        void hasExceptionInstance() {
            var exception = new RuntimeException("expected for test");

            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), exception);

            assertThat(sut).containsLogs().withException(exception);
        }

        @Test
        void exceptionInstanceNotFound() {
            var exception = new RuntimeException("expected for test");

            var somethingThatLogs = new SomethingThatLogs();
            somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value"), exception);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> assertThat(sut).containsLogs().withException(new IllegalStateException("expected for test")))
                    .withMessageContaining("Expecting log:\n  [[java.lang.RuntimeException: expected for test]]\n")
                    .withMessageContaining("to contain:\n  [[java.lang.IllegalStateException: expected for test]]\n")
                    .withMessageContaining("but could not find the following:\n  [[java.lang.IllegalStateException: expected for test]]");
        }
    }

    @Test
    void demonstrateAllAssertions() {
        var expectedException = new RuntimeException("expected for test");

        var somethingThatLogs = new SomethingThatLogs();
        somethingThatLogs.logInfoWithMdcAndException("message", Map.of("key", "value", "foo", "bar"), expectedException);

        assertThat(sut).containsLogs()
                .withMessage("message")
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
                .withException(RuntimeException.class, "expected for test");
    }
}
