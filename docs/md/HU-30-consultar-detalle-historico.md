# HU-30 — Consultar el detalle histórico de una lista

> Quinta historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-30  
**Nombre:** Consultar el detalle histórico de una lista  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-30-consultar-detalle-historico`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-29 — Consultar el historial de listas  
**Issue prevista:** `#34`

---

## 2. Historia de usuario

Como usuario,  
quiero abrir una lista guardada en el historial,  
para revisar su título, fecha, referencias, cantidades, unidades y ubicaciones históricas.

---

## 3. Objetivo

Completar la navegación desde el listado histórico hacia una pantalla de detalle de solo lectura.

Flujo previsto:

```text
WithdrawalHistoryListActivity
        ↓ seleccionar una fila
WithdrawalHistoryDetailActivity
        ↓ historyId
WithdrawalHistoryDetailViewModel
        ↓
WithdrawalHistoryRepository.findById(...)
        ↓
WithdrawalHistoryRecord
        ↓
cabecera + líneas ordenadas
```

Resultado visual orientativo:

```text
Reposición tienda centro
2 ago 2026 · 12:30
15 referencias

MR · 1210A
4 CAJAS
Ubicación al registrar: A1 · Posición 2

MZ · 1300C
2 PCS
No encontrada al registrar la lista
```

HU-30 deberá mostrar los datos históricos exactamente como fueron guardados.

---

## 4. Regla principal

La pantalla mostrará la instantánea histórica.

```text
sitio histórico
posición histórica
estado histórico
```

No consultará ni sustituirá esos valores por la ubicación actual de la mercadería.

Ejemplo:

```text
historial guardado:
A1 · Posición 2

ubicación actual:
B3 · Posición 1
```

HU-30 deberá mostrar:

```text
A1 · Posición 2
```

porque ese fue el dato documentado al registrar la lista.

La ubicación actual podrá incorporarse en una evolución posterior si existe una necesidad explícita y se distingue claramente de la histórica.

---

## 5. Documentos y código de referencia

