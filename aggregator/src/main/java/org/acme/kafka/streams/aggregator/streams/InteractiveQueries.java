package org.acme.kafka.streams.aggregator.streams;

import org.acme.kafka.streams.aggregator.model.Aggregation;
import org.acme.kafka.streams.aggregator.model.WeatherStationData;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class InteractiveQueries {
    @Inject
    KafkaStreams streams;

    public List<WeatherStationData> getWeatherStationData(int id) {
        // To get *all* available windows we fetch windows from the beginning of time until now.
        Instant timeFrom = Instant.ofEpochMilli(0); // beginning of time = oldest available
        Instant timeTo = Instant.now(); // now (in processing-time)
        WindowStoreIterator<Aggregation> iterator = getWeatherStationStore().fetch(id, timeFrom, timeTo);
        if (iterator != null) {
            List<WeatherStationData> ret = new ArrayList<>();
            while (iterator.hasNext()) {
                KeyValue<Long, Aggregation> next = iterator.next();
                long windowTimestamp = next.key;
                ret.add(WeatherStationData.from(next.value, windowTimestamp));
            }
            return ret;
        }
        else {
            return new ArrayList<>();
        }
    }

    private ReadOnlyWindowStore<Integer, Aggregation> getWeatherStationStore() {
        while (true) {
            try {
                return streams.store(TopologyProducer.WEATHER_STATIONS_STORE, QueryableStoreTypes.windowStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
