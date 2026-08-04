# HU-21 — Sustituir el código durante la edición

> Cuarta historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-21  
**Nombre:** Sustituir el código durante la edición  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-21-sustituir-codigo-escaneado`  
**Rama de integración:** `develop`  
**Dependencias:**  
- HU-18 — Escanear códigos de barras y códigos QR  
- HU-20 — Registrar mercancía con un código escaneado  

**Issue prevista:** `#24`

---

## 2. Historia de usuario

Como usuario,  
quiero escanear un nuevo código mientras edito una mercancía,  
para sustituir el código actual con menos errores de transcripción.

---

## 3. Objetivo

Extender el formulario existente para permitir el uso del escáner cuando `ItemFormActivity` se encuentre en modo `EDIT`.

Flujo previsto:

```text
ItemDetailActivity
        ↓ Editar
ItemFormActivity — EDIT
        ↓ acción Escanear código
ScannerActivity
        ↓ resultado válido
confirmación con código actual y código nuevo
        ↓ usuario confirma
WarehouseItemFormViewModel
        ↓
campo código sustituido
        ↓ usuario pulsa Guardar
WarehouseItemSaveService.update(...)
        ↓
Room
```

La sustitución del código será una modificación pendiente del formulario.

El escaneo no actualizará Room automáticamente.

---

## 4. Referencias del proyecto

La HU-21 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-18-escanear-codigos-barras-qr.md`;
- `HU-20-registrar-mercancia-codigo-escaneado.md`;
- el estado real de `AlmacenTrackerHU20.zip`;
- la arquitectura MVVM organizada por funcionalidades;
- Room como fuente de verdad;
- la identidad funcional `categoría + código`;
- la normalización centralizada;
- la validación y el control de duplicados existentes;
- el funcionamiento completamente sin conexión;
- la política de crear únicamente componentes con una responsabilidad real.

El plan de v1.2 asigna a HU-21:

- integración del escáner con `EDIT`;
- confirmación del reemplazo;
- conservación del id;
- conservación de la fecha de creación;
- actualización normal al guardar.

---

## 5. Estado real antes de HU-21

El ZIP `AlmacenTrackerHU20.zip` confirma que el proyecto dispone de:

- `ScannerActivity`;
- CameraX y ML Kit Barcode Scanning;
- contrato de resultado con valor textual;
- `ItemFormActivity` en modos `CREATE` y `EDIT`;
- acción de escaneo integrada en el campo código;
- `ActivityResultLauncher<Intent>` dentro del formulario;
- confirmación de reemplazo en modo `CREATE`;
- `WarehouseItemFormViewModel.applyScannedCode(...)`;
- normalización mediante `WarehouseItemNormalizer`;
- conservación de los demás campos al aplicar un escaneo;
- código inicial para altas procedentes de `NOT_FOUND`;
- validación y control de duplicados;
- actualización que conserva el id y `createdAt`;
- actualización de `updatedAt` al guardar;
- pruebas unitarias y de contrato del formulario.

Actualmente:

```text
CREATE
→ muestra la acción de escaneo

