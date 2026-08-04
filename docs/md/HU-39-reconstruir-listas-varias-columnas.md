# HU-39 — Reconstruir listas de varias columnas

> Séptima historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-39  
**Nombre:** Reconstruir listas de varias columnas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-39-reconstruir-varias-columnas`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-38 — Corregir la orientación de imágenes antes del OCR  
**Issue prevista:** `#43`

---

## 2. Historia de usuario

Como usuario,  
quiero procesar listas que distribuyen sus referencias en más de dos columnas,  
para revisar todas las referencias en el orden correcto sin que se mezclen columnas distintas.

---

## 3. Objetivo

Evolucionar la reconstrucción documental existente para detectar y ordenar una cantidad variable de columnas cuando exista evidencia espacial suficiente.

Flujo previsto:

```text
PP-OCRv5
        ↓
RecognizedTextElement
        ↓
agrupación en filas visuales
        ↓
RecognizedTextLine con coordenadas
        ↓
detección de estructura columnar
        ↓
columna 1
columna 2
columna 3
...
        ↓
lectura vertical de cada columna
        ↓
orden global de izquierda a derecha
        ↓
RecognizedDocument
        ↓
revisión vigente
```

Ejemplo de tres columnas:

```text
MR 1210      MA 2300      ML 4170
MZ 1300      MD 2400      MI 5100
MR 1500      MA 2500
```

Orden esperado:

```text
MR 1210
MZ 1300
MR 1500
MA 2300
MD 2400
MA 2500
ML 4170
MI 5100
```

HU-39 deberá conservar el comportamiento correcto para una y dos columnas.

---

## 4. Regla principal

> Una columna solo se reconocerá cuando exista evidencia espacial repetida y estable.

La aplicación no deberá crear columnas basándose únicamente en:

- un hueco aislado;
- una línea muy larga;
- un título centrado;
- una cantidad separada;
- una unidad ubicada lejos del código;
- una sola referencia desplazada;
- el número esperado de columnas;
- una constante rígida de cuatro columnas.

Cuando la estructura sea ambigua:

```text
degradación segura
        ↓
orden vertical estable
        ↓
revisión manual
```

Es preferible no detectar una columna a mezclar referencias de columnas diferentes.

---

## 5. Base documental y arquitectónica

HU-39 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-37-integrar-ppocrv5-reconstruccion-revision-listas.md`;
- `HU-38-corregir-orientacion-imagenes-antes-ocr.md`;
- el estado real de `AlmacenTrackerHU38.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- `DocumentLineReconstructor` como responsable de reconstruir líneas;
- `DocumentColumnDetector` como componente de análisis columnar;
- `RecognizedTextElement` y `RecognizedTextLine` como modelos existentes;
- PP-OCRv5 como proveedor de texto y coordenadas;
- `RecognizedDocument` como resultado común;
- la lectura por columnas completas;
- la revisión manual obligatoria;
- el funcionamiento offline;
- la conservación del orden;
- la política de no crear clases sin responsabilidad real.

El plan de v1.4 asigna a HU-39:

```text
cantidad variable de columnas
+
agrupación espacial estable
+
reconstrucción independiente
+
títulos o líneas globales
+
lectura vertical por columna
+
orden izquierda a derecha
+
columnas desiguales
+
prevención de mezcla
+
pruebas con tres y cuatro columnas
+
degradación segura
```

---

## 6. Estado real antes de HU-39

