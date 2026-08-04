# HU-33 — Preparar ONNX Runtime y los recursos PP-OCRv5

> Primera historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-33  
**Nombre:** Preparar ONNX Runtime y los recursos PP-OCRv5  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-33-preparar-onnx-ppocrv5`  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.3.0`  
**Issue prevista:** `#37`

---

## 2. Historia de usuario

Como usuario,  
quiero que la aplicación disponga de una base local y estable para ejecutar los modelos PP-OCRv5,  
para preparar la mejora del reconocimiento documental sin depender de Internet ni alterar las funciones existentes.

---

## 3. Objetivo

Integrar la infraestructura mínima necesaria para cargar y validar localmente:

```text
ONNX Runtime
+
PP-OCRv5_mobile_det
+
PP-OCRv5_mobile_rec
+
diccionario compatible con reconocimiento
```

Flujo previsto para esta historia:

```text
inicio de la aplicación
        ↓
composición de dependencias OCR
        ↓
localización de recursos
        ↓
creación controlada de OrtEnvironment
        ↓
creación de sesión de detección
        ↓
creación de sesión de reconocimiento
        ↓
carga y validación del diccionario
        ↓
estado READY o ERROR
```

HU-33 deberá demostrar que los recursos necesarios:

- están incluidos correctamente;
- pueden abrirse en Android;
- son compatibles con ONNX Runtime;
- exponen entradas y salidas verificables;
- funcionan en las ABI publicadas;
- no requieren conexión;
- pueden cerrarse sin fugas evidentes.

HU-33 no ejecutará todavía el reconocimiento funcional de una lista.

---

## 4. Regla principal

> Esta historia prepara y valida la infraestructura. No implementa detección, reconocimiento ni sustitución del flujo OCR actual.

Resultado correcto de HU-33:

```text
los modelos cargan
las sesiones se crean
el diccionario es compatible
la infraestructura informa READY
```

Resultado que pertenece a historias posteriores:

```text
imagen
    ↓
regiones detectadas
    ↓
texto reconocido
```

No se deberá conectar PP-OCRv5 al flujo de usuario únicamente porque las sesiones puedan abrirse.

---

## 5. Documentos y código de referencia

HU-33 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- el estado real de `AlmacenTracker-ver1.3.0.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- el contrato existente `DocumentTextRecognizer`;
- el procesamiento completamente local;
- el funcionamiento offline;
- la política de introducir clases únicamente cuando representen una responsabilidad real;
- la compatibilidad de APK por ABI;
- la revisión manual obligatoria del OCR;
- la separación entre infraestructura ONNX y reglas de mercadería.

El plan general asigna a HU-33:

```text
dependencia ONNX Runtime
+
modelos móviles
+
diccionario
+
carga local
+
validación
+
sesiones
+
errores de inicialización
+
pruebas de carga
+
compatibilidad ABI
+
medición inicial de tamaño
```

---

## 6. Estado real antes de HU-33

El análisis de `AlmacenTracker-ver1.3.0.zip` confirma:

```groovy
versionCode 4
versionName "1.3.0"
minSdk 26
targetSdk 36
Java 11
```

La rama incluida es:

```text
develop
```

El último commit funcional es:

```text
merge HU32 #36 into develop
```

La aplicación genera APK para:

```text
arm64-v8a
armeabi-v7a
x86_64
universal
```

El proyecto ya utiliza:

```text
core/document/
├── DocumentImage
├── DocumentImageLoader
├── DocumentImageProcessor
├── DocumentTextRecognizer
├── DocumentRecognitionCallback
├── RecognizedDocument
├── RecognizedTextLine
└── RecognizedTextElement
```

La implementación documental actual es:

```text
MlKitDocumentTextRecognizer
```

y se compone dentro de:

```text
ReferenceListModule
```

El flujo actual:

```text
ReferenceListCaptureViewModel
        ↓
DocumentImageProcessor
        ↓
DocumentTextRecognizer
        ↓
