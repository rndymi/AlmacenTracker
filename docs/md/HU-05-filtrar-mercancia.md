# HU-05 — Filtrar mercancía

> Quinta historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-05  
**Nombre:** Filtrar mercancía  
**Prioridad:** Media  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-05-filtrar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero filtrar la mercancía por categoría, sitio o posición,  
para reducir el listado y localizar con mayor precisión los registros que necesito.

---

## 3. Objetivo

Ampliar el listado principal para permitir filtros exactos y combinables sobre:

- categoría;
- sitio;
- posición.

Los filtros deberán funcionar junto con la búsqueda textual implementada en la HU-04 y conservar Room como fuente de verdad.

Flujo previsto:

```text
Controles de filtro + búsqueda existente
                ↓
WarehouseItemListViewModel
                ↓
FilterWarehouseItemsUseCase
                ↓
FilterWarehouseItemsService
                ↓
WarehouseItemRepository
                ↓
RoomWarehouseItemRepository
                ↓
WarehouseItemDao
                ↓
Room / SQLite
```

La Activity se limitará a capturar selecciones y renderizar el estado. No deberá filtrar manualmente la lista recibida.

---

## 4. Estado real del proyecto antes de la HU-05

La implementación actual ya dispone de:

- `MainActivity` con listado observable;
- `WarehouseItemAdapter` con navegación al detalle por `id`;
- `WarehouseItemListViewModel` basado en `MediatorLiveData`;
- `ObserveWarehouseItemsUseCase`;
- `SearchWarehouseItemsUseCase`;
- `SearchWarehouseItemsService`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao` con `observeAll()`, `search()` y `observeById()`;
- estados `LOADING`, `CONTENT`, `EMPTY_DATABASE`, `NO_RESULTS` y `ERROR`;
- conservación del texto de búsqueda en el ViewModel;
- diferenciación entre base vacía y búsqueda sin resultados.

La HU-05 deberá extender esta implementación sin reemplazarla por una segunda lógica paralela.

---

## 5. Alcance incluido

La HU-05 incluye:

- acceso a controles de filtro desde `MainActivity`;
- filtro por categoría;
- filtro por sitio;
- filtro por posición;
- opción general “Todas” o equivalente para cada filtro;
- posibilidad de representar mercancía sin posición;
- combinación simultánea de varios filtros;
- combinación de filtros con el texto de búsqueda de HU-04;
- filtros con coincidencia exacta;
- comparación sin distinguir mayúsculas y minúsculas;
- obtención de opciones disponibles desde Room;
- eliminación de valores duplicados en las opciones;
- orden estable de las opciones;
- indicación visual de filtros activos;
- acción para limpiar todos los filtros;
- estado con resultados;
- estado sin resultados;
- diferenciación entre base vacía y filtros sin coincidencias;
- conservación de filtros ante rotación;
- conservación de búsqueda y filtros de forma conjunta;
- actualización automática cuando cambian los datos de Room;
- navegación al detalle desde el listado filtrado;
- pruebas unitarias, DAO y de interfaz relacionadas.

---

## 6. Alcance excluido

La HU-05 no incluye:

- ordenación configurable;
- rangos de fechas;
- filtros por observaciones;
- filtros por fecha de creación;
- filtros por fecha de actualización;
- guardado de filtros favoritos;
- historial de filtros;
- categorías configurables;
- múltiples almacenes;
- paginación;
- edición;
- eliminación individual;
- selección múltiple;
- sincronización remota;
- búsqueda por voz;
- chips dinámicos creados desde un backend.

La HU-05 no deberá implementar anticipadamente las funciones de HU-06 o posteriores.

---

## 7. Precondiciones

Antes de comenzar la HU-05 deberán cumplirse estas condiciones:

- HU-01 implementada y fusionada en `develop`;
- HU-02 implementada y fusionada en `develop`;
- HU-03 implementada y fusionada en `develop`;
- HU-04 implementada y fusionada en `develop`;
- CI de `develop` satisfactoria;
- búsqueda textual operativa;
- listado observable desde Room;
- navegación al detalle por `id` operativa;
- `WarehouseItemListViewModel` conserva el criterio de búsqueda;
- `WarehouseItemListUiState` diferencia base vacía y sin resultados;
- `WarehouseItemDao` conserva el orden por categoría y código;
- Room continúa siendo la única fuente de datos.

---

## 8. Filtros disponibles

### 8.1. Categoría

El usuario podrá seleccionar una categoría exacta existente.

Ejemplo:

```text
Todas
CA
MD
MR
```

Seleccionar `MR` mostrará únicamente registros cuya categoría sea `MR`.

### 8.2. Sitio

El usuario podrá seleccionar un sitio exacto existente.

Ejemplo:

```text
Todos
A1
A2
B3
C1
```

Seleccionar `A1` mostrará únicamente mercancía ubicada en ese sitio.

### 8.3. Posición

El usuario podrá seleccionar una posición exacta existente.

Ejemplo:

```text
Todas
Sin posición
Nivel 1
Nivel 2
Nivel 3
Nivel 4
```

`Sin posición` deberá representar registros cuyo valor sea `null`, vacío o solo contenga espacios según la convención de persistencia del proyecto.

### 8.4. Valores únicos

Las opciones se obtendrán desde Room sin duplicados.

Ejemplo:

```text
MR
MR
MD
```

deberá producir:

```text
MD
MR
```

### 8.5. Orden

Las opciones se mostrarán en orden alfabético o natural sin distinguir mayúsculas y minúsculas.

La opción general y `Sin posición` deberán ocupar una posición visual coherente y predecible.

---

## 9. Reglas funcionales

### 9.1. Coincidencia exacta de filtros

Los filtros son distintos de la búsqueda textual:

```text
Búsqueda: coincidencia parcial.
Filtro: coincidencia exacta.
```

Ejemplo:

```text
Filtro de sitio: A1
```

No deberá incluir automáticamente `A10` o `BA1`.

### 9.2. Comparación sin distinguir mayúsculas

```text
MR
mr
Mr
```

se considerarán equivalentes al aplicar el filtro.

### 9.3. Combinación mediante AND

Cuando se seleccionen varios filtros, todos deberán cumplirse.

Ejemplo:

```text
Categoría = MR
Sitio = A1
Posición = Nivel 2
```

Resultado:

```text
mercancía MR ubicada en A1 y Nivel 2
```

### 9.4. Combinación con búsqueda

La búsqueda global y los filtros deberán combinarse también mediante AND.

Ejemplo:

```text
Búsqueda = 105
Categoría = MR
Sitio = A1
```

El resultado deberá:

1. coincidir parcialmente con `105` en categoría, código, sitio o posición;
2. tener categoría exacta `MR`;
3. tener sitio exacto `A1`.

### 9.5. Filtros no seleccionados

Un filtro en “Todas” no limitará la consulta.

### 9.6. Limpiar filtros

La acción Limpiar deberá:

- restablecer categoría;
- restablecer sitio;
- restablecer posición;
- conservar el texto de búsqueda activo;
- recalcular el listado.

La limpieza completa de búsqueda y filtros podrá ofrecerse como acción adicional, pero no sustituirá la limpieza exclusiva de filtros.

### 9.7. Actualización de opciones

Si Room cambia:

- las opciones disponibles deberán actualizarse;
- una nueva categoría o sitio deberá aparecer;
- una opción sin registros podrá desaparecer;
- el estado seleccionado deberá tratarse de forma controlada.

Si una opción activa deja de existir, la implementación deberá evitar un estado inconsistente. Se recomienda conservar el criterio hasta que el usuario lo limpie o restablecerlo documentadamente a “Todas”. La decisión final deberá ser uniforme y estar cubierta por pruebas.

### 9.8. Orden de resultados

Los resultados conservarán el orden establecido desde HU-01:

1. categoría ascendente;
2. código ascendente.

---

## 10. Criterio de filtros

Se recomienda representar los filtros mediante un objeto inmutable:

```text
WarehouseItemFilterCriteria
├── query
├── category
├── site
└── positionFilter
```

`positionFilter` deberá distinguir al menos:

```text
ALL
WITHOUT_POSITION
EXACT_VALUE
```

Esto evita utilizar valores mágicos como:

```text
""
"NULL"
"SIN_POSICION"
```

como reglas de negocio dispersas por la UI o el DAO.

Una alternativa válida es utilizar campos explícitos:

```text
position
withoutPosition
```

siempre que no puedan producir combinaciones contradictorias.

---

## 11. Estados de interfaz

### 11.1. Loading

Se están obteniendo resultados u opciones.

Características:

- criterio de búsqueda conservado;
- filtros seleccionados conservados;
- indicador discreto;
- interfaz no bloqueada.

### 11.2. Content

Existen registros que cumplen la búsqueda y los filtros.

Características:

- RecyclerView visible;
- filtros activos identificables;
- navegación al detalle disponible;
- resultados ordenados.

### 11.3. EmptyDatabase

Room no contiene mercancía.

Mensaje orientativo:

```text
Todavía no hay mercancía registrada.
```

No deberá mostrarse como un fallo de filtros.

### 11.4. NoResults

Existen registros en Room, pero ninguno cumple la combinación actual.

Mensaje orientativo:

```text
No se encontraron resultados con los filtros seleccionados.
```

Cuando también exista búsqueda:

```text
No se encontraron resultados para "105" con los filtros seleccionados.
```

Características:

- filtros visibles;
- búsqueda visible;
- acción para limpiar filtros;
- no se confunde con base vacía.

### 11.5. Error

La consulta o la carga de opciones falla.

Mensaje orientativo:

```text
No se pudieron aplicar los filtros.
```

La aplicación no deberá cerrarse ni mostrar excepciones técnicas.

---

## 12. Flujo principal

1. El usuario abre AlmacenTracker.
2. `MainActivity` muestra el listado completo.
3. El usuario abre o visualiza los controles de filtro.
4. Selecciona una categoría.
5. Opcionalmente selecciona sitio y posición.
6. La Activity notifica los cambios al ViewModel.
7. El ViewModel actualiza `WarehouseItemFilterCriteria`.
8. El ViewModel invoca `FilterWarehouseItemsUseCase`.
9. El servicio normaliza los criterios.
10. El repositorio ejecuta la consulta observable.
11. Room aplica búsqueda y filtros.
12. El mapper convierte entidades a dominio.
13. El ViewModel emite el estado correspondiente.
14. La Activity actualiza el RecyclerView.
15. El usuario puede abrir el detalle de un resultado.

---

## 13. Flujos alternativos

### FA-01 — Filtrar solo por categoría

1. El usuario selecciona `MR`.
2. Los demás filtros permanecen en “Todas”.
3. Se muestran únicamente registros `MR`.

### FA-02 — Filtrar solo por sitio

1. El usuario selecciona `A1`.
2. Se muestran únicamente registros ubicados en `A1`.

### FA-03 — Filtrar solo por posición

1. El usuario selecciona `Nivel 2`.
2. Se muestran únicamente registros con esa posición.

### FA-04 — Filtrar mercancía sin posición

1. El usuario selecciona `Sin posición`.
2. Se muestran registros cuyo campo posición está ausente.
3. No se produce error por valores nulos.

### FA-05 — Combinar categoría y sitio

1. El usuario selecciona `MR`.
2. Selecciona `A1`.
3. Solo se muestran registros que cumplen ambos criterios.

### FA-06 — Combinar los tres filtros

1. El usuario selecciona categoría, sitio y posición.
2. La consulta aplica los tres criterios mediante AND.

### FA-07 — Combinar búsqueda y filtros

1. Existe una búsqueda activa.
2. El usuario añade uno o varios filtros.
3. Los resultados cumplen búsqueda y filtros simultáneamente.

### FA-08 — Limpiar filtros

1. Existen filtros activos.
2. El usuario pulsa Limpiar filtros.
3. Los filtros vuelven a “Todas”.
4. La búsqueda activa se conserva.
5. El listado se recalcula.

### FA-09 — Sin resultados

1. La combinación no coincide con ningún registro.
2. El ViewModel emite `NoResults`.
3. La interfaz muestra un mensaje y permite limpiar.

### FA-10 — Base vacía

1. Room no contiene registros.
2. Los filtros no generan `NoResults`.
3. Se muestra `EmptyDatabase`.

### FA-11 — Rotación

1. Existen búsqueda y filtros activos.
2. El usuario rota el dispositivo.
3. El ViewModel conserva criterios y resultados.
4. Los controles visuales reflejan las selecciones correctas.

### FA-12 — Nuevo registro coincidente

1. Existen filtros activos.
2. Room recibe un nuevo registro coincidente.
3. El listado se actualiza automáticamente.

### FA-13 — Nuevo valor de filtro

1. Se registra mercancía con una categoría o sitio nuevo.
2. Room actualiza las opciones.
3. La nueva opción aparece sin reiniciar la aplicación.

### FA-14 — Error inesperado

1. La consulta falla.
2. El error se transforma en un resultado de aplicación.
3. El ViewModel emite `Error`.
4. La Activity conserva criterios y no se cierra.

---

## 14. Criterios de aceptación

### CA-01 — Filtros disponibles

**Dado** que el usuario está en el listado,  
**cuando** accede a los controles de filtro,  
**entonces** puede filtrar por categoría, sitio y posición.

### CA-02 — Categorías únicas

**Dado** que existen varias mercancías con la misma categoría,  
**cuando** se cargan las opciones,  
**entonces** la categoría aparece una sola vez.

### CA-03 — Sitios únicos

**Dado** que existen varias mercancías en el mismo sitio,  
**cuando** se cargan las opciones,  
**entonces** el sitio aparece una sola vez.

### CA-04 — Posiciones únicas

**Dado** que existen varias mercancías en la misma posición,  
**cuando** se cargan las opciones,  
**entonces** la posición aparece una sola vez.

### CA-05 — Filtro por categoría

**Dado** que se selecciona una categoría,  
**cuando** se aplica el filtro,  
**entonces** solo aparecen registros con esa categoría exacta.

### CA-06 — Filtro por sitio

**Dado** que se selecciona un sitio,  
**cuando** se aplica el filtro,  
**entonces** solo aparecen registros con ese sitio exacto.

### CA-07 — Filtro por posición

**Dado** que se selecciona una posición,  
**cuando** se aplica el filtro,  
**entonces** solo aparecen registros con esa posición exacta.

### CA-08 — Sin posición

**Dado** que existen registros sin posición,  
**cuando** el usuario selecciona `Sin posición`,  
**entonces** esos registros se muestran correctamente.

### CA-09 — Combinación de filtros

**Dado** que se seleccionan varios filtros,  
**cuando** se ejecuta la consulta,  
**entonces** los resultados cumplen todos los filtros.

### CA-10 — Combinación con búsqueda

**Dado** que existe una búsqueda textual activa,  
**cuando** se seleccionan filtros,  
**entonces** los resultados cumplen búsqueda y filtros.

### CA-11 — Comparación exacta

**Dado** un filtro de sitio `A1`,  
**cuando** se aplica,  
**entonces** no se incluye `A10` únicamente por contener el mismo texto.

### CA-12 — Mayúsculas y minúsculas

**Dado** un valor almacenado con distinta capitalización,  
**cuando** se aplica el filtro equivalente,  
**entonces** el registro coincide.

### CA-13 — Limpiar filtros

**Dado** que existen filtros activos,  
**cuando** el usuario los limpia,  
**entonces** todos vuelven al estado general y la búsqueda se conserva.

### CA-14 — Indicador de filtros activos

**Dado** que existe al menos un filtro seleccionado,  
**cuando** se muestra el listado,  
**entonces** la interfaz indica que hay filtros activos.

### CA-15 — Sin resultados

**Dado** que ningún registro cumple los criterios,  
**cuando** se aplican,  
**entonces** se muestra `NoResults` y no `EmptyDatabase`.

### CA-16 — Base vacía

**Dado** que Room no contiene registros,  
**cuando** se abre el listado,  
**entonces** se muestra `EmptyDatabase`.

### CA-17 — Rotación

**Dado** que existen filtros activos,  
**cuando** el usuario rota el dispositivo,  
**entonces** los criterios y resultados se conservan.

### CA-18 — Opciones observables

**Dado** que Room incorpora una nueva categoría, sitio o posición,  
**cuando** la fuente emite el cambio,  
**entonces** las opciones se actualizan automáticamente.

### CA-19 — Navegación al detalle

**Dado** que existe un resultado filtrado,  
**cuando** el usuario lo pulsa,  
**entonces** se abre el detalle correcto mediante su `id`.

### CA-20 — Error controlado

**Dado** que la consulta falla,  
**cuando** la aplicación recibe el error,  
**entonces** muestra un mensaje y no se cierra.

### CA-21 — Operación no bloqueante

**Dado** que se aplican filtros,  
**cuando** Room ejecuta la consulta,  
**entonces** no se bloquea el hilo principal.

---

## 15. Diseño técnico propuesto

### 15.1. Criterio de entrada

`WarehouseItemFilterCriteria` representará el estado completo de consulta.

Firma conceptual:

```java
public final class WarehouseItemFilterCriteria {
    private final String query;
    private final String category;
    private final String site;
    private final PositionFilter positionFilter;
}
```

Deberá ser inmutable o comportarse como valor estable para evitar estados contradictorios.

### 15.2. Filtro de posición

Modelo orientativo:

```java
public final class PositionFilter {
    public enum Type {
        ALL,
        WITHOUT_POSITION,
        EXACT_VALUE
    }

