# HU-28 — Registrar una lista confirmada en el historial

> Tercera historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-28  
**Nombre:** Registrar una lista confirmada en el historial  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-28-registrar-lista-historial`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-26 — Preparar el modelo histórico y la migración Room  
- HU-27 — Capturar título, cantidad y unidad documentales  

**Issue prevista:** `#32`

---

## 2. Historia de usuario

Como usuario,  
quiero guardar una lista revisada y confirmada en el historial,  
para conservar qué mercadería fue sacada, qué información documental contenía y dónde se encontraba cada referencia en ese momento.

---

## 3. Objetivo

Conectar el borrador documental preparado por HU-27 con la persistencia histórica creada en HU-26.

Flujo previsto:

```text
WithdrawalHistoryCreateActivity
        ↓
título, fecha, cantidades y unidades revisadas
        ↓
WithdrawalHistoryDraft válido
        ↓
confirmación explícita
        ↓
WithdrawalHistorySaveService
        ↓
WithdrawalHistoryRecord
        ↓
WithdrawalHistoryRepository.insert(...)
        ↓
Room
        ↓
cabecera + líneas en una única transacción
```

HU-28 será la primera historia de v1.3 que persistirá una lista procesada.

La operación deberá garantizar:

- guardado único;
- validación final;
- inserción transaccional;
- conservación de instantáneas;
- referencias encontradas y no encontradas;
- ausencia de modificaciones sobre la mercadería;
- ausencia de gestión de stock;
- error recuperable sin perder el borrador.

---

## 4. Regla principal

El historial tendrá carácter documental.

```text
guardar historial
        ≠
actualizar mercadería
        ≠
descontar stock
```

Ejemplo:

```text
MR 1210
4 CAJAS
Sitio A1 · Posición 2
```

El guardado significará:

```text
la lista indicó 4 CAJAS de MR 1210
y la referencia estaba ubicada en A1 / 2
cuando se registró el documento
```

No significará:

```text
restar 4 unidades
cambiar la ubicación actual
crear una salida contable
```

---

## 5. Documentos y código de referencia

HU-28 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-26-preparar-modelo-historico-migracion-room.md`;
- `HU-27-capturar-titulo-cantidad-unidad.md`;
- el estado real de `AlmacenTrackerHU27.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- Room como fuente local de verdad;
- el carácter documental de cantidad y unidad;
- la identidad `categoría + código`;
- la conservación del orden;
- la separación entre mercadería e historial;
- el funcionamiento completamente sin conexión;
- la política de no crear componentes sin responsabilidad real.

El plan de v1.3 asigna a HU-28:

```text
confirmación
+
instantánea de ubicación
+
referencias encontradas y no encontradas
+
guardado atómico
+
prevención de guardados duplicados
```

---

## 6. Estado real antes de HU-28

El análisis de `AlmacenTrackerHU27.zip` confirma:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida en el ZIP es:

```text
HU28
```

El historial ya dispone de:

```text
domain/history/
├── WithdrawalHistory.java
├── WithdrawalHistoryEntry.java
├── WithdrawalHistoryRecord.java
├── WithdrawalHistoryDraft.java
├── WithdrawalHistoryDraftEntry.java
├── WithdrawalHistoryDraftValidator.java
├── WithdrawalHistoryDraftValidationResult.java
└── WithdrawalLocationStatus.java
```

La persistencia ya dispone de:

```text
WithdrawalHistoryDao
WithdrawalHistoryEntity
WithdrawalHistoryEntryEntity
WithdrawalHistoryWithEntries
WithdrawalHistoryRoomMapper
WithdrawalHistoryRepository
RoomWithdrawalHistoryRepository
```

El repositorio actual ofrece:

```java
void insert(
        WithdrawalHistoryRecord record,
        RepositoryCallback<Long> callback
);
```

También ofrece:

```java
void findById(...)
void deleteById(...)
```

El DAO ya dispone de:

```java
@Transaction
default long insertHistoryWithEntries(...)
```

La transacción:

1. inserta la cabecera;
2. obtiene el id generado;
3. vincula las líneas;
4. inserta todas las líneas;
5. devuelve el id.

`AppContainer` ya construye:

```text
RoomWithdrawalHistoryRepository
```

utilizando:

- `database.withdrawalHistoryDao()`;
- `WithdrawalHistoryRoomMapper`;
- el `databaseExecutor` existente.

---

## 7. Estado real de la pantalla de HU-27

HU-27 ya proporciona:

```text
feature/withdrawal_history/
├── common/
│   ├── WithdrawalHistoryCreateInput.java
│   └── WithdrawalHistoryCreateIntentContract.java
└── create/
    ├── WithdrawalHistoryCreateActivity.java
    ├── WithdrawalHistoryCreateAdapter.java
    ├── WithdrawalHistoryCreateUiState.java
    ├── WithdrawalHistoryCreateViewModel.java
    ├── WithdrawalHistoryCreateViewModelFactory.java
    └── WithdrawalHistoryDraftEntryUiModel.java
```

El flujo actual:

```text
ReferenceListLocationActivity
        ↓
WithdrawalHistoryCreateActivity
        ↓
continueToConfirmation(...)
        ↓
UiEvent<WithdrawalHistoryDraft>
        ↓
Toast: borrador preparado
```

El evento actual todavía no guarda en Room.

La pantalla ya permite:

- título opcional;
- fecha documental inicial;
- cantidades;
- unidades;
- validación;
- propuestas documentales;
- referencias `FOUND`;
- referencias `NOT_FOUND`;
- instantáneas de id, sitio y posición;
- conservación del orden;
- creación de `WithdrawalHistoryDraft`.

HU-28 deberá ampliar estos componentes, no reemplazarlos.

---

## 8. Limitaciones actuales que HU-28 debe cerrar

Antes de HU-28:

- `continueToConfirmation(...)` solo genera un borrador;
- `consumeContinueEvent(...)` solo muestra un `Toast`;
- no existe confirmación de guardado;
- no existe `WithdrawalHistorySaveService`;
- el ViewModel no conoce `WithdrawalHistoryRepository`;
- la factory no recibe dependencias;
- no existen estados `SAVING`, `SAVED` o error de persistencia;
- no se devuelve el id generado;
- no existe bloqueo de doble guardado;
- no existe reintento después de un error;
- no existe resultado `RESULT_OK` de la creación histórica.

HU-28 deberá cerrar exactamente esas carencias.

---

## 9. Alcance incluido

HU-28 incluye:

- reutilizar `WithdrawalHistoryCreateActivity`;
- reutilizar `WithdrawalHistoryDraft`;
- reutilizar la validación de HU-27;
- sustituir el cierre provisional por un guardado real;
- mostrar una confirmación antes de persistir;
- mostrar resumen de referencias;
- mostrar número de encontradas y no encontradas;
- recordar que las cantidades no modifican stock;
- permitir cancelar la confirmación;
- construir un `WithdrawalHistoryRecord`;
- asignar timestamps de creación;
- conservar `registeredAt`;
- conservar título normalizado;
- conservar todas las líneas;
- conservar `orderIndex`;
- conservar categoría y código;
- conservar ceros iniciales;
- conservar cantidad y unidad opcionales;
- conservar `warehouseItemIdSnapshot`;
- conservar `siteSnapshot`;
- conservar `positionSnapshot`;
- conservar `FOUND`;
- conservar `NOT_FOUND`;
- delegar la inserción al repositorio;
- utilizar la transacción existente;
- obtener el id histórico generado;
- bloquear la interfaz durante el guardado;
- impedir doble pulsación;
- impedir dos callbacks activos;
- emitir un único evento de éxito;
- devolver `RESULT_OK`;
- devolver el id histórico;
- finalizar la pantalla después del éxito;
- conservar el borrador después de error;
- permitir reintento;
- no modificar mercadería;
- no volver a consultar ubicaciones;
- no modificar el esquema Room;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas unitarias;
- pruebas de servicio;
- pruebas de ViewModel;
- pruebas de integración con repositorio;
- pruebas instrumentadas cuando aporten valor;
- CI.

---

## 10. Alcance excluido

HU-28 no incluye:

- listado histórico;
- opción global de menú para historial;
- detalle histórico;
- búsqueda por título;
- búsqueda por categoría;
- búsqueda por código;
- filtros por fecha;
- eliminación desde interfaz;
- edición de historiales;
- duplicación de historiales;
- exportación del historial;
- importación del historial;
- modificación del backup CSV;
- nuevas tablas;
- nueva migración Room;
- contadores persistidos;
- estadísticas;
- actualización de ubicaciones históricas;
- sincronización remota;
- autenticación;
- backend;
- ONNX Runtime;
- PP-OCRv5;
- gestión de stock;
- entradas o salidas contables.

El listado corresponde a HU-29.

El detalle corresponde a HU-30.

La búsqueda y filtros corresponden a HU-31.

La eliminación corresponde a HU-32.

