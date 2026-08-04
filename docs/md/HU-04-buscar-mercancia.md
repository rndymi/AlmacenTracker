# HU-04 — Buscar mercancía

> Cuarta historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-04  
**Nombre:** Buscar mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-04-buscar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero buscar mercancía mediante texto,  
para encontrar rápidamente un registro por su categoría, código o ubicación.

---

## 3. Objetivo

Implementar una búsqueda textual global sobre el listado principal, reutilizando la arquitectura hexagonal ya aplicada en las HUs anteriores.

La búsqueda deberá consultar Room mediante los puertos de aplicación y devolver coincidencias parciales sobre:

- categoría;
- código;
- sitio;
- posición.

Flujo previsto:

```text
SearchView / campo de búsqueda
        ↓
WarehouseItemListViewModel
        ↓
SearchWarehouseItemsUseCase
        ↓
SearchWarehouseItemsService
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
Room / SQLite
```

La búsqueda no deberá implementarse filtrando manualmente una copia del listado dentro de la Activity.

---

## 4. Alcance incluido

La HU-04 incluye:

- barra o campo de búsqueda en `MainActivity`;
- introducción de texto libre;
- búsqueda por categoría;
- búsqueda por código;
- búsqueda por sitio;
- búsqueda por posición;
- coincidencias parciales;
- búsqueda sin distinguir mayúsculas y minúsculas;
- eliminación de espacios externos del criterio;
- actualización del listado al cambiar la consulta;
- restauración del listado completo al vaciar la búsqueda;
- estado con resultados;
- estado sin resultados;
- diferenciación entre base vacía y búsqueda sin coincidencias;
- conservación del texto de búsqueda ante rotación;
- conservación del estado visual ante recreación;
- navegación al detalle desde los resultados;
- actualización observable de los resultados cuando Room cambie;
- control de errores;
- pruebas unitarias, DAO y de interfaz relacionadas.

---

## 5. Alcance excluido

La HU-04 no incluye:

- filtros independientes por categoría;
- filtros independientes por sitio;
- filtros independientes por posición;
- combinación de varios filtros;
- chips de filtros activos;
- ordenación configurable;
- edición;
- eliminación;
- selección múltiple;
- historial de búsquedas;
- sugerencias automáticas;
- búsqueda por observaciones;
- búsqueda remota;
- sincronización;
- paginación.

Los filtros avanzados pertenecen a la HU-05.

---

## 6. Precondiciones

Antes de comenzar la HU-04 deberán cumplirse estas condiciones:

- HU-01 implementada y fusionada en `develop`;
- HU-02 implementada y fusionada en `develop`;
- HU-03 implementada y fusionada en `develop`;
- CI de `develop` satisfactoria;
- `MainActivity` muestra el listado observable;
- `WarehouseItemAdapter` permite abrir el detalle por `id`;
- existe `WarehouseItemRepository`;
- existe `RoomWarehouseItemRepository`;
- existe `WarehouseItemDao`;
- existe `WarehouseItemListViewModel`;
- existe `AppContainer`;
- Room continúa siendo la fuente de verdad.

---

## 7. Campos incluidos en la búsqueda

| Campo | Incluido | Ejemplo de criterio | Resultado esperado |
|---|---:|---|---|
| Categoría | Sí | `MR` | Registros cuya categoría contiene `MR` |
| Código | Sí | `105` | Registros cuyo código contiene `105` |
| Sitio | Sí | `A1` | Registros cuyo sitio contiene `A1` |
| Posición | Sí | `nivel 2` | Registros cuya posición contiene `nivel 2` |
| Observaciones | No | — | Fuera del alcance de HU-04 |

La búsqueda será global: el mismo texto se comparará contra los cuatro campos incluidos.

---

## 8. Reglas de búsqueda

### 8.1. Coincidencia parcial

El criterio podrá coincidir con cualquier parte del valor.

Ejemplo:

```text
Criterio: 105
Coincide con: 1050, A105, 2105
```

### 8.2. Sin distinción de mayúsculas

Ejemplo:

```text
mr
MR
Mr
```

Todos deberán producir el mismo resultado.

### 8.3. Espacios externos

El criterio se normalizará mediante `trim()` antes de consultar.

```text
"  A1  " → "A1"
```

### 8.4. Consulta vacía

Una consulta vacía o compuesta solo por espacios deberá mostrar el listado completo.

### 8.5. Posición nula