    private final Type type;
    private final String value;
}
```

`value` solo será obligatorio para `EXACT_VALUE`.

### 15.3. Puerto de entrada

`FilterWarehouseItemsUseCase` representará la consulta combinada.

Firma orientativa:

```java
public interface FilterWarehouseItemsUseCase {
    LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    );
}
```

La firma definitiva deberá mantener el patrón ya utilizado en el proyecto.

### 15.4. Servicio de aplicación

`FilterWarehouseItemsService` deberá:

- normalizar búsqueda y filtros;
- interpretar filtros generales;
- validar combinaciones internas;
- delegar la consulta al repositorio;
- no depender de Activities, Views o Room.

### 15.5. Opciones de filtro

Se recomienda un puerto de entrada adicional:

```text
ObserveWarehouseItemFilterOptionsUseCase
```

que exponga:

```text
WarehouseItemFilterOptions
├── categories
├── sites
├── positions
└── hasItemsWithoutPosition
```

Esto evita que `MainActivity` inspeccione el listado para construir opciones.

Si las opciones se derivan del listado observable dentro del ViewModel o servicio, deberá justificarse y mantenerse fuera de la Activity.

### 15.6. Puerto de salida

`WarehouseItemRepository` podrá ampliarse con:

```java
LiveData<WarehouseItemsResult> filter(
        WarehouseItemFilterCriteria criteria
);