El análisis de `AlmacenTrackerHU38.zip` confirma:

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
merge HU38 #42 into develop
```

HU-38 ya aporta:

- lectura EXIF;
- giro manual a izquierda y derecha;
- `DocumentImageProcessingRequest`;
- `DocumentImageRotation`;
- previsualización coherente;
- reprocesamiento con la orientación elegida;
- invalidación de resultados anteriores;
- conservación de cámara y Photo Picker.

Por tanto, HU-39 puede asumir que el documento llega al OCR con una orientación revisada por el usuario.

---

## 7. Estado real de la reconstrucción

El proyecto dispone de:

```text
data/document/
├── DocumentLineReconstructor.java
└── DocumentColumnDetector.java
```

También dispone de:

```text
core/document/
├── RecognizedTextElement.java
├── RecognizedTextLine.java
└── RecognizedDocument.java
```

`DocumentLineReconstructor` actualmente:

1. limpia elementos vacíos;
2. ordena por `top` y `left`;
3. agrupa elementos compatibles verticalmente;
4. divide filas visuales por huecos horizontales grandes;
5. reconstruye el texto de cada fila;
6. delega el orden columnar a `DocumentColumnDetector`;
7. reindexa el resultado final.

La agrupación ya utiliza:

```text
solapamiento vertical
+
distancia entre centros
+
altura media
```

La separación horizontal ya considera:

```text
ancho del documento
+
anchura media de caracteres
+
inicio de una segunda referencia
```

HU-39 deberá reutilizar esta base y modificar únicamente lo necesario.

---

## 8. Limitación real de `DocumentColumnDetector`

La implementación actual:

```text
detectDivision(...)
```

busca:

```text
un único hueco horizontal máximo
```

y produce como máximo:

```text
columna izquierda
columna derecha
```

Después:

```text
ordena izquierda verticalmente
+
ordena derecha verticalmente
```

Constantes actuales:

```text
MINIMUM_LINES_PER_COLUMN = 2
MINIMUM_COLUMN_GAP_FACTOR = 0.16
```

Limitaciones:

- solo admite una división;
- no representa varias columnas;
- no conserva una estructura explícita de columna;
- no distingue líneas globales;
- asigna líneas sin caja a la columna izquierda;
- no valida repetición espacial de cada división;
- no maneja tres o cuatro grupos;
- no evalúa columnas con distinto número de filas;
- puede confundir un hueco grande aislado con una división global.

HU-39 deberá cerrar estas limitaciones sin trasladar reglas de columnas al ViewModel.

---

## 9. Estado real de las pruebas

`DocumentLineReconstructorTest` ya cubre:

- unión de elementos de una misma fila;
- orden horizontal;
- separación de filas;
- orden vertical;
- elementos vacíos;
- separación de dos columnas;
- lectura completa de izquierda a derecha;
- filas normales de una columna;
- calificadores alfabéticos;
- listas reales de dos columnas;
- prevención de dos inicios de referencia en una misma línea.

Antes de HU-39 no existen pruebas específicas para:

- tres columnas;
- cuatro columnas;
- número variable de columnas;
- columnas con distinto número de filas;
- título global;
- línea global intermedia;
- columna vacía parcial;
- falsos huecos;
- ambigüedad estructural;
- orden estable con varias divisiones.

---

## 10. Alcance incluido

HU-39 incluye:

- evolucionar `DocumentColumnDetector`;
- detectar una cantidad variable de columnas;
- no imponer un máximo funcional rígido de cuatro;
- analizar posiciones horizontales de líneas;
- detectar agrupaciones espaciales repetidas;
- calcular límites o centros de columnas;
- exigir evidencia mínima por columna;
- exigir separación horizontal suficiente;
- tolerar anchuras de columna diferentes;
- tolerar distinto número de filas;
- ordenar columnas de izquierda a derecha;
- ordenar líneas de cada columna de arriba abajo;
- preservar orden determinista;
- conservar una sola columna;
- conservar dos columnas;
- soportar tres columnas;
- soportar cuatro columnas;
- tratar títulos a ancho completo;
- tratar líneas globales;
- evitar mezclar títulos con referencias;
- evitar mezclar columnas adyacentes;
- evitar crear columnas por cantidades o unidades;
- evitar dividir una referencia completa;
- mantener `rawText`;
- mantener `reconstructedText`;
- mantener coordenadas;
- reindexar el resultado final;
- degradar a orden vertical ante ambigüedad;
- no modificar PP-OCRv5;
- no modificar parser de referencias;
- no modificar Room;
- no modificar navegación;
- no modificar historial;
- conservar revisión manual;
- funcionar offline;
- pruebas unitarias;
- pruebas de integración;
- pruebas instrumentadas cuando aporten valor;
- pruebas manuales;
- regresión;
- CI.

---

## 11. Alcance excluido

HU-39 no incluye:

- modificar modelos ONNX;
- volver a detectar texto;
- cambiar umbrales del detector OCR sin evidencia;
- cambiar decodificación CTC;
- interpretar referencias con guion;
- resolver referencias especiales mediante Room;
- extraer destinos;
- interpretar símbolos `①`, `②` o `③`;
- añadir unidad abreviada `P`;
- modificar la identidad funcional;
- ampliar categorías a tres letras;
- crear una pantalla para elegir número de columnas;
- pedir al usuario que dibuje divisiones;
- guardar una plantilla por documento;
- permitir arrastrar líneas entre columnas;
- procesar tablas genéricas;
- procesar celdas con encabezados complejos;
- detectar filas y columnas de una hoja de cálculo;
- gestionar stock;
- almacenar fotografías;
- persistir coordenadas en Room;
- evaluar todavía precisión global;
- optimizar rendimiento avanzado;
- retirar ML Kit;
- reducir tamaño de APK;
- procesar PDF;
- procesar varias páginas.

Las referencias especiales y destinos pertenecen a HU-40.

La evaluación pertenece a HU-41.

La optimización pertenece a HU-42.

---

## 12. Decisión sobre el componente principal

Se mantendrá:

```text
DocumentColumnDetector
```

como componente principal.

No se creará:

```text
MultiColumnDocumentColumnDetector
```

porque sustituiría una responsabilidad ya existente y duplicaría nombres.

La clase actual deberá evolucionar desde:

```text
detectar una división
```

hacia:

```text
detectar una estructura de columnas
```

El nombre existente sigue representando correctamente la responsabilidad.

---

## 13. Resultado interno de detección

Se recomienda introducir un modelo interno:

```text
DetectedDocumentColumns
```

o:

```text
ColumnLayout
```

solo si evita devolver listas anidadas sin significado.

Datos orientativos:

```text
columns
globalLines
confidence o ambiguity flag
```

Cada columna podrá representarse mediante:

```text
DocumentColumn
├── left
├── right
├── centerX
└── lines
```

Reglas:

- clases internas o package-private;
- no exponerlas a UI;
- no añadirlas a `core` si solo sirven al algoritmo;
- colecciones defensivas;
- columnas ordenadas;
- líneas ordenadas;
- sin dependencia de Android.

Si la implementación puede mantenerse clara con listas locales, no se crearán modelos por simetría.

---

## 14. No añadir `columnIndex` al dominio sin necesidad

No se recomienda añadir directamente:

```text
columnIndex
```

a `RecognizedTextLine`.

Motivos:

- el índice de columna es un dato derivado;
- la pantalla actual solo necesita el orden final;
- ampliar el modelo afectaría múltiples constructores y pruebas;
- la estructura puede ser ambigua;
- no se persiste;
- no participa en el parser.

Solo deberá añadirse si las pruebas demuestran que el orden lineal no basta para una historia posterior.

Decisión inicial:

```text
detectar columnas internamente
+
devolver líneas en orden final
```

---

## 15. Fuente de evidencia espacial

La detección deberá utilizar datos disponibles:

```text
left
right
top
bottom
centerX
width
height
```

La fuente preferida será:

```text
RecognizedTextLine
```

después de reconstruir filas funcionales.

No deberá basarse en:

- contenido semántico;
- categoría;
- códigos conocidos;
- cantidades;
- Room;
- longitud textual;
- número esperado de referencias.

La detección columnar será geométrica.

---

## 16. Centros horizontales

Para cada línea con caja:

```text
centerX = (left + right) / 2
```

Los centros permiten identificar concentraciones horizontales.

Sin embargo, no bastan por sí solos.

Dos líneas pueden tener centros parecidos aunque:

- una sea título ancho;
- una cruce varias columnas;
- una tenga texto muy largo;
- una columna tenga referencias de anchuras distintas.

La detección deberá considerar también:

```text
left
right
anchura
solapamiento horizontal
```

---

## 17. Líneas elegibles para detectar columnas

Una línea podrá participar en el cálculo cuando:

- tenga caja válida;
- tenga texto no vacío;
- no ocupe una proporción excesiva del documento;
- no cruce claramente varias agrupaciones;
- tenga anchura compatible con otras líneas candidatas.

Una línea no elegible podrá conservarse como:

```text
línea global
```

o incluirse en degradación vertical.

No se deberá eliminar del documento.

---

## 18. Línea global

Una línea global es una línea que:

- ocupa una parte significativa del ancho;
- se extiende sobre varias columnas;
- funciona como título o separador;
- no pertenece claramente a una única columna.

Ejemplos:

```text
LISTA DE REPOSICIÓN
TIENDA CENTRO
PEDIDO 4 DE AGOSTO
```

La detección no deberá usar estas líneas para decidir el número de columnas.

---

## 19. Posición de las líneas globales

Una línea global deberá conservar su posición vertical relativa.

Ejemplo:

```text
LISTA DE REPOSICIÓN
[columna 1] [columna 2] [columna 3]
```

Orden:

```text
LISTA DE REPOSICIÓN
columna 1 completa
columna 2 completa
columna 3 completa
```

Cuando una línea global aparece entre dos bloques:

```text
BLOQUE A
columnas del bloque A
BLOQUE B
columnas del bloque B
```

no deberá moverse al inicio o al final del documento.

Esto implica que HU-39 deberá considerar:

```text
segmentos verticales
```

cuando existan líneas globales intermedias.

---

## 20. Segmentación por líneas globales

Estrategia recomendada:

1. identificar líneas globales;
2. ordenar todas las líneas verticalmente;
3. dividir el documento en bloques entre líneas globales;
4. detectar columnas de forma independiente dentro de cada bloque;
5. insertar la línea global en su posición;
6. concatenar los bloques.

Ejemplo:

```text
Título A
bloque de tres columnas
Subtítulo B
bloque de dos columnas
```

Resultado:

```text
Título A
columnas del bloque A
Subtítulo B
columnas del bloque B
```

No se deberá asumir una única estructura columnar para toda la altura del documento.

---

## 21. Criterio inicial de línea global

Criterio orientativo:

```text
lineWidth / documentWidth
```

Una línea podrá considerarse global cuando:

- supere un umbral alto de anchura; o
- solape horizontalmente varias columnas detectadas.

El umbral exacto deberá surgir de pruebas.

No se fijará una constante sin cubrir:

- documentos estrechos;
- capturas recortadas;
- títulos cortos centrados;
- referencias largas.

Se recomienda combinar anchura relativa y posición.

---

## 22. Detección de agrupaciones

La implementación podrá utilizar:

- huecos significativos entre centros ordenados;
- agrupación por proximidad horizontal;
- clustering unidimensional determinista;
- intervalos de ocupación;
- combinación de estos criterios.

No deberá introducir una dependencia externa de machine learning para resolver agrupación 1D.

La estrategia debe ser:

- pequeña;
- determinista;
- comprobable;
- adecuada al número reducido de líneas;
- sin aleatoriedad.

---

## 23. Alternativa recomendada: huecos múltiples

Evolución directa del algoritmo actual:

1. obtener centros elegibles;
2. ordenarlos;
3. calcular todos los huecos consecutivos;
4. seleccionar huecos suficientemente grandes;
5. usar cada hueco como límite provisional;
6. formar columnas;
7. validar cada columna;
8. descartar divisiones débiles;
9. repetir validación hasta obtener una estructura estable.

Ventajas:

- reutiliza el enfoque actual;
- no requiere librerías;
- permite tres o más columnas;
- es determinista.

Limitación:

- necesita validación adicional para no detectar huecos internos falsos.

---

## 24. Separación mínima

La separación mínima no deberá depender solo de:

```text
documentWidth * factor
```

También deberá considerar:

- anchura media de línea;
- anchura media de caracteres;
- proximidad entre cajas;
- repetición del hueco en varias alturas.

Un documento de cuatro columnas tendrá huecos menores que uno de dos columnas.

Mantener únicamente:

```text
MINIMUM_COLUMN_GAP_FACTOR = 0.16
```

podría impedir detectar cuatro columnas.

HU-39 deberá revisar esa regla.

---

## 25. Evidencia vertical repetida

Una división será más fiable cuando el espacio entre columnas aparezca en varias filas.

Ejemplo estable:

```text
A1      B1      C1
A2      B2      C2
A3      B3
```

Ejemplo débil:

```text
A1                 texto auxiliar
A2
A3
```

La implementación deberá favorecer divisiones respaldadas por varias líneas.

No se requiere una cuadrícula perfecta.

---

## 26. Mínimo de líneas por columna

La regla actual exige:

```text
2 líneas por columna
```

HU-39 deberá conservar un mínimo razonable, pero permitir columnas desiguales.

Ejemplo válido:

```text
columna 1 = 4 líneas
columna 2 = 3 líneas
columna 3 = 1 línea
```

La tercera columna con una sola línea puede ser real si:

- su posición coincide con una columna detectada en otro bloque;
- la separación es clara;
- no parece texto auxiliar.

Sin evidencia adicional, una sola línea desplazada no deberá crear una columna nueva.

---

## 27. Columnas con diferente número de filas

No se deberá exigir:

```text
same row count
```

Ejemplo:

```text
columna 1: 5 referencias
columna 2: 4 referencias
columna 3: 2 referencias
```

Debe conservarse:

```text
1 completa
2 completa
3 completa
```

No se insertarán filas vacías.

No se emparejarán líneas por índice vertical.

---

## 28. Anchuras diferentes

Las columnas pueden contener:

```text
MR 1210
ML 4170 BLACK
M873-12 - 1P
```

Por tanto, sus cajas pueden tener anchuras distintas.

La asignación a columna deberá usar principalmente:

- zona horizontal;
- inicio de línea;
- centro razonable;
- solapamiento con el intervalo de columna.

No deberá exigir anchuras idénticas.

---

## 29. Anclaje por borde izquierdo

Para listas alineadas, el borde izquierdo suele ser más estable que el centro.

HU-39 deberá evaluar el uso de:

```text
line.left
```

como característica principal o complementaria.

Ventajas:

- referencias largas no desplazan el grupo;
- títulos anchos se detectan mejor como globales;
- columnas con códigos largos conservan el mismo inicio.

No se deberá sustituir el centro sin pruebas.

La estrategia puede combinar:

```text
left + centerX
```

---

## 30. Asignación de líneas a columnas

Después de detectar intervalos:

1. calcular solapamiento de cada línea con cada columna;
2. usar mayor solapamiento cuando sea suficiente;
3. usar distancia al ancla como desempate;
4. marcar ambigua cuando cruza varias columnas;
5. no asignar silenciosamente a la primera columna.

Una línea sin caja deberá conservarse mediante degradación segura.

---

## 31. Prevención de mezcla

Una línea no deberá incorporarse a una columna cuando:

- su centro está fuera del intervalo;
- cruza dos columnas de forma significativa;
- se encuentra más cerca de otro grupo;
- funciona como línea global;
- carece de evidencia suficiente.

En caso de ambigüedad:

```text
layout ambiguo
        ↓
