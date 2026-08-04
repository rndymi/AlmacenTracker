# HU-36 — Construir el pipeline documental PP-OCRv5

> Cuarta historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-36  
**Nombre:** Construir el pipeline documental PP-OCRv5  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-36-pipeline-documental-ppocrv5`  
**Rama de integración:** `develop`  

**Dependencias:**

- HU-33 — Preparar ONNX Runtime y los recursos PP-OCRv5.
- HU-34 — Detectar regiones de texto con PP-OCRv5.
- HU-35 — Reconocer el contenido de las regiones detectadas.

**Issue prevista:** `#40`

---

## 2. Historia de usuario

Como usuario,  
quiero que la aplicación procese una imagen completa mediante detección y reconocimiento PP-OCRv5,  
para obtener un documento textual local que pueda utilizarse posteriormente en el flujo de revisión de listas.

---

## 3. Objetivo

Construir la primera implementación completa del contrato:

```text
DocumentTextRecognizer
```

utilizando:

```text
PaddleTextDetector
+
PaddleTextRecognizer
```

Flujo previsto:

```text
DocumentImage
        ↓
PaddleOcrDocumentTextRecognizer
        ↓
inicializar runtime cuando sea necesario
        ↓
obtener Bitmap procesado
        ↓
detectar regiones
        ↓
reconocer cada región
        ↓
descartar resultados vacíos no útiles
        ↓
adaptar coordenadas y texto
        ↓
RecognizedTextElement
        ↓
RecognizedTextLine
        ↓
RecognizedDocument
        ↓
DocumentRecognitionCallback
```

HU-36 deberá demostrar que una imagen completa puede atravesar el pipeline PP-OCRv5 y producir un `RecognizedDocument` compatible con el contrato existente.

---

## 4. Regla principal

> HU-36 coordina detección y reconocimiento y adapta el resultado al contrato documental existente.

HU-36 no deberá asumir responsabilidades de:

```text
parser de referencias
revisión de mercadería
consulta de ubicaciones
cantidad y unidad
historial
```

Resultado esperado:

```text
imagen
    ↓
regiones
    ↓
texto por región
    ↓
RecognizedDocument
```

Resultado reservado para HU-37:

```text
RecognizedDocument
    ↓
reconstrucción documental completa
    ↓
referencias y datos documentales
    ↓
pantalla de revisión
```

---

## 5. Base documental y arquitectónica

HU-36 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-33-preparar-onnx-ppocrv5.md`;
- `HU-34-detectar-regiones-texto-ppocrv5.md`;
- `HU-35-reconocer-contenido-regiones-ppocrv5.md`;
- el estado real de `AlmacenTrackerHU35.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- el contrato existente `DocumentTextRecognizer`;
- el funcionamiento completamente offline;
- la revisión obligatoria del resultado OCR;
- la separación entre infraestructura ONNX y reglas de mercadería;
- la liberación explícita de imágenes y recursos temporales;
- la prevención de callbacks repetidos;
- la cancelación lógica de resultados obsoletos;
- la política de no crear capas ceremoniales.

El plan general asigna a HU-36:

```text
coordinación detector–reconocedor
+
orden de regiones
+
resultado RecognizedDocument
+
estados
+
cancelación lógica
+
errores
+
integración con ViewModel
+
funcionamiento offline
```

---

## 6. Estado real antes de HU-36

El análisis de `AlmacenTrackerHU35.zip` confirma:

```groovy
versionCode 5
versionName "1.4.0"
minSdk 26
targetSdk 36
Java 11
```

La rama incluida es:

```text
develop
```

La última integración es:

```text
merge HU35 #39 into develop
```

HU-35 ya proporciona:

```text
data/document/onnx/recognition/
├── CtcDecodingResult.java
├── PaddleCtcDecoder.java
├── PaddleOcrTokenMapper.java
├── PaddleTextRecognizer.java
├── PaddleTextRecognizerConfiguration.java
├── PaddleTextRecognizerPreprocessor.java
├── RecognizerInput.java
└── TextRecognitionException.java
```

También proporciona:

```text
data/document/onnx/model/
└── TextRecognitionResult.java
```

HU-34 ya proporciona:

```text
PaddleTextDetector
TextDetectionResult
DetectedTextRegion
DetectedTextPolygon
TextPoint
```

`ReferenceListModule` ya compone:

```text
PaddleTextDetector
PaddleTextRecognizer
```

pero la factory visible continúa creando:

```text
MlKitDocumentTextRecognizer
```

El contrato actual es:

```java
public interface DocumentTextRecognizer {

    void recognize(
            DocumentImage documentImage,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    );

    void close();
}
```

El callback actual ofrece:

```java
void onSuccess(RecognizedDocument document);
void onImageOpenError();
void onRecognitionError();
```

---

## 7. Estado real del flujo de captura

`ReferenceListCaptureViewModel` ya:

- recibe un `DocumentImageProcessor`;
- recibe un `DocumentTextRecognizer`;
- procesa la imagen antes del OCR;
- aplica un identificador incremental por solicitud;
- evita dos procesamientos simultáneos;
- ignora resultados obsoletos;
- diferencia error de imagen y error de reconocimiento;
- representa documento sin líneas como `NO_TEXT`;
- conserva el resultado ante rotación;
- cierra el procesador y reconocedor en `onCleared()`.

Por tanto, HU-36 no deberá reimplementar dentro del pipeline:

- estado de pantalla;
- ids de solicitud del ViewModel;
- navegación;
- selección de imagen;
- captura de cámara;
- carga de URI;
- corrección EXIF general;
- renderizado de errores.

El nuevo reconocedor deberá ajustarse al contrato ya consumido por el ViewModel.

---

## 8. Restricción real de acceso al Bitmap

`AndroidDocumentImage` se encuentra en:

```text
data/document/AndroidDocumentImage.java
```

y es una clase con visibilidad de paquete.

Su método:

```java
Bitmap getRecognitionBitmap()
```

también utiliza visibilidad de paquete.

Por tanto, una clase ubicada directamente en:

```text
data/document/
```

puede obtener el bitmap procesado sin ampliar innecesariamente la API pública.

