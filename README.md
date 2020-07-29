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
/opt/kafka_2.12-2.2.0/bin/kafka-topics.sh --zookeeper localhost:2181 --create --topic temperatures-aggregated --replication-factor 1 --partitions 1 --config "cleanup.policy=compact" --config "delete.retention.ms=100"  --config "segment.ms=100" --config "min.cleanable.dirty.ratio=0.01"
```

materialize, live rolling average temps per station for window
```bash
psql -h localhost -p 6875 materialize -f ./load.sql

materialize=> select * from AVGTEMP;
 avg  | count | max  |  min  | stationid | stationname 
------+-------+------+-------+-----------+-------------
  3.5 |    10 | 24.1 | -30.8 |         8 | Oslo
 13.2 |     9 |   21 |   -10 |         5 | Cusco
 20.9 |     7 | 37.1 |  -6.8 |         4 | Tokio
 10.7 |     7 | 37.3 |  -6.8 |         3 | Boston
 25.5 |     6 | 40.4 |  -3.2 |         1 | Hamburg
 -8.3 |     9 |  0.8 | -29.5 |         6 | Svalbard
 20.5 |    14 | 44.7 |   6.7 |         9 | Marrakesh
  1.6 |    10 | 29.1 | -28.7 |         2 | Snowdonia
 16.6 |    14 | 30.1 |   3.2 |         7 | Porthsmouth
(9 rows)

materialize=> 
```