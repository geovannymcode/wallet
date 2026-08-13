CREATE TABLE processed_events (
                                  event_id     UUID        NOT NULL,
                                  processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  CONSTRAINT pk_processed_events PRIMARY KEY (event_id)
);