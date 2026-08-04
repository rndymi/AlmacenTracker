# HU-32 — Eliminar un registro histórico

> Séptima historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-32  
**Nombre:** Eliminar un registro histórico  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-32-eliminar-registro-historico`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-29 — Consultar el historial de listas  
- HU-30 — Consultar el detalle histórico de una lista  
- HU-31 — Buscar y filtrar el historial  

**Issue prevista:** `#36`

---

## 2. Historia de usuario

Como usuario,  
quiero eliminar una lista histórica incorrecta,  
para mantener el historial documental limpio sin alterar la mercadería almacenada.

---

## 3. Objetivo

Completar el ciclo de vida básico del historial mediante una eliminación explícita, confirmada y segura.

Flujo previsto:

```text
WithdrawalHistoryDetailActivity
        ↓ acción Eliminar
confirmación explícita
        ↓
WithdrawalHistoryDetailViewModel
        ↓
WithdrawalHistoryRepository.deleteById(...)
        ↓
Room
        ↓
eliminar cabecera
        ↓ ON DELETE CASCADE
eliminar líneas históricas
        ↓
RESULT_OK
        ↓
WithdrawalHistoryListActivity
        ↓
refrescar criterios actuales
```

HU-32 deberá garantizar que:

- se elimina únicamente el registro histórico seleccionado;
- se eliminan sus líneas asociadas;
- la operación no afecta `warehouse_items`;
- la pantalla de detalle no permanece abierta con datos inexistentes;
- el listado se actualiza;
- los filtros activos de HU-31 se conservan;
- el usuario recibe una confirmación clara;
- un fallo no elimina visualmente el registro antes de tiempo.

---

## 4. Regla principal

Eliminar historial significa:

```text
eliminar cabecera histórica
+
eliminar líneas históricas asociadas
```

No significa:

```text
eliminar mercadería
modificar ubicación actual
descontar stock
eliminar fotografías
alterar otras listas
```

La mercadería y el historial son agregados independientes.

```text
WithdrawalHistory
        ≠
WarehouseItem
```

Aunque una línea histórica conserve:

```text
warehouseItemIdSnapshot
```

ese campo no es una clave foránea hacia `warehouse_items` y no deberá utilizarse para eliminar mercadería.

---

## 5. Documentos y código de referencia