LiveData<WarehouseItemFilterOptionsResult>
observeFilterOptions();
```

La estructura exacta puede adaptarse, pero deberá existir una única ruta coherente entre UI y Room.

### 15.7. DAO — consulta combinada

Consulta conceptual:

```sql
SELECT *
FROM warehouse_items
WHERE
    (
        :query = ''
        OR category LIKE '%' || :query || '%' COLLATE NOCASE
        OR code LIKE '%' || :query || '%' COLLATE NOCASE
        OR site LIKE '%' || :query || '%' COLLATE NOCASE
        OR position LIKE '%' || :query || '%' COLLATE NOCASE
    )
    AND (
        :category IS NULL
        OR category = :category COLLATE NOCASE
    )
    AND (
        :site IS NULL
        OR site = :site COLLATE NOCASE
    )
    AND (
        :positionMode = 'ALL'
        OR (
            :positionMode = 'WITHOUT_POSITION'
            AND (position IS NULL OR TRIM(position) = '')
        )
        OR (
            :positionMode = 'EXACT_VALUE'
            AND position = :position COLLATE NOCASE
        )
    )
ORDER BY
    category COLLATE NOCASE ASC,
    code COLLATE NOCASE ASC
```

Room no debería recibir directamente un enum si la conversión añade complejidad innecesaria. El repositorio podrá transformar el criterio a parámetros simples.

### 15.8. DAO — opciones distintas

Consultas orientativas:

```java
@Query(
    "SELECT DISTINCT category FROM warehouse_items " +
    "WHERE TRIM(category) <> '' " +
    "ORDER BY category COLLATE NOCASE ASC"
)
LiveData<List<String>> observeCategories();
```

```java
@Query(
    "SELECT DISTINCT site FROM warehouse_items " +
    "WHERE TRIM(site) <> '' " +
    "ORDER BY site COLLATE NOCASE ASC"
)
LiveData<List<String>> observeSites();
```

```java
@Query(
    "SELECT DISTINCT position FROM warehouse_items " +
    "WHERE position IS NOT NULL " +
    "AND TRIM(position) <> '' " +
    "ORDER BY position COLLATE NOCASE ASC"
)
LiveData<List<String>> observePositions();
```

También deberá poder determinarse si existen elementos sin posición.

### 15.9. Adaptador Room

`RoomWarehouseItemRepository` deberá:

- traducir el criterio a parámetros DAO;
- mapear entidades a dominio;
- combinar opciones si se usan varias fuentes LiveData;
- conservar observabilidad;
- no ejecutar filtrado manual en `MainActivity`.

### 15.10. ViewModel

`WarehouseItemListViewModel` deberá ampliarse para:

- conservar `searchQuery` existente;
- conservar categoría seleccionada;
- conservar sitio seleccionado;
- conservar posición seleccionada;
- cambiar la fuente observable sin acumular observadores;
- exponer opciones disponibles;
- exponer filtros activos;
- limpiar filtros;
- recalcular resultados;
- conservar estado ante rotación;
- diferenciar `EmptyDatabase` y `NoResults`.

La lógica actual basada en `MediatorLiveData` deberá evolucionar sin mantener simultáneamente fuentes obsoletas.

### 15.11. Estado de interfaz

`WarehouseItemListUiState` podrá ampliarse con:

```text
query
items
selectedCategory
selectedSite
selectedPosition
availableCategories
availableSites
availablePositions
hasItemsWithoutPosition
activeFilterCount
errorMessage
```

No es obligatorio almacenar todas las opciones en el mismo estado si el ViewModel expone un estado separado y consistente.

### 15.12. Activity

`MainActivity` deberá:

- renderizar controles;
- notificar selecciones;
- mostrar filtros activos;
- ejecutar limpiar filtros mediante ViewModel;
- mantener búsqueda existente;
- no construir consultas SQL;
- no filtrar listas;
- no acceder al repositorio concreto.

### 15.13. Composición de dependencias

`AppContainer` deberá proporcionar:

- `FilterWarehouseItemsService`;
- servicio de opciones si se crea;
- dependencias actualizadas de `WarehouseItemListViewModelFactory`.

No se añadirá un framework de inyección exclusivamente para esta HU.

---

## 16. Decisiones técnicas importantes

### 16.1. Extender la búsqueda actual

No se mantendrán dos listados independientes:

```text
listado buscado
listado filtrado
```

Debe existir una única consulta combinada basada en el estado completo.

### 16.2. Filtros en Room

Los filtros se aplicarán en Room, no mediante `stream()`, bucles o copias dentro de `MainActivity`.

### 16.3. Opciones desde datos reales

Categorías, sitios y posiciones deberán derivarse de los registros existentes.

No se codificarán valores fijos como:

```text
MR
MD
A1
A2
Nivel 1
Nivel 2
```

### 16.4. “Sin posición” es una opción semántica

No se guardará el texto `Sin posición` en Room para representar ausencia.

### 16.5. No sustituir búsqueda parcial por filtro exacto

Búsqueda y filtros tienen comportamientos distintos y complementarios.

### 16.6. Evitar observadores duplicados

Cada cambio de criterio deberá retirar o reemplazar la fuente anterior.

### 16.7. No crear una nueva Activity de resultados

Los resultados seguirán mostrándose en `MainActivity`.

### 16.8. No adelantar ordenación

La configuración de orden queda fuera del alcance.

---

## 17. Estructura de archivos orientativa

La HU-05 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── FilterWarehouseItemsUseCase.java
│   │   │   ├── ObserveWarehouseItemFilterOptionsUseCase.java
│   │   │   ├── WarehouseItemFilterCriteria.java
│   │   │   └── PositionFilter.java
│   │   └── out/
│   │       └── WarehouseItemRepository.java
│   ├── result/
│   │   └── WarehouseItemFilterOptionsResult.java
│   └── service/
│       ├── FilterWarehouseItemsService.java
│       └── ObserveWarehouseItemFilterOptionsService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   └── MainActivity.java
│   │       ├── state/
│   │       │   └── WarehouseItemListUiState.java
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
```

