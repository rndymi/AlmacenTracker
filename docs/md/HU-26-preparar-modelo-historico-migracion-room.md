# HU26 — Preparar el modelo histórico y la migración Room

> Primera historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-26  
**Nombre:** Preparar el modelo histórico y la migración Room  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-26-modelo-historico-room`  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.2.0`  
**Issue prevista:** `#30`

---

## 2. Historia de usuario

Como usuario,  
quiero que el historial documental pueda almacenarse localmente sin afectar la mercadería existente,  
para actualizar la aplicación de forma segura y preparar el registro de listas de la versión 1.3.

---

## 3. Objetivo

Crear la base de dominio y persistencia necesaria para el historial documental de mercadería sacada.

La HU-26 deberá establecer:

```text
modelos de dominio histórico
        ↓
entidades Room
        ↓
relación cabecera–líneas
        ↓
DAO transaccional
        ↓
repositorio histórico
        ↓
migración Room 1 → 2
        ↓
pruebas de persistencia y migración
```

Esta historia no implementará todavía:

- captura de título;
- cantidades y unidades desde la interfaz;
- guardado iniciado por el usuario;
- listado de historiales;
- detalle histórico;
- búsqueda o filtros;
- eliminación desde la interfaz.

Su finalidad será dejar preparada y probada la infraestructura local que utilizarán las historias posteriores.

---

## 4. Documentos y código de referencia

La HU-26 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- el estado real de `AlmacenTracker-ver1.2.0.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- Room como fuente local de verdad;
- el funcionamiento completamente sin conexión;
- la separación entre mercadería e historial documental;
- la regla de no gestionar ni descontar stock;
- la política de crear únicamente componentes con una responsabilidad real;
- la exportación de esquemas Room ya configurada;
- el flujo de ramas desde `develop`.

El plan de v1.3 asigna a HU-26:

```text
cabecera histórica
+
líneas históricas
+
relación uno a muchos
+
DAO
+
transacción
+
repositorio
+
migración
+
pruebas
```

---

## 5. Estado real antes de HU-26

El análisis de `AlmacenTracker-ver1.2.0.zip` confirma que el proyecto se encuentra en:

```groovy
versionCode 3
versionName "1.2.0"
```

La rama incluida es:

```text
develop
```

La base de datos actual está declarada como:

```java
@Database(
        entities = {
                WarehouseItemEntity.class
        },
        version = 1,
        exportSchema = true
)
```

El proyecto dispone de:

```text
app/schemas/
└── com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase/
    └── 1.json
```

La base actual contiene únicamente:

```text
warehouse_items
```

La aplicación ya utiliza:

- `AlmacenTrackerDatabase`;
- `WarehouseItemDao`;
- `WarehouseItemEntity`;
- `WarehouseItemRoomMapper`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- un `ExecutorService` de base de datos creado en `AppContainer`;
- composición explícita de dependencias;
- pruebas instrumentadas de DAO;
- Room `2.7.2`;
- `exportSchema = true`;
- ausencia de `fallbackToDestructiveMigration()`.

La construcción actual de la base se realiza mediante:

```java
Room.databaseBuilder(
        applicationContext,
        AlmacenTrackerDatabase.class,
        "almacen_tracker.db"
).build();
```

Antes de HU-26 no existen:

- modelos de dominio histórico;
- tablas de historial;
- DAO histórico;
- relación cabecera–líneas;
- repositorio histórico;
- mapper histórico;
- migraciones registradas en `AppContainer`;
- esquema Room de versión `2`;
- pruebas de migración;
- pruebas transaccionales del historial.

---

## 6. Regla principal

El historial será un agregado independiente de la mercadería.

```text
WarehouseItem
        ≠
