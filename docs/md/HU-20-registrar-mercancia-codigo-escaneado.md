# HU-20 — Registrar mercancía con un código escaneado

> Tercera historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-20  
**Nombre:** Registrar mercancía con un código escaneado  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-20-registrar-codigo-escaneado`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-18 — Escanear códigos de barras y códigos QR  
- HU-19 — Buscar mercancía mediante un código escaneado  

**Issue prevista:** `#23`

---

## 2. Historia de usuario

Como usuario,  
quiero rellenar el código del formulario de alta mediante el escáner,  
para registrar mercancía con menos escritura manual y reducir errores de transcripción.

---

## 3. Objetivo

Integrar el escáner existente con `ItemFormActivity` cuando el formulario se encuentre en modo `CREATE`.

La HU-20 permitirá iniciar el alta de dos formas:

### Desde el formulario

```text
ItemFormActivity — CREATE
        ↓ acción Escanear código
ScannerActivity
        ↓ resultado válido
WarehouseItemFormViewModel
        ↓
campo código rellenado
```

### Desde una búsqueda escaneada sin coincidencias

```text
MainActivity
        ↓ código no encontrado
acción Registrar
        ↓
ItemFormActivity — CREATE
        ↓
código escaneado ya rellenado
```

En ambos casos, el usuario conservará el control del formulario:

- podrá editar el código;
- deberá completar categoría y sitio;
- podrá completar posición y observaciones;
- deberá pulsar Guardar;
- se aplicarán las validaciones y el control de duplicados existentes.

La HU-20 no registrará mercancía automáticamente después del escaneo.

---

## 4. Referencias del proyecto

