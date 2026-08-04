# HU-25 — Mostrar ubicaciones de una lista reconocida

> Octava historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-25  
**Nombre:** Mostrar ubicaciones de una lista reconocida  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-25-mostrar-ubicaciones-lista`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-24 — Revisar y corregir referencias reconocidas  
**Issue prevista:** `#28`

---

## 2. Historia de usuario

Como usuario,  
quiero consultar conjuntamente las referencias que confirmé,  
para saber en qué sitio y posición se encuentra cada mercancía de la lista.

---

## 3. Objetivo

Conectar la lista confirmada en HU-24 con la mercancía almacenada localmente en Room y presentar el resultado respetando el orden de la lista.

Flujo previsto:

```text
ReferenceListReviewActivity
        ↓ lista confirmada
ReferenceListLocationActivity
        ↓
ReferenceListLocationViewModel
        ↓
ReferenceListLocationService
        ↓
WarehouseItemRepository
        ↓
Room
        ↓
ubicaciones encontradas y referencias no encontradas
```

Resultado visual:

```text
MR 1210A
Sitio A1 · Posición 2

MZ 1300C
Sitio B3

MI 900
No encontrada
```

HU-25 cerrará el flujo funcional de listas de v1.2:

```text
capturar o seleccionar
        ↓
OCR
        ↓
revisar
        ↓
confirmar
        ↓
mostrar ubicaciones
```

---

## 4. Regla principal

La consulta se realizará siempre mediante la identidad funcional:

```text
categoría + código
```

No se buscará únicamente por código.

Ejemplo:

```text
MR + 1050
MD + 1050
```

son dos mercancías diferentes.

La lista confirmada por HU-24 ya contiene referencias normalizadas y sin duplicados. HU-25 deberá conservar esa identidad y no volver a interpretar el texto OCR.

---

## 5. Referencias del proyecto

La HU-25 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-24-revisar-corregir-referencias-reconocidas.md`;
- el estado real de `AlmacenTrackerHU24.zip`;
- Room como fuente de verdad;
- la identidad `categoría + código`;
- el orden confirmado por el usuario;
- la deduplicación realizada en HU-24;
- la regla de oro de v1.2: identificar mercancía y mostrar su ubicación;
- el funcionamiento completamente sin conexión;
- la ausencia de historial persistente;
- la ausencia de gestión de stock;
- la arquitectura MVVM organizada por funcionalidades;
- la política de no crear abstracciones sin responsabilidad real.

El plan de v1.2 asigna a HU-25:

- consulta conjunta por categoría y código;
- conservación del orden de la hoja;
- presentación de sitio y posición;
- identificación de referencias no encontradas;
- acceso al detalle;
- Room como fuente de verdad;
- ausencia de historial persistente.

---

## 6. Estado real antes de HU-25

El análisis de `AlmacenTrackerHU24.zip` confirma que el proyecto ya dispone de:

```text
domain/reference/
├── WarehouseReference.java
├── WarehouseReferenceMatch.java
└── WarehouseReferenceParser.java
```

```text
feature/reference_list/review/
├── ReferenceInputResult.java
├── ReferenceListReviewActivity.java
├── ReferenceListReviewAdapter.java
├── ReferenceListReviewUiState.java
├── ReferenceListReviewViewModel.java
├── ReferenceListReviewViewModelFactory.java
└── ReferenceProposal.java
```

HU-24 ya permite:

- recibir líneas OCR;
- extraer referencias;
- separar categoría y código;
- corregir;
- añadir;
- eliminar;
- validar;
- deduplicar;
- conservar el orden;
- confirmar una colección de `WarehouseReference`;
- devolver la colección mediante Activity Result API.

El contrato actual utiliza:

```text
EXTRA_CONFIRMED_REFERENCES
```

con una representación compacta de:

```text
category + separador + code
```

La `ReferenceListCaptureActivity` recibe actualmente la lista confirmada y muestra únicamente una notificación temporal con el número de referencias.

El inventario ya dispone de:

- `WarehouseItem`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- `WarehouseItemDao`;
- `findAllByCode(...)`;
- `findById(...)`;
- navegación a `ItemDetailActivity`;
- executor de repositorio;
- mapeo Room ↔ dominio;
- pruebas DAO y repositorio.

Sin embargo, el proyecto todavía no dispone de:

- consulta exacta por categoría y código para una lista;
- servicio de localización por lote;
- resultado encontrado/no encontrado por referencia;
- pantalla de ubicaciones;
- acceso al detalle desde una fila de lista;
- reintento de consulta conjunta;
- resumen de resultados.

HU-25 añadirá esas capacidades sin modificar el esquema Room.

---

## 7. Alcance incluido

HU-25 incluye:

- recibir una colección confirmada de `WarehouseReference`;
- rechazar entrada nula o vacía;
- conservar el orden de entrada;
- consultar cada referencia por categoría y código exactos;
- comparar sin distinguir mayúsculas y minúsculas;
- conservar ceros iniciales;
- ejecutar la operación fuera del hilo principal;
- exponer una única operación de repositorio para la lista completa;
- evitar que la UI coordine múltiples callbacks;
- clasificar cada referencia como encontrada o no encontrada;
- asociar una mercancía encontrada con su id;
- mostrar categoría y código;
- mostrar sitio;
- mostrar posición cuando exista;
- ocultar la posición cuando sea nula o vacía;
- mostrar claramente “No encontrada”;
- mantener las referencias no encontradas en su posición original;
- mostrar todas las referencias, no solo las encontradas;
- mostrar resumen de encontradas y no encontradas;
- permitir abrir `ItemDetailActivity` desde una referencia encontrada;
- impedir abrir detalle desde una referencia no encontrada;
- permitir reintentar cuando Room falle;
- conservar el resultado ante rotación;
- evitar consultas duplicadas;
- evitar navegaciones repetidas;
- no modificar la mercancía;
- no crear registros automáticamente;
- no abrir el formulario de alta desde esta historia;
- no guardar la lista;
- no guardar la imagen;
- no crear historial;
- no interpretar cantidades;
- no modificar stock;
- funcionar completamente sin conexión;
- accesibilidad;
- pruebas DAO;
- pruebas de repositorio;
- pruebas del servicio;
- pruebas del ViewModel;
- pruebas de Activity;
- pruebas de integración;
- CI.

---

## 8. Alcance excluido

HU-25 no incluye:

- persistir la lista procesada;
- crear historial de mercancía sacada;
- guardar título;
- guardar fecha y hora de retirada;
- guardar cantidades;
- guardar unidades;
- interpretar piezas, paquetes o cajas;
- interpretar tallas;
- asociar clientes;
- descontar stock;
- incrementar o reducir cantidades;
- marcar mercancía como retirada;
- actualizar sitio o posición;
- registrar movimientos;
- crear mercancía no encontrada;
- editar mercancía desde la lista;
- búsqueda parcial;
- sugerencias aproximadas;
- autocorrección de referencias;
- volver a ejecutar OCR;
- conservar la fotografía;
- superponer ubicaciones sobre la imagen;
- exportar el resultado;
- compartir el resultado;
- sincronización remota;
- autenticación;
- backend.

El historial documental de mercancía sacada pertenecerá a v1.3.

---

## 9. Entrada válida

HU-25 recibirá una lista:

```text
List<WarehouseReference>
```

Condiciones esperadas por el contrato:

- al menos una referencia;
- categoría normalizada;
- código normalizado;
- referencias válidas;
- referencias únicas;
- orden confirmado.

Ejemplo:

```text
0 → MR + 1210A
1 → MZ + 1300C
2 → MI + 900
```

HU-25 no deberá depender ciegamente del origen.

Antes de consultar deberá:

- ignorar entradas nulas;
- rechazar categoría o código vacíos;
- evitar una caída por contrato malformado;
- conservar las referencias válidas restantes;
- mostrar un error de entrada si no queda ninguna.

No deberá volver a ejecutar `WarehouseReferenceParser` sobre referencias ya estructuradas.

---

## 10. Consulta exacta

La consulta individual conceptual será:

```sql
SELECT *
FROM warehouse_items
WHERE category = :category COLLATE NOCASE
  AND code = :code COLLATE NOCASE
