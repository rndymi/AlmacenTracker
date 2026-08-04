# AlmacenTracker — Plan de la versión 1.3

> Cuarta entrega funcional: registro e historial documental de mercadería sacada a partir de listas procesadas.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android prevista:** 1.3.0  
**Nombre de la versión:** Historial documental de mercadería sacada  
**Estado:** Planificada  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.2.0`

---

## 2. Contexto de partida

AlmacenTracker v1.2.0 permitirá gestionar mercadería localmente, intercambiar y respaldar información mediante CSV, escanear códigos individuales y procesar imágenes de listas mediante OCR local.

El flujo documental permitirá:

- tomar una fotografía o seleccionar una imagen;
- corregir orientación, escala y contraste;
- reconstruir filas y documentos de una o dos columnas;
- extraer referencias;
- revisar coincidencias exactas, sugeridas, ambiguas o no encontradas;
- corregir, añadir, eliminar y confirmar referencias;
- consultar sitio y posición;
- funcionar sin conexión.

La versión 1.2 utilizará esas listas para localizar mercadería, pero no conservará un historial persistente.

La versión 1.3 ampliará el flujo para registrar documentalmente las listas confirmadas y la mercadería sacada.

La versión reutilizará la arquitectura MVVM pragmática organizada por funcionalidades:

```text
app
core
domain
data
feature
```

Cada historia deberá mantener un alcance independiente y dejar la aplicación operativa antes de comenzar la siguiente.

---

## 3. Objetivo de la versión

Permitir que una lista procesada, revisada y confirmada pueda registrarse como un documento histórico local.

```text
fotografía o imagen
        ↓
OCR y reconstrucción
        ↓
revisión de referencias
        ↓
captura de datos documentales
        ↓
consulta de ubicaciones
        ↓
confirmación
        ↓
