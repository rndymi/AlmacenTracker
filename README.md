# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercadería dentro de un almacén.

Cada mercadería se identifica mediante una combinación única de categoría y código. Su ubicación está formada por un sitio obligatorio y una posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

---

## Versión

**AlmacenTracker v1.4.0**

Novedades principales:

```text
Evolución del OCR documental local mediante PP-OCRv5 y ONNX Runtime,
con orientación manual, reconstrucción multicolumna,
interpretación revisable y mejoras de estabilidad
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
- Utilizar desplazamiento rápido en listados largos cuando corresponde.

### Gestión de archivos CSV

- Exportar, compartir e importar archivos CSV.
- Informar filas inválidas o duplicadas durante la importación.
- Crear y restaurar copias de seguridad CSV versionadas.
- Conservar los datos existentes cuando una restauración falla.
- Utilizar el selector de documentos de Android sin permisos generales de almacenamiento.

### Escaneo individual

- Escanear códigos de barras y códigos QR.
- Buscar mercadería mediante el código escaneado.
- Utilizar el escáner durante el registro y la edición.
- Mantener la introducción manual cuando la cámara no está disponible.
- Controlar permisos, cancelaciones y errores de cámara.
- Utilizar ML Kit Barcode Scanning exclusivamente para el escaneo individual.

### Procesamiento de listas

- Tomar fotografías de listas.
- Seleccionar imágenes o capturas de pantalla.
- Procesar imágenes localmente mediante PP-OCRv5 y ONNX Runtime.
- Corregir la orientación inicial mediante EXIF.
- Girar manualmente la imagen a izquierda o derecha antes de ejecutar el OCR.
- Reprocesar una imagen después de corregir su orientación.
- Detectar y reconocer regiones de texto localmente.
- Reconstruir listas de una, dos o varias columnas cuando existe evidencia espacial suficiente.
- Conservar encabezados y líneas documentales durante la reconstrucción.
- Mantener el orden de lectura de las columnas.
- Revisar coincidencias exactas, sugeridas, ambiguas o no encontradas.
- Proponer correcciones para errores OCR compatibles con referencias conocidas.
- Interpretar referencias especiales sin sustituir silenciosamente el texto reconocido.
- Conservar cantidad y unidad como datos documentales.
- Conservar títulos, comprador o tienda y destinos documentales cuando corresponda.
- Corregir, añadir, eliminar y confirmar referencias.
- Consultar conjuntamente la ubicación de las referencias confirmadas.
- Identificar referencias no encontradas.
- Mantener la revisión manual obligatoria.
- Procesar las imágenes sin enviarlas a servicios externos.
- Liberar los recursos temporales utilizados durante el procesamiento.

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
- Conservar los criterios al abrir y cerrar un detalle.
- Eliminar un registro histórico con confirmación.
- Eliminar sus líneas mediante cascada sin modificar la mercadería.
- Utilizar desplazamiento rápido en listados largos cuando corresponde.
- Funcionar completamente sin conexión a Internet.

---

## OCR documental local

AlmacenTracker v1.4.0 utiliza PP-OCRv5 como motor activo para el reconocimiento documental de listas.

El procesamiento se ejecuta localmente mediante:

```text
PP-OCRv5
+
ONNX Runtime
```

Los modelos de detección y reconocimiento, junto con el diccionario necesario, forman parte de la aplicación. El flujo documental no requiere descargar modelos ni enviar imágenes a servicios externos.

```text
fotografía o imagen
        ↓
preprocesamiento local
        ↓
detección de regiones
        ↓
reconocimiento de texto
        ↓
reconstrucción documental
        ↓
interpretación revisable
        ↓
revisión del usuario
        ↓
ubicaciones e historial
```

El reconocimiento documental y el escaneo individual son componentes diferentes:

```text
PP-OCRv5 + ONNX Runtime
        → listas y documentos

ML Kit Barcode Scanning
        → códigos de barras y códigos QR
