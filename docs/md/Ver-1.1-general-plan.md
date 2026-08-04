# AlmacenTracker — Plan de la versión 1.1

> Segunda entrega funcional: importación, exportación, copia de seguridad y compartición de mercancía mediante archivos CSV.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android prevista:** 1.1.0  
**Nombre de la versión:** Intercambio y copia de datos CSV  
**Estado inicial:** Planificada  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.0.0`

---

## 2. Contexto de partida

AlmacenTracker v1.0.0 ya ofrece una gestión local completa de mercancía mediante:

- listado;
- registro;
- detalle;
- búsqueda;
- filtros;
- edición;
- eliminación individual;
- eliminación múltiple;
- validación;
- normalización;
- control de duplicados;
- estados vacíos y sin resultados;
- persistencia Room;
- funcionamiento sin conexión.

La versión 1.1 no sustituirá este núcleo.

Su propósito será permitir que los datos locales puedan:

- salir de la aplicación en un formato interoperable;
- compartirse con otras aplicaciones;
- volver a incorporarse de forma controlada;
- utilizarse como copia de seguridad local;
- restaurarse sin comprometer la integridad de Room.

Room continuará siendo la única fuente de verdad de la aplicación.

---

## 3. Objetivo de la versión

Permitir que el usuario exporte, comparta, importe, respalde y restaure los registros de mercancía mediante archivos CSV, manteniendo:

- funcionamiento sin conexión;
- integridad de categoría + código;
- validación y normalización existentes;
- arquitectura hexagonal pragmática;
- control explícito de errores;
- compatibilidad con el almacenamiento moderno de Android;
- ausencia de permisos generales de almacenamiento;
- trazabilidad suficiente mediante resúmenes de operación.

---

## 4. Alcance incluido

La versión 1.1 incluirá:

- exportación de todos los registros a CSV;
- selección del destino mediante el selector de documentos de Android;
- nombre de archivo sugerido;
- codificación UTF-8;
- encabezados CSV definidos y versionados;
- escape correcto de comas, comillas y saltos de línea;
- campos opcionales vacíos representados de forma coherente;
- compartición mediante el selector nativo de Android;
- `FileProvider` para archivos temporales compartidos;
- importación desde archivos seleccionados por el usuario;
- lectura mediante `ContentResolver`;
- validación de encabezados;
- validación de campos obligatorios;
- normalización antes de persistir;
- resumen de filas válidas, duplicadas, inválidas y procesadas;
- protección de la restricción categoría + código;
- transacciones para operaciones masivas;
- creación de copia de seguridad CSV;
- restauración de copia de seguridad;
- validación completa antes de reemplazar datos;
- confirmación explícita antes de restaurar;
- conservación de los datos existentes si la restauración falla;
- mensajes de éxito, advertencia y error;
- pruebas unitarias;
- pruebas instrumentadas;
- CI;
- release `v1.1.0`.

---

## 5. Alcance excluido

La versión 1.1 no incluirá:

- archivos Excel `.xls` o `.xlsx`;
- PDF;
- JSON como formato público;
- conexión a Google Drive mediante API propia;
- sincronización automática;
- backend;
- Firebase;
- Supabase;
- autenticación;
- usuarios;
- roles;
- subida automática a la nube;
- importación desde una URL;
- envío por correo sin intervención del usuario;
- programación periódica de copias;
- cifrado de archivos;
- contraseña para copias de seguridad;
- historial de importaciones persistente;
- deshacer una restauración después de confirmarla;
- escaneo de códigos;
- categorías configurables;
- gestión de stock;
- cantidades;
- varios almacenes.

El usuario podrá elegir proveedores de documentos instalados en Android desde el selector del sistema, pero AlmacenTracker no integrará directamente servicios remotos.

---

## 6. Principios funcionales

### 6.1. Room continúa siendo la fuente de verdad

Los archivos CSV serán medios de entrada, salida y respaldo.

No sustituirán:

```text
Room / SQLite
```

El listado seguirá actualizándose a partir de Room.

### 6.2. Sin permisos generales de almacenamiento

La implementación deberá priorizar:

```text
Storage Access Framework
```

mediante:

- `ACTION_CREATE_DOCUMENT`;
- `ACTION_OPEN_DOCUMENT`;
- contratos equivalentes de Activity Result API.

No se solicitarán:

```text
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE
MANAGE_EXTERNAL_STORAGE
```

salvo que una necesidad real, compatible y documentada lo exija. Para el alcance previsto no son necesarios.

### 6.3. Funcionamiento offline

Exportar, importar, respaldar, restaurar y compartir el archivo generado deberán funcionar sin conexión.

El selector de Android podrá mostrar proveedores externos instalados, pero el núcleo de la aplicación no dependerá de ellos.

### 6.4. Datos ficticios y privacidad

No se utilizarán datos reales de empresas.

El usuario será responsable del destino que elija al compartir un archivo.

### 6.5. Operaciones no bloqueantes

Lectura, escritura, análisis de CSV y persistencia masiva no deberán bloquear el hilo principal.

---

## 7. Formatos CSV de la versión

La versión distinguirá dos usos:

```text
CSV de intercambio
CSV de copia de seguridad
```

No deberán confundirse.

---

## 8. CSV de intercambio

### 8.1. Finalidad

Permitir exportar información comprensible y volver a importar registros desde una fuente externa compatible.

### 8.2. Encabezados

Formato inicial propuesto:

```csv
category,code,site,position,observations
```

Campos:

| Columna | Obligatoria | Regla |
|---|---:|---|
| `category` | Sí | Se normaliza a mayúsculas |
| `code` | Sí | Se normaliza a mayúsculas |
| `site` | Sí | Se normaliza según las reglas existentes |
| `position` | No | Vacío representa ausencia |
| `observations` | No | Vacío representa ausencia |

### 8.3. Campos excluidos

El CSV de intercambio no incluirá como identidad importable:

- `id`;
- `createdAt`;
- `updatedAt`.

Al importar, Room generará el id y la aplicación generará las fechas correspondientes.

### 8.4. Codificación

```text
UTF-8
```

Se recomienda escribir BOM únicamente si las pruebas de compatibilidad con herramientas habituales justifican su uso.

La decisión deberá ser uniforme y quedar probada.

### 8.5. Reglas CSV

La implementación deberá soportar correctamente:

- comas dentro de un campo;
- comillas dobles;
- saltos de línea en observaciones;
- campos vacíos;
- última línea con o sin salto final.

No se deberá implementar el análisis separando cada línea mediante:

```java
split(",")
```

porque no respeta el formato CSV real.

---

## 9. CSV de copia de seguridad

### 9.1. Finalidad

Permitir reconstruir el estado de datos de AlmacenTracker con mayor fidelidad que una importación ordinaria.

### 9.2. Encabezados propuestos

```csv
format_version,category,code,site,position,observations,created_at,updated_at
```

### 9.3. Versión de formato

Cada fila incluirá:

```text
format_version = 1
```

Esto permitirá detectar archivos incompatibles en versiones futuras.

### 9.4. Identificador interno

No es obligatorio restaurar el `id` original porque v1.1 no contiene relaciones externas que dependan de él.

La restauración deberá:

- generar ids locales nuevos;
- conservar categoría, código, sitio y posición;
- conservar observaciones;
- conservar fechas válidas del respaldo;
- mantener la unicidad funcional.

Esta decisión evita conflictos de claves primarias y mantiene el respaldo portable.

---

## 10. Estrategia de importación

### 10.1. Importación ordinaria

La importación CSV tendrá comportamiento acumulativo:

```text
datos existentes
+
filas válidas nuevas
```

No eliminará registros actuales.

### 10.2. Duplicados

Una fila será duplicada cuando exista:

```text
categoría normalizada + código normalizado
```

Política inicial recomendada:

- no sobrescribir;
- no crear otra fila;
- omitir la fila;
- incluirla en el resumen.

No se recomienda preguntar por cada fila porque haría inviable una importación grande.

### 10.3. Filas inválidas

Una fila inválida:

- no se persistirá;
- no cancelará necesariamente todo el archivo;
- aparecerá en el resumen;
- deberá identificar número de fila y causa cuando sea posible.

### 10.4. Resultado parcial

La importación podrá finalizar con:

```text
SUCCESS
PARTIAL_SUCCESS
NO_VALID_ROWS
INVALID_FORMAT
READ_ERROR
PERSISTENCE_ERROR
```

### 10.5. Atomicidad

Se distinguen dos niveles:

- análisis completo del archivo antes de escribir;
- persistencia de las filas aceptadas dentro de una transacción.

Si ocurre un error de persistencia durante el lote, no deberá quedar una importación parcialmente escrita sin una decisión explícita.

Para v1.1 se recomienda que el lote aceptado sea atómico.

---

## 11. Estrategia de restauración

### 11.1. Diferencia respecto a importar

Importar:

```text
añade registros válidos
```

Restaurar:

```text
reemplaza el conjunto actual
```

### 11.2. Confirmación obligatoria

Antes de restaurar deberá mostrarse:

```text
La restauración reemplazará todos los registros actuales.
Esta acción no se puede deshacer desde la aplicación.
```

Acciones:

```text
Cancelar
Restaurar
```

### 11.3. Validación previa completa

Antes de eliminar datos existentes:

1. abrir el archivo;
2. validar encabezados;
3. validar `format_version`;
4. analizar todas las filas;
5. validar campos;
6. validar duplicados internos;
7. construir el conjunto restaurable.

Solo después podrá iniciarse la transacción de reemplazo.

### 11.4. Transacción

La restauración deberá ejecutar conceptualmente:

```text
BEGIN TRANSACTION
    eliminar datos actuales
    insertar respaldo validado