historial local en Room
```

El historial permitirá conocer posteriormente:

```text
qué lista se procesó
cuándo se registró
qué mercadería contenía
qué cantidad documental aparecía
en qué ubicación se encontraba entonces
```

---

## 4. Regla de oro de la versión

> El historial de v1.3 tendrá carácter documental y no representará el stock real del almacén.

Registrar:

```text
MR 1210 — 4 cajas
```

significará que la lista documentó cuatro cajas de esa referencia.

No significará que la aplicación deba descontar cuatro unidades.

La mercadería principal seguirá identificándose mediante:

```text
categoría + código
```

La cantidad y la unidad pertenecerán únicamente al registro histórico.

---

## 5. Alcance incluido

La versión 1.3 incluirá:

- registrar una lista procesada como historial;
- título opcional;
- fecha y hora automáticas;
- conservación del orden confirmado;
- categoría y código;
- cantidad documental opcional;
- unidad documental opcional;
- corrección manual de cantidad y unidad;
- sitio y posición como instantánea histórica;
- referencias encontradas y no encontradas;
- confirmación antes del guardado;
- persistencia de cabecera y líneas mediante Room;
- migración explícita de base de datos;
- guardado transaccional;
- listado histórico;
- detalle histórico;
- búsqueda por título, categoría o código;
- filtro por intervalo de fechas;
- estados vacíos y sin resultados;
- eliminación de registros históricos con confirmación;
- funcionamiento completamente offline;
- pruebas unitarias, Room, migración, repositorio, servicios y ViewModels;
- CI;
- release `v1.3.0`.

---

## 6. Alcance excluido

La versión 1.3 no incluirá:

- gestión de stock;
- descuento o incremento automático de cantidades;
- existencias disponibles;
- reservas;
- pedidos;
- devoluciones;
- transferencias entre almacenes;
- actualización de mercadería desde el historial;
- modificación retrospectiva de ubicaciones históricas;
- asociación obligatoria con clientes;
- autenticación;
- usuarios o roles;
- sincronización remota;
- backend;
- almacenamiento permanente de fotografías;
- persistencia del texto OCR completo;
- PDF o varias páginas;
- estadísticas avanzadas;
- ONNX Runtime o PP-OCRv5.

La evolución del OCR pertenecerá a v1.4.

---

## 7. Conceptos de la versión

### 7.1. Registro histórico

Representará una lista confirmada y guardada.

Datos previstos:

```text
id
title
registeredAt
createdAt
updatedAt
```

El título será opcional.

Cuando no exista, la interfaz podrá mostrar un título derivado de la fecha.

### 7.2. Línea histórica

Cada referencia confirmada generará una línea.

Datos previstos:

```text
id
historyId
orderIndex
category
code
quantity
unit
warehouseItemIdSnapshot
siteSnapshot
positionSnapshot
locationStatus
```

### 7.3. Instantánea de ubicación

El historial conservará el sitio y la posición existentes al guardar la lista.

Si la ubicación actual cambia después:

```text
historial → conserva la ubicación anterior
mercadería → muestra la ubicación actual
```

### 7.4. Estado de localización

Cada línea podrá conservar:

```text
FOUND
NOT_FOUND
```

Una referencia no encontrada podrá formar parte del historial sin bloquear el guardado de la lista.

---

## 8. Cantidad y unidad documentales

La cantidad y la unidad serán opcionales.

Ejemplos:

```text
quantity = 4
unit = CAJAS
```

```text
quantity = null
unit = null
```

La aplicación podrá proponerlas a partir del texto reconocido, pero el usuario deberá poder:

- corregirlas;
- completarlas;
- eliminarlas;
- dejarlas vacías.

Para la primera versión se recomienda admitir cantidades enteras positivas.

No se admitirán inicialmente negativos, operaciones, intervalos o decimales sin una necesidad documentada.

La unidad podrá normalizarse a mayúsculas.

---

## 9. Título y fecha

El título será opcional y podrá representar una tienda, persona, empresa o tipo de lista.

Ejemplos:

```text
Reposición tienda centro
Lista de Ana
Proveedor Norte
```

La fecha y hora se asignarán automáticamente al crear el registro.

La presentación utilizará la zona horaria del dispositivo.

---

## 10. Modelo conceptual

### Cabecera

```java
public final class WithdrawalHistory {
    private long id;
    private String title;
    private long registeredAt;
    private long createdAt;
    private long updatedAt;
}
```

### Línea

```java
public final class WithdrawalHistoryEntry {
    private long id;
    private long historyId;
    private int orderIndex;
    private String category;
    private String code;
    private Integer quantity;
    private String unit;
    private Long warehouseItemIdSnapshot;
    private String siteSnapshot;
    private String positionSnapshot;
    private LocationStatus locationStatus;
}
```

Relación:

```text
WithdrawalHistory 1 ─── N WithdrawalHistoryEntry
```

Los nombres definitivos podrán ajustarse al código real.

---

## 11. Persistencia Room

La versión añadirá tablas orientativas:

```text
withdrawal_history
withdrawal_history_entries
```

Reglas:

- migración explícita;
- ausencia de migración destructiva;
- conservación de toda la mercadería;
- creación atómica de cabecera y líneas;
- rollback completo ante error;
- `orderIndex` para conservar el orden;
- entidades Room fuera de la UI;
- mapeo a modelos de dominio;
- índices solo para consultas reales.

---

## 12. Arquitectura prevista

La versión añadirá:

```text
feature/
└── withdrawal_history/
    ├── create/
    ├── list/
    ├── detail/
    └── common/
```

Componentes orientativos:

```text
domain/history/
├── WithdrawalHistory.java
├── WithdrawalHistoryEntry.java
├── WithdrawalHistoryDraft.java
└── WithdrawalHistoryValidationResult.java
```

Flujo:

```text
Activity
    ↓
ViewModel
    ↓
WithdrawalHistorySaveService
    ↓
WithdrawalHistoryRepository
    ↓
Room
```

La implementación deberá mantener:

- Room fuera de Activities;
- validación fuera de DAO;
- navegación fuera de ViewModels;
- entidades Room fuera de la feature;
- historial separado de `WarehouseItem`;
- servicios solo cuando representen operaciones reales.

---

## 13. Integración con las listas

El flujo de v1.2 se ampliará:

```text
capturar o seleccionar
        ↓
OCR
        ↓
revisar referencias
        ↓
consultar ubicaciones
        ↓
Registrar historial
        ↓
revisar título, cantidad y unidad
        ↓
confirmar
        ↓