La estructura deberá adaptarse al código real y podrá simplificarse si se conservan responsabilidades claras.

No se crearán clases vacías ni duplicados de `SearchWarehouseItemsUseCase` sin una decisión explícita. Si la consulta combinada reemplaza internamente la ruta de búsqueda anterior, la compatibilidad deberá mantenerse o refactorizarse con pruebas.

---

## 18. Diseño de interfaz esperado

Propuesta orientativa:

```text
Toolbar
AlmacenTracker

[ Buscar por categoría, código o ubicación... ]

[ Categoría ▼ ] [ Sitio ▼ ] [ Posición ▼ ]

Filtros activos: 2                  [Limpiar]

RecyclerView / EmptyDatabase / NoResults

FloatingActionButton
```

También será válida una hoja inferior o diálogo de filtros:

```text
[Filtrar]
    ↓
BottomSheet / Dialog
├── Categoría
├── Sitio
├── Posición
├── Limpiar
└── Aplicar
```

La elección deberá priorizar:

- claridad;
- poco espacio ocupado;
- accesibilidad;
- facilidad para pantallas pequeñas;
- coherencia con Material Components.

### Requisitos visuales

- filtros activos reconocibles;
- opción clara para limpiar;
- controles no deben bloquear el FAB;
- controles deben conservar selección al rotar;
- `NoResults` debe permitir recuperar el listado;
- navegación al detalle debe seguir operativa;
- búsqueda y filtros deben verse como funciones complementarias.

