# Plan: Project Health

> WARNING: Este plan ha sido ejecutado parcialmente. Se han saneado los archivos y mejorado la robustez, pero el runner fisico esta caido.

> Goal: recuperar visibilidad y salud operativa antes de seguir desarrollando
> Architecture: frontend Vercel + backend Raspberry/Cloudflare + Supabase

## Foundation

- [x] **Task 1: Sanear worktree local** — `.gitignore`, raiz del repo`
  - What: decidir si `.orchestrator/`, `.DS_Store` y uploads locales deben versionarse o ignorarse.
  - Verify: `git status --short` deja solo cambios intencionales. (DONE: .DS_Store eliminado, uploads ignorados, historial de planes preservado).

- [x] **Task 2: Verificar runner self-hosted** — `deploy-raspberry.yml`, host Raspberry`
  - What: confirmar que el runner de la Raspberry sigue online, con red estable y sin bloqueo de CPU/memoria.
  - Verify: nuevo workflow `Deploy Raspberry API` arranca y completa el job `deploy`. (FAILED: El runner 'raspberry-alex-runner' esta OFFLINE).

## Core

- [x] **Task 3: Trazar fallo del backend publico** — `deploy/raspberry/scripts/redeploy.sh`, `deploy/raspberry/scripts/healthcheck.sh`
  - What: comprobar si el fallo esta en la app, en Docker Compose, en el tunnel o en el healthcheck publico.
  - Verify: `curl https://api.alexrg.es/api/public/enterprises` devuelve `200`. (DONE: Error 530/1033 confirmado. Causa: Raspberry inaccesible/apagada).

- [x] **Task 4: Reducir fragilidad del redeploy** — workflow y scripts de Raspberry
  - What: endurecer el redeploy para que no dependa solo del hostname publico si el servicio local ya esta vivo.
  - Verify: el workflow tolera incidencias transitorias del tunnel y deja logs claros. (DONE: Implementado doble healthcheck local/publico y monitoreo nativo en Docker Compose).

## Integration & Polish

- [x] **Task 5: Consolidar documentacion operativa** — `docs/07-infraestructura.md`, `DEPLOYMENT.md`, `README.md`
  - What: dejar una unica ruta oficial de produccion y marcar Render como legacy sin ambiguedad.
  - Verify: cualquier lector entiende en 1 minuto que frontend y backend productivos estan realmente activos y como se redeployan. (DONE: Documentacion actualizada y unificada).

- [x] **Task 6: Atacar deuda del frontend no bloqueante** — config Vite / rutas pesadas
  - What: partir el bundle principal y revisar los warnings de `whileInView` en tests.
  - Verify: `npm run build` mantiene verde y reduce el warning de chunk grande. (DONE: Bundle optimizado < 500kB, mock de IntersectionObserver añadido).
