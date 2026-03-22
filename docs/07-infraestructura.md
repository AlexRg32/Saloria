# ☁️ Infraestructura y Despliegue

La aplicación es un sistema **cloud-native** preparado para escalar horizontalmente mediante contenedores Docker.

## ✅ Estado Verificado del Despliegue

> Última verificación manual: **2026-03-22**

La infraestructura activa de la aplicación queda así:

| Capa | Entorno | Plataforma | Identificador / URL | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | Producción | Vercel | `peluqueria-saas-three.vercel.app` | Activo |
| **Frontend** | Staging / Preview | Vercel | `peluqueria-saas-git-staging-alexrg32s-projects.vercel.app` | Activo |
| **Backend** | Producción | Render | `peluqueria-saas-prod-fra` → `https://peluqueria-saas-prod-fra.onrender.com` | Activo |
| **Backend** | Staging | Render | `peluqueria-saas-staging-fra` → `https://peluqueria-saas-staging-fra.onrender.com` | Activo |
| **Base de Datos** | Producción | Supabase | `peluqueria-saas` (`xwnumlcqnrwhgpbrldhf`) | Activo |
| **Base de Datos** | Otro proyecto Supabase | Supabase | `clases-claudia` (`cvrezcodldsscprjsvas`) | Activo |
| **Servicios antiguos** | Legacy | Render Oregon | `peluqueria-saas`, `peluqueria-saas-prod`, `peluqueria-saas-api-staging` | Suspendidos |

### Resumen Ejecutivo

- **Producción real**:
  - Frontend en **Vercel**
  - Backend en **Render Frankfurt**
  - Base de datos en **Supabase Frankfurt**
- **Staging real**:
  - Frontend en **Vercel Preview/Staging**
  - Backend en **Render Frankfurt**
- **Infra legacy** en Render Oregon sigue existiendo, pero está **suspendida** y no forma parte de la ruta activa.

## 🐳 Docker (Entorno Local)

Para el desarrollo y pruebas locales, se orquesta el sistema completo con `docker-compose`.

```yaml
version: '3'
services:
  db:
    image: postgres:15
    ports: ["5433:5432"]
    volumes: [postgres_data:/var/lib/postgresql/data]
    
  app:
    build: ./peluqueria-api
    ports: ["8080:8080"]
    depends_on: [db]
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/peluqueria_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - CORS_ALLOWED_ORIGINS=http://localhost:3000
      
  client:
    build: 
      context: ./peluqueria-client
      args:
        - VITE_API_BASE_URL=http://localhost:8080
    ports: ["3000:80"]
    depends_on: [app]
```

### Comandos Útiles

- **Arrancar**: `docker-compose up --build`
- **Tirar abajo**: `docker-compose down`
- **Logs**: `docker-compose logs -f app`

## 💻 Desarrollo Local

El proyecto soporta dos formas principales de desarrollo:

### 1. Stack completo en Docker

- Frontend: `http://localhost:3000`
- Backend: `http://localhost:8080`
- PostgreSQL: `localhost:5433`

### 2. Desarrollo mixto

- Base de datos en Docker: `docker-compose up -d db`
- Backend con Spring Boot: `./mvnw spring-boot:run`
- Frontend con Vite: `npm run dev`
- En este modo, el frontend suele correr en `http://localhost:5173`

## ⚙️ Configuración Real por Entorno

El backend **no** usa archivos separados `application-staging.properties` o `application-prod.properties`.
Actualmente, staging y producción comparten el mismo binario y se diferencian por:

- rama desplegada (`staging` o `main`)
- variables de entorno en Render
- URL del frontend que se conecta a cada entorno
- configuración de base de datos y storage

Las variables más importantes son:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `APP_API_BASE_URL`
- `APP_STORAGE_TYPE`
- `SUPABASE_URL`
- `SUPABASE_KEY`
- `SUPABASE_BUCKET`

## 🌍 Entornos de Despliegue

El proyecto cuenta con dos entornos totalmente aislados para garantizar la estabilidad de la producción.

| Característica | **Staging** (Pre-producción) | **Production** (Producción) |
| :--- | :--- | :--- |
| **Rama Git** | `staging` | `main` |
| **Base de Datos** | PostgreSQL (configurada por variables de entorno) | PostgreSQL (Supabase) |
| **Backend API** | Render (Frankfurt-fra) | Render (Frankfurt-fra) |
| **Frontend** | Vercel (Staging) | Vercel (Production) |
| **Propósito** | Pruebas y validación | Uso por clientes reales |

