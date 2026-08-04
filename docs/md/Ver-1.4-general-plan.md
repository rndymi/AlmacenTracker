# AlmacenTracker — Plan de la versión 1.4

> Quinta entrega funcional: evaluación y evolución del OCR local mediante PP-OCRv5 y ONNX Runtime.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android prevista:** 1.4.0  
**Nombre de la versión:** Evolución del OCR local con PP-OCRv5  
**Estado inicial:** Planificada  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.3.0`

---

## 2. Contexto de partida

AlmacenTracker v1.3.0 ya permite:

- gestionar mercadería localmente mediante Room;
- importar, exportar, compartir, respaldar y restaurar mercadería mediante CSV;
- escanear códigos de barras y códigos QR;
- utilizar el escáner individual durante búsqueda, registro y edición;
- tomar fotografías de listas;
- seleccionar imágenes y capturas de pantalla;
- corregir orientación, escala y contraste;
- reconocer texto localmente;
- reconstruir listas de una o dos columnas;
- extraer y revisar referencias;
- clasificar coincidencias exactas, sugeridas, ambiguas o no encontradas;
- corregir, añadir, eliminar y confirmar referencias;
- consultar ubicaciones;
- capturar título, fecha, cantidad y unidad;
- registrar listas como historial documental;
- consultar listado y detalle histórico;
- buscar y filtrar el historial;
- eliminar registros históricos;
- funcionar completamente sin conexión.

La versión 1.4 no sustituirá estos flujos funcionales.

Su propósito será evaluar e integrar una evolución del reconocimiento documental local mediante:

```text
PP-OCRv5
+
ONNX Runtime
```

La integración deberá respetar la arquitectura MVVM pragmática organizada por funcionalidades:

```text
app
core
domain
data
feature
```

Cada historia deberá mantener la aplicación funcional antes de comenzar la siguiente.

---

## 3. Objetivo de la versión

Mejorar la capacidad de detectar y reconocer texto en fotografías, imágenes y capturas de listas mediante modelos PP-OCRv5 optimizados para dispositivos móviles y ejecutados localmente con ONNX Runtime.

Flujo previsto:

```text
fotografía o imagen
        ↓
carga y preprocesamiento
        ↓
PP-OCRv5_mobile_det
        ↓
regiones de texto
        ↓
recorte, normalización y ordenación
        ↓
PP-OCRv5_mobile_rec
        ↓
texto reconocido
        ↓
reconstrucción documental
        ↓
referencias, cantidades y unidades propuestas
        ↓
revisión obligatoria
```

La versión deberá comprobar mediante resultados medibles:

- precisión;
- estabilidad;
- tiempo de inferencia;
- memoria;
- tamaño de la aplicación;
- compatibilidad con dispositivos Android;
- calidad sobre listas reales de prueba.

---

## 4. Regla de oro de la versión

> PP-OCRv5 deberá mejorar el reconocimiento documental sin eliminar el control del usuario ni comprometer el funcionamiento offline.

El resultado continuará siendo una propuesta.

```text
OCR
    → propone

usuario
    → revisa, corrige y confirma
