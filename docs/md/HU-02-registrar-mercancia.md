# HU-02 — Registrar mercancía

> Segunda historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-02  
**Nombre:** Registrar mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-02-registrar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero registrar una mercancía indicando su categoría, código y ubicación,  
para poder localizarla posteriormente desde el listado principal.

---

## 3. Objetivo

Implementar el flujo completo de alta de mercancía, desde la interacción del usuario con el formulario hasta la persistencia del nuevo registro en Room.

La historia deberá extender la arquitectura creada en la HU-01 sin romper la dirección de dependencias:

```text
ItemFormActivity
        ↓
WarehouseItemFormViewModel
        ↓
CreateWarehouseItemUseCase
        ↓
CreateWarehouseItemService
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
Room / SQLite
```

Al finalizar, un registro válido deberá almacenarse localmente y aparecer automáticamente en el listado implementado en la HU-01.

---

## 4. Alcance incluido

La HU-02 incluye:

- acceso al formulario desde el listado principal;
- creación de `ItemFormActivity`;
- formulario de alta;
- campo categoría;
- campo código;
- campo sitio;
- campo posición opcional;
- campo observaciones opcional;
- acción Guardar;
- acción Cancelar;
- validación de campos obligatorios;
- normalización de los datos;
- generación automática de fechas;
- persistencia mediante Room;
- control técnico de la restricción categoría + código;
- estado de formulario;
- estado de guardado;
- confirmación de operación correcta;
- visualización de errores comprensibles;
- regreso al listado tras guardar;
- actualización automática del RecyclerView;
- conservación de los datos introducidos ante rotación;
- prevención de envíos repetidos mientras se guarda;
- pruebas unitarias y de persistencia asociadas.

---

## 5. Alcance excluido

La HU-02 no incluye:

- editar un registro existente;
- consultar el detalle de una mercancía;
- eliminar registros;
- buscar mercancía;
- aplicar filtros;
- eliminar varios registros;
- importar o exportar información;
- escanear códigos;
- sincronizar con un servicio remoto;
- autenticación;
- historial de cambios;
- selección de categorías desde un catálogo configurable.

La HU-02 deberá impedir que Room almacene una combinación duplicada de categoría y código. Sin embargo, la validación integral de esta regla en alta, edición y persistencia seguirá cerrándose formalmente en la HU-08.

---

## 6. Precondiciones

Antes de comenzar la HU-02 deberán cumplirse estas condiciones:

- la HU-01 está implementada y fusionada en `develop`;
- la CI de `develop` finaliza correctamente;
- existe `WarehouseItem` como modelo de dominio;
- existe `WarehouseItemEntity`;
- existe `WarehouseItemDao`;
- existe `AlmacenTrackerDatabase`;
- existe `WarehouseItemRepository`;
- existe `RoomWarehouseItemRepository`;
- existe `WarehouseItemPersistenceMapper`;
- existe `AppContainer`;
- `MainActivity` muestra el listado observable;
- la entidad Room conserva el índice único de categoría + código.

---

## 7. Datos del formulario

| Campo | Obligatorio | Ejemplo | Descripción |
|---|---:|---|---|
| Categoría | Sí | MR | Categoría que diferencia códigos iguales |
| Código | Sí | 1050 | Código de la mercancía |
| Sitio | Sí | A1 | Área general del almacén |
| Posición | No | Nivel 2 | Nivel o fila dentro del sitio |
| Observaciones | No | Caja exterior dañada | Información complementaria |

### Ayudas visuales recomendadas

```text
Categoría
Ejemplo: MR

Código
Ejemplo: 1050

Sitio
Área general, por ejemplo A1

Posición
Nivel o fila opcional, por ejemplo Nivel 2
```

La interfaz deberá indicar claramente qué campos son obligatorios y cuáles son opcionales.

---

## 8. Reglas de negocio aplicables

### 8.1. Categoría

- es obligatoria;
- no puede contener únicamente espacios;
- se eliminan espacios al inicio y al final;
- se convierte a mayúsculas antes de guardar.

Ejemplo:

```text
"  mr  " → "MR"
```

### 8.2. Código

- es obligatorio;
- no puede contener únicamente espacios;
- se eliminan espacios al inicio y al final;
- se convierte a mayúsculas antes de guardar;
- puede repetirse en categorías diferentes.

### 8.3. Sitio

- es obligatorio;
- no puede contener únicamente espacios;
- se eliminan espacios al inicio y al final;
- se convierte a mayúsculas para mantener un formato coherente.