---

## 11. Servicio de guardado

Se creará:

```text
WithdrawalHistorySaveService
```

Ubicación recomendada:

```text
feature/withdrawal_history/create/
```

El servicio representará una operación real:

```text
validar borrador
+
convertirlo en agregado histórico
+
persistirlo
```

Responsabilidades:

- recibir `WithdrawalHistoryDraft`;
- validar defensivamente;
- obtener un único instante actual;
- crear `WithdrawalHistory`;
- crear `WithdrawalHistoryEntry`;
- crear `WithdrawalHistoryRecord`;
- llamar a `WithdrawalHistoryRepository.insert(...)`;
- entregar éxito con id;
- entregar error controlado;
- no depender de Android;
- no navegar;
- no mostrar diálogos;
- no acceder directamente al DAO.

No se creará una interfaz adicional para este servicio salvo que aparezca una segunda implementación real.

---

## 12. Conversión de `WithdrawalHistoryDraft`

La cabecera se construirá con:

```text
id = 0
title = draft.title
registeredAt = draft.registeredAt
createdAt = now
updatedAt = now
```

Regla:

```text
createdAt == updatedAt
```

en el alta inicial.

Cada línea se construirá con:

```text
id = 0
historyId = 0
orderIndex = draftEntry.orderIndex
category = draftEntry.category
code = draftEntry.code
quantity = draftEntry.quantity
unit = draftEntry.unit
warehouseItemIdSnapshot
siteSnapshot
positionSnapshot
locationStatus
```

El DAO sustituirá el `historyId` temporal por el id de la cabecera recién insertada.

---

## 13. Diferencia entre fechas

`registeredAt` representa:

```text
fecha documental elegida por el usuario
```

`createdAt` representa:

```text
momento real en que Room creó el registro
```

`updatedAt` representa inicialmente:

```text
el mismo instante de creación
```

Ejemplo válido:

```text
registeredAt = 1 ago 2026 · 10:00
createdAt = 2 ago 2026 · 03:50
updatedAt = 2 ago 2026 · 03:50
```

HU-28 no deberá reemplazar `registeredAt` por la hora actual.

---

## 14. Fuente de tiempo

El servicio deberá obtener el instante actual una única vez.

Opción coherente con el proyecto:

```java
System::currentTimeMillis
```

mediante una dependencia funcional pequeña.

Esto permitirá:

- probar timestamps;
- garantizar `createdAt == updatedAt`;
- evitar llamadas múltiples al reloj.

No se necesita una jerarquía compleja de clases de tiempo.

---

## 15. Validación final

HU-28 reutilizará las reglas de HU-27 y aplicará validación defensiva.

### Cabecera

- borrador no nulo;
- título opcional;
- título de máximo 120 caracteres;
- `registeredAt > 0`;
- fecha no inválida según la regla vigente.

### Colección

- no nula;
- no vacía;
- sin líneas nulas;
- índices no negativos;
- índices consecutivos;
- sin índices repetidos.

### Cantidad

- nula o positiva;
- no se admite cero;
- no se admiten negativos.

### Unidad

- opcional;
- máximo 30 caracteres;
- no podrá existir unidad sin cantidad, porque HU-27 ya rechaza esa combinación.

### `FOUND`

Debe conservar:

```text
warehouseItemIdSnapshot > 0
siteSnapshot no vacío
```

La posición podrá ser nula.

### `NOT_FOUND`

Debe conservar:

```text
warehouseItemIdSnapshot = null
siteSnapshot = null
positionSnapshot = null
```

---

## 16. Confirmación

El botón actual podrá cambiar de:

```text
Continuar
```

a:

```text
Guardar historial
```

Al pulsarlo:

1. el ViewModel valida;
2. si existen errores, se muestran;
3. si el borrador es válido, emite un evento de confirmación;
4. la Activity muestra un diálogo;
5. el usuario cancela o confirma;
6. solo la confirmación inicia la persistencia.

Mensaje orientativo:

```text
Guardar registro histórico

Se guardará esta lista con 15 referencias.
13 están encontradas y 2 no encontradas.

Las cantidades son documentales y no modificarán el stock.

[Cancelar] [Guardar]
```

No se utilizará confirmación destructiva.

No se exigirá escribir `delete`.

---

## 17. Eventos del ViewModel

El actual:

```text
UiEvent<WithdrawalHistoryDraft> continueEvent
```

deberá evolucionar hacia eventos con responsabilidades claras.

Alternativa recomendada:

```text
UiEvent<WithdrawalHistoryDraft> confirmationEvent
UiEvent<Long> savedEvent
```

El primero solicita que la Activity muestre la confirmación.

El segundo comunica el id histórico generado.

También podrá utilizarse un modelo único de evento si resulta más coherente con el proyecto, pero no deberá mezclar:

- confirmación;
- error;
- navegación;
- persistencia.

---

## 18. Método de guardado

El ViewModel deberá exponer:

```java
public void confirmSave();
```

o un nombre equivalente.

Flujo:

```text
requestSave()
    → validar
    → confirmationEvent

confirmSave()
    → comprobar que no se está guardando
    → WithdrawalHistorySaveService.save(...)
```

No deberá reconstruir el formulario desde Views.

El borrador deberá obtenerse del estado ya mantenido por el ViewModel.

---

## 19. Estado de interfaz

`WithdrawalHistoryCreateUiState.Status` deberá ampliarse.

Estados previstos:

```text
INITIALIZING
READY
INVALID_INPUT
SAVING
SAVED
ERROR
```

### `SAVING`

- campos deshabilitados;
- adapter no editable;
- botón deshabilitado;
- progreso visible;
- navegación Atrás bloqueada temporalmente;
- no se aceptan nuevos cambios.

### `SAVED`

- no se vuelve a insertar;
- evento de éxito pendiente de consumo;
- formulario no editable.

### `ERROR`

- borrador conservado;
- campos habilitados;
- mensaje controlado;
- guardado disponible para reintento.

No se deberá reutilizar el estado `ERROR` inicial de entrada inválida para perder todo el formulario después de un fallo de Room.

---

## 20. ViewModelFactory

La factory actual crea:

```java
new WithdrawalHistoryCreateViewModel(
        new WithdrawalHistoryDraftValidator()
)
```

HU-28 deberá recibir dependencias reales.

Firma orientativa:

```java
public WithdrawalHistoryCreateViewModelFactory(
        WithdrawalHistorySaveService saveService,
        WithdrawalHistoryDraftValidator validator
)
```

La factory no deberá obtener directamente:

- `Context`;
- DAO;
- base de datos;
- `AppContainer` global.

---

## 21. Composición de dependencias

`AppContainer` ya expone:

```java
provideWithdrawalHistoryRepository()
```

HU-28 deberá componer:

```text
WithdrawalHistoryRepository
        ↓
WithdrawalHistorySaveService
        ↓
WithdrawalHistoryCreateViewModelFactory
```

La composición podrá ubicarse en:

```text
AppContainer
```

o en un pequeño:

```text
WithdrawalHistoryModule
```

Se creará un módulo solo si agrupa una responsabilidad real y será reutilizado por HU-29, HU-30, HU-31 y HU-32.

Una opción razonable es introducir:

```text
app/di/WithdrawalHistoryModule.java
```

porque la feature histórica continuará creciendo en las siguientes historias.

---

## 22. Persistencia transaccional

HU-28 reutilizará:

```java
WithdrawalHistoryDao.insertHistoryWithEntries(...)
```

No deberá crear otra transacción.

Flujo:

```text
insertHistory(...)
        ↓
generatedId
        ↓
entry.withHistoryId(generatedId)
        ↓
insertEntries(...)
```

Ante cualquier excepción:

```text
rollback completo
```

No deberá quedar:

- cabecera sin líneas;
- líneas huérfanas;
- lista parcialmente guardada.

---

## 23. Referencias `FOUND`

Cada referencia encontrada conservará:

```text
category
code
quantity
unit
warehouseItemIdSnapshot
siteSnapshot
positionSnapshot
locationStatus = FOUND
```

HU-28 no volverá a consultar `warehouse_items`.

La instantánea será exactamente la revisada en HU-27.

Esto evita que un cambio concurrente de ubicación altere el documento que el usuario confirmó.

---

## 24. Referencias `NOT_FOUND`

Cada referencia no encontrada conservará:

```text
category
code
quantity
unit
warehouseItemIdSnapshot = null
siteSnapshot = null
positionSnapshot = null
locationStatus = NOT_FOUND
```

Una referencia no encontrada:

- no se elimina;
- no bloquea la lista;
- no crea mercadería;
- no recibe una ubicación inventada;
- forma parte del resumen y del historial.

---

## 25. Prevención de duplicados

HU-28 deberá impedir duplicados provocados por la misma interacción:

- doble pulsación;
- rotación;
- callback repetido;
- reobservación del evento;
- regreso desde segundo plano.

