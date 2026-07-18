# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercancía dentro de un almacén.

La aplicación organiza cada registro mediante una categoría, un código y una ubicación compuesta por sitio y posición opcional.

## Versiones

### Versión estable

**v1.0.0 — Gestión local de mercancía**

La primera versión estable incluye:

- listado local de mercancía;
- registro y consulta de detalle;
- búsqueda por categoría, código y ubicación;
- filtros por categoría, sitio y posición;
- edición y eliminación individual;
- selección y eliminación múltiple;
- validación y normalización de datos;
- prevención de combinaciones duplicadas;
- estados vacíos y sin resultados;
- persistencia mediante Room y SQLite;
- funcionamiento completo sin conexión a Internet.

### Versión en desarrollo

**v1.1.0 — Intercambio y copia de datos mediante CSV**

Funcionalidades previstas:

- exportar la mercancía a archivos CSV;
- guardar archivos mediante el selector de documentos de Android;
- compartir exportaciones con otras aplicaciones;
- importar mercancía desde archivos CSV;
- validar filas antes de incorporarlas;
- informar registros importados, omitidos y erróneos;
- crear copias de seguridad locales;
- restaurar copias de seguridad con confirmación previa.

Las funcionalidades de v1.1.0 se implementarán progresivamente en ramas de historia de usuario y no se consideran disponibles hasta su integración y publicación.

## Tecnologías

- Android
- Java
- Android Views
- ViewModel y LiveData
- Room
- SQLite
- Material Components
- JUnit
- AndroidX Test
- Espresso
- GitHub Actions

## Requisitos

- Android 8.0 o superior
- No requiere conexión a Internet para gestionar la mercancía

## Autor

Randy Méndez