HU-32 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-29-consultar-historial-listas.md`;
- `HU-30-consultar-detalle-historico.md`;
- `HU-31-buscar-filtrar-historial.md`;
- el estado real disponible de `AlmacenTrackerHU30.zip`;
- la implementación aplicada de HU-31 descrita en su MD;
- Room como fuente local de verdad;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- la separación entre historial y mercadería;
- la eliminación en cascada ya definida;
- el funcionamiento completamente sin conexión;
- la política de no crear componentes sin responsabilidad real.

El plan de v1.3 asigna a HU-32:

```text
confirmación
+
eliminación transaccional de cabecera y líneas
+
conservación de mercadería
+
actualización del listado
```

---

## 6. Limitación de las fuentes analizadas

En las fuentes disponibles aparece:

```text
AlmacenTrackerHU30.zip
```

No aparece:

```text
AlmacenTrackerHU31.zip
```

Por tanto, el estado real comprobado directamente corresponde a HU-30.

La integración con HU-31 se basa en el contrato definido por:

```text
HU-31-buscar-filtrar-historial.md
```

HU-32 deberá adaptar los nombres concretos únicamente cuando el ZIP actualizado con HU-31 confirme diferencias reales.

No se deberán inventar clases o métodos alternativos si HU-31 ya los implementó con otros nombres equivalentes.

---

## 7. Estado real antes de HU-32

El ZIP disponible confirma:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida es:

```text
develop
```

La última integración comprobable es:

```text
merge HU30 #34 into develop
```

La feature histórica ya dispone de:

```text
WithdrawalHistoryListActivity
WithdrawalHistoryListViewModel
WithdrawalHistoryListAdapter
WithdrawalHistoryDetailActivity
WithdrawalHistoryDetailViewModel
WithdrawalHistoryDetailUiState
WithdrawalHistoryDetailAdapter
WithdrawalHistoryDetailIntentContract
```

La pantalla de detalle ya:

- recibe `historyId`;
- consulta `WithdrawalHistoryRepository.findById(...)`;
- muestra cabecera y líneas;
- conserva el estado ante rotación;
- representa `LOADING`, `CONTENT`, `NOT_FOUND` y `ERROR`;
- permite reintentar;
- funciona sin conexión.

La pantalla de listado ya:

- consulta resúmenes;
- abre el detalle;
- refresca al volver a primer plano;
- conserva contenido durante una recarga.

HU-31 añade sobre ese listado:

- búsqueda por título, categoría o código;
- fechas inicial y final;
- criterios combinables;
- estado `NO_RESULTS`;
- limpieza de filtros;
- conservación de los criterios;
- refresco usando los criterios actuales.

---

## 8. Infraestructura de eliminación ya disponible

El repositorio histórico ya declara:

```java
void deleteById(
        long historyId,
        RepositoryCallback<Void> callback
);
```

`RoomWithdrawalHistoryRepository` ya:

1. valida el callback;
2. ejecuta la operación en el executor;
3. llama a `dao.deleteById(historyId)`;
4. devuelve `onNotFound()` si no se eliminó ninguna cabecera;
5. devuelve `onSuccess(null)` si la eliminación fue correcta;
6. transforma excepciones mediante `onError`.

El DAO ya dispone de:

```java
@Query(
    "DELETE FROM withdrawal_history " +
    "WHERE id = :historyId"
)
int deleteById(long historyId);
```

La entidad de líneas ya define:

```java
@ForeignKey(
    entity = WithdrawalHistoryEntity.class,
    parentColumns = "id",
    childColumns = "history_id",
    onDelete = ForeignKey.CASCADE
)
```

Por tanto, eliminar la cabecera provoca que SQLite elimine sus líneas asociadas.

---

## 9. Decisión crítica sobre la transacción

HU-32 no necesita añadir:

```java
deleteEntriesByHistoryId(...)
deleteHeaderById(...)
```

ni coordinar ambas operaciones desde el ViewModel.

La operación correcta es:

```text
DELETE withdrawal_history
        ↓
FOREIGN KEY ON DELETE CASCADE
        ↓
withdrawal_history_entries eliminadas
```

La eliminación de la cabecera y sus líneas se ejecuta como una única operación coherente a nivel de SQLite.

No se deberá implementar:

```text
borrar líneas
        ↓
