package io.eagle.util.jts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.stream.Collectors;

public class Metrics extends ComplexValue<List<Double>> {
    public static final String METRICS_KEY = "$metrics";

    @JsonCreator
    public Metrics(List<Number> value) {
        super(value.stream()
                .map((v) -> v.doubleValue())
                .collect(Collectors.toList()));
    }

    public void add(Double value) {
        this.value.add(value);
    }

    public int size() {
        return this.value.size();
    }

    public Double get(int index) {
        return this.value.get(index);
    }

    @Override
    public String getKey() {
        return METRICS_KEY;
    }

    @Override
    public int compareTo(ComplexValue<List<Double>> other) {
        return ComparisonChain.start()
                .compare(this.value, other.value, Ordering.natural().lexicographical())
                .result();
    }
}