Mecanismos mínimos:

```text
isSaving
botón deshabilitado
estado SAVING
estado SAVED
evento único
```

HU-28 no implementará deduplicación global por contenido.

Dos listas diferentes podrán tener el mismo título, fecha o referencias si se registran en flujos independientes.

---

## 26. Resultado satisfactorio

Después de insertar:

```text
generatedId > 0
```

La Activity deberá:

1. consumir el evento una sola vez;
2. mostrar un mensaje breve;
3. preparar `RESULT_OK`;
4. incluir el id generado;
5. finalizar.

Contrato recomendado:

```text
EXTRA_SAVED_HISTORY_ID
```

Mensaje:

```text
Historial guardado.
```

HU-28 no abrirá todavía el detalle histórico.

---

## 27. Error de persistencia

Mensaje orientativo:

```text
No se pudo guardar el historial.
```

Comportamiento:

- conservar título;
- conservar fecha;
- conservar cantidades;
- conservar unidades;
- conservar instantáneas;
- conservar orden;
- volver a habilitar la pantalla;
- permitir reintentar;
- no mostrar excepción;
- no mostrar SQL;
- no finalizar la Activity.

La transacción deberá garantizar que el reintento empieza sin datos parciales.

---

## 28. Cancelación y navegación Atrás

### Antes de guardar

La pantalla podrá cerrarse normalmente o mantener el aviso de descarte definido por HU-27.

Room no deberá cambiar.

### Durante `SAVING`

Se recomienda bloquear temporalmente la salida.

La operación es local y breve; no se necesita:

- `WorkManager`;
- servicio en primer plano;
- persistencia de tareas.

### Después de `SAVED`

La pantalla no deberá aceptar otro guardado.

---

## 29. Rotación

HU-28 deberá garantizar:

- borrador conservado;
- `registeredAt` conservado;
- `isSaving` coherente;
- confirmación no repetida automáticamente;
- inserción no repetida;
- éxito consumido una sola vez;
- error recuperable;
- campos bloqueados mientras corresponde.

El ViewModel deberá sobrevivir a la recreación de la Activity.

---

## 30. Integridad de la mercadería

HU-28 no deberá llamar a:

```text
WarehouseItemRepository.update(...)
WarehouseItemDao.update(...)
WarehouseItemSaveService
```

Después del guardado deberán permanecer intactos:

```text
warehouse_items.category
warehouse_items.code
warehouse_items.site
warehouse_items.position
warehouse_items.observations
warehouse_items.created_at
warehouse_items.updated_at
```

Las únicas escrituras deberán ocurrir en:

```text
withdrawal_history
withdrawal_history_entries
```

---

## 31. Accesibilidad

HU-28 deberá verificar:

- texto claro para Guardar historial;
- confirmación comprensible;
- progreso anunciado;
- campos deshabilitados durante guardado;
- error anunciado una sola vez;
- reintento accesible;
- objetivos táctiles de 48 dp;
- contraste claro y oscuro;
- resumen no dependiente del color;
- foco correcto al cerrar el diálogo;
- mensaje de éxito accesible.

---

## 32. Privacidad

HU-28 deberá:

- guardar únicamente datos confirmados;
- no guardar fotografías;
- no guardar URI;
- no guardar texto OCR completo;
- no enviar historial;
- no solicitar Internet;
- no registrar datos completos en logs de producción;
- mantener toda la operación en Room local.

---

## 33. Pruebas del servicio

### Borrador válido

- crea cabecera;
- asigna `id = 0`;
- conserva `registeredAt`;
- asigna un único `now`;
- cumple `createdAt == updatedAt`;
- conserva líneas;
- conserva orden;
- delega una sola vez.

### Borrador inválido

- nulo;
- sin líneas;
- índice repetido;
- índice no consecutivo;
- `FOUND` sin id;
- `FOUND` sin sitio;
- `NOT_FOUND` con instantánea;
- cantidad inválida;
- unidad sin cantidad.

### Repositorio

- éxito con id válido;
- error;
- callback único.

---

## 34. Pruebas del ViewModel

- solicitud de confirmación;
- cancelación;
- inicio de guardado;
- estado `SAVING`;
- edición bloqueada;
- doble pulsación ignorada;
- éxito;
- evento con id;
- estado `SAVED`;
- error;
- borrador conservado;
- reintento;
- callback repetido ignorado;
- rotación durante guardado;
- rotación después de éxito.

---

## 35. Pruebas de integración

