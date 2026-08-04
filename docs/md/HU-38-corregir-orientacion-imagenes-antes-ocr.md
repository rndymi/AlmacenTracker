# HU-38 — Corregir la orientación de imágenes antes del OCR

> Sexta historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-38  
**Nombre:** Corregir la orientación de imágenes antes del OCR  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-38-corregir-orientacion-imagen`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-37 — Integrar PP-OCRv5 con la reconstrucción y revisión de listas  
**Issue prevista:** `#42`

---

## 2. Historia de usuario

Como usuario,  
quiero girar una fotografía o imagen seleccionada cuando la lista no se vea en la orientación correcta,  
para ejecutar el OCR sobre un documento legible sin tener que editar el archivo fuera de AlmacenTracker.

---

## 3. Objetivo

Permitir corregir manualmente la orientación de la imagen seleccionada antes de ejecutar PP-OCRv5.

Flujo previsto:

```text
cámara o Photo Picker
        ↓
lectura de orientación EXIF
        ↓
previsualización corregida inicialmente
        ↓
girar 90° a la izquierda o derecha
        ↓
previsualización actualizada
        ↓
procesar imagen
        ↓
EXIF + giro manual
        ↓
PP-OCRv5
        ↓
resultado revisable
```

También deberá permitirse corregir una imagen después de un resultado insatisfactorio:

```text
resultado OCR / sin texto / error
        ↓
girar imagen
        ↓
invalidar resultado anterior
        ↓
volver al estado de imagen preparada
        ↓
ejecutar nuevamente el OCR
```

HU-38 no deberá depender de una clasificación automática infalible de la orientación.

---

## 4. Regla principal

> La orientación EXIF y el giro manual son transformaciones distintas y acumulables.

La orientación efectiva será:

```text
orientación efectiva
=
orientación EXIF
+
giro manual elegido por el usuario
```

Ejemplo:

```text
EXIF = 90°
giro manual = 270°
orientación efectiva = 0°
```

La rotación deberá normalizarse siempre a:

```text
0°
90°
180°
270°
```

El giro manual no deberá modificar el archivo original.

---

## 5. Base documental y arquitectónica

HU-38 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-37-integrar-ppocrv5-reconstruccion-revision-listas.md`;
- el estado real de `AlmacenTrackerHU37.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- `DocumentImageProcessor` como límite del procesamiento técnico;
- `DocumentImageLoader` como cargador de previsualización;
- `ReferenceListCaptureViewModel` como propietario del estado de captura;
- la lectura EXIF ya existente;
- PP-OCRv5 como motor documental activo;
- el funcionamiento offline;
- la liberación explícita de bitmaps;
- la cancelación lógica de resultados obsoletos;
- la revisión manual obligatoria;
- la política de no introducir capas sin responsabilidad real.

El plan de v1.4 asigna a HU-38:

```text
EXIF
+
giro manual de 90°
+
previsualización
+
bitmap de trabajo
+
reprocesamiento
+
cámara y Photo Picker
+
liberación de bitmaps
+
accesibilidad
+
pruebas
```

---

## 6. Estado real antes de HU-38

