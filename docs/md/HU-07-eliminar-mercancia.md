# HU-07 — Eliminar mercancía

> Séptima historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-07  
**Nombre:** Eliminar mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-07-eliminar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero eliminar una mercancía incorrecta u obsoleta,  
para mantener actualizada la información almacenada.

---

## 3. Objetivo

Implementar la eliminación individual de mercancía desde la pantalla de detalle, exigiendo siempre una confirmación explícita antes de modificar Room.

La eliminación deberá ejecutarse mediante la arquitectura hexagonal existente:

```text
ItemDetailActivity
        ↓
confirmación del usuario
        ↓
WarehouseItemDetailViewModel
        ↓
DeleteWarehouseItemUseCase
        ↓
DeleteWarehouseItemService
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
Room / SQLite
```

Después de eliminar correctamente:

- el detalle deberá cerrarse;
- el listado deberá actualizarse automáticamente;
- la búsqueda y los filtros deberán recalcularse;
- las opciones de filtros deberán actualizarse;
- si era el último registro, deberá mostrarse `EmptyDatabase`;
- si era el último resultado de una consulta activa, deberá mostrarse `NoResults`.

Room continuará siendo la única fuente de verdad.

---

## 4. Estado real del proyecto antes de la HU-07

La implementación actual dispone de:

- `MainActivity` con listado, búsqueda y filtros;
- `WarehouseItemListViewModel` con fuentes observables;
- `ItemDetailActivity`;
- `WarehouseItemDetailViewModel`;
- `WarehouseItemDetailUiState`;
- `GetWarehouseItemDetailUseCase`;
- `ItemFormActivity` en modos CREATE y EDIT;
- `CreateWarehouseItemUseCase`;
- `UpdateWarehouseItemUseCase`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao`;
- `AppContainer`;
- botón Editar habilitado en estado `CONTENT`;
- botón Eliminar ya presente en `activity_item_detail.xml`;
- recurso `delete_action`;
- consulta observable por `id`;
- operación `findById()`;
- operación `update()`;
- operación `deleteAll()` utilizada únicamente para apoyo técnico o pruebas;
- actualización automática de detalle, listado, búsqueda y filtros.

Todavía no existen:

- `DeleteWarehouseItemUseCase`;
- `DeleteWarehouseItemService`;
- `DeleteWarehouseItemResult`;
- callback de eliminación;
- operación de repositorio para eliminar un registro;
- DAO para eliminar por `id`;
- estado de eliminación en el ViewModel;
- confirmación funcional desde el detalle.

La HU-07 deberá extender estas clases sin introducir una Activity específica para eliminar.

---

## 5. Alcance incluido

La HU-07 incluye:

- habilitar el botón Eliminar cuando el detalle se encuentre en `CONTENT`;
- mantenerlo deshabilitado en otros estados;
- iniciar la eliminación desde `ItemDetailActivity`;
- mostrar un diálogo de confirmación;
- identificar claramente la mercancía que será eliminada;
- permitir cancelar el diálogo;
- confirmar la eliminación;
- eliminar únicamente por `id`;
- utilizar un caso de uso específico;
- ejecutar Room fuera del hilo principal;
- distinguir éxito, registro inexistente y error;
- impedir pulsaciones repetidas mientras se elimina;
- deshabilitar Editar y Eliminar durante la operación;
- mostrar progreso o estado visual coherente;
- cerrar el detalle tras éxito;
- mostrar confirmación breve;
- actualizar automáticamente el listado;
- actualizar automáticamente búsqueda y filtros;
- actualizar automáticamente las opciones de filtros;
- mostrar `EmptyDatabase` si se elimina el último registro;
- mostrar `NoResults` si dejan de existir coincidencias;
- controlar el caso en que el registro ya no exista;
- controlar errores inesperados;
- conservar la aplicación estable ante rotación;
- evitar repetir la eliminación después de recrear la Activity;
- pruebas unitarias, DAO y de interfaz relacionadas.

---

## 6. Alcance excluido

La HU-07 no incluye:

- eliminación múltiple;
- selección de varios registros;
- papelera;
- deshacer eliminación;
- recuperación de registros eliminados;
- eliminación lógica;
- historial de eliminaciones;
- auditoría;
- permisos;
- autenticación;
- eliminación remota;
- sincronización;
- borrado en cascada de entidades relacionadas;
- confirmación mediante contraseña;
- archivado;
- exportación previa.

La eliminación múltiple pertenece a la HU-10.

La HU-07 implementará eliminación física local porque la versión 1.0 no contempla historial, auditoría ni sincronización.

---

## 7. Precondiciones

Antes de comenzar la HU-07 deberán cumplirse estas condiciones:

- HU-01 integrada en `develop`;
- HU-02 integrada en `develop`;
- HU-03 integrada en `develop`;
- HU-04 integrada en `develop`;
- HU-05 integrada en `develop`;
- HU-06 integrada en `develop`;
- CI de `develop` satisfactoria;
- detalle observable operativo;
- edición operativa;
- listado, búsqueda y filtros operativos;
- botón Eliminar presente en el layout;
- Room estable;
- `AppContainer` como composición explícita;
- Room continúa siendo la única fuente de verdad.

---

## 8. Regla principal de eliminación

Toda eliminación individual deberá requerir confirmación explícita.

No se permitirá:

```text
pulsar Eliminar
        ↓
