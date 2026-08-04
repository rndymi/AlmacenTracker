# HU-18 — Escanear códigos de barras y códigos QR

> Primera historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-18  
**Nombre:** Escanear códigos de barras y códigos QR  
**Prioridad:** Alta  
**Estado:** Implementada en la rama de trabajo; pendiente de integración en `develop`  
**Rama de trabajo:** `feature/hu-18-escanear-codigos`  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.1.0`  
**Issue prevista:** `#21`

---

## 2. Historia de usuario

Como usuario,  
quiero escanear un código de barras o un código QR mediante la cámara,  
para obtener su valor sin tener que escribirlo manualmente.

---

## 3. Objetivo

Implementar el componente base de escaneo que utilizarán las historias posteriores de la versión 1.2.

El flujo inicial será:

```text
MainActivity
    ↓ acción Escanear
ScannerActivity
    ↓ solicita permiso cuando corresponda
cámara del dispositivo
    ↓ analiza fotogramas localmente
código compatible detectado
    ↓
ScannerViewModel
    ↓
resultado textual único
    ↓
MainActivity muestra el valor detectado
```

La HU-18 deberá establecer una integración reutilizable y estable para:

- códigos de barras lineales;
- códigos QR;
- permiso de cámara;
- apertura y cierre de la cámara;
- entrega de un único resultado;
- cancelación;
- errores controlados;
- funcionamiento sin conexión.

En esta historia el resultado todavía no buscará mercancía en Room ni rellenará el formulario. Esas integraciones pertenecen a HU-19, HU-20 y HU-21.

La ampliación posterior del plan de v1.2 no modifica el alcance de HU-18.

HU-18 seguirá siendo exclusivamente el escáner individual reutilizable. La captura documental, OCR y reconocimiento de varias referencias se desarrollarán en HU-23, HU-24 y HU-25.

---

## 4. Documentos de referencia

La HU-18 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- el estado real de `AlmacenTrackerRefactor1-1.zip`;
- la arquitectura MVVM por funcionalidades ya aplicada;
- Room como única fuente de verdad para mercancía;
- las reglas de identidad funcional cerradas en versiones anteriores;
- el funcionamiento completamente sin conexión;
- la política de crear únicamente clases con una responsabilidad real;
- el flujo de ramas desde `develop`.

El plan general asigna a v1.2:

```text
Escaneo de códigos de barras y códigos QR
```

El plan de versión define HU-18 como la base técnica y funcional que permitirá posteriormente:

```text
HU-19 → buscar mercancía
HU-20 → registrar con código escaneado
HU-21 → sustituir código durante edición
HU-22 → consolidar permisos y errores
```

---

## 5. Estado real del proyecto antes de HU-18

El ZIP `AlmacenTrackerRefactor1-1.zip` confirma que el proyecto ya se encuentra en:

```groovy
versionCode 3
versionName "1.2.0"
```

La aplicación dispone de:

- Android Java;
- Android Views;
- View Binding;
- Material Components;
- ViewModel;
- LiveData;
- Room;
- SQLite;
- arquitectura MVVM organizada por funcionalidades;
- composición explícita mediante `AppContainer`;
- módulos `InventoryModule` y `DataManagementModule`;
- `feature.inventory.list.MainActivity`;
- `feature.inventory.detail.ItemDetailActivity`;
- `feature.inventory.form.ItemFormActivity`;
- `feature.data_management.common.DataManagementActivity`;
- `WarehouseItemRepository`;
- `RoomWarehouseItemRepository`;
- reglas de normalización, validación e identidad en `domain.rule`;
- eventos de una sola consumición mediante `UiEvent`;
- pruebas unitarias;
- pruebas de arquitectura;
- pruebas instrumentadas;
- CI y CD de artefactos;
- funcionamiento completamente offline.

La estructura principal actual es:

```text
com.rndymi.almacentracker/
├── app/
│   ├── AlmacenTrackerApplication
│   ├── AppContainer
│   └── di/
├── core/
├── data/
├── domain/
└── feature/
    ├── inventory/
    └── data_management/
```

El Manifest actual:

- declara las Activities existentes;
- declara `FileProvider`;
- no solicita permiso de cámara;
- no declara una Activity de escaneo;
- no solicita permisos de red;
- no solicita permisos generales de almacenamiento.

El proyecto todavía no dispone de:

- dependencia de cámara;
- dependencia de reconocimiento de códigos;
- permiso `android.permission.CAMERA`;
- `ScannerActivity`;
- `ScannerViewModel`;
- `ScannerUiState`;
- contrato de resultado de escaneo;
- preview de cámara;
- analizador de fotogramas;
- formatos de escaneo configurados;
- prevención de resultados repetidos;
- acceso visual al escáner;
- pruebas específicas de escaneo.

HU-18 deberá añadir estas capacidades sin alterar el modelo Room ni las funciones de v1.0 y v1.1.

---

## 6. Alcance incluido

HU-18 incluye:

- seleccionar una tecnología de escaneo compatible con el proyecto;
- documentar la decisión técnica;
- añadir las dependencias estrictamente necesarias;
- añadir permiso de cámara al Manifest;
- crear una feature específica de escáner;
- añadir una pantalla de escaneo integrada en la aplicación;
- mostrar una previsualización de cámara;
- solicitar permiso de cámara al iniciar el escaneo cuando sea necesario;
- iniciar la cámara únicamente con permiso concedido;
- analizar fotogramas fuera del hilo principal;
- reconocer los formatos admitidos;
- obtener el valor bruto como `String`;
- eliminar espacios externos del resultado;
- rechazar valores nulos o vacíos;
- conservar ceros iniciales;
- no convertir el valor a número;
- entregar el formato detectado junto con el texto cuando aporte valor;
- aceptar únicamente la primera lectura válida;
- detener el análisis después de detectar un resultado;
- evitar navegaciones o eventos duplicados;
- permitir cancelar el escaneo;
- liberar cámara y analizador al cerrar la pantalla;
- controlar error de inicialización;
- controlar cámara no disponible;
- controlar código no reconocido;
- mantener la entrada manual de la aplicación sin cambios;
- añadir acceso inicial desde `MainActivity`;
- mostrar el valor detectado de forma controlada al regresar;
- funcionar sin conexión;
- no guardar imágenes;
- no enviar imágenes ni resultados a servicios remotos;
- pruebas unitarias;
- pruebas de ViewModel;
- pruebas instrumentadas viables;
- pruebas manuales con códigos reales;
- CI.

---

## 7. Alcance excluido

HU-18 no incluye:

- buscar mercancía en Room después del escaneo;
- abrir automáticamente el detalle;
- resolver múltiples coincidencias;
- registrar mercancía con el valor leído;
- rellenar el formulario de alta;
- sustituir el código durante edición;
- validar duplicados después del escaneo;
- generación de códigos;
- impresión de etiquetas;
- lectura desde galería;
- OCR;
- NFC;
- escaneo masivo;
- historial de escaneos;
- almacenamiento de fotogramas;
- almacenamiento de fotografías;
- apertura automática de URLs;
- ejecución de contenido de un QR;
- telemetría;
- sincronización remota;
- descarga de modelos durante el uso normal;
- una segunda fuente de datos distinta de Room.

La búsqueda mediante escaneo pertenece a HU-19.

La integración con creación pertenece a HU-20.

La integración con edición pertenece a HU-21.

La consolidación completa de permisos y escenarios alternativos pertenece a HU-22, aunque HU-18 deberá incluir el manejo mínimo seguro para poder usar la cámara.

También quedan expresamente fuera de HU-18:

- tomar fotografías de listas;
- seleccionar imágenes o capturas;
- aplicar OCR documental;
- extraer varias referencias;
- interpretar títulos, cantidades, unidades o tallas;
- mostrar ubicaciones de una lista;
- crear historial de mercancía sacada.

La captura y localización de listas pertenece a HU-23, HU-24 y HU-25.

El historial persistente pertenecerá a la versión 1.3.

---

## 8. Precondiciones

Antes de comenzar HU-18 deberán cumplirse:

- release `v1.1.0` estable;
- migración MVVM finalizada;
- `develop` configurada con `versionName "1.2.0"`;
- `versionCode 3`;
- CI de `develop` satisfactoria;
- `MainActivity` operativa;
- `AppContainer` operativo;
- `InventoryModule` operativo;
- pruebas arquitectónicas satisfactorias;
- Room estable;
- funcionamiento offline verificado;
- ausencia de errores abiertos que comprometan las funcionalidades existentes.

---

## 9. Decisión tecnológica

### 9.1. Necesidad real

AlmacenTracker necesita:

- previsualización de cámara integrada;
- control directo del ciclo de vida;
- formatos lineales y QR;
- resultado único;
- funcionamiento local;
- integración Java;
- posibilidad de realizar pruebas;
- futura reutilización desde listado y formulario.

### 9.2. Alternativas consideradas

#### Google Code Scanner

Ventajas:

- integración sencilla;
- poca gestión directa de cámara;
- interfaz proporcionada por Google.

Limitaciones para este proyecto:

- menor control sobre la pantalla;
- menor capacidad de personalización;
- posible dependencia de componentes de Google Play Services;
- menos adecuada para una feature propia que deberá evolucionar.

#### ZXing integrado directamente

Ventajas:

- tecnología conocida;
- funcionamiento local;
- soporte amplio de formatos.

Limitaciones:

- algunas integraciones disponibles encapsulan demasiado la UI;
- puede dificultar mantener una pantalla coherente con el proyecto;
- debe revisarse cuidadosamente el estado de mantenimiento de la integración elegida.

#### CameraX + ML Kit Barcode Scanning con modelo incluido

Ventajas:

- preview de cámara controlado por la aplicación;
- integración con ciclo de vida;
- reconocimiento local;
- soporte de códigos lineales y QR;
- posibilidad de limitar formatos;
- separación clara entre cámara, análisis y estado MVVM;
- reutilización posterior desde varias features.

Costes:

- mayor cantidad de componentes;
- necesidad de gestionar executor, rotación y cierre;
- incremento del tamaño del APK;
- mayor responsabilidad de implementación.

### 9.3. Decisión recomendada

