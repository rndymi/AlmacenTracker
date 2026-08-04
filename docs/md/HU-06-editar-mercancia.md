# HU-06 — Editar mercancía

> Sexta historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-06  
**Nombre:** Editar mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-06-editar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero modificar los datos y la ubicación de una mercancía existente,  
para mantener actualizada la información almacenada.

---

## 3. Objetivo

Ampliar el formulario implementado en la HU-02 para que funcione tanto en modo creación como en modo edición.

La edición deberá iniciarse desde el detalle implementado en la HU-03, cargar el registro actual mediante su identificador interno y actualizarlo en Room sin crear una nueva fila.

Flujo previsto:

```text
ItemDetailActivity
        ↓
acción Editar
        ↓
ItemFormActivity en modo edición
        ↓
WarehouseItemFormViewModel
        ├── GetWarehouseItemDetailUseCase
        └── UpdateWarehouseItemUseCase
                ↓
        UpdateWarehouseItemService
                ↓
        WarehouseItemRepository
                ↓
        RoomWarehouseItemRepository
                ↓
        WarehouseItemDao
                ↓
        Room / SQLite
```

Después de guardar, Room continuará siendo la fuente de verdad y actualizará automáticamente:

- la pantalla de detalle;
- el listado principal;
- los resultados de búsqueda;
- los resultados filtrados;
- las opciones de filtros cuando cambien categoría, sitio o posición.

---

## 4. Estado real del proyecto antes de la HU-06

La implementación actual dispone de:

- `MainActivity` con listado, búsqueda y filtros;
- `WarehouseItemListViewModel` con fuentes observables;
- `ItemDetailActivity`;
- `WarehouseItemDetailViewModel`;
- `GetWarehouseItemDetailUseCase`;
- `ItemFormActivity` en modo creación;
- `WarehouseItemFormViewModel`;
- `WarehouseItemFormUiState`;
- `CreateWarehouseItemUseCase`;
- `CreateWarehouseItemService`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao`;
- `WarehouseItemPersistenceMapper`;
- `AppContainer`;
- índice único de categoría + código;
- acciones visuales Editar y Eliminar en el detalle;
- botón Editar actualmente deshabilitado;
- conservación del formulario ante rotación;
- control de doble envío;
- validación y normalización de creación;
- actualización observable de detalle y listado.

La HU-06 deberá extender estos componentes. No deberá crear una segunda Activity o un formulario duplicado exclusivamente para editar.

---

## 5. Alcance incluido

La HU-06 incluye:

- habilitar la acción Editar en el detalle cuando exista contenido;
- abrir `ItemFormActivity` en modo edición;
- enviar únicamente el `id` interno;
- distinguir modo creación y modo edición;
- cargar el registro actual por `id`;
- mostrar un estado de carga inicial;
- rellenar categoría;
- rellenar código;
- rellenar sitio;
- rellenar posición cuando exista;
- rellenar observaciones cuando existan;
- permitir modificar los campos;
- reutilizar las validaciones de HU-02;
- reutilizar las reglas de normalización;
- conservar el mismo `id`;
- conservar `fechaCreacion`;
- generar una nueva `fechaActualizacion`;
- actualizar mediante Room;
- evitar que la edición cree una fila nueva;
- permitir mantener la misma categoría y código;
- rechazar una combinación categoría + código perteneciente a otro registro;
- controlar registro inexistente;
- controlar eliminación del registro durante la edición;
- controlar errores de persistencia;
- impedir envíos repetidos;
- conservar datos modificados ante rotación;
- cancelar sin guardar;
- mostrar confirmación de actualización;
- regresar al detalle tras guardar;
- actualizar automáticamente el detalle;
- actualizar automáticamente listado, búsqueda y filtros;
- pruebas unitarias, DAO y de interfaz relacionadas.

---

## 6. Alcance excluido

La HU-06 no incluye:

- eliminar mercancía;
- confirmar eliminación;
- editar varios registros;
- historial de modificaciones;
- auditoría;
- motivo del cambio;
- restaurar valores anteriores;
- control de versiones del registro;
- resolución de conflictos remotos;
- sincronización;
- bloqueo pesimista;
- usuarios o permisos;
- categorías configurables;
- edición desde selección múltiple;
- modificación de `id`;
- modificación manual de fechas.

La eliminación individual pertenece a la HU-07.

La validación integral y formal de duplicados seguirá cerrándose en la HU-08, aunque la edición deberá respetar desde ahora la restricción existente.

---

## 7. Precondiciones

Antes de comenzar la HU-06 deberán cumplirse estas condiciones:

- HU-01 integrada en `develop`;
- HU-02 integrada en `develop`;
- HU-03 integrada en `develop`;
- HU-04 integrada en `develop`;
- HU-05 integrada en `develop`;
- CI de `develop` satisfactoria;
- formulario de creación operativo;
- detalle observable por `id`;
- búsqueda y filtros operativos;
- índice único categoría + código activo;
- persistencia Room estable;
- botón Editar presente en el layout del detalle;
- `ItemFormActivity` conserva estado ante rotación;
- Room continúa siendo la única fuente de verdad.

---

## 8. Modos del formulario

`ItemFormActivity` deberá admitir dos modos explícitos:

```text
CREATE
EDIT
```

### 8.1. Modo CREATE

Comportamiento existente:

- formulario inicialmente vacío;
- título “Registrar mercancía”;
- Guardar ejecuta creación;
- fechas se generan durante la creación;
- no existe `warehouseItemId`.

### 8.2. Modo EDIT

Nuevo comportamiento:

- requiere `warehouseItemId`;
- carga el registro por `id`;
- título “Editar mercancía”;
- campos rellenados con datos actuales;
- Guardar ejecuta actualización;
- conserva `id`;
- conserva `createdAt`;
- actualiza `updatedAt`.

### 8.3. Detección del modo

La Activity podrá utilizar la presencia del extra:

```text
EXTRA_WAREHOUSE_ITEM_ID
```

Regla:

```text
sin id válido → CREATE
con id válido → EDIT
```

No se recomienda añadir un valor textual de modo si el `id` ya permite determinarlo de forma inequívoca.

También será válida una representación explícita si evita ambigüedades y está cubierta por pruebas.

---

## 9. Navegación

### 9.1. Entrada desde detalle

```text
ItemDetailActivity
        ↓
