# HU-12 — Exportar mercancía a CSV

> Primera historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-12  
**Nombre:** Exportar mercancía a CSV  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-12-exportar-mercancia-csv`  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.0.0`

---

## 2. Historia de usuario

Como usuario,  
quiero exportar toda la mercancía registrada a un archivo CSV,  
para consultar o utilizar los datos fuera de AlmacenTracker.

---

## 3. Objetivo

Implementar el primer flujo de intercambio de datos de AlmacenTracker v1.1:

```text
usuario abre Gestión de datos
        ↓
pulsa Exportar CSV
        ↓
Android muestra el selector de destino
        ↓
el usuario elige nombre y ubicación
        ↓
la aplicación recupera todos los registros desde Room
        ↓
serializa el CSV
        ↓
escribe mediante ContentResolver
        ↓
muestra la cantidad exportada
```

La exportación deberá respetar la arquitectura hexagonal pragmática del proyecto:

```text
DataManagementActivity
        ↓
DataManagementViewModel
        ↓
ExportWarehouseItemsUseCase
        ↓
ExportWarehouseItemsService
        ├── WarehouseItemRepository
        └── WarehouseItemCsvExporter
                ↓
        RoomWarehouseItemRepository
        AndroidCsvDocumentExporter
                ↓
        WarehouseItemDao / ContentResolver
```

Room continuará siendo la única fuente de verdad.

El archivo CSV será una representación de intercambio. No sustituirá la base de datos y no contendrá identificadores internos ni fechas.

---

## 4. Documentos de referencia

La HU-12 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.1-general-plan.md`;
- el estado real del ZIP `AlmacenTracker-ver1-0-0.zip`;
- las reglas funcionales cerradas en v1.0;
- la política de arquitectura hexagonal pragmática;
- la política de documentación interna en “Fuentes”;
- el flujo de ramas desde `develop`.

El plan general establece que v1.1 incorpora:

```text
importación
exportación
copia de seguridad
compartición CSV
```

El plan de v1.1 define HU-12 como la primera historia porque fija el formato de intercambio que reutilizarán las historias posteriores.

---

## 5. Estado real del proyecto antes de HU-12

El ZIP actualizado confirma que el proyecto se encuentra en:

```groovy
versionCode 2
versionName "1.1.0"
```

La aplicación ya dispone de:

- Android Java;
- Android Views;
- View Binding;
- Material Components;
- ViewModel;
- LiveData;
- Room;
- SQLite;
- arquitectura hexagonal pragmática;
- `WarehouseItem`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao`;
- `WarehouseItemPersistenceMapper`;
- `AppContainer`;
- executor de base de datos;
- listado observable;
- búsqueda;
- filtros;
- creación;
- detalle;
- edición;
- eliminación individual;
- eliminación múltiple;
- control de duplicados;
- estados vacíos y sin resultados;
- funcionamiento completamente offline;
- pruebas unitarias e instrumentadas;
- CI y CD.

El repositorio actual ofrece:

```text
observeAll
search
filter
observeFilterOptions
observeById
findById
existsByCategoryAndCode
existsByCategoryAndCodeExcludingId
insert
update
deleteById
deleteByIds
```

El DAO actual ofrece una consulta observable:

```java
LiveData<List<WarehouseItemEntity>> observeAll();
```

Todavía no existe una consulta puntual para obtener una instantánea completa destinada a exportación.

El proyecto todavía no dispone de:

- `DataManagementActivity`;
- pantalla de gestión de datos;
- `DataManagementViewModel`;
- `ExportWarehouseItemsUseCase`;
- `ExportWarehouseItemsService`;
- `ExportWarehouseItemsResult`;
- puerto de salida para exportar CSV;
- adaptador `adapter.out.file.csv`;
- selector de creación de documentos;
- escritura con `ContentResolver`;
- formato CSV implementado;
- pruebas de serialización CSV;
- mensajes de exportación.

El Manifest no solicita permisos generales de almacenamiento y deberá continuar así.

---

## 6. Alcance incluido

HU-12 incluye:

