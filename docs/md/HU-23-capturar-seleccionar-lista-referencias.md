# HU-23 — Capturar o seleccionar una lista de referencias

> Sexta historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-23  
**Nombre:** Capturar o seleccionar una lista de referencias  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-23-capturar-seleccionar-lista`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-18 — Escanear códigos de barras y códigos QR  
- HU-22 — Consolidar permisos y errores del escáner  

**Issue prevista:** `#26`

---

## 2. Historia de usuario

Como usuario,  
quiero tomar una fotografía o seleccionar una imagen de una lista,  
para extraer localmente el texto que contiene y prepararlo para su revisión posterior.

---

## 3. Objetivo

Crear el flujo de entrada documental de AlmacenTracker.

La HU-23 permitirá obtener una imagen mediante:

```text
tomar fotografía
        o
seleccionar imagen
```

y procesarla localmente mediante OCR:

```text
imagen
    ↓
ML Kit Text Recognition
    ↓
bloques y líneas de texto
    ↓
resultado temporal
```

Esta historia se limitará a:

- adquirir la imagen;
- validar que pueda abrirse;
- corregir su orientación cuando corresponda;
- ejecutar OCR local;
- obtener líneas de texto;
- mantener el resultado temporal para la siguiente pantalla.

HU-23 no interpretará todavía qué líneas son referencias válidas y no consultará Room.

---

## 4. Regla principal

HU-23 deberá separar claramente tres responsabilidades:

```text
adquirir imagen
        ↓
reconocer texto
        ↓
entregar líneas temporales
```

No deberá mezclar:

```text
OCR
+
reglas de categoría y código
+
consulta de ubicaciones
```

La extracción y validación de referencias pertenecerá a HU-24.

La consulta de ubicaciones pertenecerá a HU-25.

---

## 5. Referencias del proyecto

