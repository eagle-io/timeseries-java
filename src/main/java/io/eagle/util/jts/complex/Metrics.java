package io.eagle.util.jts.complex;

import com.fasterxml.jackson.annotation.JsonCreator;

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

    @Override
    public String getKey() {
        return METRICS_KEY;
    }
}
