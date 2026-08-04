# HU-17 — Restaurar copia de seguridad CSV

> Sexta historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-17  
**Nombre:** Restaurar copia de seguridad CSV  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-17-restaurar-copia-seguridad-csv`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-16 — Crear copia de seguridad CSV  
**Issue prevista:** `#19`

---

## 2. Historia de usuario

Como usuario,  
quiero restaurar una copia de seguridad creada por AlmacenTracker,  
para reemplazar los datos actuales por el estado conservado en el archivo.

---

## 3. Objetivo

Añadir en Gestión de datos un flujo seguro de restauración desde el formato versionado definido en HU-16.

```text
DataManagementActivity
        ↓ seleccionar respaldo
DataManagementViewModel
        ↓
ValidateWarehouseBackupUseCase
        ↓
WarehouseBackupCsvReader
        ↓
respaldo válido
        ↓ confirmación explícita
RestoreWarehouseBackupUseCase
        ↓
WarehouseItemRepository.replaceAll(...)
        ↓
Room / SQLite
```

La restauración deberá:

- leer el formato de respaldo de HU-16;
- validar completamente el archivo antes de modificar Room;
- mostrar un resumen previo;
- requerir confirmación explícita;
- reemplazar todos los registros actuales;
- conservar los datos existentes si cualquier paso falla;
- generar nuevos identificadores internos;
- conservar las fechas válidas del respaldo;
- actualizar automáticamente el listado mediante Room.

---

## 4. Estado real del proyecto antes de HU-17

El ZIP `AlmacenTrackerHU16.zip` confirma que ya existen:

- `DataManagementActivity`;
- `DataManagementViewModel`;
- `CreateWarehouseBackupUseCase`;
- `CreateWarehouseBackupService`;
- `CreateWarehouseBackupResult`;
- `WarehouseBackupCsvExporter`;
- `AndroidWarehouseBackupDocumentExporter`;
- `WarehouseBackupCsvCodec`;
- `WarehouseBackupCsvMapper`;
- formato de respaldo versión 1;
- selector `CreateDocument("text/csv")`;
- `WarehouseItemRepository.findAll()`;
- `WarehouseItemRepository.insertAll()`;
- `WarehouseItemDao.findAll()`;
- `WarehouseItemDao.insertAll()`;
- `WarehouseItemDao.deleteAll()`;
- executor de archivos;
- executor de persistencia;
- bloqueo de operaciones simultáneas;
- eventos de una sola consumición;
- composición explícita en `AppContainer`.

El formato generado actualmente es:

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

Todavía no existen:

- acción Restaurar copia;
- selector específico de respaldo;
- lector del CSV de copia de seguridad;
- modelos de lectura de respaldo;
- validación completa del archivo;
- resumen previo a la restauración;
- confirmación destructiva;
- `ValidateWarehouseBackupUseCase`;
- `RestoreWarehouseBackupUseCase`;
- operación `WarehouseItemRepository.replaceAll(...)`;
- transacción Room para borrar e insertar;
- resultado de restauración;
- pruebas del rollback de restauración.

HU-17 deberá añadir estas capacidades sin reutilizar la importación acumulativa como sustituto de una restauración.

---

## 5. Alcance incluido

HU-17 incluye:

- añadir Restaurar copia de seguridad en Gestión de datos;
- abrir el selector de documentos de Android;
- aceptar archivos accesibles mediante `ContentResolver`;
- cancelar el selector sin error;
- leer UTF-8;
- validar el encabezado exacto;
- validar `format_version`;
- aceptar únicamente la versión compatible;
- interpretar campos CSV entrecomillados;
- admitir comas, comillas y saltos de línea;
- revertir la protección de fórmulas de forma controlada;
- validar todas las filas antes de modificar Room;
- validar campos obligatorios;
- validar fechas;
- validar duplicados internos;
- normalizar categoría, código y sitio;
- convertir opcionales vacíos a ausencia;
- generar nuevos ids locales;
- conservar `createdAt`;
- conservar `updatedAt`;
- mostrar cantidad de registros restaurables;
- mostrar confirmación explícita;
- reemplazar todos los registros en una transacción;
- permitir restaurar una copia vacía;
- conservar datos actuales si falla la transacción;
- actualizar automáticamente listado, búsqueda y filtros;
- impedir operaciones simultáneas;
- conservar estado ante rotación;
- evitar doble confirmación;
- mostrar resultado de éxito o error;
- funcionar sin conexión;
- no solicitar permisos generales de almacenamiento;
- pruebas unitarias;
- pruebas Room;
- pruebas de ViewModel;
- pruebas instrumentadas;
- CI.