La HU-23 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-22-consolidar-permisos-errores-escaner.md`;
- el estado real de `AlmacenTrackerHU22.zip`;
- la arquitectura MVVM organizada por funcionalidades;
- procesamiento completamente local;
- privacidad de las imágenes;
- Room como fuente de verdad únicamente cuando corresponda;
- ausencia de persistencia del resultado en esta historia;
- ausencia de gestión de stock;
- política de no crear abstracciones vacías.

El plan de v1.2 define para HU-23:

- captura mediante cámara;
- selección desde fotos;
- soporte para capturas de pantalla;
- OCR local;
- extracción inicial de líneas;
- eliminación de la imagen temporal;
- ausencia de consultas a Room.

---

## 6. Estado real antes de HU-23

El análisis de `AlmacenTrackerHU22.zip` confirma que el proyecto dispone de:

- Java 11;
- Android Views;
- View Binding;
- Material Components;
- ViewModel y LiveData;
- CameraX;
- ML Kit Barcode Scanning;
- permiso de cámara consolidado;
- `ScannerActivity`;
- `CameraPermissionHistory`;
- `FileProvider`;
- rutas temporales de archivos;
- `AppContainer`;
- módulos de composición explícita;
- pruebas unitarias, instrumentadas y arquitectónicas;
- funcionamiento completamente sin conexión.

La aplicación no dispone todavía de:

- acceso visual a “Procesar lista”;
- pantalla de captura documental;
- selector de imágenes;
- contrato para tomar una fotografía;
- archivo temporal específico para fotografías de listas;
- dependencia de OCR de texto;
- reconocedor documental;
- modelo de líneas reconocidas;
- estado de procesamiento OCR;
- pantalla o resultado temporal con el texto extraído.

HU-23 añadirá estas capacidades sin modificar las funcionalidades de escaneo individual.

---

## 7. Decisión de alcance técnico

### 7.1. No reutilizar `ScannerActivity`

`ScannerActivity` está especializada en:

```text
preview continuo
+
ImageAnalysis
+
detección de un código
+
resultado inmediato
```

El flujo documental necesita:

```text
captura explícita de una imagen completa
+
procesamiento de una imagen estática
+
resultado con varias líneas
```

Por tanto, HU-23 no convertirá `ScannerActivity` en una pantalla híbrida.

Se creará una feature separada.

### 7.2. Captura mediante contrato del sistema

Para la primera versión se recomienda:

```text
ActivityResultContracts.TakePicture
+
FileProvider
+
archivo temporal privado
```

Ventajas:

- no duplica una interfaz completa de cámara;
- utiliza la aplicación de cámara disponible;
- obtiene una imagen con resolución suficiente;
- reutiliza `FileProvider`;
- reduce complejidad y riesgo;
- mantiene la captura iniciada desde AlmacenTracker.

No se utilizará `TakePicturePreview`, porque suele devolver una miniatura insuficiente para OCR documental.

### 7.3. Selección mediante Photo Picker

Se recomienda:

```text
ActivityResultContracts.PickVisualMedia
```

con tipo:

```text
ImageOnly
```

Ventajas:

- acceso limitado al elemento elegido;
- no requiere permisos generales de almacenamiento;
- admite fotografías, imágenes descargadas y capturas de pantalla;
- encaja con las versiones modernas de Android.

Cuando el sistema necesite compatibilidad alternativa, se utilizará el fallback proporcionado por AndroidX.

---

## 8. Alcance incluido

HU-23 incluye:

- añadir una acción independiente para procesar una lista;
- abrir una pantalla específica;
- ofrecer “Tomar fotografía”;
- ofrecer “Seleccionar imagen”;
- utilizar `TakePicture`;
- crear un archivo temporal privado;
- compartir temporalmente la URI mediante `FileProvider`;
- utilizar Photo Picker;
- limitar la selección a imágenes;
- aceptar fotografías;
- aceptar imágenes descargadas;
- aceptar capturas de pantalla;
- validar que la URI pueda leerse;
- mostrar una previsualización razonable;
- ejecutar OCR local;
- utilizar reconocimiento de texto latino;
- obtener bloques, líneas y elementos cuando estén disponibles;
- conservar el orden visual proporcionado por el reconocedor;
- representar cada línea con texto y orden;
- conservar opcionalmente coordenadas para evolución posterior;
- mostrar progreso;
- impedir procesamientos simultáneos;
- permitir cancelar;
- permitir reintentar;
- controlar imagen no disponible;
- controlar formato no legible;
- controlar imagen sin texto;
- controlar error del reconocedor;
- conservar resultado durante rotación;
- evitar repetir el procesamiento;
- eliminar archivos temporales propios cuando dejen de ser necesarios;
- no modificar la imagen seleccionada por el usuario;
- no guardar imágenes en galería;
- no enviar imágenes;
- no consultar Room;
- no crear historial;
- no interpretar cantidades;
- funcionar sin conexión;
- accesibilidad;
- pruebas unitarias;
- pruebas instrumentadas;
- pruebas manuales;
- CI.

---

## 9. Alcance excluido

HU-23 no incluye:

- validar el patrón de una referencia;
- separar categoría y código;
- aplicar la regla de dos letras;
- corregir confusiones OCR;
- editar líneas;
- eliminar falsos positivos;
- añadir referencias manualmente;
- deduplicar referencias;
- confirmar una lista;
- consultar Room;
- mostrar sitio o posición;
- abrir detalle de mercancía;
- guardar el texto en Room;
- crear historial de sacado;
- conservar título de la lista;
- persistir cantidades;
- interpretar piezas, paquetes, cajas o tallas;
- superponer ubicaciones sobre la imagen;
- recortar manualmente;
- corregir perspectiva;
- aplicar filtros fotográficos;
- escanear varias páginas;
- aceptar PDF;
- importar documentos ofimáticos;
- almacenar permanentemente fotografías;
- usar servicios OCR remotos;
- añadir permiso de Internet;
- añadir permisos generales de almacenamiento.

La revisión y corrección pertenece a HU-24.

La consulta de ubicaciones pertenece a HU-25.

---

## 10. Entrada desde la interfaz principal

Se añadirá una acción independiente de:

```text
Escanear código
```

Nombre recomendado:

```text
Procesar lista
```

No se reutilizará la misma opción porque ambos flujos tienen objetivos diferentes.

Opciones de ubicación válidas:

- nuevo elemento en `menu_main.xml`;
- acción secundaria claramente identificada;
- pantalla intermedia de herramientas de captura.

Para el estado actual del proyecto, se recomienda un nuevo elemento de menú:

```text
Escanear
Procesar lista
Gestión de datos
```

La acción deberá estar deshabilitada durante el modo de selección múltiple del listado.

---

## 11. Pantalla documental

Nombre orientativo:

```text
ReferenceListCaptureActivity
```

La pantalla deberá incluir:

- Toolbar;
- explicación breve;
- acción Tomar fotografía;
- acción Seleccionar imagen;
- contenedor de previsualización;
- nombre u origen genérico de la imagen;
- acción Procesar;
- progreso;
- estado de error;
- Reintentar;
- Cambiar imagen;
- Cancelar.

Texto orientativo:

```text
Toma una fotografía o selecciona una imagen de la lista.
El texto se procesará únicamente en el dispositivo.
```

No se mostrará una promesa de precisión absoluta.

---

## 12. Flujo de captura

1. El usuario pulsa Tomar fotografía.
2. La aplicación crea un archivo temporal privado.
3. Obtiene una `content://Uri` mediante `FileProvider`.
4. Lanza `TakePicture`.
5. La aplicación de cámara captura la imagen.
6. Devuelve éxito o cancelación.
7. Si existe éxito, la URI queda seleccionada.
8. Se muestra una previsualización.
9. El usuario pulsa Procesar.
10. Se ejecuta OCR.

