# AlmacenTracker

AlmacenTracker es una aplicación Android para registrar, consultar y localizar mercadería dentro de un almacén.

Cada mercadería se identifica mediante una combinación única de categoría y código. Su ubicación está formada por un sitio obligatorio y una posición opcional. Toda la información se almacena localmente mediante Room, por lo que la aplicación puede utilizarse sin conexión a Internet.

---

## Versión

**AlmacenTracker v1.4.0 — En desarrollo**

Objetivo:

```text
Evaluar y evolucionar el OCR local mediante PP-OCRv5 y ONNX Runtime
sin perder el funcionamiento offline
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
- Utilizar el selector de documentos de Android sin permisos generales de almacenamiento.

### Escaneo individual

- Escanear códigos de barras y códigos QR.
- Buscar mercadería mediante el código escaneado.
- Utilizar el escáner durante el registro y la edición.
- Mantener la introducción manual cuando la cámara no está disponible.
- Controlar permisos, cancelaciones y errores de cámara.

### Procesamiento de listas

- Tomar fotografías de listas.
- Seleccionar imágenes o capturas de pantalla.
- Procesar imágenes localmente mediante reconocimiento de texto.
- Corregir orientación, escala y contraste antes del OCR.
- Reconstruir referencias de listas de una o dos columnas.
- Revisar coincidencias exactas, sugeridas, ambiguas o no encontradas.
- Corregir, añadir, eliminar y confirmar referencias.
- Consultar conjuntamente la ubicación de las referencias confirmadas.
- Identificar referencias no encontradas.
- Conservar el orden documental.
- Procesar las imágenes sin enviarlas a servicios externos.

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
- Funcionar completamente sin conexión a Internet.

### Funcionalidades previstas para v1.4

- Evaluar PP-OCRv5 como evolución del reconocimiento local de listas.
- Integrar ONNX Runtime para ejecutar modelos OCR en el dispositivo.
- Incorporar un modelo de detección de texto optimizado para dispositivos móviles.
- Incorporar un modelo de reconocimiento de texto optimizado para dispositivos móviles.
- Preparar la carga local de los modelos necesarios para el procesamiento.
- Mantener las imágenes y los resultados dentro del dispositivo.
- Separar la detección de regiones de texto del reconocimiento de su contenido.
- Adaptar la salida del nuevo motor al contrato documental existente.
- Mantener la reconstrucción de filas y columnas.
- Mantener la revisión obligatoria antes de confirmar referencias.
- Conservar la corrección, adición y eliminación manual de referencias.
- Comparar la precisión del reconocimiento sobre listas reales de prueba.
- Medir el tiempo de procesamiento.
- Medir el consumo aproximado de memoria.
- Evaluar el impacto de los modelos sobre el tamaño de la aplicación.
- Controlar errores de inicialización, carga y ejecución del motor OCR.
- Evitar bloqueos del hilo principal durante el procesamiento.
- Liberar correctamente imágenes, tensores y recursos nativos.
- Mantener compatibilidad con fotografías, imágenes y capturas de pantalla.
- Mantener el funcionamiento completamente offline.
- Conservar intactos la mercadería, los archivos CSV y el historial documental.
- Mantener una separación clara entre la interfaz, el contrato OCR y la implementación técnica.
- Añadir pruebas unitarias, instrumentadas y comparativas para el nuevo procesamiento.

---

## Evolución del OCR local en v1.4

La versión 1.4 se centrará en evaluar y evolucionar el reconocimiento local de documentos mediante PP-OCRv5 y ONNX Runtime.

El flujo previsto será:

```text
fotografía o imagen
        ↓
preprocesamiento local
        ↓
detección de regiones de texto
        ↓
reconocimiento del contenido
        ↓
reconstrucción documental
        ↓
extracción de referencias y datos documentales
        ↓
