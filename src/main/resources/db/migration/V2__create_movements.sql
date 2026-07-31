CREATE TABLE movements (
                           id          UUID           NOT NULL,
                           from_id     UUID           NOT NULL,
                           to_id       UUID           NOT NULL,
                           amount      NUMERIC(19, 2) NOT NULL,
                           occurred_at TIMESTAMPTZ    NOT NULL,
                           CONSTRAINT pk_movements PRIMARY KEY (id)
);