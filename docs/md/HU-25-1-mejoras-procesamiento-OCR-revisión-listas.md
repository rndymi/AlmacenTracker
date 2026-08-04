# HU-25 — Mejoras del procesamiento OCR y revisión de listas

> Extensión técnica y funcional de HU-25 implementada después de la issue `#28` y consolidada en la issue `#29`.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia relacionada:** HU-25  
**Nombre:** Mejoras del procesamiento OCR y revisión de listas  
**Tipo:** Extensión y mejora de la implementación de HU-25  
**Prioridad recomendada:** Alta
**Rama de trabajo:** `HU25`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-25 — Mostrar ubicaciones de una lista reconoci

---

## 2. Relación con la issue #28

La issue `#28` implementó el cierre funcional de HU-25:

```text
lista confirmada
        ↓
consulta conjunta por categoría + código
        ↓
resultado ordenado
        ↓
sitio, posición o referencia no encontrada
```

La issue `#29` no sustituye ese comportamiento. Lo amplía y mejora en las etapas anteriores del mismo flujo:

```text
capturar o seleccionar una imagen
        ↓
preprocesar la imagen
        ↓
reconocer texto con ML Kit
        ↓
reconstruir filas y columnas por posición
        ↓
extraer referencias y descartar cantidades
        ↓
comparar con el inventario
        ↓
clasificar coincidencias y solicitar revisión
        ↓
confirmar la lista
        ↓
mostrar las ubicaciones implementadas en #28
```

Por tanto, todo lo incluido en `#29` debe entenderse como una extensión mejorada de la solución aplicada en `#28`.

---

## 3. Historia de usuario

Como usuario,  
quiero que la aplicación procese mejor las fotografías de listas, reconstruya correctamente su orden y me indique el grado de coincidencia de cada referencia,  
para revisar un resultado más fiable antes de consultar las ubicaciones de la mercancía.

---

## 4. Objetivo

Mejorar la calidad y la seguridad del flujo de listas reconocido en HU-23, HU-24 y HU-25 mediante:

- preprocesamiento local de la imagen antes del OCR;
- corrección de la orientación EXIF;
- reducción controlada del tamaño y mejora moderada del contraste;
- reconstrucción espacial de texto a partir de elementos y coordenadas;
- soporte para documentos de una o dos columnas;
- aceptación de referencias con códigos de tres, cuatro o cinco cifras;
- separación de referencias y cantidades;
- sugerencias limitadas a confusiones OCR plausibles;
- clasificación visible de las coincidencias con el inventario;
- conservación de la decisión final en manos del usuario;
- aviso de que el escáner de listas es experimental;
- confirmación reforzada para eliminar mercancía;
- ajustes de accesibilidad y coherencia visual.

La mejora debe funcionar completamente sin conexión y no debe alterar la consulta de ubicaciones ya implementada en `#28`.

---

## 5. Estado inicial heredado de #28

Antes de comenzar esta extensión, el proyecto ya disponía de:

- captura de fotografía y selección de imagen;
- OCR local mediante ML Kit Text Recognition;
- pantalla de revisión de referencias;
- edición, adición, eliminación y deduplicación manual;
- confirmación de la lista revisada;
- consulta de mercancía por identidad `categoría + código`;
- conservación del orden confirmado;
- presentación de sitio y posición;
- estado de referencia no encontrada;
- navegación desde la revisión hasta las ubicaciones;
- acceso al detalle de una mercancía encontrada;
- funcionamiento local con Room;
- pruebas del servicio de localización, DAO, ViewModel y adaptadores.

Limitaciones detectadas sobre esa base:

- la imagen se enviaba al OCR sin una fase dedicada de preprocesamiento;
- la orientación almacenada en EXIF podía perjudicar el reconocimiento;
- las líneas proporcionadas por ML Kit no siempre respetaban el orden visual del documento;
- una lista con dos columnas podía intercalar referencias;
- una categoría o un código dividido entre líneas podía perderse;
- el parser estaba orientado principalmente a códigos de cuatro o cinco cifras;
- cantidades y unidades podían confundirse con sufijos de una referencia;
- las sugerencias podían resultar demasiado permisivas;
- la pantalla de revisión no explicaba claramente si una referencia era exacta, sugerida, ambigua o inexistente;
- no se diferenciaba visualmente el texto reconstruido del texto bruto detectado;
- la eliminación individual o múltiple dependía solamente de una confirmación básica.

---

## 6. Alcance incluido

### 6.1. Preprocesamiento de imágenes

Se incorporó una fase independiente anterior al reconocimiento:

```text
URI de imagen
        ↓
lectura de dimensiones y EXIF
        ↓
decodificación escalada
        ↓
corrección de orientación
        ↓
reducción a un máximo de 2200 px por lado
        ↓
escala de grises
        ↓
contraste moderado
        ↓
bitmap preparado para OCR
```

El procesamiento:

- se ejecuta fuera del hilo principal mediante un `ExecutorService`;
- diferencia entre error de apertura y error de procesamiento;
- controla imágenes inválidas y errores de memoria;
- libera los bitmaps intermedios cuando dejan de ser necesarios;
- ignora de forma segura resultados pertenecientes a una petición antigua;
- cierra sus recursos al destruirse el `ViewModel`.

### 6.2. Orientación y tamaño

La orientación se obtiene con `ExifInterface` y contempla giros de:

- `0°`;
- `90°`;
- `180°`;
- `270°`.

La imagen se decodifica de forma escalada y su lado máximo final para OCR queda limitado a `2200 px`.

Este límite evita trabajar innecesariamente con bitmaps muy grandes y reduce el riesgo de consumo excesivo de memoria.

### 6.3. Mejora visual para OCR

Antes de entregar el bitmap a ML Kit se aplica:

- saturación `0`, para obtener escala de grises;
- contraste moderado de `1.18`;
- filtrado durante el escalado.

El contraste se mantiene deliberadamente moderado para no destruir trazos finos de letras o números.

### 6.4. Modelo espacial del texto

El reconocimiento ya no conserva únicamente cadenas de texto. Se añadieron modelos con:

- texto detectado;
- coordenadas `left`, `top`, `right` y `bottom`;
- anchura y altura;
- centro horizontal y vertical;
- elementos pertenecientes a cada línea;
- texto bruto;
- texto reconstruido;
- límites espaciales de la línea.

Esta información permite reorganizar el resultado según la posición real de cada fragmento en la imagen.

### 6.5. Reconstrucción de filas

Los elementos detectados se:

1. limpian y ordenan por posición;
2. agrupan según solapamiento vertical o cercanía entre centros;
3. ordenan horizontalmente dentro de cada fila;
4. separan cuando existe un hueco horizontal suficientemente grande;
5. convierten en líneas reconstruidas;
6. reindexan en el orden final de lectura.

La reconstrucción permite:

- unir fragmentos que ML Kit entregó separados pero pertenecen a la misma fila;
- mantener separadas filas visualmente distintas;
- ordenar correctamente elementos de izquierda a derecha;
- ignorar elementos vacíos;
- conservar el texto bruto para diagnóstico.

### 6.6. Detección de columnas

Se añadió soporte específico para listas de dos columnas.

El detector:

- calcula los centros horizontales de las líneas;
- localiza el mayor hueco entre grupos;
- exige un hueco mínimo proporcional al ancho del documento;
- exige al menos dos líneas por columna;
- evita dividir una lista normal de una sola columna;
- ordena primero la columna izquierda de arriba abajo;
- continúa después por la columna derecha de arriba abajo.

Orden esperado:

```text
columna izquierda, de arriba abajo
        ↓
columna derecha, de arriba abajo
```