La HU-20 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-18-escanear-codigos-barras-qr.md`;
- `HU-19-buscar-mercancia-codigo-escaneado.md`;
- el estado real de `AlmacenTrackerHU19.zip`;
- la arquitectura MVVM organizada por funcionalidades;
- Room como fuente de verdad;
- la identidad funcional `categoría + código`;
- las reglas de validación y normalización vigentes;
- el funcionamiento completamente sin conexión;
- la política de no crear capas o clases sin responsabilidad real.

El plan de v1.2 establece para HU-20:

- integración con modo `CREATE`;
- conservación de campos;
- edición manual posterior;
- validación y duplicados existentes.

---

## 5. Estado real antes de HU-20

El ZIP `AlmacenTrackerHU19.zip` confirma que el proyecto ya dispone de:

- `ScannerActivity`;
- contrato de Activity Result para devolver valor y formato;
- escaneo local mediante CameraX y ML Kit;
- `ItemFormActivity` reutilizada en modos `CREATE` y `EDIT`;
- `WarehouseItemFormViewModel`;
- `WarehouseItemFormUiState`;
- `WarehouseItemSaveService`;
- validación de categoría, código y sitio;
- normalización centralizada;
- control de duplicados;
- conservación del formulario en el ViewModel;
- prevención de guardados repetidos;
- navegación de `MainActivity` al formulario;
- búsqueda exacta por código escaneado;
- resultado `NOT_FOUND`;
- diálogo de mercancía no encontrada;
- acción Registrar que actualmente abre el formulario vacío.

El formulario actual no dispone de:

- acción visual para escanear el código;
- `ActivityResultLauncher` dentro de `ItemFormActivity`;
- Intent de creación con código inicial;
- aplicación controlada de un código escaneado al estado del formulario;
- tratamiento específico para conservar los demás campos al regresar del escáner.

La HU-20 deberá ampliar el flujo existente sin duplicar el formulario ni crear otra Activity de alta.

---

## 6. Alcance incluido

HU-20 incluye:

- añadir una acción de escaneo asociada al campo código;
- mostrar esa acción únicamente en modo `CREATE`;
- mantenerla oculta o deshabilitada en modo `EDIT`;
- abrir `ScannerActivity` desde `ItemFormActivity`;
- recibir el resultado mediante Activity Result API;
- obtener el valor con el contrato existente;
- normalizar el código mediante la regla vigente;
- rechazar resultados nulos o vacíos;
- conservar ceros iniciales;
- no convertir el código a número;
- rellenar el campo código después de una lectura válida;
- permitir modificar manualmente el valor escaneado;
- conservar categoría, sitio, posición y observaciones;
- conservar errores de campos no relacionados cuando corresponda;
- limpiar el error del campo código al aplicar un valor válido;
- mantener el formulario sin cambios si el escáner se cancela;
- mantener el formulario sin cambios si el resultado es inválido;
- impedir abrir el escáner mientras el formulario se guarda;
- impedir aperturas repetidas del escáner;
- permitir sustituir dentro de `CREATE` un código introducido manualmente;
- pedir confirmación antes de sustituir un código no vacío en `CREATE`;
- permitir cancelar esa sustitución;
- abrir el formulario con el código prellenado desde el resultado `NOT_FOUND` de HU-19;
- mantener vacío el resto de los campos al iniciar ese alta;
- no guardar automáticamente;
- reutilizar validación, normalización y duplicados existentes;
- mantener Room sin cambios hasta pulsar Guardar;
- conservar el estado ante rotación;
- evitar repetir el resultado del escáner;
- funcionar sin conexión;
- pruebas unitarias;
- pruebas de ViewModel;
- pruebas de contrato de Intent;
- pruebas de Activity o instrumentadas necesarias;
- CI.

---

## 7. Alcance excluido

HU-20 no incluye:

- escanear durante el modo `EDIT`;
- sustituir el código de una mercancía existente;
- confirmar reemplazo durante edición;
- modificar el identificador de un registro;
- buscar automáticamente después de escanear dentro del formulario;
- rellenar automáticamente la categoría;
- deducir la categoría desde el código;
- rellenar sitio o posición;
- crear mercancía sin pulsar Guardar;
- omitir validaciones;
- omitir el control de duplicados;
- escoger automáticamente una categoría;
- modificar Room al recibir el escaneo;
- gestionar stock;
- registrar cantidades;
- capturar listas;
- seleccionar imágenes;
- aplicar OCR;
- registrar historial de escaneos;
- abrir URLs contenidas en QR;
- realizar consultas remotas.

La integración del escáner con el modo `EDIT` pertenece a HU-21.

La consolidación completa de permisos y errores pertenece a HU-22.

---

## 8. Regla principal

El escaneo en HU-20 actúa como una forma alternativa de introducir texto.

```text
escaneo
    ↓
rellenar código
    ↓
usuario revisa y completa formulario
    ↓
Guardar
    ↓
validación y persistencia normal
```

No se permitirá:

```text
escaneo
    ↓
creación automática
```

El código escaneado no tendrá privilegios especiales frente a un código escrito manualmente.

Antes de persistir deberá superar exactamente las mismas reglas:

- código obligatorio;
- normalización;
- categoría obligatoria;
- sitio obligatorio;
- combinación única `categoría + código`;
- control de error de Room.

---

## 9. Modo de formulario permitido

La funcionalidad se habilitará únicamente cuando:

```text
WarehouseItemFormMode.CREATE
```

En modo `EDIT`:

- no se mostrará la acción de escaneo de HU-20;
- no se aplicará un extra de código inicial;
- no se reemplazará el código existente;
- el comportamiento actual permanecerá sin cambios.

Esta separación evita adelantar HU-21.

---

## 10. Acción de escaneo en el campo código

Se recomienda utilizar el icono final de `codeInputLayout`.

Configuración conceptual:

```xml
app:endIconMode="custom"
app:endIconDrawable="@drawable/ic_scan_code"
app:endIconContentDescription="@string/scan_code_for_registration_action"
```

Reglas:

- visible en modo `CREATE`;
- accesible mediante descripción;
- área táctil suficiente;
- deshabilitado durante guardado;
- no deberá cubrir ni impedir la edición manual;
- no dependerá únicamente de un color.

También será válida una acción Material independiente junto al campo si resulta más clara y accesible.

No se añadirá un segundo campo de código.

---

## 11. Inicio desde `ItemFormActivity`

Flujo:

1. El usuario abre el formulario en modo creación.
2. Completa uno o varios campos.
3. Pulsa la acción de escaneo del campo código.
4. Si el código actual está vacío, se abre directamente `ScannerActivity`.
5. Si el código actual contiene texto, se solicita confirmación.
6. El usuario confirma o cancela.
7. Si confirma, se abre el escáner.
8. El escáner devuelve un valor válido.
9. La Activity delega el valor al ViewModel.
10. El ViewModel actualiza únicamente el código.
11. La interfaz conserva los demás campos.
12. El usuario puede editar el valor y guardar normalmente.

---

## 12. Confirmación al reemplazar en modo CREATE

Aunque el registro todavía no exista, un código escrito manualmente no deberá sustituirse de forma silenciosa.

Condición:

```text
código actual normalizado no vacío
```

Diálogo orientativo:

```text
Reemplazar código