WithdrawalHistory
```

La mercadería representa la ubicación actual.

El historial representará una instantánea documental de una lista en un momento concreto.

HU-26 no deberá añadir campos históricos a `WarehouseItemEntity`.

No se permitirá una solución como:

```text
warehouse_items
├── lastQuantity
├── lastWithdrawalDate
└── lastListTitle
```

porque:

- una mercadería puede aparecer en muchas listas;
- una lista puede contener muchas referencias;
- la ubicación histórica no debe sustituir la ubicación actual;
- las cantidades documentales no pertenecen al inventario principal.

---

## 7. Alcance incluido

HU-26 incluye:

- definir el agregado histórico en dominio;
- crear un modelo para la cabecera histórica;
- crear un modelo para cada línea histórica;
- definir el estado de localización histórico;
- definir invariantes mínimas de los modelos;
- crear entidades Room independientes;
- crear una clave foránea entre líneas y cabecera;
- configurar eliminación en cascada de líneas al eliminar una cabecera;
- crear índices necesarios para la relación;
- conservar el orden mediante `orderIndex`;
- conservar categoría y código como texto;
- conservar ceros iniciales;
- permitir cantidad y unidad nulas;
- permitir título nulo;
- permitir sitio y posición históricos nulos cuando la referencia no fue encontrada;
- conservar el id de la mercadería como instantánea opcional;
- no crear una clave foránea obligatoria hacia `warehouse_items`;
- crear DAO histórico;
- insertar una cabecera;
- insertar varias líneas;
- guardar cabecera y líneas en una única transacción;
- obtener una cabecera con sus líneas para pruebas y uso posterior;
- definir un repositorio histórico con operaciones mínimas para esta historia;
- implementar el repositorio Room;
- crear mappers Room ↔ dominio;
- reutilizar el executor de base de datos;
- actualizar `AlmacenTrackerDatabase` a versión `2`;
- crear la migración `MIGRATION_1_2`;
- registrar la migración en `AppContainer`;
- exportar el esquema `2.json`;
- comprobar que los datos de `warehouse_items` se conservan;
- comprobar que las nuevas tablas comienzan vacías;
- comprobar el guardado atómico;
- comprobar la relación uno a muchos;
- comprobar la eliminación en cascada a nivel DAO;
- añadir pruebas unitarias de modelos y mappers cuando aporten valor;
- añadir pruebas instrumentadas de DAO;
- añadir pruebas instrumentadas de migración;
- mantener CI satisfactoria.

---

## 8. Alcance excluido

HU-26 no incluye:

- cambiar `versionCode` o publicar la release;
- crear pantallas de historial;
- crear Activities, layouts o adapters de historial;
- añadir elementos al menú;
- capturar título desde la interfaz;
- capturar o editar cantidades desde la interfaz;
- extraer cantidades mediante OCR;
- guardar una lista desde `ReferenceListLocationActivity`;
- conectar HU-25 con el repositorio histórico;
- mostrar un listado histórico;
- mostrar detalle histórico;
- buscar historiales;
- filtrar por fecha;
- eliminar historiales desde UI;
- exportar historial;
- importar historial;
- modificar el formato CSV de copia de seguridad;
- almacenar fotografías;
- almacenar texto OCR completo;
- actualizar mercadería;
- descontar stock;
- calcular existencias;
- sincronización remota;
- autenticación;
- backend;
- ONNX Runtime;
- PP-OCRv5.

La captura de datos documentales pertenece a HU-27.

El guardado funcional de una lista confirmada pertenece a HU-28.

El listado y el detalle pertenecen a HU-29 y HU-30.

---

## 9. Decisión de modelado

### 9.1. Agregado

El agregado estará formado por:

```text
WithdrawalHistory
        1
        ↓
        N
WithdrawalHistoryEntry
```

La cabecera representará la lista.

Cada línea representará una referencia confirmada de esa lista.

### 9.2. Motivo de separar cabecera y líneas

No se guardará toda la lista como:

- JSON dentro de una columna;
- texto concatenado;
- CSV embebido;
- objeto serializado.

La relación normalizada permitirá posteriormente:

- consultar una lista completa;
- buscar por categoría o código;
- filtrar por fechas;
- contar referencias;
- eliminar una lista con sus líneas;
- conservar el orden;
- evolucionar el modelo sin parsear cadenas opacas.

---

## 10. Modelo de dominio de cabecera

Nombre recomendado:

```text
WithdrawalHistory
```

Modelo orientativo:

```java
public final class WithdrawalHistory {

    private final long id;
    private final String title;
    private final long registeredAt;
    private final long createdAt;
    private final long updatedAt;

    // Constructor y getters.
}
```

### Reglas

- `id = 0` podrá representar un registro todavía no persistido.
- `title` podrá ser `null`.
- Un título vacío o compuesto solo por espacios deberá normalizarse posteriormente a `null`.
- `registeredAt` representará el momento documental de la lista.
- `createdAt` representará cuándo se creó el registro local.
- `updatedAt` permitirá una evolución posterior sin alterar `registeredAt`.
- Los timestamps deberán ser mayores que cero en modelos persistidos.
- El modelo no dependerá de Android ni Room.
- El modelo no contendrá la lista completa si se utiliza un agregado separado para operaciones de persistencia.

### Observación crítica

HU-26 no necesita todavía un modelo con contadores persistidos como:

```text
entryCount
foundCount
notFoundCount
```

Esos valores pueden derivarse de las líneas.

Solo deberán persistirse si una historia posterior demuestra una necesidad de rendimiento real.

---

## 11. Modelo de dominio de línea

Nombre recomendado:

```text
WithdrawalHistoryEntry
```

Modelo orientativo:

```java
public final class WithdrawalHistoryEntry {

    private final long id;
    private final long historyId;
    private final int orderIndex;
    private final String category;
    private final String code;
    private final Integer quantity;
    private final String unit;
    private final Long warehouseItemIdSnapshot;
    private final String siteSnapshot;
    private final String positionSnapshot;
    private final WithdrawalLocationStatus locationStatus;

