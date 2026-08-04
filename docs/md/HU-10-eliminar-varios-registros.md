# HU-10 — Eliminar varios registros

> Décima historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-10  
**Nombre:** Eliminar varios registros  
**Prioridad:** Media  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-10-eliminar-varios-registros`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero seleccionar y eliminar varios registros de mercancía en una sola operación,  
para limpiar información obsoleta de manera más eficiente.

---

## 3. Objetivo

Implementar un modo de selección múltiple dentro del listado principal que permita:

- activar la selección de forma deliberada;
- seleccionar y deseleccionar registros visibles;
- conocer cuántos registros están seleccionados;
- cancelar sin modificar Room;
- confirmar la eliminación;
- eliminar los identificadores seleccionados mediante una única operación de persistencia;
- actualizar automáticamente el listado y sus estados.

La HU-10 deberá extender la arquitectura existente sin reutilizar incorrectamente la eliminación individual de HU-07 mediante un bucle.

Flujo previsto:

```text
WarehouseItemAdapter
        ↓ pulsación prolongada
WarehouseItemListViewModel
        ↓ conserva ids seleccionados
MainActivity
        ↓ confirmación
DeleteWarehouseItemsUseCase
        ↓
DeleteWarehouseItemsService
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao.deleteByIds(...)
        ↓