HU-30 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-29-consultar-historial-listas.md`;
- el estado real de `AlmacenTrackerHU29.zip`;
- Room como fuente local de verdad;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- la separación entre mercadería actual e historial;
- el carácter documental de cantidad y unidad;
- la conservación del orden mediante `orderIndex`;
- el funcionamiento completamente sin conexión;
- la política de crear componentes únicamente cuando aporten una responsabilidad real.

El plan de v1.3 asigna a HU-30:

```text
cabecera
+
líneas ordenadas
+
cantidad
+
unidad
+
ubicación histórica
+
estado no encontrado
```

---

## 6. Estado real antes de HU-30

El análisis de `AlmacenTrackerHU29.zip` confirma:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida es:

```text
develop
```

HU-29 ya está integrada mediante:

```text
merge HU29 #33 into develop
```

El proyecto ya dispone de:

```text
WithdrawalHistoryListActivity
WithdrawalHistoryListAdapter
WithdrawalHistoryListUiState
WithdrawalHistoryListViewModel
WithdrawalHistoryListViewModelFactory
WithdrawalHistorySummary
WithdrawalHistorySummaryRow
```

La pantalla Historial ya:

- se abre desde `MainActivity`;
- consulta resúmenes;
- muestra título;
- muestra `registeredAt`;
- muestra total de referencias;
- muestra encontradas y no encontradas;
- ordena por `registeredAt DESC, id DESC`;
- presenta estados `LOADING`, `CONTENT`, `EMPTY` y `ERROR`;
- recarga al volver a primer plano;
- funciona sin conexión.

Actualmente las filas del adapter están configuradas expresamente como:

```text
clickable = false
focusable = false
OnClickListener = null
```

HU-30 deberá activar una navegación real y accesible.

---

## 7. Infraestructura ya disponible

El repositorio histórico ya ofrece:

```java
void findById(
        long historyId,
        RepositoryCallback<WithdrawalHistoryRecord> callback
);
```

La implementación Room:

1. consulta la cabecera;
2. devuelve `onNotFound()` si no existe;
3. consulta las líneas por `history_id`;
4. las ordena por `order_index ASC`;
5. mapea a `WithdrawalHistoryRecord`;
6. ejecuta la operación fuera del hilo principal.

El DAO ya dispone de:

```java
WithdrawalHistoryWithEntries findByIdWithEntries(
        long historyId
);
```

y:

```java
List<WithdrawalHistoryEntryEntity>
findEntriesByHistoryId(
        long historyId
);
```

La consulta de líneas ya aplica:

```text
ORDER BY order_index ASC
```

Además, `WithdrawalHistoryRecord` vuelve a ordenar defensivamente por `orderIndex`.

Por tanto, HU-30 no necesita:

- una nueva tabla;
- una nueva migración;
- una nueva consulta de detalle;
- un nuevo repositorio;
- una proyección Room adicional;
- consultar línea por línea desde la UI.

---

## 8. Alcance incluido

HU-30 incluye:

- hacer seleccionables las filas del historial;
- añadir callback de selección al adapter;
- navegar mediante el id histórico;
- crear contrato de Intent para detalle;
- validar el id recibido;
- crear `WithdrawalHistoryDetailActivity`;
- declarar la Activity en el Manifest;
- crear layout de detalle;
- crear RecyclerView para líneas;
- crear adapter de líneas;
- crear estado de interfaz;
- crear ViewModel;
- crear factory;
- ampliar `WithdrawalHistoryModule`;
- cargar mediante `findById(...)`;
- mostrar estado de carga;
- mostrar cabecera;
- mostrar título;
- mostrar texto sustituto cuando el título sea nulo;
- mostrar fecha documental;
- mostrar número de referencias;
- mostrar cada línea en su orden;
- mostrar categoría y código;
- conservar ceros iniciales;
- mostrar cantidad cuando exista;
- mostrar unidad cuando exista;
- mostrar cantidad sin unidad cuando corresponda;
- ocultar la sección documental cuando no exista cantidad;
- mostrar sitio histórico;
- mostrar posición histórica cuando exista;
- ocultar posición cuando sea nula;
- mostrar estado `NOT_FOUND`;
- diferenciar ubicación histórica de ubicación actual;
- mostrar `NOT_FOUND` sin inventar ubicación;
- mostrar registro inexistente;
- mostrar error controlado;
- permitir reintentar;
- conservar el resultado ante rotación;
- evitar cargas duplicadas;
- no modificar Room;
- no modificar mercadería;
- no consultar la ubicación actual;
- no gestionar stock;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas unitarias;
- pruebas de ViewModel;
- pruebas de adapter;
- pruebas de contrato de Intent;
- pruebas instrumentadas cuando aporten valor;
- CI.

---

## 9. Alcance excluido

HU-30 no incluye:

- editar un historial;
- editar título;
- editar fecha;
- editar cantidad;
- editar unidad;
- editar ubicación histórica;
- actualizar `updatedAt`;
- consultar ubicación actual;
- comparar ubicación histórica y actual;
- abrir el detalle actual de mercadería;
- eliminar el historial;
- añadir acción Eliminar;
- búsqueda;
- filtros;
- exportación;
- compartición;
- modificación del backup;
- estadísticas;
- navegación entre historiales;
- agrupación por categoría;
- orden alternativo;
- paginación;
- sincronización remota;
- autenticación;
- backend;
- gestión de stock.

La búsqueda y filtros corresponden a HU-31.

La eliminación corresponde a HU-32.

---

## 10. Decisión crítica sobre ubicación actual

Aunque cada línea `FOUND` conserva:

```text
warehouseItemIdSnapshot
```

HU-30 no deberá utilizar ese id para consultar `WarehouseItemRepository`.

Motivos:

- el historial debe ser estable;
- la mercadería puede cambiar de ubicación;
- la mercadería puede eliminarse;
- el id podría no existir actualmente;
- mezclar ambos datos puede confundir al usuario;
- el plan de HU-30 exige ubicación histórica.

`warehouseItemIdSnapshot` permanecerá como dato documental y técnico del registro.

No se mostrará como id interno.

---

## 11. Contrato de navegación

Se añadirá un contrato pequeño:

```text
WithdrawalHistoryDetailIntentContract
```

Ubicación recomendada:

```text
feature/withdrawal_history/common/
```

Constante:

```java
EXTRA_HISTORY_ID
```

Operaciones orientativas:

```java
Intent createIntent(
        Context context,
        long historyId
);

