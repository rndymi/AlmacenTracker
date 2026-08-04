# HU-35 — Reconocer el contenido de las regiones detectadas

> Tercera historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-35  
**Nombre:** Reconocer el contenido de las regiones detectadas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-35-reconocer-regiones-texto`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-33 — Preparar ONNX Runtime y los recursos PP-OCRv5  
- HU-34 — Detectar regiones de texto con PP-OCRv5  

**Issue prevista:** `#39`

---

## 2. Historia de usuario

Como usuario,  
quiero que la aplicación convierta las regiones de texto detectadas en contenido legible,  
para preparar el procesamiento completo de listas mediante PP-OCRv5.

---

## 3. Objetivo

Implementar la etapa de reconocimiento de texto utilizando:

```text
PP-OCRv5_mobile_rec
+
ONNX Runtime
+
diccionario PP-OCRv5
```

Flujo previsto:

```text
Bitmap procesado
        +
DetectedTextRegion
        ↓
recorte seguro de la región
        ↓
corrección geométrica cuando corresponda
        ↓
normalización para reconocimiento
        ↓
tensor FLOAT NCHW
        ↓
recognizerSession.run(...)
        ↓
salida [batch, timeSteps, classes]
        ↓
decodificación CTC
        ↓
texto + confianza
        ↓
TextRecognitionResult
```

HU-35 deberá transformar cada región detectada en un resultado textual independiente.

No deberá construir todavía un `RecognizedDocument` completo ni conectar PP-OCRv5 al flujo visible de captura.

---

## 4. Regla principal

> HU-35 reconoce qué texto contiene una región ya detectada. No reconstruye todavía el documento completo.

Resultado de HU-34:

```text
región 1
región 2
región 3
```

Resultado de HU-35:

```text
región 1 → "MR 1210"
región 2 → "4 CAJAS"
región 3 → "MZ 1300A"
```

Resultado que pertenece a HU-36:

```text
imagen completa
        ↓
detección
        ↓
reconocimiento de todas las regiones
        ↓
RecognizedDocument
```

No se deberá en HU-35:

- extraer referencias de mercadería;
- separar categoría y código;
- extraer cantidad o unidad;
- consultar Room;
- crear historial;
- modificar mercadería;
- reemplazar todavía el OCR visible.

---

## 5. Documentos y código de referencia

HU-35 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-33-preparar-onnx-ppocrv5.md`;
- `HU-34-detectar-regiones-texto-ppocrv5.md`;
- el estado real de `AlmacenTrackerHU34.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- el funcionamiento completamente offline;
- el ciclo de vida de sesiones preparado en HU-33;
- las regiones detectadas por HU-34;
- la separación entre detección, reconocimiento, reconstrucción y reglas de mercadería;
- la liberación explícita de bitmaps, tensores y resultados ONNX;
- la política de crear componentes únicamente cuando representen una responsabilidad real.

El plan de v1.4 asigna a HU-35:

```text
recorte
+
perspectiva
+
orientación
+
preprocesamiento del reconocedor
+
inferencia
+
diccionario
+
decodificación
+
confianza
+
pruebas de palabras y referencias
```

---

## 6. Estado real antes de HU-35

El análisis de `AlmacenTrackerHU34.zip` confirma:

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
merge HU34 #38 into develop
```

HU-34 ya proporciona:

```text
data/document/onnx/detection/
├── DetectorImageTransform.java
├── DetectorInput.java
├── PaddleTextDetector.java
├── PaddleTextDetectorConfiguration.java
├── PaddleTextDetectorPostProcessor.java
├── PaddleTextDetectorPreprocessor.java
├── TextDetectionException.java
└── TextProbabilityMapAnalyzer.java
```

Modelos disponibles:

```text
data/document/onnx/model/
├── DetectedTextPolygon.java
├── DetectedTextRegion.java
├── TextDetectionResult.java
└── TextPoint.java
```

El detector ya:

- reutiliza `PaddleOcrRuntimeProvider`;
- reutiliza la sesión detectora;
- prepara tensor `FLOAT`;
- ejecuta inferencia real;
- interpreta el mapa de probabilidad;
- devuelve regiones con polígono;
- devuelve confianza;
- conserva `sourceOrder`;
- restaura coordenadas al bitmap procesado;
- evita cerrar la sesión compartida;
- mide preprocesamiento, inferencia y posprocesamiento;
- funciona completamente offline.

`ReferenceListModule`, ubicado realmente en:

```text
app/di/ReferenceListModule.java
```

ya compone:

```text
PaddleTextDetector
```

pero continúa entregando al flujo visible:

```text
MlKitDocumentTextRecognizer
```

Por tanto, HU-35 deberá ampliar esa composición sin sustituir todavía el reconocedor visible.

---

## 7. Metadatos reales del reconocedor

El archivo:

```text
app/src/main/assets/ocr/ppocrv5/model_manifest.properties
```

declara:

```text
recognizer.input.name = x
recognizer.input.rank = 4
recognizer.input.fixed.height = 48

recognizer.output.name = fetch_name_0
recognizer.output.count = 1
recognizer.output.rank = 3

recognizer.class.count = 18385

dictionary.entry.count = 18383
dictionary.charset = UTF-8