RecognizedDocument
```

El ViewModel ya:

- evita procesamientos simultáneos;
- utiliza identificadores de solicitud;
- ignora resultados obsoletos;
- conserva estados;
- cierra imágenes descartadas;
- no depende de clases internas del OCR.

Antes de HU-33 no existen:

- dependencia de ONNX Runtime;
- archivos de modelos PP-OCRv5 dentro del proyecto;
- diccionario de reconocimiento PP-OCRv5;
- cargador de modelos ONNX;
- configuración de sesiones;
- validación de entradas y salidas;
- modelo de estado de inicialización;
- infraestructura para cerrar sesiones ONNX;
- pruebas de apertura de modelos;
- medición del impacto de ONNX Runtime y modelos;
- comprobación de carga en las ABI publicadas.

---

## 7. Observación sobre los recursos suministrados

Las fuentes visuales indican como modelos objetivo:

```text
PaddlePaddle/PP-OCRv5_mobile_det
PaddlePaddle/PP-OCRv5_mobile_rec
```

Sin embargo, HU-33 deberá verificar los archivos reales antes de incorporarlos.

No se deberá asumir únicamente por el nombre de la página que:

- el archivo descargado ya está en formato ONNX;
- utiliza una única entrada;
- los nombres de nodos son conocidos;
- las formas son estáticas;
- el reconocedor incluye el diccionario;
- el diccionario de otra versión es compatible;
- los operadores son compatibles con Android;
- la licencia permite cualquier forma de redistribución.

Antes de implementar, deberán estar disponibles:

```text
modelo de detección en formato ONNX
modelo de reconocimiento en formato ONNX
diccionario exacto del modelo de reconocimiento
información de licencia
```

Si alguno falta, HU-33 no deberá inventarlo ni sustituirlo silenciosamente por un recurso de otra versión.

---

## 8. Alcance incluido

HU-33 incluye:

- iniciar la versión Android `1.4.0`;
- actualizar `versionCode` de forma coherente;
- mantener `minSdk 26`;
- añadir ONNX Runtime al catálogo de versiones;
- añadir la dependencia Android correspondiente;
- incorporar el modelo móvil de detección;
- incorporar el modelo móvil de reconocimiento;
- incorporar el diccionario compatible;
- documentar procedencia y licencia de los recursos;
- definir nombres locales estables;
- elegir la ubicación local de los modelos;
- evitar compresión problemática cuando resulte necesario;
- crear configuración de recursos PP-OCRv5;
- crear un cargador de recursos;
- crear o reutilizar `OrtEnvironment`;
- crear sesión de detección;
- crear sesión de reconocimiento;
- validar que ambas sesiones abren;
- inspeccionar metadatos de entradas;
- inspeccionar metadatos de salidas;
- validar tipos de tensores esperados;
- validar dimensiones básicas;
- validar disponibilidad del diccionario;
- validar que el diccionario no está vacío;
- validar compatibilidad básica entre salida de reconocimiento y diccionario;
- representar estados de inicialización;
- representar errores técnicos controlados;
- cerrar sesiones;
- cerrar recursos parcialmente creados ante error;
- evitar inicialización duplicada;
- evitar acceso desde Activities;
- preparar una composición clara en `ReferenceListModule`;
- mantener activo el flujo actual;
- no conectar todavía el motor PP-OCRv5 al usuario;
- mantener funcionamiento offline;
- medir tamaño de modelos;
- medir diferencia aproximada de APK;
- comprobar empaquetado por ABI;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas de carga real;
- CI.

---

## 9. Alcance excluido

HU-33 no incluye:

- preprocesar una imagen para detección;
- crear tensores de imagen funcionales;
- ejecutar inferencia sobre fotografías;
- interpretar mapas de probabilidad;
- detectar contornos;
- calcular cajas;
- recortar regiones;
- corregir perspectiva;
- reconocer texto;
- decodificar logits;
- calcular confianza OCR;
- reconstruir líneas;
- integrar PP-OCRv5 con `ReferenceListCaptureViewModel`;
- cambiar el resultado visible del OCR;
- comparar precisión entre motores;
- optimizar tiempos de inferencia;
- optimizar memoria de imágenes;
- retirar dependencias existentes;
- modificar `DocumentTextRecognizer` sin una necesidad comprobada;
- modificar reglas de referencia;
- modificar cantidad o unidad;
- modificar historial;
- modificar Room;
- añadir migraciones;
- añadir pantallas;
- añadir opciones de usuario;
- descargar modelos desde Internet;
- entrenar modelos;
- modificar modelos.

La detección funcional pertenece a HU-34.

El reconocimiento funcional pertenece a HU-35.

El pipeline completo pertenece a HU-36.

---

## 10. Versión Android

Al comenzar v1.4 se deberá actualizar:

```groovy
versionCode 5
versionName "1.4.0"
```

Reglas:

- `versionCode` debe ser mayor que `4`;
- no se cambiará `applicationId`;
- no se cambiará `minSdk`;
- no se modificará la versión de Room;
- no se añadirá una migración;
- el cambio de versión deberá quedar probado mediante build.

El README de desarrollo deberá indicar:

```text
AlmacenTracker v1.4.0 — En desarrollo
```

---

## 11. Dependencia ONNX Runtime

La dependencia deberá declararse en:

```text
gradle/libs.versions.toml
```

y consumirse desde:

```text
app/build.gradle
```

Estructura orientativa:

```toml
[versions]
onnxRuntime = "<versión validada>"

