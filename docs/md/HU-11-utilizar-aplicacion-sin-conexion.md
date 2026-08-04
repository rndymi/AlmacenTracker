# HU-11 — Utilizar la aplicación sin conexión

> Undécima historia de usuario de AlmacenTracker v1.0 y cierre funcional transversal de la versión.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-11  
**Nombre:** Utilizar la aplicación sin conexión  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-11-funcionamiento-sin-conexion`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero consultar y gestionar la mercancía sin conexión a Internet,  
para poder trabajar desde cualquier zona del almacén aunque no exista cobertura o acceso a una red.

---

## 3. Objetivo

Cerrar formalmente el comportamiento offline de AlmacenTracker v1.0.

Esta historia no debe añadir un modo offline artificial, porque la aplicación ya fue diseñada como una solución local basada en Room. Su objetivo es verificar de extremo a extremo que todas las funciones implementadas en HU-01 a HU-10:

- funcionan con Wi-Fi y datos móviles desactivados;
- no solicitan conexión;
- no dependen de autenticación;
- no invocan servicios remotos;
- conservan los datos tras cerrar y volver a abrir la aplicación;
- conservan los datos tras reiniciar el dispositivo o emulador;
- siguen utilizando Room como única fuente de verdad;
- no muestran errores de red inexistentes;
- no bloquean operaciones por falta de conectividad.

Flujo real esperado:

```text
Activity / ViewModel
        ↓
puerto de entrada
        ↓
servicio de aplicación
        ↓
WarehouseItemRepository
        ↓
RoomWarehouseItemRepository
        ↓
WarehouseItemDao
        ↓
Room / SQLite local
```

No debe existir en v1.0:

```text
Activity
    ↓