### 6.7. Resultado preliminar y texto original

La pantalla de captura presenta el texto espacialmente reorganizado como:

```text
Resultado preliminar
```

También permite alternar:

```text
Ver texto original detectado
Ocultar texto original
```

El estado de expansión del texto bruto forma parte del estado de interfaz y solo puede modificarse cuando existe un documento reconocido.

### 6.8. Parser de referencias ampliado

El formato funcional aceptado queda definido como:

```text
2 letras + código de 3 a 5 cifras + sufijo textual opcional
```

Ejemplos válidos:

```text
MR 900
MR 1210
MR 1210A
MR 1210 CAJA
```

El parser:

- conserva ceros iniciales;
- normaliza mayúsculas y espacios Unicode;
- mantiene el orden de aparición;
- admite varias referencias en una misma línea;
- evita extraer coincidencias incrustadas en valores mayores;
- conserva candidatos OCR imperfectos para revisión;
- puede unir una categoría fragmentada con la línea siguiente;
- no corrige automáticamente el resultado observado.

### 6.9. Separación de cantidades y unidades

El procesamiento deja de interpretar cantidades como parte de la identidad de la mercancía.

Ejemplos:

```text
MR 1210 - 20 PCS
MR 1210PCS
MR 1210 4 CAJAS
```

La referencia funcional debe continuar siendo:

```text
MR 1210
```

Se reconocen y descartan como unidades de cantidad, entre otras:

- `PC`, `PCS`;
- `PZ`, `PZA`, `PZAS`, `PIEZA`, `PIEZAS`;
- `PQT`, `PQTS`, `PAQUETE`, `PAQUETES`;
- `UD`, `UDS`, `UNIDAD`, `UNIDADES`;
- `CJ`, `CJA`, `CAJA`, `CAJAS`;
- `BTO`, `BULTO`, `BULTOS`;
- `PACK`, `PACKS`;
- `BOX`, `BOXES`;
- `CTN`, `CTNS`.

La extracción también se detiene cuando, después de una referencia, aparece un guion utilizado como delimitador de cantidad.

### 6.10. Sugerencias OCR restringidas

Las sugerencias se comparan contra las referencias reales del inventario y solo se generan ante confusiones visuales plausibles.

Confusiones contempladas en la categoría:

```text
5 → S
2 → Z
0 → O
8 → B
1 → I o L
E o 2 → R, con menor prioridad
```

Confusiones contempladas en el código:

```text
I o L → 1 o 7
O → 0
S → 5
Z → 2
B → 8
G → 6
1 o 9 → 7, con menor prioridad
```

Restricciones:

- la longitud numérica observada y la esperada debe coincidir;
- solo se admite una diferencia cuando el OCR ya reconoció un dígito;
- no se sustituyen dígitos fiables de forma global;
- no se rellenan sugerencias con referencias no relacionadas;
- una categoría no se cambia si la diferencia no responde a una confusión prevista;
- el sufijo debe coincidir o estar ausente de forma justificable;
- las opciones se ordenan por puntuación y valor visible;
- se eliminan sugerencias duplicadas;
- se respeta el máximo solicitado por la pantalla de revisión.

### 6.11. Clasificación de coincidencias

Cada propuesta reconocida queda clasificada con uno de estos estados:

| Estado interno | Mensaje visible | Significado |
|---|---|---|
| `EXACT` | Coincidencia exacta en el inventario | La referencia válida existe tal cual en Room. |
| `UNIQUE_SUGGESTION` | Una posible coincidencia; revísala antes de continuar | Existe una única alternativa plausible. |
| `AMBIGUOUS` | Varias coincidencias posibles; selecciona una | Existen varias alternativas plausibles. |
| `NO_MATCH` | Sin coincidencias; corrige la referencia manualmente | No existe coincidencia exacta ni sugerencia fiable. |
| `UNVERIFIED` | No se pudo verificar con el inventario | La referencia tiene formato válido, pero no fue posible cargar el inventario. |
| `USER_CONFIRMED` | Referencia revisada manualmente | El usuario editó, añadió o eligió explícitamente la referencia. |