EDIT
→ oculta la acción de escaneo
```

Además, `applyScannedCode(...)` ignora el resultado cuando el formulario está en modo `EDIT`.

HU-21 deberá ampliar estos componentes sin duplicar:

- `ScannerActivity`;
- `ItemFormActivity`;
- `WarehouseItemFormViewModel`;
- el servicio de actualización;
- la lógica de duplicados.

---

## 6. Alcance incluido

HU-21 incluye:

- mostrar la acción de escaneo en modo `EDIT`;
- mantener la acción disponible únicamente cuando el registro haya cargado correctamente;
- abrir `ScannerActivity` desde el formulario de edición;
- reutilizar el launcher existente;
- recibir el resultado mediante el contrato existente;
- conservar el código como `String`;
- conservar ceros iniciales;
- normalizar mediante la regla vigente;
- rechazar resultados nulos o vacíos;
- comparar el código escaneado con el código actual normalizado;
- informar cuando ambos códigos sean iguales;
- no pedir reemplazo si no existe un cambio real;
- mostrar una confirmación después de conocer el nuevo código;
- mostrar en la confirmación el código actual y el código nuevo;
- sustituir el código únicamente después de confirmar;
- mantener el código actual cuando el usuario cancela;
- mantener el código actual cuando se cancela `ScannerActivity`;
- conservar categoría, sitio, posición y observaciones;
- conservar el id del registro;
- conservar `createdAt`;
- no modificar `updatedAt` hasta guardar;
- permitir editar manualmente el nuevo código;
- reutilizar la validación normal;
- reutilizar el control de duplicados excluyendo el propio id;
- mantener Room sin cambios hasta pulsar Guardar;
- impedir abrir el escáner durante carga o guardado;
- impedir aperturas repetidas;
- controlar la desaparición del registro durante la edición;
- conservar el estado ante rotación;
- evitar repetir la confirmación o aplicar dos veces el resultado;
- funcionar completamente sin conexión;
- pruebas unitarias;
- pruebas de ViewModel;
- pruebas de Activity o instrumentadas;
- CI.

---

## 7. Alcance excluido

HU-21 no incluye:

- guardar automáticamente después del escaneo;
- modificar categoría automáticamente;
- deducir categoría desde el código;
- buscar el código antes de mostrarlo en el formulario;
- abrir el detalle de otra mercancía;
- cambiar el id;
- cambiar manualmente las fechas;
- conservar historial de códigos anteriores;
- auditoría de modificaciones;
- deshacer después de guardar;
- modificar varios registros;
- escanear listas;
- seleccionar imágenes;
- OCR;
- guardar fotografías;
- registrar cantidades;
- gestionar stock;
- abrir URLs contenidas en QR;
- realizar consultas remotas;
- consolidar todos los escenarios de permisos de cámara.

La consolidación de permisos y errores pertenece a HU-22.

La captura de listas pertenece a HU-23.

---

## 8. Regla principal

El escaneo propone un nuevo valor para el campo código.

```text
código almacenado
        ↓
escanear propuesta
        ↓
comparar
        ↓
confirmar
        ↓
actualizar estado del formulario
        ↓
Guardar
        ↓
persistir mediante el flujo normal
```

No se permitirá:

```text
escanear
    ↓
actualizar Room automáticamente
```

El usuario deberá confirmar dos decisiones diferentes:

1. sustituir el valor visible del formulario;
2. guardar posteriormente la edición completa.

---

## 9. Diferencia entre CREATE y EDIT

### CREATE

La confirmación existente se realiza antes de abrir el escáner cuando el campo ya contiene texto.

Motivo:

- se protege una entrada manual aún no persistida;
- si el escáner se cancela, el valor anterior permanece.

### EDIT

La confirmación se realizará después del escaneo.

Motivo:

- siempre existe normalmente un código cargado;
- el usuario debe conocer el nuevo valor antes de decidir;
- la confirmación puede mostrar claramente el cambio;
- evita aceptar una sustitución sin saber qué código será aplicado.

Flujo recomendado en `EDIT`:

```text
Escanear
    ↓
resultado = 001050
    ↓
¿Sustituir 1050 por 001050?
    ↓
