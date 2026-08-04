# HU-03 — Consultar detalle de mercancía

> Tercera historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-03  
**Nombre:** Consultar detalle de mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-03-consultar-detalle-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero consultar toda la información de una mercancía seleccionada,  
para conocer su categoría, código, ubicación, observaciones y fechas registradas.

---

## 3. Objetivo

Implementar una pantalla de detalle que permita seleccionar una mercancía desde el listado principal, recuperar el registro correcto mediante su identificador interno y mostrar toda su información.

La historia extenderá la arquitectura hexagonal aplicada en las HUs anteriores:

```text
WarehouseItemAdapter
        ↓
MainActivity
        ↓
ItemDetailActivity
        ↓
WarehouseItemDetailViewModel
        ↓
GetWarehouseItemDetailUseCase
        ↓
GetWarehouseItemDetailService
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
Room / SQLite
```

La pantalla consultará siempre la fuente local mediante el puerto de entrada. No se enviará el objeto completo desde el listado como fuente definitiva de datos.

---

## 4. Alcance incluido

La HU-03 incluye:

- seleccionar un elemento desde el RecyclerView;
- abrir `ItemDetailActivity`;
- enviar el identificador interno del registro seleccionado;
- validar el identificador recibido;
- recuperar la mercancía por `id`;
- mostrar estado de carga;
- mostrar estado con contenido;
- mostrar estado de registro inexistente;
- mostrar estado de identificador inválido;
- mostrar estado de error;
- mostrar categoría;
- mostrar código;
- mostrar sitio;
- mostrar posición cuando exista;
- mostrar observaciones cuando existan;
- mostrar fecha de creación;
- mostrar fecha de actualización;
- ocultar o adaptar campos opcionales vacíos;
- conservar el estado ante rotación;
- actualizar el detalle si Room emite cambios sobre el registro;
- mostrar acciones visuales para Editar y Eliminar;
- dejar dichas acciones preparadas para historias posteriores;
- permitir volver al listado;
- pruebas unitarias, de DAO y de interfaz relacionadas.

---

## 5. Alcance excluido

La HU-03 no incluye:

- editar la mercancía;
- eliminar la mercancía;
- confirmar eliminaciones;
- buscar mercancía;
- filtrar mercancía;
- registrar nuevos elementos;
- eliminar varios registros;
- compartir el detalle;
- importar o exportar información;
- historial de ubicaciones;
- sincronización remota;
- autenticación;
- navegación funcional hacia edición;
- ejecución real de la eliminación.

Las acciones Editar y Eliminar podrán mostrarse como parte del diseño, pero no deberán ejecutar esas operaciones hasta sus historias correspondientes.

---

## 6. Precondiciones

Antes de comenzar la HU-03 deberán cumplirse estas condiciones:

- la HU-01 está implementada y fusionada en `develop`;
- la HU-02 está implementada y fusionada en `develop`;
- la CI de `develop` finaliza correctamente;
- el listado muestra registros desde Room;
- cada elemento del RecyclerView dispone de su `id`;
- existe `WarehouseItem`;
- existe `WarehouseItemEntity`;
- existe `WarehouseItemDao`;
- existe `WarehouseItemRepository`;
- existe `RoomWarehouseItemRepository`;
- existe `WarehouseItemPersistenceMapper`;
- existe `AppContainer`;
- el registro creado en HU-02 persiste correctamente;
- las fechas de creación y actualización se almacenan.

---

## 7. Información mostrada

La pantalla de detalle deberá mostrar:

| Campo | Obligatorio | Comportamiento |
|---|---:|---|
| Categoría | Sí | Se muestra siempre |
| Código | Sí | Se muestra siempre |
| Sitio | Sí | Se muestra siempre |
| Posición | No | Se oculta o adapta si no existe |
| Observaciones | No | Se oculta o adapta si no existe |
| Fecha de creación | Sí | Se presenta en formato legible |
| Fecha de actualización | Sí | Se presenta en formato legible |

### Ejemplo con todos los campos

```text
Categoría
MR

Código
1050

Sitio
A1

Posición
Nivel 2

Observaciones
Caja exterior dañada

Creado
18/07/2026 10:30

Última actualización
18/07/2026 10:30
```