editButton
        ↓
ItemFormActivity.createEditIntent(context, id)
```

Firma orientativa:

```java
public static Intent createEditIntent(
        Context context,
        long warehouseItemId
) {
    Intent intent =
            new Intent(context, ItemFormActivity.class);

    intent.putExtra(
            EXTRA_WAREHOUSE_ITEM_ID,
            warehouseItemId
    );

    return intent;
}
```

### 9.2. Regreso tras guardar

Después de una actualización correcta:

1. `ItemFormActivity` muestra una confirmación breve.
2. Finaliza.
3. `ItemDetailActivity` vuelve al frente.
4. Su observación de Room recibe el registro actualizado.
5. El detalle se renderiza nuevamente.

No será necesario reenviar el objeto actualizado mediante Intent.

### 9.3. Regreso tras cancelar

Cancelar o volver atrás:

- finaliza el formulario;
- no actualiza Room;
- el detalle conserva los valores originales.

---

## 10. Datos editables y no editables

| Campo | Editable | Regla |
|---|---:|---|
| id | No | Se conserva |
| categoría | Sí | Obligatoria y normalizada |
| código | Sí | Obligatorio y normalizado |
| sitio | Sí | Obligatorio y normalizado |
| posición | Sí | Opcional |
| observaciones | Sí | Opcionales |
| fechaCreacion | No | Se conserva |
| fechaActualizacion | No | Se renueva al guardar |

La interfaz no deberá mostrar ni permitir editar:

```text
id
fechaCreacion
fechaActualizacion
```

dentro del formulario.

Las fechas seguirán visibles en el detalle.

---

## 11. Reglas de negocio aplicables

### 11.1. Conservación del identificador

La actualización deberá modificar la fila existente.

```text
id antes = id después
```

No se generará un nuevo identificador.

### 11.2. Conservación de fecha de creación

```text
createdAt antes = createdAt después
```

La fecha de creación no deberá sustituirse por el instante de edición.

### 11.3. Renovación de fecha de actualización

Cuando la actualización se confirme:

```text
updatedAt = instante actual
```

### 11.4. Validación

Se mantienen las reglas de HU-02:

- categoría obligatoria;
- código obligatorio;
- sitio obligatorio;
- posición opcional;
- observaciones opcionales;
- no aceptar campos obligatorios compuestos solo por espacios.

### 11.5. Normalización

Se mantienen las reglas existentes:

- `trim()` en todos los campos;
- categoría en mayúsculas;
- código en mayúsculas;
- sitio en mayúsculas;
- opcionales vacíos convertidos a la representación adoptada.

### 11.6. Identidad funcional

La combinación:

```text
categoría + código
```

continúa siendo única.

### 11.7. El propio registro no es duplicado

Si el usuario guarda sin cambiar categoría y código, la actualización deberá permitirse.

Ejemplo:

```text
Registro editado:
id = 7
MR + 1050

