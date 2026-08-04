# HU-22 — Consolidar permisos y errores del escáner

> Quinta historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-22  
**Nombre:** Consolidar permisos y errores del escáner  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-22-consolidar-permisos-errores-escaner`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-18 — Escanear códigos de barras y códigos QR  
- HU-19 — Buscar mercancía mediante un código escaneado  
- HU-20 — Registrar mercancía con un código escaneado  
- HU-21 — Sustituir el código durante la edición  

**Issue prevista:** `#25`

---

## 2. Historia de usuario

Como usuario,  
quiero recibir indicaciones claras cuando la cámara o el reconocimiento no puedan utilizarse,  
para poder reintentar, revisar el permiso o continuar mediante entrada manual sin perder el flujo en el que estaba trabajando.

---

## 3. Objetivo

Completar y unificar el comportamiento del escáner ante permisos, ausencia de cámara, fallos de inicialización y errores de reconocimiento.

La HU-22 no añadirá una nueva función de inventario. Consolidará la experiencia transversal de `ScannerActivity`, que ya puede abrirse desde:

```text
MainActivity
ItemFormActivity — CREATE
ItemFormActivity — EDIT
```

Flujo general:

```text
usuario abre ScannerActivity
        ↓
comprobar cámara y permiso
        ↓
escaneo disponible
    → reconocer código

problema recuperable
    → explicar + Reintentar

permiso bloqueado
    → explicar + Abrir ajustes

cámara no disponible
    → explicar + Continuar manualmente

error técnico
    → detener recursos + Reintentar o continuar manualmente
```

La salida manual deberá devolver `RESULT_CANCELED` para que la pantalla llamadora conserve su estado y permita escribir el código.

---

## 4. Referencias del proyecto

