# HU-37 — Integrar PP-OCRv5 con la reconstrucción y revisión de listas

> Quinta historia de usuario de AlmacenTracker v1.4.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.4  
**Versión Android:** 1.4.0  
**Historia:** HU-37  
**Nombre:** Integrar PP-OCRv5 con la reconstrucción y revisión de listas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-37-integrar-ppocrv5-revision-listas`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-36 — Construir el pipeline documental PP-OCRv5  
**Issue prevista:** `#41`

---

## 2. Historia de usuario

Como usuario,  
quiero revisar las referencias obtenidas mediante PP-OCRv5 dentro del flujo existente,  
para corregirlas, confirmarlas, consultar sus ubicaciones y conservar sus datos documentales.

---

## 3. Objetivo

Completar la integración funcional del nuevo motor OCR con las capacidades ya existentes de:

```text
reconstrucción de filas
+
orden de una o dos columnas
+
parser de referencias
+
propuestas de cantidad y unidad
+
coincidencias con mercadería
+
corrección manual
+
consulta de ubicaciones
+
preparación del historial documental
```

Flujo esperado:

```text
fotografía o imagen
        ↓
PaddleOcrDocumentTextRecognizer
        ↓
RecognizedDocument
        ↓
filas reconstruidas y ordenadas
        ↓
ReferenceListReviewActivity
        ↓
WarehouseReferenceParser
        ↓
DocumentReferenceDataParser
        ↓
ReferenceProposal
        ↓
coincidencias y sugerencias
        ↓
corrección manual
        ↓
confirmación
        ↓
ubicaciones
        ↓
preparación histórica
```

HU-37 deberá hacer que los resultados de PP-OCRv5 recorran correctamente el flujo funcional ya disponible.

---

## 4. Regla principal

> HU-37 no crea un segundo flujo de revisión. Adapta el resultado PP-OCRv5 al flujo existente y corrige únicamente los límites que impidan obtener propuestas útiles.

La aplicación ya dispone de:

```text
captura
revisión
corrección
ubicaciones
historial
```

Por tanto, no se deberá crear:

```text
PaddleReferenceListReviewActivity
PaddleWarehouseReferenceParser
PaddleReferenceListLocationActivity
```

El motor OCR debe permanecer sustituible detrás de:

```text
DocumentTextRecognizer
```

Las reglas funcionales de mercadería deberán continuar siendo compartidas.

---

## 5. Base documental y arquitectónica

HU-37 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.4-general-plan.md`;
- `HU-33-preparar-onnx-ppocrv5.md`;
- `HU-34-detectar-regiones-texto-ppocrv5.md`;
- `HU-35-reconocer-contenido-regiones-ppocrv5.md`;
- `HU-36-construir-pipeline-documental-ppocrv5.md`;
- el estado real de `AlmacenTrackerHU36.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- `DocumentTextRecognizer` como contrato OCR;
- `RecognizedDocument` como salida común;
- `WarehouseReferenceParser` como parser funcional;
- `DocumentReferenceDataParser` como extractor documental;
- la revisión manual obligatoria;
- la identidad `categoría + código`;
- Room como fuente de verdad para coincidencias;
- la separación entre OCR, parser, revisión y persistencia;
- el funcionamiento completamente offline;
- la política de no crear componentes ceremoniales.

El plan de v1.4 asigna a HU-37:

```text
filas
+
columnas
+
parser
+
referencias
+
cantidad
+
unidad
+
coincidencias
+
corrección manual
+
navegación vigente
+
historial documental
```

---

## 6. Estado real antes de HU-37

El análisis de `AlmacenTrackerHU36.zip` confirma:

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
merge HU36 #40 into develop
```

Commits principales de HU-36:

```text
Add testable OCR pipeline component contracts #40
Build PP-OCRv5 document recognition pipeline #40
Connect PP-OCRv5 pipeline to reference list capture #40
Test complete PP-OCRv5 document pipeline #40
```

La captura ya utiliza:

```text
PaddleOcrDocumentTextRecognizer
```

mediante:

```text
ReferenceListModule
```

El pipeline ya:

- inicializa PP-OCRv5;
- detecta regiones;
- reconoce cada región;
- convierte regiones en `RecognizedTextElement`;
- reutiliza `DocumentLineReconstructor`;
- construye `RecognizedDocument`;
- entrega el resultado al ViewModel;
- funciona fuera del hilo principal;
- cierra la imagen;
- evita callbacks repetidos;
- conserva las sesiones compartidas;
- funciona offline.

---

## 7. Estado real de la navegación

`ReferenceListCaptureActivity` ya obtiene:

```java
state.getRecognizedDocument()
        .getReconstructedLines();