Room / SQLite
```

Después de confirmar la eliminación, Room continuará siendo la única fuente de verdad y actualizará automáticamente:

- el listado completo;
- la búsqueda activa;
- los filtros activos;
- las opciones de filtros;
- `EMPTY_DATABASE`;
- `NO_RESULTS`.

---

## 4. Justificación de entrada en la versión 1.0

El plan de la versión define HU-10 como una mejora de prioridad media y permite aplazarla cuando el núcleo obligatorio no sea estable.

Antes de esta historia ya se encuentran cerradas y operativas:

- consulta de listado;
- creación;
- detalle;
- búsqueda;
- filtros;
- edición;
- eliminación individual;
- prevención de duplicados;
- estados vacíos y sin resultados.

Por tanto, la condición de entrada se considera cumplida y HU-10 puede implementarse sin comprometer el núcleo obligatorio.

---

## 5. Estado real del proyecto antes de HU-10

El ZIP actualizado de HU-09 confirma que actualmente existen:

- `MainActivity`;
- `WarehouseItemAdapter` basado en `ListAdapter`;
- `DiffUtil`;
- navegación al detalle mediante clic;
- `WarehouseItemListViewModel`;
- `WarehouseItemListUiState`;
- `WarehouseItemFilterCriteria`;
- `NoResultsReason`;
- búsqueda y filtros combinados;
- `clearSearch()`;
- `clearFilters()`;
- `clearAllCriteria()`;
- estados `LOADING`, `CONTENT`, `EMPTY_DATABASE`, `NO_RESULTS` y `ERROR`;
- `DeleteWarehouseItemUseCase`;
- `DeleteWarehouseItemService`;
- `DeleteWarehouseItemResult`;
- `WarehouseItemDeleteCallback`;
- `WarehouseItemRepository.deleteById()`;
- `WarehouseItemDao.deleteById()`;
- actualización observable de Room;
- confirmación de eliminación individual desde el detalle.

El adapter actual:

- recibe un único listener de clic;
- abre directamente el detalle;
- no expone pulsación prolongada;
- no representa selección visual;
- no mantiene ids seleccionados.

El ViewModel del listado actual:

- conserva criterios de búsqueda y filtros;
- cambia fuentes observables;
- no mantiene selección;
- no ejecuta operaciones de eliminación.

El DAO actual:

- elimina un registro por id;
- dispone de `deleteAll()` únicamente para apoyo técnico o pruebas;
- no dispone de eliminación por conjunto de ids.

HU-10 deberá ampliar estos componentes de manera coherente.

---

## 6. Alcance incluido

HU-10 incluye:

- activar modo selección mediante pulsación prolongada;
- impedir activación accidental mediante clic normal;
- seleccionar el primer registro que activa el modo;
- alternar selección mediante clic cuando el modo esté activo;
- seleccionar varios registros visibles;
- deseleccionar registros;
- mostrar contador de seleccionados;
- representar visualmente cada fila seleccionada;
- mostrar una acción contextual de eliminación;
- mostrar una acción contextual para cancelar;
- cancelar mediante botón Atrás;
- no abrir detalle durante modo selección;
- impedir abrir formulario de alta durante modo selección;
- controlar interacción con búsqueda y filtros;
- bloquear o deshabilitar búsqueda y filtros mientras exista selección;
- conservar selección ante rotación;
- podar ids seleccionados que dejen de estar disponibles;
- confirmar eliminación múltiple;
- mostrar la cantidad que será eliminada;
- cancelar el diálogo;
- ejecutar una única operación SQL por conjunto de ids;
- validar ids;
- eliminar solo los ids seleccionados;
- distinguir éxito total, éxito parcial, ningún registro eliminado y error;
- impedir confirmaciones repetidas;
- mostrar estado de eliminación;
- limpiar selección tras resultado satisfactorio;
- actualizar automáticamente el listado;
- actualizar automáticamente las opciones de filtro;
- mostrar `EMPTY_DATABASE` si se eliminan todos los registros;
- mostrar `NO_RESULTS` si desaparecen todos los resultados visibles pero Room aún contiene registros;
- conservar criterios de búsqueda y filtros después de eliminar;
- pruebas unitarias;
- pruebas DAO;
- pruebas del repositorio;
- pruebas de ViewModel;
- pruebas de adapter e interfaz;
- pruebas instrumentadas necesarias;
- funcionamiento sin conexión.

---

## 7. Alcance excluido

HU-10 no incluye:

- seleccionar todos los registros de la base mediante una sola acción;
- seleccionar todos los resultados de páginas no cargadas;
- paginación;
- arrastrar para seleccionar;
- selección por rango;
- edición múltiple;
- cambio masivo de sitio;
- cambio masivo de posición;
- papelera;
- deshacer;
- recuperación;
- eliminación lógica;
- auditoría;
- historial;
- sincronización remota;
- selección persistente después de cerrar la aplicación;
- selección entre varias Activities;
- conservar selección cuando cambia deliberadamente la búsqueda o los filtros;
- mostrar una lista completa de nombres dentro del diálogo;
- reutilizar `deleteAll()` para simular eliminación múltiple;
- ejecutar un bucle de `deleteById()` desde la UI.

---

## 8. Precondiciones

Antes de comenzar HU-10 deberán cumplirse:

- HU-01 a HU-09 integradas en `develop`;
- CI de `develop` satisfactoria;
- listado principal estable;
- eliminación individual estable;
- estados vacíos y sin resultados cerrados;
- búsqueda y filtros operativos;
- `WarehouseItemAdapter` basado en ids estables;
- Room como fuente de verdad;
- no existencia de errores abiertos que comprometan el CRUD individual.

---

## 9. Decisión de interacción principal

### 9.1. Entrada al modo selección

El modo selección se activará mediante:

```text
pulsación prolongada sobre una fila
```

La fila que recibe la pulsación prolongada quedará seleccionada inmediatamente.

No se activará mediante clic normal.

### 9.2. Comportamiento del clic

Fuera del modo selección:

```text
clic → abrir detalle
```

Dentro del modo selección:

```text
clic → seleccionar o deseleccionar
```

### 9.3. Salida del modo selección

El modo finalizará cuando:

- el usuario pulse Cancelar;
- pulse Atrás;
- deseleccione el último elemento;
- finalice una eliminación total o parcial;
- el conjunto seleccionado quede vacío por cambios observables.

### 9.4. Razón de esta decisión

La pulsación prolongada:

- evita activar selección accidentalmente;
- conserva el comportamiento habitual del clic;
- es coherente con patrones Android;
- cumple el criterio del plan de versión;
- evita añadir checkboxes permanentes que recarguen la interfaz normal.

---

## 10. Alcance de la selección

La selección se limita al conjunto de registros actualmente visible cuando se activa el modo.

Reglas:

- solo pueden seleccionarse filas presentes en el adapter;
- la selección se identifica por `id`;
- no se selecciona por posición del RecyclerView;
- no se selecciona por categoría o código;
- los ids no visibles no deberán añadirse;
- una actualización de `DiffUtil` no deberá cambiar qué registros están seleccionados.

---

## 11. Interacción con búsqueda y filtros

### 11.1. Decisión recomendada

Mientras exista al menos un registro seleccionado:

- búsqueda deshabilitada;
- desplegable de categoría deshabilitado;
- desplegable de sitio deshabilitado;
- desplegable de posición deshabilitado;
- acción Limpiar filtros deshabilitada;
- acción de recuperación de `NO_RESULTS` no aplicable;
- FAB de alta oculto o deshabilitado.

### 11.2. Justificación

Permitir cambios de criterio durante la selección podría producir:

- registros seleccionados que desaparecen visualmente;
- eliminación de elementos que el usuario ya no ve;
- contador que no representa la pantalla actual;
- confusión sobre el alcance de la confirmación.

Bloquear temporalmente estos controles es más claro que conservar selecciones ocultas.

### 11.3. Conservación de criterios

Los criterios existentes antes de entrar en selección se mantendrán.

Al cancelar o finalizar:

- la búsqueda reaparece con el mismo texto;
- los filtros mantienen sus valores;
- Room recalcula el resultado si hubo eliminación.

### 11.4. Actualizaciones de Room durante selección

Si Room cambia por una actualización observable:

- se conservarán únicamente ids que sigan presentes en el resultado visible;
- los ids desaparecidos se eliminarán del conjunto seleccionado;
- si no queda ningún id, se abandonará el modo selección;
- no se seleccionarán automáticamente registros nuevos.

---

## 12. Representación de la selección

Se recomienda mantener un conjunto inmutable o defensivamente copiado:

```text
Set<Long> selectedWarehouseItemIds
```

No se utilizará:

```text
Set<Integer> selectedAdapterPositions
```

porque las posiciones cambian con:

- inserciones;
- eliminaciones;
- filtros;
- búsqueda;
- `DiffUtil`;
- reordenación.

### 12.1. Estado visual de una fila

Una fila seleccionada deberá mostrar al menos una diferencia clara:

- fondo o superficie seleccionada;
- icono de selección;
- borde;
- estado `activated`;
- descripción accesible.

La selección no deberá depender solo de un cambio de color.

### 12.2. Item layout

`item_warehouse.xml` podrá añadir:

```text
selectionIndicator
```

o usar estado activado del contenedor.

Debe conservar:

- identidad;
- ubicación;
- accesibilidad;
- área táctil.

---

## 13. Estado de interfaz de selección

La selección es un estado transversal sobre `CONTENT`.

Se recomienda no añadir nuevos valores al enum principal para cada combinación.

Modelo orientativo dentro de `WarehouseItemListUiState`:

```text
selectionMode
selectedIds
selectedCount
deletingSelection
selectionError
```

o un estado separado:

```text
WarehouseItemSelectionUiState
├── selectedIds
├── deleting
└── errorMessage
```

La solución deberá evitar combinaciones inválidas:

```text
EMPTY_DATABASE + selectedIds no vacíos
NO_RESULTS + selectedIds no vacíos
ERROR + deletingSelection
```

### 13.1. Estado normal

```text
selectionMode = false
selectedCount = 0
deletingSelection = false
```

### 13.2. Seleccionando

```text
selectionMode = true
selectedCount > 0
deletingSelection = false
```

### 13.3. Eliminando selección

```text
selectionMode = true
selectedCount > 0
deletingSelection = true
```

Durante esta fase:

- no se cambia selección;
- no se abre el detalle;
- no se vuelve a confirmar;
- acciones quedan deshabilitadas;
- se muestra progreso discreto.

---

## 14. Barra contextual

Al activar selección, la Toolbar normal deberá cambiar o complementarse con una barra contextual.

Contenido mínimo:

```text
[X]  3 seleccionados                  [Eliminar]
```

Acciones:

- cerrar/cancelar selección;
- mostrar contador;
- eliminar.

No es obligatorio utilizar `ActionMode` si complica la arquitectura existente. También es válida una Toolbar contextual propia dentro del layout, siempre que:

- sea accesible;
- sobreviva a rotación mediante el ViewModel;
- no duplique la Toolbar normal;
- restaure correctamente el estado.

### Recomendación

Para el proyecto actual basado en View Binding y una única Activity:

```text
MaterialToolbar contextual dentro de activity_main.xml
```

puede ser más predecible que `ActionMode`.

---

## 15. Confirmación de eliminación múltiple

Toda eliminación múltiple requerirá confirmación.

Título orientativo:

```text
Eliminar mercancía seleccionada
```

Mensaje para un elemento:

```text
¿Quieres eliminar 1 registro seleccionado?
Esta acción no se puede deshacer.
```

Mensaje para varios:

```text
¿Quieres eliminar 5 registros seleccionados?
Esta acción no se puede deshacer.
```

Acciones:

```text
Cancelar
Eliminar
```

Reglas:

- Cancelar no cambia Room;
- cerrar el diálogo se considera cancelación;
- confirmar ejecuta una única llamada al ViewModel;
- el diálogo usa el contador actual;
- no muestra ids internos;
- no requiere listar todos los códigos;
- no debe abrirse con selección vacía;
- no debe abrirse mientras ya se está eliminando.

---

## 16. Operación de eliminación por ids

### 16.1. No usar bucle de eliminación individual

No se implementará:

```java
for (Long id : selectedIds) {
    deleteWarehouseItemUseCase.deleteWarehouseItem(id, ...);
}
```

Problemas:

- múltiples operaciones independientes;
- resultados intermedios;
- más callbacks;
- riesgo de navegación o mensajes repetidos;
- peor rendimiento;
- comportamiento parcial difícil de controlar.

### 16.2. Consulta única

Se utilizará una operación SQL equivalente a:

```sql
DELETE FROM warehouse_items
WHERE id IN (:warehouseItemIds)
```

Room deberá devolver el número de filas afectadas.

### 16.3. Atomicidad

Una única sentencia `DELETE ... IN (...)` será la unidad de persistencia.

No será necesario añadir `@Transaction` para una sola sentencia, aunque será válido documentarlo si el DAO evoluciona a más operaciones.

---

## 17. Resultado de eliminación múltiple

Se añadirá:

```text
DeleteWarehouseItemsResult
```

Estados recomendados:

```text
SUCCESS
PARTIAL_SUCCESS
EMPTY_SELECTION
INVALID_IDS
NOT_FOUND
PERSISTENCE_ERROR
```

Datos útiles:

```text
requestedCount
deletedCount
```

### 17.1. SUCCESS

```text
deletedCount == requestedCount
AND requestedCount > 0
```

Mensaje:

```text
Se eliminaron 5 registros.
```

### 17.2. PARTIAL_SUCCESS

```text
0 < deletedCount < requestedCount
```

Puede ocurrir si algunos ids desaparecen entre selección y confirmación.

Mensaje:

```text
Se eliminaron 3 de 5 registros.
```

La operación no deberá considerarse un fallo total.

### 17.3. NOT_FOUND

```text
deletedCount == 0
```

Mensaje:

```text
Los registros seleccionados ya no están disponibles.
```

### 17.4. EMPTY_SELECTION

No se invoca el repositorio.

### 17.5. INVALID_IDS

Existe algún id menor o igual que cero.

No se invoca el repositorio.

### 17.6. PERSISTENCE_ERROR

Ocurre una excepción inesperada.

La selección se conserva para permitir reintento.

---

## 18. Flujo principal

1. El usuario abre AlmacenTracker.
2. Room muestra contenido.
3. El usuario mantiene pulsada una fila.
4. El ViewModel activa selección con su id.
5. La Toolbar contextual muestra `1 seleccionado`.
6. El usuario pulsa otras filas.
7. El contador se actualiza.
8. Pulsa Eliminar.
9. `MainActivity` muestra confirmación con la cantidad.
10. El usuario confirma.
11. El ViewModel bloquea cambios.
12. Invoca `DeleteWarehouseItemsUseCase`.
13. El servicio valida y copia los ids.
14. El repositorio ejecuta `deleteByIds`.
15. El DAO elimina mediante `IN`.
16. Room devuelve el número de filas.
17. El resultado se transforma.
18. El ViewModel emite evento de éxito.
19. Se limpia la selección.
20. La Toolbar normal se restaura.
21. Room actualiza el listado y filtros.
22. La UI muestra `CONTENT`, `NO_RESULTS` o `EMPTY_DATABASE`.
23. Se muestra confirmación breve.

---

## 19. Flujos alternativos

### FA-01 — Cancelar modo selección

1. Hay registros seleccionados.
2. El usuario pulsa cerrar o Atrás.
3. Se vacía la selección.
4. Room no cambia.
5. Se restaura la Toolbar normal.

### FA-02 — Deseleccionar el último

1. Solo queda un registro seleccionado.
2. El usuario lo pulsa.
3. El conjunto queda vacío.
4. El modo selección termina.

### FA-03 — Cancelar diálogo

1. Se abre confirmación.
2. El usuario pulsa Cancelar.
3. La selección permanece.
4. Puede modificarla o volver a confirmar.
5. Room no cambia.

### FA-04 — Pulsación normal fuera de selección

Se abre el detalle como antes de HU-10.

### FA-05 — Pulsación normal durante selección

Alterna el id; no abre detalle.

### FA-06 — Pulsación prolongada durante selección

Podrá comportarse como selección normal o ignorarse. La decisión deberá ser consistente y probada.

### FA-07 — Eliminar todos los registros de Room

1. El usuario selecciona todos los registros visibles.
2. Estos representan todos los registros existentes.
3. Confirma.
4. Room queda vacío.
5. El ViewModel muestra `EMPTY_DATABASE`.

### FA-08 — Eliminar todos los resultados filtrados

1. Existen criterios activos.
2. Se seleccionan todos los resultados visibles.
3. Room contiene otros registros fuera del criterio.
4. Se eliminan los visibles.
5. Se conserva búsqueda y filtros.
6. El estado pasa a `NO_RESULTS`.

### FA-09 — Eliminar parte de los resultados

Los restantes continúan visibles y el estado sigue en `CONTENT`.

### FA-10 — Registro desaparece antes de confirmar

El ViewModel poda la selección cuando recibe la nueva lista.

### FA-11 — Registro desaparece durante la operación

El DAO puede devolver menos filas; se emite `PARTIAL_SUCCESS`.

### FA-12 — Error inesperado

1. Room lanza excepción.
2. Se emite `PERSISTENCE_ERROR`.
3. La selección permanece.
4. Se reactiva la acción.
5. El usuario puede reintentar o cancelar.

### FA-13 — Doble confirmación

Solo se ejecuta una llamada mientras `deletingSelection` sea verdadero.

### FA-14 — Rotación durante selección

1. Existen ids seleccionados.
2. El usuario rota.
3. El ViewModel conserva el conjunto.
4. La nueva Activity restaura Toolbar, contador y filas seleccionadas.

### FA-15 — Rotación durante eliminación

1. La operación está activa.
2. El usuario rota.
3. No se inicia una segunda eliminación.
4. La nueva Activity recibe el resultado.

### FA-16 — Evento de éxito y recreación

El Toast, Snackbar o confirmación no se repite después de consumirse.

### FA-17 — Base o resultados cambian a vacío

El conjunto seleccionado se limpia antes de renderizar `EMPTY_DATABASE` o `NO_RESULTS`.

---

## 20. Criterios de aceptación

### CA-01 — Activación deliberada

**Dado** un listado con contenido,  
**cuando** el usuario hace clic normal,  
**entonces** no se activa selección y se abre el detalle.

### CA-02 — Pulsación prolongada

**Dado** un registro visible,  
**cuando** se mantiene pulsado,  
**entonces** entra en modo selección y ese registro queda seleccionado.

### CA-03 — Selección múltiple

**Dado** modo selección activo,  
**cuando** se pulsan otras filas,  
**entonces** se añaden o eliminan del conjunto.

### CA-04 — Identidad por id

**Dado** que el RecyclerView cambia posiciones,  
**cuando** se actualiza,  
**entonces** la selección continúa asociada a los ids correctos.

### CA-05 — Contador

**Dado** un conjunto seleccionado,  
**cuando** cambia,  
**entonces** el contador coincide exactamente.

### CA-06 — Representación visual

**Dado** un registro seleccionado,  
**cuando** se renderiza,  
**entonces** se identifica visualmente y de forma accesible.

### CA-07 — Cancelación

**Dado** registros seleccionados,  
**cuando** se cancela,  
**entonces** no se elimina ninguno.

### CA-08 — Botón Atrás

**Dado** modo selección activo,  
**cuando** se pulsa Atrás,  
**entonces** primero se cancela la selección y no se cierra la Activity.

### CA-09 — Confirmación obligatoria

**Dado** registros seleccionados,  
**cuando** se pulsa Eliminar,  
**entonces** Room no cambia hasta confirmar.

### CA-10 — Cantidad en diálogo

**Dado** N registros seleccionados,  
**cuando** se muestra confirmación,  
**entonces** el mensaje muestra N.

### CA-11 — Eliminación exacta

**Dado** varios ids seleccionados,  
**cuando** se confirma,  
**entonces** solo esos ids se eliminan.

### CA-12 — Consulta única

**Dado** una eliminación múltiple,  
**cuando** el repositorio persiste,  
**entonces** utiliza una operación por conjunto y no un bucle desde la UI.

### CA-13 — `deleteAll()` prohibido

**Dado** que no necesariamente están seleccionados todos los registros,  
**cuando** se elimina,  
**entonces** nunca se llama a `deleteAll()`.

### CA-14 — Criterios bloqueados

**Dado** modo selección activo,  
**cuando** se visualizan búsqueda y filtros,  
**entonces** no pueden modificarse hasta salir del modo.

### CA-15 — Criterios conservados

**Dado** una búsqueda o filtros activos,  
**cuando** finaliza selección o eliminación,  
**entonces** los criterios permanecen.

### CA-16 — Resultado parcial

**Dado** que solo parte de los ids existe al borrar,  
**cuando** Room devuelve menos filas,  
**entonces** se informa un éxito parcial controlado.

### CA-17 — Doble envío bloqueado

**Dado** que la eliminación está en curso,  
**cuando** se intenta confirmar otra vez,  
**entonces** no se ejecuta una segunda operación.

### CA-18 — Actualización observable

**Dado** que Room elimina filas,  
**cuando** emite cambios,  
**entonces** el listado se actualiza sin manipulación manual.

### CA-19 — Estado vacío

**Dado** que se eliminan todos los registros existentes,  
**cuando** se actualiza Room,  
**entonces** aparece `EMPTY_DATABASE`.

### CA-20 — Sin resultados

**Dado** que se eliminan todos los resultados actuales pero quedan otros registros,  
**cuando** se actualiza Room,  
**entonces** aparece `NO_RESULTS`.

### CA-21 — Rotación

**Dado** una selección activa,  
**cuando** se rota,  
**entonces** ids, contador y modo se conservan.

### CA-22 — Error controlado

**Dado** un error de persistencia,  
**cuando** ocurre,  
**entonces** la selección permanece y la aplicación no se cierra.

### CA-23 — Operación no bloqueante

**Dado** una eliminación múltiple,  
**cuando** Room procesa,  
**entonces** no se bloquea el hilo principal.

### CA-24 — Offline

**Dado** que no existe conexión,  
**cuando** se eliminan varios registros,  
**entonces** la operación funciona porque depende únicamente de Room.

---

## 21. Diseño técnico propuesto

### 21.1. Puerto de entrada

Se añadirá:

```text
DeleteWarehouseItemsUseCase
```

Firma orientativa:

```java
public interface DeleteWarehouseItemsUseCase {