Decisión recomendada:

```text
data/document/PaddleOcrDocumentTextRecognizer.java
```

No se recomienda ubicar el orquestador en:

```text
data/document/onnx/
```

si eso obliga a hacer pública toda la implementación de `AndroidDocumentImage`.

El orquestador puede depender de componentes ONNX internos sin exponer el bitmap en `core`.

---

## 9. Decisión sobre el contrato `DocumentImage`

HU-36 no deberá añadir:

```java
Bitmap getBitmap();
```

a:

```text
core/document/DocumentImage
```

Motivos:

- `core` dejaría de ser independiente de Android;
- expondría una implementación técnica;
- otros motores podrían no utilizar Bitmap;
- el contrato actual ya funciona con el adaptador Android.

Tampoco se deberá hacer pública `AndroidDocumentImage` únicamente para resolver el acceso desde otro paquete.

La solución preferida es mantener el adaptador dentro de `data.document`.

---

## 10. Alcance incluido

HU-36 incluye:

- crear `PaddleOcrDocumentTextRecognizer`;
- implementar `DocumentTextRecognizer`;
- recibir `PaddleOcrRuntimeProvider`;
- recibir `PaddleTextDetector`;
- recibir `PaddleTextRecognizer`;
- recibir un executor OCR;
- recibir o reutilizar un reloj simple;
- validar `DocumentImage`;
- exigir `AndroidDocumentImage`;
- validar `DocumentImageSource`;
- validar callback;
- comprobar estado cerrado;
- inicializar el runtime de forma perezosa;
- esperar el resultado de inicialización sin bloquear UI;
- ejecutar el pipeline fuera del hilo principal;
- obtener el bitmap procesado;
- ejecutar detección una sola vez;
- tratar una detección sin regiones;
- recorrer regiones en orden estable;
- reconocer las regiones secuencialmente;
- conservar la región de origen;
- descartar textos vacíos;
- definir política para errores individuales;
- adaptar resultados a `RecognizedTextElement`;
- construir líneas iniciales compatibles;
- construir `RecognizedDocument`;
- conservar `DocumentImageSource`;
- asignar `recognizedAt`;
- emitir exactamente un callback terminal;
- cerrar siempre `DocumentImage`;
- impedir callbacks después de `close()`;
- ignorar resultados obsoletos internamente cuando corresponda;
- cerrar de forma idempotente;
- no cerrar el runtime compartido desde el reconocedor;
- no cerrar sesiones compartidas;
- mantener ML Kit disponible mientras se valida la integración;
- conectar PP-OCRv5 al `ReferenceListCaptureViewModel` solo cuando el pipeline esté probado;
- mantener funcionamiento offline;
- pruebas unitarias;
- pruebas de integración;
- pruebas instrumentadas;
- pruebas de regresión;
- CI.

---

## 11. Alcance excluido

HU-36 no incluye:

- cambiar reglas de referencias;
- modificar `WarehouseReferenceParser`;
- extraer cantidad;
- extraer unidad;
- corregir automáticamente caracteres;
- resolver `O ↔ 0`;
- detectar una o dos columnas con nuevas reglas;
- rediseñar `DocumentLineReconstructor`;
- comparar ubicaciones;
- consultar Room;
- modificar mercadería;
- registrar historial;
- cambiar tablas Room;
- añadir migración;
- comparar precisión final de ambos motores;
- optimizar memoria de forma avanzada;
- paralelizar reconocimientos;
- usar GPU;
- usar NNAPI;
- eliminar ML Kit;
- eliminar código previo;
- añadir selector de motor visible;
- añadir ajustes;
- almacenar imágenes;
- almacenar texto OCR completo;
- procesar PDF;
- procesar varias páginas.

La reconstrucción avanzada y la integración con revisión corresponden a HU-37.

La evaluación comparativa corresponde a HU-38.

La optimización corresponde a HU-39.

---

## 12. Nuevo adaptador OCR

Se creará:

```text
PaddleOcrDocumentTextRecognizer
```

Ubicación recomendada:

```text
data/document/
```

Responsabilidad:

```text
adaptar el pipeline PP-OCRv5
al contrato DocumentTextRecognizer
```

Dependencias:

```text
ExecutorService
PaddleOcrRuntimeProvider
PaddleTextDetector
PaddleTextRecognizer
DocumentLineReconstructor opcional
LongSupplier clock
```

No deberá depender de:

- Activity;
- ViewModel;
- LiveData;
- Room;
- repositorios;
- parser de referencias;
- recursos de interfaz.

---

## 13. Firma orientativa

```java
public final class PaddleOcrDocumentTextRecognizer
        implements DocumentTextRecognizer {

    public PaddleOcrDocumentTextRecognizer(
            ExecutorService ocrExecutor,
            PaddleOcrRuntimeProvider runtimeProvider,
            PaddleTextDetector detector,
            PaddleTextRecognizer recognizer,
            DocumentLineReconstructor lineReconstructor,
            LongSupplier clock
    ) {
        // Validación y asignación.
    }

    @Override
    public void recognize(
            DocumentImage documentImage,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    ) {
        // Inicialización y coordinación.
    }

    @Override
    public void close() {
        // Cierre lógico idempotente.
    }
}
```

No se requiere una interfaz adicional para el pipeline porque:

```text
DocumentTextRecognizer
```

ya representa el puerto real necesario.

---

## 14. Responsabilidad del orquestador

`PaddleOcrDocumentTextRecognizer` deberá:

1. validar argumentos;
2. comprobar que no esté cerrado;
3. validar tipo de imagen;
4. reservar una solicitud interna;
5. solicitar inicialización al runtime;
6. continuar solo si el runtime queda `READY`;
7. ejecutar detector;
8. reconocer regiones;
9. mapear resultados;
10. construir documento;
11. entregar callback;
12. cerrar imagen;
13. limpiar estado interno.

No deberá contener:

- normalización de píxeles;
- lectura de logits;
- CTC;
- flood fill;
- consulta SQL;
- parseo de referencias.

---

## 15. Inicialización del runtime

`PaddleOcrRuntimeProvider.initialize(...)` ya:

- agrupa callbacks concurrentes;
- ejecuta inicialización en `ocrExecutor`;
- reutiliza el resultado `READY`;
- devuelve error cuando está cerrado;
- evita sesiones duplicadas.

El pipeline deberá usar:

```java
runtimeProvider.initialize(result -> {
    // Continuar o fallar.
});
```

No deberá llamar directamente:

```java
runtimeProvider.requireReadySessions()
```

antes de asegurar que el runtime está listo.

No deberá realizar espera activa:

```text
while state != READY
```

ni bloquear con:

```text
Thread.sleep(...)
Future.get() en UI
CountDownLatch.await() en UI
```

---

## 16. Uso del executor OCR

`AppContainer` ya crea:

```java
Executors.newSingleThreadExecutor()
```

para OCR.

HU-36 deberá pasar esa misma instancia a:

```text
ReferenceListModule
```

y al nuevo reconocedor.

Cambio de composición previsto:

```text
AppContainer
    ├── ocrExecutor
    ├── PaddleOcrRuntimeProvider
    └── ReferenceListModule(
            context,
            repository,
            ocrExecutor,
            runtimeProvider
        )
```

No se creará un executor adicional dentro de:

```text
PaddleOcrDocumentTextRecognizer
```

Motivos:

- evitar hilos sin cerrar;
- mantener inferencia serializada;
- reutilizar la política ya definida;
- controlar el ciclo de vida desde `AppContainer`.

---

## 17. Precaución con el executor único

El mismo executor ejecuta la inicialización del runtime.

Por tanto:

- `initialize()` programará la carga cuando sea necesario;
- el callback de inicialización podrá ejecutarse en el hilo OCR;
- no deberá bloquear ese callback esperando otra tarea del mismo executor;
- el trabajo posterior podrá ejecutarse directamente si ya está en el hilo OCR;
- o podrá programarse después de devolver el callback.

No se deberá provocar un deadlock como:

```text
tarea OCR
    ↓
espera Future de otra tarea
    ↓
mismo executor de un solo hilo
```

---

## 18. Estrategia de continuación

Opción recomendada:

1. `recognize()` solicita inicialización;
2. el callback recibe `READY`;
3. el callback programa `processDocument(...)` en `ocrExecutor`;
4. `recognize()` retorna inmediatamente;
5. `processDocument(...)` ejecuta detector y reconocedor;
6. se entrega callback terminal.

Si la callback de `initialize()` ya se ejecuta en el mismo executor, programar una nueva tarea seguirá siendo seguro siempre que no se espere sincrónicamente su resultado.

Esto separa:

```text
inicialización
```

de:

```text
procesamiento documental
```

---

## 19. Estado interno del reconocedor

Se recomienda utilizar:

```text
AtomicBoolean closed
AtomicLong requestSequence
```

y una colección mínima de solicitudes activas solo si resulta necesaria.

El reconocedor deberá poder determinar:

```text
la solicitud sigue vigente
el reconocedor fue cerrado
el callback ya fue emitido
```

No deberá duplicar toda la lógica del ViewModel.

La protección interna existe para:

- cerrar la imagen si el ViewModel desaparece;
- no emitir después de `close()`;
- soportar inicialización asíncrona;
- garantizar callback único.

---

## 20. Solicitud interna

Se podrá crear una clase privada:

```text
RecognitionRequest
```

Datos:

```text
id
documentImage
sourceType
callback
completed
```

Responsabilidades:

- completar una sola vez;
- cerrar la imagen una sola vez;
- impedir callback repetido;
- comprobar cancelación;
- no sobrevivir después de finalizar.

No se recomienda crear un modelo público para esta responsabilidad interna.

---

## 21. Política de concurrencia

El ViewModel ya evita dos procesamientos simultáneos.

Aun así, el reconocedor deberá ser defensivo.

Política recomendada:

```text
una solicitud documental activa
por instancia de PaddleOcrDocumentTextRecognizer
```

Ante una segunda solicitud simultánea:

- rechazarla mediante `onRecognitionError()`; o
- ponerla en cola explícita.

Para mantener el comportamiento actual, se recomienda rechazar de forma controlada.

No se deberá ejecutar dos documentos simultáneamente sobre las mismas sesiones durante HU-36.

---

## 22. Validación de `DocumentImage`

El reconocedor deberá validar:

```text
documentImage != null
sourceType != null
callback != null
```

También:

```text
documentImage instanceof AndroidDocumentImage
documentImage.isClosed() == false
processedWidth > 0
processedHeight > 0
```

Si la implementación de imagen es incompatible:

```text
documentImage.close()
callback.onRecognitionError()
```

Si el bitmap no puede abrirse o está reciclado:

```text
callback.onImageOpenError()
```

La clasificación deberá ser coherente con el contrato existente.

---

## 23. Propiedad de `DocumentImage`

El `DocumentTextRecognizer` recibe la propiedad temporal de la imagen para el reconocimiento.

Regla:

> Toda ruta terminal deberá cerrar `DocumentImage`.

Rutas:

- éxito;
- imagen incompatible;
- runtime no disponible;
- detector falla;
- reconocedor falla;
- texto vacío;
- cierre durante procesamiento;
- excepción inesperada;
- error de memoria.

No deberá cerrarse antes de que todas las regiones hayan sido recortadas y reconocidas.

---

## 24. Obtención del Bitmap

Dentro de `data.document`:

```java
AndroidDocumentImage androidImage =
        (AndroidDocumentImage) documentImage;

Bitmap bitmap =
        androidImage.getRecognitionBitmap();
```

El bitmap:

- ya tiene orientación corregida;
- ya fue limitado por el procesador general;
- ya utiliza la imagen de reconocimiento;
- no pertenece al pipeline;
- no deberá reciclarse directamente;
- será reciclado al cerrar `AndroidDocumentImage`.

El detector y reconocedor podrán crear y reciclar sus propios bitmaps temporales.

---

## 25. Detección

El pipeline deberá ejecutar:

```java
TextDetectionResult detectionResult =
        detector.detect(bitmap);
```

Reglas:

- una sola detección por documento;
- no repetir detección por región;
- respetar el orden devuelto;
- no modificar la lista;
- lista vacía es un resultado válido;
- error técnico produce fallo del documento.

