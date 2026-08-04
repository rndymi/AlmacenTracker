# HU-31 — Buscar y filtrar el historial

> Sexta historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-31  
**Nombre:** Buscar y filtrar el historial  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-31-buscar-filtrar-historial`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-29 — Consultar el historial de listas  
**Issue prevista:** `#35`

---

## 2. Historia de usuario

Como usuario,  
quiero buscar y filtrar las listas guardadas en el historial,  
para encontrar rápidamente un registro anterior por su título, sus referencias o su fecha documental.

---

## 3. Objetivo

Ampliar el listado histórico existente para admitir criterios combinables de consulta.

Flujo previsto:

```text
WithdrawalHistoryListActivity
        ↓
texto de búsqueda
+
fecha inicial opcional
+
fecha final opcional
        ↓
WithdrawalHistoryListViewModel
        ↓
WithdrawalHistorySearchCriteria
        ↓
WithdrawalHistoryRepository.searchSummaries(...)
        ↓
Room
        ↓
resúmenes coincidentes
```

La búsqueda deberá admitir:

```text
título parcial
categoría parcial
código parcial
```

Los filtros de fecha deberán aplicarse sobre:

```text
registeredAt
```

Los criterios podrán combinarse.

Ejemplo:

```text
texto = MR
fecha inicial = 1 ago 2026
fecha final = 2 ago 2026
```

Resultado:

```text
listas registradas entre ambas fechas
que tengan MR en el título, categoría o código
```

---

## 4. Regla principal

HU-31 filtrará registros históricos.

No filtrará:

```text
mercadería actual
ubicaciones actuales
stock
fotografías
texto OCR completo
```

La fuente de verdad será:

```text
withdrawal_history
+
withdrawal_history_entries
```

La consulta deberá mantener el resumen completo de cada lista.

Si una lista coincide porque una línea contiene:

```text
MR · 1210A
```

la fila del listado deberá seguir mostrando los contadores de **todas** sus líneas, no únicamente la línea coincidente.

---

## 5. Documentos y código de referencia