recognizer.blank.token.count = 1
recognizer.additional.special.token.count = 1
recognizer.ctc.blank.index = 0
```

La relación documentada es:

```text
18385 clases
-
1 token blank
-
1 token especial adicional
=
18383 entradas del diccionario
```

HU-35 deberá respetar exactamente esta relación.

No deberá asumir silenciosamente qué representa el token especial adicional hasta verificar la exportación real del modelo.

---

## 8. Limitación actual del manifiesto Java

`PaddleOcrModelManifest` actualmente expone:

- nombre de entrada del reconocedor;
- rank de entrada;
- número de salidas;
- número de clases;
- número de tokens blank;
- número de tokens especiales adicionales;
- tamaño esperado del diccionario.

Sin embargo, todavía no expone todos los datos ya presentes en el archivo de propiedades:

```text
recognizer.input.fixed.height
recognizer.output.name
recognizer.output.rank
recognizer.ctc.blank.index
dictionary.entry.count
dictionary.charset
```

HU-35 deberá ampliar `PaddleOcrModelManifest` para que esos valores tengan una única fuente de verdad.

No se deberán duplicar como constantes dispersas dentro del reconocedor.

---

## 9. Alcance incluido

HU-35 incluye:

- ampliar el manifiesto Java del reconocedor;
- exponer nombre de salida;
- exponer rank de salida;
- exponer altura fija;
- exponer índice blank;
- exponer número declarado de entradas del diccionario;
- exponer charset declarado;
- validar esos metadatos;
- definir configuración funcional del reconocedor;
- recibir un bitmap y una región detectada;
- validar la región;
- limitar el polígono a la imagen;
- recortar la región;
- aplicar transformación geométrica cuando aporte valor;
- normalizar orientación horizontal;
- conservar proporción;
- redimensionar a altura fija `48`;
- calcular anchura de inferencia;
- aplicar padding horizontal;
- definir anchura máxima;
- evitar truncamiento silencioso;
- convertir píxeles a canales esperados;
- normalizar valores;
- construir tensor `FLOAT`;
- respetar formato NCHW;
- ejecutar la sesión reconocedora;
- obtener la salida `fetch_name_0`;
- validar tipo y forma de salida;
- interpretar `timeSteps`;
- interpretar `classCount`;
- validar `classCount = 18385`;
- aplicar decodificación CTC;
- ignorar el índice blank `0`;
- colapsar repeticiones consecutivas según CTC;
- mapear índices al diccionario;
- tratar el token especial adicional;
- calcular confianza textual;
- devolver texto y confianza;
- conservar la región de origen;
- conservar el orden de origen;
- aceptar texto vacío como resultado válido;
- tratar índices inválidos;
- cerrar tensor;
- cerrar resultado de sesión;
- reciclar únicamente bitmaps creados por el reconocedor;
- no reciclar el bitmap fuente;
- no cerrar la sesión compartida;
- medir tiempos internos;
- mantener ejecución fuera del hilo principal;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas con texto ficticio;
- CI.

---

## 10. Alcance excluido

HU-35 no incluye:

- ejecutar primero el detector desde el reconocedor;
- recorrer todas las regiones de una imagen;
- construir el pipeline completo;
- crear `PaddleOcrDocumentTextRecognizer`;
- crear `RecognizedDocument`;
- crear `RecognizedTextLine`;
- crear `RecognizedTextElement`;
- reconstruir filas;
- detectar una o dos columnas;
- agrupar palabras;
- asociar cantidad y unidad;
- extraer referencias;
- normalizar códigos;
- corregir `O ↔ 0`;
- comparar con Room;
- conectar con `ReferenceListCaptureViewModel`;
- modificar estados de la pantalla;
- mostrar texto PP-OCRv5 al usuario;
- sustituir el motor actual;
- comparar precisión global entre motores;
- optimizar paralelismo;
- usar GPU;
- usar NNAPI;
- modificar Room;
- modificar historial;
- modificar mercadería;
- retirar dependencias existentes.

El pipeline completo pertenece a HU-36.

La reconstrucción e integración funcional pertenecen a HU-37.

---

## 11. Modelo de resultado textual

Se añadirá:

```text
TextRecognitionResult
```

Ubicación recomendada:

```text
data/document/onnx/model/
```

Datos previstos:

```text
text
confidence
sourceRegion
preprocessDurationMs
inferenceDurationMs
decodeDurationMs
```

Reglas:

- `text` no nulo;
- texto vacío permitido;
- confianza finita;
- confianza entre `0` y `1`;
- región no nula;
- métricas no negativas;
- sin Bitmap;
- sin tensor;
- sin `OrtSession.Result`;
- sin dependencia de Activity;
- colección de caracteres internos no expuesta salvo necesidad real.

Las métricas podrán mantenerse en un objeto interno si no aportan valor al resultado público.

No se deberá inflar el modelo por simetría con `TextDetectionResult`.

---

## 12. Relación con `DetectedTextRegion`

Cada reconocimiento deberá conservar:

```text
DetectedTextRegion sourceRegion
```

Motivos:

- HU-36 necesitará reconstruir el documento;
- el texto deberá mantener coordenadas;
- deberá conservarse `sourceOrder`;
- permitirá ordenar y agrupar;
- facilitará pruebas.

No se deberá copiar manualmente:

```text
left
top
right
bottom
confidence
sourceOrder
```

en otro modelo si puede conservarse la región inmutable.

---

## 13. Configuración funcional del reconocedor

Se recomienda crear:

```text
PaddleTextRecognizerConfiguration
```

Datos previstos:

```text
inputName
outputName
fixedHeight
maximumWidth
widthMultiple
minimumWidth
blankIndex
classCount
recognitionThreshold
```

También podrá incluir:

```text
channelOrder
mean
std
paddingValue
```

Los valores deberán centralizarse.

Valores confirmados por manifiesto:

```text
inputName = x
outputName = fetch_name_0
fixedHeight = 48
blankIndex = 0
classCount = 18385
```

Los valores no confirmados deberán validarse mediante pruebas reales.

---

## 14. Anchura de reconocimiento

La entrada del reconocedor utiliza altura fija:

```text
48
```

La anchura deberá calcularse manteniendo proporción:

```text
targetWidth =
    round(sourceWidth * 48 / sourceHeight)
