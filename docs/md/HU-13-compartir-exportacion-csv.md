# HU-13 — Compartir exportación CSV

> Segunda historia de usuario de AlmacenTracker v1.1.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.1  
**Versión Android:** 1.1.0  
**Historia:** HU-13  
**Nombre:** Compartir exportación CSV  
**Prioridad:** Media  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-13-compartir-exportacion-csv`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-12 — Exportar mercancía a CSV  
**Versión estable de partida:** `v1.0.0`

---

## 2. Historia de usuario

Como usuario,  
quiero compartir un archivo CSV generado por AlmacenTracker,  
para enviarlo mediante otra aplicación instalada en el dispositivo.

---

## 3. Objetivo

Ampliar la pantalla de Gestión de datos para permitir compartir todos los registros de mercancía mediante el selector nativo de Android.

El flujo deberá:

```text
usuario abre Gestión de datos
        ↓
pulsa Compartir CSV
        ↓
la aplicación recupera todos los registros desde Room
        ↓
genera un CSV temporal en almacenamiento privado
        ↓
FileProvider crea una URI content:// temporal
        ↓
Android abre el selector de compartir
        ↓
el usuario elige una aplicación receptora
```

La HU-13 reutilizará el formato CSV y los componentes puros implementados en HU-12. No deberá crear un segundo formato, mapper o codec paralelo.

Flujo arquitectónico previsto:

```text
DataManagementActivity
        ↓
DataManagementViewModel
        ↓
ShareWarehouseItemsUseCase
        ↓
ShareWarehouseItemsService
        ├── WarehouseItemRepository
        └── WarehouseItemCsvShareFileGateway
                ↓
        RoomWarehouseItemRepository
        AndroidCsvShareFileGateway
                ↓
        WarehouseItemDao
        WarehouseItemCsvCodec
        cacheDir / FileProvider
```

La creación del `Intent.ACTION_SEND` y la apertura del chooser pertenecen al adaptador de entrada Android. La lógica de consulta y generación del archivo temporal no deberá residir en la Activity.

---

## 4. Documentos de referencia

La HU-13 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.1-general-plan.md`;
- `HU-12-exportar-mercancia-a-csv.md`;
- el estado real de `AlmacenTrackerHU12.zip`;
- las reglas funcionales cerradas en v1.0;
- la arquitectura hexagonal pragmática;
- Room como fuente de verdad;
- la ausencia de permisos generales de almacenamiento;
- la política documental del proyecto.

El plan general del proyecto asigna a v1.1:

```text
importación
exportación
copia de seguridad
compartición CSV
```

El plan de v1.1 define para HU-13:

- generar un archivo temporal;
- utilizar `FileProvider`;
- abrir el selector de compartir;
- conceder acceso temporal de lectura;
- no exponer rutas internas;
- limpiar archivos temporales de forma controlada;
- controlar la ausencia de aplicaciones receptoras.

---

## 5. Estado real del proyecto antes de HU-13

El ZIP actualizado de HU-12 confirma que el proyecto ya dispone de:

- `DataManagementActivity`;
- `DataManagementViewModel`;
- `DataManagementViewModelFactory`;
- `DataManagementUiState`;
- `ExportWarehouseItemsUseCase`;
- `ExportWarehouseItemsService`;
- `ExportWarehouseItemsResult`;
- `WarehouseItemsFindCallback`;
- `WarehouseItemRepository.findAll()`;
- `WarehouseItemDao.findAll()`;
- `WarehouseItemCsvExporter`;
- `WarehouseItemCsvExportCallback`;
- `AndroidCsvDocumentExporter`;
- `WarehouseItemCsvMapper`;
- `WarehouseItemCsvCodec`;
- lectura puntual y ordenada de todos los registros;
- escritura UTF-8;
- encabezado CSV oficial;
- escape de comas, comillas y saltos de línea;
- protección frente a fórmulas CSV;
- selector `CreateDocument("text/csv")`;
- nombre sugerido de exportación;
- eventos de una sola consumición;
- manejo de base vacía y errores;
- funcionamiento sin conexión;
- acceso a Gestión de datos desde `MainActivity`;
- composición explícita en `AppContainer`.

El formato CSV implementado en HU-12 es:

```csv
category,code,site,position,observations
```

El proyecto todavía no dispone de:

- acción Compartir CSV en `DataManagementActivity`;
- `ShareWarehouseItemsUseCase`;
- `ShareWarehouseItemsService`;
- `ShareWarehouseItemsResult`;
- puerto para generar archivos temporales compartibles;
- implementación de archivo temporal en `cacheDir`;
- configuración de `FileProvider`;
- `file_paths.xml` o recurso equivalente;
- URI `content://` compartible;
- `Intent.ACTION_SEND` para CSV;
- `FLAG_GRANT_READ_URI_PERMISSION`;
- selector `Intent.createChooser()`;
- control de ausencia de aplicación receptora;
- política de limpieza de archivos temporales;
- pruebas específicas del flujo de compartición.

El Manifest actual no declara `FileProvider` y no solicita permisos generales de almacenamiento.

---

## 6. Alcance incluido

HU-13 incluye:

- añadir la acción Compartir CSV a Gestión de datos;
- generar un CSV con todos los registros de Room;
- reutilizar `WarehouseItemRepository.findAll()`;
- reutilizar `WarehouseItemCsvMapper`;
- reutilizar `WarehouseItemCsvCodec`;
- generar un archivo temporal dentro del almacenamiento privado de la aplicación;
- crear un nombre de archivo comprensible;
- usar extensión `.csv`;
- utilizar codificación UTF-8;
- mantener exactamente el encabezado y formato de HU-12;
- utilizar `FileProvider`;
- generar una URI `content://`;
- evitar URI `file://`;
- usar `Intent.ACTION_SEND`;
- usar MIME `text/csv`;
- añadir `Intent.EXTRA_STREAM`;
- añadir `FLAG_GRANT_READ_URI_PERMISSION`;
- usar `ClipData` cuando sea necesario para compatibilidad;
- abrir `Intent.createChooser()`;
- permitir seleccionar una aplicación receptora;
- controlar cancelación del chooser sin tratarla como error;
- controlar que no existan aplicaciones receptoras;
- controlar base vacía;
- controlar error de lectura de Room;
- controlar error de serialización;
- controlar error de creación o escritura temporal;
- controlar error de `FileProvider`;
- impedir compartir dos veces simultáneamente;
- conservar estado ante rotación;
- evitar repetir apertura del chooser tras recreación;
- limpiar archivos temporales antiguos de manera controlada;
- no eliminar el archivo antes de que la aplicación receptora pueda leerlo;
- funcionar sin conexión;
- no solicitar permisos de almacenamiento;
- no exponer rutas internas;
- pruebas unitarias;
- pruebas de adaptador;
- pruebas de ViewModel;
- pruebas instrumentadas;
- CI.

