CREATE TABLE IF NOT EXISTS endpoint_hits (
    id           BIGSERIAL PRIMARY KEY,
    app          VARCHAR(255)    NOT NULL,
    uri          VARCHAR(512)    NOT NULL,
    ip           VARCHAR(45)     NOT NULL,
    hit_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