### Cancelación

Si la cámara se cancela:

- no se ejecuta OCR;
- se elimina el archivo temporal vacío o incompleto;
- se conserva la pantalla;
- el usuario puede escoger otra fuente.

### Fallo

Si el contrato devuelve éxito pero la imagen no puede leerse:

- se elimina el temporal propio cuando corresponda;
- se muestra un error controlado;
- se permite volver a capturar.

---

## 13. Flujo de selección

1. El usuario pulsa Seleccionar imagen.
2. Se abre Photo Picker.
3. El usuario elige una imagen.
4. La aplicación recibe una URI.
5. Verifica que pueda abrirse.
6. Muestra una previsualización.
7. El usuario pulsa Procesar.
8. Se ejecuta OCR.

Si el usuario cancela:

- no cambia la imagen previa;
- no se muestra error;
- no se ejecuta OCR.

La aplicación no eliminará ni modificará el archivo elegido por el usuario.

---

## 14. Archivo temporal de captura

La fotografía tomada se guardará en almacenamiento privado temporal.

Ubicación orientativa:

```text
cacheDir/reference_lists/
```

Nombre orientativo:

```text
reference-list-<uuid>.jpg
```

Reglas:

- nombre no basado en datos de usuario;
- no sobrescribir capturas activas;
- no usar almacenamiento público;
- no añadir la imagen a la galería;
- compartir solo mediante `content://`;
- conceder permisos temporales de URI;
- eliminar el archivo al descartarlo;
- eliminarlo después de completar el flujo cuando ya no sea necesario;
- limpiar temporales huérfanos antiguos de forma controlada.

`file_paths.xml` deberá exponer únicamente el subdirectorio necesario.

No se expondrá todo `cacheDir` sin necesidad.

---

## 15. OCR local

Se añadirá una única biblioteca de reconocimiento de texto latino con modelo incluido.

Opción prevista:

```text
ML Kit Text Recognition
modelo latino incluido
```

Razones:

- procesamiento local;
- integración con Java;
- entrada desde URI o imagen;
- salida por bloques, líneas y elementos;
- coordenadas disponibles;
- coherencia con el uso existente de ML Kit;
- ausencia de dependencia de red durante el uso.

La versión concreta se fijará en `libs.versions.toml` después de verificar compatibilidad con:

```text
minSdk 26
compileSdk 36.1
AGP 9.3.1
```

No se incorporarán simultáneamente varios motores OCR.

---

## 16. Separación arquitectónica

Estructura orientativa:

```text
com.rndymi.almacentracker/
├── core/
│   └── document/
│       ├── RecognizedDocument.java
│       └── RecognizedTextLine.java
├── data/
│   └── document/
│       └── MlKitDocumentTextRecognizer.java
└── feature/
    └── reference_list/
        └── capture/
            ├── ReferenceListCaptureActivity.java
            ├── ReferenceListCaptureUiState.java
            ├── ReferenceListCaptureViewModel.java
            └── ReferenceListCaptureViewModelFactory.java
```

La estructura definitiva deberá adaptarse al código real.