El análisis de `AlmacenTrackerHU37.zip` confirma:

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
merge HU37 #41 into develop
```

HU-37 ya está integrada con PP-OCRv5 y conserva:

- reconstrucción de filas;
- una y dos columnas;
- extracción de referencias;
- títulos opcionales;
- cantidades y unidades;
- coincidencias con Room;
- corrección manual;
- ubicaciones;
- preparación histórica.

La captura documental continúa centralizada en:

```text
feature/reference_list/capture/
├── ReferenceListCaptureActivity.java
├── ReferenceListCaptureUiState.java
├── ReferenceListCaptureViewModel.java
└── ReferenceListCaptureViewModelFactory.java
```

---

## 7. Estado real del procesamiento de imagen

`AndroidDocumentImageProcessor` ya:

- recibe la URI;
- lee dimensiones;
- lee EXIF;
- calcula una rotación inicial;
- decodifica con muestreo;
- aplica la rotación EXIF;
- limita el lado máximo a `2200`;
- genera un bitmap `ARGB_8888`;
- aplica escala de grises y contraste;
- entrega `AndroidDocumentImage`;
- ejecuta fuera del hilo principal;
- recicla bitmaps temporales;
- se cierra de forma idempotente.

La rotación actual procede exclusivamente de EXIF:

```text
0°
90°
180°
270°
```

Antes de HU-38, `DocumentImageProcessor` no recibe una rotación manual.

---

## 8. Estado real de la previsualización

`AndroidDocumentImageLoader` ya:

- abre la URI;
- lee dimensiones;
- decodifica una versión reducida;
- lee orientación EXIF;
- aplica la rotación EXIF;
- devuelve un `Bitmap` de previsualización.

`ReferenceListCaptureActivity` ya:

- carga la previsualización fuera del hilo principal;
- muestra cámara o Photo Picker;
- conserva una URI seleccionada;
- elimina archivos temporales de cámara;
- recicla la previsualización anterior cuando corresponde;
- evita renderizados obsoletos mediante la URI mostrada.

Antes de HU-38 no existen:

- botones para girar;
- rotación manual en estado;
- previsualización manualmente rotada;
- reprocesamiento con orientación elegida;
- invalidación del resultado al girar.

---

## 9. Problema que HU-38 debe resolver

EXIF no garantiza que la imagen se vea correctamente.

Casos reales:

- captura de pantalla sin EXIF;
- imagen descargada;
- aplicación que eliminó metadatos;
- fotografía guardada con píxeles ya rotados;
- orientación EXIF incorrecta;
- cámara o proveedor que entrega una orientación inesperada;
- documento fotografiado con el dispositivo inclinado lateralmente.

En estos casos, PP-OCRv5 puede recibir:

```text
texto girado 90°
texto girado 180°
texto girado 270°
```

y producir:

- ninguna región;
- texto incoherente;
- referencias omitidas;
- orden incorrecto;
- falso error de reconocimiento.

HU-38 debe permitir corregir la entrada antes de considerar estos casos fallos del OCR.

---

## 10. Alcance incluido

HU-38 incluye:

- conservar la lectura EXIF actual;
- representar un giro manual independiente;
- admitir `0`, `90`, `180` y `270` grados;
- girar 90 grados a la izquierda;
- girar 90 grados a la derecha;
- mostrar acciones junto a la previsualización;
- actualizar la previsualización;
- conservar la URI original;
- conservar la fuente de imagen;
- conservar el giro tras rotación de pantalla;
- aplicar el giro manual al bitmap enviado al OCR;
- combinar EXIF y giro manual;
- normalizar la orientación efectiva;
- invalidar un resultado OCR anterior al girar;
- invalidar estados `NO_TEXT` y error al girar;
- volver al estado de imagen preparada;
- permitir ejecutar otra vez el OCR;
- impedir giros mientras exista procesamiento activo;
- impedir procesamientos simultáneos;
- mantener cancelación lógica;
- mantener cámara;
- mantener Photo Picker;
- mantener archivos temporales;
- no escribir una copia rotada permanente;
- no modificar el archivo original;
- reciclar previsualizaciones anteriores;
- reciclar bitmaps de trabajo temporales;
- conservar cierre de `DocumentImage`;
- mensajes y descripciones accesibles;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas manuales;
- regresión;
- CI.

---

## 11. Alcance excluido

HU-38 no incluye:

- detectar automáticamente la orientación mediante IA;
- ejecutar cuatro inferencias y elegir una;
- girar en grados arbitrarios;
- enderezar inclinaciones pequeñas;
- corregir perspectiva manualmente;
- recortar la imagen;
- ajustar brillo;
- ajustar contraste desde la interfaz;
- dibujar sobre la imagen;
- guardar una copia rotada en galería;
- sobrescribir la imagen seleccionada;
- modificar EXIF del archivo;
- reconstruir tres o más columnas;
- modificar referencias especiales;
- interpretar destinos documentales;
- medir todavía precisión global;
- crear corpus comparativo definitivo;
- optimizar hilos;
- reducir tamaño de APK;
- eliminar ML Kit;
- modificar Room;
- modificar mercadería;
- gestionar stock;
- almacenar fotografías;
- procesar PDF;
- procesar varias páginas.

La reconstrucción de varias columnas corresponde a HU-39.

Las referencias especiales y destinos corresponden a HU-40.

La evaluación corresponde a HU-41.

---

## 12. Decisión crítica sobre la fuente de verdad

La fuente de verdad de la orientación manual deberá estar en:

```text
ReferenceListCaptureUiState
```

No deberá residir únicamente en:

```text
ImageView.setRotation(...)
```

Motivos:

- `ImageView` se recrea;
- la Activity puede rotar;
- el OCR necesita conocer la misma orientación;
- una transformación visual aislada no modifica el bitmap procesado;
- la previsualización y la inferencia podrían divergir.

La UI renderizará el giro contenido en el estado.

---

## 13. Modelo de rotación manual

Se recomienda representar:

```text
manualRotationDegrees
```

como `int` normalizado.

Valores válidos:

```text
0
90
180
270
```

No se necesita un enum si solo añade conversión sin comportamiento.

Sí se recomienda un componente pequeño y puro cuando concentra reglas reutilizables:

```text
DocumentImageRotation
```

Responsabilidades posibles:

- normalizar;
- girar a la izquierda;
- girar a la derecha;
- combinar rotaciones;
- validar múltiplos de 90.

No deberá depender de Android.

---

## 14. Regla de normalización

Función conceptual:

```text
normalize(rotation)
=
((rotation % 360) + 360) % 360
```

Ejemplos:

```text
-90  → 270
0    → 0
90   → 90
360  → 0
450  → 90
```

Después de normalizar, solo se aceptarán múltiplos de 90.

Una rotación de:

```text
45°
```

deberá rechazarse como argumento inválido.

---

## 15. Giro a la izquierda

Operación:

```text
manualRotation - 90°
```

Ejemplos:

```text
0°   → 270°
90°  → 0°
180° → 90°
270° → 180°
```

La etiqueta visible deberá expresar:

```text
Girar a la izquierda
```

No deberá mostrarse un nombre técnico como:

```text
rotate -90
```

---

## 16. Giro a la derecha

Operación:

```text
manualRotation + 90°
```

Ejemplos:

```text
0°   → 90°
90°  → 180°
180° → 270°
270° → 0°
```

La etiqueta visible deberá expresar:

```text
Girar a la derecha
```

---

## 17. Orientación EXIF

HU-38 conservará la lectura existente:

```text
ORIENTATION_NORMAL
ORIENTATION_ROTATE_90
ORIENTATION_ROTATE_180
ORIENTATION_ROTATE_270
ORIENTATION_TRANSPOSE
ORIENTATION_TRANSVERSE
```

La historia no deberá eliminar la corrección EXIF para sustituirla por controles manuales.

Flujo correcto:

```text
archivo
    ↓
