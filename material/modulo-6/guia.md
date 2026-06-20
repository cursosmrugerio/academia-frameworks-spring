# Módulo 6 — Web, datos y herramientas

Guía del instructor. Continúa sobre el mismo proyecto `ejemplo/` del Módulo 5 (BPM de banca con Kogito sobre Spring Boot). Cada tema se apoya en una pieza **real y probada**.

> Temas: Spring Batch · Git · Maven · BD relacional · MongoDB · HTTP/HTTPS · URIs y resources · verbos HTTP · response codes · REST vs SOAP · crear servicios RESTful con Java.

## Cómo ejecutar

```bash
mvn -f ejemplo/pom.xml test                 # todas las pruebas (13)
mvn -f ejemplo/pom.xml test -Dtest=ApiRestIntegrationTest   # una sola

# Con base de datos real (PostgreSQL) y/o servidor:
docker compose -f ejemplo/docker-compose.yml up -d
mvn -f ejemplo/pom.xml spring-boot:run -Dspring-boot.run.profiles=postgres
```

> Con el daemon de Docker activo, las pruebas de **Postgres** y **MongoDB** corren contra contenedores reales (Testcontainers); si no hay Docker, se omiten solas y el resto sigue verde.

## Mapa tema → código

| Tema | Dónde se ve (en `ejemplo/`) |
|---|---|
| Maven | `pom.xml` (parent, BOM de Kogito, dependencias, plugins) |
| Git | este repositorio y su historial de commits por incremento |
| BD relacional | `clientes/Cliente` + `ClienteRepository`; perfil `postgres`, `docker-compose.yml`, `ClientePostgresContainerTest` |
| MongoDB | `bitacora/EventoSolicitud` + `EventoSolicitudRepository` + `BitacoraService`; `EventoSolicitudMongoTest` |
| Spring Batch | `batch/BatchAltaClientesConfig` + `SolicitanteCsv` + `datos/solicitantes.csv`; `AltaClientesBatchTest` |
| HTTP, verbos, URIs, response codes | `clientes/ClienteController` + REST autogenerado del proceso; `ApiRestIntegrationTest` |
| Crear servicios RESTful con Java | `ClienteController` (propio) y los endpoints que Kogito autogenera |
| Service calls (REST/SOAP) | `riesgo/EvaluacionRiesgoService` invocado por el service task de `evaluacionRiesgo.bpmn` |

---

## 1. Maven

El `pom.xml` del ejemplo es el material vivo:
- **Parent** `spring-boot-starter-parent` (gestión de versiones) + **import del BOM** `kogito-spring-boot-bom` (alinea Kogito y Spring Boot).
- **Dependencias** por *starter* (web, data-jpa, data-mongodb, batch, validation, test) — sin fijar versiones (las gestiona el parent/BOM).
- **Plugins**: `spring-boot-maven-plugin` (empaquetado) y `kogito-maven-plugin` (genera el código de los `.bpmn` en tiempo de build).
- Ciclo: `mvn test` (compila → genera → prueba), `mvn package`, `mvn spring-boot:run`.

## 2. Git

El propio repositorio es el ejemplo: historial **incremental** (un commit verificado por tema), mensajes en español, `.gitignore` para `target/`. Puntos para clase: `clone/add/commit/log/branch/merge`, y por qué conviene commitear en incrementos pequeños y verdes (cada commit de este proyecto deja la suite en verde).

## 3. Base de datos relacional (PostgreSQL)

La entidad de negocio `Cliente` se persiste con Spring Data JPA. En desarrollo/pruebas se usa **H2**; para datos reales, **PostgreSQL** vía el perfil `postgres`:

```properties
# application-postgres.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/banca
spring.jpa.hibernate.ddl-auto=update
```

```bash
docker compose -f ejemplo/docker-compose.yml up -d   # Postgres 16
mvn -f ejemplo/pom.xml spring-boot:run -Dspring-boot.run.profiles=postgres
```