---

## 7. Alcance excluido

HU-13 no incluye:

- compartir el archivo elegido previamente en HU-12;
- recordar la última URI exportada;
- pedir al usuario seleccionar un archivo existente;
- importar CSV;
- validar archivos de entrada;
- editar el CSV antes de compartir;
- seleccionar solo parte de los registros;
- compartir únicamente búsqueda o filtros activos;
- compartir únicamente selección múltiple;
- incluir `id`;
- incluir `createdAt`;
- incluir `updatedAt`;
- usar el formato de copia de seguridad;
- enviar automáticamente por correo;
- elegir una aplicación receptora sin intervención del usuario;
- subir automáticamente a Drive, Dropbox u otro servicio;
- integración directa con API de nube;
- generar enlaces públicos;
- compartir varios archivos a la vez;
- usar `ACTION_SEND_MULTIPLE`;
- comprimir el CSV;
- cifrar el archivo;
- protegerlo con contraseña;
- mantener un historial persistente de comparticiones;
- auditoría de aplicaciones receptoras;
- importación, respaldo o restauración.

Importar pertenece a HU-14 y HU-15.

Copia de seguridad y restauración pertenecen a HU-16 y HU-17.

---

## 8. Precondiciones

Antes de comenzar HU-13 deberán cumplirse:

- HU-12 integrada en `develop`;
- CI de HU-12 satisfactoria;
- `DataManagementActivity` operativa;
- exportación CSV operativa;
- formato CSV cerrado y probado;
- `WarehouseItemCsvCodec` operativo;
- `WarehouseItemCsvMapper` operativo;
- `WarehouseItemRepository.findAll()` operativo;
- `AppContainer` actualizado con los componentes de HU-12;
- Room como única fuente de verdad;
- Manifest sin permisos generales de almacenamiento;
- aplicación en `versionName "1.1.0"`;
- `versionCode 2`;
- funcionamiento offline conservado.

---

## 9. Decisiones funcionales principales

### 9.1. Se comparte una exportación nueva

Cada pulsación sobre Compartir CSV generará una instantánea nueva de Room.

No se reutilizará automáticamente:

- el último documento guardado por HU-12;
- una URI persistida;
- un archivo seleccionado por el usuario;
- un archivo externo desconocido.

Motivos:

- HU-12 no garantiza que la aplicación conserve acceso a la URI;
- el contenido podría haber quedado desactualizado;
- compartir debe reflejar el estado actual de Room;
- evita almacenar referencias externas innecesarias;
- simplifica permisos y privacidad.

### 9.2. Se comparten todos los registros

La compartición utilizará:

```text
todos los registros de Room
```

No dependerá de:

- búsqueda activa;
- filtros activos;
- selección múltiple;
- filas visibles en RecyclerView.

### 9.3. Archivo temporal privado

El archivo se generará bajo un directorio controlado por la aplicación, por ejemplo:

```text
cacheDir/shared_csv/
```

No se escribirá directamente en almacenamiento público.

### 9.4. Acceso temporal

La aplicación receptora recibirá permiso de lectura temporal sobre una URI `content://`.

No recibirá:

- ruta absoluta;
- acceso al resto de la caché;
- permiso de escritura;
- permiso persistente.

### 9.5. Chooser obligatorio

Se utilizará:

```java
Intent.createChooser(...)
```

La aplicación no seleccionará automáticamente un receptor concreto.

### 9.6. Base vacía

Si Room no contiene registros:

- no se genera archivo;
- no se abre chooser;
- se muestra un mensaje comprensible.

Mensaje orientativo:

```text
No hay mercancía para compartir.
```

---

## 10. Diferencia entre Exportar y Compartir

### Exportar CSV

```text
usuario elige destino
        ↓
archivo persistente fuera de la app
```

Características:

- usa Storage Access Framework;
- usa `CreateDocument`;
- el usuario controla nombre y ubicación;
- la aplicación escribe en una URI elegida.

### Compartir CSV

```text
app genera archivo temporal
        ↓
FileProvider entrega URI temporal
        ↓
usuario elige receptor
```

Características:

- no solicita destino;
- no expone rutas;
- usa `ACTION_SEND`;
- el archivo pertenece a caché privada;
- el permiso es temporal.

No deberán mezclarse ambos flujos en un único método Android con condicionales confusos.

Se reutilizará la serialización, no el adaptador de destino.

---

## 11. Pantalla Gestión de datos

La pantalla actual se ampliará para mostrar dos acciones:

```text
Gestión de datos

Exportar CSV
Guarda todos los registros en un archivo CSV.
[Exportar]

Compartir CSV
Genera un archivo temporal para enviarlo a otra aplicación.
[Compartir]
```

### 11.1. Reutilización de Activity

No se creará una `ShareCsvActivity`.

La compartición se integrará en:

```text
DataManagementActivity
```

### 11.2. Estados simultáneos

Mientras una operación de archivo esté activa:

- Exportar quedará deshabilitado;
- Compartir quedará deshabilitado;
- no se iniciará otra operación;
- se mostrará un único progreso coherente.

### 11.3. Acciones futuras

HU-13 no habilitará aún:

- Importar;
- Crear copia de seguridad;
- Restaurar.

---

## 12. Formato del archivo compartido

El archivo compartido deberá ser byte a byte compatible con HU-12, salvo por el nombre y el destino.

Encabezado:

```csv
category,code,site,position,observations
```

Reglas heredadas:

- UTF-8;
- sin BOM, según la decisión de HU-12;
- CRLF uniforme;
- opcionales como campos vacíos;
- comas escapadas;
- comillas duplicadas;
- multilínea entrecomillada;
- protección reversible frente a fórmulas;
- sin `id`;
- sin fechas;
- orden por categoría y código.

No se creará:

```text
WarehouseItemShareCsvCodec
```

si `WarehouseItemCsvCodec` ya cumple el formato.

---

## 13. Nombre del archivo temporal

Formato recomendado:

```text
almacentracker-share-AAAA-MM-DD-HHmmss.csv
```

Ejemplo:

```text
almacentracker-share-2026-07-18-153025.csv
```

La inclusión de hora, minuto y segundo reduce colisiones durante comparticiones sucesivas.

Reglas:

- nombre seguro para sistemas de archivos;
- sin espacios obligatorios;
- sin caracteres dependientes de locale;
- extensión `.csv`;
- fecha inyectable para pruebas;
- no incluir datos de mercancía en el nombre.

---

## 14. Configuración de FileProvider

### 14.1. Declaración en Manifest

Se añadirá un provider no exportado:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">

    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

La autoridad deberá derivar de:

```text
${applicationId}.fileprovider
```

No se escribirá una autoridad rígida distinta al `applicationId` sin justificación.

### 14.2. Rutas permitidas

Recurso orientativo:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <cache-path
        name="shared_csv"
        path="shared_csv/" />
</paths>
```

No se permitirá:

```xml
<cache-path path="." />
```

si puede limitarse al subdirectorio exacto.

### 14.3. URI esperada

Ejemplo conceptual:

```text
content://com.rndymi.almacentracker.fileprovider/shared_csv/almacentracker-share-....csv
```

Nunca:

```text
file:///data/user/0/.../cache/...
```

---

## 15. Generación del archivo temporal

### 15.1. Directorio

El adaptador creará, si no existe:

```text
cacheDir/shared_csv
```

### 15.2. Escritura

El flujo técnico será:

1. obtener la lista de dominio;
2. serializar con `WarehouseItemCsvCodec`;
3. crear el directorio limitado;
4. crear o reemplazar el archivo objetivo;
5. escribir mediante `FileOutputStream`;
6. cerrar con try-with-resources;
7. obtener URI mediante `FileProvider.getUriForFile()`;
8. devolver una referencia compartible.

### 15.3. Operación no bloqueante

La consulta Room y la escritura temporal no se ejecutarán en el hilo principal.

Podrá reutilizarse el executor existente, aunque se recomienda revisar si conviene separar:

```text
databaseExecutor
fileExecutor
```

La decisión final deberá ser consistente con HU-12.

---

## 16. Referencia compartible

La capa de aplicación no deberá depender de:

```java
android.net.Uri
android.content.Intent
androidx.core.content.FileProvider
```

Se recomienda un resultado puro:

```text
ShareableCsvFile
├── contentReference
├── displayName
├── mimeType
└── recordCount
```

Donde:

```text
contentReference = URI convertida a String
mimeType = text/csv
```

La Activity convertirá la referencia nuevamente a `Uri` exclusivamente para construir el Intent Android.

También será válida una clase equivalente si mantiene el límite arquitectónico.

---

## 17. Intent de compartición

La Activity construirá conceptualmente:

```java
Intent sendIntent = new Intent(Intent.ACTION_SEND);
sendIntent.setType("text/csv");
sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
```

Se recomienda añadir:

```java
sendIntent.setClipData(
        ClipData.newUri(
                getContentResolver(),
                displayName,
                contentUri
        )
);
```

Esto mejora la propagación del permiso en ciertos receptores.

Después:

```java
Intent chooser = Intent.createChooser(
        sendIntent,
        getString(R.string.share_csv_chooser_title)
);
```

### 17.1. Extras opcionales

Podrá añadirse:

```java
Intent.EXTRA_SUBJECT
```

con un texto genérico:

```text
Exportación de AlmacenTracker
```

No se añadirá el contenido completo del CSV como `EXTRA_TEXT`.

---

## 18. Detección de aplicaciones receptoras

Antes de abrir el chooser se comprobará que existe al menos una Activity capaz de resolver el Intent.

Alternativas válidas:

```java
sendIntent.resolveActivity(getPackageManager()) != null
```

u obtención de lista mediante `PackageManager`.

Si no existe receptor:

- no se cierra la Activity;
- no se lanza `ActivityNotFoundException` sin controlar;
- se muestra un mensaje comprensible;
- el archivo podrá limpiarse posteriormente.

Mensaje orientativo:

```text
No hay ninguna aplicación disponible para compartir el archivo CSV.
```

La ausencia de receptor no es un error de Room ni de serialización.

---

## 19. Estados de interfaz

`DataManagementUiState` deberá ampliarse sin crear estados contradictorios.

Modelo orientativo:

```text
IDLE
SELECTING_DESTINATION
EXPORTING
PREPARING_SHARE
READY_TO_SHARE
EMPTY_DATABASE
ERROR
```

`READY_TO_SHARE` puede representarse mediante evento en lugar de estado persistente.

### 19.1. IDLE

- Exportar habilitado;
- Compartir habilitado;
- sin progreso.

### 19.2. SELECTING_DESTINATION

Estado heredado de HU-12.

- Exportar y Compartir deshabilitados;
- selector de documento en curso.

### 19.3. EXPORTING

Estado heredado de HU-12.

- ambas acciones deshabilitadas;
- progreso visible.

### 19.4. PREPARING_SHARE

- se consulta Room;
- se genera archivo temporal;
- se obtiene URI;
- ambas acciones deshabilitadas;
- progreso visible;
- texto “Preparando archivo para compartir…”.

### 19.5. READY_TO_SHARE

La aplicación dispone de una referencia válida.

Se recomienda emitir un evento de una sola consumición:

```text
UiEvent<ShareableCsvFile>
```

La Activity abre el chooser y después devuelve el estado a `IDLE`.

### 19.6. EMPTY_DATABASE

El mensaje deberá ser contextual según la operación solicitada.

Para compartir:

```text
No hay mercancía para compartir.
```

No se recomienda mantener un único texto fijo “para exportar” en el ViewModel si ahora existen dos acciones.

### 19.7. ERROR

Debe distinguir internamente:

- lectura;
- serialización;
- escritura temporal;
- FileProvider;
- receptor ausente.

La UI podrá mostrar mensajes específicos.

---

## 20. Resultado de aplicación

Se añadirá:

```text
ShareWarehouseItemsResult
```

Estados recomendados:

```text
SUCCESS
EMPTY_DATABASE
READ_ERROR
SERIALIZATION_ERROR
TEMP_FILE_ERROR
FILE_PROVIDER_ERROR
UNKNOWN_ERROR
```

Datos para `SUCCESS`:

```text
ShareableCsvFile
```

La ausencia de receptor puede modelarse:

- como estado de UI al construir el Intent;
- o como `NO_RECEIVER` si la responsabilidad se encapsula en un adaptador Android.

Recomendación:

```text
NO_RECEIVER
```

pertenece al adaptador de entrada, porque el caso de uso solo prepara el archivo y no conoce las aplicaciones instaladas.

---

## 21. Puerto de entrada

Se añadirá:

```text
ShareWarehouseItemsUseCase
```

Firma orientativa:

```java
public interface ShareWarehouseItemsUseCase {

