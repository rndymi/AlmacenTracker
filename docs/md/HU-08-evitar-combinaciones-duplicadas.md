# HU-08 — Evitar combinaciones duplicadas

> Octava historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-08  
**Nombre:** Evitar combinaciones duplicadas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-08-evitar-combinaciones-duplicadas`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero recibir un aviso cuando intento registrar o modificar una mercancía con una combinación de categoría y código que ya existe,  
para evitar registros duplicados y mantener la información consistente.

---

## 3. Objetivo

Cerrar formalmente la regla de identidad funcional de AlmacenTracker:

```text
categoría normalizada + código normalizado
```

La HU-08 consolidará y verificará de extremo a extremo el control de duplicados en:

- creación;
- edición;
- servicios de aplicación;
- puerto de salida;
- adaptador Room;
- DAO;
- índice único compuesto;
- mensajes de interfaz;
- pruebas unitarias;
- pruebas instrumentadas.

La comprobación previa permitirá ofrecer una respuesta comprensible antes de intentar escribir cuando sea posible. Sin embargo, Room seguirá siendo la garantía definitiva frente a condiciones de carrera, errores de implementación o escrituras concurrentes futuras.

Flujo previsto en creación:

```text
ItemFormActivity
        ↓
WarehouseItemFormViewModel
        ↓
CreateWarehouseItemUseCase
        ↓
CreateWarehouseItemService
        ↓ normaliza categoría + código
WarehouseItemRepository.existsByCategoryAndCode(...)
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
si no existe
        ↓
insert con ABORT
```

Flujo previsto en edición:

```text
ItemFormActivity
        ↓
WarehouseItemFormViewModel
        ↓
UpdateWarehouseItemUseCase
        ↓
UpdateWarehouseItemService
        ↓ normaliza categoría + código
WarehouseItemRepository.existsByCategoryAndCodeExcludingId(...)
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
si no pertenece a otro registro
        ↓