```

No se permitirá que una predicción OCR:

- modifique mercadería automáticamente;
- cambie sitio o posición;
- descuente stock;
- guarde historial sin confirmación;
- sustituya silenciosamente una referencia revisada;
- elimine la revisión manual.

---

## 5. Alcance incluido

La versión 1.4 incluirá:

- integración de ONNX Runtime en Android;
- carga local de modelos;
- modelo PP-OCRv5 de detección móvil;
- modelo PP-OCRv5 de reconocimiento móvil;
- gestión segura de sesiones ONNX;
- lectura de metadatos necesarios;
- carga de diccionario de caracteres;
- preparación de tensores;
- normalización de entradas;
- posprocesamiento de salidas;
- detección de regiones de texto;
- cálculo de cajas o polígonos;
- filtrado por confianza;
- recorte de regiones;
- corrección de orientación de regiones;
- reconocimiento de cada región;
- decodificación del texto;
- cálculo de confianza de reconocimiento;
- orden de lectura;
- adaptación a `RecognizedDocument`;
- adaptación a `RecognizedTextLine`;
- conservación del texto reconocido;
- integración con la reconstrucción de filas;
- integración con listas de una o dos columnas;
- corrección manual de la orientación de imágenes antes del OCR;
- reconstrucción de listas con varias columnas;
- interpretación revisable de referencias especiales separadas por guion;
- conservación de destinos documentales cuando aparezcan en la lista;
- integración con el parser de referencias;
- integración con propuestas de cantidad y unidad;
- revisión manual obligatoria;
- ejecución fuera del hilo principal;
- prevención de inferencias simultáneas incompatibles;
- cancelación lógica de resultados obsoletos;
- liberación de tensores;
- liberación de imágenes intermedias;
- cierre controlado de sesiones;
- control de memoria;
- tratamiento de errores de inicialización;
- tratamiento de modelos ausentes o corruptos;
- tratamiento de imágenes inválidas;
- tratamiento de salidas incompatibles;
- medición del tiempo de procesamiento;
- medición aproximada de memoria;
- medición del tamaño de modelos y APK;
- corpus local de pruebas;
- comparación reproducible;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas manuales;
- CI;
- release `v1.4.0`.

---

## 6. Alcance excluido

La versión 1.4 no incluirá:

- gestión de stock;
- descuento o incremento de existencias;
- pedidos;
- reservas;
- devoluciones;
- transferencias;
- autenticación;
- usuarios;
- roles;
- backend;
- sincronización remota;
- OCR remoto;
- subida de imágenes;
- almacenamiento permanente de fotografías;
- almacenamiento del texto OCR completo en Room;
- documentos PDF;
- automatización completa de orientación sin control del usuario;
- reglas hardcodeadas para familias concretas de referencias especiales;
- gestión logística o de stock basada en destinos documentales;
- procesamiento de varias páginas;
- reconocimiento de documentos arbitrarios;
- entrenamiento de modelos;
- reentrenamiento con datos privados;
- fine-tuning;
- descarga dinámica de modelos desde Internet;
- actualización remota de modelos;
- generación de etiquetas;
- impresión;
- ampliación automática a prefijos de tres letras;
- modificación del formato funcional de las referencias;
- eliminación de la revisión manual;
- sustitución de Room;
- cambios en el historial documental no relacionados con OCR.

Los documentos de varias páginas pertenecerán a v1.6.

---

## 7. Modelos previstos

La evaluación inicial utilizará modelos móviles:

```text
PP-OCRv5_mobile_det
PP-OCRv5_mobile_rec
```

### 7.1. Detección

El modelo de detección deberá recibir una imagen preprocesada y producir información suficiente para localizar regiones de texto.

Resultado conceptual:

```text
TextDetectionResult
├── región
├── confianza
├── orden provisional
└── orientación estimada cuando corresponda
```

### 7.2. Reconocimiento

El modelo de reconocimiento deberá recibir una región de texto normalizada y producir:

```text
TextRecognitionResult
├── texto
├── confianza
└── caracteres decodificados
```

### 7.3. Pipeline completo

```text
PaddleOcrDocumentTextRecognizer
        ↓
detección
        ↓
regiones
        ↓
reconocimiento
        ↓
RecognizedDocument
```

Los nombres definitivos deberán ajustarse a las responsabilidades reales del código.

---

## 8. Decisión sobre los modelos

Los modelos deberán almacenarse dentro de la aplicación o en recursos locales compatibles con Android.

La decisión entre:

```text
assets
res/raw
archivo interno preparado durante instalación
```

deberá basarse en:

- lectura eficiente;
- compatibilidad con ONNX Runtime;
- tamaño;
- compresión;
- facilidad de pruebas;
- ausencia de copias innecesarias;
- soporte para APK divididas por ABI.

No se asumirá que un modelo puede abrirse directamente desde un recurso comprimido sin verificar el comportamiento real.

---

## 9. Diccionario de reconocimiento

El modelo de reconocimiento necesita un diccionario coherente con su salida.

El diccionario deberá:

- incluirse localmente;
- cargarse una sola vez;
- conservar el orden exacto esperado por el modelo;
- manejar tokens especiales;
- manejar blank o padding cuando corresponda;
- validar el número de clases;
- fallar de forma controlada si no coincide con el modelo;
- no depender de red;
- no ser modificado por la interfaz.

La decodificación deberá conservar:

- letras;
- dígitos;
- espacios;
- signos necesarios para separar referencias y cantidades.

No deberá normalizar agresivamente durante la decodificación.

---

## 10. Arquitectura de OCR

El contrato funcional deberá continuar siendo:

```text
DocumentTextRecognizer
```

Las pantallas y ViewModels deberán seguir dependiendo de ese contrato.

Implementación prevista:

```text
data/document/onnx/
├── PaddleOcrDocumentTextRecognizer
├── PaddleTextDetector
├── PaddleTextRecognizer
├── OnnxModelLoader
├── OnnxTensorFactory
├── DetectionPostProcessor
├── RecognitionDecoder
└── PaddleOcrConfiguration
```

Esta estructura es orientativa.

No se crearán clases separadas cuando una responsabilidad sea demasiado pequeña o no tenga valor independiente.

---

## 11. Dirección de dependencias

```text
ReferenceListCaptureActivity
        ↓