    void prepareWarehouseItemsForSharing(
            Callback callback
    );

    interface Callback {
        void onResult(ShareWarehouseItemsResult result);
    }
}
```

No necesita recibir una URI de destino porque el archivo se crea internamente en caché.

---

## 22. Servicio de aplicación

`ShareWarehouseItemsService` deberá:

1. solicitar todos los registros mediante `WarehouseItemRepository.findAll()`;
2. devolver `EMPTY_DATABASE` si corresponde;
3. solicitar al puerto de salida la creación del archivo compartible;
4. transformar errores;
5. devolver la referencia compartible;
6. no importar Android;
7. no crear Intents;
8. no abrir chooser;
9. no comprobar aplicaciones instaladas;
10. no eliminar archivos activos antes de compartir.

No deberá duplicar el código de `ExportWarehouseItemsService` mediante copia literal extensa.

Se permite compartir una función o componente común de lectura si aporta claridad real. No se recomienda crear un servicio genérico excesivamente abstracto únicamente por evitar pocas líneas.

---

## 23. Puerto de salida

Se añadirá un contrato específico para archivo temporal compartible:

```text
WarehouseItemCsvShareFileGateway
```

Firma orientativa:

```java
public interface WarehouseItemCsvShareFileGateway {

    void createShareableFile(
            List<WarehouseItem> warehouseItems,
            String suggestedFileName,
            Callback callback
    );
}
```

Callback orientativo:

```java
interface Callback {
    void onSuccess(ShareableCsvFile file);
    void onSerializationError(Throwable throwable);
    void onTemporaryFileError(Throwable throwable);
    void onFileProviderError(Throwable throwable);
    void onUnknownError(Throwable throwable);
}
```

No se recomienda modificar `WarehouseItemCsvExporter` para que devuelva a veces una URI elegida y otras veces un archivo temporal. Son destinos con semánticas distintas.

Se reutiliza:

```text
WarehouseItemCsvCodec
```

No necesariamente el mismo puerto de escritura.

---

## 24. Adaptador de salida

Se añadirá, dentro de la estructura existente:

```text
adapter.out.file.csv
```

Clase orientativa:

```text
AndroidCsvShareFileGateway
```

Responsabilidades:

- recibir `Context` de aplicación;
- recibir `WarehouseItemCsvCodec`;
- recibir executor;
- crear directorio temporal;
- limpiar temporales antiguos según política;
- generar archivo;
- escribir bytes;
- obtener URI de `FileProvider`;
- devolver `ShareableCsvFile`;
- clasificar errores.

No deberá:

- consultar Room;
- mostrar Toast;
- abrir chooser;
- crear ViewModels;
- guardar historial.

---

## 25. Política de limpieza de archivos temporales

### 25.1. Problema

Eliminar el archivo inmediatamente después de llamar a `startActivity()` puede impedir que la aplicación receptora lo lea.

Android no garantiza que el receptor consuma el archivo antes de que el método retorne.

### 25.2. Decisión recomendada

No eliminar el archivo recién compartido de inmediato.

Antes de crear uno nuevo:

- limpiar archivos del directorio `shared_csv` con antigüedad superior a un umbral;
- conservar los archivos recientes;
- limitar el número total de temporales.

Política inicial propuesta:

```text
eliminar archivos con más de 24 horas
```

y, adicionalmente:

```text
conservar como máximo los 10 archivos más recientes
```

La política deberá ser determinista y probada.

### 25.3. Fallos de limpieza

Un fallo al borrar un archivo antiguo no deberá impedir compartir uno nuevo, salvo que provoque falta de espacio o imposibilidad de crear el archivo.

### 25.4. Limpieza de archivo fallido

Si la escritura del archivo nuevo falla:

- cerrar el stream;
- intentar eliminar el archivo parcial;
- no devolver una URI válida;
- informar `TEMP_FILE_ERROR`.

---

## 26. Seguridad y privacidad

### 26.1. Provider no exportado

```text
android:exported="false"
```

### 26.2. Permisos temporales

Solo:

```text
FLAG_GRANT_READ_URI_PERMISSION
```

No añadir:

```text
FLAG_GRANT_WRITE_URI_PERMISSION
```

sin necesidad real.

### 26.3. Ruta limitada

El provider expondrá exclusivamente:

```text
cacheDir/shared_csv/
```

### 26.4. Sin rutas internas

No se registrará ni mostrará al usuario la ruta absoluta del archivo.

### 26.5. Datos compartidos

El archivo contiene todos los registros de intercambio:

- categoría;
- código;
- sitio;
- posición;
- observaciones.

Antes de compartir, la descripción de UI deberá dejar claro que se compartirán todos los registros.

### 26.6. Intervención del usuario

El envío final depende de que el usuario elija y confirme en una aplicación receptora.

AlmacenTracker no enviará información silenciosamente.

---

## 27. Flujo principal

1. El usuario abre AlmacenTracker.
2. Entra en Gestión de datos.
3. Pulsa Compartir CSV.
4. El ViewModel comprueba que no existe otra operación activa.
5. Emite `PREPARING_SHARE`.
6. Invoca `ShareWarehouseItemsUseCase`.
7. El servicio solicita `repository.findAll()`.
8. Room devuelve todos los registros ordenados.
9. El servicio comprueba que la lista no está vacía.
10. Solicita crear archivo temporal.
11. El adaptador limpia temporales antiguos de forma controlada.
12. El codec genera el contenido CSV.
13. El adaptador escribe el archivo en `cacheDir/shared_csv`.
14. `FileProvider` genera una URI `content://`.
15. El adaptador devuelve `ShareableCsvFile`.
16. El servicio devuelve `SUCCESS`.
17. El ViewModel emite un evento único.
18. La Activity construye `ACTION_SEND`.
19. Añade `EXTRA_STREAM`.
20. Añade permiso temporal de lectura.
21. Comprueba que existe receptor.
22. Abre `Intent.createChooser()`.
23. El usuario selecciona una aplicación.
24. La aplicación receptora lee el archivo.
25. AlmacenTracker vuelve a estado `IDLE`.