---

## 19. Pruebas recomendadas

### 19.1. Servicio de filtros

- normaliza búsqueda;
- normaliza categoría;
- normaliza sitio;
- normaliza posición exacta;
- interpreta “Todas” como ausencia de restricción;
- interpreta `WITHOUT_POSITION`;
- combina criterios correctamente;
- delega al repositorio;
- no depende de Android ni Room.

### 19.2. Servicio de opciones

- devuelve categorías únicas;
- devuelve sitios únicos;
- devuelve posiciones únicas;
- ordena opciones;
- identifica existencia de registros sin posición;
- maneja base vacía;
- propaga errores controlados.

### 19.3. ViewModel

- conserva búsqueda existente;
- aplica categoría;
- aplica sitio;
- aplica posición;
- combina los tres filtros;
- combina búsqueda y filtros;
- limpia filtros sin borrar búsqueda;
- calcula filtros activos;
- emite `Content` con resultados;
- emite `NoResults` sin coincidencias;
- emite `EmptyDatabase` sin datos;
- conserva estado tras recreación;
- no acumula fuentes LiveData;
- actualiza opciones observables.

### 19.4. DAO — resultados

- filtra categoría exacta;
- filtra sitio exacto;
- filtra posición exacta;
- filtra sin posición;
- combina categoría y sitio;
- combina categoría, sitio y posición;
- combina búsqueda parcial y filtros exactos;
- ignora mayúsculas;
- mantiene orden;
- devuelve lista vacía sin coincidencias;
- actualiza resultados al cambiar Room.

