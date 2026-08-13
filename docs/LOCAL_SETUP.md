# Entorno local para el taller (sin Supabase ni Redpanda Cloud)

Si no tienes acceso a Supabase ni a un cluster de Redpanda Cloud, puedes correr
todo el stack (Postgres 17, Kafka/Redpanda y un capturador de correos) en tu
máquina con Docker Compose. El código de la app no cambia: solo activas el
perfil `local` de Spring.

## 1. Requisitos

- Docker y Docker Compose instalados (`docker --version`, `docker compose version`).
- JDK 25 (o el toolchain configurado en `build.gradle.kts`).

## 2. Levantar la infraestructura

Desde la raíz del proyecto:

```bash
docker compose up -d
```

Esto levanta 4 contenedores:

| Servicio          | Para qué sirve                                   | Puerto(s)                     |
|--------------------|---------------------------------------------------|--------------------------------|
| `postgres`         | Base de datos (Postgres 17)                       | `5432`                         |
| `mailpit`          | Captura los correos que envía la app (sin SMTP real) | `1025` (SMTP), `8025` (UI web) |
| `redpanda`         | Broker de Kafka local (sin autenticación)          | `9092` (Kafka API)              |
| `redpanda-console` | UI web para ver tópicos, mensajes y la DLQ         | `8090`                         |

Verifica que todo esté arriba y saludable:

```bash
docker compose ps
```

Espera a que `wallet-postgres` muestre `healthy` antes de arrancar la app
(la primera vez Postgres tarda unos segundos en inicializar).

## 3. Levantar la aplicación con el perfil `local`

El archivo `src/main/resources/application-local.yaml` sobrescribe:

- El `datasource` para apuntar al Postgres del `docker-compose.yml`
  (`localhost:5432/wallet`, usuario/clave `wallet`/`wallet`).
- El `bootstrap-servers` de Kafka a `localhost:9092`, sin SASL/SSL (Redpanda
  local corre en modo `dev-container`, sin autenticación).
- El `mail.host`/`mail.port` a `localhost:1025` (Mailpit).

Corre la app así:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Flyway crea automáticamente las tablas (`accounts`, `movements`, `outbox`,
`processed_events`) contra el Postgres local la primera vez que arranca.

> Si usas el run configuration de tu IDE, agrega `local` en
> **Active profiles** (o la variable de entorno `SPRING_PROFILES_ACTIVE=local`)
> en vez de pasarlo por línea de comandos.

## 4. Probar el flujo normal

Usa `test.http` (o cualquier cliente REST) contra `http://localhost:8080`:

```http
POST http://localhost:8080/api/transfers
Content-Type: application/json

{
  "fromId": "11111111-1111-1111-1111-111111111111",
  "toId": "22222222-2222-2222-2222-222222222222",
  "amount": 15000.00
}
```

Para confirmar que todo funcionó:

- **Correo de notificación**: abre <http://localhost:8025> (Mailpit) y deberías
  ver el correo "Recibiste un movimiento en tu wallet".
- **Outbox / eventos**: conéctate al Postgres local
  (`jdbc:postgresql://localhost:5432/wallet`, usuario/clave `wallet`) y corre:

  ```sql
  SELECT id, topic, msg_key, sent_at, created_at FROM outbox ORDER BY created_at DESC LIMIT 10;
  SELECT event_id, processed_at FROM processed_events ORDER BY processed_at DESC LIMIT 10;
  ```

  `sent_at` debe poblarse a los pocos segundos (el `OutboxRelay` corre cada 2s).

- **Tópicos en Kafka**: abre <http://localhost:8090> (Redpanda Console) y
  revisa el tópico `wallet.movements`.

## 5. Probar el escenario de la DLQ (Dead Letter Queue)

El listener `MovimientoPersistenceListener` tiene un centinela para la demo:
si el `amount` del evento es negativo, lanza una excepción a propósito. Está
**desactivado por defecto** y se controla con la propiedad
`wallet.demo.dlq-sentinel-enabled` (variable de entorno
`WALLET_DEMO_DLQ_SENTINEL`), para no tener que comentar/descomentar código
antes y después de la demo:

```bash
WALLET_DEMO_DLQ_SENTINEL=true SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Con el centinela activo, el `DefaultErrorHandler` (`KafkaErrorConfig`)
reintenta 3 veces (1s entre intentos) y, agotados los reintentos, el
`DeadLetterPublishingRecoverer` publica el evento en `wallet.movements.DLT`.

### 5.1. Producir un evento "envenenado" directo al tópico

Como el endpoint valida los datos con `@DecimalMin`, no puedes mandar un
monto negativo por HTTP. Hay que publicarlo directo al tópico con `rpk`
(cliente de Redpanda), usando el contenedor local:

```bash
docker exec -i wallet-redpanda rpk topic produce wallet.movements <<'EOF'
{"eventId":"00000000-0000-0000-0000-000000000000","fromId":"11111111-1111-1111-1111-111111111111","toId":"22222222-2222-2222-2222-222222222222","amount":-1,"occurredAt":"2026-01-01T00:00:00Z"}
EOF
```

### 5.2. Ver el mensaje llegar a la DLQ

Tienes tres formas de confirmarlo (con Redpanda local, sin SASL, no hace
falta configurar credenciales en `rpk`):

1. **Consola de la app**: verás el `log.warn("💀 Mensaje muerto en el DLT...")`
   de `DeadLetterListener` tras ~3-4 segundos (3 reintentos x 1s de backoff).
2. **Correo visual**: abre <http://localhost:8025> (Mailpit) — el
   `DeadLetterListener` también envía un correo "⚠️ Mensaje enviado a la
   DLQ" con el `key`, `value`, partición y offset del mensaje. Esta es la
   forma más visible para mostrarlo al público sin usar Slack.
3. **rpk / Redpanda Console**:

   ```bash
   docker exec -it wallet-redpanda rpk topic consume wallet.movements.DLT
   ```

   o en la UI web (<http://localhost:8090>), abre el tópico
   `wallet.movements.DLT` y revisa los mensajes.

> ⚠️ Recuerda: el centinela de `amount < 0` en `MovimientoPersistenceListener`
> es **solo para esta demo**. Al estar apagado por defecto
> (`wallet.demo.dlq-sentinel-enabled=false`), no representa riesgo si el flag
> nunca se activa en producción — pero igual conviene eliminarlo del código
> una vez terminado el taller.

## 6. Apagar el entorno local

```bash
docker compose down        # detiene los contenedores
docker compose down -v     # además borra los datos de Postgres
```

## 7. Volver a Supabase / Redpanda Cloud

Simplemente no actives el perfil `local` (o quita `SPRING_PROFILES_ACTIVE`).
La app usará de nuevo `application.yaml` con las variables de entorno
`SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`,
`REDPANDA_BOOTSTRAP`, `REDPANDA_USER`, `REDPANDA_PASSWORD` como antes.