LIMIT 1
```

La combinación es única en Room, por lo que cada referencia podrá producir:

```text
0 mercancías
        o
1 mercancía
```

No deberá producir varias coincidencias válidas.

No se utilizará:

```text
LIKE
findAllByCode(...)
search(...)
```

como sustitución de la consulta compuesta.

Motivo:

```text
code = 1050
```

puede pertenecer a varias categorías.

HU-25 ya conoce la categoría confirmada y debe utilizarla.

---

## 11. Estrategia de consulta por lote

La Activity y el ViewModel no deberán lanzar una consulta independiente por cada fila.

Se añadirá una operación de repositorio:

```text
findAllByReferences(
    List<WarehouseReference>,
    RepositoryCallback<List<WarehouseReferenceLocation>>
)
```

o un contrato equivalente.

La implementación podrá realizar internamente una consulta exacta por cada referencia dentro del mismo trabajo del executor.

Para el alcance previsto de aproximadamente hasta 15 referencias:

- la solución es sencilla;
- cada consulta utiliza la combinación única;
- no bloquea la UI;
- mantiene el orden;
- evita SQL dinámico innecesario;
- evita exponer Room a la feature.

La operación deberá producir un único callback final.

No se permitirá:

```text
ViewModel
    → 15 llamadas independientes
    → 15 estados parciales
    → 15 navegaciones posibles
```

Si en el futuro el volumen crece significativamente, podrá optimizarse la implementación interna mediante una consulta por lote sin cambiar el contrato de aplicación.

---

## 12. DAO

Se añadirá una consulta síncrona exacta:

```text
findByCategoryAndCode(
    String category,
    String code
)
```

Resultado:

```text
WarehouseItemEntity nullable
```

Consulta orientativa:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "WHERE category = :category COLLATE NOCASE " +
    "AND code = :code COLLATE NOCASE " +
    "LIMIT 1"
)
WarehouseItemEntity findByCategoryAndCode(
        String category,
        String code
);
```

La consulta se ejecutará únicamente desde el executor del repositorio.

