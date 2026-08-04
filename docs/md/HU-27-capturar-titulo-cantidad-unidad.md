# HU-27 — Capturar título, cantidad y unidad documentales

> Segunda historia de usuario de AlmacenTracker v1.3.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.3  
**Versión Android:** 1.3.0  
**Historia:** HU-27  
**Nombre:** Capturar título, cantidad y unidad documentales  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-27-datos-documentales-lista`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-26 — Preparar el modelo histórico y la migración Room  
**Issue prevista:** `#31`

---

## 2. Historia de usuario

Como usuario,  
quiero completar el título, la cantidad y la unidad de una lista procesada,  
para revisar sus datos documentales antes de registrarla en el historial.

---

## 3. Objetivo

Preparar una lista localizada para su futuro registro histórico mediante una pantalla editable que permita revisar:

```text
título opcional de la lista
+
fecha y hora propuestas
+
cantidad opcional por referencia
+
unidad opcional por referencia
```

Flujo previsto:

```text
ReferenceListLocationActivity
        ↓ acción Registrar historial
WithdrawalHistoryCreateActivity
        ↓
título + fecha y hora
        ↓
referencias localizadas en su orden actual
        ↓
cantidad y unidad propuestas o vacías
        ↓
revisión y edición manual
        ↓
borrador documental válido
```

HU-27 deberá producir un borrador documental preparado para HU-28.

HU-27 no guardará todavía el historial en Room.

---

## 4. Regla principal

La cantidad y la unidad tendrán carácter exclusivamente documental.

```text
categoría + código
        → identidad de la mercadería

cantidad + unidad
        → información de la lista
```

Ejemplo:

```text
MR 1210 — 4 CAJAS
```

La identidad continuará siendo:

```text
MR + 1210
```

La cantidad `4` y la unidad `CAJAS` no deberán:

- modificar `WarehouseItem`;
- descontar stock;
- incrementar existencias;
- participar en la identidad;
- alterar sitio o posición;
- provocar escrituras en Room durante HU-27.

---

## 5. Documentos y código de referencia

HU-27 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.3-general-plan.md`;
- `HU-26-preparar-modelo-historico-migracion-room.md`;
- el estado real de `AlmacenTrackerHU26.zip`;
- la arquitectura MVVM pragmática organizada por funcionalidades;
- Room como fuente local de verdad;
- el funcionamiento completamente sin conexión;
- la separación entre mercadería e historial documental;
- la identidad `categoría + código`;
- el orden confirmado de la lista;
- la instantánea de ubicación preparada por el modelo histórico;
- la regla de no gestionar stock;
- la política de crear componentes únicamente cuando aporten una responsabilidad real.

El plan de v1.3 asigna a HU-27:

```text
título opcional
+
fecha y hora
+
cantidad opcional
+
unidad opcional
+
propuesta OCR cuando exista
+
edición manual
+
validación
+
conservación del orden
```

---

## 6. Estado real antes de HU-27

El análisis de `AlmacenTrackerHU26.zip` confirma que el proyecto se encuentra en:

```groovy
versionCode 4
versionName "1.3.0"
```

La rama incluida es:

```text
develop
```

HU-26 ya proporciona:

```text
domain/history/
├── WithdrawalHistory.java
├── WithdrawalHistoryEntry.java
├── WithdrawalHistoryRecord.java
└── WithdrawalLocationStatus.java
```

También proporciona:

- `WithdrawalHistoryEntity`;
- `WithdrawalHistoryEntryEntity`;
- `WithdrawalHistoryDao`;
- `WithdrawalHistoryWithEntries`;
- `WithdrawalHistoryRoomMapper`;
- `WithdrawalHistoryRepository`;
- `RoomWithdrawalHistoryRepository`;
- base de datos Room en versión `2`;
- migración `MIGRATION_1_2`;
- tablas `withdrawal_history` y `withdrawal_history_entries`;
- composición del repositorio histórico en `AppContainer`;
- pruebas de dominio, DAO, repositorio y migración.

El flujo de listas actual proporciona:

- `ReferenceListReviewActivity`;
- `WarehouseReferenceIntentContract`;
- `ReferenceListLocationActivity`;
- `ReferenceListLocationViewModel`;
- `ReferenceListLocationService`;
- `ReferenceListLocationResult`;
- una colección ordenada de `WarehouseReferenceLocation`;
- estado `FOUND` o `NOT_FOUND`;
- id de mercadería cuando existe;
- sitio y posición cuando existen.

Sin embargo, el contrato actual entre revisión y ubicaciones conserva únicamente:

```text
categoría + código
```

El procesamiento de v1.2 descarta deliberadamente cantidades y unidades para localizar mercadería.

Por tanto, antes de HU-27 no existen:

- pantalla de preparación histórica;
- borrador documental de interfaz;
- título editable;
- fecha y hora editables o confirmables;
- cantidad por línea;
- unidad por línea;
- asociación estable entre una propuesta documental y una ubicación;
- parser documental de cantidad y unidad;
- contrato que preserve propuestas OCR de cantidad y unidad;
- validación específica de datos documentales;
- estado de ViewModel para preparación histórica;
- navegación desde ubicaciones hacia preparación histórica.

---

## 7. Decisión crítica sobre las propuestas OCR

### 7.1. Limitación actual

HU-27 no deberá fingir que puede recuperar cantidades desde:

```text
WarehouseReferenceLocation
```

porque ese modelo contiene ubicación e identidad, pero no conserva el texto OCR original.

Tampoco deberá intentar deducir cantidades desde:

- código;
- categoría;
- sitio;
- posición;
- número de repeticiones;
- orden de la lista.

### 7.2. Estrategia requerida

Para ofrecer propuestas OCR reales se deberá ampliar el flujo documental antes de perder el contexto de la línea reconocida.

Flujo conceptual:

```text
línea OCR reconstruida
        ↓
