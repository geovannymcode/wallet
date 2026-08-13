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

> **Nota**: entre la Fase 4 y esta sección el proyecto avanzó con Kafka (patrón
> outbox, idempotencia por `eventId`, notificación por correo, error handling
> con reintentos + Dead Letter Queue) — pendiente de documentar fase por fase.
> Ver `docs/LOCAL_SETUP.md` para el flujo completo de eventos y la demo de DLQ.

### Fase 9 — Coroutines (opcional, avanzada)

**No hace falta para que la wallet funcione**: es un ejercicio extra sobre el
lado Kotlin de Spring, pensado para cuando un consumidor de Kafka deja de solo
"guardar en la base" y empieza a hablar con servicios externos por red.

**El problema que resuelve.** El listener `MovimientoNotificationListener`
(el que envía el correo real vía SMTP/Mailpit) llama a un solo servicio. Pero
en muchos sistemas reales, al llegar un evento hay que avisarle a *varios*
servicios externos a la vez (correo, push, antifraude...). Hecho de forma
bloqueante y secuencial, tres llamadas de red de ~200 ms cada una suman
~600 ms **y** dejan un hilo congelado todo ese tiempo — un recurso caro y
limitado.

**La solución: `suspend fun` + `coroutineScope`/`async`/`awaitAll`.** Una
función `suspend` puede *pausarse* mientras espera una respuesta de red, sin
bloquear el hilo — a diferencia de una llamada bloqueante, donde el hilo
queda parado. Con `coroutineScope { async { ... } }` se lanzan las tres
llamadas a la vez (en paralelo, no en fila), y `awaitAll(...)` espera a que
las tres terminen. Total: ~200 ms (lo que tarde la más lenta), no 600 ms, y
sin hilos bloqueados. Además, `coroutineScope` da **concurrencia
estructurada**: si una llamada falla, cancela las otras dos y propaga el
error — nada queda corriendo huérfano.

**Qué se agregó al proyecto:**

| Archivo | Rol |
|---|---|
| `notification/domain/NotificationService.kt` | Orquesta las 3 llamadas en paralelo con `coroutineScope` + `async` + `awaitAll`. |
| `notification/domain/EmailClient.kt`, `PushClient.kt`, `AntifraudClient.kt` | Clientes HTTP no bloqueantes (`WebClient` + `awaitBodilessEntity()`), uno por servicio externo simulado. |
| `notification/mock/MockExternalController.kt` | **Solo para la demo**: 3 endpoints (`/mock-external/emails`, `/push`, `/antifraud`) que simulan la latencia (~200 ms) de un servicio externo real, para que el ejercicio corra 100% en local sin infraestructura adicional. |
| `config/WebClientConfig.kt` | Bean `WebClient` con la base URL configurable (`wallet.notification.external-base-url`, por defecto `http://localhost:8080/mock-external`). |
| `notification/messaging/MovimientoCoroutineListener.kt` | `@KafkaListener` con método `suspend fun` (soportado desde Spring for Apache Kafka 3.2) que llama a `NotificationService.notificar(...)`. |

**Decisión de diseño — no se tocó el listener existente.** El tutorial
original propone *reemplazar* `MovimientoNotificationListener`. Como ese
listener ya envía el correo real de confirmación (vía SMTP/Mailpit) y varias
demos previas dependen de él, se agregó un listener **nuevo**
(`MovimientoCoroutineListener`) con su propio `groupId`
(`notification-coroutines-demo`). Ambos consumen el mismo tópico
`wallet.movements` de forma independiente — en Kafka, cada *consumer group*
lleva su propio offset, así que no hay conflicto ni duplicidad entre ellos.

**Dependencias agregadas** (`build.gradle.kts`):

```kotlin
implementation("org.springframework.boot:spring-boot-starter-webflux")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
```

`spring-boot-starter-webflux` trae `WebClient`; `kotlinx-coroutines-reactor`
es el puente que convierte sus operaciones reactivas en funciones `suspend`
(`awaitBodilessEntity()`, `awaitBody()`, etc.).

**Cómo probarlo:** con la app corriendo, dispara una transferencia normal
(`test.http`) y observa en la consola los logs de `MockExternalController`
(`🐢 ... procesando`, `✅ ... terminó`) — las tres llamadas (`email`, `push`,
`antifraude`) deben aparecer casi al mismo tiempo, no una tras otra, y el
`notificar(...)` completo debe tardar ~200 ms en vez de ~600 ms.

**Por qué no se usó Arrow/`parMap` aquí.** `parMap` (de `arrow-fx-coroutines`)
brilla cuando hay que llamar a un servicio externo **N veces** sobre una
colección (por ejemplo, un lote de movimientos) y quieres acotar cuántas
llamadas van a la vez. Acá son solo 3 llamadas fijas y conocidas de antemano
(correo, push, antifraude), así que `coroutineScope { async {} } + awaitAll`
es la herramienta correcta sin sumar una dependencia nueva.

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

## Entorno local (sin Supabase ni Redpanda Cloud)

¿No tienes credenciales de Supabase o Redpanda Cloud? Levanta todo con Docker
Compose (Postgres 17, Redpanda y Mailpit). Ver la guía completa en
[`docs/LOCAL_SETUP.md`](docs/LOCAL_SETUP.md).