API / Firebase / backend / nube
```

---

## 4. Naturaleza transversal de HU-11

HU-11 ha estado parcialmente cubierta desde HU-01 porque todas las operaciones se han construido sobre persistencia local.

Su cierre se realiza al final porque ahora pueden verificarse todas las funciones de la versión:

- listado;
- registro;
- detalle;
- búsqueda;
- filtros;
- edición;
- eliminación individual;
- control de duplicados;
- estados vacíos y sin resultados;
- eliminación múltiple.

Por tanto, HU-11 es principalmente una historia de:

```text
auditoría técnica
+ pruebas offline
+ persistencia
+ prevención de regresiones
+ evidencias de cierre
```

No es una historia para crear una capa remota inexistente.

---

## 5. Estado real del proyecto antes de HU-11

El ZIP actualizado de HU-10 confirma que el proyecto dispone de:

- aplicación Android Java;
- Android Views;
- ViewModel y LiveData;
- Room como persistencia;
- SQLite local;
- arquitectura hexagonal pragmática;
- `WarehouseItemRepository` como puerto de salida;
- `RoomWarehouseItemRepository` como único adaptador de persistencia;
- `WarehouseItemDao`;
- `AlmacenTrackerDatabase`;
- executor para operaciones no bloqueantes;
- CRUD individual completo;
- búsqueda y filtros locales;
- eliminación múltiple mediante Room;
- estados observables;
- pruebas unitarias;
- pruebas instrumentadas de DAO;
- CI y CD de artefacto Android.

La auditoría actual también confirma:

- `AndroidManifest.xml` no declara `android.permission.INTERNET`;
- no declara `android.permission.ACCESS_NETWORK_STATE`;
- no existen dependencias de Retrofit;
- no existen dependencias de OkHttp;
- no existen dependencias de Volley;
- no existen dependencias de Firebase;
- no existen dependencias de Supabase;
- no existen referencias funcionales a `java.net`;
- no existe `WebView` operativo;
- no existe autenticación;
- no existe backend;
- todas las funciones de mercancía terminan en Room.

Esto significa que HU-11 deberá consolidar y demostrar el comportamiento existente, no reestructurarlo sin necesidad.

---

## 6. Alcance incluido

HU-11 incluye:

- auditar permisos del Manifest;
- auditar dependencias Gradle;
- auditar referencias a APIs de red;
- confirmar que Room es la única fuente de datos;
- confirmar que `WarehouseItemRepository` solo tiene implementación local en v1.0;
- verificar inicio sin conexión;
- verificar listado sin conexión;
- verificar creación sin conexión;
- verificar consulta de detalle sin conexión;
- verificar búsqueda sin conexión;
- verificar filtros sin conexión;
- verificar edición sin conexión;
- verificar eliminación individual sin conexión;
- verificar prevención de duplicados sin conexión;
- verificar estados vacíos y sin resultados sin conexión;
- verificar selección y eliminación múltiple sin conexión;
- verificar actualización observable sin conexión;
- verificar persistencia tras cerrar la Activity;
- verificar persistencia tras forzar cierre de la aplicación;
- verificar persistencia tras abrir nuevamente;
- verificar persistencia tras reiniciar dispositivo o emulador;
- verificar funcionamiento en modo avión;
- verificar funcionamiento con Wi-Fi y datos móviles desactivados;
- verificar que no aparece un spinner esperando red;
- verificar que no aparece un error de conectividad;
- verificar que no se solicita iniciar sesión;
- verificar que la aplicación no intenta sincronizar;
- añadir pruebas de prevención de regresiones offline cuando sean viables;
- documentar una matriz completa de evidencias;
- ejecutar el conjunto completo de pruebas;
- preparar el cierre técnico de la versión 1.0.

---

## 7. Alcance excluido

HU-11 no incluye:

- crear backend;
- crear API REST;
- Retrofit;
- OkHttp;
- Firebase;
- Supabase;
- autenticación;
- usuarios;
- roles;
- sincronización;
- colas de sincronización;
- estado `pendingSync`;
- resolución de conflictos;
- detección activa de conectividad;
- `ConnectivityManager`;
- mensajes “Sin conexión” permanentes;
- reintentos de red;
- WorkManager para sincronización;
- caché HTTP;
- importación o exportación;
- copia de seguridad manual;
- restauración desde archivo;
- sincronización con Google Drive;
- cifrado nuevo de base de datos;
- migraciones no relacionadas;
- modo online;
- conmutador online/offline.

Estas capacidades pertenecen a versiones posteriores, especialmente v2.0.

---

## 8. Decisión principal: no añadir un modo offline

La aplicación no necesita:

```text
boolean offlineMode
```

ni:

```text
if (hasInternet) {
    ...
} else {
    ...
}
```

Todas las operaciones de v1.0 deben funcionar igual con o sin conexión.

Añadir detección de red en HU-11 sería contraproducente porque:

- introduciría una dependencia que hoy no existe;
- podría bloquear operaciones locales;
- aumentaría estados de UI innecesarios;
- no aportaría valor a una aplicación exclusivamente local;
- confundiría “sin conexión” con “sin funcionalidad”.

La ausencia de red no es un error en AlmacenTracker v1.0.

---

## 9. Fuente de verdad

La única fuente de verdad de la versión 1.0 será:

```text
Room / SQLite
```

Reglas:

- el adapter no conserva datos como fuente permanente;
- las Activities no mantienen copias autoritativas;
- los ViewModels conservan estado de presentación, no persistencia definitiva;
- los resultados de creación, edición o eliminación no modifican manualmente el listado;
- Room emite los cambios;
- cerrar una Activity no elimina datos;
- destruir el proceso no elimina datos;
- reiniciar el dispositivo no elimina datos;
- desinstalar la aplicación sí elimina los datos locales, salvo restauración gestionada por el sistema.

---

## 10. Persistencia esperada

### 10.1. Recreación de Activity

Después de una rotación:

- los datos de Room siguen disponibles;
- el ViewModel o la nueva observación recupera el estado correcto;
- no se duplica información.

### 10.2. Cierre normal

Después de cerrar la aplicación desde recientes y abrirla:

- los registros permanecen;
- listado, detalle, búsqueda y filtros vuelven a consultar Room.

### 10.3. Forzar detención

Después de forzar detención y abrir nuevamente:

- los registros permanecen;
- no se requiere red;
- no se requiere autenticación.

### 10.4. Reinicio del dispositivo

Después de reiniciar:

- Room conserva la base;
- la aplicación vuelve a mostrar los registros;
- no necesita descargar información.

### 10.5. Desinstalación

La desinstalación elimina el almacenamiento privado de la aplicación.

Esto no se considerará un fallo de HU-11.

Las copias de seguridad automáticas gestionadas por Android no forman parte de la lógica funcional de esta historia y no deberán presentarse como garantía de recuperación.

---

## 11. Matriz funcional offline

| Función | Dependencia esperada | Resultado sin conexión |
|---|---|---|
| Abrir aplicación | Room | Correcto |
| Consultar listado | Room observable | Correcto |
| Ver detalle | Consulta por id en Room | Correcto |
| Registrar | Insert local | Correcto |
| Buscar | Consulta SQL local | Correcto |
| Filtrar | Consulta SQL local | Correcto |
| Editar | Update local | Correcto |
| Eliminar uno | Delete local | Correcto |
| Evitar duplicados | Índice y consulta local | Correcto |
| Estados vacíos | Estado derivado de Room | Correcto |
| Eliminar varios | Delete por ids local | Correcto |
| Rotar | ViewModel + Room | Correcto |
| Cerrar y abrir | Archivo SQLite | Correcto |
| Reiniciar dispositivo | Archivo SQLite | Correcto |

---

## 12. Flujos principales de verificación

### 12.1. Consulta offline

1. Existen registros.
2. Se desactiva Wi-Fi.
3. Se desactivan datos móviles o se activa modo avión.
4. Se abre la aplicación.
5. Room recupera los registros.
6. El listado se muestra.
7. No aparece error de red.

### 12.2. Registro offline

1. La aplicación está sin conexión.
2. El usuario abre el formulario.
3. Introduce datos válidos.
4. Guarda.
5. Room inserta.
6. El registro aparece en el listado.
7. Se cierra la aplicación.
8. Se vuelve a abrir sin conexión.
9. El registro permanece.

### 12.3. Edición offline

1. La aplicación está sin conexión.
2. Se abre detalle.
3. Se edita ubicación.
4. Room actualiza.
5. El detalle y listado reflejan los cambios.
6. Tras reiniciar, los cambios permanecen.

### 12.4. Eliminación offline

1. La aplicación está sin conexión.
2. Se elimina un registro con confirmación.
3. Room elimina.
4. El registro desaparece.
5. Tras abrir nuevamente, no reaparece.

### 12.5. Eliminación múltiple offline

1. La aplicación está sin conexión.
2. Se seleccionan varios registros.
3. Se confirma.
4. Room ejecuta `DELETE ... IN (...)`.
5. Los registros desaparecen.
6. El estado se actualiza.
7. Tras reiniciar, continúan eliminados.

---

## 13. Flujos alternativos

### FA-01 — Base vacía sin conexión

La aplicación muestra `EMPTY_DATABASE`, no un error de red.

### FA-02 — Búsqueda sin resultados sin conexión

La aplicación muestra `NO_RESULTS` contextual.

### FA-03 — Filtros sin resultados sin conexión

La aplicación muestra el mensaje correspondiente y permite limpiar criterios.

### FA-04 — Duplicado sin conexión

Room y la comprobación local rechazan la combinación.

### FA-05 — Error de persistencia local

Un error real de SQLite o del repositorio se muestra como error de persistencia, no como error de red.

### FA-06 — Cambio de conectividad mientras se usa

Activar o desactivar Wi-Fi no modifica el estado funcional.

No se reinician consultas ni Activities.

### FA-07 — Red disponible

La aplicación continúa utilizando Room.

No cambia a una fuente remota porque no existe en v1.0.

### FA-08 — Reinicio con búsqueda o filtros previos

Los registros persisten.

No es obligatorio persistir búsqueda, filtros o selección entre cierres completos de proceso, salvo que ya exista esa funcionalidad.

La persistencia obligatoria de HU-11 se refiere a mercancía almacenada, no a estado temporal de interfaz.

### FA-09 — Eliminación y reinicio inmediato

Si Room confirmó la eliminación antes del cierre, el registro no debe reaparecer.

### FA-10 — Guardado interrumpido antes de confirmación

Si la escritura no se confirmó, no se asumirá éxito.

La UI debe esperar el resultado del callback existente.

---

## 14. Criterios de aceptación

### CA-01 — Inicio sin red

**Dado** que no existe conexión,  
**cuando** se abre la aplicación,  
**entonces** inicia normalmente.

### CA-02 — Listado sin red

**Dado** que existen registros locales,  
**cuando** se consulta sin conexión,  
**entonces** se muestran desde Room.

### CA-03 — Registro sin red

**Dado** un formulario válido,  
**cuando** se guarda sin conexión,  
**entonces** el registro se crea.

### CA-04 — Detalle sin red

**Dado** un id existente,  
**cuando** se abre sin conexión,  
**entonces** se muestran sus datos.

### CA-05 — Búsqueda sin red

**Dado** datos locales,  
**cuando** se busca sin conexión,  
**entonces** Room devuelve las coincidencias.

### CA-06 — Filtros sin red

**Dado** datos locales,  
**cuando** se aplican filtros sin conexión,  
**entonces** los resultados son correctos.

### CA-07 — Edición sin red

**Dado** un registro existente,  
**cuando** se edita sin conexión,  
**entonces** Room conserva los cambios.

### CA-08 — Eliminación individual sin red

**Dado** una eliminación confirmada,  
**cuando** se ejecuta sin conexión,  
**entonces** el registro se elimina.

### CA-09 — Eliminación múltiple sin red

**Dado** varios ids seleccionados,  
**cuando** se eliminan sin conexión,  
**entonces** solo esos registros desaparecen.

### CA-10 — Duplicados sin red

**Dado** una combinación existente,  
**cuando** se intenta repetir sin conexión,  
**entonces** se rechaza.

### CA-11 — Persistencia tras cierre

**Dado** un cambio confirmado,  
**cuando** se cierra y abre la aplicación,  
**entonces** el cambio permanece.

### CA-12 — Persistencia tras forzar detención

**Dado** datos confirmados,  
**cuando** se fuerza la detención y se abre,  
**entonces** permanecen.

### CA-13 — Persistencia tras reinicio

**Dado** datos confirmados,  
**cuando** se reinicia el dispositivo,  
**entonces** permanecen.

### CA-14 — Sin autenticación

**Dado** que se abre la aplicación,  
**cuando** no existe red,  
**entonces** no solicita iniciar sesión.

### CA-15 — Sin permiso INTERNET

**Dado** el Manifest de v1.0,  
**cuando** se inspecciona,  
**entonces** no declara `android.permission.INTERNET`.

### CA-16 — Sin permiso de estado de red

**Dado** el Manifest de v1.0,  
**cuando** se inspecciona,  
**entonces** no declara `android.permission.ACCESS_NETWORK_STATE`.

### CA-17 — Sin cliente remoto

**Dado** las dependencias de producción,  
**cuando** se inspeccionan,  
**entonces** no existe un cliente HTTP o SDK remoto funcional.

### CA-18 — Room como única implementación

**Dado** `WarehouseItemRepository`,  
**cuando** se revisa la composición,  
**entonces** `AppContainer` utiliza `RoomWarehouseItemRepository`.

### CA-19 — Sin mensaje de red

**Dado** que no existe conexión,  
**cuando** se usa cualquier operación local,  
**entonces** no aparece un error de conectividad.

### CA-20 — Red irrelevante

**Dado** que la aplicación está abierta,  
**cuando** se activa o desactiva la red,  
**entonces** las operaciones locales continúan.

### CA-21 — Datos temporales diferenciados

**Dado** que se destruye el proceso,  
**cuando** se abre nuevamente,  
**entonces** la mercancía persiste aunque selección, diálogo o borrador no confirmado no tengan que persistir.

### CA-22 — Error local controlado

**Dado** un fallo real de persistencia,  
**cuando** ocurre sin conexión,  
**entonces** se muestra el error local correcto y no se atribuye a Internet.

### CA-23 — CI independiente de servicios remotos funcionales

**Dado** el conjunto de pruebas de la aplicación,  
**cuando** se ejecuta CI,  
**entonces** no requiere levantar backend, Firebase ni emuladores de servicios remotos.

### CA-24 — Operaciones no bloqueantes

**Dado** cualquier escritura local,  
**cuando** Room la ejecuta,  
**entonces** no bloquea el hilo principal.

---

## 15. Auditoría técnica obligatoria

### 15.1. Manifest

Comprobar ausencia de:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

y:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

No deben añadirse “por si acaso”.

### 15.2. Dependencias

Auditar:

```text
app/build.gradle
gradle/libs.versions.toml
```

Comprobar ausencia funcional de:

- Retrofit;
- OkHttp;
- Volley;
- Firebase;
- Supabase;
- AWS SDK;
- GraphQL client;
- WebSocket client.

### 15.3. Código fuente

Buscar referencias a:

```text
java.net
HttpURLConnection
URLConnection
Socket
WebView
ConnectivityManager
NetworkCapabilities
```

Una referencia comentada en reglas de herramientas no constituye dependencia funcional, pero debe evaluarse.

### 15.4. Composición

Confirmar:

```text
AppContainer
    ↓