    // Constructor y getters.
}
```

### Reglas

- `orderIndex` será mayor o igual que cero.
- `category` será obligatoria.
- `code` será obligatorio.
- categoría y código se conservarán como `String`.
- no se convertirán códigos a número.
- `quantity` será opcional.
- cuando exista, `quantity` deberá ser positiva.
- `unit` será opcional.
- `warehouseItemIdSnapshot` será opcional.
- `siteSnapshot` será opcional.
- `positionSnapshot` será opcional.
- el modelo no dependerá de `WarehouseItemEntity`.
- la igualdad funcional de una línea no deberá utilizar su cantidad como identidad.

### Duplicados

HU-26 no impondrá una restricción única por:

```text
historyId + category + code
```

Motivo:

- la deduplicación corresponde al flujo de revisión;
- la persistencia no debe impedir una futura decisión de conservar repeticiones documentales;
- la base debe conservar exactamente las líneas entregadas por el caso de uso, en su orden.

La historia posterior responsable del guardado validará la colección antes de persistirla.

---

## 12. Estado histórico de localización

Nombre recomendado:

```text
WithdrawalLocationStatus
```

Valores:

```text
FOUND
NOT_FOUND
```

### `FOUND`

Deberá representar una referencia localizada al crear el historial.

Invariantes previstas:

```text
warehouseItemIdSnapshot != null
siteSnapshot no vacío
```

La posición podrá ser nula.

### `NOT_FOUND`

Deberá representar una referencia confirmada que no existía en Room.

Invariantes previstas:

```text
warehouseItemIdSnapshot == null
siteSnapshot == null
positionSnapshot == null
```

### Persistencia del enum

Room podrá almacenarlo como texto mediante:

- `String` controlado en la entidad; o
- `TypeConverter` pequeño y explícito.

Para dos valores estables, almacenar texto es preferible a depender del ordinal.

No deberá persistirse:

```text
0 = FOUND
1 = NOT_FOUND
```

mediante `ordinal()`, porque reordenar el enum cambiaría el significado almacenado.

---

## 13. Modelo agregado para persistencia

Para guardar cabecera y líneas en una única operación se recomienda un modelo:

```text
WithdrawalHistoryRecord
├── history
└── entries
```

Nombre alternativo válido:

```text
WithdrawalHistoryAggregate
```

Responsabilidades:

- contener una cabecera;
- contener una copia defensiva de las líneas;
- impedir colección nula;
- conservar orden;
- no depender de Room;
- no contener `Context`;
- no ejecutar persistencia.

No es necesario crear simultáneamente `Draft`, `Record` y `Aggregate` en HU-26.

Se elegirá un único modelo cuando exista una responsabilidad concreta.

---

## 14. Entidad Room de cabecera

Nombre recomendado:

```text
WithdrawalHistoryEntity
```

Tabla:

```text
withdrawal_history
```

Columnas:

| Columna | Tipo SQLite | Nulo | Descripción |
|---|---|---:|---|
| `id` | INTEGER | No | PK autogenerada |
| `title` | TEXT | Sí | Título opcional |
| `registered_at` | INTEGER | No | Momento documental |
| `created_at` | INTEGER | No | Creación local |
| `updated_at` | INTEGER | No | Última actualización |

Definición orientativa:

```java
@Entity(tableName = "withdrawal_history")
public class WithdrawalHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "registered_at")
    private long registeredAt;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;
}
```

No se almacenarán contadores derivados en HU-26.

---

## 15. Entidad Room de línea

Nombre recomendado:

```text
WithdrawalHistoryEntryEntity
```

Tabla:

```text
withdrawal_history_entries
```

Columnas:

| Columna | Tipo SQLite | Nulo | Descripción |
|---|---|---:|---|
| `id` | INTEGER | No | PK autogenerada |
| `history_id` | INTEGER | No | FK hacia cabecera |
| `order_index` | INTEGER | No | Orden documental |
| `category` | TEXT | No | Categoría histórica |
| `code` | TEXT | No | Código histórico |
| `quantity` | INTEGER | Sí | Cantidad documental |
| `unit` | TEXT | Sí | Unidad documental |
| `warehouse_item_id_snapshot` | INTEGER | Sí | Id existente al guardar |
| `site_snapshot` | TEXT | Sí | Sitio histórico |
| `position_snapshot` | TEXT | Sí | Posición histórica |
| `location_status` | TEXT | No | `FOUND` o `NOT_FOUND` |

### Clave foránea hacia la cabecera

```java
foreignKeys = @ForeignKey(
        entity = WithdrawalHistoryEntity.class,
        parentColumns = "id",
        childColumns = "history_id",
        onDelete = ForeignKey.CASCADE
)
```

### Índice obligatorio

```java
@Index(value = "history_id")
```

Room exige que la columna hija de una clave foránea esté indexada para evitar consultas y eliminaciones ineficientes.

### Restricción de orden

Se recomienda un índice único:

```text
history_id + order_index
```

Motivo:

- una lista no debe contener dos filas con la misma posición interna;
- permite detectar errores del mapper o del caso de uso;
- no impide repetir una misma referencia en posiciones distintas.

---

## 16. Decisión sobre la relación con `warehouse_items`

`warehouse_item_id_snapshot` no tendrá una clave foránea obligatoria hacia `warehouse_items`.

Motivos:

- la mercadería puede eliminarse posteriormente;
- el historial debe seguir existiendo;
- una referencia `NOT_FOUND` no tiene id;
- el campo representa una instantánea, no una relación viva;
- una eliminación de mercadería no debe eliminar ni invalidar el historial.

No se utilizará:

```text
ON DELETE CASCADE
```

entre mercadería e historial.

Tampoco se utilizará:

```text
ON DELETE SET NULL
```

porque modificaría retrospectivamente la instantánea guardada.

El id histórico será únicamente informativo y deberá validarse antes de intentar abrir la mercadería actual en historias posteriores.

---

## 17. Relación Room

Se añadirá un modelo de relación para lectura:

```text
WithdrawalHistoryWithEntries
```

Estructura orientativa:

```java
public final class WithdrawalHistoryWithEntries {

    @Embedded
    public WithdrawalHistoryEntity history;