Room
```

El historial no se guardará automáticamente al confirmar referencias.

Para referencias encontradas se conservarán la identidad, cantidad y unidad opcionales, sitio, posición y estado `FOUND`.

Para referencias no encontradas se conservarán la identidad, cantidad y unidad opcionales y estado `NOT_FOUND`.

---

## 14. Historias de usuario de la versión 1.3

### HU-26 — Preparar el modelo histórico y la migración Room

Como usuario, quiero que el historial se conserve localmente sin afectar mis datos actuales.

Incluye cabecera, líneas, relación uno a muchos, DAO, transacción, repositorio, migración y pruebas.

---

### HU-27 — Capturar título, cantidad y unidad

Como usuario, quiero completar los datos documentales de la lista.

Incluye título opcional, fecha y hora, cantidad y unidad opcionales, propuesta OCR, edición manual, validación y conservación del orden.

**Dependencia:** HU-26.

---

### HU-28 — Registrar una lista confirmada

Como usuario, quiero guardar una lista revisada para conservar qué mercadería fue sacada y dónde se encontraba.

Incluye confirmación, instantánea de ubicación, referencias encontradas y no encontradas, guardado atómico y prevención de guardados duplicados.

**Dependencias:** HU-26 y HU-27.

---

### HU-29 — Consultar el historial

Como usuario, quiero ver las listas registradas.

Incluye listado por fecha descendente, título, fecha, número de referencias, resumen, estado vacío y actualización después de guardar o eliminar.

**Dependencia:** HU-28.

---

### HU-30 — Consultar el detalle histórico

Como usuario, quiero abrir una lista guardada para revisar sus referencias, cantidades y ubicaciones históricas.

Incluye cabecera, líneas ordenadas, cantidad, unidad, ubicación histórica y estado no encontrado.

**Dependencia:** HU-29.

---

### HU-31 — Buscar y filtrar el historial

Como usuario, quiero encontrar rápidamente una lista anterior.

Incluye búsqueda por título, categoría y código; intervalo de fechas; combinación de criterios; estado sin resultados; limpieza de filtros.

**Dependencia:** HU-29.

---

### HU-32 — Eliminar un registro histórico

Como usuario, quiero eliminar una lista histórica incorrecta.

Incluye confirmación, eliminación transaccional de cabecera y líneas, conservación de mercadería y actualización del listado.

**Dependencias:** HU-29 y HU-30.

---

## 15. Orden de implementación

```text
HU-26 — Modelo histórico y migración
        ↓
HU-27 — Datos documentales
        ↓
HU-28 — Guardar historial
        ↓
HU-29 — Listado
        ↓
HU-30 — Detalle
        ↓
HU-31 — Búsqueda y filtros
        ↓