El código actual será sustituido por el valor que se escanee.

[Cancelar] [Escanear]
```

Reglas:

- Cancelar conserva el formulario;
- Escanear abre `ScannerActivity`;
- confirmar no borra anticipadamente el código actual;
- el valor anterior solo se reemplaza cuando el escáner devuelve `RESULT_OK`;
- cancelar el escáner conserva el código anterior.

Esta confirmación pertenece al modo `CREATE`.

La confirmación de sustitución durante `EDIT` se definirá en HU-21.

---

## 13. Inicio desde HU-19 sin coincidencias

Actualmente, cuando un código no existe, `MainActivity` ofrece:

```text
Volver a escanear
Registrar
Cerrar
```

HU-20 modificará únicamente la acción Registrar.

Nuevo flujo:

```text
Registrar
    ↓
ItemFormActivity.createIntent(context, scannedCode)
    ↓
modo CREATE
    ↓
campo código prellenado
```

Los demás campos deberán iniciar vacíos:

```text
category = ""
site = ""
position = ""
observations = ""
```

El usuario deberá completar los obligatorios y guardar manualmente.

---

## 14. Contrato de Intent para modo CREATE

Se añadirá un extra específico:

```java
public static final String EXTRA_INITIAL_CODE =
        "com.rndymi.almacentracker.extra.FORM_INITIAL_CODE";
```

Factory recomendada:

```java
public static Intent createIntent(
        Context context,
        @Nullable String initialCode
) {
    Intent intent =
            new Intent(context, ItemFormActivity.class);

    if (initialCode != null) {
        intent.putExtra(
                EXTRA_INITIAL_CODE,
                initialCode
        );
    }

    return intent;
}
```

La navegación ordinaria de alta podrá:

- continuar usando un Intent vacío; o
- utilizar `createIntent(context, null)`.

La Activity no deberá interpretar `EXTRA_INITIAL_CODE` cuando exista un `warehouseItemId` válido de edición.

Regla:

```text
EDIT tiene prioridad
→ se ignora initialCode
```

Esto evita mezclar creación y edición por un Intent mal construido.

---

## 15. Aplicación del código inicial

El código inicial deberá aplicarse una sola vez.

Orden recomendado:

1. determinar modo;
2. crear ViewModel;
3. si el modo es `CREATE`, leer `EXTRA_INITIAL_CODE`;
4. entregar el valor al ViewModel;
5. el ViewModel decide si puede aplicarlo;
6. no volver a aplicarlo tras rotación.

Método orientativo:

```java
public void applyInitialCode(String initialCode);
```

Reglas:

- solo funciona en `CREATE`;
- solo se aplica una vez;
- normaliza el valor;
- ignora nulo o vacío;
- no sobrescribe una edición ya realizada por el usuario;
- limpia el error de código;
- no activa guardado;
- no modifica otros campos.

No se recomienda rellenar directamente `codeEditText` desde el Intent porque:

- duplicaría estado fuera del ViewModel;
- podría reaplicarse tras recreación;
- complicaría conservar cambios del usuario;
- podría dejar UI y estado inconsistentes.

---

## 16. Resultado del escáner dentro del formulario

`ItemFormActivity` registrará:

```text
ActivityResultLauncher<Intent>
```

El resultado deberá procesarse así:

```text
RESULT_CANCELED
    → no cambiar nada