fallback
```

No se deberá mezclar parcialmente el documento.

---

## 32. Política de fallback

El fallback inicial será:

```text
orden vertical
+
desempate horizontal
```

igual al comportamiento seguro actual.

No se devolverá:

- lista vacía;
- excepción;
- orden aleatorio;
- columnas parciales mezcladas.

La detección ambigua no es un error de OCR.

El usuario conservará la revisión manual.

---

## 33. Fallback por bloque

Cuando existan líneas globales y un bloque sea ambiguo:

- el bloque ambiguo podrá usar orden vertical;
- otros bloques claramente columnares podrán conservar su orden por columnas.

Esto es preferible a degradar todo el documento si solo una sección es ambigua.

La decisión deberá mantenerse determinista.

---

## 34. Lectura dentro de cada columna

Orden:

```text
top ASC
left ASC
source order cuando sea necesario
```

Los índices anteriores no deberán imponerse si proceden de un orden provisional.

La posición vertical es la fuente principal.

---

## 35. Orden entre columnas

Orden:

```text
column.left ASC
```

Desempates:

```text
centerX ASC
primer sourceOrder
```

El resultado no deberá depender del orden de inserción en un mapa.

---

## 36. Reindexado final

`DocumentLineReconstructor.reindex(...)` deberá seguir generando:

```text
0
1
2
...
```

después de aplicar:

- líneas globales;
- bloques;
- columnas;
- fallback.

Los índices deberán coincidir con el orden enviado a revisión.

---

## 37. Conservación de elementos

Cada `RecognizedTextLine` deberá conservar:

```text
elements
rawText
reconstructedText
boundingBox
```

HU-39 modifica el orden, no el contenido reconocido.

No deberá reconstruir de nuevo el texto después de asignar columnas.

---

## 38. Relación con `splitRowsByLargeHorizontalGaps`

El flujo actual divide una fila visual cuando:

- aparece una segunda referencia;
- existe un hueco horizontal grande.

HU-39 deberá revisar la interacción entre:

```text
separación de filas visuales
```

y:

```text
detección de columnas
```

Si una fila visual contiene tres referencias alineadas:

```text
MR 1210      MA 2300      ML 4170
```

debe producir tres líneas antes de detectar columnas.

No deberá quedar:

```text
"MR 1210 MA 2300 ML 4170"
```

como una sola línea.

---

## 39. Varios inicios de referencia

La regla existente:

```text
reconstructNeverKeepsTwoReferenceStartsInSameLine
```

deberá ampliarse mediante pruebas para:

```text
tres inicios
cuatro inicios
```

El parser no debe ser quien descubra la estructura columnar después de perder coordenadas.

La separación deberá ocurrir durante reconstrucción.

---

## 40. Texto que no comienza por referencia

No todas las columnas empiezan necesariamente por una referencia reconocible.

Ejemplos:

```text
4 CAJAS
OBSERVACIÓN
PENDIENTE
```

La detección de columnas no deberá depender de `WarehouseReferenceParser`.

Sin embargo, `splitRowsByLargeHorizontalGaps` podrá mantener sus reglas funcionales actuales para separar referencias.

HU-39 debe distinguir:

```text
separación de línea
```

de:

```text
detección de columna
```

---

## 41. Documento de una columna

Para una lista normal:

```text
MR 1210
MZ 1300
ML 4170
```

Resultado esperado:

```text
orden vertical original
```

HU-39 no deberá crear varias columnas por pequeñas variaciones de alineación.

---

## 42. Documento de dos columnas

El comportamiento existente deberá mantenerse.

Entrada visual:

```text
MR 1210      MA 2300
MZ 1300      MD 2400
```

Resultado:

```text
MR 1210
MZ 1300
MA 2300
MD 2400
```

Las pruebas anteriores no deberán romperse.

---

## 43. Documento de tres columnas

Caso mínimo:

```text
MR 1210      MA 2300      ML 4170
MZ 1300      MD 2400      MI 5100
MR 1500      MA 2500
```

Resultado:

```text
MR 1210
MZ 1300
MR 1500
MA 2300
MD 2400
MA 2500
ML 4170
MI 5100
```

La prueba deberá construir coordenadas realistas.

---

## 44. Documento de cuatro columnas

Caso mínimo:

```text
A1      B1      C1      D1
A2      B2      C2      D2
A3              C3
```

Resultado:

```text
A1
A2
A3
B1
B2
C1
C2
C3
D1
D2
```

Los textos de prueba podrán usar referencias válidas para verificar integración.

No se deberá programar:

```text
MAX_COLUMNS = 4
```

solo porque la aceptación exige probar cuatro.

---

## 45. Título global superior

Entrada:

```text
LISTA DE REPOSICIÓN