    void deleteWarehouseItems(
            Set<Long> warehouseItemIds,
            DeleteWarehouseItemsCallback callback
    );
}
```

La colección deberá copiarse antes de iniciar trabajo asíncrono.

También podrá aceptarse `List<Long>` si el servicio elimina duplicados de manera segura.

### 21.2. Servicio de aplicación

`DeleteWarehouseItemsService` deberá:

1. rechazar colección nula o vacía;
2. eliminar ids duplicados;
3. validar que todos sean mayores que cero;
4. crear una copia inmutable;
5. invocar el puerto de salida;
6. comparar cantidad solicitada y eliminada;
7. emitir éxito total, parcial o not found;
8. transformar excepciones;
9. no depender de Android ni Room.

### 21.3. Callback de entrada

Se podrá crear:

```text
DeleteWarehouseItemsCallback
```

en `application.port.in`, siguiendo el patrón actual de los casos de uso.

No deberá exponer excepciones Room directamente a la Activity.

### 21.4. Resultado de aplicación

Se añadirá:

```text
DeleteWarehouseItemsResult
```

con:

```text
status
requestedCount
deletedCount
errorMessage opcional
```

### 21.5. Puerto de salida

`WarehouseItemRepository` se ampliará con:

```java
void deleteByIds(
        List<Long> warehouseItemIds,
        WarehouseItemsDeleteCallback callback
);
```

### 21.6. Callback de salida

Se añadirá:

```java
public interface WarehouseItemsDeleteCallback {