RESULT_OK + valor válido
    → delegar al ViewModel

RESULT_OK + valor inválido
    → conservar formulario y mostrar mensaje controlado
```

Método orientativo del ViewModel:

```java
public void applyScannedCode(String scannedCode);
```

Responsabilidades:

- comprobar modo `CREATE`;
- normalizar;
- validar que no quede vacío;
- reemplazar únicamente el código;
- limpiar `codeError`;
- conservar categoría, sitio, posición y observaciones;
- conservar el estado editable;
- no guardar.

La Activity será responsable de:

- abrir el escáner;
- recibir el Intent;
- mostrar la confirmación previa;
- mostrar mensajes visuales.

El ViewModel no dependerá de:

- `Intent`;
- `ActivityResultLauncher`;
- `ScannerActivity`;
- `Context`.

---

## 17. Conservación de los demás campos

Ejemplo inicial:

```text
category = MR
code = 1000
site = A1
position = Nivel 2
observations = Revisar embalaje
```

Resultado escaneado:

```text
001050
```

Estado esperado:

```text
category = MR
code = 001050
site = A1
position = Nivel 2
observations = Revisar embalaje
```

No deberá ocurrir:

```text
category = ""
site = ""
position = ""
observations = ""
```

El escaneo modifica un único campo.

---

## 18. Edición manual posterior

Después de aplicar un valor escaneado:

- el campo código continuará habilitado;
- el cursor podrá posicionarse normalmente;
- el usuario podrá borrar o modificar el valor;
- los cambios serán enviados al ViewModel mediante el listener existente;
- Guardar utilizará el valor final visible y conservado en el estado.

Ejemplo:

```text
escaneado = 001050
usuario corrige = 001050A
guardado = 001050A
```

---

## 19. Validación y duplicados

HU-20 no creará una ruta especial de persistencia.

Se reutilizará:

```text
WarehouseItemFormViewModel.save()
        ↓
WarehouseItemSaveService.create(...)
        ↓
normalización
        ↓
validación
        ↓
comprobación de duplicado
        ↓