```

Después deberá:

- respetar un ancho mínimo;
- ajustarse a un múltiplo cuando el modelo lo requiera;
- limitarse a un ancho máximo;
- aplicar padding.

No se deberá deformar cada región a un ancho fijo sin conservar proporción.

---

## 15. Anchura dinámica y forma real

HU-35 deberá inspeccionar la forma real de entrada de:

```text
recognizerSession.getInputInfo()
```

Casos posibles:

```text
[1, 3, 48, dynamic]
```

o una anchura fija exportada.

La implementación deberá adaptarse a la forma real.

No se deberá asumir anchura dinámica solo porque el manifiesto no declara una anchura fija.

La prueba instrumentada deberá registrar y validar la dimensión concreta.

---

## 16. Política para regiones demasiado anchas

Una región podrá superar el máximo aceptado.

Opciones:

### Opción A — Reducir hasta el máximo

Adecuada cuando la relación continúa siendo legible.

### Opción B — Dividir la región

Más compleja y con riesgo de cortar caracteres.

### Opción C — Marcar como no reconocible

Evita producir texto engañoso.

Para HU-35 se recomienda:

```text
reducir hasta maximumWidth
+
conservar proporción
+
no truncar
```

Si la altura resultante o la legibilidad se degradan demasiado, deberá devolverse un error controlado o baja confianza.

No se recortará la parte derecha silenciosamente.

---

## 17. Recorte rectangular inicial

`DetectedTextPolygon` conserva cuatro puntos, pero la implementación actual de HU-34 genera principalmente regiones rectangulares.

HU-35 deberá comenzar con un recorte basado en:

```text
left
top
right
bottom
```

Reglas:

- usar `floor` para origen;
- usar `ceil` para límite;
- limitar a bitmap;
- ancho y alto positivos;
- descartar regiones degeneradas;
- no reciclar bitmap fuente;
- crear una copia propia.

Esto permite validar primero la inferencia real del reconocedor.

---

## 18. Transformación de perspectiva

Se deberá evaluar si el polígono contiene inclinación real.

Cuando los cuatro puntos representen una caja no rectangular, podrá utilizarse:

```text
Matrix.setPolyToPoly(...)
```

o una transformación equivalente.

La transformación deberá:

- ordenar correctamente los cuatro puntos;
- calcular ancho superior e inferior;
- calcular alto izquierdo y derecho;
- generar un bitmap horizontal;
- limitar dimensiones;
- evitar deformaciones extremas;
- fallar de forma controlada.

No se implementará una transformación geométrica compleja si HU-34 solo entrega rectángulos.

La clase podrá prepararse para evolución sin añadir código muerto.

---

## 19. Orientación de la región

El reconocedor espera texto horizontal.

HU-35 deberá detectar regiones predominantemente verticales:

```text
height > width * threshold
```

En ese caso podrá rotar:

```text
90 grados
```

antes de inferencia.

La decisión deberá probarse con texto vertical ficticio.

No se deberá rotar automáticamente toda región alta, porque podría representar varias líneas unidas por una detección imperfecta.

Configuración orientativa:

```text
verticalAspectThreshold
```

---

## 20. Preprocesador del reconocedor

Se añadirá:

```text
PaddleTextRecognizerPreprocessor
```

Responsabilidades:

- recibir bitmap y región;
- validar bitmap;
- validar región;
- recortar;
- corregir perspectiva cuando corresponda;
- corregir orientación;
- calcular tamaño destino;
- redimensionar a altura fija;
- aplicar padding;
- leer píxeles;
- construir tensor NCHW;
- devolver tensor y metadatos;
- cerrar bitmaps temporales;
- no ejecutar inferencia;
- no decodificar.

Resultado interno:

```text
RecognizerInput
├── OnnxTensor
├── sourceRegion
├── contentWidth
├── paddedWidth
└── fixedHeight
```

`RecognizerInput` deberá implementar:

```text
AutoCloseable
```

---

## 21. Propiedad de bitmaps

El preprocesador deberá distinguir:

### Bitmap fuente

Propiedad de `DocumentImage`.

No se recicla.

### Bitmap recortado

Creado por el reconocedor.

Debe reciclarse.

### Bitmap rotado

Creado por el reconocedor.

Debe reciclarse.

### Bitmap redimensionado

Creado por el reconocedor.

Debe reciclarse.

### Bitmap con padding

Creado por el reconocedor.

Debe reciclarse.

No se deberá reciclar dos veces la misma instancia cuando Android reutilice un bitmap en una operación.

---

## 22. Formato del tensor

Forma prevista:

```text
[1, 3, 48, width]
```

Tipo:

```text
FLOAT
```

Orden:

```text
NCHW
```

El preprocesador deberá confirmar:

- batch `1`;
- canales `3`;
- altura `48`;
- anchura válida;
- tamaño de buffer exacto.

No se deberá construir:

```text
[1, 48, width, 3]
```

salvo que los metadatos reales indiquen NHWC.

---

## 23. Conversión de píxeles

El bitmap utiliza ARGB.

El tensor deberá utilizar únicamente:

```text
R
G
B
```

El canal alfa se ignorará.

Se recomienda:

```text
Bitmap.getPixels(...)
```

para obtener un array lineal.

El buffer NCHW deberá escribirse como:

```text
plano R
plano G
plano B
```

No se deberá intercalar:

```text
RGB RGB RGB
```

si el modelo espera NCHW.

---

## 24. Normalización

La normalización deberá utilizar los valores esperados por PP-OCRv5_mobile_rec.

Proceso conceptual:

```text
normalized =
    (channel / 255.0 - mean) / std