borrado inmediato
```

Flujo obligatorio:

```text
pulsar Eliminar
        ↓
mostrar confirmación
        ↓
Cancelar o Eliminar
```

La opción destructiva deberá estar claramente diferenciada de Cancelar.

---

## 9. Identificación de la mercancía

La eliminación deberá utilizar exclusivamente el identificador interno:

```text
warehouseItemId
```

No se eliminará por:

```text
categoría + código
```

porque:

- esos campos pueden modificarse;
- la clave primaria identifica una sola fila;
- evita ambigüedad;
- mantiene coherencia con detalle y edición.

La Activity no deberá enviar una entidad Room al caso de uso.

---

## 10. Contenido del diálogo de confirmación

El diálogo deberá permitir reconocer el registro antes de borrar.

Título orientativo:

```text
Eliminar mercancía
```

Mensaje orientativo:

```text
¿Quieres eliminar MR · 1050?
Esta acción no se puede deshacer.
```

Acciones:

```text
Cancelar
Eliminar
```

Reglas:

- `Cancelar` no modifica Room;
- `Eliminar` inicia el caso de uso;
- el diálogo no deberá abrirse si el detalle no contiene un registro válido;
- el mensaje no deberá mostrar `id`;
- los datos mostrados procederán del contenido actual del detalle.

Podrá usarse `MaterialAlertDialogBuilder` para mantener coherencia con Material Components.

---

## 11. Estados relacionados con la eliminación

La consulta del detalle seguirá utilizando:

```text
LOADING
CONTENT
NOT_FOUND
INVALID_ID
ERROR
```

La HU-07 deberá añadir estado de eliminación sin crear combinaciones contradictorias.

### 11.1. Content

El registro existe.

Características:

- Editar habilitado;
- Eliminar habilitado;
- contenido visible.

### 11.2. ConfirmingDelete

El diálogo está visible.

Puede mantenerse como estado local de UI si no contiene lógica persistente.

No será obligatorio modelarlo en el ViewModel si el diálogo solo captura una decisión del usuario.

### 11.3. Deleting

La eliminación está en curso.

Características:

- Editar deshabilitado;
- Eliminar deshabilitado;
- pulsaciones repetidas ignoradas;
- indicador discreto;
- contenido puede permanecer visible;
- no iniciar una segunda eliminación.

### 11.4. DeleteSuccess

Room confirmó la eliminación.

Acciones:

- evento de una sola consumición;
- mostrar confirmación;
- finalizar `ItemDetailActivity`;
- regresar al listado.

### 11.5. DeleteNotFound

El registro ya no existe.

Mensaje orientativo:

```text
La mercancía ya no está disponible.
```

Acciones:

- no tratarlo como éxito silencioso;
- no ejecutar una segunda operación;
- cerrar el detalle o mostrar estado `NOT_FOUND`;
- permitir volver al listado.

### 11.6. DeleteError

Ocurrió un error inesperado.

Mensaje orientativo:

```text
No se pudo eliminar la mercancía.
```

Características:

- no cerrar la Activity;
- volver a habilitar acciones si el registro sigue disponible;
- no mostrar excepciones técnicas;
- permitir reintentar.

---

## 12. Flujo principal

1. El usuario abre el detalle de una mercancía.
2. Room devuelve el registro.
3. `ItemDetailActivity` muestra estado `CONTENT`.
4. Editar y Eliminar están habilitados.
5. El usuario pulsa Eliminar.
6. La Activity muestra un diálogo de confirmación.
7. El usuario confirma.
8. La Activity delega al ViewModel.
9. El ViewModel cambia a `Deleting`.
10. El ViewModel invoca `DeleteWarehouseItemUseCase`.
11. El servicio valida el `id`.
12. El servicio solicita la eliminación al repositorio.
13. `RoomWarehouseItemRepository` ejecuta la operación en el executor.
14. El DAO elimina la fila por `id`.
15. El DAO devuelve una fila afectada.
16. El repositorio devuelve éxito.
17. El ViewModel emite `DeleteSuccess`.
18. La Activity muestra confirmación.
19. La Activity finaliza.
20. `MainActivity` vuelve al frente.
21. Room actualiza el listado y las opciones.

---

## 13. Flujos alternativos

### FA-01 — Cancelar confirmación

1. El usuario pulsa Eliminar.
2. Se muestra el diálogo.
3. Pulsa Cancelar.
4. El diálogo se cierra.
5. Room no cambia.
6. El detalle permanece visible.

### FA-02 — Cerrar diálogo

1. El diálogo está abierto.
2. El usuario pulsa fuera o utiliza Atrás, si el diseño lo permite.
3. Se interpreta como cancelación.
4. No se elimina el registro.

También será válido impedir el cierre externo si se mantiene claramente disponible Cancelar.

### FA-03 — Último registro de la base

1. Solo existe una mercancía.
2. El usuario la elimina.
3. El detalle se cierra.
4. `MainActivity` recibe una lista vacía.
5. Se muestra `EmptyDatabase`.

### FA-04 — Último resultado de búsqueda

1. Existe una búsqueda activa con un único resultado.
2. El usuario abre ese resultado y lo elimina.
3. Vuelve al listado.
4. La base aún contiene otros registros.
5. Se muestra `NoResults`.

### FA-05 — Último resultado filtrado

1. Existen filtros activos con un único resultado.
2. Se elimina ese registro.
3. La base aún contiene datos.
4. El estado cambia a `NoResults`.
5. Los filtros se conservan.

### FA-06 — Cambio de opciones

1. El registro eliminado era el único con categoría, sitio o posición determinados.
2. Room elimina la fila.
3. Las opciones observables se recalculan.
4. El valor puede desaparecer de los filtros disponibles.

### FA-07 — Registro ya inexistente

1. El detalle conserva un id válido.
2. La eliminación afecta cero filas.
3. El repositorio devuelve `NOT_FOUND`.
4. El ViewModel no emite éxito.
5. La Activity muestra que el registro ya no existe.

### FA-08 — Doble pulsación

1. El usuario confirma varias veces o pulsa Eliminar repetidamente.
2. Solo se ejecuta una operación.
3. Los controles quedan deshabilitados durante `Deleting`.

### FA-09 — Rotación antes de confirmar

1. El diálogo está abierto.
2. El dispositivo rota.
3. La aplicación no deberá eliminar automáticamente.
4. El usuario deberá volver a confirmar o el diálogo deberá restaurarse de forma segura.

### FA-10 — Rotación durante eliminación

1. Room está procesando la eliminación.
2. El dispositivo rota.
3. El ViewModel mantiene `Deleting`.
4. No se lanza una segunda operación.
5. El resultado se entrega a la nueva Activity.

### FA-11 — Éxito seguido de recreación

1. La eliminación termina.
2. Se emite evento de éxito.
3. La Activity se recrea.
4. El evento no debe ejecutarse dos veces.
5. No deben aparecer dos Toast ni dos navegaciones.

### FA-12 — Error inesperado

1. El DAO o repositorio produce una excepción.
2. El error se transforma.
3. El ViewModel emite `DeleteError`.
4. La Activity permanece abierta.
5. Se permite reintentar.

### FA-13 — Pulsar Editar mientras elimina

1. La eliminación está activa.
2. Editar se encuentra deshabilitado.
3. No se abre el formulario.

### FA-14 — Room emite NotFound antes del callback

1. El detalle observa el registro.
2. La fila desaparece.
3. El estado observable cambia a `NOT_FOUND`.
4. La Activity no deberá mostrar acciones habilitadas.
5. La navegación final seguirá siendo segura.

---

## 14. Criterios de aceptación

### CA-01 — Acción Eliminar disponible

**Dado** que el detalle contiene un registro válido,  
**cuando** se renderiza `CONTENT`,  
**entonces** Eliminar está habilitado.

### CA-02 — Acción deshabilitada fuera de contenido

**Dado** un estado `LOADING`, `NOT_FOUND`, `INVALID_ID` o `ERROR`,  
**cuando** se renderiza el detalle,  
**entonces** Eliminar está deshabilitado.

### CA-03 — Confirmación obligatoria

**Dado** que el usuario pulsa Eliminar,  
**cuando** todavía no confirma,  
**entonces** Room no cambia.

### CA-04 — Identificación comprensible

**Dado** un registro válido,  
**cuando** se muestra el diálogo,  
**entonces** aparecen categoría y código sin mostrar el id interno.

### CA-05 — Cancelación

**Dado** que el diálogo está visible,  
**cuando** el usuario pulsa Cancelar,  
**entonces** el registro permanece.

### CA-06 — Eliminación confirmada

**Dado** que el usuario confirma,  
**cuando** Room elimina la fila,  
**entonces** el registro deja de existir.

### CA-07 — Eliminar por id

**Dado** que existen registros con códigos repetidos en categorías diferentes,  
**cuando** se elimina uno,  
**entonces** solo se elimina el id seleccionado.

### CA-08 — Una fila afectada

**Dado** un id existente,  
**cuando** se ejecuta la eliminación,  
**entonces** el DAO devuelve una fila afectada.

### CA-09 — Cero filas afectadas

**Dado** un id inexistente,  
**cuando** se ejecuta la eliminación,  
**entonces** se devuelve `NOT_FOUND`.

### CA-10 — Sin eliminación múltiple accidental

**Dado** cualquier eliminación individual,  
**cuando** termina,  
**entonces** no se eliminan otras filas.

### CA-11 — Regreso al listado

**Dado** que la eliminación fue correcta,  
**cuando** termina,  
**entonces** el detalle se cierra y se vuelve al listado.

### CA-12 — Listado actualizado

**Dado** que se eliminó un registro,  
**cuando** `MainActivity` vuelve al frente,  
**entonces** el registro ya no aparece.

### CA-13 — Base vacía

**Dado** que se elimina el último registro,  
**cuando** se actualiza el listado,  
**entonces** aparece `EmptyDatabase`.

### CA-14 — Sin resultados

**Dado** que una búsqueda o filtros pierden su último resultado,  
**cuando** se actualiza Room,  
**entonces** aparece `NoResults` si la base aún contiene registros.

### CA-15 — Opciones actualizadas

**Dado** que el registro eliminado era el único con un valor de filtro,  
**cuando** Room emite cambios,  
**entonces** las opciones se actualizan.

### CA-16 — Doble envío bloqueado

**Dado** que la eliminación está en curso,  
**cuando** el usuario vuelve a pulsar una acción destructiva,  
**entonces** no se ejecuta otra eliminación.

### CA-17 — Rotación segura

**Dado** que la eliminación está en curso,  
**cuando** el dispositivo rota,  
**entonces** no se duplica la operación.

### CA-18 — Evento único

**Dado** que la eliminación terminó,  
**cuando** la Activity se recrea,  
**entonces** la navegación y confirmación no se repiten.

### CA-19 — Error controlado

**Dado** que ocurre un error inesperado,  
**cuando** falla la eliminación,  
**entonces** se muestra un mensaje y la aplicación no se cierra.

### CA-20 — Operación no bloqueante

**Dado** que se elimina una fila,  
**cuando** Room procesa la operación,  
**entonces** no se bloquea el hilo principal.

---

## 15. Diseño técnico propuesto

### 15.1. Puerto de entrada

Se añadirá:

```text
DeleteWarehouseItemUseCase
```

Firma orientativa:

```java
public interface DeleteWarehouseItemUseCase {