### 1. Base de Datos

- **Producción**: La base activa identificada para esta app en Supabase es `peluqueria-saas` con `project_ref` `xwnumlcqnrwhgpbrldhf`.
- **Staging**: El código y Render permiten separar staging por variables de entorno, pero el repositorio no versiona la URL exacta del datasource de staging.
- **Compatibilidad**: El backend soporta URLs JDBC normales y también URLs estilo `postgres://` o `postgresql://`, incluidas configuraciones de Supabase.

### 2. Backend API

Desplegado en Render mediante contenedores.

- **Variables de Entorno Críticas**:
  - `SPRING_DATASOURCE_URL`: String de conexión JDBC (específico de cada entorno).
  - `JWT_SECRET`: Clave secreta (debe ser distinta en cada entorno).
  - `CORS_ALLOWED_ORIGINS`: URLs permitidas del frontend correspondiente. Para soportar previsualizaciones de Vercel, se recomienda usar patrones: `https://*.vercel.app,https://tu-dominio.com`.

### 4. Configuración del Monorepo (Importante)

Al ser un monorepo, Render requiere configuraciones específicas para encontrar el código del backend:

- **Root Directory**: Debe establecerse en `peluqueria-api`.
- **Dockerfile Path**: Render lo buscará dentro del `rootDir`, por lo que el path relativo debe ser `./Dockerfile` (si está en la raíz de `peluqueria-api`).

#### Troubleshooting: Error de Lectura de Dockerfile

Si el despliegue falla con: `error: failed to solve: failed to read dockerfile: open Dockerfile: no such file or directory`, verifica que el **Root Directory** en el dashboard de Render no esté vacío.

### 5. Render Blueprints (`render.yaml`)

El proyecto utiliza **Infrastructure as Code (IaC)** mediante el archivo `render.yaml` en la raíz para unificar la configuración de los entornos.

```yaml
services:
  - type: web
    name: peluqueria-saas-prod-fra
    runtime: docker
    rootDir: peluqueria-api # Crucial para el monorepo
    region: frankfurt
```

Para aplicar cambios globales a la infraestructura, se recomienda editar este archivo en lugar de usar el dashboard manual.

### 3. Frontend

Desplegado en Vercel.

- **Variables de Entorno**:
  - `VITE_API_BASE_URL`: URL del backend (Staging o Producción).

### 4. Storage de Archivos

La aplicación soporta dos estrategias:

- **Local filesystem**: modo por defecto en desarrollo, usando la carpeta `uploads/`
- **Supabase Storage**: modo opcional para despliegues persistentes mediante `APP_STORAGE_TYPE=supabase`

Esto evita el problema de pérdida de archivos cuando se usa filesystem local dentro de contenedores efímeros.

## 🚀 Estrategia de Ramas y CI/CD

El flujo de trabajo sigue el modelo de promoción de entornos:

1. **Desarrollo**: Se crean ramas `feat/`, `fix/`, etc. a partir de `staging`.
2. **Promoción a Staging**: Al terminar una tarea, se integra en `staging` mediante el comando `/ship`.
3. **Validación**: Los cambios se prueban en el entorno de Staging.
4. **Promoción a Producción**: Una vez validados, los cambios se integran de `staging` a `main` (Despliegue automático a producción).
5. **Pipeline real observado**:
   - **Render**: `autoDeploy` activado para staging y producción.
   - **Vercel**: despliegue automático asociado al proyecto y sus aliases.
   - **Repositorio**: no hay workflows versionados en `.github/workflows` en el momento de esta verificación.

## 🧭 Notas Operativas Importantes

- La separación entre staging y producción depende principalmente de **variables de entorno externas** y no de perfiles Spring versionados por archivo.
- El frontend de producción público es `peluqueria-saas-three.vercel.app`.
- El backend de producción público es `peluqueria-saas-prod-fra.onrender.com`.
- Hay servicios antiguos en Oregon suspendidos por el usuario que conviene no confundir con la infraestructura activa actual.

> [Siguiente: Guía de Contribución](./08-guia-contribucion.md)