```

Los valores deberán confirmarse con la configuración oficial del modelo o con una prueba reproducible.

No se copiarán automáticamente los valores del detector.

Detector y reconocedor pueden compartir valores, pero esa coincidencia deberá verificarse.

---

## 25. Padding

La región redimensionada deberá alinearse al inicio del tensor.

El espacio restante se rellenará con un valor definido.

Opciones:

```text
0 normalizado
blanco
valor de borde
```

La elección deberá corresponder al preprocesamiento esperado por el modelo.

No se utilizará un color arbitrario.

El padding no deberá formar parte del texto devuelto; solo afecta a la entrada.

---

## 26. RecognizerInput

Se añadirá:

```text
RecognizerInput
```

Responsabilidades:

- conservar el `OnnxTensor`;
- conservar la región;
- conservar dimensiones;
- conocer estado cerrado;
- cerrar tensor;
- cierre idempotente;
- impedir acceso después de cierre.

No deberá conservar bitmaps temporales una vez creado el tensor.

---

## 27. Recognizer

Se añadirá:

```text
PaddleTextRecognizer
```

Responsabilidades:

- recibir bitmap y región;
- obtener el bundle listo;
- reutilizar sesión reconocedora;
- obtener diccionario;
- preprocesar;
- ejecutar inferencia;
- validar salida;
- decodificar;
- devolver `TextRecognitionResult`;
- cerrar input;
- cerrar resultado;
- no cerrar sesión;
- no ejecutar detector;
- no construir documento;
- no aplicar parser de mercadería.

Firma orientativa:

```java
TextRecognitionResult recognize(
        Bitmap bitmap,
        DetectedTextRegion region
) throws TextRecognitionException;
```

La operación podrá ser síncrona porque el llamador la ejecutará en el executor OCR.

---

## 28. Obtención del runtime

HU-35 deberá reutilizar:

```text
PaddleOcrRuntimeProvider
```

Flujo:

```text
provider.getReadySessionBundle()
        ↓
getRecognizerSession()
        ↓
getDictionary()
```

No se deberá crear:

```text
OrtEnvironment
OrtSession
PaddleOcrDictionary
```

por cada región.

No se volverá a leer el diccionario desde assets durante cada reconocimiento.

---

## 29. Bloqueo de inferencia

`PaddleTextDetector` ya utiliza un bloqueo interno para serializar su inferencia.

`PaddleTextRecognizer` deberá definir una estrategia equivalente.

Para HU-35 se recomienda:

```text
synchronized inferenceLock
```

alrededor de:

```text
session.run(...)
```

Motivos:

- comportamiento reproducible;
- evitar concurrencia no validada;
- reducir presión de memoria;
- preparar el pipeline secuencial de HU-36.

HU-39 podrá evaluar paralelismo posteriormente.

---

## 30. Ejecución ONNX

Flujo:

```text
Map.of(inputName, tensor)
        ↓
recognizerSession.run(...)
        ↓
OrtSession.Result
```

La implementación deberá buscar la salida por nombre:

```text
fetch_name_0
```

No deberá tomar ciegamente:

```text
result.get(0)
```

sin validar.

Deberá comprobar:

- salida presente;
- valor tipo tensor;
- tipo `FLOAT`;
- rank `3`;
- batch `1`;
- `timeSteps > 0`;
- `classCount = 18385`;
- todos los valores finitos.

---

## 31. Forma de salida

La forma esperada es conceptualmente:

```text
[1, timeSteps, 18385]
```

La implementación deberá verificar la forma real.

No se deberá asumir:

```text
[1, 18385, timeSteps]
```

sin inspeccionar metadatos e inferencia.

La prueba instrumentada deberá confirmar cuál dimensión representa clases.

El decodificador deberá recibir una representación explícita:

```text
float[][] timeStepScores
```

o una vista lineal con dimensiones claras.

---

## 32. Decodificación CTC

La decodificación greedy inicial será:

1. para cada `timeStep`, obtener el índice con mayor puntuación;
2. calcular confianza de ese índice;
3. ignorar el índice blank;
4. colapsar repeticiones consecutivas;
5. mapear índices visibles al diccionario;
6. concatenar tokens;
7. calcular confianza global.

Ejemplo conceptual:

```text
0, 25, 25, 0, 80, 80, 0
        ↓
25, 80
        ↓
"MR"
```

La repetición deberá reiniciarse cuando aparezca blank.

---

## 33. Índice blank

El manifiesto declara:

```text
recognizer.ctc.blank.index = 0
```

HU-35 deberá utilizar ese valor.

No se codificará:

```java
private static final int BLANK = 0;
```

como única fuente de verdad si ya existe en el manifiesto.

La configuración funcional podrá copiarlo desde el manifiesto validado.

---

## 34. Mapeo de clases al diccionario

El diccionario tiene:

```text
18383 entradas
```

El modelo tiene:

```text
18385 clases
```

Existe:

```text
1 blank
+
1 token especial adicional
```

HU-35 deberá definir y probar la función exacta:

```text
classIndex
        ↓