MR 1210      MA 2300      ML 4170
MZ 1300      MD 2400      MI 5100
```

Resultado:

```text
LISTA DE REPOSICIÓN
MR 1210
MZ 1300
MA 2300
MD 2400
ML 4170
MI 5100
```

El título no deberá convertirse en una columna independiente.

---

## 46. Línea global intermedia

Entrada:

```text
BLOQUE NORTE
[3 columnas]

BLOQUE SUR
[2 columnas]
```

Resultado:

```text
BLOQUE NORTE
columnas del bloque norte
BLOQUE SUR
columnas del bloque sur
```

No se deberá aplicar el layout de tres columnas al segundo bloque si solo tiene dos.

---

## 47. Título corto centrado

Un título puede no ocupar todo el ancho:

```text
URGENTE
```

pero estar centrado sobre varias columnas.

La detección deberá evitar crear una columna central solo por esa línea.

Criterios posibles:

- aislamiento vertical;
- centro cercano al documento;
- ausencia de otras líneas con el mismo ancla;
- anchura relativa;
- posición entre bloques.

No se requiere reconocer semánticamente la palabra.

---

## 48. Línea desplazada

Caso:

```text
MR 1210
MZ 1300
        NOTA
ML 4170
```

`NOTA` no deberá crear una segunda columna si es la única línea desplazada.

El layout deberá degradar o tratarla como global/auxiliar según evidencia.

---

## 49. Cantidad alejada

Caso:

```text
MR 1210                    4 CAJAS
MZ 1300                    2 PCS
```

Puede representar:

- dos columnas semánticas de una tabla; o
- referencia y cantidad de la misma fila.

HU-39 no debe alterar la asociación documental definida en HU-37.

La detección deberá priorizar la reconstrucción ya realizada.

Si `DocumentLineReconstructor` conserva cada fila como:

```text
MR 1210 4 CAJAS
```

no deberá dividirla después por columnas.

La historia no convierte listas tabulares de atributos en columnas de lectura independientes.

---

## 50. Diferencia entre columnas de referencias y campos de una fila

Columnas de lectura:

```text
MR 1210      MA 2300      ML 4170
```

Campos de una fila:

```text
MR 1210      4      CAJAS
```

HU-39 deberá apoyarse en la reconstrucción previa y en la repetición espacial.

No deberá tratar automáticamente cada alineación vertical como una columna de lectura.

Este es uno de los principales riesgos de falsos positivos.

---

## 51. Criterio conservador

Para considerar un grupo como columna de lectura se recomienda exigir:

- varias líneas candidatas;
- inicios horizontales repetidos;
- contenido lineal independiente;
- separación consistente;
- ausencia de líneas que enlacen los grupos;
- estructura repetida en altura.

Si no se cumple:

```text
fallback
```

---

## 52. Configuración

Los umbrales deberán centralizarse.

Opciones:

- constantes privadas en `DocumentColumnDetector`;
- `DocumentColumnDetectorConfiguration` si el número de parámetros crece y necesita pruebas independientes.

No se creará una configuración pública si solo existen dos valores simples.

La decisión dependerá del código final.

Valores conceptuales:

```text
minimumLinesPerColumn
minimumHorizontalGap
maximumAnchorDeviation
globalLineWidthRatio
minimumColumnEvidence
```

---

## 53. No hardcodear posiciones absolutas

No se deberán usar reglas como:

```text
columna 1 = x < 300
columna 2 = x < 600
columna 3 = x < 900
```

Las posiciones deben derivarse de:

```text
documentWidth
+
líneas detectadas
```

Debe funcionar con:

- capturas estrechas;
- fotografías grandes;
- distintos escalados;
- diferentes densidades;
- rotación aplicada en HU-38.

---

## 54. Coordenadas normalizadas

La implementación podrá normalizar:

```text
xNormalized = x / documentWidth
```

para comparar documentos de distintos tamaños.

No deberá perderse la coordenada original.

La normalización es interna al detector de columnas.

---

## 55. Determinismo

Con la misma colección:

```text
mismas columnas
mismo orden
mismos índices
```

No se utilizarán:

- clustering aleatorio;
- `HashMap` sin orden;
- paralelismo para ordenar;
- confianza OCR como desempate principal.

Las pruebas deberán repetir la operación con entradas desordenadas.

---

## 56. Inmutabilidad de la entrada

`DocumentColumnDetector` no deberá modificar:

```text
sourceLines
```

La implementación actual ya crea una copia.

HU-39 deberá conservar:

- copia defensiva;
- ausencia de cambios en índices de entrada;
- ausencia de cambios en elementos.

---

## 57. Tratamiento de líneas sin caja

Una línea sin bounding box no permite asignación fiable.

Política recomendada:

- conservarla;
- tratar el layout como ambiguo cuando afecte a la estructura;
- usar orden vertical;
- no enviarla automáticamente a la primera columna.

La política actual de añadirla a la izquierda deberá revisarse.

---

## 58. Entradas inválidas

Casos:

```text
null
lista vacía
documentWidth <= 0
líneas nulas
cajas inválidas
```

Comportamiento:

- lista vacía para entrada vacía;
- orden vertical seguro cuando no existe ancho;
- ignorar o rechazar líneas nulas según contrato vigente;
- no lanzar excepción por estructura no detectable.

Una estructura ambigua es un resultado válido, no un error técnico.

---

## 59. Complejidad

El número habitual de líneas es reducido.

Una estrategia:

```text
O(n²)
```

puede ser aceptable si mejora claridad y estabilidad.

No se deberá introducir una estructura compleja prematuramente para ahorrar microsegundos.

La optimización formal corresponde a HU-42.

---

## 60. Integración con `DocumentLineReconstructor`

La firma podrá mantenerse:

```java
List<RecognizedTextLine> orderByColumns(
        List<RecognizedTextLine> sourceLines,
        int documentWidth
)
```

Aunque el método admita varias columnas, el nombre sigue siendo correcto.

No es obligatorio renombrarlo a:

```text
orderByMultipleColumns
```

porque una y dos columnas también forman parte del comportamiento.

---

## 61. Integración con `PaddleOcrDocumentTextRecognizer`

No deberá requerir cambios funcionales.

Flujo vigente:

```text
TextRecognitionResult
        ↓