Para HU-18 se recomienda:

```text
CameraX
+
ML Kit Barcode Scanning con modelo incluido
```

La variante incluida localmente es preferible para garantizar que el escaneo funcione sin depender de descargar un modelo durante el uso.

La implementación utiliza actualmente:

```text
CameraX 1.6.1
ML Kit Barcode Scanning 17.3.0
```

con:

```text
minSdk 26
targetSdk 36
versionCode 3
versionName 1.2.0
```

No se han añadido varias bibliotecas de escaneo simultáneamente.

---

## 10. Formatos admitidos

### 10.1. Códigos lineales

HU-18 reconocerá inicialmente:

```text
CODE_128
CODE_39
EAN_13
EAN_8
UPC_A
UPC_E
ITF
CODABAR
```

### 10.2. Código bidimensional

```text
QR_CODE
```

### 10.3. Formatos no habilitados

No se habilitarán por defecto:

```text
DATA_MATRIX
PDF_417
AZTEC
```

Podrán evaluarse en una versión posterior si aparece una necesidad real.

### 10.4. Motivo de limitar formatos

Limitar el lector permite:

- reducir falsos positivos;
- facilitar pruebas;
- mantener el alcance;
- evitar reconocer formatos que la aplicación no promete soportar.

---

## 11. Tratamiento del valor detectado

El resultado se tratará siempre como texto.

Reglas:

```text
rawValue == null → ignorar
trim()
resultado vacío → ignorar
resultado no vacío → válido
```

No se aplicará conversión numérica.

Ejemplo:

```text
001050
```

deberá mantenerse como:

```text
001050
```

No deberá convertirse en:

```text
1050
```

### 11.1. Normalización en HU-18

HU-18 solo aplicará:

```text
trim()
```

No aplicará todavía toda la normalización funcional de mercancía dentro del lector.

La normalización mediante `WarehouseItemNormalizer` se realizará en el flujo que consuma el resultado:

- HU-19 para búsqueda;
- HU-20 para creación;
- HU-21 para edición.

Esta separación evita que el componente de cámara conozca reglas específicas del inventario.

### 11.2. Contenido QR

Un QR podrá contener:

- texto;
- números;
- URL;
- correo;
- cualquier otra cadena.

AlmacenTracker lo devolverá únicamente como texto.

No deberá:

- abrir navegador;
- marcar teléfono;
- enviar correo;
- ejecutar Intents implícitos;
- interpretar comandos.

---

## 12. Modelo de resultado

Se recomienda un modelo Java puro:

```text
ScannedCode
├── value
└── format
```

Ejemplo conceptual:

```java
public final class ScannedCode {

    private final String value;
    private final ScannedCodeFormat format;

    public ScannedCode(
            String value,
            ScannedCodeFormat format
    ) {
        this.value = value;
        this.format = format;
    }

    public String getValue() {
        return value;
    }

    public ScannedCodeFormat getFormat() {
        return format;
    }
}
```

Formatos de aplicación recomendados:

```text
CODE_128
CODE_39
EAN_13
EAN_8
UPC_A
UPC_E
ITF
CODABAR
QR_CODE
UNKNOWN
```

El modelo:

- no dependerá de clases de ML Kit;
- no expondrá constantes de una biblioteca externa;
- no contendrá `Bitmap`;
- no contendrá `Image`;
- no contendrá `Uri`;
- no será Parcelable si no existe una necesidad real.

Para devolver el resultado a la Activity llamadora podrá utilizarse:

```text
EXTRA_SCANNED_VALUE
EXTRA_SCANNED_FORMAT
```

La capa externa transformará las constantes de la biblioteca al enum propio.

---

## 13. Resultado de Activity

`ScannerActivity` deberá finalizar mediante el contrato habitual de Activity.

### Resultado correcto

```text
RESULT_OK
EXTRA_SCANNED_VALUE
EXTRA_SCANNED_FORMAT
```

### Cancelación

```text
RESULT_CANCELED
```

### Error

El error se mostrará dentro de `ScannerActivity`.

Cuando el usuario cierre después de un error sin resultado:

```text
RESULT_CANCELED
```

No se recomienda devolver excepciones serializadas a la Activity llamadora.

### Lanzamiento desde MainActivity

Se utilizará Activity Result API.

Flujo:

```text
MainActivity
    ↓ launch
ScannerActivity
    ↓ result
MainActivity
```

En HU-18, `MainActivity` podrá mostrar un diálogo o mensaje con:

```text
Código detectado: <valor>
Formato: <formato>
```

Este comportamiento sirve para verificar la feature base.

HU-19 sustituirá esta presentación simple por la búsqueda en Room.

---

## 14. Estructura propuesta

La estructura deberá adaptarse a la arquitectura existente:

```text
com.rndymi.almacentracker/
├── app/
│   ├── AppContainer.java
│   └── di/
│       └── ScannerModule.java
│
├── core/
│   └── scanner/
│       ├── CodeScanner.java
│       ├── ScannedCode.java
│       ├── ScannedCodeFormat.java
│       └── ScannerCallback.java
│
├── data/
│   └── scanner/
│       ├── MlKitBarcodeMapper.java
│       └── MlKitCodeScanner.java
│
└── feature/
    └── scanner/
        ├── ScannerActivity.java
        ├── ScannerUiState.java
        ├── ScannerViewModel.java
        └── ScannerViewModelFactory.java
```