### 19.5. DAO — opciones

- obtiene categorías distintas;
- obtiene sitios distintos;
- obtiene posiciones distintas;
- excluye cadenas vacías;
- ordena sin distinguir mayúsculas;
- detecta registros sin posición.

### 19.6. Interfaz

- abre controles de filtro;
- selecciona categoría;
- selecciona sitio;
- selecciona posición;
- muestra filtros activos;
- limpia filtros;
- conserva búsqueda;
- conserva selección tras rotación;
- abre detalle desde resultado;
- muestra `NoResults` correctamente.

### 19.7. Pruebas manuales

- filtro individual por cada campo;
- combinación de dos filtros;
- combinación de tres filtros;
- búsqueda más un filtro;
- búsqueda más tres filtros;
- misma categoría con códigos distintos;
- mismo código en categorías distintas;
- sitio `A1` frente a `A10`;
- posición vacía;
- `Sin posición`;
- base vacía;
- filtros sin resultados;
- limpiar filtros;
- rotación;
- creación de nueva opción;
- varios cambios rápidos;
- uso sin conexión;
- pantalla pequeña.

---

## 20. Tareas de implementación

1. Confirmar HU-04 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado y la implementación real.
4. Crear `feature/hu-05-filtrar-mercancia` desde `develop`.
5. Definir `WarehouseItemFilterCriteria`.
6. Definir representación de posición.
7. Crear `FilterWarehouseItemsUseCase`.
8. Crear `FilterWarehouseItemsService`.
9. Decidir y crear el caso de uso de opciones.
10. Ampliar `WarehouseItemRepository`.
11. Añadir consulta combinada al DAO.
12. Añadir consultas de categorías distintas.
13. Añadir consultas de sitios distintos.
14. Añadir consultas de posiciones distintas.
15. Añadir detección de registros sin posición.
16. Implementar nuevas operaciones en `RoomWarehouseItemRepository`.
17. Confirmar mapeo existente.
18. Refactorizar la ruta de búsqueda para evitar duplicación.
19. Ampliar `WarehouseItemListUiState`.
20. Ampliar `WarehouseItemListViewModel`.
21. Gestionar una única fuente observable de resultados.
22. Gestionar opciones observables.
23. Conservar búsqueda y filtros.
24. Implementar limpieza de filtros.
25. Calcular indicador de filtros activos.
26. Actualizar `WarehouseItemListViewModelFactory`.
27. Actualizar `AppContainer`.
28. Diseñar controles de filtros en `activity_main.xml` o recurso adicional.
29. Conectar controles desde `MainActivity`.
30. Renderizar selecciones desde el estado.
31. Implementar `NoResults` para filtros.
32. Mantener `EmptyDatabase`.
33. Mantener navegación al detalle.
34. Comprobar actualizaciones observables.
35. Crear pruebas unitarias.
36. Crear o ampliar pruebas DAO.
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