Room
```

### Código escaneado duplicado

Si el usuario introduce:

```text
category = MR
code = 1050
```

y ya existe:

```text
MR + 1050
```

se mostrará el error existente:

```text
Ya existe una mercancía con esta categoría y código.
```

El formulario permanecerá abierto.

### Mismo código en otra categoría

Si existe:

```text
MD + 1050
```

deberá permitirse crear:

```text
MR + 1050
```

si no existe esa combinación.

---

## 20. Estados y disponibilidad

### Estado editable en CREATE

- acción de escaneo habilitada;
- campos habilitados;
- Guardar disponible según la lógica vigente.

### Guardando

- acción de escaneo deshabilitada;
- campos deshabilitados;
- no se abrirá `ScannerActivity`;
- no se aceptará un resultado tardío para cambiar el formulario mientras se guarda.

### Error de validación

- acción de escaneo disponible si el formulario sigue editable;
- escanear un código válido limpia el error del campo código;
- errores de categoría o sitio permanecen.

### Error de persistencia o duplicado

- formulario conservado;
- acción de escaneo vuelve a estar disponible;
- el usuario puede corregir manualmente o escanear otro código.

### EDIT

- acción de escaneo de HU-20 no visible;
- comportamiento existente sin cambios.

---

## 21. Cancelación

### Cancelar el diálogo de reemplazo

- no abre el escáner;
- no cambia ningún campo.

### Cancelar `ScannerActivity`

- no cambia el código;
- no cambia otros campos;
- no muestra error.

### Volver atrás desde el formulario

- conserva el comportamiento actual;
- no guarda datos;
- no modifica Room.

---

## 22. Rotación y recreación

La HU-20 deberá garantizar:

- el código escaneado permanece tras rotación;
- los demás campos permanecen;
- el código inicial del Intent no se reaplica;
- no se vuelve a abrir el escáner automáticamente;
- no se repite el diálogo de reemplazo;
- no se repite un resultado ya procesado;
- no se borra una corrección manual realizada después del escaneo.

El ViewModel continuará siendo la fuente del estado del formulario.

---

## 23. Flujo principal

1. El usuario abre `ItemFormActivity` en modo `CREATE`.
2. Introduce categoría y sitio.
3. Pulsa el icono de escaneo del campo código.
4. `ScannerActivity` se abre.
5. Se detecta un código válido.
6. La Activity recibe `RESULT_OK`.
7. Entrega el valor al ViewModel.
8. El ViewModel normaliza el código.
9. Actualiza únicamente el campo código.
10. El usuario revisa o modifica el valor.
11. Pulsa Guardar.
12. Se ejecutan las reglas existentes.
13. Si el formulario es válido y no existe duplicado, Room crea el registro.
14. El formulario se cierra.
15. El listado se actualiza desde Room.

---

## 24. Flujos alternativos

### FA-01 — Alta desde código no encontrado

1. El usuario escanea desde el listado.
2. HU-19 no encuentra coincidencias.
3. Pulsa Registrar.
4. Se abre `ItemFormActivity` en `CREATE`.
5. El código aparece prellenado.
6. El usuario completa categoría y sitio.
7. Guarda normalmente.

### FA-02 — Escáner cancelado

1. El formulario contiene datos.
2. El usuario abre el escáner.
3. Cancela.
4. Todos los campos permanecen iguales.

### FA-03 — Código actual no vacío

1. El usuario ha escrito un código.
2. Pulsa Escanear.
3. Se muestra confirmación.
4. Cancela.
5. El código escrito permanece.

### FA-04 — Reemplazo confirmado

1. Existe un código manual.
2. El usuario confirma Escanear.
3. El escáner devuelve un nuevo valor.
4. Solo el código se sustituye.

### FA-05 — Escáner sin resultado válido

1. El escáner devuelve un valor nulo o vacío.
2. El ViewModel no modifica el estado.
3. Se muestra un mensaje comprensible.
4. El formulario sigue operativo.

### FA-06 — Código con ceros iniciales

1. Se escanea `001050`.
2. El formulario muestra `001050`.
3. Guardar conserva los ceros.

### FA-07 — Código alfanumérico

1. Se escanea `1210A`.
2. El formulario muestra `1210A`.
3. Se aplican las reglas normales de guardado.

### FA-08 — Duplicado

1. Se escanea `1050`.
2. El usuario introduce categoría `MR`.
3. Ya existe `MR + 1050`.
4. Guardar devuelve duplicado.
5. El formulario conserva sus campos.

### FA-09 — Mismo código, distinta categoría

1. Existe `MD + 1050`.
2. Se escanea `1050`.
3. El usuario selecciona o escribe `MR`.
4. Se permite guardar si `MR + 1050` está disponible.

### FA-10 — Rotación

1. El código escaneado ya está aplicado.
2. El dispositivo rota.
3. El formulario conserva todos los valores.
4. El extra inicial no sobrescribe el estado.

### FA-11 — Intent inconsistente

1. El Intent contiene un id de edición y un código inicial.
2. Se abre en modo `EDIT`.
3. El código inicial se ignora.
4. El registro existente se carga normalmente.

### FA-12 — Guardado en curso

1. El usuario pulsa Guardar.
2. La persistencia está en curso.
3. La acción de escaneo queda deshabilitada.
4. No se inicia otro flujo.

---

## 25. Criterios de aceptación

### CA-01 — Escaneo disponible en CREATE

**Dado** que el formulario está en modo `CREATE`,  
**cuando** se muestra el campo código,  
**entonces** existe una acción accesible para abrir el escáner.

### CA-02 — Escaneo no disponible en EDIT

**Dado** que el formulario está en modo `EDIT`,  
**cuando** se muestra el registro existente,  
**entonces** la acción de escaneo de HU-20 no está disponible.

### CA-03 — Código rellenado

**Dado** que el escáner devuelve un valor válido,  
**cuando** el resultado se procesa,  
**entonces** el campo código se rellena con ese valor.

### CA-04 — Conservación de campos

**Dado** que el usuario ya introdujo datos,  
**cuando** aplica un código escaneado,  
**entonces** categoría, sitio, posición y observaciones permanecen sin cambios.

### CA-05 — Edición manual

**Dado** que el código fue escaneado,  
**cuando** el usuario lo modifica,  
**entonces** Guardar utiliza el valor final editado.

### CA-06 — Ceros iniciales

**Dado** el resultado `001050`,  
**cuando** se aplica al formulario,  
**entonces** se conserva exactamente como texto.

### CA-07 — Cancelación del escáner

**Dado** que el formulario contiene datos,  
**cuando** el usuario cancela el escáner,  
**entonces** ningún campo cambia.

### CA-08 — Confirmación de reemplazo

**Dado** que el código actual no está vacío,  
**cuando** el usuario pulsa Escanear,  
**entonces** se solicita confirmación antes de sustituirlo.

### CA-09 — Cancelación del reemplazo

**Dado** que se muestra la confirmación,  
**cuando** el usuario cancela,  
**entonces** el código actual permanece.

### CA-10 — Registro desde NOT_FOUND

**Dado** que HU-19 no encuentra el código,  
**cuando** el usuario pulsa Registrar,  
**entonces** se abre CREATE con el código prellenado.

### CA-11 — Sin creación automática

**Dado** que se aplica un código escaneado,  
**cuando** vuelve el formulario,  
**entonces** Room no cambia hasta que el usuario pulse Guardar.

### CA-12 — Validación existente

**Dado** un código escaneado y campos obligatorios incompletos,  
**cuando** el usuario pulsa Guardar,  
**entonces** se muestran los errores normales del formulario.

### CA-13 — Duplicado existente

**Dado** que ya existe la misma categoría y código,  
**cuando** se intenta guardar,  
**entonces** no se crea otro registro y se muestra el mensaje de duplicado.

### CA-14 — Código repetido en otra categoría

**Dado** que el código existe en otra categoría,  
**cuando** la nueva combinación está disponible,  
**entonces** el registro puede crearse.

### CA-15 — Rotación

**Dado** que el código fue escaneado o prellenado,  
**cuando** la Activity se recrea,  
**entonces** el valor y los demás campos permanecen sin reaplicar el Intent.

### CA-16 — Guardado en curso

**Dado** que el formulario está guardando,  
**cuando** el usuario intenta escanear,  
**entonces** no se abre un segundo flujo.

### CA-17 — Funcionamiento offline

**Dado** que el dispositivo no tiene conexión,  
**cuando** el usuario escanea y registra,  
**entonces** el flujo funciona con el escáner local y Room.

### CA-18 — Sin cambios en edición

**Dado** un formulario en modo `EDIT`,  
**cuando** se aplica HU-20,  
**entonces** el comportamiento de edición existente no se altera.

---

## 26. Diseño técnico propuesto

### `ItemFormActivity`

Responsabilidades nuevas:

- registrar el launcher del escáner;
- mostrar u ocultar la acción según el modo;
- solicitar confirmación cuando el código no esté vacío;
- abrir `ScannerActivity`;
- extraer el resultado;
- delegar al ViewModel;
- leer y delegar el código inicial;
- no modificar directamente otros campos.

### `WarehouseItemFormViewModel`

Responsabilidades nuevas:

```java
void applyInitialCode(String initialCode);
void applyScannedCode(String scannedCode);
```

Ambos métodos deberán:

- limitarse a `CREATE`;
- normalizar;
- rechazar vacío;
- actualizar únicamente código;
- limpiar `codeError`;
- no guardar.

El ViewModel deberá distinguir que el valor inicial ya fue procesado.

### `WarehouseItemFormUiState`

No se requiere añadir un estado específico de “código escaneado”.

El código continuará representándose mediante:

```text
state.getCode()
```

Podrá añadirse información mínima solo si existe una responsabilidad visual real.

### `MainActivity`

Cambio específico:

```text
showScannedCodeNotFound(...)
```

La acción Registrar deberá abrir:

```text
ItemFormActivity
    con EXTRA_INITIAL_CODE