### Ejemplo sin campos opcionales

```text
Categoría
MD

Código
1050

Sitio
B3

Creado
18/07/2026 11:10

Última actualización
18/07/2026 11:10
```

No deberán mostrarse `null`, cadenas vacías ni separadores innecesarios cuando los valores opcionales no existan.

---

## 8. Identificación del registro

La navegación utilizará exclusivamente el identificador interno:

```text
warehouseItemId
```

El `id` será enviado desde `MainActivity` a `ItemDetailActivity`.

Ejemplo conceptual:

```java
Intent intent = new Intent(this, ItemDetailActivity.class);
intent.putExtra(EXTRA_WAREHOUSE_ITEM_ID, warehouseItem.getId());
startActivity(intent);
```

El objeto completo no se utilizará como fuente definitiva porque:

- podría quedar desactualizado;
- duplicaría datos entre Activities;
- obligaría a serializar el dominio;
- evitaría observar cambios desde Room;
- mezclaría navegación con persistencia.

El detalle deberá recuperar el registro por `id` mediante el caso de uso.

---

## 9. Estados de interfaz

### 9.1. Loading

La aplicación está recuperando el registro.

- indicador de progreso discreto;
- contenido oculto;
- acciones Editar y Eliminar deshabilitadas;
- navegación hacia atrás disponible.

### 9.2. Content

El registro existe y se recuperó correctamente.

- contenido visible;
- campos obligatorios mostrados;
- campos opcionales adaptados;
- fechas formateadas;
- acciones visuales disponibles.

### 9.3. NotFound

El identificador es válido, pero el registro no existe.

Mensaje orientativo:

```text
La mercancía ya no está disponible.
```

### 9.4. InvalidId

La Activity no recibió un identificador válido.

Mensaje orientativo:

```text
No se pudo identificar la mercancía.
```

No se deberá consultar Room en este estado.

### 9.5. Error

Ocurrió un error inesperado al consultar.

Mensaje orientativo:

```text
No se pudo cargar el detalle de la mercancía.
```

La aplicación no deberá cerrarse ni mostrar excepciones técnicas al usuario.

---

## 10. Flujo principal

1. El usuario abre AlmacenTracker.
2. `MainActivity` muestra el listado.
3. El usuario pulsa una mercancía.
4. `WarehouseItemAdapter` notifica la selección.
5. `MainActivity` abre `ItemDetailActivity`.
6. La navegación envía únicamente el `id`.
7. `ItemDetailActivity` valida el identificador.
8. El ViewModel recibe el `id`.
9. El ViewModel invoca `GetWarehouseItemDetailUseCase`.
10. El servicio solicita el registro al repositorio.
11. El repositorio consulta Room mediante el DAO.
12. Room devuelve el registro observable.
13. El mapper convierte entidad a dominio.
14. El ViewModel emite estado `Content`.
15. La Activity muestra todos los datos.
16. El usuario puede volver al listado.

---

## 11. Flujos alternativos

### FA-01 — Registro con posición

La sección Posición se muestra con su nivel o fila.

### FA-02 — Registro sin posición

La sección se oculta o se adapta sin dejar espacios incorrectos ni mostrar `null`.

### FA-03 — Registro con observaciones

La sección Observaciones se muestra completa.

### FA-04 — Registro sin observaciones

La sección se oculta o se adapta según el diseño adoptado.

### FA-05 — Identificador ausente

1. La Activity se abre sin `id`.
2. No invoca el caso de uso.
3. Muestra `InvalidId`.
4. Permite volver al listado.

### FA-06 — Identificador inválido

Un `id` menor o igual que cero no deberá provocar una consulta a Room.

### FA-07 — Registro inexistente

1. El `id` es válido.
2. Room no encuentra el registro.
3. El ViewModel emite `NotFound`.
4. La Activity no muestra contenido obsoleto.
5. Las acciones quedan deshabilitadas.

### FA-08 — Cambio observado

Si Room emite una nueva versión del registro, el detalle deberá actualizarse. Este comportamiento prepara la futura HU-06.

### FA-09 — Registro eliminado mientras se observa

