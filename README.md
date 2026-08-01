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
- Corregir orientación, escala y contraste antes del OCR.
- Reconstruir referencias de listas de una o dos columnas.
- Revisar coincidencias exactas, sugeridas, ambiguas o no encontradas.
- Corregir, añadir, eliminar y confirmar referencias.
- Consultar conjuntamente la ubicación de las referencias confirmadas.
- Identificar referencias no encontradas.
- Funcionar completamente sin conexión a Internet.

### Funcionalidades previstas para v1.3

- Registrar una lista procesada como historial documental.
- Añadir un título opcional.
- Registrar automáticamente fecha y hora.
- Conservar categoría y código.
- Conservar cantidades y unidades cuando estén disponibles.
- Corregir o completar cantidad y unidad antes de guardar.
- Permitir referencias sin cantidad.
- Guardar sitio y posición como instantánea histórica.
- Conservar referencias no encontradas.
- Diferenciar ubicación histórica y actual.
- Consultar un listado de registros históricos.
- Ver el detalle de cada lista guardada.
- Buscar por título, categoría o código.
- Filtrar por intervalo de fechas.
- Eliminar un registro histórico con confirmación.
- Mantener la mercadería principal sin modificaciones.
- Mantener el historial completamente offline.
- No descontar cantidades ni gestionar stock.

---

## Historial documental de v1.3

La versión 1.3 ampliará el procesamiento de listas para conservar un registro local después de la revisión.

```text
fotografía o imagen
        ↓
OCR local
        ↓
revisión de referencias
        ↓
consulta de ubicaciones
        ↓
título, cantidad y unidad
        ↓
confirmación
        ↓
historial en Room
```

Cada registro histórico podrá conservar:

- título opcional;
- fecha y hora;
- categoría;
- código;
- cantidad opcional;
- unidad opcional;
- sitio histórico;
- posición histórica;
- estado encontrado o no encontrado.

La ubicación se almacenará como una instantánea. Si la mercadería cambia de sitio posteriormente, el historial conservará la ubicación que tenía cuando se registró la lista.

El historial será documental. Las cantidades no reducirán existencias ni convertirán AlmacenTracker en un sistema de stock.

---

## Escaneo y procesamiento de listas

### Escaneo individual

Permite leer un código de barras o QR para:

- buscar mercadería;
- abrir su detalle;
- rellenar el código durante un registro;
- sustituir el código durante una edición.

La entrada manual continuará disponible.

### Procesamiento de listas

Permite:

- tomar una fotografía o seleccionar una imagen;
- procesar el texto localmente;
- corregir orientación y mejorar la imagen;
- reconstruir una o dos columnas;
- revisar coincidencias;
- corregir, añadir o eliminar referencias;
- consultar ubicaciones;
- preparar el historial de v1.3.

El reconocimiento es experimental y deberá revisarse.

Las imágenes no se conservarán permanentemente ni se enviarán a servicios externos.

---

## Gestión de archivos CSV

AlmacenTracker diferencia:

- **CSV de intercambio:** exportación, compartición e importación de mercadería.
- **CSV de copia de seguridad:** conservación y restauración de datos.

La versión 1.3 deberá revisar el formato versionado para decidir cómo incluir el historial manteniendo compatibilidad con copias anteriores.

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
- `domain` contiene modelos y reglas.
- `data` contiene Room, repositorios e infraestructura.
- `core` reúne contratos compartidos.
- `app` compone dependencias.

Room continúa siendo la fuente local de verdad.

Se introducirán puertos o adaptadores adicionales únicamente cuando aporten una responsabilidad real.

---

## Requisitos

- Android 8.0 o superior.
- No requiere Internet para gestionar mercadería, CSV, escaneo, OCR o historial.
- El escaneo requiere una cámara compatible.
- La selección de imágenes utiliza Photo Picker.
- El reconocimiento depende de la calidad y legibilidad de la imagen.
- El historial de v1.3 permanecerá disponible offline.
- Sus cantidades serán documentales y no representarán stock.

---

## Versión

**AlmacenTracker v1.3.0 — En desarrollo**

Objetivo:

```text
registrar y consultar un historial documental
de mercadería sacada a partir de listas procesadas
```

---

## Autor

Randy Méndez