COMMIT
```

Si falla:

```text
ROLLBACK
```

Los datos previos deberán permanecer.

### 11.5. Archivo vacío

Una copia sin registros podrá ser válida si conserva encabezados correctos.

Restaurarla dejará la base vacía únicamente después de confirmación explícita.

---

## 12. Arquitectura aplicada

La versión continuará con arquitectura hexagonal pragmática.

```text
adapter.in
    ↓
application.port.in
    ↓
application.service
    ↓
application.port.out
    ↑
adapter.out
```

Se añadirá un adaptador de archivos claramente separado de Room.

Estructura orientativa:

```text
<package-root>/
├── domain/
│   └── model/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ExportWarehouseItemsUseCase.java
│   │   │   ├── ImportWarehouseItemsUseCase.java
│   │   │   ├── CreateWarehouseBackupUseCase.java
│   │   │   └── RestoreWarehouseBackupUseCase.java
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       └── WarehouseItemCsvGateway.java
│   ├── result/
│   └── service/
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       ├── state/
│   │       └── viewmodel/
│   └── out/
│       ├── persistence/
│       │   └── room/
│       └── file/
│           └── csv/
│               ├── CsvWarehouseItemReader.java
│               ├── CsvWarehouseItemWriter.java
│               ├── CsvWarehouseItemMapper.java
│               └── CsvFormatValidator.java
└── configuration/
```

Los nombres definitivos deberán reflejar responsabilidades reales y evitar clases vacías.

---

## 13. Puertos y responsabilidades

### 13.1. ExportWarehouseItemsUseCase

Responsabilidad:

- obtener los registros;
- solicitar serialización;
- escribir en el destino proporcionado;
- devolver cantidad exportada;
- controlar errores.

### 13.2. ImportWarehouseItemsUseCase

Responsabilidad:

- leer CSV;
- validar formato;
- normalizar;
- identificar duplicados;
- persistir lote;
- devolver resumen.

### 13.3. CreateWarehouseBackupUseCase

Responsabilidad:

- recuperar todos los registros;
- generar CSV de respaldo versionado;
- conservar fechas;
- devolver cantidad respaldada.

### 13.4. RestoreWarehouseBackupUseCase

Responsabilidad:

- validar respaldo completo;
- pedir al repositorio un reemplazo transaccional;
- devolver cantidad restaurada;
- no gestionar el diálogo de confirmación.

### 13.5. WarehouseItemCsvGateway

Responsabilidad:

- leer y escribir el formato CSV;
- escapar valores;
- validar encabezados;
- no consultar Room;
- no navegar;
- no mostrar mensajes.

### 13.6. WarehouseItemRepository

Podrá ampliarse con operaciones masivas reales:

```text
insertAll
replaceAll
```

La forma exacta deberá conservar:

- transacciones;
- restricción compuesta;
- ejecución fuera del hilo principal;
- resultados comprensibles.

---

## 14. Integración con Android

### 14.1. Exportar

Se utilizará el selector de creación de documento:

```text
text/csv
```

Nombre sugerido:

```text
almacentracker-export-AAAA-MM-DD.csv
```

### 14.2. Importar

Se utilizará un selector de apertura:

```text
text/csv
text/comma-separated-values
application/csv
```

La implementación deberá tolerar que algunos proveedores entreguen tipos MIME genéricos si el archivo seleccionado es accesible.

### 14.3. Compartir

Se utilizará:

```text
ACTION_SEND
```

con:

```text
FileProvider
FLAG_GRANT_READ_URI_PERMISSION
```

No se compartirán rutas `file://`.