    void onComplete(int deletedCount);

    void onError(Throwable throwable);
}
```

El repositorio no necesita decidir si el resultado es total o parcial; esa interpretación pertenece al servicio de aplicación.

### 21.7. DAO

Se añadirá:

```java
@Query(
    "DELETE FROM warehouse_items " +
    "WHERE id IN (:warehouseItemIds)"
)
int deleteByIds(List<Long> warehouseItemIds);
```

La implementación deberá verificar el comportamiento de Room con listas vacías.

El servicio impedirá que una lista vacía llegue al DAO.

### 21.8. Adaptador Room

`RoomWarehouseItemRepository.deleteByIds()` deberá:

- copiar la lista;
- ejecutar en el executor;
- invocar una sola vez al DAO;
- devolver filas afectadas;
- transformar excepción;
- no usar `deleteById()` repetidamente;
- no usar `deleteAll()`.

### 21.9. ViewModel del listado

`WarehouseItemListViewModel` deberá ampliarse con:

```text
Set<Long> selectedWarehouseItemIds
boolean deletingSelection
UiEvent<DeleteWarehouseItemsResult>
```

Operaciones orientativas:

```java
public void startSelection(long warehouseItemId);

public void toggleSelection(long warehouseItemId);

public void clearSelection();

public void deleteSelectedItems();