No se añadirá `LiveData` porque la pantalla representa una consulta puntual de una lista confirmada.

No se requiere una migración Room.

---

## 13. Modelo de resultado por referencia

Modelo recomendado:

```text
WarehouseReferenceLocation
├── reference
├── status
├── warehouseItemId
├── site
└── position
```

Estados:

```text
FOUND
NOT_FOUND
```

### FOUND

Contendrá:

- referencia;
- id de mercancía;
- sitio;
- posición opcional.

### NOT_FOUND

Contendrá:

- referencia;
- sin id;
- sin sitio;
- sin posición.

Reglas:

- modelo Java puro;
- sin `Context`;
- sin tipos Room;
- sin recursos Android;
- sin entidad Room expuesta;
- inmutable;
- validación de invariantes.

Ejemplo de invariante:

```text
FOUND
→ warehouseItemId válido
→ site no vacío

NOT_FOUND
→ sin warehouseItemId utilizable
```

---

## 14. Resultado completo

Modelo orientativo:

```text
ReferenceListLocationResult
├── locations
├── foundCount
├── notFoundCount
└── totalCount
```

La colección deberá:

- conservar el orden de entrada;
- ser inmutable o defensivamente copiada;
- contener una fila por referencia;
- no eliminar resultados no encontrados;
- no reordenarse por sitio;
- no reordenarse por estado.

Ejemplo:

```text
entrada
1. MR1210A
2. MZ1300C
3. MI900

Room
MR1210A → A1 / 2
MZ1300C → no existe
MI900 → C4

salida
1. MR1210A → A1 / 2
2. MZ1300C → NOT_FOUND
3. MI900 → C4
```

---

## 15. Servicio de localización

Se recomienda crear:

```text
ReferenceListLocationService
```

Responsabilidades:

- validar la colección de entrada;
- conservar el orden;
- delegar la operación por lote al repositorio;
- transformar el resultado en un modelo completo;
- calcular contadores;
- convertir errores técnicos en un error de aplicación;
- no depender de Android;
- no navegar;
- no construir mensajes visuales.

El servicio representa una operación real de aplicación:

```text
localizar una lista confirmada
```

No se reutilizará `WarehouseItemCodeSearchService`, porque ese servicio:

- busca solo por código;
- admite varias categorías;
- resuelve una interacción individual;
- produce estados diferentes.

HU-25 conoce categoría y código y necesita una respuesta ordenada para varias referencias.

---

## 16. Contrato del repositorio

El repositorio podrá ampliarse con:

```java
void findAllByReferences(
        List<WarehouseReference> references,
        RepositoryCallback<
                List<WarehouseReferenceLocation>
        > callback
);
```

La implementación `RoomWarehouseItemRepository` deberá:

1. validar callback y colección;
2. copiar defensivamente la entrada;
3. ejecutar un único trabajo en su executor;
4. recorrer en orden;
5. llamar a `findByCategoryAndCode(...)`;
6. mapear la entidad encontrada;
7. crear `NOT_FOUND` cuando la consulta devuelva `null`;
8. devolver una lista completa;
9. ejecutar un único `onSuccess`;
10. ejecutar `onError` si Room falla;
11. no devolver resultados parciales después de un error.

Una lista vacía podrá devolverse como éxito vacío a nivel de repositorio.

La validación funcional de “debe existir al menos una referencia” pertenecerá al servicio o ViewModel.

---

## 17. Tratamiento de errores

### Referencia no encontrada

No es un error técnico.

```text
NOT_FOUND
```

deberá mostrarse como una fila normal del resultado.

### Error de Room

Es un error técnico de la operación completa.

Mensaje:

```text
No se pudieron consultar las ubicaciones.
```

Acciones:

```text
Reintentar
Volver
```

No se mostrará:

- excepción;
- consulta SQL;
- nombre de tabla;
- stack trace;
- ruta de base de datos.

### Entrada vacía o inválida

Mensaje:

```text
No hay referencias válidas para consultar.
```

Acción:

```text
Volver
```

No se ejecutará Room.

---

## 18. Pantalla de ubicaciones

Nombre recomendado:

```text
ReferenceListLocationActivity
```

La pantalla deberá incluir:

- Toolbar;
- resumen;
- RecyclerView;
- progreso inicial;
- estado de error;
- acción Reintentar;
- estado vacío de entrada;
- navegación Atrás.

Cada fila encontrada mostrará:

```text
MR · 1210A
Sitio A1 · Posición 2
```

Sin posición:

```text
MZ · 1300C
Sitio B3
```

No encontrada:

```text
MI · 900
No encontrada
```

Las referencias no encontradas deberán diferenciarse mediante:

- texto explícito;
- icono opcional;
- estilo visual accesible.

No se dependerá únicamente del color.

---

## 19. Resumen

La pantalla mostrará un resumen, por ejemplo:

```text
12 referencias
10 encontradas · 2 no encontradas
```

Casos:

### Todas encontradas

```text
15 encontradas
```

### Algunas no encontradas

```text
12 encontradas · 3 no encontradas
```

### Ninguna encontrada

```text
0 encontradas · 5 no encontradas
```