```

ML Kit Text Recognition no forma parte del OCR documental de v1.4.0.

---

## Orientación de imágenes

Las imágenes pueden corregirse antes del OCR mediante:

- orientación EXIF;
- giro manual de 90 grados a la izquierda;
- giro manual de 90 grados a la derecha;
- combinaciones equivalentes de 0, 90, 180 y 270 grados.

El giro manual no modifica el archivo original.

Si el usuario gira una imagen después de haberla procesado, el resultado anterior se invalida y la imagen puede procesarse nuevamente con la nueva orientación.

---

## Reconstrucción de listas

La reconstrucción documental utiliza la posición de las regiones reconocidas para conservar un orden de lectura útil.

La aplicación puede trabajar con:

- una columna;
- dos columnas;
- tres o más columnas cuando existe evidencia espacial suficiente;
- columnas con diferente número de referencias;
- encabezados o líneas globales;
- regiones OCR que necesitan separarse antes de ordenar el documento.

La lectura se realiza verticalmente dentro de cada columna y de izquierda a derecha entre columnas.

Cuando la estructura espacial es ambigua, la aplicación prioriza una degradación segura antes que mezclar referencias de columnas distintas.

---

## Interpretación revisable

El texto obtenido mediante OCR se considera siempre una propuesta.

La aplicación puede utilizar referencias conocidas en Room para ayudar a resolver errores habituales de reconocimiento, sin reemplazar silenciosamente el valor observado.

El flujo puede distinguir entre:

- coincidencia exacta;
- sugerencia única;
- ambigüedad;
- referencia no encontrada.

También puede conservar información documental asociada, como:

- cantidad;
- unidad;
- título;
- comprador o tienda;
- destino documental.

Los errores OCR potenciales se tratan únicamente cuando son compatibles con la estructura esperada y con la información disponible.

La revisión manual continúa siendo obligatoria antes de confirmar la lista.

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

- corregir su orientación cuando sea necesario;
- procesar el documento localmente mediante PP-OCRv5;
- reconstruir listas de una, dos o varias columnas;
- interpretar referencias y datos documentales;
- revisar coincidencias exactas, sugeridas, ambiguas o no encontradas;
- corregir, añadir o eliminar referencias;
- consultar sus ubicaciones en el orden confirmado;
- preparar y registrar el historial documental.

La calidad del reconocimiento depende de la orientación, nitidez, iluminación y legibilidad de la imagen. El usuario debe revisar siempre las propuestas antes de continuar.

Las fotografías no se conservan permanentemente ni se envían a servicios externos.

---

## Evaluación y estabilidad del OCR

La versión 1.4 incorpora una infraestructura reproducible para evaluar el comportamiento del reconocimiento documental.

La evaluación permite separar errores correspondientes a:

- detección de regiones;
- reconocimiento de caracteres;
- reconstrucción de líneas;
- orden de columnas;
- interpretación documental;
- comparación con referencias conocidas.

También permite observar:

- tiempo de procesamiento;
- diferencia entre primera ejecución y ejecuciones posteriores;
- consumo aproximado de memoria;
- estabilidad en ejecuciones consecutivas.

Las métricas son diagnósticas y no sustituyen la revisión funcional del usuario.

La versión también reduce asignaciones y copias temporales del pipeline OCR, reutiliza las sesiones ONNX durante su ciclo de vida y mantiene la inferencia fuera del hilo principal.

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
- ML Kit Barcode Scanning
- PP-OCRv5
- ONNX Runtime
- ExifInterface
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
- `domain` contiene modelos, reglas, normalización y validaciones.
- `data` contiene Room, archivos, escáner, OCR y demás implementaciones técnicas.
- `core` reúne contratos y modelos técnicos reutilizables.
- `app` compone explícitamente las dependencias.

La dirección preferente de dependencias es:

```text
Activity
    ↓
ViewModel
    ↓
servicio o repositorio
    ↓
contrato de dominio
    ↑
implementación técnica
```

El OCR documental permanece detrás de:

```text
DocumentTextRecognizer
        ↑
PaddleOcrDocumentTextRecognizer
```

La interfaz no conoce directamente sesiones de ONNX Runtime, tensores, modelos internos, DAO, entidades Room ni consultas SQL.

Se introducen puertos, adaptadores o servicios adicionales únicamente cuando representan límites o responsabilidades reales.

---

## Funcionamiento offline

Las funciones principales de AlmacenTracker continúan funcionando sin conexión a Internet:

- gestión de mercadería;
- importación y exportación CSV;
- copias de seguridad y restauración;
- escaneo individual;
- procesamiento OCR;
- revisión de listas;
- consulta de ubicaciones;
- historial documental.

Los modelos PP-OCRv5 y el diccionario forman parte de la aplicación.

El procesamiento documental no requiere un backend ni un servicio OCR remoto.

---

## Requisitos

- Android 8.0 o superior.
- No requiere Internet para gestionar mercadería, archivos CSV, escaneo, OCR o historial.
- El escaneo individual requiere una cámara compatible.
- La selección de imágenes utiliza el selector de fotos de Android.
- El reconocimiento depende de la calidad, orientación y legibilidad de la imagen.
- Los modelos PP-OCRv5 se incluyen en la aplicación y se ejecutan localmente mediante ONNX Runtime.
- El dispositivo debe disponer de memoria suficiente para cargar y ejecutar los modelos.
- Las cantidades del historial son documentales y no representan stock.

---

## Autor

Randy Méndez