---

## 6. Alcance excluido

HU-17 no incluye:

- importar el CSV de intercambio;
- añadir registros a los existentes;
- fusionar datos;
- conservar ids originales;
- resolver conflictos uno por uno;
- sobrescribir solo duplicados;
- elegir filas concretas;
- previsualizar todos los campos de todas las filas;
- editar el respaldo;
- corregir errores desde la aplicación;
- exportar un informe de incidencias;
- deshacer después de confirmar;
- crear una copia automática previa;
- mantener historial de restauraciones;
- cifrado;
- contraseña;
- compresión;
- restauración desde URL;
- integración directa con nube;
- sincronización remota;
- aceptar versiones de formato desconocidas.

La importación acumulativa continúa perteneciendo a HU-14 y HU-15.

---

## 7. Diferencia entre importar y restaurar

### Importar CSV

```text
datos existentes
+
filas válidas nuevas
```

Características:

- no elimina registros;
- omite duplicados;
- puede finalizar parcialmente;
- genera fechas nuevas.

### Restaurar copia

```text
datos actuales
→ reemplazados por el respaldo validado
```

Características:

- elimina el conjunto actual;
- no admite resultado parcial;
- conserva fechas del respaldo;
- genera ids nuevos;
- exige confirmación;
- toda la escritura es atómica.

No se implementará la restauración llamando a:

```text
ImportWarehouseItemsUseCase
```

ni combinando:

```text
deleteAll()
+
insertAll()
```

desde el ViewModel.

---

## 8. Formato admitido

Encabezado obligatorio:

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

Reglas:

- ocho columnas exactas;
- orden obligatorio;
- encabezado en la primera fila;
- UTF-8;
- última línea con o sin salto final;
- campos multilínea entre comillas;
- sin columna `id`;
- sin columnas adicionales;
- `format_version` obligatorio en cada fila.

---

## 9. Compatibilidad de versión

HU-17 aceptará:

```text
format_version = 1
```

Se rechazará:

```text
format_version vacío
format_version no numérico
format_version = 0
format_version > 1
versiones mezcladas dentro del mismo archivo
```

Resultado recomendado:

```text
UNSUPPORTED_FORMAT_VERSION
```

El mensaje deberá diferenciarse de un CSV genéricamente inválido:

```text
La versión de esta copia de seguridad no es compatible.
```

No se intentará interpretar una versión futura como si fuera la versión 1.

---

## 10. Validación previa completa

Antes de mostrar la confirmación se deberá completar:

1. apertura del documento;
2. lectura completa;
3. validación de UTF-8;
4. validación del encabezado;
5. validación de sintaxis CSV;
6. validación de versión;
7. validación del número de columnas;
8. validación de campos obligatorios;
9. normalización;
10. validación de fechas;
11. validación de duplicados internos;
12. construcción del conjunto restaurable.

Durante esta fase:

```text
Room no cambia
```

Si se detecta una sola fila inválida:

- el respaldo completo se rechaza;
- no se ofrece confirmación;
- no se conserva un subconjunto;
- no se eliminan datos actuales.

A diferencia de la importación, la restauración no admite éxito parcial.

---

## 11. Reglas de datos

### 11.1. Categoría

```text
trim
uppercase con Locale.ROOT
obligatoria
```

### 11.2. Código

```text
trim
uppercase con Locale.ROOT
obligatorio
```

### 11.3. Sitio

```text
trim
uppercase según la regla vigente
obligatorio
```

### 11.4. Posición

```text
trim
vacío → null
```

### 11.5. Observaciones

```text
trim
vacío → null
```

### 11.6. Fechas

```text
created_at > 0
updated_at > 0
updated_at >= created_at
```

Las fechas se conservarán exactamente.

### 11.7. Identidad funcional

Cada combinación normalizada:

```text
category + code
```

deberá aparecer una sola vez en el respaldo.

El mismo código en categorías distintas continúa permitido.

---

## 12. Duplicados internos

Ejemplo inválido:

```csv
1,MR,1050,A1,,,1721304000000,1721304000000
1,mr,1050,B2,,,1721305000000,1721305000000
```

Después de normalizar:

```text
MR + 1050
MR + 1050
```

Resultado:

```text
DUPLICATE_IN_BACKUP
```