update con ABORT
```

---

## 4. Estado real del proyecto antes de la HU-08

El ZIP actualizado de HU-07 confirma que ya existen las siguientes defensas:

- `WarehouseItemEntity` contiene un índice único sobre `category` y `code`;
- `WarehouseItemDao.insert()` utiliza `OnConflictStrategy.ABORT`;
- `WarehouseItemDao.update()` utiliza `OnConflictStrategy.ABORT`;
- `RoomWarehouseItemRepository` captura `SQLiteConstraintException`;
- la inserción transforma el conflicto en `WarehouseItemInsertCallback.onDuplicate()`;
- la actualización transforma el conflicto en `WarehouseItemUpdateCallback.onDuplicate()`;
- `CreateWarehouseItemResult` contiene `DUPLICATE`;
- `UpdateWarehouseItemResult` contiene `DUPLICATE`;
- `WarehouseItemFormViewModel` muestra mensajes distintos para alta y edición;
- `CreateWarehouseItemService` normaliza categoría y código;
- `UpdateWarehouseItemService` normaliza categoría y código;
- existen pruebas unitarias parciales para resultados duplicados;
- existen pruebas DAO parciales para la restricción compuesta.

Por tanto, la HU-08 no parte desde cero.

Todavía falta cerrar formalmente:

- comprobación previa de existencia en alta;
- comprobación previa excluyendo el propio `id` en edición;
- contratos explícitos para consultar identidad funcional;
- comportamiento uniforme entre alta y edición;
- cobertura de normalización y diferencias de mayúsculas;
- cobertura de espacios externos;
- cobertura del mismo código en categorías diferentes;
- comprobación de que el propio registro no se considera duplicado;
- comprobación de que Room continúa protegiendo aunque falle o se omita la validación previa;
- mensajes visuales consistentes;
- pruebas de integración completas;
- revisión de migración o esquema si el índice no estuviera correctamente generado.

---

## 5. Alcance incluido

La HU-08 incluye:

- definir formalmente la identidad funcional;
- revisar el índice único compuesto de Room;
- mantener `OnConflictStrategy.ABORT`;
- prohibir `REPLACE`;
- añadir consulta previa para alta;
- añadir consulta previa para edición excluyendo el propio registro;
- normalizar categoría y código antes de comprobar;
- comparar sin distinguir mayúsculas después de normalizar;
- eliminar espacios externos antes de comprobar;
- permitir el mismo código en categorías distintas;
- impedir la misma categoría con el mismo código;
- permitir editar sin cambiar identidad;
- permitir editar hacia una combinación disponible;
- impedir editar hacia una combinación perteneciente a otro registro;
- conservar el formulario ante duplicado;
- mostrar un mensaje comprensible;
- impedir doble envío;
- mantener Room como defensa final;
- distinguir duplicado de error de persistencia;
- ejecutar consultas y escrituras fuera del hilo principal cuando corresponda;
- ampliar pruebas unitarias;
- ampliar pruebas DAO;
- añadir pruebas de repositorio;
- añadir pruebas de ViewModel e interfaz necesarias;
- verificar comportamiento en creación y edición;
- verificar comportamiento después de eliminar un registro;
- verificar persistencia tras cerrar y abrir la aplicación.

---

## 6. Alcance excluido

La HU-08 no incluye:

- modificar el modelo de identidad para usar solo código;
- permitir duplicados temporales;
- fusionar registros duplicados;
- sugerir automáticamente otra categoría o código;
- renombrar categorías de forma masiva;
- catálogo configurable de categorías;
- detección de duplicados por sitio o posición;
- similitud fonética;
- detección difusa;
- limpieza automática de duplicados ya existentes;
- importación o exportación;
- sincronización remota;
- resolución de conflictos de red;
- auditoría;
- historial de cambios;
- selección múltiple;
- eliminación múltiple.

La identidad funcional continuará siendo exclusivamente:

```text
categoría + código
```

---

## 7. Precondiciones

Antes de comenzar la HU-08 deberán cumplirse estas condiciones:

- HU-01 integrada en `develop`;
- HU-02 integrada en `develop`;
- HU-03 integrada en `develop`;
- HU-04 integrada en `develop`;
- HU-05 integrada en `develop`;
- HU-06 integrada en `develop`;
- HU-07 integrada en `develop`;
- CI de `develop` satisfactoria;
- alta operativa;
- edición operativa;
- eliminación operativa;
- índice único compuesto existente;
- Room como única fuente de verdad;
- resultados `DUPLICATE` existentes;
- formularios conservan sus valores ante error.

---

## 8. Definición de identidad funcional

Dos registros se consideran duplicados cuando, después de aplicar la normalización oficial, cumplen:

```text
registroA.category == registroB.category
AND
registroA.code == registroB.code
```

Ejemplo duplicado:

```text
MR + 1050
MR + 1050
```

Ejemplo permitido:

```text
MR + 1050
MD + 1050
```

Ejemplo duplicado tras normalización:

```text
" mr " + " 1050 "
"MR"   + "1050"
```

Resultado normalizado:

```text
MR + 1050
MR + 1050
```

---

## 9. Reglas de normalización aplicables

Antes de validar duplicados:

### Categoría

```text
null → ""
trim()
toUpperCase(Locale.ROOT)
```

### Código

```text
null → ""
trim()
toUpperCase(Locale.ROOT)
```

Ejemplos:

```text
" mr " → "MR"
"ab-10" → "AB-10"
" 1050 " → "1050"
```

La normalización deberá ejecutarse en la capa de aplicación, no en la Activity.

La validación previa y la escritura deberán utilizar exactamente los mismos valores normalizados.

No se permitirá:

```text
comprobar valores sin normalizar
        ↓