```

y abre:

```text
ReferenceListReviewActivity
```

mediante una lista de `String`.

La pantalla de revisión ya:

- recibe líneas reconstruidas;
- crea propuestas;
- muestra categoría y código;
- muestra coincidencias;
- permite aplicar sugerencias;
- permite editar;
- permite eliminar;
- permite añadir referencias;
- deduplica;
- confirma;
- abre ubicaciones;
- conserva datos documentales.

Por tanto, HU-37 no necesita una nueva Activity ni un nuevo contrato de navegación para iniciar la revisión.

---

## 8. Estado real de la reconstrucción

El proyecto ya dispone de:

```text
DocumentLineReconstructor
DocumentColumnDetector
RecognizedTextElement
RecognizedTextLine
RecognizedDocument
```

`DocumentLineReconstructor` actualmente:

- limpia elementos sin texto;
- ordena por coordenadas;
- agrupa verticalmente;
- divide filas por separaciones horizontales;
- reconstruye texto;
- ordena una o dos columnas;
- reindexa líneas.

`DocumentColumnDetector` actualmente:

- requiere al menos cuatro líneas;
- exige al menos dos líneas por columna;
- calcula una posible división horizontal;
- ordena primero la columna izquierda;
- ordena después la columna derecha;
- degrada a orden vertical cuando no existe evidencia suficiente.

HU-37 deberá ajustar estas reglas solo cuando los resultados PP-OCRv5 demuestren una incompatibilidad concreta.

---

## 9. Estado real del parser de referencias

`WarehouseReferenceParser` ya mantiene dos estrategias:

```text
parseLine(...)
parseOcrLine(...)
```

Formato funcional estricto:

```text
categoría = exactamente dos letras
código = entre tres y cinco dígitos
sufijo textual opcional
```

Patrón estricto conceptual:

```text
[A-Z]{2}
+
[0-9]{3,5}
+
sufijo opcional
```

La estrategia OCR admite provisionalmente caracteres alfanuméricos en:

```text
categoría observada
código observado
```

para poder generar sugerencias frente a referencias conocidas.

También dispone de:

- normalización;
- detección de unidades adjuntas;
- correcciones limitadas;
- puntuación de sugerencias;
- coincidencia exacta;
- sugerencia única;
- ambigüedad;
- ausencia de coincidencia.

HU-37 deberá reutilizar este parser.

No se creará otro parser exclusivo para PP-OCRv5.

---

## 10. Estado real de los datos documentales

El proyecto ya dispone de:

```text
DocumentReferenceData
DocumentReferenceDataParser
DocumentQuantityUnitVocabulary
```

`DocumentReferenceData` conserva:

```text
reference
quantity
unit
sourceLineIndex
sourceText
```

`DocumentReferenceDataParser` ya intenta asociar:

```text
cantidad positiva
+
unidad reconocida
```

a una referencia extraída de su línea de origen.

`ReferenceProposal` ya conserva:

```text
WarehouseReference
MatchStatus
suggestions
DocumentReferenceData
```

Cuando el usuario corrige una referencia, `withReference(...)` conserva:

```text
quantity
unit
sourceLineIndex
sourceText
```

Por tanto, HU-37 deberá fortalecer la asociación con las líneas PP-OCRv5, no crear un segundo modelo documental.

---

## 11. Problema funcional que HU-37 debe cerrar

HU-36 demuestra que PP-OCRv5 reconoce texto y lo muestra en la captura.

Sin embargo, un texto visible no garantiza que el flujo obtenga propuestas correctas.

Posibles fallos:

```text
categoría y código quedan en elementos separados
cantidad queda en otra región
unidad se une al código
dos columnas se intercalan
una fila se divide en varias líneas
dos filas se unen
espacios desaparecen
signos aparecen entre categoría y código
texto auxiliar se interpreta como referencia
```

HU-37 deberá comprobar y corregir la transformación:

```text
regiones PP-OCRv5
        ↓
