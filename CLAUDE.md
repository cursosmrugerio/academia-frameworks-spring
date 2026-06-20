# CLAUDE.md

Guía operativa del workspace. Léela antes de proponer o hacer cambios. Es la **fuente de verdad** del proyecto; si algo aquí contradice una suposición, gana este archivo.

## Qué es este workspace

Material para un **curso de frameworks Java** que imparte el autor. El alcance del temario está en `temario/README.md`. El objetivo es **enseñar los temas de Spring del temario usando un ejemplo real**: un BPM (gestión de procesos de negocio) construido con **Kogito 10.2.0 corriendo sobre Spring Boot**.

El workspace arrancó vacío (solo el temario). Todo el material —explicaciones, código y despliegue— se construye aquí.

## Objetivo y alcance

Cubrimos **solo dos módulos** del temario:

- **Módulo 5:** JUnit, *working with assertions*, Mockito, Spring Core, Spring MVC, Spring Boot, Spring Data JPA.
- **Módulo 6:** Spring Batch, Git, Maven, base de datos relacional, MongoDB, HTTP/HTTPS, URIs y *resources*, verbos HTTP (POST/PUT/GET/DELETE/PATCH), *response codes*, REST vs SOAP, crear servicios RESTful con Java.

**Fuera de alcance:** Módulo 7 (IA generativa / Copilot) y los módulos 1–4 (no están en el temario entregado).

## Enfoque pedagógico

El hilo conductor es **un BPM de banca** (dominio anonimizado: apertura de cuenta / aprobación de crédito) implementado con **Kogito 10.2.0 sobre Spring Boot 3.5.10 / Java 21**. Cada tema del temario se enseña con una pieza real de ese proyecto:

| Tema del temario | Cómo se enseña en el ejemplo |
|---|---|
| Spring Core | IoC/DI: *service tasks* del proceso como `@Service`/`@Component` inyectados |
| Spring MVC | Controllers REST propios + endpoints REST autogenerados por proceso |
| Spring Boot | El runtime mismo (`jbpm-spring-boot-starter`, autoconfig, `application.yml`, Actuator) |
| Spring Data JPA | Entidades de **negocio** (Cliente, Solicitud…) con repositorios `JpaRepository` |
| JUnit / assertions / Mockito | Tests de servicios Spring + tests de procesos BPMN |
| Spring Batch | Job batch sobre el dominio (p. ej. procesar lote de solicitudes) |
| BD relacional / MongoDB | PostgreSQL (motor + negocio) y MongoDB (`persistence-mongodb`) |
| HTTP / REST / SOAP / verbos / códigos | Endpoints autogenerados + controllers + un *service task* REST vs SOAP |
| Git / Maven | El propio proyecto (POM con BOM Kogito) |

## Origen del ejemplo y confidencialidad ⚠️

El ejemplo se **inspira** en una arquitectura BPM real, usada solo como referencia de patrones y dominio.

**Regla innegociable:** todo el material de este workspace va **completamente anonimizado**. NUNCA incluir nombres de cliente, nombres de personas, folios, correos, credenciales, datos comerciales ni políticas internas de ningún proyecto real. Se toman **solo patrones de arquitectura y conceptos técnicos**, jamás datos identificables. Ante la duda de si algo es sensible, abstraerlo.

## Estructura (prevista — se materializa al avanzar)

```
kogitoAcademiaMundial/
├── CLAUDE.md                 # este archivo (guía operativa / continuidad)
├── README.md                 # presentación del curso (instructor/alumnos)
├── temario/                  # alcance del temario (Módulos 5 y 6)
├── ejemplo/                  # proyecto Kogito 10.2.0 sobre Spring Boot (código que corre)
├── material/
│   ├── modulo-5/             # guías por tema: Spring Core/MVC/Boot/Data JPA + testing
│   └── modulo-6/             # guías por tema: Batch, Git, Maven, BD, Mongo, HTTP/REST/SOAP
├── azure/                    # IaC + guía de despliegue de la demo (Container Apps)
├── docs/                     # caso de estudio de arquitectura (anonimizado)
└── sitio/                    # sitio de estudio para alumnos (HTML/JS/CSS plano, offline)
```

## Decisiones confirmadas — no reabrir

- **Framework del ejemplo:** Kogito sobre **Spring Boot** (no Quarkus, aunque el proyecto hermano use Quarkus).
- **Dominio:** banca (apertura de cuenta / aprobación de crédito).
- **Nube de la demo:** **Azure Container Apps** (cuenta de Azure del autor). El proyecto hermano es on-premise/OpenShift; aquí se re-mapea a Azure a propósito, para la academia.
- **Entregables:** material didáctico + código de ejemplo + demo en Azure + caso de estudio. (No se piden labs/ejercicios.)
- **Java 21** (LTS; es el default de Kogito 10.2.0 y el JDK activo en el equipo). Build con **Maven**.