Si Room emite ausencia de datos, el estado deberá cambiar a `NotFound` sin conservar información obsoleta.

### FA-10 — Rotación

El ViewModel conservará el `id` y el estado para reconstruir correctamente la pantalla.

### FA-11 — Error de persistencia

El error se transformará a un estado de aplicación comprensible y no cerrará la Activity.

---

## 12. Criterios de aceptación

### CA-01 — Apertura del detalle

**Dado** que existe mercancía en el listado,  
**cuando** el usuario pulsa un elemento,  
**entonces** se abre la pantalla de detalle del registro seleccionado.

### CA-02 — Registro correcto

**Dado** que el usuario selecciona una mercancía,  
**cuando** se abre el detalle,  
**entonces** la información mostrada corresponde al `id` seleccionado.

### CA-03 — Categoría y código

**Dado** un registro existente,  
**cuando** se muestra su detalle,  
**entonces** aparecen categoría y código.

### CA-04 — Sitio

**Dado** un registro existente,  
**cuando** se muestra su detalle,  
**entonces** aparece el sitio almacenado.

### CA-05 — Posición existente

**Dado** un registro con posición,  
**cuando** se muestra el detalle,  
**entonces** aparece su nivel o fila.

### CA-06 — Posición inexistente

**Dado** un registro sin posición,  
**cuando** se muestra el detalle,  
**entonces** no aparece `null`, una cadena vacía ni un espacio visual incorrecto.

### CA-07 — Observaciones existentes

**Dado** un registro con observaciones,  
**cuando** se muestra el detalle,  
**entonces** aparece el contenido completo.

### CA-08 — Observaciones inexistentes

**Dado** un registro sin observaciones,  
**cuando** se muestra el detalle,  
**entonces** la interfaz adapta u oculta esa sección.

### CA-09 — Fecha de creación

**Dado** un registro existente,  
**cuando** se muestra el detalle,  
**entonces** la fecha de creación aparece en formato legible.

### CA-10 — Fecha de actualización

**Dado** un registro existente,  
**cuando** se muestra el detalle,  
**entonces** la fecha de actualización aparece en formato legible.

### CA-11 — Identificador ausente

**Dado** que la Activity no recibe un identificador,  
**cuando** intenta cargar el detalle,  
**entonces** no consulta Room y muestra un estado controlado.

### CA-12 — Registro inexistente

**Dado** un identificador válido sin registro asociado,  
**cuando** se consulta Room,  
**entonces** se muestra un estado `NotFound`.

### CA-13 — Error controlado

**Dado** que ocurre un error inesperado,  
**cuando** la consulta falla,  
**entonces** la aplicación muestra un mensaje y no se cierra.

### CA-14 — Rotación

**Dado** que el detalle está visible,  
**cuando** el usuario rota el dispositivo,  
**entonces** conserva el registro y vuelve a mostrar el contenido correcto.

### CA-15 — Observación de cambios

**Dado** que el detalle está abierto,  
**cuando** Room actualiza el registro,  
**entonces** la pantalla refleja el nuevo contenido.

### CA-16 — Navegación de regreso

**Dado** que el usuario está en el detalle,  
**cuando** pulsa Atrás,  
**entonces** regresa al listado sin alterar el registro.

### CA-17 — Acciones futuras

**Dado** que el detalle contiene un registro válido,  
**cuando** se renderiza la pantalla,  
**entonces** pueden mostrarse acciones de Editar y Eliminar sin ejecutar todavía esas operaciones.

### CA-18 — Operación no bloqueante

**Dado** que se consulta el registro,  
**cuando** Room procesa la operación,  
**entonces** no se ejecuta trabajo bloqueante en el hilo principal.

---

## 13. Diseño técnico propuesto

### 13.1. Puerto de entrada

`GetWarehouseItemDetailUseCase` representará la operación de aplicación para observar un registro concreto.

Firma orientativa:

```java
public interface GetWarehouseItemDetailUseCase {
    LiveData<WarehouseItem> observeById(long warehouseItemId);
}
```

La firma definitiva deberá respetar el patrón asíncrono ya utilizado en el proyecto.

### 13.2. Servicio de aplicación