líneas funcionalmente parseables
```

No deberá modificar el modelo ONNX para resolver problemas de parser.

---

## 12. Alcance incluido

HU-37 incluye:

- analizar las líneas reales producidas por HU-36;
- comprobar `rawText`;
- comprobar `reconstructedText`;
- comprobar coordenadas;
- comprobar orden;
- ajustar agrupación vertical cuando resulte necesario;
- ajustar separación horizontal cuando resulte necesario;
- ajustar reconstrucción de espacios;
- ajustar orden de una columna;
- ajustar orden de dos columnas;
- conservar determinismo;
- evitar mezcla entre columnas;
- evitar unión de filas distintas;
- evitar división injustificada de una referencia;
- conservar texto bruto;
- conservar texto reconstruido;
- entregar a revisión las líneas adecuadas;
- reutilizar `ReferenceListReviewActivity`;
- reutilizar `ReferenceListReviewViewModel`;
- reutilizar `WarehouseReferenceParser`;
- reutilizar `DocumentReferenceDataParser`;
- extraer referencias PP-OCRv5;
- mantener categoría de dos letras;
- mantener código de tres a cinco dígitos;
- conservar ceros iniciales;
- conservar sufijo cuando corresponda;
- tratar espacios y separadores OCR;
- tratar categoría y código pegados;
- tratar categoría y código separados;
- tratar unidades adjuntas;
- asociar cantidad cuando exista;
- asociar unidad cuando exista;
- mantener cantidad y unidad opcionales;
- comprobar coincidencias exactas;
- comprobar sugerencia única;
- comprobar ambigüedad;
- comprobar no encontrada;
- conservar corrección manual;
- conservar adición manual;
- conservar eliminación manual;
- conservar deduplicación;
- conservar confirmación;
- conservar navegación a ubicaciones;
- conservar `DocumentReferenceData` durante la navegación;
- conservar propuestas para historial;
- mantener Room sin cambios;
- mantener mercadería sin cambios;
- funcionar offline;
- pruebas unitarias;
- pruebas de integración;
- pruebas instrumentadas;
- pruebas manuales;
- CI.

---

## 13. Alcance excluido

HU-37 no incluye:

- modificar los modelos ONNX;
- convertir otros modelos;
- entrenar;
- fine-tuning;
- medir comparativamente precisión global;
- crear corpus definitivo de evaluación;
- concluir que PP-OCRv5 es mejor que ML Kit;
- optimizar hilos;
- paralelizar reconocimiento;
- reducir tamaño de APK;
- retirar ML Kit;
- retirar código de compatibilidad;
- añadir selector de motor;
- añadir ajustes;
- usar GPU;
- usar NNAPI;
- cambiar Room;
- crear migraciones;
- modificar tablas;
- cambiar reglas de identidad;
- admitir categorías de tres letras;
- admitir códigos de longitud arbitraria;
- gestionar stock;
- guardar historial automáticamente;
- eliminar revisión manual;
- procesar PDF;
- procesar varias páginas;
- añadir controles manuales para girar la imagen;
- resolver automáticamente fotografías recibidas con orientación incorrecta;
- reconstruir documentos de tres o más columnas;
- interpretar extensiones especiales de código separadas por guion;
- interpretar indicadores de destino como `①`, `②` o equivalentes;
- dividir una lista en varios pedidos o destinos;
- almacenar fotografías;
- persistir texto OCR completo.

La corrección manual de orientación corresponderá a HU-38.

La reconstrucción de varias columnas corresponderá a HU-39.

Las referencias especiales y los destinos documentales corresponderán a HU-40.

La evaluación corresponderá a HU-41.

La optimización corresponderá a HU-42.

La consolidación final corresponderá a HU-43.

---

## 14. Decisión sobre la entrada de revisión

Actualmente la captura envía:

```text
List<String> reconstructedLines
```

Esto es suficiente para el flujo actual, pero pierde:

- coordenadas;
- texto bruto por elemento;
- confianza OCR;
- relación explícita entre elementos.

HU-37 deberá decidir con base en el código real si esa pérdida impide:

- ordenar correctamente;
- asociar cantidad y unidad;
- diagnosticar errores;
- conservar una línea estable.

### Opción A — Mantener `List<String>`

Adecuada cuando:

- la reconstrucción queda resuelta antes de navegar;
- cada string representa una fila funcional;
- cantidad y unidad están en la misma línea;
- no se necesita confianza en revisión.

### Opción B — Transportar un modelo documental pequeño

Adecuada cuando se necesita conservar:

```text
lineIndex
rawText
reconstructedText
```

No se deberán transportar:

- Bitmap;
- `OrtSession`;
- tensores;
- regiones ONNX;
- logits;
- objetos internos del motor.

Decisión inicial recomendada:

> Mantener `List<String>` mientras las pruebas confirmen que basta. Ampliar el contrato solo si existe una pérdida funcional demostrable.

---

## 15. No transportar `RecognizedDocument` completo por Intent

No se recomienda serializar:

```text
RecognizedDocument
```

completo.

Motivos:

- contiene líneas y elementos;
- puede aumentar el tamaño del Intent;
- acoplaría la revisión al modelo técnico;
- las coordenadas no son necesarias después de reconstruir;
- puede acercarse al límite de Binder en documentos grandes.

Si se amplía el contrato, deberá utilizar un modelo pequeño con datos estrictamente necesarios.

---

## 16. Reconstrucción orientada a filas funcionales

Una fila funcional puede contener:

```text
MR 1210 4 CAJAS
```

o:

```text
MR1210
```

o:

```text
MR 1210A 20 PCS
```

El objetivo de la reconstrucción no es reproducir visualmente cada espacio del documento.

El objetivo es conservar suficiente separación para que:

```text
WarehouseReferenceParser
DocumentReferenceDataParser
```

interpreten correctamente la línea.

No se deberán introducir separadores que cambien el contenido.

---

## 17. Agrupación vertical

Dos elementos podrán pertenecer a la misma fila cuando exista evidencia combinada de:

- solapamiento vertical;
- centros verticales cercanos;
- alturas compatibles;
- separación horizontal razonable.

No bastará con compartir una coordenada superior parecida.

Casos que deben agruparse:

```text
MR
1210
4
CAJAS
```

cuando están alineados horizontalmente.

Casos que no deben agruparse:

```text
MR 1210
MZ 1300
```

aunque sus cajas se solapen parcialmente por una detección imperfecta.

---

## 18. Alturas heterogéneas

PP-OCRv5 puede generar regiones con alturas distintas para:

- categoría;
- código;
- cantidad;
- unidad;
- texto auxiliar.

La agrupación deberá tolerar diferencias moderadas.

No deberá unir un título grande con la primera referencia únicamente porque sus cajas se solapan.

Las decisiones deberán basarse en:

```text
solapamiento
distancia de centros
altura media
separación horizontal
```

y quedar cubiertas por pruebas.

---

## 19. Separación por grandes espacios

Un espacio horizontal grande puede significar:

```text
dos columnas
```

o:

```text
referencia + cantidad
```

No se deberá dividir una fila únicamente porque existe un hueco grande.

La decisión deberá considerar:

- anchura del documento;
- distribución de otras filas;
- posición repetida del hueco;
- número de líneas por lado;
- consistencia vertical.

Un hueco aislado dentro de una sola fila no demuestra dos columnas.

---

## 20. Detección de dos columnas

Una lista de dos columnas debe ordenarse:

```text
columna izquierda de arriba abajo
        ↓