HU-32 — Eliminación
```

---

## 16. Pantalla de preparación

Nombre orientativo:

```text
WithdrawalHistoryCreateActivity
```

Mostrará título, fecha, referencias, cantidad, unidad, ubicación, estado y acción Guardar.

No permitirá modificar sitio, posición, mercadería o stock.

---

## 17. Listado histórico

Nombre orientativo:

```text
WithdrawalHistoryListActivity
```

Ejemplo:

```text
Reposición tienda centro
1 ago 2026 · 14:30
15 referencias · 13 encontradas · 2 no encontradas
```

Orden predeterminado:

```text
registeredAt DESC
id DESC
```

---

## 18. Detalle histórico

Nombre orientativo:

```text
WithdrawalHistoryDetailActivity
```

Ejemplo:

```text
MR · 1210A
4 CAJAS
Sitio A1 · Posición 2
```

No encontrada:

```text
MZ · 1300C
2 PCS
No encontrada al registrar la lista
```

---

## 19. Ubicación histórica y actual

La interfaz deberá distinguir:

```text
Ubicación al registrar la lista
```

de:

```text
Mercadería actual
```

Si la mercadería se elimina posteriormente, el historial seguirá conservando su información documental.

---

## 20. Búsqueda y filtros

Se admitirá:

- título parcial;
- categoría;
- código;
- fecha inicial;
- fecha final.

Room deberá resolver las consultas fuera del hilo principal.

Los criterios se conservarán tras rotación.

---

## 21. Eliminación

La eliminación requerirá confirmación:

```text
Se eliminará esta lista y todas sus líneas.
La mercadería almacenada no se modificará.
```

La operación será transaccional.

---

## 22. Copias de seguridad

La versión deberá revisar el formato versionado de backup.

La release deberá decidir explícitamente si el historial se incorpora a las copias.

La opción recomendada será incluirlo manteniendo compatibilidad con copias anteriores, validación previa y restauración transaccional.

Si la ampliación supera el alcance previsto, deberá documentarse como historia adicional y no modificarse silenciosamente.

---

## 23. Privacidad

La versión deberá:

- procesar localmente;
- no conservar fotografías;
- no guardar texto OCR completo;
- guardar solo datos confirmados;
- no solicitar Internet;
- no enviar historial;
- evitar contenido sensible en logs;
- permitir eliminar registros.

---

## 24. Accesibilidad

Se verificarán:

- títulos descriptivos;
- etiquetas de título, cantidad y unidad;
- errores asociados;
- orden de foco;
- objetivos táctiles;
- contraste;
- tamaño de texto;
- estados encontrado/no encontrado;
- fechas legibles;
- mensajes no dependientes del color.

---

## 25. Estados de interfaz

Creación:

```text
IDLE
READY
SAVING
SAVED
INVALID_INPUT
ERROR
```

Listado:

```text
LOADING
CONTENT
EMPTY
NO_RESULTS
ERROR
```

Detalle:

```text
LOADING
CONTENT
NOT_FOUND
ERROR
```

---

## 26. Pruebas principales

### Validación

- título vacío;
- cantidad vacía;
- cantidad positiva;
- cero o negativa;
- unidad vacía;
- normalización;
- encontradas y no encontradas;
- orden.

### Room

- migración desde v1.2;
- datos conservados;
- inserción transaccional;
- rollback;
- relación uno a muchos;
- búsqueda;
- fechas;
- eliminación en cascada;
- mercadería no modificada.

### ViewModels y servicios

- guardado único;
- errores;
- rotación;
- reintento;
- entrada inválida;
- historial vacío;
- detalle inexistente;
- eliminación.

### Manuales

- lista sin título;
- con y sin cantidades;
- referencias no encontradas;
- cambio posterior de ubicación;
- eliminación posterior de mercadería;
- funcionamiento offline;
- accesibilidad;
- backup anterior.

---

## 27. Riesgos

### Confusión con stock

**Mitigación:** modelo separado, textos explícitos y ausencia de operaciones sobre existencias.

### Cantidades OCR incorrectas

**Mitigación:** revisión manual.

### Pérdida de ubicación histórica

**Mitigación:** instantánea de sitio y posición.

### Migración defectuosa

**Mitigación:** pruebas de migración y ausencia de migración destructiva.

### Historial duplicado

**Mitigación:** bloqueo de doble pulsación y evento único.

### Restauración incompleta

**Mitigación:** formato versionado y decisión explícita sobre backup.

---

## 28. Fuera del objetivo de v1.3

```text
stock actual
entradas y salidas contables
reservas
pedidos
devoluciones
usuarios
roles
servidor
sincronización
OCR remoto
ONNX
```

---

## 29. Definición de terminado

La versión estará terminada cuando:

- exista un modelo histórico separado;
- Room migre sin perder datos;
- puedan añadirse título, cantidad y unidad;
- se guarde fecha y hora;
- se conserve una instantánea de ubicación;
- se admitan referencias no encontradas;
- el guardado sea transaccional;
- exista listado y detalle;
- existan búsqueda y filtros;
- pueda eliminarse un registro;
- la mercadería no se modifique;
- no se gestione stock;
- funcione sin conexión;
- las pruebas, lint, build y CI sean satisfactorios;
- se publique `v1.3.0`.

---

## 30. Resultado esperado

```text
fotografía o imagen
        ↓
OCR local
        ↓
revisión
        ↓
ubicaciones
        ↓
datos documentales
        ↓
historial local
```

La siguiente versión prevista será:

```text
v1.4.0 — Evaluación y evolución del OCR local mediante PP-OCRv5 y ONNX Runtime
```
