# HU-15 — Validar e informar incidencias de importación

> Cuarta historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-15  
**Nombre:** Validar e informar incidencias de importación  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-15-incidencias-importacion-csv`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-14 — Importar mercancía desde CSV

---

## 2. Historia de usuario

Como usuario,  
quiero conocer qué filas de un archivo CSV no se importaron y por qué,  
para poder corregir los datos y volver a intentarlo.

---

## 3. Objetivo

Ampliar el flujo de importación implementado en HU-14 para ofrecer un resultado trazable y comprensible por fila.

La HU-15 deberá conservar el flujo existente:

```text
seleccionar archivo
        ↓
leer y analizar CSV
        ↓
normalizar
        ↓
clasificar filas
        ↓
insertar lote aceptado
        ↓
mostrar resultado
```

y añadir información detallada:

```text
fila
tipo de incidencia
causa
identidad funcional cuando esté disponible
```

No se creará un segundo caso de uso de importación ni una segunda pantalla de selección de archivos.

---

## 4. Estado real del proyecto antes de HU-15

El ZIP `AlmacenTrackerHU14.zip` confirma que la importación ya dispone de:

- `DataManagementActivity`;
- `DataManagementViewModel`;
- `ImportWarehouseItemsUseCase`;
- `ImportWarehouseItemsService`;
- `ImportWarehouseItemsResult`;
- `WarehouseItemCsvReader`;
- `AndroidCsvDocumentReader`;
- `WarehouseItemCsvCodec.decode(...)`;
- `WarehouseItemCsvReadResult`;
- `WarehouseItemCsvRow`;
- detección de formato inválido;
- normalización;
- detección de duplicados contra Room;
- detección de duplicados internos;
- inserción masiva;
- transacción Room;
- resumen con:
  - `totalRows`;
  - `importedCount`;
  - `duplicateCount`;
  - `invalidCount`;
- resultados:
  - `SUCCESS`;
  - `PARTIAL_SUCCESS`;
  - `NO_VALID_ROWS`;
  - `INVALID_SOURCE`;
  - `INVALID_FORMAT`;
  - `READ_ERROR`;
  - `PERSISTENCE_ERROR`;
  - `UNKNOWN_ERROR`.

La implementación actual pierde información relevante:

- `WarehouseItemCsvRow` no conserva número de fila;
- `WarehouseItemCsvReadResult` solo conserva `invalidRowCount`;
- una fila con cantidad incorrecta de columnas se cuenta, pero no se describe;
- una fila con campo obligatorio vacío se cuenta, pero no se identifica;
- un duplicado se cuenta, pero no se indica si ya existía en Room o estaba repetido dentro del archivo;
- `ImportWarehouseItemsResult` no expone una colección de incidencias;
- la interfaz muestra únicamente un resumen numérico.

HU-15 deberá extender estas clases y mantener la compatibilidad con el flujo ya operativo.

---

## 5. Alcance incluido

HU-15 incluye:

- conservar el número de fila física o lógica de cada registro CSV;
- clasificar incidencias de formato por fila;
- clasificar campos obligatorios vacíos;
- clasificar duplicados existentes en Room;
- clasificar duplicados dentro del archivo;
- conservar categoría y código normalizados cuando estén disponibles;
- producir una colección inmutable de incidencias;
- incluir incidencias en `ImportWarehouseItemsResult`;
- mostrar un resumen general;
- permitir consultar el detalle de incidencias;
- diferenciar advertencias de errores globales;
- ordenar incidencias por número de fila;
- limitar la cantidad representada en pantalla cuando sea necesario;
- informar si existen más incidencias de las mostradas;
- mantener la importación acumulativa;
- mantener la inserción atómica del lote aceptado;
- mantener los registros existentes;
- evitar persistir filas inválidas o duplicadas;
- conservar el comportamiento offline;
- conservar ausencia de permisos generales de almacenamiento;
- ampliar pruebas del parser;
- ampliar pruebas del servicio;
- ampliar pruebas del ViewModel y de interfaz;
- mantener CI satisfactoria.

---

## 6. Alcance excluido

HU-15 no incluye:

- corregir una fila dentro de la aplicación;
- editar el CSV;
- reintentar únicamente filas seleccionadas;
- sobrescribir duplicados;
- fusionar registros;
- preguntar al usuario qué hacer con cada fila;
- exportar un archivo de errores;
- guardar un historial permanente de importaciones;
- almacenar incidencias en Room;
- restaurar copias de seguridad;
- cambiar el formato del CSV de intercambio;
- aceptar columnas adicionales;
- aceptar encabezados en orden distinto;
- importar Excel;
- deshacer la importación;
- reemplazar los datos existentes.

La creación y restauración de copias de seguridad pertenecen a HU-16 y HU-17.

---

## 7. Tipos de incidencia

Se añadirá una clasificación explícita.

```text
ImportIssueType
├── INVALID_COLUMN_COUNT
├── MISSING_CATEGORY
├── MISSING_CODE
├── MISSING_SITE
├── DUPLICATE_EXISTING
└── DUPLICATE_IN_FILE
```

Podrán añadirse tipos técnicos únicamente cuando representen una causa que el usuario pueda comprender.

No se utilizará `UNKNOWN` como sustituto habitual de una clasificación conocida.

### 7.1. INVALID_COLUMN_COUNT

La fila no contiene exactamente cinco columnas.

Ejemplo:

```csv
MR,1050,A1
```

Mensaje orientativo:

```text
La fila no contiene las 5 columnas esperadas.
```

### 7.2. MISSING_CATEGORY

Categoría vacía o compuesta solo por espacios.

### 7.3. MISSING_CODE

Código vacío o compuesto solo por espacios.

### 7.4. MISSING_SITE

Sitio vacío o compuesto solo por espacios.

### 7.5. DUPLICATE_EXISTING

La identidad normalizada ya existe en Room.

Ejemplo:

```text
MR + 1050 ya existe.
```

### 7.6. DUPLICATE_IN_FILE

La identidad ya apareció en una fila aceptada anterior del mismo archivo.

El detalle deberá poder indicar la primera fila cuando sea viable.

Ejemplo:

```text
MR + 1050 ya apareció en la fila 2.
```

---

## 8. Modelo de incidencia

Se añadirá un modelo Java puro:

```text
ImportWarehouseItemIssue
├── rowNumber
├── type
├── category
├── code
├── message
└── relatedRowNumber
```

### Reglas

- `rowNumber` identifica la fila del archivo;
- la fila de encabezado es la fila 1;
- la primera fila de datos es la fila 2;
- `category` y `code` podrán estar vacíos si no pudieron obtenerse;
- `message` será comprensible y no contendrá stack traces;
- `relatedRowNumber` se utilizará para duplicados internos cuando corresponda;
- el modelo no dependerá de Android;
- la colección expuesta será inmutable.

No se recomienda guardar únicamente textos. El tipo estructurado permitirá:

- probar reglas;
- traducir mensajes;
- ordenar;
- filtrar;
- cambiar la presentación sin modificar el servicio.

---

## 9. Conservación del número de fila

### 9.1. Fila lógica

El parser deberá conservar la fila inicial de cada registro CSV.

Un campo entrecomillado puede contener saltos de línea:

```csv
MR,1050,A1,,"Primera línea
Segunda línea"
```

Esto representa un solo registro.

Por tanto, el número de fila de incidencia deberá corresponder al inicio del registro, no a cada salto dentro del campo.

### 9.2. Encabezado

```text
encabezado = fila 1
primer registro = fila 2
```

### 9.3. Líneas vacías

Las líneas completamente vacías podrán ignorarse.

La numeración deberá continuar reflejando la ubicación original del archivo.

---

## 10. Evolución de los modelos existentes

### 10.1. WarehouseItemCsvRow

Deberá conservar como mínimo:

```text
rowNumber
category
code
site
position
observations
```

### 10.2. WarehouseItemCsvReadResult

Deberá sustituir o complementar:

```text
invalidRowCount
```

por:

```text
List<ImportWarehouseItemIssue> parsingIssues
```

El contador podrá derivarse de la lista.

No se mantendrán dos fuentes de verdad inconsistentes.

### 10.3. ImportWarehouseItemsResult

Deberá incorporar:

```text
List<ImportWarehouseItemIssue> issues
```

y mantener:

```text
totalRows
importedCount
duplicateCount
invalidCount
```

Los contadores deberán derivarse o validarse contra la colección.

Regla obligatoria:

```text
duplicateCount
    = DUPLICATE_EXISTING
    + DUPLICATE_IN_FILE