```

No añadirá lógica de formulario.

### Recursos

Añadir o reutilizar:

```text
ic_scan_code.xml
```

Strings orientativos:

```text
Escanear código
Reemplazar código
El código actual será sustituido por el valor que se escanee.
No se pudo aplicar el código escaneado.
```

---

## 27. Archivos previstos

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── feature/inventory/form/
│   ├── ItemFormActivity.java
│   ├── WarehouseItemFormViewModel.java
│   └── WarehouseItemFormUiState.java
└── feature/inventory/list/
    └── MainActivity.java
```

Recursos probables:

```text
app/src/main/res/
├── layout/
│   └── activity_item_form.xml
└── values/
    └── strings.xml
```

Pruebas probables:

```text
app/src/test/java/com/rndymi/almacentracker/
└── feature/inventory/form/
    └── WarehouseItemFormViewModelTest.java

app/src/androidTest/java/com/rndymi/almacentracker/
└── feature/inventory/form/
    └── ItemFormActivityContractTest.java
```

No se prevén cambios en:

- entidad Room;
- esquema de base de datos;
- DAO;
- repositorio;
- servicio de guardado;
- `ScannerActivity`;
- dependencias Gradle;
- Manifest.

Solo se modificarán si el código real revela una necesidad imprescindible y directamente relacionada con HU-20.