EXIF
    ↓
bitmap inicialmente orientado
    ↓
giro manual
```

No:

```text
ignorar EXIF
    ↓
obligar al usuario a corregir siempre
```

---

## 18. Limitación de los reflejos EXIF

La implementación actual transforma orientaciones de reflejo en rotaciones aproximadas.

HU-38 no está obligada a implementar espejado horizontal o vertical.

Sin embargo, deberá conservar el comportamiento existente y documentar que:

- la orientación manual corrige giros;
- no corrige imágenes reflejadas;
- los casos de espejo no deberán presentarse como soporte garantizado.

No se ampliará el alcance salvo que una prueba real lo exija.

---

## 19. Solicitud de procesamiento

El contrato actual:

```java
process(
        String imageUri,
        DocumentImageProcessingCallback callback
)
```

ya no contiene toda la información necesaria.

Se recomienda crear un modelo inmutable:

```text
DocumentImageProcessingRequest
```

Datos:

```text
imageUri
manualRotationDegrees
```

Firma prevista:

```java
void process(
        DocumentImageProcessingRequest request,
        DocumentImageProcessingCallback callback
);
```

Motivos:

- evita añadir parámetros posicionales;
- permite evolución controlada;
- mantiene la rotación fuera de la URI;
- facilita pruebas;
- no expone Bitmap;
- representa una solicitud real.

No se deberá añadir una ruta ficticia como:

```text
uri?rotation=90
```

---

## 20. Compatibilidad del contrato

El proyecto dispone de una sola implementación funcional de:

```text
DocumentImageProcessor
```

Por tanto, cambiar la firma es asumible si:

- se actualizan dobles de prueba;
- se actualiza la factory;
- se actualiza el ViewModel;
- no se introducen sobrecargas ambiguas.

Alternativa válida:

```java
void process(
        String imageUri,
        int manualRotationDegrees,
        DocumentImageProcessingCallback callback
);
```

Sin embargo, el modelo de solicitud es preferible porque expresa mejor la operación.

No se mantendrá el método anterior si queda sin uso solo por compatibilidad interna inexistente.

---

## 21. Responsabilidad del ViewModel

`ReferenceListCaptureViewModel` deberá:

- inicializar giro manual en `0`;
- conservarlo al seleccionar una imagen;
- exponerlo mediante el estado;
- girar a izquierda;
- girar a derecha;
- impedir giro durante procesamiento;
- invalidar resultados anteriores;
- incrementar `processingRequestId`;
- volver a estado de imagen seleccionada;
- conservar URI;
- conservar fuente;
- enviar una solicitud con rotación;
- conservar rotación tras error;
- conservar rotación tras `NO_TEXT`;
- limpiar rotación al cambiar o eliminar imagen.

Métodos orientativos:

```java
public void rotateImageLeft();
public void rotateImageRight();
```

La Activity no calculará el nuevo ángulo.

---

## 22. Estado inicial al seleccionar imagen

Cuando se selecciona una nueva imagen:

```text
manualRotationDegrees = 0
```

Esto significa:

```text
sin giro adicional después de EXIF
```

No significa que el archivo se procese sin corrección EXIF.

Cambiar de imagen deberá descartar el giro de la imagen anterior.

---

## 23. Estado después de girar

Después de una acción de giro:

```text
Status = IMAGE_SELECTED
recognizedDocument = null
rawTextExpanded = false
error anterior = descartado
```

Se conservarán:

```text
imageUri
imageSource
manualRotationDegrees
```

La UI deberá volver a mostrar:

```text
Procesar imagen
```

No deberá conservar visible un resultado obtenido con otra orientación.

---

## 24. Giro después de resultado reconocido

Caso:

```text
TEXT_RECOGNIZED
        ↓ girar
IMAGE_SELECTED
        ↓ procesar
PROCESSING
        ↓
nuevo resultado
```

El resultado anterior deberá invalidarse inmediatamente.

No se deberán mezclar:

- líneas del resultado anterior;
- título detectado anterior;
- referencias anteriores;
- nueva orientación.

La revisión no se abrirá con datos obsoletos.

---

## 25. Giro después de `NO_TEXT`

Caso:

```text
NO_TEXT
    ↓ girar