borrar cabecera
```

mediante dos llamadas independientes.

Ese enfoque podría dejar datos parciales si una operación falla.

---

## 10. Alcance incluido

HU-32 incluye:

- añadir acción Eliminar en el detalle histórico;
- mostrar la acción únicamente con contenido válido;
- mantenerla oculta o deshabilitada durante carga;
- mantenerla deshabilitada durante eliminación;
- solicitar confirmación explícita;
- identificar claramente la lista afectada;
- indicar que también se eliminarán sus líneas;
- indicar que la mercadería no se modificará;
- permitir cancelar;
- delegar la confirmación al ViewModel;
- reutilizar `WithdrawalHistoryRepository.deleteById(...)`;
- eliminar la cabecera;
- eliminar las líneas mediante cascada;
- ejecutar fuera del hilo principal;
- bloquear dobles pulsaciones;
- impedir dos eliminaciones simultáneas;
- representar estado `DELETING`;
- representar eliminación completada;
- representar registro inexistente;
- representar error recuperable;
- conservar el detalle si falla;
- permitir reintentar;
- emitir un único evento de éxito;
- devolver `RESULT_OK`;
- devolver el id eliminado;
- finalizar la pantalla después del éxito;
- actualizar el listado;
- conservar búsqueda y filtros de HU-31;
- actualizar `CONTENT`, `EMPTY` o `NO_RESULTS`;
- no modificar mercadería;
- no modificar otras listas;
- no añadir migración;
- no modificar el esquema Room;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas DAO;
- pruebas de repositorio;
- pruebas de ViewModel;
- pruebas de Activity;
- pruebas de integración;
- CI.

---

## 11. Alcance excluido

HU-32 no incluye:

- eliminar varias listas;
- selección múltiple de historiales;
- deshacer eliminación;
- papelera;
- restaurar un historial eliminado;
- editar historiales;
- renombrar listas;
- modificar fechas;
- modificar cantidades;
- modificar unidades;
- modificar ubicaciones históricas;
- eliminar mercadería asociada;
- actualizar mercadería;
- descontar stock;
- borrar fotografías;
- exportar antes de eliminar;
- sincronización remota;
- backend;
- auditoría de eliminaciones;
- roles o permisos;
- confirmación biométrica;
- eliminación automática por antigüedad;
- limpieza masiva.

---

## 12. Punto de entrada recomendado

La acción Eliminar deberá incorporarse en:

```text
WithdrawalHistoryDetailActivity
```

Motivos:

- el usuario puede revisar exactamente qué lista eliminará;
- se reduce el riesgo de eliminar la fila equivocada;
- HU-30 ya carga el agregado completo;
- el detalle conoce el id;
- el plan de HU-32 depende de HU-30;
- no se sobrecarga inicialmente el listado con gestos destructivos.

No se recomienda añadir en HU-32:

```text
swipe to delete
```

ni:

```text
pulsación prolongada
```

porque una acción destructiva oculta aumenta el riesgo de error.

---

## 13. Acción visual

La acción podrá ubicarse en:

- menú de la Toolbar;
- botón de desbordamiento;
- acción Material claramente identificada.

Nombre visible:

```text
Eliminar lista
```

Icono orientativo:

```text
delete
```

Reglas:

- descripción accesible;
- no depender únicamente del color;
- no mostrarse durante `LOADING`;
- no mostrarse durante `NOT_FOUND`;
- no mostrarse durante error sin contenido;
- deshabilitarse durante `DELETING`;
- no ejecutarse directamente sin confirmación.

---

## 14. Confirmación

Diálogo orientativo:

```text
Eliminar lista histórica

Se eliminará esta lista y todas sus referencias guardadas.
La mercadería almacenada no se modificará.

