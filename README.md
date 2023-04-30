# kafka-streams

turns this example into a windowed streams application

https://quarkus.io/guides/kafka-streams

streams app interactive query, all windows, per station:
```bash
http localhost:8081/weather-stations/data/1
[
    {
        "avg": 40.6,
        "count": 1,
        "max": 40.6,
        "min": 40.6,
        "stationId": 1,
        "stationName": "Hamburg",
        "timestamp": 1595994060000
    },
    {
        "avg": 21.8,
        "count": 14,
        "max": 52.2,
        "min": 3.7,
        "stationId": 1,
        "stationName": "Hamburg",
        "timestamp": 1595994120000
    }
]
```

scale out interactive query, hosts, partitions
```bash
http localhost:8081/weather-stations/meta-data
```

compacted topic for temperatures-aggregated
```bash
/opt/kafka_2.13-2.8.0/bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic temperatures-aggregated --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=100"  --config "segment.ms=100" --config "min.cleanable.dirty.ratio=0.01"
```

materialize, live rolling average temps per station for window
```bash
psql -h localhost -p 6875 -U materialize materialize -f ./load.sql

materialize=> select * from AVGTEMP order by stationId;
 avg  | count | max  |  min  | stationid | stationname 
------+-------+------+-------+-----------+-------------
 13.6 |   141 | 59.8 | -30.1 |         1 | Hamburg
    6 |   123 | 44.5 | -35.3 |         2 | Snowdonia
 15.7 |   138 | 47.6 | -20.5 |         3 | Boston
 15.1 |   126 |   55 | -23.9 |         4 | Tokio
 11.3 |   133 | 46.9 | -26.1 |         5 | Cusco
 -6.5 |   113 | 32.7 | -46.3 |         6 | Svalbard
   13 |   127 |   51 | -23.9 |         7 | Porthsmouth
  7.2 |   127 | 46.3 | -34.7 |         8 | Oslo
 20.4 |   137 | 52.8 | -19.5 |         9 | Marrakesh
(9 rows) 
```

## Running

Start Kafka and Materalize.

```bash
podman-compose -f docker-compose.yaml up -d
```

Run Producer.

```bash
mvn quarkus:dev -Dquarkus.http.port=8080 -Ddebug=5005 -f producer/pom.xml
```

Run Aggregator.

```bash
mvn quarkus:dev -Dquarkus.http.port=8081 -Ddebug=5006 -f aggregator/pom.xml
```

Run Consumer.

```bash
mvn quarkus:dev -Dquarkus.http.port=8082 -Ddebug=5007 -f consumer/pom.xml
```

## Stopping

Stop Kafka and Materalize.

```bash
podman-compose down
```


## Queries

```bash
/opt/kafka_2.13-2.8.0/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic temperatures-aggregated --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}'

/opt/kafka_2.13-2.8.0/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic weather-stations --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}'

/opt/kafka_2.13-2.8.0/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic temperature-values --time -1 --offsets 1 | awk -F  ":" '{sum += $3} END {print sum}'
```