RecognizedTextElement
        ↓
DocumentLineReconstructor
        ↓
RecognizedDocument
```

Al evolucionar el reconstructor, PP-OCRv5 obtendrá automáticamente el nuevo orden.

No se añadirá lógica columnar dentro de:

```text
PaddleOcrDocumentTextRecognizer
```

---

## 62. Integración con revisión

`ReferenceListCaptureActivity` continuará enviando:

```text
getReconstructedLines()
```

a:

```text
ReferenceListReviewActivity
```

La revisión no necesita conocer:

- número de columnas;
- límites;
- cajas;
- columnas internas.

Recibirá el orden final.

---

## 63. Integración con parser

`WarehouseReferenceParser` y `DocumentReferenceDataParser` no deberán modificarse salvo que una prueba revele una regresión causada por el nuevo orden.

HU-39 no cambia:

- categoría;
- código;
- cantidad;
- unidad;
- sugerencias;
- coincidencias.

Solo cambia la reconstrucción espacial y el orden.

---

## 64. Integración con historial

El historial continuará conservando:

```text
orderIndex
```

según el orden confirmado.

Si el usuario confirma una lista de varias columnas:

```text
columna 1
columna 2
columna 3
```

ese será el orden documental persistido.

No se guardará:

```text
columnIndex
```

en Room durante HU-39.

---

## 65. Pruebas unitarias de `DocumentColumnDetector`

Casos mínimos:

- `null`;
- lista vacía;
- ancho inválido;
- una línea;
- una columna;
- dos columnas;
- tres columnas;
- cuatro columnas;
- columnas desiguales;
- líneas desordenadas;
- cajas de anchuras diferentes;
- título global;
- título corto centrado;
- línea global intermedia;
- una línea desplazada;
- hueco aislado;
- línea sin caja;
- estructura ambigua;
- resultado determinista;
- entrada no modificada.

Se recomienda crear:

```text
DocumentColumnDetectorTest
```

porque actualmente la detección se prueba indirectamente desde el reconstructor.

---

## 66. Pruebas de `DocumentLineReconstructor`

Casos mínimos:

- tres referencias en una misma fila visual;
- cuatro referencias en una misma fila visual;
- tres columnas completas;
- cuatro columnas completas;
- título global;
- subtítulo intermedio;
- columnas con distinto número de referencias;
- texto auxiliar;
- cantidades y unidades dentro de la fila;
- no dividir un código con calificador;
- no dividir una referencia con espacios;
- conservar pruebas de una y dos columnas;
- reindexado final.

---

## 67. Pruebas con elementos desordenados

La colección de `RecognizedTextElement` deberá mezclarse deliberadamente.

Ejemplo de inserción:

```text
columna 3 fila 2
columna 1 fila 1
columna 2 fila 3
columna 1 fila 2
...
```

El resultado deberá seguir siendo:

```text
columna 1 completa
columna 2 completa
columna 3 completa
```

Esto evita depender del `sourceOrder` provisional.

---

## 68. Prueba de tres columnas

La prueba debe verificar:

- tres grupos detectados;
- cada grupo ordenado verticalmente;
- grupos ordenados horizontalmente;
- diferente número de filas;
- índices consecutivos;
- texto intacto.

No basta con comprobar solo el primer y último elemento.

---

## 69. Prueba de cuatro columnas

La prueba debe verificar:

- cuatro grupos;
- separaciones menores que en dos columnas;
- ausencia de `MAX_COLUMNS = 4`;
- tolerancia a una columna corta;
- ausencia de mezcla.

---

## 70. Prueba de título global

La prueba debe verificar:

- título primero;
- título no participa en clustering;
- columnas detectadas correctamente;
- texto del título intacto;
- índice del título igual a `0`.

---

## 71. Prueba de bloque mixto

Entrada:

```text
Título A
3 columnas