guardar valores normalizados
```

porque podría generar resultados inconsistentes.

---

## 10. Reglas funcionales

### 10.1. Alta con combinación disponible

Si no existe la combinación normalizada:

```text
category + code
```

la inserción deberá continuar.

### 10.2. Alta con combinación ocupada

Si ya existe la combinación:

- no se insertará otra fila;
- se conservarán los datos del formulario;
- se mostrará un mensaje comprensible;
- no se cerrará la Activity.

Mensaje orientativo:

```text
Ya existe una mercancía con esta categoría y código.
```

### 10.3. Código repetido en otra categoría

Debe permitirse:

```text
MR + 1050
MD + 1050
```

### 10.4. Edición sin cambiar identidad

Debe permitirse editar:

```text
id = 7
MR + 1050
```

manteniendo:

```text
MR + 1050
```

La comprobación deberá excluir:

```text
id = 7
```

### 10.5. Edición hacia combinación disponible

Debe permitirse cambiar:

```text
id = 7
MR + 1050
```

a:

```text
MR + 1051
```

si la nueva combinación no pertenece a otro registro.

### 10.6. Edición hacia combinación ocupada

Si existe:

```text
id = 7 → MR + 1050
id = 9 → MD + 1050
```

no deberá permitirse cambiar `id = 9` a:

```text
MR + 1050
```

### 10.7. Eliminación libera la combinación

Si se elimina:

```text
MR + 1050
```

deberá ser posible registrar nuevamente:

```text
MR + 1050
```

La HU-08 no implementará restauración del registro anterior.

### 10.8. Room es la defensa final

Aunque la consulta previa devuelva que la combinación está libre, la operación final deberá continuar usando:

```java
OnConflictStrategy.ABORT
```

y capturando:

```java
SQLiteConstraintException
```

### 10.9. Duplicado no es error genérico

No deberá mostrarse:

```text
No se pudo guardar.
```

cuando el problema sea una combinación duplicada conocida.

### 10.10. No usar REPLACE

Queda prohibido:

```java
OnConflictStrategy.REPLACE
```

porque podría:

- borrar una fila existente;
- generar otro id;
- alterar fechas;
- ocultar el conflicto;
- sobrescribir información sin consentimiento.

---

## 11. Comprobación previa

### 11.1. Alta

Consulta conceptual:

```sql
SELECT EXISTS(
    SELECT 1
    FROM warehouse_items
    WHERE category = :category COLLATE NOCASE
      AND code = :code COLLATE NOCASE
)
```

Firma DAO orientativa:

```java
@Query(
    "SELECT EXISTS(" +
    "SELECT 1 FROM warehouse_items " +
    "WHERE category = :category COLLATE NOCASE " +
    "AND code = :code COLLATE NOCASE" +
    ")"
)
boolean existsByCategoryAndCode(
        String category,
        String code
);
```

### 11.2. Edición

Consulta conceptual:

```sql
SELECT EXISTS(
    SELECT 1
    FROM warehouse_items
    WHERE category = :category COLLATE NOCASE
      AND code = :code COLLATE NOCASE
      AND id <> :excludedId
)
```

Firma DAO orientativa:

```java
@Query(
    "SELECT EXISTS(" +
    "SELECT 1 FROM warehouse_items " +
    "WHERE category = :category COLLATE NOCASE " +
    "AND code = :code COLLATE NOCASE " +
    "AND id <> :excludedId" +
    ")"
)
boolean existsByCategoryAndCodeExcludingId(
        String category,
        String code,
        long excludedId
);
```

### 11.3. Ejecución

Estas consultas deberán ejecutarse mediante el executor utilizado por el repositorio.

No se permitirá acceso síncrono desde la Activity o ViewModel.

---

## 12. Flujo principal de creación

1. El usuario abre el formulario de alta.
2. Introduce categoría, código y sitio.
3. Pulsa Guardar.
4. El ViewModel bloquea un segundo envío.
5. `CreateWarehouseItemService` normaliza.
6. Valida los campos obligatorios.
7. Consulta si categoría + código ya existen.
8. El repositorio ejecuta la consulta en Room.
9. Room devuelve que no existe.
10. El servicio genera fechas.
11. Solicita la inserción.
12. El DAO inserta con `ABORT`.
13. Room devuelve el id.
14. El ViewModel emite éxito.
15. La Activity finaliza.
16. El listado se actualiza.

---

## 13. Flujo principal de edición

1. El usuario abre una mercancía.
2. Pulsa Editar.
3. El formulario carga el registro.
4. Modifica los campos.
5. Pulsa Guardar cambios.
6. El ViewModel bloquea un segundo envío.
7. `UpdateWarehouseItemService` normaliza.
8. Valida campos obligatorios.
9. Recupera el registro original.
10. Consulta la combinación excluyendo el mismo id.
11. Room devuelve que no pertenece a otro registro.
12. El servicio conserva `id` y `createdAt`.
13. Genera un nuevo `updatedAt`.
14. Solicita la actualización.
15. El DAO actualiza con `ABORT`.
16. Room confirma una fila afectada.
17. El formulario emite éxito.
18. El detalle se actualiza.

---

## 14. Flujos alternativos

### FA-01 — Duplicado exacto en alta

1. Existe `MR + 1050`.
2. El usuario introduce `MR + 1050`.
3. La comprobación previa devuelve existente.
4. No se invoca `insert()`.
5. Se muestra `DUPLICATE`.

### FA-02 — Duplicado con minúsculas

1. Existe `MR + 1050`.
2. El usuario introduce `mr + 1050`.
3. La normalización produce `MR + 1050`.
4. Se rechaza.

### FA-03 — Duplicado con espacios

1. Existe `MR + 1050`.
2. El usuario introduce `" MR "` y `" 1050 "`.
3. Se normaliza.
4. Se rechaza.

### FA-04 — Mismo código, otra categoría

1. Existe `MR + 1050`.
2. El usuario introduce `MD + 1050`.
3. La consulta previa devuelve libre.
4. Se inserta.

### FA-05 — Editar ubicación sin cambiar identidad

1. El usuario edita `MR + 1050`.
2. Cambia sitio o posición.
3. La consulta excluye su id.
4. No se considera duplicado.
5. Se actualiza.

### FA-06 — Editar hacia combinación ocupada

1. Otro registro posee la combinación.
2. La consulta previa devuelve existente.
3. No se invoca `update()`.
4. Se conserva el formulario.
5. Se muestra mensaje específico.

### FA-07 — Condición de carrera o inconsistencia

1. La validación previa devuelve libre.
2. Antes de guardar aparece la misma combinación.
3. Room rechaza mediante índice único.
4. El repositorio captura `SQLiteConstraintException`.
5. Se devuelve `DUPLICATE`.

### FA-08 — Registro eliminado

1. Se elimina `MR + 1050`.
2. Se intenta crear de nuevo.
3. La consulta previa devuelve libre.
4. La inserción se realiza.

### FA-09 — Error al comprobar existencia

1. Room produce una excepción en la consulta previa.
2. No se intenta guardar.
3. Se devuelve `PERSISTENCE_ERROR`.
4. Los datos permanecen.

### FA-10 — Doble guardado

1. El usuario pulsa Guardar repetidamente.
2. Solo se ejecuta una secuencia de comprobación y escritura.

### FA-11 — Rotación tras duplicado

1. El formulario muestra el mensaje de duplicado.
2. El usuario rota.
3. Los valores permanecen.
4. No se reintenta automáticamente la operación.

---

## 15. Criterios de aceptación

### CA-01 — Identidad compuesta

**Dado** dos registros,  
**cuando** tienen la misma categoría y código normalizados,  
**entonces** se consideran duplicados.

### CA-02 — Alta duplicada

**Dado** que existe `MR + 1050`,  
**cuando** se intenta registrar nuevamente,  
**entonces** no se crea otra fila.

### CA-03 — Mismo código en otra categoría

**Dado** que existe `MR + 1050`,  
**cuando** se registra `MD + 1050`,  
**entonces** se permite.

### CA-04 — Mayúsculas y minúsculas

**Dado** que existe `MR + 1050`,  
**cuando** se intenta registrar `mr + 1050`,  
**entonces** se rechaza.

### CA-05 — Espacios externos

**Dado** que existe `MR + 1050`,  
**cuando** se introducen espacios externos,  
**entonces** se rechaza después de normalizar.

### CA-06 — Edición propia

**Dado** un registro existente,  
**cuando** se edita sin cambiar categoría y código,  
**entonces** se permite.

### CA-07 — Edición a combinación disponible

**Dado** una combinación libre,  
**cuando** un registro se edita hacia ella,  
**entonces** se permite.

### CA-08 — Edición a combinación ocupada

**Dado** que otro registro posee la combinación,  
**cuando** se intenta editar hacia ella,  
**entonces** se rechaza.

### CA-09 — Exclusión por id

**Dado** un registro editado,  
**cuando** se comprueba duplicado,  
**entonces** su propio id queda excluido.

### CA-10 — Índice único conservado

**Dado** el esquema Room,  
**cuando** se inspecciona la entidad o base,  
**entonces** existe un índice único sobre `category` y `code`.

### CA-11 — ABORT en inserción

**Dado** un conflicto durante insert,  
**cuando** Room procesa la escritura,  
**entonces** aborta sin reemplazar otra fila.

### CA-12 — ABORT en actualización

**Dado** un conflicto durante update,  
**cuando** Room procesa la escritura,  
**entonces** aborta sin sustituir otra fila.

### CA-13 — Defensa final

**Dado** que la comprobación previa no detecta el conflicto,  
**cuando** Room encuentra un duplicado,  
**entonces** se devuelve `DUPLICATE`.

### CA-14 — Mensaje comprensible

**Dado** un duplicado,  
**cuando** el formulario recibe el resultado,  
**entonces** muestra un mensaje específico.

### CA-15 — Datos conservados

**Dado** un duplicado,  
**cuando** se muestra el error,  
**entonces** los campos permanecen con los valores introducidos.

### CA-16 — Sin cierre de Activity

**Dado** un duplicado,  
**cuando** la operación falla,  
**entonces** el formulario continúa abierto.

### CA-17 — Combinación liberada

**Dado** que el único registro con una combinación fue eliminado,  
**cuando** se registra de nuevo,  
**entonces** se permite.

### CA-18 — Error diferenciado

**Dado** un error inesperado,  
**cuando** falla la consulta o escritura,  
**entonces** no se presenta como duplicado.

### CA-19 — Doble envío bloqueado

**Dado** que la validación o escritura está en curso,  
**cuando** se pulsa Guardar otra vez,  
**entonces** no se ejecuta otra operación.

### CA-20 — Operación no bloqueante

**Dado** que se consulta o escribe Room,  
**cuando** se procesa la operación,  
**entonces** no se bloquea el hilo principal.

---

## 16. Diseño técnico propuesto

### 16.1. Puerto de salida

`WarehouseItemRepository` podrá ampliarse con:

```java
void existsByCategoryAndCode(
        String category,
        String code,
        WarehouseItemDuplicateCheckCallback callback
);
```

y:

```java
void existsByCategoryAndCodeExcludingId(
        String category,
        String code,
        long excludedId,
        WarehouseItemDuplicateCheckCallback callback
);
```

También es válida una única operación:

```java
void existsByCategoryAndCode(
        String category,
        String code,
        Long excludedId,
        WarehouseItemDuplicateCheckCallback callback
);
```

La opción elegida deberá priorizar claridad y evitar valores mágicos.

### 16.2. Callback

Se recomienda:

```java
public interface WarehouseItemDuplicateCheckCallback {