- crear acceso a Gestión de datos desde `MainActivity`;
- crear `DataManagementActivity`;
- mostrar inicialmente la acción Exportar CSV;
- abrir el selector de creación de documentos de Android;
- utilizar MIME `text/csv`;
- sugerir un nombre de archivo;
- permitir que el usuario elija ubicación y nombre;
- cancelar el selector sin error;
- recuperar todos los registros de Room mediante una consulta puntual;
- mantener orden estable por categoría y código;
- exportar todos los registros, independientemente de búsqueda o filtros activos;
- definir encabezados CSV;
- usar codificación UTF-8;
- representar campos opcionales vacíos;
- escapar comas;
- escapar comillas;
- escapar saltos de línea;
- escribir terminadores de línea coherentes;
- controlar base vacía;
- controlar destino inválido o inaccesible;
- controlar fallo al consultar Room;
- controlar fallo al serializar;
- controlar fallo al escribir;
- impedir exportaciones simultáneas;
- conservar estado ante rotación;
- evitar repetir mensajes de éxito;
- mostrar cantidad exportada;
- funcionar sin conexión;
- no solicitar permisos de almacenamiento;
- pruebas unitarias;
- pruebas del adaptador CSV;
- pruebas de repositorio necesarias;
- pruebas del ViewModel;
- pruebas instrumentadas de escritura mediante URI;
- CI.

---

## 7. Alcance excluido

HU-12 no incluye:

- compartir el CSV;
- `FileProvider`;
- archivos temporales para compartir;
- importar CSV;
- validar archivos de entrada;
- mostrar incidencias por fila;
- copia de seguridad versionada;
- restauración;
- incluir `id`;
- incluir `createdAt`;
- incluir `updatedAt`;
- seleccionar solo algunos registros;
- exportar únicamente resultados filtrados;
- exportar únicamente registros seleccionados;
- exportar Excel `.xls`;
- exportar Excel `.xlsx`;
- exportar PDF;
- exportar JSON;
- enviar por correo automáticamente;
- subir a Google Drive mediante API;
- sincronizar con backend;
- solicitar acceso completo al almacenamiento;
- guardar automáticamente sin intervención del usuario;
- programar exportaciones;
- cifrar el archivo;
- proteger el CSV con contraseña.

Compartir pertenece a HU-13.

Importar pertenece a HU-14 y HU-15.

Copia de seguridad y restauración pertenecen a HU-16 y HU-17.

---

## 8. Precondiciones

Antes de comenzar HU-12 deberán cumplirse:

- release `v1.0.0` publicada;
- `develop` en `versionName "1.1.0"`;
- `versionCode 2`;
- README de `develop` actualizado;
- plan de v1.1 disponible en “Fuentes”;
- CI de `develop` satisfactoria;
- Room estable;
- CRUD v1.0 operativo;
- funcionamiento offline verificado;
- no existencia de permisos generales de almacenamiento;
- `AppContainer` como composición explícita;
- arquitectura actual sin dependencias remotas.

---

## 9. Decisiones funcionales principales

### 9.1. Se exportan todos los registros

La acción Exportar CSV siempre utilizará:

```text
todos los registros de Room
```

No dependerá de:

- búsqueda activa;
- filtros activos;
- selección múltiple;
- contenido visible en el RecyclerView.

### 9.2. El usuario elige el destino

Se utilizará el selector de documentos del sistema.

La aplicación no decidirá una ruta fija.

### 9.3. No se exportan datos internos

El CSV no incluirá:

```text
id
createdAt
updatedAt
```

### 9.4. Base vacía

Si Room no contiene registros:

- no se informará una exportación correcta;
- se mostrará un mensaje comprensible;
- el caso de uso devolverá `EMPTY_DATABASE`.

Mensaje orientativo:

```text
No hay mercancía para exportar.
```

### 9.5. Sin permisos generales

No se solicitarán:

```text
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE
MANAGE_EXTERNAL_STORAGE
```

---

## 10. Pantalla de Gestión de datos

### 10.1. Navegación

Flujo recomendado:

```text
MainActivity
        ↓ acción Gestión de datos
DataManagementActivity
```

La acción podrá estar en Toolbar o menú overflow.

### 10.2. Contenido inicial

En HU-12 la pantalla mostrará:

```text
Gestión de datos

Exportar CSV
Guarda todos los registros en un archivo CSV.

[Exportar]
```

La opción más segura es mostrar únicamente Exportar CSV en HU-12.

### 10.3. Activity separada

`DataManagementActivity` permitirá incorporar posteriormente:

- Compartir CSV;
- Importar CSV;
- Crear copia de seguridad;
- Restaurar copia de seguridad.

---

## 11. Selector de documentos

### 11.1. Contrato recomendado

Se recomienda Activity Result API:

```java
ActivityResultContracts.CreateDocument
```

con tipo:

```text
text/csv
```

### 11.2. Nombre sugerido

Formato recomendado:

```text
almacentracker-export-AAAA-MM-DD.csv
```