public boolean hasSelection();
```

También deberá:

- verificar que el id pertenece al resultado visible;
- podar ids tras cada emisión de contenido;
- bloquear cambios mientras elimina;
- limpiar selección tras éxito total o parcial;
- conservar selección tras rotación;
- no mezclar selección con criterios.

### 21.10. Estado de UI

`WarehouseItemListUiState` podrá incluir una copia inmutable de ids seleccionados.

Alternativamente, el ViewModel podrá exponer:

```text
LiveData<WarehouseItemSelectionUiState>
```

Recomendación:

- mantener el estado de listado centrado en consulta;
- añadir un estado separado de selección si evita inflar una clase ya compleja;
- garantizar que ambos estados se renderizan de forma coordinada.

### 21.11. Adapter

`WarehouseItemAdapter` deberá aceptar:

```java
OnWarehouseItemClickListener
OnWarehouseItemLongClickListener
```

y un conjunto de ids seleccionados.

Operaciones orientativas:

```java
public void submitSelection(Set<Long> selectedIds);
```

El ViewHolder deberá:

- configurar clic;
- configurar pulsación prolongada;
- usar `warehouseItem.getId()` para saber si está seleccionado;
- limpiar estado reciclado en cada `bind`;
- no guardar selección dentro del ViewHolder.

Puede utilizarse payload para actualizar solo la selección, pero no es obligatorio si la implementación simple mantiene rendimiento adecuado.

### 21.12. Activity

`MainActivity` deberá:

- delegar clic normal según selección;
- delegar pulsación prolongada;
- renderizar contador;
- mostrar Toolbar contextual;
- gestionar Atrás mediante `OnBackPressedDispatcher`;
- mostrar diálogo;
- observar evento de eliminación;
- mostrar mensaje;
- habilitar y deshabilitar controles;
- no construir SQL;
- no recorrer ids para eliminar;
- no conservar selección como fuente principal.

### 21.13. Layout

`activity_main.xml` podrá añadir:

```text
selectionToolbar
├── closeSelectionButton
├── selectedCountText
├── deleteSelectedButton
└── selectionProgress
```

La Toolbar normal podrá ocultarse durante selección.

`item_warehouse.xml` deberá añadir representación de selección.

### 21.14. Composición

`AppContainer` deberá crear y proporcionar:

- `DeleteWarehouseItemsService`;
- `DeleteWarehouseItemsUseCase`;
- Factory actualizada de `WarehouseItemListViewModel`.

No se añadirá un framework de inyección exclusivamente para esta HU.

---

## 22. Decisiones técnicas importantes

### 22.1. Caso de uso separado

No se reutilizará `DeleteWarehouseItemUseCase` dentro de un bucle.

La operación múltiple tiene:

- validación distinta;
- resultado agregado;
- una sola confirmación;
- una sola consulta;
- estados parciales.

### 22.2. Selección en ViewModel

La Activity no será la fuente de verdad de los ids seleccionados.

Esto permite conservarla ante rotación.

### 22.3. Selección por id

Nunca se seleccionará por posición de adapter.

### 22.4. No cambiar filtros durante selección

Se prioriza claridad y seguridad sobre flexibilidad.

### 22.5. No seleccionar automáticamente nuevos registros

Una emisión observable no debe ampliar la intención original.

### 22.6. No ocultar éxito parcial

Si se solicitaron cinco ids y se eliminaron tres, el usuario debe recibir una confirmación precisa.

### 22.7. No mantener selección después de éxito

Después de éxito total o parcial, se abandona modo selección.

### 22.8. Conservar selección después de error

Permite reintentar sin seleccionar de nuevo.

### 22.9. Room actualiza la UI

No se eliminarán elementos manualmente del adapter como fuente de verdad.

### 22.10. `deleteAll()` no pertenece al flujo

Aunque se hayan seleccionado todos los visibles, podrían existir registros fuera de los filtros.

---

## 23. Estructura de archivos orientativa

HU-10 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── DeleteWarehouseItemsUseCase.java
│   │   │   └── DeleteWarehouseItemsCallback.java
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       └── WarehouseItemsDeleteCallback.java
│   ├── result/
│   │   └── DeleteWarehouseItemsResult.java
│   └── service/
│       └── DeleteWarehouseItemsService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   └── MainActivity.java
│   │       ├── adapter/
│   │       │   └── WarehouseItemAdapter.java
│   │       ├── state/
│   │       │   ├── WarehouseItemListUiState.java
│   │       │   └── WarehouseItemSelectionUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemListViewModel.java
│   │           └── WarehouseItemListViewModelFactory.java
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               │   └── WarehouseItemDao.java
│               └── repository/
│                   └── RoomWarehouseItemRepository.java
└── configuration/
    └── AppContainer.java

res/
├── drawable/
│   ├── ic_close.xml
│   ├── ic_delete.xml
│   └── selector_warehouse_item.xml
├── layout/
│   ├── activity_main.xml
│   └── item_warehouse.xml
└── values/
    └── strings.xml
```

