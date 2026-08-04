# HU-19 — Buscar mercancía mediante un código escaneado

> Segunda historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-19  
**Nombre:** Buscar mercancía mediante un código escaneado  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-19-buscar-codigo-escaneado`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-18 — Escanear códigos de barras y códigos QR  
**Issue prevista:** `#22`

---

## 2. Historia de usuario

Como usuario,  
quiero buscar mercancía mediante un código escaneado,  
para consultar rápidamente dónde se encuentra sin escribir el código manualmente.

---

## 3. Objetivo

Conectar el resultado individual generado por `ScannerActivity` con la información almacenada en Room.

Flujo previsto:

```text
MainActivity
    ↓ acción Escanear
ScannerActivity
    ↓ resultado textual
WarehouseItemListViewModel
    ↓ búsqueda exacta por código
WarehouseItemRepository
    ↓
RoomWarehouseItemRepository
    ↓
WarehouseItemDao
    ↓
Room
```

La respuesta dependerá del número de coincidencias:

```text
1 coincidencia
    → abrir ItemDetailActivity

varias coincidencias
    → mostrar opciones para elegir categoría

0 coincidencias
    → informar y ofrecer acciones de continuación
```

La HU-19 reemplazará la presentación informativa temporal de HU-18 por una búsqueda funcional.

---

## 4. Documentos y código de referencia

La HU-19 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-18-escanear-codigos-barras-qr.md`;
- el estado real de `AlmacenTrackerHU18.zip`;
- la arquitectura MVVM por funcionalidades;
- Room como fuente de verdad;
- la identidad funcional `categoría + código`;
- el funcionamiento completamente sin conexión;
- la política de no crear clases o capas sin responsabilidad real.

HU-18 proporciona:

```text
ScannerActivity
ScannerViewModel
ScannerUiState
ScannedCode
ScannedCodeFormat
MlKitCodeScanner
```

y devuelve mediante Activity Result API:

```text
EXTRA_SCANNED_VALUE
EXTRA_SCANNED_FORMAT
```

La HU-19 reutilizará ese contrato. No modificará la cámara ni el reconocimiento de códigos salvo una corrección imprescindible.

---

## 5. Estado del proyecto antes de HU-19

El proyecto ya dispone de:

- acción de escaneo en `MainActivity`;
- `ActivityResultLauncher<Intent>`;
- extracción del valor mediante `ScannerActivity.getScannedValue(...)`;
- extracción del formato mediante `ScannerActivity.getScannedFormat(...)`;
- diálogo temporal que muestra valor y formato;
- `WarehouseItemListViewModel`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao`;
- navegación existente a `ItemDetailActivity` mediante id;
- `WarehouseItemNormalizer`;
- `UiEvent`;
- operaciones asíncronas mediante `RepositoryCallback`;
- pruebas de Room y ViewModel;
- funcionamiento offline.

El repositorio todavía no ofrece una operación exacta y puntual para recuperar todas las mercancías que comparten un código.

La consulta textual existente:

```text
search(query)
```

no debe reutilizarse como sustituto porque:

- realiza coincidencia parcial;
- busca también categoría, sitio y posición;
- es observable y está diseñada para el listado normal;
- podría devolver mercancía que no corresponde exactamente al código escaneado.

HU-19 deberá añadir una operación específica y exacta.

---

## 6. Alcance incluido

HU-19 incluye:

- recibir el resultado válido de `ScannerActivity`;
- delegar la búsqueda al `WarehouseItemListViewModel`;
- normalizar el código escaneado con la regla vigente;
- consultar Room por coincidencia exacta de código;
- comparar sin distinguir mayúsculas y minúsculas;
- conservar ceros iniciales;
- no convertir el código a número;
- recuperar todas las categorías asociadas al código;
- ordenar las coincidencias por categoría y código;
- abrir directamente el detalle cuando exista una coincidencia;
- mostrar una selección cuando existan varias coincidencias;
- mostrar categoría, código, sitio y posición en cada coincidencia;
- ocultar la posición cuando no exista;
- informar cuando no exista ninguna coincidencia;
- permitir volver a escanear;
- permitir cerrar el resultado;
- permitir acceder al formulario de registro existente sin rellenar todavía el código;
- controlar valor nulo o vacío;
- controlar errores de Room;
- impedir búsquedas simultáneas del mismo resultado;
- evitar repetir navegación tras rotación;
- mantener búsqueda y filtros actuales sin cambios;
- no modificar Room;
- no alterar el formato escaneado;
- funcionar completamente sin conexión;
- pruebas unitarias;
- pruebas DAO;
- pruebas de repositorio;
- pruebas de ViewModel;
- pruebas instrumentadas necesarias;
- CI.