revisión del usuario
```

La evaluación inicial considerará modelos optimizados para dispositivos móviles:

```text
PP-OCRv5_mobile_det
PP-OCRv5_mobile_rec
```

El modelo de detección deberá localizar las regiones que contienen texto.

El modelo de reconocimiento deberá convertir cada región detectada en contenido textual.

La integración deberá adaptar sus resultados a los contratos existentes del proyecto, evitando que Activities y ViewModels dependan directamente de clases internas de ONNX Runtime.

---

## Objetivos de la evaluación

La incorporación del nuevo procesamiento no se considerará satisfactoria únicamente porque el modelo pueda ejecutarse.

La evaluación deberá comprobar:

- precisión sobre referencias impresas;
- precisión sobre referencias con espacios;
- conservación de ceros iniciales;
- reconocimiento de letras finales;
- separación entre referencia, cantidad y unidad;
- comportamiento en listas de una columna;
- comportamiento en listas de dos columnas;
- tolerancia a inclinación moderada;
- comportamiento con iluminación irregular;
- comportamiento con imágenes de distinta resolución;
- tiempo total de procesamiento;
- memoria utilizada durante la inferencia;
- estabilidad en dispositivos compatibles;
- impacto sobre el tamaño de las APK;
- recuperación después de errores;
- funcionamiento sin conexión.

La decisión final deberá basarse en resultados medibles y no únicamente en una prueba aislada.

---

## Contrato del reconocimiento documental

La interfaz de usuario no deberá conocer directamente:

- sesiones de ONNX Runtime;
- tensores;
- nombres internos de entradas y salidas;
- dimensiones específicas de los modelos;
- detalles de posprocesamiento;
- archivos internos de los modelos.

El flujo deberá conservar un contrato equivalente a:

```text
DocumentTextRecognizer
        ↓
RecognizedDocument
        ↓
RecognizedTextLine
```

La implementación técnica podrá evolucionar sin obligar a reescribir el flujo de captura, revisión, localización o historial.

Cuando exista más de una implementación real, el contrato podrá admitir componentes como:

```text
DocumentTextRecognizer
        ├── implementación actual
        └── OnnxPaddleDocumentTextRecognizer
```

La separación deberá responder a una necesidad real de sustitución y evaluación, no a una organización ceremonial.

---

## Preprocesamiento y posprocesamiento

La versión 1.4 deberá conservar o mejorar el preprocesamiento local existente:

- lectura segura de la imagen;
- corrección de orientación;
- reducción controlada de resolución;
- mejora moderada del contraste;
- prevención de consumo excesivo de memoria;
- liberación de bitmaps intermedios.

La detección deberá producir regiones de texto ordenables.

El reconocimiento deberá conservar el contenido como texto.

El posprocesamiento deberá permitir:

- ordenar regiones;
- reconstruir filas;
- detectar una o dos columnas;
- conservar el orden de lectura;
- separar identidad, cantidad y unidad;
- mantener el texto original cuando aporte contexto;
- evitar correcciones automáticas agresivas;
- entregar propuestas revisables por el usuario.

---

## Revisión obligatoria

El resultado del OCR continuará siendo una propuesta.

La aplicación deberá permitir:

- revisar cada referencia;
- corregir categoría y código;
- conservar ceros iniciales;
- corregir cantidad y unidad;
- añadir referencias omitidas;
- eliminar falsos positivos;
- resolver coincidencias sugeridas o ambiguas;
- confirmar únicamente una lista válida.

La incorporación de PP-OCRv5 no deberá eliminar el control del usuario sobre el documento reconocido.

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

El reconocimiento continuará considerándose experimental mientras la evaluación de v1.4 no determine resultados suficientemente estables.

Las fotografías no se conservan permanentemente ni se envían a servicios externos.

---

## Gestión de archivos CSV

AlmacenTracker diferencia dos formatos:

- **CSV de intercambio:** permite exportar, compartir e importar mercadería.
- **CSV de copia de seguridad:** conserva los datos y sus fechas para restaurar el estado de la mercadería.

La aplicación utiliza el selector de documentos de Android sin solicitar acceso general al almacenamiento.

El historial documental no forma parte actualmente de estos archivos CSV.

---

## Tecnologías

### Tecnologías actuales

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

### Tecnologías previstas para v1.4

- PP-OCRv5
- ONNX Runtime
- Modelos OCR optimizados para dispositivos móviles
- Procesamiento local de tensores
- Inferencia fuera del hilo principal
- Pruebas comparativas del reconocimiento

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

La interfaz no deberá conocer DAO, entidades Room, consultas SQL, clases internas del motor OCR ni modelos ONNX.

Se introducirán puertos, adaptadores o servicios adicionales únicamente cuando representen límites o responsabilidades reales.

---

## Requisitos

- Android 8.0 o superior.
- No requiere Internet para gestionar mercadería, archivos CSV, escaneo, OCR o historial.
- El escaneo individual requiere una cámara compatible.
- La selección de imágenes utiliza el selector de fotos de Android.
- El reconocimiento depende de la calidad, orientación y legibilidad de la imagen.
- Los modelos previstos para v1.4 deberán ejecutarse localmente.
- El dispositivo deberá disponer de memoria suficiente para cargar y ejecutar los modelos.
- Las cantidades del historial son documentales y no representan stock.

---

## Autor

Randy Méndez