Ejemplo:

```text
almacentracker-export-2026-07-18.csv
```

### 11.3. Cancelación

Si el selector devuelve `null`:

- no se inicia exportación;
- no se muestra error;
- el estado vuelve a `IDLE`;
- el usuario puede intentarlo de nuevo.

### 11.4. URI opaca

La capa de aplicación no deberá importar:

```java
android.net.Uri
android.content.ContentResolver
```

Una estrategia pragmática válida es entregar al caso de uso una referencia opaca:

```text
String destinationReference
```

El adaptador Android será responsable de convertirla nuevamente en `Uri`.

---

## 12. Formato CSV de intercambio

### 12.1. Encabezado oficial

```csv
category,code,site,position,observations
```

El orden es obligatorio.

### 12.2. Ejemplo

```csv
category,code,site,position,observations
MR,1050,A1,Nivel 2,
MD,1050,B2,,"Caja exterior dañada"
CA,2000,C1,Nivel 1,"Contiene comas, comillas ""dobles"" y texto"
```

### 12.3. Columnas

| Posición | Columna | Obligatoria | Origen |
|---:|---|---:|---|
| 1 | `category` | Sí | `WarehouseItem.category` |
| 2 | `code` | Sí | `WarehouseItem.code` |
| 3 | `site` | Sí | `WarehouseItem.site` |
| 4 | `position` | No | `WarehouseItem.position` |
| 5 | `observations` | No | `WarehouseItem.observations` |

### 12.4. Campos opcionales

Valores `null` o vacíos se exportarán como campo vacío.

No se escribirán:

```text
null
NULL
N/A
-
```

### 12.5. Orden de filas

Las filas conservarán el orden:

1. categoría ascendente sin distinguir mayúsculas;
2. código ascendente sin distinguir mayúsculas.

---

## 13. Codificación y terminadores

### 13.1. Codificación

```text
UTF-8
```

### 13.2. BOM

HU-12 no añadirá BOM por defecto.

La decisión podrá revisarse si pruebas reales demuestran una incompatibilidad crítica.

### 13.3. Terminadores de línea

Se recomienda:

```text
CRLF: \r\n
```

La implementación y las pruebas deberán ser uniformes.

---

## 14. Reglas de escape CSV

No se permitirá construir filas mediante `String.join()` sin escapar cada campo.

### 14.1. Regla general

Un campo deberá encerrarse entre comillas dobles si contiene:

- coma;
- comilla doble;
- retorno de carro;
- salto de línea.

### 14.2. Comillas internas

Entrada:

```text
Caja "A"
```

Salida:

```csv
"Caja ""A"""
```

### 14.3. Comas

Entrada:

```text
Frágil, revisar
```

Salida:

```csv
"Frágil, revisar"
```

### 14.4. Saltos de línea

Una observación multilínea deberá permanecer dentro de un único campo entrecomillado.

### 14.5. Campos simples

Los campos sin caracteres especiales podrán escribirse sin comillas.

También será válida la estrategia de entrecomillar todos los campos si se aplica uniformemente y queda probada.

---

## 15. Riesgo de fórmulas CSV

Herramientas de hoja de cálculo pueden interpretar como fórmula valores que comienzan con:

```text
=
+
-
@
```

Antes de cerrar HU-12 deberá definirse una política compatible con la futura importación.

Estrategia recomendada:

- detectar campos que comienzan por un prefijo peligroso;
- anteponer un apóstrofo de protección;
- proteger también un apóstrofo original para evitar ambigüedad;
- documentar que HU-14 deberá revertir la transformación de forma controlada.

Ejemplo:

```text
valor original: =SUM(A1:A2)
valor CSV:      '=SUM(A1:A2)

valor original: '=SUM(A1:A2)
valor CSV:      ''=SUM(A1:A2)
```

La regla deberá centralizarse en el codec CSV, no en la Activity.

---

## 16. Estados de exportación

Modelo orientativo:

```text
DataManagementUiState
├── IDLE
├── SELECTING_DESTINATION
├── EXPORTING
├── EMPTY_DATABASE
└── ERROR
```

El éxito se recomienda como evento de una sola consumición.

### 16.1. IDLE

- botón Exportar habilitado;
- sin progreso;
- sin error activo.

### 16.2. SELECTING_DESTINATION

- se ha solicitado el selector;
- no se lanza otro selector;
- no se considera una exportación en curso todavía.

### 16.3. EXPORTING

- botón deshabilitado;
- indicador de progreso;
- doble exportación bloqueada.