La interfaz usa un mensaje y color diferenciados para facilitar la revisión.

### 6.12. Revisión asistida sin autocorrección

Las coincidencias sugeridas se muestran como acciones seleccionables.

Reglas:

- la aplicación no sustituye silenciosamente una referencia;
- una sugerencia única sigue necesitando revisión;
- una coincidencia ambigua obliga al usuario a elegir o editar;
- una propuesta sin coincidencia se conserva para que pueda corregirse;
- aplicar una sugerencia reemplaza únicamente la propuesta seleccionada;
- una edición manual cambia el estado a `USER_CONFIRMED`;
- se conserva la deduplicación por identidad `categoría + código`;
- se mantiene el orden de la lista.

### 6.13. Aviso de escáner experimental

Al entrar en el procesamiento de listas se muestra:

```text
Escáner experimental

La extracción puede no ser precisa. Revisa el resultado antes de
guardarlo; mejorará en futuras actualizaciones.
```

El aviso:

- se consume una sola vez durante cada sesión de la aplicación;
- vuelve a estar disponible al comenzar una sesión nueva desde `MainActivity`;
- no bloquea el uso del escáner después de pulsar `Entendido`;
- refuerza que el resultado debe revisarse antes de continuar.

### 6.14. Confirmación reforzada de eliminación

Se creó un diálogo común para la eliminación individual y múltiple.

Flujo:

```text
solicitar eliminación
        ↓
confirmación contextual existente
        ↓
escribir delete exactamente
        ↓
habilitar botón Eliminar
        ↓
ejecutar eliminación
```

Reglas:

- el botón positivo comienza deshabilitado;
- solo se habilita si el texto coincide exactamente con `delete`;
- una entrada incorrecta muestra un error;
- una entrada vacía no muestra error;
- cancelar no modifica datos;
- la misma verificación se reutiliza en detalle y selección múltiple.

### 6.15. Ajustes de accesibilidad y coherencia

Se aplicaron ajustes complementarios:

- descripción accesible de navegación hacia atrás en las barras;
- uso consistente de atributos `app:title` y `app:titleTextColor`;
- título principal alineado con el nombre de la aplicación;
- mensajes de progreso con el carácter de elipsis `…`;
- textos específicos para estados, sugerencias y acciones de OCR;
- etiquetas y descripción del campo de confirmación destructiva.

---

## 7. Alcance excluido

Esta extensión no incluye:

- OCR remoto o servicios en la nube;
- envío de imágenes fuera del dispositivo;
- aprendizaje automático personalizado;
- corrección automática sin confirmación;
- reconocimiento garantizado al cien por cien;
- soporte general para cualquier formato documental;
- detección arbitraria de más de dos columnas;
- inventario, stock o cantidades persistentes;
- almacenamiento de las cantidades impresas en la lista;
- historial de listas procesadas;
- persistencia de fotografías o resultados OCR;
- modificación automática de registros de Room;
- cambios en la consulta de ubicaciones implementada en `#28`;
- migración del esquema de Room;
- pruebas instrumentadas de cámara o galería dentro de este rango.

---

## 8. Reglas funcionales

### RF-01 — Procesamiento local

La imagen y el texto deben procesarse localmente y sin conexión.

### RF-02 — Revisión obligatoria

El resultado OCR es preliminar y el usuario conserva la decisión final.

### RF-03 — Identidad de mercancía

La identidad sigue siendo:

```text
categoría + código
```

### RF-04 — Orden documental

La reconstrucción debe respetar el orden espacial de filas y columnas.

### RF-05 — Cantidad fuera de la identidad

Las cantidades y unidades no forman parte de la referencia consultada.

### RF-06 — Sugerencias justificables