IMAGE_SELECTED
```

Esto permitirá corregir un documento que fue procesado de lado.

El mensaje de “no se encontró texto” deberá ocultarse al aplicar el giro.

---

## 26. Giro después de error

Los estados:

```text
IMAGE_ERROR
RECOGNITION_ERROR
```

podrán volver a:

```text
IMAGE_SELECTED
```

cuando el usuario gire.

Observación crítica:

- un giro puede resolver un reconocimiento fallido;
- no puede resolver una URI ilegible;
- aun así, mantener una transición uniforme simplifica la recuperación.

Si la imagen no puede abrirse, el siguiente intento volverá a informar el error correspondiente.

---

## 27. Giro durante procesamiento

Mientras:

```text
processing == true
```

las acciones de giro deberán estar deshabilitadas.

No se deberá:

- modificar el estado visual;
- iniciar otra carga;
- reciclar el bitmap activo;
- cambiar la solicitud ya enviada.

El usuario podrá girar después de finalizar o cambiar de imagen según las acciones existentes.

HU-38 no añade cancelación física de una inferencia ONNX activa.

---

## 28. Prevención de resultados obsoletos

Cada giro deberá:

```text
processingRequestId++
```

cuando no exista procesamiento activo.

Esto invalida cualquier callback tardío que pudiera corresponder a un render o solicitud anterior.

El control existente mediante id deberá mantenerse.

No se crearán callbacks de orientación separados si el estado ya resuelve la invalidez.

---

## 29. Estado de interfaz

`ReferenceListCaptureUiState` deberá añadir:

```text
manualRotationDegrees
```

Todos los factories deberán definirlo explícitamente:

```text
empty
imageSelected
processing
textRecognized
noTextFound
imageError
recognitionError
```

Reglas:

- `EMPTY` utiliza `0`;
- estados con imagen conservan el giro;
- `PROCESSING` conserva el giro enviado;
- éxito y errores conservan el giro usado;
- `withRawTextExpanded(...)` conserva el giro;
- no se aceptan valores inválidos.

---

## 30. Operaciones del estado

Se podrán añadir métodos:

```java
getManualRotationDegrees()
canRotateImage()
withManualRotationDegrees(...)
```

`canRotateImage()` deberá ser verdadero cuando:

- existe imagen;
- no se está procesando.

Podrá ser verdadero en:

```text
IMAGE_SELECTED
TEXT_RECOGNIZED
NO_TEXT
IMAGE_ERROR
RECOGNITION_ERROR
```

No será verdadero en:

```text
EMPTY
PROCESSING
```

---

## 31. Previsualización

La previsualización debe reflejar:

```text
EXIF + giro manual
```

La imagen mostrada debe coincidir con la orientación enviada posteriormente al OCR.

No es aceptable:

```text
preview correcta
OCR sin giro
```

ni:

```text
preview sin giro
OCR rotado
```

La coherencia visual es un criterio funcional.

---

## 32. Estrategia de previsualización

Se recomienda ampliar:

```text
DocumentImageLoader
```

para recibir el giro manual:

```java
T loadPreview(
        String imageUri,
        int targetSize,
        int manualRotationDegrees
);
```

`AndroidDocumentImageLoader` deberá:

1. decodificar la vista reducida;
2. aplicar EXIF;
3. aplicar giro manual;
4. reciclar el bitmap reemplazado;
5. devolver el bitmap final.

Ventajas:

- la previsualización utiliza píxeles realmente rotados;
- evita recortes de una transformación visual del `ImageView`;
- permite probar dimensiones;
- mantiene la lógica Android fuera de la Activity.

---

## 33. Alternativa visual con `ImageView.setRotation`

No se recomienda como solución principal.

Problemas:

- el layout conserva dimensiones anteriores;
- una imagen a 90° puede quedar reducida o recortada;
- el OCR utiliza otro bitmap;
- obliga a duplicar reglas;
- puede complicar accesibilidad y pruebas;
- no demuestra que la transformación técnica sea la misma.

Puede utilizarse solo como animación temporal, nunca como fuente de verdad.

---

## 34. Carga asíncrona de preview

La Activity ya utiliza:

```text
previewExecutor
```

La recarga por giro deberá continuar fuera del hilo principal.

Cada solicitud de preview deberá identificar:

```text
imageUri
manualRotationDegrees
```

Antes de aplicar el bitmap, deberá comprobar que ambos siguen coincidiendo con el estado actual.

No basta con comparar únicamente la URI.

---

## 35. Clave de renderizado

Se recomienda reemplazar:

```text
renderedImageUri
```

por una clave conceptual:

```text
PreviewRenderKey
├── imageUri
└── manualRotationDegrees
```

Puede implementarse sin crear una clase si dos campos claros son suficientes:

```text
renderedImageUri
renderedManualRotationDegrees
```

No se creará una clase solo por simetría.

---

## 36. Resultado obsoleto de preview

Caso:

```text
solicitud preview 0°
solicitud preview 90°
callback preview 0° llega después
```

La Activity deberá descartar y reciclar el bitmap de `0°`.

Solo podrá mostrar:

```text
URI actual + giro actual
```

Esto evita que la previsualización retroceda visualmente.

---

## 37. Propiedad del bitmap de preview

El bitmap devuelto por `DocumentImageLoader` pasa temporalmente a la Activity.

Cuando se sustituye:

- obtener el bitmap anterior del `ImageView` cuando sea propio;
- retirar el drawable;
- reciclar el anterior;
- asignar el nuevo.

Cuando se descarta un callback obsoleto:

- reciclar el bitmap recibido.

Cuando la Activity se destruye:

- reciclar preview activo;
- cerrar `previewExecutor` según el ciclo existente.

No se deberá reciclar un bitmap mientras siga dibujándose.

---

## 38. Procesamiento OCR

`AndroidDocumentImageProcessor` deberá recibir:

```text
manualRotationDegrees
```

Después de leer EXIF:

```text
effectiveRotation =
normalize(
    exifRotation
    +
    manualRotationDegrees
)
```

Podrá aplicarse:

- en una única transformación; o
- EXIF primero y giro manual después.

El resultado visible deberá ser equivalente.

---

## 39. Orden recomendado de transformaciones

Flujo recomendado:

```text
decode
    ↓