Cancelar / Sustituir
```

---

## 10. Disponibilidad de la acción

La acción de escaneo estará visible en:

```text
CREATE
EDIT con registro cargado
```

Estará deshabilitada o no responderá cuando el estado sea:

```text
LOADING
SAVING
NOT_FOUND
INVALID_ID
ERROR no editable
```

La visibilidad ya no deberá depender únicamente de:

```text
mode == CREATE
```

La disponibilidad deberá derivarse de:

```text
state.isEditable()
```

y de que el formulario se encuentre en un modo compatible.

---

## 11. Resultado del escáner en EDIT

Al recibir `RESULT_OK`:

1. obtener el valor mediante `ScannerActivity.getScannedValue(...)`;
2. rechazar `null`;
3. aplicar `trim()` y normalización centralizada;
4. rechazar vacío;
5. recuperar el código actual desde el estado del ViewModel;
6. comparar ambos valores;
7. mostrar el resultado correspondiente.

Resultado conceptual:

```text
INVALID
SAME_CODE
REPLACEMENT_REQUIRED
```

No es obligatorio crear un enum si la Activity puede resolverlo sin duplicar reglas.

Sin embargo, la normalización y comparación no deberán dispersarse entre varios listeners.

---

## 12. Código igual al actual

Si:

```text
código actual normalizado = código escaneado normalizado
```

no se modificará el formulario.

Mensaje orientativo:

```text
El código escaneado ya es el código actual.
```

Reglas:

- no mostrar confirmación de reemplazo;
- no modificar errores;
- no cambiar `updatedAt`;
- no marcar Room como modificado;
- permitir continuar editando otros campos;
- permitir volver a escanear.

Ejemplo:

```text
actual = "1050"
escaneado = " 1050 "
resultado = sin cambio
```

---

## 13. Confirmación de sustitución

Cuando el código sea diferente se mostrará:

```text
Sustituir código

Código actual: 1050
Código nuevo: 001050

El cambio no se guardará hasta que pulses Guardar.

[Cancelar] [Sustituir]
```

Reglas:

- el diálogo se abre después de obtener un resultado válido;
- Cancelar conserva el código actual;
- Sustituir aplica el código nuevo al ViewModel;
- cerrar el diálogo equivale a cancelar;
- no se muestran excepciones ni datos internos;
- el mensaje deberá ser accesible;
- los códigos deberán mostrarse como texto.

No se modificará el campo antes de confirmar.

---

## 14. Aplicación del código confirmado

Se recomienda ampliar el ViewModel mediante un método claro:

```java
public void applyConfirmedScannedCode(
        String scannedCode
);
```

Alternativamente, podrá ampliarse el método actual:

```java
applyScannedCode(...)
```

si mantiene una semántica inequívoca y continúa siendo reutilizable por `CREATE`.

Responsabilidades del ViewModel:

- comprobar que el formulario sea editable;
- admitir `CREATE` y `EDIT`;
- normalizar el código;
- rechazar vacío;
- actualizar únicamente el campo código;
- limpiar únicamente `codeError`;
- conservar los demás campos;
- marcar el formulario como editado;
- no guardar;
- no navegar;
- no mostrar diálogos;
- no depender de Android.

La confirmación continuará siendo responsabilidad de la Activity.

---

## 15. Conservación del registro

Ejemplo inicial:

```text
id = 7
category = MR
code = 1050
site = A1
position = Nivel 2
observations = Revisar embalaje
createdAt = 1721304000000
updatedAt = 1721308000000
```

Código confirmado:

```text
001050
```

Estado pendiente esperado:

```text
id = 7
category = MR
code = 001050
site = A1
position = Nivel 2
observations = Revisar embalaje
createdAt = 1721304000000
updatedAt = sin modificar todavía
```

Después de Guardar:

```text
id = 7
createdAt = 1721304000000
updatedAt = instante actual
```

El flujo existente de `WarehouseItemSaveService.update(...)` continuará siendo responsable de estas garantías.

---

## 16. Edición manual posterior

Después de confirmar la sustitución:

- el campo código seguirá habilitado;
- el usuario podrá corregirlo;
- el valor final será el que se envíe al guardar;
- no se conservará el valor escaneado como una fuente separada.

Ejemplo:

```text
escaneado = 001050
confirmado = 001050
corregido manualmente = 001050A
guardado = 001050A
```

---

## 17. Validación y duplicados

La sustitución no realizará una escritura ni creará una ruta especial de validación.

Al pulsar Guardar se reutilizará:

```text
WarehouseItemFormViewModel.save()
        ↓
WarehouseItemSaveService.update(...)
        ↓
normalización
        ↓
validación
        ↓