### `MlKitDocumentTextRecognizer`

Responsable de:

- recibir una imagen válida;
- ejecutar ML Kit;
- transformar tipos externos;
- devolver texto reconocido;
- cerrar el reconocedor;
- transformar errores técnicos;
- no navegar;
- no acceder a Room;
- no interpretar referencias.

### ViewModel

Responsable de:

- imagen seleccionada;
- estado de procesamiento;
- resultado temporal;
- error controlado;
- impedir ejecuciones simultáneas;
- conservar resultado ante recreación;
- no depender de Views.

### Activity

Responsable de:

- contratos Activity Result;
- creación de URI temporal;
- previsualización;
- renderizado;
- acciones de usuario;
- navegación posterior.

---

## 17. Modelo del resultado OCR

Modelo Java puro orientativo:

```text
RecognizedDocument
├── sourceType
├── lines
└── recognizedAt
```

Cada línea:

```text
RecognizedTextLine
├── index
├── rawText
├── boundingBox opcional
└── confidence opcional cuando la API lo proporcione
```

Reglas:

- `rawText` conserva el texto reconocido;
- `index` conserva el orden;
- no se normaliza como referencia;
- no se elimina una línea por no coincidir con un patrón;
- no se separan categoría y código;
- no se suman cantidades;
- la colección será inmutable o defensivamente copiada.

`boundingBox` podrá conservarse como datos simples y no como tipos de ML Kit.

---

## 18. Orden de las líneas

HU-23 conservará el orden entregado por el reconocedor.

Si ML Kit devuelve:

```text
Block 1
    Line 1
    Line 2
Block 2
    Line 3
```

el resultado será:

```text
index 0 → Line 1
index 1 → Line 2
index 2 → Line 3
```

No se reordenará alfabéticamente.

No se deduplicará.

No se descartarán líneas por contenido.

La revisión semántica pertenece a HU-24.

---

## 19. Imagen sin texto

Si el OCR finaliza correctamente pero no devuelve líneas:

Estado:

```text
NO_TEXT_FOUND
```

Mensaje:

```text
No se encontró texto legible en la imagen.
```

Acciones:

```text
Cambiar imagen
Reintentar
Cancelar
```

No se tratará como excepción técnica.

No se navegará a HU-24 con una lista vacía.

---

## 20. Imagen ilegible o no disponible

Casos:

- URI revocada;
- archivo eliminado;
- stream nulo;
- formato no decodificable;
- tamaño o contenido inválido;
- error al obtener metadatos.

Mensaje:

```text
No se pudo abrir la imagen seleccionada.
```

Acciones:

```text
Cambiar imagen
Cancelar
```

No se mostrará la URI completa.

No se mostrarán rutas internas.

---

## 21. Error de reconocimiento

Si el motor OCR falla:

```text
No se pudo reconocer el texto de la imagen.
```

Acciones:

```text
Reintentar
Cambiar imagen
Cancelar
```

Reglas:

- conservar la imagen mientras siga disponible;
- no repetir el error automáticamente;
- permitir un único procesamiento activo;
- no mostrar excepción;
- registrar detalles solo durante desarrollo;
- liberar recursos.

---

## 22. Tamaño, memoria y orientación

Las fotografías pueden ser grandes.

La implementación deberá:

- evitar decodificar el bitmap completo para la previsualización;
- cargar una versión escalada para UI;
- permitir que ML Kit procese desde URI cuando sea viable;
- leer metadatos de orientación;
- evitar operaciones pesadas en el hilo principal;
- controlar `OutOfMemoryError` como error de imagen;
- no mantener simultáneamente varias copias grandes;
- liberar referencias de previsualización al cambiar de imagen.

La imagen original no deberá comprimirse de forma destructiva antes del OCR salvo que sea necesario por memoria.

---

## 23. Estados de interfaz

Estado orientativo:

```text
ReferenceListCaptureUiState.Status
├── EMPTY
├── IMAGE_SELECTED
├── PROCESSING
├── TEXT_RECOGNIZED
├── NO_TEXT_FOUND
└── ERROR
```

Datos posibles:

```text
imageSource
previewUri
recognizedDocument
errorType
```

Reglas:

- `EMPTY`: permite elegir origen;
- `IMAGE_SELECTED`: permite procesar o cambiar;
- `PROCESSING`: bloquea nuevos lanzamientos;
- `TEXT_RECOGNIZED`: tiene una o más líneas;
- `NO_TEXT_FOUND`: no contiene líneas;
- `ERROR`: distingue apertura y reconocimiento cuando afecta a las acciones.

No se almacenará un `Bitmap` dentro del ViewModel.

---

## 24. Navegación posterior

Al obtener texto:

```text
TEXT_RECOGNIZED
        ↓
continuar a revisión
```

La pantalla de revisión completa pertenece a HU-24.

Para no adelantar esa historia, HU-23 podrá terminar de una de estas formas:

### Opción recomendada

Crear un contrato temporal que entregue:

```text
RecognizedDocument
```

a una pantalla mínima de confirmación técnica o dejar preparado el evento de navegación sin implementar edición semántica.

### Límite obligatorio

HU-23 deberá demostrar que:

- la imagen se adquirió;
- el OCR finalizó;
- las líneas se obtuvieron;
- el orden se conservó.

No deberá implementar validación de referencias.

El mecanismo definitivo de transferencia deberá evitar extras demasiado grandes. Se recomienda conservar temporalmente el documento en el ViewModel o en un almacén de sesión limitado al flujo, no serializar bitmaps ni imágenes.

---

## 25. Privacidad

HU-23 deberá cumplir:

- OCR completamente local;
- sin permiso de Internet;
- sin permisos generales de almacenamiento;
- acceso únicamente a la imagen elegida;
- fotografía propia en caché privada;
- sin publicación en galería;
- sin copia permanente;
- sin envío a terceros;
- sin contenido de imagen en logs;
- sin texto completo en logs de producción;
- eliminación de temporales propios;
- Room sin cambios.

La interfaz deberá informar:

```text
La imagen se procesa únicamente en este dispositivo.
```

---

## 26. Accesibilidad

La pantalla deberá incluir:

- descripciones para Tomar fotografía;
- descripciones para Seleccionar imagen;
- descripción de la previsualización;
- progreso anunciado;
- error anunciado;
- botones con mínimo 48 dp;
- orden de foco coherente;
- texto compatible con escalado;
- contraste en modo claro y oscuro;
- acciones que no dependan solo del icono;
- mensajes comprensibles.

La imagen no necesita una descripción detallada de su contenido antes del OCR.

---

## 27. Flujo principal — Fotografía

1. El usuario abre Procesar lista.
2. Pulsa Tomar fotografía.
3. Se crea una URI temporal.
4. Se abre la cámara del sistema.
5. Captura la lista.
6. Regresa a AlmacenTracker.
7. Se muestra la previsualización.
8. Pulsa Procesar.
9. El ViewModel entra en `PROCESSING`.
10. El reconocedor abre la imagen.
11. ML Kit procesa localmente.
12. Se transforman bloques y líneas.
13. Se conserva el orden.
14. Se obtiene `TEXT_RECOGNIZED`.
15. La imagen no se guarda permanentemente.
16. Room no cambia.

---

## 28. Flujo principal — Imagen seleccionada

1. El usuario abre Procesar lista.
2. Pulsa Seleccionar imagen.
3. Se abre Photo Picker.
4. Selecciona una captura de pantalla o fotografía.
5. Regresa a la aplicación.
6. Se muestra la previsualización.
7. Pulsa Procesar.
8. El OCR se ejecuta localmente.
9. Se obtienen las líneas.
10. La imagen original no se modifica.
11. Room no cambia.

---

## 29. Flujos alternativos

### FA-01 — Cámara cancelada

1. El usuario abre la cámara.
2. Cancela.
3. Se elimina el temporal incompleto.
4. La pantalla continúa operativa.

### FA-02 — Selector cancelado

1. Se abre Photo Picker.
2. El usuario vuelve atrás.
3. La imagen actual permanece.
4. No se muestra error.

### FA-03 — Captura fallida

1. `TakePicture` informa error o archivo vacío.
2. Se descarta el temporal.
3. Se permite repetir.

### FA-04 — URI no accesible

1. La imagen seleccionada deja de estar disponible.
2. No puede abrirse.
3. Se muestra error.
4. Se permite elegir otra imagen.