    void deleteWarehouseItem(
            long warehouseItemId,
            DeleteWarehouseItemCallback callback
    );
}
```

La firma final deberá seguir el patrón asíncrono ya utilizado en creación y actualización.

Podrá utilizarse un callback definido en `application.port.in` o un resultado entregado al ViewModel, siempre que no exponga detalles Room.

### 15.2. Resultado de aplicación

Se añadirá:

```text
DeleteWarehouseItemResult
```

Estados mínimos:

```text
SUCCESS
INVALID_ID
NOT_FOUND
PERSISTENCE_ERROR
```

Podrá modelarse mediante clases inmutables o tipos explícitos, siguiendo la estrategia actual del proyecto.

### 15.3. Servicio de aplicación

`DeleteWarehouseItemService` deberá:

1. validar que `warehouseItemId > 0`;
2. devolver `INVALID_ID` si no es válido;
3. invocar el puerto de salida;
4. transformar éxito;
5. transformar not found;
6. transformar error inesperado.

No deberá:

- mostrar diálogos;
- conocer Activities;
- acceder al DAO;
- cerrar pantallas;
- modificar listas manualmente.

La confirmación pertenece al adaptador de entrada porque representa una interacción de UI.

### 15.4. Puerto de salida

`WarehouseItemRepository` se ampliará con:

```java
void deleteById(
        long warehouseItemId,
        WarehouseItemDeleteCallback callback
);
```

### 15.5. Callback de salida

Se añadirá:

```text
WarehouseItemDeleteCallback
```

Contrato orientativo:

```java
public interface WarehouseItemDeleteCallback {