ReferenceListCaptureViewModel
        ↓
DocumentTextRecognizer
        ↑
PaddleOcrDocumentTextRecognizer
        ↓
ONNX Runtime
```

Reglas:

- Activities no conocerán `OrtSession`;
- ViewModels no conocerán tensores;
- dominio no importará ONNX Runtime;
- modelos ONNX permanecerán en infraestructura;
- resultados se adaptarán a contratos existentes;
- la composición se realizará desde `AppContainer` o un módulo real;
- no se utilizará un singleton global mutable;
- no se accederá a modelos mediante rutas codificadas en Activities.

---

## 12. Sesiones ONNX Runtime

La inicialización deberá considerar:

```text
OrtEnvironment
OrtSession detector
OrtSession recognizer
```

Reglas:

- evitar crear sesiones por cada imagen;
- evitar crear una sesión por cada región;
- inicializar fuera del hilo principal;
- reutilizar sesiones cuando sea seguro;
- cerrar sesiones al finalizar la vida útil;
- controlar inicialización parcial;
- evitar carreras entre inicialización y reconocimiento;
- publicar un estado de error controlado;
- no mantener referencias a Activities.

La estrategia de ciclo de vida deberá quedar probada.

---

## 13. Configuración de ejecución

La integración deberá evaluar opciones de sesión como:

- número de hilos intraoperación;
- número de hilos interoperación;
- optimización del grafo;
- ejecución secuencial o paralela;
- asignación de memoria;
- compatibilidad por ABI.

No se aplicará una configuración extrema sin mediciones.

La configuración inicial deberá priorizar:

```text
estabilidad
+
consumo razonable
+
reproducibilidad
```

antes que el menor tiempo aislado posible.

---

## 14. Preprocesamiento de detección

La entrada del detector deberá contemplar:

- bitmap válido;
- orientación corregida;
- dimensiones conocidas;
- redimensionado manteniendo proporción;
- dimensiones compatibles con el modelo;
- padding cuando sea necesario;
- conversión de canales;
- orden RGB o BGR correcto;
- normalización;
- tensor con forma esperada;
- relación entre tamaño original y tamaño inferido.

Se deberá conservar la transformación necesaria para proyectar las regiones detectadas sobre la imagen original.

---

## 15. Posprocesamiento de detección

El posprocesamiento deberá:

- interpretar correctamente la salida;
- producir un mapa o representación equivalente;
- aplicar umbral de binarización;
- detectar contornos o regiones;
- calcular cajas;
- descartar regiones demasiado pequeñas;
- aplicar umbral de confianza;
- expandir regiones cuando el algoritmo lo necesite;
- limitar coordenadas a la imagen;
- restaurar escala original;
- ordenar resultados;
- evitar regiones duplicadas;
- tolerar una imagen sin texto.

No se implementará un posprocesamiento aproximado sin validarlo contra ejemplos conocidos.

---

## 16. Recorte y orientación de regiones

Cada región detectada deberá prepararse para reconocimiento.

La preparación podrá incluir:

- recorte por caja;
- transformación de perspectiva;
- corrección de rotación;
- normalización de orientación;
- redimensionado a altura fija;
- anchura variable o limitada;
- padding;
- liberación de bitmap temporal.

Reglas:

- no deformar el texto más de lo necesario;
- conservar regiones estrechas;
- evitar recortes fuera de límites;
- controlar regiones degeneradas;
- no bloquear el hilo principal.

---

## 17. Preprocesamiento de reconocimiento

La entrada del reconocedor deberá respetar:

- forma esperada;
- altura requerida;
- límite de anchura;
- proporción;
- canales;
- normalización;
- padding;
- orden de dimensiones;
- tipo de dato esperado.

Cuando una región supere la anchura admitida, deberá definirse una política explícita:

- redimensionar;
- segmentar;
- descartar con motivo;
- marcar como baja confianza.

No se truncará silenciosamente información.

---

## 18. Decodificación del reconocimiento

El decodificador deberá:

- interpretar logits o índices;
- aplicar la estrategia esperada por el modelo;
- manejar blank;
- colapsar repeticiones cuando corresponda;
- convertir índices mediante diccionario;
- calcular confianza;
- rechazar índices inválidos;
- conservar texto vacío como resultado válido de baja utilidad;
- no aplicar reglas de mercadería.

La decodificación OCR y el parser de referencias deberán permanecer separados.

---

## 19. Confianza

La versión deberá definir umbrales diferenciados para:

```text
detección
reconocimiento
propuesta documental
```

Una confianza baja no deberá convertirse automáticamente en una referencia válida.

Podrá producir:

- texto visible para revisión;
- propuesta ambigua;
- línea descartable;
- aviso de baja confianza.

Los umbrales deberán centralizarse y quedar cubiertos por pruebas.

---

## 20. Reconstrucción documental

Después del reconocimiento se reutilizará o adaptará la reconstrucción existente.

La reconstrucción deberá mantener:

- coordenadas;
- orden vertical;
- orden horizontal;
- agrupación de elementos;
- separación de filas;
- detección de una, dos o varias columnas cuando exista evidencia espacial suficiente;
- texto bruto;
- texto reconstruido;
- orden final estable.

La integración no deberá devolver una lista arbitraria según el orden interno del modelo.

---

## 21. Integración con referencias

El parser funcional continuará aplicando la identidad:

```text
exactamente dos letras iniciales
+
parte numérica
+
letra final opcional
```

Ejemplos:

```text
MR1210
MR 1210
MR1210A
MR 1210 A
```

PP-OCRv5 no cambiará por sí mismo las reglas del dominio.

La versión deberá comprobar:

- letras iniciales;
- ceros iniciales;
- letra final;
- espacios;
- referencias fragmentadas;
- varias referencias en una línea;
- referencia junto a cantidad;
- referencia junto a unidad.

---

## 22. Integración con cantidad y unidad

La salida reconocida podrá proponer:

```text
cantidad
unidad
```

Reglas vigentes:

- cantidad opcional;
- unidad opcional;
- revisión manual;
- enteros positivos cuando exista cantidad;
- normalización de unidad;
- ausencia de gestión de stock.

La versión deberá mejorar la separación entre:

```text
identidad
cantidad
unidad
```

Ejemplo:

```text
MR 1210A - 4 CAJAS
```

Resultado esperado:

```text
category = MR
code = 1210A
quantity = 4
unit = CAJAS
```

---

## 23. Estados de interfaz

El flujo de captura podrá representar:

```text
IDLE
IMAGE_SELECTED
INITIALIZING_OCR
PROCESSING
CONTENT
NO_TEXT
ERROR
```

Podrán adaptarse los estados existentes.

La UI deberá diferenciar:

- modelo inicializándose;
- imagen procesándose;
- imagen sin texto;
- error de carga de modelo;
- error de inferencia;
- error de imagen;
- resultado válido.

No se mostrarán nombres internos de nodos, tensores o excepciones.

---

## 24. Errores previstos

La implementación deberá tratar:

- modelo no encontrado;
- modelo ilegible;
- modelo incompatible;
- entrada ONNX inexistente;
- salida ONNX inexistente;
- forma inesperada;
- diccionario incompatible;
- error de memoria;
- bitmap inválido;
- región degenerada;
- error de inferencia;
- sesión cerrada;
- procesamiento cancelado;
- resultado antiguo;
- imagen sin texto;
- texto reconocido vacío.

Cada error deberá transformarse en un resultado controlado.

---

## 25. Concurrencia

El OCR deberá ejecutarse fuera del hilo principal.

Se deberá garantizar:

- una inicialización controlada;
- una inferencia documental activa por ViewModel;
- ausencia de inferencias duplicadas tras rotación;
- ignorar callbacks obsoletos;
- no reutilizar tensores cerrados;
- no cerrar sesiones mientras están en uso;
- no acumular bitmaps;
- no publicar estados desde el hilo incorrecto.

Se podrá usar el executor existente solo si su carga y responsabilidad son compatibles.

Si el OCR requiere un executor dedicado, deberá justificarse por:

- aislamiento de trabajo intensivo;
- control de hilos;
- cierre explícito;
- prevención de bloqueo de operaciones Room.

---

## 26. Memoria

La versión deberá evitar:

- decodificar imágenes completas innecesariamente;
- conservar bitmap original y múltiples copias;
- crear tensores repetidos sin liberar;
- mantener todos los recortes simultáneamente;
- almacenar salidas grandes después de decodificar;
- retener referencias a Activities;
- recrear sesiones repetidamente.

Se deberán medir al menos:

- memoria antes de procesar;
- pico aproximado;
- memoria después de liberar;
- comportamiento con imágenes grandes;
- comportamiento con varias regiones.

No se prometerá un consumo concreto sin mediciones.

---

## 27. Rendimiento

Las mediciones deberán separar:

```text
carga de modelos
detección
posprocesamiento
reconocimiento total
reconstrucción
tiempo completo
```

Se recomienda registrar métricas solo en builds de desarrollo o pruebas.

La release no deberá mostrar información técnica al usuario.

Se deberán comparar:

- primera ejecución;
- ejecuciones posteriores;
- imágenes pequeñas;
- imágenes grandes;
- pocas regiones;
- muchas regiones;
- una columna;
- dos columnas.

---

## 28. Tamaño de la aplicación

La evaluación deberá medir:

- tamaño de `PP-OCRv5_mobile_det`;
- tamaño de `PP-OCRv5_mobile_rec`;
- tamaño del diccionario;
- impacto de ONNX Runtime;
- tamaño por ABI;
- tamaño de APK universal;
- tamaño de APK divididas;
- compresión real en el artefacto;
- espacio instalado aproximado.

No se considerará el tamaño como único criterio de decisión.

La calidad, estabilidad y funcionamiento offline tendrán mayor prioridad.

---

## 29. Compatibilidad ABI

El proyecto genera artefactos para:

```text
arm64-v8a
armeabi-v7a
x86_64
```

La integración deberá comprobar que ONNX Runtime y sus bibliotecas nativas sean compatibles con las ABI publicadas.

Se deberá verificar:

- compilación;
- empaquetado;
- instalación;
- carga de biblioteca nativa;
- inferencia;
- tamaño de cada APK.

No deberá publicarse una APK de una ABI que compile pero falle al cargar el runtime.

---

## 30. Corpus de evaluación

Se preparará un conjunto local de imágenes de prueba sin datos privados de terceros.

El corpus deberá incluir:

- listas impresas claras;
- capturas de pantalla;
- fotografías;
- iluminación irregular;
- inclinación moderada;
- una columna;
- dos columnas;
- tres o más columnas;
- imágenes con orientación incorrecta;
- referencias especiales con extensiones separadas por guion;
- unidades abreviadas como `P`;
- indicadores de destino como `①`, `②` o equivalentes;
- códigos con tres cifras;
- códigos con cuatro cifras;
- códigos con cinco cifras;
- códigos con letra final;
- ceros iniciales;
- cantidades;
- unidades;
- referencias no encontradas;
- falsos positivos;
- texto irrelevante.

Las imágenes no deberán incorporarse al repositorio público si contienen información sensible o si su licencia no lo permite.

---

## 31. Métricas de calidad

La evaluación deberá registrar al menos:

### Texto

- líneas detectadas;
- líneas reconocidas;
- texto esperado;
- texto obtenido;
- errores de caracteres.

### Referencias

- referencias esperadas;
- referencias propuestas;
- coincidencias correctas;
- referencias omitidas;
- falsos positivos;
- categoría correcta;
- código correcto;
- ceros iniciales conservados;
- sufijo correcto.

### Datos documentales

- cantidades correctas;
- unidades correctas;
- asociación correcta con referencia.

### Rendimiento

- tiempo total;
- tiempo de detección;
- tiempo de reconocimiento;
- memoria aproximada;
- tamaño de modelos;
- tamaño de APK.

---

## 32. Comparación reproducible

La comparación deberá utilizar:

- mismas imágenes;
- mismo dispositivo cuando sea posible;
- mismo preprocesamiento cuando resulte comparable;
- mismas reglas de parser;
- mismos criterios de éxito;
- resultados registrados;
- ausencia de ajustes manuales diferentes por motor.

No se concluirá que una solución es mejor basándose únicamente en una imagen favorable.

---

## 33. Privacidad

La versión deberá mantener:

- procesamiento completamente local;
- ausencia de permiso de Internet por necesidad OCR;
- ausencia de subida de imágenes;
- ausencia de telemetría de contenido;
- eliminación de temporales propios;
- no persistencia del texto completo;
- no inclusión de datos reales en pruebas públicas;
- no registro de documentos completos en logs.

---

## 34. Accesibilidad

La evolución técnica no deberá degradar:

- descripción de acciones;
- estados de carga;
- anuncios de progreso;
- mensajes de error;
- acción Reintentar;
- revisión de referencias;
- tamaños táctiles;
- contraste;
- soporte de texto ampliado;
- navegación por teclado;
- orden de foco.

La precisión del OCR no sustituye una interfaz revisable y accesible.

---

## 35. Historias de usuario propuestas

La versión se dividirá en las siguientes historias.

Los nombres y límites podrán ajustarse después de inspeccionar cada implementación, pero no deberán mezclarse varias responsabilidades críticas en un único cambio.

### HU-33 — Preparar ONNX Runtime y los recursos PP-OCRv5

Como usuario, quiero que la aplicación disponga de una base local y estable para ejecutar los modelos OCR.

Incluye:

- dependencia ONNX Runtime;
- modelos móviles;
- diccionario;
- carga local;
- validación;
- sesiones;
- errores de inicialización;
- pruebas de carga;
- compatibilidad ABI;
- medición inicial de tamaño.

No incluirá todavía procesamiento funcional de documentos.

---

### HU-34 — Detectar regiones de texto con PP-OCRv5

Como usuario, quiero que la aplicación detecte correctamente las zonas de texto de una lista.

Incluye:

- preprocesamiento del detector;
- inferencia;
- posprocesamiento;
- cajas;
- confianza;
- restauración de coordenadas;
- orden provisional;
- pruebas con imágenes.

No incluirá reconocimiento del contenido.

**Dependencia:** HU-33.

---

### HU-35 — Reconocer el contenido de las regiones detectadas

Como usuario, quiero que la aplicación convierta las regiones detectadas en texto legible.

Incluye:

- recorte;
- perspectiva;
- orientación de regiones;
- preprocesamiento del reconocedor;
- inferencia;
- diccionario;
- decodificación;
- confianza;
- pruebas de palabras y referencias.

**Dependencias:** HU-33 y HU-34.

---

### HU-36 — Construir el pipeline documental PP-OCRv5

Como usuario, quiero procesar una imagen completa mediante detección y reconocimiento local.

Incluye:

- coordinación detector–reconocedor;
- orden de regiones;
- resultado `RecognizedDocument`;
- estados;
- cancelación lógica;
- errores;
- integración con ViewModel;
- funcionamiento offline.

**Dependencias:** HU-34 y HU-35.

---

### HU-37 — Integrar PP-OCRv5 con la reconstrucción y revisión de listas

Como usuario, quiero revisar las referencias obtenidas mediante el nuevo procesamiento dentro del flujo existente.

Incluye:

- filas;
- una y dos columnas;
- parser;
- referencias;
- título opcional;
- cantidad;
- unidad;
- coincidencias;
- corrección manual;
- navegación vigente;
- historial documental.

**Dependencia:** HU-36.

---

### HU-38 — Corregir la orientación de imágenes antes del OCR

Como usuario, quiero girar una imagen cuando su orientación no permita leer correctamente la lista.

Incluirá:

- lectura y aplicación de orientación EXIF cuando corresponda;
- acciones para girar 90 grados a la izquierda o a la derecha;
- previsualización actualizada;
- sustitución segura del bitmap de trabajo;
- nueva ejecución del OCR con la orientación seleccionada;
- conservación del funcionamiento con cámara y Photo Picker;
- liberación de bitmaps anteriores;
- accesibilidad de las acciones;
- pruebas unitarias, instrumentadas y manuales.

No deberá depender de una detección automática infalible de la orientación.

**Dependencia:** HU-37.

---

### HU-39 — Reconstruir listas de varias columnas

Como usuario, quiero procesar listas que distribuyen referencias en más de dos columnas.

Incluirá:

- detección de una cantidad variable de columnas;
- agrupación espacial estable;
- reconstrucción independiente por columna;
- títulos o líneas globales;
- lectura de cada columna de arriba abajo;
- orden completo de izquierda a derecha;
- tolerancia a columnas con diferente número de filas;
- prevención de mezcla entre columnas;
- pruebas con tres y cuatro columnas;
- degradación segura a revisión manual cuando la estructura sea ambigua.

No deberá limitarse a una constante rígida de cuatro columnas.

**Dependencia:** HU-38.

---

### HU-40 — Interpretar referencias especiales y destinos documentales

Como usuario, quiero revisar listas que contienen referencias especiales, unidades abreviadas y uno o varios destinos.

Incluirá:

- referencias conocidas con extensiones separadas por guion;
- resolución mediante Room sin reglas hardcodeadas para una familia concreta;
- preferencia por la referencia conocida más larga al inicio de la línea;
- conservación de referencias desconocidas para revisión manual;
- cantidad documental posterior a la identidad;
- unidad abreviada `P` cuando aparezca en el documento;
- indicadores de destino como `①`, `②`, `③` o equivalentes;
- varios destinos asociados a una referencia;
- separación entre identidad, cantidad, unidad y destino;
- transporte de los datos documentales necesarios;
- revisión y corrección manual;
- pruebas con referencias externas y pedidos para varias tiendas.

Los destinos no deberán cambiar el sitio almacenado, la ubicación ni el stock.

**Dependencia:** HU-39.

---

### HU-41 — Evaluar precisión y comportamiento del OCR

Como usuario, quiero que el reconocimiento utilizado haya sido evaluado de forma reproducible.

Incluirá:

- corpus;
- métricas;
- referencias esperadas;
- falsos positivos;
- omisiones;
- títulos;
- cantidades;
- unidades;
- destinos;
- una, dos y varias columnas;
- imágenes con orientación corregida;
- referencias especiales;
- informe comparativo interno.

**Dependencia:** HU-40.

---

### HU-42 — Optimizar rendimiento, memoria y estabilidad

Como usuario, quiero procesar listas sin bloqueos ni consumo descontrolado de recursos.

Incluirá:

- sesiones reutilizables;
- hilos;
- bitmaps;
- tensores;
- cierre;
- imágenes grandes;
- tiempos;
- memoria;
- errores;
- pruebas prolongadas;
- ABI.

**Dependencias:** HU-36 y HU-41.

---

### HU-43 — Consolidar la integración OCR y preparar v1.4.0

Como usuario, quiero una versión estable del procesamiento de listas mejorado.

Incluirá:

- integración final;
- textos de interfaz;
- documentación;
- pruebas regresivas;
- verificación offline;
- APK por ABI;
- CI;
- actualización de README;
- preparación de release.

**Dependencia:** HU-42.


## 36. Orden recomendado

```text
HU-33
    ↓