HU-31 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-29-consultar-historial-listas.md`;
- `HU-30-consultar-detalle-historico.md`;
- el estado real de `AlmacenTrackerHU30.zip`;
- Room como fuente local de verdad;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- el listado histórico existente;
- la navegación al detalle existente;
- el orden `registeredAt DESC, id DESC`;
- el funcionamiento completamente sin conexión;
- la separación entre historial y mercadería actual;
- la política de crear componentes únicamente cuando aporten una responsabilidad real.

El plan de v1.3 asigna a HU-31:

```text
búsqueda por título
+
búsqueda por categoría
+
búsqueda por código
+
intervalo de fechas
+
combinación de criterios
+
estado sin resultados
+
limpieza de filtros
```

---

## 6. Estado real antes de HU-31

El análisis de `AlmacenTrackerHU30.zip` confirma:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida es:

```text
develop
```

HU-30 ya está integrada mediante:

```text
merge HU30 #34 into develop
```

La feature histórica dispone de:

```text
feature/withdrawal_history/
├── common/
├── create/
├── detail/
└── list/
```

El listado ya proporciona:

```text
WithdrawalHistoryListActivity
WithdrawalHistoryListAdapter
WithdrawalHistoryListUiState
WithdrawalHistoryListViewModel
WithdrawalHistoryListViewModelFactory
```

La pantalla actual:

- se abre desde `MainActivity`;
- muestra todos los resúmenes;
- ordena por fecha documental descendente;
- conserva contenido durante una recarga;
- dispone de estados `LOADING`, `CONTENT`, `EMPTY` y `ERROR`;
- permite reintentar;
- refresca al volver a primer plano;
- abre `WithdrawalHistoryDetailActivity`;
- funciona sin conexión.

El repositorio actual ofrece:

```java
void findAllSummaries(...)
```

El DAO actual ofrece:

```java
List<WithdrawalHistorySummaryRow> findAllSummaries();
```

La consulta actual:

- agrega líneas mediante `LEFT JOIN`;
- calcula total, encontradas y no encontradas;
- no recibe criterios;
- devuelve todos los registros.

Antes de HU-31 no existen:

- modelo de criterios históricos;
- consulta filtrada;
- operación de repositorio para búsqueda;
- campo de búsqueda en la pantalla;
- selección de fecha inicial;
- selección de fecha final;
- validación del intervalo;
- estado `NO_RESULTS`;
- acción para limpiar criterios;
- conservación de criterios tras rotación.

---

## 7. Alcance incluido

HU-31 incluye:

- ampliar `WithdrawalHistoryListActivity`;
- mantener una única pantalla de historial;
- añadir campo de búsqueda;
- buscar por título;
- buscar por categoría;
- buscar por código;
- admitir coincidencia parcial;
- ignorar mayúsculas y minúsculas;
- eliminar espacios externos;
- conservar ceros iniciales;
- tratar el texto como `String`;
- añadir filtro de fecha inicial;
- añadir filtro de fecha final;
- aplicar fechas sobre `registeredAt`;
- utilizar la zona horaria del dispositivo;
- incluir todo el día inicial;
- incluir todo el día final;
- permitir solo fecha inicial;
- permitir solo fecha final;
- permitir ambas fechas;
- validar el intervalo;
- combinar texto y fechas mediante `AND`;
- buscar texto en título o líneas mediante `OR`;
- conservar los contadores completos;
- conservar el orden histórico;
- mostrar estado sin resultados;
- diferenciar historial vacío de consulta sin resultados;
- limpiar texto;
- limpiar fechas;
- limpiar todos los criterios;
- restaurar el listado completo;
- conservar criterios ante rotación;
- conservar criterios al abrir y regresar del detalle;
- conservar criterios durante errores;
- permitir reintentar la misma consulta;
- evitar consultas simultáneas;
- evitar resultados obsoletos;
- mantener navegación al detalle;
- actualizar los resultados después de futuras eliminaciones;
- no modificar Room;
- no modificar mercadería;
- no crear una segunda pantalla de listado;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas DAO;
- pruebas de criterios;
- pruebas de repositorio;
- pruebas de ViewModel;
- pruebas de Activity;
- pruebas manuales;
- CI.

---

## 8. Alcance excluido

HU-31 no incluye:

- eliminar historiales;
- confirmar eliminación;
- editar historiales;
- editar títulos históricos;
- editar fechas históricas;
- modificar cantidades;
- modificar unidades;
- modificar ubicaciones históricas;
- buscar por sitio;
- buscar por posición;
- buscar por cantidad;
- buscar por unidad;
- buscar por estado `FOUND` o `NOT_FOUND`;
- ordenar por otros criterios;
- guardar búsquedas frecuentes;
- sugerencias automáticas;
- autocompletado;
- historial de búsquedas;
- paginación;
- estadísticas;
- exportación;
- backup;
- sincronización remota;
- backend;
- gestión de stock.

La eliminación corresponde a HU-32.

---

## 9. Decisión de interfaz

HU-31 ampliará:

```text
WithdrawalHistoryListActivity
```

No se creará:

```text
WithdrawalHistorySearchActivity
```

Motivos:

- los resultados son el mismo tipo de listado;
- la navegación al detalle ya existe;
- separar pantallas duplicaría adapter, estados y renderizado;
- los filtros deben poder limpiarse sin abandonar el historial;
- el plan de v1.3 define búsqueda como capacidad del listado.

---

## 10. Modelo de criterios

Se añadirá un modelo Java puro:

```text
WithdrawalHistorySearchCriteria
```

Datos previstos:

```text
query
registeredFromInclusive
registeredToExclusive
```

Estructura conceptual:

```java
public final class WithdrawalHistorySearchCriteria {

    private final String query;
    private final Long registeredFromInclusive;
    private final Long registeredToExclusive;
}
```

El modelo deberá:

- no depender de Android;
- no depender de Room;
- ser inmutable;
- normalizar el texto;
- permitir criterios vacíos;
- validar el intervalo;
- implementar `equals()` y `hashCode()`;
- permitir detectar si existe algún criterio activo;
- preservar timestamps como `long`;
- no almacenar fechas formateadas.

---

## 11. Normalización del texto

Reglas:

```text
null
    → ""

trim()
    → eliminar espacios externos

texto vacío
    → sin criterio textual