existsByCategoryAndCodeExcludingId(...)
        ↓
Room
```

### Mantener la misma identidad

Debe permitirse guardar sin cambiar:

```text
id = 7
MR + 1050
```

### Código disponible

Debe permitirse cambiar:

```text
id = 7
MR + 1050
```

a:

```text
id = 7
MR + 1051
```

si la combinación está libre.

### Conflicto con otra mercancía

Si existe:

```text
id = 9
MR + 1051
```

la edición de `id = 7` hacia:

```text
MR + 1051
```

deberá rechazarse.

Mensaje existente:

```text
Ya existe otra mercancía con esta categoría y código.
```

El formulario permanecerá abierto con el valor propuesto.

---

## 18. Cancelación

### Cancelar `ScannerActivity`

- no muestra confirmación;
- conserva el código;
- conserva los demás campos;
- no modifica Room.

### Cancelar la confirmación

- conserva el código actual;
- no limpia errores;
- no cambia fechas;
- no modifica Room.

### Volver atrás después de confirmar

Si el usuario confirmó el nuevo código pero sale sin guardar:

- Room mantiene el código anterior;
- se conserva el comportamiento general del formulario ante salida;
- no se realizará actualización implícita.

---

## 19. Carga y registro inexistente

El escáner no deberá abrirse mientras se carga el registro.

Si el registro desaparece antes de guardar:

- el flujo existente devolverá `NOT_FOUND`;
- no se creará un registro nuevo;
- no se aplicará la edición a otra fila;
- se mostrará el mensaje vigente;
- el usuario volverá de forma controlada.

HU-21 no deberá almacenar una copia de la entidad para actualizarla directamente.

---

## 20. Rotación y recreación

La HU-21 deberá garantizar:

- el código confirmado permanece tras rotación;
- el código anterior permanece si se canceló;
- no se reabre el escáner;
- no se repite la confirmación;
- no se reaplica el último resultado;
- id y modo se mantienen;
- los demás campos permanecen;
- el evento de actualización no se duplica.

Un diálogo abierto podrá cerrarse al recrear la Activity si el código todavía no fue aplicado.

No se persistirá automáticamente una propuesta no confirmada.

---

## 21. Flujo principal

1. El usuario abre el detalle de una mercancía.
2. Pulsa Editar.
3. `ItemFormActivity` carga el registro.
4. El formulario entra en estado editable.
5. El usuario pulsa Escanear código.
6. Se abre `ScannerActivity`.
7. Se obtiene un resultado válido.
8. La Activity normaliza o delega la normalización.
9. Comprueba que sea diferente.
10. Muestra código actual y código nuevo.
11. El usuario pulsa Sustituir.
12. El ViewModel actualiza únicamente el código.
13. El usuario revisa el formulario.
14. Pulsa Guardar.
15. Se ejecuta la validación existente.
16. Se comprueba el duplicado excluyendo el propio id.
17. Room actualiza la fila existente.
18. Se conserva `createdAt`.
19. Se renueva `updatedAt`.
20. Se vuelve al detalle actualizado.

---

## 22. Flujos alternativos

### FA-01 — Escáner cancelado

1. El usuario abre el escáner.
2. Cancela.
3. El código actual permanece.
4. El formulario continúa editable.

### FA-02 — Código igual

1. El código actual es `1050`.
2. Se escanea `1050`.
3. Se informa que no existe cambio.
4. No se muestra confirmación.

### FA-03 — Código diferente y cancelación

1. El código actual es `1050`.
2. Se escanea `1051`.
3. Se muestra la confirmación.
4. El usuario cancela.
5. El campo continúa mostrando `1050`.

### FA-04 — Código diferente y confirmación

1. El código actual es `1050`.
2. Se escanea `1051`.
3. El usuario confirma.
4. El campo muestra `1051`.
5. Room todavía conserva `1050`.

### FA-05 — Salir sin guardar

1. El usuario confirma `1051`.
2. Vuelve atrás sin guardar.
3. Room mantiene `1050`.

### FA-06 — Duplicado

1. Se confirma `1051`.
2. Ya existe `MR + 1051` en otro id.
3. Guardar devuelve duplicado.
4. El formulario conserva `1051`.
5. El usuario puede corregir o escanear otra vez.

### FA-07 — Mismo código en otra categoría

1. Existe `MD + 1051`.
2. Se edita una mercancía `MR`.
3. Se confirma `1051`.
4. Se permite guardar si no existe `MR + 1051`.

### FA-08 — Ceros iniciales

1. El código actual es `1050`.
2. Se escanea `001050`.
3. Se muestran ambos valores como diferentes.
4. Al confirmar, se conserva `001050`.

### FA-09 — Resultado vacío

1. El resultado es nulo o vacío.
2. No se muestra confirmación.
3. El código actual permanece.
4. Se muestra un mensaje controlado.

### FA-10 — Guardado en curso

1. El usuario pulsa Guardar.
2. La actualización está en curso.
3. La acción de escaneo se deshabilita.
4. No se abre otro flujo.

### FA-11 — Registro eliminado

1. El formulario estaba abierto.
2. El registro deja de existir.
3. Guardar devuelve `NOT_FOUND`.
4. No se crea otro registro.

### FA-12 — Rotación

1. El nuevo código ya fue confirmado.
2. El dispositivo rota.
3. El valor permanece.
4. No se repite el diálogo.

---

## 23. Criterios de aceptación

### CA-01 — Acción disponible en EDIT

**Dado** que el registro se cargó correctamente,  
**cuando** el formulario está en modo `EDIT`,  
**entonces** existe una acción accesible para escanear un nuevo código.

### CA-02 — Acción no disponible durante carga

**Dado** que el registro todavía se está cargando,  
**cuando** se muestra el formulario,  
**entonces** no puede iniciarse el escáner.

### CA-03 — Confirmación informada

**Dado** un código escaneado diferente,  
**cuando** finaliza el escaneo,  
**entonces** se muestran el código actual y el nuevo antes de sustituir.

### CA-04 — Cancelación de sustitución

**Dado** que se muestra la confirmación,  
**cuando** el usuario cancela,  
**entonces** el código actual permanece.

### CA-05 — Sustitución confirmada

**Dado** que el usuario confirma,  
**cuando** se aplica el resultado,  
**entonces** solo cambia el campo código.

### CA-06 — Código igual

**Dado** que el código escaneado equivale al actual tras normalización,  
**cuando** se procesa,  
**entonces** no se muestra reemplazo ni se modifica el formulario.

### CA-07 — Conservación de campos

**Dado** un formulario con datos cargados,  
**cuando** se sustituye el código,  
**entonces** categoría, sitio, posición y observaciones permanecen.

### CA-08 — Sin persistencia automática

**Dado** que el código fue sustituido en pantalla,  
**cuando** el usuario todavía no ha guardado,  
**entonces** Room conserva el código anterior.

### CA-09 — Salida sin guardar

**Dado** un código nuevo confirmado,  
**cuando** el usuario sale sin guardar,  
**entonces** el registro almacenado no cambia.

### CA-10 — Conservación de identidad interna

**Dado** que la actualización finaliza correctamente,  
**cuando** Room guarda los cambios,  
**entonces** el id permanece igual.

### CA-11 — Conservación de fecha de creación

**Dado** que se guarda el nuevo código,  
**cuando** se actualiza la mercancía,  
**entonces** `createdAt` permanece igual.

### CA-12 — Actualización de fecha

**Dado** que se guarda correctamente,  
**cuando** finaliza la actualización,  
**entonces** `updatedAt` se renueva.

### CA-13 — Duplicado de otra mercancía

**Dado** que otra mercancía posee la misma categoría y nuevo código,  
**cuando** se intenta guardar,  
**entonces** se rechaza la actualización.

### CA-14 — Propio registro no duplicado

**Dado** que el código escaneado equivale al código actual,  
**cuando** se guarda,  
**entonces** el propio id no se considera duplicado.

### CA-15 — Ceros iniciales

**Dado** el resultado `001050`,  
**cuando** se confirma,  
**entonces** se conserva exactamente como texto.

### CA-16 — Edición manual posterior

**Dado** que se confirmó un código escaneado,  
**cuando** el usuario lo corrige manualmente,  
**entonces** se guarda el valor final visible.

### CA-17 — Rotación

**Dado** que el nuevo código fue confirmado,  
**cuando** la Activity se recrea,  
**entonces** no se pierde ni se reaplica el resultado.

### CA-18 — Funcionamiento offline

**Dado** que el dispositivo está sin conexión,  
**cuando** se escanea y guarda un nuevo código,  
**entonces** el flujo funciona mediante reconocimiento local y Room.

---

## 24. Diseño técnico propuesto

### `ItemFormActivity`

Cambios:

- mostrar el icono de escaneo también en `EDIT`;
- mantenerlo deshabilitado mientras el estado no sea editable;
- abrir el escáner sin confirmación previa en `EDIT`;
- procesar el resultado;
- detectar código igual;
- mostrar confirmación con valor actual y nuevo;
- delegar únicamente después de confirmar;
- conservar el flujo actual de `CREATE`.

La Activity no deberá:

- modificar Room;
- validar duplicados;
- cambiar fechas;
- construir un `WarehouseItem`;
- acceder al repositorio.

### `WarehouseItemFormViewModel`

Cambio principal:

```text
permitir aplicar un código escaneado confirmado en EDIT
```

El método deberá continuar:

- normalizando;
- conservando los demás campos;
- limpiando solo el error de código;
- marcando edición;
- sin persistir.

### `WarehouseItemFormUiState`

No se requiere un estado adicional para el código escaneado.

El valor confirmado se representará mediante:

```text
state.getCode()
```

No se almacenará simultáneamente:

```text
originalCode
pendingScannedCode
confirmedCode
```

salvo que una necesidad real de presentación lo justifique.

### `WarehouseItemSaveService`

No se prevén cambios funcionales.

El servicio ya:

- recupera el registro original;
- excluye su id al comprobar duplicados;
- conserva `createdAt`;
- actualiza `updatedAt`;
- actualiza la fila existente.

---

## 25. Archivos previstos

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
└── feature/inventory/form/
    ├── ItemFormActivity.java
    └── WarehouseItemFormViewModel.java
```