La estructura deberá adaptarse al código real.

No será obligatorio crear `WarehouseItemSelectionUiState` si `WarehouseItemListUiState` puede ampliarse sin perder claridad.

---

## 24. Diseño de interfaz esperado

### 24.1. Estado normal

```text
AlmacenTracker

[Buscar...]

[Categoría] [Sitio] [Posición]

Listado
```

Clic:

```text
abrir detalle
```

Pulsación prolongada:

```text
activar selección
```

### 24.2. Modo selección

```text
[X]  3 seleccionados                  [Eliminar]

[controles de búsqueda y filtros deshabilitados]

✓ MR · 1050
  Sitio A1 · Nivel 2

  MD · 2040
  Sitio B1

✓ CA · 3000
  Sitio C2
```

### 24.3. Confirmación

```text
Eliminar mercancía seleccionada

¿Quieres eliminar 3 registros seleccionados?
Esta acción no se puede deshacer.

[Cancelar] [Eliminar]
```

### 24.4. Eliminando

```text
[X deshabilitado]  3 seleccionados   [Progreso]
```

### 24.5. Accesibilidad

- contador anunciable;
- fila seleccionada con estado accesible;
- botón destructivo con etiqueta;
- cierre con etiqueta “Cancelar selección”;
- no depender solo del color;
- tamaño táctil suficiente.