long readHistoryId(Intent intent);
```

Reglas:

- `historyId > 0`;
- extra con nombre completo y estable;
- no serializar `WithdrawalHistoryRecord`;
- no serializar todas las líneas;
- no consultar Room desde el adapter;
- la Activity de detalle es responsable de cargar el id.

---

## 12. Activación de filas en HU-29

`WithdrawalHistoryListAdapter` deberá recibir un listener:

```java
OnHistoryClickListener
```

Contrato orientativo:

```java
void onHistoryClick(long historyId);
```

Cada fila deberá:

- ser pulsable;
- ser focusable;
- utilizar su id estable;
- emitir únicamente el id;
- no navegar directamente;
- no conocer `Context.startActivity(...)`;
- mantener su descripción accesible.

`WithdrawalHistoryListActivity` deberá:

1. recibir el callback;
2. crear el Intent;
3. abrir `WithdrawalHistoryDetailActivity`.

---

## 13. Pantalla de detalle

Nombre:

```text
WithdrawalHistoryDetailActivity
```

La pantalla deberá incluir:

- Toolbar;
- título de pantalla;
- navegación Atrás;
- bloque de cabecera;
- título de la lista;
- fecha documental;
- resumen de referencias;
- RecyclerView de líneas;
- progreso;
- estado de registro inexistente;
- estado de error;
- acción Reintentar.

No se reutilizará `WithdrawalHistoryCreateActivity`.

Motivos:

- la creación es editable;
- el detalle es de solo lectura;
- sus estados son distintos;
- la creación contiene validación;
- reutilizarla obligaría a deshabilitar demasiados controles.

---

## 14. Estructura visual

Layout recomendado:

```text
activity_withdrawal_history_detail.xml
```

Jerarquía orientativa:

```text
CoordinatorLayout
└── LinearLayout vertical
    ├── MaterialToolbar
    ├── ProgressIndicator
    ├── DetailContent
    │   ├── HeaderCard
    │   │   ├── Title
    │   │   ├── RegisteredDate
    │   │   └── ReferenceCount
    │   └── RecyclerView
    ├── NotFoundContainer
    └── ErrorContainer
