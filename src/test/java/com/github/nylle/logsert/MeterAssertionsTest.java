package com.github.nylle.logsert;

import com.github.nylle.javafixture.annotations.fixture.TestWithFixture;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Nested;

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
                    .withMessageContaining("[name=other, measurements=[1.0]]\n")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("[name=" + name + ", measurements=[1.0]]\n")
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
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("[tags=[key1=value1, key2=value2], measurements=[1.0]]\n")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("[tags=[" + key + "=" + value + "], measurements=[1.0]]\n")
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
        void failsWhenMeasurementDoesNotMatch() {
            meterRegistry.gauge("gauge", Tags.of(Tag.of("key1", "value1")), 14);

            assertThatExceptionOfType(AssertionError.class)
                    .isThrownBy(() -> MeterAssertions.assertThat(meterRegistry)
                            .withName("gauge")
                            .ofType(Gauge.class)
                            .containsMeasurement(1))
                    .withMessageContaining("Expecting meters:\n")
                    .withMessageContaining("[name=gauge, measurements=[14.0], type=DefaultGauge]\n")
                    .withMessageContaining("to contain:\n")
                    .withMessageContaining("[name=gauge, measurements=[1.0], type=Gauge]\n")
                    .withMessageContaining("but was not found");
        }
    }
}