---

## 28. Pruebas

### ViewModel

- aplica código inicial en `CREATE`;
- ignora código inicial en `EDIT`;
- aplica el código inicial una sola vez;
- no sobrescribe datos editados por el usuario;
- aplica código escaneado válido;
- normaliza espacios y mayúsculas;
- conserva ceros iniciales;
- rechaza nulo;
- rechaza vacío;
- conserva categoría;
- conserva sitio;
- conserva posición;
- conserva observaciones;
- limpia únicamente `codeError`;
- no modifica otros errores;
- no guarda automáticamente;
- ignora aplicación durante guardado;
- conserva estado tras recreación del ViewModel.

### Contrato de Activity

- `createIntent(context, code)` incluye `EXTRA_INITIAL_CODE`;
- Intent sin código no incluye valor útil;
- modo edición mantiene `EXTRA_WAREHOUSE_ITEM_ID`;
- un Intent con id de edición ignora el código inicial;
- resultado de `ScannerActivity` se delega correctamente.

### Activity e instrumentación

- acción visible en `CREATE`;
- acción no disponible en `EDIT`;
- acción abre `ScannerActivity`;
- cancelar conserva campos;
- resultado rellena código;
- los demás campos permanecen;
- código existente solicita confirmación;
- cancelar confirmación no abre escáner;
- confirmar abre escáner;
- resultado inválido no borra el código anterior;
- guardado deshabilita escaneo;
- rotación conserva código;
- registro desde `NOT_FOUND` prellena código;
- Guardar continúa aplicando validaciones y duplicados.

### Manuales