HU-34
    ↓
HU-35
    ↓
HU-36
    ↓
HU-37
    ↓
HU-38
    ↓
HU-39
    ↓
HU-40
    ↓
HU-41
    ↓
HU-42
    ↓
HU-43
```

Motivo:

- primero se validará que los modelos puedan cargarse;
- después se implementarán detección y reconocimiento por separado;
- luego se construirá el pipeline completo;
- posteriormente se integrará con el dominio y la revisión existentes;
- antes de medir precisión se corregirá la orientación visible de las imágenes;
- después se ampliará la reconstrucción a varias columnas;
- a continuación se interpretarán referencias especiales y destinos documentales;
- finalmente se evaluará, optimizará y consolidará la versión.

No se deberá medir como fallo de reconocimiento una imagen cuya orientación visible todavía no haya sido corregida.

Tampoco se deberá optimizar el pipeline antes de definir el comportamiento funcional de varias columnas y referencias especiales.


## 37. Estrategia de pruebas

### Unitarias

- configuración;
- validación de modelos;
- cálculo de tamaños;
- normalización;
- construcción de tensores;
- proyección de coordenadas;
- filtrado de regiones;
- orden;
- decodificación;
- confianza;
- diccionario;
- resultados vacíos;
- errores.

### Instrumentadas

- carga de modelos desde assets;
- inicialización de ONNX Runtime;
- inferencia real;
- compatibilidad ABI;
- imágenes pequeñas;
- imágenes grandes;
- memoria;
- cierre de sesiones;
- rotación;
- recreación de pantalla;
- integración con Photo Picker;
- integración con cámara.

### Regresión

- gestión de mercadería;
- CSV;
- escáner individual;
- procesamiento de listas;
- revisión;
- ubicaciones;
- historial;
- búsqueda;
- filtros;
- eliminación;
- migración Room;
- funcionamiento offline.

### Manuales

- una columna;
- dos columnas;
- tres y cuatro columnas;
- fotografía girada 90, 180 y 270 grados;
- fotografía inclinada;
- captura de pantalla;
- baja iluminación;
- texto pequeño;
- códigos con ceros;
- letra final;
- cantidad y unidad;
- imagen sin texto;
- modelo no disponible;
- referencias especiales con guion;
- unidad abreviada `P`;
- indicadores de destino simples y múltiples;
- varias ejecuciones consecutivas.

---

## 38. CI

La CI deberá continuar ejecutando:

```text
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Además, deberá comprobar:

- inclusión de modelos;
- disponibilidad del diccionario;
- compilación de ONNX Runtime;
- empaquetado por ABI;
- pruebas unitarias del pipeline;
- ausencia de archivos sensibles;
- tamaño de artefactos cuando se defina un control.

Las pruebas de inferencia real deberán diseñarse para no hacer la CI excesivamente lenta o inestable.

---

## 39. Criterios de aceptación de la versión

La versión se considerará funcionalmente aceptable cuando:

- los modelos carguen localmente;
- la detección produzca regiones válidas;
- el reconocimiento produzca texto;
- el pipeline entregue `RecognizedDocument`;
- el orden de lectura sea estable;
- se mantengan una y dos columnas;
- se puedan corregir manualmente imágenes con orientación incorrecta;
- se puedan reconstruir varias columnas con orden estable;
- las referencias especiales y los destinos documentales permanezcan revisables;
- se reconozcan referencias utilizables;
- se conserven ceros iniciales;
- se mantenga la revisión manual;
- se integren cantidad y unidad;
- se controle imagen sin texto;
- se controlen errores;
- no se bloquee el hilo principal;
- no existan fugas evidentes de recursos;
- funcione sin conexión;
- las funcionalidades de v1.3 permanezcan operativas;
- las APK soportadas carguen el runtime correctamente;
- existan mediciones reproducibles.

