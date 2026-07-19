# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercancía dentro de un almacén.

Cada registro se identifica mediante una combinación única de categoría y código, e incluye una ubicación formada por sitio y posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

## Funcionalidades principales

- Consultar el listado local de mercancía.
- Registrar y visualizar el detalle de cada mercancía.
- Buscar por categoría, código, sitio o posición.
- Filtrar por categoría, sitio y posición.
- Editar y eliminar registros.
- Seleccionar y eliminar varios registros.
- Validar y normalizar los datos introducidos.
- Evitar combinaciones duplicadas de categoría y código.
- Mostrar estados vacíos y búsquedas sin resultados.
- Exportar todos los registros a un archivo CSV.
- Compartir una exportación CSV con otras aplicaciones.
- Importar mercancía desde archivos CSV.
- Validar la importación e informar filas inválidas o duplicadas.
- Crear copias de seguridad CSV versionadas.
- Restaurar copias de seguridad con validación previa y confirmación.
- Conservar los datos existentes cuando una restauración falla.
- Funcionar completamente sin conexión a Internet.

## Gestión de archivos CSV

AlmacenTracker diferencia dos formatos:

- **CSV de intercambio:** permite exportar, compartir e importar mercancía.
- **CSV de copia de seguridad:** conserva los datos y sus fechas para poder restaurar el estado de la aplicación.

La aplicación utiliza el selector de documentos de Android para leer y guardar archivos, sin solicitar acceso general al almacenamiento.

## Tecnologías

- Android
- Java
- Android Views
- Material Components
- ViewModel y LiveData
- Room
- SQLite
- JUnit
- AndroidX Test
- Espresso
- Gradle
- GitHub Actions

## Arquitectura

El proyecto aplica una arquitectura hexagonal pragmática, separando:

- dominio;
- puertos de entrada;
- casos de uso;
- puertos de salida;
- interfaz Android;
- persistencia Room;
- adaptadores de archivos CSV;
- configuración de dependencias.

Room continúa siendo la única fuente de verdad de la aplicación.

## Requisitos

- Android 8.0 o superior.
- No requiere conexión a Internet para gestionar, importar, exportar, respaldar o restaurar mercancía.

## Versión

**AlmacenTracker v1.1.0**

## Autor

Randy Méndez