rotate(exif + manual)
    ↓
scaleToMaximumSide
    ↓
grayscale + contrast
    ↓
AndroidDocumentImage
```

Ventajas:

- el límite de lado se calcula sobre orientación final;
- las dimensiones procesadas coinciden con el OCR;
- no se aplican dos interpolaciones de rotación;
- simplifica propiedad de bitmaps.

La previsualización puede seguir aplicando ambas rotaciones de forma secuencial si mantiene la equivalencia.

---

## 40. Rotación efectiva en `AndroidDocumentImage`

`AndroidDocumentImage.getAppliedRotation()` deberá representar:

```text
rotación efectiva total aplicada
```

No únicamente EXIF.

Ejemplo:

```text
EXIF 90 + manual 90
→ appliedRotation = 180
```

Esto permite:

- diagnóstico;
- pruebas;
- coherencia del modelo;
- futura evaluación de HU-41.

No se añadirá un segundo getter si no existe una necesidad real de distinguir ambos valores después del procesamiento.

---

## 41. Dimensiones originales

`originalWidth` y `originalHeight` deberán continuar representando:

```text
dimensiones codificadas del archivo antes de rotar
```

`processedWidth` y `processedHeight` representarán:

```text
bitmap final entregado al OCR
```

Para giros de 90° o 270°:

```text
processedWidth y processedHeight intercambian orientación
```

sin alterar el significado de las dimensiones originales.

---

## 42. Sustitución segura del bitmap

Cuando una rotación crea un nuevo bitmap:

```text
rotated != source
```

el bitmap anterior deberá reciclarse solo después de crear correctamente el nuevo.

Ante error:

- reciclar bitmap nuevo parcial;
- reciclar bitmap fuente cuando sea propiedad del procesador;
- entregar un único callback;
- no conservar referencias.

No se deberá reciclar el bitmap original antes de completar `Bitmap.createBitmap(...)`.

---

## 43. Error de memoria

Rotar una imagen puede elevar temporalmente el uso de memoria.

HU-38 deberá conservar tratamiento de:

```text
OutOfMemoryError
```

como error controlado de procesamiento.

La mitigación principal seguirá siendo:

- decodificación con muestreo;
- límite de lado;
- una sola previsualización;
- reciclado temprano;
- ausencia de copias permanentes.

No se deberá mantener simultáneamente una cadena innecesaria de bitmaps rotados.

---

## 44. No crear archivos rotados

No se deberá implementar:

```text
crear PNG/JPEG temporal por cada giro
```

Motivos:

- trabajo de E/S innecesario;
- pérdida de calidad por recompresión;
- limpieza adicional;
- mayor latencia;
- posibles residuos;
- divergencia entre original y copia.

La URI original y el giro manual son suficientes para reconstruir el bitmap.

---

## 45. Cámara

El flujo de cámara deberá mantenerse:

```text
TakePicture
    ↓
archivo temporal
    ↓
FileProvider URI
    ↓
selección
    ↓
preview
    ↓
giro opcional
    ↓
OCR
```

Girar no deberá:

- eliminar `activeCapturedFile`;
- crear otro archivo;
- perder la URI;
- impedir cambiar de imagen;
- afectar la limpieza al finalizar.

---

## 46. Photo Picker

El flujo deberá mantenerse:

```text
PickVisualMedia
    ↓
content URI
    ↓
preview
    ↓
giro opcional
    ↓
