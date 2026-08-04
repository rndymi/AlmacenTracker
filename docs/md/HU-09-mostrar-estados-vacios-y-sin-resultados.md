# HU-09 — Mostrar estados vacíos y sin resultados

> Novena historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-09  
**Nombre:** Mostrar estados vacíos y sin resultados  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-09-estados-vacios-sin-resultados`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero recibir mensajes claros cuando no haya mercancía registrada o cuando una búsqueda o filtros no produzcan coincidencias,  
para comprender el estado actual de la aplicación y saber cómo recuperar el contenido.

---

## 3. Objetivo

Cerrar formalmente el comportamiento visual y funcional de los estados sin contenido del listado principal.

La HU-09 deberá consolidar la implementación iniciada desde HU-01, HU-04 y HU-05, distinguiendo de manera inequívoca:

```text
EMPTY_DATABASE
```

de:

```text
NO_RESULTS
```

Además, cada estado deberá ofrecer una acción contextual útil:

```text
EMPTY_DATABASE
        ↓
Registrar mercancía

NO_RESULTS por búsqueda
        ↓
Limpiar búsqueda

NO_RESULTS por filtros
        ↓
Limpiar filtros

NO_RESULTS por búsqueda + filtros
        ↓
Limpiar búsqueda y filtros
```

El flujo continuará utilizando las fuentes observables existentes:

```text
Room
  ↓
ObserveWarehouseItemsUseCase
  ↓
FilterWarehouseItemsUseCase
  ↓
WarehouseItemListViewModel
  ↓
WarehouseItemListUiState
  ↓
MainActivity
```

Esta historia no debe crear una segunda lógica de consulta ni calcular el estado inspeccionando manualmente el RecyclerView.

---

## 4. Estado real del proyecto antes de la HU-09

El ZIP actualizado de HU-08 confirma que el proyecto ya dispone de:

- `WarehouseItemListUiState`;
- estados `LOADING`, `CONTENT`, `EMPTY_DATABASE`, `NO_RESULTS` y `ERROR`;
- `WarehouseItemListViewModel`;
- observación separada del total de registros;
- observación de resultados filtrados;
- `databaseStateKnown`;
- `databaseEmpty`;
- `WarehouseItemFilterCriteria`;
- búsqueda textual;
- filtros por categoría, sitio y posición;
- combinación de búsqueda y filtros;
- conservación de criterios en el ViewModel;
- `emptyState` en `activity_main.xml`;
- `noResultsState` en `activity_main.xml`;
- mensaje de base vacía;
- mensajes diferenciados para búsqueda, filtros y combinación;
- botón `clearNoResultsButton`;
- `clearSearch()`;
- `clearFilters()`;
- FAB de registro;
- pruebas unitarias parciales de `EMPTY_DATABASE` y `NO_RESULTS`.

La lógica actual diferencia correctamente la base vacía de los resultados vacíos:

```text
si Room no contiene registros
    → EMPTY_DATABASE

si Room contiene registros
y la consulta combinada devuelve vacío
    → NO_RESULTS
