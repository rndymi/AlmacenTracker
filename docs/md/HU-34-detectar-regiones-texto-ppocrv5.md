# HU-34 — Detectar regiones de texto con PP-OCRv5

> Segunda historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-34  
**Nombre:** Detectar regiones de texto con PP-OCRv5  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-34-detectar-regiones-texto`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-33 — Preparar ONNX Runtime y los recursos PP-OCRv5  
**Issue prevista:** `#38`

---

## 2. Historia de usuario

Como usuario,  
quiero que la aplicación pueda localizar las zonas que contienen texto dentro de una fotografía o imagen de una lista,  
para preparar el reconocimiento posterior de cada región mediante PP-OCRv5.

---

## 3. Objetivo

Implementar la etapa de detección de texto utilizando:

```text
PP-OCRv5_mobile_det
+
ONNX Runtime
```

Flujo previsto:

```text
Bitmap procesado
        ↓
PaddleTextDetector
        ↓
redimensionado y normalización
        ↓
tensor FLOAT NCHW
        ↓
detectorSession.run(...)
        ↓
mapa de probabilidad
        ↓
binarización
        ↓
regiones conectadas
        ↓
cajas de texto filtradas
        ↓
coordenadas restauradas
        ↓
TextDetectionResult
```

HU-34 deberá producir una colección ordenada y estable de regiones de texto.

No deberá reconocer todavía el contenido textual de esas regiones.

---

## 4. Regla principal

> HU-34 detecta dónde existe texto. No determina qué texto contiene cada región.

Resultado de esta historia:

```text
imagen
    ↓
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

No se deberá:

- crear referencias de mercadería;
- extraer categoría o código;
- extraer cantidad o unidad;
- consultar Room;
- construir historial;
- mostrar resultados al usuario;
- reemplazar el OCR actual.

---

## 5. Documentos y código de referencia

HU-34 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-33-preparar-onnx-ppocrv5.md`;
- el estado real de `AlmacenTrackerHU33.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- el funcionamiento completamente offline;
- el contrato existente `DocumentTextRecognizer`;
- el ciclo de vida de sesiones preparado en HU-33;
- la separación entre detección, reconocimiento y reglas de mercadería;
- la liberación explícita de tensores y resultados ONNX;
- la política de crear componentes únicamente cuando representen una responsabilidad real.

El plan de v1.4 asigna a HU-34:

```text
preprocesamiento del detector
+
inferencia
+
posprocesamiento
+
cajas
+
confianza
+
restauración de coordenadas
+
orden provisional
+
pruebas con imágenes
```

---

## 6. Estado real antes de HU-34

El análisis de `AlmacenTrackerHU33.zip` confirma:

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
merge HU33 #37 into develop
```

HU-33 ya proporciona:

```text
data/document/onnx/
├── OnnxModelAssetLoader.java
├── PaddleOcrDictionary.java
├── PaddleOcrInitializationError.java
├── PaddleOcrInitializationResult.java
├── PaddleOcrIntegrityException.java
├── PaddleOcrManifestException.java
├── PaddleOcrMetadataException.java
├── PaddleOcrModelConfiguration.java
├── PaddleOcrModelManifest.java
├── PaddleOcrRuntimeFactory.java
├── PaddleOcrRuntimeInitializer.java
├── PaddleOcrRuntimeProvider.java
├── PaddleOcrSessionBundle.java
└── PaddleOcrSessionMetadataValidator.java
```

También dispone de:

```text
app/src/main/assets/ocr/ppocrv5/
├── NOTICE.md
├── model_manifest.properties
├── ppocrv5_mobile_det.onnx
├── ppocrv5_mobile_rec.onnx
└── ppocrv5_mobile_rec_dict.txt
```

El manifiesto confirmado declara para el detector:

```text
detector.input.name = x
detector.input.rank = 4
detector.output.name = fetch_name_0
detector.output.count = 1
detector.output.rank = 4
```

ONNX Runtime se encuentra fijado en:

```text
1.24.3
```

El runtime se inicializa de forma:

- perezosa;
- thread-safe;
- fuera del hilo principal;
- reutilizando sesiones;
- con cierre idempotente;
- validación de integridad;
- validación de metadatos;
- errores controlados.

La sesión detectora ya puede obtenerse mediante:

```java
PaddleOcrSessionBundle.getDetectorSession()
```

Antes de HU-34 no existen:

- modelo de región detectada;
- configuración funcional del detector;
- preprocesador de imagen del detector;
- tensor de entrada de detección;
- ejecución real del modelo detector;
- lectura del mapa de probabilidad;
- binarización;
- extracción de componentes o contornos;
- cálculo de cajas;
- confianza por región;
- restauración de coordenadas;
- orden de lectura provisional;
- pruebas de detección sobre imágenes reales.

---

## 7. Relación con el procesamiento de imagen existente

El proyecto ya dispone de:

```text
AndroidDocumentImageProcessor
```

Este componente:

- abre la imagen;
- lee dimensiones;
- corrige orientación EXIF;
- limita el lado máximo;
- utiliza `ARGB_8888`;
- aplica mejora moderada de contraste;
- entrega un `AndroidDocumentImage`;
- conserva dimensiones originales;
- conserva dimensiones procesadas;
- libera el bitmap mediante `close()`.

HU-34 no deberá duplicar:

- apertura de URI;
- lectura EXIF;
- rotación general;
- escalado inicial de seguridad;
- gestión de cierre de `DocumentImage`.

La detección deberá trabajar sobre un bitmap ya preparado.

El preprocesamiento específico de PP-OCRv5 será responsabilidad del detector.

---

## 8. Decisión sobre la integración en esta historia

HU-34 no conectará todavía:

```text
ReferenceListCaptureViewModel
        ↓
PaddleTextDetector
```

Motivos:

- el detector solo produce regiones;
- todavía no existe reconocimiento de contenido;
- el flujo visible espera `RecognizedDocument`;
- mostrar regiones sin texto no aporta una funcionalidad útil al usuario;
- conectar parcialmente el motor añadiría estados temporales innecesarios.

La integración completa comenzará en HU-36.

HU-34 deberá ser verificable mediante:

- pruebas unitarias;
- pruebas instrumentadas;
- imágenes de prueba;
- una posible utilidad de desarrollo no expuesta en producción.

---

## 9. Alcance incluido

HU-34 incluye:

- definir la configuración funcional del detector;
- confirmar nombre de entrada y salida;
- confirmar forma real de entrada;
- confirmar forma real de salida;
- definir tamaño de inferencia;
- mantener proporción;
- redimensionar la imagen;
- aplicar padding cuando corresponda;
- conservar factores de escala;
- convertir píxeles a RGB;
- normalizar canales;
- construir tensor `FLOAT`;
- respetar orden NCHW;
- ejecutar `detectorSession.run(...)`;
- obtener la salida esperada;
- validar tipo de salida;
- validar rango y dimensiones;
- interpretar el mapa de probabilidad;
- aplicar umbral de binarización;
- localizar componentes de texto;
- descartar ruido;
- calcular caja delimitadora;
- calcular confianza de región;
- aplicar umbral de confianza;
- expandir la región cuando corresponda;
- limitar coordenadas;
- restaurar coordenadas al bitmap procesado;
- conservar coordenadas originales del resultado de detección;
- ordenar regiones provisionalmente;
- evitar regiones duplicadas o casi idénticas;
- devolver colección inmutable;
- representar imagen sin texto mediante lista vacía;
- tratar salida incompatible;
- cerrar tensor de entrada;
- cerrar resultado de sesión;
- no cerrar la sesión compartida;
- medir tiempo de preprocesamiento;
- medir tiempo de inferencia;
- medir tiempo de posprocesamiento;
- mantener ejecución fuera del hilo principal;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas con imágenes reales;
- CI.

---

## 10. Alcance excluido

HU-34 no incluye:

- reconocimiento de texto;
- carga funcional del diccionario;
- decodificación CTC;
- cálculo de confianza de caracteres;
- recorte definitivo para reconocimiento;
- transformación de perspectiva completa;
- orientación individual de cada región para el reconocedor;
- creación de `RecognizedTextLine`;
- creación de `RecognizedTextElement`;
- creación de `RecognizedDocument`;
- parser de referencias;
- cantidad;
- unidad;
- reconstrucción de filas;
- detección de una o dos columnas;
- conexión con la pantalla de captura;
- cambios visibles de interfaz;
- comparación con ML Kit;
- selección de motor OCR;
- retirada de dependencias;
- cambios de Room;
- cambios de historial;
- modificación de mercadería;
- optimización avanzada de hilos;
- uso de GPU;
- uso de NNAPI;
- procesamiento de varias páginas.

El reconocimiento pertenece a HU-35.

El pipeline detector–reconocedor pertenece a HU-36.

La reconstrucción e integración funcional pertenecen a HU-37.

---

## 11. Modelos de detección

Se añadirán modelos Java puros orientados a la salida del detector.

### 11.1. Región detectada

Nombre recomendado:

```text
DetectedTextRegion
```

Datos previstos:

```text
left
top
right
bottom
confidence
sourceOrder
```

Alternativa cuando se necesiten cuatro puntos:

```text
TextQuadrilateral
├── topLeft
├── topRight
├── bottomRight
└── bottomLeft
```

Decisión recomendada para HU-34:

- conservar internamente el polígono cuando el posprocesamiento pueda obtenerlo;
- exponer como mínimo una caja válida;
- no reducir prematuramente una región inclinada a un rectángulo si se pierde información necesaria para HU-35.

---

## 12. Resultado de detección

Nombre recomendado:

```text
TextDetectionResult
```

Datos previstos:

```text
regions
sourceWidth
sourceHeight
inferenceWidth
inferenceHeight
preprocessDurationMs
inferenceDurationMs
postprocessDurationMs
```

Reglas:

- colección no nula;
- colección inmutable;
- dimensiones positivas;
- regiones dentro de límites;
- orden estable;
- lista vacía válida;
- métricas no negativas;
- sin referencias a Bitmap;
- sin referencias a `OrtSession.Result`;
- sin referencias a tensores;
- sin dependencia de Activity.

Las métricas podrán excluirse del modelo público si se registran mediante un objeto interno de diagnóstico.

No se debe inflar el modelo únicamente para almacenar logs.

---

## 13. Punto y polígono

Se recomienda un modelo pequeño:

```text
TextPoint
```

con:

```text
x
y
```

Reglas:

- coordenadas `float`;
- valores finitos;
- no usar `android.graphics.PointF` en modelos reutilizables si se desea mantener independencia de Android;
- igualdad y `hashCode()` solo si son útiles para pruebas.

