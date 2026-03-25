# 📑 Contratos de API (API Contract)

Este documento detalla los endpoints expuestos por el backend de **Saloria**. Sirve como contrato de integración para el desarrollo del client-side (frontend o apps móviles).

> **Nota de Seguridad**: Todos los endpoints que no sean explícitamente públicos requieren un token JWT válido en la cabecera `Authorization: Bearer <JWT_TOKEN>`.
> Además, la autorización aplica controles multi-tenant y de ownership mediante `SecurityService`, `@PreAuthorize` y validaciones de servicio. No basta con conocer un ID para operar sobre recursos ajenos.

---

## 🔒 1. Autenticación (`AuthController`)

**Base Path:** `/auth`

### Registrar nueva empresa y administrador

- **Método:** `POST /auth/register`
- **Autorización:** *Público*
- **Descripción:** Crea una nueva empresa junto con su cuenta de administrador inicial en un solo paso.
- **Restricción:** Si `enterpriseName` ya existe, el endpoint debe rechazar la petición con conflicto; el alta pública no puede reutilizar una empresa existente.
- **Comportamiento adicional:** En altas profesionales, el backend genera automáticamente un `slug` único a partir del nombre del negocio y reutiliza el email del registro como contacto inicial de la empresa.
- **Body Esperado:** `RegisterRequest`
- **Respuesta:** `AuthResponse` (contiene `token`) o `409 Conflict`

### Iniciar sesión

- **Método:** `POST /auth/login`
- **Autorización:** *Público*
- **Descripción:** Autentica a un usuario usando email y contraseña.
- **Body Esperado:** `AuthRequest`
- **Respuesta:** `AuthResponse` (contiene `token`)

---

## 🌍 2. Público (`PublicController`)

**Base Path:** `/api/public`
**Propósito:** Endpoints de lectura pública para marketplace, perfil público y horarios visibles del negocio.

### Directorio público de empresas

- **Método:** `GET /api/public/enterprises`
- **Descripción:** Lista negocios visibles en el marketplace y permite filtrar con `?q=` por nombre, dirección o servicios.
- **Restricción:** Solo aparecen empresas con perfil público listo (`slug`, dirección, descripción y al menos un canal público de contacto).
- **Respuesta:** Lista de `PublicEnterpriseSummaryResponse`

### Obtener empresa por slug

- **Método:** `GET /api/public/enterprises/slug/{slug}`
- **Descripción:** Recupera la información pública de una empresa a partir de su `slug`.
- **Respuesta:** `EnterpriseResponse` (incluye `readiness` cuando aplica)

### Listar servicios de empresa

- **Método:** `GET /api/public/enterprises/{id}/services`
- **Descripción:** Obtiene el catálogo completo de servicios activos que ofrece una empresa.
- **Respuesta:** Lista de `ServiceOfferingResponse`

### Listar empleados de empresa

- **Método:** `GET /api/public/enterprises/{id}/employees`
- **Descripción:** Obtiene la lista de empleados visibles para reserva.
- **Respuesta:** Lista de `UserResponse`

### Consultar horarios públicos de empresa

- **Método:** `GET /api/public/enterprises/{id}/working-hours`
- **Descripción:** Devuelve un snapshot de solo lectura de los horarios generales publicados de la empresa. Si todavía no hay filas persistidas, responde con el horario por defecto sin crear registros.
- **Respuesta:** Lista de `WorkingHourDTO`

### Consultar horarios públicos de un profesional

- **Método:** `GET /api/public/enterprises/{enterpriseId}/employees/{userId}/working-hours`
- **Descripción:** Devuelve la disponibilidad efectiva del profesional para la reserva web. Prioriza su horario propio y, si no existe, cae al horario general de la empresa.
- **Respuesta:** Lista de `WorkingHourDTO`

> Importante: estos endpoints son públicos y de solo lectura. La creación real de una reserva requiere un usuario autenticado con rol `CLIENTE`.

---

## 👥 3. Usuarios y Empleados (`UserController`)

**Base Path:** `/api/users`

### Listar usuarios de una empresa