Cuando no existan regiones:

```text
RecognizedDocument con líneas vacías
```

y:

```text
callback.onSuccess(document)
```

El ViewModel actual lo convertirá en:

```text
NO_TEXT
```

No se deberá usar `onRecognitionError()` para una imagen válida sin texto.

---

## 26. Reconocimiento secuencial

Las regiones deberán procesarse:

```text
for region in detectionResult.regions
    recognizer.recognize(bitmap, region)
```

Orden:

```text
sourceOrder ascendente
```

o el orden estable ya entregado por `TextDetectionResult`.

No se deberán ordenar mediante texto reconocido.

No se deberán ejecutar varias inferencias en paralelo en HU-36.

Motivos:

- menor pico de memoria;
- sesión compartida;
- consistencia;
- aproximadamente quince referencias por lista;
- optimización reservada para HU-39.

---

## 27. Resultado vacío por región

`TextRecognitionResult` admite:

```text
text = ""
confidence = 0.0
```

El pipeline deberá descartar como elemento textual:

```text
null
texto vacío
texto compuesto solo por espacios
```

No deberá considerar que una región vacía invalida todo el documento.

Una región detectada puede ser ruido.

El documento será válido mientras el pipeline complete correctamente.

---

## 28. Política de confianza

HU-35 ya proporciona:

```java
TextRecognitionResult.isAboveThreshold(...)
```

HU-36 deberá definir una política explícita.

Recomendación inicial:

- conservar cualquier texto no vacío;
- conservar su posición;
- no ocultarlo únicamente por baja confianza;
- no convertirlo automáticamente en referencia válida;
- dejar que HU-37 y la revisión decidan.

Motivo:

```text
OCR propone
usuario revisa
```

No se debe perder texto potencialmente útil antes de la revisión.

La confianza podrá utilizarse en pruebas y métricas internas.

---

## 29. Error individual de reconocimiento

Debe definirse una política clara cuando una región falla técnicamente.

### Opción estricta

```text
una región falla
    → falla todo el documento
```

Ventaja:

- no se presenta un documento incompleto como completo.

### Opción tolerante

```text
una región falla
    → se omite
    → continúan las demás
```

Ventaja:

- puede recuperar resultados parciales.

Para HU-36 se recomienda la política estricta ante errores técnicos:

```text
TextRecognitionException
    → callback.onRecognitionError()
```

Los textos vacíos no son errores técnicos y sí se omiten.

La recuperación parcial podrá evaluarse cuando existan métricas reales.

---

## 30. Adaptación a `RecognizedTextElement`

Cada `TextRecognitionResult` no vacío deberá convertirse en:

```text
RecognizedTextElement
```

Datos:

```text
rawText
left
top
right
bottom
```

Las coordenadas se obtendrán de:

```text
result.getSourceRegion().getPolygon()
```

o de los límites calculados por la región.

Conversión:

```text
left = floor(minX)
top = floor(minY)
right = ceil(maxX)
bottom = ceil(maxY)
```

Después:

- limitar a `0..bitmapWidth`;
- limitar a `0..bitmapHeight`;
- exigir `right >= left`;
- exigir `bottom >= top`.

No se usarán coordenadas del tensor de reconocimiento.

---

## 31. Conversión del polígono

Se recomienda una clase o método privado:

```text
PaddleOcrRecognizedElementMapper
```

Solo se creará como clase si:

- centraliza límites;
- tiene pruebas propias;
- será reutilizada;
- evita que el orquestador crezca demasiado.

En caso contrario, un método privado es suficiente.

No se creará un mapper público por simetría.

---

## 32. Construcción inicial de líneas

HU-36 deberá entregar un `RecognizedDocument` válido.

Dos estrategias posibles:

### Estrategia A — Una región por línea

```text
TextRecognitionResult
    ↓
RecognizedTextLine
```

Ventajas:

- conserva el resultado directo;
- menor transformación;
- útil para validar el pipeline.

Limitaciones:

- una línea documental puede estar fragmentada;
- no aprovecha agrupación espacial existente.

### Estrategia B — Reutilizar `DocumentLineReconstructor`

```text
TextRecognitionResult
    ↓
RecognizedTextElement
    ↓
DocumentLineReconstructor
    ↓
RecognizedTextLine
```

Recomendación:

```text
reutilizar DocumentLineReconstructor
```

porque ya representa una responsabilidad real y entrega el contrato esperado.

Sin embargo, HU-36 no deberá modificar todavía sus reglas.

---

## 33. Reutilización de `DocumentLineReconstructor`

El flujo será:

```java
List<RecognizedTextElement> elements =
        mapRecognitionResults(...);

List<RecognizedTextLine> lines =
        lineReconstructor.reconstruct(
                elements,
                bitmap.getWidth()
        );
```

Ventajas:

- conserva coordenadas;
- agrupa elementos;
- mantiene compatibilidad con ML Kit;
- reutiliza detección de columnas existente;
- produce `rawText` y `reconstructedText`;
- evita duplicar lógica.

HU-36 deberá comprobar que sus entradas PP-OCRv5 son compatibles.

Los ajustes específicos de reconstrucción se reservarán para HU-37.

---

## 34. Fallback de líneas

Es posible que:

```text
elements no vacíos
```

pero:

```text
lineReconstructor.reconstruct(...) vacío
```

El pipeline deberá tener un fallback defensivo:

```text
una línea por resultado reconocido
```

ordenada por `sourceOrder`.

Cada línea fallback deberá incluir:

- índice;
- texto;
- límites;
- elemento correspondiente.

Esto evita perder un reconocimiento válido por una anomalía de reconstrucción.

---

## 35. Construcción de `RecognizedDocument`

Se deberá crear:

```java
new RecognizedDocument(
        sourceType,
        lines,
        clock.getAsLong()
)
```

Reglas:

- `sourceType` se conserva;
- `lines` no nula;
- lista vacía válida;
- `recognizedAt > 0`;
- el reloj se consulta una sola vez;
- no se utiliza la hora de inicio si se quiere representar finalización;
- la estrategia temporal deberá probarse.