OCR
```

No se solicitarán permisos generales de almacenamiento.

El giro se aplicará sobre el contenido leído mediante `ContentResolver`.

---

## 47. Cambio de imagen

Al pulsar:

```text
Cambiar imagen
```

deberán limpiarse:

- URI;
- fuente;
- giro manual;
- preview;
- resultado OCR;
- error;
- título detectado asociado;
- archivo temporal activo cuando corresponda.

El siguiente documento empezará con giro manual `0`.

---

## 48. Rotación de pantalla Android

Al recrearse la Activity:

- el ViewModel conserva URI;
- el ViewModel conserva giro;
- el ViewModel conserva estado;
- la Activity vuelve a cargar la preview;
- no se vuelve a aplicar el giro acumulándolo;
- no se inicia OCR automáticamente;
- no se pierde resultado reconocido.

La preview debe reconstruirse desde:

```text
URI original + giro manual absoluto
```

No desde el bitmap visual anterior.

---

## 49. No acumular por recreación

Error a evitar:

```text
estado = 90°
Activity recreada
preview ya rotada 90°
se rota otros 90°
resultado visual = 180°
```

La carga siempre debe partir del archivo original y aplicar:

```text
EXIF + manualRotationDegrees
```

una sola vez.

---

## 50. Reprocesamiento explícito

Girar no deberá iniciar OCR automáticamente.

Motivos:

- cada inferencia puede tardar;
- el usuario puede necesitar dos o tres giros;
- evita procesamiento innecesario;
- mantiene una acción explícita;
- permite verificar la preview antes de ejecutar.

Después de girar se mostrará:

```text
Procesar imagen
```

---

## 51. Resultado y navegación

Cuando el nuevo procesamiento finalice:

- el resultado reemplaza al anterior;
- la revisión se abre solo con el resultado nuevo;
- el título propuesto procede del nuevo documento;
- referencias, cantidades y unidades proceden del nuevo documento;
- la navegación restante no necesita conocer la rotación.

La orientación es responsabilidad del flujo de captura y procesamiento.

---

## 52. Textos de interfaz

Textos previstos:

```text
Girar a la izquierda
Girar a la derecha
Orientación de la imagen
La imagen se girará antes de procesarla.
```

No es necesario mostrar:

```text
90°
180°
270°
```

como dato principal.

Podrá mostrarse una descripción de estado si aporta accesibilidad:

```text
Orientación manual: 90 grados
```

---

## 53. Ubicación de las acciones

Las acciones deberán situarse cerca de:

```text
imagePreview
```

Estructura orientativa:

```text
PreviewCard
├── ImageView
└── RotationActions
    ├── Girar a la izquierda
    └── Girar a la derecha
```

No se ocultarán dentro de un menú distante si son necesarias para preparar la imagen.

Los botones podrán ser iconos con texto o botones compactos.

No deberán depender solo del icono.

---

## 54. Visibilidad

Los controles de giro serán visibles cuando:

```text
existe una imagen seleccionada
```

Serán ocultos cuando:

```text
Status = EMPTY
```

Serán deshabilitados cuando:

```text
Status = PROCESSING
```

Podrán seguir visibles y habilitados en:

```text
TEXT_RECOGNIZED
NO_TEXT
IMAGE_ERROR
RECOGNITION_ERROR
```

para permitir reprocesamiento.

---

## 55. Accesibilidad

Cada acción deberá incluir:

- texto visible o `contentDescription`;
- objetivo táctil mínimo de 48 dp;
- estado habilitado correcto;
- orden de foco coherente;
- compatibilidad con TalkBack;
- anuncio del cambio de orientación cuando aporte valor.

Mensajes orientativos:

```text
Imagen girada a la izquierda.
Imagen girada a la derecha.
```

No se deberá anunciar repetidamente la previsualización completa.

---

## 56. Estado de procesamiento

Durante OCR:

- controles de giro deshabilitados;
- controles de selección deshabilitados según comportamiento actual;
- preview visible;
- progreso visible;
- resultado anterior oculto cuando corresponda;
- rotación usada conservada.

La UI no deberá parpadear entre orientaciones durante el procesamiento.

---

## 57. Pruebas unitarias de rotación

Casos mínimos:

- normalizar `0`;
- normalizar `90`;
- normalizar `180`;
- normalizar `270`;
- normalizar `360`;
- normalizar `-90`;
- rechazar `45`;
- izquierda desde `0`;
- izquierda desde `270`;
- derecha desde `0`;
- derecha desde `270`;
- combinar EXIF y manual;
- cuatro giros vuelven a `0`.

---

## 58. Pruebas del estado

`ReferenceListCaptureUiStateTest` deberá comprobar:

- imagen nueva empieza en `0`;
- giro se conserva en `PROCESSING`;
- giro se conserva en éxito;
- giro se conserva en `NO_TEXT`;
- giro se conserva en error;
- `withRawTextExpanded` no cambia giro;
- `EMPTY` limpia giro;
- valor inválido se rechaza;
- `canRotateImage()` por estado.

---

## 59. Pruebas del ViewModel

`ReferenceListCaptureViewModelTest` deberá comprobar:

- girar izquierda;
- girar derecha;
- cuatro giros;
- no girar sin imagen;
- no girar durante procesamiento;
- girar invalida documento reconocido;
- girar invalida `NO_TEXT`;
- girar invalida error;
- URI se conserva;
- fuente se conserva;
- cambiar imagen reinicia giro;
- procesar envía giro correcto;
- reintentar conserva giro;
- callback obsoleto se ignora;
- rotación no dispara OCR;
- `onCleared()` mantiene cierre vigente.

---

## 60. Pruebas del procesador

`AndroidDocumentImageProcessorTest` o pruebas instrumentadas deberán comprobar:

- EXIF `0` + manual `0`;
- EXIF `90` + manual `0`;
- EXIF `0` + manual `90`;
- EXIF `90` + manual `90`;
- EXIF `270` + manual `90`;
- dimensiones tras 90°;
- dimensiones tras 180°;
- `appliedRotation`;
- bitmap no reciclado antes del callback;
- temporales reciclados;
- error de URI;
- cierre;
- solicitud después de cierre;
- rotación inválida.

---

## 61. Pruebas del loader

`AndroidDocumentImageLoader` deberá probar:

- preview sin rotación manual;
- preview a 90°;
- preview a 180°;
- preview a 270°;
- EXIF más manual;
- intercambio de dimensiones;
- bitmap anterior reciclado;
- URI inválida;
- error de memoria controlado;
- archivo de cámara;
- URI de Photo Picker.

---

## 62. Pruebas de la Activity

Casos relevantes:

- controles ocultos sin imagen;
- controles visibles con imagen;
- controles deshabilitados procesando;
- clic izquierdo delega al ViewModel;
- clic derecho delega al ViewModel;
- preview recarga al cambiar giro;
- callback viejo de preview se descarta;
- resultado OCR se oculta al girar;
- botón Procesar reaparece;
- TalkBack dispone de descripciones;
- recreación conserva orientación.

No se deberá probar la lógica matemática de rotación desde la Activity.

---

## 63. Prueba instrumentada de integración

Flujo mínimo:

```text
seleccionar imagen lateral
    ↓