El archivo completo se rechazará.

La validación no deberá depender únicamente del índice único de Room, porque los registros actuales todavía no se han eliminado y el archivo debe verificarse antes de iniciar la transacción.

---

## 13. Copia vacía

Un archivo con solo el encabezado es válido:

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

Resumen previo:

```text
La copia no contiene registros.
```

Después de confirmar:

```text
Room queda vacío
```

La eliminación de todos los datos mediante una copia vacía requiere la misma confirmación destructiva que cualquier otra restauración.

---

## 14. Modelos propuestos

### 14.1. WarehouseBackupCsvRow

```text
WarehouseBackupCsvRow
├── rowNumber
├── formatVersion
├── category
├── code
├── site
├── position
├── observations
├── createdAt
└── updatedAt
```

### 14.2. WarehouseBackupValidationIssue

```text
WarehouseBackupValidationIssue
├── rowNumber
├── type
└── message
```

Tipos recomendados:

```text
INVALID_COLUMN_COUNT
UNSUPPORTED_FORMAT_VERSION
MISSING_CATEGORY
MISSING_CODE
MISSING_SITE
INVALID_CREATED_AT
INVALID_UPDATED_AT
UPDATED_BEFORE_CREATED
DUPLICATE_IN_BACKUP
```

### 14.3. ValidatedWarehouseBackup

```text
ValidatedWarehouseBackup
├── formatVersion
├── items
└── itemCount
```

Reglas:

- Java puro;
- colección inmutable;
- no contiene `Uri`;
- no contiene entidades Room;
- no expone una colección modificable.

---

## 15. Separación entre validación y restauración

Se recomienda dividir el flujo en dos operaciones de aplicación.

### Validar respaldo

```text
ValidateWarehouseBackupUseCase
```

Responsabilidad:

- leer;
- analizar;
- validar;
- construir `ValidatedWarehouseBackup`;
- no modificar Room.

### Restaurar respaldo

```text
RestoreWarehouseBackupUseCase
```

Responsabilidad:

- recibir un respaldo ya validado;
- ejecutar el reemplazo atómico;
- devolver el resultado.

Esta separación evita:

- volver a leer el archivo después de confirmar;
- que el documento cambie entre validación y restauración;
- borrar datos antes de conocer el contenido;
- mezclar confirmación Android con lógica de archivos.

Si se conserva el respaldo validado en memoria, debe mantenerse únicamente durante el flujo actual y liberarse al finalizar o cancelar.

---

## 16. Lector del respaldo

Se añadirá un puerto de salida:

```text
WarehouseBackupCsvReader
```

Implementación Android:

```text
AndroidWarehouseBackupDocumentReader
```

Responsabilidades:

- convertir referencia opaca a `Uri`;
- abrir mediante `ContentResolver`;
- leer en `fileExecutor`;
- delegar análisis al codec;
- cerrar recursos;
- devolver resultado estructurado.

No deberá:

- eliminar Room;
- mostrar diálogos;
- acceder al ViewModel;
- decidir la confirmación.

---

## 17. Codec del respaldo

`WarehouseBackupCsvCodec` deberá evolucionar para soportar:

```text
encode(...)
decode(...)
```

Siempre que mantenga una responsabilidad cohesionada.

También será válida una clase separada:

```text
WarehouseBackupCsvDecoder
```

La decisión deberá evitar:

- duplicar parser CSV;
- utilizar `split(",")`;
- reutilizar directamente el decoder del CSV de intercambio con ocho condicionales;
- mezclar reglas de importación y restauración.

Las utilidades puras de parsing, escape y protección de fórmulas podrán compartirse cuando ya exista una abstracción real.

---

## 18. Confirmación obligatoria

La confirmación solo se mostrará después de una validación satisfactoria.

Título:

```text
Restaurar copia de seguridad
```

Mensaje con registros:

```text
Se reemplazarán todos los registros actuales por 25 registros de la copia.
Esta acción no se puede deshacer desde la aplicación.
```

Mensaje con copia vacía:

```text
La copia no contiene registros.
Se eliminarán todos los registros actuales.
Esta acción no se puede deshacer desde la aplicación.
```

Acciones:

```text
Cancelar
Restaurar
```

Reglas:

- Cancelar no modifica Room;
- cerrar el diálogo equivale a cancelar;
- no abrir con archivo inválido;
- no confirmar automáticamente;
- no reutilizar un diálogo de importación;
- la acción destructiva debe estar claramente identificada.

