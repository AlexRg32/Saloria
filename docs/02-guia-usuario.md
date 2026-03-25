# 📖 Guía de Usuario - Saloria

Esta guía describe cómo utilizar las funcionalidades principales de la plataforma, dirigida tanto a los propietarios de negocio como a sus clientes finales.

---

## 👨‍💼 Para Administradores y Empleados

### 1. Inicio de Sesión y Registro del Negocio

- **Registro**: Al crear una cuenta profesional, el sistema crea un negocio nuevo y su cuenta administradora inicial. El alta pública no sirve para unirse a una empresa ya existente; ese acceso debe gestionarse por una vía interna o controlada.
- **Login**: Accede al panel de control con tu correo electrónico y contraseña seguros.

### 2. Gestión de Servicios

Configura los servicios que ofrece tu negocio.

- **Categorías**: Organiza cortes, tintes, tratamientos, etc.
- **Precios y Duración**: Define cuánto cuesta cada servicio y el tiempo estimado para el bloqueo en agenda.

### 3. Agenda y Calendario

El corazón de la gestión diaria.

- **Vista Diaria/Semanal**: Visualiza de un vistazo la ocupación de todos los empleados o filtra por un profesional específico.
- **Crear Cita Manualmente**: Bloquea horas para clientes que llaman por teléfono o acuden presencialmente.
- **Gestión de Estados**: Marca citas como `Pendiente`, `Confirmada`, `Completada` o `Cancelada`.

### 4. Gestión de Clientes (CRM)

- **Base de Datos**: Mantén un registro de todos los clientes que han visitado tu negocio.
- **Historial**: Consulta qué servicios se ha realizado un cliente en el pasado.
- **Notas Internas**: Añade observaciones importantes (e.g. alergias, preferencias de estilo).

### 5. Reportes y Estadísticas

- **Dashboard Principal**: Visualiza ingresos diarios, servicios más populares y tasa de retención de clientes.

---

## 👤 Para Clientes Finales

### 1. Portal de Reserva Online

Los clientes pueden acceder a la página pública de tu negocio para descubrir servicios, ver el equipo y consultar horarios sin necesidad de llamar.

### 2. Proceso de Reserva

1. **Explorar negocio**: Entra desde el marketplace o el buscador público.
2. **Iniciar sesión como cliente**: La reserva online actual requiere una cuenta `CLIENTE` autenticada.
3. **Seleccionar Servicio y Profesional**: Elige el servicio y el empleado disponibles.
4. **Fecha y Hora**: Reserva usando los horarios públicos configurados por el negocio.
5. **Confirmación**: La cita queda asociada al historial del cliente.

### 3. Mi Perfil

- **Mis Citas**: Consulta las próximas citas y el historial de servicios pasados.
- **Resumen de Cuenta**: Visualiza nombre, correo, rol y accesos rápidos a historial y búsqueda.

---

## 🛡️ Roles y Permisos

| Funcionalidad | Administrador (Dueño) | Empleado | Cliente |
| :--- | :---: | :---: | :---: |
| Configurar Negocio | ✅ | ❌ | ❌ |
| Gestionar Empleados | ✅ | ❌ | ❌ |
| Ver Agenda Completa | ✅ | ✅ | ❌ |
| Gestionar sus propias citas | ✅ | ✅ | ✅ |
| Ver reportes financieros | ✅ | ❌ | ❌ |

> [Siguiente: Arquitectura Técnica](./03-arquitectura-tecnica.md)