Esta estructura es orientativa.

### Regla crítica

No deberán crearse todos los archivos por obligación.

Por ejemplo:

- si el análisis depende directamente del ciclo de vida de `ScannerActivity`, puede no ser útil crear un `CodeScanner` global;
- si `ScannerViewModel` no aporta estado real, no debe crearse como envoltorio vacío;
- si la integración técnica necesita un componente ligado a `ImageAnalysis`, deberá ubicarse en `data.scanner` o en la feature según su responsabilidad real.

La decisión final debe priorizar:

- separación comprobable;
- facilidad de prueba;
- ausencia de abstracciones vacías.

---

## 15. Diseño técnico recomendado

Para el estado real del proyecto, se recomienda dividir responsabilidades así:

### `ScannerActivity`

Responsable de:

- solicitar permiso;
- enlazar CameraX al ciclo de vida;
- configurar `PreviewView`;
- crear `ImageAnalysis`;
- entregar fotogramas al analizador;
- observar estado;
- devolver el resultado;
- liberar recursos.

### `ScannerViewModel`

Responsable de:

- estado exclusivo del escáner;
- aceptar una sola detección;
- ignorar resultados posteriores;
- exponer evento de resultado;
- exponer errores de aplicación;
- sobrevivir a recreación sin repetir navegación.

### `MlKitCodeScanner` o analizador equivalente

Responsable de:

- recibir la imagen analizable;
- invocar ML Kit;
- mapear el primer código admitido;
- cerrar correctamente `ImageProxy`;
- informar éxito o error;
- no navegar;
- no mostrar mensajes.

### `MlKitBarcodeMapper`

Responsable de:

- transformar formato externo a `ScannedCodeFormat`;
- no acceder a UI;
- ser cubierto por pruebas unitarias.

### Composición

`ScannerModule` podrá crear:

- mapper;
- analizador;
- Factory del ViewModel.

Sin embargo, no se utilizará el contenedor para guardar una instancia de cámara o analyzer compartida entre Activities.

Los componentes ligados a cámara deberán respetar el ciclo de vida de `ScannerActivity`.

---

## 16. Layout de escaneo

Se añadirá un layout específico, por ejemplo:

```text
activity_scanner.xml
```

Contenido mínimo:

- Toolbar con título;
- acción Atrás;
- `PreviewView`;
- guía visual de encuadre;
- texto de ayuda;
- indicador de inicialización;
- contenedor de error;
- acción Reintentar cuando corresponda.

Texto orientativo:

```text
Apunta la cámara al código.
```

La guía de encuadre:

- no necesita recortar físicamente la imagen en HU-18;
- sirve como ayuda visual;
- no debe ocultar completamente el preview;
- debe funcionar en modo claro y oscuro.

---

## 17. Manifest

Se añadirá:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

También deberá declararse la Activity:

```xml
<activity
    android:name=".feature.scanner.ScannerActivity"
    android:exported="false" />
```

### Característica de cámara

Se recomienda declarar:

```xml
<uses-feature
    android:name="android.hardware.camera.any"
    android:required="false" />
```

Motivo:

- la entrada manual debe seguir disponible;
- la aplicación no debe excluir dispositivos sin cámara;
- el escáner deberá mostrar un estado controlado cuando la cámara no exista.

No se añadirán permisos de:

```text
INTERNET
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE
MANAGE_EXTERNAL_STORAGE
```

---

## 18. Permiso de cámara

HU-18 incluirá el flujo mínimo seguro.

### 18.1. Permiso concedido

- se inicializa la cámara;
- se muestra el preview;
- comienza el análisis.

### 18.2. Permiso no concedido

La Activity solicitará el permiso cuando el usuario haya iniciado el escaneo.

No se solicitará al abrir AlmacenTracker.

### 18.3. Permiso denegado

Se mostrará un mensaje comprensible:

```text
La cámara es necesaria para escanear códigos.
```

Acciones:

```text
Reintentar
Cancelar
```

### 18.4. Permiso denegado permanentemente

HU-18 deberá evitar un bucle de solicitudes.

Podrá mostrar:

```text
El permiso de cámara está desactivado.
Puedes habilitarlo desde los ajustes de la aplicación.
```

Acciones:

```text
Abrir ajustes
Cancelar
```

HU-22 revisará y completará la experiencia de permisos y errores.

### 18.5. Cancelación

Cancelar:

- devuelve `RESULT_CANCELED`;
- no altera Room;
- no altera búsqueda;
- no altera formularios;
- libera la cámara.

---

## 19. Estados de interfaz

Se recomienda un estado exclusivo:

```text
ScannerUiState
├── INITIALIZING
├── REQUESTING_PERMISSION
├── SCANNING
├── CODE_DETECTED
├── PERMISSION_DENIED
├── PERMISSION_DENIED_PERMANENTLY
├── CAMERA_UNAVAILABLE
└── ERROR
```