### FA-05 — Sin texto

1. OCR finaliza correctamente.
2. No devuelve líneas.
3. Se muestra `NO_TEXT_FOUND`.
4. No se continúa a revisión.

### FA-06 — Texto impreso

1. Se captura una lista impresa clara.
2. OCR devuelve sus líneas.
3. Se conserva el orden.

### FA-07 — Escritura manual clara

1. Se selecciona una lista manuscrita legible.
2. OCR intenta reconocerla.
3. Se muestran únicamente las líneas obtenidas.
4. No se promete exactitud.

### FA-08 — Imagen rotada

1. La imagen contiene metadatos de orientación.
2. El reconocedor la procesa con orientación correcta.
3. Se obtienen líneas cuando sean legibles.

### FA-09 — Error OCR

1. El reconocedor falla.
2. Se conserva la selección.
3. Se ofrece Reintentar o Cambiar imagen.

### FA-10 — Doble procesamiento

1. El usuario pulsa Procesar varias veces.
2. Solo se inicia una operación.

### FA-11 — Rotación durante OCR

1. El procesamiento está activo.
2. La Activity se recrea.
3. No se inicia otro OCR.
4. El resultado se entrega una sola vez.

### FA-12 — Modo avión

1. No existe conexión.
2. Se selecciona una imagen.
3. OCR funciona con el modelo incluido.

---

## 30. Criterios de aceptación

### CA-01 — Acceso independiente

**Dado** el listado principal,  
**cuando** el usuario pulsa Procesar lista,  
**entonces** se abre un flujo diferente del escáner individual.

### CA-02 — Tomar fotografía

**Dado** que el usuario elige Tomar fotografía,  
**cuando** confirma una captura válida,  
**entonces** la imagen queda disponible para procesar.

### CA-03 — Seleccionar imagen

**Dado** que el usuario elige Seleccionar imagen,  
**cuando** selecciona una fotografía o captura,  
**entonces** la imagen queda disponible para procesar.

### CA-04 — Sin permisos generales

**Dado** que se utiliza Photo Picker,  
**cuando** se selecciona una imagen,  
**entonces** la aplicación no solicita acceso general al almacenamiento.

### CA-05 — Previsualización

**Dado** que existe una imagen válida,  
**cuando** regresa a la pantalla,  
**entonces** se muestra una previsualización sin bloquear la UI.

### CA-06 — OCR local

**Dado** que el usuario pulsa Procesar,  
**cuando** se ejecuta el reconocimiento,  
**entonces** la imagen no se envía a un servicio remoto.

### CA-07 — Líneas reconocidas

**Dado** que la imagen contiene texto legible,  
**cuando** finaliza OCR,  
**entonces** se obtiene una colección ordenada de líneas.

### CA-08 — Sin interpretación de referencias

**Dado** el texto reconocido,  
**cuando** finaliza HU-23,  
**entonces** todavía no se valida categoría ni código.

### CA-09 — Sin texto

**Dado** que la imagen no contiene texto reconocible,  
**cuando** finaliza OCR,  
**entonces** se informa y no se continúa con una lista vacía.

### CA-10 — Cancelación de cámara

**Dado** que el usuario cancela la captura,  
**cuando** vuelve,  
**entonces** no se ejecuta OCR ni se conserva un temporal incompleto.

### CA-11 — Cancelación del selector

**Dado** que el usuario cancela Photo Picker,  
**cuando** vuelve,  
**entonces** no se muestra un error.

### CA-12 — Error controlado

**Dado** que la imagen no puede abrirse o reconocerse,  
**cuando** ocurre el fallo,  
**entonces** se muestra una acción de recuperación sin detalles técnicos.

### CA-13 — Procesamiento único

**Dado** que OCR está activo,  
**cuando** el usuario pulsa nuevamente Procesar,  
**entonces** no se inicia otra operación.

### CA-14 — Rotación

**Dado** que OCR está procesando o ya terminó,  
**cuando** la Activity se recrea,  
**entonces** la operación y el resultado no se duplican.

### CA-15 — Temporal privado

**Dado** que se toma una fotografía,  
**cuando** finaliza o se descarta el flujo,  
**entonces** el archivo temporal propio se elimina cuando deja de ser necesario.

### CA-16 — Imagen seleccionada intacta