### 16.4. EMPTY_DATABASE

Mensaje:

```text
No hay mercancía para exportar.
```

### 16.5. ERROR

Mensaje general:

```text
No se pudo exportar la mercancía.
```

### 16.6. SUCCESS

Evento único con:

```text
exportedCount
```

---

## 17. Resultado de aplicación

Se añadirá:

```text
ExportWarehouseItemsResult
```

Estados recomendados:

```text
SUCCESS
EMPTY_DATABASE
INVALID_DESTINATION
READ_ERROR
SERIALIZATION_ERROR
WRITE_ERROR
UNKNOWN_ERROR
```

Datos:

```text
status
exportedCount
```

---

## 18. Flujo principal

1. El usuario abre `MainActivity`.
2. Pulsa Gestión de datos.
3. Se abre `DataManagementActivity`.
4. Pulsa Exportar.
5. Android abre el selector de creación.
6. El usuario elige destino.
7. La Activity entrega la referencia del destino al ViewModel.
8. El ViewModel bloquea nuevas exportaciones.
9. Invoca `ExportWarehouseItemsUseCase`.
10. `ExportWarehouseItemsService` solicita todos los registros.
11. `RoomWarehouseItemRepository` ejecuta la consulta puntual.
12. El DAO devuelve las entidades ordenadas.
13. El mapper las convierte a dominio.
14. El servicio solicita exportación al puerto CSV.
15. `AndroidCsvDocumentExporter` abre un `OutputStream`.
16. El codec escribe encabezado y filas UTF-8.
17. El stream se cierra mediante try-with-resources.
18. El adaptador devuelve éxito.
19. El servicio devuelve cantidad exportada.
20. El ViewModel emite evento `SUCCESS`.
21. La Activity muestra confirmación.
22. El botón vuelve a habilitarse.

---

## 19. Flujos alternativos

### FA-01 — Cancelar selector

No se ejecuta el caso de uso y no aparece error.

### FA-02 — Base vacía

El servicio devuelve `EMPTY_DATABASE` y no informa éxito.

### FA-03 — Campo con coma

Se exporta entre comillas.

### FA-04 — Campo con comillas

Se duplican las comillas y se encierra el campo.

### FA-05 — Observaciones multilínea

Se conservan dentro de un campo entrecomillado.

### FA-06 — Caracteres Unicode

Tildes, ñ y otros caracteres se conservan en UTF-8.

### FA-07 — Proveedor inaccesible

Se devuelve `WRITE_ERROR` y la pantalla permanece abierta.

### FA-08 — Error de Room

Se devuelve `READ_ERROR` y no se intenta escribir.

### FA-09 — Error de serialización

Se devuelve `SERIALIZATION_ERROR`.

### FA-10 — Escritura parcial

Se cierra el stream, no se informa éxito y el archivo parcial no se considera válido.

### FA-11 — Doble pulsación

No se abre otro selector ni se inicia otra escritura.

### FA-12 — Rotación durante selector

No se relanza automáticamente el selector.

### FA-13 — Rotación durante exportación

El ViewModel conserva `EXPORTING` y no inicia otra operación.

### FA-14 — Evento después de recreación

El mensaje de éxito o error se consume una sola vez.

### FA-15 — Exportación offline

El proceso funciona en modo avión.

---

## 20. Criterios de aceptación

### CA-01 — Acceso a Gestión de datos

**Dado** que el usuario está en el listado,  
**cuando** pulsa la acción correspondiente,  
**entonces** se abre Gestión de datos.

### CA-02 — Acción Exportar

**Dado** que el usuario está en Gestión de datos,  
**cuando** visualiza la pantalla,  
**entonces** dispone de Exportar CSV.

### CA-03 — Selector del sistema

**Dado** que el usuario pulsa Exportar,  
**cuando** inicia el flujo,  
**entonces** Android permite elegir nombre y ubicación.

### CA-04 — Nombre sugerido

**Dado** que se abre el selector,  
**cuando** aparece,  
**entonces** propone un nombre `.csv` comprensible.

### CA-05 — Cancelación

**Dado** que el selector está abierto,  
**cuando** el usuario cancela,  
**entonces** no aparece error ni se inicia exportación.

### CA-06 — Todos los registros

**Dado** que existen registros en Room,  
**cuando** se exporta,  
**entonces** el archivo contiene todos, no solo los visibles.

### CA-07 — Independencia de búsqueda y filtros

**Dado** criterios activos en `MainActivity`,  
**cuando** se exporta,  
**entonces** el CSV incluye todos los registros.