Nuevo valor:
id = 7
MR + 1050
```

Resultado:

```text
actualización permitida
```

### 11.8. Conflicto con otro registro

Si existe:

```text
id = 7 → MR + 1050
id = 9 → MD + 1050
```

y se intenta cambiar el registro `id = 9` a:

```text
MR + 1050
```

la actualización deberá rechazarse.

### 11.9. Room es la garantía definitiva

La restricción única existente seguirá siendo la última defensa.

Una comprobación previa podrá mejorar el mensaje, pero no sustituirá el índice único.

### 11.10. No usar REPLACE

No deberá utilizarse:

```java
OnConflictStrategy.REPLACE
```

porque podría eliminar y recrear filas, alterar ids u ocultar conflictos.

---

## 12. Estados del formulario en edición

### 12.1. Loading

El registro se está recuperando.

Características:

- campos ocultos o deshabilitados;
- indicador de progreso;
- Guardar deshabilitado;
- Cancelar o Atrás disponible;
- no mostrar datos parciales.

### 12.2. Editing

El registro fue cargado.

Características:

- campos rellenados;
- edición habilitada;
- Guardar disponible;
- errores inicialmente vacíos.

### 12.3. Saving

La actualización está en curso.

Características:

- Guardar deshabilitado;
- doble envío bloqueado;
- campos conservados;
- progreso visible;
- Cancelar deshabilitado si abandonar puede producir un estado ambiguo.

### 12.4. Success

La actualización terminó correctamente.

Acciones:

- evento único;
- Toast o Snackbar;
- finalizar Activity;
- detalle actualizado por Room.

### 12.5. ValidationError

Uno o varios campos son inválidos.

Características:

- no invocar actualización;
- errores junto a campos;
- datos modificados conservados;
- formulario editable.

### 12.6. DuplicateError

La nueva combinación pertenece a otro registro.

Mensaje orientativo:

```text
Ya existe otra mercancía con esta categoría y código.
```

### 12.7. NotFound

El `id` es válido, pero el registro ya no existe.

Mensaje orientativo:

```text
La mercancía ya no está disponible.
```

Características:

- no permitir guardar;
- no crear un registro nuevo;
- permitir volver al detalle o listado;
- no conservar un formulario editable basado en datos inexistentes.

### 12.8. InvalidId

El modo edición recibió un identificador inválido.

Mensaje orientativo:

```text
No se pudo identificar la mercancía.
```

### 12.9. LoadError

No fue posible cargar el registro.

Mensaje orientativo:

```text
No se pudo cargar la mercancía para editarla.
```

### 12.10. PersistenceError

No fue posible guardar los cambios.

Mensaje orientativo:

```text
No se pudieron guardar los cambios.
```

Características:

- conservar datos;
- volver a habilitar Guardar;
- no cerrar Activity;
- no mostrar excepciones técnicas.

---

## 13. Flujo principal

1. El usuario abre el detalle.
2. Room muestra el registro actual.
3. El usuario pulsa Editar.
4. `ItemDetailActivity` abre `ItemFormActivity` con el `id`.
5. La Activity detecta modo EDIT.
6. El ViewModel carga el registro mediante `GetWarehouseItemDetailUseCase`.
7. Room devuelve el registro.
8. El ViewModel inicializa el formulario.
9. La Activity muestra los valores actuales.
10. El usuario modifica uno o varios campos.
11. Pulsa Guardar.
12. El ViewModel bloquea nuevos envíos.
13. Construye `UpdateWarehouseItemCommand`.
14. `UpdateWarehouseItemService` normaliza.
15. El servicio valida.
16. Conserva `id` y `createdAt`.
17. Genera un nuevo `updatedAt`.
18. Invoca `WarehouseItemRepository.update()`.
19. El adaptador Room ejecuta la actualización.
20. Room confirma una fila actualizada.
21. El ViewModel emite éxito.
22. La Activity muestra confirmación.
23. La Activity finaliza.
24. El detalle recibe la actualización observable.
25. Listado, búsqueda, filtros y opciones se recalculan cuando corresponda.

---

## 14. Flujos alternativos

### FA-01 — Editar solo ubicación

1. El usuario conserva categoría y código.
2. Cambia sitio o posición.
3. Guarda.
4. La actualización se realiza sobre el mismo id.
5. El detalle muestra la nueva ubicación.

### FA-02 — Editar observaciones

1. El usuario modifica observaciones.
2. Guarda.
3. Se conserva fecha de creación.
4. Se actualiza fecha de actualización.

### FA-03 — Eliminar posición

1. El registro contiene una posición.
2. El usuario deja el campo vacío.
3. Guarda.
4. El valor se transforma a ausencia.
5. El detalle oculta la sección.
6. Las opciones de filtros se actualizan.

### FA-04 — Añadir posición

1. El registro no contiene posición.
2. El usuario añade `Nivel 2`.
3. Guarda.
4. El detalle muestra la posición.
5. El filtro correspondiente queda disponible.

### FA-05 — Mantener identidad funcional

1. El usuario no cambia categoría ni código.
2. Modifica otro campo.
3. Guarda.
4. No se detecta el propio registro como duplicado.

### FA-06 — Cambiar a combinación disponible

1. El usuario modifica categoría o código.
2. La nueva combinación no existe.
3. La actualización se guarda.

### FA-07 — Cambiar a combinación ocupada

1. Otro registro posee la combinación.
2. El usuario intenta utilizarla.
3. Room rechaza la operación.
4. Se muestra `DuplicateError`.
5. Los datos editados permanecen.

### FA-08 — Campos obligatorios vacíos

1. El usuario elimina categoría, código o sitio.
2. Pulsa Guardar.
3. La validación falla.
4. No se invoca el repositorio.
5. Se muestran errores.

### FA-09 — Cancelar edición

1. El usuario modifica campos.
2. Pulsa Cancelar.
3. No se actualiza Room.
4. El detalle mantiene los datos originales.

La confirmación de cambios sin guardar es recomendable, pero no obligatoria salvo que se implemente de forma completa y probada.

### FA-10 — Rotación durante carga

1. El formulario está cargando el registro.
2. El usuario rota.
3. El ViewModel mantiene el `id`.
4. No se duplican observadores.
5. El registro se muestra una vez disponible.

### FA-11 — Rotación durante edición

1. El usuario modifica campos.
2. Rota.
3. Los valores modificados permanecen.
4. Los datos cargados desde Room no sobrescriben las modificaciones del usuario.

### FA-12 — Doble guardado

1. El usuario pulsa Guardar varias veces.
2. Solo se ejecuta una actualización.

### FA-13 — Registro inexistente al abrir

1. Se abre edición con un id válido.
2. Room no encuentra el registro.
3. Se muestra `NotFound`.
4. No se transforma el formulario en creación.

### FA-14 — Registro desaparece mientras se edita

1. El registro estaba cargado.
2. Deja de existir antes de guardar.
3. El DAO actualiza cero filas o Room emite ausencia.
4. Se muestra `NotFound`.
5. No se crea una nueva fila.

### FA-15 — El registro cambia externamente

1. El formulario está abierto.
2. Room emite una versión nueva.
3. La implementación no deberá sobrescribir silenciosamente cambios ya escritos por el usuario.

Para v1.0 se recomienda:

- utilizar Room para la carga inicial;
- conservar el borrador local una vez que el usuario modifica el formulario;
- no implementar resolución de conflictos avanzada.

### FA-16 — El registro deja de cumplir búsqueda o filtros

1. El usuario llegó al detalle desde una búsqueda o filtro.
2. Edita un campo que cambia la coincidencia.
3. Al volver al listado, Room recalcula resultados.
4. El registro puede desaparecer del listado actual.
5. Esto es comportamiento correcto, no un error.

### FA-17 — Error inesperado

1. Room genera un error no relacionado con duplicado.
2. Se transforma a `PersistenceError`.
3. El formulario conserva los cambios.
4. La aplicación no se cierra.

---

## 15. Criterios de aceptación

### CA-01 — Acción Editar

**Dado** que el detalle contiene un registro válido,  
**cuando** se muestra la pantalla,  
**entonces** la acción Editar está habilitada.

### CA-02 — Apertura por id

**Dado** que el usuario pulsa Editar,  
**cuando** se abre el formulario,  
**entonces** se utiliza el id del registro seleccionado.

### CA-03 — Carga de datos

**Dado** un id existente,  
**cuando** se abre el modo edición,  
**entonces** se cargan categoría, código, sitio, posición y observaciones actuales.

### CA-04 — Campos opcionales

**Dado** un registro sin posición u observaciones,  
**cuando** se carga el formulario,  
**entonces** esos campos aparecen vacíos y no muestran `null`.

### CA-05 — Actualización válida

**Dado** un formulario válido,  
**cuando** el usuario guarda,  
**entonces** se actualiza el registro existente.

### CA-06 — Mismo id

**Dado** un registro editado,  
**cuando** termina la actualización,  
**entonces** conserva el mismo id.

### CA-07 — Misma fecha de creación

**Dado** un registro editado,  
**cuando** termina la actualización,  
**entonces** conserva su fecha de creación.

### CA-08 — Nueva fecha de actualización

**Dado** un registro editado,  
**cuando** termina la actualización,  
**entonces** su fecha de actualización refleja la edición.

### CA-09 — No crear una fila adicional

**Dado** un registro existente,  
**cuando** se guarda la edición,  
**entonces** el número de filas no aumenta.

### CA-10 — Mantener categoría y código

**Dado** que el usuario no modifica categoría ni código,  
**cuando** guarda otros cambios,  
**entonces** la operación no se rechaza como duplicada.

### CA-11 — Combinación disponible

**Dado** una nueva combinación no utilizada,  
**cuando** se guarda,  
**entonces** la actualización se realiza correctamente.

### CA-12 — Combinación ocupada

**Dado** que otro registro utiliza la combinación,  
**cuando** el usuario intenta guardarla,  
**entonces** se rechaza y se muestra un mensaje comprensible.

### CA-13 — Validación obligatoria

**Dado** que categoría, código o sitio están vacíos,  
**cuando** se intenta guardar,  
**entonces** no se actualiza Room.

### CA-14 — Posición opcional

**Dado** que posición está vacía,  
**cuando** se guardan datos válidos,  
**entonces** la actualización se completa sin posición.

### CA-15 — Observaciones opcionales

**Dado** que observaciones está vacío,  
**cuando** se guardan datos válidos,  
**entonces** la actualización se completa.

### CA-16 — Normalización

**Dado** que existen espacios externos o minúsculas,  
**cuando** se guarda,  
**entonces** se aplican las mismas reglas de HU-02.

### CA-17 — Cancelación

**Dado** que el usuario modificó datos,  
**cuando** cancela,  
**entonces** Room conserva los valores originales.

### CA-18 — Rotación

**Dado** que el usuario ha modificado el formulario,  
**cuando** rota el dispositivo,  
**entonces** conserva el borrador y no vuelve a sobrescribirlo con los datos iniciales.

### CA-19 — Doble envío

**Dado** que la actualización está en curso,  
**cuando** el usuario vuelve a pulsar Guardar,  
**entonces** no se ejecuta una segunda actualización.

### CA-20 — Registro inexistente

**Dado** un id sin registro asociado,  
**cuando** se abre o guarda la edición,  
**entonces** se muestra `NotFound` y no se crea una fila.

### CA-21 — Detalle actualizado

**Dado** que la edición finalizó correctamente,  
**cuando** el usuario vuelve al detalle,  
**entonces** se muestran los nuevos valores.

### CA-22 — Listado actualizado

**Dado** que la edición finalizó correctamente,  
**cuando** el usuario vuelve al listado,  
**entonces** se muestran los nuevos valores sin reiniciar la aplicación.

### CA-23 — Búsqueda y filtros actualizados

**Dado** que cambian campos usados en búsqueda o filtros,  
**cuando** Room confirma la actualización,  
**entonces** los resultados se recalculan automáticamente.

### CA-24 — Error controlado

**Dado** un error inesperado de persistencia,  
**cuando** ocurre la actualización,  
**entonces** se conservan los datos y la aplicación no se cierra.

### CA-25 — Operación no bloqueante

**Dado** que se actualiza Room,  
**cuando** se procesa la operación,  
**entonces** no se bloquea el hilo principal.

---

## 16. Diseño técnico propuesto

### 16.1. Comando de actualización

Se añadirá `UpdateWarehouseItemCommand`.

Estructura orientativa:

```java
public final class UpdateWarehouseItemCommand {

