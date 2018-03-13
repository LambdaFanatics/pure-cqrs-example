DROP TABLE IF EXISTS event_consumers;

CREATE TABLE event_consumers (
    consumer_name VARCHAR PRIMARY KEY,
    log_offset BIGINT  NOT NULL
);