    @Relation(
            parentColumn = "id",
            entityColumn = "history_id"
    )
    public List<WithdrawalHistoryEntryEntity> entries;
}
```

La consulta deberá ordenar las líneas por `order_index`.

Como `@Relation` no garantiza por sí sola el orden de la colección, el DAO o mapper deberá asegurar explícitamente el orden.

No deberá confiarse en el orden físico de inserción de SQLite.

---

## 18. DAO histórico

Nombre recomendado:

```text
WithdrawalHistoryDao
```

Operaciones mínimas de HU-26:

```text
insertHistory(...)
insertEntries(...)
insertHistoryWithEntries(...)
findByIdWithEntries(...)
deleteById(...)
```

La lectura por id será necesaria para probar la persistencia completa y preparar HU-30.

### Inserción de cabecera

```java
@Insert(onConflict = OnConflictStrategy.ABORT)
long insertHistory(WithdrawalHistoryEntity entity);
```

### Inserción de líneas

```java
@Insert(onConflict = OnConflictStrategy.ABORT)
List<Long> insertEntries(
        List<WithdrawalHistoryEntryEntity> entities
);
```

### Guardado transaccional

La operación deberá:

1. insertar cabecera;
2. obtener el id;
3. asignar el id a cada línea;
4. insertar todas las líneas;
5. finalizar correctamente o revertir todo.

La transacción podrá implementarse mediante:

- un método `@Transaction default` dentro del DAO; o
- `database.runInTransaction(...)` dentro del repositorio.

Se recomienda mantener la operación atómica cerca de Room y evitar que el ViewModel coordine inserciones.

---

## 19. Regla de atomicidad

No deberá ser posible este estado:

```text
withdrawal_history
→ cabecera creada

withdrawal_history_entries
→ solo algunas líneas
```

Ante cualquier error:

```text
cabecera = no persistida
líneas = no persistidas
```

La prueba deberá forzar un fallo después de iniciar el guardado, por ejemplo mediante:

- un `order_index` duplicado bajo el índice único;
- una línea inválida que viole una restricción `NOT NULL`;
- una clave foránea inexistente en una operación de prueba controlada.

Después del fallo se comprobará que no quedó la cabecera huérfana.

---

## 20. Mappers Room

Se recomienda crear:

```text
WithdrawalHistoryRoomMapper
```

Responsabilidades:

- dominio de cabecera → entidad;
- entidad → dominio de cabecera;
- dominio de línea → entidad;
- entidad → dominio de línea;
- relación Room → agregado de dominio;
- transformar `locationStatus` sin ordinal;
- copiar colecciones defensivamente;
- conservar orden.

No deberá:

- generar timestamps;
- decidir cantidades;
- consultar Room;
- acceder a recursos Android;
- construir mensajes de UI;
- aplicar reglas OCR.

Si separar cabecera y línea en dos mappers mejora la legibilidad, será válido.

No deberán crearse varios mappers que solo deleguen entre sí.

---

## 21. Repositorio histórico

Contrato recomendado:

```text
WithdrawalHistoryRepository
```

Ubicación coherente con el proyecto actual:

```text
data/repository/
```

Aunque el contrato sea una interfaz, deberá representar una dependencia real y preparar la sustitución futura de Room por una fuente sincronizada.

Operaciones mínimas:

```java
void insert(
        WithdrawalHistoryRecord record,
        RepositoryCallback<Long> callback
);

void findById(
        long historyId,
        RepositoryCallback<WithdrawalHistoryRecord> callback
);

void deleteById(
        long historyId,
        RepositoryCallback<Void> callback
);
```

### Observación

`deleteById` podrá existir a nivel de persistencia en HU-26 para probar cascada y completar el contrato base.

La acción funcional de eliminación y su confirmación seguirán perteneciendo a HU-32.

No se añadirán todavía:

```text
observeAll
search
filterByDate
```

porque esas operaciones pertenecen a historias posteriores y sus contratos deberán diseñarse según las necesidades reales de esas pantallas.

---

## 22. Implementación Room del repositorio

Nombre recomendado:

```text
RoomWithdrawalHistoryRepository
```

Dependencias:

```text
WithdrawalHistoryDao
WithdrawalHistoryRoomMapper
ExecutorService
```

Responsabilidades:

- validar callback;
- copiar defensivamente el agregado;
- ejecutar fuera del hilo principal;
- delegar el guardado transaccional;
- mapear dominio y Room;
- devolver el id generado;
- devolver `onNotFound()` cuando no exista un id;
- transformar errores mediante `onError()`;
- ejecutar un único callback final;
- no exponer entidades Room.

No deberá:

- generar interfaces adicionales sin necesidad;
- conocer Activities;
- navegar;
- mostrar mensajes;
- descontar stock;
- consultar `WarehouseItemDao` para modificar mercadería.

---

## 23. Actualización de `AlmacenTrackerDatabase`

La base pasará de:

```text
version = 1
```

a:

```text
version = 2
```

Entidades:

```java
@Database(
        entities = {
                WarehouseItemEntity.class,
                WithdrawalHistoryEntity.class,
                WithdrawalHistoryEntryEntity.class
        },
        version = 2,
        exportSchema = true
)
```

DAOs:

```java
public abstract WarehouseItemDao warehouseItemDao();

