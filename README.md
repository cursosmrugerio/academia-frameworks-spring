# Academia — Frameworks Java con Spring (Módulos 5 y 6)

Material de curso para enseñar **frameworks Java (Spring)** con un **ejemplo real**: un sistema de procesos de negocio (BPM) de **banca** construido con **Kogito 10.2.0 sobre Spring Boot**. En lugar de ejemplos sueltos, todos los temas se aprenden sobre un mismo proyecto que crece módulo a módulo y que, al final, se despliega en **Azure**.

## Qué cubre

Este material desarrolla los **Módulos 5 y 6** del temario:

**Módulo 5 — Spring y pruebas**
- JUnit · *Working with assertions* · Mockito
- Spring Core · Spring MVC · Spring Boot · Spring Data JPA

**Módulo 6 — Web, datos y herramientas**
- Spring Batch · Git · Maven
- Base de datos relacional · MongoDB
- HTTP/HTTPS · URIs y *resources* · verbos HTTP · *response codes*
- REST vs SOAP · creación de servicios RESTful con Java

## El ejemplo: un BPM de banca

El hilo conductor es un proceso de **apertura de cuenta / aprobación de crédito**: un cliente solicita, una regla evalúa, una persona aprueba, y el sistema expone todo por REST. Ese único proyecto sirve para ilustrar inyección de dependencias, controllers, persistencia relacional y documental, *batch*, pruebas y servicios web — es decir, todo el temario, pero con sentido de negocio.

> El dominio es genérico y **anonimizado**; no contiene datos de ningún cliente real.

## Organización del repositorio

| Carpeta | Contenido |
|---|---|
| `temario/` | Alcance del temario (Módulos 5 y 6) |
| `ejemplo/` | El proyecto Kogito + Spring Boot que se construye en clase (código que corre) |
| `material/modulo-5/` | Guías y apoyos por tema del Módulo 5 |
| `material/modulo-6/` | Guías y apoyos por tema del Módulo 6 |
| `azure/` | Infraestructura como código y guía para desplegar la demo en Azure |
| `docs/` | Caso de estudio de la arquitectura (anonimizado) |
| `sitio/` | **Sitio de estudio** para alumnos (HTML/JS/CSS; abre `sitio/index.html`) |

## Requisitos

- **Java 21** (JDK)
- **Maven 3.9+**
- **Git**
- Docker (para la base de datos local, a partir del Módulo 6)
- Una suscripción de **Azure** (solo para la fase de despliegue)

## Cómo empezar

**Para estudiar** — abre el sitio de estudio en tu navegador (funciona offline, sin instalar nada):

```bash
open sitio/index.html        # o haz doble clic en el archivo
```

**Para correr el ejemplo:**

```bash
docker compose -f ejemplo/docker-compose.yml up -d
mvn -f ejemplo/pom.xml spring-boot:run -Dspring-boot.run.profiles=postgres
mvn -f ejemplo/pom.xml test
```

## Despliegue en Azure

La aplicación se publica en **Azure Container Apps**, con **Azure Database for PostgreSQL** y **Cosmos DB for MongoDB** como almacenamiento. El detalle (infraestructura como código y pasos) vive en `azure/`.

## Estado

✅ **Módulos 5 y 6 completos**: ejemplo con 13 pruebas verdes, material por módulo, **sitio de estudio** (17 temas) y caso de estudio de arquitectura. El andamiaje de despliegue en Azure está listo; el despliegue real es opcional (ver `azure/`).