dictionaryIndex o specialToken
```

No se deberá asumir automáticamente:

```text
dictionaryIndex = classIndex - 1
```

para todas las clases sin tratar la clase especial adicional.

La implementación deberá:

- identificar dónde está el token especial;
- documentar su significado;
- decidir si se añade al texto;
- decidir si representa espacio u otro marcador;
- rechazar índices imposibles.

---

## 35. Token especial adicional

El manifiesto declara:

```text
recognizer.additional.special.token.count = 1
```

pero no declara todavía su semántica.

HU-35 deberá verificar:

- índice exacto;
- token que representa;
- si participa en el texto;
- si equivale a espacio;
- si debe ignorarse;
- si se añade antes o después del diccionario.

Hasta verificarlo, no se deberá inventar una representación.

Si el modelo exportado no permite justificarlo mediante metadatos o documentación del recurso, deberá quedar explícitamente documentado y cubierto por pruebas de inferencia conocidas.

---

## 36. Confianza por time step

La salida puede representar:

- probabilidades; o
- logits.

HU-35 deberá comprobarlo.

Si son probabilidades:

```text
confidence = maxValue
```

Si son logits:

```text
softmax por timeStep
```

No se deberá usar un logit bruto como confianza entre `0` y `1`.

La detección de formato deberá basarse en:

- rango de valores;
- documentación del modelo;
- pruebas conocidas.

La implementación no deberá aplicar softmax dos veces.

---

## 37. Softmax estable

Cuando sea necesario aplicar softmax:

```text
maxLogit = max(scores)
sum = Σ exp(score - maxLogit)
probability = exp(selected - maxLogit) / sum
```

Reglas:

- evitar overflow;
- evitar división por cero;
- rechazar `NaN`;
- rechazar infinito;
- no crear arrays adicionales por time step si puede evitarse;
- probar valores extremos.

---

## 38. Confianza global

La confianza textual deberá calcularse sobre los tokens visibles aceptados.

Recomendación inicial:

```text
media aritmética
de las probabilidades
de los tokens emitidos
```

Casos:

```text
texto no vacío
    → media de tokens

texto vacío
    → 0.0
```

No se incluirán pasos blank en la media.

No se incluirán repeticiones colapsadas varias veces.

La estrategia deberá centralizarse y probarse.

---

## 39. Umbral de reconocimiento

Se podrá definir:

```text
recognitionThreshold
```

pero HU-35 no deberá descartar silenciosamente todo texto por debajo del umbral.

Resultado recomendado:

```text
text
confidence
isLowConfidence
```

o permitir que el pipeline futuro decida.

Para mantener el modelo pequeño, HU-35 podrá devolver siempre texto y confianza.

HU-36 o HU-37 decidirán cómo tratar baja confianza.

---

## 40. Texto vacío

Un resultado vacío es válido cuando:

- la región era ruido;
- el recorte era incorrecto;
- el reconocedor emitió blanks;
- no se superó ninguna clase visible;
- la región contenía un símbolo no representable.

HU-35 deberá devolver:

```text
text = ""
confidence = 0.0
```

No deberá lanzar excepción únicamente por texto vacío.

---

## 41. Texto y normalización

El reconocedor deberá conservar el texto decodificado.

No deberá aplicar:

- `trim()` agresivo si elimina espacios significativos;
- mayúsculas automáticas;
- reemplazo de `O` por `0`;
- reemplazo de `I` por `1`;
- eliminación de símbolos;
- parser de referencias;
- normalización de unidades.

La normalización funcional pertenece a capas posteriores.

Sí podrá eliminar tokens técnicos que no forman parte del contenido.

---

## 42. Excepción de reconocimiento

Se añadirá:

```text
TextRecognitionException
```

Causas orientativas:

```text
INVALID_IMAGE
INVALID_REGION
REGION_OUT_OF_BOUNDS
REGION_DEGENERATE
RUNTIME_NOT_READY
SESSION_CLOSED
INPUT_SHAPE_INCOMPATIBLE
OUTPUT_NOT_FOUND
OUTPUT_TYPE_INCOMPATIBLE
OUTPUT_SHAPE_INCOMPATIBLE
CLASS_COUNT_INCOMPATIBLE
DICTIONARY_INCOMPATIBLE
TOKEN_INDEX_INVALID
OUTPUT_VALUE_INVALID
MEMORY_ERROR
INFERENCE_ERROR
DECODING_ERROR
```

No se expondrá `OrtException` fuera de infraestructura.

La causa técnica podrá conservarse para tests y logs.

---

## 43. Decoder separado

Se recomienda crear:

```text
PaddleCtcTextDecoder
```

Responsabilidades:

- recibir salida por time steps;
- conocer class count;
- conocer blank index;
- conocer mapeo de tokens;
- aplicar greedy decode;
- colapsar repeticiones;
- calcular confianza;
- devolver texto y confianza;
- no conocer Bitmap;
- no conocer ONNX Runtime;
- no conocer referencias de mercadería.

Esto permite pruebas JVM exhaustivas sin ejecutar el modelo.

---

## 44. Resultado interno del decoder

Se podrá crear:

```text
DecodedText
```

con:

```text
text
confidence
emittedTokenCount
timeStepCount
```

No será necesario exponerlo fuera del paquete si `TextRecognitionResult` ya cubre la necesidad pública.

No se deberán crear dos modelos públicos equivalentes.

---

## 45. Mapeador de tokens

Se recomienda encapsular la relación entre clase y diccionario:

```text
PaddleOcrTokenMapper
```

Responsabilidades:

- conocer blank;
- conocer token especial;
- validar class count;
- mapear índice;
- indicar token ignorado;
- indicar token visible;
- no aplicar CTC;
- no aplicar normalización.

Esta clase se justifica porque la relación:

```text
18385 ↔ 18383 + 2
```

no debe quedar dispersa.

---

## 46. Arquitectura propuesta

Estructura orientativa:

```text
data/document/onnx/
├── recognition/
│   ├── PaddleTextRecognizer.java
│   ├── PaddleTextRecognizerConfiguration.java
│   ├── PaddleTextRecognizerPreprocessor.java
│   ├── RecognizerInput.java
│   ├── PaddleCtcTextDecoder.java
│   ├── PaddleOcrTokenMapper.java
│   └── TextRecognitionException.java
└── model/
    └── TextRecognitionResult.java
