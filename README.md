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

### Novedades de v1.2.0

- Escanear códigos de barras y códigos QR mediante la cámara.
- Buscar la mercancía escaneada y abrir su detalle o elegir entre varias coincidencias.
- Rellenar el código al registrar mercancía y sustituirlo con confirmación durante la edición.
- Mantener la introducción manual cuando no se conceda el permiso o la cámara no esté disponible.
- Procesar de forma local fotografías e imágenes de listas de referencias mediante reconocimiento de texto experimental.
- Revisar, corregir y confirmar las referencias detectadas antes de consultar sus ubicaciones en el inventario.
- Reorganizar el proyecto en una arquitectura MVVM pragmática por funcionalidades.

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
- CameraX
- ML Kit
- ViewModel y LiveData
- Room y SQLite
- JUnit
- AndroidX Test
- Espresso
- Gradle
- GitHub Actions

## Arquitectura

El proyecto aplica una arquitectura MVVM pragmática organizada por funcionalidades:

- `feature` agrupa las pantallas y flujos de inventario y gestión de datos.
- `domain` contiene el modelo y las reglas de negocio independientes de Android.
- `data` contiene Room, el repositorio y la infraestructura de archivos.
- `core` reúne componentes compartidos, como eventos y utilidades CSV.
- `app` compone las dependencias de la aplicación.

Las Activities se encargan del renderizado, la interacción y la navegación, mientras que los ViewModels mantienen el estado de cada pantalla. Room continúa siendo la única fuente de verdad.

## Requisitos

- Android 8.0 o superior.
- No requiere conexión a Internet para gestionar mercancía o archivos CSV.
- El escaneo directo requiere una cámara disponible; la introducción manual continúa disponible como alternativa.

## Versión

**AlmacenTracker v1.2.0**

## Autor

Randy Méndez