columna derecha de arriba abajo
```

No:

```text
fila superior izquierda
fila superior derecha
fila siguiente izquierda
fila siguiente derecha
```

salvo que el diseño documental real indique lectura horizontal.

La estrategia actual de `DocumentColumnDetector` ya prioriza columnas completas.

HU-37 deberá comprobar:

- columna izquierda suficiente;
- columna derecha suficiente;
- separación estable;
- ausencia de líneas que crucen ambas columnas;
- comportamiento con títulos a ancho completo.

---

## 21. Títulos y líneas a ancho completo

Una línea como:

```text
LISTA DE REPOSICIÓN
```

puede ocupar todo el ancho sobre dos columnas.

No deberá forzar que el documento sea de una sola columna.

Tampoco deberá asignarse arbitrariamente a una columna.

Para HU-37 se podrá:

- conservarla antes de las columnas;
- excluirla del cálculo de división;
- mantenerla como línea global.

Solo se implementará si aparece en pruebas reales.

No se añadirá complejidad preventiva sin evidencia.

---

## 22. Orden estable

El resultado deberá ser determinista.

Con la misma imagen y configuración:

```text
mismas líneas
mismo orden
mismos índices
```

El orden no dependerá de:

- tiempo de inferencia;
- orden de un `HashMap`;
- confianza;
- longitud del texto;
- identidad de objetos.

Desempates recomendados:

```text
top
left
sourceOrder
```

---

## 23. Texto bruto y texto reconstruido

`RecognizedTextLine` ya conserva:

```text
rawText
reconstructedText
```

Reglas:

- `rawText` conserva el contenido reconocido;
- `reconstructedText` añade separación funcional;
- ninguno debe contener `null`;
- no se deben aplicar reglas de mercadería en infraestructura;
- la pantalla puede mostrar ambos para diagnóstico;
- la revisión debe usar el texto reconstruido.

No se deberá sobrescribir el texto bruto con una corrección funcional.

---

## 24. Inserción de espacios

La reconstrucción podrá insertar un espacio cuando:

```text
distancia entre elementos
>
umbral derivado del tamaño de caracteres
```

No deberá insertar espacios dentro de una región ya reconocida.

Ejemplo:

```text
"MR" + "1210"
→ "MR 1210"
```

No:

```text
"M" + "R" + "1" + "2" + "1" + "0"
```

salvo que el detector realmente entregue caracteres separados.

La estrategia deberá evitar:

```text
MR1210 → MR 1 2 1 0
```

---

## 25. Separadores OCR admitidos

El parser OCR ya tolera separadores como:

```text
espacio
dos puntos
punto
guion
guion bajo
```

HU-37 deberá probar entradas como:

```text
MR:1210
MR-1210
MR_1210
MR.1210
MR 1210
MR1210
```

La salida funcional deberá mantener:

```text
category = MR
code = 1210
```

cuando el contenido sea suficientemente claro.

---

## 26. Regla de categoría

La categoría funcional válida continúa siendo:

```text
exactamente dos letras A-Z
```

No se admitirá como confirmación automática:

```text
M1
1R
M
MRR
```

Una observación OCR alfanumérica podrá generar:

- sugerencia;
- ambigüedad;
- no encontrada.

No deberá convertirse silenciosamente en categoría válida sin evidencia.

---

## 27. Regla de código

El código funcional continúa siendo:

```text
entre tres y cinco dígitos
+
sufijo textual opcional
```

Se deberán conservar:

- ceros iniciales;
- sufijo;
- espacios funcionales del sufijo cuando el dominio los permita.

Ejemplos:

```text
00120
1210A
1300 C
```

No se convertirán a enteros.

---

## 28. Confusiones OCR

HU-37 podrá reutilizar la lógica existente para proponer correcciones frente a referencias conocidas.

Confusiones relevantes:

```text
O ↔ 0
I ↔ 1
L ↔ 1
S ↔ 5
B ↔ 8
Z ↔ 2
G ↔ 6
```

Regla:

> Una corrección OCR no se convertirá automáticamente en coincidencia exacta salvo que las reglas existentes lo justifiquen sin ambigüedad.

Cuando existan varias referencias candidatas:

```text
AMBIGUOUS
```

El usuario deberá elegir o editar.

---

## 29. Coincidencia exacta

Una propuesta será:

```text
EXACT
```

cuando la referencia normalizada:

```text
categoría + código
```

coincida exactamente con una referencia conocida.

No se utilizará solo el código.

Ejemplo:

```text
MR 1050
MD 1050
```

son referencias diferentes.

---

## 30. Sugerencia única

Una propuesta será:

```text
UNIQUE_SUGGESTION
```

cuando:

- no exista coincidencia exacta;
- exista una única referencia conocida suficientemente cercana;
- la puntuación esté dentro del límite existente.

La sugerencia deberá mostrarse.

No deberá sustituirse silenciosamente antes de que el usuario la aplique.

---

## 31. Ambigüedad

Una propuesta será:

```text
AMBIGUOUS
```

cuando existan varias candidatas plausibles.

La interfaz deberá conservar:

- texto observado;
- propuesta actual;
- lista de sugerencias;
- acción de edición.

No se elegirá la primera por orden alfabético como decisión automática.

---

## 32. Sin coincidencia

Una propuesta será:

```text
NO_MATCH
```

cuando el texto produzca una referencia válida o candidata pero no exista una correspondencia suficiente en Room.

El usuario podrá:

- corregir;
- eliminar;
- mantener una referencia confirmada manualmente.

La ausencia en Room no implica que el OCR esté necesariamente equivocado.

---

## 33. Propuesta no verificada

Cuando el repositorio todavía no haya entregado referencias conocidas o la verificación no pueda completarse:

```text
UNVERIFIED
```

No deberá mostrarse como coincidencia exacta.

Un error de consulta no deberá borrar las propuestas OCR.

La pantalla debe conservar contenido editable.

---

## 34. Extracción de varias referencias por línea

Una línea reconstruida podría contener más de una referencia.

Ejemplo:

```text
MR 1210 MZ 1300
```

La estrategia actual permite extraer ocurrencias múltiples.

HU-37 deberá comprobar:

- índices de ocurrencia;
- orden;
- deduplicación posterior;
- asociación documental.

La cantidad y unidad no deberán asignarse simultáneamente a dos referencias cuando el texto no permita decidir cuál corresponde.

---

## 35. Cantidad documental

La cantidad propuesta debe ser:

```text
entero positivo
```

Ejemplos válidos:

```text
1
4
20
```

No se admitirán automáticamente:

```text
0
-2
2.5
4/6
```

La ausencia de cantidad no invalida la referencia.

---

## 36. Unidad documental

La unidad propuesta deberá pasar por:

```text
DocumentQuantityUnitVocabulary
```

Unidades existentes o equivalentes normalizadas podrán incluir:

```text
PCS
PES
PQT
PQTS
PAT
PATS
PZA
PZAS
CAJA
CAJAS
```

La lista exacta deberá corresponder al código real.

No se añadirá una unidad nueva sin:

- necesidad observada;
- normalización definida;
- prueba.

---

## 37. Unidad adjunta al código

PP-OCRv5 puede producir:

```text
1210PCS
```

cuando el documento contiene:

```text
1210 PCS
```

El parser ya dispone de una regla para separar determinadas unidades adjuntas.

HU-37 deberá probar:

```text
MR 1210PCS
MR1210PES
MR 1210 PQTS
```

Resultado esperado cuando sea inequívoco:

```text
reference = MR 1210
quantity = null o cantidad detectada
unit = PCS/PES/PQTS
```

No se deberá eliminar un sufijo real del código si también puede ser una unidad.

La decisión debe apoyarse en el vocabulario y en las referencias conocidas.

---

## 38. Asociación cantidad–referencia

La cantidad podrá aparecer:

```text
después de la referencia
antes de la unidad
en un elemento separado de la misma fila
```

Ejemplo:

```text
MR 1210 4 CAJAS
```

Debe producir:

```text
reference = MR 1210
quantity = 4
unit = CAJAS
```

No se deberá tomar como cantidad:

- parte numérica del código;
- número de sitio;
- número dentro de un título;
- otra referencia de la misma fila.

---

## 39. Cantidad en línea contigua

Si el documento visual separa:

```text
MR 1210
4 CAJAS
```

en dos líneas OCR distintas, HU-37 deberá decidir si existe evidencia suficiente para asociarlas.

Criterios posibles:

- misma columna;
- proximidad vertical;
- línea inferior sin referencia;
- contenido exclusivo de cantidad y unidad;
- ausencia de otra referencia cercana.

Esta asociación solo deberá añadirse si aparece como necesidad real y puede probarse con seguridad.

No se deberá asociar por simple adyacencia global.

---

## 40. Política inicial para líneas contiguas

Recomendación para HU-37:

1. priorizar cantidad y unidad dentro de la misma línea;
2. admitir asociación con la línea inmediatamente siguiente solo cuando:
   - pertenece a la misma columna;
   - contiene exclusivamente cantidad y unidad;
   - no contiene referencia;
   - la distancia vertical está dentro de un umbral;
   - no existe otra referencia candidata;
3. conservar `null` cuando exista duda.

Es preferible una propuesta vacía a una cantidad asociada a la mercadería equivocada.

---

## 41. Modelo para asociación entre líneas

No se deberá añadir cantidad y unidad a:

```text
RecognizedTextLine
```

porque son interpretaciones funcionales, no datos OCR.

La asociación deberá ocurrir en:

```text
domain/reference
```

o en un componente funcional cercano al parser.

Opciones:

```text
DocumentReferenceDataParser
DocumentReferenceLineContext
```

Solo se añadirá un nuevo modelo si se necesita representar:

- línea anterior;
- línea actual;
- línea siguiente;
- columna;
- distancia.

No se introducirán coordenadas ONNX en dominio.

---

## 42. Corrección manual

La pantalla ya permite:

- editar categoría;
- editar código;
- aplicar sugerencia;
- eliminar propuesta;
- añadir referencia.

HU-37 deberá conservar este comportamiento.

Cuando el usuario corrige una referencia:

- el estado pasa a `USER_CONFIRMED`;
- se eliminan sugerencias anteriores;
- se conserva cantidad;
- se conserva unidad;
- se conserva línea de origen cuando sea útil.

No se deberá volver a aplicar automáticamente la predicción OCR sobre una corrección manual.

---

## 43. Deduplicación

Las referencias confirmadas se deduplican por:

```text
categoría normalizada + código normalizado
```

La deduplicación no deberá utilizar:

- cantidad;
- unidad;
- línea;
- confianza;
- estado de coincidencia.

Cuando dos propuestas duplicadas contengan datos documentales distintos, deberá definirse una regla.

Recomendación:

- conservar la primera por orden documental;
- no sumar cantidades;
- no fusionar unidades;
- notificar consolidación existente;
- permitir corrección manual antes de confirmar.

La gestión de stock sigue fuera de alcance.

---

## 44. Conservación del orden

El orden confirmado deberá seguir el documento reconstruido.

Orden:

```text
línea
ocurrencia dentro de línea
```

Las referencias añadidas manualmente podrán ubicarse al final.

Las correcciones no deberán cambiar de posición.

Las sugerencias no deberán reordenar propuestas.

---

## 45. Navegación a ubicaciones

Después de confirmar:

```text
ReferenceListReviewActivity
        ↓
