package org.acme.kafka.streams.aggregator.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

public class AggregationEncoder implements Encoder.Text<Aggregation> {
    ObjectMapper om;

    @Override
    public String encode(Aggregation object) throws EncodeException {
        try {
            return om.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new EncodeException(object, e.getMessage());
        }
    }

    @Override
    public void init(EndpointConfig config) {
        this.om = new ObjectMapper();
    }

    @Override
    public void destroy() {
        this.om = null;
    }
}
