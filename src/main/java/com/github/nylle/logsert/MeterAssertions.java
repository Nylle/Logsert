package com.github.nylle.logsert;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Statistic;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.assertj.core.api.AbstractAssert;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

public class MeterAssertions extends AbstractAssert<MeterAssertions, MeterRegistry> {

    private static final String METER_SEPARATOR = ",\n  ";
    private Stream<Meter> candidates;
    private final ExpectedMeter expected = new ExpectedMeter();

    private MeterAssertions(MeterRegistry actual) {
        super(actual, MeterAssertions.class);
        this.candidates = actual.getMeters().stream();
    }

    public static MeterAssertions assertThat(MeterRegistry actual) {
        return new MeterAssertions(actual);
    }

    public MeterAssertions withName(String name) {
        this.candidates = this.candidates.filter(x -> x.getId().getName().equals(name));
        this.expected.setName(name);
        return this;
    }

    public MeterAssertions withTag(String key, String value) {
        this.candidates = this.candidates.filter(x -> x.getId().getTags().contains(Tag.of(key, value)));
        this.expected.addTag(key, value);
        return this;
    }

    public MeterAssertions ofType(Class<? extends Meter> type) {
        this.candidates = this.candidates.filter(x -> type.isAssignableFrom(x.getClass()));
        this.expected.setType(type.getSimpleName());
        return this;
    }

    public MeterAssertions containsCount(double count) {
        isNotNull();

        this.expected.setType(Counter.class.getSimpleName());
        this.expected.setMeasurements(List.of(Statistic.COUNT.name() + "=" + count));

        var meters = candidates
                .filter(x -> StreamSupport.stream(x.measure().spliterator(), false).allMatch(y -> y.getStatistic() == Statistic.COUNT && y.getValue() == count))
                .collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain counter:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsGauge(double value) {
        isNotNull();

        this.expected.setType(Gauge.class.getSimpleName());
        this.expected.setMeasurements(List.of(Statistic.VALUE.name() + "=" + value));

        var meters = candidates
                .filter(x -> StreamSupport.stream(x.measure().spliterator(), false).allMatch(y -> y.getStatistic() == Statistic.VALUE && y.getValue() == value))
                .collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain gauge:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsTimer(double count, double totalTime, double max) {
        isNotNull();

        this.expected.setType(Timer.class.getSimpleName());
        this.expected.setMeasurements(List.of(Statistic.COUNT.name() + "=" + count, Statistic.TOTAL_TIME.name() + "=" + totalTime, Statistic.MAX + "=" + max));

        var meters = candidates
                .filter(x -> StreamSupport.stream(x.measure().spliterator(), false).anyMatch(y -> y.getStatistic() == Statistic.COUNT && y.getValue() == count))
                .filter(x -> StreamSupport.stream(x.measure().spliterator(), false).anyMatch(y -> y.getStatistic() == Statistic.TOTAL_TIME && y.getValue() == totalTime))
                .filter(x -> StreamSupport.stream(x.measure().spliterator(), false).anyMatch(y -> y.getStatistic() == Statistic.MAX && y.getValue() == max))
                .collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain timer:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsMeasurement(double value) {
        isNotNull();

        this.expected.setMeasurements(List.of(String.valueOf(value)));

        var meters = candidates.filter(x -> StreamSupport.stream(x.measure().spliterator(), false).anyMatch(y -> y.getValue() == value)).collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsMeasurement() {
        isNotNull();

        this.expected.setMeasurements(List.of("*"));

        var meters = candidates.filter(x -> StreamSupport.stream(x.measure().spliterator(), false).findAny().isPresent()).collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsMeasurements(double... values) {
        isNotNull();

        this.expected.setMeasurements(DoubleStream.of(values).boxed().map(x -> String.valueOf(x)).collect(Collectors.toList()));

        var meters = candidates.filter(x -> StreamSupport.stream(x.measure().spliterator(), false)
                        .map(y -> String.valueOf(y.getValue()))
                        .collect(Collectors.toList())
                        .equals(DoubleStream.of(values).boxed().map(y -> String.valueOf(y)).collect(Collectors.toList())))
                .collect(Collectors.toList());

        if (meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain:\n  %s\nbut was not found",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format());
        }

        return new MeterAssertions(actual);
    }

    public MeterAssertions containsNoMeasurements() {
        isNotNull();

        this.expected.setMeasurements(List.of("*"));

        var meters = candidates.collect(Collectors.toList());

        if (!meters.isEmpty()) {
            failWithMessage("\nExpecting meters:\n  %s\nto contain no measurement for:\n  %s\nbut found:\n  %s",
                    actual.getMeters().stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)),
                    expected.format(),
                    meters.stream().map(x -> expected.format(x)).collect(joining(METER_SEPARATOR)));
        }

        return new MeterAssertions(actual);
    }

    public static class ExpectedMeter {

        private String name;
        private String type;
        private List<Tag> tags;
        private List<String> measurements;

        public ExpectedMeter() {
        }

        public ExpectedMeter(String name, String type, List<Tag> tags, List<String> measurements) {
            this.name = name;
            this.type = type;
            this.tags = tags;
            this.measurements = measurements;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setMeasurements(List<String> measurements) {
            this.measurements = measurements;
        }

        public void addTag(String key, String value) {
            if (tags == null) {
                tags = new LinkedList<>();
            }
            tags.add(Tag.of(key, value));
        }

        public String format() {
            var ret = new LinkedList<String>();

            if (name != null) {
                ret.add("name=" + name);
            }
            if (tags != null) {
                ret.add("tags=[" + tags.stream().map(x -> x.getKey() + "=" + x.getValue()).collect(joining(", ")) + "]");
            }
            if (measurements != null) {
                ret.add("measurements=[" + String.join(", ", measurements) + "]");
            }
            if (type != null) {
                ret.add("type=" + type);
            }

            return "[" + String.join(", ", ret) + "]";
        }

        public String format(Meter meter) {
            return new ExpectedMeter(
                    name == null ? null : meter.getId().getName(),
                    type == null ? null : meter.getClass().getSimpleName(),
                    tags == null ? null : meter.getId().getTags(),
                    measurements == null ? null : StreamSupport.stream(meter.measure().spliterator(), false)
                            .map(x -> x.getStatistic().name() + "=" + x.getValue())
                            .collect(Collectors.toList())).format();
        }
    }
}