ReferenceListLocationActivity
```

deberá continuar utilizando:

```text
DocumentReferenceDataIntentContract
WarehouseReferenceIntentContract
```

HU-37 deberá comprobar que se conservan:

- categoría;
- código;
- cantidad;
- unidad;
- orden.

La consulta de ubicación seguirá usando únicamente:

```text
categoría + código
```

Cantidad y unidad no participarán en la búsqueda.

---

## 46. Integración con historial documental

Desde ubicaciones:

```text
ReferenceListLocationActivity
        ↓
WithdrawalHistoryCreateActivity
```

deberán continuar llegando las propuestas documentales.

HU-37 deberá comprobar:

```text
quantity
unit
```

desde PP-OCRv5 hasta la pantalla histórica.

El usuario deberá poder:

- corregir cantidad;
- corregir unidad;
- eliminar ambas;
- guardar sin ambas.

No se guardará historial durante OCR ni durante revisión.

---

## 47. No modificar mercadería

PP-OCRv5 no deberá:

- crear mercadería;
- actualizar categoría;
- actualizar código;
- cambiar sitio;
- cambiar posición;
- descontar cantidad;
- guardar historial sin confirmación.

Las coincidencias de Room son consultas para ayudar al usuario.

---

## 48. Integración con el repositorio

`ReferenceListReviewViewModel` ya recibe:

```text
WarehouseItemRepository
```

para obtener referencias conocidas.

HU-37 deberá mantener la consulta fuera de la Activity.

No se deberá:

- consultar DAO desde parser;
- consultar Room desde el OCR;
- pasar repositorio a `PaddleOcrDocumentTextRecognizer`;
- mezclar coincidencias con inferencia ONNX.

---

## 49. Estados de revisión

La revisión deberá mantener los estados existentes.

Las propuestas deberán representar correctamente:

```text
EXACT
UNIQUE_SUGGESTION
AMBIGUOUS
NO_MATCH
UNVERIFIED
USER_CONFIRMED
```

No se añadirá:

```text
PADDLE_MATCH
```

porque el estado describe la relación funcional con Room, no el motor OCR.

---

## 50. Errores de parser

Una línea sin referencia válida no deberá provocar error de pantalla.

Comportamiento:

```text
línea no parseable
    → sin propuesta