public abstract WithdrawalHistoryDao
withdrawalHistoryDao();
```

No se modificará la tabla `warehouse_items` en HU-26.

---

## 24. Migración `1 → 2`

Nombre recomendado:

```text
AlmacenTrackerMigrations
```

Ubicación:

```text
data/local/room/database/
```

o:

```text
data/local/room/migration/
```

Se elegirá una ubicación coherente y única.

Contrato:

```java
public static final Migration MIGRATION_1_2 =
        new Migration(1, 2) {
            @Override
            public void migrate(
                    SupportSQLiteDatabase database
            ) {
                // CREATE TABLE ...
                // CREATE INDEX ...
            }
        };
```

La migración deberá crear:

```text
withdrawal_history
withdrawal_history_entries
índice de history_id
índice único de history_id + order_index
```

No deberá:

- borrar `warehouse_items`;
- renombrar columnas actuales;
- recrear la base completa;
- utilizar migración destructiva;
- insertar historiales ficticios;
- inferir historial desde datos existentes.

Después de migrar:

```text
warehouse_items → conserva todos sus registros
withdrawal_history → vacía
withdrawal_history_entries → vacía
```

---

## 25. SQL conceptual de la migración

La implementación deberá ajustarse exactamente al esquema generado por Room.

SQL orientativo:

```sql
CREATE TABLE IF NOT EXISTS `withdrawal_history` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `title` TEXT,
    `registered_at` INTEGER NOT NULL,
    `created_at` INTEGER NOT NULL,
    `updated_at` INTEGER NOT NULL
);
```

```sql
CREATE TABLE IF NOT EXISTS `withdrawal_history_entries` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `history_id` INTEGER NOT NULL,
    `order_index` INTEGER NOT NULL,
    `category` TEXT NOT NULL,
    `code` TEXT NOT NULL,
    `quantity` INTEGER,
    `unit` TEXT,
    `warehouse_item_id_snapshot` INTEGER,
    `site_snapshot` TEXT,
    `position_snapshot` TEXT,
    `location_status` TEXT NOT NULL,
    FOREIGN KEY(`history_id`)
        REFERENCES `withdrawal_history`(`id`)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
```

```sql
CREATE INDEX IF NOT EXISTS
`index_withdrawal_history_entries_history_id`
ON `withdrawal_history_entries` (`history_id`);
```

```sql
CREATE UNIQUE INDEX IF NOT EXISTS
`index_withdrawal_history_entries_history_id_order_index`
ON `withdrawal_history_entries`
(`history_id`, `order_index`);
```

El SQL definitivo deberá compararse con `2.json`.

---

## 26. Registro de la migración

`AppContainer` deberá pasar de:

```java
Room.databaseBuilder(
        applicationContext,
        AlmacenTrackerDatabase.class,
        "almacen_tracker.db"
).build();
```

a una construcción que registre:

```java
.addMigrations(
        AlmacenTrackerMigrations.MIGRATION_1_2
)
```

La aplicación no deberá abrir una base versión `1` con la definición versión `2` sin la migración.

No se añadirá:

```java
.fallbackToDestructiveMigration()
```

---

## 27. Composición de dependencias

Se podrá crear:

```text
WithdrawalHistoryModule
```

solo si agrupa dependencias reales que utilizarán las historias posteriores.

En HU-26, el contenedor deberá poder construir al menos:

```text
WithdrawalHistoryDao
WithdrawalHistoryRoomMapper
RoomWithdrawalHistoryRepository
```

Sin embargo, no se añadirán factories de ViewModel porque todavía no existe una pantalla histórica.

Opciones válidas:

### Opción A — Campo directo en `AppContainer`

Adecuada mientras solo exista el repositorio.

### Opción B — `WithdrawalHistoryModule`

Adecuada si se deja preparado para servicios y ViewModels posteriores.

La decisión deberá priorizar claridad, no simetría artificial con otros módulos.

---

## 28. Exportación del esquema Room

Después de compilar, deberá generarse:

```text
app/schemas/
└── com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase/
    ├── 1.json
    └── 2.json