---

## 19. Estado pendiente de confirmación

El ViewModel deberá conservar temporalmente:

```text
ValidatedWarehouseBackup
```

y emitir un evento:

```text
UiEvent<WarehouseBackupRestorePreview>
```

El preview podrá contener:

```text
recordCount
emptyBackup
formatVersion
```

La Activity muestra el diálogo y devuelve la decisión:

```text
confirmRestore()
cancelRestore()
```

Al cancelar:

- se elimina el respaldo pendiente;
- se vuelve a `IDLE`;
- no se modifica Room.

Al confirmar:

- se consume una sola vez;
- no se permite una segunda confirmación;
- comienza `RESTORING_BACKUP`.

---

## 20. Reemplazo atómico en Room

Se añadirá una operación de repositorio:

```text
WarehouseItemRepository.replaceAll(...)
```

y un callback específico.

DAO orientativo:

```java
@Transaction
default void replaceAll(
        List<WarehouseItemEntity> entities
) {
    deleteAll();
    insertAllInternal(entities);
}
```

La implementación final deberá asegurar:

```text
BEGIN TRANSACTION
    DELETE FROM warehouse_items
    INSERT respaldo validado
COMMIT
```

Ante cualquier excepción:

```text
ROLLBACK
```

Consecuencia obligatoria:

```text
los registros anteriores permanecen intactos
```

No se ejecutarán dos tareas separadas en el repositorio:

```text
executor.execute(deleteAll)
executor.execute(insertAll)
```

porque perderían atomicidad.

---

## 21. Identificadores restaurados

El respaldo no contiene ids.

Durante la inserción:

```text
id = 0
```

o la representación utilizada para autogeneración.

Room asignará nuevos ids.

La restauración garantiza:

- contenido funcional;
- identidad categoría + código;
- ubicación;
- observaciones;
- fechas.

No garantiza conservar:

```text
id anterior
```

La UI no deberá presentar los ids como datos restaurables.

---

## 22. Resultado de validación

Se añadirá:

```text
ValidateWarehouseBackupResult
```

Estados recomendados:

```text
VALID
INVALID_SOURCE
INVALID_FORMAT
UNSUPPORTED_FORMAT_VERSION
INVALID_DATA
READ_ERROR
UNKNOWN_ERROR
```

Datos de `VALID`:

```text
ValidatedWarehouseBackup
```

Datos de error opcionales:

```text
rowNumber
message
```

No es necesario construir un informe tan amplio como HU-15, pero el error debe identificar una fila cuando sea posible.

---

## 23. Resultado de restauración

Se añadirá:

```text
RestoreWarehouseBackupResult
```

Estados recomendados:

```text
SUCCESS
INVALID_BACKUP
PERSISTENCE_ERROR
UNKNOWN_ERROR
```

Datos:

```text
restoredCount
```

### SUCCESS

La transacción fue confirmada.

### INVALID_BACKUP

El caso de uso recibió un respaldo nulo, vacío estructuralmente o no validado.

### PERSISTENCE_ERROR

Room revirtió la transacción.

### UNKNOWN_ERROR

Fallo no clasificado.

No se utilizará `PARTIAL_SUCCESS`.

---

## 24. Estados de interfaz

`DataManagementUiState` podrá añadir:

```text
SELECTING_BACKUP_SOURCE
VALIDATING_BACKUP
AWAITING_RESTORE_CONFIRMATION
RESTORING_BACKUP
```

### SELECTING_BACKUP_SOURCE

- selector abierto;
- todas las acciones deshabilitadas.

### VALIDATING_BACKUP

- lectura y validación en curso;
- progreso visible;
- mensaje:

```text
Validando copia de seguridad…
```

### AWAITING_RESTORE_CONFIRMATION

- archivo validado;
- diálogo pendiente;
- no iniciar otras operaciones.

### RESTORING_BACKUP

- transacción en curso;
- diálogo cerrado;
- acciones deshabilitadas;
- mensaje:

```text
Restaurando copia de seguridad…
```

El éxito y los errores se emitirán como eventos de una sola consumición o estados controlados según el patrón actual.

---

## 25. Flujo principal