### 14.4. Copia de seguridad

El usuario elegirá el destino mediante el selector del sistema.

### 14.5. Restauración

El usuario elegirá el archivo.

La Activity gestionará:

- selector;
- URI;
- confirmación;
- renderizado.

No deberá contener análisis CSV ni reglas de negocio.

---

## 15. Pantallas y navegación

La versión podrá incorporar una pantalla o menú de gestión de datos.

Opción recomendada:

```text
MainActivity
    ↓
DataManagementActivity
```

Acciones:

```text
Exportar CSV
Compartir CSV
Importar CSV
Crear copia de seguridad
Restaurar copia de seguridad
```

La separación evita sobrecargar el listado principal.

También será válida una sección Material equivalente si mantiene claridad y accesibilidad.

---

## 16. Estados de interfaz

Operaciones de archivo deberán distinguir:

```text
IDLE
SELECTING_FILE
READING
VALIDATING
WRITING
IMPORTING
RESTORING
SUCCESS
PARTIAL_SUCCESS
ERROR
```

No será obligatorio utilizar todos en un único enum.

El diseño deberá impedir:

- doble importación;
- doble restauración;
- navegación contradictoria;
- pérdida del resultado tras rotación;
- repetición de mensajes de éxito.

---

## 17. Historias de usuario de la versión 1.1