Los registros sin posición no deberán provocar errores durante la búsqueda.

### 8.6. Orden de resultados

Los resultados conservarán el orden definido en HU-01:

1. categoría ascendente;
2. código ascendente.

### 8.7. Observabilidad

Si Room cambia mientras existe una consulta activa, los resultados deberán recalcularse automáticamente.

---

## 9. Estados de interfaz

### 9.1. Loading

La búsqueda inicial o el cambio de fuente está en proceso.

Características:

- indicador discreto;
- criterio conservado;
- interfaz no bloqueada.

### 9.2. Content

Existen coincidencias.

Características:

- RecyclerView visible;
- resultados ordenados;
- navegación al detalle disponible.

### 9.3. EmptyDatabase

No existe ningún registro en Room y tampoco hay una búsqueda activa con datos previos.

Mensaje orientativo:

```text
Todavía no hay mercancía registrada.
```

### 9.4. NoResults

Existen o pueden existir registros, pero ninguno coincide con el criterio.

Mensaje orientativo:

```text
No se encontraron resultados para "A1".
```

Características:

- el criterio permanece visible;
- se ofrece limpiar la búsqueda;
- no se confunde con base vacía.

### 9.5. Error

La consulta falla de forma inesperada.

Mensaje orientativo:

```text
No se pudo realizar la búsqueda.
```

La aplicación no deberá cerrarse ni mostrar excepciones técnicas.

---

## 10. Flujo principal

1. El usuario abre AlmacenTracker.
2. `MainActivity` muestra el listado completo.
3. El usuario introduce texto en la búsqueda.
4. La Activity notifica el criterio al ViewModel.
5. El ViewModel conserva el texto.
6. El ViewModel invoca `SearchWarehouseItemsUseCase`.
7. El servicio normaliza el criterio.
8. El repositorio solicita la consulta al DAO.
9. Room busca coincidencias en categoría, código, sitio y posición.
10. El mapper convierte las entidades a dominio.
11. El ViewModel emite el nuevo estado.
12. La Activity actualiza el RecyclerView.
13. El usuario puede seleccionar un resultado y abrir HU-03.

---

## 11. Flujos alternativos

### FA-01 — Búsqueda por categoría

1. El usuario escribe `MR`.
2. Se muestran todos los registros cuya categoría contiene `MR`.

### FA-02 — Búsqueda por código

1. El usuario escribe `105`.
2. Se muestran códigos con coincidencia parcial.

### FA-03 — Búsqueda por sitio

1. El usuario escribe `A1`.
2. Se muestran registros localizados en sitios coincidentes.

### FA-04 — Búsqueda por posición

1. El usuario escribe `nivel 2`.
2. Se muestran registros cuya posición coincide sin distinguir mayúsculas.

### FA-05 — Sin coincidencias

1. El usuario introduce un criterio válido.
2. Room devuelve una lista vacía.
3. El ViewModel emite `NoResults`.
4. Se mantiene visible el criterio.

### FA-06 — Limpiar búsqueda

1. Existe una consulta activa.
2. El usuario elimina el texto o pulsa limpiar.
3. Se vuelve al listado completo.

### FA-07 — Solo espacios

1. El usuario introduce espacios.
2. El criterio se normaliza a vacío.
3. Se muestra el listado completo.

### FA-08 — Rotación

1. Existe una búsqueda activa.
2. El usuario rota el dispositivo.
3. El ViewModel conserva el criterio.
4. Se mantienen los resultados correctos.

### FA-09 — Nuevo registro coincidente

1. Existe una búsqueda activa.
2. Room recibe un nuevo registro que coincide.
3. El resultado aparece automáticamente.

### FA-10 — Registro deja de coincidir

1. Existe una búsqueda activa.
2. Un registro cambia en Room.
3. Si deja de coincidir, desaparece de los resultados.

### FA-11 — Error inesperado

1. La consulta falla.
2. El repositorio transforma el error.
3. El ViewModel emite `Error`.
4. La aplicación conserva el criterio y no se cierra.

---

## 12. Criterios de aceptación

### CA-01 — Búsqueda disponible

**Dado** que el usuario está en el listado,  
**cuando** visualiza la pantalla principal,  
**entonces** dispone de una acción o campo de búsqueda.

### CA-02 — Coincidencia por categoría

**Dado** que existen registros con categoría `MR`,  
**cuando** el usuario busca `MR`,  
**entonces** se muestran esos registros.