1. El usuario abre Gestión de datos.
2. Pulsa Restaurar copia.
3. Android abre el selector.
4. El usuario selecciona un archivo.
5. La Activity entrega la referencia al ViewModel.
6. El ViewModel cambia a `VALIDATING_BACKUP`.
7. El caso de uso lee el documento.
8. El codec valida encabezado y sintaxis.
9. El servicio valida versión, campos, fechas y duplicados.
10. Se construye `ValidatedWarehouseBackup`.
11. El ViewModel conserva el respaldo validado.
12. La Activity muestra la confirmación.
13. El usuario pulsa Restaurar.
14. El ViewModel cambia a `RESTORING_BACKUP`.
15. El caso de uso solicita `replaceAll`.
16. Room inicia una transacción.
17. Elimina los registros actuales.
18. Inserta los registros restaurados.
19. Room confirma la transacción.
20. El ViewModel emite éxito.
21. La Activity muestra la cantidad restaurada.
22. El estado vuelve a `IDLE`.
23. El listado y filtros se actualizan desde Room.

---

## 26. Flujos alternativos

### FA-01 — Cancelar selector

No se valida ni modifica Room.

### FA-02 — Encabezado de intercambio

Un archivo de HU-12 se rechaza como formato de respaldo inválido.

### FA-03 — Versión no compatible

No aparece confirmación y Room no cambia.

### FA-04 — Fila inválida

El respaldo completo se rechaza.

### FA-05 — Duplicado interno

El respaldo completo se rechaza.

### FA-06 — Fecha inválida

El respaldo completo se rechaza.

### FA-07 — Cancelar confirmación

Se descarta el respaldo validado y Room no cambia.

### FA-08 — Copia vacía confirmada

Room queda vacío y se muestra éxito con cero registros.

### FA-09 — Error al eliminar

La transacción se revierte.

### FA-10 — Error al insertar

La transacción se revierte y los datos anteriores permanecen.

### FA-11 — Doble confirmación

Solo se ejecuta una restauración.

### FA-12 — Rotación durante validación

No se abre dos veces el selector ni se duplica la lectura.

### FA-13 — Rotación con diálogo

La confirmación se restaura de forma segura o vuelve a emitirse sin ejecutar automáticamente la restauración.

### FA-14 — Rotación durante restauración

No se inicia una segunda transacción.

### FA-15 — Modo avión

La restauración funciona con un proveedor local accesible.

---

## 27. Criterios de aceptación

1. Gestión de datos muestra Restaurar copia de seguridad.
2. Pulsar la acción abre el selector.
3. Cancelar no muestra error.
4. Solo se acepta el encabezado de respaldo.
5. Un CSV de intercambio no se restaura.
6. Solo se acepta `format_version = 1`.
7. Versiones mezcladas se rechazan.
8. El archivo se valida completamente antes de modificar Room.
9. Una fila inválida rechaza todo el respaldo.
10. Los duplicados internos rechazan todo el respaldo.
11. Las fechas inválidas rechazan todo el respaldo.
12. La confirmación solo aparece después de validar.
13. La confirmación informa que los datos serán reemplazados.
14. Cancelar confirmación conserva Room.
15. Confirmar reemplaza todos los registros.
16. Los ids originales no se conservan.
17. Las fechas se conservan exactamente.
18. Una copia vacía es válida.
19. Confirmar una copia vacía deja Room vacío.
20. El reemplazo se ejecuta en una única transacción.
21. Un fallo de eliminación revierte la operación.
22. Un fallo de inserción revierte la operación.
23. Ante fallo permanecen los datos anteriores.
24. No existe resultado parcial.
25. El listado se actualiza automáticamente.
26. Búsqueda y filtros se recalculan desde Room.
27. No se bloquea el hilo principal.
28. No se permiten operaciones simultáneas.
29. La rotación no duplica la restauración.
30. El éxito se consume una sola vez.
31. No se solicitan permisos generales.
32. Funciona sin conexión.
33. La Activity no analiza CSV ni accede al DAO.
34. Las pruebas, lint, compilación y CI finalizan correctamente.

---

## 28. Pruebas recomendadas

### WarehouseBackupCsvCodecTest

- decode de encabezado correcto;
- rechazo del encabezado de intercambio;
- versión 1;
- versión no compatible;
- versiones mezcladas;
- fila completa;
- opcionales vacíos;
- comas;
- comillas;
- multilínea;
- Unicode;
- reversión de protección de fórmulas;
- columnas insuficientes;
- columnas adicionales;
- comilla sin cerrar;
- archivo vacío sin encabezado;
- solo encabezado;
- CRLF y LF.

### ValidateWarehouseBackupServiceTest