`GetWarehouseItemDetailService` implementará el puerto de entrada.

Responsabilidades:

- validar que el `id` sea utilizable;
- solicitar el registro al puerto de salida;
- transformar ausencia de datos en un resultado comprensible;
- mantener la capa de entrada desacoplada de Room.

No dependerá de Android UI, Activities, ViewModels, entidades Room o DAO.

### 13.3. Puerto de salida

`WarehouseItemRepository` se ampliará con una operación para observar por id.

Firma orientativa:

```java
LiveData<WarehouseItem> observeById(long warehouseItemId);
```

### 13.4. Adaptador de salida Room

`RoomWarehouseItemRepository` deberá:

- invocar el DAO;
- observar la entidad por id;
- mapear `WarehouseItemEntity` a `WarehouseItem`;
- conservar ausencia de datos como ausencia;
- no crear objetos ficticios cuando el registro no exista.

### 13.5. DAO

Consulta orientativa:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "WHERE id = :warehouseItemId " +
    "LIMIT 1"
)
LiveData<WarehouseItemEntity> observeById(long warehouseItemId);
```

El detalle se identificará mediante la clave primaria interna, no por categoría y código.

### 13.6. ViewModel

`WarehouseItemDetailViewModel` deberá:

- recibir el `id`;
- validar la inicialización;
- invocar el caso de uso;
- exponer un estado exclusivo;
- sobrevivir a rotación;
- evitar recargar innecesariamente el mismo id;
- no acceder al DAO ni a Room.

Estados mínimos:

```text
LOADING
CONTENT
NOT_FOUND
INVALID_ID
ERROR
```

### 13.7. Estado de interfaz

Estructura orientativa:

```text
WarehouseItemDetailUiState
├── status
├── item
└── errorMessage
```

Los estados deberán ser mutuamente excluyentes.

### 13.8. Factory del ViewModel

`WarehouseItemDetailViewModelFactory` recibirá el caso de uso y el `warehouseItemId`.

La Activity no construirá manualmente repositorio, base de datos o servicio.

### 13.9. Activity

`ItemDetailActivity` deberá:

- obtener el `id` del Intent;
- crear el ViewModel mediante Factory;
- observar el estado;
- renderizar carga, contenido, ausencia y error;
- mostrar fechas formateadas;
- controlar navegación;
- no consultar Room directamente.

### 13.10. Adapter del listado

`WarehouseItemAdapter` deberá exponer una acción de selección por id:

```java
public interface OnWarehouseItemClickListener {
    void onWarehouseItemClick(long warehouseItemId);
}
```

No se utilizará la posición del RecyclerView como identificador estable.

### 13.11. Composición de dependencias

`AppContainer` deberá proporcionar:

- `GetWarehouseItemDetailService`;
- `WarehouseItemDetailViewModelFactory`;
- las dependencias ya existentes.

No se añadirá un framework de inyección exclusivamente para esta historia.

---

## 14. Formato de fechas

Las fechas almacenadas como `long` deberán convertirse a un formato comprensible.

Formato recomendado para la v1.0:

```text
dd/MM/yyyy HH:mm
```

Ejemplo:

```text
18/07/2026 10:30
```

Reglas:

- no mostrar directamente el timestamp;
- centralizar el formateo;
- no guardar texto formateado en Room;
- conservar `long` como valor persistido;
- respetar la zona horaria del dispositivo.

Clase orientativa:

```text
WarehouseItemDateFormatter
```

El formatter pertenecerá al adaptador de entrada o a presentación, nunca al dominio.

---

## 15. Decisiones técnicas importantes

### 15.1. Navegar por id

Se enviará únicamente `warehouseItemId`.

### 15.2. Consulta observable

El detalle observará Room para reflejar futuras ediciones o eliminaciones.

### 15.3. No consultar por categoría + código

Aunque categoría + código formen la identidad funcional, el detalle utilizará la clave primaria interna.

### 15.4. No mostrar valores técnicos

La interfaz no mostrará timestamps, nombres de columnas, `null`, excepciones ni ids internos.

### 15.5. Acciones sin implementación prematura

Editar y Eliminar podrán aparecer, pero sus flujos funcionales no se implementarán en HU-03.

### 15.6. Estado NotFound real

La ausencia del registro no deberá convertirse en un error genérico.

### 15.7. Room sigue siendo la fuente de verdad

El detalle no utilizará una copia del objeto recibida desde el listado para renderizar permanentemente.

---

## 16. Estructura de archivos orientativa

La HU-03 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── GetWarehouseItemDetailUseCase.java
│   │   └── out/
│   │       └── WarehouseItemRepository.java
│   └── service/
│       └── GetWarehouseItemDetailService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   ├── MainActivity.java
│   │       │   └── ItemDetailActivity.java
│   │       ├── adapter/
│   │       │   └── WarehouseItemAdapter.java
│   │       ├── formatter/
│   │       │   └── WarehouseItemDateFormatter.java
│   │       ├── state/
│   │       │   └── WarehouseItemDetailUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemDetailViewModel.java
│   │           └── WarehouseItemDetailViewModelFactory.java
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

No se crearán directorios vacíos ni clases sin responsabilidad concreta.

---

## 17. Diseño de interfaz esperado

Estructura orientativa:

```text
Toolbar
← Detalle de mercancía