`ClientePostgresContainerTest` prueba el **mismo** repositorio contra Postgres real (Testcontainers): demuestra que el código no cambia entre H2 y Postgres. Puntos para clase: modelo relacional, JPA/Hibernate, perfiles de Spring, y el contraste con MongoDB.

## 4. MongoDB

La **bitácora** de la solicitud (`EventoSolicitud`) se guarda como documentos — datos tipo histórico que encajan mejor en un almacén documental:

```java
@Document(collection = "eventos_solicitud")
public class EventoSolicitud { @Id String id; String solicitudId; String tipo; ... }

public interface EventoSolicitudRepository extends MongoRepository<EventoSolicitud, String> {
    List<EventoSolicitud> findBySolicitudIdOrderByFechaAsc(String solicitudId);
}
```

`EventoSolicitudMongoTest` corre contra Mongo real (Testcontainers). Puntos para clase: documento vs tabla, cuándo conviene cada uno, `MongoRepository` (misma idea que JPA).

## 5. Spring Batch

Alta **masiva** de clientes desde un CSV con el patrón *chunk-oriented*:

```
reader (FlatFileItemReader<CSV>) -> processor (mapea y completa) -> writer (RepositoryItemWriter)
```

Ver `BatchAltaClientesConfig`. Puntos para clase: `Job`/`Step`/chunk, lectura de archivos, el *processor* idempotente (omite duplicados), y por qué los jobs no se ejecutan al arrancar (`spring.batch.job.enabled=false`) sino a propósito. `AltaClientesBatchTest` lanza el job con `JobLauncherTestUtils` y verifica las altas.

## 6. HTTP/HTTPS, URIs, verbos y response codes

Dos APIs conviven: la **propia** (`/api/clientes`) y la **autogenerada** por Kogito para el proceso (`/solicitudCuenta`).

| Verbo | URI | Código |
|---|---|---|
| POST | `/api/clientes` | 201 Created (+ `Location`) |
| GET | `/api/clientes/{id}` | 200 / 404 |
| POST inválido | `/api/clientes` | 400 (validación) |
| POST | `/solicitudCuenta` | 2xx (crea instancia de proceso) |

`ApiRestIntegrationTest` ejercita todo esto con un cliente HTTP real (sustituye la demo con curl). Puntos para clase: recursos y URIs, semántica de verbos, códigos de estado, y cómo `ResponseEntity`/`@ResponseStatus` los controlan.

## 7. REST vs SOAP

Comparación para clase (conceptual):

| | REST | SOAP |
|---|---|---|
| Estilo | Arquitectura sobre HTTP, orientada a **recursos** | Protocolo basado en **mensajes XML** (sobre HTTP, JMS, etc.) |
| Formato | JSON (típico), también XML | XML (envelope) obligatorio |
| Contrato | OpenAPI (opcional) | WSDL (formal) |
| Estado | Sin estado | Puede llevar estado/transacciones (WS-*) |
| Ventajas | Simple, ligero, cacheable, ubicuo | Contratos estrictos, estándares empresariales (seguridad/transacciones) |
| Cuándo | APIs web/móviles, microservicios | Integraciones legadas/empresariales con contrato formal |

En el ejemplo, **crear servicios RESTful con Java** se ve en `ClienteController` y en los endpoints autogenerados. El **consumo de servicios** desde un proceso se ve en el *service task* (`evaluacionRiesgo` → `EvaluacionRiesgoService`); el mismo patrón sirve para invocar un servicio REST o SOAP externo (cambia la implementación del `@Component`, no el flujo).

---

## Cierre

Todo el código de los Módulos 5 y 6 vive en `ejemplo/` y está respaldado por **13 pruebas en verde**. El siguiente paso del proyecto (fuera del temario de código) es la **demo en Azure** (Container Apps + Azure Database for PostgreSQL / Cosmos DB) y el caso de estudio de arquitectura.