---

## 7. Alcance excluido

HU-19 no incluye:

- rellenar automáticamente el formulario de creación;
- transmitir el código escaneado al modo CREATE;
- reemplazar el código durante edición;
- validar duplicados para guardar;
- crear mercancía automáticamente;
- editar mercancía;
- modificar sitio o posición;
- mostrar resultados parciales;
- buscar por sitio o posición a partir del escaneo;
- interpretar una referencia completa de una lista;
- separar categoría y código mediante OCR;
- capturar fotografías;
- seleccionar imágenes;
- reconocer varias referencias;
- crear historial de escaneos;
- guardar fecha u hora del escaneo;
- gestionar stock;
- descontar cantidades;
- abrir URLs contenidas en QR;
- realizar consultas remotas.

El relleno del formulario pertenece a HU-20.

La sustitución durante edición pertenece a HU-21.

La captura y reconocimiento de listas pertenece a HU-23, HU-24 y HU-25.

---

## 8. Regla principal de búsqueda

La búsqueda de HU-19 será exacta sobre el campo:

```text
WarehouseItem.code
```

Consulta conceptual:

```sql
SELECT *
FROM warehouse_items
WHERE code = :normalizedCode COLLATE NOCASE
ORDER BY category COLLATE NOCASE ASC,
         code COLLATE NOCASE ASC
```

No se utilizará:

```text
LIKE '%valor%'
```

porque un escaneo representa un código completo.

Ejemplo:

```text
Código escaneado: 1050
```

Coincide con:

```text
MR + 1050
MD + 1050
```

No coincide con:

```text
MR + 10501
MR + A1050
MR + 21050
```

---

## 9. Relación con la identidad funcional

La identidad funcional continúa siendo:

```text
categoría + código
```

El escáner individual entrega un valor textual, pero no garantiza que incluya la categoría de AlmacenTracker.

Por tanto, un código puede devolver:

```text
0 coincidencias
1 coincidencia
varias coincidencias
```

Ejemplo:

```text
MR + 1050
MD + 1050
```

Al escanear:

```text
1050
```

la aplicación no deberá elegir automáticamente una categoría.

El usuario deberá seleccionar la mercancía correcta.

---

## 10. Normalización del código

Antes de consultar Room:

```text
null → inválido
trim()
uppercase con Locale.ROOT
vacío → inválido
```

Se reutilizará:

```java
WarehouseItemNormalizer.normalizeCode(...)
```

o la misma regla centralizada vigente.

No deberá duplicarse la normalización dentro de `MainActivity`.

Ejemplos:

```text
" 1050 " → "1050"
"mr-10a" → "MR-10A"
"001050" → "001050"
```

Los ceros iniciales deberán conservarse.

---

## 11. Resultado de búsqueda

Se recomienda un resultado explícito:

```text
WarehouseItemCodeSearchResult
├── SINGLE_MATCH
├── MULTIPLE_MATCHES
├── NOT_FOUND
├── INVALID_CODE
└── ERROR
```

Datos mínimos:

```text
scannedCode
matches
errorMessage
```

### SINGLE_MATCH

Contiene exactamente una mercancía.

### MULTIPLE_MATCHES

Contiene dos o más mercancías con el mismo código y categorías diferentes.

### NOT_FOUND

Room no contiene ninguna coincidencia exacta.

### INVALID_CODE

El valor recibido es nulo, vacío o queda vacío después de normalizar.

### ERROR

La consulta falla de forma inesperada.

La colección de coincidencias deberá ser inmutable o defensivamente copiada.

---

## 12. Diseño técnico propuesto

### 12.1. DAO

