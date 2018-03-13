DROP TABLE IF EXISTS events;

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    payload JSON NOT NULL
);

DROP TABLE IF EXISTS cars;

CREATE TABLE cars (
    reg_plate VARCHAR PRIMARY KEY, -- registration plate of the car
    model VARCHAR NOT NULL,
    status VARCHAR NOT NULL
);

DROP TABLE IF EXISTS car_parts;

CREATE TABLE car_parts (
    name VARCHAR NOT NULL,
    car_reg_plate VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    PRIMARY KEY (name, car_reg_plate)
);

