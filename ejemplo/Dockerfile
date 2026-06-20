# Imagen del ejemplo BPM (Kogito sobre Spring Boot) — build multietapa.
#
# Etapa 1: compila con Maven + JDK 21 (incluye la generación de código de Kogito).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Cachear dependencias primero (mejor uso de capas)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
# Compilar y empaquetar el jar ejecutable
COPY src ./src
RUN mvn -q -B -DskipTests package

# Etapa 2: imagen ligera de runtime (solo JRE 21).
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ejemplo-*.jar app.jar
EXPOSE 8080
# Las conexiones (PostgreSQL, MongoDB) se inyectan por variables de entorno en Azure.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