```

La búsqueda no convertirá códigos a números.

Ejemplo:

```text
" 001210 "
```

deberá buscar:

```text
001210
```

y no:

```text
1210
```

La comparación será insensible a mayúsculas y minúsculas mediante SQLite.

No será necesario convertir el texto a mayúsculas antes de consultar si la consulta usa:

```text
COLLATE NOCASE
```

---

## 12. Semántica del campo de búsqueda

Un único campo buscará en:

```text
withdrawal_history.title
withdrawal_history_entries.category
withdrawal_history_entries.code
```

La lógica será:

```text
título contiene query
OR categoría contiene query
OR código contiene query
```

Ejemplos:

```text
query = centro
→ título "Reposición tienda centro"
```

```text
query = MR
→ categoría MR
```

```text
query = 1210
→ código 1210A
```

La coincidencia será parcial.

No se exigirá que el usuario seleccione previamente si busca por título, categoría o código.

---

## 13. Tratamiento literal de `%` y `_`

SQLite utiliza:

```text
%
_
```

como comodines de `LIKE`.

HU-31 deberá evitar que esos caracteres cambien accidentalmente el significado de la búsqueda.

Se recomienda construir un patrón escapado:

```text
\%  para porcentaje literal
\_  para guion bajo literal
\\  para barra invertida literal
```

Consulta conceptual:

```sql
LIKE :queryPattern ESCAPE '\'
```

El repositorio o un componente pequeño de criterios podrá preparar:

```text
%textoEscapado%
```

La Activity no deberá construir SQL ni patrones de base de datos.

---

## 14. Fechas internas

La UI permitirá seleccionar fechas de calendario.

La consulta utilizará timestamps:

```text
registeredFromInclusive
registeredToExclusive
```

Ejemplo para:

```text
1 ago 2026
a
2 ago 2026
```

se convertirá en:

```text
desde:
1 ago 2026 · 00:00:00.000 inclusive

hasta:
3 ago 2026 · 00:00:00.000 exclusive
```

Condición SQL:

```text
registered_at >= :registeredFromInclusive
AND registered_at < :registeredToExclusive
```

Usar un límite superior exclusivo evita depender de:

```text
23:59:59.999
```

y reduce errores de precisión.

---

## 15. Zona horaria

La conversión de fechas utilizará:

```text
ZoneId.systemDefault()
```

La fecha elegida representa un día local del usuario.

Flujo:

```text
LocalDate
    ↓ atStartOfDay(zoneId)
Instant
    ↓ epochMilli