```

El RecyclerView deberá usar:

```text
clipToPadding = false
```

cuando exista padding inferior.

No se anidará un RecyclerView dentro de un `ScrollView`.

---

## 15. Estado de interfaz

Se añadirá:

```text
WithdrawalHistoryDetailUiState
```

Estados:

```text
LOADING
CONTENT
NOT_FOUND
ERROR
```

### `LOADING`

- progreso visible;
- contenido oculto;
- error oculto;
- no encontrado oculto.

### `CONTENT`

- cabecera visible;
- RecyclerView visible;
- progreso oculto;
- error oculto.

### `NOT_FOUND`

- mensaje de registro inexistente;
- contenido oculto;
- acción Volver;
- no se confunde con error técnico.

### `ERROR`

- mensaje controlado;
- acción Reintentar;
- contenido anterior podrá conservarse si existía;
- no se muestra excepción.

No se necesita estado `EMPTY` porque un historial válido debería contener al menos una línea.

Una cabecera sin líneas deberá mostrarse defensivamente como contenido con:

```text
0 referencias
```

sin provocar caída.

---

## 16. ViewModel

Se creará:

```text
WithdrawalHistoryDetailViewModel
```

Responsabilidades:

- recibir o inicializar `historyId`;
- validar que sea mayor que cero;
- cargar una sola vez;
- impedir cargas simultáneas;
- llamar a `WithdrawalHistoryRepository.findById(...)`;
- publicar `LOADING`;
- publicar `CONTENT`;
- publicar `NOT_FOUND`;
- publicar `ERROR`;
- conservar `WithdrawalHistoryRecord`;
- permitir reintento;
- no navegar;
- no formatear recursos;
- no depender de `Context`;
- no conocer DAO;
- no conocer entidades Room;
- no consultar mercadería actual.

Métodos orientativos:

```java
public void load(long historyId);
public void retry();
```

El id deberá aplicarse una única vez.

---

## 17. ViewModelFactory

Se creará:

```text
WithdrawalHistoryDetailViewModelFactory
```

Dependencia:

```text
WithdrawalHistoryRepository
```

o un servicio de detalle si realmente aporta valor.

Para HU-30 no se recomienda crear:

```text
WithdrawalHistoryDetailService
```

si su única responsabilidad es delegar `findById(...)`.

El repositorio ya devuelve exactamente el agregado que necesita la pantalla.

Añadir un servicio sin reglas adicionales sería una capa ceremonial.

---

## 18. Composición de dependencias

`WithdrawalHistoryModule` deberá proporcionar:

```text
WithdrawalHistoryDetailViewModelFactory
```

`AppContainer` deberá exponer:

```java
provideWithdrawalHistoryDetailViewModelFactory()
```

La Activity no deberá construir:

- DAO;
- Room;
- mapper;
- repositorio;
- executor.

La misma instancia de `WithdrawalHistoryRepository` deberá reutilizarse.

---

## 19. Modelo de presentación por línea

Se podrá renderizar directamente:

```text
WithdrawalHistoryEntry
```

si el adapter solo necesita lectura.

No es obligatorio crear:

```text
WithdrawalHistoryDetailEntryUiModel
```

si únicamente duplicaría campos.

Un modelo de presentación solo se justificará si concentra:

- textos derivados;
- banderas visuales;
- datos ya formateados;
- lógica reutilizable fuera del adapter.

Para mantener claridad, se recomienda que el adapter reciba `WithdrawalHistoryEntry` y que el formato visual permanezca en el adapter mediante recursos Android.

---

## 20. Adapter de detalle

Se creará:

```text
WithdrawalHistoryDetailAdapter
```

Responsabilidades:

- renderizar las líneas en orden;
- mostrar categoría y código;
- mostrar cantidad y unidad;
- mostrar ubicación histórica;
- mostrar estado no encontrado;
- reciclar correctamente las vistas;
- ocultar elementos no aplicables;
- no modificar datos;
- no navegar;
- no consultar repositorios;
- no interpretar stock.

No será necesario un listener de fila en HU-30.

---

## 21. Layout de una línea

Layout recomendado:

```text
item_withdrawal_history_entry.xml
```

Cada fila mostrará:

### Identidad

```text
MR · 1210A
```

### Datos documentales

Con cantidad y unidad:

```text
4 CAJAS
```

Solo cantidad:

```text
4
```

Sin cantidad:

```text
sección oculta
```

### Ubicación histórica encontrada

Con posición:

```text
Ubicación al registrar: A1 · Posición 2
```

Sin posición:

```text
Ubicación al registrar: A1
```

### No encontrada

```text
No encontrada al registrar la lista
```

No deberá mostrar:

```text
Sitio —
Posición —
```

para una referencia `NOT_FOUND`.

---

## 22. Cantidad y unidad

Reglas visuales:

```text
quantity != null
unit != null
    → "4 CAJAS"

quantity != null
unit == null
    → "4"

quantity == null
unit == null
    → ocultar línea documental