No se añadirán campos ONNX a `RecognizedDocument`.

---

## 36. Callback único

Cada solicitud deberá terminar exactamente en una de estas rutas:

```text
onSuccess
onImageOpenError
onRecognitionError
```

Nunca:

```text
onSuccess + onRecognitionError
dos onSuccess
dos errores
```

Se recomienda encapsular la finalización:

```text
completeSuccess(...)
completeImageError(...)
completeRecognitionError(...)
completeSilentlyAfterClose(...)
```

Cada método deberá:

1. comprobar `completed`;
2. marcar completada;
3. cerrar imagen;
4. retirar solicitud activa;
5. emitir callback cuando corresponda.

---

## 37. Callback después de `close()`

Cuando el reconocedor se cierre durante una operación:

- la imagen deberá cerrarse;
- el resultado posterior deberá ignorarse;
- no deberá actualizarse la UI;
- no se deberá emitir un error tardío;
- la solicitud deberá quedar finalizada internamente.

El ViewModel ya invalida su solicitud al limpiarse, pero el adaptador debe ser seguro por sí mismo.

---

## 38. Método `close()`

`close()` deberá:

- ser idempotente;
- marcar el reconocedor como cerrado;
- invalidar solicitudes activas;
- cerrar imágenes activas;
- impedir nuevas solicitudes;
- no cerrar `PaddleOcrRuntimeProvider`;
- no cerrar sesiones;
- no cerrar `ocrExecutor`;
- no cerrar detector o reconocedor si estos no son propietarios de sesiones.

Motivo:

```text
runtime y executor pertenecen a AppContainer
```

Si `PaddleTextDetector` o `PaddleTextRecognizer` no implementan `close()`, HU-36 no deberá añadir uno sin recursos propios que liberar.

---

## 39. Propiedad del runtime

`PaddleOcrRuntimeProvider` es compartido por:

- detector;
- reconocedor;
- pipeline;
- futuras pruebas o componentes.

Por tanto:

```text
PaddleOcrDocumentTextRecognizer.close()
        ≠
PaddleOcrRuntimeProvider.close()
```

El runtime se cerrará desde el propietario global cuando exista un punto de ciclo de vida real.

No se deberá romper una segunda apertura de la pantalla cerrando sesiones al finalizar el primer ViewModel.

---

## 40. Error de inicialización

Si:

```text
PaddleOcrInitializationResult.isReady() == false
```

el pipeline deberá:

- cerrar la imagen;
- emitir `onRecognitionError()`;
- no exponer el error técnico;
- no ejecutar detector;
- no ejecutar reconocedor;
- permitir que una futura solicitud reintente según el estado del provider.

La política actual del provider conserva `ERROR`.

Si HU-36 necesita reintento real, deberá comprobar si el provider permite volver a inicializar desde `ERROR`.

No se deberá fingir reintento si el estado actual no lo soporta.

---

## 41. Observación crítica sobre reintento del provider

La implementación actual de `PaddleOcrRuntimeProvider.initialize(...)` trata:

```text
ERROR
```

como una nueva inicialización porque entra en la rama general distinta de `READY`, `INITIALIZING` y `CLOSED`.

Por tanto, una solicitud posterior puede reintentar.

HU-36 deberá conservar ese comportamiento.

No se añadirá otro mecanismo de reintento dentro del pipeline.

---

## 42. Clasificación de errores

### `onImageOpenError()`

Usar cuando:

- imagen incompatible;
- bitmap cerrado;
- bitmap reciclado;
- dimensiones inválidas;
- acceso al bitmap falla antes de inferencia.

### `onRecognitionError()`

Usar cuando:

- runtime no inicia;
- detector falla;
- reconocedor falla;
- salida ONNX incompatible;
- error de memoria;
- error de mapeo;
- excepción inesperada durante OCR.

### `onSuccess()` con documento vacío

Usar cuando:

- imagen válida sin regiones;
- todas las regiones producen texto vacío;
- solo se detecta ruido reconocible como vacío.

No se deberá confundir:

```text
sin texto
```

con:

```text
fallo técnico
```

---

## 43. Excepciones esperadas

El pipeline deberá tratar:

```text
TextDetectionException
TextRecognitionException
IllegalArgumentException
IllegalStateException
OutOfMemoryError
RuntimeException inesperada
```

No deberá capturar:

```text
Throwable
```

de forma general.

No deberá ocultar errores fatales de la máquina virtual ajenos al alcance.

`OutOfMemoryError` sí podrá transformarse porque el procesamiento de imágenes es una causa plausible y debe cerrar la imagen.

---

## 44. Estado visible de la captura

HU-36 deberá reutilizar los estados actuales:

```text
EMPTY
IMAGE_SELECTED
PROCESSING
TEXT_RECOGNIZED
NO_TEXT
IMAGE_ERROR
RECOGNITION_ERROR
```

No es necesario añadir:

```text
INITIALIZING_OCR
DETECTING
RECOGNIZING
```

en esta historia si el flujo actual solo muestra procesamiento general.

Motivo:

- mantener alcance;
- evitar cambios visuales temporales;
- la operación es única para el usuario.

Los estados por etapa podrán añadirse en HU-39 si las mediciones muestran que el tiempo exige más información.

---

## 45. Integración con el ViewModel

La integración deberá realizarse mediante composición.

Antes:

```text
ReferenceListCaptureViewModel
    ← MlKitDocumentTextRecognizer
```

Después de validar HU-36:

```text
ReferenceListCaptureViewModel
    ← PaddleOcrDocumentTextRecognizer
```

El ViewModel no deberá modificarse para conocer:

- detector;
- reconocedor;
- ONNX;
- runtime;
- regiones;
- logits;
- diccionario.

El cambio de implementación deberá demostrar el valor del contrato `DocumentTextRecognizer`.

---

## 46. Estrategia de activación

HU-36 deberá evitar cambiar el motor al principio del desarrollo.

Orden recomendado:

1. construir pipeline;
2. probar unidad por unidad;
3. probar pipeline instrumentado;
4. probar una imagen completa;
5. verificar callback y cierre;
6. conectar en `ReferenceListModule`;
7. ejecutar regresión manual;
8. mantener posibilidad de revertir la composición si falla.

