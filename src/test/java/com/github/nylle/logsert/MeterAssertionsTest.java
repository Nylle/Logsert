package com.github.nylle.logsert;

import com.github.nylle.javafixture.annotations.fixture.TestWithFixture;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MeterAssertionsTest {

    MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Nested
    class WithName {

        @TestWithFixture
        void succeedsWhenNameWasFound(String name) {
            meterRegistry.counter(name, Tags.of(Tag.of("key1", "value1"))).increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withName(name)
                    .containsMeasurement(1.0);
        }

        @TestWithFixture
        void failsWhenNameWasNotFound(String name) {
            meterRegistry.counter("other", Tags.of(Tag.of("key1", "value1"))).increment();

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry).withName(name).containsMeasurement(1.0))
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("  [name=other, measurements=[COUNT=1.0]]")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("  [name=" + name + ", measurements=[1.0]]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class WithTag {

        @TestWithFixture
        void succeedsWhenTagWasFound(String name, String key, String value) {
            meterRegistry.counter(name, Tags.of(Tag.of(key, value), Tag.of("key2", "value2"))).increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withTag(key, value)
                    .containsMeasurement(1.0);
        }

        @TestWithFixture
        void succeedsWhenAllTagsWereFound(String name, String key, String value) {
            meterRegistry.counter(name, Tags.of(Tag.of(key, value), Tag.of("key2", "value2"))).increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withTag(key, value)
                    .withTag("key2", "value2")
                    .containsMeasurement(1.0);
        }

        @TestWithFixture
        void failsWhenTagWasNotFound(String name, String key, String value) {
            meterRegistry.counter(name, Tags.of(Tag.of("key1", "value1"), Tag.of("key2", "value2"))).increment();

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry).withTag(key, value).containsMeasurement(1.0))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [tags=[key1=value1, key2=value2], measurements=[COUNT=1.0]]")
                    .withMessageContaining("to contain:")
                    .withMessageContaining("  [tags=[" + key + "=" + value + "], measurements=[1.0]]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsCount {

        @TestWithFixture
        void succeedsWhenCountMatches(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withName("counter")
                    .containsCount(2.0);
        }

        @Test
        void failsWhenCountDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("counter")
                            .containsCount(1))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge],")
                    .withMessageContaining("  [name=counter, measurements=[COUNT=2.0], type=CumulativeCounter]")
                    .withMessageContaining("to contain counter:")
                    .withMessageContaining("  [name=counter, measurements=[COUNT=1.0], type=Counter]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsGauge {

        @TestWithFixture
        void succeedsWhenValueMatches(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withName("gauge")
                    .containsGauge(value);
        }

        @Test
        void failsWhenValueDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("gauge")
                            .containsGauge(1))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge],")
                    .withMessageContaining("  [name=counter, measurements=[COUNT=2.0], type=CumulativeCounter]")
                    .withMessageContaining("to contain gauge:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=1.0], type=Gauge]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsTimer {

        @TestWithFixture
        void succeedsWhenAllValuesMatch(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);
            timer.record(180, TimeUnit.SECONDS);

            MeterAssertions.assertThat(meterRegistry)
                    .withName("timer")
                    .containsTimer(2.0, 240.0, 180.0);
        }

        @Test
        void failsWhenCountDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("timer")
                            .containsTimer(2.0, 60.0, 60.0))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=1.0, TOTAL_TIME=60.0, MAX=60.0], type=CumulativeTimer]")
                    .withMessageContaining("to contain timer:")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=2.0, TOTAL_TIME=60.0, MAX=60.0], type=Timer]")
                    .withMessageContaining("but was not found");
        }

        @Test
        void failsWhenTotalTimeDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("timer")
                            .containsTimer(1.0, 180.0, 60.0))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=1.0, TOTAL_TIME=60.0, MAX=60.0], type=CumulativeTimer]")
                    .withMessageContaining("to contain timer:")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=1.0, TOTAL_TIME=180.0, MAX=60.0], type=Timer]")
                    .withMessageContaining("but was not found");
        }

        @Test
        void failsWhenMaxDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("timer")
                            .containsTimer(1.0, 60.0, 180.0))
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=1.0, TOTAL_TIME=60.0, MAX=60.0], type=CumulativeTimer]")
                    .withMessageContaining("to contain timer:")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=1.0, TOTAL_TIME=60.0, MAX=180.0], type=Timer]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsMeasurement {

        @TestWithFixture
        void succeedsWhenMeasurementMatches(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withName("counter")
                    .ofType(Counter.class)
                    .containsMeasurement(2.0)
                    .withName("gauge")
                    .ofType(Gauge.class)
                    .containsMeasurement(value);
        }

        @TestWithFixture
        void succeedsWhenAnyMeasurementMatches(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);
            var counter = meterRegistry.counter("counter", Tags.of(Tag.of("key1", "value1")));
            counter.increment();
            counter.increment();

            MeterAssertions.assertThat(meterRegistry)
                    .withName("counter")
                    .ofType(Counter.class)
                    .containsMeasurement()
                    .withName("gauge")
                    .ofType(Gauge.class)
                    .containsMeasurement();
        }

        @TestWithFixture
        void failsWhenMeasurementDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("gauge")
                            .ofType(Gauge.class)
                            .containsMeasurement(1))
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("  [name=gauge, measurements=[1.0], type=Gauge]")
                    .withMessageContaining("but was not found");
        }

        @TestWithFixture
        void failsWhenNoMeasurementMatches() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("counter")
                            .ofType(Counter.class)
                            .containsMeasurement())
                    .withMessageContaining("Expecting meters:")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("to contain:")
                    .withMessageContaining("  [name=counter, measurements=[*], type=Counter]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsMeasurements {

        @TestWithFixture
        void succeedsWhenAllMeasurementsMatch(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);
            timer.record(180, TimeUnit.SECONDS);

            MeterAssertions.assertThat(meterRegistry)
                    .withName("timer")
                    .ofType(Timer.class)
                    .containsMeasurements(2.0, 240.0, 180.0);
        }

        @Test
        void failsWhenAnyMeasurementDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);
            timer.record(180, TimeUnit.SECONDS);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("timer")
                            .ofType(Timer.class)
                            .containsMeasurements(2.0, 99999.0, 180.0))
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=2.0, TOTAL_TIME=240.0, MAX=180.0], type=CumulativeTimer]")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("  [name=timer, measurements=[2.0, 99999.0, 180.0], type=Timer]")
                    .withMessageContaining("but was not found");
        }
    }

    @Nested
    class ContainsNoMeasurements {

        @TestWithFixture
        void succeedsWhenNoMeasurementsMatch(int value) {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), value);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);
            timer.record(180, TimeUnit.SECONDS);

            MeterAssertions.assertThat(meterRegistry)
                    .withName("counter")
                    .ofType(Counter.class)
                    .containsNoMeasurements();
        }

        @Test
        void failsWhenAnyMeasurementMatches() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            var timer = meterRegistry.timer("timer", Tags.of(Tag.of("key1", "value1")));
            timer.record(60, TimeUnit.SECONDS);
            timer.record(180, TimeUnit.SECONDS);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("timer")
                            .ofType(Timer.class)
                            .containsNoMeasurements())
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("  [name=gauge, measurements=[VALUE=14.0], type=DefaultGauge]")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=2.0, TOTAL_TIME=240.0, MAX=180.0], type=CumulativeTimer]")
                    .withMessageContaining("to contain no measurement for:\n")
                    .withMessageContaining("  [name=timer, measurements=[*], type=Timer]")
                    .withMessageContaining("but found:")
                    .withMessageContaining("  [name=timer, measurements=[COUNT=2.0, TOTAL_TIME=240.0, MAX=180.0], type=CumulativeTimer]");
        }
    }
}