```

El dominio de HU-27 y HU-28 no permite unidad sin cantidad.

Si apareciera un dato anómalo:

```text
quantity == null
unit != null
```

la UI no deberá caer.

Podrá:

- mostrar la unidad como dato documental; o
- tratar el registro como error técnico.

Recomendación:

```text
mostrar la unidad
```

de forma defensiva, sin ocultar información persistida.

---

## 23. Estado `FOUND`

Una línea `FOUND` deberá mostrar:

```text
siteSnapshot
positionSnapshot opcional
```

No deberá consultar:

```text
WarehouseItem.site
WarehouseItem.position
```

No deberá mostrar el id de mercadería.

La etiqueta visible debe dejar claro:

```text
Ubicación al registrar
```

y no simplemente:

```text
Ubicación
```

---

## 24. Estado `NOT_FOUND`

Una línea `NOT_FOUND` deberá mostrar:

```text
No encontrada al registrar la lista
```

No deberá:

- ocultar la línea;
- intentar localizarla ahora;
- convertirla en error;
- ofrecer crear mercadería;
- mostrar una ubicación vacía;
- inferir sitio o posición.

La cantidad y unidad documentales podrán seguir mostrándose.

---

## 25. Cabecera

La cabecera deberá mostrar:

### Título

Con título:

```text
Reposición tienda centro
```

Sin título:

```text
Lista sin título
```

### Fecha principal

```text
2 ago 2026 · 12:30
```

La fecha deberá utilizar:

```text
registeredAt
```

### Número de referencias

```text
15 referencias
```

o:

```text
1 referencia
```

No se mostrarán todavía:

- `createdAt`;
- `updatedAt`;
- ids internos;
- nombre de tabla;
- datos técnicos.

---

## 26. Formato de fecha

La pantalla reutilizará el mismo criterio de HU-29:

```text
registeredAt
+
locale del dispositivo
+
zona horaria del dispositivo
```

Podrá reutilizarse un formatter común si evita duplicación real.

Nombre orientativo:

```text
HistoryDateFormatter
```

No se creará una abstracción solo por una llamada.

Si el formateo se repite únicamente en dos adapters, una utilidad pequeña de presentación puede ser suficiente.

---

## 27. Orden de líneas

El orden visible será:

```text
orderIndex ASC
```

No se reordenará por:

- categoría;
- código;
- estado;
- sitio;
- cantidad;
- id.

La pantalla deberá representar el orden documental guardado.

Aunque el repositorio ya devuelve el orden correcto, el adapter no deberá aplicar otro orden.

---

## 28. Registro inexistente

Puede ocurrir si:

- se abre un Intent antiguo;
- el registro fue eliminado;
- el id es válido pero ya no existe;
- se restaura una tarea antigua.

Estado:

```text
NOT_FOUND
```

Mensaje:

```text
Esta lista histórica ya no existe.
```

Acción:

```text
Volver al historial
```

No se mostrará:

- error SQL;
- stack trace;
- id interno;
- botón Reintentar como acción principal.

---

## 29. Id inválido

Si el Intent no contiene id o contiene:

```text
historyId <= 0
```

la Activity deberá:

- no consultar Room;
- mostrar estado `NOT_FOUND` o entrada inválida controlada;
- permitir volver;
- no lanzar excepción;
- no usar id `0` como registro válido.

Se recomienda resolverlo como:

```text
NOT_FOUND
```

para mantener una única experiencia visual.

---

## 30. Error técnico

Mensaje orientativo:

```text
No se pudo cargar el detalle del historial.
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

El ViewModel deberá conservar el id para el reintento.

---

## 31. Rotación y recreación

HU-30 deberá garantizar:

- el id permanece;
- el registro cargado permanece;
- no se duplica la consulta;
- no se pierde el estado `NOT_FOUND`;
- no se pierde el error;
- el scroll del RecyclerView se conserva cuando sea posible;
- no se vuelve al inicio por recreación innecesaria;
- no se emite navegación desde el ViewModel.

La Activity no deberá volver a aplicar el id como una carga nueva si el ViewModel ya contiene contenido.

---

## 32. Regreso desde detalle

Al pulsar Atrás:

```text
WithdrawalHistoryDetailActivity
        ↓
WithdrawalHistoryListActivity
```

HU-30 no modifica datos.

Por tanto, no necesita devolver:

```text
RESULT_OK
```

La lista no necesita recargarse por una consulta de detalle.

La recarga para eliminación llegará en HU-32.

---

## 33. Actualización mientras el detalle está abierto

HU-30 no observará cambios en tiempo real.

La consulta será puntual.

Motivo:

- el historial no es editable;
- no existe sincronización remota;
- HU-32 eliminará desde una acción explícita;
- `LiveData` de Room no aporta una necesidad actual.