La HU-22 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-18-escanear-codigos-barras-qr.md`;
- el estado real de `AlmacenTrackerHU21.zip`;
- el contrato existente de `ScannerActivity`;
- CameraX y ML Kit ya integrados;
- la arquitectura MVVM por funcionalidades;
- funcionamiento completamente sin conexión;
- privacidad de fotogramas;
- entrada manual siempre disponible;
- accesibilidad;
- ausencia de cambios en Room;
- la política de no crear abstracciones vacías.

El plan de v1.2 asigna a HU-22:

- permiso denegado;
- permiso denegado permanentemente;
- acceso voluntario a Ajustes;
- cámara no disponible;
- reintento;
- accesibilidad;
- alternativa manual.

---

## 5. Estado real antes de HU-22

El ZIP `AlmacenTrackerHU21.zip` confirma que ya existen:

```text
feature/scanner/
├── ScannerActivity.java
├── ScannerUiState.java
└── ScannerViewModel.java
```

Estados actuales:

```text
INITIALIZING
REQUESTING_PERMISSION
SCANNING
CODE_DETECTED
PERMISSION_DENIED
PERMISSION_DENIED_PERMANENTLY
CAMERA_UNAVAILABLE
ERROR
```

La pantalla ya dispone de:

- preview;
- indicador de progreso;
- tarjeta de error;
- botón Reintentar;
- botón Abrir ajustes;
- botón Cancelar;
- detección de cámara;
- solicitud contextual de permiso;
- retorno desde Ajustes;
- cancelación mediante `RESULT_CANCELED`;
- liberación de CameraX y ML Kit;
- mensajes básicos;
- pruebas del ViewModel.

Sin embargo, el análisis del código real muestra varios puntos que HU-22 deberá cerrar:

### 5.1. Detección de permiso permanente

Actualmente se utiliza una variable de sesión:

```text
permissionRequestedInCurrentFlow
```

junto con:

```text
shouldShowRequestPermissionRationale(...)
```

Esa combinación no distingue de forma completamente fiable entre:

- primera denegación;
- denegación posterior;
- opción “No volver a preguntar”;
- comportamiento específico de distintas versiones de Android.

La aplicación deberá recordar si el permiso ya fue solicitado previamente.

### 5.2. Estado al volver de Ajustes

Actualmente la cámara se reinicia si el permiso fue concedido.

Falta consolidar:

- regreso sin concederlo;
- regreso después de revocarlo;
- reanudaciones repetidas;
- evitar inicializaciones dobles.

### 5.3. Cámara no disponible

El estado existe, pero debe ofrecer una salida manual explícita y accesible.

No tiene sentido mostrar Reintentar indefinidamente cuando el dispositivo no dispone de cámara trasera compatible.

### 5.4. Error del analizador

`onScannerError()` cambia el estado a `ERROR`, pero el flujo técnico debe garantizar que:

- la cámara deje de analizar;
- no se acumulen errores por fotograma;
- no continúe el preview oculto consumiendo recursos;
- Reintentar reconstruya el flujo limpiamente.

### 5.5. Mensajes y accesibilidad

La UI debe anunciar correctamente:

- solicitud de permiso;
- permiso denegado;
- permiso bloqueado;
- cámara no disponible;
- error;
- reintento;
- continuidad manual.

---

## 6. Alcance incluido

HU-22 incluye:

- centralizar la decisión del permiso de cámara;
- distinguir primera solicitud de solicitudes anteriores;
- distinguir denegación temporal de bloqueo permanente;
- mostrar explicación antes de volver a solicitar cuando corresponda;
- evitar bucles de solicitudes;
- abrir Ajustes solo por decisión del usuario;
- comprobar de nuevo el permiso al regresar;
- iniciar la cámara una sola vez;
- mantener la solicitud contextual;
- controlar dispositivo sin cámara;
- controlar ausencia de cámara trasera;
- controlar fallo al obtener `ProcessCameraProvider`;
- controlar fallo al enlazar CameraX;
- controlar fallo del analizador;
- detener cámara y analizador al entrar en error fatal;
- impedir varios errores simultáneos;
- permitir reintento limpio;
- permitir cancelar;
- permitir continuar manualmente;
- devolver `RESULT_CANCELED` al continuar manualmente;
- conservar el estado de la pantalla llamadora;
- mantener búsqueda, filtros y formulario;
- mantener entrada manual en listado, alta y edición;
- mostrar mensajes específicos por situación;
- no mostrar excepciones técnicas;
- accesibilidad de botones, textos y estados;
- anuncios para lectores de pantalla;
- objetivos táctiles adecuados;
- contraste suficiente;
- mantener funcionamiento offline;
- mantener privacidad;
- no modificar Room;
- pruebas unitarias;
- pruebas de estado;
- pruebas de Activity;
- pruebas manuales en varias versiones de Android;
- CI.

---

## 7. Alcance excluido

HU-22 no incluye:

- nuevos formatos de códigos;
- cambios en el parser de códigos;
- búsqueda de mercancía;
- alta o edición funcional;
- cambios en reglas de duplicados;
- linterna;
- zoom;
- enfoque manual;
- selección de cámara frontal;
- reconocimiento desde galería;
- fotografías de listas;
- OCR;
- selección de imágenes;
- historial de escaneos;
- telemetría;
- permisos de almacenamiento;
- permiso de Internet;
- cambios en Room;
- cambios en el esquema;
- gestión de stock;
- reestructuración completa de la feature de escáner.

La captura de listas comienza en HU-23.

---

## 8. Principios de comportamiento

### 8.1. Solicitud contextual

El permiso se solicitará únicamente cuando el usuario abra el escáner.

No se solicitará:

- al iniciar la aplicación;
- al abrir el listado;
- al abrir el formulario;
- después de cancelar;
- sin una acción del usuario.

### 8.2. Alternativa manual

El escáner es una ayuda, no un requisito para usar AlmacenTracker.

Ante cualquier bloqueo definitivo:

```text
Continuar manualmente
```

cerrará `ScannerActivity` con:

```text
RESULT_CANCELED
```

La pantalla llamadora deberá permanecer operativa.

### 8.3. Sin automatismos invasivos

La aplicación no abrirá Ajustes automáticamente.

La aplicación no solicitará repetidamente el permiso en bucle.

### 8.4. Un único flujo activo

No deberán coexistir:

```text
solicitud de permiso + inicialización de cámara
dos inicializaciones de CameraX
dos analizadores
ERROR + análisis activo
CAMERA_UNAVAILABLE + preview activo
```

---

## 9. Estados consolidados

Se mantendrá un modelo explícito:

```text
ScannerUiState.Status
├── INITIALIZING
├── REQUESTING_PERMISSION
├── SCANNING
├── CODE_DETECTED
├── PERMISSION_DENIED
├── PERMISSION_DENIED_PERMANENTLY
├── CAMERA_UNAVAILABLE
└── ERROR
```

Podrán añadirse datos de presentación:

```text
message
canRetry
canOpenSettings
canContinueManually
```

No se añadirán estados redundantes si las capacidades pueden derivarse del `Status`.

### Matriz orientativa

| Estado | Preview | Progreso | Reintentar | Ajustes | Manual |
|---|---:|---:|---:|---:|---:|
| INITIALIZING | Sí | Sí | No | No | Sí |
| REQUESTING_PERMISSION | No | Sí | No | No | No |
| SCANNING | Sí | No | No | No | Sí |
| CODE_DETECTED | No | No | No | No | No |
| PERMISSION_DENIED | No | No | Sí | No | Sí |
| PERMISSION_DENIED_PERMANENTLY | No | No | No | Sí | Sí |
| CAMERA_UNAVAILABLE | No | No | No | No | Sí |
| ERROR | No | No | Sí | No | Sí |

Durante `SCANNING`, la alternativa manual podrá mantenerse mediante navegación Atrás. No es obligatorio mostrar un botón superpuesto sobre el preview.

---

## 10. Historial mínimo de solicitud del permiso

Para distinguir una denegación inicial de un bloqueo permanente se almacenará únicamente:

```text
camera_permission_requested_before = true
```

Ubicación posible:

```text
SharedPreferences
```

Este valor:

- no contiene información personal;
- no requiere Room;
- no se sincroniza;
- solo registra que Android ya mostró la solicitud;
- podrá encapsularse en una clase pequeña con responsabilidad real.

Nombre orientativo:

```text
CameraPermissionHistory
```

Contrato conceptual:

```java
public interface CameraPermissionHistory {