No se recomienda introducir un selector visible de motor en HU-36.

---

## 47. Composición en `ReferenceListModule`

`ReferenceListModule` ya crea:

```text
PaddleTextDetector
PaddleTextRecognizer
```

HU-36 deberá añadir:

```text
PaddleOcrDocumentTextRecognizer
```

y reutilizar:

```text
DocumentLineReconstructor
```

La factory deberá recibir el nuevo contrato:

```java
DocumentTextRecognizer textRecognizer =
        paddleOcrDocumentTextRecognizer;
```

No se deberán construir nuevas instancias de detector y reconocedor por cada ViewModel.

---

## 48. Vida útil de la instancia

El `ReferenceListModule` actual crea un reconocedor y lo reutiliza al construir factories.

Sin embargo, el ViewModel llama:

```java
textRecognizer.close()
```

en `onCleared()`.

Si la misma instancia se reutiliza en otro ViewModel, quedaría cerrada.

HU-36 deberá revisar esta propiedad.

Decisión recomendada:

```text
crear una instancia de PaddleOcrDocumentTextRecognizer
por ReferenceListCaptureViewModel
```

pero reutilizar:

```text
runtime provider
detector
recognizer
executor
```

que son componentes compartidos.

Así:

- cada ViewModel posee su adaptador;
- `close()` solo invalida sus solicitudes;
- las sesiones continúan disponibles;
- una nueva pantalla obtiene un adaptador abierto.

---

## 49. Ajuste de la factory

`ReferenceListCaptureViewModelFactory` podrá recibir:

- un `DocumentTextRecognizer` creado para esa factory; o
- un proveedor/factory de reconocedores.

Debe verificarse cómo se crea actualmente el ViewModel.

Si la misma factory se usa una sola vez por Activity, crear el reconocedor al pedir la factory puede ser suficiente.

No se deberá introducir:

```text
DocumentTextRecognizerFactory
```

si solo envuelve un constructor sin mejorar el ciclo de vida.

Una alternativa pragmática es crear la instancia dentro de:

```text
provideReferenceListCaptureViewModelFactory()
```

---

## 50. Riesgo de cerrar ML Kit compartido

La misma observación afecta al reconocedor actual.

HU-36 no necesita refactorizar toda la composición anterior, pero sí deberá garantizar que el nuevo adaptador no quede inutilizable después de cerrar una pantalla.

El ownership deberá quedar cubierto por prueba:

```text
abrir captura
cerrar captura
abrir captura nuevamente
procesar imagen
```

---

## 51. Modelo intermedio del pipeline

No es obligatorio crear:

```text
PaddleOcrPipelineResult
```

si el pipeline puede construir directamente:

```text
RecognizedDocument
```

Los modelos ya disponibles son suficientes:

```text
TextDetectionResult
TextRecognitionResult
RecognizedTextElement
RecognizedTextLine
RecognizedDocument
```

Solo deberá añadirse un resultado intermedio si concentra una responsabilidad concreta de diagnóstico o pruebas.

---

## 52. Métricas

HU-34 y HU-35 ya contienen tiempos parciales.

HU-36 podrá sumar:

```text
initializationDuration
detectionDuration
recognitionDurationTotal
mappingDuration
totalDuration
regionCount
recognizedRegionCount
emptyRegionCount
```

Estas métricas:

- se usarán en desarrollo;
- no se mostrarán;
- no se persistirán;
- no se añadirán al dominio;
- no deberán registrar texto.

No se requiere un sistema de métricas completo en HU-36.

HU-38 y HU-39 ampliarán la evaluación.

---

## 53. Privacidad

El pipeline deberá mantener:

- procesamiento local;
- ausencia de red;
- ausencia de subida de imágenes;
- ausencia de persistencia de fotografías;
- ausencia de logs de texto completo;
- ausencia de logs de logits;
- cierre del bitmap al terminar;
- solo resultado temporal en memoria;
- revisión obligatoria posterior.

No se añadirá permiso de Internet.

---

## 54. Pruebas unitarias del orquestador

Se deberán utilizar dobles controlados para detector, reconocedor y runtime cuando sea posible.

Casos mínimos:

- argumentos nulos;
- imagen incompatible;
- imagen cerrada;
- reconocedor cerrado;
- runtime listo;
- runtime falla;
- detector devuelve cero regiones;
- detector devuelve una región;
- detector devuelve varias regiones;
- reconocimiento devuelve texto;
- reconocimiento devuelve vacío;
- una región falla;
- reconstrucción devuelve líneas;
- reconstrucción devuelve vacío y usa fallback;
- callback único;
- imagen cerrada en éxito;
- imagen cerrada en error;
- cierre durante inicialización;
- cierre durante procesamiento;
- segunda llamada después de cierre;
- dos solicitudes simultáneas;
- `recognizedAt` usa reloj inyectado.

---

## 55. Testabilidad de detector y reconocedor

Actualmente:

```text
PaddleTextDetector
PaddleTextRecognizer
```

son clases concretas.

Para probar el orquestador sin ONNX real existen opciones:

### Opción A — Interfaces pequeñas

```text
TextRegionDetector
TextRegionRecognizer
```

### Opción B — Adaptadores funcionales de paquete

### Opción C — Pruebas instrumentadas exclusivamente reales

Se recomienda evaluar una interfaz únicamente si existe una segunda implementación real o si la prueba del orquestador queda imposible.

No se deberán introducir interfaces solo por formalidad.

Alternativa pragmática:

- extraer un coordinador puro que reciba funciones;
- usar métodos package-private;
- complementar con prueba instrumentada real.

La decisión debe minimizar complejidad sin renunciar a probar callbacks y ownership.

---

## 56. Posible puerto interno

Si se justifica por testabilidad, se podrán crear en:

```text
data/document/onnx/pipeline/
```

contratos internos:

```java
interface TextRegionDetector {
    TextDetectionResult detect(Bitmap bitmap);
}

interface TextRegionRecognizer {
    TextRecognitionResult recognize(
            Bitmap bitmap,
            DetectedTextRegion region
    );
}
```