Polígono:

```text
DetectedTextPolygon
```

con cuatro puntos ordenados:

```text
topLeft
topRight
bottomRight
bottomLeft
```

El orden deberá ser determinista.

---

## 14. Coordenadas de referencia

HU-34 deberá definir claramente el espacio de coordenadas.

Se utilizarán tres espacios:

```text
1. bitmap procesado
2. imagen de inferencia
3. mapa de salida
```

### Bitmap procesado

Representa el bitmap entregado por `AndroidDocumentImageProcessor`.

### Imagen de inferencia

Representa la imagen redimensionada y rellenada para el modelo.

### Mapa de salida

Representa la resolución del tensor devuelto por el detector.

El resultado público deberá expresarse respecto al:

```text
bitmap procesado
```

Motivo:

- será la imagen utilizada por HU-35 para recortar;
- evita mezclar dimensiones originales previas a rotación;
- coincide con el contenido real enviado al motor.

La transformación hacia la fotografía original no es necesaria para reconocer texto.

---

## 15. Configuración funcional del detector

Se recomienda ampliar el manifiesto o crear:

```text
PaddleTextDetectorConfiguration
```

Datos previstos:

```text
inputName
outputName
maximumSide
dimensionMultiple
pixelThreshold
boxThreshold
minimumRegionSize
unclipRatio
```

Los valores definitivos deberán basarse en:

- metadatos reales del modelo;
- documentación de PP-OCRv5;
- pruebas sobre imágenes del proyecto;
- estabilidad en Android.

No se deberán dispersar números mágicos entre métodos.

---

## 16. Tamaño de inferencia

La imagen deberá redimensionarse manteniendo proporción.

Criterios:

- no ampliar imágenes pequeñas sin necesidad;
- limitar el lado mayor;
- ajustar ancho y alto a un múltiplo requerido por el detector;
- mantener dimensiones mayores que cero;
- evitar imágenes excesivamente grandes;
- conservar los factores de escala.

Configuración inicial recomendada:

```text
maximumSide = 960
dimensionMultiple = 32
```

Estos valores son iniciales y deberán confirmarse con el modelo real.

No se utilizará automáticamente el máximo de `2200` del preprocesador general como tamaño del tensor detector.

---

## 17. Estrategia de redimensionado

Se recomienda:

```text
scale = min(
    1.0,
    maximumSide / max(width, height)
)
```

Después:

```text
resizedWidth = round(width * scale)
resizedHeight = round(height * scale)
```

A continuación se ajustarán las dimensiones al múltiplo requerido.

La implementación deberá decidir explícitamente entre:

### Ajuste por redimensionado

```text
redimensionar directamente al múltiplo
```

### Ajuste mediante padding

```text
mantener tamaño redimensionado
+
añadir bordes hasta el múltiplo
```

Se recomienda padding porque conserva mejor la proporción.

El padding deberá registrarse para restaurar coordenadas.

---

## 18. Metadatos de transformación

Se recomienda crear:

```text
DetectorImageTransform
```

Datos:

```text
sourceWidth
sourceHeight
resizedWidth
resizedHeight
paddedWidth
paddedHeight
scaleX
scaleY
paddingLeft
paddingTop
paddingRight
paddingBottom
```

Responsabilidades:

- validar dimensiones;
- convertir coordenadas de salida;
- eliminar padding;
- limitar coordenadas;
- no almacenar Bitmap;
- facilitar pruebas.

---

## 19. Preprocesador del detector

Nombre recomendado:

```text
PaddleTextDetectorPreprocessor
```

Responsabilidades:

- recibir Bitmap;
- validar que no esté reciclado;
- calcular transformación;
- crear bitmap temporal redimensionado;
- aplicar padding;
- leer píxeles;
- convertir a tensor NCHW;
- devolver tensor y transformación;
- liberar bitmaps temporales;
- no ejecutar sesión;
- no interpretar resultados.

Resultado interno:

```text
DetectorInput
├── OnnxTensor
└── DetectorImageTransform
```

`DetectorInput` deberá implementar:

```text
AutoCloseable
```

para cerrar el tensor.

---

## 20. Formato del tensor de entrada

La forma prevista será:

```text
[1, 3, height, width]
```

La forma final deberá comprobarse con la sesión real.

Tipo:

```text
FLOAT
```

Orden:

```text
NCHW
```

No se asumirá `NHWC`.

La prueba instrumentada deberá confirmar:

```text
input rank = 4
channel dimension = 3
```

cuando la forma exponga esa dimensión.

---

## 21. Conversión de píxeles

El bitmap Android utiliza píxeles ARGB.

El detector deberá extraer:

```text
R
G
B
```

No utilizará el canal alfa.

Proceso conceptual:

```text
pixel
    ↓
red = Color.red(pixel)
green = Color.green(pixel)
blue = Color.blue(pixel)
```

Para evitar llamadas excesivas por píxel se recomienda:

```text
Bitmap.getPixels(...)
```

sobre un array reutilizable dentro de la operación.

No se mantendrá el array después de crear el tensor.

---

## 22. Normalización

La normalización deberá utilizar los valores esperados por PP-OCRv5.

Configuración prevista:

```text
value = channel / 255.0
value = (value - mean) / std
```

Los valores de:

```text
mean
std
```

deberán centralizarse.

No se utilizarán valores de otro modelo OCR sin confirmarlos.

