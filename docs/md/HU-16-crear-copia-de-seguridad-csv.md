# HU-16 — Crear copia de seguridad CSV

> Quinta historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-16  
**Nombre:** Crear copia de seguridad CSV  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-16-crear-copia-seguridad-csv`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-12 — Exportar mercancía a CSV  
**Historia posterior relacionada:** HU-17 — Restaurar copia de seguridad CSV

---

## 2. Historia de usuario

Como usuario,  
quiero crear una copia de seguridad completa de la mercancía,  
para conservar sus datos y fechas fuera de la aplicación y poder restaurarlos posteriormente.

---

## 3. Objetivo

Añadir en Gestión de datos una operación específica para crear un respaldo CSV versionado.

```text
DataManagementActivity
        ↓ elegir destino
DataManagementViewModel
        ↓
CreateWarehouseBackupUseCase
        ↓
CreateWarehouseBackupService
        ├── WarehouseItemRepository
        └── WarehouseBackupCsvExporter
                ↓
        RoomWarehouseItemRepository
        AndroidWarehouseBackupDocumentExporter
                ↓
        Room / ContentResolver
```

La copia deberá:

- incluir todos los registros de Room;
- conservar `createdAt` y `updatedAt`;
- incluir una versión explícita del formato;
- omitir el `id` interno;
- escribir UTF-8;
- utilizar el selector de documentos de Android;
- quedar preparada para la restauración de HU-17.

La copia de seguridad será un formato distinto del CSV de intercambio.

---

## 4. Estado real del proyecto antes de HU-16

El ZIP `AlmacenTrackerHU15.zip` confirma que ya existen:

- `DataManagementActivity`;
- `DataManagementViewModel`;
- selector `CreateDocument("text/csv")` para exportación;
- `WarehouseItemRepository.findAll()`;
- `WarehouseItemDao.findAll()`;
- `AndroidCsvDocumentExporter`;
- `WarehouseItemCsvCodec`;
- `WarehouseItemCsvMapper`;
- `fileExecutor`;
- exportación, compartición e importación CSV;
- resultados y eventos de una sola consumición;
- bloqueo de operaciones simultáneas;
- composición explícita en `AppContainer`.

El formato de intercambio actual es:

```csv
category,code,site,position,observations
```

Todavía no existen:

- acción Crear copia de seguridad;
- `CreateWarehouseBackupUseCase`;
- `CreateWarehouseBackupService`;
- resultado de creación de respaldo;
- codec específico de respaldo;
- mapper específico de respaldo;
- adaptador de escritura de respaldo;
- encabezado versionado;
- exportación de fechas;
- nombre sugerido de respaldo.

HU-16 deberá reutilizar la infraestructura común que tenga sentido, pero no modificar el formato de intercambio existente.

---

## 5. Alcance incluido

HU-16 incluye:

- añadir Crear copia de seguridad en Gestión de datos;
- abrir el selector de creación de documentos;
- sugerir un nombre de archivo `.csv`;
- permitir elegir nombre y ubicación;
- cancelar el selector sin error;
- recuperar todos los registros de Room;
- crear una instantánea ordenada;
- escribir un CSV de respaldo versionado;
- incluir categoría;
- incluir código;
- incluir sitio;
- incluir posición;
- incluir observaciones;
- incluir fecha de creación;
- incluir fecha de actualización;
- excluir el `id`;
- preservar valores opcionales vacíos;
- preservar caracteres especiales mediante escape CSV;
- proteger valores frente a fórmulas con la misma política reversible existente;
- usar UTF-8;
- usar terminadores de línea uniformes;
- validar datos antes de escribir;
- impedir respaldos simultáneos;
- bloquear las demás acciones durante la operación;
- conservar el estado ante rotación;
- evitar repetir el mensaje de éxito;
- informar la cantidad respaldada;
- funcionar sin conexión;
- no solicitar permisos generales de almacenamiento;
- pruebas unitarias;
- pruebas del adaptador;
- pruebas de ViewModel;
- pruebas instrumentadas;
- CI.

---

## 6. Alcance excluido

HU-16 no incluye:

- restaurar la copia;
- seleccionar un archivo de respaldo;
- reemplazar datos de Room;
- confirmar una restauración;
- conservar el `id` original;
- cifrar el archivo;
- protegerlo con contraseña;
- comprimirlo;
- programar respaldos automáticos;
- guardar copias en una ruta fija;
- subir copias automáticamente a la nube;
- integrar Google Drive mediante API;
- compartir automáticamente la copia;
- mantener historial de respaldos;
- verificar el contenido después de una restauración;
- aceptar otros formatos;
- incluir metadatos de dispositivo;
- incluir versión Android o modelo del teléfono.

La restauración pertenece a HU-17.

---

## 7. Diferencia entre exportación y copia de seguridad

### Exportación CSV

Finalidad:

```text
intercambiar datos comprensibles
```

Formato:

```csv
category,code,site,position,observations
```

No conserva fechas.

### Copia de seguridad CSV

Finalidad:

```text
reconstruir el estado funcional de los registros
```

Formato:

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

Conserva fechas.

No deberá reutilizarse `WarehouseItemCsvCodec.encode(...)` añadiendo condicionales según el tipo de operación. Se recomienda un codec específico:

```text
WarehouseBackupCsvCodec
```

Ambos codecs podrán compartir utilidades puras de escape cuando eso reduzca duplicación real.

---

## 8. Formato oficial del respaldo

### 8.1. Encabezado

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

El orden es obligatorio.

### 8.2. Versión del formato

Para HU-16:

```text
format_version = 1
```

Cada fila deberá contener el valor `1`.

Esta versión pertenece al formato del archivo, no a `versionName`.

### 8.3. Ejemplo

```csv
format_version,category,code,site,position,observations,created_at,updated_at
1,MR,1050,A1,Nivel 2,,1721304000000,1721304000000
1,MD,1050,B2,,"Caja exterior dañada",1721305000000,1721308000000
```

---

## 9. Definición de columnas

| Columna | Obligatoria | Regla |
|---|---:|---|
| `format_version` | Sí | Entero compatible; en HU-16 siempre `1` |
| `category` | Sí | Valor almacenado normalizado |
| `code` | Sí | Valor almacenado normalizado |
| `site` | Sí | Valor almacenado |
| `position` | No | Vacío representa ausencia |
| `observations` | No | Vacío representa ausencia |
| `created_at` | Sí | Epoch milliseconds positivo |
| `updated_at` | Sí | Epoch milliseconds positivo y no anterior a `created_at` |

No se incluirá:

```text
id
```

Al restaurar, Room generará identificadores nuevos.

---

## 10. Reglas de fechas

Las fechas se escribirán como:

```text
epoch milliseconds
```

Ejemplo:

```text
1721304000000
```

Motivos:

- representación independiente de zona horaria;
- no depende del locale;
- coincide con el modelo actual;
- evita pérdidas de precisión;
- simplifica validación y restauración.

Reglas:

- `created_at > 0`;
- `updated_at > 0`;
- `updated_at >= created_at`;
- no se convierten a textos visuales;
- no se reemplazan por el instante del respaldo.

Si un registro almacenado contiene fechas inválidas, el respaldo completo deberá fallar de forma controlada. No se generará silenciosamente un archivo que HU-17 no pueda restaurar.

---

## 11. Base vacía

A diferencia de Exportar CSV, una base vacía podrá generar una copia válida.

Contenido:

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

sin filas de datos.

Resultado:

```text
SUCCESS
backedUpCount = 0
```

Justificación:

- representa fielmente el estado actual;
- HU-17 podrá restaurarla para dejar Room vacío;
- el encabezado permite validar formato y versión.

La UI deberá informar claramente:

```text
Copia de seguridad creada sin registros.
```

No deberá mostrarse como error ni como `EMPTY_DATABASE`.

---

## 12. Nombre sugerido

Formato recomendado:

```text
almacentracker-backup-AAAA-MM-DD-HHmmss.csv
```

Ejemplo:

```text
almacentracker-backup-2026-07-19-153025.csv
```

Reglas:

- extensión `.csv`;
- nombre seguro;
- sin datos de mercancía;
- fecha y hora inyectables para pruebas;
- evitar colisiones;
- diferenciar visualmente respaldo y exportación.

---

## 13. Selector de destino

Se reutilizará Activity Result API:

```java
ActivityResultContracts.CreateDocument("text/csv")
```

La Activity deberá distinguir qué operación solicitó el destino.

No se recomienda utilizar un único booleano ambiguo. Se propone:

```text
PendingDocumentOperation
├── EXPORT
└── BACKUP
```

o lanzadores separados si mantienen el código más claro.

Al cancelar:

- no se ejecuta el caso de uso;
- no se muestra error;
- el estado vuelve a `IDLE`;
- no queda una operación pendiente.

---

## 14. Diseño técnico propuesto

### Puerto de entrada

```text
CreateWarehouseBackupUseCase
```

Firma orientativa:

```java
void createBackup(
        String destinationReference,
        CreateWarehouseBackupCallback callback
);
```

### Servicio

```text
CreateWarehouseBackupService
```

Responsabilidades:

1. validar la referencia;
2. recuperar todos los registros;
3. validar que los datos son respaldables;
4. solicitar la escritura;
5. devolver cantidad respaldada;
6. transformar errores.

### Puerto de salida

```text
WarehouseBackupCsvExporter
```

Responsabilidad:

- escribir el formato de respaldo;
- no consultar Room;
- no mostrar mensajes;
- no gestionar navegación.

### Adaptador Android

```text
AndroidWarehouseBackupDocumentExporter
```

Responsabilidad:

- usar `ContentResolver`;
- convertir la referencia opaca en `Uri`;
- abrir `OutputStream`;
- delegar serialización al codec;
- cerrar recursos;
- ejecutar en `fileExecutor`.

### Codec

```text
WarehouseBackupCsvCodec
```

Responsabilidad:

- escribir encabezado;
- escribir `format_version`;
- mapear campos;
- escapar valores;
- validar fechas;
- producir UTF-8.

### Mapper

```text
WarehouseBackupCsvMapper
```

Responsabilidad:

- convertir `WarehouseItem` a las ocho columnas;
- no acceder a Android ni Room.

---

## 15. Resultado de aplicación

Se añadirá:

```text
CreateWarehouseBackupResult
```

Estados recomendados:

```text
SUCCESS
INVALID_DESTINATION
READ_ERROR
INVALID_DATA
SERIALIZATION_ERROR
WRITE_ERROR
UNKNOWN_ERROR
```

Datos:

```text
status
backedUpCount
```

### SUCCESS

El archivo se escribió y cerró correctamente.

### INVALID_DESTINATION

La referencia es nula, vacía o no accesible.

### READ_ERROR

No se pudieron recuperar los registros desde Room.

### INVALID_DATA

Un registro contiene datos que no forman una copia restaurable, especialmente fechas inválidas.

### SERIALIZATION_ERROR

No se pudo generar el CSV de respaldo.

### WRITE_ERROR

No se pudo escribir o cerrar el documento.

---

## 16. Integración con la interfaz

`DataManagementActivity` mostrará:

```text
Crear copia de seguridad
Conserva todos los registros y sus fechas en un archivo CSV.
[Crear copia]
```

Durante la operación:

- Exportar deshabilitado;
- Compartir deshabilitado;
- Importar deshabilitado;
- Crear copia deshabilitado;
- progreso visible;
- texto contextual:

```text
Creando copia de seguridad…
```

`DataManagementUiState` podrá incorporar:

```text
SELECTING_BACKUP_DESTINATION
CREATING_BACKUP
```

El éxito se emitirá como evento de una sola consumición.

---

## 17. Flujo principal

1. El usuario abre Gestión de datos.
2. Pulsa Crear copia.
3. El ViewModel solicita el destino.
4. Android abre el selector.
5. El usuario elige nombre y ubicación.
6. La Activity entrega la referencia al ViewModel.
7. El ViewModel cambia a `CREATING_BACKUP`.
8. El caso de uso recupera todos los registros.
9. El servicio valida los datos.
10. El adaptador abre el destino.
11. El codec escribe encabezado y filas.
12. El stream se cierra correctamente.
13. El caso de uso devuelve `SUCCESS`.
14. El ViewModel emite el resultado una vez.
15. La Activity muestra la cantidad respaldada.
16. El estado vuelve a `IDLE`.

---

## 18. Flujos alternativos

### FA-01 — Cancelar selector

No se crea archivo y no se muestra error.

### FA-02 — Base vacía

Se genera un respaldo válido con solo el encabezado.

### FA-03 — Registro sin posición

Se escribe un campo vacío.

### FA-04 — Registro sin observaciones

Se escribe un campo vacío.

### FA-05 — Caracteres especiales

Comas, comillas y saltos de línea se escapan correctamente.

### FA-06 — Fórmula CSV

Se aplica la política reversible ya definida.

### FA-07 — Fecha inválida

No se escribe una copia incompleta y se devuelve `INVALID_DATA`.

### FA-08 — Error de Room

No se abre o no se confirma el archivo como copia válida.

### FA-09 — Error de escritura

Se devuelve `WRITE_ERROR`; no se informa éxito.

### FA-10 — Doble pulsación

Solo se inicia una operación.

### FA-11 — Rotación durante escritura

No se crea una segunda copia y el resultado llega a la Activity recreada.

### FA-12 — Modo avión

La copia se genera normalmente sobre un proveedor local accesible.

---

## 19. Criterios de aceptación

1. Gestión de datos muestra Crear copia de seguridad.
2. Pulsar la acción abre el selector de destino.
3. Se sugiere un nombre de respaldo diferenciado.
4. Cancelar no muestra error.
5. Se incluyen todos los registros de Room.
6. Búsqueda y filtros no limitan el respaldo.
7. El encabezado contiene ocho columnas.
8. Cada fila contiene `format_version = 1`.
9. Se incluyen `created_at` y `updated_at`.
10. No se incluye `id`.
11. Las fechas conservan exactamente sus valores.
12. `updated_at` no puede ser anterior a `created_at`.
13. Una fecha inválida impide generar una copia restaurable.
14. Los opcionales se escriben como campos vacíos.
15. Los caracteres especiales se escapan correctamente.
16. La protección frente a fórmulas es reversible.
17. La base vacía produce una copia válida.
18. El archivo se escribe en UTF-8.
19. El terminador de línea es uniforme.
20. La escritura no bloquea el hilo principal.
21. Solo una operación de datos puede estar activa.
22. La rotación no duplica la operación.
23. El éxito se muestra una sola vez.
24. Se informa la cantidad respaldada.
25. No se solicitan permisos generales.
26. Funciona sin conexión.
27. La Activity no consulta DAO ni serializa CSV.
28. Las pruebas, lint, compilación y CI finalizan correctamente.

---

## 20. Pruebas recomendadas

### WarehouseBackupCsvCodecTest

- encabezado exacto;
- formato versión 1;
- una fila completa;
- opcionales vacíos;
- comas;
- comillas;
- multilínea;
- Unicode;
- protección de fórmulas;
- fechas conservadas;
- fecha de creación inválida;
- fecha de actualización inválida;
- `updatedAt < createdAt`;
- lista vacía;
- UTF-8;
- CRLF;
- ausencia de id.

### CreateWarehouseBackupServiceTest

- referencia inválida;
- lectura correcta;
- base vacía;
- cantidad respaldada;
- error de lectura;
- datos inválidos;
- error de serialización;
- error de escritura;
- error desconocido.

### DataManagementViewModelTest

- solicitud de destino;
- cancelación;
- estado `CREATING_BACKUP`;
- bloqueo de acciones;
- éxito con registros;
- éxito con cero registros;
- errores;
- evento único;
- rotación.

### Pruebas instrumentadas

- escritura mediante URI;
- contenido exacto;
- documento vacío válido;
- cierre del stream;
- ausencia de permisos generales;
- funcionamiento en modo avión.

---

## 21. Tareas de implementación

1. Crear `feature/hu-16-crear-copia-seguridad-csv`.
2. Crear `CreateWarehouseBackupUseCase`.
3. Crear `CreateWarehouseBackupService`.
4. Crear callback y resultado.
5. Crear `WarehouseBackupCsvExporter`.
6. Crear `WarehouseBackupCsvMapper`.
7. Crear `WarehouseBackupCsvCodec`.
8. Crear `AndroidWarehouseBackupDocumentExporter`.
9. Añadir acción a `activity_data_management.xml`.
10. Ampliar `DataManagementUiState`.
11. Ampliar `DataManagementViewModel`.
12. Ampliar Factory y `AppContainer`.
13. Añadir recursos de texto y plurales.
14. Añadir pruebas unitarias.
15. Añadir pruebas instrumentadas.
16. Ejecutar:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

17. Verificar CI.
18. Fusionar en `develop`.
19. Verificar CI de `develop`.

---

## 22. Estrategia de commits orientativa

### Bloque 1 — Formato de respaldo

```text
git commit -m "feat: add versioned warehouse backup CSV format #18"
```

### Bloque 2 — Caso de uso

```text
git commit -m "feat: add warehouse backup creation use case #18"
```

### Bloque 3 — Integración Android

```text
git commit -m "feat: integrate CSV backup creation into data management #18"
```

### Bloque 4 — Pruebas

```text
git commit -m "test: cover warehouse CSV backup creation #18"
```

---

## 23. Definición de terminado

HU-16 estará terminada cuando:

- exista la acción Crear copia de seguridad;
- el usuario pueda elegir destino;
- el formato de respaldo sea independiente del CSV de intercambio;
- exista `format_version = 1`;
- se conserven todas las fechas;
- no se exporten ids internos;
- la base vacía genere una copia válida;
- los datos inválidos no produzcan una copia engañosa;
- el archivo use UTF-8 y escape CSV correcto;
- se informe la cantidad respaldada;
- la operación no se duplique;
- funcione sin conexión;
- no solicite permisos generales;
- la arquitectura mantenga separados aplicación, Room, archivos y UI;
- las pruebas unitarias e instrumentadas finalicen correctamente;
- lint y compilación finalicen correctamente;
- CI de la rama y de `develop` sean satisfactorias.

La siguiente historia será:

```text
HU-17 — Restaurar copia de seguridad CSV
```