invalidCount
    = INVALID_COLUMN_COUNT
    + MISSING_CATEGORY
    + MISSING_CODE
    + MISSING_SITE
```

No deberán existir diferencias entre el resumen y el detalle.

---

## 11. Clasificación dentro del parser y el servicio

### 11.1. Responsabilidad del parser

`WarehouseItemCsvCodec` o el componente lector deberá detectar:

- cantidad incorrecta de columnas;
- número de fila;
- sintaxis global inválida;
- encabezado inválido;
- comillas sin cerrar;
- caracteres inesperados tras cierre de comillas.

Diferencia:

```text
incidencia de una fila
    → continuar con las demás cuando sea seguro

formato global irrecuperable
    → INVALID_FORMAT
```

### 11.2. Responsabilidad del servicio

`ImportWarehouseItemsService` deberá detectar:

- categoría vacía;
- código vacío;
- sitio vacío;
- duplicado existente;
- duplicado dentro del archivo.

La normalización continuará en la capa de aplicación.

### 11.3. No duplicar validaciones

La Activity no deberá:

- comprobar columnas;
- normalizar;
- detectar duplicados;
- generar causas;
- construir incidencias.

---

## 12. Reglas de procesamiento

### 12.1. Fila con varios campos obligatorios vacíos

Se recomienda generar una incidencia por causa.

Ejemplo:

```csv
,,A1,,
```

Incidencias:

```text
MISSING_CATEGORY
MISSING_CODE
```

La fila cuenta una sola vez dentro de `invalidCount`.

Por tanto, el resultado deberá diferenciar:

```text
invalidRowCount
issueCount
```

o calcular el contador de filas inválidas por números de fila únicos.

No se deberá incrementar `invalidCount` por cada mensaje si representa filas inválidas.

### 12.2. Duplicado interno

La primera fila válida queda aceptada.

Las siguientes con la misma identidad:

- no se insertan;
- se clasifican como `DUPLICATE_IN_FILE`;
- apuntan a la primera fila aceptada.

### 12.3. Duplicado existente

No se inserta y se clasifica como `DUPLICATE_EXISTING`.

### 12.4. Prioridad de clasificación

Una fila inválida por campos obligatorios no deberá clasificarse además como duplicada.

Orden:

1. estructura;
2. normalización;
3. campos obligatorios;
4. duplicidad;
5. aceptación.

### 12.5. Fallo de persistencia

Si falla la transacción:

- no se informará que las filas aceptadas fueron importadas;
- se devolverá `PERSISTENCE_ERROR`;
- Room conservará el estado previo;
- las incidencias de análisis podrán conservarse para diagnóstico;
- la UI deberá dejar claro que el lote no fue guardado.

---

## 13. Resumen de importación

Después de una operación completada se mostrará:

```text
Importación completada