[libraries]
onnxruntime-android = {
    module = "com.microsoft.onnxruntime:onnxruntime-android",
    version.ref = "onnxRuntime"
}
```

y:

```groovy
implementation libs.onnxruntime.android
```

La versión exacta deberá fijarse después de comprobar:

- compatibilidad con `minSdk 26`;
- compatibilidad con AGP utilizado;
- disponibilidad de las ABI necesarias;
- ausencia de conflictos de empaquetado;
- ejecución de pruebas instrumentadas.

No se utilizará un rango dinámico como:

```text
+
latest.release
```

---

## 12. Selección de la variante del runtime

HU-33 deberá utilizar una variante de ONNX Runtime compatible con Android y con las ABI del proyecto.

La decisión deberá comprobar:

```text
arm64-v8a
armeabi-v7a
x86_64
```

No se asumirá que todas las variantes publicadas contienen las mismas bibliotecas nativas.

Si una ABI no está soportada por la dependencia elegida, deberá documentarse antes de modificar los splits de publicación.

No se eliminará una ABI silenciosamente.

---

## 13. Ubicación de los modelos

Ubicación recomendada inicialmente:

```text
app/src/main/assets/ocr/ppocrv5/
```

Estructura orientativa:

```text
assets/
└── ocr/
    └── ppocrv5/
        ├── ppocrv5_mobile_det.onnx
        ├── ppocrv5_mobile_rec.onnx
        ├── ppocrv5_mobile_rec_dict.txt
        └── NOTICE.md