### CA-08 — Encabezados

**Dado** una exportación correcta,  
**cuando** se abre el archivo,  
**entonces** la primera fila es:

```csv
category,code,site,position,observations
```

### CA-09 — Sin datos internos

**Dado** el CSV,  
**cuando** se inspecciona,  
**entonces** no contiene id ni fechas.

### CA-10 — Campos opcionales

**Dado** un registro sin posición u observaciones,  
**cuando** se exporta,  
**entonces** aparecen campos vacíos y no `null`.

### CA-11 — Escape CSV

**Dado** un campo con comas, comillas o saltos,  
**cuando** se exporta,  
**entonces** el CSV sigue siendo válido.

### CA-12 — UTF-8

**Dado** texto Unicode,  
**cuando** se exporta,  
**entonces** se conserva.

### CA-13 — Orden de filas

**Dado** varios registros,  
**cuando** se exportan,  
**entonces** aparecen ordenados por categoría y código.

### CA-14 — Base vacía

**Dado** que no existen registros,  
**cuando** se intenta exportar,  
**entonces** se muestra un mensaje y no se informa éxito.

### CA-15 — Cantidad exportada

**Dado** una exportación correcta,  
**cuando** finaliza,  
**entonces** el mensaje muestra la cantidad exacta.

### CA-16 — Doble exportación bloqueada

**Dado** que hay una exportación en curso,  
**cuando** el usuario pulsa otra vez,  
**entonces** no se inicia otra operación.

### CA-17 — Error de escritura

**Dado** un destino inaccesible,  
**cuando** falla la escritura,  
**entonces** se muestra un error y la aplicación no se cierra.

### CA-18 — Rotación

**Dado** que la exportación está en curso,  
**cuando** se rota,  
**entonces** no se duplica.

### CA-19 — Evento único

**Dado** que finaliza la exportación,  
**cuando** la Activity se recrea,  
**entonces** el mensaje no se repite.

### CA-20 — Sin permisos generales

**Dado** el Manifest,  
**cuando** se inspecciona,  
**entonces** no contiene permisos generales de almacenamiento.

### CA-21 — Offline

**Dado** que no existe conexión,  
**cuando** se exporta,  
**entonces** la operación funciona.

### CA-22 — UI desacoplada

**Dado** el flujo de exportación,  
**cuando** se revisa el código,  
**entonces** la Activity no consulta DAO ni serializa el CSV.

### CA-23 — Operación no bloqueante

**Dado** una exportación,  
**cuando** se consulta Room y se escribe el archivo,  
**entonces** el hilo principal no se bloquea.

---

## 21. Diseño técnico propuesto

### 21.1. Consulta puntual de todos los registros

La exportación necesita una instantánea, no una observación continua.

No se recomienda utilizar `observeForever()` para capturar la primera emisión.

DAO orientativo:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "ORDER BY category COLLATE NOCASE ASC, " +
    "code COLLATE NOCASE ASC"
)
List<WarehouseItemEntity> findAll();
```

### 21.2. Callback de lectura

```java
public interface WarehouseItemsFindCallback {

    void onSuccess(List<WarehouseItem> warehouseItems);

    void onError(Throwable throwable);
}
```

### 21.3. Puerto de repositorio

`WarehouseItemRepository` se ampliará con:

```java
void findAll(
        WarehouseItemsFindCallback callback
);
```

### 21.4. Adaptador Room

`RoomWarehouseItemRepository.findAll()` deberá:

- ejecutar en `databaseExecutor`;
- invocar `dao.findAll()`;
- mapear entidades a dominio;
- entregar una copia defensiva;
- conservar orden;
- transformar errores.

### 21.5. Puerto de entrada

```text
ExportWarehouseItemsUseCase
```

Firma orientativa:

```java
public interface ExportWarehouseItemsUseCase {

    void exportWarehouseItems(
            String destinationReference,
            ExportWarehouseItemsCallback callback
    );
}
```

### 21.6. Servicio de aplicación

`ExportWarehouseItemsService` deberá:

1. validar destino;
2. solicitar instantánea completa;
3. comprobar base vacía;
4. solicitar escritura al puerto CSV;
5. transformar resultados;
6. devolver cantidad;
7. no depender de Android;
8. no conocer `Uri` ni `ContentResolver`.

### 21.7. Puerto de salida CSV

```text
WarehouseItemCsvExporter
```

Firma orientativa:

```java
public interface WarehouseItemCsvExporter {