referencia reconocida
        +
propuesta documental opcional
        ↓
revisión
        ↓
ubicaciones
        ↓
preparación histórica
```

La ampliación deberá conservar, por cada referencia:

```text
categoría
código
cantidad opcional
unidad opcional
orden documental
```

No deberá conservar:

- Bitmap;
- URI de imagen;
- clases de ML Kit;
- texto OCR completo sin necesidad;
- coordenadas si ya no aportan valor a HU-27.

### 7.3. Degradación segura

Cuando el flujo no disponga de propuesta OCR:

```text
quantity = null
unit = null
```

La pantalla deberá continuar plenamente operativa mediante edición manual.

La ausencia de propuesta no será un error.

---

## 8. Alcance incluido

HU-27 incluye:

- añadir una acción `Registrar historial` desde la pantalla de ubicaciones;
- mostrar la acción únicamente cuando exista un resultado válido;
- crear una pantalla específica de preparación histórica;
- recibir las referencias localizadas en su orden actual;
- conservar referencias `FOUND` y `NOT_FOUND`;
- conservar el id de mercadería cuando exista;
- conservar sitio y posición como datos preparados para instantánea;
- mostrar un título opcional;
- permitir escribir, editar y eliminar el título;
- normalizar título vacío a `null`;
- proponer fecha y hora actuales;
- mostrar la fecha y hora de forma legible;
- permitir confirmar la fecha propuesta;
- permitir ajustar fecha y hora si el alcance técnico se mantiene pequeño;
- conservar el instante como `long`;
- mostrar una fila por referencia;
- mostrar categoría y código;
- mostrar sitio y posición cuando existan;
- mostrar estado no encontrado cuando corresponda;
- conservar el orden documental;
- mostrar cantidad opcional;
- mostrar unidad opcional;
- aplicar propuesta OCR cuando exista información fiable;
- dejar cantidad y unidad vacías cuando no exista propuesta;
- permitir introducir cantidad manualmente;
- permitir corregir cantidad;
- permitir eliminar cantidad;
- permitir introducir unidad manualmente;
- permitir corregir unidad;
- permitir eliminar unidad;
- normalizar unidad a mayúsculas con `Locale.ROOT`;
- rechazar cantidades inválidas;
- admitir únicamente enteros positivos cuando exista cantidad;
- no exigir cantidad;
- no exigir unidad;
- permitir cantidad sin unidad;
- permitir unidad sin cantidad únicamente si se justifica durante implementación; de lo contrario, limpiar o marcar la unidad;
- crear un borrador documental independiente de Room;
- validar el borrador antes de continuar;
- conservar el borrador ante rotación;
- evitar aplicar dos veces los datos iniciales;
- evitar navegación repetida;
- preparar un contrato pequeño para HU-28;
- mantener Room sin cambios;
- mantener la mercadería sin cambios;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas unitarias;
- pruebas de parser documental;
- pruebas de ViewModel;
- pruebas de contrato de navegación;
- pruebas de Activity cuando aporten valor;
- CI.

---

## 9. Alcance excluido

HU-27 no incluye:

- insertar historial en Room;
- llamar a `WithdrawalHistoryRepository.insert(...)`;
- crear ids históricos;
- guardar automáticamente al abrir la pantalla;
- crear cabecera o líneas persistidas;
- mostrar listado histórico;
- mostrar detalle histórico;
- buscar historial;
- filtrar historial;
- eliminar historial;
- modificar la migración Room de HU-26;
- añadir nuevas tablas;
- modificar `WarehouseItemEntity`;
- modificar sitio o posición de mercadería;
- registrar stock;
- descontar cantidades;
- sumar cantidades repetidas;
- interpretar tallas;
- asociar clientes;
- guardar fotografías;
- guardar texto OCR completo;
- reprocesar la imagen;
- volver a ejecutar OCR;
- corregir perspectiva;
- incorporar ONNX Runtime;
- incorporar PP-OCRv5;
- exportar historial;
- modificar todavía el formato de copia de seguridad;
- sincronización remota;
- autenticación;
- backend.

El guardado funcional y transaccional pertenece a HU-28.

---

## 10. Modelo documental temporal

HU-27 necesita un modelo que represente los datos preparados antes de persistir.

Nombre recomendado:

```text
WithdrawalHistoryDraft
```

Estructura conceptual:

```text
WithdrawalHistoryDraft
├── title
├── registeredAt
└── entries
```

Cada línea:

```text
WithdrawalHistoryDraftEntry
├── orderIndex
├── category
├── code
├── quantity
├── unit
├── warehouseItemIdSnapshot
├── siteSnapshot
├── positionSnapshot
└── locationStatus
```

Este modelo deberá:

- ser Java puro;
- no depender de Android;
- no depender de Room;
- no contener ids históricos persistidos;
- conservar una copia defensiva de las líneas;
- conservar el orden;
- permitir datos opcionales;
- representar exactamente lo que HU-28 deberá convertir en `WithdrawalHistoryRecord`.

### Decisión de alcance

No se recomienda reutilizar directamente:

```text
WithdrawalHistoryRecord
```

como estado editable de la pantalla.

Motivos:

- `WithdrawalHistoryRecord` representa un agregado listo para persistencia;
- sus timestamps deben ser válidos;
- sus líneas ya responden a invariantes históricas;
- el formulario necesita estados parciales e inválidos durante la edición;
- la UI no debería construir entidades de persistencia progresivamente.

---

## 11. Modelo editable por fila

La feature podrá utilizar un modelo de presentación:

```text
WithdrawalHistoryDraftEntryUiModel
```

Datos orientativos:

```text
stableId
orderIndex
category
code
quantityText
unitText
warehouseItemIdSnapshot
siteSnapshot
positionSnapshot
locationStatus
quantityError
unitError
```

Reglas:

- `stableId` será temporal;
- no será id de Room;
- `quantityText` permitirá representar entrada incompleta;
- el dominio recibirá `Integer` solo después de validar;
- la ubicación no será editable;
- categoría y código no serán editables en esta pantalla;
- el estado de localización no será editable;
- la fila permanecerá asociada a su referencia mediante su posición o id temporal.

---

## 12. Captura del título

### 12.1. Reglas

El título será opcional.

Normalización:

```text
null → null
trim()
vacío → null
texto no vacío → conservar
```

Ejemplos válidos:

```text
Reposición tienda centro
Lista de Ana
Proveedor Norte
```

No se aplicará mayúscula automática.

No se intentará reconocer el título mediante OCR en HU-27 salvo que el flujo actual ya disponga de una propuesta claramente separada de las referencias.

### 12.2. Longitud

Se deberá definir un límite razonable de interfaz y dominio.

Recomendación:

```text
máximo 120 caracteres
```

El límite definitivo deberá ser coherente entre:

- layout;
- ViewModel;
- validación;
- pruebas.

Room no impone por sí solo este límite, por lo que debe validarse antes de HU-28.

---

## 13. Fecha y hora

### 13.1. Valor inicial

La pantalla propondrá:

```text
System.currentTimeMillis()
```

al inicializar un borrador nuevo.

Se aplicará una sola vez.

No deberá reemplazarse tras:

- rotación;
- regreso desde un diálogo;
- edición de una cantidad;
- cambio de unidad.

### 13.2. Edición

La fecha y hora podrán mostrarse mediante:

- campo no editable con acción Cambiar; o
- controles Material Date Picker y Time Picker.

No se utilizará texto libre para introducir timestamps.

### 13.3. Regla temporal

`registeredAt` representará el momento documental elegido por el usuario.

No será todavía:

```text
createdAt
updatedAt
```

HU-28 asignará los timestamps de creación local al persistir.

### 13.4. Validación

El instante deberá ser mayor que cero.

No se bloqueará automáticamente una fecha pasada.

Una fecha futura podrá rechazarse o advertirse según la decisión de UX, pero la regla deberá ser consistente y estar probada.

Para mantener el alcance, se recomienda:

```text
no permitir un instante futuro posterior al margen de reloj razonable
```

sin introducir lógica compleja de zona horaria.

---

## 14. Cantidad documental

### 14.1. Formato inicial

HU-27 admitirá:

```text
entero positivo
```

Ejemplos válidos:

```text
1
4
20
250
```

Ejemplos inválidos:

```text
0
-3
1.5
2,5
4-6
cuatro
```

### 14.2. Campo opcional

Entrada vacía:

```text
quantity = null
```

No se mostrará error por dejar el campo vacío.

### 14.3. Límite

Se deberá evitar desbordamiento y entradas absurdamente largas.

Recomendación:

```text
máximo 9 dígitos
valor <= Integer.MAX_VALUE
```

La validación deberá capturar errores sin lanzar una excepción visible.

### 14.4. Teclado

El campo utilizará entrada numérica positiva.

No deberá impedir pegar texto, pero deberá validarlo.

---

## 15. Unidad documental

### 15.1. Naturaleza

La unidad será opcional y se conservará como texto.

Ejemplos:

```text
PCS
CAJAS
PAQUETES
UDS
BULTOS
```

### 15.2. Normalización

```text
null → null
trim()
vacío → null
uppercase(Locale.ROOT)
reducir espacios internos repetidos
```

No se deberá traducir automáticamente una unidad.

Ejemplo:

```text
" cajas " → "CAJAS"
```

### 15.3. Longitud

Recomendación:

```text
máximo 30 caracteres
```

### 15.4. Catálogo

HU-27 no creará una tabla de unidades.

La interfaz podrá ofrecer sugerencias comunes mediante un dropdown local, pero deberá permitir texto manual.

No se añadirá una entidad Room solo para esta historia.

---

## 16. Relación entre cantidad y unidad

La combinación recomendada será:

| Cantidad | Unidad | Resultado |
|---:|---|---|
| Vacía | Vacía | Válido |
| Informada | Vacía | Válido |
| Informada | Informada | Válido |
| Vacía | Informada | Requiere decisión explícita |

Para evitar un dato difícil de interpretar, se recomienda que:

```text
unidad informada + cantidad vacía
```

produzca un error de validación:

```text
Introduce una cantidad o elimina la unidad.
```

Esta regla deberá estar centralizada y no quedar únicamente en el adapter.

---

## 17. Extracción de propuestas desde OCR

### 17.1. Objetivo limitado

La extracción documental deberá buscar únicamente patrones sencillos y revisables.

Ejemplos:

```text
MR 1210 - 20 PCS
MR 1210 4 CAJAS
MR 1210A - 3 PAQUETES
```

Propuestas:

```text
MR 1210 → 20 / PCS
MR 1210 → 4 / CAJAS
MR 1210A → 3 / PAQUETES
```

### 17.2. Sin inferencias agresivas

No se deberá inferir cantidad cuando exista ambigüedad.

Ejemplo:

```text
MR 1210 2026
```

El valor `2026` podría ser:

- cantidad;
- fecha;
- lote;
- texto ajeno.

Resultado seguro:

```text
sin propuesta documental
```

### 17.3. Asociación

La propuesta deberá asociarse a la referencia detectada en la misma línea o contexto inmediato justificable.

No se asignará una cantidad global a todas las referencias.

No se asociará por simple cercanía de índices después de reordenamientos que hayan perdido el vínculo original.

### 17.4. Unidades conocidas

La detección podrá reutilizar el vocabulario ya utilizado para excluir unidades durante el parser de referencias:

```text
PC
PCS
PZ
PZA
PZAS
PIEZA
PIEZAS
PQT
PQTS
PAQUETE
PAQUETES
UD
UDS
UNIDAD
UNIDADES
CJ
CJA
CAJA
CAJAS
BTO
BULTO
BULTOS
PACK
PACKS
BOX
BOXES
CTN
CTNS
```

La lista deberá permanecer en un componente reutilizable, no duplicada en dos parsers.

### 17.5. Resultado del parser

Modelo orientativo:

```text
DocumentReferenceDataProposal
├── reference
├── quantity
├── unit
├── sourceLineIndex
└── sourceText opcional y limitado
```

Este modelo será temporal.

No se persistirá en Room.

---

## 18. Conservación del orden

HU-27 deberá recibir y mostrar las líneas en el mismo orden de `ReferenceListLocationActivity`.

No se ordenará por:

- categoría;
- código;
- cantidad;
- unidad;
- sitio;
- posición;
- estado.

`orderIndex` deberá generarse mediante la posición actual:

```text
0, 1, 2, ...
```

La edición de cantidad o unidad no alterará ese índice.

---

## 19. Referencias encontradas y no encontradas

### `FOUND`

La fila deberá conservar:

```text
warehouseItemIdSnapshot
siteSnapshot
positionSnapshot
locationStatus = FOUND
```

### `NOT_FOUND`

La fila deberá conservar:

```text
warehouseItemIdSnapshot = null
siteSnapshot = null
positionSnapshot = null
locationStatus = NOT_FOUND
```

Ambos tipos permitirán cantidad y unidad documentales.

Una referencia no encontrada no deberá eliminarse automáticamente del borrador.

---

## 20. Pantalla de preparación histórica

Nombre recomendado:

```text
WithdrawalHistoryCreateActivity
```

La pantalla deberá incluir:

- Toolbar;
- navegación Atrás;
- explicación breve;
- campo Título opcional;
- fecha y hora;
- acción Cambiar fecha y hora cuando se implemente;
- contador de referencias;
- RecyclerView;
- cantidad por fila;
- unidad por fila;
- sitio y posición no editables;
- estado no encontrada;
- estado de validación;
- acción Continuar;
- acción Cancelar o navegación Atrás.

Texto orientativo:

```text
Completa los datos de la lista antes de registrarla.
Las cantidades son documentales y no modifican el stock.
```

HU-27 no deberá mostrar todavía:

```text
Historial guardado
```

porque no existe persistencia en esta historia.

---

## 21. Fila de preparación

Cada fila mostrará:

```text
MR · 1210A
Sitio A1 · Posición 2
Cantidad [    ]
Unidad   [    ]
```

No encontrada:

```text
MZ · 1300C
No encontrada al procesar la lista
Cantidad [    ]
Unidad   [    ]
```

Reglas:

- categoría y código serán de solo lectura;
- ubicación será de solo lectura;
- cantidad será editable;
- unidad será editable;
- los errores se mostrarán junto al campo correspondiente;
- el reciclado de filas no deberá intercambiar valores;
- los listeners deberán actualizar mediante id temporal o posición estable;
- no se dependerá únicamente de `adapterPosition` después de cambios de lista.

---

## 22. Estado de interfaz

Estado orientativo:

```text
WithdrawalHistoryCreateUiState
├── status
├── title
├── registeredAt
├── entries
├── titleError
├── dateError
├── invalidEntryCount
└── canContinue
```

Estados:

```text
INITIALIZING
READY
INVALID_INPUT
ERROR
```

No se añadirá `SAVING` en HU-27 porque todavía no se persiste.

Podrá existir un estado breve `PREPARING_RESULT` si la construcción del borrador se realiza fuera del renderizado, pero no deberá introducirse sin necesidad.

---

## 23. ViewModel

Nombre recomendado:

```text
WithdrawalHistoryCreateViewModel
```

Responsabilidades:

- recibir la lista localizada una sola vez;
- crear filas editables;
- asignar fecha y hora iniciales;
- aplicar título;
- aplicar cantidad;
- aplicar unidad;
- normalizar datos cuando corresponda;
- validar campos;
- conservar estado ante recreación;
- construir `WithdrawalHistoryDraft`;
- emitir un evento único de continuación;
- no navegar;
- no mostrar diálogos;
- no acceder a Room;
- no llamar todavía al repositorio histórico;
- no depender de `Intent`;
- no depender de `Context`.

Métodos orientativos:

```text
initialize(...)
onTitleChanged(...)
onRegisteredAtChanged(...)
onQuantityChanged(stableId, value)
onUnitChanged(stableId, value)
continueToConfirmation()
```

---

## 24. Servicio de preparación

Se podrá crear:

```text
WithdrawalHistoryDraftService
```

solo si concentra responsabilidades reales como:

- normalización de título;
- validación de fecha;
- parseo de cantidades;
- normalización de unidades;
- construcción del borrador;
- transformación de ubicaciones en líneas históricas temporales.

No se creará un servicio que únicamente invoque un constructor.

La validación no deberá quedar duplicada entre:

- Activity;
- adapter;
- ViewModel;
- HU-28.

---

## 25. Integración arquitectónica

Estructura orientativa:

```text
com.rndymi.almacentracker/
├── domain/
│   └── history/
│       ├── WithdrawalHistoryDraft.java
│       ├── WithdrawalHistoryDraftEntry.java
│       └── WithdrawalHistoryDraftValidator.java
│
├── feature/
│   └── withdrawal_history/
│       ├── common/
│       │   └── WithdrawalHistoryDraftContract.java
│       └── create/
│           ├── WithdrawalHistoryCreateActivity.java
│           ├── WithdrawalHistoryCreateAdapter.java
│           ├── WithdrawalHistoryCreateUiState.java
│           ├── WithdrawalHistoryCreateViewModel.java
│           └── WithdrawalHistoryCreateViewModelFactory.java
│
└── feature/
    └── reference_list/
        └── common/
            └── DocumentReferenceDataProposal.java
