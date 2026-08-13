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

> Entre la Fase 4 y la Fase 10 el proyecto incorpora Kafka (patrón outbox, idempotencia por `eventId`, notificación por correo, error handling con reintentos + Dead Letter Queue) y coroutines — pendiente de documentar fase por fase. Ver `docs/LOCAL_SETUP.md` para el flujo completo de eventos y la demo de DLQ.

### Fase 10 — Empaquetado, CI/CD y despliegue

Se empaqueta la aplicación con un `Dockerfile` multi-etapa: la primera etapa (`eclipse-temurin:25-jdk`) compila el `jar` con `./gradlew bootJar`, y la segunda (`eclipse-temurin:25-jre`) solo copia el artefacto generado, de modo que la imagen final no carga con el compilador ni el código fuente. Como alternativa existe `./gradlew bootBuildImage` (buildpacks), pero se prefiere el `Dockerfile` explícito porque deja visible cada paso del build y no depende de convenciones de un plugin externo.

La integración continua corre en `.github/workflows/ci.yml` en cada `push` y cada pull request a `main`: hace *checkout*, configura JDK 25 (Temurin) y ejecuta `./gradlew test`. Los tests con MockK de la Fase 4 corren sin infraestructura, y los que sí la necesitan (Testcontainers) también funcionan porque el runner de GitHub Actions trae Docker disponible.

El despliegue continuo ocurre en Render: el Web Service se conecta al repositorio, detecta el `Dockerfile` y reconstruye la imagen con cada `push` a `main`. El reparto queda limpio: Actions se encarga del CI, Render del CD, y no hace falta guardar secretos de despliegue en Actions. La configuración del servicio es Language `Docker`, Branch `main`, Dockerfile Path `./Dockerfile`, Root Directory vacío, Region `Virginia (US East)` —por cercanía con el clúster de Redpanda en `us-east-1`— e Instance Type `Free`.

La configuración queda externalizada para que el mismo binario corra en local y en la nube: los defaults de `application.yaml` apuntan al entorno de Docker Compose, y las variables de entorno lo reapuntan a Supabase, Redpanda y Resend. `server.port: ${PORT:8080}` existe porque Render asigna el puerto por variable de entorno, y esa variable no se crea a mano: la inyecta la plataforma. El nombre de cada variable lo decide `application.yaml`, no el host.

| Variable | Ejemplo | Para qué |
|---|---|---|
| `SUPABASE_DB_URL` | `jdbc:postgresql://<host>.pooler.supabase.com:5432/postgres` | Conexión a Postgres |
| `SUPABASE_DB_USER` | `postgres.<ref-del-proyecto>` | Usuario del pooler |
| `SUPABASE_DB_PASSWORD` | *(secreto)* | Clave de la base |
| `REDPANDA_BOOTSTRAP` | `<cluster>.any.us-east-1.mpx.prd.cloud.redpanda.com:9092` | Broker de eventos |
| `REDPANDA_USER` | `workshop-wallet` | Usuario SASL |
| `REDPANDA_PASSWORD` | *(secreto)* | Clave SASL |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=70 -Xss512k` | Que la JVM quepa en 512 MB |
| `SMTP_HOST` | `smtp.resend.com` | Servidor de correo |
| `SMTP_PORT` | `587` | Puerto SMTP |
| `SMTP_USER` | `resend` | Literalmente la palabra `resend` |
| `SMTP_PASSWORD` | `re_xxxx...` | API key de Resend |
| `SMTP_AUTH` | `true` | Activa autenticación |
| `SMTP_STARTTLS` | `true` | Activa TLS |
| `MAIL_FROM` | `onboarding@resend.dev` | Remitente |
| `MAIL_OPS_TO` | *(correo de operaciones)* | Destino de las alertas de DLQ |

El correo se maneja distinto en cada entorno. En local, Mailpit (`localhost:1025`, bandeja en `http://localhost:8025`) recibe todo sin restricciones ni cuentas. En la nube ese `localhost` no existe, así que entra Resend por SMTP. En modo sandbox, con el remitente `onboarding@resend.dev`, Resend solo entrega al correo registrado en la cuenta y cualquier otro destinatario devuelve `403`; para levantar esa restricción hay que verificar un dominio propio en la sección *Domains* de Resend. Por la misma razón, los datos semilla de `V1__create_accounts.sql` (correos `elena@example.com` y `geovanny@example.com`) no reciben nada en sandbox: para que `EmailNotifier` entregue de verdad, la cuenta destino de la demo debe tener el correo registrado en la cuenta de Resend, o debe usarse un dominio propio verificado.

Algunas notas operativas:

- El `Dockerfile` debe estar en `main` antes de conectar el repositorio en Render.
- `gradlew` necesita bit de ejecución en git (`git ls-files -s gradlew` debe dar `100755`; se corrige con `git update-index --chmod=+x gradlew`), o el runner Linux falla con `permission denied`.
- Sin `JAVA_TOOL_OPTIONS`, la JVM asume que es dueña de la máquina y el plan Free de 512 MB se queda sin memoria.
- El plan Free duerme el servicio tras 15 minutos sin tráfico: el primer *request* tarda alrededor de 50 segundos en despertar el contenedor.
- El primer build puede tomar entre 8 y 10 minutos porque el multi-etapa descarga Gradle desde cero.
- El *pooler* de Supabase debe usarse en el puerto 5432 (*session mode*); el 6543 (*transaction mode*) rompe los *prepared statements* de Hibernate.
- `TOPIC_AUTHORIZATION_FAILED` en Redpanda también aparece cuando el tópico no existe y el usuario no puede crearlo, porque el *broker* no revela qué tópicos hay. Las ACLs necesarias son Topic (`Prefixed` / `wallet.`), Consumer Group (`Prefixed` / `wallet.`, con el `group.id` usando ese mismo prefijo) y Cluster con `IdempotentWrite`, ya que el *producer* de Spring Boot trae `enable.idempotence=true` por defecto.

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