Si el registro se elimina desde otra ruta futura mientras el detalle está abierto, el estado podrá actualizarse al volver a cargar o recrear.

---

## 34. Accesibilidad

HU-30 deberá verificar:

- Toolbar con título descriptivo;
- navegación Atrás accesible;
- cabecera leída en orden;
- cada línea leída como unidad;
- categoría y código comprensibles;
- cantidad y unidad anunciadas;
- ubicación etiquetada como histórica;
- estado no encontrado anunciado;
- singular y plural correctos;
- progreso anunciado;
- error anunciado;
- Reintentar accesible;
- objetivos táctiles de 48 dp;
- contraste claro y oscuro;
- texto adaptable;
- información no dependiente del color;
- filas del listado de HU-29 con estado pulsable accesible.

---

## 35. Privacidad

HU-30 deberá:

- consultar únicamente Room local;
- no solicitar Internet;
- no enviar historial;
- no registrar el detalle completo en logs de producción;
- no guardar fotografías;
- no guardar texto OCR;
- no modificar registros;
- no exponer ids internos en la interfaz.

---

## 36. Pruebas del contrato de Intent

- id válido;
- id cero;
- id negativo;
- Intent nulo;
- extra ausente;
- creación correcta del Intent;
- lectura correcta del id;
- nombre estable del extra.

---

## 37. Pruebas del ViewModel

- estado inicial;
- id válido;
- `LOADING`;
- `CONTENT`;
- cabecera correcta;
- líneas conservadas;
- orden conservado;
- id inválido;
- `NOT_FOUND`;
- repositorio `onNotFound`;
- error;
- reintento;
- carga simultánea ignorada;
- rotación;
- carga única;
- conservación del registro.

---

## 38. Pruebas del adapter

### Identidad

- categoría;
- código;
- ceros iniciales.

### Cantidad y unidad

- ambas presentes;
- solo cantidad;
- ambas ausentes;
- dato anómalo con solo unidad.

### Ubicación

- `FOUND` con posición;
- `FOUND` sin posición;
- `NOT_FOUND`;
- reciclado de una fila encontrada hacia no encontrada;
- reciclado de una fila con cantidad hacia otra sin cantidad.

### Accesibilidad

- descripción de contenido;
- orden legible;
- estado no encontrado;
- ubicación histórica.

---

## 39. Pruebas de Activity

- apertura desde una fila;
- id correcto;
- navegación Atrás;
- estado de carga;
- contenido;
- registro inexistente;
- error;
- Reintentar;
- rotación;
- fila del listado activada;
- ausencia de navegación duplicada.

---

## 40. Pruebas manuales

### Registro completo

- título;
- fecha;
- cantidades;
- unidades;
- referencias encontradas;
- referencias no encontradas.

### Sin título

- mostrar `Lista sin título`.

### Cantidad sin unidad

- mostrar solo el número.

### Sin cantidad

- ocultar sección documental.

### Encontrada sin posición

- mostrar solo sitio.

### No encontrada

- mostrar mensaje histórico.

### Ubicación actual modificada

1. guardar historial;
2. cambiar ubicación actual de la mercadería;
3. abrir detalle;
4. comprobar que conserva la ubicación anterior.

### Mercadería eliminada

1. guardar historial;
2. eliminar mercadería actual;
3. abrir detalle;
4. comprobar que la instantánea histórica permanece.

### Offline

- activar modo avión;
- abrir detalle;
- consultar normalmente.

---

## 41. Criterios de aceptación

### CA-01 — Apertura

**Dado** el listado histórico,  
**cuando** el usuario pulsa una fila,  
**entonces** se abre el detalle correspondiente.

### CA-02 — Identidad del registro

**Dado** un id histórico válido,  
**cuando** se carga,  
**entonces** se muestra únicamente el registro asociado a ese id.

### CA-03 — Cabecera

**Dado** un registro existente,  
**cuando** se muestra,  
**entonces** presenta título, fecha documental y número de referencias.

### CA-04 — Sin título