    boolean wasRequestedBefore();

    void markAsRequested();
}
```

Implementación Android orientativa:

```text
SharedPreferencesCameraPermissionHistory
```

No es obligatorio crear una interfaz si solo añade indirección sin beneficio. Una clase concreta inyectable o creada por la Activity también es válida.

---

## 11. Clasificación del permiso

Antes de solicitar:

```text
permiso concedido
    → iniciar cámara

permiso no concedido
+ nunca solicitado
    → solicitar

permiso no concedido
+ solicitado previamente
+ shouldShowRequestPermissionRationale = true
    → PERMISSION_DENIED

permiso no concedido
+ solicitado previamente
+ shouldShowRequestPermissionRationale = false
    → PERMISSION_DENIED_PERMANENTLY
```

Después de la respuesta del sistema:

```text
granted = true
    → iniciar cámara

granted = false
+ rationale = true
    → PERMISSION_DENIED

granted = false
+ rationale = false
    → PERMISSION_DENIED_PERMANENTLY
```

La marca `requestedBefore` deberá establecerse justo antes de lanzar la solicitud.

---

## 12. Denegación temporal

Mensaje:

```text
La cámara es necesaria para escanear códigos.
Puedes volver a solicitar el permiso o continuar introduciendo el código manualmente.
```

Acciones:

```text
Reintentar
Continuar manualmente
```

`Reintentar`:

1. comprueba el estado actual;
2. muestra de nuevo el permiso si Android lo permite;
3. no abre Ajustes;
4. no crea dos solicitudes simultáneas.

---

## 13. Denegación permanente

Mensaje:

```text
El permiso de cámara está desactivado.
Puedes habilitarlo desde los ajustes de AlmacenTracker o continuar manualmente.
```

Acciones:

```text
Abrir ajustes
Continuar manualmente
```

`Abrir ajustes` utilizará:

```text
Settings.ACTION_APPLICATION_DETAILS_SETTINGS
package:<applicationId>
```

Reglas:

- no abre Ajustes automáticamente;
- no finaliza la Activity antes de abrirlos;
- al volver se verifica el permiso;
- si fue concedido, se inicia la cámara;
- si continúa denegado, permanece el estado;
- no vuelve a lanzar automáticamente la solicitud.

---

## 14. Cámara no disponible

Debe diferenciarse de un error temporal.

Condiciones:

- el dispositivo no declara `FEATURE_CAMERA_ANY`;
- no existe cámara trasera compatible;
- CameraX informa que no puede usar `DEFAULT_BACK_CAMERA`.

Mensaje:

```text
Este dispositivo no dispone de una cámara compatible para escanear.
Puedes continuar introduciendo el código manualmente.
```

Acción:

```text
Continuar manualmente
```

No se ofrecerá Reintentar salvo que el error pueda ser temporal.

---

## 15. Error de inicialización

Incluye:

- fallo al obtener provider;
- fallo al enlazar casos de uso;
- excepción de CameraX;
- error inesperado al crear el analizador.

Comportamiento:

1. registrar información técnica solo en desarrollo;
2. desvincular CameraX;
3. cerrar ML Kit;
4. detener análisis;
5. cambiar a `ERROR`;
6. mostrar mensaje comprensible;
7. permitir Reintentar;
8. permitir Continuar manualmente.

Mensaje:

```text
No se pudo iniciar el escáner.
```

No se mostrará:

- stack trace;
- clase de excepción;
- detalles de CameraX;
- detalles de ML Kit.

---

## 16. Error del analizador

Un fallo de análisis no deberá generar un error visible por cada fotograma.

Reglas:

- aceptar un único error fatal por sesión;
- ignorar callbacks posteriores;
- cerrar siempre `ImageProxy`;
- liberar cámara y scanner antes de mostrar `ERROR`;
- no mantener el preview oculto consumiendo recursos;
- permitir un reintento completo.

Flujo:

```text
MlKitCodeScanner.onScannerError(...)
        ↓