Añadir una consulta puntual:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "WHERE code = :code COLLATE NOCASE " +
    "ORDER BY category COLLATE NOCASE ASC, " +
    "code COLLATE NOCASE ASC"
)
List<WarehouseItemEntity> findAllByCode(String code);
```

La consulta deberá ejecutarse fuera del hilo principal.

### 12.2. Repositorio

Ampliar `WarehouseItemRepository`:

```java
void findAllByCode(
        String code,
        RepositoryCallback<List<WarehouseItem>> callback
);
```

`RoomWarehouseItemRepository` deberá:

1. validar sus dependencias;
2. ejecutar la consulta en el executor existente;
3. mapear entidades a dominio;
4. devolver una lista vacía cuando no existan coincidencias;
5. transformar errores mediante `onError`.

Una lista vacía no deberá utilizar `onNotFound()`, porque la operación devuelve una colección válida sin elementos.

### 12.3. Servicio de búsqueda

Se recomienda crear:

```text
WarehouseItemCodeSearchService
```

Responsabilidades:

- normalizar el código;
- rechazar valores vacíos;
- solicitar coincidencias exactas;
- clasificar cero, una o varias coincidencias;
- devolver `WarehouseItemCodeSearchResult`.

Este servicio representa una operación real y evita colocar normalización y clasificación dentro de la Activity.

No es necesario crear una interfaz de caso de uso adicional si el proyecto actual no la requiere para mantener sus límites.

### 12.4. ViewModel

`WarehouseItemListViewModel` deberá:

- recibir el valor escaneado;
- impedir búsquedas duplicadas mientras una está activa;
- delegar al servicio;
- exponer el resultado mediante un evento de una sola consumición;
- no navegar;
- no construir diálogos;
- no depender de `Intent`;
- no depender de `ScannerActivity`.

Firma orientativa:

```java
public void searchScannedCode(String scannedCode);
```

Evento orientativo:

```text
LiveData<UiEvent<WarehouseItemCodeSearchResult>>
```

### 12.5. Factory y composición

`WarehouseItemListViewModelFactory` recibirá el nuevo servicio.

`InventoryModule` construirá:

```text
WarehouseItemCodeSearchService
```

utilizando el repositorio existente.

No se creará un módulo separado para una única operación.

### 12.6. Activity

`MainActivity` deberá:

1. recibir el valor de `ScannerActivity`;
2. validar únicamente que exista un resultado utilizable;
3. llamar a `viewModel.searchScannedCode(...)`;
4. observar el evento;
5. navegar o mostrar la opción adecuada.

Se eliminará el diálogo temporal que muestra solamente valor y formato.

El formato detectado podrá mantenerse para diagnóstico o accesibilidad, pero no determinará la consulta Room.

---

## 13. Interfaz para una coincidencia

Cuando exista exactamente una coincidencia:

```text
resultado escaneado
    ↓