- guardar una lista completa;
- crear una cabecera;
- crear todas las líneas;
- vincular el id;
- conservar orden;
- conservar cantidades;
- conservar unidades;
- conservar `FOUND`;
- conservar `NOT_FOUND`;
- recuperar mediante `findById`;
- id generado mayor que cero;
- rollback ante error;
- ausencia de líneas huérfanas;
- mercadería intacta.

No se duplicarán pruebas DAO ya cubiertas por HU-26.

---

## 36. Criterios de aceptación

### CA-01 — Confirmación

**Dado** un borrador válido,  
**cuando** el usuario solicita guardar,  
**entonces** la aplicación muestra una confirmación.

### CA-02 — Cancelación

**Dado** el diálogo,  
**cuando** el usuario cancela,  
**entonces** el borrador permanece y Room no cambia.

### CA-03 — Guardado transaccional

**Dado** un borrador confirmado,  
**cuando** se guarda,  
**entonces** cabecera y líneas se insertan en una única transacción.

### CA-04 — Timestamps

**Dado** un borrador,  
**cuando** se persiste,  
**entonces** `registeredAt` se conserva y `createdAt` y `updatedAt` reciben el mismo instante.

### CA-05 — Encontradas

**Dada** una referencia `FOUND`,  
**cuando** se guarda,  
**entonces** conserva id, sitio y posición históricos.

### CA-06 — No encontradas

**Dada** una referencia `NOT_FOUND`,  
**cuando** se guarda,  
**entonces** permanece con instantáneas nulas.

### CA-07 — Cantidades documentales

**Dada** una cantidad válida,  
**cuando** se guarda,  
**entonces** se conserva sin modificar stock.

### CA-08 — Campos opcionales

**Dada** una lista sin título, cantidad o unidad,  
**cuando** se guarda,  
**entonces** la operación continúa siendo válida.

### CA-09 — Doble pulsación

**Dado** un guardado activo,  
**cuando** el usuario vuelve a pulsar,  
**entonces** no se crea otro registro.

### CA-10 — Error recuperable

**Dado** un error del repositorio,  
**cuando** se informa,  
**entonces** el borrador se conserva y puede reintentarse.

### CA-11 — Resultado

**Dado** un guardado satisfactorio,  
**cuando** se consume el evento,  
**entonces** la Activity devuelve el id una sola vez.

### CA-12 — Mercadería intacta

**Dado** cualquier guardado,  
**cuando** finaliza,  
**entonces** `warehouse_items` no ha cambiado.

### CA-13 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se guarda,  
**entonces** la operación funciona mediante Room.

---

## 37. Riesgos

### Guardado duplicado

**Mitigación:** estado `SAVING`, bloqueo del botón y evento único.

### Validación divergente

**Mitigación:** reutilizar `WithdrawalHistoryDraftValidator` y las invariantes del dominio.

### Instantánea alterada

**Mitigación:** no volver a consultar ubicaciones.

### Registro parcial

**Mitigación:** reutilizar la transacción de HU-26.

### Confusión con stock

**Mitigación:** confirmación explícita y ausencia de escrituras sobre mercadería.

### Exceso de clases

**Mitigación:** reutilizar los modelos y repositorio existentes; añadir solo el servicio, estados y composición necesarios.

---

## 38. Definición de terminado

HU-28 estará terminada cuando:

- el botón provisional se convierta en guardado real;
- exista confirmación explícita;
- exista `WithdrawalHistorySaveService`;
- la factory reciba dependencias;
- el ViewModel gestione guardado;
- existan estados `SAVING`, `SAVED` y error recuperable;
- el borrador se convierta en `WithdrawalHistoryRecord`;
- `registeredAt` se conserve;
- `createdAt` y `updatedAt` se asignen correctamente;
- todas las líneas se persistan;
- se conserven cantidades y unidades;
- se conserven instantáneas;
- se guarden referencias no encontradas;
- la operación sea transaccional;
- se eviten duplicados interactivos;
- se devuelva el id generado;
- el éxito se consuma una sola vez;
- el error permita reintento;
- la mercadería permanezca intacta;
- no se gestione stock;
- funcione sin conexión;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 39. Resultado esperado

Al cerrar HU-28:

```text
lista revisada
        ↓
WithdrawalHistoryDraft
        ↓
confirmación
        ↓
WithdrawalHistorySaveService
        ↓
WithdrawalHistoryRecord
        ↓
Room
        ↓
historial persistido
```

La aplicación podrá registrar listas documentales completas.

La consulta del historial comenzará en HU-29.
