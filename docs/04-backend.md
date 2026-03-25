# ⚙️ Backend (API & Lógica de Servidor)

El backend de la aplicación está construido con Java 21 y Spring Boot 3, siguiendo principios RESTful y una arquitectura de capas limpia.

## 📂 Estructura de Paquetes

La aplicación reside en `com.saloria` y se divide en subpaquetes funcionales:

- **`config`**: Configuraciones generales (CORS, Security, Database).
- **`controller`**: Puntos de entrada de la API. Responsables de recibir JSON, validar y responder.
- **`service`**: Lógica de negocio. Donde ocurren los cálculos y reglas.
- **`repository`**: Definición de consultas a base de datos (Interfaces JPA).
- **`model`**: Entidades de base de datos (JPA Entities).
- **`dto`**: Data Transfer Objects. Objetos para intercambio seguro de información.
- **`exception`**: Manejadores globales de errores (`@ControllerAdvice`).

## 🔑 Autenticación y Seguridad

La seguridad está gestionada por **Spring Security**.

### Flujo de Autenticación

1. **Registro/Login**: Gestionado por `AuthController` y `AuthenticationService`.
2. **Validación**: Se usa `JWTAuthenticationFilter` para interceptar cada petición.
3. **Autorización**: Anotaciones `@PreAuthorize` en los controladores aseguran que solo usuarios con el rol adecuado accedan (e.g., solo ADMIN puede borrar usuarios).

### Archivos Clave

- `SecurityConfig.java`: Configuración de cadenas de filtros de seguridad.
- `JwtUtil.java`: Generación y validación de tokens JWT.
- `SecurityService.java`: Reglas de autorización multi-tenant y ownership checks.

## 📡 API Endpoints y Contratos

Los contratos detallados (Endopoints, Métodos, Requests, Responses) se han abstraído en un documento exhaustivo propio.

🔗 **[Ver Contratos de API](./10-api-contract.md)**

## 💾 Modelado y Migraciones de Datos

El sistema utiliza JPA (Java Persistence API) con Hibernate como implementación para el modelado.

### Migraciones con Flyway

Las migraciones de bases de datos son controladas por **Flyway**.

- **Ruta de los scripts**: `src/main/resources/db/migration/`
- **Baseline**: La propiedad `spring.flyway.baseline-on-migrate=true` está configurada para crear automáticamente la tabla del historial (`flyway_schema_history`) sobre instalaciones previas sin requerir un vaciado de la base de datos.
- **Validación estricta**: El proyecto utiliza `spring.jpa.hibernate.ddl-auto=validate` para garantizar que el modelo de datos coincida siempre exactamente con el esquema SQL, deshabilitando actualizaciones de esquema automáticas e inseguras en producción.

### Detalles de JPA

- **Estrategia de Ids**: `GenerationType.IDENTITY` (Auto-increment).
- **Relaciones**:
  - `Enterprise` 1:N `User` (Empleados/Admins)
  - `User` 1:N `Appointment` (Como empleado)
  - `Customer` 1:N `Appointment`
  - `ServiceOffering` 1:N `Appointment`
- **Archivado / Soft Delete**:
  - `User` conserva histórico mediante `archived=true` y `active=false`.
  - `ServiceOffering` se retira con `deleted=true`.
  - La autenticación y los listados activos excluyen usuarios archivados o inactivos.

> [Siguiente: Frontend](./05-frontend.md)