---

## 28. Flujos alternativos

### FA-01 — Base vacía

1. El usuario pulsa Compartir.
2. Room devuelve lista vacía.
3. No se genera archivo.
4. No se abre chooser.
5. Se muestra “No hay mercancía para compartir”.

### FA-02 — Error de lectura

- Room falla;
- no se serializa;
- se devuelve `READ_ERROR`;
- la pantalla permanece abierta.

### FA-03 — Error de serialización

- el codec falla;
- no se devuelve URI;
- se elimina cualquier archivo parcial;
- se muestra un error.

### FA-04 — Error al crear directorio

- no se puede crear `shared_csv`;
- se devuelve `TEMP_FILE_ERROR`.

### FA-05 — Error al escribir

- se cierra el stream;
- se intenta eliminar archivo parcial;
- no se abre chooser.

### FA-06 — Error de FileProvider

- el archivo existe pero no puede obtenerse URI;
- se devuelve `FILE_PROVIDER_ERROR`;
- no se usa `Uri.fromFile()` como alternativa.

### FA-07 — Sin aplicación receptora

1. El archivo se prepara correctamente.
2. Ninguna Activity resuelve `ACTION_SEND text/csv`.
3. No se abre chooser.
4. Se muestra un mensaje específico.
5. El archivo queda sujeto a limpieza posterior.

### FA-08 — Usuario cancela chooser

Android no garantiza un callback universal sobre la elección.

Cancelar el chooser:

- no se trata como error;
- no se muestra “Compartido correctamente”;
- no se elimina inmediatamente el archivo.

### FA-09 — Receptor genérico

Algunas aplicaciones pueden declarar `*/*` y no `text/csv`.

La implementación mantendrá `text/csv` como MIME correcto. No se degradará automáticamente a `*/*` salvo evidencia de compatibilidad y decisión documentada.

### FA-10 — Doble pulsación

Mientras se prepara el archivo:

- se ignora una nueva pulsación;
- no se generan dos archivos;
- no se abren dos chooser.

### FA-11 — Pulsar Exportar mientras comparte

Exportar permanece deshabilitado hasta que finalice la preparación.

### FA-12 — Rotación durante preparación

- el ViewModel mantiene `PREPARING_SHARE`;
- no se inicia otra operación;
- la nueva Activity recibe el evento cuando termine.

### FA-13 — Rotación después del evento

- el evento ya consumido no vuelve a abrir el chooser;
- no aparecen chooser duplicados.

### FA-14 — Activity en background

Si el resultado llega cuando la Activity no puede abrir el chooser de forma segura:

- el evento deberá conservarse hasta que exista un observer activo;
- no se perderá el archivo preparado;
- no se abrirá desde un contexto inadecuado.

### FA-15 — Compartición offline

El flujo funciona en modo avión.

### FA-16 — Comparticiones sucesivas

Cada operación crea un nombre nuevo.

La limpieza conserva límites de antigüedad y cantidad.

### FA-17 — Archivo antiguo todavía en uso

La política de 24 horas reduce el riesgo de eliminar archivos que un receptor aún esté leyendo.

No se promete disponibilidad indefinida.

### FA-18 — Observaciones con caracteres especiales

El archivo conserva exactamente las reglas de HU-12.

---

## 29. Criterios de aceptación

### CA-01 — Acción Compartir disponible

**Dado** que el usuario abre Gestión de datos,  
**cuando** la pantalla se renderiza,  
**entonces** dispone de Compartir CSV.

### CA-02 — Reutilización del formato

**Dado** que se genera el archivo,  
**cuando** se inspecciona,  
**entonces** utiliza el mismo formato de HU-12.

### CA-03 — Todos los registros

**Dado** que Room contiene mercancía,  
**cuando** se comparte,  
**entonces** el archivo incluye todos los registros.

### CA-04 — Independencia de búsqueda y filtros

**Dado** que existen criterios activos en el listado,  
**cuando** se comparte,  
**entonces** no limitan el archivo.

### CA-05 — Archivo temporal

**Dado** que se prepara la compartición,  
**cuando** se escribe el CSV,  
**entonces** se guarda dentro de caché privada.

### CA-06 — Subdirectorio limitado

**Dado** el archivo temporal,  
**cuando** se genera,  
**entonces** pertenece al subdirectorio `shared_csv`.

### CA-07 — FileProvider

**Dado** un archivo válido,  
**cuando** se prepara para compartir,  
**entonces** se obtiene mediante `FileProvider`.

### CA-08 — URI content

**Dado** el archivo compartible,  
**cuando** se entrega a la Activity,  
**entonces** la URI utiliza esquema `content`.

### CA-09 — Sin file URI

**Dado** el flujo completo,  
**cuando** se revisa el código,  
**entonces** no utiliza `Uri.fromFile()` ni `file://`.

### CA-10 — Provider no exportado

**Dado** el Manifest,  
**cuando** se inspecciona,  
**entonces** el provider tiene `android:exported="false"`.

### CA-11 — Authority correcta

**Dado** el provider,  
**cuando** se configura,  
**entonces** usa `${applicationId}.fileprovider`.

### CA-12 — Ruta mínima

**Dado** `file_paths.xml`,  
**cuando** se inspecciona,  
**entonces** solo expone la carpeta necesaria.

### CA-13 — ACTION_SEND

**Dado** un archivo preparado,  
**cuando** se inicia compartición,  
**entonces** se utiliza `Intent.ACTION_SEND`.

### CA-14 — MIME correcto

**Dado** el Intent,  
**cuando** se inspecciona,  
**entonces** usa `text/csv`.

### CA-15 — EXTRA_STREAM

**Dado** el Intent,  
**cuando** se inspecciona,  
**entonces** contiene la URI en `Intent.EXTRA_STREAM`.

### CA-16 — Permiso de lectura