```

Se deberá verificar que `2.json` contiene:

- `warehouse_items` sin cambios inesperados;
- `withdrawal_history`;
- `withdrawal_history_entries`;
- clave foránea;
- índices;
- tipos y nulabilidad correctos.

El esquema `1.json` no deberá modificarse manualmente.

---

## 29. Pruebas unitarias de dominio

Se cubrirán como mínimo:

### Cabecera

- título nulo permitido;
- timestamps conservados;
- id cero permitido para no persistido;
- valores expuestos sin mutación.

### Línea `FOUND`

- categoría y código obligatorios;
- ceros iniciales conservados;
- posición nula permitida;
- sitio requerido;
- id snapshot requerido;
- cantidad positiva permitida.

### Línea `NOT_FOUND`

- id snapshot nulo;
- sitio nulo;
- posición nula;
- cantidad opcional permitida.

### Cantidad inválida

- cero rechazado cuando se aplique la validación;
- negativos rechazados.

La validación podrá ubicarse en un modelo o componente separado según la estructura real.

No deberán escribirse pruebas triviales de getters únicamente para aumentar cobertura.

---

## 30. Pruebas de mapper

Se comprobará:

- dominio de cabecera → entidad;
- entidad → dominio;
- dominio de línea → entidad;
- entidad → dominio;
- `FOUND` se persiste como texto;
- `NOT_FOUND` se persiste como texto;
- cantidad nula se conserva;
- unidad nula se conserva;
- ceros iniciales se conservan;
- `orderIndex` se conserva;
- la colección se ordena por `orderIndex` al reconstruir el agregado.

---

## 31. Pruebas instrumentadas del DAO

Archivo recomendado:

```text
WithdrawalHistoryDaoTest
```

Casos mínimos:

1. insertar una cabecera devuelve id positivo;
2. guardar cabecera y líneas conserva todos los datos;
3. las líneas se recuperan por `orderIndex`;
4. una lista sin título puede persistirse;
5. cantidad y unidad nulas pueden persistirse;
6. referencias `FOUND` conservan ubicación;
7. referencias `NOT_FOUND` conservan valores nulos;
8. eliminar cabecera elimina líneas por cascada;
9. eliminar historial no modifica `warehouse_items`;
10. índices impiden dos líneas con el mismo `orderIndex` dentro de una lista;
11. el mismo `orderIndex` puede existir en listas diferentes;
12. el mismo código puede aparecer en historiales diferentes;
13. un fallo intermedio revierte cabecera y líneas;
14. consultar id inexistente devuelve ausencia controlada.

Se utilizará una base en memoria cuando no se pruebe migración.

---

## 32. Prueba de migración

Archivo recomendado:

```text
AlmacenTrackerDatabaseMigrationTest
```

Se utilizará:

```text
MigrationTestHelper
```

Flujo principal:

1. crear base en versión `1`;
2. insertar una o varias filas reales en `warehouse_items` mediante SQL;
3. cerrar la base versión `1`;
4. ejecutar `MIGRATION_1_2`;
5. validar el esquema versión `2`;
6. consultar `warehouse_items`;
7. comprobar que sus datos permanecen;
8. comprobar que las nuevas tablas existen;
9. comprobar que comienzan vacías;
10. insertar un historial después de migrar;
11. comprobar que la clave foránea y los índices funcionan.

### Datos que deben conservarse

Se comprobarán expresamente:

- id;
- categoría;
- código;
- sitio;
- posición;
- observaciones;
- `created_at`;
- `updated_at`.

No bastará con comprobar solo el número de filas.

---

## 33. Pruebas del repositorio

Casos mínimos:

- inserción devuelve el id de cabecera;
- inserción se ejecuta en el executor;
- entrada se copia defensivamente;
- `findById` devuelve cabecera y líneas;
- `findById` conserva orden;
- id inexistente produce `onNotFound()`;
- error del DAO produce `onError()`;
- callback de éxito se ejecuta una vez;
- eliminación elimina el agregado;
- la mercadería no se modifica.

Podrá utilizarse un DAO falso o una base en memoria según el tipo de prueba.

No se deberá probar Room mediante mocks cuando una prueba instrumentada sencilla ofrezca mayor confianza.

---

## 34. Flujo técnico principal

1. La aplicación abre una base existente en versión `1`.
2. Room detecta que la definición actual es versión `2`.
3. Ejecuta `MIGRATION_1_2`.
4. Se crean las tablas de historial.
5. Se crean los índices.
6. `warehouse_items` permanece intacta.
7. `AppContainer` obtiene `WithdrawalHistoryDao`.
8. Construye el mapper y el repositorio histórico.
9. Una prueba o historia posterior entrega un agregado.
10. El repositorio ejecuta el guardado en el executor.
11. El DAO abre una transacción.
12. Inserta la cabecera.
13. Inserta las líneas con el id generado.
14. La transacción finaliza.
15. El repositorio devuelve el id histórico.

---

## 35. Flujos alternativos

### FA-01 — Base nueva

1. La aplicación se instala sin base previa.
2. Room crea directamente el esquema versión `2`.
3. Las tres tablas quedan disponibles.
4. No se ejecuta una migración innecesaria.

### FA-02 — Migración con mercadería existente

1. La base versión `1` contiene mercadería.
2. Se actualiza la aplicación.
3. Se ejecuta la migración.
4. La mercadería permanece sin cambios.
5. Las tablas históricas comienzan vacías.

### FA-03 — Historial sin título

1. Se crea una cabecera con `title = null`.
2. Se insertan sus líneas.
3. El guardado finaliza correctamente.

### FA-04 — Línea sin cantidad

1. `quantity = null`.
2. `unit = null`.
3. La línea se persiste correctamente.

### FA-05 — Referencia no encontrada

1. `locationStatus = NOT_FOUND`.
2. No existe id snapshot.
3. No existe sitio ni posición.
4. La línea se persiste correctamente.

### FA-06 — Mercadería eliminada posteriormente

1. Se guarda una línea `FOUND` con instantánea.
2. Se elimina la mercadería actual.
3. El historial permanece.
4. Su sitio y posición históricos permanecen.

### FA-07 — Error al insertar una línea

1. Se inserta la cabecera dentro de la transacción.
2. Una línea viola una restricción.
3. Room cancela la transacción.
4. No queda cabecera huérfana.
5. No quedan líneas parciales.

### FA-08 — Id histórico inexistente

1. El repositorio consulta un id ausente.
2. Devuelve `onNotFound()`.
3. No devuelve un agregado vacío como si existiera.

### FA-09 — Eliminación de cabecera

1. Se elimina un historial.
2. Room elimina sus líneas por cascada.
3. No modifica mercadería.

### FA-10 — Dos historiales con la misma referencia

1. La referencia `MR + 1210` aparece en dos listas.
2. Ambos historiales se guardan.
3. No existe conflicto de unicidad global.

---

## 36. Criterios de aceptación

### CA-01 — Base actualizada

**Dado** el esquema existente de Room versión `1`,  
**cuando** se implemente HU-26,  
**entonces** `AlmacenTrackerDatabase` tendrá versión `2` y declarará las entidades históricas.

### CA-02 — Migración no destructiva

**Dado** que existen registros en `warehouse_items`,  
**cuando** se ejecute `MIGRATION_1_2`,  
**entonces** todos los registros y campos existentes permanecerán intactos.

### CA-03 — Tablas nuevas

**Dado** el esquema versión `2`,  
**cuando** se cree o migre la base,  
**entonces** existirán `withdrawal_history` y `withdrawal_history_entries`.

### CA-04 — Relación uno a muchos

**Dado** un historial con varias referencias,  
**cuando** se persista,  
**entonces** una cabecera quedará relacionada con todas sus líneas.

### CA-05 — Orden conservado

**Dado** un conjunto ordenado de líneas,  
**cuando** se guarde y vuelva a consultar,  
**entonces** se recuperará según `orderIndex`.

### CA-06 — Guardado atómico

**Dado** que una línea no puede insertarse,  
**cuando** falle el guardado,  
**entonces** no quedará persistida la cabecera ni ninguna línea parcial.

### CA-07 — Título opcional

**Dado** un historial sin título,  
**cuando** se persista,  
**entonces** el guardado finalizará correctamente.

### CA-08 — Cantidad opcional

**Dado** una línea sin cantidad ni unidad,  
**cuando** se persista,  
**entonces** ambos campos se conservarán como nulos.

### CA-09 — Instantánea encontrada

**Dado** una referencia localizada,  
**cuando** se guarde,  
**entonces** conservará id, sitio, posición opcional y estado `FOUND`.

### CA-10 — Referencia no encontrada

**Dado** una referencia no localizada,  
**cuando** se guarde,  
**entonces** conservará categoría y código con estado `NOT_FOUND` sin exigir ubicación.

### CA-11 — Independencia de mercadería

**Dado** un historial existente,  
**cuando** se elimine una mercadería actual,  
**entonces** el historial y su instantánea permanecerán.

### CA-12 — Cascada interna

**Dado** un historial con líneas,  
**cuando** se elimine su cabecera,  
**entonces** Room eliminará únicamente sus líneas asociadas.

### CA-13 — Repositorio asíncrono

**Dado** un agregado válido,  
**cuando** el repositorio lo persista,  
**entonces** la operación se ejecutará fuera del hilo principal y devolverá un único resultado.

### CA-14 — Esquema exportado

**Dado** que `exportSchema` está activo,  
**cuando** se compile el proyecto,  
**entonces** se generará y validará `2.json` sin modificar `1.json`.

### CA-15 — Sin funcionalidades adelantadas

**Dado** el alcance de HU-26,  
**cuando** finalice,  
**entonces** no se habrán creado todavía pantallas ni flujos de guardado iniciados por el usuario.

---

## 37. Archivos previstos

La lista definitiva deberá ajustarse al código real.

Archivos nuevos orientativos:

```text
app/src/main/java/com/rndymi/almacentracker/
├── domain/history/
│   ├── WithdrawalHistory.java
│   ├── WithdrawalHistoryEntry.java
│   ├── WithdrawalHistoryRecord.java
│   └── WithdrawalLocationStatus.java
│
├── data/local/room/entity/
│   ├── WithdrawalHistoryEntity.java
│   └── WithdrawalHistoryEntryEntity.java
│
├── data/local/room/relation/
│   └── WithdrawalHistoryWithEntries.java
│
├── data/local/room/dao/
│   └── WithdrawalHistoryDao.java
│
├── data/local/room/mapper/
│   └── WithdrawalHistoryRoomMapper.java
│
├── data/local/room/database/
│   └── AlmacenTrackerMigrations.java
│
└── data/repository/
    ├── WithdrawalHistoryRepository.java
    └── RoomWithdrawalHistoryRepository.java