```

La fecha final se convertirá mediante:

```text
endDate.plusDays(1).atStartOfDay(zoneId)
```

La consulta histórica seguirá almacenando y comparando timestamps absolutos.

---

## 16. Fecha inicial opcional

Cuando solo exista fecha inicial:

```text
registered_at >= :from
```

No se aplicará límite superior.

Ejemplo:

```text
Desde 1 ago 2026
```

mostrará registros desde el inicio de ese día hasta el más reciente.

---

## 17. Fecha final opcional

Cuando solo exista fecha final:

```text
registered_at < :toExclusive
```

No se aplicará límite inferior.

Ejemplo:

```text
Hasta 2 ago 2026
```

mostrará registros anteriores al inicio del 3 de agosto.

---

## 18. Intervalo completo

Cuando existan ambas fechas:

```text
fromInclusive < toExclusive
```

La fecha inicial podrá ser igual a la fecha final visible.

Ejemplo válido:

```text
Desde 2 ago 2026
Hasta 2 ago 2026
```

La consulta abarcará todo el 2 de agosto.

No se compararán directamente las fechas visuales mediante strings.

---

## 19. Intervalo inválido

Entrada inválida:

```text
fecha inicial posterior a fecha final
```

Comportamiento:

- no consultar Room;
- mostrar error asociado al filtro;
- conservar los resultados anteriores;
- permitir corregir una fecha;
- no limpiar silenciosamente;
- no intercambiar fechas automáticamente.

Mensaje orientativo:

```text
La fecha inicial no puede ser posterior a la fecha final.
```

---

## 20. Consulta SQL

La consulta deberá conservar la agregación completa de cada historial.

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
WHERE
    (
        :hasQuery = 0
        OR history.title LIKE :queryPattern
            ESCAPE '\' COLLATE NOCASE
        OR EXISTS (
            SELECT 1
            FROM withdrawal_history_entries
                AS matching_entry
            WHERE
                matching_entry.history_id = history.id
                AND (
                    matching_entry.category
                        LIKE :queryPattern
                        ESCAPE '\' COLLATE NOCASE
                    OR matching_entry.code
                        LIKE :queryPattern
                        ESCAPE '\' COLLATE NOCASE
                )
        )
    )
    AND (
        :registeredFromInclusive IS NULL
        OR history.registered_at
            >= :registeredFromInclusive
    )
    AND (
        :registeredToExclusive IS NULL
        OR history.registered_at
            < :registeredToExclusive
    )
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

La sintaxis final deberá adaptarse a las restricciones de Room y SQLite del proyecto.

---

## 21. Decisión crítica sobre `EXISTS`

La coincidencia de categoría y código se resolverá mediante:

```text
EXISTS
```

No se filtrará directamente el alias agregado:

```text
entry.category
entry.code
```

dentro del `WHERE` principal.

Una solución incorrecta como:

```sql
LEFT JOIN entries
WHERE entry.code LIKE ...
GROUP BY history.id
```

haría que:

```text
COUNT(entry.id)
found_count
not_found_count
```

solo contabilizaran líneas coincidentes.

Ejemplo:

```text
lista con 15 referencias
1 línea coincide con MR
```

Resultado correcto:

```text
15 referencias
```

Resultado incorrecto si se filtra el JOIN:

```text
1 referencia
```

`EXISTS` selecciona la cabecera sin reducir las líneas utilizadas para los contadores.

---

## 22. Reutilización de la consulta sin criterios

Se recomienda que:

```text
findAllSummaries()
```

continúe disponible para pruebas existentes y compatibilidad interna.

HU-31 añadirá:

```java
List<WithdrawalHistorySummaryRow>
searchSummaries(
        int hasQuery,
        String queryPattern,
        Long registeredFromInclusive,
        Long registeredToExclusive
);
```

Alternativamente, `findAllSummaries()` podrá delegar conceptualmente en criterios vacíos si Room permite una solución clara.

No se deberá duplicar una consulta extensa sin necesidad.

Sin embargo, evitar duplicación no justifica introducir SQL dinámico inseguro.

---

## 23. Repositorio

Se ampliará:

```text
WithdrawalHistoryRepository
```

con:

```java
void searchSummaries(
        WithdrawalHistorySearchCriteria criteria,
        RepositoryCallback<
                List<WithdrawalHistorySummary>
        > callback
);
```

Reglas:

- criterios no nulos;
- copia defensiva cuando corresponda;
- consulta fuera del hilo principal;
- patrón escapado fuera de la Activity;
- lista vacía mediante `onSuccess`;
- error técnico mediante `onError`;
- un único callback;
- orden conservado;
- entidades Room no expuestas.

No se utilizará:

```text
onNotFound()
```

para resultados vacíos.

---

## 24. Relación con `findAllSummaries()`

El ViewModel podrá utilizar siempre:

```text
searchSummaries(criteria)
```

incluidos criterios vacíos.

El repositorio deberá interpretar criterios vacíos como:

```text
todos los historiales
```

Esto evita que el ViewModel elija entre dos operaciones.

`findAllSummaries()` podrá conservarse porque:

- ya forma parte del contrato;
- existen pruebas;
- puede ser útil para operaciones sin filtros;
- eliminarlo no aporta valor a HU-31.

No es necesario retirar código funcional existente.

---

## 25. Servicio de búsqueda

No se recomienda crear:

```text
WithdrawalHistorySearchService
```

si únicamente:

- recibe criterios;
- llama al repositorio;
- devuelve la misma colección.

Las reglas relevantes pueden vivir en:

```text
WithdrawalHistorySearchCriteria
+
WithdrawalHistoryListViewModel
+
RoomWithdrawalHistoryRepository
```

Un servicio solo se justificará si durante la implementación concentra reglas adicionales reales.

La arquitectura deberá evitar una capa ceremonial.

---

## 26. Estado del ViewModel

`WithdrawalHistoryListViewModel` deberá conservar:

```text
query
selectedFromDate
selectedToDate
criteria
currentSummaries
hasLoaded
loading
requestGeneration
```

Los criterios deberán sobrevivir a:

- rotación;
- apertura del detalle;
- regreso del detalle;
- error;
- reintento;
- refresco.

No se guardarán en Room ni en preferencias.

---

## 27. Métodos del ViewModel

Métodos orientativos:

```java
public void updateQuery(String query);

public void updateFromDate(
        @Nullable LocalDate date
);

public void updateToDate(
        @Nullable LocalDate date
);

public void clearQuery();

public void clearFromDate();

public void clearToDate();

public void clearCriteria();

public void retry();

public void refresh();
```

La API definitiva podrá agrupar acciones si mantiene claridad.

La Activity no deberá construir directamente:

```text
WithdrawalHistorySearchCriteria
```

a partir de Views en cada renderizado.

---

## 28. Aplicación de búsqueda textual

Se recomienda no consultar Room en cada carácter sin control.

Opciones válidas:

### Opción A — Acción explícita

```text
usuario escribe
        ↓
pulsa Buscar o acción IME
        ↓
consulta
```

### Opción B — Debounce

```text
usuario escribe
        ↓
espera breve
        ↓