    void onSuccess();

    void onNotFound();

    void onError(Throwable throwable);
}
```

No se reutilizará `WarehouseItemUpdateCallback` porque una eliminación representa otra operación y no tiene estado `DUPLICATE`.

### 15.6. DAO

Se añadirá una operación específica por id:

```java
@Query(
    "DELETE FROM warehouse_items " +
    "WHERE id = :warehouseItemId"
)
int deleteById(long warehouseItemId);
```

Resultado:

```text
1 → eliminación correcta
0 → registro inexistente
```

No se utilizará `deleteAll()` para la eliminación individual.

También sería válido:

```java
@Delete
int delete(WarehouseItemEntity entity);
```

pero `deleteById()` es preferible porque:

- la operación parte de un id;
- no necesita mapear o cargar de nuevo una entidad;
- evita depender de datos potencialmente desactualizados;
- expresa claramente la intención.

### 15.7. Adaptador Room

`RoomWarehouseItemRepository.deleteById()` deberá:

- validar sus dependencias;
- ejecutar en `databaseExecutor`;
- llamar a `dao.deleteById(id)`;
- interpretar `1` como éxito;
- interpretar `0` como not found;
- transformar excepciones en error;
- no llamar a `deleteAll()`;
- no ejecutar lógica en el hilo principal.

### 15.8. ViewModel de detalle

`WarehouseItemDetailViewModel` deberá ampliarse para:

- recibir `DeleteWarehouseItemUseCase`;
- mantener la observación existente;
- exponer si está eliminando;
- impedir llamadas repetidas;
- emitir resultado de eliminación;
- emitir evento único de éxito;
- controlar not found;
- controlar error;
- no mostrar diálogos.

Firma orientativa:

```java
public void deleteWarehouseItem()
```

El ViewModel ya conoce el id recibido en construcción, por lo que deberá conservarlo de forma segura.

### 15.9. Estado de detalle

`WarehouseItemDetailUiState` podrá ampliarse con:

```text
isDeleting
deleteErrorMessage
```

o podrá existir un estado separado de operación:

```text
WarehouseItemDeleteUiState
```

La decisión deberá evitar que una operación puntual destruya el estado observable del detalle.

Recomendación:

- mantener el contenido del detalle;
- añadir `isDeleting`;
- entregar éxito mediante `UiEvent`;
- entregar error mediante estado o evento controlado.

### 15.10. Evento de éxito

Se reutilizará la estrategia `UiEvent` ya existente si encaja con el patrón actual.

El éxito deberá consumirse una sola vez:

```text
mostrar confirmación
        ↓