La numeración continúa desde v1.0.

---

### HU-12 — Exportar mercancía a CSV

**Historia**

Como usuario, quiero exportar la mercancía a un archivo CSV para consultar o utilizar los datos fuera de la aplicación.

**Alcance principal**

- acceso desde gestión de datos;
- exportar todos los registros;
- elegir destino;
- encabezados definidos;
- UTF-8;
- escape CSV correcto;
- mensaje con cantidad exportada;
- controlar base vacía;
- funcionamiento sin conexión.

**Prioridad:** Alta

**Dependencia:** v1.0.0 estable.

---

### HU-13 — Compartir exportación CSV

**Historia**

Como usuario, quiero compartir un archivo CSV generado por AlmacenTracker para enviarlo mediante otra aplicación instalada.

**Alcance principal**

- generar archivo temporal;
- usar `FileProvider`;
- abrir selector de compartir;
- conceder acceso temporal de lectura;
- no exponer rutas internas;
- limpiar archivos temporales de forma controlada;
- controlar ausencia de aplicación receptora.

**Prioridad:** Media

**Dependencia:** HU-12.

---

### HU-14 — Importar mercancía desde CSV

**Historia**

Como usuario, quiero seleccionar un archivo CSV e importar sus registros para evitar registrarlos manualmente uno por uno.