```

Los nombres son internos y podrán ajustarse a los archivos reales.

Reglas:

- nombres en minúsculas;
- sin espacios;
- sin versión ambigua;
- detección y reconocimiento claramente diferenciados;
- diccionario junto al reconocedor;
- información de procedencia separada del código.

---

## 14. Decisión sobre `assets`

Se recomienda `assets` porque:

- permite conservar nombres y estructura;
- facilita agrupar modelos y diccionario;
- permite abrir recursos mediante `AssetManager`;
- evita generar identificadores `R.raw` para archivos grandes;
- mantiene juntos los recursos del motor.

Sin embargo, deberá comprobarse cómo ONNX Runtime recibe el modelo.

Opciones posibles:

```text
leer bytes desde AssetManager
crear OrtSession desde byte[]
```

o:

```text
copiar una vez a almacenamiento interno
crear OrtSession desde ruta
```

La decisión deberá basarse en medición real.

No se copiarán los modelos en cada inicialización.

---

## 15. Compresión de modelos

Los modelos ONNX no deberán comprimirse si eso obliga a:

- descomprimirlos repetidamente;
- duplicar memoria;
- copiar temporalmente en cada sesión;
- bloquear el inicio.

Si se utiliza una ruta que requiere acceso directo al archivo, podrá configurarse:

```groovy
androidResources {
    noCompress += ["onnx"]
}
```

o la sintaxis compatible con la versión del plugin.

La configuración solo se añadirá después de verificar que aporta una necesidad real.

El diccionario sí podrá permanecer como texto comprimible si se lee una sola vez.

---

## 16. Procedencia y licencia

Se añadirá un archivo interno:

```text
NOTICE.md
```

o equivalente, con:

- nombre del modelo;
- repositorio o fuente;
- versión o revisión;
- fecha de obtención;
- formato recibido;
- proceso de conversión si existió;
- licencia;
- hash SHA-256 del archivo incorporado;
- nombre del diccionario;
- relación entre modelo y diccionario.

No se dependerá únicamente de la URL de una página mutable.

No se incluirán modelos sin licencia identificable.

---

## 17. Hashes de integridad

Durante la incorporación se deberán calcular hashes:

```text
SHA-256 detector
SHA-256 recognizer
SHA-256 dictionary
```

Los hashes servirán para:

- detectar reemplazos accidentales;
- documentar exactamente los recursos probados;
- reproducir la evaluación;
- evitar mezclar modelo y diccionario de revisiones distintas.

No es obligatorio verificar los hashes en cada inicio de producción si aumenta innecesariamente el tiempo.

Sí deberán verificarse en una prueba o tarea de desarrollo.

---

## 18. Configuración PP-OCRv5

Se recomienda crear:

```text
PaddleOcrModelConfiguration
```

Ubicación:

```text
data/document/onnx/
```

Datos orientativos:

```text
detectorAssetPath
recognizerAssetPath
dictionaryAssetPath
detectorInputName
recognizerInputName
expectedDetectorOutputCount
expectedRecognizerOutputCount
```

La configuración deberá:

- ser inmutable;
- no depender de Activity;
- validar rutas;
- evitar strings dispersos;
- no contener lógica de inferencia;
- permitir pruebas con recursos alternativos.

No se incluirán todavía umbrales funcionales de detección ni reconocimiento si no se usan en HU-33.

---

## 19. Cargador de modelos

Se recomienda crear:

```text
OnnxModelAssetLoader
```

Responsabilidades:

- abrir un asset;
- leerlo de forma segura;
- devolver bytes o ruta preparada;
- comprobar tamaño mayor que cero;
- cerrar streams;
- transformar errores técnicos;
- no crear sesiones;
- no conocer ViewModels;
- no conocer reglas OCR.

No deberá:

- ejecutar inferencia;
- decodificar texto;
- conocer categoría o código;
- publicar LiveData.

Si ONNX Runtime admite directamente el mecanismo elegido sin duplicación, no se creará un wrapper innecesario.

---

## 20. Entorno ONNX

Se recomienda una única instancia de:

```text
OrtEnvironment
```

por contenedor de aplicación.

Reglas:

- crear mediante la API oficial;
- no crear una instancia por imagen;
- no exponerla a Activities;
- no almacenarla en un singleton global mutable fuera de composición;
- tratar errores de creación;
- definir claramente quién la cierra;
- no cerrarla mientras existan sesiones activas.

Si la API devuelve un entorno global gestionado, la implementación deberá respetar su ciclo de vida real y no simular propiedad exclusiva.

---

## 21. Sesiones

Se deberán crear dos sesiones:

```text
detectorSession
recognizerSession
```

La infraestructura podrá agruparse en:

```text
PaddleOcrSessionBundle
```

Responsabilidades:

- conservar las sesiones;
- exponer metadatos necesarios;
- conocer su estado cerrado;
- cerrar ambas;
- tolerar cierre repetido;
- cerrar la sesión ya creada si falla la segunda;
- no ejecutar todavía el pipeline.

No es obligatorio crear esta clase si un inicializador pequeño puede gestionar ambas sesiones con claridad.

---

## 22. Opciones de sesión

Configuración inicial recomendada:

```text
optimización del grafo habilitada
ejecución CPU
número de hilos conservador
sin execution provider experimental
```

HU-33 deberá priorizar:

```text
compatibilidad
+
estabilidad
+
reproducibilidad
```

No deberá incorporar todavía:

- NNAPI;
- XNNPACK experimental;
- GPU;
- aceleradores específicos;
- configuración agresiva de hilos.

Esas optimizaciones deberán medirse en HU-39.

---

## 23. Inicializador de recursos

Se recomienda crear:

```text
PaddleOcrRuntimeInitializer
```

Responsabilidades:

- recibir `Context` de aplicación;
- recibir configuración;
- obtener el entorno;
- cargar detector;
- crear sesión detectora;
- cargar reconocedor;
- crear sesión reconocedora;
- cargar diccionario;
- validar metadatos;
- devolver un resultado preparado;
- cerrar recursos parciales ante error;
- ser idempotente o estar protegido contra doble llamada.

No deberá:

- procesar imágenes;
- devolver `RecognizedDocument`;
- mostrar mensajes;
- navegar;
- acceder a Room.

---

## 24. Estado de inicialización

Se añadirá un modelo Java:

```text
PaddleOcrInitializationResult
```

Estados orientativos:

```text
READY
MODEL_NOT_FOUND
MODEL_EMPTY
MODEL_INCOMPATIBLE
DICTIONARY_NOT_FOUND
DICTIONARY_EMPTY
DICTIONARY_INCOMPATIBLE
RUNTIME_ERROR
CLOSED
```

El resultado podrá incluir:

```text
errorCode
technicalCause opcional solo para logs de desarrollo
metadata
```

La UI no deberá recibir excepciones ONNX directamente.

En HU-33 este estado podrá utilizarse únicamente en pruebas y composición, sin mostrarse todavía al usuario.

---

## 25. Metadatos de sesión

HU-33 deberá inspeccionar y registrar en pruebas:

### Detector

- nombres de entrada;
- nombres de salida;
- tipo de entrada;
- forma de entrada;
- tipo de salida;
- forma de salida;
- cantidad de salidas.

### Reconocedor

- nombres de entrada;
- nombres de salida;
- tipo de entrada;
- forma de entrada;
- tipo de salida;
- forma de salida;
- cantidad de salidas.

No se deberán codificar nombres de nodos antes de inspeccionarlos.

Si se deciden nombres configurados, una prueba deberá confirmar que existen realmente.

---

## 26. Validación de forma

La validación inicial deberá ser suficientemente flexible para dimensiones dinámicas.

Ejemplos de dimensiones posibles:

```text
-1
0
símbolos
```

No se exigirá que todas las dimensiones sean positivas si el modelo declara entradas dinámicas.

Sí se deberá validar:

- rango de dimensiones razonable;
- tipo tensor;
- número de canales cuando pueda determinarse;
- existencia de al menos una entrada;
- existencia de al menos una salida;
- ausencia de tipos no soportados por el pipeline previsto.

La forma funcional exacta se utilizará en HU-34 y HU-35.

---

## 27. Diccionario

Se recomienda crear:

```text
PaddleOcrDictionary
```

Responsabilidades:

- cargar líneas en orden;
- conservar caracteres exactamente;
- manejar línea vacía solo si el formato lo requiere;
- exponer tamaño;
- obtener token por índice;
- no normalizar letras;
- no convertir a mayúsculas;
- no aplicar reglas de referencia;
- ser inmutable.

La lectura deberá utilizar:

```text
UTF-8
```

No se deberá usar el locale del dispositivo.

---

## 28. Compatibilidad diccionario–salida

HU-33 deberá documentar y probar la relación:

```text
número de clases del reconocedor
↔
número de entradas del diccionario
+
tokens especiales
```

No se deberá imponer una fórmula universal sin verificar el export real.

La prueba deberá conocer explícitamente:

- si existe token blank;
- si existe token space;
- si existen tokens start/end;
- en qué posición se insertan;
- cómo se calcula el índice visible.

Si la relación no puede justificarse con los recursos reales, la inicialización deberá fallar como incompatible.

---

## 29. Arquitectura propuesta

Estructura orientativa:

```text
data/
└── document/
    └── onnx/
        ├── PaddleOcrModelConfiguration.java
        ├── OnnxModelAssetLoader.java
        ├── PaddleOcrDictionary.java
        ├── PaddleOcrRuntimeInitializer.java
        ├── PaddleOcrInitializationResult.java
        └── PaddleOcrSessionBundle.java