Categoría
MR

Código
1050

Ubicación
Sitio A1
Nivel 2

Observaciones
Caja exterior dañada

Creado
18/07/2026 10:30

Última actualización
18/07/2026 10:30

[Editar] [Eliminar]
```

### Requisitos visuales

- pantalla desplazable;
- jerarquía clara;
- categoría y código destacados;
- sitio y posición agrupados como ubicación;
- campos opcionales sin espacios vacíos;
- fechas legibles;
- acciones accesibles;
- navegación hacia atrás visible;
- coherencia con Material Components;
- correcto funcionamiento en pantallas pequeñas.

---

## 18. Pruebas recomendadas

### 18.1. Pruebas unitarias del servicio

- devuelve el registro correcto por id;
- no transforma una ausencia en un modelo vacío;
- identifica un id inválido;
- transforma errores de repositorio;
- no depende de Room.

### 18.2. Pruebas unitarias del ViewModel

- estado inicial correcto;
- emite `Content` con un registro;
- emite `NotFound` ante ausencia;
- emite `InvalidId` ante id no válido;
- emite `Error` ante fallo;
- conserva el id;
- no reinicia observadores innecesariamente;
- mantiene estado tras recreación.

### 18.3. Pruebas del DAO

- `observeById()` devuelve el registro correcto;
- un id inexistente devuelve ausencia;
- dos registros distintos no se confunden;
- la consulta refleja una actualización;
- la consulta refleja una eliminación cuando se simula en prueba.

### 18.4. Pruebas del mapper

- convierte todos los campos;
- conserva id;
- conserva fechas;
- conserva opcionales nulos;
- no sustituye valores ausentes por texto visual.

### 18.5. Pruebas del formatter

- formatea timestamp válido;
- produce un formato legible;
- no modifica el valor persistido;
- maneja la zona horaria del dispositivo.

### 18.6. Pruebas instrumentadas de interfaz

Cuando sea razonable:

- pulsar un elemento abre el detalle;
- se muestra el registro correcto;
- posición existente aparece;
- posición ausente no aparece;
- observaciones existentes aparecen;
- observaciones ausentes se adaptan;
- Atrás regresa al listado;
- rotación conserva contenido;
- id inexistente muestra `NotFound`.

### 18.7. Pruebas manuales

- registro con todos los campos;
- registro sin posición;
- registro sin observaciones;
- texto largo;
- rotación;
- id ausente;
- id inexistente;
- cierre y reapertura;
- navegación repetida;
- uso sin conexión;
- pantalla pequeña;
- formato de fechas;
- botones Editar y Eliminar sin comportamiento prematuro.

---

## 19. Tareas de implementación

1. Confirmar que HU-02 está integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Crear `feature/hu-03-consultar-detalle-mercancia`.
4. Revisar la implementación real del listado y su listener.
5. Añadir listener de selección por id al adapter.
6. Conectar la selección desde `MainActivity`.
7. Crear constante para el extra del `id`.
8. Crear `GetWarehouseItemDetailUseCase`.
9. Crear `GetWarehouseItemDetailService`.
10. Ampliar `WarehouseItemRepository` con consulta por id.
11. Añadir `observeById()` al DAO.
12. Implementar consulta por id en `RoomWarehouseItemRepository`.
13. Confirmar mapeo entidad-dominio.
14. Crear `WarehouseItemDetailUiState`.
15. Crear `WarehouseItemDetailViewModel`.
16. Crear `WarehouseItemDetailViewModelFactory`.
17. Actualizar `AppContainer`.
18. Crear `ItemDetailActivity`.
19. Registrar la Activity en el manifest si es necesario.
20. Crear layout de detalle.
21. Implementar estados Loading, Content, NotFound, InvalidId y Error.
22. Implementar visualización de campos obligatorios.
23. Implementar tratamiento de posición y observaciones opcionales.
24. Implementar formateo de fechas.
25. Añadir acciones visuales Editar y Eliminar sin lógica funcional.
26. Implementar navegación hacia atrás.
27. Verificar actualización observable.
28. Crear pruebas unitarias.
29. Crear pruebas DAO.
30. Crear pruebas de interfaz necesarias.
31. Ejecutar `./gradlew testDebugUnitTest`.
32. Ejecutar `./gradlew lintDebug`.
33. Ejecutar `./gradlew assembleDebug`.
34. Publicar commits representativos.
35. Verificar CI en la rama.
36. Revisar criterios de aceptación.
37. Fusionar localmente en `develop`.
38. Verificar CI en `develop`.
39. Eliminar la rama local y remota tras confirmar la integración.

---

## 20. Evidencias necesarias para cerrar la HU

- captura del listado antes de seleccionar;
- captura del detalle con todos los campos;
- captura del detalle sin posición;
- captura del detalle sin observaciones;
- evidencia de fechas formateadas;
- evidencia del registro correcto por id;
- evidencia de navegación hacia atrás;
- evidencia de estado `NotFound`;
- evidencia de id inválido controlado;
- evidencia de conservación tras rotación;
- evidencia de actualización observable;
- confirmación de que no aparece `null`;
- confirmación de que no se envía el objeto completo como fuente definitiva;
- confirmación de que la Activity no accede a Room;
- resultado de pruebas unitarias;
- resultado de pruebas DAO;
- resultado de lint;
- compilación debug correcta;
- CI satisfactoria en `feature/hu-03-consultar-detalle-mercancia`;
- evidencia del merge local en `develop`;
- CI satisfactoria en `develop`.

---

## 21. Definición de terminado

La HU-03 estará terminada cuando:

- un elemento del listado pueda seleccionarse;
- la navegación envíe el id interno;
- se abra `ItemDetailActivity`;
- la Activity valide el id recibido;
- el caso de uso consulte el registro por id;
- el repositorio utilice el puerto de salida;
- Room devuelva el registro correcto;
- la categoría se muestre;
- el código se muestre;
- el sitio se muestre;
- la posición se muestre únicamente cuando exista;
- las observaciones se muestren únicamente cuando existan o se adapten correctamente;
- la fecha de creación sea legible;
- la fecha de actualización sea legible;
- no se muestre el id interno;
- no aparezcan valores `null`;
- existan estados Loading, Content, NotFound, InvalidId y Error;
- la pantalla sobreviva a rotación;
- el detalle observe cambios de Room;
- Atrás regrese al listado;
- Editar y Eliminar estén preparados visualmente sin implementación funcional;
- la consulta no bloquee el hilo principal;
- la UI dependa del puerto de entrada;
- el dominio no dependa de Android ni Room;
- las pruebas definidas finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione localmente en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 22. Resultado esperado

Al cerrar la HU-03, AlmacenTracker tendrá el flujo completo de consulta individual:

```text
usuario selecciona una mercancía
        ↓
la aplicación envía su id
        ↓
el caso de uso consulta Room
        ↓
el ViewModel recibe el registro
        ↓
la pantalla muestra el detalle completo
```

El proyecto quedará preparado para continuar con:

```text
HU-04 — Buscar mercancía
```

---

## 23. Commit documental recomendado

```text
docs: add HU-03 warehouse item detail plan
```