- captura de controles de filtro;
- captura de filtro por categoría;
- captura de filtro por sitio;
- captura de filtro por posición;
- captura de `Sin posición`;
- evidencia de opciones sin duplicados;
- evidencia de coincidencia exacta;
- evidencia de combinación de dos filtros;
- evidencia de combinación de tres filtros;
- evidencia de búsqueda más filtros;
- evidencia de filtros activos;
- evidencia de limpiar filtros conservando búsqueda;
- captura de `NoResults`;
- captura de `EmptyDatabase` diferenciada;
- evidencia de conservación tras rotación;
- evidencia de actualización automática de resultados;
- evidencia de actualización de opciones;
- evidencia de navegación al detalle desde resultados filtrados;
- confirmación de que `MainActivity` no filtra listas manualmente;
- confirmación de que Room continúa como fuente de verdad;
- resultado de pruebas unitarias;
- resultado de pruebas DAO;
- resultado de lint;
- compilación debug correcta;
- resultado de pruebas instrumentadas necesarias;
- CI satisfactoria en `feature/hu-05-filtrar-mercancia`;
- evidencia del merge local en `develop`;
- CI satisfactoria en `develop`.

---

## 22. Definición de terminado

La HU-05 estará terminada cuando:

- existan filtros por categoría, sitio y posición;
- las opciones procedan de Room;
- las opciones no contengan duplicados;
- los filtros sean exactos;
- los filtros no distingan mayúsculas;
- exista opción general para no restringir;
- pueda filtrarse mercancía sin posición;
- puedan combinarse varios filtros;
- búsqueda y filtros funcionen conjuntamente;
- limpiar filtros conserve la búsqueda;
- la interfaz indique filtros activos;
- los resultados mantengan el orden de HU-01;
- exista `NoResults` para filtros sin coincidencias;
- `NoResults` se diferencie de `EmptyDatabase`;
- búsqueda y filtros sobrevivan a rotación;
- las opciones se actualicen al cambiar Room;
- los resultados se actualicen al cambiar Room;
- un resultado abra HU-03 mediante su `id`;
- `MainActivity` no filtre manualmente;
- exista una única ruta coherente de consulta;
- no se acumulen observadores;
- la UI dependa de puertos de entrada;
- el dominio no dependa de Android ni Room;
- la consulta no bloquee el hilo principal;
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

Al cerrar la HU-05, AlmacenTracker permitirá refinar la localización de mercancía:

```text
usuario selecciona filtros
        ↓
la aplicación combina búsqueda y criterios
        ↓
Room aplica coincidencia parcial y filtros exactos
        ↓
el ViewModel actualiza estado y opciones
        ↓
la pantalla muestra resultados o NoResults
```

El proyecto quedará preparado para continuar con:

```text
HU-06 — Editar mercancía
```

---

## 24. Commit documental recomendado

```text
docs: add HU-05 warehouse item filtering plan
```