Una prueba de píxel conocido deberá verificar la conversión.

---

## 23. Orden de canales

HU-34 deberá confirmar si el modelo espera:

```text
RGB
```

o:

```text
BGR
```

No se deberá asumir basándose únicamente en el formato del bitmap.

La decisión deberá quedar:

- documentada;
- centralizada;
- cubierta por prueba.

---

## 24. Creación del tensor

Se recomienda construir:

```text
FloatBuffer
```

con tamaño:

```text
1 * 3 * height * width
```

Orden de escritura:

```text
todos los R
todos los G
todos los B
```

cuando se utilice NCHW.

El tensor se creará con:

```text
OnnxTensor.createTensor(...)
```

Reglas:

- comprobar desbordamiento de tamaño;
- evitar arrays multidimensionales de objetos;
- usar buffer directo o estructura eficiente;
- cerrar siempre;
- no reutilizar después de cerrar;
- no exponer a la UI.

---

## 25. Detector

Nombre recomendado:

```text
PaddleTextDetector
```

Responsabilidades:

- recibir Bitmap;
- obtener la sesión detectora;
- preprocesar;
- ejecutar inferencia;
- extraer salida;
- posprocesar;
- devolver `TextDetectionResult`;
- cerrar tensor;
- cerrar resultado;
- no cerrar sesión;
- no reconocer texto;
- no consultar Room;
- no navegar.

Firma orientativa síncrona:

```java
TextDetectionResult detect(Bitmap bitmap)
        throws TextDetectionException;
```

La ejecución síncrona es válida porque el llamador deberá ejecutarla en el executor OCR.

No se necesita callback dentro del detector si no aporta valor.

---

## 26. Obtención de la sesión

El detector no deberá construir:

```text
OrtEnvironment
OrtSession
```

Deberá recibir:

```text
PaddleOcrSessionBundle
```

o únicamente:

```text
OrtSession detectorSession
```

Decisión recomendada:

```text
recibir OrtSession en constructor
```

si el ciclo de vida queda gestionado por el runtime provider.

Sin embargo, no deberá conservar una sesión que pueda cerrarse sin coordinación.

La composición final deberá garantizar que el detector no se use después del cierre del bundle.

---

## 27. Ejecución ONNX

Flujo:

```text
Map<String, OnnxTensor>
        ↓
detectorSession.run(...)
        ↓
OrtSession.Result
```

Se usará el nombre configurado:

```text
x
```

La salida esperada:

```text
fetch_name_0
```

La implementación deberá:

- comprobar que existe;
- comprobar que es tensor;
- comprobar tipo `FLOAT`;
- comprobar rank 4;
- comprobar batch 1;
- comprobar canal o mapa esperado;
- rechazar formas incompatibles;
- cerrar `Result`.

No se deberá obtener la primera salida por índice sin validar su nombre.

---

## 28. Modelo de salida esperado

El detector devuelve conceptualmente un mapa:

```text
[1, 1, mapHeight, mapWidth]
```

La forma exacta deberá verificarse con el modelo real.

La implementación deberá soportar una forma dinámica siempre que el resultado concreto sea válido.

No deberá aceptar silenciosamente:

```text
batch > 1
channels > 1
mapHeight <= 0
mapWidth <= 0
```

salvo que el modelo real justifique otra estructura.

---

## 29. Lectura del mapa de probabilidad

El resultado deberá convertirse en una representación lineal:

```text
float[] probabilityMap
```

o equivalente.

Reglas:

- longitud exacta `mapHeight * mapWidth`;
- valores finitos;
- valores esperados entre `0` y `1`;
- valores fuera de rango limitados solo si la salida real lo justifica;
- `NaN` o infinito producen error técnico;
- no conservar `OnnxTensor` después del posprocesamiento.

---

## 30. Binarización

Se aplicará:

```text
probability >= pixelThreshold
```

Configuración inicial orientativa:

```text
pixelThreshold = 0.3
```

El valor deberá quedar centralizado.

La máscara podrá representarse como:

```text
boolean[]
```

o:

```text
byte[]
```

Se recomienda `byte[]` para reducir sobrecarga.

No se utilizará `Boolean[]`.

---

## 31. Extracción de regiones

El proyecto no incorpora actualmente una dependencia de visión artificial para contornos.

HU-34 deberá elegir conscientemente entre:

### Opción A — Componentes conectados en Java

Ventajas:

- no añade otra biblioteca nativa;
- menor impacto de APK;
- control total;
- suficiente para una primera detección rectangular.

Limitaciones:

- menor precisión geométrica;
- más trabajo para polígonos inclinados;
- expansión aproximada.

### Opción B — Biblioteca específica de visión

Ventajas:

- contornos y polígonos robustos;
- operaciones morfológicas;
- aproximación más cercana al posprocesamiento de PaddleOCR.

Limitaciones:

- mayor tamaño;
- otra dependencia nativa;
- compatibilidad ABI adicional;
- complejidad de empaquetado.

Para HU-34 se recomienda comenzar con componentes conectados en Java únicamente si las pruebas demuestran que conserva adecuadamente las líneas.

No se añadirá OpenCV solo por costumbre.

---

## 32. Componentes conectados

Si se utiliza la opción Java, se recomienda:

```text
TextProbabilityMapAnalyzer
```

Responsabilidades:

- recibir mapa y máscara;
- recorrer píxeles no visitados;
- aplicar conectividad 8;
- acumular límites;
- acumular probabilidad;
- contar píxeles;
- producir candidatos;
- evitar recursión profunda;
- utilizar cola iterativa;
- limitar memoria;
- no conocer Bitmap.

Datos por componente:

```text
minX
minY
maxX
maxY
pixelCount
probabilitySum
maximumProbability
```

Confianza inicial:

```text
probabilitySum / pixelCount
```

---

## 33. Prevención de desbordamiento

No se deberá implementar flood fill recursivo.

Una región grande podría provocar:

```text
StackOverflowError
```

Se utilizará:

```text
ArrayDeque<Integer>
```

o una cola equivalente.

El índice lineal:

```text
index = y * width + x
```

deberá comprobar el tamaño máximo antes de reservar arrays.

---

## 34. Filtro de tamaño

Se descartarán componentes demasiado pequeños.

Configuración orientativa:

```text
minimumPixelCount
minimumWidth
minimumHeight
```

Los filtros deberán aplicarse en coordenadas del mapa de salida o convertirse de forma consistente.

No se deberá descartar automáticamente texto estrecho como:

```text
1
I
```

solo por su anchura.

El área y la altura tendrán más peso que un ancho mínimo rígido.

---

## 35. Confianza de región

La confianza de una región deberá derivarse del mapa de probabilidad.

No se utilizará:

```text
confidence = 1.0
```

para todas las regiones.

Opciones:

- media de píxeles del componente;
- media dentro de la caja;
- media dentro del polígono.

Para la primera implementación se recomienda:

```text
media de píxeles positivos del componente
```

La estrategia deberá quedar cubierta por pruebas.

---

## 36. Umbral de caja

Después de calcular confianza:

```text
confidence >= boxThreshold
```

Configuración inicial orientativa:

```text
boxThreshold = 0.6
```

Los valores deberán evaluarse con imágenes reales.

No se confundirá:

```text
pixelThreshold
```

con:

```text
boxThreshold
```

---

## 37. Expansión de región

PaddleOCR utiliza habitualmente una expansión de las cajas detectadas.

HU-34 podrá aplicar un `unclip` simplificado a cajas rectangulares:

```text
expandedWidth = width * ratio
expandedHeight = height * ratio
```

centrado en la región.

Configuración orientativa:

```text
unclipRatio = 1.5
```

La expansión deberá:

- preservar el centro;
- limitarse a la imagen;
- evitar dimensiones cero;
- no incluir áreas excesivas;
- poder deshabilitarse en pruebas.

Si se implementan polígonos reales, el algoritmo de expansión deberá tratarse con mayor precisión.

---

## 38. Restauración desde el mapa

Transformación conceptual:

```text
mapX
    ↓
inferenceX
    ↓
removePadding
    ↓
sourceX
```

Factores:

```text
inferenceScaleX =
    paddedWidth / mapWidth

sourceScaleX =
    sourceWidth / resizedWidth
```

La implementación deberá centralizar esta lógica en:

```text
DetectorImageTransform
```

No se repetirán fórmulas en varios componentes.

---

## 39. Eliminación de padding

Una región detectada exclusivamente dentro del padding deberá descartarse.

Cuando una caja atraviese el padding:

- se recortará al área redimensionada;
- después se restaurará;
- se validará que conserve área positiva.

No se deberán devolver coordenadas negativas.

---

## 40. Limitación de coordenadas

Resultado final:

```text
0 <= left < right <= sourceWidth
0 <= top < bottom <= sourceHeight
```

Los cuatro puntos del polígono deberán quedar dentro de esos límites.

Valores fuera de rango por redondeo deberán limitarse.

Una región degenerada después de limitarse deberá descartarse.

---

## 41. Duplicados y solapamientos

Componentes próximos pueden generar cajas duplicadas o casi iguales.

Se recomienda un filtro mediante:

```text
Intersection over Union
```

Configuración inicial orientativa:

```text
duplicateIouThreshold = 0.85
```

Regla:

- conservar la región de mayor confianza;
- mantener el orden estable;
- no fusionar regiones distintas únicamente porque estén próximas.

La fusión de palabras en líneas pertenece a reconstrucción posterior.

---

## 42. Orden provisional

HU-34 deberá devolver un orden útil pero no definitivo.

Orden recomendado:

```text
top ascendente
left ascendente
```

Con tolerancia vertical:

```text
si dos regiones comparten banda vertical,
ordenar por left
```

No se deberá implementar todavía:

- detección de dos columnas;
- reconstrucción de filas documentales;
- asociación referencia–cantidad.

El orden definitivo pertenece a HU-37.

---

## 43. Resultado sin texto

Una imagen sin regiones válidas deberá devolver:

```text
TextDetectionResult
regions = []
```

No se tratará como excepción.

Casos:

- imagen en blanco;
- fotografía sin texto;
- confianza inferior al umbral;
- solo ruido descartado.

El pipeline futuro podrá convertirlo en estado:

```text
NO_TEXT
```

---

## 44. Errores de detección

Se recomienda crear:

```text
TextDetectionException
```

con causas controladas:

```text
INVALID_IMAGE
RUNTIME_NOT_READY
SESSION_CLOSED
INPUT_SHAPE_INCOMPATIBLE
OUTPUT_NOT_FOUND
OUTPUT_TYPE_INCOMPATIBLE
OUTPUT_SHAPE_INCOMPATIBLE
OUTPUT_VALUE_INVALID
MEMORY_ERROR
INFERENCE_ERROR
POSTPROCESSING_ERROR
```

No se expondrá `OrtException` fuera de infraestructura.

La causa técnica podrá conservarse para logs y pruebas.

---

## 45. Uso de memoria

HU-34 deberá evitar conservar simultáneamente:

- bitmap fuente;
- bitmap redimensionado;
- bitmap con padding;
- array de píxeles;
- float buffer;
- tensor;
- mapa de salida;
- máscara;
- cola;
- lista de candidatos;

durante más tiempo del necesario.

Estrategia:

1. crear bitmap temporal;
2. extraer píxeles;
3. liberar bitmap temporal;
4. construir tensor;
5. liberar array cuando sea posible;
6. ejecutar;
7. cerrar tensor;
8. copiar salida necesaria;
9. cerrar resultado;
10. posprocesar;
11. liberar arrays temporales.

---

## 46. Bitmaps temporales

Los bitmaps creados por HU-34 deberán:

- utilizar `ARGB_8888` cuando sea necesario;
- reciclarse únicamente si fueron creados por el detector;
- no reciclar el bitmap recibido;
- cerrarse en bloque `finally`;
- no almacenarse en campos de larga duración.

El propietario del bitmap fuente continuará siendo `DocumentImage`.

---

## 47. Tensores y resultados

Se utilizará `try-with-resources` cuando la API lo permita.

Ejemplo conceptual:

```java
try (
    DetectorInput input = preprocessor.prepare(...);
    OrtSession.Result result =
        session.run(...)
) {
    return postProcessor.process(...);
}
```

La sesión compartida no se incluirá en el bloque de cierre.

---

## 48. Concurrencia

`PaddleTextDetector` podrá ser síncrono.

La ejecución asíncrona pertenece al llamador mediante:

```text
ocrExecutor
```

Reglas:

- no ejecutar en UI;
- no lanzar dos inferencias simultáneas sobre la misma sesión sin verificar seguridad;
- inicialmente serializar detecciones;
- respetar cierre del runtime;
- no publicar callbacks después del cierre;
- no crear un executor por detección.

HU-33 ya dispone de un executor OCR que podrá reutilizarse.

---

## 49. Cancelación

ONNX Runtime no garantiza necesariamente cancelación inmediata de una inferencia iniciada.

HU-34 deberá permitir cancelación lógica:

```text
resultado obsoleto
    → se descarta
```

El detector no necesita conocer ids de solicitud.

La gestión de resultados obsoletos continuará en el ViewModel o pipeline futuro.

---

## 50. Métricas internas

Se medirán mediante:

```text
System.nanoTime()
```

Etapas:

```text
preprocess
inference
postprocess
total
```

Las métricas:

- se usarán en tests y desarrollo;
- no se mostrarán al usuario;
- no incluirán contenido de imagen;
- no se persistirán en Room;
- no serán criterio único de éxito.

---

## 51. Arquitectura propuesta

Estructura orientativa:

```text
data/document/onnx/
├── detection/
│   ├── PaddleTextDetector.java
│   ├── PaddleTextDetectorConfiguration.java
│   ├── PaddleTextDetectorPreprocessor.java
│   ├── PaddleTextDetectorPostProcessor.java
│   ├── DetectorImageTransform.java
│   ├── DetectorInput.java
│   ├── TextProbabilityMapAnalyzer.java
│   └── TextDetectionException.java
└── model/
    ├── TextPoint.java
    ├── DetectedTextRegion.java
    └── TextDetectionResult.java
```

Para mantener claridad se acepta:

```text
data/document/onnx/detection/
```

y:

```text
data/document/onnx/model/
```

No se recomienda colocar todo directamente en:

```text
data/document/onnx/
```

si la detección empieza a reunir varias responsabilidades reales.

Tampoco deberán crearse subdirectorios con una sola clase sin motivo.

---

## 52. Dependencias

HU-34 no deberá añadir automáticamente:

- OpenCV;
- RenderScript;
- TensorFlow Lite;
- bibliotecas remotas;
- GPU runtime.

La primera implementación debe utilizar:

```text
Android Bitmap
Java
ONNX Runtime
```

Una dependencia adicional solo se justificará si el posprocesamiento no puede alcanzar resultados aceptables y se documenta su impacto.

---

## 53. Relación con `PaddleOcrModelManifest`

Se recomienda ampliar el manifiesto con datos funcionales confirmados:

```text
detector.output.name
detector.output.rank
detector.input.channel.count
```

Los valores ya presentes en `model_manifest.properties` deberán ser consumidos por Java cuando aporten validación real.

Actualmente el manifiesto contiene algunos datos que todavía no expone `PaddleOcrModelManifest`.

HU-34 deberá revisar esa diferencia.

No se duplicarán nombres de salida en código y propiedades sin una fuente de verdad clara.

---

## 54. Ampliación del manifiesto Java

`PaddleOcrModelManifest` podrá incorporar:

```text
detectorOutputName
detectorOutputRank
```

y, si se confirma:

```text
detectorChannelCount
```

Las validaciones deberán comprobar:

- propiedad presente;
- texto no vacío;
- rank positivo;
- salida existente;
- tensor FLOAT;
- forma compatible.