    void export(
            List<WarehouseItem> warehouseItems,
            String destinationReference,
            WarehouseItemCsvExportCallback callback
    );
}
```

### 21.8. Adaptador de salida

Paquete:

```text
adapter.out.file.csv
```

Componentes orientativos:

```text
AndroidCsvDocumentExporter
WarehouseItemCsvCodec
WarehouseItemCsvMapper
```

### 21.9. Executor

Se recomienda separar:

```text
databaseExecutor
fileExecutor
```

para que una escritura grande no bloquee futuras operaciones de Room.

### 21.10. ViewModel

Se añadirá:

```text
DataManagementViewModel
```

Responsabilidades:

- recibir `ExportWarehouseItemsUseCase`;
- exponer estado;
- bloquear doble exportación;
- emitir evento único;
- conservar estado ante rotación;
- no abrir el selector directamente;
- no usar `ContentResolver`.

### 21.11. Activity

`DataManagementActivity` deberá:

- registrar launcher de `CreateDocument`;
- configurar Toolbar;
- renderizar estado;
- lanzar selector;
- convertir URI en referencia opaca;
- delegar al ViewModel;
- mostrar resultado;
- no serializar;
- no consultar Room;
- no abrir streams directamente.

### 21.12. Nombre de archivo

Se recomienda:

```text
CsvExportFileNameFormatter
```

con fecha inyectable en pruebas.

### 21.13. AppContainer

Deberá construir:

- codec CSV;
- exportador Android;
- servicio de exportación;
- caso de uso;
- Factory del ViewModel;
- executor de archivos si se adopta.

### 21.14. Manifest

Añadir `DataManagementActivity` sin permisos de almacenamiento.

---

## 22. Estructura de archivos orientativa

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ExportWarehouseItemsUseCase.java
│   │   │   └── ExportWarehouseItemsCallback.java
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       ├── WarehouseItemsFindCallback.java
│   │       ├── WarehouseItemCsvExporter.java
│   │       └── WarehouseItemCsvExportCallback.java
│   ├── result/
│   │   └── ExportWarehouseItemsResult.java
│   └── service/
│       └── ExportWarehouseItemsService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   ├── MainActivity.java
│   │       │   └── DataManagementActivity.java
│   │       ├── formatter/
│   │       │   └── CsvExportFileNameFormatter.java
│   │       ├── state/
│   │       │   └── DataManagementUiState.java
│   │       └── viewmodel/
│   │           ├── DataManagementViewModel.java
│   │           └── DataManagementViewModelFactory.java
│   └── out/
│       ├── persistence/
│       │   └── room/
│       │       ├── dao/
│       │       │   └── WarehouseItemDao.java
│       │       └── repository/
│       │           └── RoomWarehouseItemRepository.java
│       └── file/
│           └── csv/
│               ├── AndroidCsvDocumentExporter.java
│               ├── WarehouseItemCsvCodec.java
│               └── WarehouseItemCsvMapper.java
└── configuration/
    └── AppContainer.java

res/
├── drawable/
│   ├── ic_data_management.xml
│   └── ic_file_download.xml
├── layout/
│   └── activity_data_management.xml
├── menu/
│   └── menu_main.xml
└── values/
    └── strings.xml
```

---

## 23. Decisiones técnicas importantes

### 23.1. No observar LiveData para exportar

La exportación es puntual. Se añadirá `findAll()`.

### 23.2. No exportar desde el adapter

El adapter solo representa filas visibles.

### 23.3. No escribir desde MainActivity

La escritura pertenece al adaptador de salida CSV.

### 23.4. No añadir permisos

Storage Access Framework es suficiente.

### 23.5. No compartir todavía

HU-13 utilizará archivo temporal y `FileProvider`.

### 23.6. No añadir una librería de lectura CSV

HU-12 solo escribe. El parser se evaluará antes de HU-14.

### 23.7. No usar el CSV de respaldo

HU-16 definirá el respaldo versionado.

### 23.8. No exportar filtros activos

La operación representa una exportación completa.

### 23.9. No prometer rollback del proveedor

Un error puede dejar un documento vacío o parcial. No se informará éxito.

### 23.10. No cambiar Room

La HU añade una consulta puntual. No cambia entidad, tabla ni versión de base.

---

## 24. Diseño de interfaz esperado

### 24.1. MainActivity

Acción nueva:

```text
Gestión de datos
```

Durante selección múltiple deberá estar deshabilitada o no disponible.

### 24.2. DataManagementActivity

```text
← Gestión de datos

Exportar mercancía a CSV
Guarda todos los registros en un archivo compatible.

[Exportar CSV]
```