RoomWarehouseItemRepository
```

No debe existir selección dinámica basada en conectividad.

### 15.5. Repositorio

Confirmar que todas las operaciones de `WarehouseItemRepository` están implementadas localmente:

- observe;
- filter;
- detail;
- insert;
- update;
- duplicate check;
- delete;
- delete multiple.

---

## 16. Diseño técnico propuesto

### 16.1. No crear casos de uso nuevos

HU-11 no necesita:

```text
EnableOfflineModeUseCase
CheckInternetUseCase
ObserveConnectivityUseCase
```

No representan operaciones necesarias en una aplicación exclusivamente local.

### 16.2. No modificar el dominio

`WarehouseItem` no necesita:

```text
synced
pendingSync
remoteId
```

Estos campos pertenecerán a v2.0 cuando exista fuente remota.

### 16.3. No modificar Room sin necesidad

La versión de base de datos no deberá incrementarse si no cambia el esquema.

No se crearán migraciones artificiales.

### 16.4. Prueba de arquitectura offline

Se recomienda añadir una prueba JVM que inspeccione el Manifest y confirme que no aparecen permisos de red.

También puede añadirse una prueba de arquitectura o script Gradle que falle si se introducen dependencias remotas prohibidas.

La solución deberá ser simple y mantenible.

### 16.5. Prueba de persistencia real

La prueba instrumentada deberá utilizar una base Room persistente de prueba, no exclusivamente `inMemoryDatabaseBuilder`, para demostrar reapertura.

Flujo recomendado:

1. crear base con nombre de prueba;
2. insertar;
3. cerrar instancia;
4. crear una nueva instancia con el mismo archivo;
5. consultar;
6. verificar datos;
7. cerrar;
8. eliminar archivo de prueba.

No debe afectar la base real del usuario.

### 16.6. Prueba de reinicio de proceso

Espresso no reproduce completamente un reinicio real de dispositivo.

La evidencia final deberá combinar:

- prueba instrumentada de reapertura del archivo Room;
- prueba manual de force-stop;
- prueba manual de reinicio de emulador o dispositivo.

### 16.7. Backup de Android

El Manifest actual permite backup mediante configuración Android.

HU-11 no debe confundir:

```text
persistencia local
```

con:

```text
restauración de backup del sistema
```

La funcionalidad offline debe funcionar incluso sin restauración de nube.

No se deshabilitará backup únicamente para demostrar ausencia de red, porque el backup gestionado por el sistema no es una dependencia operativa del CRUD.

---

## 17. Archivos candidatos a modificación

HU-11 debería modificar pocos archivos.

Posibles archivos:

```text
app/src/test/
└── .../architecture/OfflineArchitectureTest.java