---

## 25. Pruebas recomendadas

### 25.1. DeleteWarehouseItemsService

- rechaza colección nula;
- rechaza colección vacía;
- rechaza ids cero;
- rechaza ids negativos;
- elimina ids duplicados;
- copia la colección;
- delega ids válidos;
- devuelve éxito total;
- devuelve éxito parcial;
- devuelve not found;
- transforma error;
- no depende de Android ni Room.

### 25.2. WarehouseItemListViewModel

- inicia sin selección;
- activa con long press;
- selecciona el primer id;
- alterna ids;
- evita ids no visibles;
- deseleccionar último cierra modo;
- `clearSelection()` vacía;
- conserva selección con nueva instancia de Activity;
- poda ids desaparecidos;
- no selecciona nuevos registros;
- bloquea cambios durante eliminación;
- invoca el caso de uso una vez;
- emite éxito total una vez;
- emite éxito parcial una vez;
- conserva selección en error;
- limpia selección en éxito;
- mantiene criterios;
- no permite selección en estados sin contenido.

### 25.3. WarehouseItemAdapter

- clic normal abre detalle;
- long press devuelve true;
- long press comunica id;
- clic en selección alterna;
- representa fila seleccionada;
- limpia visual al reciclar;
- usa id y no posición;
- DiffUtil mantiene identidades;
- cambio de selección actualiza la fila adecuada.

### 25.4. DAO

- elimina un id;
- elimina varios ids;
- devuelve número exacto;
- ignora ids inexistentes;
- devuelve cero si ninguno existe;
- no elimina ids no incluidos;
- no afecta registros con mismo código y otra categoría;
- actualiza `observeAll()`;
- actualiza consulta filtrada;
- actualiza opciones;
- no usa `deleteAll()`.

### 25.5. RoomWarehouseItemRepository

- ejecuta una sola llamada DAO;
- ejecuta en executor;
- copia ids;
- devuelve cantidad;
- transforma excepción;
- no llama repetidamente a `deleteById()`;
- no llama a `deleteAll()`.

### 25.6. MainActivity

- long press activa selección;
- toolbar contextual visible;
- toolbar normal restaurada;
- contador correcto;
- controles bloqueados;
- FAB bloqueado u oculto;
- Back cancela;
- diálogo muestra cantidad;
- Cancelar conserva selección;
- Confirmar delega una vez;
- muestra progreso;
- muestra éxito;
- muestra parcial;
- muestra error sin abandonar;
- no abre detalle durante selección.

### 25.7. Integración

- seleccionar dos y eliminar;
- cancelar;
- eliminar todos los registros;
- eliminar todos los resultados filtrados;
- eliminar parte de resultados;
- búsqueda activa antes de selección;
- filtros activos antes de selección;
- rotación;
- registro desaparece antes de confirmar;
- error simulado;
- cierre y reapertura;
- funcionamiento sin conexión.

### 25.8. Pruebas manuales

- clic corto;
- pulsación prolongada;
- selección y deselección rápida;
- lista con muchos registros;
- mismo código en categorías distintas;
- cambiar orientación;
- pulsar Atrás;
- cancelar diálogo;
- pulsar confirmar repetidamente;
- modo oscuro;
- pantalla pequeña;
- TalkBack básico;
- búsqueda activa;
- filtros activos;
- `EMPTY_DATABASE`;
- `NO_RESULTS`.

---

## 26. Tareas de implementación