### INITIALIZING

Se están preparando los componentes.

### REQUESTING_PERMISSION

La solicitud del sistema está activa.

### SCANNING

La cámara y el análisis están operativos.

### CODE_DETECTED

Existe un resultado válido y el análisis debe detenerse.

### PERMISSION_DENIED

El usuario puede reintentar.

### PERMISSION_DENIED_PERMANENTLY

Se ofrece acceso voluntario a Ajustes.

### CAMERA_UNAVAILABLE

El dispositivo no dispone de una cámara utilizable.

### ERROR

Ocurrió un error de inicialización o análisis.

La UI no deberá mostrar simultáneamente:

```text
SCANNING + ERROR
CODE_DETECTED + analizador activo
CAMERA_UNAVAILABLE + preview activo
```

---

## 20. Prevención de lecturas repetidas

Una cámara analiza varios fotogramas por segundo.

El mismo código puede aparecer repetidamente:

```text
fotograma 1 → 1050
fotograma 2 → 1050
fotograma 3 → 1050
```

Solo se aceptará:

```text
primer resultado válido
```

Reglas:

1. mantener un indicador atómico o estado equivalente;
2. al aceptar el resultado, bloquear detecciones posteriores;
3. detener `ImageAnalysis`;
4. cerrar o pausar el scanner de ML Kit;
5. emitir un único evento;
6. devolver un único resultado;
7. no reabrir el resultado tras rotación.

No se resolverá únicamente mediante un retraso temporal.

---

## 21. Ciclo de vida y liberación de recursos

### Al iniciar

- comprobar disponibilidad;
- obtener `ProcessCameraProvider`;
- enlazar preview;
- enlazar análisis;
- utilizar cámara trasera por defecto.

### Al pausar o cerrar

- detener o desvincular casos de uso;
- cerrar analyzer cuando corresponda;
- detener executor;
- no conservar referencia a Activity;
- no continuar procesando fotogramas.

### `ImageProxy`

Cada imagen deberá cerrarse exactamente una vez:

```java
imageProxy.close();
```

Debe cerrarse:

- tras éxito;
- tras error;
- cuando no exista `mediaImage`;
- cuando ML Kit complete la tarea.

No cerrar `ImageProxy` bloquearía el flujo de cámara.

---

## 22. Rotación y recreación

HU-18 deberá adoptar una decisión explícita.

### Opción recomendada

Mantener `ScannerActivity` en orientación vertical durante esta versión:

```text
portrait
```

Motivos:

- simplifica el preview inicial;
- evita reinicios durante la lectura;
- reduce escenarios de cámara;
- coincide con la orientación principal del flujo.

Esto debe declararse solo si el resto del proyecto acepta esa restricción.

### Alternativa

Permitir rotación y volver a enlazar CameraX correctamente.

La decisión deberá documentarse y probarse.

Independientemente de la orientación:

- un resultado aceptado no deberá emitirse dos veces;
- una recreación no deberá mantener un analyzer antiguo;
- no deberán existir dos cámaras enlazadas.

---

## 23. Flujo principal

1. El usuario abre AlmacenTracker.
2. `MainActivity` muestra el listado.
3. El usuario pulsa Escanear.
4. Se abre `ScannerActivity`.
5. La Activity comprueba el permiso.
6. Si es necesario, solicita la cámara.
7. El usuario concede el permiso.
8. CameraX inicializa el preview trasero.
9. `ImageAnalysis` entrega fotogramas.
10. El analizador procesa localmente las imágenes.
11. Se detecta un formato admitido.
12. Se obtiene el valor textual.
13. Se elimina espacio externo.
14. El ViewModel acepta la primera lectura.
15. Se detiene el análisis.
16. `ScannerActivity` prepara el resultado.
17. Finaliza con `RESULT_OK`.
18. `MainActivity` recibe valor y formato.
19. Muestra una confirmación del código detectado.
20. Room no cambia.

---

## 24. Flujos alternativos

### FA-01 — Cancelar antes de detectar

1. El escáner está abierto.
2. El usuario pulsa Atrás.
3. La Activity libera la cámara.
4. Devuelve `RESULT_CANCELED`.
5. `MainActivity` permanece sin cambios.

### FA-02 — Permiso denegado

1. Se solicita cámara.
2. El usuario deniega.
3. No se inicializa CameraX.
4. Se muestra explicación.
5. Puede reintentar o cancelar.

### FA-03 — Permiso denegado permanentemente

1. Android ya no muestra el diálogo.
2. La aplicación detecta el estado.
3. Se ofrece abrir Ajustes.
4. No se abre automáticamente.
5. Cancelar cierra el escáner.

### FA-04 — Cámara no disponible

1. El dispositivo no ofrece una cámara utilizable.
2. Se emite `CAMERA_UNAVAILABLE`.
3. Se muestra un mensaje.
4. El usuario vuelve al listado.
5. La entrada manual sigue disponible.

### FA-05 — Código no admitido

1. La cámara detecta un patrón no configurado.
2. El analizador lo ignora.
3. El escaneo continúa.
4. No se muestra un error por cada fotograma.

### FA-06 — Resultado nulo