consulta
```

Para mantener el alcance y evitar temporizadores innecesarios, se recomienda:

```text
acción IME Buscar
+
botón o icono de búsqueda
```

La limpieza del texto deberá volver a ejecutar la consulta con los filtros de fecha restantes.

No se añadirá una dependencia reactiva únicamente para debounce.

---

## 29. Prevención de resultados obsoletos

Puede ocurrir:

```text
consulta A inicia
consulta B inicia después
consulta B termina
consulta A termina tarde
```

El ViewModel no deberá publicar el resultado antiguo de A.

Se recomienda usar:

```text
long requestGeneration
```

Flujo:

```text
cada consulta incrementa generación
callback conserva generación propia
callback solo publica si sigue vigente
```

También podrá impedirse una nueva consulta mientras exista otra y ejecutar después la pendiente.

La estrategia deberá garantizar que la UI represente los criterios actuales.

---

## 30. Cargas simultáneas

El comportamiento actual ignora una recarga cuando:

```text
loading == true
```

HU-31 deberá revisar esa regla porque el usuario puede cambiar criterios durante una consulta.

Una solicitud con criterios diferentes no deberá perderse.

Opciones válidas:

- cancelar lógicamente la anterior mediante generación;
- marcar una consulta pendiente;
- permitir ambas e ignorar callbacks obsoletos.

Se recomienda:

```text
generación de solicitud
```

porque el repositorio utiliza un executor y no expone cancelación real.

---

## 31. Evolución del estado de interfaz

`WithdrawalHistoryListUiState.Status` deberá incluir:

```text
LOADING
CONTENT
EMPTY
NO_RESULTS
ERROR
```

### `EMPTY`

Significa:

```text
Room no contiene historiales
+
no hay criterios activos
```

### `NO_RESULTS`

Significa:

```text
existen criterios activos
+
la consulta no encontró coincidencias
```

### `CONTENT`

Significa:

```text
hay resultados
```

### `ERROR`

Significa:

```text
la consulta actual falló
```

El estado deberá conservar también:

```text
criteria
fromDate
toDate
validationError
```

o datos equivalentes necesarios para renderizar.

---

## 32. Diferencia entre `EMPTY` y `NO_RESULTS`

### Historial vacío

Mensaje:

```text
Todavía no hay listas guardadas.
```

Descripción:

```text
Procesa una lista y guárdala para verla aquí.
```

### Sin resultados

Mensaje:

```text
No se encontraron listas.
```

Descripción:

```text
Prueba con otro texto o cambia las fechas.
```

Acción:

```text
Limpiar filtros
```

No se reutilizará el estado vacío para una búsqueda sin coincidencias.

---

## 33. Error con criterios activos

Si falla una consulta:

- se conservan texto y fechas;
- Reintentar repite los mismos criterios;
- no se vuelve automáticamente al listado completo;
- no se limpian filtros;
- no se muestra una excepción;
- el contenido anterior podrá conservarse.

Mensaje orientativo:

```text
No se pudo buscar en el historial.
```

Si existía contenido anterior:

- se mantiene visible;
- se muestra un Snackbar;
- Reintentar utiliza los criterios vigentes.

---

## 34. Refresco al volver al listado

HU-30 no modifica datos.

HU-32 sí podrá eliminarlos posteriormente.

HU-31 deberá conservar:

```text
refresh()
```

y volver a ejecutar:

```text
los criterios actuales
```

No deberá refrescar siempre con criterios vacíos.

Ejemplo:

```text
query = MR
        ↓ abrir detalle
        ↓ volver
        ↓