No se ocultará la lista cuando ninguna exista en Room.

El usuario necesita saber exactamente qué referencias no pudieron localizarse.

---

## 20. Orden de presentación

La presentación conservará el orden confirmado en HU-24.

No se ordenará por:

- categoría;
- código;
- sitio;
- posición;
- estado;
- id Room.

Motivo:

el orden original ayuda a recorrer físicamente la lista o compararla con el documento.

La deduplicación ya se realizó en HU-24.

HU-25 podrá realizar una defensa adicional contra duplicados malformados, pero no deberá cambiar silenciosamente el orden válido.

---

## 21. Acceso al detalle

Una fila `FOUND` será seleccionable.

Flujo:

```text
usuario pulsa MR 1210A
        ↓
ItemDetailActivity.createIntent(context, id)
        ↓
detalle existente
```

El detalle continuará mostrando:

- categoría;
- código;
- sitio;
- posición;
- observaciones;
- fechas;
- acciones disponibles.

Una fila `NOT_FOUND`:

- no será seleccionable;
- no abrirá un id inválido;
- podrá anunciar que no existe en el almacén.

HU-25 no añadirá una acción automática para registrar mercancía faltante.

---

## 22. Navegación entre revisión y ubicaciones

El flujo más coherente será:

```text
ReferenceListCaptureActivity
        ↓
ReferenceListReviewActivity
        ↓ Confirmar
ReferenceListLocationActivity
```

La pantalla de revisión deberá permanecer en la pila mientras se muestran las ubicaciones.

Así, al pulsar Atrás desde ubicaciones:

```text
ReferenceListLocationActivity
        ↓
ReferenceListReviewActivity
```

el usuario podrá corregir la lista y confirmar nuevamente.

Esta navegación es preferible al comportamiento temporal actual:

```text
ReviewActivity
    → devuelve resultado
CaptureActivity
    → muestra Snackbar
```

HU-25 deberá sustituir esa confirmación temporal por navegación real.

El contrato de resultado de HU-24 podrá conservarse si sigue siendo útil para pruebas o reutilización, pero no deberá forzar un recorrido incómodo.

No se duplicará la consulta al regresar desde el detalle.

---

## 23. Contrato de navegación

`ReferenceListLocationActivity` expondrá:

```text
createIntent(
    Context,
    List<WarehouseReference>
)
```

La transferencia podrá reutilizar la representación compacta ya utilizada por HU-24.

Para evitar dos implementaciones incompatibles de codificación, se recomienda extraer un contrato compartido:

```text
feature/reference_list/common/
└── WarehouseReferenceIntentContract.java
```

Responsabilidades:

- codificar una lista;
- decodificar una lista;
- conservar orden;
- ignorar entradas malformadas;
- no validar reglas OCR;
- no acceder a Room.

Este componente tiene una responsabilidad real porque el mismo formato será utilizado por:

- revisión;
- ubicaciones.

No deberá ubicarse en dominio, porque depende del contrato Android de `Intent`.

No se enviarán:

- entidades Room;
- imágenes;
- `Bitmap`;
- tipos ML Kit;
- grafos serializados grandes.

---

## 24. Estado de interfaz

Estado orientativo:

```text
ReferenceListLocationUiState.Status
├── IDLE
├── LOADING
├── CONTENT
├── INVALID_INPUT
└── ERROR
```

Datos:

```text
locations
foundCount
notFoundCount
message
canRetry
```

### IDLE

Entrada todavía no aplicada.

### LOADING

Consulta local activa.

### CONTENT

Contiene todas las filas encontradas y no encontradas.

### INVALID_INPUT

No existe una colección válida.

### ERROR

Room no pudo completar la consulta.

No se necesita un estado separado para:

```text
ALL_FOUND
PARTIALLY_FOUND
NONE_FOUND
```

porque puede derivarse de los contadores sin multiplicar estados.

---

## 25. ViewModel

Nombre recomendado:

```text
ReferenceListLocationViewModel
```

Responsabilidades:

- aplicar la lista inicial una sola vez;
- iniciar la consulta;
- bloquear llamadas duplicadas;
- delegar al servicio;
- publicar `LOADING`;
- publicar `CONTENT`;
- publicar `INVALID_INPUT`;
- publicar `ERROR`;
- conservar el resultado ante rotación;
- permitir reintento;
- mantener la entrada necesaria para reintentar;
- no navegar;
- no construir Intents;
- no acceder directamente al DAO;
- no depender de Views o Context.

Métodos orientativos:

```text
applyInitialReferences(references)
retry()
```

La entrada inicial no deberá reaplicarse después de una recreación.

---

## 26. Activity

`ReferenceListLocationActivity` será responsable de:

- decodificar el Intent mediante el contrato compartido;
- crear ViewModel;
- aplicar la entrada una sola vez;
- observar estado;
- renderizar progreso;
- renderizar contenido;
- renderizar error;
- mostrar resumen;
- reaccionar a Reintentar;
- abrir detalle mediante id;
- no consultar repositorio directamente;
- no alterar la colección;
- no modificar mercancía.

La Activity no deberá:

- recorrer referencias y lanzar consultas;
- mapear entidades Room;
- decidir reglas de identidad;
- calcular duplicados;
- guardar historial.

---

## 27. Adapter

Nombre recomendado:

```text
ReferenceListLocationAdapter
```

Responsabilidades:

- mostrar filas ordenadas;
- diferenciar encontrada/no encontrada;
- ocultar posición vacía;
- emitir clic solo para `FOUND`;
- usar `ListAdapter` y `DiffUtil` si encaja con el proyecto;
- proporcionar descripciones accesibles.

Identidad visual de fila:

```text
category + code
```

No se utilizará el índice como única identidad estable si existe una clave funcional disponible.

---

## 28. Conservación ante recreación

Después de rotación deberán conservarse:

- lista de resultados;
- orden;
- contadores;
- estado de error;
- referencias originales para reintentar.

No deberá:

- volver a consultar si ya existe `CONTENT`;
- duplicar la lista;
- abrir nuevamente el detalle;
- perder filas no encontradas;
- reaplicar el Intent y sobrescribir estado.

Si la Activity es recreada durante `LOADING`, el ViewModel continuará coordinando una única operación.

---

## 29. Actualización de datos durante la pantalla

HU-25 será una consulta puntual.

Si una mercancía cambia en otra pantalla mientras la lista de ubicaciones permanece abierta:

- el resultado actual podrá mantenerse;
- el usuario podrá volver y confirmar de nuevo o utilizar Reintentar cuando esté disponible;
- no es obligatorio observar continuamente cada fila.

Esta decisión evita:

- múltiples observadores;
- complejidad de combinación de LiveData;
- cambios de orden inesperados;
- una feature reactiva desproporcionada para una lista temporal.

Una futura versión podrá añadir refresco explícito si aparece la necesidad.

---

## 30. Privacidad y persistencia

HU-25 deberá mantener:

- Room como única fuente de mercancía;
- funcionamiento offline;
- sin permiso de Internet;
- sin envío de referencias;
- sin almacenamiento de la lista;
- sin almacenamiento de la imagen;
- sin logs con el contenido completo;
- sin historial persistente;
- sin cambios en stock;
- sin cambios en la entidad de mercancía;
- sin migración Room.

La consulta de ubicaciones no constituye una retirada ni un movimiento de inventario.

---

## 31. Accesibilidad

La pantalla deberá incluir:

- título descriptivo;
- resumen anunciado;
- progreso con descripción;
- fila encontrada con categoría, código y ubicación;
- fila no encontrada con estado explícito;
- área táctil suficiente;
- navegación accesible;
- contraste en modo claro y oscuro;
- soporte para escalado de texto;
- orden de foco coherente;
- Reintentar identificable;
- no depender únicamente de iconos o color.

Descripción orientativa:

```text
Referencia MR 1210A, sitio A1, posición 2.
```

Sin posición:

```text
Referencia MZ 1300C, sitio B3.
```

No encontrada:

```text
Referencia MI 900, no encontrada.
```

---

## 32. Flujo principal

1. El usuario revisa las referencias.
2. Pulsa Confirmar.
3. La pantalla de ubicaciones recibe la lista.
4. El ViewModel aplica la entrada una sola vez.
5. El servicio valida la colección.
6. El repositorio ejecuta la operación en su executor.
7. Room consulta cada categoría y código.
8. Se construye una fila por referencia.
9. Se conserva el orden.
10. El ViewModel publica `CONTENT`.
11. La Activity muestra resumen y ubicaciones.
12. El usuario pulsa una mercancía encontrada.
13. Se abre el detalle mediante id.
14. Room no se modifica.

---

## 33. Flujos alternativos

### FA-01 — Todas encontradas

1. Se confirman cinco referencias.
2. Room contiene las cinco.
3. Se muestran cinco ubicaciones.
4. El resumen indica cinco encontradas.

### FA-02 — Algunas no encontradas

1. Se confirman cinco referencias.
2. Room contiene tres.
3. Se muestran cinco filas.
4. Dos indican No encontrada.
5. El orden permanece.

### FA-03 — Ninguna encontrada

1. Se confirman tres referencias.
2. Room no contiene ninguna.
3. Se muestran tres filas no encontradas.
4. No se presenta como error técnico.

### FA-04 — Posición vacía

1. Una mercancía tiene sitio `A1`.
2. Su posición es nula o vacía.
3. La fila muestra únicamente `Sitio A1`.

### FA-05 — Mismo código en categorías distintas

1. La lista contiene `MR + 1050` y `MD + 1050`.
2. Room contiene ambas.
3. Cada una muestra su propia ubicación.

### FA-06 — Código con ceros iniciales

1. La referencia es `MR + 001050`.
2. Room contiene exactamente ese código.
3. Se encuentra sin convertirlo a número.

### FA-07 — Referencia inexistente

1. La referencia no existe.
2. Se crea `NOT_FOUND`.
3. No se abre un id incorrecto.

### FA-08 — Error de Room

1. Room produce una excepción.
2. No se presentan resultados parciales.
3. Se muestra un error controlado.
4. El usuario puede reintentar.

### FA-09 — Reintento