    private final long warehouseItemId;
    private final String category;
    private final String code;
    private final String site;
    private final String position;
    private final String observations;
    private final long createdAt;
}
```

El comando no deberá recibir `updatedAt` desde la UI.

Una alternativa más segura es que el servicio cargue internamente el registro original y no acepte `createdAt` desde el adaptador de entrada. Esta opción evita que la UI pueda modificar accidentalmente la fecha de creación.

La implementación deberá priorizar:

```text
id + datos editables
```

y obtener `createdAt` desde la fuente actual cuando sea viable.

### 16.2. Puerto de entrada

`UpdateWarehouseItemUseCase` representará la operación de actualización.

Firma orientativa:

```java
public interface UpdateWarehouseItemUseCase {

    void updateWarehouseItem(
            UpdateWarehouseItemCommand command,
            UpdateWarehouseItemCallback callback
    );
}
```

El callback o resultado deberá seguir el patrón de creación existente.

### 16.3. Resultado de aplicación

`UpdateWarehouseItemResult` deberá distinguir:

```text
SUCCESS
VALIDATION_ERROR
DUPLICATE
NOT_FOUND
PERSISTENCE_ERROR
```

Errores de campos mínimos:

```text
CATEGORY_REQUIRED
CODE_REQUIRED
SITE_REQUIRED
```

### 16.4. Servicio de aplicación

`UpdateWarehouseItemService` deberá:

1. validar el id;
2. obtener o conservar de forma segura el registro original;
3. normalizar campos;
4. validar obligatorios;
5. conservar `id`;
6. conservar `createdAt`;
7. generar `updatedAt`;
8. crear el modelo actualizado;
9. invocar el puerto de salida;
10. transformar resultados técnicos.

No dependerá de:

- Activities;
- Views;
- ViewModels;
- entidades Room;
- DAO.

### 16.5. Puerto de salida

`WarehouseItemRepository` se ampliará con una operación de actualización.

Firma orientativa:

```java
void update(
        WarehouseItem warehouseItem,
        WarehouseItemUpdateCallback callback
);
```

El resultado del callback deberá distinguir:

```text
SUCCESS
DUPLICATE
NOT_FOUND
ERROR
```

### 16.6. Callback de actualización

Se recomienda crear:

```text
WarehouseItemUpdateCallback
```

o un resultado equivalente dentro de `application.port.out`.

No se reutilizará `WarehouseItemInsertCallback` si sus estados o semántica dependen de una inserción.

### 16.7. DAO

Operación orientativa:

```java
@Update(onConflict = OnConflictStrategy.ABORT)
int update(WarehouseItemEntity entity);
```

Resultado esperado:

```text
1 → registro actualizado
0 → registro inexistente
```

También sería válida una consulta SQL explícita si mejora el control:

```java
@Query(
    "UPDATE warehouse_items SET " +
    "category = :category, " +
    "code = :code, " +
    "site = :site, " +
    "position = :position, " +
    "observations = :observations, " +
    "updated_at = :updatedAt " +
    "WHERE id = :warehouseItemId"
)
int update(...);
```

Una consulta explícita puede garantizar técnicamente que `createdAt` no sea modificado.

La implementación deberá alinearse con los nombres reales de columnas de `WarehouseItemEntity`.

### 16.8. Adaptador Room

`RoomWarehouseItemRepository` deberá:

- ejecutar fuera del hilo principal;
- mapear dominio a entidad;
- usar `ABORT`;
- detectar violación de unicidad;
- interpretar cero filas como `NOT_FOUND`;
- interpretar una fila como éxito;
- transformar otros errores;
- no ejecutar inserción como sustituto de actualización.

### 16.9. Carga para edición

El formulario podrá reutilizar:

```text
GetWarehouseItemDetailUseCase
```

para obtener el registro inicial.

No se creará otro caso de uso idéntico como `LoadWarehouseItemForEditUseCase` sin una diferencia real de comportamiento.

### 16.10. ViewModel del formulario

`WarehouseItemFormViewModel` deberá evolucionar para:

- distinguir CREATE y EDIT;
- recibir un id opcional;
- cargar el registro en edición;
- inicializar campos una sola vez;
- conservar el modelo original;
- conservar el borrador del usuario;
- evitar sobrescribir el borrador con emisiones posteriores;
- invocar creación o actualización según modo;
- mantener validaciones;
- exponer estados de carga y guardado;
- emitir evento de éxito diferenciado;
- impedir doble envío.

### 16.11. Estado de interfaz

`WarehouseItemFormUiState` podrá ampliarse con:

```text
mode
loading
loaded
warehouseItemId
category
code
site
position
observations
categoryError
codeError
siteError
saving
generalError
notFound
```

Se recomienda utilizar un estado exclusivo o propiedades consistentes para impedir:

```text
loading + saving
notFound + editable
success + error
```

### 16.12. Factory

`WarehouseItemFormViewModelFactory` deberá recibir:

- `CreateWarehouseItemUseCase`;
- `UpdateWarehouseItemUseCase`;
- `GetWarehouseItemDetailUseCase`;
- id opcional o modo.

La Factory deberá construir un único ViewModel coherente para ambos modos.

### 16.13. Activity

`ItemFormActivity` deberá:

- leer id opcional;
- configurar título según modo;
- obtener Factory apropiada;
- renderizar carga;
- renderizar formulario;
- mostrar errores;
- delegar Guardar;
- mostrar mensaje de creación o actualización;
- no acceder a repositorio ni Room.

### 16.14. Detalle

`ItemDetailActivity` deberá:

- habilitar Editar solo en estado `CONTENT`;
- abrir formulario con id;
- deshabilitar Editar en Loading, NotFound, InvalidId y Error;
- continuar observando Room;
- no refrescar manualmente el objeto.

### 16.15. Composición de dependencias

`AppContainer` deberá crear y exponer:

- `UpdateWarehouseItemService`;
- `UpdateWarehouseItemUseCase`;
- Factory actualizada del formulario.

No se añadirá un framework de inyección exclusivamente para esta HU.

---

## 17. Decisiones técnicas importantes

### 17.1. Reutilizar ItemFormActivity

No se creará:

```text
EditWarehouseItemActivity
```

salvo que exista una limitación real y documentada.

Crear y editar comparten:

- campos;
- validaciones;
- normalización;
- estado Saving;
- errores;
- layout.

### 17.2. Separar casos de uso

Aunque se reutilice la UI, creación y actualización deberán tener casos de uso separados:

```text
CreateWarehouseItemUseCase
UpdateWarehouseItemUseCase
```

Sus reglas no son idénticas.

### 17.3. Preservar createdAt fuera de la UI

La fecha de creación no deberá confiarse a un valor editable o reconstruido por la Activity.

### 17.4. Actualizar por id

La operación técnica deberá utilizar la clave primaria.

No deberá actualizar por:

```text
category + code
```

porque esos campos pueden cambiar.

### 17.5. No insertar si no existe

Un update que afecta cero filas deberá devolver `NOT_FOUND`.

No deberá convertirse automáticamente en creación.

### 17.6. Room actualiza las demás pantallas

No se modificarán manualmente:

- adapter;
- lista buscada;
- lista filtrada;
- detalle.

Las fuentes observables se encargarán.

### 17.7. No implementar resolución de conflictos avanzada

La v1.0 es local y de un solo usuario.

Se protegerá la integridad, pero no se añadirá versionado optimista o merge de cambios.

### 17.8. HU-08 sigue existiendo

La edición deberá rechazar duplicados desde esta HU porque guardar información inválida sería incorrecto.

La HU-08 cerrará formalmente la cobertura global de la regla, incluyendo pruebas y comportamiento uniforme.

---

## 18. Estructura de archivos orientativa

La HU-06 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateWarehouseItemUseCase.java
│   │   │   ├── GetWarehouseItemDetailUseCase.java
│   │   │   ├── UpdateWarehouseItemCommand.java
│   │   │   └── UpdateWarehouseItemUseCase.java
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       └── WarehouseItemUpdateCallback.java
│   ├── result/
│   │   └── UpdateWarehouseItemResult.java
│   └── service/
│       ├── CreateWarehouseItemService.java
│       └── UpdateWarehouseItemService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   ├── ItemDetailActivity.java
│   │       │   └── ItemFormActivity.java
│   │       ├── state/
│   │       │   └── WarehouseItemFormUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemFormViewModel.java
│   │           └── WarehouseItemFormViewModelFactory.java
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

Podrá añadirse:

```text
WarehouseItemFormMode
```

si una enumeración mejora la claridad.

La estructura deberá adaptarse al código real y no crear duplicados innecesarios.

---

## 19. Diseño de interfaz esperado

### 19.1. Detalle

Estado actual:

```text
[Editar deshabilitado] [Eliminar deshabilitado]
```

Resultado de HU-06:

```text
[Editar habilitado] [Eliminar deshabilitado]
```

Eliminar seguirá deshabilitado hasta HU-07.

### 19.2. Formulario de edición

```text
Toolbar
← Editar mercancía