- referencia inválida;
- respaldo válido;
- copia vacía;
- categoría vacía;
- código vacío;
- sitio vacío;
- `createdAt <= 0`;
- `updatedAt <= 0`;
- `updatedAt < createdAt`;
- duplicado interno;
- normalización;
- mismo código en categoría distinta;
- error de lectura;
- formato inválido;
- versión incompatible.

### RestoreWarehouseBackupServiceTest

- respaldo nulo;
- reemplazo con registros;
- reemplazo vacío;
- cantidad restaurada;
- error de persistencia;
- error desconocido;
- no resultado parcial.

### WarehouseItemDaoTest

- `replaceAll` elimina datos anteriores;
- `replaceAll` inserta el respaldo;
- genera ids nuevos;
- conserva fechas;
- respaldo vacío;
- rollback ante conflicto;
- rollback ante excepción;
- los datos previos permanecen tras fallo.

### DataManagementViewModelTest

- solicitud de fuente;
- cancelación del selector;
- validación;
- preview;
- cancelar confirmación;
- confirmar una vez;
- estado `RESTORING_BACKUP`;
- éxito;
- copia vacía;
- error de validación;
- error de persistencia;
- rotación;
- bloqueo de acciones.

### Pruebas instrumentadas

- lectura mediante URI;
- restauración real sobre Room;
- rollback real;
- actualización observable del listado;
- modo avión;
- ausencia de permisos generales.

---

## 29. Tareas de implementación

1. Crear `feature/hu-17-restaurar-copia-seguridad-csv`.
2. Crear modelos de fila y validación.
3. Ampliar o separar decoder de respaldo.
4. Crear `WarehouseBackupCsvReader`.
5. Crear `AndroidWarehouseBackupDocumentReader`.
6. Crear `ValidateWarehouseBackupUseCase`.
7. Crear `ValidateWarehouseBackupService`.
8. Crear resultados de validación.
9. Crear preview de restauración.
10. Crear `RestoreWarehouseBackupUseCase`.
11. Crear `RestoreWarehouseBackupService`.
12. Crear resultado de restauración.
13. Añadir `replaceAll()` al repositorio.
14. Añadir reemplazo transaccional al DAO.
15. Implementar callback de reemplazo.
16. Añadir acción al layout.
17. Registrar selector `OpenDocument`.
18. Ampliar `DataManagementUiState`.
19. Ampliar `DataManagementViewModel`.
20. Implementar confirmación.
21. Ampliar Factory y `AppContainer`.
22. Añadir textos y plurales.
23. Añadir pruebas unitarias.
24. Añadir pruebas Room e instrumentadas.
25. Ejecutar:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

26. Verificar CI.
27. Fusionar en `develop`.
28. Verificar CI de `develop`.

---

## 30. Estrategia de commits orientativa

### Bloque 1 — Lectura y validación del respaldo

```text
git commit -m "feat: add warehouse backup CSV validation #19"
```

### Bloque 2 — Reemplazo atómico en Room

```text
git commit -m "feat: add transactional warehouse backup replacement #19"
```

### Bloque 3 — Caso de uso de restauración

```text
git commit -m "feat: add warehouse backup restore use case #19"
```

### Bloque 4 — Integración de interfaz

```text
git commit -m "feat: integrate backup restore into data management #19"
```

### Bloque 5 — Pruebas

```text
git commit -m "test: cover warehouse backup restore flow #19"
```

---

## 31. Definición de terminado

HU-17 estará terminada cuando:

- el usuario pueda seleccionar una copia;
- se valide completamente antes de modificar Room;
- solo se acepte el formato versión 1;
- un archivo inválido no altere los datos;
- se muestre una confirmación destructiva;
- cancelar conserve los datos;
- confirmar reemplace el conjunto completo;
- la copia vacía pueda dejar Room vacío;
- se generen ids nuevos;
- se conserven las fechas;
- la escritura sea atómica;
- cualquier fallo produzca rollback;
- no exista éxito parcial;
- Room continúe siendo la fuente de verdad;
- la UI se actualice automáticamente;
- funcione sin conexión;
- no solicite permisos generales;
- las capas de UI, aplicación, archivo y Room permanezcan separadas;
- las pruebas unitarias e instrumentadas finalicen correctamente;
- lint y compilación finalicen correctamente;
- CI de la rama y de `develop` sean satisfactorias.

Con HU-17 quedará cerrado el alcance funcional previsto para AlmacenTracker v1.1.