**Dado** el Intent,  
**cuando** se envía,  
**entonces** contiene `FLAG_GRANT_READ_URI_PERMISSION`.

### CA-17 — Sin permiso de escritura

**Dado** el Intent,  
**cuando** se inspecciona,  
**entonces** no concede escritura sin necesidad.

### CA-18 — Chooser

**Dado** que existe al menos un receptor,  
**cuando** se comparte,  
**entonces** Android muestra un selector.

### CA-19 — Usuario elige receptor

**Dado** el selector,  
**cuando** aparece,  
**entonces** AlmacenTracker no fuerza una aplicación específica.

### CA-20 — Base vacía

**Dado** que Room no contiene registros,  
**cuando** se pulsa Compartir,  
**entonces** no se abre chooser y aparece un mensaje.

### CA-21 — Sin receptor

**Dado** que no existe aplicación compatible,  
**cuando** se intenta compartir,  
**entonces** se muestra un error controlado.

### CA-22 — Doble compartición bloqueada

**Dado** que se prepara un archivo,  
**cuando** el usuario vuelve a pulsar,  
**entonces** no se inicia otra operación.

### CA-23 — Operaciones mutuamente excluyentes

**Dado** que Exportar o Compartir está activo,  
**cuando** se pulsa la otra acción,  
**entonces** no se inicia simultáneamente.

### CA-24 — Error de escritura

**Dado** un fallo temporal,  
**cuando** se genera el archivo,  
**entonces** no se abre chooser y la aplicación no se cierra.

### CA-25 — Error de FileProvider

**Dado** un fallo al obtener URI,  
**cuando** ocurre,  
**entonces** no se usa una URI insegura como fallback.

### CA-26 — Archivo parcial

**Dado** un fallo durante escritura,  
**cuando** termina el control de error,  
**entonces** se intenta eliminar el archivo parcial.

### CA-27 — Limpieza controlada

**Dado** que existen temporales antiguos,  
**cuando** se prepara una nueva compartición,  
**entonces** se aplica la política definida.

### CA-28 — Archivo reciente conservado

**Dado** que el chooser acaba de abrirse,  
**cuando** vuelve el control a AlmacenTracker,  
**entonces** el archivo no se elimina inmediatamente.

### CA-29 — Rotación

**Dado** que se prepara la compartición,  
**cuando** el dispositivo rota,  
**entonces** la operación no se duplica.

### CA-30 — Evento único

**Dado** que el archivo está listo,  
**cuando** la Activity se recrea después de consumir el evento,  
**entonces** no se abre otro chooser.

### CA-31 — Offline

**Dado** que no existe conexión,  
**cuando** se comparte,  
**entonces** el archivo se prepara y el chooser local funciona.

### CA-32 — Sin permisos generales

**Dado** el Manifest,  
**cuando** se inspecciona,  
**entonces** no contiene permisos generales de almacenamiento.

### CA-33 — UI desacoplada

**Dado** el flujo,  
**cuando** se revisa `DataManagementActivity`,  
**entonces** no consulta Room ni serializa el CSV.

### CA-34 — Aplicación desacoplada de Android

**Dado** el servicio de compartición,  
**cuando** se inspecciona,  
**entonces** no importa `Intent`, `Uri`, `Context` ni `FileProvider`.

### CA-35 — Operación no bloqueante

**Dado** que se crea el archivo,  
**cuando** se ejecuta,  
**entonces** no bloquea el hilo principal.

---

## 30. Diseño técnico propuesto

### 30.1. Modelo compartible

Se añadirá una clase inmutable, preferentemente en `application.result` o un paquete de modelo de aplicación:

```text
ShareableCsvFile
```

Campos orientativos:

```text
contentReference
fileName
mimeType
recordCount
```

Validaciones:

- referencia no vacía;
- nombre no vacío;
- MIME no vacío;
- cantidad positiva.

### 30.2. Resultado

```text
ShareWarehouseItemsResult
```

Métodos orientativos:

```text
success(ShareableCsvFile)
emptyDatabase()
readError()
serializationError()
temporaryFileError()
fileProviderError()
unknownError()
```

### 30.3. Caso de uso

```text
ShareWarehouseItemsUseCase
```

### 30.4. Servicio

```text
ShareWarehouseItemsService
```

Dependencias:

```text
WarehouseItemRepository
WarehouseItemCsvShareFileGateway
Supplier<String> fileNameSupplier
```

La generación de nombre puede pertenecer al adaptador si la política se mantiene testeable. La decisión deberá evitar `LocalDateTime.now()` disperso.

### 30.5. Gateway temporal

```text
WarehouseItemCsvShareFileGateway
```

### 30.6. Implementación Android

```text
AndroidCsvShareFileGateway
```

Dependencias:

```text
Context applicationContext
WarehouseItemCsvCodec
Executor
String fileProviderAuthority
TemporaryFileCleanupPolicy
```

No es obligatorio extraer `TemporaryFileCleanupPolicy` si una clase separada no aporta claridad. La lógica sí deberá estar aislada y cubierta por pruebas.

### 30.7. ViewModel

`DataManagementViewModel` deberá ampliarse para:

- recibir `ShareWarehouseItemsUseCase`;
- exponer evento de archivo preparado;
- gestionar `PREPARING_SHARE`;
- bloquear operaciones simultáneas;
- transformar resultados en mensajes;
- volver a `IDLE` después de entregar el evento;
- manejar ausencia de receptor informada por la Activity;
- no construir Intent.

Métodos orientativos:

```java
public void shareWarehouseItems();

public void onShareChooserLaunched();

public void onNoShareApplicationAvailable();
```

### 30.8. Activity

`DataManagementActivity` deberá:

- configurar botón Compartir;
- observar evento `ShareableCsvFile`;
- convertir referencia en `Uri`;
- construir `ACTION_SEND`;
- añadir `EXTRA_STREAM`;
- añadir `ClipData`;
- conceder lectura temporal;
- comprobar receptor;
- abrir chooser;
- informar al ViewModel si no existe receptor;
- no generar archivo.

### 30.9. Factory

`DataManagementViewModelFactory` deberá recibir ambos casos de uso:

```text
ExportWarehouseItemsUseCase
ShareWarehouseItemsUseCase
```

### 30.10. AppContainer

`AppContainer` deberá:

- reutilizar `WarehouseItemCsvCodec`;
- construir `AndroidCsvShareFileGateway`;
- construir `ShareWarehouseItemsService`;
- añadirlo a `DataManagementViewModelFactory`;
- definir autoridad a partir de `BuildConfig.APPLICATION_ID` o una fuente equivalente segura;
- reutilizar o separar executor de archivos de forma coherente.

---

## 31. Estructura de archivos orientativa

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ShareWarehouseItemsUseCase.java
│   │   └── out/
│   │       └── WarehouseItemCsvShareFileGateway.java
│   ├── result/
│   │   ├── ShareWarehouseItemsResult.java
│   │   └── ShareableCsvFile.java
│   └── service/
│       └── ShareWarehouseItemsService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   └── DataManagementActivity.java
│   │       ├── state/
│   │       │   └── DataManagementUiState.java
│   │       └── viewmodel/
│   │           ├── DataManagementViewModel.java
│   │           └── DataManagementViewModelFactory.java
│   └── out/
│       └── file/
│           └── csv/
│               ├── WarehouseItemCsvCodec.java
│               ├── WarehouseItemCsvMapper.java
│               └── AndroidCsvShareFileGateway.java
└── configuration/
    └── AppContainer.java

res/
├── layout/
│   └── activity_data_management.xml
├── values/
│   └── strings.xml
└── xml/
    └── file_paths.xml

AndroidManifest.xml
```

Los nombres finales deberán ajustarse a la implementación real.

No se crearán clases vacías o duplicadas únicamente para seguir el diagrama.

---

## 32. Recursos de texto recomendados

```text
share_csv_title
share_csv_description
share_csv_action
share_csv_preparing
share_csv_empty
share_csv_read_error
share_csv_serialization_error
share_csv_temp_file_error
share_csv_file_provider_error
share_csv_no_receiver
share_csv_unknown_error
share_csv_chooser_title
share_csv_subject
```

No se mostrará un mensaje “Archivo compartido correctamente” porque Android no garantiza que el usuario complete el envío después de abrir el chooser.

Mensaje correcto antes del chooser, si se necesita feedback:

```text
Archivo preparado para compartir.
```

Aun así, puede omitirse porque el chooser ya comunica el siguiente paso.

---

## 33. Pruebas recomendadas

### 33.1. ShareWarehouseItemsServiceTest

- lista válida;
- base vacía;
- error de lectura;
- invoca gateway una vez;
- no invoca gateway con lista vacía;
- devuelve archivo compartible;
- transforma serialización;
- transforma error temporal;
- transforma FileProvider;
- transforma error desconocido;
- conserva cantidad;
- no depende de Android.

### 33.2. AndroidCsvShareFileGatewayTest

- crea subdirectorio;
- genera nombre correcto;
- escribe bytes del codec;
- usa archivo `.csv`;
- obtiene URI content;
- autoridad correcta;
- fallo de directorio;
- fallo de escritura;
- fallo de codec;
- fallo de provider;
- elimina parcial;
- limpia archivos antiguos;
- conserva recientes;
- limita cantidad;
- no expone otras rutas;
- ejecuta en executor.

Parte de estas pruebas puede requerir Robolectric o instrumentación según las dependencias actuales. No se añadirá Robolectric solo por comodidad si aumenta excesivamente el proyecto; en ese caso se separará la lógica pura de archivos y se instrumentará la integración Android mínima.

### 33.3. TemporaryFileCleanupTest

Si se extrae componente:

- elimina >24 horas;
- conserva <24 horas;
- máximo 10;
- orden por modificación;
- ignora directorios;
- fallo de borrado no detiene;
- directorio inexistente.

### 33.4. DataManagementViewModelTest

- IDLE inicial;
- share inicia preparación;
- bloquea segundo share;
- bloquea export mientras comparte;
- bloquea share mientras exporta;
- success emite evento único;
- empty contextual;
- read error;
- serialization error;
- temp error;
- provider error;
- vuelve a IDLE;
- ausencia de receptor;
- rotación;
- no abre chooser.

### 33.5. DataManagementActivityTest

- muestra Compartir;
- pulsación delega al ViewModel;
- crea ACTION_SEND;
- MIME text/csv;
- EXTRA_STREAM;
- URI content;
- permiso de lectura;
- sin permiso de escritura;
- ClipData;
- chooser;
- receptor disponible;
- receptor ausente;
- progreso;
- botones bloqueados;
- evento único;
- rotación;
- Activity sin Room;
- Activity sin codec.

### 33.6. ManifestTest

- provider presente;
- authority correcta;
- exported false;
- grantUriPermissions true;
- metadata correcta;
- sin permisos de almacenamiento.

### 33.7. FileProvider instrumentado

- crear archivo real en cache;
- obtener URI;
- abrir URI con `ContentResolver`;
- leer contenido;
- verificar encabezado;
- verificar Unicode;
- verificar que una ruta fuera de `shared_csv` no se expone;
- verificar esquema content.

### 33.8. Pruebas manuales

- compartir un registro;
- compartir varios;
- observaciones vacías;
- posición vacía;
- comas;
- comillas;
- saltos de línea;
- Unicode;
- fórmula protegida;
- Gmail u otra aplicación instalada;
- app de mensajería instalada;
- guardar mediante app receptora compatible;
- cancelar chooser;
- rotar durante preparación;
- doble pulsación;
- base vacía;
- modo avión;
- repetir 11 veces y revisar limpieza;
- archivo antiguo simulado;
- comprobar que la URI no muestra ruta privada.

---

## 34. Tareas de implementación

1. Confirmar HU-12 integrada.
2. Verificar CI de `develop`.
3. Analizar `AlmacenTrackerHU12.zip`.
4. Crear `feature/hu-13-compartir-exportacion-csv`.
5. Confirmar reutilización del formato HU-12.
6. Definir nombre temporal.
7. Definir política de limpieza.
8. Crear `ShareableCsvFile`.
9. Crear `ShareWarehouseItemsResult`.
10. Crear `ShareWarehouseItemsUseCase`.
11. Crear `WarehouseItemCsvShareFileGateway`.
12. Implementar `ShareWarehouseItemsService`.
13. Implementar `AndroidCsvShareFileGateway`.
14. Reutilizar `WarehouseItemCsvCodec`.
15. Añadir `file_paths.xml`.
16. Añadir `FileProvider` al Manifest.
17. Verificar autoridad.
18. Limitar ruta a `shared_csv/`.
19. Ampliar `DataManagementUiState`.
20. Ampliar `DataManagementViewModel`.
21. Ampliar Factory.
22. Actualizar `AppContainer`.
23. Añadir acción Compartir al layout.
24. Añadir strings.
25. Añadir evento compartible.
26. Construir `ACTION_SEND` en Activity.
27. Añadir MIME.
28. Añadir `EXTRA_STREAM`.
29. Añadir permiso de lectura.
30. Añadir ClipData.
31. Comprobar receptor.
32. Abrir chooser.
33. Controlar ausencia de receptor.
34. Bloquear operaciones simultáneas.
35. Controlar rotación.
36. Controlar evento único.
37. Probar limpieza temporal.
38. Crear pruebas de servicio.
39. Crear pruebas de gateway.
40. Crear pruebas de ViewModel.
41. Crear pruebas de Activity.
42. Crear prueba de Manifest.
43. Crear prueba instrumentada de FileProvider.
44. Ejecutar `./gradlew testDebugUnitTest`.
45. Ejecutar `./gradlew lintDebug`.
46. Ejecutar `./gradlew assembleDebug`.
47. Ejecutar `./gradlew connectedDebugAndroidTest`.
48. Publicar commits con `#15`.
49. Verificar CI de rama.
50. Recopilar evidencias.
51. Revisar criterios.
52. Fusionar localmente en `develop`.
53. Verificar CI de `develop`.
54. Eliminar rama local y remota tras integración.