- **Método:** `GET /api/users/{enterpriseId}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Obtiene la lista de usuarios operativos asociados a la empresa.
- **Respuesta:** Lista de `UserResponse`

### Crear nuevo usuario

- **Método:** `POST /api/users`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Permite registrar un nuevo empleado dentro de la empresa.
- **Body Esperado:** `CreateUserRequest`
- **Respuesta:** `UserResponse`

### Actualizar usuario

- **Método:** `PUT /api/users/{id}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Actualiza datos personales, rol o información profesional de un usuario existente.
- **Body Esperado:** `UpdateUserRequest`
- **Respuesta:** `UserResponse`

### Archivar usuario

- **Método:** `DELETE /api/users/{id}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Archiva el usuario (`active=false`, `archived=true`) sin destruir histórico de citas y clientes.

---

## 🏢 4. Empresas (`EnterpriseController`)

**Base Path:** `/api/enterprises`

### Listar todas las empresas

- **Método:** `GET /api/enterprises`
- **Autorización:** `ROLE_SUPER_ADMIN`
- **Descripción:** Obtiene un directorio de empresas registradas.
- **Respuesta:** Lista de `EnterpriseResponse`

### Crear nueva empresa internamente

- **Método:** `POST /api/enterprises`
- **Autorización:** `ROLE_SUPER_ADMIN`
- **Body Esperado:** `EnterpriseRequest`
- **Respuesta:** `EnterpriseResponse`

### Detalles de empresa

- **Método:** `GET /api/enterprises/{id}`
- **Respuesta:** `EnterpriseResponse` con `readiness`:
  - `publicProfileReady`
  - `bookingReady`
  - `publicProfilePath`
  - `missingPublicProfile`
  - `missingBookingSetup`

### Actualizar empresa

- **Método:** `PUT /api/enterprises/{id}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Body Esperado:** `EnterpriseRequest`
- **Descripción:** Si `slug` llega vacío o ausente, el backend genera o conserva uno automáticamente a partir del nombre. Si llega informado, lo normaliza y asegura unicidad.
- **Respuesta:** `EnterpriseResponse`

### Empleados internos

- **Método:** `GET /api/enterprises/{id}/employees`
- **Descripción:** Obtiene empleados internos de la empresa.
- **Respuesta:** Lista de `UserResponse`

---

## 📊 5. Dashboard y Métricas (`DashboardController`)

**Base Path:** `/api/dashboard`

### Obtener métricas rápidas

- **Método:** `GET /api/dashboard/stats/{enterpriseId}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Calcula total de citas del día, ingresos estimados del mes y estadísticas del negocio.
- **Respuesta:** `DashboardStatsDTO`

---

## ✂️ 6. Servicios Ofrecidos (`ServiceOfferingController`)

**Base Path:** `/api/services`

### Listar catálogo de la empresa

- **Método:** `GET /api/services/{enterpriseId}`
- **Respuesta:** Lista de `ServiceOfferingResponse`

### Crear nuevo servicio

- **Método:** `POST /api/services`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Crea un nuevo servicio y permite subir una imagen opcional.
- **Body/Form-data:** `service` (`ServiceOfferingRequest`, serializado como JSON) e `image` (`MultipartFile` opcional)
- **Respuesta:** `ServiceOfferingResponse`

### Detalle de servicio

- **Método:** `GET /api/services/{enterpriseId}/{id}`
- **Descripción:** Devuelve un servicio solo si pertenece a la empresa indicada en la ruta.
- **Respuesta:** `ServiceOfferingResponse`

### Eliminar servicio

- **Método:** `DELETE /api/services/{enterpriseId}/{id}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Retira el servicio del catálogo mediante borrado lógico (`deleted=true`) solo si pertenece a la empresa indicada en la ruta.

---

## 📅 7. Citas y Facturación (`AppointmentController`)

**Base Path:** `/api/appointments`

### Crear cita

