# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercadería dentro de un almacén.

Cada mercadería se identifica mediante una combinación única de categoría y código. Su ubicación está formada por un sitio obligatorio y una posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

---

## Funcionalidades

### Funcionalidades principales

- Consultar el listado local de mercadería.
- Registrar y visualizar el detalle de cada mercadería.
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
- Escanear códigos de barras y códigos QR.
- Buscar mercadería mediante el código escaneado.
- Utilizar el escáner durante el registro y la edición.
- Mantener la introducción manual cuando la cámara no esté disponible.
- Capturar o seleccionar imágenes de listas.
- Procesar listas localmente mediante reconocimiento de texto.
- Revisar, corregir y confirmar referencias reconocidas.
- Consultar conjuntamente la ubicación de las referencias confirmadas.
- Identificar referencias no encontradas.
- Funcionar completamente sin conexión a Internet.

### Funcionalidades previstas para v1.3

- Crear un registro documental a partir de una lista revisada.
- Asignar un título opcional al documento.
- Organizar una lista en secciones o grupos cuando sea necesario.
- Conservar el orden original y las referencias repetidas.
- Registrar cantidad y unidad como información documental.
- Registrar automáticamente la fecha y hora.
- Conservar un snapshot del sitio y la posición de cada mercadería.
- Guardar también referencias que no existan en el inventario.
- Consultar un historial local de mercadería sacada.
- Visualizar el detalle de cada registro histórico.
- Buscar y filtrar registros anteriores.
- Eliminar registros históricos sin modificar la mercadería principal.
- Mantener todas las operaciones completamente offline.

Estas funcionalidades están planificadas y no se consideran disponibles hasta completar y publicar v1.3.0.

El historial será exclusivamente documental. No descontará stock, no calculará existencias y no modificará las cantidades de la mercadería.

---

## Escaneo y procesamiento de listas

AlmacenTracker ofrece dos flujos diferentes.

### Escaneo individual

Permite leer un código de barras o QR mediante la cámara para:

- buscar mercadería;
- abrir su detalle;
- rellenar el código durante un registro;
- sustituir el código durante una edición con confirmación.

La introducción manual continúa disponible cuando el permiso no se concede o la cámara no puede utilizarse.

### Procesamiento de listas

Permite tomar una fotografía o seleccionar una imagen para:

- procesar el texto localmente;
- reconstruir referencias de una o varias columnas;
- revisar coincidencias exactas, sugeridas o no encontradas;
- corregir, añadir o eliminar referencias;
- consultar sus ubicaciones en el orden confirmado.

El reconocimiento de listas es experimental y puede cometer errores. El usuario debe revisar el resultado antes de continuar.

Las imágenes y el texto se procesan en el dispositivo. Las fotografías no se conservan permanentemente ni se envían a servicios externos.

---

## Gestión de archivos CSV

AlmacenTracker diferencia dos formatos:

- **CSV de intercambio:** permite exportar, compartir e importar mercadería.
- **CSV de copia de seguridad:** conserva los datos y sus fechas para restaurar el estado de la aplicación.

La aplicación utiliza el selector de documentos de Android sin solicitar acceso general al almacenamiento.

---

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

---

## Arquitectura

El proyecto aplica una arquitectura MVVM pragmática organizada por funcionalidades:

- `feature` agrupa las pantallas y flujos de inventario, gestión de datos, escaneo y listas.
- `domain` contiene modelos y reglas independientes de Android.
- `data` contiene Room, repositorios e infraestructura técnica.
- `core` reúne contratos y componentes compartidos.
- `app` compone las dependencias.

Las Activities se encargan del renderizado, la interacción y la navegación. Los ViewModels mantienen el estado de cada pantalla. Room continúa siendo la fuente de verdad.

La evolución del proyecto priorizará una separación hexagonal pragmática, introduciendo puertos y servicios únicamente cuando representen responsabilidades reales.

---

## Requisitos

- Android 8.0 o superior.
- No requiere conexión a Internet para gestionar mercadería, CSV, escaneo o procesamiento de listas.
- El escaneo individual requiere una cámara compatible.
- La selección de imágenes utiliza el selector de fotos de Android.
- El reconocimiento de listas puede variar según la calidad, orientación y legibilidad de la imagen.

---

## Versión

**AlmacenTracker v1.3.0 — En desarrollo**

---

## Autor

Randy Méndez