```

Sin embargo, todavía falta cerrar formalmente:

- acción directa de registro desde el estado vacío;
- iconografía y descripción específica para `NO_RESULTS`;
- tratamiento explícito del caso búsqueda + filtros;
- una acción que garantice la recuperación del contenido cuando ambos criterios están activos;
- ocultación y visibilidad mutuamente exclusiva de todos los estados;
- accesibilidad;
- mensajes consistentes;
- pruebas completas de transiciones;
- comportamiento ante creación, edición y eliminación mientras un estado vacío está visible;
- revisión del estado de error para evitar combinaciones visuales contradictorias.

---

## 5. Alcance incluido

La HU-09 incluye:

- consolidar `EMPTY_DATABASE`;
- consolidar `NO_RESULTS`;
- diferenciar base vacía y consulta sin coincidencias;
- distinguir búsqueda sin resultados;
- distinguir filtros sin resultados;
- distinguir búsqueda más filtros sin resultados;
- mensajes específicos para cada caso;
- icono o recurso visual para base vacía;
- icono o recurso visual para sin resultados;
- acción para registrar desde base vacía;
- acción para limpiar búsqueda;
- acción para limpiar filtros;
- acción para limpiar búsqueda y filtros;
- restaurar contenido cuando existan registros;
- conservar criterios no limpiados cuando la acción sea específica;
- garantizar una acción de recuperación completa en el caso combinado;
- mantener el FAB disponible de forma coherente;
- evitar mostrar RecyclerView y estado vacío simultáneamente;
- evitar mostrar `EMPTY_DATABASE` y `NO_RESULTS` simultáneamente;
- mantener carga y error mutuamente exclusivos;
- actualizar estados automáticamente cuando cambie Room;
- conservar estado correcto ante rotación;
- accesibilidad de textos, botones e iconos;
- pruebas unitarias del ViewModel;
- pruebas de renderizado o interfaz;
- pruebas instrumentadas necesarias;
- pruebas manuales de transiciones.

---

## 6. Alcance excluido

La HU-09 no incluye:

- crear una nueva Activity para estados vacíos;
- implementar búsqueda nueva;
- implementar filtros nuevos;
- cambiar la lógica SQL de búsqueda o filtros salvo corrección necesaria;
- ordenación configurable;
- paginación;
- animaciones complejas;
- ilustraciones descargadas de Internet;
- onboarding;
- tutorial inicial;
- eliminación múltiple;
- selección múltiple;
- sincronización remota;
- autenticación;
- estadísticas;
- historial;
- recomendaciones automáticas;
- sugerencias de búsqueda;
- guardar búsquedas;
- filtros favoritos.

La eliminación múltiple pertenece a HU-10.

---

## 7. Precondiciones

Antes de comenzar HU-09 deberán cumplirse:

- HU-01 integrada en `develop`;
- HU-02 integrada en `develop`;
- HU-03 integrada en `develop`;
- HU-04 integrada en `develop`;
- HU-05 integrada en `develop`;
- HU-06 integrada en `develop`;
- HU-07 integrada en `develop`;
- HU-08 integrada en `develop`;
- CI de `develop` satisfactoria;
- listado principal operativo;
- búsqueda operativa;
- filtros operativos;
- CRUD individual operativo;
- Room como única fuente de verdad;
- criterios conservados en ViewModel;
- estados base ya existentes.

---

## 8. Definición formal de estados

### 8.1. LOADING

Se utiliza mientras todavía no se conoce de forma suficiente:

- el estado global de la base;
- el resultado de la consulta actual.

No deberá mostrarse junto con:

- RecyclerView;
- estado vacío;
- sin resultados;
- error.

### 8.2. CONTENT

Se utiliza cuando la consulta actual devuelve al menos un registro.

Debe mostrar:

- RecyclerView;
- criterios actuales;
- filtros activos si existen;
- FAB.

No deberá mostrar:

- `emptyState`;
- `noResultsState`;
- `errorText`;
- indicador de carga.

### 8.3. EMPTY_DATABASE

Se utiliza únicamente cuando Room no contiene ningún registro.

Condición conceptual:

```text
total de registros = 0
```

Debe mostrarse aunque existan criterios residuales restaurados, porque una base realmente vacía tiene prioridad sobre `NO_RESULTS`.

Mensaje principal orientativo:

```text
Todavía no hay mercancía registrada.
```

Descripción orientativa:

```text
Registra la primera mercancía para comenzar.
```

Acción contextual:

```text
Registrar mercancía
```

### 8.4. NO_RESULTS

Se utiliza cuando:

```text
total de registros > 0
AND
resultado de consulta actual = 0
```

No es un error técnico.

Debe conservar visibles los controles de búsqueda y filtros para que el usuario entienda el criterio aplicado.

### 8.5. ERROR

Se utiliza cuando la consulta global, la consulta filtrada o las opciones fallan de forma inesperada.

No deberá confundirse con:

- base vacía;
- búsqueda sin coincidencias;
- filtros sin coincidencias.

---

## 9. Tipos de NO_RESULTS

Se recomienda representar explícitamente el motivo de `NO_RESULTS`.

Modelo orientativo:

```text
NoResultsReason
├── SEARCH
├── FILTERS
└── SEARCH_AND_FILTERS
```

Puede derivarse desde `WarehouseItemFilterCriteria` dentro del estado o ViewModel.

No es obligatorio persistirlo como campo si existen métodos inequívocos:

```java
hasSearchQuery()
hasActiveFilters()
```

La decisión final debe evitar lógica repetida y contradictoria en `MainActivity`.

### 9.1. SEARCH

Condición:

```text
query no vacía
AND
sin filtros activos
```

Mensaje:

```text
No se encontraron resultados para "105".
```

Acción:

```text
Limpiar búsqueda
```

### 9.2. FILTERS

Condición:

```text
query vacía
AND
uno o más filtros activos
```

Mensaje:

```text
No se encontraron resultados con los filtros seleccionados.
```

Acción:

```text
Limpiar filtros
```

### 9.3. SEARCH_AND_FILTERS

Condición:

```text
query no vacía
AND
uno o más filtros activos
```

Mensaje:

```text
No se encontraron resultados para "105" con los filtros seleccionados.
```

Acción principal recomendada:

```text
Limpiar búsqueda y filtros
```

Esta acción debe restaurar el listado completo cuando Room contenga registros.

Podrán añadirse acciones secundarias separadas, pero una única acción ambigua que limpie solo parte de los criterios no es suficiente para cerrar HU-09.

---

## 10. Problema detectado en la implementación actual

Actualmente, `clearNoResultsButton` aplica:

```text
si existen filtros activos
    → clearFilters()
