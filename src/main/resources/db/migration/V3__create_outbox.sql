CREATE TABLE outbox (
                        id         UUID         NOT NULL,
                        topic      VARCHAR(255) NOT NULL,
                        msg_key    VARCHAR(255) NOT NULL,
                        payload    TEXT         NOT NULL,
                        created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
                        sent_at    TIMESTAMPTZ,
                        CONSTRAINT pk_outbox PRIMARY KEY (id)
);