**Alcance principal**

- seleccionar archivo;
- validar encabezados;
- leer UTF-8;
- analizar CSV correctamente;
- normalizar datos;
- insertar registros válidos;
- conservar registros existentes;
- omitir duplicados;
- mostrar resumen.

**Prioridad:** Alta

**Dependencia:** HU-12 por definición compartida del formato.

---

### HU-15 — Validar e informar incidencias de importación

**Historia**

Como usuario, quiero conocer qué filas no pudieron importarse y por qué para corregir el archivo sin perder las filas válidas.

**Alcance principal**

- identificar número de fila;
- errores de campos obligatorios;
- encabezado inválido;
- columnas inesperadas o ausentes;
- duplicados del archivo;
- duplicados de Room;
- resumen de importadas, omitidas e inválidas;
- resultado parcial;
- límite razonable de detalles mostrados;
- pruebas con caracteres especiales.

**Prioridad:** Alta

**Dependencia:** HU-14.

Esta historia consolidará la robustez de importación; no deberá crear un segundo importador paralelo.

---

### HU-16 — Crear copia de seguridad CSV

**Historia**

Como usuario, quiero crear una copia de seguridad completa para conservar la información y sus fechas fuera de la aplicación.

**Alcance principal**

- formato de respaldo versionado;
- exportar todos los registros;
- conservar fechas;
- elegir destino;
- nombre sugerido;
- verificar escritura completa;
- mensaje de cantidad respaldada;
- base vacía permitida;
- funcionamiento sin conexión.

**Prioridad:** Alta

**Dependencia:** HU-12 y formato CSV estable.

---

### HU-17 — Restaurar copia de seguridad CSV

**Historia**

Como usuario, quiero restaurar una copia de seguridad para recuperar el conjunto guardado anteriormente.

**Alcance principal**

- seleccionar respaldo;
- validar versión de formato;
- analizar todo antes de modificar Room;
- detectar duplicados internos;
- mostrar confirmación destructiva;
- reemplazar datos en transacción;
- conservar datos previos ante fallo;
- actualizar listado y filtros;
- controlar respaldo vacío;
- mostrar cantidad restaurada.

**Prioridad:** Alta

**Dependencia:** HU-16.

---

## 18. Orden de implementación

Orden recomendado:

```text
HU-12 Exportar CSV
        ↓
HU-13 Compartir CSV
        ↓
HU-14 Importar CSV
        ↓
HU-15 Validación de importación
        ↓
HU-16 Crear copia de seguridad
        ↓
HU-17 Restaurar copia de seguridad
```

Justificación:

- primero se define y prueba la escritura;
- después se reutiliza para compartir;
- posteriormente se implementa lectura;
- se consolida el tratamiento de incidencias;
- por último se crea el formato de respaldo y su restauración destructiva.

---

## 19. Backlog técnico de la versión

1. Actualizar `versionCode` a `2`.
2. Actualizar `versionName` a `1.1.0`.
3. Actualizar README de `develop`.
4. Crear menú o pantalla de gestión de datos.
5. Configurar Activity Result API.
6. Definir formato CSV de intercambio.
7. Definir formato CSV de respaldo.
8. Elegir o implementar parser CSV probado.
9. Crear adaptador `adapter.out.file.csv`.
10. Crear puertos de aplicación.
11. Implementar exportación.
12. Implementar FileProvider.
13. Implementar compartición.
14. Implementar importación.
15. Implementar validación y resumen.
16. Añadir inserción masiva transaccional.
17. Implementar copia de seguridad.
18. Añadir reemplazo transaccional.
19. Implementar restauración.
20. Añadir recursos y mensajes.
21. Añadir pruebas unitarias.
22. Añadir pruebas instrumentadas.
23. Probar archivos grandes razonables.
24. Probar caracteres especiales.
25. Probar modo avión.
26. Ejecutar CI por historia.
27. Actualizar README al cerrar la versión.
28. Crear `release/v1.1.0`.
29. Estabilizar sin añadir funciones nuevas.
30. Publicar `master`.
31. Crear tag `v1.1.0`.
32. Crear GitHub Release.