- alta abriendo escáner desde formulario vacío;
- alta con categoría y sitio ya escritos;
- reemplazo de código manual;
- cancelación;
- código con ceros iniciales;
- código alfanumérico;
- duplicado;
- mismo código en otra categoría;
- flujo desde `NOT_FOUND`;
- modo avión;
- rotación;
- varios escaneos consecutivos.

---

## 29. Tareas de implementación

1. Confirmar HU-19 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-20-registrar-codigo-escaneado`.
4. Revisar el contrato real de `ScannerActivity`.
5. Añadir `EXTRA_INITIAL_CODE`.
6. Añadir factory de Intent para CREATE con código inicial.
7. Registrar Activity Result launcher en `ItemFormActivity`.
8. Añadir acción visual de escaneo al campo código.
9. Mostrar la acción solo en `CREATE`.
10. Deshabilitarla durante guardado.
11. Añadir confirmación cuando ya exista código.
12. Implementar `applyInitialCode(...)`.
13. Implementar `applyScannedCode(...)`.
14. Garantizar aplicación única del código inicial.
15. Conservar los demás campos.
16. Limpiar únicamente el error de código.
17. Modificar Registrar en el resultado `NOT_FOUND`.
18. Mantener el modo `EDIT` sin cambios.
19. Añadir strings.
20. Actualizar pruebas del ViewModel.
21. Añadir pruebas del contrato del Intent.
22. Añadir pruebas instrumentadas necesarias.
23. Ejecutar pruebas unitarias.
24. Ejecutar lint.
25. Ejecutar build debug.
26. Ejecutar pruebas instrumentadas.
27. Verificar funcionamiento sin conexión.
28. Verificar criterios de aceptación.
29. Integrar en `develop`.
30. Verificar CI de `develop`.
31. Eliminar la rama tras confirmar la integración.

---

## 30. Evidencias necesarias

- acción Escanear código en modo `CREATE`;
- ausencia de esa acción en `EDIT`;
- código aplicado al formulario;
- conservación de categoría, sitio, posición y observaciones;
- confirmación al reemplazar;
- cancelación sin cambios;
- edición manual del código escaneado;
- código con ceros iniciales;
- alta desde `NOT_FOUND` con código prellenado;
- validación de campos obligatorios;
- duplicado controlado;
- mismo código permitido en otra categoría;
- rotación sin pérdida;
- modo avión;
- pruebas unitarias;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 31. Definición de terminado

HU-20 estará terminada cuando:

- el formulario `CREATE` permita abrir el escáner;
- `EDIT` no incorpore todavía esa acción;
- un resultado válido rellene el código;
- los demás campos se conserven;
- el código pueda editarse manualmente;
- los ceros iniciales se mantengan;
- cancelar no modifique el formulario;
- reemplazar un código existente requiera confirmación;
- el código anterior permanezca si no existe un nuevo resultado;
- `NOT_FOUND → Registrar` abra CREATE con el código prellenado;
- el código inicial se aplique una sola vez;
- Room no cambie antes de Guardar;
- se reutilicen las validaciones existentes;
- se reutilice el control de duplicados;
- el guardado continúe siendo manual;
- la acción se bloquee durante persistencia;
- la rotación no reaplique el Intent;
- no se modifique el esquema Room;
- no se duplique `ScannerActivity`;
- funcione completamente sin conexión;
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

- escaneo desde CREATE;
- conservación de campos;
- reemplazo confirmado y cancelado;
- código prellenado desde NOT_FOUND;
- duplicado;
- rotación;
- modo avión.

---

## 33. Resultado esperado

Al cerrar HU-20:

```text
usuario abre el formulario CREATE
        ↓
escanea o recibe un código prellenado
        ↓
revisa y completa los demás campos
        ↓
pulsa Guardar
        ↓
se aplican validación y duplicados existentes
        ↓
Room registra la mercancía
```

La siguiente historia será:

```text
HU-21 — Sustituir el código durante la edición
```