Recursos probables:

```text
app/src/main/res/values/
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

- `ScannerActivity`;
- ML Kit;
- CameraX;
- Manifest;
- entidad Room;
- esquema Room;
- DAO;
- repositorio;
- `WarehouseItemSaveService`;
- dependencias Gradle.

Solo se modificarán si el código real demuestra una necesidad imprescindible de HU-21.

---

## 26. Pruebas

### ViewModel

- aplica código escaneado confirmado en `EDIT`;
- conserva el id;
- conserva categoría;
- conserva sitio;
- conserva posición;
- conserva observaciones;
- normaliza espacios y mayúsculas;
- conserva ceros iniciales;
- rechaza nulo;
- rechaza vacío;
- limpia solo `codeError`;
- no modifica otros errores;
- no guarda automáticamente;
- ignora aplicación durante carga;
- ignora aplicación durante guardado;
- conserva funcionamiento en `CREATE`.

### Activity e instrumentación

- icono visible en `EDIT` cargado;
- icono deshabilitado durante carga;
- icono deshabilitado durante guardado;
- escáner cancelado conserva código;
- código igual muestra información sin reemplazo;
- código diferente muestra confirmación;
- confirmación muestra ambos valores;
- cancelar conserva el anterior;
- confirmar aplica el nuevo;
- otros campos permanecen;
- rotación no repite confirmación;
- `CREATE` mantiene su flujo anterior.

### Persistencia

Las pruebas existentes deberán seguir cubriendo:

- actualización conserva id;
- actualización conserva `createdAt`;
- actualización renueva `updatedAt`;
- propio id no es duplicado;
- conflicto con otro id devuelve duplicado;
- registro inexistente devuelve `NOT_FOUND`.

### Manuales

- escaneo desde edición;
- cancelación;
- mismo código;
- código diferente;
- confirmación;
- salida sin guardar;
- guardado correcto;
- duplicado;
- ceros iniciales;
- código alfanumérico;
- rotación;
- modo avión.

---

## 27. Tareas de implementación

1. Confirmar HU-20 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-21-sustituir-codigo-escaneado`.
4. Revisar el flujo real de `ItemFormActivity`.
5. Habilitar el icono en `EDIT` cuando sea editable.
6. Mantenerlo deshabilitado durante carga y guardado.
7. Conservar el comportamiento de `CREATE`.
8. Procesar el resultado escaneado según el modo.
9. Añadir detección de código igual.
10. Añadir confirmación con código actual y nuevo.
11. Aplicar el código solo tras confirmar.
12. Ampliar `applyScannedCode(...)` o introducir un método más explícito.
13. Conservar id y demás campos.
14. Añadir strings.
15. Ampliar pruebas del ViewModel.
16. Ampliar pruebas de Activity.
17. Verificar pruebas existentes de actualización y duplicados.
18. Ejecutar pruebas unitarias.
19. Ejecutar lint.
20. Ejecutar build debug.
21. Ejecutar pruebas instrumentadas.
22. Verificar funcionamiento sin conexión.
23. Verificar criterios de aceptación.
24. Integrar en `develop`.
25. Verificar CI de `develop`.
26. Eliminar la rama tras confirmar la integración.