1. La consulta falló.
2. El usuario pulsa Reintentar.
3. Se reutiliza la lista original.
4. Se inicia una única operación.

### FA-10 — Doble pulsación

1. El usuario pulsa Reintentar varias veces.
2. Solo se inicia una consulta.

### FA-11 — Rotación durante carga

1. La consulta está activa.
2. La Activity se recrea.
3. No se inicia otra operación.
4. El resultado se publica una vez.

### FA-12 — Rotación con contenido

1. Las ubicaciones ya se muestran.
2. El dispositivo rota.
3. La lista y el resumen permanecen.
4. Room no se consulta nuevamente.

### FA-13 — Abrir detalle

1. El usuario pulsa una fila encontrada.
2. Se abre el id correcto.
3. Al volver, la lista permanece.

### FA-14 — Pulsar no encontrada

1. El usuario pulsa una fila `NOT_FOUND`.
2. No se abre detalle.
3. La pantalla permanece estable.

### FA-15 — Volver a corregir

1. El usuario detecta una referencia equivocada.
2. Pulsa Atrás.
3. Regresa a revisión.
4. Corrige y confirma otra vez.
5. Se ejecuta una nueva consulta con la lista corregida.

### FA-16 — Modo avión

1. El dispositivo no tiene conexión.
2. Se confirma la lista.
3. Room resuelve las ubicaciones localmente.

---

## 34. Criterios de aceptación

### CA-01 — Consulta por identidad completa

**Dado** una referencia confirmada,  
**cuando** se consulta Room,  
**entonces** se utilizan categoría y código.

### CA-02 — No buscar solo por código

**Dado** el mismo código en categorías distintas,  
**cuando** se procesa la lista,  
**entonces** cada categoría se resuelve de forma independiente.

### CA-03 — Orden conservado

**Dado** el orden confirmado en HU-24,  
**cuando** se muestran resultados,  
**entonces** las filas mantienen ese orden.

### CA-04 — Referencia encontrada

**Dado** que Room contiene la combinación,  
**cuando** finaliza la consulta,  
**entonces** se muestran sitio y posición cuando exista.

### CA-05 — Posición opcional

**Dado** que una mercancía no tiene posición,  
**cuando** se presenta,  
**entonces** se muestra el sitio sin un marcador vacío.

### CA-06 — Referencia no encontrada

**Dado** que Room no contiene la combinación,  
**cuando** finaliza la consulta,  
**entonces** la referencia permanece en la lista como No encontrada.

### CA-07 — Algunas no encontradas

**Dado** un resultado mixto,  
**cuando** se presenta,  
**entonces** se muestran juntas las encontradas y no encontradas respetando el orden.

### CA-08 — Ninguna encontrada

**Dado** que no existe ninguna referencia,  
**cuando** finaliza la consulta,  
**entonces** se muestran todas como no encontradas y no como error técnico.

### CA-09 — Resumen

**Dado** el resultado completo,  
**cuando** se muestra la pantalla,  
**entonces** se indican total, encontradas y no encontradas.

### CA-10 — Ceros iniciales

**Dado** `001050`,  
**cuando** se consulta,  
**entonces** se conserva como texto y no se transforma en `1050`.

### CA-11 — Detalle

**Dado** una fila encontrada,  
**cuando** el usuario la pulsa,  
**entonces** se abre el detalle de su id.

### CA-12 — Sin detalle para inexistente

**Dado** una fila no encontrada,  
**cuando** el usuario la pulsa,  
**entonces** no se navega a un detalle inválido.

### CA-13 — Error controlado

**Dado** que Room falla,  
**cuando** la consulta no puede completarse,  
**entonces** se muestra Reintentar sin detalles técnicos.

### CA-14 — Operación única

**Dado** que una consulta está activa,  
**cuando** ocurre una recreación o doble acción,  
**entonces** no se inicia otra operación simultánea.

### CA-15 — Rotación

**Dado** que ya existen resultados,  
**cuando** la Activity se recrea,  
**entonces** lista, orden y resumen permanecen.

### CA-16 — Corrección posterior

**Dado** que el usuario vuelve desde ubicaciones,  
**cuando** regresa a revisión,  
**entonces** puede modificar la lista y consultar nuevamente.

### CA-17 — Sin modificación de Room

**Dado** cualquier resultado,  
**cuando** finaliza HU-25,  
**entonces** no se crea, actualiza ni elimina mercancía.

### CA-18 — Sin historial

**Dado** una lista localizada,  
**cuando** se cierra el flujo,  
**entonces** no queda registrada como historial.

### CA-19 — Funcionamiento offline

**Dado** el dispositivo en modo avión,  
**cuando** se consultan ubicaciones,  
**entonces** el flujo funciona mediante Room.

### CA-20 — Integración completa

**Dado** una imagen con referencias,  
**cuando** el usuario captura, revisa, confirma y consulta,  
**entonces** obtiene una lista de ubicaciones sin salir del flujo de AlmacenTracker.

---

## 35. Diseño técnico propuesto

### Dominio / aplicación

```text
domain/reference/
├── WarehouseReference.java
└── WarehouseReferenceLocation.java
```