**Dado** que la imagen proviene de Photo Picker,  
**cuando** termina el procesamiento,  
**entonces** el archivo original no se modifica ni se elimina.

### CA-17 — Room sin cambios

**Dado** cualquier resultado OCR,  
**cuando** finaliza HU-23,  
**entonces** Room no se consulta ni se modifica.

### CA-18 — Funcionamiento offline

**Dado** el dispositivo en modo avión,  
**cuando** se procesa una imagen,  
**entonces** OCR funciona localmente.

---

## 31. Diseño técnico propuesto

### Gradle

Añadir:

```text
ML Kit Text Recognition para escritura latina
```

mediante alias en:

```text
gradle/libs.versions.toml
```

No se añadirá una dependencia OCR descargable durante el uso normal.

### Manifest

Registrar:

```text
ReferenceListCaptureActivity
```

No añadir:

```text
READ_MEDIA_IMAGES
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE
INTERNET
```

El permiso de cámara existente podrá permanecer por el escáner individual, aunque `TakePicture` no requiera que la aplicación gestione directamente CameraX.

### `FileProvider`

Ampliar `file_paths.xml` solo con el subdirectorio temporal necesario.

### Composición

Podrá añadirse:

```text
ReferenceListModule
```

si agrupa:

- reconocedor;
- factory del ViewModel;
- dependencias del flujo.

No se creará un módulo si únicamente devuelve una instancia trivial sin aportar claridad.

---

## 32. Archivos previstos

Archivos probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── core/document/
│   ├── RecognizedDocument.java
│   ├── RecognizedTextLine.java
│   └── DocumentRecognitionCallback.java
├── data/document/
│   └── MlKitDocumentTextRecognizer.java
└── feature/reference_list/capture/
    ├── ReferenceListCaptureActivity.java
    ├── ReferenceListCaptureUiState.java
    ├── ReferenceListCaptureViewModel.java
    └── ReferenceListCaptureViewModelFactory.java
```

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── app/AppContainer.java
├── app/di/ReferenceListModule.java
└── feature/inventory/list/MainActivity.java
```

Recursos probables:

```text
app/src/main/res/
├── drawable/ic_reference_list.xml
├── layout/activity_reference_list_capture.xml
├── menu/menu_main.xml
├── values/strings.xml
└── xml/file_paths.xml
```

Pruebas probables:

```text
app/src/test/java/com/rndymi/almacentracker/
├── core/document/RecognizedDocumentTest.java
└── feature/reference_list/capture/
    ├── ReferenceListCaptureUiStateTest.java
    └── ReferenceListCaptureViewModelTest.java

app/src/androidTest/java/com/rndymi/almacentracker/
├── data/document/MlKitDocumentTextRecognizerTest.java
└── feature/reference_list/capture/
    └── ReferenceListCaptureActivityContractTest.java
```

Los nombres deberán ajustarse al código real y a las pruebas existentes.

---

## 33. Pruebas

### Modelos

- documento exige colección no nula;
- lista defensivamente copiada;
- orden conservado;
- línea conserva texto bruto;
- índice válido;
- coordenadas opcionales;
- no depende de ML Kit.

### ViewModel

- estado inicial;
- imagen seleccionada;
- procesamiento iniciado;
- segunda solicitud ignorada;
- líneas reconocidas;
- sin texto;
- error de apertura;
- error OCR;
- reintento;
- cambiar imagen;
- resultado único;
- rotación sin duplicado;
- no contiene Bitmap.

### Reconocedor

- transforma bloques y líneas;
- conserva orden;
- devuelve cero líneas como éxito vacío;
- transforma errores;
- no interpreta referencias;
- cierra recursos;
- acepta URI válida.

### Activity e instrumentación

- acción Procesar lista abre la Activity;
- Tomar fotografía crea URI segura;
- captura cancelada limpia temporal;
- Photo Picker limitado a imágenes;
- selector cancelado no muestra error;
- imagen seleccionada se previsualiza;
- Procesar muestra progreso;
- error muestra recuperación;
- Room no interviene;
- estado se conserva tras recreación.

### Manuales