app/src/androidTest/
└── .../persistence/WarehouseItemPersistenceTest.java

app/src/main/AndroidManifest.xml
app/build.gradle
gradle/libs.versions.toml
README.md
```

Reglas:

- Manifest y Gradle solo se modifican si la auditoría detecta una dependencia indebida;
- no se tocará producción para “simular” offline;
- no se crearán capas vacías;
- README podrá actualizarse al cierre general de v1.0, no necesariamente dentro del commit funcional si el repositorio mantiene documentación separada.

---

## 18. Pruebas recomendadas

### 18.1. Pruebas de arquitectura

- Manifest no contiene `INTERNET`;
- Manifest no contiene `ACCESS_NETWORK_STATE`;
- producción no contiene clientes remotos;
- `AppContainer` construye Room repository;
- no existe una implementación remota del puerto;
- dominio no importa Android o Room;
- servicios no importan APIs de red.

### 18.2. Prueba instrumentada de persistencia

- insertar y cerrar base;
- reabrir y consultar;
- actualizar y cerrar;
- reabrir y verificar cambio;
- eliminar y cerrar;
- reabrir y verificar ausencia;
- eliminar varios y reabrir;
- verificar restricción única tras reapertura;
- verificar registros sin posición;
- limpiar archivo de prueba.

### 18.3. Pruebas funcionales offline

- listado;
- alta;
- detalle;
- búsqueda;
- filtros;
- edición;
- eliminación individual;
- duplicado;
- estados vacíos;
- eliminación múltiple.

### 18.4. Pruebas de ciclo de vida

- rotación;
- cerrar desde recientes;
- force-stop;
- volver a abrir;
- reinicio de emulador;
- cambio Wi-Fi activo/inactivo;
- modo avión.

### 18.5. Pruebas negativas

- no aparece login;
- no aparece “sin conexión”;
- no aparece reintentar red;
- no se bloquea Guardar;
- no se bloquea Editar;
- no se bloquea Eliminar;
- no se bloquea búsqueda;
- no se bloquean filtros;
- no se pierde información confirmada.

### 18.6. CI

Ejecutar:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Para instrumentadas:

```text
./gradlew connectedDebugAndroidTest
```

La ejecución instrumentada requiere emulador o dispositivo disponible; no debe confundirse con una dependencia de Internet.

---

## 19. Plan manual de verificación offline

### Escenario A — Preparación

1. Instalar APK.
2. Crear al menos seis registros ficticios.
3. Incluir:
   - códigos iguales en categorías distintas;
   - registros con posición;
   - registros sin posición;
   - varias categorías y sitios.
4. Cerrar aplicación.
5. Activar modo avión.

### Escenario B — Consulta

1. Abrir.
2. Verificar listado.
3. Abrir detalle.
4. Rotar.
5. Volver.

### Escenario C — Escrituras

1. Crear un registro.
2. Editarlo.
3. Eliminar otro.
4. Eliminar varios.
5. Verificar resultados.

### Escenario D — Consulta avanzada

1. Buscar parcialmente.
2. Aplicar filtros.
3. Combinar búsqueda y filtros.
4. Provocar `NO_RESULTS`.
5. Limpiar criterios.

### Escenario E — Persistencia

1. Cerrar desde recientes.
2. Abrir y verificar.
3. Forzar detención.
4. Abrir y verificar.
5. Reiniciar emulador o dispositivo.
6. Abrir y verificar.

### Escenario F — Cambio de conectividad

1. Con aplicación abierta, desactivar modo avión.
2. Verificar que nada cambia.
3. Volver a activarlo.
4. Verificar que nada cambia.

---

## 20. Evidencias necesarias para cerrar HU-11

- captura de Manifest sin permisos de red;
- captura o resultado de auditoría de dependencias;
- evidencia de `AppContainer` con Room repository;
- listado en modo avión;
- alta en modo avión;
- detalle en modo avión;
- búsqueda en modo avión;
- filtros en modo avión;
- edición en modo avión;
- eliminación individual en modo avión;
- duplicado rechazado en modo avión;
- eliminación múltiple en modo avión;
- `EMPTY_DATABASE` en modo avión;
- `NO_RESULTS` en modo avión;
- datos después de cerrar y abrir;
- datos después de force-stop;
- datos después de reiniciar dispositivo;
- prueba instrumentada de reapertura Room;
- prueba de ausencia de permisos de red;
- resultado de pruebas unitarias;
- resultado de pruebas instrumentadas;
- resultado de lint;
- APK debug generado;
- CI satisfactoria en `feature/hu-11-funcionamiento-sin-conexion`;
- merge local en `develop`;
- CI satisfactoria en `develop`;
- confirmación de que no se creó lógica de conectividad;
- confirmación de que no se añadió fuente remota;
- confirmación de que Room sigue siendo la única fuente de verdad.

---

## 21. Tareas de implementación

1. Confirmar HU-10 integrada en `develop`.
2. Verificar CI satisfactoria.
3. Analizar ZIP actualizado de HU-10.
4. Crear `feature/hu-11-funcionamiento-sin-conexion`.
5. Auditar Manifest.
6. Auditar dependencias.
7. Auditar código fuente.
8. Auditar `AppContainer`.
9. Auditar implementaciones del repositorio.
10. Confirmar ausencia de autenticación.
11. Confirmar ausencia de backend.
12. Crear prueba de permisos de red.
13. Crear prueba instrumentada de reapertura Room.
14. Cubrir insert tras reapertura.
15. Cubrir update tras reapertura.
16. Cubrir delete tras reapertura.
17. Cubrir delete múltiple tras reapertura.
18. Cubrir unicidad tras reapertura.
19. Ejecutar matriz funcional en modo avión.
20. Ejecutar cierre y reapertura.
21. Ejecutar force-stop.
22. Ejecutar reinicio de emulador.
23. Verificar estados vacíos.
24. Verificar búsqueda y filtros.
25. Verificar rotación.
26. Confirmar que la conectividad no altera UI.
27. Corregir solo regresiones reales.
28. No añadir `ConnectivityManager`.
29. No añadir permisos de red.
30. No añadir clientes HTTP.
31. Ejecutar `./gradlew testDebugUnitTest`.
32. Ejecutar `./gradlew lintDebug`.
33. Ejecutar `./gradlew assembleDebug`.
34. Ejecutar `./gradlew connectedDebugAndroidTest`.
35. Publicar commits representativos con `#13`.
36. Verificar CI en la rama.
37. Recopilar evidencias.
38. Revisar criterios de aceptación.
39. Fusionar localmente en `develop`.
40. Verificar CI de `develop`.
41. Eliminar rama local y remota.
42. Preparar el siguiente paso: estabilización y release v1.0.0.

