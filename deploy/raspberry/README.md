# Despliegue en Raspberry Pi

Este directorio prepara una **fase 1 segura** para eliminar el cold start de Render:

- **Frontend** sigue en Vercel
- **Backend** pasa a la Raspberry Pi
- **Base de datos** se mantiene primero en Supabase o Postgres externo
- **PostgreSQL local** queda preparado como fase 2 opcional

## Recomendacion operativa

Empieza por mover **solo la API**. En este proyecto eso ya quita el problema del sleep de Render y te evita mezclar en el mismo cambio:

- migracion de red publica
- migracion de secretos
- migracion de datos
- endurecimiento y backups de Postgres

## Estado del host auditado

Revision remota realizada el **2026-03-22** sobre la Raspberry accesible por SSH:

- Arquitectura: `aarch64`
- RAM: `4.0 GiB`
- Disco raiz: `29 GiB` en `mmcblk0` (tarjeta SD)
- Docker: **no instalado** en el momento de la auditoria
- IP LAN observada: `192.168.50.187`

Conclusion:

- La Raspberry **sirve bien para la API**.
- Para alojar **PostgreSQL en serio**, conviene usar **SSD externo** en lugar de la SD.

## Archivos principales

- `docker-compose.prod.yml`: stack de produccion con perfiles
- `.env.prod.example`: variables a copiar en `.env.prod`
- `Caddyfile`: opcion con proxy HTTPS clasico
- `cloudflared/config.yml`: plantilla alternativa para Cloudflare Tunnel
- `scripts/bootstrap-host.sh`: instala Docker en Debian
- `scripts/backup-postgres.sh`: backup de PostgreSQL local
- `scripts/restore-postgres.sh`: restauracion de PostgreSQL local
- `scripts/healthcheck.sh`: comprobacion rapida de la API

## Perfiles de Docker Compose

- `cloudflare`: recomendado cuando quieres publicar la API sin abrir puertos del router
- `caddy`: util si vas a usar DDNS + port forwarding + TLS local
- `local-db`: activa PostgreSQL local en la Raspberry

## Fase 1 recomendada

1. Instala Docker con `scripts/bootstrap-host.sh`.
2. Copia este directorio a la Raspberry.
3. Crea `.env.prod` a partir de `.env.prod.example`.
4. Si usas Cloudflare Tunnel, edita `cloudflared/config.yml` y copia el fichero de credenciales JSON al mismo directorio.
5. Manten `SPRING_DATASOURCE_URL` apuntando a Supabase.
6. Manten `APP_STORAGE_TYPE=supabase`.
7. Arranca con Cloudflare Tunnel:

```bash
docker compose -f docker-compose.prod.yml --profile cloudflare up -d --build
```

8. Comprueba la API:

```bash
./scripts/healthcheck.sh https://api.tudominio.com
```

## Fase 2 opcional: PostgreSQL local

Cuando la fase 1 este estable:

1. Conecta un SSD externo.
2. Ajusta el volumen de `postgres_data` al disco persistente.
3. Cambia `SPRING_DATASOURCE_URL` a `jdbc:postgresql://db:5432/peluqueria_db`.
4. Arranca con el perfil local:

```bash
docker compose -f docker-compose.prod.yml --profile local-db up -d
```

5. Migra los datos con dump/restore.
6. Programa backups regulares.

## Rollback

Si algo falla tras apuntar Vercel a la Raspberry:

1. Reestablece `VITE_API_BASE_URL` en Vercel hacia la URL actual de Render.
2. Redeploya el frontend.
3. Revisa logs con `docker compose logs -f app`.

## Notas de seguridad

- No expongas PostgreSQL a Internet.
- No subas `.env.prod` al repositorio.
- Rota el `JWT_SECRET` al salir de Render.
- Si usas Cloudflare Tunnel, evita abrir puertos en el router.