Ejemplo:

```text
"  a1  " → "A1"
```

### 8.4. Posición

- es opcional;
- se eliminan espacios al inicio y al final;
- una cadena vacía deberá transformarse en `null` o en la representación vacía adoptada por el proyecto;
- representa el nivel o fila dentro del sitio.

### 8.5. Observaciones

- son opcionales;
- se eliminan espacios al inicio y al final;
- una cadena vacía deberá transformarse en `null` o en la representación vacía adoptada por el proyecto;
- no deberán contener información real o privada de una empresa.

### 8.6. Unicidad funcional

La combinación:

```text
categoría + código
```

debe ser única.

Permitido:

```text
MR + 1050
MD + 1050
```

No permitido:

```text
MR + 1050
MR + 1050
```

La restricción deberá existir en Room aunque también se realice una comprobación previa desde la aplicación.

### 8.7. Fechas

Al crear un registro:

```text
fechaCreacion = instante actual
fechaActualizacion = instante actual
```

Ambos valores deberán generarse en la aplicación y no introducirse manualmente desde la interfaz.

---

## 9. Estados del formulario

### 9.1. Editing

- campos habilitados;
- botón Guardar disponible;
- botón Cancelar disponible;
- sin indicador de progreso.

### 9.2. Saving

- Guardar deshabilitado;
- envíos repetidos bloqueados;
- progreso discreto;
- datos mantenidos en pantalla.

### 9.3. Success

- evento único de guardado correcto;
- regreso a `MainActivity`;
- confirmación breve;
- actualización automática del listado mediante Room.

### 9.4. ValidationError

- no se invoca persistencia;
- errores junto a los campos;
- datos conservados;
- foco en el primer campo inválido cuando sea razonable.

### 9.5. DuplicateError

Mensaje orientativo:

```text
Ya existe una mercancía con esta categoría y código.
```

### 9.6. PersistenceError

Mensaje orientativo:

```text
No se pudo registrar la mercancía.
```

La Activity no deberá cerrarse y los datos deberán conservarse.

---

## 10. Flujo principal

1. El usuario abre AlmacenTracker.
2. La pantalla principal muestra el listado.
3. El usuario pulsa el botón para registrar mercancía.
4. Se abre `ItemFormActivity` en modo creación.
5. El usuario completa categoría, código y sitio.
6. Opcionalmente completa posición y observaciones.
7. El usuario pulsa Guardar.
8. El ViewModel recibe los valores.
9. La aplicación normaliza los datos.
10. La aplicación valida los campos.
11. El caso de uso crea el modelo de dominio.
12. El servicio solicita la inserción mediante `WarehouseItemRepository`.
13. El adaptador Room convierte el modelo a entidad.
14. El DAO inserta el registro.
15. Room confirma la operación.
16. El ViewModel emite éxito.
17. La Activity vuelve al listado.
18. El listado se actualiza automáticamente.
19. El usuario visualiza el nuevo registro.

---

## 11. Flujos alternativos

### FA-01 — Categoría vacía

No se invoca el repositorio y se muestra un error junto a categoría.

### FA-02 — Código vacío

No se persiste ningún registro y se muestra un error junto a código.

### FA-03 — Sitio vacío

No se persiste ningún registro y se muestra un error junto a sitio.

### FA-04 — Posición vacía

El registro se crea correctamente sin posición.

### FA-05 — Observaciones vacías

El registro se crea correctamente.

### FA-06 — Combinación duplicada

1. Ya existe `MR + 1050`.
2. El usuario intenta registrar nuevamente `MR + 1050`.
3. No se crea un segundo registro.
4. El formulario conserva los valores.
5. Se muestra un mensaje comprensible.

### FA-07 — Código repetido en otra categoría

`MR + 1050` y `MD + 1050` pueden coexistir.

### FA-08 — Cancelación

No se guarda ningún registro y se vuelve al listado.

### FA-09 — Rotación durante la edición

El ViewModel conserva los valores del formulario.

### FA-10 — Pulsación repetida de Guardar

Solo se crea un registro.

### FA-11 — Error inesperado de Room

El error se transforma en un resultado de aplicación, se conserva el formulario y la aplicación no se cierra.

---

## 12. Criterios de aceptación

### CA-01 — Acceso al formulario

**Dado** que el usuario se encuentra en el listado principal,  
**cuando** pulsa la acción de registrar mercancía,  
**entonces** se abre el formulario de alta.

### CA-02 — Registro válido