```

Archivos existentes a modificar:

```text
AlmacenTrackerDatabase.java
AppContainer.java
```

Archivos de prueba orientativos:

```text
app/src/test/java/com/rndymi/almacentracker/
├── domain/history/
│   └── WithdrawalHistoryEntryTest.java
├── data/local/room/mapper/
│   └── WithdrawalHistoryRoomMapperTest.java
└── data/repository/
    └── RoomWithdrawalHistoryRepositoryTest.java
```

```text
app/src/androidTest/java/com/rndymi/almacentracker/
└── data/local/room/
    ├── dao/
    │   └── WithdrawalHistoryDaoTest.java
    └── database/
        └── AlmacenTrackerDatabaseMigrationTest.java
```

Esquema generado:

```text
app/schemas/com.rndymi.almacentracker.data.local.room.database.AlmacenTrackerDatabase/2.json
```

### Regla crítica

No deberán crearse todos los archivos por obligación.

Podrán combinarse componentes cuando:

- mantengan una responsabilidad clara;
- reduzcan delegaciones triviales;
- no dificulten las pruebas;
- respeten el estilo actual del proyecto.

No se crearán Activities, ViewModels, adapters ni layouts en HU-26.

---

## 38. Estrategia de commits recomendada

Los commits deberán realizarse en el mismo orden lógico de los cambios.

### Commit 1 — Dominio histórico

```bash
git commit -m "add withdrawal history domain models #30"
```

Incluye:

- cabecera;
- línea;
- estado de localización;
- agregado;
- pruebas unitarias de reglas.

### Commit 2 — Entidades y DAO Room

```bash
git commit -m "add withdrawal history Room entities and DAO #30"
```

Incluye:

- entidades;
- relación;
- DAO;
- mapper;
- pruebas DAO y mapper.

### Commit 3 — Repositorio histórico

```bash
git commit -m "add Room withdrawal history repository #30"
```

Incluye:

- contrato;
- implementación;
- composición básica;
- pruebas del repositorio.

### Commit 4 — Migración de base de datos

```bash
git commit -m "add Room migration for withdrawal history #30"
```

Incluye:

- versión `2`;
- `MIGRATION_1_2`;
- registro en `AppContainer`;
- esquema `2.json`;
- pruebas de migración.

No se recomienda agrupar toda la HU en un único commit si los bloques pueden verificarse de forma independiente.

---

## 39. Pruebas manuales

Aunque HU-26 no añade interfaz, se deberá verificar:

1. instalar v1.2.0 con datos de mercadería;
2. actualizar a una build con HU-26;
3. abrir el listado principal;
4. comprobar que los datos continúan visibles;
5. buscar y abrir detalles existentes;
6. crear, editar y eliminar mercadería para comprobar que el CRUD sigue operativo;
7. cerrar y abrir la aplicación;
8. comprobar que no aparece error de migración;
9. ejecutar el flujo OCR existente;
10. comprobar que las funciones de v1.2 siguen operativas;
11. verificar que la aplicación continúa sin requerir Internet.

La persistencia histórica se validará principalmente mediante pruebas automatizadas hasta que HU-28 añada el flujo funcional de guardado.

---

## 40. Comandos de verificación

Desde la raíz del proyecto:

```bash
./gradlew testDebugUnitTest
```

```bash
./gradlew connectedDebugAndroidTest
```

```bash
./gradlew lintDebug
```

```bash
./gradlew assembleDebug
```

También deberá comprobarse que el esquema Room versión `2` fue generado.

---

## 41. Riesgos

### Pérdida de datos durante migración

**Mitigación:** migración explícita, `MigrationTestHelper` y verificación campo por campo.

### Cabeceras huérfanas

**Mitigación:** guardado transaccional y prueba de rollback.

### Líneas huérfanas

**Mitigación:** clave foránea hacia la cabecera y `ON DELETE CASCADE`.

### Historial eliminado al borrar mercadería

**Mitigación:** no crear clave foránea viva hacia `warehouse_items`.

### Orden inestable

**Mitigación:** `orderIndex`, índice único por lista y ordenación explícita.

### Modelo confundido con stock

**Mitigación:** cantidad separada de `WarehouseItem` y sin operaciones cuantitativas.

### Interfaces y capas innecesarias

**Mitigación:** crear solo modelos, mapper, DAO y repositorio con responsabilidades comprobables.

### Esquema SQL distinto del generado por Room

**Mitigación:** comparar la migración con `2.json` y ejecutar pruebas de validación.

### Cambios silenciosos en backup

**Mitigación:** no modificar CSV ni copias de seguridad en HU-26.

---

## 42. Definición de terminado

HU-26 estará terminada cuando:

- existan modelos de dominio para cabecera y líneas;
- el historial esté separado de `WarehouseItem`;
- existan entidades Room para cabecera y líneas;
- exista una relación uno a muchos;
- la clave foránea aplique cascada únicamente dentro del historial;
- exista `orderIndex` y se conserve el orden;
- cantidad, unidad y título puedan ser nulos;
- referencias encontradas y no encontradas sean representables;
- exista un DAO histórico;
- el guardado de cabecera y líneas sea transaccional;
- exista un repositorio Room histórico;
- las entidades Room no se expongan a la UI;
- `AlmacenTrackerDatabase` utilice versión `2`;
- exista y se registre `MIGRATION_1_2`;
- los datos de `warehouse_items` se conserven al migrar;
- se genere `2.json`;
- las pruebas unitarias sean satisfactorias;
- las pruebas DAO sean satisfactorias;
- las pruebas de migración sean satisfactorias;
- lint sea satisfactorio;
- build sea satisfactoria;
- CI sea satisfactoria;
- no se hayan añadido pantallas ni funciones de historias posteriores;
- la aplicación continúe funcionando completamente sin conexión.

---

## 43. Resultado esperado

Al finalizar HU-26, el proyecto dispondrá de una base persistente segura para v1.3:

```text
mercadería actual
        ↓
warehouse_items

historial documental
        ↓
withdrawal_history
        ↓
withdrawal_history_entries
```

La actualización desde v1.2.0 conservará toda la mercadería existente.

El historial podrá guardar en historias posteriores:

- título;
- fecha y hora;
- categoría;
- código;
- cantidad y unidad opcionales;
- sitio y posición históricos;
- referencia encontrada o no encontrada.

La siguiente historia será:

```text
HU-27 — Capturar título, cantidad y unidad de una lista
```
