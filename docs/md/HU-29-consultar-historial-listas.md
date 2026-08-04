# HU-29 — Consultar el historial de listas

> Cuarta historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-29  
**Nombre:** Consultar el historial de listas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-29-consultar-historial`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-28 — Registrar una lista confirmada en el historial  
**Issue prevista:** `#33`

---

## 2. Historia de usuario

Como usuario,  
quiero consultar las listas registradas en el historial,  
para revisar rápidamente qué documentos fueron guardados, cuándo se registraron y cuántas referencias contenían.

---

## 3. Objetivo

Crear el primer punto de consulta del historial documental persistido en Room.

Flujo previsto:

```text
MainActivity
        ↓ acción Historial
WithdrawalHistoryListActivity
        ↓
WithdrawalHistoryListViewModel
        ↓
WithdrawalHistoryListService
        ↓
WithdrawalHistoryRepository
        ↓
Room
        ↓
resúmenes ordenados por fecha descendente
```

Resultado visual orientativo:

```text
Reposición tienda centro
2 ago 2026 · 12:30
15 referencias · 13 encontradas · 2 no encontradas

Lista sin título
1 ago 2026 · 18:10
8 referencias · 8 encontradas
```

HU-29 deberá permitir consultar el conjunto de registros existentes sin cargar todavía el detalle de cada línea.

---

## 4. Regla principal

El listado mostrará un resumen de cada historial.

```text
listado
    → cabecera
    → fecha
    → contadores
```

No mostrará todavía:

```text
todas las líneas
cantidades individuales
unidades individuales
sitios y posiciones por referencia
```

Esos datos pertenecen al detalle de HU-30.

La consulta del listado no deberá cargar todas las líneas históricas para cada fila si Room puede calcular directamente los contadores necesarios.

---

## 5. Documentos y código de referencia

HU-29 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-28-registrar-lista-confirmada-historial.md`;
- el estado real de `AlmacenTrackerHU28.zip`;
- Room como fuente local de verdad;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- la separación entre mercadería e historial;
- el carácter documental del historial;
- el funcionamiento completamente sin conexión;
- el orden predeterminado por `registeredAt DESC, id DESC`;
- la política de crear componentes únicamente cuando aporten una responsabilidad real.

El plan de v1.3 asigna a HU-29:

```text
listado por fecha descendente
+
título
+
fecha y hora
+
número de referencias
+
resumen encontradas/no encontradas
+
estado vacío
+
actualización después de guardar o eliminar
```

---

## 6. Estado real antes de HU-29

El análisis de `AlmacenTrackerHU28.zip` confirma:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida es:

```text
develop
```

HU-28 ya está integrada mediante:

```text
merge HU28 #32 into develop
```

El historial dispone de:

```text
WithdrawalHistory
WithdrawalHistoryEntry
WithdrawalHistoryRecord
WithdrawalLocationStatus
WithdrawalHistoryRepository
RoomWithdrawalHistoryRepository
WithdrawalHistoryDao
WithdrawalHistoryRoomMapper
WithdrawalHistoryModule
WithdrawalHistorySaveService
```

La creación histórica ya permite:

- revisar título;
- revisar fecha documental;
- revisar cantidad y unidad;
- confirmar el guardado;
- guardar cabecera y líneas;
- utilizar una única transacción Room;
- bloquear dobles pulsaciones;
- conservar referencias `FOUND`;
- conservar referencias `NOT_FOUND`;
- devolver el id generado;
- finalizar con `RESULT_OK`;
- funcionar sin conexión.

El repositorio histórico actual ofrece únicamente:

```java
void insert(...)
void findById(...)
void deleteById(...)
```

El DAO actual ofrece:

```java
insertHistoryWithEntries(...)
findByIdWithEntries(...)
findEntriesByHistoryId(...)
deleteById(...)
countHistories()
countEntriesByHistoryId(...)
countAllEntries()
```

Antes de HU-29 no existen:

- consulta de todos los historiales;
- modelo de resumen;
- proyección Room para contadores;
- pantalla de listado histórico;
- ViewModel de listado;
- adapter histórico;
- acceso desde el menú principal;
- estado vacío del historial;
- reintento de carga;
- refresco del listado;
- contrato para abrir un registro histórico.

---

## 7. Alcance incluido

HU-29 incluye:

- añadir acceso al historial desde `MainActivity`;
- añadir una opción clara al menú principal;
- crear `WithdrawalHistoryListActivity`;
- declarar la Activity en el Manifest;
- crear un layout de listado;
- crear RecyclerView;
- crear adapter;
- crear modelo de resumen de dominio;
- crear proyección Room específica;
- consultar todos los historiales;
- calcular total de referencias;
- calcular referencias encontradas;
- calcular referencias no encontradas;
- ordenar por fecha documental descendente;
- desempatar por id descendente;
- mostrar título;
- mostrar texto sustituto cuando no exista título;
- mostrar fecha y hora local;
- mostrar total de referencias;
- mostrar resumen encontradas/no encontradas;
- mostrar estado de carga;
- mostrar estado vacío;
- mostrar error controlado;
- permitir reintentar;
- conservar el resultado ante rotación;
- evitar cargas duplicadas;
- actualizar al volver a primer plano cuando resulte necesario;
- preparar la selección de una fila para HU-30;
- no cargar el detalle completo;
- no modificar Room;
- no modificar mercadería;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas DAO;
- pruebas de mapper;
- pruebas de repositorio;
- pruebas del servicio;
- pruebas del ViewModel;
- pruebas del adapter;
- pruebas instrumentadas cuando aporten valor;
- CI.

---

## 8. Alcance excluido

HU-29 no incluye:

- mostrar líneas completas;
- mostrar cantidades por referencia;
- mostrar unidades por referencia;
- mostrar sitio y posición por referencia;
- abrir todavía una pantalla de detalle funcional;
- buscar por título;
- buscar por categoría;
- buscar por código;
- filtrar por fecha;
- combinar filtros;
- eliminar historiales;
- editar historiales;
- duplicar historiales;
- compartir historiales;
- exportar historiales;
- importar historiales;
- modificar backup CSV;
- estadísticas;
- agrupación por día, semana o mes;
- paginación;
- sincronización remota;
- autenticación;
- backend;
- gestión de stock.

El detalle corresponde a HU-30.

La búsqueda y filtros corresponden a HU-31.

La eliminación corresponde a HU-32.

---

## 9. Decisión crítica sobre la consulta

No se utilizará el siguiente enfoque:

```text
obtener ids
        ↓