**Dado** que categoría, código y sitio son válidos,  
**cuando** el usuario pulsa Guardar,  
**entonces** el registro se almacena en Room y aparece en el listado.

### CA-03 — Categoría obligatoria

**Dado** que categoría está vacía o contiene únicamente espacios,  
**cuando** el usuario intenta guardar,  
**entonces** se muestra un error y no se crea el registro.

### CA-04 — Código obligatorio

**Dado** que código está vacío o contiene únicamente espacios,  
**cuando** el usuario intenta guardar,  
**entonces** se muestra un error y no se crea el registro.

### CA-05 — Sitio obligatorio

**Dado** que sitio está vacío o contiene únicamente espacios,  
**cuando** el usuario intenta guardar,  
**entonces** se muestra un error y no se crea el registro.

### CA-06 — Posición opcional

**Dado** que posición está vacía,  
**cuando** el resto de campos obligatorios es válido,  
**entonces** el registro se guarda correctamente sin posición.

### CA-07 — Observaciones opcionales

**Dado** que observaciones está vacío,  
**cuando** el resto de campos obligatorios es válido,  
**entonces** el registro se guarda correctamente.

### CA-08 — Normalización

**Dado** que el usuario introduce espacios externos o minúsculas,  
**cuando** guarda el formulario,  
**entonces** categoría, código y sitio se almacenan normalizados.

### CA-09 — Mismo código en categorías diferentes

**Dado** que existe `MR + 1050`,  
**cuando** el usuario registra `MD + 1050`,  
**entonces** el nuevo registro se guarda correctamente.

### CA-10 — Combinación duplicada

**Dado** que existe `MR + 1050`,  
**cuando** el usuario intenta registrar nuevamente `MR + 1050`,  
**entonces** no se crea otro registro y se muestra un aviso comprensible.

### CA-11 — Fechas automáticas

**Dado** un formulario válido,  
**cuando** se crea el registro,  
**entonces** fecha de creación y fecha de actualización se asignan automáticamente.

### CA-12 — Cancelación

**Dado** que el usuario está en el formulario,  
**cuando** cancela la operación,  
**entonces** no se guarda información y se vuelve al listado.

### CA-13 — Rotación

**Dado** que el usuario ha introducido datos,  
**cuando** rota el dispositivo,  
**entonces** el formulario conserva su contenido.

### CA-14 — Envío único

**Dado** que una operación de guardado está en curso,  
**cuando** el usuario vuelve a pulsar Guardar,  
**entonces** no se genera una segunda inserción.

### CA-15 — Error de persistencia controlado

**Dado** que ocurre un error inesperado al guardar,  
**cuando** la aplicación recibe el error,  
**entonces** conserva los datos, muestra un mensaje y no se cierra.

### CA-16 — Actualización del listado

**Dado** que el registro se guardó correctamente,  
**cuando** el usuario vuelve a la pantalla principal,  
**entonces** el listado muestra el nuevo elemento sin reiniciar la aplicación.

### CA-17 — Operación no bloqueante

**Dado** que se realiza una inserción,  
**cuando** Room procesa la operación,  
**entonces** no se ejecuta trabajo bloqueante en el hilo principal.

---

## 13. Diseño técnico propuesto

### 13.1. Puerto de entrada

`CreateWarehouseItemUseCase` representará la operación ofrecida por la aplicación para crear un registro.

La firma definitiva deberá respetar el patrón asíncrono ya utilizado en la HU-01.

### 13.2. Comando de entrada

`CreateWarehouseItemCommand` agrupará:

```text
category
code
site
position
notes
```

No contendrá `id`, `createdAt` ni `updatedAt`.

### 13.3. Resultado de aplicación

`CreateWarehouseItemResult` deberá distinguir:

```text
SUCCESS
VALIDATION_ERROR
DUPLICATE
PERSISTENCE_ERROR
```

Errores de campo mínimos:

```text
CATEGORY_REQUIRED
CODE_REQUIRED
SITE_REQUIRED
```

### 13.4. Servicio de aplicación

`CreateWarehouseItemService` deberá:

1. normalizar;
2. validar;
3. generar fechas;
4. crear el modelo de dominio;
5. invocar el repositorio;
6. transformar errores técnicos.

No dependerá de Android ni Room.

### 13.5. Puerto de salida

`WarehouseItemRepository` se ampliará con una operación de creación coherente con la estrategia asíncrona actual.

Room seguirá siendo la garantía definitiva de unicidad.

### 13.6. Adaptador Room

`RoomWarehouseItemRepository` deberá:

- mapear dominio a entidad;
- insertar fuera del hilo principal;
- detectar conflictos;
- devolver un resultado comprensible;
- conservar el id generado.

### 13.7. DAO

```java
@Insert(onConflict = OnConflictStrategy.ABORT)
long insert(WarehouseItemEntity entity);
```

No se utilizará `REPLACE`, porque podría ocultar un duplicado y alterar el id del registro.

### 13.8. ViewModel

`WarehouseItemFormViewModel` deberá:

- mantener valores;
- invocar el caso de uso;
- impedir envíos repetidos;
- exponer errores por campo;
- exponer estado Saving;
- emitir éxito una sola vez;
- sobrevivir a rotación.

### 13.9. Estado de interfaz

```text
WarehouseItemFormUiState
├── category
├── code
├── site
├── position
├── notes
├── categoryError
├── codeError
├── siteError
├── saving
└── generalError
```

### 13.10. Activity

`ItemFormActivity` deberá renderizar y delegar. No contendrá reglas de negocio ni accederá al DAO.

### 13.11. Composición de dependencias

`AppContainer` deberá exponer:

- `CreateWarehouseItemService`;
- `WarehouseItemFormViewModelFactory`;
- dependencias necesarias para la Activity.

No se añadirá un framework de inyección solo para esta HU.

---

## 14. Decisiones técnicas importantes

### 14.1. No utilizar `OnConflictStrategy.REPLACE`

Se utilizará `ABORT`.

### 14.2. Room es la garantía definitiva

Una comprobación previa puede mejorar el mensaje, pero no sustituye el índice único.

### 14.3. La Activity no normaliza

`trim`, mayúsculas, fechas y reglas no pertenecen a la Activity.

### 14.4. Las fechas no vienen del formulario

El usuario no introduce fechas.

### 14.5. El formulario se reutilizará en HU-06

La Activity se diseñará para poder evolucionar a edición, pero la HU-02 solo implementará alta.

### 14.6. Categoría sigue siendo texto libre

Las categorías configurables pertenecen a una versión posterior.

---

## 15. Estructura de archivos orientativa

```text
<package-root>/
├── domain/
│   └── model/
│       └── WarehouseItem.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── ObserveWarehouseItemsUseCase.java
│   │   │   ├── CreateWarehouseItemUseCase.java
│   │   │   ├── CreateWarehouseItemCommand.java
│   │   │   └── CreateWarehouseItemResult.java
│   │   └── out/
│   │       └── WarehouseItemRepository.java
│   └── service/
│       ├── ObserveWarehouseItemsService.java
│       └── CreateWarehouseItemService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   ├── MainActivity.java
│   │       │   └── ItemFormActivity.java
│   │       ├── state/
│   │       │   └── WarehouseItemFormUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemFormViewModel.java
│   │           └── WarehouseItemFormViewModelFactory.java
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               │   └── WarehouseItemDao.java
│               ├── entity/
│               │   └── WarehouseItemEntity.java
│               ├── mapper/
│               │   └── WarehouseItemPersistenceMapper.java
│               └── repository/
│                   └── RoomWarehouseItemRepository.java
└── configuration/
    └── AppContainer.java
```

La estructura podrá simplificarse si se mantiene la dirección de dependencias y se evitan clases sin responsabilidad real.

---

## 16. Diseño de interfaz esperado

```text
Toolbar
Título: Registrar mercancía

Categoría *
Código *
Sitio *
Posición
Observaciones

[Cancelar] [Guardar]
```

Requisitos visuales:

- campos obligatorios identificados;
- errores próximos al campo;
- formulario desplazable;
- observaciones multilínea;
- Guardar accesible con teclado abierto;
- estado Saving visible;
- uso coherente de Material Components.

Navegación:

```text
FloatingActionButton
        ↓
ItemFormActivity
        ↓
Room guarda
        ↓
ItemFormActivity finaliza
        ↓
MainActivity recibe lista actualizada
```

Room seguirá siendo la fuente de verdad. No se reconstruirá manualmente el listado desde el resultado de la Activity.

---

## 17. Pruebas recomendadas

### 17.1. Servicio de aplicación

- crea con datos válidos;
- rechaza categoría vacía;
- rechaza código vacío;
- rechaza sitio vacío;
- acepta posición vacía;
- acepta observaciones vacías;
- normaliza categoría, código y sitio;
- genera ambas fechas;
- transforma duplicado;
- transforma error inesperado;
- no invoca repositorio ante validación fallida.

