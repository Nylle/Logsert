[![Tests](https://github.com/Nylle/Logsert/workflows/test/badge.svg?branch=main)](https://github.com/Nylle/Logsert/actions?query=workflow%3ATest)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.nylle/logsert.svg?label=maven-central)](https://maven-badges.herokuapp.com/maven-central/com.github.nylle/logsert)
[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)

# Logsert

Logsert helps to test logging functionality by recording log-events during test-runs and providing easy assertions.

## Getting Started
```xml
<dependency>
    <groupId>com.github.nylle</groupId>
    <artifactId>logsert</artifactId>
    <version>1.2.0</version>
    <scope>test</scope>
</dependency>
```

## Usage
```java
class SomethingThatLogsTest {

    @RegisterExtension
    LogRecorder logRecorder = new LogRecorder(SomethingThatLogs.class);

    @Test
    void fluentAssertionsAreConvenient() {
        var sut = new SomethingThatLogs();
        
        var expectedException = new RuntimeException("expected for test");

        sut.logInfoWithMdcAndException("message", Map.of("key", "value", "foo", "bar"), expectedException);
        sut.logInfo("message 2");
        sut.logInfo("message 2");

        assertThat(logRecorder).containsLogs(3)
                .withMessage("message")
                .withMessageContaining("essa")
                .withLevel(Level.INFO)
                .withMdcEntry("foo", "bar")
                .withMdcEntry("key", "value")
                .withMdcEntries(Map.of("key", "value"))
                .withMdcEntries(Map.of("foo", "bar"))
                .withMdcEntriesExactly(Map.of("key", "value", "foo", "bar"))
                .withException(expectedException)
                .withException(RuntimeException.class)
                .withException(RuntimeException.class, "expected for test")
                .times(1)
                .withMessage("message 2")
                .withLevel(Level.INFO)
                .times(2);
    }

    @Test
    void standardListAssertionsAreAlsoSupported() {
        var sut = new SomethingThatLogs();
        sut.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

        assertThat(logRecorder.getLogEvents())
                .extracting("level", "message", "MDCPropertyMap", "throwableProxy.className", "throwableProxy.message")
                .contains(tuple(Level.INFO, "message", Map.of("key", "value"), RuntimeException.class.getName(), "expected for test"));
    }
}
```
