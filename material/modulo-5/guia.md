# Módulo 5 — Spring y pruebas

Guía del instructor. Cada tema del temario se enseña sobre una pieza **real y probada** del proyecto `ejemplo/` (BPM de banca con Kogito sobre Spring Boot). El hilo: una **solicitud de apertura de cuenta** que, al aprobarse, da de alta a un **cliente**.

> Temas: JUnit · Working with assertions · Mockito · Spring Core · Spring MVC · Spring Boot · Spring Data JPA.

## Cómo ejecutar

```bash
# Todas las pruebas del ejemplo
mvn -f ejemplo/pom.xml test

# Una sola clase de prueba (útil para demostrar un tema)
mvn -f ejemplo/pom.xml test -Dtest=ClienteServiceTest

# Levantar la aplicación
mvn -f ejemplo/pom.xml spring-boot:run    # http://localhost:8080
```

## Mapa tema → código

| Tema | Dónde se ve (en `ejemplo/`) |
|---|---|
| Spring Boot | `BancaApplication`, `application.properties`, *slices* de prueba |
| Spring Core (DI) | `clientes/ClienteService` (inyección por constructor) |
| Spring MVC | `clientes/ClienteController` (+ `NuevoClienteRequest`, excepciones) |
| Spring Data JPA | `clientes/Cliente`, `clientes/ClienteRepository` |
| JUnit | las tres clases `…Test` |
| Working with assertions | AssertJ en `ClienteServiceTest`, `ClienteRepositoryTest` |
| Mockito | `ClienteServiceTest` (`@Mock`/`@InjectMocks`), `ClienteControllerTest` (`@MockBean`) |

---

## 1. Spring Boot

Spring Boot arranca la aplicación con **autoconfiguración** y un servidor embebido (Tomcat). El punto de entrada:

```java
@SpringBootApplication(scanBasePackages = { "org.kie.kogito.**", "mx.academia.banca.**" })
public class BancaApplication { public static void main(String[] a) { SpringApplication.run(BancaApplication.class, a); } }
```

Puntos para clase:
- Una sola anotación (`@SpringBootApplication`) habilita componentes, autoconfiguración y escaneo de paquetes.
- El `pom.xml` hereda de `spring-boot-starter-parent` (gestiona versiones) y declara *starters* (`web`, `data-jpa`, `validation`, `test`).
- **Actuator** expone endpoints de operación (`/actuator`). Útil para hablar de "production-ready".
- Las pruebas usan *slices* (`@WebMvcTest`, `@DataJpaTest`) que cargan **solo** la porción necesaria del contexto: más rápidas y enfocadas.

## 2. Spring Core — Inyección de dependencias (IoC/DI)

El contenedor de Spring construye los objetos y les **inyecta** sus colaboradores. `ClienteService` recibe su repositorio por **constructor** (la forma recomendada):

```java
@Service
public class ClienteService {
    private final ClienteRepository repositorio;
    public ClienteService(ClienteRepository repositorio) { this.repositorio = repositorio; }
    ...
}
```

Puntos para clase:
- `@Service` registra la clase como *bean*; Spring resuelve `ClienteRepository` (otro bean) y lo pasa al constructor.
- Inyección por constructor → `final`, objeto siempre en estado válido, y **fácil de probar** (puedo pasar un mock en una prueba unitaria, sin Spring).
- Contraste con inyección por campo (`@Autowired` en un atributo): más difícil de probar.

## 3. Spring MVC — servicios REST

`ClienteController` expone la API HTTP. Aquí viven verbos, URIs, *response codes* y validación (que se profundizan en el Módulo 6):

```java
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @PostMapping
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody NuevoClienteRequest p, UriComponentsBuilder uri) {
        Cliente creado = servicio.registrar(p.aCliente());
        return ResponseEntity.created(uri.path("/api/clientes/{id}").buildAndExpand(creado.getId()).toUri()).body(creado);
    }
    @GetMapping("/{id}") public Cliente porId(@PathVariable Long id) { return servicio.porId(id); }
}
```