Categoría *
[MR]

Código *
[1050]

Sitio *
[A1]

Posición
[Nivel 2]

Observaciones
[Caja exterior dañada]

[Cancelar] [Guardar cambios]
```

### 19.3. Carga inicial

```text
Toolbar
← Editar mercancía

[ProgressIndicator]
```

Los campos no deberán aparecer vacíos durante unos instantes si eso permite que el usuario escriba antes de terminar la carga.

### 19.4. Requisitos visuales

- título diferenciado entre creación y edición;
- valores actuales visibles;
- errores próximos a campos;
- formulario desplazable;
- progreso de carga;
- progreso de guardado;
- Guardar cambios deshabilitado durante operaciones;
- navegación Atrás coherente;
- no mostrar id ni timestamps;
- Material Components consistente con HU-02.

---

## 20. Pruebas recomendadas

### 20.1. UpdateWarehouseItemService

- actualiza datos válidos;
- rechaza id inválido;
- rechaza categoría vacía;
- rechaza código vacío;
- rechaza sitio vacío;
- acepta posición vacía;
- acepta observaciones vacías;
- normaliza campos;
- conserva id;
- conserva createdAt;
- genera updatedAt;
- permite la misma categoría y código del registro;
- transforma duplicado;
- transforma not found;
- transforma error inesperado;
- no invoca repositorio ante validación fallida.

### 20.2. WarehouseItemFormViewModel

- inicia correctamente en CREATE;
- inicia Loading en EDIT;
- carga campos por id;
- trata campos opcionales ausentes;
- no sobrescribe borrador tras cambio del usuario;
- conserva borrador tras recreación;
- invoca create en CREATE;
- invoca update en EDIT;
- muestra errores de validación;
- muestra duplicado;
- muestra not found;
- muestra error de carga;
- muestra error de persistencia;
- bloquea doble guardado;
- emite éxito de actualización una sola vez.

### 20.3. DAO

- actualiza una fila existente;
- devuelve una fila afectada;
- conserva id;
- conserva createdAt;
- cambia updatedAt;
- actualiza todos los campos editables;
- permite mantener categoría + código;
- permite combinación disponible;
- rechaza combinación perteneciente a otro registro;
- devuelve cero para id inexistente;
- no aumenta el número de filas;
- actualiza LiveData de detalle;
- actualiza LiveData de listado;
- actualiza búsqueda y filtros relacionados.

### 20.4. RoomWarehouseItemRepository

- ejecuta update en executor;
- mapea correctamente;
- transforma constraint en duplicate;
- transforma cero filas en not found;
- transforma error desconocido;
- no usa insert para editar.

### 20.5. Activity de detalle

- habilita Editar en Content;
- mantiene Editar deshabilitado en otros estados;
- abre el id correcto;
- refleja cambios al volver.

### 20.6. Activity de formulario

- muestra título de edición;
- muestra Loading;
- rellena campos;
- guarda cambios;
- cancela sin guardar;
- conserva borrador tras rotación;
- muestra duplicado;
- muestra not found;
- impide doble envío.

### 20.7. Pruebas manuales

- editar solo categoría;
- editar solo código;
- editar solo sitio;
- editar solo posición;
- eliminar posición;
- añadir posición;
- editar observaciones;
- eliminar observaciones;
- guardar sin cambiar valores;
- dejar campo obligatorio vacío;
- usar combinación de otro registro;
- usar mismo código con categoría distinta;
- rotar antes de cargar;
- rotar después de escribir;
- pulsar Guardar repetidamente;
- cancelar;
- volver con gesto o toolbar;
- editar desde detalle abierto desde búsqueda;
- editar desde detalle abierto desde filtros;
- comprobar desaparición de resultado cuando deja de coincidir;
- comprobar opciones de filtros;
- cerrar y reabrir aplicación;
- uso sin conexión.

---

## 21. Tareas de implementación

1. Confirmar HU-05 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado de HU-05.
4. Crear `feature/hu-06-editar-mercancia`.
5. Definir modo CREATE y EDIT.
6. Crear `UpdateWarehouseItemCommand`.
7. Crear `UpdateWarehouseItemResult`.
8. Crear `UpdateWarehouseItemUseCase`.
9. Crear `WarehouseItemUpdateCallback`.
10. Implementar `UpdateWarehouseItemService`.
11. Ampliar `WarehouseItemRepository`.
12. Añadir update al DAO con `ABORT`.
13. Implementar update en `RoomWarehouseItemRepository`.
14. Interpretar cero filas como not found.
15. Transformar conflicto único en duplicate.
16. Reutilizar `GetWarehouseItemDetailUseCase` para carga inicial.
17. Ampliar `WarehouseItemFormUiState`.
18. Ampliar `WarehouseItemFormViewModel`.
19. Inicializar el formulario una sola vez.
20. Proteger el borrador ante nuevas emisiones.
21. Conservar id y createdAt.
22. Generar updatedAt.
23. Diferenciar mensajes de creación y edición.
24. Ampliar `WarehouseItemFormViewModelFactory`.
25. Actualizar `AppContainer`.
26. Añadir `createEditIntent()` a `ItemFormActivity`.
27. Leer id opcional.
28. Configurar título y acción según modo.
29. Renderizar Loading de edición.
30. Renderizar NotFound e InvalidId.
31. Habilitar Editar en `ItemDetailActivity`.
32. Abrir el formulario con id.
33. Mantener Eliminar deshabilitado.
34. Comprobar actualización automática del detalle.
35. Comprobar actualización del listado.
36. Comprobar búsqueda y filtros.
37. Crear pruebas unitarias del servicio.
38. Ampliar pruebas del ViewModel.
39. Ampliar pruebas DAO.
40. Crear pruebas del repositorio necesarias.
41. Crear pruebas de interfaz necesarias.
42. Ejecutar `./gradlew testDebugUnitTest`.
43. Ejecutar `./gradlew lintDebug`.
44. Ejecutar `./gradlew assembleDebug`.
45. Ejecutar pruebas instrumentadas necesarias.
46. Publicar commits representativos.
47. Verificar CI en la rama.
48. Revisar criterios de aceptación.
49. Fusionar localmente en `develop`.
50. Verificar CI en `develop`.
51. Eliminar la rama local y remota tras confirmar la integración.

---

## 22. Evidencias necesarias para cerrar la HU

- captura del detalle con Editar habilitado;
- captura del formulario cargando;
- captura del formulario con datos actuales;
- evidencia de edición de categoría;
- evidencia de edición de ubicación;
- evidencia de posición eliminada;
- evidencia de observaciones actualizadas;
- evidencia de mismo id antes y después;
- evidencia de createdAt conservado;
- evidencia de updatedAt modificado;
- evidencia de una sola fila;
- evidencia de guardado sin cambiar categoría y código;
- evidencia de combinación duplicada rechazada;
- evidencia de campos obligatorios;
- evidencia de cancelación sin cambios;
- evidencia de conservación tras rotación;
- evidencia de bloqueo de doble guardado;
- evidencia de NotFound;
- evidencia de detalle actualizado;
- evidencia de listado actualizado;
- evidencia de búsqueda actualizada;
- evidencia de filtros y opciones actualizados;
- confirmación de que ItemFormActivity no accede a Room;
- confirmación de que no se usa REPLACE;
- confirmación de que update no llama a insert;
- resultado de pruebas unitarias;
- resultado de pruebas DAO;
- resultado de lint;
- compilación debug correcta;
- pruebas instrumentadas necesarias;
- CI satisfactoria en `feature/hu-06-editar-mercancia`;
- evidencia del merge local en `develop`;
- CI satisfactoria en `develop`.

---

## 23. Definición de terminado

La HU-06 estará terminada cuando:

- Editar esté habilitado en un detalle válido;
- el formulario se reutilice en modo edición;
- la navegación utilice el id interno;
- el registro se cargue desde Room;
- los campos actuales aparezcan correctamente;
- los campos opcionales no muestren `null`;
- se puedan modificar todos los campos editables;
- categoría, código y sitio sigan siendo obligatorios;
- posición y observaciones sigan siendo opcionales;
- la normalización sea igual a HU-02;
- la actualización conserve id;
- la actualización conserve createdAt;
- la actualización renueve updatedAt;
- editar no cree una nueva fila;
- el propio registro no se detecte como duplicado;
- una combinación de otro registro sea rechazada;
- Room use `ABORT`;
- no se utilice `REPLACE`;
- un id inexistente produzca NotFound;
- un update de cero filas no se convierta en insert;
- cancelar no modifique Room;
- el borrador sobreviva a rotación;
- la carga inicial no sobrescriba cambios del usuario;
- Guardar no ejecute varias actualizaciones;
- los errores estén controlados;
- el detalle se actualice automáticamente;
- el listado se actualice automáticamente;
- búsqueda y filtros se recalculen automáticamente;
- Editar se deshabilite fuera del estado Content;
- Eliminar siga fuera del alcance;
- la UI dependa de puertos de entrada;
- el dominio no dependa de Android ni Room;
- la operación no bloquee el hilo principal;
- las pruebas definidas finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione localmente en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 24. Resultado esperado

Al cerrar la HU-06, AlmacenTracker permitirá mantener actualizada una mercancía existente:

```text
usuario abre el detalle
        ↓
pulsa Editar
        ↓
el formulario carga el registro por id
        ↓
el usuario modifica los datos
        ↓
la aplicación valida y normaliza
        ↓
Room actualiza la misma fila
        ↓
detalle, listado, búsqueda y filtros se actualizan
```

El proyecto quedará preparado para continuar con:

```text
HU-07 — Eliminar mercancía
```

---

## 25. Commit documental recomendado

```text
docs: add HU-06 warehouse item editing plan
```