```

No se crearán todavía:

```text
PaddleTextDetector
PaddleTextRecognizer
DetectionPostProcessor
RecognitionDecoder
PaddleOcrDocumentTextRecognizer
```

salvo que una clase mínima sea necesaria para validar la sesión.

Esas responsabilidades pertenecen a HU-34, HU-35 y HU-36.

---

## 30. Integración con `ReferenceListModule`

El módulo actual crea directamente:

```text
MlKitDocumentTextRecognizer
```

HU-33 deberá mantener ese comportamiento funcional.

La infraestructura PP-OCRv5 podrá quedar preparada mediante:

```text
PaddleOcrRuntimeProvider
```

o un componente equivalente dentro de `ReferenceListModule`.

Reglas:

- no sustituir todavía el reconocedor entregado al ViewModel;
- no inicializar modelos cada vez que se abre una pantalla;
- no crear sesiones si nunca se utilizarán, salvo que una prueba controlada lo requiera;
- no hacer el arranque de la aplicación perceptiblemente más lento;
- no bloquear `Application.onCreate()`.

Se recomienda inicialización perezosa:

```text
lazy
+
thread-safe
+
una sola vez
```

---

## 31. Ciclo de vida

La propiedad de los recursos deberá quedar explícita.

Opción recomendada:

```text
AppContainer
    └── PaddleOcrRuntimeProvider
            └── sesiones reutilizables
