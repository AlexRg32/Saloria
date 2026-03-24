# 🚀 Guía de Despliegue - Saloria

Esta guía detalla los pasos para desplegar la aplicación de forma gratuita.

## 1. Base de Datos (PostgreSQL en Supabase)

1. Ve a [Supabase](https://supabase.com/) y crea un nuevo proyecto.
2. En la configuración del proyecto, busca la sección **Database** y copia la **URI de conexión**.
   - Ejemplo: `postgresql://postgres:password@db.xxxx.supabase.co:5432/postgres`

## 2. Backend (Spring Boot en Raspberry Pi)

La ruta principal soportada para producción es la Raspberry Pi descrita en [`deploy/raspberry/README.md`](./deploy/raspberry/README.md).

Recomendacion:

1. Mantener la base de datos en Supabase al principio.
2. Mover solo la API Spring Boot a la Raspberry.
3. Publicar la API con Cloudflare Tunnel o, si prefieres, con Caddy + DDNS.
4. Usar Tailscale para acceso SSH remoto a la Raspberry sin abrir puertos.
5. Cambiar en Vercel `VITE_API_BASE_URL` al dominio HTTPS público de la API.

### Alternativa: Backend en Render

1. Crea una cuenta en [Render](https://render.com/).
2. Crea un nuevo **Web Service** y conecta tu repositorio de GitHub.
3. Elige el directorio `/saloria-api`.
4. Render detectará automáticamente el Dockerfile o puedes usar el build nativo:
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/*.jar`
5. **Variables de Entorno** (Environment Variables):
   - `SPRING_DATASOURCE_URL`: La URI que copiaste de Supabase.
   - `SPRING_DATASOURCE_USERNAME`: `postgres` (o el que definieras).
   - `SPRING_DATASOURCE_PASSWORD`: Tu contraseña de Supabase.
   - `JWT_SECRET`: Un secreto largo y aleatorio para firmar los tokens JWT.
   - `CORS_ALLOWED_ORIGINS`: La URL que te dé Vercel (ej: `https://tu-app.vercel.app`).
   - `APP_BOOTSTRAP_SUPERADMIN_ENABLED`: Déjalo en `false` salvo una inicialización de emergencia controlada.
   - `APP_BOOTSTRAP_SUPERADMIN_EMAIL` y `APP_BOOTSTRAP_SUPERADMIN_PASSWORD`: Solo si activas el bootstrap anterior.

## 3. Frontend (React en Vercel)

1. Ve a [Vercel](https://vercel.com/) y crea un nuevo proyecto.
2. Conecta tu repositorio de GitHub y selecciona la carpeta `/saloria-client`.
3. **Build Settings**: Vercel detectará que es Vite. No cambies nada.
4. **Environment Variables**:
   - `VITE_API_BASE_URL`: La URL pública de la API (ej: `https://api.tudominio.com`).

---

## 🔄 Flujo de Actualización

A partir de ahora, el flujo soportado es **solo `main`**. Cada vez que promociones cambios a `main`, tanto el Frontend como el Backend productivos deben quedar alineados con esa rama.

- **Frontend (Vercel)**: despliegue automatico desde el proyecto conectado.
- **Backend (Raspberry)**: despliegue automatico via GitHub Actions en un runner self-hosted de la Raspberry usando `deploy/raspberry/scripts/redeploy.sh`.