finish()
```

No deberá reemitirse al rotar.

### 15.11. Factory

`WarehouseItemDetailViewModelFactory` deberá recibir:

- `GetWarehouseItemDetailUseCase`;
- `DeleteWarehouseItemUseCase`;
- `warehouseItemId`.

### 15.12. Activity

`ItemDetailActivity` deberá:

- habilitar Eliminar únicamente en `CONTENT`;
- guardar o tener acceso al contenido actual para el diálogo;
- mostrar `MaterialAlertDialogBuilder`;
- delegar la confirmación al ViewModel;
- renderizar `Deleting`;
- observar evento de éxito;
- mostrar Toast o Snackbar;
- finalizar después del éxito;
- mostrar error sin cerrar;
- no acceder al repositorio ni DAO.

### 15.13. Composición de dependencias

`AppContainer` deberá crear:

```text
DeleteWarehouseItemUseCase
DeleteWarehouseItemService
```

y actualizar:

```text
provideWarehouseItemDetailViewModelFactory()
```

No se incorporará un framework de inyección para esta HU.

---

## 16. Decisiones técnicas importantes

### 16.1. Confirmación en la UI

El servicio no pedirá confirmación.

La confirmación es responsabilidad del adaptador de entrada.

### 16.2. Eliminación física

La v1.0 realizará borrado físico porque no existe:

- historial;
- auditoría;
- sincronización;
- recuperación.

No se añadirá un campo `deleted`.

### 16.3. Operación por id

La eliminación no dependerá del objeto completo.

### 16.4. No reutilizar deleteAll

`deleteAll()` no representa una eliminación individual y no deberá utilizarse.

### 16.5. Cero filas no es éxito

Una eliminación con cero filas afectadas deberá producir `NOT_FOUND`.

### 16.6. No cerrar antes del callback

La Activity no finalizará inmediatamente después de confirmar.

Deberá esperar al resultado de Room.

### 16.7. Observación y callback pueden competir

Room puede emitir `NotFound` como consecuencia del borrado antes o después del callback.

La implementación deberá soportar ambos órdenes sin:

- mostrar error falso;
- ejecutar navegación dos veces;
- rehabilitar acciones;
- generar otro borrado.

### 16.8. El detalle no se modifica manualmente

Después de eliminar no se vaciarán TextViews manualmente como fuente de verdad.

La Activity finalizará tras éxito.

### 16.9. HU-10 permanece separada

No se añadirá selección múltiple ni `deleteByIds()` en esta historia.

---

## 17. Estructura de archivos orientativa

La HU-07 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── DeleteWarehouseItemUseCase.java
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       └── WarehouseItemDeleteCallback.java
│   ├── result/
│   │   └── DeleteWarehouseItemResult.java
│   └── service/
│       └── DeleteWarehouseItemService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   └── ItemDetailActivity.java
│   │       ├── state/
│   │       │   └── WarehouseItemDetailUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemDetailViewModel.java
│   │           └── WarehouseItemDetailViewModelFactory.java
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               │   └── WarehouseItemDao.java
│               └── repository/
│                   └── RoomWarehouseItemRepository.java
└── configuration/
    └── AppContainer.java
```