si no
    → clearSearch()
```

En el caso:

```text
búsqueda activa + filtros activos
```

solo se limpian los filtros.

Esto puede dejar la búsqueda activa y mantener `NO_RESULTS`, por lo que el usuario pulsa una acción de recuperación pero continúa sin contenido.

HU-09 deberá corregir este comportamiento.

Solución recomendada:

```java
public void clearAllCriteria()
```

en `WarehouseItemListViewModel`, que aplique:

```text
query = ""
category = null
site = null
position = ALL
```

Alternativamente, el estado podrá exponer una acción tipada y la Activity delegará la operación correspondiente.

No se recomienda que la Activity reconstruya manualmente `WarehouseItemFilterCriteria`.

---

## 11. Acciones contextuales

### 11.1. Registrar desde EMPTY_DATABASE

El estado vacío deberá ofrecer un botón visible:

```text
Registrar mercancía
```

Comportamiento:

```text
emptyStateRegisterButton
        ↓
ItemFormActivity en modo CREATE
```

El FAB puede mantenerse visible, pero el botón dentro del estado mejora la comprensión y accesibilidad.

No debe duplicarse lógica: ambos accesos deberán usar el mismo método de navegación.

### 11.2. Limpiar búsqueda

Debe ejecutar:

```java
viewModel.clearSearch();
```

y conservar filtros activos.

### 11.3. Limpiar filtros

Debe ejecutar:

```java
viewModel.clearFilters();
```

y conservar la búsqueda activa.

### 11.4. Limpiar todo

Debe ejecutar:

```java
viewModel.clearAllCriteria();
```

y restaurar el listado completo cuando existan registros.

---

## 12. Flujo principal — Base vacía

1. El usuario abre AlmacenTracker.
2. Room devuelve cero registros.
3. El ViewModel conoce que la base está vacía.
4. Emite `EMPTY_DATABASE`.
5. `MainActivity` oculta carga, lista, sin resultados y error.
6. Muestra icono, título, descripción y acción Registrar.
7. El usuario pulsa Registrar mercancía.
8. Se abre `ItemFormActivity`.
9. Guarda un registro válido.
10. Room emite el nuevo contenido.
11. El ViewModel cambia a `CONTENT`.
12. El estado vacío desaparece.
13. El RecyclerView muestra el registro.

---

## 13. Flujo principal — Sin resultados

1. Room contiene mercancía.
2. El usuario introduce búsqueda o filtros.
3. La consulta combinada devuelve una lista vacía.
4. El ViewModel emite `NO_RESULTS`.
5. `MainActivity` oculta RecyclerView, carga, base vacía y error.
6. Muestra mensaje contextual.
7. Muestra acción contextual.
8. El usuario limpia el criterio correspondiente.
9. El ViewModel actualiza `WarehouseItemFilterCriteria`.
10. Room emite nuevos resultados.
11. La interfaz cambia a `CONTENT`.

---

## 14. Flujos alternativos

### FA-01 — Búsqueda sin resultados

- existen registros;
- query activa;
- no hay filtros;
- se muestra mensaje con la query;
- Limpiar búsqueda restaura la lista.

### FA-02 — Filtros sin resultados

- existen registros;
- query vacía;
- hay filtros;
- se muestra mensaje de filtros;
- Limpiar filtros restaura la lista.

### FA-03 — Búsqueda y filtros sin resultados

- existen registros;
- query activa;
- hay filtros;
- se muestra mensaje combinado;
- Limpiar búsqueda y filtros restaura la lista completa.

### FA-04 — Limpiar búsqueda conserva filtros

Si los filtros sí producen coincidencias, al limpiar solo búsqueda deberán mostrarse esos resultados filtrados.

### FA-05 — Limpiar filtros conserva búsqueda

Si la búsqueda sí produce coincidencias, al limpiar solo filtros deberán mostrarse esos resultados.

### FA-06 — Eliminar el último registro

1. El usuario elimina el último registro.
2. Room emite cero elementos.
3. El estado pasa a `EMPTY_DATABASE`.
4. No se muestra `NO_RESULTS`.

### FA-07 — Eliminar el último resultado visible

1. Room contiene otros registros.
2. Se elimina el único resultado de los criterios activos.
3. El estado pasa a `NO_RESULTS`.
4. Los criterios permanecen.

### FA-08 — Editar deja de coincidir

1. Un registro visible se edita.
2. Deja de cumplir búsqueda o filtros.
3. Si era el último resultado, aparece `NO_RESULTS`.
4. No se interpreta como base vacía.

### FA-09 — Crear un registro coincidente

1. Está visible `NO_RESULTS`.
2. Se crea un registro que coincide con los criterios conservados.
3. Room actualiza la consulta.
4. El estado pasa automáticamente a `CONTENT`.

### FA-10 — Crear un registro no coincidente

1. Está visible `NO_RESULTS`.
2. Se crea un registro que no coincide.
3. La base sigue sin estar vacía.
4. Se mantiene `NO_RESULTS`.

### FA-11 — Rotación

1. Está visible `EMPTY_DATABASE` o `NO_RESULTS`.
2. El usuario rota.
3. ViewModel conserva criterios y estado.
4. No aparecen brevemente estados contradictorios.

### FA-12 — Error posterior

1. Está visible contenido o un estado vacío.
2. La fuente emite error.
3. Se muestra únicamente `ERROR`.
4. No permanecen visibles mensajes anteriores.

### FA-13 — Recuperación tras error

Si Room vuelve a emitir correctamente, la UI deberá renderizar el estado correspondiente sin quedar bloqueada en error.

---

## 15. Criterios de aceptación

### CA-01 — Base vacía específica

**Dado** que Room no contiene registros,  
**cuando** se abre el listado,  
**entonces** se muestra `EMPTY_DATABASE`.

### CA-02 — Descripción de base vacía

**Dado** `EMPTY_DATABASE`,  
**cuando** se renderiza,  
**entonces** se muestra un título y una explicación comprensibles.

### CA-03 — Acción de registro

**Dado** `EMPTY_DATABASE`,  
**cuando** el usuario pulsa Registrar mercancía,  
**entonces** se abre el formulario de alta.

### CA-04 — Búsqueda sin coincidencias

**Dado** que existen registros,  
**cuando** una búsqueda no coincide,  
**entonces** se muestra `NO_RESULTS` de búsqueda.

### CA-05 — Filtros sin coincidencias

**Dado** que existen registros,  
**cuando** los filtros no coinciden,  
**entonces** se muestra `NO_RESULTS` de filtros.

### CA-06 — Combinación sin coincidencias

**Dado** que existen búsqueda y filtros activos,  
**cuando** no existen coincidencias,  
**entonces** se muestra el mensaje combinado.

### CA-07 — Limpiar búsqueda

**Dado** `NO_RESULTS` causado solo por búsqueda,  
**cuando** se limpia,  
**entonces** se recupera el contenido disponible.

### CA-08 — Limpiar filtros

**Dado** `NO_RESULTS` causado solo por filtros,  
**cuando** se limpian,  
**entonces** se recupera el contenido disponible.

### CA-09 — Limpiar criterios combinados

**Dado** búsqueda y filtros activos sin coincidencias,  
**cuando** se pulsa la acción de recuperación,  
**entonces** se limpian ambos y se restaura el listado completo.

### CA-10 — Estados exclusivos

**Dado** cualquier estado,  
**cuando** se renderiza,  
**entonces** no aparecen simultáneamente RecyclerView, carga, vacío, sin resultados y error.

### CA-11 — EmptyDatabase tiene prioridad

**Dado** que no existen registros aunque haya criterios activos,  
**cuando** se renderiza,  
**entonces** aparece `EMPTY_DATABASE` y no `NO_RESULTS`.

### CA-12 — Criterios visibles

**Dado** `NO_RESULTS`,  
**cuando** se muestra,  
**entonces** búsqueda y filtros continúan reflejando los valores activos.

### CA-13 — Actualización observable

**Dado** un estado vacío o sin resultados,  
**cuando** Room cambia,  
**entonces** la UI se actualiza automáticamente.

### CA-14 — Último registro eliminado

**Dado** un único registro,  
**cuando** se elimina,  
**entonces** aparece `EMPTY_DATABASE`.

### CA-15 — Último resultado eliminado

**Dado** varios registros y un único resultado visible,  
**cuando** se elimina,  
**entonces** aparece `NO_RESULTS`.

### CA-16 — Rotación

**Dado** un estado vacío o sin resultados,  
**cuando** se rota el dispositivo,  
**entonces** el estado y los criterios se conservan.

### CA-17 — Accesibilidad

**Dado** cualquier estado sin contenido,  
**cuando** se navega con tecnologías de asistencia,  
**entonces** títulos y acciones tienen etiquetas comprensibles.

### CA-18 — No es error

**Dado** que una consulta devuelve cero coincidencias,  
**cuando** se procesa,  
**entonces** no se registra ni muestra como error técnico.

---

## 16. Diseño técnico propuesto

### 16.1. WarehouseItemListUiState

El estado actual ya contiene:

```text
status
items
criteria
filterOptions
errorMessage
```

Podrá ampliarse con métodos derivados:

```java
public boolean hasSearchQuery();

