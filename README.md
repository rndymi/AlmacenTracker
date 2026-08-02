# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercadería dentro de un almacén.

Cada mercadería se identifica mediante una combinación única de categoría y código. Su ubicación está formada por un sitio obligatorio y una posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

---

## Versión

**AlmacenTracker v1.3.0**

Novedades principales:

```text
Registro, consulta, búsqueda, filtrado y eliminación
de historial documental de mercadería sacada
a partir de listas procesadas
```

---

## Funcionalidades

### Gestión de mercadería

- Consultar el listado local de mercadería.
- Registrar y visualizar el detalle de cada mercadería.
- Buscar por categoría, código, sitio o posición.
- Filtrar por categoría, sitio y posición.
- Editar y eliminar registros.
- Seleccionar y eliminar varios registros.
- Validar y normalizar los datos introducidos.
- Evitar combinaciones duplicadas de categoría y código.
- Mostrar estados vacíos y búsquedas sin resultados.

### Gestión de archivos CSV

- Exportar, compartir e importar archivos CSV.
- Informar filas inválidas o duplicadas durante la importación.
- Crear y restaurar copias de seguridad CSV versionadas.
- Conservar los datos existentes cuando una restauración falla.

### Escaneo individual

- Escanear códigos de barras y códigos QR.
- Buscar mercadería mediante el código escaneado.
- Utilizar el escáner durante el registro y la edición.
- Mantener la introducción manual cuando la cámara no esté disponible.
- Controlar permisos, cancelaciones y errores de cámara.

### Procesamiento de listas

- Capturar o seleccionar imágenes de listas.
- Procesar listas localmente mediante reconocimiento de texto.
- Corregir orientación, escala y contraste antes del OCR.
- Reconstruir referencias de listas de una o dos columnas.
- Revisar coincidencias exactas, sugeridas, ambiguas o no encontradas.
- Corregir, añadir, eliminar y confirmar referencias.
- Consultar conjuntamente la ubicación de las referencias confirmadas.
- Identificar referencias no encontradas.

### Historial documental

- Registrar una lista procesada como historial documental.
- Añadir un título opcional.
- Seleccionar la fecha y hora documental.
- Conservar categoría y código.
- Proponer, corregir o completar cantidad y unidad.
- Permitir referencias sin cantidad ni unidad.
- Guardar sitio y posición como instantánea histórica.
- Conservar referencias encontradas y no encontradas.
- Consultar el listado de registros históricos.
- Mostrar título, fecha y resumen de referencias.
- Abrir el detalle completo de cada lista.
- Mostrar cantidades, unidades y ubicaciones históricas.
- Buscar por título, categoría o código.
- Filtrar por fecha inicial y fecha final.
- Combinar búsqueda y filtros.
- Conservar criterios al abrir y cerrar un detalle.
- Eliminar un registro histórico con confirmación.
- Eliminar sus líneas mediante cascada sin modificar la mercadería.
- Funcionar completamente sin conexión a Internet.

---

## Historial documental

El historial permite conservar localmente una lista después de revisarla y confirmar sus datos documentales.

```text
fotografía o imagen
        ↓
OCR local
        ↓
revisión de referencias
        ↓
consulta de ubicaciones
        ↓
título, fecha, cantidad y unidad
        ↓
confirmación
        ↓
historial en Room
```

Cada registro histórico puede conservar:

- título opcional;
- fecha y hora documental;
- categoría;
- código;
- cantidad opcional;
- unidad opcional;
- sitio histórico;
- posición histórica;
- estado encontrado o no encontrado.

La ubicación se guarda como una instantánea. Si la mercadería cambia de sitio o se elimina posteriormente, el historial conserva la información existente cuando se registró la lista.

El historial es documental. Las cantidades no reducen existencias ni convierten AlmacenTracker en un sistema de gestión de stock.

---

## Búsqueda y filtros del historial

El historial puede consultarse mediante:

- título parcial;
- categoría;
- código;
- fecha inicial;
- fecha final;
- combinación de texto e intervalo de fechas.

Las búsquedas no distinguen mayúsculas y minúsculas y conservan los ceros iniciales de los códigos.

Los registros se muestran por fecha documental descendente. Al eliminar un resultado, el listado vuelve a consultarse manteniendo los criterios activos.

---

## Escaneo y procesamiento de listas

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
- reconstruir referencias de una o dos columnas;
- revisar coincidencias exactas, sugeridas, ambiguas o no encontradas;
- corregir, añadir o eliminar referencias;
- consultar sus ubicaciones en el orden confirmado;
- preparar y registrar el historial documental.

El reconocimiento de listas es experimental y puede cometer errores. El usuario debe revisar el resultado antes de continuar.

Las imágenes y el texto se procesan en el dispositivo. Las fotografías no se conservan permanentemente ni se envían a servicios externos.

---

## Gestión de archivos CSV

AlmacenTracker diferencia dos formatos:

- **CSV de intercambio:** permite exportar, compartir e importar mercadería.
- **CSV de copia de seguridad:** conserva los datos y sus fechas para restaurar el estado de la mercadería.

La aplicación utiliza el selector de documentos de Android sin solicitar acceso general al almacenamiento.

El historial documental no forma parte actualmente de estos archivos CSV.

---

## Tecnologías

- Android
- Java
- Android Views
- View Binding
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

- `feature` agrupa inventario, gestión de datos, escaneo, listas e historial.
- `domain` contiene modelos y reglas independientes de Android.
- `data` contiene Room, repositorios e infraestructura técnica.
- `core` reúne contratos y componentes compartidos.
- `app` compone explícitamente las dependencias.

Las Activities se encargan del renderizado, la interacción y la navegación. Los ViewModels mantienen el estado de cada pantalla. Room continúa siendo la fuente local de verdad.

Se introducen servicios, puertos o adaptadores adicionales únicamente cuando representan una responsabilidad real.

---

## Requisitos

- Android 8.0 o superior.
- No requiere conexión a Internet para gestionar mercadería, archivos CSV, escaneo, OCR o historial.
- El escaneo individual requiere una cámara compatible.
- La selección de imágenes utiliza el selector de fotos de Android.
- El reconocimiento depende de la calidad, orientación y legibilidad de la imagen.
- Las cantidades del historial son documentales y no representan stock.

---

## Autor

Randy Méndez