### 24.3. Exportando

```text
Exportando mercancía...
[ProgressIndicator]
```

### 24.4. Base vacía

```text
No hay mercancía para exportar.
```

### 24.5. Error

```text
No se pudo exportar la mercancía.
[Reintentar]
```

---

## 25. Recursos de texto recomendados

```text
data_management_title
data_management_action
export_csv_title
export_csv_description
export_csv_action
export_csv_selecting_destination
export_csv_in_progress
export_csv_empty
export_csv_success
export_csv_invalid_destination
export_csv_read_error
export_csv_serialization_error
export_csv_write_error
export_csv_unknown_error
```

Se recomienda utilizar plurales Android para la cantidad exportada.

---

## 26. Pruebas recomendadas

### 26.1. WarehouseItemCsvCodecTest

- encabezado exacto;
- una fila;
- varias filas;
- orden;
- `null` a vacío;
- coma;
- comillas;
- CR;
- LF;
- CRLF;
- Unicode;
- UTF-8;
- CRLF;
- sin id;
- sin fechas;
- protección de fórmulas;
- sin BOM;
- última fila correcta.

### 26.2. WarehouseItemCsvMapperTest

- columnas en orden;
- campos obligatorios;
- campos opcionales;
- exclusión de internos;
- modelo no modificado.

### 26.3. ExportWarehouseItemsServiceTest

- destino nulo;
- destino vacío;
- solicita `findAll()`;
- base vacía;
- no invoca exporter con lista vacía;
- exportación válida;
- cantidad;
- read error;
- serialization error;
- write error;
- error desconocido;
- sin Android.

### 26.4. RoomWarehouseItemRepositoryTest

- `findAll()` en executor;
- mapeo;
- orden;
- lista vacía;
- error;
- no utiliza LiveData.

### 26.5. WarehouseItemDaoTest

- devuelve todos;
- ordena categoría;
- ordena código;
- incluye sin posición;
- no depende de filtros.

### 26.6. DataManagementViewModelTest

- IDLE;
- solicitud única de selector;
- cancelación;
- exportación;
- doble pulsación;
- success único;
- empty;
- error;
- rotación;
- nombre de archivo.

### 26.7. DataManagementActivityTest

- acción Exportar;
- MIME correcto;
- nombre sugerido;
- cancelación;
- progreso;
- cantidad;
- error;
- Atrás;
- rotación;
- sin DAO;
- sin serialización.

### 26.8. Prueba instrumentada de documento

- abrir destino;
- escribir;
- cerrar;
- leer resultado;
- verificar encabezado;
- verificar Unicode;
- simular `IOException`.

### 26.9. Pruebas manuales

- un registro;
- varios;
- sin posición;
- observaciones vacías;
- comas;
- comillas;
- multilínea;
- tildes y ñ;
- cancelar selector;
- modo avión;
- rotación;
- doble pulsación;
- base vacía;
- abrir en editor;
- abrir en hoja de cálculo.

---

## 27. Tareas de implementación

1. Confirmar `develop` en `1.1.0`.
2. Verificar CI.
3. Analizar ZIP actualizado.
4. Crear `feature/hu-12-exportar-mercancia-csv`.
5. Definir formato CSV.
6. Definir escape.
7. Definir protección de fórmulas.
8. Crear callback de lectura.
9. Ampliar repositorio.
10. Añadir `findAll()` al DAO.
11. Implementar lectura puntual en Room repository.
12. Crear resultado de exportación.
13. Crear callback de entrada.
14. Crear caso de uso.
15. Crear puerto CSV.
16. Crear callback CSV.
17. Implementar mapper.
18. Implementar codec.
19. Implementar exportador Android.
20. Crear executor de archivos si procede.
21. Implementar servicio.
22. Crear estado UI.
23. Crear ViewModel.
24. Crear Factory.
25. Actualizar `AppContainer`.
26. Crear Activity.
27. Crear layout.
28. Añadir Activity al Manifest.
29. Añadir acceso desde MainActivity.
30. Añadir menú e iconos.
31. Registrar `CreateDocument`.
32. Implementar nombre sugerido.
33. Renderizar estados.
34. Añadir plurales.
35. Bloquear doble exportación.
36. Probar rotación.
37. Crear pruebas del codec.
38. Crear pruebas del mapper.
39. Crear pruebas del servicio.
40. Ampliar pruebas DAO.
41. Crear pruebas del repositorio.
42. Crear pruebas del ViewModel.
43. Crear pruebas de Activity.
44. Ejecutar pruebas instrumentadas.
45. Ejecutar `./gradlew testDebugUnitTest`.
46. Ejecutar `./gradlew lintDebug`.
47. Ejecutar `./gradlew assembleDebug`.
48. Ejecutar `./gradlew connectedDebugAndroidTest`.
49. Publicar commits con `#14`.
50. Verificar CI de la rama.
51. Recopilar evidencias.
52. Revisar criterios.
53. Fusionar localmente en `develop`.
54. Verificar CI de `develop`.
55. Eliminar rama tras integración.