public boolean hasActiveFilters();

public NoResultsReason getNoResultsReason();
```

No es necesario duplicar la query o los filtros fuera de `criteria`.

### 16.2. NoResultsReason

Clase o enum orientativo:

```java
public enum NoResultsReason {
    SEARCH,
    FILTERS,
    SEARCH_AND_FILTERS
}
```

Solo deberá ser consultable cuando:

```text
status == NO_RESULTS
```

También puede derivarse en la Activity mediante métodos del estado, pero la clasificación debe tener una única definición.

### 16.3. WarehouseItemListViewModel

Deberá conservar la lógica actual que usa:

- resultado total;
- resultado filtrado;
- `databaseEmpty`.

Deberá añadir:

```java
public void clearAllCriteria()
```

No deberá:

- consultar Views;
- manejar recursos Android;
- navegar;
- inspeccionar el adapter.

### 16.4. MainActivity

Deberá:

- renderizar un único estado cada vez;
- mostrar botón de registro en `EMPTY_DATABASE`;
- reutilizar navegación al formulario;
- construir o seleccionar mensaje contextual;
- ejecutar la acción adecuada;
- soportar el caso combinado;
- no reconstruir criterios manualmente;
- no decidir si Room está vacío examinando el adapter.

### 16.5. Layout

`activity_main.xml` podrá ampliar:

```text
emptyState
├── icon
├── title
├── description
└── emptyStateRegisterButton
```

y:

```text
noResultsState
├── icon
├── title/message
└── clearNoResultsButton
```

Si se utilizan dos acciones en el caso combinado, deberán ser claras y accesibles.

La solución recomendada es un único botón contextual cuyo texto y acción cambian según `NoResultsReason`.

### 16.6. Recursos

Se recomienda añadir o revisar:

```text
warehouse_empty_title
warehouse_empty_description
warehouse_empty_register_action
warehouse_search_no_results
warehouse_filter_no_results
warehouse_search_filter_no_results
clear_search_action
clear_filters_action
clear_all_criteria_action
```

También podrá añadirse un drawable local para sin resultados.

### 16.7. Arquitectura

HU-09 no necesita un nuevo caso de uso de aplicación.

Los estados se derivan de operaciones ya existentes.

No se creará:

```text
GetEmptyStateUseCase
```

porque no representa una operación real del dominio o de aplicación.

---

## 17. Decisiones técnicas importantes

### 17.1. HU-09 es una consolidación de UI y estado

Los estados ya existen parcialmente.

La historia debe completar comportamiento, recuperación y pruebas, no duplicar el flujo.

### 17.2. EmptyDatabase depende del total real

No se determinará a partir de la lista filtrada.

### 17.3. NoResults depende de criterios activos

No debería existir `NO_RESULTS` con:

```text
query vacía
sin filtros
```

Si ocurre, representa una inconsistencia que debe cubrirse con prueba.

### 17.4. Acción combinada debe recuperar contenido

En búsqueda + filtros, limpiar solo una parte no garantiza salir de `NO_RESULTS`.

### 17.5. Room sigue siendo fuente de verdad

No se insertarán elementos manualmente en el adapter tras crear.

### 17.6. El FAB no sustituye completamente el CTA vacío

El FAB puede mantenerse, pero un botón explícito dentro del estado vacío mejora el significado de la pantalla.

### 17.7. No mostrar datos obsoletos

Al entrar en estado vacío, sin resultados o error, el adapter deberá recibir una lista vacía o `null` según la convención actual.

### 17.8. Sin animaciones obligatorias

La claridad funcional tiene prioridad sobre transiciones visuales.

---

## 18. Estructura de archivos orientativa

HU-09 podrá añadir o modificar:

```text
<package-root>/
├── adapter/
│   └── in/
│       └── ui/
│           ├── activity/
│           │   └── MainActivity.java
│           ├── state/
│           │   ├── WarehouseItemListUiState.java
│           │   └── NoResultsReason.java
│           └── viewmodel/
│               └── WarehouseItemListViewModel.java
└── res/
    ├── drawable/
    │   └── ic_search_off.xml
    ├── layout/
    │   └── activity_main.xml
    └── values/
        └── strings.xml