---

## 20. Estrategia de pruebas

### 20.1. Pruebas unitarias

- encabezados válidos;
- encabezados ausentes;
- orden de columnas;
- comas;
- comillas;
- saltos de línea;
- caracteres Unicode;
- campos vacíos;
- categoría obligatoria;
- código obligatorio;
- sitio obligatorio;
- normalización;
- duplicados internos;
- resumen de importación;
- formato de respaldo;
- versión incompatible;
- resultados totales y parciales.

### 20.2. Pruebas de Room

- insertar lote;
- lote atómico;
- permitir mismo código en categoría distinta;
- rechazar duplicado compuesto;
- reemplazar todos en transacción;
- rollback ante error;
- restaurar base vacía;
- conservar fechas;
- actualizar opciones de filtro;
- actualizar observadores.

### 20.3. Pruebas instrumentadas

- crear documento;
- escribir mediante URI;
- abrir documento;
- leer mediante URI;
- compartir content URI;
- conceder permiso temporal;
- rotación durante operación;
- reapertura después de importación;
- persistencia después de restauración.

### 20.4. Pruebas manuales

- exportar base vacía;
- exportar con registros;
- abrir CSV en una aplicación compatible;
- compartir;
- cancelar selector;
- importar archivo válido;
- importar parcialmente válido;
- importar duplicados;
- importar caracteres especiales;
- archivo inaccesible;
- archivo muy grande dentro del límite acordado;
- crear respaldo;
- cancelar restauración;
- restaurar;
- reiniciar aplicación;
- funcionamiento sin conexión.

---

## 21. Criterios de calidad

La versión deberá mantener:

- arquitectura hexagonal pragmática;
- dominio sin Android ni Room;
- UI sin acceso directo al DAO;
- adaptador CSV separado de Room;
- operaciones masivas transaccionales;
- errores técnicos transformados;
- mensajes comprensibles;
- ausencia de permisos de almacenamiento innecesarios;
- archivos compartidos mediante `content://`;
- trabajo pesado fuera del hilo principal;
- datos ficticios;
- pruebas y CI por historia.

---

## 22. Riesgos de la versión

### Riesgo 1 — Parser CSV incorrecto

Usar `split(",")` corrompería observaciones con comas o saltos.

**Mitigación:** parser real y pruebas de escape.

### Riesgo 2 — Pérdida de datos al restaurar

Eliminar antes de validar podría dejar la base vacía.

**Mitigación:** validación completa y transacción con rollback.

### Riesgo 3 — Duplicados

Un archivo puede contener duplicados internos o conflictos con Room.

**Mitigación:** normalización, comprobación previa e índice único final.

### Riesgo 4 — Permisos de almacenamiento

Solicitar permisos generales empeoraría compatibilidad y privacidad.

**Mitigación:** Storage Access Framework y FileProvider.

### Riesgo 5 — Archivos grandes

Leer todo sin límites puede consumir memoria.

**Mitigación:** procesamiento controlado, límites documentados y pruebas de volumen razonable.

### Riesgo 6 — Fórmulas en CSV

Valores que comienzan con `=`, `+`, `-` o `@` pueden interpretarse como fórmulas en hojas de cálculo.

**Mitigación:** definir una estrategia de neutralización al exportar campos libres sin alterar la semántica al reimportar.

### Riesgo 7 — Compatibilidad futura

Cambiar columnas sin versión rompería respaldos.

**Mitigación:** formato de backup versionado.

### Riesgo 8 — Confundir importación con restauración

Una importación acumulativa no debe borrar datos.

