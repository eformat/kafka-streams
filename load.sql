CREATE SOURCE temps
FROM KAFKA BROKER 'localhost:9092' TOPIC 'temperatures-aggregated'
FORMAT TEXT
ENVELOPE UPSERT;

CREATE MATERIALIZED VIEW AVGTEMP AS
    SELECT CAST((text::JSONB)->'avg' as float) as avg,
           CAST((text::JSONB)->'count' as int) as count,
           CAST((text::JSONB)->'max' as float) as max,
           CAST((text::JSONB)->'min' as float) as min,
           CAST((text::JSONB)->'stationId' as int) as stationId,
           (text::JSONB)->>'stationName' as stationName
    FROM (SELECT * FROM temps);