**Dado** un registro sin título,  
**cuando** se muestra,  
**entonces** aparece `Lista sin título`.

### CA-05 — Orden

**Dado** un historial con varias líneas,  
**cuando** se presenta,  
**entonces** respeta `orderIndex ASC`.

### CA-06 — Cantidad y unidad

**Dada** una línea con cantidad y unidad,  
**cuando** se presenta,  
**entonces** muestra ambos datos documentales.

### CA-07 — Cantidad opcional

**Dada** una línea sin cantidad,  
**cuando** se presenta,  
**entonces** no muestra un valor inventado.

### CA-08 — Ubicación histórica

**Dada** una línea `FOUND`,  
**cuando** se presenta,  
**entonces** muestra `siteSnapshot` y `positionSnapshot` cuando exista.

### CA-09 — Ubicación actual diferente

**Dado** que la mercadería cambió de ubicación,  
**cuando** se abre el detalle,  
**entonces** permanece visible la ubicación histórica.

### CA-10 — Mercadería eliminada

**Dado** que la mercadería actual ya no existe,  
**cuando** se abre el detalle,  
**entonces** la línea histórica continúa disponible.

### CA-11 — No encontrada

**Dada** una línea `NOT_FOUND`,  
**cuando** se presenta,  
**entonces** muestra `No encontrada al registrar la lista`.

### CA-12 — Registro inexistente

**Dado** un id que no existe,  
**cuando** se consulta,  
**entonces** se muestra un estado controlado y no un error técnico.

### CA-13 — Error recuperable

**Dado** un error de Room,  
**cuando** se informa,  
**entonces** el usuario puede reintentar.

### CA-14 — Solo lectura

**Dado** cualquier detalle,  
**cuando** el usuario lo consulta,  
**entonces** no se modifica Room.

### CA-15 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se abre el detalle,  
**entonces** funciona mediante Room local.

---

## 42. Riesgos

### Mostrar ubicación actual por error

**Mitigación:** no usar `WarehouseItemRepository` ni `warehouseItemIdSnapshot` para consultar la mercadería.

### Duplicar consulta de detalle

**Mitigación:** reutilizar `findById(...)`.

### Reordenar líneas

**Mitigación:** respetar el `WithdrawalHistoryRecord` y no ordenar en el adapter.

### Reciclado visual incorrecto

**Mitigación:** restaurar visibilidad y texto de todos los campos en cada `bind()`.

### Capa de servicio ceremonial

**Mitigación:** ViewModel directamente contra repositorio para esta consulta puntual.

### Confundir `NOT_FOUND` con error

**Mitigación:** estado visual específico y fila histórica normal.

### Adelantar eliminación

**Mitigación:** no añadir menú destructivo hasta HU-32.

---

## 43. Definición de terminado

HU-30 estará terminada cuando:

- las filas de HU-29 sean seleccionables;
- exista contrato de navegación;
- exista `WithdrawalHistoryDetailActivity`;
- exista layout de detalle;
- exista `WithdrawalHistoryDetailUiState`;
- exista `WithdrawalHistoryDetailViewModel`;
- exista factory;
- `WithdrawalHistoryModule` proporcione la dependencia;
- se utilice `findById(...)`;
- se muestre la cabecera;
- se muestren líneas ordenadas;
- se muestren cantidades y unidades opcionales;
- se muestre ubicación histórica;
- se represente `NOT_FOUND`;
- exista estado de registro inexistente;
- exista error recuperable;
- la rotación conserve el estado;
- no se consulte mercadería actual;
- no se modifique Room;
- no se gestione stock;
- funcione sin conexión;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 44. Resultado esperado

Al cerrar HU-30:

```text
Historial
        ↓
seleccionar lista
        ↓
WithdrawalHistoryDetailActivity
        ↓
WithdrawalHistoryRecord
        ↓
cabecera + líneas históricas
```

El usuario podrá revisar:

```text
título
fecha documental
referencias
cantidad
unidad
ubicación al registrar
estado no encontrado
```

sin alterar la mercadería ni el historial.

La búsqueda y los filtros comenzarán en HU-31.