```

Reglas:

- las sesiones no pertenecen a una Activity;
- la rotación no las recrea;
- el ViewModel no las cierra directamente si son compartidas;
- el contenedor controla su cierre;
- el cierre repetido es seguro;
- las pruebas pueden crear y cerrar instancias aisladas.

La aplicación actual no expone todavía un método global de cierre del `AppContainer`.

HU-33 deberá documentar cómo se liberan los recursos y añadir un cierre explícito si existe un punto de vida útil real.

No se deberá añadir un cierre que se ejecute mientras la aplicación pueda seguir usando el OCR.

---

## 32. Inicialización perezosa

El proveedor deberá distinguir:

```text
NOT_INITIALIZED
INITIALIZING
READY
ERROR
CLOSED
```

Cuando dos consumidores soliciten inicialización simultáneamente:

- no se crearán cuatro sesiones;
- la segunda solicitud esperará o recibirá el mismo resultado;
- no se publicarán callbacks contradictorios;
- un error quedará disponible para reintento controlado;
- un cierre impedirá nuevas solicitudes.

HU-33 podrá implementar el proveedor sin conectarlo todavía a UI.

---

## 33. Hilo de ejecución

La carga de modelos y creación de sesiones no deberá ejecutarse en el hilo principal.

Se recomienda un executor dedicado:

```text
ocrExecutor
```

Motivo:

- crear sesiones puede ser costoso;
- no debe bloquear Room;
- no debe bloquear operaciones CSV;
- la carga OCR tiene una responsabilidad distinta.

Configuración inicial:

```text
Executors.newSingleThreadExecutor()
```

Ventajas:

- inicialización serializada;
- inferencia futura controlable;
- menor complejidad inicial.

HU-39 podrá revisar el número de hilos.

---

## 34. Cambios en `AppContainer`

`AppContainer` podrá añadir:

```text
ExecutorService ocrExecutor
PaddleOcrRuntimeProvider paddleOcrRuntimeProvider
```

y proporcionar el componente al módulo de listas.

Reglas:

- usar `applicationContext`;
- no exponer `OrtEnvironment` directamente;
- no exponer sesiones a Activities;
- no duplicar el proveedor;
- no mezclarlo con `databaseExecutor`;
- no mezclarlo con `fileExecutor`.

No se modificará la composición de inventario, CSV o historial.

---

## 35. Compatibilidad con `DocumentTextRecognizer`

HU-33 no deberá modificar el contrato existente:

```java
void recognize(
        DocumentImage documentImage,
        DocumentImageSource sourceType,
        DocumentRecognitionCallback callback
);

void close();
```

Motivo:

- todavía no existe una implementación PP-OCRv5 completa;
- cambiar el contrato ahora anticiparía necesidades no verificadas;
- el flujo actual funciona;
- HU-36 será el momento correcto para adaptar la implementación completa.

Sí se deberá comprobar que la infraestructura futura podrá producir:

```text
RecognizedDocument
RecognizedTextLine
RecognizedTextElement
```

sin introducir clases ONNX en `core`.

---

## 36. Tratamiento de errores

Los errores técnicos deberán mapearse a códigos internos estables.

No se mostrarán:

- ruta interna del asset;
- nombre de nodo ONNX;
- stack trace;
- hash;
- excepción nativa;
- versión de operador;
- detalles de tensor.

En logs de desarrollo podrá registrarse información técnica sin contenido documental.

No se registrarán modelos completos ni bytes.

---

## 37. Logs

Los logs de desarrollo podrán incluir:

```text
modelo cargado
duración de inicialización
tamaño de archivo
nombres y formas de nodos
ABI actual
versión de ONNX Runtime
```

No deberán incluir:

- imágenes;
- texto reconocido;
- referencias reales;
- rutas privadas externas;
- contenido del diccionario completo.

Los logs de producción deberán reducirse al mínimo.

---

## 38. Medición de tamaño

HU-33 deberá registrar un baseline antes y después.

Mediciones mínimas:

```text
APK arm64-v8a antes
APK arm64-v8a después
APK armeabi-v7a antes
APK armeabi-v7a después
APK x86_64 antes
APK x86_64 después
APK universal antes
APK universal después
```

También:

```text
tamaño detector
tamaño reconocedor
tamaño diccionario
```

La medición deberá diferenciar:

- tamaño comprimido del APK;
- tamaño aproximado de archivos incluidos;
- impacto de librerías nativas por ABI.

No se afirmará que la aplicación pesa menos o más sin medir.

---

## 39. Baseline de la release estable

Antes de modificar dependencias se deberán generar los artefactos de `v1.3.0` mediante:

```text
assembleDebug
```

Después se generarán los mismos artefactos con HU-33.

Los resultados deberán documentarse en pruebas o notas internas.

No es necesario mostrar las mediciones al usuario dentro de la aplicación.

---

## 40. Verificación ABI

Para cada ABI publicada se deberá comprobar:

### `arm64-v8a`

- APK generada;
- instalación;
- carga de biblioteca;
- creación de entorno;
- apertura de detector;
- apertura de reconocedor;
- cierre.

### `armeabi-v7a`

- mismas comprobaciones;
- atención especial a memoria y soporte nativo.

### `x86_64`

- mismas comprobaciones;
- útil principalmente para emulador.

La APK universal deberá contener las bibliotecas necesarias para las tres ABI.

---

## 41. Pruebas unitarias

### Configuración

- rutas válidas;
- ruta nula;
- ruta vacía;
- configuración inmutable;
- igualdad si se implementa;
- nombres diferenciados.

### Diccionario

- carga de líneas;
- UTF-8;
- orden conservado;
- archivo vacío;
- índice válido;
- índice negativo;
- índice fuera de rango;
- inmutabilidad;
- cierre de stream.

### Resultado de inicialización

- `READY`;
- errores diferenciados;
- causa técnica opcional;
- no exposición de excepción a capa superior.

### Provider

- inicialización única;
- dos solicitudes simultáneas;
- resultado compartido;
- error conservado;
- reintento si se admite;
- cierre idempotente;
- solicitud después de cierre.

Las pruebas JVM no deberán fingir que pueden abrir una sesión Android real si la biblioteca requiere entorno instrumentado.

---

## 42. Pruebas instrumentadas

Las pruebas instrumentadas deberán utilizar los modelos reales incluidos.

### Recursos

- detector existe;
- reconocedor existe;
- diccionario existe;
- tamaños mayores que cero;
- hashes esperados.

### ONNX Runtime

- crear entorno;
- abrir detector;
- abrir reconocedor;
- enumerar entradas;
- enumerar salidas;
- validar tipos;
- validar formas básicas;
- cerrar sesiones;
- cierre repetido;
- error con modelo corrupto de prueba cuando sea viable.

### Compatibilidad

- ejecución en emulador `x86_64`;
- ejecución en dispositivo `arm64-v8a`;
- `armeabi-v7a` cuando exista dispositivo o emulador compatible.

La ausencia de hardware de una ABI deberá documentarse; compilar no equivale a validar ejecución.

---

## 43. Pruebas de integración

Se deberá comprobar:

```text
AppContainer
        ↓