```text
feature/reference_list/location/
├── ReferenceListLocationService.java
└── ReferenceListLocationResult.java
```

Si el proyecto evoluciona inmediatamente hacia la arquitectura hexagonal prevista, el servicio podrá ubicarse en:

```text
application/service/reference/
```

No se realizará una migración completa de paquetes dentro de HU-25.

La prioridad será respetar la estructura real de v1.2 y mantener las dependencias limpias.

### Feature

```text
feature/reference_list/location/
├── ReferenceListLocationActivity.java
├── ReferenceListLocationAdapter.java
├── ReferenceListLocationUiState.java
├── ReferenceListLocationViewModel.java
└── ReferenceListLocationViewModelFactory.java
```

### Contrato compartido

```text
feature/reference_list/common/
└── WarehouseReferenceIntentContract.java
```

Solo se extraerá si elimina la codificación duplicada existente.

---

## 36. Archivos previstos

Archivos probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── domain/reference/
│   └── WarehouseReferenceLocation.java
├── feature/reference_list/common/
│   └── WarehouseReferenceIntentContract.java
└── feature/reference_list/location/
    ├── ReferenceListLocationActivity.java
    ├── ReferenceListLocationAdapter.java
    ├── ReferenceListLocationResult.java
    ├── ReferenceListLocationService.java
    ├── ReferenceListLocationUiState.java
    ├── ReferenceListLocationViewModel.java
    └── ReferenceListLocationViewModelFactory.java
```

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── app/AppContainer.java
├── app/di/ReferenceListModule.java
├── data/local/room/dao/WarehouseItemDao.java
├── data/repository/WarehouseItemRepository.java
├── data/repository/RoomWarehouseItemRepository.java
├── feature/reference_list/capture/
│   └── ReferenceListCaptureActivity.java
└── feature/reference_list/review/
    └── ReferenceListReviewActivity.java
```

Recursos probables:

```text
app/src/main/res/
├── layout/activity_reference_list_location.xml
├── layout/item_reference_location.xml
├── values/strings.xml
└── drawable/
```

Manifest:

```text
ReferenceListLocationActivity
```

Pruebas probables:

```text
app/src/test/java/com/rndymi/almacentracker/
├── domain/reference/
│   └── WarehouseReferenceLocationTest.java
└── feature/reference_list/location/
    ├── ReferenceListLocationResultTest.java
    ├── ReferenceListLocationServiceTest.java
    ├── ReferenceListLocationUiStateTest.java
    └── ReferenceListLocationViewModelTest.java
```

```text
app/src/androidTest/java/com/rndymi/almacentracker/
├── data/local/room/dao/
│   └── WarehouseItemDaoTest.java
└── feature/reference_list/location/
    └── ReferenceListLocationActivityContractTest.java
```

No se prevén cambios en:

- entidad `WarehouseItemEntity`;
- esquema Room;
- versión de base de datos;
- migraciones;
- CameraX;
- ML Kit Barcode Scanning;
- ML Kit Text Recognition;
- permisos;
- `ScannerActivity`;
- `ItemFormActivity`;
- CSV;
- backup;
- importación o exportación.

---

## 37. Pruebas

### DAO

- encuentra categoría y código exactos;
- no encuentra código parcial;
- no encuentra categoría diferente;
- ignora mayúsculas y minúsculas;
- conserva ceros iniciales;
- devuelve `null` cuando no existe;
- mismo código en categorías distintas devuelve la fila correcta.

### Repositorio

- procesa una referencia;
- procesa varias referencias;
- conserva orden;
- produce `FOUND`;
- produce `NOT_FOUND`;
- combina encontradas y no encontradas;
- devuelve un único callback;
- ejecuta fuera del hilo principal;
- transforma error;
- no devuelve resultados parciales tras error;
- copia defensivamente la entrada.

### Servicio

- rechaza lista nula;
- rechaza lista vacía;
- ignora entradas nulas o inválidas según contrato;
- conserva orden;
- calcula total;
- calcula encontradas;
- calcula no encontradas;
- transforma error;
- no deduplica una lista ya confirmada de forma destructiva;
- no modifica referencias.

### ViewModel

- estado inicial;
- aplica referencias una vez;
- pasa a `LOADING`;
- bloquea segunda carga;
- publica `CONTENT`;
- publica `INVALID_INPUT`;
- publica `ERROR`;
- conserva entrada para reintento;
- reintenta una vez;
- mantiene contenido tras recreación;
- no navega;
- no consulta DAO directamente.

### Activity y adapter

- decodifica contrato;
- muestra progreso;
- muestra resumen;
- mantiene orden;
- muestra sitio y posición;
- oculta posición vacía;
- muestra No encontrada;
- fila encontrada abre id correcto;
- fila no encontrada no navega;
- Reintentar delega al ViewModel;
- Atrás vuelve a revisión;
- rotación conserva contenido;
- accesibilidad de filas.

### Integración manual

- todas encontradas;
- algunas encontradas;
- ninguna encontrada;
- mismo código con dos categorías;
- ceros iniciales;
- posición opcional;
- lista aproximada de 15 referencias;
- error y reintento;
- abrir detalle y volver;
- volver a revisión y corregir;
- modo oscuro;
- fuente grande;
- rotación;
- modo avión.