```

Las clases existentes de detección permanecerán en:

```text
data/document/onnx/detection/
```

No se deberá mezclar el preprocesamiento del reconocedor dentro de:

```text
PaddleTextDetector
```

---

## 47. Composición en `ReferenceListModule`

El módulo real está ubicado en:

```text
app/di/ReferenceListModule.java
```

HU-35 deberá componer:

```text
PaddleTextRecognizerConfiguration
PaddleTextRecognizerPreprocessor
PaddleCtcTextDecoder
PaddleTextRecognizer
```

utilizando:

```text
PaddleOcrRuntimeProvider
```

Podrá exponer:

```java
providePaddleTextRecognizer()
```

para pruebas y para HU-36.

No deberá todavía sustituir:

```java
new MlKitDocumentTextRecognizer(...)
```

dentro de:

```java
provideReferenceListCaptureViewModelFactory()
```

---

## 48. Relación con el manifiesto

`PaddleOcrModelManifest` deberá ampliarse de forma compatible.

Campos nuevos recomendados:

```text
recognizerFixedHeight
recognizerOutputName
recognizerOutputRank
recognizerCtcBlankIndex
dictionaryEntryCount
dictionaryCharset
```

Validaciones:

- altura mayor que cero;
- output name no vacío;
- output rank `3`;
- blank index no negativo;
- blank index menor que class count;
- dictionary entry count positivo;
- charset compatible con UTF-8;
- tamaño cargado igual al declarado;
- fórmula de clases coherente.

Las pruebas existentes de HU-33 deberán actualizarse, no eliminarse.

---

## 49. Validación del diccionario cargado

`PaddleOcrDictionary` ya:

- carga UTF-8;
- rechaza entrada vacía;
- conserva orden;
- expone `size()`;
- expone `get(index)`;
- devuelve lista inmutable.

HU-35 deberá reutilizarlo.

No se creará otro lector de diccionario.

Validación requerida:

```text
dictionary.size()
==
manifest.dictionary.entry.count
==
manifest.expectedDictionarySize()
```

---

## 50. Salida por nombre

El reconocedor deberá usar:

```text
recognizer.output.name = fetch_name_0
```

La implementación deberá:

- buscar el valor por nombre;
- informar `OUTPUT_NOT_FOUND` si falta;
- no depender del orden del mapa;
- validar que solo exista la salida necesaria o ignorar otras salidas documentadas.

No se deberá suponer que `output.count = 1` elimina la necesidad de validar el nombre.

---

## 51. Recursos y cierre

En cada reconocimiento deberán cerrarse:

```text
RecognizerInput
OrtSession.Result
bitmaps temporales
```

No deberán cerrarse:

```text
OrtEnvironment
recognizerSession
dictionary
PaddleOcrSessionBundle
```

porque pertenecen al runtime compartido.

El cierre deberá ocurrir también ante:

- salida ausente;
- forma inválida;
- clase inválida;
- error de decodificación;
- excepción de memoria;
- bitmap inválido.

---

## 52. Memoria

El reconocedor deberá evitar mantener simultáneamente:

- bitmap fuente completo;
- recorte;
- bitmap transformado;
- bitmap redimensionado;
- bitmap con padding;
- array de píxeles;
- `FloatBuffer`;
- tensor;
- salida completa;
- copia multidimensional de logits.

Estrategia recomendada:

1. recortar;
2. transformar;
3. redimensionar;
4. extraer píxeles;
5. reciclar bitmaps temporales;
6. crear tensor;
7. ejecutar;
8. decodificar por vista lineal cuando sea posible;
9. cerrar salida;
10. devolver solo texto y confianza.

---

## 53. Evitar copia completa de la salida

La salida puede ser grande:

```text
timeSteps * 18385
```

HU-35 deberá evitar copias redundantes.

Se deberá evaluar la API real de ONNX Runtime para:

- acceder al `FloatBuffer`;
- obtener el array una sola vez;
- iterar por time step;
- no duplicar en `float[][][]` y después `float[][]`.

No se optimizará a ciegas, pero se evitará una copia evidentemente innecesaria.

---

## 54. Concurrencia

Para HU-35:

```text
una inferencia de reconocimiento activa
por PaddleTextRecognizer
```

El pipeline futuro reconocerá regiones inicialmente de forma secuencial.

Motivos:

- menor pico de memoria;
- sesión compartida;
- comportamiento reproducible;
- corpus pequeño, normalmente unas quince referencias;
- optimización posterior en HU-39.

No se creará un thread pool por región.

---

## 55. Cancelación lógica

El reconocedor será síncrono y no conocerá ids de solicitud.

HU-36 gestionará:

```text
requestId
resultado obsoleto
cancelación lógica
```

Si una inferencia ya comenzó, podrá terminar, pero su resultado podrá descartarse.

No se deberá intentar interrumpir la sesión de forma insegura.

---

## 56. Métricas internas

Se medirán:

```text
cropAndPreprocess
inference
decode
total
```

mediante:

```text
System.nanoTime()
```

Las métricas:

- no se mostrarán al usuario;
- no se guardarán en Room;
- no contendrán texto;
- servirán para HU-38 y HU-39;
- deberán poder desactivarse o reducir logs en producción.

---

## 57. Logs

En desarrollo podrán registrarse:

```text
dimensión de región
anchura de entrada
timeSteps
classCount
duración
confianza
longitud del texto
```

No deberán registrarse por defecto:

- texto completo reconocido;
- referencias reales;
- imagen;
- logits;
- diccionario completo;
- contenido documental privado.

---

## 58. Pruebas unitarias del token mapper

Casos:

- blank;
- primer token visible;
- último token visible;
- token especial;
- índice negativo;
- índice igual a class count;
- diccionario incompatible;
- blank fuera de rango;
- clase no mapeable;
- tamaño correcto.

---

## 59. Pruebas unitarias CTC

Casos:

```text
blank
repetición
blank entre repeticiones
dos caracteres
texto completo
solo blanks
token especial
índice inválido
```

Ejemplos conceptuales:

```text
[0, 5, 5, 0]
→ token 5 una vez
```

```text
[5, 0, 5]
→ token 5 dos veces
```

```text
[0, 0, 0]
→ ""
```

También:

- confianza media;
- texto vacío con confianza cero;
- logits extremos;
- softmax estable;
- `NaN`;
- infinito;
- class count incorrecto.

---

## 60. Pruebas unitarias de dimensiones

Casos:

- salida `[1, T, 18385]`;
- batch distinto de uno;
- rank distinto de tres;
- `T = 0`;
- class count menor;
- class count mayor;
- longitud lineal incorrecta;
- salida nula;
- salida tipo no FLOAT.

---

## 61. Pruebas instrumentadas del preprocesador

Casos:

- región rectangular;
- región en borde;
- región pequeña;
- región horizontal;
- región vertical;
- región fuera de límites;
- bitmap reciclado;
- altura final `48`;
- tensor rank `4`;
- tensor NCHW;
- anchura válida;
- padding;
- cierre idempotente.

---

## 62. Prueba instrumentada de inferencia real

Se deberá utilizar el modelo real incluido.

Imagen sintética orientativa:

```text
MR 1210
```

Flujo:

1. crear bitmap ficticio;
2. crear una región que cubra el texto;
3. ejecutar reconocedor;
4. verificar que no hay crash;
5. verificar texto no nulo;
6. verificar confianza finita;
7. verificar salida dentro de rango;
8. verificar sesión abierta;
9. ejecutar una segunda vez;
10. cerrar runtime al final.

La prueba no deberá exigir inicialmente una coincidencia exacta si el renderizado de fuente del emulador no es estable.

Sin embargo, deberá existir al menos una prueba controlada con expectativa textual suficientemente estable antes de cerrar HU-35.

---

## 63. Pruebas con referencias ficticias

Corpus mínimo:

```text
MR 1210
MR1210A
MZ 001300
4 CAJAS
20 PCS
```

Se deberá comprobar:

- letras;
- números;
- espacios;
- ceros iniciales;
- letra final;
- cantidad;
- unidad.

HU-35 solo comprobará el texto.

No comprobará todavía el parser de referencia.

---

## 64. Prueba de región detectada real

Además del recorte manual, se deberá probar:

```text
PaddleTextDetector
        ↓