Solo se ofrecerán referencias del inventario compatibles con confusiones OCR expresamente contempladas.

### RF-07 — Sin autocorrección

Una sugerencia nunca se aplica sin acción explícita del usuario.

### RF-08 — Conservación de #28

Después de confirmar la revisión, la lista continuará hacia la consulta de ubicaciones ya implementada, manteniendo su orden y deduplicación.

### RF-09 — Eliminación consciente

Una eliminación individual o múltiple requiere escribir exactamente `delete`.

---

## 9. Flujo principal

1. El usuario abre `Procesar lista`.
2. La aplicación muestra el aviso experimental si todavía no se mostró en la sesión.
3. El usuario toma una fotografía o selecciona una imagen.
4. La aplicación lee tamaño y orientación.
5. La imagen se escala, rota, convierte a escala de grises y ajusta con contraste moderado.
6. ML Kit reconoce bloques, líneas y elementos con sus coordenadas.
7. Los elementos se reconstruyen en filas.
8. Si se detectan dos columnas, se ordena primero la izquierda y después la derecha.
9. Se presenta el resultado preliminar y se permite consultar el texto bruto.
10. El usuario abre la revisión.
11. El parser extrae referencias de tres a cinco cifras y elimina unidades de cantidad.
12. Las referencias se comparan con el inventario local.
13. Cada propuesta se clasifica como exacta, única, ambigua, sin coincidencia o no verificada.
14. El usuario acepta una sugerencia, edita, añade o elimina cuando resulte necesario.
15. Las acciones manuales quedan identificadas como revisadas por el usuario.
16. El usuario confirma la lista.
17. Se abre la pantalla de ubicaciones implementada en `#28`.
18. Se muestran sitio, posición o estado no encontrado para cada referencia, conservando el orden confirmado.

---

## 10. Flujos alternativos

### FA-01 — Imagen con orientación EXIF

La aplicación gira el bitmap antes del reconocimiento y continúa el flujo normal.

### FA-02 — Imagen demasiado grande

La aplicación reduce la imagen hasta un máximo de `2200 px` por lado antes del OCR.

### FA-03 — Error al abrir la imagen

Se publica el estado `IMAGE_ERROR` y no se inicia el reconocimiento.

### FA-04 — Error de procesamiento u OCR

Se publica `RECOGNITION_ERROR`, se conserva la imagen y se permite reintentar.

### FA-05 — OCR sin texto

Se publica `NO_TEXT_FOUND` y se permite cambiar la imagen o reintentar.

### FA-06 — Segunda petición durante procesamiento

La petición adicional se ignora para impedir operaciones duplicadas.

### FA-07 — Resultado antiguo

Si finaliza una petición que ya no es la vigente, su imagen procesada se cierra y el estado no se modifica.

### FA-08 — Lista de una columna

Si no existe una separación horizontal suficientemente clara, las líneas se ordenan verticalmente sin dividir el documento.

### FA-09 — Lista de dos columnas

Las líneas se separan en grupos y se leen por columnas.

### FA-10 — Categoría separada del código

Si una línea contiene un fragmento compatible con categoría y la siguiente completa la referencia, ambas se combinan para el análisis.

### FA-11 — Código de tres cifras

La referencia se acepta con las mismas reglas de normalización y revisión que los códigos de cuatro o cinco cifras.

### FA-12 — Cantidad unida a la referencia

La unidad reconocida se separa y no se usa para buscar mercancía.

### FA-13 — Coincidencia exacta

La propuesta se marca como `EXACT` y no muestra sugerencias.

### FA-14 — Una sugerencia

La propuesta se marca como `UNIQUE_SUGGESTION`, pero no se reemplaza automáticamente.

### FA-15 — Varias sugerencias

La propuesta se marca como `AMBIGUOUS` y el usuario selecciona o edita.

### FA-16 — Sin coincidencias