ScannerActivity detiene recursos
        ↓
ScannerViewModel.onScannerError(...)
        ↓
ERROR
```

La Activity deberá evitar llamar a `releaseCamera()` desde un hilo incorrecto. La liberación vinculada a UI se realizará en el executor principal cuando sea necesario.

---

## 17. Reintento limpio

`Reintentar` deberá:

1. comprobar que no existe resultado aceptado;
2. bloquear dobles pulsaciones;
3. liberar recursos anteriores;
4. comprobar cámara;
5. comprobar permiso;
6. solicitar permiso o iniciar cámara;
7. reconstruir el analizador;
8. volver a `SCANNING` solo después del enlace correcto.

No se reutilizará una instancia cerrada de:

```text
MlKitCodeScanner
```

No se crearán executors ilimitados.

El executor existente podrá reutilizarse mientras siga activo.

---

## 18. Regreso desde segundo plano

Al regresar a `ScannerActivity`:

### Permiso concedido desde Ajustes

```text
PERMISSION_DENIED_PERMANENTLY
        ↓ onResume
permiso concedido
        ↓
iniciar cámara una vez
```

### Permiso aún denegado

- conservar el estado;
- no solicitar automáticamente;
- no abrir Ajustes otra vez.

### Permiso revocado mientras la Activity estaba abierta

- detener cámara;
- volver a clasificar el permiso;
- no continuar analizando sin autorización.

### Escáner ya activo

- no volver a enlazar CameraX si ya está correctamente enlazado.

Podrá mantenerse una bandera técnica:

```text
cameraBound
```

o consultar el estado real de forma segura.

---

## 19. Continuar manualmente

Se añadirá una acción visible en estados bloqueados.

Comportamiento:

```java
setResult(RESULT_CANCELED);
finish();
```

La Activity llamadora deberá conservar:

### Desde `MainActivity`

- listado;
- búsqueda;
- filtros;
- selección;
- posibilidad de buscar escribiendo.

### Desde `ItemFormActivity`

- categoría;
- código actual;
- sitio;
- posición;
- observaciones;
- errores del formulario;
- edición manual del código.

No se añadirá un extra de error a `RESULT_CANCELED`.

---

## 20. Accesibilidad

HU-22 deberá verificar:

- `contentDescription` del botón Atrás;
- descripción de Reintentar;
- descripción de Abrir ajustes;
- descripción de Continuar manualmente;
- progreso con descripción;
- textos legibles con tamaño del sistema;
- orden de foco coherente;
- botones con al menos 48 dp de área táctil;
- contraste en modo claro y oscuro;
- tarjeta de error identificable;
- mensajes no dependientes solo de iconos o color.

Cuando aparezca un error se recomienda anunciar:

```java
binding.scannerErrorCard.announceForAccessibility(message);
```

El anuncio no deberá repetirse en cada render si el estado no cambió.

---

## 21. UI prevista

La tarjeta de error contendrá:

```text
mensaje
[Reintentar]
[Abrir ajustes]
[Continuar manualmente]
```

La visibilidad dependerá del estado.

No deberán mostrarse botones sin función.

### PERMISSION_DENIED

```text
Reintentar
Continuar manualmente
```

### PERMISSION_DENIED_PERMANENTLY

```text
Abrir ajustes
Continuar manualmente
```

### CAMERA_UNAVAILABLE

```text
Continuar manualmente
```

### ERROR

```text
Reintentar
Continuar manualmente
```

`Cancelar` y `Continuar manualmente` no necesitan coexistir con el mismo significado. Se recomienda renombrar el botón existente según el contexto para que la alternativa sea clara.

---

## 22. Seguridad y privacidad

HU-22 mantendrá:

- sin permiso de Internet;
- sin permisos de almacenamiento;
- sin guardar fotogramas;
- sin guardar fotografías;
- sin subir imágenes;
- sin historial de códigos;
- sin abrir URLs;
- sin ejecutar contenido QR;
- sin mostrar detalles técnicos;
- cámara activa únicamente dentro de `ScannerActivity`.

El acceso a Ajustes se limitará a la ficha de la propia aplicación.

---

## 23. Flujo principal

1. El usuario inicia el escáner.
2. La aplicación comprueba la cámara.
3. Comprueba el permiso.
4. Si nunca fue solicitado, lo solicita.
5. El usuario concede.
6. Se inicia CameraX.
7. El analizador queda operativo.
8. Se muestra `SCANNING`.
9. Se detecta un código.
10. Se libera la cámara.
11. Se devuelve `RESULT_OK`.

---

## 24. Flujos alternativos

### FA-01 — Primera denegación

1. El permiso nunca fue solicitado.
2. Android muestra el diálogo.
3. El usuario deniega.
4. Se muestra `PERMISSION_DENIED`.
5. Puede reintentar o continuar manualmente.

### FA-02 — Denegación permanente

1. El permiso ya fue solicitado.
2. Android no permite volver a mostrar el diálogo.
3. Se muestra `PERMISSION_DENIED_PERMANENTLY`.
4. Puede abrir Ajustes o continuar manualmente.

### FA-03 — Concede desde Ajustes

1. Abre Ajustes.
2. Concede cámara.
3. Regresa.
4. La cámara se inicia una sola vez.

### FA-04 — Regresa sin conceder

1. Abre Ajustes.
2. No modifica el permiso.
3. Regresa.
4. La aplicación conserva el estado permanente.
5. No abre de nuevo Ajustes ni solicita automáticamente.

### FA-05 — Sin cámara

1. El dispositivo no dispone de cámara compatible.
2. Se muestra `CAMERA_UNAVAILABLE`.
3. El usuario continúa manualmente.
4. La pantalla anterior conserva su estado.

### FA-06 — Error al iniciar CameraX

1. CameraX falla.
2. Se liberan recursos.
3. Se muestra `ERROR`.
4. El usuario reintenta.
5. El flujo se reconstruye.

### FA-07 — Error repetido del analizador

1. ML Kit falla en varios fotogramas.
2. Solo se procesa el primer error fatal.
3. La cámara se detiene.
4. Se muestra una única tarjeta de error.

### FA-08 — Cancelación

1. El usuario pulsa Atrás.
2. Se liberan recursos.
3. Se devuelve `RESULT_CANCELED`.
4. La pantalla anterior no cambia.

### FA-09 — Continuidad manual desde alta

1. El formulario contiene datos.
2. El escáner no está disponible.
3. El usuario continúa manualmente.
4. Regresa al formulario.
5. Todos los campos permanecen.

### FA-10 — Continuidad manual desde edición

1. Se está editando mercancía.
2. El permiso está bloqueado.
3. El usuario continúa manualmente.
4. El código actual y los demás campos permanecen.

### FA-11 — Permiso revocado

1. La cámara estaba autorizada.
2. El usuario revoca el permiso desde Ajustes.
3. Regresa a la Activity.
4. La cámara no continúa.
5. Se muestra el estado correspondiente.

### FA-12 — Doble pulsación en Reintentar

1. El usuario pulsa Reintentar varias veces.
2. Solo se inicia un flujo.
3. No se crean dos cámaras ni dos solicitudes.

---

## 25. Criterios de aceptación

### CA-01 — Solicitud contextual

**Dado** que el permiso no está concedido,  
**cuando** el usuario abre el escáner,  
**entonces** se solicita únicamente dentro de ese flujo.

### CA-02 — Primera denegación

**Dado** que es la primera solicitud,  
**cuando** el usuario deniega,  
**entonces** se ofrece Reintentar y no se clasifica inmediatamente como permanente.

### CA-03 — Bloqueo permanente

**Dado** que Android ya no permite solicitar el permiso,  
**cuando** se abre el escáner,  
**entonces** se ofrece Abrir ajustes.

### CA-04 — Ajustes voluntarios

**Dado** un permiso permanente,  
**cuando** se muestra el error,  
**entonces** Ajustes no se abre hasta que el usuario pulse la acción.

### CA-05 — Permiso concedido desde Ajustes

**Dado** que el usuario concede la cámara desde Ajustes,  
**cuando** regresa,  
**entonces** el escáner se inicia sin duplicar CameraX.

### CA-06 — Regreso sin permiso

**Dado** que el usuario vuelve de Ajustes sin conceder,  
**cuando** la Activity se reanuda,  
**entonces** conserva la explicación y no entra en bucle.

### CA-07 — Cámara no disponible

**Dado** un dispositivo sin cámara compatible,  
**cuando** intenta escanear,  
**entonces** se informa y puede continuar manualmente.

### CA-08 — Error de inicialización

**Dado** que CameraX no puede iniciarse,  
**cuando** ocurre el fallo,  
**entonces** los recursos se liberan y se ofrece Reintentar.

### CA-09 — Error único

**Dado** que el analizador falla repetidamente,  
**cuando** se procesa el primer error,  
**entonces** solo se muestra una transición a `ERROR`.

### CA-10 — Reintento limpio

**Dado** un error recuperable,  
**cuando** el usuario pulsa Reintentar,  
**entonces** se reconstruye un único flujo de cámara.

### CA-11 — Continuar manualmente

**Dado** cualquier estado bloqueado,  
**cuando** el usuario continúa manualmente,  
**entonces** vuelve con `RESULT_CANCELED`.

### CA-12 — Conservación del formulario

**Dado** que el escáner se abrió desde alta o edición,  
**cuando** se cancela o continúa manualmente,  
**entonces** el formulario conserva todos sus datos.

### CA-13 — Conservación del listado

**Dado** que el escáner se abrió desde el listado,  
**cuando** se cancela,  
**entonces** búsqueda y filtros permanecen.

### CA-14 — Accesibilidad

**Dado** un estado de error,  
**cuando** aparece,  
**entonces** el mensaje y sus acciones pueden utilizarse con lector de pantalla.

### CA-15 — Sin detalles técnicos

**Dado** un error interno,  
**cuando** se muestra al usuario,  
**entonces** no aparecen excepciones ni nombres de componentes.

### CA-16 — Privacidad

**Dado** cualquier error o cancelación,  
**cuando** finaliza el escáner,  
**entonces** no se conserva ninguna imagen.

### CA-17 — Sin cambios en Room

**Dado** cualquier flujo de permiso o error,  
**cuando** termina HU-22,  
**entonces** no se modifica mercancía.

### CA-18 — Funcionamiento offline

**Dado** que el dispositivo está sin conexión,  
**cuando** el permiso y la cámara están disponibles,  
**entonces** el escáner continúa funcionando localmente.

---

## 26. Diseño técnico propuesto

### `ScannerActivity`

Responsabilidades:

- consultar cámara;
- consultar permiso;
- lanzar solicitud;
- abrir Ajustes;
- enlazar CameraX;
- liberar recursos;
- evitar inicializaciones múltiples;
- procesar acciones de UI;
- devolver resultado o cancelación;
- anunciar estados accesibles.

Cambios probables:

```text
prepareScanner()
classifyPermissionState()
requestCameraPermission()
handleCameraPermissionResult()
handleReturnFromSettings()
handleFatalScannerError()
retryScanner()
continueManually()
```

### `ScannerViewModel`

Responsabilidades:

- transición de estados;
- resultado único;
- error único;
- bloqueo de reintento cuando exista resultado;
- no depender de `Context`;
- no consultar permisos Android;
- no abrir Ajustes.

Podrá añadirse:

```java
public boolean onScannerErrorOnce();
```

o un control equivalente en Activity/ViewModel.

### Historial de permiso

Componente opcional con responsabilidad concreta:

```text
CameraPermissionHistory
```

No deberá ubicarse en dominio, porque representa estado de una API Android.

Ubicación orientativa:

```text
data/scanner/permission/
```

o:

```text
feature/scanner/permission/
```

La elección deberá seguir la estructura real y evitar un paquete excesivo para una sola clase.

### `ScannerUiState`

Añadir únicamente capacidades necesarias:

```text
canContinueManually()
```

La UI no deberá decidir acciones mediante comparaciones repetidas y dispersas del enum si el estado puede exponerlas.

---

## 27. Archivos previstos

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── feature/scanner/
│   ├── ScannerActivity.java
│   ├── ScannerUiState.java
│   └── ScannerViewModel.java
└── data/scanner/
    └── CameraPermissionHistory.java
```