    void onResult(boolean exists);

    void onError(Throwable throwable);
}
```

No se reutilizarán callbacks de inserción o actualización porque la consulta no representa una escritura.

### 16.3. DAO

Se añadirán las consultas definidas en la sección 11.

El DAO no normalizará valores. Recibirá valores ya normalizados.

### 16.4. Adaptador Room

`RoomWarehouseItemRepository` deberá:

- ejecutar consultas en el executor;
- invocar el DAO;
- devolver `exists`;
- transformar excepciones;
- mantener insert y update con `ABORT`;
- continuar capturando `SQLiteConstraintException`.

### 16.5. CreateWarehouseItemService

Deberá evolucionar para:

1. normalizar;
2. validar;
3. comprobar duplicado;
4. si existe, devolver `DUPLICATE`;
5. si no existe, generar fechas;
6. insertar;
7. conservar manejo de constraint.

### 16.6. UpdateWarehouseItemService

Deberá evolucionar para:

1. validar id;
2. normalizar;
3. validar campos;
4. recuperar original;
5. comprobar duplicado excluyendo `original.getId()`;
6. si existe, devolver `DUPLICATE`;
7. construir el modelo actualizado;
8. actualizar;
9. conservar manejo de constraint.

### 16.7. Resultados existentes

Se reutilizarán:

```text
CreateWarehouseItemResult.DUPLICATE
UpdateWarehouseItemResult.DUPLICATE
```

No se creará un nuevo resultado de formulario si los existentes ya representan correctamente el caso.

### 16.8. ViewModel

`WarehouseItemFormViewModel` deberá conservar:

- estado `saving`;
- bloqueo de doble envío;
- valores introducidos;
- mensaje específico de creación;
- mensaje específico de edición;
- reactivación de Guardar tras duplicado.

### 16.9. Activity

`ItemFormActivity` continuará limitándose a:

- observar estado;
- mostrar errores;
- conservar campos;
- no consultar duplicados;
- no acceder a Room.

### 16.10. Composición

`AppContainer` no necesitará un caso de uso nuevo si la comprobación forma parte de creación y actualización.

Solo deberá adaptarse si cambian constructores o dependencias.

---

## 17. Decisiones técnicas importantes

### 17.1. La HU-08 es de consolidación

No se duplicarán clases que ya funcionan.

Se ampliarán las rutas existentes de creación y edición.

### 17.2. No crear CheckDuplicateWarehouseItemUseCase para la UI

La comprobación no será una acción separada del usuario ni se ejecutará mientras escribe.

Formará parte atómica del intento de guardar a nivel de aplicación.

### 17.3. No confiar únicamente en la comprobación previa

La secuencia:

```text
exists → insert/update
```

no es una garantía transaccional absoluta.

El índice único seguirá siendo obligatorio.

### 17.4. No mostrar disponibilidad en tiempo real

No se añadirá:

```text
“Código disponible”
```

mientras el usuario escribe.

Esto introduciría consultas frecuentes y una promesa que podría quedar obsoleta antes de guardar.

### 17.5. Comparación coherente

Alta, edición, consulta previa e índice deberán operar sobre valores normalizados.

### 17.6. No cambiar versión de base sin necesidad

Si el índice ya existe correctamente, no se incrementará la versión Room ni se creará una migración artificial.

Si se detecta que el esquema instalado no contiene el índice, deberá añadirse una migración real y pruebas de migración.

### 17.7. No destruir datos

Queda prohibido resolver un cambio de esquema mediante:

```java
fallbackToDestructiveMigration()
```

en una versión estable, salvo entorno de pruebas expresamente aislado.

---

## 18. Estructura de archivos orientativa

La HU-08 podrá añadir o modificar:

```text
<package-root>/
├── application/
│   ├── port/
│   │   └── out/
│   │       ├── WarehouseItemRepository.java
│   │       └── WarehouseItemDuplicateCheckCallback.java
│   └── service/
│       ├── CreateWarehouseItemService.java
│       └── UpdateWarehouseItemService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       └── viewmodel/
│   │           └── WarehouseItemFormViewModel.java
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               │   └── WarehouseItemDao.java
│               ├── entity/
│               │   └── WarehouseItemEntity.java
│               └── repository/
│                   └── RoomWarehouseItemRepository.java
└── configuration/
    └── AppContainer.java