Pero esto solo se aceptará si:

- `PaddleTextDetector` y `PaddleTextRecognizer` los implementan directamente;
- no duplican métodos;
- no salen de infraestructura;
- permiten probar el pipeline;
- no se presentan como puertos de dominio.

No deben ubicarse en `core`.

---

## 57. Prueba de adaptación espacial

Se deberá comprobar:

```text
DetectedTextRegion
        ↓
RecognizedTextElement
```

Casos:

- región dentro de límites;
- coordenadas decimales;
- región tocando borde;
- región ligeramente fuera por redondeo;
- región degenerada;
- polígono inclinado;
- texto vacío;
- texto con espacios.

El elemento final deberá mantener límites válidos.

---

## 58. Prueba de orden

Con regiones:

```text
A sourceOrder = 0
B sourceOrder = 1
C sourceOrder = 2
```

y reconocimientos completados secuencialmente:

```text
A
B
C
```

El resultado no deberá cambiar de orden por:

- confianza;
- longitud del texto;
- tiempo de inferencia;
- contenido alfabético.

Después, `DocumentLineReconstructor` podrá reagrupar por coordenadas de forma determinista.

---

## 59. Prueba de documento vacío

### Sin regiones

```text
detector → []
pipeline → RecognizedDocument(lines = [])
callback → onSuccess
```

### Regiones con texto vacío

```text
detector → [r1, r2]
recognizer → "", ""
pipeline → RecognizedDocument(lines = [])
callback → onSuccess
```

El ViewModel deberá mostrar:

```text
NO_TEXT
```

No se deberá mostrar error técnico.

---

## 60. Prueba de cierre de imagen

Para cada ruta:

```text
success
no text
initialization error
detection error
recognition error
close
invalid image
```

se deberá verificar:

```text
documentImage.isClosed() == true
```

No debe existir una ruta que conserve el bitmap después del callback terminal.

---

## 61. Prueba de callback único

Un doble defectuoso podrá intentar:

```text
éxito
+
error posterior
```

El pipeline deberá entregar solo el primer resultado terminal permitido.

Se verificará:

```text
successCount + imageErrorCount + recognitionErrorCount == 1
```

---

## 62. Pruebas instrumentadas reales

Casos mínimos:

### Imagen sintética con una línea

```text
MR 1210
```

Debe producir:

- al menos una región;
- al menos un texto no vacío;
- documento no nulo;
- coordenadas dentro del bitmap.

### Imagen con varias líneas

```text
MR 1210
4 CAJAS
MZ 1300A
```

Debe producir:

- varias regiones o líneas;
- orden estable;
- texto no nulo;
- documento válido.

### Imagen en blanco

Debe producir:

```text
RecognizedDocument sin líneas
```

### Segunda ejecución

Procesar dos imágenes consecutivas sin recrear sesiones.

### Nueva instancia

Cerrar el adaptador y crear otro utilizando el mismo runtime.

---

## 63. Prueba con `AndroidDocumentImageProcessor`

La integración deberá probar el flujo real:

```text
URI o bitmap de prueba
        ↓
AndroidDocumentImageProcessor
        ↓
AndroidDocumentImage
        ↓
PaddleOcrDocumentTextRecognizer
        ↓
RecognizedDocument
```

Esto verificará:

- propiedad de imagen;
- dimensiones;
- rotación;
- cierre;
- compatibilidad entre adaptadores.

No basta con pasar directamente un bitmap artificial si se quiere cerrar HU-36.

---

## 64. Pruebas del ViewModel

`ReferenceListCaptureViewModelTest` deberá ejecutarse con el nuevo contrato o con dobles equivalentes.

Casos:

- texto reconocido;
- documento vacío;
- error de imagen;
- error de reconocimiento;
- callback tardío;
- resultado obsoleto;
- rotación;
- `onCleared()`;
- doble pulsación;
- conservación de imagen seleccionada.

No se deberán acoplar estas pruebas a clases ONNX.

---

## 65. Pruebas de regresión manual

Después de activar PP-OCRv5:

- abrir Procesar lista;
- tomar fotografía;
- seleccionar imagen;
- procesar captura;
- revisar texto bruto;
- continuar a revisión;
- cancelar;
- volver a procesar;
- rotar durante pantalla;
- salir mientras procesa;
- abrir otra vez;
- procesar sin Internet;
- verificar que escáner individual sigue funcionando;
- verificar historial;
- verificar CSV.

HU-36 no deberá modificar otros flujos.

---

## 66. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además:

- compilar `PaddleOcrDocumentTextRecognizer`;
- ejecutar pruebas del pipeline;
- ejecutar pruebas del mapper espacial;
- ejecutar pruebas de callback único;
- comprobar generación por ABI;
- comprobar que no se añade permiso de Internet;
- comprobar que modelos y diccionario permanecen incluidos;
- evitar corpus privado.

Las pruebas instrumentadas reales podrán seguir siendo locales si la CI no dispone de emulador.

No se deberá afirmar que la inferencia completa está cubierta únicamente con dobles.

---

## 67. Criterios de aceptación

### CA-01 — Implementación del contrato

**Dado** el pipeline PP-OCRv5,  
**cuando** se compila,  
**entonces** implementa `DocumentTextRecognizer` sin modificar el contrato existente.

### CA-02 — Imagen compatible

**Dado** un `AndroidDocumentImage` válido,  
**cuando** se reconoce,  
**entonces** utiliza su bitmap procesado.

### CA-03 — Inicialización perezosa

**Dado** un runtime no inicializado,  
**cuando** se solicita reconocimiento,  
**entonces** se inicializa fuera del hilo principal y continúa al quedar listo.

### CA-04 — Detección única

**Dada** una imagen,  
**cuando** se procesa,  
**entonces** el detector se ejecuta una sola vez.

### CA-05 — Reconocimiento por región

**Dadas** varias regiones,  
**cuando** se procesan,  
**entonces** el reconocedor se ejecuta una vez por región y en orden estable.

### CA-06 — Texto vacío aislado

**Dada** una región con texto vacío,  
**cuando** se procesa,  
**entonces** se omite sin invalidar todo el documento.

### CA-07 — Elementos espaciales