Recursos probables:

```text
app/src/main/res/
├── layout/
│   └── activity_scanner.xml
└── values/
    └── strings.xml
```

Pruebas probables:

```text
app/src/test/java/com/rndymi/almacentracker/
├── data/scanner/
│   └── CameraPermissionHistoryTest.java
└── feature/scanner/
    ├── ScannerUiStateTest.java
    └── ScannerViewModelTest.java

app/src/androidTest/java/com/rndymi/almacentracker/
└── feature/scanner/
    └── ScannerActivityContractTest.java
```

No se prevén cambios en:

- entidad Room;
- DAO;
- repositorio;
- esquema;
- `MainActivity`;
- `ItemFormActivity`;
- búsqueda por código;
- alta;
- edición;
- CameraX o ML Kit en Gradle;
- formatos admitidos.

Las Activities llamadoras solo se modificarán si el código real demuestra que no conservan correctamente el estado tras `RESULT_CANCELED`.

---

## 28. Pruebas

### `ScannerUiState`

- permiso temporal permite reintentar;
- permiso permanente permite Ajustes;
- cámara no disponible permite manual;
- error permite reintentar y manual;
- estados activos no muestran acciones incompatibles;
- preview y progreso no se superponen incorrectamente.

### `ScannerViewModel`

- estado inicial;
- solicitud de permiso;
- denegación temporal;
- denegación permanente;
- cámara no disponible;
- error;
- segundo error ignorado;
- reintento vuelve a inicialización;
- resultado aceptado bloquea errores;
- resultado aceptado bloquea reintentos;
- evento de código sigue siendo único.