```

También se modificarán pruebas existentes de:

```text
CreateWarehouseItemServiceTest
UpdateWarehouseItemServiceTest
WarehouseItemDaoTest
```

No se crearán paquetes o servicios sin una responsabilidad real.

---

## 19. Pruebas recomendadas

### 19.1. CreateWarehouseItemService

- consulta duplicado con valores normalizados;
- no consulta si faltan campos obligatorios;
- devuelve duplicate cuando existe;
- no invoca insert cuando existe;
- inserta cuando no existe;
- mantiene defensa ante `onDuplicate()` de insert;
- diferencia duplicate de persistence error;
- bloquea doble flujo mediante ViewModel.

### 19.2. UpdateWarehouseItemService

- consulta excluyendo el id original;
- permite mantener identidad;
- permite combinación disponible;
- rechaza combinación de otro registro;
- no invoca update cuando existe;
- mantiene defensa ante `onDuplicate()` de update;
- conserva id y createdAt;
- actualiza updatedAt solo cuando corresponde;
- devuelve not found si el original no existe.

### 19.3. DAO

- `existsByCategoryAndCode()` devuelve true;
- devuelve false para combinación libre;
- permite mismo código en distinta categoría;
- ignora capitalización;
- `existsByCategoryAndCodeExcludingId()` excluye el propio id;
- detecta otro id;
- insert rechaza duplicado exacto;
- update rechaza conflicto;
- no reemplaza filas;
- eliminar libera la combinación.

### 19.4. RoomWarehouseItemRepository

- ejecuta comprobación en executor;
- devuelve exists true;
- devuelve exists false;
- transforma error;
- insert conserva captura de constraint;
- update conserva captura de constraint.

### 19.5. ViewModel

- muestra mensaje de alta duplicada;
- muestra mensaje de edición duplicada;
- conserva campos;
- reactivar Guardar;
- no emite éxito;
- no cierra formulario;
- no repite evento tras rotación.

### 19.6. Pruebas instrumentadas

- registrar `MR + 1050`;
- rechazar segundo `MR + 1050`;
- permitir `MD + 1050`;
- rechazar variantes con espacios;
- rechazar variantes con minúsculas;
- editar sin cambiar identidad;
- editar solo ubicación;
- editar hacia combinación libre;
- rechazar combinación de otro registro;
- eliminar y volver a registrar;
- persistir restricción tras cerrar y abrir.

### 19.7. Pruebas manuales

- alta duplicada;
- alta con otra categoría;
- edición propia;
- edición a conflicto;
- espacios;
- minúsculas;
- doble pulsación;
- rotación;
- error simulado;
- eliminación y reutilización;
- funcionamiento sin conexión.

---

## 20. Tareas de implementación

1. Confirmar HU-07 integrada en `develop`.
2. Verificar CI satisfactoria en `develop`.
3. Analizar el ZIP actualizado de HU-07.
4. Crear `feature/hu-08-evitar-combinaciones-duplicadas`.
5. Auditar índice único de `WarehouseItemEntity`.
6. Confirmar nombres reales `category` y `code`.
7. Confirmar `ABORT` en insert.
8. Confirmar `ABORT` en update.
9. Crear `WarehouseItemDuplicateCheckCallback`.
10. Ampliar `WarehouseItemRepository`.
11. Añadir `existsByCategoryAndCode()` al DAO.
12. Añadir consulta excluyendo id.
13. Implementar consultas en `RoomWarehouseItemRepository`.
14. Ejecutarlas en el executor.
15. Ampliar `CreateWarehouseItemService`.
16. Comprobar antes de insertar.
17. Mantener captura de constraint.
18. Ampliar `UpdateWarehouseItemService`.
19. Excluir el id original.
20. Mantener captura de constraint.
21. Revisar mensajes de `WarehouseItemFormViewModel`.
22. Confirmar conservación del formulario.
23. Confirmar bloqueo de doble envío.
24. Revisar `AppContainer`.
25. Ampliar pruebas de creación.
26. Ampliar pruebas de actualización.
27. Ampliar pruebas DAO.
28. Añadir pruebas del repositorio si faltan.
29. Añadir pruebas de ViewModel necesarias.
30. Ejecutar `./gradlew testDebugUnitTest`.
31. Ejecutar `./gradlew lintDebug`.
32. Ejecutar `./gradlew assembleDebug`.
33. Ejecutar pruebas instrumentadas.
34. Publicar commits representativos.
35. Verificar CI en la rama.
36. Revisar criterios de aceptación.
37. Fusionar localmente en `develop`.
38. Verificar CI en `develop`.
39. Eliminar la rama local y remota tras confirmar la integración.

---

## 21. Evidencias necesarias para cerrar la HU

- evidencia del índice único compuesto;
- evidencia de `ABORT` en insert;
- evidencia de `ABORT` en update;
- alta de combinación disponible;
- rechazo de alta duplicada;
- mismo código en categoría diferente;
- rechazo con minúsculas;
- rechazo con espacios externos;
- edición sin cambiar identidad;
- edición solo de ubicación;
- edición hacia combinación libre;
- rechazo al editar hacia combinación ocupada;
- evidencia de exclusión del propio id;
- evidencia de consulta previa;
- evidencia de defensa final de Room;
- evidencia de datos conservados;
- evidencia de formulario abierto tras duplicado;
- evidencia de doble envío bloqueado;
- evidencia de eliminación y reutilización;
- pruebas unitarias;
- pruebas DAO;
- pruebas instrumentadas;
- lint;
- compilación debug;
- CI satisfactoria en `feature/hu-08-evitar-combinaciones-duplicadas`;
- merge local en `develop`;
- CI satisfactoria en `develop`;
- confirmación de que la Activity no accede a Room;
- confirmación de que no se usa `REPLACE`;
- confirmación de que no existe migración destructiva.

---

## 22. Definición de terminado

La HU-08 estará terminada cuando:

- categoría + código sean la identidad funcional formal;
- ambos valores se normalicen antes de comparar;
- exista comprobación previa en alta;
- exista comprobación previa en edición;
- la edición excluya su propio id;
- un duplicado de alta no invoque insert;
- un duplicado de edición no invoque update;
- el mismo código en otra categoría se permita;
- minúsculas no eviten la detección;
- espacios externos no eviten la detección;
- Room conserve el índice único;
- insert utilice `ABORT`;
- update utilice `ABORT`;
- no se utilice `REPLACE`;
- `SQLiteConstraintException` siga transformándose a duplicate;
- Room continúe siendo la defensa final;
- el mensaje sea comprensible;
- el formulario conserve los datos;
- la Activity no se cierre;
- duplicate se diferencie de persistence error;
- eliminar una combinación permita reutilizarla;
- las consultas y escrituras no bloqueen el hilo principal;
- la UI dependa de casos de uso;
- el dominio no dependa de Android ni Room;
- las pruebas unitarias finalicen correctamente;
- las pruebas DAO finalicen correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- lint y compilación debug finalicen correctamente;
- la CI de la rama finalice correctamente;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione localmente en `develop`;
- la CI de `develop` finalice correctamente;
- la rama se elimine tras verificar la integración.

---

## 23. Resultado esperado

Al cerrar la HU-08, la regla de duplicados quedará protegida de extremo a extremo:

```text
usuario guarda
        ↓
la aplicación normaliza
        ↓
comprueba categoría + código
        ↓
si existe → mensaje Duplicate
        ↓
si no existe → insert o update con ABORT
        ↓
Room valida nuevamente
        ↓
se conserva una única combinación válida
```

El proyecto quedará preparado para continuar con:

```text
HU-09 — Mostrar estados vacíos y sin resultados
```

---

## 24. Commit documental recomendado

```text
docs: add HU-08 duplicate warehouse item prevention plan
```