[Cancelar] [Eliminar]
```

Cuando exista título:

```text
¿Eliminar “Reposición tienda centro”?
```

Cuando no exista título:

```text
¿Eliminar esta lista histórica?
```

La fecha podrá mostrarse como contexto adicional cuando mejore la identificación.

---

## 15. Contenido obligatorio de la confirmación

La confirmación deberá comunicar tres consecuencias:

```text
1. la eliminación es permanente;
2. se eliminarán cabecera y líneas;
3. la mercadería no será modificada.
```

No se deberá usar un mensaje genérico como:

```text
¿Estás seguro?
```

porque no explica el alcance real.

No se deberá afirmar:

```text
se eliminará la mercadería
```

ni utilizar términos de stock.

---

## 16. Responsabilidad de la Activity

`WithdrawalHistoryDetailActivity` deberá:

1. renderizar la acción;
2. mostrar la confirmación;
3. delegar el id al ViewModel;
4. observar el estado;
5. bloquear la UI durante borrado;
6. consumir el evento de éxito;
7. preparar `RESULT_OK`;
8. finalizar la Activity;
9. mostrar mensajes visuales;
10. no acceder al DAO;
11. no eliminar directamente;
12. no actualizar el listado manualmente.

La Activity no deberá crear un repositorio.

---

## 17. Responsabilidad del ViewModel

`WithdrawalHistoryDetailViewModel` deberá ampliarse para:

- conservar el `historyId`;
- validar el id;
- comprobar que existe contenido;
- impedir eliminación durante carga;
- impedir eliminación duplicada;
- publicar `DELETING`;
- llamar a `repository.deleteById(...)`;
- publicar éxito;
- publicar `NOT_FOUND` si ya no existe;
- publicar error conservando el registro;
- permitir reintento;
- emitir un evento de una sola consumición;
- no mostrar diálogos;
- no navegar;
- no depender de Android;
- no construir `Intent`.

Método orientativo:

```java
public void deleteHistory();
```

No es necesario volver a pasar el id desde la Activity si el ViewModel ya lo conserva desde `load(...)`.

---

## 18. Servicio de eliminación

No se recomienda crear:

```text
WithdrawalHistoryDeleteService
```

si su única responsabilidad es llamar:

```text
repository.deleteById(...)
```

El repositorio ya ofrece exactamente la operación requerida.

Un servicio adicional solo se justificaría si aparecieran reglas reales como:

- autorización;
- papelera;
- auditoría;
- eliminación remota;
- validaciones adicionales reutilizables.

Para HU-32, añadir ese servicio sería una capa ceremonial.

---

## 19. Estado de interfaz

`WithdrawalHistoryDetailUiState` deberá poder representar:

```text
LOADING
CONTENT
DELETING
NOT_FOUND
ERROR
```

También deberá conservar:

```text
WithdrawalHistoryRecord
```

cuando exista.

### `CONTENT`

- detalle visible;
- acción Eliminar disponible;
- progreso oculto.

### `DELETING`

- detalle visible;
- acción Eliminar deshabilitada;
- indicador de progreso;
- navegación Atrás bloqueada o controlada;
- no permitir otra confirmación.

### `NOT_FOUND`

- detalle oculto;
- mensaje de registro inexistente;
- acción Volver.

### `ERROR`

- si existía registro, conservar contenido;
- mostrar Snackbar;
- permitir reintentar eliminación;
- acción Eliminar podrá reactivarse después del error.

---

## 20. Diferenciar error de carga y error de eliminación

El estado deberá distinguir la operación que falló.

No basta con:

```text
ERROR
```

sin contexto, porque Reintentar podría significar:

```text
volver a cargar
```

o:

```text
volver a eliminar
```

Opciones válidas:

### Opción A

Añadir:

```text
Operation
├── LOAD
└── DELETE
```

### Opción B

Mantener eventos separados:

```text
loadError
deleteError
```

### Opción C

Exponer métodos de reintento explícitos:

```java
retryLoad();
retryDelete();
```

La implementación elegida deberá evitar que el botón Reintentar ejecute la operación equivocada.

---

## 21. Evento de eliminación completada

Se añadirá un evento de una sola consumición:

```text
UiEvent<Long>
```

Contenido:

```text
historyId eliminado
```

Nombre orientativo:

```text
deleteSuccessEvent
```

La Activity deberá consumirlo una sola vez.

El evento no deberá repetirse:

- tras rotación;
- al volver del segundo plano;
- al recrear observers;
- después de cerrar la Activity.

---

## 22. Resultado de la Activity

Después del éxito:

```java
Intent result = new Intent();
result.putExtra(EXTRA_DELETED_HISTORY_ID, historyId);
setResult(RESULT_OK, result);
finish();
```

Se recomienda centralizar el extra en:

```text
WithdrawalHistoryDetailIntentContract
```

o en un contrato de resultado pequeño dentro de `common`.

Constante orientativa:

```text
EXTRA_DELETED_HISTORY_ID
```

No se deberá serializar el registro completo.

---

## 23. Relación con HU-31

HU-31 conserva:

```text
query
fecha inicial
fecha final
criterios actuales
```

Cuando HU-32 finalice:

```text
WithdrawalHistoryListActivity
        ↓
refrescar criterios actuales
```

No deberá hacer:

```text
limpiar filtros
volver a findAllSummaries()
```

Ejemplo:

```text
query = MR
        ↓
abrir detalle
        ↓
eliminar
        ↓
volver al listado
        ↓