Procesadas: 10
Importadas: 6
Duplicadas: 2
Inválidas: 2
```

Estados:

### SUCCESS

```text
importadas > 0
duplicadas = 0
inválidas = 0
```

### PARTIAL_SUCCESS

```text
importadas > 0
y existen incidencias
```

### NO_VALID_ROWS

```text
importadas = 0
y existen filas procesadas
```

Los errores globales continuarán mostrando mensajes independientes:

- formato incompatible;
- lectura;
- persistencia;
- origen inválido.

---

## 14. Presentación del detalle

Se recomienda un diálogo o BottomSheet desde `DataManagementActivity`.

Contenido:

```text
Resultado de importación

6 importadas
2 duplicadas
2 inválidas

Incidencias
Fila 4 · Código obligatorio
Fila 7 · MR + 1050 ya existe
Fila 9 · Se esperaban 5 columnas
```

Acciones:

```text
Cerrar
```

Opcional:

```text
Ver incidencias
```

cuando el resumen inicial sea compacto.

### 14.1. Sin Activity adicional

No se requiere una nueva Activity si un diálogo o BottomSheet presenta el contenido de forma clara y accesible.

### 14.2. Listado

Si se utiliza RecyclerView:

```text
ImportIssueAdapter
```

será un adaptador de presentación.

No contendrá reglas de validación.

### 14.3. Límite visual

Para evitar una interfaz inmanejable:

- el resultado conservará todas las incidencias razonables en memoria durante la operación;
- la UI podrá mostrar inicialmente las primeras 100;
- si existen más, indicará:

```text
Se muestran 100 de 350 incidencias.
```

Este límite no deberá alterar los contadores.

Una política diferente es válida si está documentada y probada.

---

## 15. Estados de interfaz

`DataManagementUiState` podrá mantener los estados ya existentes:

```text
IDLE
SELECTING_SOURCE
IMPORTING
ERROR
```

El detalle final se emitirá como evento de una sola consumición:

```text
UiEvent<ImportWarehouseItemsResult>
```

No se recomienda convertir cada combinación de contadores en un estado permanente.

Mientras `IMPORTING`:

- Exportar deshabilitado;
- Compartir deshabilitado;
- Importar deshabilitado;
- progreso visible;
- no se permite iniciar otra operación.

---

## 16. Flujo principal

1. El usuario selecciona un CSV.
2. El parser valida el encabezado.
3. Lee cada registro conservando su fila inicial.
4. Crea incidencias estructurales recuperables.
5. El servicio normaliza los registros.
6. Crea incidencias de campos obligatorios.
7. Obtiene identidades existentes de Room.
8. Clasifica duplicados existentes.
9. Clasifica duplicados internos.
10. Construye el lote aceptado.
11. Room inserta el lote de manera atómica.
12. Se construye `ImportWarehouseItemsResult`.
13. Los contadores se validan contra las incidencias.
14. El ViewModel emite el resultado una sola vez.
15. La Activity muestra resumen y detalle.
16. El listado se actualiza desde Room.

---

## 17. Flujos alternativos

### FA-01 — Todas las filas válidas

Se muestra éxito sin sección de incidencias.

### FA-02 — Éxito parcial

Se muestran contadores y detalle.

### FA-03 — Ninguna fila válida

Room no cambia y se muestran todas las causas disponibles.

### FA-04 — Varias causas en una fila

La fila aparece agrupada o con varias causas, sin aumentar incorrectamente el número de filas inválidas.

### FA-05 — Duplicado interno

Se muestra la fila actual y la fila original relacionada.

### FA-06 — Duplicado existente

Se muestra categoría y código normalizados.

### FA-07 — Columna incorrecta

Se informa el número de fila y la cantidad esperada.

### FA-08 — Comillas sin cerrar

Se devuelve `INVALID_FORMAT` cuando no es posible continuar con seguridad.

### FA-09 — Archivo con líneas vacías

Las líneas se ignoran, pero la numeración original se conserva.

### FA-10 — Campo multilínea

El registro conserva la fila inicial y no genera incidencias falsas.

### FA-11 — Error de persistencia

No se informa ninguna fila como importada.

### FA-12 — Rotación con resultado abierto

El resultado no se duplica. La presentación podrá restaurarse de forma segura o volver a abrirse desde el estado conservado.

---

## 18. Criterios de aceptación

1. Cada incidencia identifica una fila.
2. La primera fila de datos se identifica como fila 2.
3. Los campos multilínea no rompen la numeración lógica.
4. Una fila con columnas incorrectas se describe.
5. Categoría vacía se clasifica correctamente.
6. Código vacío se clasifica correctamente.
7. Sitio vacío se clasifica correctamente.
8. Un duplicado de Room se distingue de uno interno.
9. Un duplicado interno referencia la primera aparición cuando sea posible.
10. Una fila inválida no se clasifica además como duplicada.
11. El resumen coincide con el detalle.
12. Varias causas de una fila no inflan `invalidCount`.
13. Las incidencias están ordenadas por fila.
14. El resultado expone una colección inmutable.
15. La UI muestra procesadas, importadas, duplicadas e inválidas.
16. La UI permite consultar causas.
17. Un éxito completo no muestra incidencias.
18. Un éxito parcial muestra advertencias, no un error global.
19. `NO_VALID_ROWS` muestra causas disponibles.
20. `INVALID_FORMAT` se reserva para errores globales irrecuperables.
21. Un fallo de persistencia no informa filas importadas.
22. La Activity no contiene reglas de validación.
23. No se crea un segundo flujo de importación.
24. La importación continúa siendo acumulativa.
25. La inserción continúa siendo atómica.
26. No se solicitan permisos generales.
27. La operación funciona sin conexión.
28. Las pruebas y CI finalizan correctamente.

---

## 19. Diseño técnico orientativo

### Nuevos modelos

```text
ImportWarehouseItemIssue
ImportIssueType
```

### Componentes a ampliar

```text
WarehouseItemCsvRow
WarehouseItemCsvReadResult
WarehouseItemCsvCodec
ImportWarehouseItemsService
ImportWarehouseItemsResult
DataManagementViewModel
DataManagementActivity
```

### Componente opcional de UI

```text
ImportIssueAdapter
```

No se añadirá un nuevo repositorio ni un nuevo caso de uso si el contrato actual puede evolucionar sin romper responsabilidades.

---

## 20. Pruebas recomendadas

### WarehouseItemCsvCodecTest

- conserva fila 2 para primer registro;
- conserva numeración tras línea vacía;
- conserva fila inicial de registro multilínea;
- incidencia por columnas insuficientes;
- incidencia por columnas adicionales;
- encabezado incorrecto sigue siendo global;
- comilla sin cerrar sigue siendo global;
- orden de incidencias;
- BOM;
- CRLF y LF.

### ImportWarehouseItemsServiceTest

- categoría vacía;
- código vacío;
- sitio vacío;
- varias causas en una fila;
- una sola fila inválida en contador;
- duplicado existente;
- duplicado interno;
- fila relacionada;
- prioridad de inválido sobre duplicado;
- contadores consistentes;
- éxito;
- éxito parcial;
- ninguna fila válida;
- error de persistencia.

### ImportWarehouseItemsResultTest

- colección inmutable;
- contadores no negativos;
- coherencia de estados;
- coherencia entre issues y contadores;
- orden estable.

### DataManagementViewModelTest

- evento de resultado único;
- resultado completo;
- resultado parcial;
- ninguna fila válida;
- error global;
- rotación;
- acciones bloqueadas durante importación.

### UI

- muestra resumen;
- oculta incidencias en éxito;
- muestra detalle en parcial;
- presenta fila y causa;
- limita lista visual;
- indica incidencias adicionales;
- accesibilidad;
- cierre del diálogo o BottomSheet.

---

## 21. Tareas de implementación

1. Crear `feature/hu-15-incidencias-importacion-csv`.
2. Crear `ImportIssueType`.
3. Crear `ImportWarehouseItemIssue`.
4. Añadir número de fila a `WarehouseItemCsvRow`.
5. Ampliar el parser para conservar posición.
6. Sustituir contador estructural por incidencias.
7. Ampliar `WarehouseItemCsvReadResult`.
8. Ampliar clasificación en `ImportWarehouseItemsService`.
9. Diferenciar duplicado existente e interno.
10. Mantener referencia de primera fila aceptada.
11. Ampliar `ImportWarehouseItemsResult`.
12. Garantizar consistencia de contadores.
13. Añadir presentación de resumen.
14. Añadir detalle de incidencias.
15. Añadir recursos de texto y plurales.
16. Ampliar pruebas del codec.
17. Ampliar pruebas del servicio.
18. Añadir pruebas del resultado.
19. Ampliar pruebas del ViewModel.
20. Añadir pruebas de interfaz necesarias.
21. Ejecutar:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

22. Verificar CI.
23. Fusionar en `develop`.
24. Verificar CI de `develop`.

---

## 22. Estrategia de commits orientativa

### Bloque 1 — Modelo de incidencias

```text
git commit -m "feat: add CSV import issue model #17"
```

### Bloque 2 — Trazabilidad del parser

```text
git commit -m "feat: track CSV import row validation issues #17"
```

### Bloque 3 — Clasificación de importación

```text
git commit -m "feat: classify warehouse import duplicates and validation issues #17"
```

### Bloque 4 — Presentación del resultado

```text
git commit -m "feat: show detailed CSV import results #17"
```

### Bloque 5 — Pruebas

```text
git commit -m "test: cover detailed CSV import issue reporting #17"
```

---

## 23. Definición de terminado

HU-15 estará terminada cuando:

- cada fila omitida tenga una causa identificable;
- las incidencias conserven el número de fila;
- los campos multilínea mantengan numeración correcta;
- los errores de columnas sean trazables;
- los campos obligatorios vacíos sean trazables;
- los duplicados existentes e internos se diferencien;
- los contadores coincidan con el detalle;
- varias causas de una fila no alteren el total de filas inválidas;
- el resultado sea inmutable;
- la UI muestre resumen y detalle;
- el éxito total no muestre advertencias;
- el éxito parcial muestre incidencias;
- `NO_VALID_ROWS` muestre causas;
- los errores globales sigan diferenciados;
- no se duplique el flujo de importación;
- Room continúe siendo la fuente de verdad;
- el lote aceptado continúe siendo atómico;
- funcione sin conexión;
- no se soliciten permisos generales;
- pruebas unitarias e instrumentadas finalicen correctamente;
- lint y compilación finalicen correctamente;
- CI de la rama y de `develop` sean satisfactorias.

La siguiente historia será:

```text
HU-16 — Crear copia de seguridad CSV
```