```

Si todas las líneas quedan sin propuestas:

- la pantalla muestra estado vacío;
- el usuario puede añadir manualmente;
- no se produce crash.

No se deberá convertir cada texto OCR en una referencia ficticia.

---

## 51. Texto auxiliar

Listas reales pueden contener:

```text
títulos
nombres
fechas
clientes
instrucciones
totales
sitios
observaciones
```

El parser deberá ignorarlos cuando no cumplen la identidad funcional.

No se deberán ampliar patrones para capturar cualquier combinación alfanumérica.

Reducir omisiones no justifica aumentar falsos positivos sin control.

---

## 52. Caso de una columna

Corpus mínimo:

```text
MR 1210
MZ 1300A
MI 00120
```

Debe producir:

```text
MR + 1210
MZ + 1300A
MI + 00120
```

en el mismo orden.

Se comprobarán:

- espacios;
- categoría pegada;
- sufijo;
- ceros iniciales.

---

## 53. Caso de dos columnas

Ejemplo visual:

```text
MR 1210        MD 1500
MZ 1300A       MI 1600
```

Orden esperado:

```text
MR 1210
MZ 1300A
MD 1500
MI 1600
```

La prueba deberá usar coordenadas realistas.

No basta con una lista de strings ya ordenada.

---

## 54. Caso con cantidad y unidad

Corpus mínimo:

```text
MR 1210 4 CAJAS
MZ 1300A 20 PCS
MI 00120
```

Resultado:

```text
MR 1210
quantity = 4
unit = CAJAS

MZ 1300A
quantity = 20
unit = PCS

MI 00120
quantity = null
unit = null
```

---

## 55. Caso con ruido OCR

Ejemplos:

```text
M8 121O
MR:1210
MZ_1300A
MI.OO120
```

HU-37 deberá comprobar:

- coincidencia exacta cuando el texto ya es válido;
- sugerencia única cuando existe una sola corrección plausible;
- ambigüedad cuando existen varias;
- no coincidencia cuando no existe candidata.

No se exigirá que todos los ejemplos se autocorrijan.

---

## 56. Caso con texto no relacionado

Ejemplo:

```text
LISTA TIENDA CENTRO
FECHA 03/08/2026
TOTAL 15
```

Resultado:

```text
sin referencias
```

No deberá producir:

```text
TI ENDA
FE CHA
TO TAL
```

---

## 57. Pruebas unitarias de reconstrucción

Casos mínimos:

- elementos nulos;
- texto vacío;
- una región;
- varios elementos de una fila;
- varias filas;
- alturas distintas;
- solapamiento parcial;
- filas cercanas;
- gran hueco horizontal;
- una columna;
- dos columnas;
- título global;
- coordenadas iguales;
- orden determinista;
- fallback.

Las pruebas deberán utilizar elementos con coordenadas.

---

## 58. Pruebas unitarias del parser

Casos mínimos:

```text
MR1210
MR 1210
MR:1210
MR-1210
MR_1210
MR.1210
MR 00120
MR 1210A
MR 1210 A
```

También:

- categoría inválida;
- código corto;
- código largo;
- texto auxiliar;
- varias referencias;
- cantidad;
- unidad adjunta;
- sufijo;
- confusiones OCR;
- sugerencias;
- ambigüedad;
- deduplicación.

---

## 59. Pruebas de datos documentales

Casos:

- cantidad y unidad válidas;
- cantidad sin unidad;
- unidad sin cantidad;
- cantidad cero;
- cantidad negativa;
- unidad desconocida;
- código seguido de unidad;
- cantidad en la misma línea;
- cantidad en línea siguiente cuando se implemente;
- dos referencias en una línea;
- conservación tras corrección;
- conservación tras sugerencia;
- conservación tras confirmación.

---

## 60. Pruebas del ViewModel de revisión

`ReferenceListReviewViewModelTest` deberá comprobar con líneas PP-OCRv5 representativas:

- aplicación inicial;
- carga de referencias conocidas;
- coincidencia exacta;
- sugerencia única;
- ambigüedad;
- no coincidencia;
- propuesta no verificada;
- corrección;
- sugerencia aplicada;
- eliminación;
- adición manual;
- duplicado;
- consolidación;
- confirmación;
- orden;
- datos documentales.

No se deberá ejecutar ONNX en estas pruebas.

---

## 61. Prueba de integración completa

Flujo:

```text
imagen de prueba
        ↓
PP-OCRv5
        ↓
RecognizedDocument
        ↓
líneas reconstruidas
        ↓
ReferenceListReviewViewModel
        ↓