Puntos para clase:
- `@RestController` + `@RequestMapping` + `@PostMapping/@GetMapping` mapean URLs a métodos.
- **Response codes** vía `ResponseEntity` (201 Created con `Location`) y vía excepciones anotadas: `ClienteNoEncontradoException` → 404, `CorreoDuplicadoException` → 409.
- **Validación**: `@Valid` sobre `NuevoClienteRequest` (con `@NotBlank`, `@Email`) → 400 automático si falla.
- Esta API **convive** con los endpoints REST que Kogito autogenera para el proceso (`/solicitudCuenta`) — buen puente al Módulo 6.

## 4. Spring Data JPA

Persistencia de la entidad de negocio `Cliente` sin escribir SQL ni el CRUD:

```java
@Entity @Table(name = "cliente")
public class Cliente { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id; /* ... */ }

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCorreo(String correo);   // query method (derivado del nombre)
    boolean existsByCorreo(String correo);
}
```

Puntos para clase:
- `@Entity` mapea la clase a una tabla; `JpaRepository` da `save/findById/findAll/...` gratis.
- **Query methods**: Spring genera la consulta a partir del nombre del método (`findByCorreo`).
- En desarrollo/pruebas la base es **H2 en memoria** (ver `application.properties`); en el Módulo 6 se cambia a **PostgreSQL**.
- **Importante (didáctico):** esta es la persistencia **de negocio**. El **motor BPM (Kogito)** tiene su *propia* persistencia (hoy en memoria) — son dos cosas distintas que conviene separar al explicar.

## 5. JUnit 5

Estructura de las pruebas (`@Test`, ciclo de vida, *runner* por extensión):

```java
@ExtendWith(MockitoExtension.class)   // integra Mockito con JUnit 5
class ClienteServiceTest {
    @Test void registrarAsignaCuentaEstatusYFecha() { /* ... */ }
}
```

Puntos para clase:
- JUnit 5 (Jupiter): `@Test`, `@ExtendWith`, aserciones.
- Spring Boot trae JUnit, AssertJ y Mockito en `spring-boot-starter-test`.
- Tipos de prueba en el ejemplo: **unitaria** (`ClienteServiceTest`, sin Spring), **de slice** (`ClienteRepositoryTest`, `ClienteControllerTest`) y **de integración completa** (`SolicitudCuentaProcessTest`).

## 6. Working with assertions (AssertJ)

Aserciones fluidas y legibles:

```java
assertThat(resultado.getEstatus()).isEqualTo("ACTIVO");
assertThat(resultado.getNumeroCuenta()).isNotBlank().hasSize(10);
assertThatThrownBy(() -> servicio.porId(99L)).isInstanceOf(ClienteNoEncontradoException.class);
```

Puntos para clase:
- Estilo `assertThat(actual).isEqualTo(...)` encadenable; mensajes de error claros.
- `assertThatThrownBy(...)` para verificar excepciones.
- Comparar con las aserciones clásicas de JUnit (`assertEquals`) — ambas presentes en el proyecto.

## 7. Mockito

Aislar la unidad bajo prueba reemplazando sus colaboradores por **mocks**:

```java
@Mock ClienteRepository repositorio;          // doble de prueba
@InjectMocks ClienteService servicio;          // se le inyecta el mock

when(repositorio.existsByCorreo("ana@example.com")).thenReturn(false);
when(repositorio.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
// ...
verify(repositorio).save(resultado);           // verificar interacción
verify(repositorio, never()).save(any());      // …o su ausencia
```

Puntos para clase:
- `@Mock` crea el doble; `@InjectMocks` lo inyecta en el objeto real → prueba **sin** base de datos ni Spring.
- `when(...).thenReturn/thenAnswer(...)` define el comportamiento; `verify(...)` comprueba llamadas.
- En la capa web, `@WebMvcTest` + `@MockBean ClienteService` mockean el servicio para probar **solo** el controller (ver `ClienteControllerTest`).
- *(Nota técnica: con JDK recientes Mockito emite un aviso de "self-attaching"; es inofensivo. La forma moderna de `@MockBean` es `@MockitoBean`.)*

---

## Estado y siguiente módulo

El código de este módulo está en `ejemplo/` (paquete `mx.academia.banca.clientes`) y **7 pruebas en verde** lo respaldan. 

En el **Módulo 6** se continúa sobre el mismo ejemplo: Maven y Git, base de datos relacional (**PostgreSQL**) y **MongoDB**, HTTP/REST a fondo (incluido un **service task que invoca un servicio**, reubicado aquí), y REST vs SOAP.