Podrá añadirse un estado específico:

```text
WarehouseItemDeleteUiState
```

si separa mejor el contenido persistente de la operación puntual.

No se crearán clases sin responsabilidad real.

---

## 18. Diseño de interfaz esperado

### 18.1. Detalle

Resultado esperado:

```text
[Editar] [Eliminar]
```

Ambos habilitados únicamente en `CONTENT`.

### 18.2. Diálogo

```text
Eliminar mercancía

¿Quieres eliminar MR · 1050?
Esta acción no se puede deshacer.

[Cancelar] [Eliminar]
```

### 18.3. Estado eliminando

Opciones válidas:

```text
[Editar deshabilitado] [Eliminando...]
```

o indicador de progreso discreto junto a las acciones.

### 18.4. Requisitos visuales

- acción destructiva clara;
- confirmación comprensible;
- mercancía identificable;
- texto breve;
- no mostrar id;
- no permitir doble confirmación;
- no bloquear permanentemente la pantalla ante error;
- coherencia con Material Components;
- accesibilidad y tamaño táctil adecuado.

---

## 19. Pruebas recomendadas

### 19.1. DeleteWarehouseItemService

- rechaza id cero;
- rechaza id negativo;
- delega id válido;
- transforma éxito;
- transforma not found;
- transforma error;
- no depende de Android ni Room.