1. Confirmar HU-09 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado de HU-09.
4. Crear `feature/hu-10-eliminar-varios-registros`.
5. Definir comportamiento de selección.
6. Crear `DeleteWarehouseItemsResult`.
7. Crear callback de entrada.
8. Crear `DeleteWarehouseItemsUseCase`.
9. Crear `DeleteWarehouseItemsService`.
10. Crear callback de salida.
11. Ampliar `WarehouseItemRepository`.
12. Añadir `deleteByIds()` al DAO.
13. Implementar `deleteByIds()` en Room repository.
14. Prohibir bucle de `deleteById()`.
15. Prohibir `deleteAll()`.
16. Ampliar Factory y `AppContainer`.
17. Crear estado de selección o ampliar el existente.
18. Ampliar `WarehouseItemListViewModel`.
19. Mantener ids seleccionados.
20. Podar ids no visibles.
21. Bloquear doble eliminación.
22. Añadir eventos de resultado.
23. Ampliar listeners del adapter.
24. Añadir long press.
25. Añadir representación visual.
26. Actualizar `item_warehouse.xml`.
27. Añadir Toolbar contextual.
28. Actualizar `activity_main.xml`.
29. Gestionar botón Atrás.
30. Bloquear búsqueda y filtros.
31. Bloquear u ocultar FAB.
32. Mostrar diálogo con pluralización.
33. Renderizar progreso.
34. Mostrar éxito total.
35. Mostrar éxito parcial.
36. Mostrar error.
37. Ampliar strings y drawables.
38. Crear pruebas de servicio.
39. Ampliar pruebas de ViewModel.
40. Crear pruebas DAO.
41. Crear pruebas de repositorio.
42. Crear pruebas de adapter e interfaz.
43. Ejecutar pruebas instrumentadas.
44. Ejecutar `./gradlew testDebugUnitTest`.
45. Ejecutar `./gradlew lintDebug`.
46. Ejecutar `./gradlew assembleDebug`.
47. Publicar commits representativos con `#12`.
48. Verificar CI en la rama.
49. Revisar criterios de aceptación.
50. Recopilar evidencias.
51. Fusionar localmente en `develop`.
52. Verificar CI en `develop`.
53. Eliminar la rama local y remota tras confirmar la integración.

---

## 27. Evidencias necesarias para cerrar HU-10

- clic normal abre detalle;
- long press activa selección;
- primer elemento queda seleccionado;
- selección de varios registros;
- deselección;
- contador correcto;
- representación visual;
- Toolbar contextual;
- búsqueda y filtros bloqueados;
- FAB bloqueado u oculto;
- Back cancela;
- diálogo con cantidad;
- cancelar mantiene registros;
- confirmar elimina seleccionados;
- ids no seleccionados permanecen;
- mismo código en otra categoría permanece si no fue seleccionado;
- evidencia de una sola operación DAO;
- evidencia de que no se usa `deleteAll()`;
- evidencia de que no se usa un bucle de eliminación individual;
- éxito parcial controlado;
- error conserva selección;
- doble confirmación bloqueada;
- rotación conserva selección;
- rotación durante eliminación no duplica;
- `EMPTY_DATABASE` al eliminar todos;
- `NO_RESULTS` al eliminar todos los resultados visibles;
- criterios conservados;
- opciones de filtros actualizadas;
- pruebas unitarias;
- pruebas DAO;
- pruebas instrumentadas;
- lint;
- compilación debug;
- CI satisfactoria en `feature/hu-10-eliminar-varios-registros`;
- merge local en `develop`;
- CI satisfactoria en `develop`;
- funcionamiento sin conexión;
- confirmación de que MainActivity no accede a Room.

---

## 28. Definición de terminado

HU-10 estará terminada cuando:

- el modo selección solo se active deliberadamente;
- el clic normal conserve la navegación al detalle;
- long press seleccione el primer registro;
- la selección utilice ids;
- puedan seleccionarse y deseleccionarse varios;
- el contador sea exacto;
- la selección sea visible y accesible;
- Atrás cancele antes de cerrar;
- exista una Toolbar contextual;
- búsqueda, filtros y alta no cambien durante selección;
- los criterios previos se conserven;
- la eliminación requiera confirmación;
- el diálogo muestre la cantidad;
- se utilice un caso de uso múltiple;
- se utilice una consulta `DELETE ... IN (...)`;
- no se ejecute un bucle desde la Activity;
- no se utilice `deleteAll()`;
- solo se eliminen ids seleccionados;
- se diferencie éxito total;
- se diferencie éxito parcial;
- se diferencie not found;
- se controle error;
- se bloquee doble envío;
- la selección se limpie tras éxito;
- la selección se conserve tras error;
- Room actualice automáticamente la UI;
- aparezca `EMPTY_DATABASE` cuando corresponda;
- aparezca `NO_RESULTS` cuando corresponda;
- las opciones de filtro se actualicen;
- la selección sobreviva a rotación;
- no se repitan eventos;
- la operación no bloquee el hilo principal;
- funcione sin conexión;
- la UI no acceda al DAO;
- dominio y aplicación no dependan de Android ni Room;
- pruebas unitarias finalicen correctamente;
- pruebas DAO finalicen correctamente;
- pruebas instrumentadas necesarias finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- CI de la rama finalice correctamente;
- todos los criterios estén verificados;
- la rama se fusione localmente en `develop`;
- CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 29. Resultado esperado

Al cerrar HU-10, el usuario podrá eliminar varios registros mediante un flujo seguro:

```text
long press
    ↓
modo selección
    ↓
seleccionar ids visibles
    ↓
confirmar cantidad
    ↓
DELETE WHERE id IN (...)
    ↓
Room actualiza
    ↓
CONTENT / NO_RESULTS / EMPTY_DATABASE
```

La versión quedará preparada para continuar con:

```text
HU-11 — Utilizar la aplicación sin conexión
```

---

## 30. Commit documental recomendado

```text
git commit -m "docs: add HU-10 multiple warehouse item deletion plan #12"
```
