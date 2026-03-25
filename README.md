# ✂️ Saloria - Sistema de Gestión Multi-cliente

🌐 **[Visita la Página Principal de la Aplicación](https://saloria.vercel.app)**

Una plataforma SaaS profesional de nivel empresarial diseñada para gestionar múltiples peluquerías y barberías de forma eficiente. Construida con un backend robusto en **Spring Boot** y un frontend dinámico en **React**.

![Licencia](https://img.shields.io/badge/licencia-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![React](https://img.shields.io/badge/React-19.x-blue)
![Docker](https://img.shields.io/badge/Docker-Listo-blue)

## 📚 Documentación Completa

Puede consultar la documentación técnica, manuales de usuario y guías de desarrollo en la carpeta **[`/docs`](./docs/00-index.md)**.

- [Visión General](./docs/01-vision-general.md)
- [Guía de Usuario](./docs/02-guia-usuario.md)
- [Arquitectura Técnica](./docs/03-arquitectura-tecnica.md)
- [Backend](./docs/04-backend.md) y [Frontend](./docs/05-frontend.md)

## 🌟 Resumen

Este proyecto es una aplicación multi-empresa (multi-tenant) que permite a dueños de negocios gestionar sus empresas (barberías o salones de belleza), servicios, empleados y citas. Incluye CRM, dashboard operativo y una capa B2C ya funcional para explorar negocios, consultar perfiles públicos y reservar como cliente autenticado.

## 🚀 Funcionalidades Principales

- **🏢 Multi-tenancy**: Aislamiento completo de datos entre diferentes empresas/salones.
- **📅 Calendario Interactivo**: Gestión de citas con una interfaz intuitiva.
- **📊 Dashboard de Negocio**: Estadísticas en tiempo real sobre ingresos, citas y servicios más populares.
- **👥 Gestión de Empleados**: Control de horarios, jornadas laborales y asignación de servicios.
- **🛠️ Catálogo de Servicios**: Oferta de servicios personalizada con precios y duraciones dinámicas.
- **🌐 Marketplace y Perfil Público**: Directorio público, buscador y ficha pública por `slug`.
- **🧾 Portal Cliente**: Inicio B2C, búsqueda de negocios, perfil básico e historial de citas.
- **🗓️ Reserva Online**: CTA de reserva conectado a datos reales de servicios, empleados y horarios públicos.
- **🎨 Branding Dinámico**: Soporte para colores de marca e identidad visual específica por empresa.
- **🔐 Acceso Seguro**: Autenticación basada en JWT con control de acceso por roles (`SUPER_ADMIN`, `ADMIN`, `EMPLEADO`, `CLIENTE`).

## 🛠️ Stack Tecnológico

### Backend

- **Java 21 / Spring Boot 3**
- **Spring Security** (Autenticación JWT)
- **Spring Data JPA** (PostgreSQL)
- **Flyway** para migraciones versionadas
- **Maven** para la gestión de dependencias

### Frontend

- **React 19** (Vite)
- **Tailwind CSS** para el diseño
- **React Hook Form** para formularios
- **Framer Motion** para animaciones
- **Recharts** para la visualización de datos
- **Lucide React** para la iconografía

### Infraestructura

- **Docker & Docker Compose**
- **PostgreSQL 15**

---

## 🏗️ Arquitectura y Buenas Prácticas

El proyecto sigue estándares de arquitectura de la industria para asegurar escalabilidad y mantenibilidad:

1. **Arquitectura por Capas**: Separación clara entre Controladores, Servicios y Repositorios.
2. **Patrón DTO**: Desacoplamiento de la capa de API de las entidades de base de datos para mayor seguridad y flexibilidad.
3. **Manejo Global de Excepciones**: Gestión centralizada de errores para respuestas de API consistentes.
4. **Archivado Lógico**: Usuarios y servicios se desactivan sin destruir el histórico.
5. **Lógica de Dominio**: Lógica de negocio encapsulada en servicios para mantener controladores limpios.

---

## 🚦 Primeros Pasos

### Requisitos Previos

- Docker & Docker Compose
- Node.js (para desarrollo local del frontend)
- JDK 21+ (para desarrollo local del backend)

### 🐳 Inicio Rápido (Docker)

La forma más fácil de ejecutar el proyecto completo es usando Docker Compose:

```bash
docker-compose up --build
```

- **Frontend**: [http://localhost:3000](http://localhost:3000)
- **Backend API**: [http://localhost:8080](http://localhost:8080)
- **Base de Datos (Postgres)**: `localhost:5433`

### 💻 Configuración de Desarrollo Local

#### 1. Base de Datos

Ejecuta una instancia de PostgreSQL o usa la que proporciona docker-compose.

#### 2. Backend (`/saloria-api`)

Antes de arrancar el backend define las variables obligatorias:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/saloria_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export JWT_SECRET=dev-only-change-this-secret-before-sharing
```

```bash
cd saloria-api
./mvnw spring-boot:run
```

#### 3. Frontend (`/saloria-client`)

```bash
cd saloria-client
npm install
npm run dev
```

- **Frontend local (Vite)**: [http://localhost:5173](http://localhost:5173)
- **Backend local**: [http://localhost:8080](http://localhost:8080)

### 🍓 Opción Self-Hosted en Raspberry Pi

El repositorio incluye una ruta de despliegue para ejecutar el backend en una Raspberry Pi y mantener el frontend en Vercel.

- Guía operativa: [`deploy/raspberry/README.md`](./deploy/raspberry/README.md)
- Objetivo recomendado: mover primero solo la API para eliminar el cold start de Render
- Base de datos: mantener Supabase al inicio y migrar PostgreSQL a la Raspberry solo en una segunda fase

---

## 🧪 Datos de Prueba (Seeding)

Para ver la aplicación en acción con datos realistas (citas, clientes, estadísticas), el sistema incluye un mecanismo de carga de datos inicial.

> Nota: el bootstrap de `SUPER_ADMIN` ya no se crea automáticamente. Si necesitas una cuenta de emergencia para un entorno controlado, actívala de forma explícita con `APP_BOOTSTRAP_SUPERADMIN_ENABLED=true`, `APP_BOOTSTRAP_SUPERADMIN_EMAIL` y `APP_BOOTSTRAP_SUPERADMIN_PASSWORD`.

---

## 📈 Roadmap (Próximas Funcionalidades)

El producto ya cubre la base operativa B2B y el flujo público principal. Lo siguiente previsto es:

- [ ] 💳 **Pagos y depósitos online**: Integración con Stripe para señal o prepago.
- [ ] 📧 **Notificaciones automáticas**: Confirmaciones y recordatorios por Email y/o WhatsApp.
- [ ] 🧾 **Facturación y tickets PDF**: Emisión automática al completar servicios.
- [ ] 👤 **Portal cliente avanzado**: Reprogramación, cancelación y edición de perfil sin pasar por soporte.
- [ ] 📱 **Aplicación móvil**: Experiencia nativa para clientes finales.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.
