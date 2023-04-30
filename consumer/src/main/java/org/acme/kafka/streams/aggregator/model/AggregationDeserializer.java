package org.acme.kafka.streams.aggregator.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class AggregationDeserializer extends ObjectMapperDeserializer<Aggregation> {

    public AggregationDeserializer() {
        super(Aggregation.class);
    }
}
