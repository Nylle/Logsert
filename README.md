# Logsert

Logsert helps to test logging functionality by recording log-events during test-runs and providing easy assertions.

## Getting Started
```xml
<dependency>
    <groupId>com.github.nylle</groupId>
    <artifactId>logsert</artifactId>
    <version>2.0.0</version>
    <scope>test</scope>
</dependency>
```
[Maven Central](https://search.maven.org/artifact/com.github.nylle/logsert)

## Usage
```java
class SomethingThatLogsTest {

    @RegisterExtension
    LogRecorder logRecorder = new LogRecorder(SomethingThatLogs.class);

    @Test
    void fluentAssertionsAreConvenient() {
        var sut = new SomethingThatLogs();
        
        var expectedException = new RuntimeException("expected for test");

        sut.logInfoWithMdcAndException("message 1", Map.of("key", "value", "foo", "bar"), expectedException);
        sut.logInfo("message 2");
        sut.logInfo("message 3");

        LogAssertions.assertThat(logRecorder)
                .withMessage("message 1")
                .withLevel(Level.INFO)
                .withMdcEntry("foo", "bar")
                .withMdcEntry("key", "value")
                .withMdcEntries(Map.of("key", "value"))
                .withMdcEntries(Map.of("foo", "bar"))
                .withMdcEntriesExactly(Map.of("key", "value", "foo", "bar"))
                .withException(expectedException)
                .withException(RuntimeException.class)
                .withException(RuntimeException.class, "expected for test")
                .containsLogs(1)
                .withMessageContaing("message")
                .withLevel(Level.INFO)
                .containsLogs(3)
                .withMessage("foo")
                .containsLogs(0);
    }

    @Test
    void standardListAssertionsAreAlsoSupported() {
        var sut = new SomethingThatLogs();
        sut.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

        Assertions.assertThat(logRecorder.getLogEvents())
                .extracting("level", "message", "MDCPropertyMap", "throwableProxy.className", "throwableProxy.message")
                .contains(tuple(Level.INFO, "message", Map.of("key", "value"), RuntimeException.class.getName(), "expected for test"));
    }
}
```