findById() por cada historial
        ↓
cargar todas las líneas
        ↓
calcular contadores en la UI
```

Ese enfoque provocaría:

- múltiples consultas;
- carga innecesaria de líneas;
- mayor complejidad asíncrona;
- coordinación de callbacks;
- trabajo que Room puede resolver directamente;
- peor escalabilidad.

HU-29 deberá usar una consulta de resumen.

---

## 10. Modelo de resumen

Se añadirá un modelo Java puro:

```text
WithdrawalHistorySummary
```

Datos previstos:

```text
id
title
registeredAt
createdAt
updatedAt
entryCount
foundCount
notFoundCount
```

Responsabilidades:

- representar una fila del listado;
- no contener líneas históricas;
- no depender de Android;
- no depender de Room;
- validar contadores;
- permitir título nulo;
- mantener timestamps;
- facilitar pruebas.

Invariantes:

```text
id > 0
registeredAt > 0
createdAt > 0
updatedAt > 0
entryCount >= 0
foundCount >= 0
notFoundCount >= 0
foundCount + notFoundCount == entryCount
```

Si Room devuelve datos incoherentes, el mapper o repositorio deberá tratarlo como error técnico.

---

## 11. Proyección Room

Se añadirá un POJO de persistencia:

```text
WithdrawalHistorySummaryRow
```

Ubicación recomendada:

```text
data/local/room/projection/
```

Campos:

```text
id
title
registeredAt
createdAt
updatedAt
entryCount
foundCount
notFoundCount
```

La proyección:

- no será `@Entity`;
- no generará una tabla;
- representará el resultado de una consulta;
- no se expondrá a la feature;
- se mapeará a `WithdrawalHistorySummary`.

No se reutilizará `WithdrawalHistoryWithEntries`, porque contiene las líneas completas y está orientado al detalle.

---

## 12. Consulta Room

Consulta conceptual:

```sql
SELECT
    history.id AS id,
    history.title AS title,
    history.registered_at AS registered_at,
    history.created_at AS created_at,
    history.updated_at AS updated_at,
    COUNT(entry.id) AS entry_count,
    COALESCE(
        SUM(
            CASE
                WHEN entry.location_status = 'FOUND'
                THEN 1
                ELSE 0
            END
        ),
        0
    ) AS found_count,
    COALESCE(
        SUM(
            CASE
                WHEN entry.location_status = 'NOT_FOUND'
                THEN 1
                ELSE 0
            END
        ),
        0
    ) AS not_found_count