---

## 28. Estrategia de commits orientativa

### Bloque 1 — Lectura puntual de Room

```text
git commit -m "feat: add snapshot query for warehouse item export #14"
```

### Bloque 2 — Formato y adaptador CSV

```text
git commit -m "feat: add CSV warehouse item export adapter #14"
```

### Bloque 3 — Caso de uso de exportación

```text
git commit -m "feat: add warehouse item CSV export use case #14"
```

### Bloque 4 — Pantalla Gestión de datos

```text
git commit -m "feat: add data management CSV export screen #14"
```

### Bloque 5 — Integración y recursos

```text
git commit -m "feat: integrate CSV export document flow #14"
```

### Bloque 6 — Pruebas

```text
git commit -m "test: cover warehouse item CSV export flow #14"
```

---

## 29. Evidencias necesarias para cerrar HU-12

- acceso Gestión de datos;
- pantalla de exportación;
- selector Android;
- MIME `text/csv`;
- nombre sugerido;
- cancelación;
- CSV con encabezados;
- CSV con una fila;
- CSV con varias filas;
- todos los registros;
- independencia de búsqueda y filtros;
- orden correcto;
- opcionales vacíos;
- coma escapada;
- comillas escapadas;
- salto de línea conservado;
- Unicode;
- ausencia de id;
- ausencia de fechas;
- base vacía;
- cantidad exportada;
- doble pulsación bloqueada;
- error de lectura;
- error de escritura;
- rotación;
- modo avión;
- Manifest sin permisos de almacenamiento;
- Activity sin acceso a DAO;
- Activity sin serialización CSV;
- DAO con `findAll()`;
- pruebas unitarias;
- pruebas DAO;
- pruebas instrumentadas;
- lint;
- compilación debug;
- CI satisfactoria en rama;
- merge en `develop`;
- CI satisfactoria en `develop`.

---

## 30. Definición de terminado

HU-12 estará terminada cuando:

- exista acceso a Gestión de datos;
- exista `DataManagementActivity`;
- exista acción Exportar CSV;
- Android permita elegir destino;
- se sugiera nombre `.csv`;
- cancelar no sea error;
- se exporten todos los registros;
- búsqueda y filtros no limiten la exportación;
- la lectura sea puntual;
- no se use `observeForever()`;
- el CSV use encabezados oficiales;
- orden de columnas y filas sea estable;
- opcionales se exporten vacíos;
- comas, comillas y saltos se escapen;
- Unicode se conserve;
- se use UTF-8;
- política de BOM esté definida;
- política de fórmulas esté definida;
- no se exporten ids ni fechas;
- base vacía se controle;
- se muestre cantidad;
- doble exportación se bloquee;
- errores se diferencien;
- rotación no duplique;
- eventos se consuman una vez;
- funcione sin conexión;
- no se soliciten permisos generales;
- Activity no consulte Room ni serialice CSV;
- dominio no dependa de Android;
- no cambie el esquema Room;
- no exista migración artificial;
- pruebas finalicen correctamente;
- lint no tenga errores;
- `assembleDebug` finalice correctamente;
- CI sea satisfactoria;
- criterios y evidencias estén completos;
- rama se fusione en `develop` y se elimine tras integración.

---

## 31. Resultado esperado

Al cerrar HU-12, AlmacenTracker podrá generar un archivo CSV interoperable mediante un flujo seguro y local:

```text
Room
  ↓ lectura puntual
WarehouseItem
  ↓ mapper y codec
CSV UTF-8
  ↓ ContentResolver
documento elegido por el usuario
```

El proyecto quedará preparado para:

```text
HU-13 — Compartir exportación CSV
```

HU-13 reutilizará la definición de columnas, el codec, el mapper y las pruebas de serialización.

---

## 32. Commit documental recomendado

Este documento se conservará en “Fuentes” y no se añadirá al repositorio público.

```text
Sin commit en Git: guardar HU-12-exportar-mercancia-a-csv.md en Fuentes.
```
