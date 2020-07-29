package org.acme.kafka.streams.aggregator.streams;

import io.quarkus.kafka.client.serialization.JsonbSerde;
import org.acme.kafka.streams.aggregator.model.Aggregation;
import org.acme.kafka.streams.aggregator.model.TemperatureMeasurement;
import org.acme.kafka.streams.aggregator.model.WeatherStation;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class TopologyProducer {
    static final String WEATHER_STATIONS_STORE = "weather-stations-store";
    private static final String WEATHER_STATIONS_TOPIC = "weather-stations";
    private static final String TEMPERATURE_VALUES_TOPIC = "temperature-values";
    private static final String TEMPERATURES_AGGREGATED_TOPIC = "temperatures-aggregated";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        JsonbSerde<WeatherStation> weatherStationSerde = new JsonbSerde<>(
                WeatherStation.class);
        JsonbSerde<Aggregation> aggregationSerde = new JsonbSerde<>(Aggregation.class);

        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(
                WEATHER_STATIONS_STORE);

        GlobalKTable<Integer, WeatherStation> stations = builder.globalTable(
                WEATHER_STATIONS_TOPIC,
                Consumed.with(Serdes.Integer(), weatherStationSerde));

        KStream<Windowed<Integer>, Aggregation> windowed = builder.stream(
                TEMPERATURE_VALUES_TOPIC,
                Consumed.with(Serdes.Integer(), Serdes.String())
        )
                .join(
                        stations,
                        (stationId, timestampAndValue) -> stationId,
                        (timestampAndValue, station) -> {
                            String[] parts = timestampAndValue.split(";");
                            return new TemperatureMeasurement(station.id, station.name,
                                    Instant.parse(parts[0]), Double.valueOf(parts[1]));
                        }
                )
                .groupByKey()
                .windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(1)))
                .aggregate(
                        Aggregation::new,
                        (stationId, value, aggregation) -> aggregation.updateFrom(value),
                        Materialized.<Integer, Aggregation, WindowStore<Bytes, byte[]>>as(WEATHER_STATIONS_STORE)
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(aggregationSerde)
                )
                .toStream();

        KStream<Integer, Aggregation> rounded = windowed.map(((integerWindowed, aggregation) -> new KeyValue<>(integerWindowed.key(), aggregation)));
        rounded.to(
                TEMPERATURES_AGGREGATED_TOPIC,
                Produced.with(Serdes.Integer(), aggregationSerde)
        );

        return builder.build();
    }
}