---

## 28. Evidencias necesarias

- acción Escanear código visible en `EDIT`;
- acción bloqueada durante carga;
- código igual sin sustitución;
- confirmación con código actual y nuevo;
- cancelación conserva código;
- sustitución cambia solo código;
- salida sin guardar conserva Room;
- guardado conserva id;
- guardado conserva `createdAt`;
- guardado renueva `updatedAt`;
- duplicado de otra mercancía controlado;
- ceros iniciales;
- rotación;
- modo avión;
- pruebas unitarias;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 29. Definición de terminado

HU-21 estará terminada cuando:

- el formulario `EDIT` permita abrir el escáner;
- la acción solo esté disponible con un registro editable;
- `CREATE` conserve su comportamiento;
- cancelar el escáner no cambie el código;
- un resultado igual no produzca reemplazo;
- un resultado diferente requiera confirmación;
- la confirmación muestre código actual y nuevo;
- cancelar conserve el código anterior;
- confirmar sustituya únicamente el código;
- los demás campos permanezcan;
- el código pueda editarse manualmente;
- Room no cambie antes de Guardar;
- salir sin guardar conserve el registro original;
- Guardar reutilice la validación existente;
- Guardar reutilice el control de duplicados;
- el propio id no se considere duplicado;
- un conflicto con otro id sea rechazado;
- el id se conserve;
- `createdAt` se conserve;
- `updatedAt` se renueve al guardar;
- los ceros iniciales se mantengan;
- la rotación no repita el resultado;
- no se modifique el esquema Room;
- no se duplique el escáner;
- funcione completamente sin conexión;
- las pruebas definidas sean satisfactorias;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 30. Validación técnica final

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

- escaneo desde `EDIT`;
- código igual;
- confirmación y cancelación;
- salida sin guardar;
- actualización correcta;
- duplicado;
- rotación;
- modo avión.

---

## 31. Resultado esperado

Al cerrar HU-21:

```text
usuario edita una mercancía
        ↓
escanea un código nuevo
        ↓
compara código actual y nuevo
        ↓
confirma la sustitución
        ↓
revisa el formulario
        ↓
pulsa Guardar
        ↓
Room actualiza la misma mercancía
```

La siguiente historia será:

```text
HU-22 — Consolidar permisos y errores del escáner
```