- **Método:** `POST /api/appointments`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`, `ROLE_CLIENTE`
- **Body Esperado:** `CreateAppointmentRequest`
- **Descripción:** Los perfiles admin crean citas internas para su empresa. Los clientes autenticados solo pueden crear reservas para su propio `userId`.
- **Respuesta:** `AppointmentResponse`

### Mis citas

- **Método:** `GET /api/appointments/me`
- **Descripción:** Lista las citas del usuario autenticado.
- **Respuesta:** Lista de `AppointmentResponse`

### Listar todas las citas

- **Método:** `GET /api/appointments?enterpriseId={id}`
- **Descripción:** Recupera todas las reservas registradas en una empresa.
- **Respuesta:** Lista de `AppointmentResponse`

### Citas por empleado

- **Método:** `GET /api/appointments/employee/{employeeId}`
- **Descripción:** Lista las citas de un empleado específico.
- **Respuesta:** Lista de `AppointmentResponse`

### Checkout (Pagar cita)

- **Método:** `POST /api/appointments/{id}/checkout`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Body:** `{"paymentMethod":"CASH|CARD|TRANSFER"}`
- **Descripción:** Cambia el estado de la cita a completada y registra el pago.
- **Respuesta:** `AppointmentResponse`

### Transacciones

- **Método:** `GET /api/appointments/transactions?enterpriseId={id}&start={isoDateTime}&end={isoDateTime}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Devuelve citas pasadas filtradas por rango temporal para historial y contabilidad.
- **Respuesta:** Lista de `AppointmentResponse`

### Resumen de facturación

- **Método:** `GET /api/appointments/billing-summary?enterpriseId={id}`
- **Autorización:** `ROLE_ADMIN`, `ROLE_SUPER_ADMIN`
- **Descripción:** Suma el total ingresado a partir de las citas cobradas.
- **Respuesta:** `BillingSummaryDTO`

---

## ⏳ 8. Horarios de Trabajo (`WorkingHourController`)

**Base Path:** `/api/working-hours`

### Horario de la empresa

- **Método:** `GET /api/working-hours/enterprise/{enterpriseId}`
- **Descripción:** Devuelve un snapshot de solo lectura de los horarios de apertura globales. Si la empresa todavía no ha guardado configuración, responde con el horario por defecto sin persistir nada.
- **Respuesta:** Lista de `WorkingHourDTO`

### Horario de empleado

- **Método:** `GET /api/working-hours/user/{userId}`
- **Descripción:** Consulta la disponibilidad efectiva de un empleado. Si no tiene un horario propio guardado, responde con el horario general de la empresa para que el panel pueda editar sin side effects.
- **Respuesta:** Lista de `WorkingHourDTO`

### Guardar horarios masivamente (batch)

- **Método:** `PUT /api/working-hours/batch`
- **Descripción:** Reemplaza o inserta jornada laboral en lote. Cuando llega un día sin `id`, el backend hace upsert por alcance (`empresa` o `empleado`) y día para evitar duplicados.
- **Restricción:** Todos los elementos del batch deben pertenecer a una empresa sobre la que el usuario tenga acceso; no basta con que el primer elemento sea válido.
- **Body:** Lista de `WorkingHourDTO`
- **Respuesta:** Lista de `WorkingHourDTO`

---

## 🤵 9. Clientes (`CustomerController`)

**Base Path:** `/api/customers`

### Listar clientes

- **Método:** `GET /api/customers/enterprise/{enterpriseId}`
- **Descripción:** Listado de clientes de la empresa.
- **Respuesta:** Lista de `CustomerResponse`

### Expediente y detalles de cliente

- **Método:** `GET /api/customers/{id}`
- **Descripción:** Obtiene los detalles de contacto e historial de citas vinculadas.
- **Respuesta:** `CustomerDetailResponse`

### Actualizar cliente

- **Método:** `PATCH /api/customers/{id}`
- **Descripción:** Actualiza datos parciales del cliente, como notas internas o etiquetas.
- **Body:** `UpdateCustomerRequest`
- **Respuesta:** `CustomerResponse`

---

## 📝 Notas de Integración

- El marketplace y la búsqueda pública consumen `GET /api/public/enterprises`.
- El perfil público consume `slug`, `services`, `employees` y `working-hours`.
- El CTA `Reservar Ahora` redirige a login si el usuario no está autenticado.
- La reserva online pública actual no soporta invitados anónimos: requiere cuenta `CLIENTE`.