1. ML Kit devuelve un barcode sin `rawValue`.
2. Se ignora.
3. El escaneo continúa.

### FA-07 — Resultado vacío

1. El valor contiene solo espacios.
2. Se normaliza a vacío.
3. Se ignora.
4. El escaneo continúa.

### FA-08 — Doble detección

1. Varios fotogramas contienen el mismo código.
2. El primer resultado se acepta.
3. Los siguientes se ignoran.
4. Solo existe una navegación de regreso.

### FA-09 — Error del analizador

1. La tarea de reconocimiento falla.
2. Se cierra `ImageProxy`.
3. El error se transforma en estado controlado.
4. Se permite reintentar o cancelar.
5. La aplicación no se cierra.

### FA-10 — Código con ceros iniciales

1. Se detecta `001050`.
2. Se conserva como `String`.
3. MainActivity muestra `001050`.
4. No se convierte a `1050`.

### FA-11 — QR con URL

1. Se detecta `https://example.com`.
2. Se devuelve como texto.
3. No se abre navegador.
4. No se ejecuta ningún Intent externo.

### FA-12 — Aplicación en modo avión

1. El dispositivo no tiene Internet.
2. Se abre el escáner.
3. La cámara y el modelo incluido funcionan localmente.
4. El código se detecta correctamente.

---

## 25. Criterios de aceptación

### CA-01 — Acceso al escáner

**Dado** que el usuario está en el listado principal,  
**cuando** pulsa la acción Escanear,  
**entonces** se abre la pantalla de cámara.

### CA-02 — Solicitud contextual

**Dado** que el permiso de cámara no fue concedido,  
**cuando** el usuario inicia el escaneo,  
**entonces** la aplicación solicita el permiso en ese momento.

### CA-03 — Permiso concedido

**Dado** que el usuario concede el permiso,  
**cuando** vuelve a la pantalla,  
**entonces** se inicia el preview y el análisis.

### CA-04 — Lectura de código lineal

**Dado** un código lineal admitido frente a la cámara,  
**cuando** el lector lo reconoce,  
**entonces** devuelve su valor textual.

### CA-05 — Lectura de QR

**Dado** un código QR legible,  
**cuando** el lector lo reconoce,  
**entonces** devuelve su contenido como texto.

### CA-06 — Ceros iniciales

**Dado** el código `001050`,  
**cuando** se detecta,  
**entonces** el resultado conserva `001050`.

### CA-07 — Resultado único

**Dado** que el mismo código aparece en varios fotogramas,  
**cuando** se procesa,  
**entonces** solo se entrega una vez.

### CA-08 — Formato limitado

**Dado** un formato no incluido en la configuración,  
**cuando** aparece frente a la cámara,  
**entonces** no se acepta como resultado.

### CA-09 — Cancelación

**Dado** que el escáner está abierto,  
**cuando** el usuario cancela,  
**entonces** vuelve al listado sin modificar datos.

### CA-10 — Permiso denegado

**Dado** que el usuario rechaza el permiso,  
**cuando** finaliza la solicitud,  
**entonces** se muestra una explicación y puede continuar sin escanear.

### CA-11 — Cámara no disponible

**Dado** un dispositivo sin cámara utilizable,  
**cuando** intenta escanear,  
**entonces** recibe un estado controlado y la aplicación no se cierra.

### CA-12 — Error controlado

**Dado** un error de cámara o análisis,  
**cuando** ocurre,  
**entonces** se libera la imagen y se muestra una opción de reintento o cancelación.

### CA-13 — Funcionamiento offline

**Dado** que el dispositivo está sin conexión,  
**cuando** escanea un código,  
**entonces** el reconocimiento funciona localmente.

### CA-14 — Privacidad

**Dado** que la cámara analiza fotogramas,  
**cuando** termina el proceso,  
**entonces** no se guarda ni se envía ninguna imagen.

### CA-15 — QR seguro

**Dado** un QR que contiene una URL,  
**cuando** se detecta,  
**entonces** se devuelve como texto y no se abre automáticamente.

### CA-16 — Room sin cambios

**Dado** un resultado escaneado,  
**cuando** HU-18 finaliza,  
**entonces** no se crea, edita ni elimina mercancía.

### CA-17 — Recursos liberados

**Dado** que la pantalla de escaneo se cierra,  
**cuando** vuelve a la Activity anterior,  
**entonces** la cámara y el analizador dejan de ejecutarse.

### CA-18 — Compatibilidad con funciones existentes

**Dado** que HU-18 está integrada,  
**cuando** se utilizan las funciones de v1.0 y v1.1,  
**entonces** continúan funcionando sin regresiones.

---

## 26. Diseño del estado y eventos

### Estado persistente

`ScannerUiState` podrá contener:

```text
status
message
permissionCanBeRequested
settingsActionAvailable
```

No deberá contener:

- `Camera`;
- `ProcessCameraProvider`;
- `ImageProxy`;
- Activity;
- View;
- Context.

### Evento de resultado

Se recomienda:

```text
UiEvent<ScannedCode>
```

o un evento específico equivalente.

Reglas:

- se emite una sola vez;
- la Activity lo consume;
- no se repite tras recreación;
- no contiene tipos de la biblioteca externa.

### Error técnico

Las excepciones:

- podrán registrarse durante desarrollo;
- no se mostrarán directamente;
- se convertirán en un mensaje o estado comprensible.

---

## 27. Integración con MainActivity

HU-18 añadirá una acción visual inicial de escaneo.

Opciones válidas:

- acción en Toolbar;
- elemento de menú;
- botón secundario claramente identificado.

No deberá reemplazar:

- FAB de alta;
- búsqueda;
- filtros;
- Gestión de datos.

La interacción inicial será:

```text
Escanear
    ↓
ScannerActivity
    ↓
resultado
    ↓
diálogo informativo
```

Ejemplo:

```text
Código detectado

Valor: 001050
Formato: CODE_128

[Aceptar]
```

No se iniciará todavía una búsqueda automática.

Este diálogo deberá considerarse comportamiento transitorio de HU-18 y podrá evolucionar en HU-19.

---

## 28. Cambios técnicos previstos

### Gradle

- añadir aliases de CameraX;
- añadir alias de ML Kit Barcode Scanning;
- fijar versiones;
- evitar dependencias no utilizadas.

### Manifest

- añadir permiso de cámara;
- añadir característica opcional;
- registrar `ScannerActivity`.

### Recursos

- layout del escáner;
- menú o acción de acceso;
- cadenas;
- icono;
- guía visual;
- mensajes de permiso;
- mensajes de error;
- descripciones de accesibilidad.

### Código

- feature de escáner;
- mapper de formatos;
- analizador;
- ViewModel y estado cuando aporten coordinación real;
- Activity Result API en `MainActivity`;
- composición necesaria.

### Pruebas

- mapper;
- resultado;
- estado;
- ViewModel;
- entrega única;
- cancelación;
- navegación;
- arquitectura offline.

---

## 29. Archivos orientativos

La implementación deberá adaptarse al estado real del repositorio.

Archivos posibles:

```text
app/src/main/java/com/rndymi/almacentracker/
├── app/di/
│   └── ScannerModule.java
├── core/scanner/
│   ├── ScannedCode.java
│   └── ScannedCodeFormat.java
├── data/scanner/
│   ├── MlKitBarcodeMapper.java
│   └── MlKitCodeScanner.java
└── feature/scanner/
    ├── ScannerActivity.java
    ├── ScannerUiState.java
    ├── ScannerViewModel.java
    └── ScannerViewModelFactory.java
```

Recursos posibles:

```text
app/src/main/res/
├── layout/activity_scanner.xml
├── drawable/ic_scan_code.xml
└── values/strings.xml
```

Pruebas posibles:

```text
app/src/test/java/com/rndymi/almacentracker/
├── data/scanner/MlKitBarcodeMapperTest.java
└── feature/scanner/ScannerViewModelTest.java
```

No será obligatorio conservar nombres orientativos si el código real demuestra una separación más clara.

---

## 30. Estrategia de pruebas

### 30.1. Pruebas unitarias

#### Mapper de formato

- cada formato admitido se transforma correctamente;
- formato desconocido devuelve `UNKNOWN` o se rechaza según decisión;
- no se filtran formatos admitidos por error.

#### Valor detectado

- `null`;
- vacío;
- espacios;
- texto;
- Unicode;
- ceros iniciales;
- URL tratada como texto.

#### ViewModel

- estado inicial;
- permiso solicitado;
- escaneo activo;
- primer resultado aceptado;
- segundo resultado ignorado;
- error;
- reintento;
- cancelación;
- evento no repetido.

### 30.2. Pruebas instrumentadas

- `ScannerActivity` está declarada;
- navegación desde `MainActivity`;
- cancelación devuelve `RESULT_CANCELED`;
- resultado simulado devuelve `RESULT_OK`;
- recreación no duplica el evento;
- permiso denegado muestra estado controlado.

La cámara real podrá sustituirse mediante una abstracción o doble cuando sea viable.

No se debe hacer que toda la suite dependa de apuntar físicamente un código a un emulador.

### 30.3. Pruebas manuales

Probar en dispositivo real:

- CODE_128;
- CODE_39;
- EAN_13;
- EAN_8;
- UPC cuando esté disponible;
- QR;
- ceros iniciales;
- luz normal;
- luz baja;
- código inclinado;
- código parcialmente deteriorado;
- varias detecciones;
- cancelar;
- permiso denegado;
- permiso permanente;
- cámara no disponible;
- modo avión;
- volver a abrir escáner varias veces.

---

## 31. Matriz mínima de prueba manual

| Escenario | Resultado esperado |
|---|---|
| CODE_128 válido | Valor correcto |
| EAN_13 válido | Valor correcto |
| QR de texto | Texto correcto |
| QR con URL | Se muestra como texto |
| `001050` | Conserva ceros |
| Mismo código varios fotogramas | Un resultado |
| Cancelar | Sin cambios |
| Permiso denegado | Mensaje y alternativa |
| Modo avión | Escaneo operativo |
| Cerrar y volver a abrir | Cámara disponible nuevamente |
| Formato no admitido | Se ignora |
| Resultado nulo | Se ignora |
| Error de análisis | Estado controlado |

