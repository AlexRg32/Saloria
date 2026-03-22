# Plan: Infraestructura actual y mapa de entornos

> WARNING: This plan is strictly theoretical. No source code files have been modified.

> Goal: dejar el mapa de infraestructura consistente entre codigo, docs y configuracion externa.
> Architecture: monorepo con backend Spring Boot en Render y frontend Vite en Vercel.

## Foundation

- [ ] **Task 1: Inventariar variables por entorno** — `render.yaml`, dashboard Render, dashboard Vercel`
  - What: documentar para local, staging y prod los valores esperados de datasource, CORS, JWT, API base URL y storage.
  - Verify: existe una tabla unica donde cada variable aparece una sola vez por entorno.

- [ ] **Task 2: Validar estrategia de frontend staging** — `peluqueria-client/.env*`, configuracion Vercel`
  - What: confirmar si hay proyecto Vercel separado para staging o si se usan preview deployments.
  - Verify: queda identificada la URL exacta que usa staging y su `VITE_API_BASE_URL`.

## Core

- [ ] **Task 3: Alinear docs con realidad operativa** — `docs/07-infraestructura.md`, `DEPLOYMENT.md`, `README.md`
  - What: corregir diferencias entre la documentacion y el repo, especialmente perfiles Spring, CI/CD y puertos de frontend local.
  - Verify: no quedan afirmaciones no respaldadas por archivos o configuraciones activas.

- [ ] **Task 4: Decidir si se mantienen perfiles Spring solo por ENV o por archivos dedicados** — `peluqueria-api/src/main/resources/`
  - What: decidir entre seguir con un `application.properties` unico o introducir `application-staging.properties` y `application-prod.properties`.
  - Verify: la estrategia elegida queda documentada y reduce ambiguedad operativa.

## Integration & Polish

- [ ] **Task 5: Formalizar storage productivo** — `StorageConfig`, entorno productivo`
  - What: confirmar si produccion debe usar siempre Supabase Storage y reflejarlo en docs/envs.
  - Verify: las imagenes sobreviven reinicios y despliegues, o queda explicitado que no es un requisito.

- [ ] **Task 6: Formalizar CI/CD real** — `docs/07-infraestructura.md`, plataforma`
  - What: documentar si el despliegue depende solo de autoDeploy en Render/Vercel o si se añadira CI versionado en `.github/workflows`.
  - Verify: el repositorio y la documentacion cuentan la misma historia del pipeline.