preview lateral
    ↓
girar a la derecha
    ↓
preview vertical
    ↓
procesar
    ↓
PP-OCRv5
    ↓
RecognizedDocument
```

Debe comprobar:

- orientación visual correcta;
- orientación efectiva correcta;
- resultado no obsoleto;
- imagen cerrada;
- ausencia de crash.

---

## 64. Pruebas manuales

Casos mínimos:

### Cámara

- fotografía vertical correcta;
- fotografía lateral izquierda;
- fotografía lateral derecha;
- fotografía invertida;
- girar una vez;
- girar dos veces;
- girar cuatro veces;
- procesar;
- girar después del resultado;
- volver a procesar.

### Photo Picker

- imagen con EXIF correcto;
- imagen sin EXIF;
- captura de pantalla;
- imagen descargada;
- imagen lateral;
- imagen invertida;
- cambio de orientación de pantalla Android;
- cambio de imagen.

### Recuperación

- `NO_TEXT` y giro;
- error OCR y giro;
- imagen ilegible;
- salir durante carga de preview;
- salir durante OCR;
- abrir de nuevo.

---

## 65. Criterios de aceptación

### CA-01 — EXIF conservado

**Dada** una imagen con orientación EXIF válida,  
**cuando** se muestra,  
**entonces** la corrección automática existente continúa aplicándose.

### CA-02 — Giro a la izquierda

**Dada** una imagen seleccionada,  
**cuando** el usuario gira a la izquierda,  
**entonces** la orientación manual disminuye 90 grados de forma normalizada.

### CA-03 — Giro a la derecha

**Dada** una imagen seleccionada,  
**cuando** el usuario gira a la derecha,  
**entonces** la orientación manual aumenta 90 grados de forma normalizada.

### CA-04 — Previsualización coherente

**Dada** una orientación manual,  
**cuando** se renderiza la imagen,  
**entonces** la previsualización refleja EXIF y giro manual.

### CA-05 — OCR coherente

**Dada** una orientación manual,  
**cuando** se procesa la imagen,  
**entonces** PP-OCRv5 recibe el bitmap con la misma orientación mostrada.

### CA-06 — Valores válidos

**Dada** cualquier secuencia de giros,  
**entonces** el estado permanece en `0`, `90`, `180` o `270`.

### CA-07 — Cuatro giros

**Dada** una imagen,  
**cuando** se gira cuatro veces en la misma dirección,  
**entonces** vuelve a la orientación manual inicial.

### CA-08 — Resultado invalidado

**Dado** un documento reconocido,  
**cuando** el usuario gira la imagen,  
**entonces** el resultado anterior deja de estar disponible.

### CA-09 — Sin texto invalidado

**Dado** un estado `NO_TEXT`,  
**cuando** el usuario gira,  
**entonces** vuelve al estado de imagen preparada.

### CA-10 — Error invalidado

**Dado** un error anterior,  
**cuando** el usuario gira,  
**entonces** puede volver a procesar la misma URI.

### CA-11 — Sin giro durante procesamiento

**Dado** un OCR activo,  
**cuando** se renderiza la pantalla,  
**entonces** los controles de giro están deshabilitados.

### CA-12 — Reprocesamiento explícito

**Dado** un giro,  
**cuando** termina la actualización visual,  
**entonces** el OCR no se inicia hasta pulsar Procesar.

### CA-13 — Cámara

**Dada** una fotografía tomada desde la aplicación,  
**cuando** se gira,  
**entonces** conserva su archivo temporal y puede procesarse.

### CA-14 — Photo Picker

**Dada** una imagen seleccionada,  
**cuando** se gira,  
**entonces** se procesa desde la misma URI sin permisos generales.

### CA-15 — Recreación

**Dado** un giro manual,  
**cuando** la Activity se recrea,  
**entonces** conserva el ángulo sin acumular otra rotación.

### CA-16 — Cambio de imagen

**Dada** una imagen con giro manual,  
**cuando** se selecciona otra,  
**entonces** el giro vuelve a `0`.

### CA-17 — Archivo original

**Dada** cualquier rotación,  
**entonces** el archivo original no se modifica.

### CA-18 — Bitmaps

**Dada** una sustitución de preview o bitmap de trabajo,  
**entonces** los bitmaps anteriores de propiedad interna se liberan.

### CA-19 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se gira y procesa,  
**entonces** el flujo funciona localmente.

### CA-20 — Revisión manual

**Dado** el nuevo resultado OCR,  
**entonces** continúa siendo una propuesta que requiere revisión.

---

## 66. Riesgos

### Preview y OCR divergentes

**Riesgo:** la imagen se ve girada pero el OCR usa la original.

**Mitigación:** rotación en estado y solicitud de procesamiento.

### Rotación duplicada

**Riesgo:** se aplica el ángulo nuevamente después de recreación.

**Mitigación:** guardar ángulo absoluto y partir siempre de la URI original.

### Callback de preview obsoleto

**Riesgo:** una carga anterior reemplaza la orientación nueva.

**Mitigación:** validar URI y giro antes de renderizar.

### Memoria elevada

**Riesgo:** coexistencia de varios bitmaps durante un giro.

**Mitigación:** muestreo, límite de tamaño y reciclado temprano.

### Resultado OCR obsoleto

**Riesgo:** revisión abierta con líneas de otra orientación.

**Mitigación:** invalidar documento al girar.

### Giro durante inferencia

**Riesgo:** estado visual y solicitud activa no coinciden.

**Mitigación:** deshabilitar acciones mientras procesa.

### Archivo temporal eliminado

**Riesgo:** girar una captura elimina la fuente.

**Mitigación:** conservar el archivo y representar solo el giro.

### Estado demasiado complejo

**Riesgo:** crear estados separados para cada ángulo.

**Mitigación:** mantener el estado funcional actual y añadir un campo.

### Automatización excesiva

**Riesgo:** intentar detectar orientación con heurísticas frágiles.

**Mitigación:** control manual explícito.

---

## 67. Regresión

Deberán permanecer operativos:

- cámara documental;
- Photo Picker;
- previsualización;
- procesamiento PP-OCRv5;
- texto bruto;
- texto reconstruido;
- títulos;
- referencias;
- cantidades;
- unidades;
- una columna;
- dos columnas;
- revisión;
- sugerencias;
- corrección manual;
- ubicaciones;
- preparación histórica;
- historial;
- escáner individual;
- CRUD;
- CSV;
- funcionamiento offline.

---

## 68. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además deberá comprobar:

- tests de normalización de rotación;
- tests del estado;
- tests del ViewModel;
- compilación del nuevo contrato;
- pruebas del loader;
- pruebas del procesador;
- recursos de interfaz;
- APK por ABI;
- ausencia de permisos nuevos;
- ausencia de archivos de prueba privados.

Las pruebas instrumentadas con bitmaps y EXIF podrán mantenerse locales cuando la CI no disponga de emulador.

---

## 69. Definición de terminado

HU-38 estará terminada cuando:

- EXIF continúe aplicándose;
- exista giro manual a izquierda;
- exista giro manual a derecha;
- el giro use pasos de 90 grados;
- el ángulo se normalice;
- el estado conserve la orientación;
- la preview refleje la orientación;
- el OCR utilice la misma orientación;
- el resultado anterior se invalide al girar;
- `NO_TEXT` se pueda recuperar girando;
- un error se pueda reintentar tras girar;
- no se permita girar durante procesamiento;
- el OCR no se inicie automáticamente;
- cámara continúe funcionando;
- Photo Picker continúe funcionando;
- recreación conserve el giro;
- cambiar imagen limpie el giro;
- no se modifique el archivo original;
- no se creen copias rotadas permanentes;
- bitmaps temporales se liberen;
- acciones sean accesibles;
- funcione offline;
- la revisión manual se conserve;
- las pruebas unitarias sean satisfactorias;
- las pruebas instrumentadas principales sean satisfactorias;
- la regresión manual sea satisfactoria;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 70. Resultado esperado

Al cerrar HU-38:

```text
imagen
    ↓
EXIF
    ↓
giro manual revisable
    ↓
previsualización coherente
    ↓
bitmap de OCR orientado
    ↓
PP-OCRv5
    ↓
resultado revisable
```

El usuario podrá corregir una orientación incorrecta sin abandonar AlmacenTracker ni modificar el archivo original.

La siguiente historia será:

```text
HU-39 — Reconstruir listas de varias columnas
```