---

## 35. Estrategia de commits orientativa

### Bloque 1 — Caso de uso y resultado

```text
git commit -m "feat: add warehouse item CSV sharing use case #15"
```

### Bloque 2 — Archivo temporal y FileProvider

```text
git commit -m "feat: add secure temporary CSV share provider #15"
```

### Bloque 3 — Integración de Gestión de datos

```text
git commit -m "feat: add CSV sharing flow to data management #15"
```

### Bloque 4 — Limpieza de temporales

```text
git commit -m "feat: add temporary CSV cleanup policy #15"
```

### Bloque 5 — Pruebas

```text
git commit -m "test: cover warehouse item CSV sharing flow #15"
```

---

## 36. Evidencias necesarias para cerrar HU-13

- acción Compartir visible;
- progreso de preparación;
- todos los registros incluidos;
- formato idéntico a HU-12;
- archivo en caché privada;
- subdirectorio `shared_csv`;
- Manifest con FileProvider;
- authority correcta;
- provider no exportado;
- ruta limitada;
- URI `content://`;
- ausencia de `file://`;
- ACTION_SEND;
- MIME `text/csv`;
- EXTRA_STREAM;
- permiso temporal de lectura;
- ausencia de permiso de escritura;
- ClipData;
- chooser;
- receptor seleccionado por usuario;
- base vacía;
- ausencia de receptor;
- error de lectura;
- error temporal;
- error de provider;
- doble pulsación bloqueada;
- Exportar bloqueado durante share;
- Compartir bloqueado durante exportación;
- rotación;
- evento único;
- modo avión;
- archivo parcial eliminado;
- limpieza de archivos >24 horas;
- límite de temporales;
- archivo reciente conservado;
- Manifest sin permisos generales;
- Activity sin DAO;
- Activity sin serialización;
- servicio sin Android;
- pruebas unitarias;
- pruebas instrumentadas;
- lint;
- assembleDebug;
- connectedDebugAndroidTest;
- CI satisfactoria en rama;
- merge en `develop`;
- CI satisfactoria en `develop`.

---

## 37. Definición de terminado

HU-13 estará terminada cuando:

- exista Compartir CSV en Gestión de datos;
- se genere una instantánea nueva de Room;
- se compartan todos los registros;
- búsqueda y filtros no limiten el archivo;
- se reutilice el codec de HU-12;
- no exista un segundo formato paralelo;
- el archivo sea temporal;
- se almacene en caché privada;
- se use un subdirectorio limitado;
- exista FileProvider;
- el provider no sea exportado;
- la authority derive del applicationId;
- se use URI content;
- no se use file URI;
- se use ACTION_SEND;
- se use text/csv;
- se use EXTRA_STREAM;
- se conceda lectura temporal;
- no se conceda escritura;
- se use chooser;
- el usuario elija receptor;
- se controle ausencia de receptor;
- se controle base vacía;
- se controlen errores de lectura;
- se controlen errores de serialización;
- se controlen errores temporales;
- se controle error de provider;
- no se elimine inmediatamente el archivo activo;
- se limpien temporales antiguos;
- se limite acumulación;
- se eliminen parciales fallidos;
- se bloqueen operaciones simultáneas;
- rotación no duplique;
- eventos no se repitan;
- funcione sin conexión;
- no se soliciten permisos generales;
- la Activity no consulte Room;
- la Activity no serialice CSV;
- el servicio no dependa de Android;
- las pruebas finalicen correctamente;
- lint no tenga errores;
- `assembleDebug` finalice correctamente;
- pruebas instrumentadas necesarias finalicen correctamente;
- CI de rama sea satisfactoria;
- criterios y evidencias estén completos;
- la rama se fusione en `develop`;
- CI de `develop` sea satisfactoria;
- la rama se elimine tras integración.

---

## 38. Resultado esperado

Al cerrar HU-13, AlmacenTracker podrá compartir de forma segura una exportación actual de mercancía:

```text
Room
  ↓ lectura puntual
WarehouseItem
  ↓ codec HU-12
CSV temporal UTF-8
  ↓ FileProvider
URI content:// con lectura temporal
  ↓ ACTION_SEND + chooser
aplicación elegida por el usuario
```

La aplicación quedará preparada para continuar con:

```text
HU-14 — Importar mercancía desde CSV
```

HU-14 reutilizará:

- encabezado oficial;
- reglas de escape;
- política de protección de fórmulas;
- definición de campos;
- Room como fuente de verdad.

HU-14 deberá añadir lectura y análisis CSV, pero no deberá modificar el flujo seguro de compartición cerrado en HU-13.

---

## 39. Commit documental recomendado

Este documento se conservará en “Fuentes” y no se añadirá al repositorio público.

```text
Sin commit en Git: guardar HU-13-compartir-exportacion-csv.md en Fuentes.
```