---

## 40. Riesgos

### Modelos incompatibles

**Riesgo:** entradas, salidas o operadores no compatibles.

**Mitigación:** validar modelos y ejecutar una prueba mínima antes de construir el pipeline.

### Posprocesamiento incorrecto

**Riesgo:** el detector produce mapas válidos, pero las cajas no representan el texto.

**Mitigación:** pruebas con coordenadas conocidas y visualización técnica en desarrollo.

### Diccionario incompatible

**Riesgo:** texto ilegible aunque la inferencia funcione.

**Mitigación:** validar clases, tokens y orden del diccionario.

### Consumo de memoria

**Riesgo:** bitmaps, recortes y tensores provocan cierre de la aplicación.

**Mitigación:** procesamiento secuencial, escalado, cierre y medición.

### Tiempo excesivo

**Riesgo:** el procesamiento deja de ser práctico.

**Mitigación:** medir por etapa y optimizar después de obtener un baseline correcto.

### APK demasiado grande

**Riesgo:** modelos y runtime aumentan considerablemente los artefactos.

**Mitigación:** modelos móviles, splits ABI y medición real.

### Precisión insuficiente

**Riesgo:** el nuevo pipeline no mejora las listas objetivo.

**Mitigación:** corpus representativo, métricas y revisión obligatoria.