No se añadirán propiedades ficticias que el modelo no necesite.

---

## 55. Composición

HU-34 podrá añadir en `ReferenceListModule` o en un módulo OCR interno:

```text
PaddleTextDetectorFactory
```

solo si simplifica la creación con:

- runtime provider;
- configuración;
- preprocesador;
- posprocesador.

No se expondrá todavía desde `AppContainer` a Activities.

Una opción válida:

```text
PaddleOcrRuntimeProvider
        ↓ READY
PaddleTextDetector
```

La creación deberá ocurrir después de una inicialización satisfactoria.

---

## 56. Pruebas unitarias del transformador

Casos mínimos:

- imagen cuadrada;
- imagen horizontal;
- imagen vertical;
- imagen pequeña;
- reducción a lado máximo;
- ajuste a múltiplo;
- padding horizontal;
- padding vertical;
- punto superior izquierdo;
- punto inferior derecho;
- eliminación de padding;
- limitación de coordenadas;
- caja degenerada;
- redondeos.

---

## 57. Pruebas unitarias de normalización

Casos:

- píxel negro;
- píxel blanco;
- rojo puro;
- verde puro;
- azul puro;
- alfa cero;
- orden RGB;
- orden NCHW;
- forma del buffer;
- media y desviación;
- dimensiones inválidas;
- bitmap reciclado.

Las pruebas de Bitmap requerirán prueba instrumentada o una abstracción de píxeles.

No se forzará una prueba JVM artificial que no represente Android.

---

## 58. Pruebas unitarias del mapa

Casos:

- mapa vacío;
- un componente;
- dos componentes;
- componentes diagonales con conectividad 8;
- ruido de un píxel;
- región grande;
- confianza media;
- confianza insuficiente;
- valores `NaN`;
- valores infinitos;
- longitud incorrecta;
- dimensiones inválidas;
- componente en borde;
- región dentro de padding.

---

## 59. Pruebas unitarias de solapamiento

Casos:

- cajas idénticas;
- cajas sin intersección;
- intersección parcial;
- una caja contenida;
- mismo nivel de confianza;
- diferente confianza;
- umbral exacto;
- orden estable.

---

## 60. Pruebas instrumentadas de inferencia

Se utilizará el detector real incluido.

Casos mínimos:

### Imagen con texto claro

Debe producir:

```text
regions.size() > 0
```

### Imagen en blanco

Debe producir:

```text
regions.isEmpty()
```

### Imagen con varias líneas

Debe producir varias regiones coherentes.

### Imagen rotada previamente por el procesador

Debe detectar en coordenadas del bitmap orientado.

### Cierre

Después de cerrar el runtime:

```text
detectar
    → error controlado
```

No se exigirán coordenadas exactas píxel por píxel porque pequeñas diferencias del runtime pueden ser válidas.

Sí se exigirán límites y estabilidad razonable.

---

## 61. Imágenes de prueba

El corpus de HU-34 deberá ser pequeño y controlado.

Se recomienda incluir imágenes sintéticas o de datos ficticios:

```text
MR 1210
MZ 1300A
4 CAJAS
```

Reglas:

- sin datos reales;
- licencia compatible;
- tamaño moderado;
- resolución conocida;
- versión controlada;
- resultados esperados documentados.

No se incluirán fotografías privadas sin necesidad.

---

## 62. Visualización técnica de cajas

Para validar durante desarrollo podrá existir una utilidad de test que dibuje cajas sobre una copia del bitmap.

No deberá:

- modificar el bitmap original;
- formar parte de la UI de producción;
- guardar automáticamente en galería;
- solicitar permisos;
- incluir texto real en logs.

Una prueba manual podrá exportar una imagen únicamente en entorno local de desarrollo.

---

## 63. Pruebas de regresión

HU-34 deberá comprobar que siguen operativos:

- apertura del flujo de listas;
- selección de imagen;
- captura fotográfica;
- procesamiento actual;
- revisión;
- ubicaciones;
- historial;
- escaneo individual;
- CSV;
- Room;
- funcionamiento offline.

El detector PP-OCRv5 todavía no deberá cambiar el resultado visible.

---

## 64. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además deberá comprobar:

- compilación de clases de detección;
- modelos presentes;
- manifiesto compatible;
- pruebas unitarias del posprocesamiento;
- generación de APK por ABI;
- ausencia de recursos duplicados;
- ausencia de imágenes privadas.

Las pruebas instrumentadas reales podrán mantenerse fuera de la CI si no existe emulador configurado.

No se reemplazarán por mocks afirmando que la inferencia real está cubierta.

---

## 65. Criterios de aceptación

### CA-01 — Sesión reutilizada

**Dado** el runtime preparado por HU-33,  
**cuando** se crea el detector,  
**entonces** utiliza la sesión detectora existente y no crea otra sesión.

### CA-02 — Imagen válida

**Dado** un bitmap válido,  
**cuando** se preprocesa,  
**entonces** se genera un tensor `FLOAT` de rank 4 con dimensiones compatibles.

### CA-03 — Proporción conservada

**Dada** una imagen rectangular,  
**cuando** se redimensiona,  
**entonces** mantiene su proporción y registra el padding aplicado.

### CA-04 — Normalización correcta

**Dado** un píxel conocido,  
**cuando** se convierte al tensor,  
**entonces** utiliza el orden y los valores de normalización configurados.