ItemDetailActivity.createIntent(context, id)
```

No se mostrará un diálogo intermedio.

El detalle existente mostrará:

- categoría;
- código;
- sitio;
- posición;
- observaciones;
- fechas;
- acciones disponibles.

La navegación se realizará mediante el id interno.

---

## 14. Interfaz para varias coincidencias

Cuando existan varias coincidencias, se mostrará una selección clara.

Título orientativo:

```text
Selecciona la mercancía
```

Descripción orientativa:

```text
El código 1050 pertenece a varias categorías.
```

Cada opción mostrará:

```text
MR · 1050
Sitio A1 · Nivel 2
```

o, sin posición:

```text
MD · 1050
Sitio B3
```

Al seleccionar una opción:

1. se cierra el selector;
2. se abre `ItemDetailActivity` con su id.

La implementación podrá utilizar:

- `MaterialAlertDialogBuilder`;
- un diálogo con adapter;
- un `BottomSheetDialog`.

No se creará una Activity nueva únicamente para mostrar pocas coincidencias, salvo que la cantidad o accesibilidad lo justifiquen durante la implementación.

---

## 15. Interfaz sin coincidencias

Cuando no exista mercancía con el código escaneado:

Título orientativo:

```text
Mercancía no encontrada
```

Mensaje:

```text
No existe mercancía con el código 1050.
```

Acciones:

```text
Volver a escanear
Registrar
Cerrar
```

### Volver a escanear

Abre nuevamente `ScannerActivity`.

### Registrar

Abre `ItemFormActivity` en modo CREATE mediante el flujo existente.

En HU-19 el formulario se abrirá sin rellenar automáticamente el código.

El prellenado pertenece a HU-20.

### Cerrar

Cierra el diálogo y conserva el listado, la búsqueda y los filtros actuales.

---

## 16. Interfaz de error

Si Room no puede completar la consulta:

```text
No se pudo buscar la mercancía.
```

Acciones:

```text
Reintentar
Cerrar
```

No se mostrará:

- excepción;
- stack trace;
- mensaje SQL;
- nombre de tabla.

El código escaneado podrá conservarse temporalmente para permitir reintentar mientras `MainActivity` y su ViewModel continúen activos.

---

## 17. Estado durante la consulta

La búsqueda exacta será breve y local, pero deberá impedir dobles acciones.

Durante la operación:

- se bloquearán nuevas búsquedas escaneadas;
- la acción de escaneo podrá deshabilitarse temporalmente;
- no se bloqueará todo el listado;
- búsqueda textual y filtros permanecerán visualmente estables;
- no se cambiará `WarehouseItemListUiState` a `LOADING`;
- no se reemplazará el contenido actual del RecyclerView.

La consulta escaneada es una acción puntual, no una nueva fuente observable del listado principal.

---

## 18. Conservación de búsqueda y filtros

La HU-19 no utilizará el campo de búsqueda existente para simular la consulta.

Después de:

- una coincidencia;
- varias coincidencias;
- ninguna coincidencia;
- error;
- cancelación;

deberán conservarse:

- texto de búsqueda;
- categoría seleccionada;
- sitio seleccionado;
- posición seleccionada;
- selección múltiple, salvo que el escáner no pueda abrirse durante ese modo.

La acción Escanear continuará deshabilitada o ignorada mientras exista selección múltiple.

---

## 19. Flujo principal — Una coincidencia

1. El usuario abre el listado.
2. Pulsa Escanear.
3. `ScannerActivity` detecta un código.
4. Devuelve `RESULT_OK`.
5. `MainActivity` extrae el valor.
6. Delega al ViewModel.
7. El servicio normaliza el código.
8. El repositorio consulta Room.
9. Room devuelve una mercancía.
10. El resultado es `SINGLE_MATCH`.
11. La Activity consume el evento.
12. Abre el detalle mediante id.
13. El usuario visualiza la ubicación.

---

## 20. Flujos alternativos

### FA-01 — Varias categorías

1. Existen `MR + 1050` y `MD + 1050`.
2. El usuario escanea `1050`.
3. Room devuelve dos coincidencias.
4. Se muestra la selección.
5. El usuario elige una categoría.
6. Se abre el detalle correcto.

### FA-02 — Código inexistente

1. El usuario escanea `9999`.
2. Room devuelve una lista vacía.
3. Se informa que no existe.
4. El usuario puede reintentar, registrar o cerrar.

### FA-03 — Resultado vacío

1. La Activity recibe un valor vacío.
2. No consulta Room.
3. Se muestra un error controlado o se ignora el resultado de forma coherente.
4. No se produce navegación.

### FA-04 — Código con espacios

1. Se recibe `" 1050 "`.
2. Se normaliza a `"1050"`.
3. Se ejecuta la consulta exacta.

### FA-05 — Código con ceros iniciales

1. Se recibe `"001050"`.
2. Se conserva como texto.
3. Solo coincide con el código exacto `"001050"`.

### FA-06 — Diferencia de mayúsculas

1. Room contiene `"AB10A"`.
2. Se escanea `"ab10a"`.
3. La normalización produce `"AB10A"`.
4. Se encuentra la mercancía.

### FA-07 — Coincidencia parcial no permitida

1. Room contiene `"10501"`.
2. Se escanea `"1050"`.
3. No se considera coincidencia.

### FA-08 — Error de Room

1. La consulta falla.
2. El repositorio devuelve error.
3. El ViewModel emite `ERROR`.
4. Se muestra una opción de reintento.
5. La aplicación no se cierra.

### FA-09 — Cancelación del escáner

1. El usuario cancela `ScannerActivity`.
2. `MainActivity` no inicia ninguna consulta.
3. El listado permanece igual.

### FA-10 — Rotación después del resultado

1. Se obtiene un resultado de búsqueda.
2. La Activity se recrea.
3. El evento ya consumido no se repite.
4. No se abre dos veces el detalle ni el diálogo.

### FA-11 — Modo selección activo

1. Existen registros seleccionados.
2. La acción de escaneo no inicia una nueva búsqueda.
3. La selección permanece estable.

---

## 21. Criterios de aceptación

### CA-01 — Búsqueda iniciada desde escaneo

**Dado** que `ScannerActivity` devuelve un código válido,  
**cuando** `MainActivity` recibe el resultado,  
**entonces** el ViewModel solicita una búsqueda exacta en Room.

### CA-02 — Coincidencia única

**Dado** que solo existe una mercancía con el código,  
**cuando** finaliza la consulta,  
**entonces** se abre su detalle mediante id.

### CA-03 — Varias coincidencias

**Dado** que el mismo código existe en categorías diferentes,  
**cuando** se escanea,  
**entonces** se muestran todas las coincidencias y no se elige una automáticamente.

### CA-04 — Información de selección

**Dado** que existen varias coincidencias,  
**cuando** se presentan,  
**entonces** cada opción muestra categoría, código, sitio y posición cuando exista.

### CA-05 — Sin coincidencias

**Dado** que Room no contiene el código,  
**cuando** finaliza la consulta,  
**entonces** se informa al usuario y se ofrecen acciones para continuar.

### CA-06 — Búsqueda exacta

**Dado** el código `1050`,  
**cuando** se consulta Room,  
**entonces** no se incluyen `10501`, `A1050` ni `21050`.

### CA-07 — Comparación sin mayúsculas

**Dado** un código almacenado en mayúsculas,  
**cuando** el escáner devuelve minúsculas,  
**entonces** se encuentra después de normalizar.

### CA-08 — Ceros iniciales

**Dado** el código `001050`,  
**cuando** se busca,  
**entonces** los ceros iniciales se conservan.

### CA-09 — Código vacío

**Dado** un valor nulo o vacío,  
**cuando** se procesa,  
**entonces** Room no se consulta y no se navega.

### CA-10 — Error controlado

**Dado** que Room produce un error,  
**cuando** la consulta falla,  
**entonces** se muestra un mensaje comprensible y puede reintentarse.

### CA-11 — Sin modificación de datos

**Dado** cualquier resultado de búsqueda,  
**cuando** HU-19 finaliza,  
**entonces** no se crea, actualiza ni elimina mercancía.

### CA-12 — Conservación de criterios

**Dado** que existen búsqueda o filtros activos,  
**cuando** se realiza un escaneo,  
**entonces** esos criterios no cambian.

### CA-13 — Evento único

**Dado** que el resultado se procesa,  
**cuando** la Activity se recrea,  
**entonces** no se repite la navegación ni el diálogo.

### CA-14 — Funcionamiento sin conexión

**Dado** que el dispositivo está en modo avión,  
**cuando** se escanea y busca un código,  
**entonces** la localización funciona mediante Room.

### CA-15 — Registro sin prellenado

**Dado** que no existe el código,  
**cuando** el usuario elige Registrar,  
**entonces** se abre el formulario CREATE sin adelantar el prellenado de HU-20.

---

## 22. Archivos previstos

La implementación deberá ajustarse al código real.

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── app/di/
│   └── InventoryModule.java
├── data/local/room/dao/
│   └── WarehouseItemDao.java
├── data/repository/
│   ├── WarehouseItemRepository.java
│   └── RoomWarehouseItemRepository.java
└── feature/inventory/list/
    ├── MainActivity.java
    ├── WarehouseItemListViewModel.java
    ├── WarehouseItemListViewModelFactory.java
    ├── WarehouseItemCodeSearchService.java
    └── WarehouseItemCodeSearchResult.java
```