ReferenceListModule
        ↓
provider PP-OCRv5
        ↓
inicialización
```

Sin modificar:

```text
ReferenceListCaptureViewModel
        ↓
MlKitDocumentTextRecognizer
```

El procesamiento actual deberá continuar funcionando.

Pruebas:

- abrir flujo de listas;
- seleccionar imagen;
- procesar con comportamiento actual;
- rotar;
- regresar;
- cerrar;
- comprobar que la infraestructura nueva no provoca crash;
- comprobar que no se crean sesiones repetidamente sin necesidad.

---

## 44. CI

La CI deberá comprobar:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además:

- modelos presentes;
- diccionario presente;
- nombres esperados;
- hashes cuando se automatice;
- APK por ABI;
- ausencia de modelos duplicados;
- ausencia de archivos temporales de conversión;
- ausencia de información privada.

Las pruebas instrumentadas reales de ONNX podrán ejecutarse localmente si la CI actual no dispone de emulador.

No se deberá fingir cobertura de inferencia real únicamente con mocks.

---

## 45. Seguridad y privacidad

HU-33 deberá garantizar:

- modelos incluidos localmente;
- ausencia de descarga;
- ausencia de llamadas de red;
- ausencia de permiso de Internet por ONNX;
- no procesamiento de documentos todavía;
- no persistencia de imágenes;
- no registro de contenido;
- recursos con procedencia conocida;
- licencia documentada.

---

## 46. Accesibilidad

HU-33 no añade una pantalla nueva.

Por tanto, no deberá introducir cambios visuales.

Si se muestra temporalmente un estado técnico durante desarrollo, no deberá llegar a la release final sin:

- texto comprensible;
- soporte de TalkBack;
- acción de reintento;
- ausencia de detalles técnicos.

La UI funcional de inicialización se definirá cuando el motor se conecte al flujo.

---

## 47. Criterios de aceptación

### CA-01 — Versión iniciada

**Dado** el comienzo de v1.4,  
**cuando** se compila la aplicación,  
**entonces** utiliza `versionCode 5` y `versionName 1.4.0`.

### CA-02 — Dependencia fijada

**Dado** el catálogo Gradle,  
**cuando** se resuelven dependencias,  
**entonces** ONNX Runtime utiliza una versión explícita y reproducible.

### CA-03 — Recursos locales

**Dados** los modelos y el diccionario,  
**cuando** se inspecciona el APK,  
**entonces** los tres recursos están incluidos localmente.

### CA-04 — Detector cargable

**Dado** el modelo detector,  
**cuando** se inicializa ONNX Runtime,  
**entonces** la sesión se crea sin conexión a Internet.

### CA-05 — Reconocedor cargable

**Dado** el modelo reconocedor,  
**cuando** se inicializa ONNX Runtime,  
**entonces** la sesión se crea sin conexión a Internet.

### CA-06 — Diccionario válido

**Dado** el diccionario,  
**cuando** se carga,  
**entonces** conserva el orden, no está vacío y es compatible con la salida del reconocedor.

### CA-07 — Metadatos verificados

**Dadas** ambas sesiones,  
**cuando** se inspeccionan,  
**entonces** existen entradas y salidas de tipo compatible con el pipeline previsto.

### CA-08 — Error controlado

**Dado** un recurso ausente o incompatible,  
**cuando** se inicializa,  
**entonces** se devuelve un estado controlado y se cierran recursos parciales.

### CA-09 — Inicialización única

**Dadas** dos solicitudes simultáneas,  
**cuando** se inicializa el runtime,  
**entonces** no se crean sesiones duplicadas.

### CA-10 — Cierre seguro

**Dadas** sesiones abiertas,  
**cuando** se cierra el proveedor,  
**entonces** ambas sesiones se liberan y un segundo cierre no provoca error.

### CA-11 — Flujo actual intacto

**Dado** el procesamiento de listas existente,  
**cuando** se procesa una imagen después de HU-33,  
**entonces** mantiene el comportamiento funcional anterior.

### CA-12 — Sin inferencia funcional

**Dada** HU-33,  
**cuando** se utiliza la aplicación,  
**entonces** PP-OCRv5 todavía no sustituye el resultado visible del OCR.

### CA-13 — ABI

**Dadas** las ABI publicadas,  
**cuando** se generan las APK,  
**entonces** ONNX Runtime queda empaquetado para cada ABI soportada.

### CA-14 — Medición

**Dados** los artefactos antes y después,  
**cuando** se comparan,  
**entonces** existe una medición documentada del impacto inicial.

### CA-15 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se inicializan los modelos,  
**entonces** la operación funciona completamente de forma local.

---

## 48. Riesgos

### Archivo no convertido a ONNX

**Riesgo:** el recurso descargado pertenece al formato nativo de Paddle.

**Mitigación:** verificar extensión, estructura y apertura real antes de incorporarlo.

### Diccionario incorrecto

**Riesgo:** la sesión abre, pero el reconocimiento futuro produce índices incompatibles.

**Mitigación:** documentar la relación exacta modelo–diccionario y validarla.

### Operadores incompatibles

**Riesgo:** ONNX Runtime no soporta el grafo exportado.

**Mitigación:** prueba instrumentada de apertura antes de implementar detección.

### ABI no soportada

**Riesgo:** una APK compila, pero falla al cargar la biblioteca nativa.

**Mitigación:** ejecutar pruebas reales por ABI cuando sea posible.

### Inicialización en hilo principal

**Riesgo:** congelación al abrir la aplicación.

**Mitigación:** executor OCR e inicialización perezosa.

### Sesiones duplicadas

**Riesgo:** memoria innecesaria y cierres inconsistentes.

**Mitigación:** provider thread-safe con una sola inicialización.

### Recursos parciales

**Riesgo:** detector abierto y reconocedor fallido deja memoria nativa viva.

**Mitigación:** cierre en orden inverso ante cualquier error.

### Tamaño de APK

**Riesgo:** runtime y modelos aumentan considerablemente los artefactos.

**Mitigación:** baseline y medición por ABI.

### Acoplamiento prematuro

**Riesgo:** la UI queda ligada a ONNX antes de tener pipeline.

**Mitigación:** mantener `DocumentTextRecognizer` y el motor actual sin cambios funcionales.

---

## 49. Definición de terminado

HU-33 estará terminada cuando:

- la versión Android sea `1.4.0`;
- ONNX Runtime esté declarado con versión fija;
- el proyecto compile;
- el detector ONNX esté incluido;
- el reconocedor ONNX esté incluido;
- el diccionario exacto esté incluido;
- procedencia, licencia y hashes estén documentados;
- el entorno pueda crearse;
- la sesión detectora pueda abrirse;
- la sesión reconocedora pueda abrirse;
- las entradas y salidas estén verificadas;
- la compatibilidad básica del diccionario esté comprobada;
- exista un estado de inicialización;
- los errores sean controlados;
- recursos parciales se cierren;
- la inicialización no bloquee el hilo principal;
- no existan sesiones duplicadas;
- el cierre sea seguro;
- las APK por ABI se generen;
- exista medición inicial de tamaño;
- el flujo OCR existente continúe operativo;
- PP-OCRv5 todavía no se use para reconocer documentos;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 50. Resultado esperado

Al cerrar HU-33:

```text
AlmacenTracker v1.4.0
        ↓
ONNX Runtime disponible
        ↓
detector PP-OCRv5 cargable
        ↓
reconocedor PP-OCRv5 cargable
        ↓
diccionario compatible
        ↓
infraestructura READY
```

La aplicación continuará utilizando el flujo OCR funcional existente.

La siguiente historia implementará:

```text
HU-34 — Detectar regiones de texto con PP-OCRv5
```