---

## 32. Integración continua

La CI deberá continuar ejecutando:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Cuando exista entorno instrumentado:

```text
./gradlew connectedDebugAndroidTest
```

También deberá verificarse:

- resolución estable de dependencias;
- ausencia de errores de Manifest;
- ausencia de conflictos de versiones;
- ausencia de permisos no previstos;
- funcionamiento de las pruebas arquitectónicas existentes.

---

## 33. Seguridad y privacidad

HU-18 deberá cumplir:

- no almacenar fotogramas;
- no guardar imágenes;
- no solicitar galería;
- no enviar contenido;
- no abrir URLs;
- no ejecutar texto;
- no registrar valores completos en logs de producción;
- no conservar historial;
- no añadir permiso de Internet;
- no añadir permisos de almacenamiento;
- liberar cámara al cerrar;
- usar la cámara únicamente durante la pantalla de escaneo.

---

## 34. Riesgos

### Riesgo 1 — Dependencias incompatibles

CameraX y ML Kit pueden requerir versiones compatibles.

**Mitigación:** fijar versiones, compilar antes de continuar y validar CI.

### Riesgo 2 — APK más grande

El modelo incluido aumenta el tamaño.

**Mitigación:** medir el artefacto y aceptar el coste como garantía offline; no incluir detectores no utilizados.

### Riesgo 3 — Lecturas duplicadas

El mismo valor aparece en múltiples fotogramas.

**Mitigación:** estado atómico y detención inmediata del analyzer.

### Riesgo 4 — Bloqueo de CameraX

No cerrar `ImageProxy` detiene el flujo.

**Mitigación:** cerrar en todos los caminos y cubrir el componente con revisión específica.

### Riesgo 5 — Fuga de recursos

Mantener executor o provider después de cerrar Activity consume recursos.

**Mitigación:** vincular al ciclo de vida y cerrar explícitamente.

### Riesgo 6 — Pruebas dependientes de hardware

Las pruebas pueden ser frágiles.

**Mitigación:** separar mapping, estado y resultado de la cámara física.

### Riesgo 7 — QR peligroso

Un QR puede contener una URL maliciosa.

**Mitigación:** tratar todo como texto sin ejecutar acciones.

### Riesgo 8 — Permiso permanente

Solicitar repetidamente genera mala experiencia.

**Mitigación:** detectar el estado y ofrecer Ajustes de forma explícita.

---

## 35. Relación con las historias posteriores

HU-18 proporcionará la base para:

```text
HU-19 → buscar mercancía con un código individual
HU-20 → rellenar código durante el alta
HU-21 → sustituir código durante edición
HU-22 → completar permisos y errores

HU-23 → capturar o seleccionar una lista
HU-24 → reconocer y revisar varias referencias
HU-25 → mostrar las ubicaciones de la lista
```

La cámara podrá reutilizar conceptos de ciclo de vida, permisos y procesamiento local, pero el OCR documental deberá implementarse como un flujo separado del lector de códigos de barras.

La versión 1.3 utilizará los resultados confirmados de listas para crear un historial de mercancía sacada, sin disminuir stock.

---

## 36. Definición de terminado

HU-18 estará terminada cuando:

- exista una acción de acceso al escáner;
- `ScannerActivity` esté declarada;
- el permiso de cámara se solicite de forma contextual;
- el preview se muestre correctamente;
- la cámara trasera se use por defecto;
- se reconozcan los formatos definidos;
- QR se reconozca;
- el valor se conserve como `String`;
- los ceros iniciales se mantengan;
- valores nulos y vacíos se ignoren;
- formatos no admitidos se ignoren;
- solo se emita una lectura;
- el análisis se detenga tras el resultado;
- cancelar no produzca cambios;
- cámara y analyzer se liberen;
- permisos denegados se controlen;
- cámara no disponible se controle;
- errores se transformen;
- QR con URL no se abra;
- no se almacenen imágenes;
- no se añada Internet;
- el escaneo funcione sin conexión;
- Room no se modifique;
- v1.0 y v1.1 sigan operativas;
- pruebas unitarias finalicen correctamente;
- pruebas instrumentadas necesarias finalicen correctamente;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- CI sea satisfactoria.

---

## 37. Validación técnica final

Ejecutar:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Con dispositivo o emulador:

```bash
./gradlew connectedDebugAndroidTest
```

Validación manual obligatoria en dispositivo físico:

- código lineal;
- QR;
- permiso;
- cancelación;
- doble detección;
- modo avión;
- cierre y reapertura.

---

## 38. Resultado esperado

Al cerrar HU-18, AlmacenTracker dispondrá de un escáner individual reutilizable:

```text
MainActivity
    ↓
ScannerActivity
    ↓
CameraX + ML Kit Barcode Scanning
    ↓
ScannedCode
    ↓
resultado único
```

El resultado no modificará ni consultará mercancía dentro de HU-18.

La historia no se ampliará con OCR, fotografías de listas ni historial.

La siguiente historia será:

```text
HU-19 — Buscar mercancía mediante un código escaneado
```