### CA-03 — Coincidencia por código

**Dado** que existe el código `1050`,  
**cuando** el usuario busca `105`,  
**entonces** el registro aparece.

### CA-04 — Coincidencia por sitio

**Dado** que existe mercancía en `A1`,  
**cuando** el usuario busca `A1`,  
**entonces** se muestran los registros coincidentes.

### CA-05 — Coincidencia por posición

**Dado** que existe una posición `Nivel 2`,  
**cuando** el usuario busca `nivel 2`,  
**entonces** el registro aparece.

### CA-06 — Mayúsculas y minúsculas

**Dado** un mismo criterio con diferente capitalización,  
**cuando** se ejecuta la búsqueda,  
**entonces** se obtienen los mismos resultados.

### CA-07 — Coincidencia parcial

**Dado** un valor almacenado más largo que el criterio,  
**cuando** el criterio está contenido en el valor,  
**entonces** el registro aparece.

### CA-08 — Consulta vacía

**Dado** que hay una búsqueda activa,  
**cuando** el usuario vacía el campo,  
**entonces** vuelve a mostrarse el listado completo.

### CA-09 — Consulta con espacios

**Dado** que el criterio contiene espacios externos,  
**cuando** se ejecuta la búsqueda,  
**entonces** se utilizan los valores normalizados.

### CA-10 — Sin resultados

**Dado** que ningún registro coincide,  
**cuando** se ejecuta la búsqueda,  
**entonces** se muestra un estado específico de sin resultados.

### CA-11 — Base vacía diferenciada

**Dado** que Room no contiene registros,  
**cuando** se abre la pantalla,  
**entonces** se muestra el estado de base vacía y no el de búsqueda sin resultados.

### CA-12 — Rotación

**Dado** que existe una consulta activa,  
**cuando** el usuario rota el dispositivo,  
**entonces** el criterio y los resultados se conservan.

### CA-13 — Resultados observables

**Dado** que existe una consulta activa,  
**cuando** Room cambia,  
**entonces** los resultados se actualizan automáticamente.

### CA-14 — Navegación al detalle

**Dado** que un resultado es visible,  
**cuando** el usuario lo pulsa,  
**entonces** se abre el detalle correcto mediante su `id`.

### CA-15 — Error controlado

**Dado** que la búsqueda falla,  
**cuando** la aplicación recibe el error,  
**entonces** muestra un mensaje y no se cierra.

### CA-16 — Operación no bloqueante

**Dado** que se ejecuta una búsqueda,  
**cuando** Room procesa la consulta,  
**entonces** no se bloquea el hilo principal.

---

## 13. Diseño técnico propuesto

### 13.1. Puerto de entrada

`SearchWarehouseItemsUseCase` representará la operación de búsqueda.

Firma orientativa:

```java
public interface SearchWarehouseItemsUseCase {

    LiveData<List<WarehouseItem>> search(String query);
}
```

La firma definitiva deberá mantener el patrón ya utilizado en el proyecto.

### 13.2. Servicio de aplicación

`SearchWarehouseItemsService` deberá:

- recibir el criterio;
- eliminar espacios externos;
- decidir si corresponde buscar o recuperar todo;
- delegar al puerto de salida;
- no depender de Android UI ni Room.

### 13.3. Puerto de salida

`WarehouseItemRepository` se ampliará con una operación de búsqueda observable.

Firma orientativa:

```java
LiveData<List<WarehouseItem>> search(String query);
```

### 13.4. Adaptador Room

`RoomWarehouseItemRepository` deberá:

- invocar la consulta correcta;
- mapear entidades a dominio;
- conservar orden y observabilidad;
- no filtrar manualmente en memoria salvo justificación técnica documentada.

### 13.5. DAO