---

## 22. Definición de terminado

HU-11 estará terminada cuando:

- la aplicación inicia sin conexión;
- el listado funciona sin conexión;
- el alta funciona sin conexión;
- el detalle funciona sin conexión;
- la búsqueda funciona sin conexión;
- los filtros funcionan sin conexión;
- la edición funciona sin conexión;
- la eliminación individual funciona sin conexión;
- la eliminación múltiple funciona sin conexión;
- los duplicados se controlan sin conexión;
- los estados vacíos funcionan sin conexión;
- los datos persisten tras cerrar;
- los datos persisten tras force-stop;
- los datos persisten tras reiniciar;
- Room es la única fuente de verdad;
- no existe autenticación;
- no existe backend;
- no existe implementación remota;
- Manifest no solicita `INTERNET`;
- Manifest no solicita estado de red;
- no existen clientes HTTP funcionales;
- la red disponible o ausente no cambia el flujo;
- no se muestra error de red;
- no se añade modo offline artificial;
- no se añade lógica de conectividad;
- no se añaden campos de sincronización;
- se prueba la reapertura real de Room;
- las pruebas unitarias finalizan correctamente;
- las pruebas instrumentadas finalizan correctamente;
- lint finaliza correctamente;
- APK debug se genera;
- CI de la rama finaliza correctamente;
- todos los criterios se verifican;
- las evidencias quedan recopiladas;
- la rama se fusiona en `develop`;
- CI de `develop` finaliza correctamente;
- la rama se elimina tras integración.

---

## 23. Resultado esperado

Al cerrar HU-11, AlmacenTracker v1.0 quedará demostrado como una aplicación completamente local:

```text
sin Internet
    ↓
todas las funciones disponibles
    ↓
Room / SQLite
    ↓
datos persistentes
```

La falta de conexión no será un estado excepcional.

El siguiente paso ya no será otra historia funcional de v1.0, sino la estabilización de la versión:

```text
develop
    ↓
release/v1.0.0
    ↓
pruebas finales + evidencias + README + versión
    ↓
master
    ↓
tag v1.0.0
```

---

## 24. Commit documental recomendado

```text
git commit -m "docs: add HU-11 offline operation verification plan #13"
```
