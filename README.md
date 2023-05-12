# Logsert

Logsert helps to test logging functionality by recording log-events during test-runs and providing easy assertions.

## Example Usage
```java
class SomethingThatLogsTest {
    
    @RegisterExtension
    LoggerExtension logger = new LoggerExtension(SomethingThatLogs.class);

    @Test
    void fluentAssertionsAreConvenient() {
        var sut = new SomethingThatLogs();
        
        sut.logInfoWithMdcAndException("message", Map.of("key", "value", "foo", "bar"), new RuntimeException("expected for test"));

        assertThat(logger).containsMessage("message")
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

    @Test
    void standardListAssertionsAreAlsoSupported() {
        var sut = new SomethingThatLogs();
        sut.logInfoWithMdcAndException("message", Map.of("key", "value"), new RuntimeException("expected for test"));

        assertThat(logger.getLogEvents())
                .extracting("level", "message", "MDCPropertyMap", "throwableProxy.className", "throwableProxy.message")
                .contains(tuple(Level.INFO, "message", Map.of("key", "value"), RuntimeException.class.getName(), "expected for test"));
    }
}
```