FROM withdrawal_history AS history
LEFT JOIN withdrawal_history_entries AS entry
    ON entry.history_id = history.id
GROUP BY
    history.id,
    history.title,
    history.registered_at,
    history.created_at,
    history.updated_at
ORDER BY
    history.registered_at DESC,
    history.id DESC
```

Firma orientativa:

```java
@Query("...")
List<WithdrawalHistorySummaryRow>
findAllSummaries();
```

La consulta se ejecutará en el executor del repositorio.

---

## 13. Motivo del `LEFT JOIN`

Se utilizará:

```text
LEFT JOIN
```

y no:

```text
INNER JOIN
```

porque la cabecera debe poder aparecer aunque exista un dato histórico anómalo sin líneas.

Sin embargo, el flujo normal de HU-28 siempre guarda al menos una línea.

Una cabecera sin líneas deberá mostrarse de forma defensiva como:

```text
0 referencias
```

y podrá investigarse mediante pruebas o mantenimiento.

HU-29 no eliminará automáticamente datos anómalos.

---

## 14. Conteo de estados

Los contadores se calcularán mediante:

```text
location_status = FOUND
location_status = NOT_FOUND
```

No se inferirá el estado a partir de:

- sitio nulo;
- id nulo;
- posición nula;
- cantidad;
- unidad.

El campo persistido `location_status` es la fuente de verdad histórica.

La posición puede ser nula incluso en una referencia encontrada.

---

## 15. Mapper de resumen

Se añadirá un mapper:

```text
WithdrawalHistorySummaryRoomMapper
```

o se ampliará `WithdrawalHistoryRoomMapper` únicamente si mantiene una responsabilidad clara.

Responsabilidades:

- transformar `WithdrawalHistorySummaryRow`;
- construir `WithdrawalHistorySummary`;
- copiar defensivamente listas;
- rechazar filas nulas;
- no formatear fechas;
- no crear textos de interfaz;
- no acceder a recursos Android.

Decisión recomendada:

```text
ampliar WithdrawalHistoryRoomMapper
```

si el método de resumen continúa siendo pequeño y coherente con el agregado histórico.

No se creará un mapper separado solo por simetría.

---

## 16. Repositorio

Se ampliará:

```java
WithdrawalHistoryRepository
```

con:

```java
void findAllSummaries(
        RepositoryCallback<
                List<WithdrawalHistorySummary>
        > callback
);
```

`RoomWithdrawalHistoryRepository` deberá:

1. validar el callback;
2. ejecutar una única tarea en el executor existente;
3. llamar a `dao.findAllSummaries()`;
4. mapear todas las filas;
5. devolver una lista vacía si no hay registros;
6. devolver una copia defensiva;
7. ejecutar un único `onSuccess`;
8. transformar excepciones mediante `onError`.

Una lista vacía será un resultado válido.

No se utilizará:

```text
callback.onNotFound()
```

para representar historial vacío.

---

## 17. Servicio de listado

Se recomienda crear:

```text
WithdrawalHistoryListService
```

Ubicación:

```text
feature/withdrawal_history/list/
```

Responsabilidades:

- solicitar resúmenes al repositorio;
- conservar el orden recibido;
- validar el resultado;
- devolver una lista inmutable;
- transformar errores técnicos en un resultado de aplicación;
- no depender de Android;
- no formatear fechas;
- no navegar;
- no construir textos visuales.

El servicio representa una operación real:

```text
consultar el listado histórico
```

No deberá limitarse a delegar sin aportar ninguna regla.

Si durante la implementación se comprueba que no añade valor sobre el repositorio, podrá omitirse y delegar directamente desde el ViewModel.

La decisión deberá basarse en el código real, no en mantener una capa formal.

---

## 18. Pantalla de listado

Nombre:

```text
WithdrawalHistoryListActivity
```

La pantalla deberá incluir:

- Toolbar;
- título Historial;
- RecyclerView;
- progreso inicial;
- estado vacío;
- estado de error;
- acción Reintentar;
- navegación Atrás;
- contenido accesible.

No deberá mezclar el historial con el listado principal de mercadería.

---

## 19. Acceso desde `MainActivity`

Se añadirá una opción:

```text
Historial
```

al menú principal.

Orden recomendado:

```text
Escanear código
Procesar lista
Historial
Gestión de datos
```

La opción abrirá:

```text
WithdrawalHistoryListActivity
```

El acceso podrá mantenerse disponible durante el modo normal del listado.

Durante selección múltiple deberá seguir la misma regla de disponibilidad que las demás acciones incompatibles.

No se utilizará el botón flotante de registro de mercadería para abrir historial.

---

## 20. Icono y texto

Se utilizará un icono comprensible, por ejemplo:

```text
history
receipt_long
list_alt
```

La decisión visual deberá mantenerse coherente con Material Components.

Se añadirá:

```text
withdrawal_history_action
withdrawal_history_list_title
```

y las descripciones necesarias.

El significado no deberá depender solo del icono.

---

## 21. Fila del listado

Layout recomendado:

```text
item_withdrawal_history_summary.xml
```

Cada fila mostrará:

### Título

Con título:

```text
Reposición tienda centro
```

Sin título:

```text
Lista sin título
```

### Fecha

```text
2 ago 2026 · 12:30
```

### Resumen

Con encontradas y no encontradas:

```text
15 referencias · 13 encontradas · 2 no encontradas
```

Todas encontradas:

```text
8 referencias · 8 encontradas
```

Todas no encontradas:

```text
3 referencias · 3 no encontradas
```

Una referencia:

```text
1 referencia · 1 encontrada
```

La pluralización deberá resolverse mediante recursos Android.

---

## 22. Título derivado

El plan general permite mostrar un título derivado cuando no exista título.

Para HU-29 se recomienda utilizar:

```text
Lista sin título
```

como texto principal y mostrar la fecha en su línea propia.

Motivo:

- evita duplicar la fecha;
- mantiene filas uniformes;
- facilita accesibilidad;
- no altera el valor persistido;
- HU-30 podrá adoptar la misma convención.

No se guardará el texto sustituto en Room.

---

## 23. Formato de fecha

La fecha visible utilizará:

```text
registeredAt
```

No utilizará:

```text
createdAt
```

como fecha principal.

Motivo:

- `registeredAt` representa el momento documental;
- el usuario pudo ajustar la fecha;
- `createdAt` representa únicamente cuándo se insertó en Room.

El formato utilizará la zona horaria y locale del dispositivo.

La Activity o un formatter de presentación podrá usar:

```text
java.time
```

porque el proyecto utiliza `minSdk 26`.

No se almacenarán textos formateados.

---

## 24. Orden del listado

Orden obligatorio:

```text
registeredAt DESC
id DESC
```

Ejemplo:

```text
id 9 · 2 ago 2026 12:00
id 7 · 2 ago 2026 12:00
id 5 · 1 ago 2026 18:00
```

El desempate por id evita cambios aleatorios cuando dos registros comparten fecha documental.

El adapter no reordenará la colección.

El ViewModel tampoco reordenará si Room ya garantiza el orden.

---

## 25. Estado de interfaz

Se añadirá:

```text
WithdrawalHistoryListUiState
```

Estados:

```text
LOADING
CONTENT
EMPTY
ERROR
```

### `LOADING`

- progreso visible;
- RecyclerView oculto;
- estado vacío oculto;
- error oculto.

### `CONTENT`

- RecyclerView visible;
- colección no vacía;
- progreso oculto;
- error oculto.

### `EMPTY`

- mensaje visible;
- RecyclerView vacío;
- progreso oculto;
- error oculto.

### `ERROR`

- mensaje controlado;
- acción Reintentar;
- contenido anterior podrá conservarse si existía;
- no se mostrará excepción.

No se añadirá `NO_RESULTS` porque HU-29 todavía no tiene búsqueda ni filtros.

---

## 26. Estado vacío

Mensaje orientativo:

```text
Todavía no hay listas guardadas.
```

Descripción opcional:

```text
Procesa una lista y guárdala para verla aquí.
```

Acción opcional:

```text
Procesar lista
```

La acción podrá abrir `ReferenceListCaptureActivity`, pero no es obligatoria para cumplir HU-29.

Si se incluye:

- deberá reutilizar el flujo existente;
- no creará una ruta alternativa;
- al volver, el listado deberá recargarse.

---

## 27. ViewModel

Se creará:

```text
WithdrawalHistoryListViewModel
```

Responsabilidades:

- iniciar la carga;
- impedir cargas simultáneas;
- delegar al servicio o repositorio;
- publicar `LOADING`;
- publicar `CONTENT`;
- publicar `EMPTY`;
- publicar `ERROR`;
- conservar la última colección;
- permitir reintento;
- no navegar;
- no formatear recursos;
- no depender de `Context`;
- no conocer DAO;
- no conocer entidades Room.

Métodos orientativos:

```java
public void load();
public void refresh();
```

`load()` podrá ejecutarse una sola vez al crear el ViewModel.

`refresh()` deberá permitir una nueva consulta cuando la pantalla vuelva al primer plano.

---

## 28. Factory y módulo

Se creará:

```text
WithdrawalHistoryListViewModelFactory
```

`WithdrawalHistoryModule` se ampliará para proporcionar:

```text
WithdrawalHistoryListService
WithdrawalHistoryListViewModelFactory
```

`AppContainer` expondrá:

```java
provideWithdrawalHistoryListViewModelFactory()
```

No se deberá construir el repositorio desde la Activity.

No se creará un segundo repositorio histórico.

---

## 29. Adapter

Se creará:

```text
WithdrawalHistoryListAdapter
```

Responsabilidades:

- renderizar resúmenes;
- mostrar título;
- mostrar fecha;
- mostrar contadores;
- emitir el id seleccionado;
- usar ids estables cuando aporte valor;
- no consultar Room;
- no formatear datos de dominio complejos;
- no navegar.

Callback orientativo:

```java
OnHistoryClickListener.onHistoryClick(long historyId)
```

En HU-29 la pulsación podrá:

- quedar preparada sin navegar; o
- mostrar un mensaje temporal indicando que el detalle se añadirá en HU-30.

La opción preferida es preparar el callback y no mostrar una acción engañosa.

Si la fila es pulsable, deberá existir una respuesta coherente.

Por tanto, para HU-29 se recomienda que la fila todavía no sea pulsable o que solo se active al implementar HU-30.

---

## 30. Preparación para HU-30

HU-29 podrá definir un contrato pequeño:

```text
WithdrawalHistoryListSelection
```

o simplemente conservar:

```text
historyId
```

No deberá crear todavía:

```text
WithdrawalHistoryDetailActivity
```

ni cargar `WithdrawalHistoryRecord`.

La selección se activará en HU-30.

No se adelantará una pantalla vacía de detalle.

---

## 31. Carga inicial

Flujo:

1. abrir Activity;
2. crear ViewModel;
3. observar estado;
4. llamar a `load()` si el ViewModel todavía no cargó;
5. mostrar progreso;
6. ejecutar consulta en el executor;
7. recibir lista;
8. publicar `CONTENT` o `EMPTY`.

La consulta no se ejecutará en el hilo principal.

---

## 32. Actualización después de guardar

El guardado de HU-28 ocurre desde el flujo de procesamiento de listas, no desde la pantalla de historial.

Por tanto:

- si el historial no está abierto, al abrirlo consultará Room y mostrará el nuevo registro;
- si el historial permanece en la pila y el usuario vuelve a él, deberá recargarse;
- `onResume()` podrá solicitar `refresh()` de forma controlada;
- el ViewModel impedirá dos cargas simultáneas.

No se añadirá un bus global de eventos únicamente para esta historia.

No se utilizará `BroadcastReceiver`.

No se utilizará una variable estática.

---

## 33. Preparación para actualización después de eliminar

La eliminación llegará en HU-32.

HU-29 deberá dejar un mecanismo simple:

```text
refresh()
```

Cuando HU-30 o HU-32 devuelvan:

```text
RESULT_OK
```

la Activity podrá recargar.

No se implementará todavía la eliminación ni el contrato definitivo de resultado.

El listado también podrá recargarse en `onResume()`.

---

## 34. Control de cargas duplicadas

El ViewModel deberá impedir:

```text
onCreate + onResume
rotación
doble Reintentar
regreso rápido desde otra pantalla
```

que generen consultas simultáneas.

Mecanismos mínimos:

```text
isLoading
hasLoaded
refreshPending opcional
```

No se ignorará permanentemente una solicitud de refresco posterior a una carga.

Si llega un refresco durante carga, podrá:

- ignorarse y confiar en el resultado actual; o
- marcarse como pendiente.

Para el alcance actual, ignorar la petición simultánea es suficiente si `onResume()` posterior vuelve a consultar cuando corresponda.

---

## 35. Conservación ante rotación

El ViewModel deberá conservar:

- estado actual;
- colección cargada;
- error;
- indicador de carga;
- si ya se realizó carga inicial.

La Activity no deberá iniciar otra consulta únicamente por recrearse.

La RecyclerView recuperará su estado mediante el comportamiento estándar o `LayoutManager`.

---

## 36. Error

Mensaje orientativo:

```text
No se pudo cargar el historial.
```

Acciones:

```text
Reintentar
Volver
```

No se mostrará:

- excepción;
- SQL;
- tabla;
- ruta de base de datos;
- stack trace.

Si existía contenido anterior y falla un refresco:

- podrá conservarse el contenido;
- podrá mostrarse un Snackbar;
- no es obligatorio reemplazarlo por una pantalla vacía de error.

La estrategia deberá ser consistente y probada.

---

## 37. Accesibilidad

HU-29 deberá verificar:

- título de pantalla descriptivo;
- navegación Atrás accesible;
- cada fila leída como una unidad;
- título, fecha y resumen comprensibles;
- singular y plural correctos;
- estado vacío anunciado;
- progreso anunciado;
- error anunciado;
- Reintentar accesible;
- objetivos táctiles de 48 dp;
- contraste claro y oscuro;
- texto adaptable;
- información no dependiente únicamente del color;
- orden de foco coherente.

---

## 38. Privacidad

HU-29 deberá:

- consultar únicamente datos locales;
- no solicitar Internet;
- no enviar historial;
- no registrar títulos ni referencias completas en logs de producción;
- no guardar fotografías;
- no guardar texto OCR;
- no modificar registros durante la consulta.

---

## 39. Pruebas DAO

- historial vacío;
- un historial;
- varios historiales;
- orden por `registeredAt DESC`;
- desempate por `id DESC`;
- contador total;
- contador `FOUND`;
- contador `NOT_FOUND`;
- todas encontradas;
- todas no encontradas;
- mezcla de estados;
- título nulo;
- cabecera defensiva sin líneas;
- consulta sin modificar datos.

---

## 40. Pruebas del mapper

- fila válida;
- título nulo;
- contadores correctos;
- contadores negativos rechazados;
- suma incoherente rechazada;
- timestamps;
- colección vacía;
- fila nula;
- copia defensiva.

---

## 41. Pruebas del repositorio

- lista vacía como `onSuccess`;
- lista con resúmenes;
- orden conservado;
- mapeo correcto;
- executor utilizado;
- un único callback;
- excepción convertida en `onError`;
- callback nulo rechazado;
- ausencia de `onNotFound` para colección vacía.

---

## 42. Pruebas del ViewModel

- estado inicial;
- carga inicial;
- `LOADING`;
- resultado vacío;
- `EMPTY`;
- resultado con datos;
- `CONTENT`;
- error;
- Reintentar;
- carga simultánea ignorada;
- refresco;
- rotación;
- conservación de colección;
- error de refresco con contenido previo.

---

## 43. Pruebas del adapter

- título informado;
- título nulo;
- fecha;
- singular;
- plural;
- todas encontradas;
- algunas no encontradas;
- todas no encontradas;
- reciclado de filas;
- contenido accesible;
- ausencia de listener cuando la fila todavía no es interactiva.

---

## 44. Pruebas manuales

### Sin historial

- abrir Historial;
- comprobar estado vacío;
- volver al listado principal.

### Un registro

- guardar una lista;
- abrir Historial;
- comprobar título, fecha y contadores.

### Sin título

- guardar sin título;
- comprobar `Lista sin título`.

### Varias listas

- guardar con fechas diferentes;
- comprobar orden descendente.

### Misma fecha

- guardar dos listas con misma fecha documental;
- comprobar id más reciente primero.

### Referencias no encontradas

- comprobar resumen correcto.

### Rotación

- durante carga;
- con contenido;
- con error.

### Offline

- activar modo avión;
- abrir historial;
- consultar normalmente.

---

## 45. Criterios de aceptación

### CA-01 — Acceso

**Dado** el listado principal,  
**cuando** el usuario pulsa Historial,  
**entonces** se abre la pantalla de registros históricos.

### CA-02 — Historial vacío

**Dado** que no existen registros,  
**cuando** se carga la pantalla,  
**entonces** se muestra un estado vacío y no un error.

### CA-03 — Listado

**Dado** que existen historiales,  
**cuando** finaliza la consulta,  
**entonces** se muestra una fila por cabecera.

### CA-04 — Orden

**Dado** que existen varias listas,  
**cuando** se presentan,  
**entonces** se ordenan por `registeredAt DESC, id DESC`.

### CA-05 — Título

**Dado** un registro con título,  
**cuando** se muestra,  
**entonces** se presenta el título persistido.

### CA-06 — Sin título

**Dado** un registro sin título,  
**cuando** se muestra,  
**entonces** se presenta `Lista sin título`.

### CA-07 — Fecha

**Dado** un registro,  
**cuando** se muestra,  
**entonces** la fecha visible corresponde a `registeredAt`.

### CA-08 — Contadores

**Dado** un historial con líneas encontradas y no encontradas,  
**cuando** se muestra,  
**entonces** los contadores coinciden con Room.

### CA-09 — Consulta eficiente

**Dado** el listado,  
**cuando** se carga,  
**entonces** no se ejecuta `findById()` individualmente para cada fila.

### CA-10 — Error recuperable

**Dado** un error de Room,  
**cuando** se informa,  
**entonces** el usuario puede reintentar.

### CA-11 — Refresco

**Dado** que se guardó un historial nuevo,  
**cuando** la pantalla vuelve a consultar Room,  
**entonces** aparece el nuevo registro.

### CA-12 — Mercadería intacta

**Dada** cualquier consulta,  
**cuando** finaliza,  
**entonces** la tabla `warehouse_items` no ha cambiado.

### CA-13 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se abre el historial,  
**entonces** el listado funciona mediante Room local.

---

## 46. Riesgos

### Carga innecesaria de líneas

**Mitigación:** proyección de resumen con agregación SQL.

### Orden inestable

**Mitigación:** desempate explícito por id descendente.

### Contadores incoherentes

**Mitigación:** usar `location_status` y validar la suma.

### Cargas duplicadas

**Mitigación:** estado interno del ViewModel.

### Lista desactualizada

**Mitigación:** refresco controlado al volver a primer plano.

### Exceso de arquitectura

**Mitigación:** omitir el servicio si solo delega sin aportar reglas reales.

### Interacción adelantada

**Mitigación:** no abrir detalle hasta HU-30.

---

## 47. Definición de terminado

HU-29 estará terminada cuando:

- exista acceso desde el menú principal;
- exista `WithdrawalHistoryListActivity`;
- exista modelo de resumen;
- exista proyección Room;
- el DAO consulte resúmenes;
- el repositorio exponga `findAllSummaries`;
- los contadores se calculen en Room;
- exista ViewModel;
- exista adapter;
- exista estado de carga;
- exista estado de contenido;
- exista estado vacío;
- exista error recuperable;
- el orden sea estable;
- título y fecha se muestren correctamente;
- singular y plural sean correctos;
- el listado pueda refrescarse;
- no se carguen detalles completos;
- no se modifique mercadería;
- funcione sin conexión;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 48. Resultado esperado

Al cerrar HU-29:

```text
MainActivity
        ↓
Historial
        ↓
WithdrawalHistoryListActivity
        ↓
resúmenes Room
        ↓
listas ordenadas y consultables
```

El usuario podrá conocer:

```text
qué listas existen
cuándo se registraron
cuántas referencias contienen
cuántas fueron encontradas
cuántas no fueron encontradas
```

sin cargar todavía el detalle completo.

La apertura y presentación de las líneas históricas corresponderá a HU-30.