**Mitigación:** flujos, mensajes y casos de uso separados.

---

## 23. Seguridad y privacidad

- no se solicitará acceso completo al almacenamiento;
- se utilizarán URIs concedidas por el sistema;
- los archivos temporales compartidos no serán públicos permanentemente;
- no se registrará el contenido completo de archivos en logs;
- no se expondrán rutas privadas;
- se validará tipo, estructura y contenido;
- no se ejecutará contenido del CSV;
- no se enviará información automáticamente;
- no se dependerá de Internet.

---

## 24. Integración continua

La CI continuará ejecutando como mínimo:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Las pruebas instrumentadas se ejecutarán con emulador o dispositivo disponible:

```text
./gradlew connectedDebugAndroidTest
```

La incorporación de una librería CSV deberá:

- estar justificada;
- fijar versión;
- no introducir red en tiempo de ejecución;
- ser compatible con Android;
- revisarse en CI.

---

## 25. Estrategia de ramas

Cada historia utilizará:

```text
feature/hu-XX-descripcion
```

Flujo individual:

```text
develop
    ↓
feature/hu-XX-descripcion
    ↓ CI correcta
merge local --no-ff
    ↓
develop
```

Al cerrar la versión:

```text
develop
    ↓
release/v1.1.0
    ↓
master
    ↓
tag v1.1.0
```

La rama release será temporal.

Las correcciones funcionales realizadas exclusivamente en release deberán reproducirse o reintegrarse en `develop` antes de eliminarla.

---

## 26. Versionado Android

Durante el desarrollo de v1.1:

```groovy
versionCode 2
versionName "1.1.0"
```

En la release v1.1.0 se mantendrán esos valores salvo que exista una compilación publicada anterior con el mismo `versionCode`.

Reglas:

- `versionCode` siempre aumenta entre publicaciones instalables;
- `versionName` utiliza `MAJOR.MINOR.PATCH`;
- la rama identifica el estado de desarrollo;
- no se utilizará una cantidad distinta de segmentos para distinguir `develop` de `master`.

---

## 27. Definición de terminado de la versión 1.1

La versión 1.1 estará terminada cuando:

- v1.0 continúe funcionando sin regresiones;
- pueda exportarse un CSV válido;
- el usuario pueda elegir destino;
- el CSV soporte caracteres especiales;
- pueda compartirse mediante `content://`;
- pueda importarse un CSV válido;
- las filas inválidas se controlen;
- los duplicados se controlen;
- exista un resumen de importación;
- pueda crearse un respaldo versionado;
- pueda restaurarse un respaldo;
- la restauración requiera confirmación;
- se valide todo antes de reemplazar datos;
- una restauración fallida conserve la base anterior;
- Room continúe siendo fuente de verdad;
- no se soliciten permisos generales de almacenamiento;
- todas las operaciones funcionen sin conexión;
- las operaciones pesadas no bloqueen UI;
- las pruebas unitarias finalicen correctamente;
- las pruebas de Room finalicen correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- CI de todas las historias sea satisfactoria;
- README refleje las funciones terminadas;
- se cree `release/v1.1.0`;
- se valide la release;
- `master` contenga la versión estable;
- exista el tag anotado `v1.1.0`;
- se publique una GitHub Release.

---

## 28. Resultado esperado

Al cerrar v1.1.0, AlmacenTracker permitirá mover y proteger sus datos sin introducir sincronización remota:

```text
Room
  ├── exportar CSV
  ├── compartir CSV
  ├── importar CSV
  ├── crear respaldo
  └── restaurar respaldo
```

La aplicación continuará siendo local y operativa sin Internet.

La siguiente versión prevista será:

```text
v1.2.0 — Escaneo de códigos de barras y códigos QR
```

---

## 29. Commit documental recomendado

Este documento se mantendrá en “Fuentes” y no se añadirá al repositorio público debido a la política `.gitignore`.

```text
Sin commit en Git: guardar Ver-1.1-general-plan.md en Fuentes.
```