seguir mostrando resultados para MR
```

---

## 35. Componentes visuales

La pantalla deberá añadir:

- campo de búsqueda;
- acción Buscar;
- acción Limpiar texto;
- control Desde;
- control Hasta;
- indicador de criterios activos;
- acción Limpiar filtros;
- mensaje de validación;
- estado sin resultados.

No se deberá saturar la Toolbar con todos los controles.

Estructura recomendada:

```text
Toolbar
SearchTextInput
DateFilterRow
ActiveCriteriaSummary opcional
RecyclerView / estados
```

---

## 36. Campo de búsqueda

Componente recomendado:

```text
TextInputLayout
+
TextInputEditText
```

Configuración:

```text
hint = Buscar por título, categoría o código
imeOptions = actionSearch
inputType = text
endIconMode = clear_text
```

Reglas:

- conservar texto tras rotación;
- no perder foco innecesariamente;
- cerrar teclado al ejecutar búsqueda;
- acción Buscar accesible;
- no buscar con espacios externos;
- aceptar números y letras;
- no convertir automáticamente a número.

---

## 37. Selectores de fecha

Se recomienda utilizar:

```text
MaterialDatePicker
```

o un selector de fecha compatible ya disponible en Material Components.

Controles:

```text
Desde
Hasta
```

Cada control mostrará:

- texto vacío cuando no existe filtro;
- fecha local legible cuando existe;
- acción para seleccionar;
- acción para limpiar.

No se utilizará entrada libre de timestamps.

---

## 38. Estado visual de fechas

Ejemplo sin filtros:

```text
Desde: Cualquier fecha
Hasta: Cualquier fecha
```

Ejemplo con intervalo:

```text
Desde: 1 ago 2026
Hasta: 2 ago 2026
```

El formato deberá usar:

```text
locale del dispositivo
```

No se almacenará el texto formateado en el ViewModel como fuente de verdad.

---

## 39. Acción Limpiar filtros

La acción deberá limpiar:

```text
query
fecha inicial
fecha final
error de intervalo
```

Después deberá consultar:

```text
todos los historiales
```

No deberá:

- cerrar la pantalla;
- borrar registros;
- modificar Room;
- cambiar el orden;
- perder la navegación al detalle.

---

## 40. Criterios activos

El estado deberá poder responder:

```java
boolean hasActiveCriteria();
```

Se considerará activo cuando exista:

```text
query no vacío
OR fecha inicial
OR fecha final
```

La acción Limpiar filtros podrá ocultarse o deshabilitarse cuando no existan criterios.

---

## 41. Contadores del listado

Los contadores conservarán el significado de HU-29:

```text
entryCount
foundCount
notFoundCount
```

No representarán:

```text
número de líneas que coincidieron con la búsqueda
```

Ejemplo:

```text
query = 1210
lista contiene 15 referencias
una de ellas es MR 1210A
```

La fila deberá mostrar:

```text
15 referencias
```

No:

```text
1 referencia
```

---

## 42. Apertura del detalle

Los resultados seguirán utilizando:

```text
WithdrawalHistoryListAdapter
```

La pulsación continuará abriendo:

```text
WithdrawalHistoryDetailActivity
```

mediante:

```text
WithdrawalHistoryDetailIntentContract
```

Los criterios permanecerán en el ViewModel del listado.

Al volver:

- el campo de búsqueda permanece;
- las fechas permanecen;
- la colección filtrada permanece;
- la posición de scroll deberá conservarse cuando sea posible.

---

## 43. No modificar el adapter innecesariamente

`WithdrawalHistoryListAdapter` ya renderiza:

- título;
- fecha;
- contadores;
- navegación;
- accesibilidad.

HU-31 no necesita un segundo adapter.

Solo deberá modificarse si:

- la descripción accesible debe indicar criterios;
- se requiere resaltar coincidencias.

No se recomienda resaltar texto en HU-31 porque:

- una lista puede coincidir por una línea no visible;
- el título puede no contener el término;
- el resumen no muestra categoría ni código;
- el resaltado podría resultar engañoso.

---

## 44. Persistencia de criterios

Los criterios existirán únicamente durante la vida del ViewModel.

No se guardarán en:

- Room;
- `SharedPreferences`;
- archivo;
- backup;
- historial de búsqueda.

La rotación quedará cubierta por ViewModel.

La recreación completa del proceso podrá reiniciar los filtros.

No es necesario `SavedStateHandle` salvo que durante la implementación aparezca una necesidad real de restauración tras muerte del proceso.

---

## 45. Accesibilidad

HU-31 deberá verificar:

- campo con etiqueta descriptiva;
- acción Buscar accesible;
- acción Limpiar texto accesible;
- controles Desde y Hasta accesibles;
- fecha seleccionada anunciada;
- error de intervalo asociado;
- estado sin resultados anunciado;
- acción Limpiar filtros accesible;
- progreso anunciado;
- resultados leídos como filas completas;
- información no dependiente del color;
- objetivos táctiles de 48 dp;
- navegación por teclado;
- orden de foco lógico;
- contraste claro y oscuro;
- mantenimiento de tamaño de texto.

---

## 46. Privacidad

HU-31 deberá:

- consultar únicamente Room local;
- no solicitar Internet;
- no enviar términos de búsqueda;
- no registrar títulos o referencias completas en logs de producción;
- no guardar historial de consultas;
- no modificar registros;
- no acceder a fotografías ni OCR.

---

## 47. Pruebas del modelo de criterios

- criterios vacíos;
- query nula;
- query con espacios;
- query con ceros iniciales;
- fecha inicial sola;
- fecha final sola;
- ambas fechas;
- mismo día;
- intervalo inválido;
- `hasActiveCriteria`;
- igualdad;
- hash;
- patrón escapado;
- `%`;
- `_`;
- barra invertida.

---

## 48. Pruebas DAO — Texto

- título exacto;
- título parcial;
- título sin distinguir mayúsculas;
- categoría exacta;
- categoría parcial;
- categoría sin distinguir mayúsculas;
- código exacto;
- código parcial;
- código con ceros iniciales;
- código con letra final;
- consulta sin coincidencias;
- porcentaje literal;
- guion bajo literal;
- lista que coincide por varias líneas aparece una sola vez.

---

## 49. Pruebas DAO — Fechas

- solo inicio;
- solo final;
- intervalo completo;
- mismo día;
- instante exacto del inicio incluido;
- instante anterior al inicio excluido;
- instante justo antes del final exclusivo incluido;
- instante igual al final exclusivo excluido;
- varios días;
- orden descendente conservado.

---

## 50. Pruebas DAO — Combinación

- texto + fecha inicial;
- texto + fecha final;
- texto + intervalo;
- título + intervalo;
- categoría + intervalo;
- código + intervalo;
- consulta vacía con fechas;
- criterios vacíos;
- contadores completos cuando solo una línea coincide;
- encontradas y no encontradas completas;
- ausencia de filas duplicadas.

---

## 51. Pruebas del repositorio

- criterios vacíos;
- criterios activos;
- patrón escapado;
- lista vacía como éxito;
- lista con resultados;
- orden conservado;
- mapper reutilizado;
- executor utilizado;
- callback único;
- error convertido en `onError`;
- callback nulo rechazado;
- criterios nulos rechazados.

---

## 52. Pruebas del ViewModel

- carga inicial sin criterios;
- actualizar query sin ejecutar prematuramente cuando se usa acción explícita;
- ejecutar búsqueda;
- establecer fecha inicial;
- establecer fecha final;
- intervalo válido;
- intervalo inválido;
- no consultar con intervalo inválido;
- limpiar query;
- limpiar fecha inicial;
- limpiar fecha final;
- limpiar todos;
- `EMPTY`;
- `NO_RESULTS`;
- `CONTENT`;
- `ERROR`;
- reintentar con mismos criterios;
- refrescar con mismos criterios;
- criterios conservados tras rotación;
- resultado obsoleto ignorado;
- consulta nueva durante otra activa;
- contenido previo conservado;
- navegación al detalle no altera criterios.

---

## 53. Pruebas de Activity

- escribir búsqueda;
- pulsar acción IME;
- pulsar Buscar;
- limpiar texto;
- seleccionar Desde;
- seleccionar Hasta;
- limpiar fechas;
- mostrar error de intervalo;
- corregir intervalo;
- mostrar sin resultados;
- limpiar filtros desde sin resultados;
- abrir detalle;
- volver conservando criterios;
- rotar;
- error y Reintentar;
- teclado;
- accesibilidad.

---

## 54. Pruebas manuales

### Por título

```text
Reposición tienda centro
```

Buscar:

```text
centro
```

### Por categoría

Lista con:

```text
MR · 1210A
```

Buscar:

```text
mr
```

### Por código

Buscar:

```text
1210
```

### Ceros iniciales

Lista con:

```text
MR · 001210
```

Buscar:

```text
001210
```

### Fecha inicial

Mostrar registros desde una fecha.

### Fecha final

Mostrar registros hasta una fecha.

### Mismo día

Seleccionar el mismo día como inicio y fin.

### Combinación

Buscar:

```text
MR
```

dentro de un intervalo.

### Sin resultados

Buscar un texto inexistente.

### Limpiar

Restaurar todos los registros.

### Offline

Activar modo avión y repetir todas las operaciones.

---

## 55. Criterios de aceptación

### CA-01 — Búsqueda por título

**Dado** un historial con título,  
**cuando** el usuario introduce una parte del título,  
**entonces** la lista aparece en los resultados.

### CA-02 — Búsqueda por categoría

**Dado** un historial con una línea de categoría `MR`,  
**cuando** el usuario busca `mr`,  
**entonces** la lista aparece sin distinguir mayúsculas.

### CA-03 — Búsqueda por código

**Dado** un historial con código `001210A`,  
**cuando** el usuario busca `001210`,  
**entonces** la lista aparece y conserva los ceros iniciales.

### CA-04 — Coincidencia parcial

**Dado** un título o referencia,  
**cuando** el texto forma parte de su valor,  
**entonces** se considera coincidencia.

### CA-05 — Fecha inicial

**Dada** una fecha inicial,  
**cuando** se aplica,  
**entonces** se incluyen registros desde el comienzo de ese día.

### CA-06 — Fecha final

**Dada** una fecha final,  
**cuando** se aplica,  
**entonces** se incluyen todos los registros de ese día.

### CA-07 — Mismo día

**Dado** el mismo día como inicio y fin,  
**cuando** se filtra,  
**entonces** se consultan las 24 horas locales de ese día.

### CA-08 — Combinación

**Dado** texto y fechas,  
**cuando** se aplican,  
**entonces** solo aparecen historiales que cumplen ambos tipos de criterio.

### CA-09 — Contadores completos

**Dada** una lista con varias líneas y una sola coincidencia,  
**cuando** se muestra,  
**entonces** sus contadores representan todas las líneas.

### CA-10 — Orden

**Dados** varios resultados,  
**cuando** se muestran,  
**entonces** conservan `registeredAt DESC, id DESC`.

### CA-11 — Sin resultados

**Dados** criterios sin coincidencias,  
**cuando** finaliza la consulta,  
**entonces** se muestra `NO_RESULTS` y no el estado de historial vacío.

### CA-12 — Limpiar filtros

**Dados** criterios activos,  
**cuando** el usuario los limpia,  
**entonces** vuelve a mostrarse el historial completo.

### CA-13 — Intervalo inválido

**Dada** una fecha inicial posterior a la final,  
**cuando** se intenta aplicar,  
**entonces** no se consulta Room y se muestra un error.

### CA-14 — Rotación

**Dados** criterios activos,  
**cuando** se rota el dispositivo,  
**entonces** texto, fechas y resultados permanecen.

### CA-15 — Detalle

**Dado** un resultado filtrado,  
**cuando** se abre y se cierra su detalle,  
**entonces** los criterios permanecen activos.

### CA-16 — Error recuperable

**Dado** un error de Room,  
**cuando** el usuario reintenta,  
**entonces** se repite la misma consulta.

### CA-17 — Solo lectura

**Dada** cualquier búsqueda,  
**cuando** finaliza,  
**entonces** Room y la mercadería no se modifican.

### CA-18 — Offline

**Dado** un dispositivo sin Internet,  
**cuando** se busca o filtra,  
**entonces** la operación funciona mediante Room local.

---

## 56. Riesgos

### Contadores reducidos por el filtro

**Mitigación:** seleccionar historiales mediante `EXISTS` y mantener el `LEFT JOIN` completo para agregación.

### Resultados duplicados

**Mitigación:** agrupar por cabecera y no devolver una fila por línea coincidente.

### Día final incompleto

**Mitigación:** límite superior exclusivo al inicio del día siguiente.

### Zona horaria incorrecta

**Mitigación:** convertir `LocalDate` mediante `ZoneId.systemDefault()`.

### Comodines involuntarios

**Mitigación:** escapar `%`, `_` y `\`.

### Resultado antiguo sobrescribe uno nuevo

**Mitigación:** generación de solicitudes en el ViewModel.

### Capa de servicio vacía

**Mitigación:** no crear un servicio si el modelo de criterios, ViewModel y repositorio ya contienen las responsabilidades reales.

### Duplicar la pantalla

**Mitigación:** ampliar `WithdrawalHistoryListActivity`.

---

## 57. Definición de terminado

HU-31 estará terminada cuando:

- exista `WithdrawalHistorySearchCriteria`;
- el DAO admita búsqueda combinada;
- el repositorio exponga `searchSummaries(...)`;
- la búsqueda admita título, categoría y código;
- las coincidencias sean parciales e insensibles a mayúsculas;
- `%`, `_` y `\` se traten literalmente;
- exista filtro Desde;
- exista filtro Hasta;
- el día final se incluya completamente;
- el intervalo se valide;
- texto y fechas puedan combinarse;
- los contadores permanezcan completos;
- el orden histórico se conserve;
- exista estado `NO_RESULTS`;
- pueda limpiarse cada criterio;
- pueda limpiarse todo;
- el ViewModel conserve criterios;
- Reintentar repita la consulta vigente;
- resultados obsoletos no sustituyan a los actuales;
- abrir el detalle no pierda filtros;
- no se modifique Room;
- no se modifique mercadería;
- funcione sin conexión;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 58. Resultado esperado

Al cerrar HU-31:

```text
Historial
        ↓
texto y/o fechas
        ↓
consulta Room
        ↓
listas coincidentes
        ↓
detalle histórico
```

El usuario podrá encontrar listas anteriores mediante:

```text
título
categoría
código
fecha inicial
fecha final
combinación de criterios
```

sin perder los contadores completos ni alterar el historial.

La eliminación de registros históricos corresponderá a HU-32.