DetectedTextRegion
        ↓
PaddleTextRecognizer
```

sobre una imagen sintética.

Esto demostrará compatibilidad entre:

- coordenadas de HU-34;
- recorte de HU-35;
- sesión reconocedora;
- diccionario.

No constituye todavía el pipeline público de HU-36.

---

## 65. Pruebas de regresión

HU-35 deberá comprobar que continúan operativos:

- flujo actual con ML Kit;
- captura de imagen;
- selección desde fotos;
- revisión de referencias;
- ubicaciones;
- historial;
- búsqueda histórica;
- filtros;
- eliminación histórica;
- CSV;
- escaneo individual;
- funcionamiento offline.

PP-OCRv5 todavía no deberá cambiar el resultado visible.

---

## 66. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además deberá comprobar:

- compilación del reconocedor;
- pruebas del decoder;
- pruebas del token mapper;
- compatibilidad del manifiesto;
- diccionario presente;
- generación de APK por ABI;
- ausencia de recursos duplicados;
- ausencia de corpus privado.

Las pruebas de inferencia real podrán ejecutarse localmente si la CI no dispone de emulador.

No se deberá afirmar cobertura de inferencia real basándose únicamente en mocks.

---

## 67. Criterios de aceptación

### CA-01 — Sesión reutilizada

**Dado** el runtime preparado,  
**cuando** se reconoce una región,  
**entonces** se reutiliza la sesión reconocedora existente.

### CA-02 — Diccionario reutilizado

**Dado** el bundle listo,  
**cuando** se reconoce texto,  
**entonces** se utiliza el diccionario ya cargado y no se vuelve a leer el asset.

### CA-03 — Región válida

**Dado** un bitmap y una región válida,  
**cuando** se preprocesa,  
**entonces** se genera un recorte dentro de límites.

### CA-04 — Altura fija

**Dada** cualquier región reconocible,  
**cuando** se prepara,  
**entonces** el tensor utiliza altura `48`.

### CA-05 — Proporción

**Dada** una región rectangular,  
**cuando** se redimensiona,  
**entonces** conserva su proporción y no se trunca.

### CA-06 — Tensor

**Dada** una entrada válida,  
**cuando** se prepara,  
**entonces** produce un tensor `FLOAT` de rank `4` compatible.

### CA-07 — Inferencia real

**Dado** el modelo reconocedor,  
**cuando** se ejecuta,  
**entonces** devuelve la salida `fetch_name_0`.

### CA-08 — Forma de salida

**Dada** la salida,  
**cuando** se valida,  
**entonces** tiene batch `1`, rank `3` y `18385` clases.

### CA-09 — Blank

**Dado** el índice `0`,  
**cuando** se decodifica,  
**entonces** no se incorpora al texto.

### CA-10 — Repeticiones CTC

**Dados** índices repetidos consecutivos,  
**cuando** se decodifican,  
**entonces** producen un único token hasta que aparezca blank.

### CA-11 — Diccionario

**Dada** una clase visible,  
**cuando** se mapea,  
**entonces** utiliza la entrada correcta del diccionario.

### CA-12 — Token especial

**Dada** la clase especial adicional,  
**cuando** se procesa,  
**entonces** su comportamiento está documentado y probado.

### CA-13 — Confianza

**Dado** un texto emitido,  
**cuando** se calcula su confianza,  
**entonces** el valor es finito y se encuentra entre `0` y `1`.

### CA-14 — Texto vacío

**Dada** una región sin contenido reconocible,  
**cuando** se procesa,  
**entonces** devuelve texto vacío sin excepción.

### CA-15 — Recursos cerrados

**Dada** una operación finalizada o fallida,  
**entonces** tensor, resultado y bitmaps temporales se liberan.

### CA-16 — Sesión conservada

**Dada** una operación finalizada,  
**entonces** la sesión compartida continúa abierta.

### CA-17 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se reconoce una región,  
**entonces** la operación funciona localmente.

### CA-18 — Sin integración visible

**Dada** HU-35,  
**cuando** el usuario procesa una lista,  
**entonces** el flujo visible continúa usando el comportamiento anterior.

---

## 68. Riesgos

### Mapeo de clases incorrecto

**Riesgo:** el modelo ejecuta, pero devuelve caracteres desplazados.

**Mitigación:** token mapper explícito, manifiesto y pruebas de textos conocidos.

### Token especial no identificado

**Riesgo:** espacios o caracteres se pierden o se añaden incorrectamente.

**Mitigación:** verificar la exportación real antes de cerrar HU-35.

### Salida interpretada como probabilidades cuando son logits

**Riesgo:** confianza inválida.

**Mitigación:** comprobar rango y aplicar softmax solo cuando corresponda.

### Orden de dimensiones incorrecto

**Riesgo:** se interpreta `timeSteps` como clases.

**Mitigación:** inspección real y prueba instrumentada.

### Recorte incorrecto

**Riesgo:** se reconoce texto incompleto.

**Mitigación:** pruebas de bordes y compatibilidad con regiones de HU-34.

### Deformación por ancho fijo

**Riesgo:** caracteres ilegibles.

**Mitigación:** mantener proporción y padding.

### Región demasiado ancha

**Riesgo:** pérdida de texto o consumo excesivo.

**Mitigación:** política explícita sin truncamiento.

### Pico de memoria

**Riesgo:** salida de `18385` clases por time step y copias múltiples.

**Mitigación:** decodificación eficiente y liberación por etapas.

### Paralelismo prematuro

**Riesgo:** presión de memoria y resultados inestables.

**Mitigación:** reconocimiento secuencial en HU-35.

### Acoplamiento con reglas de mercadería

**Riesgo:** el reconocedor empieza a corregir códigos.

**Mitigación:** devolver texto bruto; parser en historias posteriores.

---

## 69. Definición de terminado

HU-35 estará terminada cuando:

- el manifiesto Java exponga los metadatos reales del reconocedor;
- altura fija, output name, output rank y blank index estén validados;
- el tamaño declarado del diccionario se compruebe;
- exista configuración funcional del reconocedor;
- exista preprocesador;
- las regiones se recorten de forma segura;
- la proporción se conserve;
- la orientación se trate de forma controlada;
- la entrada utilice altura `48`;
- el tensor FLOAT NCHW se construya correctamente;
- la sesión reconocedora se reutilice;
- la inferencia real funcione;
- la salida por nombre se obtenga;
- rank, batch, time steps y clases se validen;
- exista decoder CTC;
- blank se ignore;
- repeticiones se colapsen correctamente;
- el diccionario se mapee correctamente;
- el token especial esté identificado y probado;
- la confianza se calcule correctamente;
- texto vacío sea válido;
- `TextRecognitionResult` conserve la región;
- tensores y resultados se cierren;
- bitmaps temporales se reciclen;
- la sesión permanezca abierta;
- no se ejecute en el hilo principal;
- funcione offline;
- el flujo visible anterior permanezca intacto;
- las pruebas unitarias sean satisfactorias;
- las pruebas instrumentadas principales sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 70. Resultado esperado

Al cerrar HU-35:

```text
Bitmap procesado
        +
DetectedTextRegion
        ↓
PP-OCRv5_mobile_rec
        ↓
salida CTC
        ↓
diccionario
        ↓
texto
        +
confianza
        +
región de origen
```

Todavía no existirá un documento PP-OCRv5 completo visible para el usuario.

La siguiente historia implementará:

```text
HU-36 — Construir el pipeline documental PP-OCRv5
```