- fotografía impresa;
- captura de pantalla;
- imagen descargada;
- lista manuscrita clara;
- lista manuscrita irregular;
- imagen vertical;
- imagen horizontal;
- imagen rotada;
- iluminación baja;
- desenfoque;
- imagen sin texto;
- imagen grande;
- cancelación;
- modo avión;
- modo oscuro;
- escalado de fuente.

---

## 34. Tareas de implementación

1. Confirmar HU-22 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-23-capturar-seleccionar-lista`.
4. Añadir dependencia de OCR latino incluido.
5. Crear modelos de resultado documental.
6. Crear reconocedor ML Kit aislado.
7. Crear estado de captura.
8. Crear ViewModel.
9. Crear Factory y composición necesaria.
10. Crear `ReferenceListCaptureActivity`.
11. Crear layout.
12. Registrar contratos `TakePicture` y Photo Picker.
13. Crear gestión de URI temporal.
14. Limitar `FileProvider`.
15. Implementar previsualización escalada.
16. Implementar procesamiento OCR.
17. Conservar orden de líneas.
18. Implementar estados sin texto y error.
19. Impedir procesamiento duplicado.
20. Gestionar temporales.
21. Añadir acción Procesar lista en `MainActivity`.
22. Registrar Activity en Manifest.
23. Añadir strings y accesibilidad.
24. Crear pruebas de modelos.
25. Crear pruebas de ViewModel.
26. Crear pruebas del reconocedor viables.
27. Crear pruebas de contratos de Activity.
28. Ejecutar pruebas unitarias.
29. Ejecutar lint.
30. Ejecutar build debug.
31. Ejecutar pruebas instrumentadas.
32. Verificar funcionamiento offline.
33. Verificar privacidad.
34. Verificar criterios de aceptación.
35. Integrar en `develop`.
36. Verificar CI de `develop`.
37. Eliminar la rama tras confirmar la integración.

---

## 35. Evidencias necesarias

- acción Procesar lista;
- pantalla documental;
- captura mediante cámara;
- selección mediante Photo Picker;
- captura de pantalla seleccionada;
- previsualización;
- OCR de lista impresa;
- OCR de escritura manual clara;
- líneas y orden extraídos;
- imagen sin texto;
- error de apertura;
- error de OCR;
- reintento;
- cancelación;
- limpieza de temporal;
- ausencia de permisos de almacenamiento;
- ausencia de Internet;
- Room sin cambios;
- modo avión;
- pruebas unitarias;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 36. Definición de terminado

HU-23 estará terminada cuando:

- exista una acción independiente para procesar listas;
- pueda tomarse una fotografía;
- pueda seleccionarse una imagen;
- las capturas de pantalla sean aceptadas como imágenes;
- no se soliciten permisos generales de almacenamiento;
- la fotografía se guarde temporalmente en espacio privado;
- la imagen pueda previsualizarse;
- OCR funcione localmente;
- se obtengan bloques y líneas;
- el orden se conserve;
- el resultado no se interprete todavía como referencias;
- una imagen sin texto se controle;
- una URI inválida se controle;
- un error OCR se controle;
- pueda reintentarse;
- no existan procesamientos simultáneos;
- la rotación no duplique el OCR;
- los temporales propios se eliminen;
- las imágenes seleccionadas no se modifiquen;
- no se guarden imágenes permanentemente;
- no se añada permiso de Internet;
- no se añadan permisos generales de almacenamiento;
- Room no se consulte;
- Room no se modifique;
- no se cree historial;
- funcione completamente sin conexión;
- las funciones de escaneo individual continúen operativas;
- las pruebas definidas sean satisfactorias;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 37. Validación técnica final

Ejecutar:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Con emulador o dispositivo:

```bash
./gradlew connectedDebugAndroidTest
```

Validación manual obligatoria:

- fotografía;
- Photo Picker;
- captura de pantalla;
- texto impreso;
- escritura manual clara;
- imagen sin texto;
- rotación;
- modo avión;
- limpieza de temporal.

---

## 38. Resultado esperado

Al cerrar HU-23:

```text
usuario abre Procesar lista
        ↓
toma una fotografía
        o
selecciona una imagen
        ↓
la imagen se procesa localmente
        ↓
se obtiene una colección ordenada de líneas
        ↓
Room permanece sin cambios
```

La siguiente historia será:

```text
HU-24 — Revisar y corregir referencias reconocidas
```
