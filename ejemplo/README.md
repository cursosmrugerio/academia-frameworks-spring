# Ejemplo del curso — BPM con Kogito sobre Spring Boot

Aplicación de ejemplo que sirve de **hilo conductor** del curso: una **solicitud de apertura de cuenta** (dominio de banca, anonimizado) modelada como proceso BPMN con **aprobación de dos líneas**, ejecutándose sobre **Apache KIE / Kogito 10.2.0 + Spring Boot 3.5.10 (Java 21)**.

## Estado — F0 (andamiaje)

Mínimo que **compila y corre**: el proceso `solicitudCuenta` con sus tareas humanas y su prueba automatizada. Persistencia **en memoria** (sin base de datos). En los módulos siguientes se irán colgando los temas del temario: REST propios, Spring Data JPA + PostgreSQL, MongoDB, Spring Batch, Mockito, etc.

## Requisitos

- Java 21 (JDK)
- Maven 3.9+

## Pruebas

```bash
mvn -f ejemplo/pom.xml test
# (o, situado dentro de ejemplo/):  mvn test
```

## Arrancar la aplicación

```bash
mvn -f ejemplo/pom.xml spring-boot:run
```

Queda escuchando en `http://localhost:8080`.

## El proceso `solicitudCuenta`

- Definición: `src/main/resources/mx/academia/banca/solicitudCuenta.bpmn`
- Kogito **autogenera la API REST** a partir del ID del proceso:
  - `POST /solicitudCuenta` — registra una solicitud (crea una instancia del proceso)
  - `GET /solicitudCuenta` — lista las solicitudes en curso
  - `GET /solicitudCuenta/{id}` — consulta una solicitud
  - `DELETE /solicitudCuenta/{id}` — cancela una solicitud
- **Tareas humanas:** aprobación de primera y de segunda línea (grupo `managers`); la segunda línea excluye a quien aprobó la primera.

> Los endpoints exactos de las tareas humanas se documentan y prueban en vivo en el módulo de servicios REST.

## Cómo se conecta con el temario

| Pieza de este proyecto | Tema del curso |
|---|---|
| `pom.xml` (BOM, dependencias, plugin) | Maven |
| `BancaApplication` + autoconfiguración | Spring Boot |
| API REST autogenerada del proceso | HTTP, verbos, URIs, REST con Java (Módulo 6) |
| `SolicitudCuentaProcessTest` | JUnit / pruebas (Módulo 5) |
| *(pendiente)* service tasks como `@Service` | Spring Core / inyección de dependencias |
| *(pendiente)* entidades + repositorios | Spring Data JPA, BD relacional, MongoDB |

## Origen

La estructura se basa en el ejemplo oficial `process-usertasks-springboot` de Apache KIE (rama `10.2.x`), adaptado y **anonimizado** al dominio de banca. No contiene datos de ningún cliente real.
