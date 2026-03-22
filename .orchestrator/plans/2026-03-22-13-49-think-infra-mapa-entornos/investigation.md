# Investigation: Infraestructura actual y mapa de entornos

## Summary
Analisis read-only para describir como esta montada hoy la infraestructura de la aplicacion, donde vive cada pieza en local, desarrollo, staging y produccion, y que partes dependen de codigo frente a configuracion externa.

## Current State
- **Tech Stack**: Monorepo con backend Spring Boot y frontend React/Vite.
- **Relevant Code**: `render.yaml`, `docker-compose.yml`, `peluqueria-api/src/main/resources/application.properties`, `peluqueria-api/src/main/java/com/peluqueria/config/*`, `peluqueria-client/.env*`, `peluqueria-client/src/lib/axios.ts`.
- **Architecture**: Monorepo con despliegue separado por contenedor para backend y despliegue independiente del frontend en Vercel.

## Requirements
### Functional
- [x] Identificar local, desarrollo, staging y produccion.
- [x] Localizar backend, frontend, base de datos y almacenamiento de archivos.
- [x] Diferenciar entre configuracion declarada en repo y configuracion que depende del dashboard o variables de entorno.

### Non-Functional
- Seguridad: separar secretos por entorno y limitar CORS por origen.
- Operabilidad: evitar configuraciones duplicadas o divergentes entre docs y codigo.

## Scope
### In Scope
- Docker local.
- Backend en Render.
- Frontend en Vercel.
- Base de datos PostgreSQL.
- Almacenamiento de archivos local/Supabase.
- Estrategia de ramas y despliegue.

### Out of Scope
- Estado real del dashboard de Vercel/Render en tiempo real.
- Valores actuales de secretos de produccion.
- Costes, observabilidad y backups efectivos del proveedor.

## Risks
| Risk | Impact | Mitigation |
|------|--------|------------|
| `SPRING_PROFILES_ACTIVE` se define en `render.yaml` pero no hay `application-prod` ni `application-staging` en repo | La separacion entre entornos depende casi por completo de variables externas | Mantener un inventario claro de env vars y, si hace falta, crear perfiles reales |
| La doc dice que hay GitHub Actions, pero no existe `.github/workflows` | Puede asumirse un CI/CD automatizado que no esta versionado aqui | Tratar Render/Vercel auto-deploy como mecanismo real actual |
| El frontend solo trae `.env.production` y no hay config de staging en repo | Riesgo de mezclar APIs entre entornos | Declarar `VITE_API_BASE_URL` por proyecto de Vercel |
| El almacenamiento por defecto es local | En contenedores efimeros puede perderse informacion en reinicios | Usar `APP_STORAGE_TYPE=supabase` en entornos persistentes |

## Findings
1. **Local completo** esta definido en `docker-compose.yml`: Postgres en `localhost:5433`, API en `localhost:8080` y frontend dockerizado en `localhost:3000`.
2. **Desarrollo local mixto** tambien esta documentado: levantar solo DB con Docker y correr backend/frontend por separado, quedando frontend normalmente en `localhost:5173`.
3. **Backend staging y prod** estan descritos como dos servicios Render separados por rama en `render.yaml`: `staging` y `main`.
4. **No existen perfiles Spring separados en archivos**; staging/prod comparten el mismo `application.properties` y cambian por variables de entorno.
5. **Base de datos**: el codigo soporta PostgreSQL local y URIs estilo Supabase/Render; la documentacion declara staging en Render DB y produccion en Supabase.
6. **Frontend** consume `VITE_API_BASE_URL`; en repo solo hay default local y produccion, por lo que staging del frontend depende del proyecto/config de Vercel.
7. **Storage de archivos** puede ser local (`uploads/`) o Supabase Storage segun `APP_STORAGE_TYPE`.
8. **CI/CD real versionado**: hay autoDeploy en Render y la documentacion habla de Vercel auto-deploy; no hay workflows de GitHub Actions en el repo.

## Recommendation
La infraestructura actual funciona como un sistema de configuracion por entorno externo: el codigo base es casi el mismo para todos los entornos y Render/Vercel inyectan las diferencias. Para entenderla correctamente conviene pensarla asi:

1. Local y desarrollo se apoyan en defaults del repo.
2. Staging y produccion comparten binario/imagen y se diferencian por rama mas variables.
3. El punto mas sensible hoy no es el codigo de despliegue, sino el inventario de variables y la sincronizacion entre docs, Render y Vercel.