### Historial de permiso

- inicialmente no solicitado;
- `markAsRequested()` persiste;
- nueva instancia lee el valor;
- no almacena datos adicionales.

### Activity e instrumentación

- primera denegación no aparece como permanente;
- segunda denegación bloqueada ofrece Ajustes;
- Abrir ajustes construye el Intent correcto;
- Continuar manualmente devuelve `RESULT_CANCELED`;
- cancelar devuelve `RESULT_CANCELED`;
- regresar con permiso inicia cámara;
- regresar sin permiso conserva estado;
- estado de cámara no disponible muestra manual;
- error muestra reintento;
- botones visibles según estado;
- recursos se liberan al cerrar;
- doble reintento no duplica flujo.

### Manuales

Probar al menos:

- Android 8/9;
- Android 10/11;
- Android 12/13;
- Android 14 o superior;
- primera solicitud;
- primera denegación;
- segunda denegación;
- bloqueo permanente;
- concesión desde Ajustes;
- regreso sin conceder;
- revocación;
- dispositivo o emulador sin cámara;
- error/reintento;
- alta con campos escritos;
- edición con campos cargados;
- modo avión;
- TalkBack cuando sea posible.

---

## 29. Tareas de implementación

1. Confirmar HU-21 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-22-consolidar-permisos-errores-escaner`.
4. Revisar el flujo actual de permisos.
5. Añadir historial mínimo de solicitud.
6. Corregir clasificación temporal/permanente.
7. Consolidar regreso desde Ajustes.
8. Evitar inicializaciones duplicadas.
9. Detener recursos ante error fatal.
10. Impedir errores repetidos.
11. Implementar reintento limpio.
12. Añadir Continuar manualmente.
13. Ajustar visibilidad de acciones.
14. Añadir mensajes específicos.
15. Añadir anuncios de accesibilidad.
16. Verificar objetivos táctiles y contraste.
17. Ampliar pruebas de `ScannerUiState`.
18. Ampliar pruebas de `ScannerViewModel`.
19. Añadir pruebas del historial de permiso.
20. Ampliar pruebas instrumentadas.
21. Verificar regresiones en listado, alta y edición.
22. Ejecutar pruebas unitarias.
23. Ejecutar lint.
24. Ejecutar build debug.
25. Ejecutar pruebas instrumentadas.
26. Verificar funcionamiento sin conexión.
27. Verificar criterios de aceptación.
28. Integrar en `develop`.
29. Verificar CI de `develop`.
30. Eliminar la rama tras confirmar la integración.

---

## 30. Evidencias necesarias

- primera denegación temporal;
- reintento de permiso;
- bloqueo permanente;
- acceso voluntario a Ajustes;
- concesión desde Ajustes;
- regreso sin conceder;
- cámara no disponible;
- error de inicialización;
- reintento limpio;
- error único del analizador;
- Continuar manualmente desde listado;
- Continuar manualmente desde CREATE;
- Continuar manualmente desde EDIT;
- conservación de campos;
- accesibilidad;
- modo avión;
- pruebas unitarias;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 31. Definición de terminado

HU-22 estará terminada cuando:

- la solicitud de cámara sea contextual;
- la primera denegación no se clasifique erróneamente como permanente;
- las solicitudes anteriores puedan recordarse;
- la denegación temporal permita reintento;
- la denegación permanente permita abrir Ajustes;
- Ajustes se abra solo por decisión del usuario;
- regresar con permiso inicie una única cámara;
- regresar sin permiso no cree bucles;
- cámara no disponible muestre alternativa manual;
- los errores de CameraX sean controlados;
- los errores del analizador detengan recursos;
- errores repetidos no creen múltiples transiciones;
- Reintentar reconstruya el flujo;
- Continuar manualmente devuelva cancelación;
- listado y formularios conserven estado;
- los mensajes sean específicos;
- los controles sean accesibles;
- no se muestren excepciones;
- la cámara se libere al cerrar;
- no se guarden imágenes;
- no se abra contenido QR;
- no se añadan permisos nuevos;
- Room no se modifique;
- el escáner funcione offline;
- las funciones de HU-19, HU-20 y HU-21 continúen operativas;
- las pruebas definidas sean satisfactorias;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 32. Validación técnica final

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

- primera denegación;
- denegación permanente;
- Ajustes;
- cámara no disponible;
- error y reintento;
- continuidad manual;
- conservación del formulario;
- modo avión.

---

## 33. Resultado esperado

Al cerrar HU-22:

```text
usuario abre el escáner
        ↓
permiso y cámara disponibles
    → escaneo normal

permiso denegado
    → explicación + reintento

permiso bloqueado
    → explicación + Ajustes

cámara no disponible o error
    → explicación + alternativa manual
```

La siguiente historia será:

```text
HU-23 — Capturar o seleccionar una lista de referencias
```
