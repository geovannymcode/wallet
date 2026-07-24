CREATE TABLE accounts (
                          id         UUID           NOT NULL,
                          owner      VARCHAR(255)   NOT NULL,
                          email      VARCHAR(255)   NOT NULL,
                          balance    NUMERIC(19, 2) NOT NULL DEFAULT 0,
                          created_at TIMESTAMPTZ    NOT NULL,
                          updated_at TIMESTAMPTZ    NOT NULL,
                          CONSTRAINT pk_accounts PRIMARY KEY (id)
);

-- Dos cuentas de prueba para poder transferir en las siguientes fases.
INSERT INTO accounts (id, owner, email, balance, created_at, updated_at) VALUES
                                                                             ('11111111-1111-1111-1111-111111111111', 'Elena',    'elena@example.com',    100000.00, now(), now()),
                                                                             ('22222222-2222-2222-2222-222222222222', 'Geovanny', 'geovanny@example.com',      0.00, now(), now());