```

También se modificarán pruebas existentes de:

```text
WarehouseItemListViewModelTest
```

y podrán añadirse pruebas de `MainActivity`.

No se crearán capas de dominio, puertos o repositorios sin necesidad.

---

## 19. Pruebas recomendadas

### 19.1. WarehouseItemListViewModel

- emite `EMPTY_DATABASE` cuando el total es cero;
- emite `EMPTY_DATABASE` aunque existan criterios activos;
- emite `CONTENT` con resultados;
- emite `NO_RESULTS` con búsqueda;
- emite `NO_RESULTS` con filtros;
- emite `NO_RESULTS` con búsqueda y filtros;
- nunca emite `NO_RESULTS` sin criterios;
- `clearSearch()` conserva filtros;
- `clearFilters()` conserva búsqueda;
- `clearAllCriteria()` limpia todo;
- recupera `CONTENT` después de limpiar;
- mantiene criterios ante rotación;
- actualiza estado después de creación;
- actualiza estado después de edición;
- actualiza estado después de eliminación;
- transforma errores correctamente.

### 19.2. WarehouseItemListUiState

- deriva correctamente `SEARCH`;
- deriva correctamente `FILTERS`;
- deriva correctamente `SEARCH_AND_FILTERS`;
- expone lista inmutable;
- conserva criterios;
- no devuelve motivo de resultados para otro estado, o lo hace de forma controlada.

### 19.3. MainActivity

- muestra solo Progress en Loading;
- muestra solo RecyclerView en Content;
- muestra `emptyState` en EmptyDatabase;
- muestra CTA de registro;
- muestra `noResultsState` en NoResults;
- utiliza mensaje de búsqueda;
- utiliza mensaje de filtros;
- utiliza mensaje combinado;
- asigna acción Limpiar búsqueda;
- asigna acción Limpiar filtros;
- asigna acción Limpiar todo;
- muestra error exclusivamente;
- abre formulario desde CTA vacío;
- mantiene FAB operativo;
- no filtra ni consulta Room directamente.

### 19.4. Integración

- base vacía → registrar → contenido;
- contenido → búsqueda sin resultados;
- contenido → filtros sin resultados;
- contenido → búsqueda + filtros sin resultados;
- no resultados → limpiar criterio → contenido;
- contenido → eliminar último registro → base vacía;
- resultado único → eliminar → sin resultados;
- no resultados → crear coincidencia → contenido;
- no resultados → crear no coincidencia → permanece;
- rotación en cada estado.

### 19.5. Accesibilidad

- botones tienen texto o content description;
- iconos decorativos no se anuncian;
- el mensaje principal se anuncia correctamente;
- orden de foco lógico;
- acciones tienen tamaño táctil adecuado.

### 19.6. Pruebas manuales

- inicio sin datos;
- CTA Registrar;
- FAB desde base vacía;
- búsqueda inexistente;
- filtros incompatibles;
- búsqueda más filtros;
- limpiar búsqueda;
- limpiar filtros;
- limpiar todo;
- rotar en EmptyDatabase;
- rotar en NoResults;
- eliminar último registro;
- editar último resultado para que deje de coincidir;
- crear resultado coincidente;
- error simulado;
- modo oscuro;
- pantalla pequeña;
- uso sin conexión.

---

## 20. Tareas de implementación

1. Confirmar HU-08 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado de HU-08.
4. Crear `feature/hu-09-estados-vacios-sin-resultados`.
5. Auditar `WarehouseItemListUiState`.
6. Auditar `WarehouseItemListViewModel`.
7. Auditar `MainActivity`.
8. Auditar `activity_main.xml`.
9. Mantener `EMPTY_DATABASE` basado en el total real.
10. Mantener `NO_RESULTS` basado en consulta vacía con base no vacía.
11. Definir `NoResultsReason` o derivación equivalente.
12. Añadir `clearAllCriteria()`.
13. Actualizar lógica de `clearNoResultsButton`.
14. Añadir CTA de registro en `emptyState`.
15. Reutilizar método de navegación al formulario.
16. Añadir icono específico para `NO_RESULTS`.
17. Revisar mensajes de strings.
18. Añadir mensaje/acción para criterios combinados.
19. Garantizar estados visuales exclusivos.
20. Verificar visibilidad del FAB.
21. Revisar accesibilidad.
22. Ampliar `WarehouseItemListViewModelTest`.
23. Crear pruebas del estado si procede.
24. Crear pruebas de Activity necesarias.
25. Ejecutar pruebas instrumentadas.
26. Ejecutar `./gradlew testDebugUnitTest`.
27. Ejecutar `./gradlew lintDebug`.
28. Ejecutar `./gradlew assembleDebug`.
29. Publicar commits representativos.
30. Verificar CI en la rama.
31. Revisar criterios de aceptación.
32. Recopilar evidencias.
33. Fusionar localmente en `develop`.
34. Verificar CI en `develop`.
35. Eliminar la rama local y remota tras confirmar la integración.

---

## 21. Evidencias necesarias para cerrar la HU

- captura de `EMPTY_DATABASE`;
- captura de CTA Registrar;
- evidencia de navegación al formulario;
- captura de `NO_RESULTS` por búsqueda;
- captura de `NO_RESULTS` por filtros;
- captura de `NO_RESULTS` combinado;
- evidencia de Limpiar búsqueda;
- evidencia de Limpiar filtros;
- evidencia de Limpiar búsqueda y filtros;
- evidencia de contenido restaurado;
- evidencia de estados mutuamente exclusivos;
- evidencia de creación desde base vacía;
- evidencia de eliminación del último registro;
- evidencia de eliminación del último resultado;
- evidencia de edición que deja de coincidir;
- evidencia de creación de resultado coincidente;
- evidencia de rotación;
- evidencia de modo oscuro;
- evidencia de accesibilidad básica;
- resultado de pruebas unitarias;
- resultado de pruebas de interfaz;
- resultado de pruebas instrumentadas;
- resultado de lint;
- compilación debug correcta;
- CI satisfactoria en `feature/hu-09-estados-vacios-sin-resultados`;
- merge local en `develop`;
- CI satisfactoria en `develop`;
- confirmación de que `MainActivity` no consulta Room;
- confirmación de que no se creó un caso de uso artificial.

---

## 22. Definición de terminado

HU-09 estará terminada cuando:

- exista un estado claro de base vacía;
- exista un estado claro de sin resultados;
- ambos estados sean diferentes;
- `EMPTY_DATABASE` dependa del total real;
- `NO_RESULTS` dependa de criterios sin coincidencias;
- se distinga búsqueda;
- se distingan filtros;
- se distinga búsqueda más filtros;
- los mensajes sean contextuales;
- exista CTA de registro en base vacía;
- exista acción de limpiar búsqueda;
- exista acción de limpiar filtros;
- exista acción de limpiar todos los criterios;
- la acción combinada garantice recuperar el listado completo;
- los controles reflejen los criterios activos;
- los estados visuales sean mutuamente exclusivos;
- el RecyclerView no aparezca con un estado vacío;
- carga, error y estados sin contenido no se solapen;
- el FAB mantenga un comportamiento coherente;
- Room continúe siendo fuente de verdad;
- creación, edición y eliminación actualicen automáticamente el estado;
- rotación conserve criterios y estado;
- la interfaz sea accesible;
- no se creen capas o casos de uso innecesarios;
- pruebas unitarias finalicen correctamente;
- pruebas instrumentadas necesarias finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- documentación y evidencias queden completas;
- la rama se fusione localmente en `develop`;
- CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 23. Resultado esperado

Al cerrar HU-09, el usuario comprenderá siempre por qué el listado no muestra mercancía y dispondrá de una forma clara de continuar:

```text
Room sin registros
        ↓
EMPTY_DATABASE
        ↓
Registrar mercancía
```

o:

```text
Room con registros
+ criterios sin coincidencias
        ↓
NO_RESULTS contextual
        ↓
Limpiar búsqueda, filtros o todo
        ↓
CONTENT
```

El proyecto quedará preparado para continuar con:

```text
HU-10 — Eliminar varios registros
```

---

## 24. Commit documental recomendado

```text
git commit -m "docs: add HU-09 empty and no-results states plan #10"
```
