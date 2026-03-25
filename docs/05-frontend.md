# 🖼 Frontend (Cliente Web)

La aplicación web está construida con **React 19** y **TypeScript**, utilizando **Vite** como empaquetador para una experiencia de desarrollo ultrarrápida.

## 📂 Organización del Proyecto

El código fuente en `src/` sigue una estructura híbrida basada en **Features** (Funcionalidades) y **Components** (Reutilizables).

```plaintext
src/
├── components/         # UI y módulos de dominio reutilizables
│   ├── appointments/
│   ├── billing/
│   ├── customers/
│   ├── dashboard/
│   ├── employees/
│   ├── layout/
│   ├── marketplace/
│   ├── services/
│   ├── settings/
│   └── ui/
├── features/
│   ├── auth/           # Login, registro y contexto de autenticación
│   └── client-portal/  # Hero, cards y flujo público/cliente
├── lib/                # Axios y utilidades base
├── pages/              # Rutas principales de la SPA
├── services/           # Acceso a API por dominio
├── test/               # Setup y utilidades de test
├── types/              # Tipos compartidos
└── utils/              # Helpers de formato y fechas
```

## 🛠 Tecnologías Clave

- **React Router**: Gestión de rutas (`/login`, `/dashboard`, `/agenda`).
- **Tailwind CSS**: Framework de utilidades para estilos rápidos y consistentes.
- **React Hook Form**: Gestión eficiente de formularios grandes.
- **Axios**: Cliente HTTP centralizado para hablar con la API.
- **framer-motion**: Animaciones y transiciones del portal cliente.
- **Zod**: Dependencia disponible para validaciones tipadas puntuales.
- **Lucide React**: Librería de iconos vectoriales ligeros.
- **date-fns**: Manipulación robusta de fechas y horas.

## 🧩 Componentes Principales

### 1. Sistema de Diseño (`components/ui`)

Componentes abstractos que aseguran consistencia visual:

- `Button`: Variantes (primary, outline, ghost).
- `Input`, `Select`: Campos de formulario estandarizados con soporte de errores.
- `Modal`: Ventanas emergentes para confirmaciones.
- `Card`: Contenedores con sombra y borde.

### 2. Calendario Interactivo (`components/calendar`)

El componente más complejo de la aplicación.

- Permite arrastrar y soltar (Drag & Drop) citas.
- Vistas por Día, Semana y Mes.
- Filtrado por profesional.

### 3. Portal del Cliente (`features/client-portal`)

La capa B2C ya está integrada dentro de la misma SPA.

- `ClientPortal`: portada pública con destacados, negocios cercanos y citas del cliente si está autenticado.
- `SearchPage`: buscador real conectado al directorio público.
- `BarbershopProfilePage`: perfil público del negocio con servicios, equipo y horarios.
- `PublicBookingModal`: reserva online para usuarios con cuenta `CLIENTE`.

## 🔄 Gestión de Estado

La aplicación utiliza dos niveles principales de estado:

1. **Estado de sesión y branding (Context API)**: autenticación, token JWT, usuario actual y branding de la empresa.
2. **Estado de pantalla (fetch por página/servicio + local state)**: resultados de búsqueda, citas, catálogos, modales y filtros.

> [Siguiente: Base de Datos](./06-base-de-datos.md)