---

## 38. Tareas de implementación

1. Confirmar HU-24 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-25-mostrar-ubicaciones-lista`.
4. Revisar el contrato real de referencias confirmadas.
5. Extraer contrato compartido si evita duplicación.
6. Crear `WarehouseReferenceLocation`.
7. Crear resultado completo de localización.
8. Añadir consulta exacta al DAO.
9. Ampliar `WarehouseItemRepository`.
10. Implementar operación por lote en `RoomWarehouseItemRepository`.
11. Crear pruebas DAO.
12. Crear pruebas del repositorio.
13. Crear `ReferenceListLocationService`.
14. Crear pruebas del servicio.
15. Crear estado de UI.
16. Crear ViewModel.
17. Crear Factory.
18. Ampliar `ReferenceListModule`.
19. Ampliar `AppContainer`.
20. Crear `ReferenceListLocationActivity`.
21. Crear layout de pantalla.
22. Crear adapter y layout de fila.
23. Implementar resumen.
24. Implementar estados encontrada/no encontrada.
25. Implementar error y reintento.
26. Implementar acceso al detalle.
27. Conectar confirmación de HU-24 con ubicaciones.
28. Mantener revisión en la pila.
29. Eliminar la notificación temporal de confirmación.
30. Registrar Activity en Manifest.
31. Añadir strings y accesibilidad.
32. Crear pruebas del resultado.
33. Crear pruebas del ViewModel.
34. Crear pruebas del contrato de Activity.
35. Verificar regresión de HU-23 y HU-24.
36. Ejecutar pruebas unitarias.
37. Ejecutar lint.
38. Ejecutar build debug.
39. Ejecutar pruebas instrumentadas.
40. Verificar funcionamiento offline.
41. Verificar Room sin modificaciones.
42. Verificar ausencia de historial.
43. Verificar criterios de aceptación.
44. Integrar en `develop`.
45. Verificar CI de `develop`.
46. Eliminar la rama tras confirmar la integración.

---

## 39. Evidencias necesarias

- navegación desde revisión;
- carga de ubicaciones;
- consulta por categoría y código;
- todas encontradas;
- algunas no encontradas;
- ninguna encontrada;
- orden conservado;
- mismo código en distintas categorías;
- ceros iniciales;
- sitio mostrado;
- posición mostrada;
- posición opcional oculta;
- resumen;
- fila no encontrada;
- apertura del detalle;
- retorno desde detalle;
- retorno a revisión;
- corrección y nueva consulta;
- error de Room;
- reintento;
- rotación durante carga;
- rotación con contenido;
- modo avión;
- Room sin modificaciones;
- historial no creado;
- pruebas DAO;
- pruebas de repositorio;
- pruebas del servicio;
- pruebas del ViewModel;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 40. Definición de terminado

HU-25 estará terminada cuando:

- la lista confirmada llegue a la pantalla de ubicaciones;
- la consulta utilice categoría y código;
- no se utilice una búsqueda parcial;
- no se busque únicamente por código;
- el DAO disponga de consulta compuesta exacta;
- el repositorio exponga una operación por lote;
- la UI no coordine múltiples callbacks;
- las consultas se ejecuten fuera del hilo principal;
- cada referencia produzca una fila;
- el orden confirmado se conserve;
- las referencias encontradas muestren sitio;
- la posición se muestre solo cuando exista;
- las referencias no encontradas permanezcan visibles;
- un resultado mixto se presente correctamente;
- ninguna encontrada no se trate como error técnico;
- el resumen sea correcto;
- una fila encontrada abra el detalle correcto;
- una fila no encontrada no navegue;
- el usuario pueda volver a revisión;
- la rotación no duplique la consulta;
- el error permita reintento;
- no se modifique mercancía;
- no se modifique el esquema Room;
- no se cree historial;
- no se interpreten cantidades;
- no se modifique stock;
- no se conserve la imagen;
- el flujo funcione completamente sin conexión;
- HU-23 y HU-24 continúen operativas;
- las pruebas definidas sean satisfactorias;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 41. Validación técnica final

Ejecutar:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Con emulador o dispositivo:

```bash
./gradlew connectedDebugAndroidTest
```

Validación manual obligatoria:

- todas encontradas;
- algunas no encontradas;
- ninguna encontrada;
- orden;
- categorías con mismo código;
- posición opcional;
- detalle;
- regreso a revisión;
- error y reintento;
- rotación;
- modo avión.

---

## 42. Resultado esperado

Al cerrar HU-25:

```text
lista confirmada
        ↓
Room consulta categoría + código
        ↓
resultado ordenado
        ↓
sitio y posición
        +
referencias no encontradas
```

Con HU-25 quedará completo el objetivo funcional de AlmacenTracker v1.2:

```text
escanear individualmente
        o
procesar una lista
        ↓
identificar mercancía
        ↓
mostrar dónde se encuentra
```

La versión no registrará todavía historial de mercancía sacada ni modificará stock.

La siguiente etapa prevista será:

```text
v1.3.0 — Registro e historial de mercancía sacada
```