### CA-05 — Inferencia real

**Dado** el modelo detector,  
**cuando** se ejecuta con una entrada válida,  
**entonces** produce la salida `fetch_name_0` con tipo y rango compatibles.

### CA-06 — Mapa interpretado

**Dada** una salida válida,  
**cuando** se posprocesa,  
**entonces** se obtiene un mapa de probabilidad verificable.

### CA-07 — Regiones

**Dada** una imagen con varias líneas,  
**cuando** se detecta,  
**entonces** se devuelve una colección no vacía de regiones.

### CA-08 — Coordenadas válidas

**Dada** cualquier región devuelta,  
**entonces** sus coordenadas permanecen dentro del bitmap procesado.

### CA-09 — Confianza

**Dada** una región detectada,  
**entonces** contiene una confianza finita y dentro del rango admitido.

### CA-10 — Ruido descartado

**Dado** un componente inferior a los umbrales,  
**cuando** se posprocesa,  
**entonces** no se devuelve como región válida.

### CA-11 — Imagen sin texto

**Dada** una imagen en blanco,  
**cuando** se procesa,  
**entonces** se devuelve una lista vacía sin error.

### CA-12 — Orden estable

**Dadas** varias regiones,  
**cuando** se devuelven,  
**entonces** mantienen un orden provisional determinista.

### CA-13 — Recursos cerrados

**Dada** una inferencia finalizada o fallida,  
**entonces** tensor y resultado ONNX quedan cerrados.

### CA-14 — Sesión conservada

**Dada** una inferencia finalizada,  
**entonces** la sesión compartida continúa abierta para futuras operaciones.

### CA-15 — Ejecución local

**Dado** un dispositivo sin Internet,  
**cuando** se detectan regiones,  
**entonces** la operación funciona localmente.

### CA-16 — Sin reconocimiento

**Dada** HU-34,  
**cuando** se obtiene una región,  
**entonces** todavía no contiene texto reconocido.

### CA-17 — Flujo visible intacto

**Dado** el usuario procesando una lista,  
**entonces** continúa utilizando el comportamiento visible previo hasta HU-36.

---

## 66. Riesgos

### Normalización incorrecta

**Riesgo:** el modelo ejecuta, pero produce mapas inútiles.

**Mitigación:** pruebas de píxeles y validación con imágenes conocidas.

### Orden de canales incorrecto

**Riesgo:** detección degradada sin excepción.

**Mitigación:** configuración explícita y comparación de resultados.

### Forma interpretada incorrectamente

**Riesgo:** lectura equivocada del mapa o acceso fuera de rango.

**Mitigación:** validar rank, batch, canal y dimensiones concretas.

### Posprocesamiento demasiado simple

**Riesgo:** regiones unidas, fragmentadas o cajas poco precisas.

**Mitigación:** corpus realista y posibilidad de evolucionar el algoritmo.

### Dependencia pesada innecesaria

**Riesgo:** añadir OpenCV incrementa el tamaño antes de comprobar necesidad.

**Mitigación:** comenzar con Java cuando los resultados sean suficientes.

### Memoria excesiva

**Riesgo:** bitmap, buffer y salida conviven demasiado tiempo.

**Mitigación:** liberación por etapas y límite de inferencia.

### Coordenadas incorrectas

**Riesgo:** HU-35 recorta zonas equivocadas.

**Mitigación:** pruebas exhaustivas de transformación.

### Regiones dentro del padding

**Riesgo:** falsos positivos en bordes añadidos.

**Mitigación:** eliminar padding antes de restaurar.

### Detección en UI

**Riesgo:** congelación de pantalla.

**Mitigación:** executor OCR preparado por HU-33.

### Cierre de sesión compartida

**Riesgo:** futuras inferencias fallan.

**Mitigación:** el detector solo cierra recursos propios.

---

## 67. Definición de terminado

HU-34 estará terminada cuando:

- exista configuración funcional del detector;
- nombre de entrada y salida estén centralizados;
- forma de entrada esté validada;
- forma de salida esté validada;
- la imagen se redimensione manteniendo proporción;
- las dimensiones sean compatibles;
- el padding se registre;
- el tensor FLOAT NCHW se construya correctamente;
- la normalización esté probada;
- la sesión detectora se reutilice;
- la inferencia real funcione;
- la salida se interprete;
- el mapa se valide;
- la máscara se genere;
- las regiones se extraigan;
- el ruido se descarte;
- cada región tenga confianza;
- las coordenadas se restauren;
- las coordenadas estén dentro de límites;
- los duplicados se filtren;
- el orden sea estable;
- una imagen en blanco produzca lista vacía;
- tensor y resultado se cierren;
- la sesión permanezca abierta;
- no se ejecute en el hilo principal;
- el procesamiento funcione offline;
- no se reconozca todavía texto;
- el flujo visible anterior permanezca intacto;
- las pruebas unitarias sean satisfactorias;
- las pruebas instrumentadas principales sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 68. Resultado esperado

Al cerrar HU-34:

```text
Bitmap procesado
        ↓
PP-OCRv5_mobile_det
        ↓
mapa de probabilidad
        ↓
regiones de texto
        ↓
coordenadas válidas
        ↓
confianza
        ↓
orden provisional
```

Todavía no existirá contenido textual reconocido.

La siguiente historia implementará:

```text
HU-35 — Reconocer el contenido de las regiones detectadas
```