### Regresión del flujo

**Riesgo:** se rompe captura, revisión, ubicación o historial.

**Mitigación:** mantener contratos y pruebas regresivas.

### Dependencia excesiva de ONNX en la UI

**Riesgo:** Activities o ViewModels quedan acoplados al runtime.

**Mitigación:** adaptar resultados mediante `DocumentTextRecognizer`.

### Conclusión prematura

**Riesgo:** decidir basándose en pocas imágenes.

**Mitigación:** comparación reproducible y criterios definidos.

---

## 41. Fuera del objetivo de v1.4

```text
stock
pedidos
reservas
usuarios
roles
servidor
sincronización
OCR remoto
entrenamiento de modelos
varias páginas
PDF
documentos arbitrarios
prefijos de tres letras
```

---

## 42. Definición de terminado

La versión estará terminada cuando:

- ONNX Runtime esté integrado;
- los modelos móviles estén disponibles localmente;
- el detector funcione;
- el reconocedor funcione;
- exista un pipeline documental completo;
- el resultado use los contratos existentes;
- la reconstrucción de filas y columnas funcione;
- exista corrección manual de orientación previa al OCR;
- la reconstrucción admita varias columnas;
- las referencias especiales y destinos documentales puedan revisarse;
- la revisión manual permanezca;
- las referencias puedan corregirse;
- cantidad y unidad continúen siendo documentales;
- se controlen errores;
- se mida precisión;
- se mida rendimiento;
- se mida memoria;
- se mida tamaño;
- se verifiquen las ABI publicadas;
- no se modifique mercadería automáticamente;
- no se gestione stock;
- funcione sin conexión;
- las funcionalidades anteriores continúen operativas;
- pruebas, lint, build y CI sean satisfactorios;
- se publique `v1.4.0`.

---

## 43. Resultado esperado

```text
fotografía o imagen
        ↓
PP-OCRv5 local
        ↓
detección y reconocimiento
        ↓
reconstrucción documental
        ↓
referencias y datos propuestos
        ↓
revisión
        ↓
ubicaciones e historial
```

La versión deberá aportar una mejora medible del procesamiento de listas sin perder:

```text
privacidad
funcionamiento offline
control del usuario
estabilidad
separación arquitectónica
```

La siguiente evolución prevista será:

```text
v1.5.0 — Historial de ubicaciones, estados y categorías configurables
```
