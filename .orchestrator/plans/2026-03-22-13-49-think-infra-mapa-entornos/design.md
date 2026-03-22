# Design: Mapa conceptual de infraestructura actual

## Architecture Overview
La aplicacion sigue un monorepo con dos artefactos desplegables:

- **Backend**: `peluqueria-api`, construido con Docker y desplegado en Render.
- **Frontend**: `peluqueria-client`, construido con Vite y desplegado aparte en Vercel o servido con Nginx en Docker local.
- **Base de datos**: PostgreSQL; local via Docker Compose y cloud via variable `SPRING_DATASOURCE_URL`.
- **Archivos**: estrategia conmutable entre filesystem local y Supabase Storage.

## Environment Matrix
| Entorno | Frontend | Backend | Base de Datos | Files | Fuente de verdad |
|--------|----------|---------|---------------|-------|------------------|
| Local Docker | Contenedor Nginx en `:3000` | Contenedor Spring en `:8080` | Postgres Docker en `:5433` | `uploads/` local del contenedor | `docker-compose.yml` |
| Desarrollo local | Vite en `:5173` | `spring-boot:run` en `:8080` | Docker DB o Postgres externo | `uploads/` local | `docs/08` + defaults de `application.properties` |
| Staging | Vercel project/config externa | Render service `peluqueria-saas-staging-fra` | Segun doc: PostgreSQL en Render | Probablemente configurable por ENV | `render.yaml` + dashboard |
| Produccion | Vercel project/config externa | Render service `peluqueria-saas-prod-fra` | Segun doc: Supabase Postgres | Probablemente Supabase Storage o filesystem si no se cambia | `render.yaml` + dashboard |

## Data Model (Infra)
| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| Render Service | name, branch, rootDir, region, envVars | Apunta a `peluqueria-api` |
| Vercel Project | rootDir, build envs | Apunta a `peluqueria-client` |
| Spring Runtime Config | datasource, cors, jwt, storage, api base url | Define comportamiento del backend por entorno |
| Client Runtime Build Config | `VITE_API_BASE_URL` | Define a que backend llama el frontend |

## API Contracts (Infra-related)
| Method | Path | Body | Response | Auth |
|--------|------|------|----------|------|
| `GET` | `/uploads/{file}` | n/a | Asset publico si storage local | Public |
| `GET` | `/v3/api-docs` | n/a | OpenAPI | Public |
| `POST` | `/auth/**` | Credenciales | JWT | Public |

## Component Design (Operacional)
### Runtime Flow
1. El frontend se construye con una `VITE_API_BASE_URL`.
2. El usuario navega la SPA.
3. La SPA llama al backend Render/local via Axios.
4. Spring resuelve datasource, CORS, JWT y storage desde variables del entorno.
5. PostgreSQL persiste dominio; archivos salen por `/uploads/**` o URL publica de Supabase.

## File Structure
```text
/
├── render.yaml                  # Servicios backend staging/prod en Render
├── docker-compose.yml           # Stack local completo
├── docs/07-infraestructura.md   # Vision documental de entornos
├── docs/08-guia-contribucion.md # Flujo desarrollo local
├── peluqueria-api/
│   ├── Dockerfile
│   ├── src/main/resources/application.properties
│   └── src/main/java/com/peluqueria/config/
└── peluqueria-client/
    ├── Dockerfile
    ├── .env
    ├── .env.production
    └── src/lib/axios.ts
```

## Dependencies
- **Existentes para infraestructura**: Docker, PostgreSQL, Spring Boot, Vite, Nginx.
- **Servicios externos previstos**: Render, Vercel, Supabase.

## Testing Strategy
- Unitario backend: H2 con `application-test.properties`.
- Unitario frontend: Vitest con `jsdom`.
- Operacional: verificar que cada entorno expone frontend, backend y conectividad DB correctos.
