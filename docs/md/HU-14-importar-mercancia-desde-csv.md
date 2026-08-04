# HU-14 — Importar mercancía desde CSV

> Tercera historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-14  
**Nombre:** Importar mercancía desde CSV  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-14-importar-mercancia-csv`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-12 — Exportar mercancía a CSV

---

## 2. Historia de usuario

Como usuario,  
quiero seleccionar un archivo CSV e importar sus registros,  
para incorporar mercancía sin registrarla manualmente una por una.

---

## 3. Objetivo

Incorporar un flujo de importación acumulativa desde la pantalla Gestión de datos.

```text
DataManagementActivity
        ↓ seleccionar archivo CSV
DataManagementViewModel
        ↓
ImportWarehouseItemsUseCase
        ↓
ImportWarehouseItemsService
        ├── WarehouseItemCsvReader
        └── WarehouseItemRepository
                ↓
        AndroidCsvDocumentReader
        RoomWarehouseItemRepository
                ↓
        ContentResolver / Room
```

La importación deberá:

- leer el formato CSV definido en HU-12;
- validar su estructura mínima;
- normalizar los datos;
- conservar los registros existentes;
- omitir duplicados;
- insertar las filas aceptadas en una única operación de Room;
- mostrar un resumen básico del resultado.

HU-14 implementará el flujo funcional de importación. La información detallada por fila y la consolidación avanzada de incidencias se completarán en HU-15.

---

## 4. Estado real del proyecto antes de HU-14

El ZIP `AlmacenTrackerHU13.zip` confirma que ya existen:

- `DataManagementActivity`;
- `DataManagementViewModel`;
- exportación y compartición CSV;
- `WarehouseItemCsvCodec`;
- `WarehouseItemCsvMapper`;
- formato de intercambio:

```csv
category,code,site,position,observations
```

- `WarehouseItemRepository.findAll()`;
- `WarehouseItemDao.findAll()`;
- `fileExecutor`;
- composición en `AppContainer`;
- Room como fuente de verdad.

Todavía no existen:

- acción Importar CSV;
- selector `OpenDocument`;
- lector CSV;
- `ImportWarehouseItemsUseCase`;
- `ImportWarehouseItemsService`;
- resultado de importación;
- inserción masiva;
- transacción de importación;
- resumen de filas importadas y omitidas.

---

## 5. Alcance incluido

HU-14 incluye:

- añadir la acción Importar CSV en Gestión de datos;
- abrir el selector de documentos de Android;
- aceptar archivos CSV accesibles mediante `ContentResolver`;
- cancelar el selector sin error;
- leer el archivo en UTF-8;
- reconocer el encabezado oficial;
- analizar campos entrecomillados;
- admitir comas, comillas y saltos de línea dentro de campos;
- convertir cada fila aceptada a datos de mercancía;
- normalizar categoría, código y sitio;
- normalizar campos opcionales;
- generar `createdAt` y `updatedAt` durante la importación;
- conservar los registros actuales;
- detectar duplicados contra Room;
- detectar duplicados dentro del mismo archivo;
- omitir duplicados sin sobrescribir;
- insertar en lote las filas aceptadas;
- ejecutar la inserción aceptada de forma atómica;
- actualizar automáticamente el listado mediante Room;
- mostrar un resumen básico;
- impedir importaciones simultáneas;
- conservar el estado ante rotación;
- funcionar sin conexión;
- no solicitar permisos generales de almacenamiento;
- pruebas unitarias, Room, ViewModel e instrumentadas.

---

## 6. Alcance excluido

HU-14 no incluye:

- reemplazar todos los registros existentes;
- restaurar una copia de seguridad;
- importar formatos Excel, JSON o PDF;
- importar desde una URL;
- descargar archivos;
- sobrescribir duplicados;
- preguntar qué hacer con cada duplicado;
- editar filas antes de importar;
- deshacer una importación;
- historial persistente de importaciones;
- mostrar una pantalla detallada con cada error;
- exportar un informe de incidencias;
- conservar `id`, `createdAt` o `updatedAt` del archivo;
- aceptar el formato de copia de seguridad;
- sincronización remota.

La validación detallada y el informe por fila pertenecen a HU-15.

---

## 7. Formato admitido

El archivo deberá usar exactamente estas columnas:

```csv
category,code,site,position,observations
```

Reglas:

- el encabezado es obligatorio;
- el orden de columnas es obligatorio;
- no se importan columnas internas;
- `category`, `code` y `site` son obligatorios;
- `position` y `observations` son opcionales;
- el archivo se interpreta como UTF-8;
- se admite última línea con o sin salto final;
- se admiten campos multilínea entre comillas.

No se utilizará:

```java
line.split(",")
```

porque no interpreta CSV válido con comas o saltos de línea dentro de campos.

---

## 8. Reglas de importación

### 8.1. Importación acumulativa

La importación añade filas aceptadas:

```text
datos existentes + filas válidas nuevas
```

No elimina ni modifica registros existentes.

### 8.2. Normalización

Antes de comprobar duplicados o persistir:

```text
category     → trim + uppercase
code         → trim + uppercase
site         → trim + uppercase
position     → trim; vacío = null
observations → trim; vacío = null
```

La lógica deberá coincidir con las reglas utilizadas al registrar mercancía manualmente.

### 8.3. Identidad funcional

Un registro se considera duplicado por:

```text
category normalizada + code normalizado
```

Debe omitirse cuando:

- ya existe en Room;
- ya apareció anteriormente dentro del mismo archivo.

El mismo código en categorías diferentes continúa permitido.

### 8.4. Fechas

Cada fila nueva recibirá:

```text
createdAt = instante de importación
updatedAt = instante de importación
```

Las fechas no se leen desde el CSV de intercambio.

### 8.5. Filas inválidas

En HU-14 una fila con campos obligatorios vacíos:

- no se persistirá;
- se contará como inválida;
- no impedirá importar las demás filas aceptadas.

HU-15 añadirá el detalle de número de fila y causa.

### 8.6. Archivo sin filas válidas

Si ninguna fila puede importarse:

- Room no cambia;
- se muestra un resultado controlado;
- no se informa éxito.

---

## 9. Selector de documentos

Se recomienda Activity Result API:

```java
ActivityResultContracts.OpenDocument
```

Tipos aceptados inicialmente:

```text
text/csv
text/comma-separated-values
application/csv
text/plain
```

`text/plain` se admite por compatibilidad con proveedores que no asignan correctamente el MIME, pero el contenido deberá superar la validación CSV.

Si el resultado es `null`:

- no se ejecuta el caso de uso;
- no aparece error;
- el estado vuelve a `IDLE`.

No se requiere permiso persistente porque la importación se procesa durante la operación actual.

---

## 10. Resultado de importación

Se añadirá:

```text
ImportWarehouseItemsResult
```

Estados recomendados:

```text
SUCCESS
PARTIAL_SUCCESS
NO_VALID_ROWS
INVALID_FORMAT
READ_ERROR
PERSISTENCE_ERROR
UNKNOWN_ERROR
```

Datos mínimos:

```text
totalRows
importedCount
duplicateCount
invalidCount
```

### SUCCESS

Todas las filas de datos fueron importadas.

### PARTIAL_SUCCESS

Al menos una fila fue importada y alguna fue omitida por duplicado o invalidez.

### NO_VALID_ROWS

No se pudo importar ninguna fila.

### INVALID_FORMAT

El encabezado o la estructura general no corresponde al formato esperado.

### READ_ERROR

No se pudo abrir o leer la URI.

### PERSISTENCE_ERROR

Falló la inserción atómica en Room.

---

## 11. Persistencia masiva

No se implementará la importación llamando repetidamente a:

```java
repository.insert(...)
```

desde el ViewModel o la Activity.

Se añadirá una operación masiva:

```text
WarehouseItemRepository.insertAll(...)
```

DAO orientativo:

```java
@Insert(onConflict = OnConflictStrategy.ABORT)
List<Long> insertAll(List<WarehouseItemEntity> entities);
```

La implementación deberá ejecutarse dentro de una transacción Room.

Reglas:

- el lote contiene únicamente filas previamente aceptadas;
- si Room falla durante la escritura, el lote completo se revierte;
- no queda una importación persistida a medias;
- `OnConflictStrategy.REPLACE` continúa prohibido.

---

## 12. Diseño técnico propuesto

### Puerto de entrada

```text
ImportWarehouseItemsUseCase
```

Firma orientativa:

```java
void importWarehouseItems(
        String sourceReference,
        Callback callback
);
```

La referencia de URI se mantiene como `String` para no introducir Android en aplicación.

### Puerto de lectura CSV

```text
WarehouseItemCsvReader
```

Responsabilidad:

- abrir la referencia;
- leer bytes o caracteres;
- interpretar CSV;
- devolver registros leídos;
- diferenciar formato inválido y error de lectura.

### Adaptador Android

```text
AndroidCsvDocumentReader
```

Responsabilidad:

- usar `ContentResolver`;
- convertir la referencia a `Uri`;
- abrir `InputStream`;
- delegar el análisis al codec;
- cerrar recursos;
- ejecutar en `fileExecutor`.

### Codec

`WarehouseItemCsvCodec` podrá ampliarse con lectura si mantiene una responsabilidad cohesionada:

```text
encode(...)
decode(...)
```

También es válida una clase separada:

```text
WarehouseItemCsvDecoder
```

No se duplicarán las reglas de escape ni se creará un parser basado en `split()`.

### Servicio

`ImportWarehouseItemsService` deberá:

1. validar la referencia;
2. leer el CSV;
3. normalizar filas;
4. clasificar filas inválidas;
5. obtener las identidades existentes;
6. omitir duplicados;
7. crear los modelos de dominio;
8. solicitar inserción masiva;
9. devolver el resumen.

### UI

`DataManagementUiState` incorporará:

```text
SELECTING_SOURCE
IMPORTING
```

El resultado final se emitirá mediante evento de una sola consumición.

---

## 13. Flujo principal

1. El usuario abre Gestión de datos.
2. Pulsa Importar CSV.
3. Android abre el selector.
4. El usuario selecciona un archivo.
5. La Activity entrega la URI al ViewModel.
6. El ViewModel cambia a `IMPORTING`.
7. El caso de uso solicita la lectura.
8. El adaptador abre el archivo mediante `ContentResolver`.
9. El parser valida encabezado y obtiene filas.
10. El servicio normaliza los campos.
11. Clasifica filas inválidas y duplicadas.
12. Construye el lote aceptado.
13. El repositorio inserta el lote en una transacción.
14. Room confirma.
15. El ViewModel emite el resumen.
16. La Activity muestra el resultado.
17. El listado se actualiza automáticamente.

---

## 14. Flujos alternativos

### FA-01 — Cancelar selector

No se inicia importación y no se muestra error.

### FA-02 — Encabezado incorrecto

Se devuelve `INVALID_FORMAT` y Room no cambia.

### FA-03 — Archivo vacío

Se devuelve `NO_VALID_ROWS`.

### FA-04 — Solo encabezado

Se devuelve `NO_VALID_ROWS`.

### FA-05 — Fila válida

Se normaliza, se inserta y aparece en el listado.

### FA-06 — Duplicado existente

Se omite y aumenta `duplicateCount`.

### FA-07 — Duplicado interno

Solo se acepta la primera aparición válida; las posteriores se omiten.

### FA-08 — Mismo código en otra categoría

Se importa correctamente.

### FA-09 — Campo obligatorio vacío

La fila se omite y aumenta `invalidCount`.

### FA-10 — Campo con coma o salto de línea

Se interpreta como una única celda cuando está correctamente entrecomillado.

### FA-11 — Error de lectura

Se devuelve `READ_ERROR` y Room no cambia.

### FA-12 — Error de persistencia

La transacción se revierte y se devuelve `PERSISTENCE_ERROR`.

### FA-13 — Doble pulsación

Solo se inicia una importación.

### FA-14 — Rotación

La operación no se duplica y el resultado llega a la Activity recreada.

### FA-15 — Modo avión

La importación desde una URI local funciona normalmente.

---

## 15. Criterios de aceptación

1. Gestión de datos muestra la acción Importar CSV.
2. Pulsar Importar abre el selector del sistema.
3. Cancelar el selector no muestra error.
4. Se reconoce el encabezado oficial.
5. Un encabezado incompatible no modifica Room.
6. Se leen correctamente campos entrecomillados.
7. Las comas dentro de observaciones no separan columnas.
8. Los saltos de línea entrecomillados no crean filas falsas.
9. Categoría, código y sitio se normalizan.
10. Posición y observaciones vacías se convierten en ausencia.
11. Las fechas se generan durante la importación.
12. Los registros existentes se conservan.
13. Los duplicados existentes se omiten.
14. Los duplicados internos se omiten.
15. El mismo código en otra categoría se permite.
16. Las filas inválidas no se persisten.
17. Las filas válidas se insertan en lote.
18. El lote aceptado se persiste de forma atómica.
19. Un error de Room no deja una importación parcial.
20. El resumen muestra importados, duplicados e inválidos.
21. No se ejecuta trabajo de archivo o Room en el hilo principal.
22. No se solicitan permisos generales de almacenamiento.
23. La Activity no analiza CSV ni accede al DAO.
24. La operación funciona sin conexión.
25. La rotación no duplica la importación.
26. Las pruebas y la CI finalizan correctamente.

---

## 16. Pruebas recomendadas

### Parser CSV

- encabezado correcto;
- encabezado incorrecto;
- archivo vacío;
- solo encabezado;
- fila simple;
- campos vacíos;
- comas entrecomilladas;
- comillas escapadas;
- CRLF;
- LF;
- campo multilínea;
- Unicode;
- columnas insuficientes;
- columnas adicionales;
- comilla sin cerrar.

### Servicio

- referencia inválida;
- normalización;
- duplicados existentes;
- duplicados internos;
- mismo código en categoría distinta;
- filas inválidas;
- éxito total;
- éxito parcial;
- ninguna fila válida;
- error de lectura;
- error de persistencia.

### Room

- inserción masiva;
- ids generados;
- transacción atómica;
- restricción compuesta;
- rollback ante conflicto;
- actualización observable del listado.

### ViewModel e interfaz

- abrir selector;
- cancelar;
- estado `IMPORTING`;
- bloqueo de acciones;
- resumen de éxito;
- resumen parcial;
- errores;
- evento único;
- rotación.

---

## 17. Tareas de implementación

1. Crear `feature/hu-14-importar-mercancia-csv`.
2. Añadir acción Importar CSV al layout.
3. Registrar `OpenDocument`.
4. Crear modelos de lectura CSV.
5. Implementar parser CSV compatible con el formato de HU-12.
6. Crear `WarehouseItemCsvReader`.
7. Implementar `AndroidCsvDocumentReader`.
8. Crear `ImportWarehouseItemsUseCase`.
9. Crear `ImportWarehouseItemsService`.
10. Crear `ImportWarehouseItemsResult`.
11. Añadir callback de inserción masiva.
12. Añadir `insertAll()` al repositorio.
13. Añadir inserción masiva al DAO.
14. Implementar transacción en Room.
15. Actualizar `DataManagementUiState`.
16. Actualizar `DataManagementViewModel`.
17. Actualizar Factory y `AppContainer`.
18. Añadir textos y plurales.
19. Añadir pruebas unitarias.
20. Añadir pruebas DAO e instrumentadas.
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

## 18. Estrategia de commits orientativa

### Bloque 1 — Lectura y parser CSV

```text
git commit -m "feat: add CSV warehouse item import reader #16"
```

### Bloque 2 — Inserción masiva Room

```text
git commit -m "feat: add transactional warehouse item batch insert #16"
```

### Bloque 3 — Caso de uso de importación

```text
git commit -m "feat: add warehouse item CSV import use case #16"
```

### Bloque 4 — Integración de interfaz

```text
git commit -m "feat: integrate CSV import into data management #16"
```

### Bloque 5 — Pruebas

```text
git commit -m "test: cover warehouse item CSV import flow #16"
```

---

## 19. Definición de terminado

HU-14 estará terminada cuando:

- el usuario pueda seleccionar un CSV;
- el formato oficial se interprete correctamente;
- el archivo incompatible se rechace sin modificar Room;
- las filas aceptadas se normalicen;
- los duplicados se omitan;
- las filas inválidas se omitan;
- los registros existentes se conserven;
- el lote se inserte en una transacción;
- no exista persistencia parcial ante fallo;
- se muestre un resumen básico;
- el listado se actualice desde Room;
- no se soliciten permisos generales;
- funcione sin conexión;
- la UI no contenga lógica CSV ni Room;
- las pruebas unitarias finalicen correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- lint y compilación finalicen correctamente;
- CI de la rama y de `develop` sean satisfactorias.

La siguiente historia será:

```text
HU-15 — Validar e informar incidencias de importación
```

HU-15 ampliará el resumen básico con detalles por fila y causas específicas, sin crear un segundo flujo de importación.
