# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercancía dentro de un almacén.

Cada registro se identifica mediante una combinación única de categoría y código, e incluye una ubicación formada por sitio y posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

## Funcionalidades

### Funcionalidades principales

- Consultar el listado local de mercancía.
- Registrar y visualizar el detalle de cada mercancía.
- Buscar por categoría, código, sitio o posición.
- Filtrar por categoría, sitio y posición.
- Editar y eliminar registros.
- Seleccionar y eliminar varios registros.
- Validar y normalizar los datos introducidos.
- Evitar combinaciones duplicadas de categoría y código.
- Mostrar estados vacíos y búsquedas sin resultados.
- Exportar, compartir e importar archivos CSV.
- Informar filas inválidas o duplicadas durante la importación.
- Crear y restaurar copias de seguridad CSV versionadas.
- Conservar los datos existentes cuando una restauración falla.
- Funcionar completamente sin conexión a Internet.

### Funcionalidades previstas para v1.2

- Escanear códigos de barras mediante la cámara.
- Escanear códigos QR compatibles.
- Buscar mercancía utilizando el valor leído.
- Abrir el detalle cuando exista una coincidencia única.
- Mostrar varias coincidencias cuando un código pertenezca a categorías diferentes.
- Rellenar el código del formulario de registro mediante escaneo.
- Utilizar el escaneo durante la edición con confirmación antes de reemplazar el código.
- Controlar permisos de cámara, cancelaciones, códigos no reconocidos y cámara no disponible.
- Mantener siempre la introducción manual como alternativa.

Estas funcionalidades están planificadas y no se consideran disponibles hasta completar y publicar v1.2.0.

## Gestión de archivos CSV

AlmacenTracker diferencia dos formatos:

- **CSV de intercambio:** permite exportar, compartir e importar mercancía.
- **CSV de copia de seguridad:** conserva los datos y sus fechas para restaurar el estado de la aplicación.

La aplicación utiliza el selector de documentos de Android sin solicitar acceso general al almacenamiento.

## Tecnologías

- Android
- Java
- Android Views
- Material Components
- ViewModel y LiveData
- Room y SQLite
- JUnit
- AndroidX Test
- Espresso
- Gradle
- GitHub Actions

## Arquitectura

El proyecto aplica una arquitectura hexagonal pragmática, separando dominio, puertos, casos de uso, interfaz Android, persistencia Room, archivos CSV, escaneo y configuración.

Room continúa siendo la única fuente de verdad.

## Requisitos

- Android 8.0 o superior.
- No requiere conexión a Internet para gestionar mercancía o archivos CSV.
- El escaneo previsto para v1.2 requiere una cámara disponible.

## Versión

**AlmacenTracker v1.2.0 — En desarrollo**

## Autor

Randy Méndez