ReferenceProposal
```

Debe verificar:

- al menos una referencia correcta;
- orden;
- estado de coincidencia;
- cantidad y unidad cuando existan;
- ausencia de crash;
- flujo offline.

Esta prueba puede ser instrumentada.

---

## 62. Prueba de navegación

Se deberá comprobar:

```text
captura
    → revisión
    → ubicaciones
    → preparación histórica
```

Datos que deben sobrevivir:

```text
category
code
quantity
unit
order
```

No es necesario guardar el historial durante la prueba de HU-37.

Sí debe comprobarse que el borrador recibe la propuesta.

---

## 63. Pruebas manuales

Casos mínimos:

- fotografía de una columna;
- fotografía de dos columnas;
- captura de pantalla;
- imagen inclinada moderadamente;
- texto pequeño;
- baja iluminación razonable;
- referencia con ceros;
- referencia con sufijo;
- cantidad y unidad;
- unidad pegada;
- texto auxiliar;
- referencia no registrada;
- sugerencia única;
- ambigüedad;
- corrección manual;
- adición manual;
- eliminación;
- ubicación;
- historial;
- sin Internet.

Los resultados deberán registrarse para preparar HU-38.

HU-37 no deberá convertir todavía esos registros en un informe comparativo final.

---

## 64. Regresión

Deberán permanecer operativos:

- listado de mercadería;
- alta;
- detalle;
- búsqueda;
- filtros;
- edición;
- eliminación;
- CSV;
- copia de seguridad;
- restauración;
- escáner individual;
- cámara documental;
- Photo Picker;
- revisión;
- ubicaciones;
- creación histórica;
- listado histórico;
- detalle histórico;
- búsqueda histórica;
- filtros históricos;
- eliminación histórica.

---

## 65. Accesibilidad

HU-37 no deberá degradar:

- descripción de propuestas;
- estado de coincidencia;
- botones de editar;
- botones de eliminar;
- sugerencias;
- acción de añadir;
- acción de confirmar;
- mensajes de duplicado;
- orden de foco;
- navegación por teclado;
- texto ampliado;
- objetivos táctiles.

El origen PP-OCRv5 no deberá cambiar las etiquetas visuales por nombres técnicos.

---

## 66. Privacidad

HU-37 deberá mantener:

- procesamiento local;
- ausencia de red;
- ausencia de telemetría de contenido;
- ausencia de texto completo en logs;
- ausencia de imágenes en Room;
- ausencia de imágenes en repositorio público;
- datos de prueba ficticios;
- revisión obligatoria;
- eliminación de temporales según el flujo existente.

---

## 67. CI

La CI deberá continuar ejecutando:

```text
testDebugUnitTest
lintDebug
assembleDebug
```

Además deberá comprobar:

- tests de reconstrucción;
- tests de columnas;
- tests de parser;
- tests de cantidad y unidad;
- tests del ViewModel de revisión;
- contratos de navegación;
- generación de APK por ABI;
- modelos incluidos;
- ausencia de permisos nuevos;
- ausencia de corpus privado.

Las pruebas de inferencia real podrán mantenerse como instrumentadas locales cuando la CI no disponga de emulador.

---

## 68. Criterios de aceptación

### CA-01 — Filas funcionales

**Dado** un documento PP-OCRv5 con elementos de una fila,  
**cuando** se reconstruye,  
**entonces** produce texto parseable en orden horizontal.

### CA-02 — Filas independientes

**Dadas** dos referencias en filas distintas,  
**cuando** se reconstruyen,  
**entonces** no se unen en una sola línea.

### CA-03 — Una columna

**Dada** una lista de una columna,  
**cuando** se procesa,  
**entonces** conserva el orden vertical.

### CA-04 — Dos columnas

**Dada** una lista de dos columnas,  
**cuando** se procesa,  
**entonces** ordena primero la izquierda y después la derecha.

### CA-05 — Parser compartido

**Dado** el resultado PP-OCRv5,  
**cuando** se extraen referencias,  
**entonces** utiliza `WarehouseReferenceParser`.

### CA-06 — Categoría

**Dada** una propuesta confirmable,  
**entonces** la categoría tiene exactamente dos letras.

### CA-07 — Código

**Dada** una propuesta confirmable,  
**entonces** el código conserva entre tres y cinco dígitos y su sufijo admitido.

### CA-08 — Ceros iniciales

**Dado** `00120`,  
**cuando** se procesa,  
**entonces** no se convierte en `120`.

### CA-09 — Coincidencia exacta

**Dada** una referencia existente,  
**cuando** categoría y código coinciden,  
**entonces** el estado es `EXACT`.

### CA-10 — Sugerencia única

**Dada** una confusión OCR con una única candidata,  
**cuando** se evalúa,  
**entonces** se muestra como `UNIQUE_SUGGESTION` sin sustituirla automáticamente.

### CA-11 — Ambigüedad

**Dadas** varias candidatas,  
**cuando** se evalúa,  
**entonces** el estado es `AMBIGUOUS`.

### CA-12 — No encontrada

**Dada** una referencia sin coincidencia,  
**cuando** se evalúa,  
**entonces** puede revisarse y confirmarse manualmente.

### CA-13 — Cantidad

**Dada** una cantidad positiva asociada,  
**cuando** se procesa,  
**entonces** queda en `DocumentReferenceData`.

### CA-14 — Unidad

**Dada** una unidad conocida,  
**cuando** se procesa,  
**entonces** se normaliza mediante el vocabulario existente.

### CA-15 — Datos opcionales

**Dada** una referencia sin cantidad o unidad,  
**cuando** se procesa,  
**entonces** sigue siendo válida.

### CA-16 — Corrección manual

**Dada** una propuesta incorrecta,  
**cuando** el usuario la corrige,  
**entonces** la corrección prevalece sobre el OCR.

### CA-17 — Conservación documental

**Dada** una corrección de referencia,  
**entonces** cantidad y unidad asociadas se conservan.

### CA-18 — Deduplicación

**Dadas** referencias repetidas,  
**cuando** se consolidan,  
**entonces** se utiliza categoría y código como identidad.

### CA-19 — Navegación

**Dadas** referencias confirmadas,  
**cuando** se continúa,  
**entonces** se abre la pantalla vigente de ubicaciones.

### CA-20 — Historial

**Dados** cantidad y unidad propuestos,  
**cuando** se abre la preparación histórica,  
**entonces** permanecen disponibles para revisión.

### CA-21 — Room sin escritura

**Dado** el procesamiento y revisión,  
**entonces** la mercadería no se modifica automáticamente.

### CA-22 — Revisión obligatoria

**Dado** cualquier resultado OCR,  
**entonces** el usuario debe confirmarlo antes de consultar ubicaciones o guardar historial.

### CA-23 — Sin texto útil

**Dado** un documento sin referencias parseables,  
**cuando** se abre revisión,  
**entonces** el usuario puede añadir referencias manualmente.

### CA-24 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se completa el flujo,  
**entonces** funciona localmente.

---

## 69. Riesgos

### Regiones demasiado fragmentadas

**Riesgo:** categoría, código y cantidad quedan en líneas distintas.

**Mitigación:** ajustar agrupación espacial con pruebas reales.

### Filas fusionadas

**Riesgo:** dos referencias producen una propuesta inválida.

**Mitigación:** límites de solapamiento y distancia más estrictos.

### Columnas intercaladas

**Riesgo:** el orden documental queda incorrecto.

**Mitigación:** pruebas con coordenadas y división estable.

### Falsos positivos

**Riesgo:** títulos y fechas se convierten en referencias.

**Mitigación:** conservar reglas estrictas y usar sugerencias, no ampliación indiscriminada.

### Corrección automática excesiva

**Riesgo:** una referencia diferente se sustituye por una conocida.

**Mitigación:** mantener `UNIQUE_SUGGESTION` y `AMBIGUOUS`.

### Cantidad mal asociada

**Riesgo:** una cantidad se asigna a otra referencia.

**Mitigación:** priorizar misma línea y conservar `null` ante duda.

### Unidad confundida con sufijo

**Riesgo:** se altera el código.

**Mitigación:** vocabulario controlado y referencias conocidas.

### Pérdida de datos al navegar

**Riesgo:** cantidad y unidad desaparecen antes del historial.

**Mitigación:** pruebas de contratos existentes.

### Duplicación de flujo

**Riesgo:** aparecen componentes exclusivos para PP-OCRv5.

**Mitigación:** reutilizar contratos, ViewModels y Activities vigentes.

### Alcance excesivo

**Riesgo:** HU-37 se convierte en evaluación y optimización.

**Mitigación:** reservar métricas comparativas para HU-38 y rendimiento para HU-39.

---

## 70. Continuidad planificada después de HU-37

HU-37 deberá cerrar la integración funcional prevista para listas de una y dos columnas, manteniendo revisión manual, títulos opcionales, cantidades, unidades, ubicaciones e historial documental.

Las observaciones que amplían el tipo de documento deberán continuar en historias separadas para evitar mezclar responsabilidades:

```text
HU-38
→ corregir la orientación de la imagen antes del OCR

