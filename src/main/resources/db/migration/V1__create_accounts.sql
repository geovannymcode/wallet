CREATE TABLE accounts (
                          id      UUID           NOT NULL,
                          owner   VARCHAR(255)   NOT NULL,
                          balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
                          CONSTRAINT pk_accounts PRIMARY KEY (id)
);

-- Dos cuentas de prueba para poder transferir en las siguientes fases.
INSERT INTO accounts (id, owner, balance) VALUES
                                              ('11111111-1111-1111-1111-111111111111', 'Elena',  100000.00),
                                              ('22222222-2222-2222-2222-222222222222', 'Geovanny',      0.00);