## Versiones de referencia (Apache KIE / Kogito 10.2.0)

| Componente | Versión |
|---|---|
| Apache KIE / Kogito | 10.2.0 |
| Spring Boot | 3.5.10 |
| Java | 21 (LTS, default del producto; admite 17) |
| Maven | 3.9.6+ |
| Hibernate ORM | 7.1.14.Final |
| PostgreSQL (servidor) | 16.x / 17.x |
| pgjdbc | 42.7.8 |
| Kafka clients | 4.0.0 |

- **Archetype:** `kogito-springboot-archetype` · **Starter:** `jbpm-spring-boot-starter`.
- **Add-ons Spring Boot:** `jbpm-addons-springboot-process-management`, `kie-addons-springboot-persistence-jdbc`, `kie-addons-springboot-persistence-mongodb`, `kie-addons-springboot-messaging`, `kie-addons-springboot-events-process-kafka`, `kie-addons-springboot-monitoring-prometheus`.
- REST autogenerado por proceso BPMN: `POST/GET/PUT/PATCH/DELETE` derivados del ID del proceso.

## Mapeo on-premise → Azure (para la demo y el caso de estudio)

| Proyecto hermano (OpenShift on-prem) | Equivalente en Azure |
|---|---|
| OpenShift (cómputo) | Azure Container Apps |
| Registry de imágenes | Azure Container Registry (ACR) |
| PostgreSQL en VM | Azure Database for PostgreSQL Flexible Server |
| MongoDB | Azure Cosmos DB for MongoDB |
| Bus de eventos (Kafka) | Azure Event Hubs (API Kafka) — *opcional en el alcance* |
| Identidad OIDC (Keycloak/RHBK) | Microsoft Entra ID (o Keycloak en contenedor) |
| Secretos | Azure Key Vault |
| Despliegue (Helm / GitLab+Jenkins) | Bicep o Terraform + GitHub Actions |

## Plan por fases

`F0` andamiaje (proceso BPMN del dominio + REST corriendo local) → `F1` Módulo 5 → `F2` Módulo 6 → `F3` demo en Azure → `F4` caso de estudio. Cada fase queda **verificable** antes de pasar a la siguiente.

**Estado actual:** **Módulos 5 y 6 completados y verdes** — `mvn -f ejemplo/pom.xml test` → **13 pruebas** (con Docker activo, Postgres y Mongo corren en contenedores reales vía Testcontainers; sin Docker se omiten solas). Código en `ejemplo/` (paquetes `clientes`, `bitacora`, `batch`, `riesgo`; procesos `solicitudCuenta` y `evaluacionRiesgo`). Material en `material/modulo-5/guia.md` y `material/modulo-6/guia.md`. **Verificado en vivo** (perfil `postgres` contra Postgres+Mongo en Docker: REST crea cliente/proceso, datos persisten en Postgres real, `/actuator/health` UP). **F4 caso de estudio** completado en `docs/caso-de-estudio.md` (arquitectura anonimizada + mapeo a Azure). **F3 andamiaje de Azure listo y verificado** (`ejemplo/Dockerfile` + `.dockerignore`, `azure/infra/main.bicep` + `main.parameters.json`, `.github/workflows/deploy-azure.yml`, guía `azure/README.md`): la imagen se construyó y se corrió como contenedor contra Postgres+Mongo reales tomando las conexiones por **variables de entorno** (modelo idéntico a Container Apps; health UP, escritura persistida en Postgres). Pendiente (opcional, lo ejecuta el usuario): **el despliegue real** a Azure siguiendo `azure/README.md` (`az login`; crea recursos con costo). El código de la app no cambia, solo la configuración de conexión. **Sitio de estudio** para alumnos en `sitio/` (HTML/JS/CSS plano, 17 temas, abre con doble clic / offline, validado con Playwright).

## Convenciones de trabajo

- Todo el contenido, comentarios y commits en **español**. Convertir fechas relativas a absolutas.
- **Anonimización siempre** (ver sección de confidencialidad). El cliente real nunca aparece en este repo.
- Mantener el material **consistente con el código** del directorio `ejemplo/`: si cambia el código, actualizar la guía del tema afectado.
- El ejemplo es material original y anonimizado del curso.
- Commit/push solo cuando se pida.