### 19.2. WarehouseItemDetailViewModel

- conserva carga de detalle;
- inicia eliminación con id correcto;
- entra en `Deleting`;
- bloquea segunda llamada;
- emite éxito una sola vez;
- emite not found;
- emite error;
- vuelve a permitir reintento tras error;
- no permite eliminación con id inválido;
- no pierde estado tras recreación.

### 19.3. DAO

- elimina por id existente;
- devuelve una fila;
- elimina únicamente el registro seleccionado;
- no elimina mismo código de otra categoría;
- devuelve cero para id inexistente;
- reduce el número de filas en uno;
- `observeById()` emite ausencia;
- `observeAll()` se actualiza;
- filtros se actualizan;
- opciones se actualizan;
- `deleteAll()` no se utiliza en el flujo individual.

### 19.4. RoomWarehouseItemRepository

- ejecuta en executor;
- interpreta una fila como success;
- interpreta cero como not found;
- transforma excepción;
- no llama a deleteAll.

### 19.5. ItemDetailActivity

- habilita Eliminar en Content;
- deshabilita fuera de Content;
- abre diálogo;
- muestra categoría y código;
- Cancelar conserva registro;
- confirmar delega una vez;
- deshabilita acciones durante eliminación;
- muestra éxito;
- finaliza tras éxito;
- muestra error sin finalizar;
- mantiene navegación segura tras rotación.

### 19.6. Integración con listado

- elimina el último registro y muestra EmptyDatabase;
- elimina último resultado de búsqueda y muestra NoResults;
- elimina último resultado filtrado y muestra NoResults;
- elimina valor único de categoría y actualiza opciones;
- conserva búsqueda y filtros.

### 19.7. Pruebas manuales

- cancelar diálogo;
- confirmar;
- pulsar fuera del diálogo;
- usar botón Atrás;
- pulsar Eliminar repetidamente;
- rotar con diálogo abierto;
- rotar durante eliminación;
- eliminar último registro;
- eliminar desde búsqueda;
- eliminar desde filtros;
- mismo código en dos categorías;
- registro inexistente;
- error simulado;
- cerrar y reabrir aplicación;
- funcionamiento sin conexión.

---

## 20. Tareas de implementación

1. Confirmar HU-06 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado de HU-06.
4. Crear `feature/hu-07-eliminar-mercancia`.
5. Crear `DeleteWarehouseItemUseCase`.
6. Crear `DeleteWarehouseItemResult`.
7. Crear `WarehouseItemDeleteCallback`.
8. Implementar `DeleteWarehouseItemService`.
9. Ampliar `WarehouseItemRepository`.
10. Añadir `deleteById()` al DAO.
11. Implementar `deleteById()` en `RoomWarehouseItemRepository`.
12. Interpretar cero filas como not found.
13. Ejecutar eliminación en el executor.
14. Ampliar `WarehouseItemDetailUiState` o crear estado de operación.
15. Ampliar `WarehouseItemDetailViewModel`.
16. Conservar el id en el ViewModel.
17. Implementar bloqueo de doble eliminación.
18. Implementar evento único de éxito.
19. Ampliar `WarehouseItemDetailViewModelFactory`.
20. Actualizar `AppContainer`.
21. Habilitar `deleteButton` en `CONTENT`.
22. Mantenerlo deshabilitado en otros estados.
23. Crear diálogo Material de confirmación.
24. Mostrar categoría y código.
25. Delegar confirmación al ViewModel.
26. Renderizar estado `Deleting`.
27. Mostrar mensaje de error.
28. Finalizar detalle tras éxito.
29. Verificar actualización automática del listado.
30. Verificar `EmptyDatabase`.
31. Verificar `NoResults`.
32. Verificar opciones de filtros.
33. Crear pruebas unitarias del servicio.
34. Ampliar pruebas del ViewModel.
35. Ampliar pruebas DAO.
36. Crear pruebas del repositorio necesarias.
37. Crear pruebas de interfaz necesarias.
38. Ejecutar `./gradlew testDebugUnitTest`.
39. Ejecutar `./gradlew lintDebug`.
40. Ejecutar `./gradlew assembleDebug`.
41. Ejecutar pruebas instrumentadas necesarias.
42. Publicar commits representativos.
43. Verificar CI en la rama.
44. Revisar criterios de aceptación.
45. Fusionar localmente en `develop`.
46. Verificar CI en `develop`.
47. Eliminar la rama local y remota tras confirmar la integración.

