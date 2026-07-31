# Wallet

Proyecto de práctica desarrollado con **Kotlin + Spring Boot + JPA + Flyway**, construido de forma incremental por fases sobre una base de datos **Postgres (Supabase)**.

La idea del ejercicio es simular el núcleo de una billetera digital: cuentas con saldo, transferencias entre cuentas con validaciones reales (saldo insuficiente, cuenta inexistente, transacción atómica) y un registro histórico de movimientos.

## Autores

- **Geovanny Mendoza** — [linkedin.com/in/geovannycode](https://www.linkedin.com/in/geovannycode/)
- **Maicol Ruidiaz.** — [linkedin.com/in/maicol-r-8365a4b1](https://www.linkedin.com/in/maicol-r-8365a4b1/)

## Stack técnico

- Kotlin + Spring Boot (Web, Data JPA, Validation)
- PostgreSQL alojado en Supabase
- Flyway para versionado de esquema
- Arquitectura por *features* (`account`, `transfer`, `movement`), cada una separada en `domain` (lógica y persistencia) y `web` (controladores REST)
- MockK + JUnit 5 para pruebas

## Estructura del proyecto

```
src/main/kotlin/com/geovannycode/wallet/
├── account/
│   ├── domain/   # AccountEntity, AccountRepository, AccountService, excepciones, mapper
│   └── web/      # AccountController
├── transfer/
│   ├── domain/   # TransferService, TransferRequest
│   └── web/      # TransferController
├── movement/
│   └── domain/   # MovementEntity, MovementRepository, MovementService, MoveResult
├── web/exception/
│   └── GlobalExceptionHandler.kt
└── JpaConfig/
    └── JpaConfig.kt   # habilita auditoría JPA (createdAt/updatedAt)
```

## Qué hace la aplicación, fase a fase

### Fase 0 — Scaffold del proyecto
Se crea el proyecto base con Spring Initializr (Kotlin, Gradle) y se organiza el código por *features* en lugar de por capas técnicas. Se configura la conexión a la base de datos Postgres de Supabase mediante variables de entorno (`SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`).

### Fase 1 — Entidad `Account` y migración inicial
Se modela la entidad `AccountEntity` (`id`, `owner`, `email`, `balance`, `createdAt`, `updatedAt`) y su repositorio JPA. Flyway introduce la migración `V1__create_accounts.sql`, que crea la tabla `accounts` y siembra dos cuentas de prueba (`Elena` con saldo inicial y `Geovanny` en cero) para poder ejercitar las transferencias en fases posteriores. `ddl-auto: validate` asegura que el esquema real de la BD sea la única fuente de verdad — el esquema lo gobierna Flyway, no Hibernate.

### Fase 2 — Consulta de saldo por REST
Se expone `GET /api/accounts/{id}`, que devuelve el saldo y datos de una cuenta (`AccountResponse`). Se separa explícitamente la capa `web` (controlador) de la capa `domain` (servicio + repositorio + mapper), estableciendo el patrón que se repite en el resto del proyecto. Si la cuenta no existe, se lanza `AccountNotFoundException`.

### Fase 3 — Transferencia transaccional entre cuentas
Se agrega `POST /api/transfers`, que recibe `fromId`, `toId` y `amount` (`TransferRequest`, validado con Bean Validation: montos > 0). `AccountService.moveMoney(...)` ejecuta el movimiento dentro de una transacción (`@Transactional`) y devuelve un resultado *sealed* (`MoveResult`): `Success`, `InsufficientFunds` o `AccountNotFound`. `TransferService` traduce ese resultado a excepciones de dominio, y un `GlobalExceptionHandler` centralizado las mapea a respuestas HTTP correctas:

| Resultado | HTTP |
|---|---|
| Transferencia exitosa | `200 OK` |
| Saldo insuficiente | `422 Unprocessable Entity` |
| Cuenta no encontrada | `404 Not Found` |
| Petición inválida (mismo origen/destino, monto <= 0) | `400 Bad Request` |

### Fase 4 — Registro de movimientos (histórico de transferencias)
Se añade el módulo `movement`: la tabla `movements` (migración `V2__create_movements.sql`) guarda cada transferencia realizada (`fromId`, `toId`, `amount`, `occurredAt`). `MovementService.record(...)` persiste el movimiento, y `TransferService` lo invoca de forma síncrona y directa justo después de que `AccountService.moveMoney(...)` confirma el `Success` — dejando explícita la costura entre ambos módulos como punto de evolución futura (por ejemplo, hacia un modelo basado en eventos). Se incorpora `TransferServiceTest`, que usando MockK verifica la orquestación: se registra el movimiento solo cuando la transferencia se completa, y no se registra si falla por saldo insuficiente.

## Cómo correrlo

```bash
export SUPABASE_DB_URL=...
export SUPABASE_DB_USER=...
export SUPABASE_DB_PASSWORD=...
./gradlew bootRun
```

Las peticiones de ejemplo están en [`test.http`](test.http).

## Tests

```bash
./gradlew test
```