Consulta orientativa:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "WHERE categoria LIKE '%' || :query || '%' COLLATE NOCASE " +
    "OR codigo LIKE '%' || :query || '%' COLLATE NOCASE " +
    "OR sitio LIKE '%' || :query || '%' COLLATE NOCASE " +
    "OR posicion LIKE '%' || :query || '%' COLLATE NOCASE " +
    "ORDER BY categoria COLLATE NOCASE ASC, " +
    "codigo COLLATE NOCASE ASC"
)
LiveData<List<WarehouseItemEntity>> search(String query);
```

La consulta definitiva deberá verificarse con Room y SQLite.

### 13.6. ViewModel

`WarehouseItemListViewModel` deberá ampliarse para:

- conservar `searchQuery`;
- cambiar la fuente observable cuando cambia el criterio;
- evitar observadores duplicados;
- exponer `Content`, `EmptyDatabase`, `NoResults` y `Error`;
- conservar búsqueda ante rotación;
- permitir limpiar la búsqueda.

Se recomienda utilizar `MediatorLiveData` o `Transformations.switchMap`, según el patrón ya existente.

### 13.7. Estado de interfaz

`WarehouseItemListUiState` deberá diferenciar:

```text
LOADING
CONTENT
EMPTY_DATABASE
NO_RESULTS
ERROR
```

Además podrá incluir:

```text
query
items
errorMessage
```

### 13.8. Activity

`MainActivity` deberá:

- capturar cambios de texto;
- delegar al ViewModel;
- renderizar estados;
- limpiar la búsqueda;
- no ejecutar consultas ni filtros manuales.

### 13.9. Composición de dependencias

`AppContainer` deberá proporcionar `SearchWarehouseItemsService` y actualizar la Factory del ViewModel si es necesario.

---

## 14. Decisiones técnicas importantes

### 14.1. Búsqueda en Room

La búsqueda se realizará en la persistencia local, no en una lista copiada por la Activity.

### 14.2. No adelantar HU-05

La HU-04 usa un único criterio textual global. No incorpora filtros seleccionables.

### 14.3. Consulta vacía devuelve todo

No se mostrará `NoResults` cuando el criterio quede vacío.

### 14.4. Diferenciar estados vacíos

`EmptyDatabase` y `NoResults` no son equivalentes.

### 14.5. No buscar por observaciones

Las observaciones quedan fuera del alcance para mantener la búsqueda centrada en identificación y ubicación.

### 14.6. Conservar navegación por id

Los resultados seguirán abriendo HU-03 mediante el identificador interno.

### 14.7. Evitar recrear observadores

Cada cambio de texto no deberá acumular observadores activos.

### 14.8. Sin debounce obligatorio

Para el volumen local previsto, el debounce no será obligatorio. Puede añadirse si la implementación lo necesita y queda probado.

---

## 15. Estructura de archivos orientativa

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── SearchWarehouseItemsUseCase.java
│   │   └── out/
│   │       └── WarehouseItemRepository.java
│   └── service/
│       └── SearchWarehouseItemsService.java
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

No se crearán paquetes vacíos ni una segunda pantalla de resultados.

---

## 16. Diseño de interfaz esperado

Estructura orientativa:

```text
Toolbar
AlmacenTracker

[ Buscar por categoría, código o ubicación... ]

RecyclerView / estado vacío / sin resultados