Título B
2 columnas
```

Resultado:

```text
Título A
3 columnas en orden
Título B
2 columnas en orden
```

Esta prueba valida que no se fuerce una única estructura global.

---

## 72. Prueba de ambigüedad

Entrada con centros irregulares y sin separación repetida.

Resultado esperado:

```text
orden vertical
```

No:

```text
columnas inventadas
```

La prueba deberá comprobar que no se pierde ninguna línea.

---

## 73. Prueba de atributos alineados

Entrada:

```text
MR 1210        4 CAJAS
MZ 1300        2 PCS
```

Cuando el reconstructor ya une cada referencia con su cantidad, la detección de columnas no deberá separar cantidades como otra columna de lectura.

Esta prueba protege la integración con HU-37.

---

## 74. Prueba de regresión con dos columnas

Todas las pruebas actuales de dos columnas deberán continuar pasando.

No se aceptará una solución que soporte tres columnas pero cambie:

```text
izquierda completa
+
derecha completa
```

por lectura intercalada.

---

## 75. Prueba de integración OCR

Con una imagen de prueba de tres columnas:

```text
imagen
    ↓
PP-OCRv5
    ↓
RecognizedDocument
```

Se deberá comprobar:

- líneas detectadas;
- columnas reconstruidas;
- orden final;
- ausencia de mezcla;
- parser funcional;
- revisión operativa.

La inferencia real podrá probarse instrumentadamente.

---

## 76. Pruebas manuales

Casos mínimos:

- lista impresa de una columna;
- lista de dos columnas;
- lista de tres columnas;
- lista de cuatro columnas;
- columnas desiguales;
- título superior;
- subtítulo intermedio;
- referencia larga;
- cantidades y unidades;
- imagen rotada y corregida mediante HU-38;
- fotografía con perspectiva moderada;
- captura de pantalla;
- baja iluminación razonable;
- documento ambiguo;
- revisión manual;
- ubicaciones;
- preparación histórica;
- funcionamiento sin Internet.

---

## 77. Criterios de aceptación

### CA-01 — Una columna

**Dada** una lista de una columna,  
**cuando** se reconstruye,  
**entonces** conserva el orden vertical.

### CA-02 — Dos columnas

**Dada** una lista de dos columnas,  
**cuando** se reconstruye,  
**entonces** mantiene el comportamiento vigente de izquierda a derecha.

### CA-03 — Tres columnas

**Dada** una lista con tres columnas estables,  
**cuando** se reconstruye,  
**entonces** lee cada columna de arriba abajo y las concatena de izquierda a derecha.

### CA-04 — Cuatro columnas

**Dada** una lista con cuatro columnas estables,  
**cuando** se reconstruye,  
**entonces** conserva todas las referencias sin mezclarlas.

### CA-05 — Cantidad variable

**Dada** una estructura con más de dos columnas,  
**entonces** el algoritmo no depende de una bifurcación fija izquierda/derecha.

### CA-06 — Sin máximo rígido de cuatro

**Dado** el detector,  
**entonces** no contiene una regla funcional que limite el resultado exactamente a cuatro columnas.

### CA-07 — Columnas desiguales

**Dadas** columnas con distinto número de líneas,  
**cuando** se reconstruyen,  
**entonces** no se insertan filas vacías ni se intercalan resultados.

### CA-08 — Título global

**Dado** un título sobre varias columnas,  
**cuando** se reconstruye,  
**entonces** el título conserva su posición y no crea una columna.

### CA-09 — Línea global intermedia

**Dada** una línea global entre bloques,  
**cuando** se procesa,  
**entonces** cada bloque puede usar su propia estructura columnar.

### CA-10 — Prevención de mezcla

**Dadas** varias columnas,  
**entonces** ninguna línea se asigna silenciosamente a una columna incompatible.

### CA-11 — Ambigüedad

**Dada** una estructura sin evidencia suficiente,  
**cuando** se analiza,  
**entonces** se utiliza un orden vertical seguro.

### CA-12 — Elementos desordenados

**Dadas** regiones en orden de entrada arbitrario,  
**cuando** se reconstruyen,  
**entonces** el resultado es determinista.

### CA-13 — Contenido intacto

**Dada** una línea reconocida,  
**cuando** cambia de posición por columnas,  
**entonces** conserva texto, caja y elementos.

### CA-14 — Reindexado

**Dado** el orden final,  
**entonces** los índices son consecutivos desde cero.

### CA-15 — Cantidad y unidad

**Dada** una referencia con cantidad y unidad,  
**cuando** se reconstruye un documento columnar,  
**entonces** esos datos continúan asociados a su referencia.

### CA-16 — Revisión

**Dado** el resultado de varias columnas,  
**cuando** se abre la revisión,  
**entonces** el usuario puede corregir, añadir, eliminar y confirmar.

### CA-17 — Ubicaciones

**Dadas** referencias confirmadas,  
**cuando** se continúa,  
**entonces** la consulta de ubicaciones conserva el orden reconstruido.

### CA-18 — Historial

**Dada** una lista confirmada,  
**cuando** se prepara el historial,  
**entonces** `orderIndex` sigue el orden de columnas reconstruido.

### CA-19 — Sin cambios de Room

**Dada** HU-39,  
**entonces** no se modifica el esquema de base de datos.

### CA-20 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se procesa una lista de varias columnas,  
**entonces** el flujo funciona localmente.

---

## 78. Riesgos

### Falsas columnas

**Riesgo:** una cantidad o nota se interpreta como otra columna.

**Mitigación:** evidencia repetida, análisis por bloques y fallback.

### Mezcla entre columnas

**Riesgo:** líneas cercanas se asignan al grupo equivocado.

**Mitigación:** intervalos, solapamiento, anclas y validación conservadora.

### Títulos como columnas

**Riesgo:** un título centrado crea un grupo independiente.

**Mitigación:** líneas globales y segmentación vertical.

### Umbral dependiente del ancho

**Riesgo:** cuatro columnas tienen huecos menores que el umbral actual.

**Mitigación:** combinar anchura relativa, anclas y repetición.

### Columnas cortas

**Riesgo:** una columna real con una sola línea se descarta.

**Mitigación:** usar evidencia del bloque y degradar ante duda.

### Regresión de dos columnas

**Riesgo:** el nuevo algoritmo cambia el orden vigente.

**Mitigación:** conservar y ampliar las pruebas actuales.

### División de referencias

**Riesgo:** una referencia con cantidad se separa en varias columnas.

**Mitigación:** mantener reconstrucción de fila antes de detectar layout.

### Complejidad excesiva

**Riesgo:** introducir un motor de layout genérico innecesario.

**Mitigación:** algoritmo 1D determinista y modelos internos mínimos.

### Orden no determinista

**Riesgo:** agrupaciones iguales producen resultados distintos.

**Mitigación:** desempates explícitos y colecciones ordenadas.

### Datos sin coordenadas

**Riesgo:** una línea no puede asignarse.

**Mitigación:** fallback vertical, nunca primera columna automática.

---

## 79. Regresión

Deberán permanecer operativos:

- orientación EXIF;
- giro manual;
- cámara;
- Photo Picker;
- PP-OCRv5;
- una columna;
- dos columnas;
- texto bruto;
- texto reconstruido;
- títulos;
- referencias;
- cantidades;
- unidades;
- sugerencias;
- ambigüedad;
- corrección manual;
- ubicaciones;
- historial;
- CRUD;
- CSV;
- escáner individual;
- funcionamiento offline.

---

## 80. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además deberá comprobar:

- tests directos de `DocumentColumnDetector`;
- tests ampliados de `DocumentLineReconstructor`;
- una columna;
- dos columnas;
- tres columnas;
- cuatro columnas;
- líneas globales;
- ambigüedad;
- orden determinista;
- compilación de APK por ABI;
- ausencia de permisos nuevos;
- ausencia de datos privados en imágenes de prueba.

Las pruebas instrumentadas con inferencia real podrán mantenerse locales cuando la CI no disponga de emulador.

---

## 81. Definición de terminado

HU-39 estará terminada cuando:

- `DocumentColumnDetector` deje de estar limitado a una división;
- una columna continúe funcionando;
- dos columnas continúen funcionando;
- tres columnas se reconstruyan correctamente;
- cuatro columnas se reconstruyan correctamente;
- no exista un máximo rígido de cuatro;
- las columnas se ordenen de izquierda a derecha;
- cada columna se ordene de arriba abajo;
- columnas desiguales funcionen;
- títulos globales no creen columnas;
- líneas globales intermedias conserven su posición;
- no se mezclen columnas;
- no se dividan cantidades como columnas de lectura;
- la ambigüedad degrade a orden vertical;
- el resultado sea determinista;
- el contenido de las líneas se conserve;
- los índices se regeneren;
- revisión continúe funcionando;
- ubicaciones continúen funcionando;
- historial conserve el orden;
- Room no cambie;
- funcione offline;
- las pruebas unitarias sean satisfactorias;
- las pruebas instrumentadas principales sean satisfactorias;
- la regresión manual sea satisfactoria;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 82. Resultado esperado

Al cerrar HU-39:

```text
RecognizedTextElement
        ↓
filas funcionales
        ↓
bloques documentales
        ↓
una, dos o varias columnas
        ↓
lectura vertical por columna
        ↓
orden izquierda a derecha
        ↓
RecognizedDocument
        ↓
revisión vigente
```

AlmacenTracker podrá reconstruir listas con una cantidad variable de columnas sin crear una segunda pantalla ni trasladar reglas geométricas al parser.

La siguiente historia será:

```text
HU-40 — Interpretar referencias especiales y destinos documentales
```