**Dado** un texto reconocido,  
**cuando** se adapta,  
**entonces** produce un `RecognizedTextElement` con coordenadas válidas.

### CA-08 — Líneas

**Dados** elementos reconocidos,  
**cuando** se reconstruyen,  
**entonces** producen `RecognizedTextLine` compatibles con el flujo actual.

### CA-09 — Documento

**Dada** una ejecución satisfactoria,  
**cuando** termina,  
**entonces** produce `RecognizedDocument` con sourceType, líneas y timestamp.

### CA-10 — Imagen sin texto

**Dada** una imagen válida sin texto,  
**cuando** se procesa,  
**entonces** produce éxito con documento vacío.

### CA-11 — Error de detector

**Dado** un fallo técnico de detección,  
**cuando** ocurre,  
**entonces** se entrega únicamente `onRecognitionError()`.

### CA-12 — Error de reconocedor

**Dado** un fallo técnico en una región,  
**cuando** ocurre,  
**entonces** se entrega únicamente `onRecognitionError()`.

### CA-13 — Callback único

**Dada** cualquier solicitud,  
**cuando** finaliza,  
**entonces** emite como máximo un callback terminal.

### CA-14 — Cierre de imagen

**Dada** cualquier ruta terminal,  
**cuando** concluye,  
**entonces** el `DocumentImage` queda cerrado.

### CA-15 — Cierre lógico

**Dado** un pipeline cerrado,  
**cuando** llega un resultado tardío,  
**entonces** se descarta sin callback.

### CA-16 — Runtime compartido

**Dado** el cierre del adaptador,  
**entonces** el runtime y sus sesiones permanecen disponibles para otra instancia.

### CA-17 — Integración con ViewModel

**Dado** `ReferenceListCaptureViewModel`,  
**cuando** recibe la implementación PP-OCRv5,  
**entonces** no necesita conocer clases ONNX.

### CA-18 — Offline

**Dado** un dispositivo sin conexión,  
**cuando** se procesa una imagen,  
**entonces** el pipeline funciona localmente.

### CA-19 — Revisión conservada

**Dado** un documento reconocido,  
**cuando** continúa el flujo,  
**entonces** el usuario todavía debe revisar y confirmar las referencias.

### CA-20 — Sin cambios de dominio

**Dada** HU-36,  
**entonces** no se modifica mercadería, stock, historial ni Room.

---

## 68. Riesgos

### Ownership incorrecto del reconocedor

**Riesgo:** cerrar un ViewModel inutiliza futuras pantallas.

**Mitigación:** adaptador por ViewModel y runtime compartido.

### Deadlock del executor

**Riesgo:** esperar otra tarea dentro del mismo executor de un hilo.

**Mitigación:** callbacks asíncronos sin espera bloqueante.

### Callback después de cerrar pantalla

**Riesgo:** actualización tardía de UI o fuga.

**Mitigación:** cierre lógico, solicitud interna y callback único.

### Imagen no cerrada

**Riesgo:** bitmap retenido y memoria acumulada.

**Mitigación:** finalización centralizada en todas las rutas.

### Documento parcial presentado como completo

**Riesgo:** una región falla y se oculta silenciosamente.

**Mitigación:** política estricta ante error técnico.

### Texto útil descartado por confianza

**Riesgo:** referencias difíciles desaparecen antes de revisión.

**Mitigación:** conservar texto no vacío y dejar decisión a HU-37.

### Reconstructor incompatible

**Riesgo:** regiones PP-OCRv5 producen líneas incorrectas.

**Mitigación:** fallback y pruebas; ajuste reservado para HU-37.

### Sesiones cerradas accidentalmente

**Riesgo:** segunda ejecución falla.

**Mitigación:** pipeline no posee runtime ni sesiones.

### Doble inferencia

**Riesgo:** memoria elevada o resultados cruzados.

**Mitigación:** una solicitud activa y executor serial.

### Acoplamiento a Android en core

**Riesgo:** añadir Bitmap a `DocumentImage`.

**Mitigación:** orquestador en `data.document`.

---

## 69. Definición de terminado

HU-36 estará terminada cuando:

- exista `PaddleOcrDocumentTextRecognizer`;
- implemente `DocumentTextRecognizer`;
- se ubique en una capa coherente con el acceso a `AndroidDocumentImage`;
- no se exponga Bitmap desde `core`;
- reutilice `ocrExecutor`;
- inicialice el runtime sin bloquear UI;
- ejecute el detector una sola vez;
- reconozca las regiones secuencialmente;
- conserve orden y coordenadas;
- descarte únicamente textos vacíos;
- convierta resultados a `RecognizedTextElement`;
- reutilice `DocumentLineReconstructor`;
- tenga fallback de líneas;
- construya `RecognizedDocument`;
- diferencie sin texto de error técnico;
- entregue un callback único;
- cierre siempre la imagen;
- ignore resultados tras `close()`;
- cierre de forma idempotente;
- no cierre runtime ni sesiones;
- resuelva correctamente la propiedad por ViewModel;
- pueda activarse desde `ReferenceListModule`;
- el ViewModel no conozca ONNX;
- funcione offline;
- no modifique Room;
- no modifique mercadería;
- no gestione stock;
- mantenga revisión manual;
- las pruebas unitarias sean satisfactorias;
- la prueba instrumentada del pipeline sea satisfactoria;
- las pruebas del ViewModel sean satisfactorias;
- la regresión manual sea satisfactoria;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 70. Resultado esperado

Al cerrar HU-36:

```text
DocumentImage
        ↓
PaddleOcrDocumentTextRecognizer
        ↓
PaddleTextDetector
        ↓
DetectedTextRegion
        ↓
PaddleTextRecognizer
        ↓
TextRecognitionResult
        ↓
RecognizedTextElement
        ↓
DocumentLineReconstructor
        ↓
RecognizedDocument
        ↓
ReferenceListCaptureViewModel
```

La aplicación dispondrá por primera vez de un pipeline PP-OCRv5 completo adaptado al contrato documental.

La siguiente historia implementará:

```text
HU-37 — Integrar PP-OCRv5 con la reconstrucción y revisión de listas
```