FloatingActionButton
```

Requisitos visuales:

- búsqueda visible y accesible;
- acción para limpiar texto;
- teclado no bloquea el listado;
- estado sin resultados comprensible;
- criterio conservado al rotar;
- RecyclerView mantiene navegación al detalle;
- FAB sigue disponible salvo decisión visual justificada.

---

## 17. Pruebas recomendadas

### 17.1. Servicio de aplicación

- normaliza espacios externos;
- consulta todo con criterio vacío;
- delega búsqueda con criterio válido;
- no altera mayúsculas de forma incompatible;
- propaga resultados vacíos correctamente;
- transforma errores si corresponde.

### 17.2. ViewModel

- conserva criterio;
- muestra contenido con coincidencias;
- muestra `NoResults` sin coincidencias;
- muestra `EmptyDatabase` sin datos y sin búsqueda;
- vuelve al listado completo al limpiar;
- no acumula observadores;
- conserva estado tras recreación;
- mantiene navegación mediante resultados.

### 17.3. DAO

- encuentra por categoría;
- encuentra por código;
- encuentra por sitio;
- encuentra por posición;
- permite coincidencias parciales;
- ignora mayúsculas;
- maneja posición nula;
- devuelve lista vacía sin coincidencias;
- conserva orden;
- actualiza resultados cuando cambian datos.

### 17.4. Interfaz

- escribir actualiza resultados;
- limpiar restaura listado;
- sin coincidencias muestra mensaje;
- rotación conserva texto;
- pulsar resultado abre detalle correcto.

### 17.5. Manuales

- categoría completa y parcial;
- código completo y parcial;
- sitio completo y parcial;
- posición completa y parcial;
- mayúsculas y minúsculas;
- espacios externos;
- consulta vacía;
- solo espacios;
- texto inexistente;
- base vacía;
- rotación;
- varios cambios rápidos;
- creación de registro coincidente;
- uso sin conexión.

---

## 18. Tareas de implementación

1. Confirmar HU-03 integrada en `develop`.
2. Verificar CI en `develop`.
3. Crear `feature/hu-04-buscar-mercancia`.
4. Revisar `MainActivity` y `WarehouseItemListViewModel` reales.
5. Diseñar la búsqueda sin adelantar HU-05.
6. Crear `SearchWarehouseItemsUseCase`.
7. Crear `SearchWarehouseItemsService`.
8. Ampliar `WarehouseItemRepository`.
9. Añadir consulta de búsqueda al DAO.
10. Implementar búsqueda en `RoomWarehouseItemRepository`.
11. Confirmar mapper existente.
12. Ampliar `WarehouseItemListUiState`.
13. Ampliar `WarehouseItemListViewModel`.
14. Gestionar cambio de fuente observable.
15. Conservar criterio ante rotación.
16. Actualizar Factory y `AppContainer`.
17. Añadir campo o SearchView en `MainActivity`.
18. Implementar acción limpiar.
19. Implementar estado `NoResults`.
20. Diferenciar `EmptyDatabase`.
21. Mantener navegación a detalle.
22. Probar actualizaciones observables.
23. Crear pruebas unitarias.
24. Crear pruebas DAO.
25. Crear pruebas de interfaz necesarias.
26. Ejecutar `./gradlew testDebugUnitTest`.
27. Ejecutar `./gradlew lintDebug`.
28. Ejecutar `./gradlew assembleDebug`.
29. Publicar commits representativos.
30. Verificar CI en la rama.
31. Revisar criterios de aceptación.
32. Fusionar localmente en `develop`.
33. Verificar CI en `develop`.
34. Eliminar la rama tras comprobar la integración.

---

## 19. Evidencias necesarias para cerrar la HU

- captura del listado completo;
- captura de búsqueda por categoría;
- captura de búsqueda por código;
- captura de búsqueda por sitio;
- captura de búsqueda por posición;
- evidencia de coincidencia parcial;
- evidencia de búsqueda sin distinguir mayúsculas;
- captura de estado sin resultados;
- captura de base vacía diferenciada;
- evidencia de limpiar búsqueda;
- evidencia de conservación tras rotación;
- evidencia de navegación al detalle desde resultados;
- evidencia de actualización observable;
- confirmación de que MainActivity no filtra manualmente;
- resultado de pruebas unitarias;
- resultado de pruebas DAO;
- resultado de lint;
- compilación debug correcta;
- CI satisfactoria en `feature/hu-04-buscar-mercancia`;
- merge local en `develop`;
- CI satisfactoria en `develop`.

---

## 20. Definición de terminado

La HU-04 estará terminada cuando:

- exista una búsqueda accesible en el listado;
- se pueda buscar por categoría;
- se pueda buscar por código;
- se pueda buscar por sitio;
- se pueda buscar por posición;
- las coincidencias sean parciales;
- no se distingan mayúsculas y minúsculas;
- los espacios externos se ignoren;
- una consulta vacía muestre todo;
- limpiar la búsqueda restaure el listado;
- exista estado `NoResults`;
- `NoResults` se diferencie de `EmptyDatabase`;
- los resultados conserven el orden de HU-01;
- los registros sin posición no produzcan errores;
- el criterio sobreviva a rotación;
- los resultados se actualicen con Room;
- un resultado abra HU-03 mediante su id;
- la Activity no filtre manualmente;
- la UI dependa del puerto de entrada;
- el dominio no dependa de Android ni Room;
- la búsqueda no bloquee el hilo principal;
- las pruebas definidas finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 21. Resultado esperado

Al cerrar la HU-04, AlmacenTracker permitirá localizar mercancía rápidamente:

```text
usuario escribe un criterio
        ↓
la aplicación normaliza la consulta
        ↓
Room busca en categoría, código, sitio y posición
        ↓
el ViewModel actualiza el estado
        ↓
la pantalla muestra coincidencias o NoResults
```

El proyecto quedará preparado para continuar con:

```text
HU-05 — Filtrar mercancía
```

---

## 22. Commit documental recomendado

```text
docs: add HU-04 warehouse item search plan
```