Recursos probables:

```text
app/src/main/res/values/strings.xml
```

Solo deberá crearse un layout específico para las coincidencias si la solución elegida no puede presentarlas de forma clara y accesible con componentes existentes.

---

## 23. Pruebas

### 23.1. DAO

- devuelve una coincidencia exacta;
- devuelve varias categorías con el mismo código;
- devuelve lista vacía;
- ignora mayúsculas y minúsculas;
- conserva ceros iniciales;
- no devuelve coincidencias parciales;
- mantiene orden por categoría y código.

### 23.2. Repositorio

- mapea una lista correctamente;
- devuelve lista vacía como éxito;
- ejecuta fuera del hilo principal;
- transforma errores;
- no utiliza `onNotFound()` para colecciones vacías.

### 23.3. Servicio

- normaliza el código;
- rechaza nulo;
- rechaza vacío;
- clasifica una coincidencia;
- clasifica varias coincidencias;
- clasifica ninguna coincidencia;
- transforma error;
- conserva el código normalizado.

### 23.4. ViewModel

- inicia búsqueda desde resultado escaneado;
- bloquea una segunda búsqueda simultánea;
- emite `SINGLE_MATCH`;
- emite `MULTIPLE_MATCHES`;
- emite `NOT_FOUND`;
- emite `INVALID_CODE`;
- emite `ERROR`;
- libera el bloqueo al finalizar;
- no modifica el estado normal del listado;
- no repite el evento consumido.

### 23.5. Activity e instrumentación