HU-39
→ reconstruir listas de varias columnas

HU-40
→ interpretar referencias especiales y destinos documentales

HU-41
→ evaluar precisión y comportamiento del OCR
```

HU-37 no deberá anticipar mediante reglas parciales el comportamiento de esas historias. Sus pruebas podrán conservar muestras que sirvan como evidencia y corpus posterior.

---

## 71. Definición de terminado

HU-37 estará terminada cuando:

- las líneas PP-OCRv5 sean funcionalmente parseables;
- la reconstrucción conserve orden;
- una columna funcione;
- dos columnas funcionen;
- no se mezclen filas;
- no se intercalen columnas;
- el parser existente reciba el texto reconstruido;
- se extraigan categoría y código válidos;
- se conserven ceros iniciales;
- se conserve el sufijo;
- se ignoren textos auxiliares;
- funcionen coincidencias exactas;
- funcionen sugerencias;
- funcione ambigüedad;
- funcione no encontrada;
- cantidad y unidad se propongan cuando corresponda;
- cantidad y unidad permanezcan opcionales;
- las propuestas lleguen a revisión;
- la corrección manual prevalezca;
- la adición y eliminación manual funcionen;
- la deduplicación funcione;
- la confirmación funcione;
- las ubicaciones se consulten por categoría y código;
- cantidad y unidad lleguen a la preparación histórica;
- no se modifique mercadería;
- no se gestione stock;
- no se escriba historial sin confirmación;
- funcione offline;
- las pruebas unitarias sean satisfactorias;
- las pruebas de integración sean satisfactorias;
- las pruebas instrumentadas principales sean satisfactorias;
- la regresión manual sea satisfactoria;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 72. Resultado esperado

Al cerrar HU-37:

```text
imagen
    ↓
PP-OCRv5
    ↓
RecognizedDocument
    ↓
filas y columnas
    ↓
WarehouseReferenceParser
    ↓
DocumentReferenceDataParser
    ↓
ReferenceProposal
    ↓
revisión manual
    ↓
ubicaciones
    ↓
preparación histórica
```

El nuevo motor OCR quedará integrado en el flujo funcional existente sin eliminar el control del usuario.

La siguiente historia prevista implementará:

```text
HU-38 — Corregir la orientación de imágenes antes del OCR
```