La propuesta se marca como `NO_MATCH` y permanece disponible para edición manual.

### FA-17 — Inventario no disponible

Una referencia formalmente válida se marca como `UNVERIFIED`.

### FA-18 — Edición o sugerencia aplicada

Solo cambia la propuesta seleccionada y su estado pasa a `USER_CONFIRMED`.

### FA-19 — Aviso ya mostrado

El aviso experimental no vuelve a abrirse dentro de la misma sesión.

### FA-20 — Confirmación destructiva incorrecta

El botón `Eliminar` permanece deshabilitado y el campo muestra que el texto no coincide.

---

## 11. Criterios de aceptación

### CA-01 — Corrección de orientación

Dada una imagen con orientación EXIF de `90°`, `180°` o `270°`, cuando se procese, entonces el bitmap entregado al OCR estará orientado correctamente.

### CA-02 — Límite de tamaño

Dada una imagen grande, cuando se prepare para OCR, entonces ningún lado del bitmap final superará `2200 px`.

### CA-03 — Mejora visual moderada

Dada una imagen válida, cuando se procese, entonces se convertirá a escala de grises y se aplicará contraste `1.18`.

### CA-04 — Gestión de errores

Dada una URI inválida o un fallo de procesamiento, cuando finalice la operación, entonces la interfaz mostrará un estado controlado y no cerrará inesperadamente la aplicación.

### CA-05 — Filas reconstruidas

Dados varios elementos situados en la misma fila, cuando se reconstruya el documento, entonces se unirán en orden horizontal.

### CA-06 — Filas separadas

Dados elementos situados en alturas diferentes, cuando se reconstruya el documento, entonces permanecerán en líneas diferentes.

### CA-07 — Dos columnas

Dada una lista con separación horizontal suficiente y al menos dos líneas por columna, cuando se reconstruya, entonces se leerá primero la columna izquierda y después la derecha.

### CA-08 — Una columna

Dada una lista normal sin hueco estructural, cuando se reconstruya, entonces no se dividirá artificialmente en columnas.

### CA-09 — Texto bruto consultable

Dado un resultado reconocido, cuando el usuario pulse la acción correspondiente, entonces podrá mostrar u ocultar el texto original detectado.

### CA-10 — Código variable

Dada una referencia con dos letras y código de tres, cuatro o cinco cifras, cuando se analice, entonces se aceptará como formato válido.

### CA-11 — Cantidad separada

Dada una referencia seguida o unida a una unidad reconocida, cuando se analice, entonces la unidad no formará parte de la referencia.

### CA-12 — Ceros iniciales

Dado un código con ceros iniciales, cuando se procese, entonces dichos ceros se conservarán.

### CA-13 — Sugerencias plausibles

Dado un candidato OCR imperfecto, cuando se compare con Room, entonces solo se sugerirán referencias compatibles con las confusiones previstas.

### CA-14 — Sin sugerencias irrelevantes

Dado un candidato sin relación plausible con el inventario, cuando se compare, entonces no se completará la lista con alternativas arbitrarias.

### CA-15 — Sin autocorrección

Dada una o varias sugerencias, cuando se muestre la revisión, entonces la referencia observada permanecerá sin cambios hasta que el usuario actúe.

### CA-16 — Clasificación exacta

Dada una referencia válida existente en el inventario, cuando se revise, entonces se mostrará como coincidencia exacta.

### CA-17 — Clasificación única

Dado un único candidato plausible, cuando se revise, entonces se mostrará como posible coincidencia única.

### CA-18 — Clasificación ambigua

Dados varios candidatos plausibles, cuando se revise, entonces se indicará que existen varias coincidencias.

### CA-19 — Clasificación sin coincidencia

Dado un candidato sin coincidencias, cuando se revise, entonces se solicitará corrección manual.

### CA-20 — Decisión del usuario