---

## 21. Evidencias necesarias para cerrar la HU

- captura del detalle con Eliminar habilitado;
- captura del diálogo;
- evidencia de identificación por categoría y código;
- evidencia de Cancelar;
- evidencia de confirmación;
- evidencia de estado eliminando;
- evidencia de eliminación de una sola fila;
- evidencia de mismo código en otra categoría conservado;
- evidencia de detalle cerrado;
- evidencia del listado actualizado;
- evidencia de EmptyDatabase;
- evidencia de NoResults con búsqueda;
- evidencia de NoResults con filtros;
- evidencia de opciones actualizadas;
- evidencia de doble pulsación bloqueada;
- evidencia de rotación segura;
- evidencia de evento único;
- evidencia de not found;
- evidencia de error controlado;
- confirmación de que la Activity no accede a Room;
- confirmación de que no se usa `deleteAll()`;
- confirmación de que no se implementó eliminación múltiple;
- resultado de pruebas unitarias;
- resultado de pruebas DAO;
- resultado de lint;
- compilación debug correcta;
- pruebas instrumentadas necesarias;
- CI satisfactoria en `feature/hu-07-eliminar-mercancia`;
- evidencia del merge local en `develop`;
- CI satisfactoria en `develop`.

---

## 22. Definición de terminado

La HU-07 estará terminada cuando:

- Eliminar esté habilitado en un detalle válido;
- Eliminar esté deshabilitado fuera de `CONTENT`;
- toda eliminación requiera confirmación;
- el diálogo identifique categoría y código;
- Cancelar no modifique Room;
- confirmar ejecute el caso de uso;
- la eliminación utilice el id interno;
- el DAO elimine por id;
- una fila afectada produzca éxito;
- cero filas produzcan not found;
- solo se elimine el registro seleccionado;
- `deleteAll()` no se utilice;
- no se implemente eliminación múltiple;
- la operación se ejecute fuera del hilo principal;
- no pueda iniciarse dos veces;
- Editar quede deshabilitado durante la eliminación;
- el éxito se consuma una sola vez;
- el detalle se cierre tras éxito;
- el listado se actualice automáticamente;
- búsqueda y filtros se recalculen;
- las opciones de filtros se actualicen;
- se muestre EmptyDatabase al eliminar el último registro;
- se muestre NoResults cuando corresponda;
- los errores estén controlados;
- una rotación no duplique la operación;
- la UI dependa del puerto de entrada;
- el dominio no dependa de Android ni Room;
- las pruebas definidas finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione localmente en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 23. Resultado esperado

Al cerrar la HU-07, AlmacenTracker completará el CRUD individual de la versión 1.0:

```text
usuario abre el detalle
        ↓
pulsa Eliminar
        ↓
confirma la acción
        ↓
el caso de uso elimina por id
        ↓
Room borra una sola fila
        ↓
el detalle se cierra
        ↓
listado, búsqueda y filtros se actualizan
```

El proyecto quedará preparado para continuar con:

```text
HU-08 — Evitar combinaciones duplicadas
```

---

## 24. Commit documental recomendado

```text
docs: add HU-07 warehouse item deletion plan
```