- un resultado único abre el detalle correcto;
- varias coincidencias muestran selector;
- seleccionar una opción abre el id correcto;
- sin resultados muestra acciones;
- Reintentar abre el escáner;
- Registrar abre CREATE sin prellenado;
- cancelar escáner no consulta;
- rotación no repite navegación;
- criterios del listado permanecen;
- selección múltiple impide escanear.

### 23.6. Manuales

- código real con una coincidencia;
- código real repetido en varias categorías;
- código inexistente;
- código con ceros iniciales;
- código alfanumérico;
- mayúsculas y minúsculas;
- modo avión;
- búsqueda y filtros activos;
- rotación;
- varios escaneos consecutivos.

---

## 24. Tareas de implementación

1. Confirmar HU-18 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-19-buscar-codigo-escaneado`.
4. Revisar el contrato real de `ScannerActivity`.
5. Añadir `findAllByCode(...)` al DAO.
6. Añadir `findAllByCode(...)` al repositorio.
7. Implementar la consulta en `RoomWarehouseItemRepository`.
8. Crear el resultado de búsqueda.
9. Crear el servicio de búsqueda por código.
10. Reutilizar `WarehouseItemNormalizer`.
11. Ampliar `WarehouseItemListViewModel`.
12. Añadir evento de una sola consumición.
13. Ampliar Factory.
14. Actualizar `InventoryModule`.
15. Sustituir el diálogo temporal de HU-18.
16. Implementar navegación de coincidencia única.
17. Implementar selección de varias coincidencias.
18. Implementar estado sin coincidencias.
19. Implementar error y reintento.
20. Mantener el registro sin prellenado.
21. Añadir strings.
22. Ampliar pruebas DAO.
23. Crear pruebas del servicio.
24. Ampliar pruebas del repositorio.
25. Ampliar pruebas del ViewModel.
26. Añadir pruebas de Activity necesarias.
27. Ejecutar pruebas unitarias.
28. Ejecutar lint.
29. Ejecutar build debug.
30. Ejecutar pruebas instrumentadas.
31. Verificar funcionamiento offline.
32. Verificar todos los criterios de aceptación.
33. Integrar en `develop`.
34. Verificar CI de `develop`.
35. Eliminar la rama tras confirmar la integración.

---

## 25. Evidencias necesarias

- escaneo con coincidencia única;
- apertura del detalle correcto;
- escaneo con varias categorías;
- selector con ubicación de cada coincidencia;
- selección y apertura correcta;
- código inexistente;
- acción Volver a escanear;
- acción Registrar sin prellenado;
- código con ceros iniciales;
- rechazo de coincidencia parcial;
- error controlado;
- modo avión;
- criterios del listado conservados;
- rotación sin repetición;
- pruebas DAO;
- pruebas del servicio;
- pruebas del ViewModel;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 26. Definición de terminado

HU-19 estará terminada cuando:

- el resultado del escáner se delegue al ViewModel;
- el diálogo informativo temporal de HU-18 deje de ser el resultado final;
- exista una consulta exacta por código;
- la consulta no sea parcial;
- se normalice mediante la regla vigente;
- se conserven ceros iniciales;
- una coincidencia abra el detalle;
- varias coincidencias requieran selección;
- cada coincidencia muestre su ubicación;
- ninguna coincidencia se informe claramente;
- sea posible volver a escanear;
- sea posible abrir el registro manual sin prellenado;
- los errores sean controlados;
- la consulta no bloquee el hilo principal;
- el listado normal no cambie;
- búsqueda y filtros se conserven;
- Room no se modifique;
- el evento no se repita tras recreación;
- funcione sin conexión;
- las pruebas definidas finalicen correctamente;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 27. Validación técnica final

Ejecutar:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Con emulador o dispositivo:

```bash
./gradlew connectedDebugAndroidTest
```

Validación manual obligatoria:

- una coincidencia;
- varias coincidencias;
- ninguna coincidencia;
- código con ceros iniciales;
- modo avión;
- rotación;
- reintento.

---

## 28. Resultado esperado

Al cerrar HU-19:

```text
usuario escanea un código
        ↓
la aplicación normaliza el valor
        ↓
Room busca coincidencias exactas
        ↓
una coincidencia
    → detalle y ubicación

varias coincidencias
    → selección de categoría

ninguna coincidencia
    → opciones de continuación
```

La siguiente historia será:

```text
HU-20 — Registrar mercancía con un código escaneado
```