Dada una edición, adición o sugerencia seleccionada, cuando se aplique, entonces la propuesta quedará marcada como revisada manualmente.

### CA-21 — Orden y deduplicación

Dada una lista revisada, cuando se confirme, entonces se conservará el orden y no existirán identidades duplicadas.

### CA-22 — Aviso por sesión

Dado el primer acceso al procesador de listas en una sesión, cuando se abra la pantalla, entonces se mostrará el aviso experimental una sola vez.

### CA-23 — Eliminación reforzada

Dada una eliminación individual o múltiple, cuando el texto no sea exactamente `delete`, entonces el botón destructivo permanecerá deshabilitado.

### CA-24 — Funcionamiento offline

Dado el dispositivo sin conexión, cuando se procese y revise una lista, entonces el flujo utilizará ML Kit local y Room sin depender de red.

### CA-25 — Integración con #28

Dada una lista confirmada, cuando continúe el flujo, entonces se abrirá la consulta de ubicaciones de HU-25 sin reinterpretar la referencia.

---

## 12. Diseño técnico implementado

### Núcleo documental

```text
DocumentImage
DocumentImageProcessor
DocumentImageProcessingCallback
DocumentTextRecognizer
RecognizedDocument
RecognizedTextElement
RecognizedTextLine
```

### Implementación Android y OCR

```text
AndroidDocumentImage
AndroidDocumentImageLoader
AndroidDocumentImageProcessor
MlKitDocumentTextRecognizer
DocumentLineReconstructor
DocumentColumnDetector
```

### Dominio de referencias

```text
WarehouseReferenceParser
WarehouseReference
WarehouseReferenceMatch
WarehouseItemNormalizer
```

### Presentación

```text
ReferenceListCaptureActivity
ReferenceListCaptureViewModel
ReferenceListCaptureUiState
ReferenceListReviewViewModel
ReferenceProposal
ReferenceListReviewAdapter
DeleteConfirmationDialog
```

### Composición

`ReferenceListModule` proporciona:

- `AndroidDocumentImageProcessor`;
- `MlKitDocumentTextRecognizer`;
- `DocumentLineReconstructor`;
- factories actualizadas para los ViewModel;
- repositorio local para contrastar referencias.

La separación entre procesamiento y reconocimiento permite distinguir:

```text
preparar bitmap
        ↓
reconocer elementos
        ↓
reconstruir estructura
        ↓
interpretar referencias
        ↓
clasificar con Room
```

---

## 13. Pruebas incorporadas o ampliadas

### Reconstrucción documental

`DocumentLineReconstructorTest` verifica:

- unión de elementos de una misma fila;
- orden horizontal;
- separación de filas;
- orden vertical;
- descarte de elementos vacíos;
- separación de dos columnas;
- lectura completa de la izquierda antes de la derecha;
- ausencia de división falsa en una lista de una columna.

### Modelo espacial

`RecognizedTextElementTest` verifica:

- cálculo de dimensiones y centro;
- rechazo de límites horizontales inválidos;
- rechazo de límites verticales inválidos.

### Parser

`WarehouseReferenceParserTest` amplía la cobertura de:

- códigos de tres, cuatro y cinco cifras;
- varias referencias y conservación del orden;
- ceros iniciales;
- sufijos unidos o separados;
- cantidades delimitadas con guion;
- unidades de cantidad;
- candidatos OCR imperfectos;
- confusiones plausibles en categoría y código;
- máximo de una corrección numérica fiable;
- rechazo de cambios no relacionados;
- ausencia de sustituciones globales de dígitos;
- eliminación de unidades unidas al código.

### Captura

`ReferenceListCaptureViewModelTest` verifica:

- estado inicial;
- selección de imagen;
- publicación de líneas reconocidas;
- estado sin texto;
- bloqueo de una segunda petición;
- error controlado de apertura;
- conservación de imagen ante error y reintento;
- integración del procesador anterior al reconocedor.

