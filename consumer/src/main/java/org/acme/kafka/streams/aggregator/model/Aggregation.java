package org.acme.kafka.streams.aggregator.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RegisterForReflection
public class Aggregation {
    public int stationId;
    public String stationName;
    public double min = Double.MAX_VALUE;
    public double max = Double.MIN_VALUE;
    public int count;
    public double sum;
    public double avg;
    public long timestamp;

    public Aggregation updateFrom(TemperatureMeasurement measurement) {
        stationId = measurement.stationId;
        stationName = measurement.stationName;

        count++;
        sum += measurement.value;
        avg = BigDecimal.valueOf(sum / count)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        min = Math.min(min, measurement.value);
        max = Math.max(max, measurement.value);

        return this;
    }

    @Override
    public String toString() {
        return "Aggregation{" +
                "stationId=" + stationId +
                ", stationName='" + stationName + '\'' +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                ", sum=" + sum +
                ", avg=" + avg +
                ", timestamp=" + timestamp +
                '}';
    }
}