```

La estructura definitiva deberá adaptarse al código real.

### Regla crítica

No deberán crearse simultáneamente:

```text
Draft
Form
Input
Command
Request
Payload
```

para representar los mismos datos.

Se elegirá el mínimo conjunto de modelos que mantenga:

- claridad;
- estado editable;
- dominio independiente;
- contrato seguro hacia HU-28.

---

## 26. Navegación desde ubicaciones

`ReferenceListLocationActivity` añadirá una acción:

```text
Registrar historial
```

Disponibilidad:

```text
SUCCESS con al menos una referencia
```

No estará disponible en:

```text
LOADING
INVALID_INPUT
ERROR
```

Flujo:

1. el usuario consulta las ubicaciones;
2. pulsa Registrar historial;
3. se construye una copia ordenada del resultado;
4. se abre `WithdrawalHistoryCreateActivity`;
5. la pantalla prepara título, fecha, cantidades y unidades;
6. el usuario revisa los datos;
7. pulsa Continuar;
8. HU-27 genera un borrador válido;
9. todavía no se escribe en Room.

Durante HU-27, la acción final podrá:

- devolver el borrador mediante un resultado de prueba; o
- mostrar una confirmación temporal de que está preparado.

La integración definitiva con persistencia pertenecerá a HU-28.

---

## 27. Contrato de entrada

El contrato deberá transportar únicamente datos pequeños.

No se enviarán:

- imágenes;
- texto OCR completo;
- entidades Room;
- ViewModels;
- objetos de ML Kit;
- excepciones;
- repositorios.

Opciones válidas:

1. representación compacta mediante `ArrayList<String>`;
2. DTO `Parcelable` pequeño;
3. almacén temporal del flujo con ciclo de vida controlado.

Para aproximadamente quince referencias, un DTO pequeño o una representación compacta será suficiente.

El contrato deberá conservar:

```text
orderIndex
category
code
quantityProposal opcional
unitProposal opcional
warehouseItemIdSnapshot opcional
siteSnapshot opcional
positionSnapshot opcional
locationStatus
```

No se utilizarán listas paralelas que puedan perder sincronización.

---

## 28. Contrato de salida hacia HU-28

HU-27 deberá producir:

```text
WithdrawalHistoryDraft
```

El resultado deberá garantizar:

- título normalizado;
- fecha válida;
- al menos una línea;
- orden consecutivo;
- referencias válidas;
- cantidades nulas o positivas;
- unidades normalizadas;
- instantáneas coherentes con `FOUND` o `NOT_FOUND`.

No deberá asignar:

```text
historyId
entryId
createdAt
updatedAt
```

Estos valores corresponderán al guardado de HU-28.

---

## 29. Validación

### Título

- opcional;
- espacios externos eliminados;
- vacío convertido a `null`;
- longitud máxima respetada.

### Fecha

- obligatoria;
- mayor que cero;
- propuesta una sola vez;
- sin texto libre.

### Cantidad

- opcional;
- entero positivo;
- cero inválido;
- negativo inválido;
- decimal inválido;
- desbordamiento controlado.

### Unidad

- opcional;
- mayúsculas;
- espacios normalizados;
- longitud máxima;
- no válida sin cantidad si se adopta la regla recomendada.

### Lista

- al menos una referencia;
- orden conservado;
- sin entradas nulas;
- instantánea coherente.

---

## 30. Flujo principal

1. El usuario procesa y revisa una lista.
2. Consulta las ubicaciones.
3. Pulsa Registrar historial.
4. Se abre la pantalla de preparación.
5. La fecha y hora actuales se proponen automáticamente.
6. El usuario introduce un título opcional.
7. La pantalla muestra las referencias en orden.
8. Las propuestas OCR de cantidad y unidad se muestran cuando existan.
9. Los campos sin propuesta permanecen vacíos.
10. El usuario corrige, completa o elimina datos documentales.
11. Pulsa Continuar.
12. El ViewModel valida título, fecha y líneas.
13. Se construye un `WithdrawalHistoryDraft`.
14. Se emite un único evento de continuación.
15. Room permanece sin cambios.

---

## 31. Flujos alternativos

### FA-01 — Lista sin título

1. El usuario deja el título vacío.
2. Se normaliza a `null`.
3. El borrador continúa siendo válido.

### FA-02 — Línea sin cantidad

1. Una referencia no dispone de cantidad.
2. El campo queda vacío.
3. Se conserva `quantity = null`.

### FA-03 — Cantidad manual

1. No existe propuesta OCR.
2. El usuario introduce `4`.
3. Se conserva `quantity = 4`.

### FA-04 — Cantidad inválida

1. El usuario introduce `0`, `-2` o `1.5`.
2. La fila muestra un error.
3. No se genera el borrador hasta corregir o vaciar.

### FA-05 — Unidad normalizada

1. El usuario introduce ` cajas `.
2. Se normaliza a `CAJAS`.
3. El borrador conserva el valor normalizado.

### FA-06 — Unidad sin cantidad

1. El usuario introduce `PCS` y deja cantidad vacía.
2. Se muestra la regla acordada.
3. Debe introducir cantidad o eliminar unidad.

### FA-07 — Referencia no encontrada

1. Una referencia tiene estado `NOT_FOUND`.
2. Se muestra sin ubicación.
3. El usuario puede completar cantidad y unidad.
4. La línea permanece en el borrador.

### FA-08 — Cambio de fecha

1. El usuario cambia fecha u hora.
2. El ViewModel conserva el nuevo instante.
3. Una rotación no lo reemplaza.

### FA-09 — Rotación

1. El usuario ya editó varias líneas.
2. El dispositivo rota.
3. Título, fecha, cantidades y unidades permanecen.
4. Los datos iniciales no se reaplican.

### FA-10 — Entrada malformada

1. La Activity recibe una colección vacía o inválida.
2. Muestra un error controlado.
3. No permite continuar.
4. Room no se consulta ni modifica.

### FA-11 — Sin propuesta OCR

1. El flujo solo contiene categoría y código.
2. Cantidad y unidad aparecen vacías.
3. La pantalla sigue siendo funcional.

### FA-12 — Cancelación

1. El usuario vuelve atrás.
2. El borrador se descarta.
3. No se crea historial.
4. Room permanece sin cambios.

---

## 32. Criterios de aceptación

### CA-01 — Acceso desde ubicaciones

**Dado** un resultado válido de ubicaciones,  
**cuando** el usuario pulsa Registrar historial,  
**entonces** se abre la preparación documental con las referencias en el mismo orden.

### CA-02 — Título opcional

**Dado** que el usuario no introduce título,  
**cuando** valida el formulario,  
**entonces** el borrador conserva `title = null` sin mostrar error.

### CA-03 — Fecha inicial

**Dado** un borrador nuevo,  
**cuando** se abre la pantalla,  
**entonces** se propone una fecha y hora válidas una sola vez.

### CA-04 — Cantidad opcional

**Dado** que una referencia no tiene cantidad,  
**cuando** el usuario continúa,  
**entonces** la línea conserva `quantity = null`.

### CA-05 — Cantidad positiva

**Dado** que el usuario introduce una cantidad,  
**cuando** se valida,  
**entonces** solo se acepta un entero positivo.

### CA-06 — Unidad opcional

**Dado** que la unidad está vacía,  
**cuando** se construye el borrador,  
**entonces** se conserva `unit = null`.

### CA-07 — Normalización de unidad

**Dado** el texto ` cajas `,  
**cuando** se valida,  
**entonces** el borrador conserva `CAJAS`.

### CA-08 — Propuesta OCR

**Dado** que el flujo conserva una propuesta OCR fiable,  
**cuando** se abre la preparación,  
**entonces** cantidad y unidad aparecen prellenadas pero continúan siendo editables.

### CA-09 — Ausencia de propuesta

**Dado** que no existe información OCR documental,  
**cuando** se abre la preparación,  
**entonces** los campos aparecen vacíos sin bloquear el flujo.

### CA-10 — Orden conservado

**Dado** el orden confirmado de referencias,  
**cuando** se prepara el borrador,  
**entonces** cada línea conserva su `orderIndex`.

### CA-11 — Referencia encontrada

**Dado** un resultado `FOUND`,  
**cuando** se prepara la línea,  
**entonces** se conservan id, sitio y posición como instantánea no editable.

### CA-12 — Referencia no encontrada

**Dado** un resultado `NOT_FOUND`,  
**cuando** se prepara la línea,  
**entonces** se conserva sin id ni ubicación y puede contener datos documentales.

### CA-13 — Sin persistencia

**Dado** cualquier edición realizada en HU-27,  
**cuando** el usuario continúa o cancela,  
**entonces** no se insertan registros en Room.

### CA-14 — Recreación

**Dado** un borrador editado,  
**cuando** la Activity se recrea,  
**entonces** no se pierden ni reaplican los datos.

### CA-15 — Sin stock

**Dado** que una línea contiene cantidad y unidad,  
**cuando** se prepara el borrador,  
**entonces** la mercadería existente no se modifica.

---

## 33. Pruebas unitarias

### Modelos y validación

- título nulo;
- título vacío;
- título normalizado;
- título demasiado largo;
- fecha positiva;
- fecha inválida;
- cantidad nula;
- cantidad positiva;
- cantidad cero;
- cantidad negativa;
- decimal;
- desbordamiento;
- unidad nula;
- unidad vacía;
- unidad en minúsculas;
- unidad demasiado larga;
- unidad sin cantidad;
- lista vacía;
- referencia `FOUND` válida;
- referencia `NOT_FOUND` válida;
- orden conservado.

### Parser documental

- `MR 1210 - 20 PCS`;
- `MR 1210 4 CAJAS`;
- `MR 1210A - 3 PAQUETES`;
- línea sin cantidad;
- número ambiguo;
- varias referencias en una línea;
- unidad desconocida;
- espacios Unicode;
- mayúsculas y minúsculas;
- ausencia de inferencia agresiva.

### ViewModel

- inicialización única;
- fecha inicial única;
- actualización de título;
- actualización de cantidad;
- actualización de unidad;
- error de cantidad;
- error de unidad;
- conservación de otras filas;
- evento único de continuación;
- entrada vacía;
- recreación;
- cancelación sin persistencia.

---

## 34. Pruebas de interfaz e integración

- navegación desde ubicaciones;
- acción no disponible durante error;
- prellenado de propuestas;
- filas sin propuesta;
- teclado numérico;
- reciclado de RecyclerView;
- errores por fila;
- selector de fecha y hora;
- rotación con teclado abierto;
- texto largo;
- modo claro y oscuro;
- tamaño de fuente grande;
- referencia no encontrada;
- botón Continuar deshabilitado con errores;
- Room sin nuevas filas después de completar HU-27.

---

## 35. Accesibilidad

HU-27 deberá verificar:

- título de pantalla descriptivo;
- explicación de que no se modifica stock;
- etiquetas persistentes para Título, Cantidad y Unidad;
- asociación de errores con el campo correspondiente;
- orden de foco por fila;
- navegación predecible entre filas;
- botones con área táctil mínima;
- fecha y hora anunciadas de forma comprensible;
- estado `No encontrada` anunciado como texto;
- ubicación no dependiente del color;
- soporte para tamaño de texto;
- contenido de RecyclerView estable para lectores de pantalla;
- descripciones de acciones Cambiar fecha y Continuar.

---

## 36. Privacidad y seguridad

HU-27 deberá mantener:

- funcionamiento sin conexión;
- sin permiso de Internet;
- sin guardar fotografías;
- sin guardar texto OCR completo;
- sin enviar datos;
- sin logs con listas completas;
- sin datos sensibles innecesarios;
- sin escritura en Room;
- sin modificación de mercadería;
- validación de todos los datos de entrada;
- contratos pequeños y defensivos.

---

## 37. Riesgos

### Pérdida de cantidades antes de HU-27

**Riesgo:** el contrato actual conserva únicamente categoría y código.

**Mitigación:** ampliar el contrato documental desde el punto donde todavía existe contexto OCR y permitir siempre edición manual.

### Asociación incorrecta de cantidad

**Riesgo:** asignar una cantidad a la referencia equivocada.

**Mitigación:** asociación limitada a la misma línea o contexto inequívoco; sin propuesta ante ambigüedad.

### Confusión con stock

**Riesgo:** el usuario interpreta la cantidad como existencia.

**Mitigación:** texto explícito, modelos separados y ausencia de escritura sobre mercadería.

### Pérdida de datos por RecyclerView

**Riesgo:** valores intercambiados al reciclar filas.

**Mitigación:** estado central en ViewModel y actualización mediante identificador estable.

### Sobrearquitectura

**Riesgo:** crear demasiados DTO para el mismo borrador.

**Mitigación:** un modelo editable de presentación y un único modelo de dominio de salida.

### Fecha reaplicada

**Riesgo:** cambiar el instante después de rotación.

**Mitigación:** inicialización idempotente en ViewModel.

### Validaciones divergentes

**Riesgo:** HU-27 acepta algo que HU-28 rechaza.

**Mitigación:** centralizar normalización y validación en dominio o servicio reutilizable.

---

## 38. Definición de terminado

HU-27 estará terminada cuando:

- exista una acción para preparar el historial desde ubicaciones;
- exista una pantalla de preparación histórica;
- el título sea opcional y editable;
- la fecha y hora se propongan y conserven;
- cada referencia mantenga su orden;
- cantidad y unidad sean opcionales;
- las propuestas OCR se apliquen únicamente cuando existan datos fiables;
- la ausencia de propuesta permita edición manual;
- las cantidades se validen como enteros positivos;
- las unidades se normalicen;
- las referencias encontradas conserven su instantánea;
- las no encontradas permanezcan en el borrador;
- la UI no permita modificar identidad ni ubicación;
- se genere un `WithdrawalHistoryDraft` válido;
- no se inserte historial en Room;
- no se modifique mercadería;
- no se gestione stock;
- el estado sobreviva a recreación;
- las pruebas sean satisfactorias;
- lint y build sean satisfactorios;
- CI sea satisfactoria.

---

## 39. Resultado esperado

Al cerrar HU-27:

```text
lista localizada
        ↓
título opcional
        ↓
fecha y hora
        ↓
cantidades y unidades revisables
        ↓
instantáneas de ubicación
        ↓
WithdrawalHistoryDraft válido
```

Room seguirá sin recibir el historial.

La siguiente historia será:

```text
HU-28 — Registrar una lista confirmada en el historial
```