### Revisión

`ReferenceListReviewViewModelTest` amplía la cobertura de:

- extracción y orden;
- deduplicación;
- sugerencias sin autocorrección;
- combinación de entradas divididas;
- sustitución exclusiva de la propuesta seleccionada;
- ausencia de sugerencias para coincidencias exactas;
- clasificación sin coincidencia;
- conservación de todos los candidatos OCR;
- separación de código y sufijo;
- descarte de unidades;
- limpieza del estado pendiente tras edición.

### Validación ejecutada

Comando:

```text
.\gradlew.bat clean
.\gradlew.bat testDebugUnitTest
```

Resultado:

```text
BUILD SUCCESSFUL
25 actionable tasks: 25 executed
```

---

## 14. Tareas de implementación completadas

- [x] Crear un contrato independiente de preprocesamiento.
- [x] Leer y aplicar orientación EXIF.
- [x] Limitar el bitmap usado por OCR.
- [x] Aplicar escala de grises y contraste moderado.
- [x] Gestionar recursos y errores de imagen.
- [x] Incorporar coordenadas a los elementos reconocidos.
- [x] Reconstruir filas por posición.
- [x] Detectar y ordenar listas de dos columnas.
- [x] Mantener un fallback con las líneas originales de ML Kit.
- [x] Mostrar resultado preliminar reconstruido.
- [x] Permitir mostrar u ocultar el texto bruto.
- [x] Admitir códigos de tres a cinco cifras.
- [x] Separar unidades de cantidad.
- [x] Restringir sugerencias a confusiones OCR plausibles.
- [x] Evitar la autocorrección silenciosa.
- [x] Contrastar propuestas con las referencias de Room.
- [x] Clasificar coincidencias en la pantalla de revisión.
- [x] Mantener orden y deduplicación.
- [x] Mostrar aviso experimental una vez por sesión.
- [x] Reforzar la eliminación individual y múltiple.
- [x] Mejorar descripciones de navegación y textos.
- [x] Añadir `ExifInterface`.
- [x] Excluir esquemas generados de Room.
- [x] Añadir y ampliar pruebas unitarias.
- [x] Fusionar el resultado en `develop`.

---

## 15. Definición de terminado

La extensión se considera terminada porque:

- el rango comienza después del merge final de `#28`;
- los seis commits funcionales de `#29` están presentes;
- el OCR recibe imágenes orientadas, escaladas y mejoradas;
- la estructura espacial se reconstruye antes de analizar referencias;
- las listas de dos columnas conservan un orden de lectura coherente;
- los códigos de tres a cinco cifras son compatibles;
- las cantidades no contaminan la identidad de mercancía;
- las sugerencias se limitan a errores OCR plausibles;
- el usuario sigue controlando cualquier corrección;
- las propuestas muestran su estado frente al inventario;
- el escáner advierte de su carácter experimental;
- la eliminación destructiva exige verificación escrita;
- la funcionalidad continúa siendo local y offline;
- la consulta de ubicaciones de `#28` permanece integrada;
- la suite `testDebugUnitTest` finaliza correctamente.

---

## 16. Resultado final

La issue `#29` convierte la implementación inicial de HU-25 en un flujo de listas más robusto y comprensible.

El sistema no se limita a leer texto: prepara la imagen, conserva la geometría detectada, reconstruye el orden documental, separa referencias de cantidades, contrasta el resultado con Room y explica al usuario el nivel de confianza de cada propuesta.

Resultado funcional consolidado:

```text
imagen
  → preprocesamiento local
  → OCR con coordenadas
  → reconstrucción de filas y columnas
  → extracción tolerante y controlada
  → clasificación frente al inventario
  → revisión explícita del usuario
  → lista confirmada
```

La mejora reduce falsos positivos sin convertir las sugerencias en correcciones automáticas y mantiene la fuente de verdad en Room y en la confirmación final del usuario.