seguir mostrando resultados de MR
```

---

## 24. Lanzamiento del detalle con resultado

Para una actualización inmediata y explícita, se recomienda que el listado use:

```text
ActivityResultLauncher<Intent>
```

para abrir el detalle.

Al recibir:

```text
RESULT_OK
```

deberá llamar:

```text
viewModel.refresh()
```

HU-31 deberá implementar `refresh()` con los criterios vigentes.

Alternativamente, el `onResume()` actual puede refrescar.

Sin embargo, el contrato de resultado aporta:

- intención explícita;
- pruebas más claras;
- actualización solo cuando ocurrió un cambio;
- menos consultas innecesarias.

La solución recomendada es:

```text
ActivityResultLauncher
+
RESULT_OK
```

---

## 25. Compatibilidad con `onResume()`

Si HU-31 mantiene el refresco en `onResume()`:

- se deberá evitar una consulta duplicada después de `RESULT_OK`;
- el ViewModel deberá impedir o ignorar cargas redundantes;
- no deberán coexistir dos generaciones de consulta iguales sin necesidad.

Opciones:

- refrescar solo mediante resultado;
- mantener `onResume()` y no refrescar en callback;
- marcar que el resultado ya provocó el refresco.

Se recomienda eliminar la dependencia de `onResume()` para cambios conocidos y usar el resultado explícito.

No obstante, deberá conservarse una estrategia defensiva para cambios futuros realizados desde otras rutas.

---

## 26. Actualización del listado

Después de eliminar, la consulta puede producir:

### Aún existen resultados

```text
CONTENT
```

### No quedan historiales y no hay criterios

```text
EMPTY
```

### Existen criterios y ya no hay coincidencias

```text
NO_RESULTS
```

El ViewModel de HU-31 deberá determinar el estado usando los criterios activos.

El adapter no deberá eliminar una fila localmente como única fuente de verdad.

Room deberá volver a consultarse.

---

## 27. Por qué no eliminar localmente del adapter

No se recomienda:

```text
adapter.remove(historyId)
```

como solución principal.

Motivos:

- el listado puede estar filtrado;
- los contadores y estados dependen de Room;
- puede existir error de persistencia;
- la fuente de verdad debe seguir siendo Room;
- `EMPTY` y `NO_RESULTS` necesitan reevaluarse;
- una eliminación local anticipada podría mostrar éxito falso.

La fila solo desaparecerá después de confirmar éxito y refrescar Room.

---

## 28. Registro inexistente durante la eliminación

Puede ocurrir si:

- el registro ya fue eliminado;
- se abrió una tarea antigua;
- otra ruta lo eliminó;
- el id dejó de existir.

Si `deleteById(...)` devuelve:

```text
onNotFound()
```

la Activity deberá:

- informar que la lista ya no existe;
- devolver un resultado que permita refrescar el listado;
- finalizar o mostrar `NOT_FOUND`;
- no presentarlo como error técnico;
- no reintentar indefinidamente.

Mensaje orientativo:

```text
Esta lista histórica ya no existe.
```

---

## 29. Resultado ante `NOT_FOUND`

La opción más consistente es:

```text
tratar como estado final
+
RESULT_OK
+
refrescar listado
```

Motivo:

- el objetivo visual es retirar una fila que ya no existe;
- el listado necesita actualizarse;
- reintentar no puede eliminar algo inexistente.

El resultado podrá incluir:

```text
historyId
```

aunque la eliminación no la haya realizado esta llamada.

No se deberá mostrar un mensaje de “eliminado correctamente” si Room devolvió `NOT_FOUND`.

---

## 30. Error de Room

Cuando ocurra un error:

- el detalle permanece visible;
- no se cierra la pantalla;
- no se devuelve `RESULT_OK`;
- la acción podrá reactivarse;
- se mostrará un mensaje;
- se permitirá reintentar;
- no se modificará el adapter del listado;
- no se mostrará excepción técnica.

Mensaje orientativo:

```text
No se pudo eliminar la lista histórica.
```

Acción:

```text
Reintentar
```

---

## 31. Bloqueo durante la eliminación

Durante `DELETING`:

- deshabilitar Eliminar;
- impedir doble toque;
- no abrir otro diálogo;
- deshabilitar Reintentar;
- mostrar progreso;
- evitar cerrar accidentalmente si puede dejar incertidumbre;
- no permitir abrir otra pantalla.

La navegación Atrás podrá:

- bloquearse temporalmente; o
- mantenerse, pero sin cancelar realmente la operación.

Recomendación:

```text
bloquear Atrás durante la operación breve
```

para evitar que el resultado llegue a una pantalla destruida sin una estrategia clara.

---

## 32. Rotación durante eliminación

El ViewModel deberá conservar:

- `historyId`;
- registro visible;
- estado `DELETING`;
- callback activo;
- evento de éxito pendiente.

La Activity recreada deberá:

- volver a observar;
- no ejecutar otra eliminación;
- mostrar progreso;
- consumir el resultado una sola vez;
- finalizar cuando corresponda.

No se deberá reiniciar la operación por recreación.

---

## 33. Eliminación y clave foránea

La prueba debe confirmar:

```text
foreign_keys = ON
```

Room habilita las claves foráneas declaradas en el esquema.

Después de:

```sql
DELETE FROM withdrawal_history
WHERE id = ?
```

debe cumplirse:

```text
countHistories disminuye en 1
countEntriesByHistoryId = 0
countAllEntries disminuye según sus líneas
```

No deberán quedar líneas huérfanas.

---

## 34. Conservación de mercadería

La prueba de integración deberá preparar:

```text
warehouse_items
withdrawal_history
withdrawal_history_entries
```

Después de eliminar historial:

```text
warehouse_items permanece intacta
```

Se deberá comprobar al menos:

- mismo número de registros;
- mismos ids;
- misma categoría;
- mismo código;
- mismo sitio;
- misma posición;
- mismas observaciones;
- mismos timestamps.

No basta comprobar solo el conteo si se quiere demostrar conservación completa.

---

## 35. No añadir una migración

HU-32 no modifica:

- tablas;
- columnas;
- índices;
- claves;
- versión de Room.

Por tanto, no debe añadirse:

```text
MIGRATION_2_3
```

ni incrementarse la versión de base de datos.

La eliminación ya está soportada por el esquema existente.

---

## 36. Factory y composición

`WithdrawalHistoryDetailViewModelFactory` ya recibe:

```text
WithdrawalHistoryRepository
```

No necesita una nueva dependencia si el ViewModel utiliza directamente:

```text
deleteById(...)
```

`WithdrawalHistoryModule` tampoco necesita crear otro repositorio.

Solo deberán modificarse factory o módulo si HU-31 alteró sus contratos reales.

No se deberá duplicar `RoomWithdrawalHistoryRepository`.

---

## 37. Strings

Strings orientativos:

```text
withdrawal_history_delete_action
withdrawal_history_delete_title
withdrawal_history_delete_message
withdrawal_history_delete_confirm
withdrawal_history_deleting
withdrawal_history_delete_success
withdrawal_history_delete_error
withdrawal_history_already_deleted
```

El mensaje de confirmación deberá mencionar:

```text
la mercadería almacenada no se modificará
```

Los textos no deberán usar lenguaje de stock.

---

## 38. Accesibilidad

HU-32 deberá verificar:

- acción Eliminar con descripción;
- diálogo con título explícito;
- consecuencias leídas por TalkBack;
- orden de botones coherente;
- foco inicial adecuado;
- botón destructivo claramente identificado;
- progreso anunciado;
- estado de éxito anunciado cuando resulte útil;
- error anunciado;
- Reintentar accesible;
- navegación bloqueada de forma comprensible durante borrado;
- contraste en modo claro y oscuro;
- objetivos táctiles de 48 dp;
- información no dependiente únicamente del color.

---

## 39. Privacidad

HU-32 deberá:

- eliminar únicamente datos locales;
- no enviar información;
- no registrar título ni referencias en logs de producción;
- no registrar contenido histórico completo;
- no acceder a fotografías;
- no eliminar archivos externos;
- no solicitar Internet;
- no crear telemetría.

---

## 40. Pruebas DAO

- eliminar cabecera existente;
- devolver una fila eliminada;
- eliminar cabecera inexistente;
- devolver cero filas;
- eliminar todas las líneas por cascada;
- no eliminar líneas de otra cabecera;
- no dejar líneas huérfanas;
- conservar `warehouse_items`;
- eliminar una cabecera sin líneas de forma defensiva;
- ejecutar con claves foráneas activas.

---

## 41. Pruebas del repositorio

- id válido;
- éxito;
- `onNotFound`;
- error de DAO;
- callback único;
- operación en executor;
- callback nulo rechazado;
- no modificar otros historiales;
- no modificar mercadería;
- propagar `Void` correctamente.

---

## 42. Pruebas del ViewModel

- eliminar desde `CONTENT`;
- entrar en `DELETING`;
- éxito;
- evento único;
- doble pulsación ignorada;
- eliminación durante `LOADING` ignorada;
- eliminación con id inválido ignorada;
- `onNotFound`;
- error;
- conservar registro ante error;
- reintentar eliminación;
- rotación durante `DELETING`;
- no repetir eliminación;
- no repetir evento;
- diferenciar error de carga y borrado.

---

## 43. Pruebas de la Activity de detalle

- acción visible con contenido;
- acción oculta sin contenido;
- abrir confirmación;
- cancelar confirmación;
- confirmar eliminación;
- deshabilitar acción durante borrado;
- mostrar progreso;
- error y Reintentar;
- éxito devuelve `RESULT_OK`;
- extra contiene id;
- Activity finaliza;
- `NOT_FOUND` controlado;
- rotación con diálogo;
- rotación durante borrado;
- navegación Atrás bloqueada o controlada.

---

## 44. Pruebas del listado con HU-31

- eliminar sin filtros;
- refrescar y retirar fila;
- eliminar último historial;
- mostrar `EMPTY`;
- eliminar con búsqueda activa;
- conservar query;
- eliminar último resultado filtrado;
- mostrar `NO_RESULTS`;
- conservar fecha inicial;
- conservar fecha final;
- conservar intervalo;
- no limpiar criterios;
- no duplicar consultas;
- abrir otro detalle después del refresco.

---

## 45. Pruebas manuales

### Eliminar una lista

1. abrir Historial;
2. abrir una lista;
3. pulsar Eliminar;
4. revisar mensaje;
5. confirmar;
6. comprobar regreso;
7. comprobar que desaparece.

### Cancelar

1. abrir confirmación;
2. pulsar Cancelar;
3. comprobar que el detalle permanece;
4. comprobar que Room no cambia.

### Mercadería relacionada

1. guardar historial con una referencia encontrada;
2. eliminar historial;
3. abrir mercadería;
4. comprobar que continúa existiendo.

### Varias listas

1. crear dos historiales;
2. eliminar uno;
3. comprobar que el otro permanece.

### Filtros

1. buscar `MR`;
2. abrir un resultado;
3. eliminar;
4. volver;
5. comprobar que `MR` sigue activo.

### Último resultado filtrado

1. aplicar criterios con un único resultado;
2. eliminar;
3. comprobar `NO_RESULTS`;
4. limpiar filtros;
5. comprobar el resto del historial.

### Último historial total

1. dejar un solo registro;
2. eliminarlo;
3. comprobar `EMPTY`.

### Offline

1. activar modo avión;
2. eliminar historial;
3. comprobar funcionamiento normal.

---

## 46. Criterios de aceptación

### CA-01 — Acción disponible

**Dado** un detalle histórico cargado,  
**cuando** se muestra,  
**entonces** existe una acción para eliminar la lista.

### CA-02 — Confirmación obligatoria

**Dado** que el usuario pulsa Eliminar,  
**cuando** aparece la confirmación,  
**entonces** la operación no comienza hasta que confirme.

### CA-03 — Cancelación

**Dado** el diálogo de confirmación,  
**cuando** el usuario cancela,  
**entonces** no se modifica Room.

### CA-04 — Cabecera eliminada

**Dado** un historial existente,  
**cuando** la eliminación finaliza correctamente,  
**entonces** la cabecera deja de existir.

### CA-05 — Líneas eliminadas

**Dado** un historial con líneas,  
**cuando** se elimina la cabecera,  
**entonces** sus líneas se eliminan mediante cascada.

### CA-06 — Otros historiales intactos

**Dadas** varias listas,  
**cuando** se elimina una,  
**entonces** las demás permanecen intactas.

### CA-07 — Mercadería intacta

**Dada** una línea con `warehouseItemIdSnapshot`,  
**cuando** se elimina el historial,  
**entonces** la mercadería continúa existiendo sin cambios.

### CA-08 — Doble pulsación

**Dado** que la eliminación está en curso,  
**cuando** el usuario pulsa otra vez,  
**entonces** no se ejecuta una segunda operación.

### CA-09 — Error recuperable

**Dado** un error de Room,  
**cuando** ocurre,  
**entonces** el detalle permanece visible y puede reintentarse.

### CA-10 — Registro inexistente

**Dado** un registro ya eliminado,  
**cuando** se intenta eliminar,  
**entonces** se informa y el listado puede refrescarse.

### CA-11 — Retorno al listado

**Dada** una eliminación correcta,  
**cuando** finaliza,  
**entonces** el detalle devuelve `RESULT_OK` y se cierra.

### CA-12 — Actualización

**Dado** el listado,  
**cuando** recibe el resultado,  
**entonces** vuelve a consultar Room.

### CA-13 — Filtros conservados

**Dados** criterios activos de HU-31,  
**cuando** se elimina un resultado,  
**entonces** texto y fechas permanecen activos.

### CA-14 — Estado sin resultados

**Dado** que se elimina el último resultado de una consulta,  
**cuando** se refresca,  
**entonces** se muestra `NO_RESULTS`.

### CA-15 — Estado vacío

**Dado** que se elimina el último historial sin filtros,  
**cuando** se refresca,  
**entonces** se muestra `EMPTY`.

### CA-16 — Sin migración

**Dada** HU-32,  
**cuando** se implementa,  
**entonces** la versión del esquema Room no cambia.

### CA-17 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se elimina una lista,  
**entonces** la operación funciona mediante Room local.

---

## 47. Riesgos

### Eliminar mercadería por error

**Mitigación:** no consultar ni invocar `WarehouseItemRepository`; el snapshot no es una clave foránea.

### Borrado parcial

**Mitigación:** eliminar cabecera y reutilizar `ON DELETE CASCADE`.

### Doble eliminación

**Mitigación:** estado `DELETING` y bloqueo en ViewModel.

### Fila eliminada antes del éxito

**Mitigación:** refrescar desde Room solo después de `onSuccess`.

### Pérdida de filtros

**Mitigación:** `refresh()` debe reutilizar los criterios vigentes de HU-31.

### Consulta duplicada

**Mitigación:** coordinar Activity Result y `onResume()`.

### Reintento equivocado

**Mitigación:** distinguir error de carga y error de eliminación.

### Capa ceremonial

**Mitigación:** usar directamente el repositorio desde el ViewModel.

---

## 48. Definición de terminado

HU-32 estará terminada cuando:

- exista acción Eliminar en el detalle;
- exista confirmación explícita;
- el mensaje explique el alcance;
- se reutilice `deleteById(...)`;
- se elimine la cabecera;
- las líneas se eliminen por cascada;
- no se modifique mercadería;
- exista estado `DELETING`;
- se impida doble eliminación;
- exista error recuperable;
- `NOT_FOUND` se trate correctamente;
- exista evento único de éxito;
- el detalle devuelva `RESULT_OK`;
- el listado se refresque;
- los criterios de HU-31 se conserven;
- se actualicen `CONTENT`, `EMPTY` y `NO_RESULTS`;
- no se añada migración;
- funcione sin conexión;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 49. Resultado esperado

Al cerrar HU-32:

```text
detalle histórico
        ↓
confirmar eliminación
        ↓
Room elimina cabecera
        ↓
CASCADE elimina líneas
        ↓
mercadería permanece
        ↓
listado se actualiza
```

La versión 1.3 tendrá completo el flujo funcional previsto:

```text
registrar
consultar
abrir detalle
buscar
filtrar
eliminar
```

sin gestionar stock ni alterar la mercadería actual.