### 17.2. ViewModel

- estado inicial correcto;
- conserva valores;
- muestra errores por campo;
- entra en Saving;
- bloquea doble envío;
- vuelve a habilitar tras error;
- emite éxito una sola vez;
- conserva el formulario tras recreación.

### 17.3. DAO

- inserta un registro válido;
- devuelve el id;
- permite `MR + 1050`;
- permite `MD + 1050`;
- rechaza otro `MR + 1050`;
- conserva opcionales y fechas.

### 17.4. Interfaz

- abre el formulario desde el FAB;
- muestra errores obligatorios;
- guarda un formulario válido;
- cancela;
- conserva datos tras rotación;
- muestra duplicado.

### 17.5. Manuales

- teclado abierto;
- pantalla pequeña;
- textos largos;
- espacios externos;
- minúsculas;
- posición vacía;
- observaciones vacías;
- pulsaciones repetidas;
- rotación;
- volver atrás;
- duplicado;
- mismo código con otra categoría;
- cierre y reapertura;
- funcionamiento sin conexión.

---

## 18. Tareas de implementación

1. Confirmar que HU-01 está integrada en `develop`.
2. Crear `feature/hu-02-registrar-mercancia` desde `develop`.
3. Revisar `WarehouseItemRepository`.
4. Añadir `CreateWarehouseItemUseCase`.
5. Crear comando y resultado.
6. Implementar `CreateWarehouseItemService`.
7. Ampliar el puerto de salida.
8. Implementar inserción en Room.
9. Añadir `insert()` con `ABORT`.
10. Confirmar índice único.
11. Implementar normalización.
12. Implementar validación.
13. Generar fechas.
14. Crear estado de formulario.
15. Crear ViewModel y Factory.
16. Actualizar `AppContainer`.
17. Crear `ItemFormActivity` y layout.
18. Conectar el FAB.
19. Implementar Guardar y Cancelar.
20. Implementar errores de campo.
21. Implementar Saving.
22. Implementar evento único de éxito.
23. Implementar duplicado y error general.
24. Comprobar actualización automática.
25. Crear pruebas.
26. Ejecutar `./gradlew testDebugUnitTest`.
27. Ejecutar `./gradlew lintDebug`.
28. Ejecutar `./gradlew assembleDebug`.
29. Publicar commits representativos.
30. Verificar CI en la rama.
31. Revisar criterios.
32. Fusionar localmente en `develop`.
33. Verificar CI en `develop`.
34. Eliminar la rama tras confirmar la integración.

---

## 19. Evidencias necesarias para cerrar la HU

- formulario vacío;
- errores obligatorios;
- formulario con y sin posición;
- registro guardado;
- nuevo elemento en el listado;
- normalización;
- coexistencia de `MR + 1050` y `MD + 1050`;
- rechazo del segundo `MR + 1050`;
- conservación tras rotación;
- bloqueo de doble guardado;
- pruebas unitarias y DAO;
- lint;
- compilación debug;
- CI en la rama;
- merge local en `develop`;
- CI en `develop`;
- confirmación de que la Activity no accede a Room.

---

## 20. Definición de terminado

La HU-02 estará terminada cuando:

- el formulario sea accesible desde el listado;
- categoría, código y sitio sean obligatorios;
- posición y observaciones sean opcionales;
- los errores sean comprensibles;
- los datos se normalicen fuera de la Activity;
- las fechas se generen automáticamente;
- un registro válido se guarde en Room;
- el nuevo registro aparezca automáticamente;
- el mismo código pueda existir en categorías distintas;
- la misma combinación sea rechazada;
- Room use `OnConflictStrategy.ABORT`;
- no se use `REPLACE`;
- el formulario sobreviva a rotación;
- Guardar no duplique inserciones;
- Cancelar no guarde;
- los errores técnicos estén controlados;
- la inserción no bloquee el hilo principal;
- la UI dependa del puerto de entrada;
- el dominio no dependa de Android ni Room;
- pruebas, lint y compilación finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios estén verificados;
- la rama se fusione en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 21. Resultado esperado

```text
usuario abre el formulario
        ↓
introduce los datos
        ↓
la aplicación valida y normaliza
        ↓
el caso de uso crea la mercancía
        ↓
Room almacena el registro
        ↓
la Activity vuelve al listado
        ↓
LiveData muestra el nuevo elemento
```

Después de cerrar la HU-02, el proyecto podrá continuar con:

```text
HU-03 — Consultar detalle de mercancía
```
