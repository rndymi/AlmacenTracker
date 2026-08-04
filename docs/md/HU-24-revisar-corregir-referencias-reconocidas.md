# HU-24 — Revisar y corregir referencias reconocidas

> Séptima historia de usuario de AlmacenTracker v1.2.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android:** 1.2.0  
**Historia:** HU-24  
**Nombre:** Revisar y corregir referencias reconocidas  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-24-revisar-referencias-reconocidas`  
**Rama de integración:** `develop`  
**Dependencia principal:** HU-23 — Capturar o seleccionar una lista de referencias  
**Issue prevista:** `#27`

---

## 2. Historia de usuario

Como usuario,  
quiero revisar y corregir las referencias propuestas a partir del texto reconocido,  
para confirmar una lista fiable antes de consultar dónde se encuentra cada mercancía.

---

## 3. Objetivo

Transformar las líneas de texto obtenidas por OCR en una lista editable de referencias de mercancía.

Flujo previsto:

```text
RecognizedDocument
        ↓
WarehouseReferenceParser
        ↓
referencias propuestas
        ↓
ReferenceListReviewActivity
        ↓
editar / eliminar / añadir
        ↓
normalizar y validar
        ↓
deduplicar
        ↓
confirmar lista
```

HU-24 deberá permitir que el usuario controle el resultado antes de cualquier consulta en Room.

La historia no asumirá que el OCR es correcto. La revisión será obligatoria.

---

## 4. Regla principal

La fuente OCR solo propone texto.

```text
OCR
    → puede confundir caracteres
    → puede omitir referencias
    → puede detectar texto irrelevante
```

La lista confirmada únicamente podrá contener referencias que cumplan la regla funcional:

```text
exactamente dos letras iniciales
+
uno o más dígitos
+
una letra final opcional
```

Ejemplo:

```text
MR 1210 A
```

Resultado:

```text
category = MR
code = 1210A
```

La identidad seguirá siendo:

```text
category + code
```

---

## 5. Referencias del proyecto

La HU-24 deberá respetar:

- `AlmacenTracker-Project-Plan.md`;
- `Ver-1.2-general-plan.md`;
- `HU-23-capturar-seleccionar-lista-referencias.md`;
- el estado real de `AlmacenTrackerHU23.zip`;
- la arquitectura MVVM organizada por funcionalidades;
- la identidad funcional `categoría + código`;
- el orden de aparición de las líneas OCR;
- la revisión obligatoria antes de consultar Room;
- la regla de dos letras iniciales;
- la letra final opcional;
- la privacidad del flujo documental;
- el funcionamiento completamente sin conexión;
- la ausencia de persistencia en esta historia;
- la política de no crear abstracciones sin responsabilidad real.

El plan de v1.2 asigna a HU-24:

- separación entre categoría y código;
- normalización;
- eliminación de falsos positivos;
- edición;
- adición manual;
- deduplicación;
- confirmación de la lista.

---

## 6. Estado real antes de HU-24

El análisis de `AlmacenTrackerHU23.zip` confirma que el proyecto ya dispone de:

```text
core/document/
├── DocumentImageSource.java
├── DocumentRecognitionCallback.java
├── DocumentTextRecognizer.java
├── RecognizedDocument.java
└── RecognizedTextLine.java
```

```text
data/document/
├── AndroidDocumentImageLoader.java
└── MlKitDocumentTextRecognizer.java
```

```text
feature/reference_list/capture/
├── ReferenceListCaptureActivity.java
├── ReferenceListCaptureUiState.java
├── ReferenceListCaptureViewModel.java
└── ReferenceListCaptureViewModelFactory.java
```

La implementación actual permite:

- tomar una fotografía;
- seleccionar una imagen;
- procesar capturas de pantalla;
- ejecutar OCR local;
- conservar líneas y orden;
- mostrar el texto reconocido;
- controlar imagen vacía o ilegible;
- mantener Room sin cambios;
- eliminar temporales propios;
- funcionar sin conexión.

El resultado actual se muestra como texto técnico y avisa que la revisión estará disponible posteriormente.

El proyecto todavía no dispone de:

- modelo de referencia extraída;
- parser de referencias;
- validación específica del formato documental;
- pantalla de revisión;
- lista editable;
- eliminación de falsos positivos;
- adición manual;
- deduplicación;
- contrato de lista confirmada.

HU-24 deberá completar esas capacidades sin incorporar todavía la consulta de ubicaciones.

---

## 7. Alcance incluido

HU-24 incluye:

- recibir las líneas reconocidas por HU-23;
- conservar su orden original;
- analizar cada línea;
- detectar cero, una o varias referencias dentro de una línea;
- admitir espacios opcionales;
- convertir letras a mayúsculas mediante `Locale.ROOT`;
- separar categoría y código;
- conservar ceros iniciales;
- conservar una letra final opcional;
- ignorar texto que no forme una referencia válida;
- evitar coincidencias parciales dentro de palabras o cadenas alfanuméricas mayores;
- crear propuestas editables;
- mostrar el texto OCR de origen de cada propuesta cuando aporte contexto;
- mostrar una lista ordenada;
- editar categoría;
- editar código;
- validar cada edición;
- eliminar una propuesta;
- añadir una referencia manual;
- rechazar una referencia manual inválida;
- permitir corregir confusiones OCR;
- no corregir automáticamente caracteres ambiguos;
- consolidar duplicados exactos;
- conservar la posición de la primera aparición;
- informar cuando se consoliden duplicados;
- impedir confirmar una lista vacía;
- impedir confirmar referencias inválidas;
- confirmar una colección normalizada;
- devolver la lista confirmada mediante un contrato pequeño;
- conservar cambios ante rotación;
- evitar confirmaciones repetidas;
- no consultar Room;
- no modificar Room;
- no crear historial;
- no procesar cantidades;
- funcionar sin conexión;
- accesibilidad;
- pruebas unitarias;
- pruebas de ViewModel;
- pruebas de Activity;
- CI.

---

## 8. Alcance excluido

HU-24 no incluye:

- consultar ubicaciones;
- buscar mercancía en Room;
- mostrar sitio o posición;
- identificar referencias no encontradas;
- abrir el detalle de mercancía;
- persistir la lista confirmada;
- crear historial de mercancía sacada;
- guardar título;
- guardar cantidades;
- guardar unidades;
- interpretar piezas, paquetes o cajas;
- interpretar tallas;
- interpretar clientes;
- sumar cantidades;
- modificar stock;
- volver a ejecutar OCR;
- modificar la imagen;
- recortar la fotografía;
- corregir perspectiva;
- superponer datos sobre la imagen;
- procesar PDF;
- procesar varias páginas;
- aplicar correcciones automáticas de `O/0`, `I/1`, `S/5` o `B/8`;
- cambiar la regla a prefijos de tres letras;
- sincronización remota;
- backend.

La consulta de ubicaciones pertenece a HU-25.

El historial persistente pertenece a v1.3.

---

## 9. Formato válido de referencia

### 9.1. Estructura

```text
dos letras
+
parte numérica
+
letra final opcional
```

Ejemplos válidos:

```text
MR1210
MR 1210
MR1210A
MR 1210A
MR1210 A
MR 1210 A
MZ 900
```

Resultado normalizado:

| Entrada | Categoría | Código |
|---|---|---|
| `MR1210` | `MR` | `1210` |
| `MR 1210 A` | `MR` | `1210A` |
| `MZ 900` | `MZ` | `900` |

### 9.2. Ejemplos inválidos

```text
M1210
MRA1210
1210
MR
MR12AB
MR-1210
```

`MR-1210` no se admitirá automáticamente mientras el formato funcional no contemple guiones.

El usuario podrá corregirlo manualmente a:

```text
MR1210
```

---

## 10. Patrón de extracción

El patrón no deberá aceptar una coincidencia incrustada dentro de otra cadena alfanumérica.

Patrón Java orientativo:

```regex
(?<![A-Z0-9])([A-Z]{2})\s*([0-9]+)\s*([A-Z]?)(?![A-Z0-9])
```

Grupos:

```text
group 1 → categoría
group 2 → parte numérica
group 3 → letra final opcional
```

Ejemplo:

```text
"Reponer MR 1210 A - 3 cajas"
```

Resultado:

```text
MR + 1210A
```

El patrón no deberá extraer:

```text
MRA1210
```

como:

```text
RA + 1210
```

La implementación deberá encapsular el patrón en dominio y no definirlo en la Activity, adapter o ViewModel.

---

## 11. Normalización previa al análisis

Cada línea se preparará mediante reglas limitadas:

```text
null → sin resultados
trim()
uppercase con Locale.ROOT
normalizar espacios Unicode a espacios ordinarios
reducir secuencias de espacios cuando sea necesario
```

No se deberá:

- eliminar todos los signos de puntuación indiscriminadamente;
- transformar letras ambiguas en dígitos;
- eliminar ceros iniciales;
- convertir el código a número;
- modificar el texto OCR original conservado para contexto.

Ejemplo:

```text
rawText = "  mr   001210   a - 2 cajas  "
```

Propuesta:

```text
category = MR
code = 001210A
```

---

## 12. Confusiones OCR

El OCR puede producir:

```text
O ↔ 0
I ↔ 1
S ↔ 5
B ↔ 8
```

HU-24 deberá permitir corregirlas, pero no deberá resolverlas automáticamente.

Motivo:

```text
MR 1O50
```

podría significar:

```text
MR 1050
```

pero una corrección automática también podría crear una referencia incorrecta.

Comportamiento:

- una propuesta que no cumpla la regla podrá omitirse;
- el texto OCR original continuará visible como contexto;
- el usuario podrá añadir o editar la referencia correcta;
- la confirmación solo aceptará el valor válido resultante.

---

## 13. Múltiples referencias en una línea

Una línea puede contener más de una referencia:

```text
MR1210A / MZ1300C
```

El parser deberá devolver ambas, en orden:

```text
1. MR + 1210A
2. MZ + 1300C
```

Una línea sin referencias:

```text
Tienda Centro
```

no generará ninguna propuesta.

Una línea con cantidad:

```text
MR1210A - 3 cajas
```

generará únicamente:

```text
MR + 1210A
```

---

## 14. Modelo de dominio

Modelo recomendado:

```text
WarehouseReference
├── category
└── code
```

Responsabilidades:

- representar una referencia válida y normalizada;
- exponer clave de identidad;
- implementar igualdad por categoría y código;
- no depender de Android;
- no depender de Room;
- no contener cantidades;
- no contener ubicación;
- no contener datos OCR.

Clave de identidad:

```text
category + "\u0000" + code
```

o un objeto equivalente.

No se recomienda concatenar sin separador:

```text
MR + 1210
```

porque puede crear claves ambiguas en otros formatos futuros.

---

## 15. Resultado del parser

Contrato orientativo:

```text
WarehouseReferenceParser
```

Operaciones:

```text
parseLine(rawText)
parseLines(recognizedLines)
validate(category, code)
```

Resultado de una línea:

```text
WarehouseReferenceMatch
├── reference
├── sourceLineIndex
├── sourceRawText
└── occurrenceIndex
```

El modelo de coincidencia podrá ubicarse fuera del dominio puro si contiene contexto de presentación.

El parser deberá:

- ser determinista;
- conservar orden;
- aceptar varias coincidencias;
- no deduplicar internamente si esa responsabilidad pertenece al servicio;
- no acceder a Room;
- no conocer Views;
- no corregir caracteres ambiguos.

---

## 16. Propuestas de revisión

Cada propuesta de la pantalla podrá contener:

```text
id temporal
category
code
sourceLineIndex
sourceRawText
isManuallyAdded
validationError
```

El id temporal:

- existirá únicamente durante la sesión;
- no será un id de Room;
- permitirá editar y eliminar una fila concreta;
- no se utilizará como identidad funcional.

La identidad funcional seguirá siendo:

```text
category + code
```

---

## 17. Pantalla de revisión

Nombre orientativo:

```text
ReferenceListReviewActivity
```

La pantalla deberá incluir:

- Toolbar;
- explicación breve;
- contador de referencias;
- RecyclerView;
- acción Añadir referencia;
- acción Confirmar;
- estado vacío;
- mensaje de validación;
- opción Volver.

Cada fila deberá mostrar:

- categoría;
- código;
- texto OCR de origen cuando exista;
- acción Editar;
- acción Eliminar;
- indicación de error cuando corresponda.

No se mostrará sitio ni posición en HU-24.

---

## 18. Edición de una propuesta

Al pulsar Editar se mostrará un formulario breve:

```text
Categoría
Código
```

Reglas:

- categoría obligatoria;
- exactamente dos letras;
- código obligatorio;
- uno o más dígitos;
- letra final opcional;
- espacios externos eliminados;
- categoría y código convertidos a mayúsculas;
- ceros iniciales conservados;
- Guardar solo aplica una referencia válida;
- Cancelar no modifica la propuesta.

La edición no utilizará el formulario completo de mercancía.

No se reutilizará `ItemFormActivity`, porque no se está creando ni modificando una mercancía.

---

## 19. Adición manual

La acción:

```text
Añadir referencia
```

abrirá el mismo formulario breve.

Ejemplos aceptados:

```text
Categoría: MR
Código: 1210A
```

También podrá admitirse una entrada única:

```text
MR 1210 A
```

solo si reutiliza exactamente el mismo parser y no duplica reglas.

Para reducir complejidad inicial se recomienda mantener dos campos:

```text
Categoría
Código
```

Una referencia manual:

- se añade al final;
- se marca como manual solo para presentación temporal;
- queda sometida a deduplicación;
- no se persiste.

---

## 20. Eliminación de falsos positivos

Cada propuesta podrá eliminarse individualmente.

Ejemplo de falso positivo:

```text
AB 2026
```

podría ser una fecha o identificador ajeno a mercancía, aunque cumpla el patrón.

El patrón estricto reduce falsos positivos, pero no puede eliminarlos todos.

Por eso la revisión humana seguirá siendo obligatoria.

La eliminación:

- no modifica el texto OCR original;
- no modifica la imagen;
- no afecta a otras propuestas;
- no requiere confirmación adicional para una sola fila, salvo que pruebas de uso demuestren lo contrario.

Podrá ofrecerse una acción Deshacer mediante Snackbar.

---

## 21. Deduplicación

La deduplicación utilizará:

```text
category normalizada + code normalizado
```

Ejemplo:

```text
MR1210A
MR 1210 A
mr1210a
```

Resultado confirmado:

```text
MR + 1210A
```

Reglas:

- conservar la primera aparición;
- eliminar duplicados posteriores;
- mantener el orden relativo de las referencias únicas;
- no sumar cantidades;
- no utilizar el número de repeticiones como stock;
- aplicar deduplicación tras extracción;
- volver a aplicarla después de editar o añadir;
- informar al usuario cuando una acción produzca un duplicado.

Mensaje orientativo:

```text
La referencia ya estaba incluida en la lista.
```

No se añadirán dos filas idénticas.

---

## 22. Estado vacío

La pantalla puede quedar vacía porque:

- OCR no produjo referencias válidas;
- todas las propuestas fueron eliminadas;
- el texto solo contenía títulos o cantidades.

Mensaje:

```text
No hay referencias para confirmar.
Revisa el texto reconocido o añade una referencia manualmente.
```

Acciones:

```text
Añadir referencia
Volver
```

Confirmar estará deshabilitado.

---

## 23. Confirmación de la lista

Antes de confirmar deberá cumplirse:

- existe al menos una referencia;
- todas son válidas;
- no existen duplicados;
- categoría y código están normalizados.

Flujo:

```text
usuario pulsa Confirmar
        ↓
ViewModel valida
        ↓
colección inmutable y ordenada
        ↓
evento único de confirmación
```

HU-24 no consultará Room.

La lista confirmada quedará preparada para HU-25.

---

## 24. Contrato de entrada

No se deberán enviar:

- Bitmap;
- imagen original;
- tipos de ML Kit;
- colecciones grandes serializadas;
- objetos Android dentro del dominio.

Dado que la primera versión trabaja aproximadamente con hasta 15 referencias, podrá transferirse la entrada mediante:

```text
ArrayList<String> con líneas OCR
```

Contrato orientativo:

```text
EXTRA_RECOGNIZED_LINES
```

`ReferenceListReviewActivity.createIntent(...)` deberá copiar:

```text
RecognizedDocument.getLines()
    → rawText ordenado
    → ArrayList<String>
```

La Activity de revisión reconstruirá propuestas mediante el parser.

La ausencia o vaciado del extra producirá un estado vacío controlado.

---

## 25. Contrato de salida

La lista confirmada podrá devolverse mediante:

```text
RESULT_OK
EXTRA_CONFIRMED_CATEGORIES
EXTRA_CONFIRMED_CODES
```

o una colección compacta de valores normalizados:

```text
MR\u001F1210A
MZ\u001F1300C
```

Se recomienda evitar dos listas paralelas si puede definirse un DTO pequeño compatible con el contrato.

No se enviarán objetos de Room.

No se enviarán imágenes.

No se utilizará Java Serialization para un grafo innecesario.

El contrato definitivo deberá ser fácilmente consumible por HU-25.

En HU-24, la Activity llamadora podrá mostrar una confirmación temporal y conservar la lista solo durante el flujo activo.

---

## 26. Estado de interfaz

Estado orientativo:

```text
ReferenceListReviewUiState
├── proposals
├── invalidCount
├── duplicateNotice
├── canConfirm
└── status
```

Estados posibles:

```text
LOADING
READY
EMPTY
CONFIRMING
ERROR
```

`LOADING` solo será necesario si el análisis inicial se realiza fuera del hilo principal.

Para una lista pequeña, el parser podrá ejecutarse de forma síncrona dentro del ViewModel si se demuestra que no bloquea la UI.

No se almacenarán Views, Context o tipos de Android en el estado.

---

## 27. Conservación ante recreación

HU-24 deberá conservar:

- propuestas actuales;
- correcciones;
- eliminaciones;
- referencias manuales;
- orden;
- estado vacío;
- validaciones.

No deberá:

- volver a parsear las líneas y sobrescribir cambios;
- volver a añadir propuestas eliminadas;
- repetir la confirmación;
- abrir dos veces HU-25 en el futuro.

Las líneas iniciales deberán aplicarse una sola vez.

---

## 28. Accesibilidad

La pantalla deberá incluir:

- título descriptivo;
- contador anunciado;
- botones Editar y Eliminar con referencia identificable;
- campos con labels;
- errores asociados a los campos;
- objetivos táctiles de al menos 48 dp;
- orden de foco coherente;
- soporte para escalado de texto;
- contraste claro y oscuro;
- estado vacío accesible;
- anuncio al añadir, editar, eliminar o detectar duplicado;
- acción Confirmar con descripción clara.

Ejemplo de descripción:

```text
Editar referencia MR 1210A
```

---

## 29. Flujo principal

1. HU-23 obtiene líneas OCR.
2. El usuario continúa a revisión.
3. `ReferenceListReviewActivity` recibe las líneas.
4. El ViewModel las aplica una sola vez.
5. `WarehouseReferenceParser` analiza cada línea.
6. Se generan propuestas válidas.
7. Se deduplican conservando la primera aparición.
8. La lista se muestra en orden.
9. El usuario revisa cada referencia.
10. Corrige o elimina cuando sea necesario.
11. Añade referencias omitidas.
12. Pulsa Confirmar.
13. El ViewModel valida la colección.
14. Se emite un resultado único.
15. Room permanece sin cambios.

---

## 30. Flujos alternativos

### FA-01 — Línea sin referencia

1. OCR devuelve `Tienda Centro`.
2. El parser no encuentra coincidencias.
3. No se crea propuesta.
4. El texto puede mantenerse como contexto general.

### FA-02 — Referencia con espacios

1. OCR devuelve `MR 1210 A`.
2. Se obtiene `MR + 1210A`.

### FA-03 — Varias referencias en una línea

1. OCR devuelve `MR1210A / MZ1300C`.
2. Se crean dos propuestas en orden.

### FA-04 — Prefijo de tres letras

1. OCR devuelve `MRA1210`.
2. No se extrae `RA1210`.
3. El usuario puede corregir manualmente si el texto real era distinto.

### FA-05 — Confusión O/0

1. OCR devuelve `MR 1O50`.
2. No se corrige automáticamente.
3. El usuario añade o edita `MR + 1050`.

### FA-06 — Falso positivo válido por patrón

1. Se propone `AB + 2026`.
2. El usuario determina que no es mercancía.
3. La elimina.

### FA-07 — Referencia omitida

1. OCR no detecta `MZ1300C`.
2. El usuario pulsa Añadir referencia.
3. La introduce manualmente.

### FA-08 — Duplicado OCR

1. Aparecen `MR1210A` y `MR 1210 A`.
2. Solo se conserva la primera.
3. Se informa de la consolidación.

### FA-09 — Duplicado manual

1. El usuario intenta añadir una referencia existente.
2. No se añade otra fila.
3. Se informa que ya está incluida.

### FA-10 — Edición crea duplicado

1. Se editan `MR1210A` y `MZ1300C`.
2. La segunda se cambia a `MR1210A`.
3. La operación se rechaza o consolida de manera explícita.
4. No quedan duplicados silenciosos.

### FA-11 — Lista vacía

1. Ninguna línea produce referencias.
2. Se muestra estado vacío.
3. Confirmar permanece deshabilitado.
4. El usuario puede añadir manualmente.

### FA-12 — Rotación

1. El usuario ha corregido y eliminado filas.
2. El dispositivo rota.
3. Los cambios permanecen.
4. No se reconstruye la lista original.

---

## 31. Criterios de aceptación

### CA-01 — Extracción de referencia simple

**Dado** `MR1210`,  
**cuando** se procesa la línea,  
**entonces** se propone categoría `MR` y código `1210`.

### CA-02 — Espacios opcionales

**Dado** `MR 1210 A`,  
**cuando** se procesa,  
**entonces** se propone `MR + 1210A`.

### CA-03 — Dos letras obligatorias

**Dado** `M1210` o `MRA1210`,  
**cuando** se procesa,  
**entonces** no se crea una referencia parcial.

### CA-04 — Letra final opcional

**Dado** `MZ1300C`,  
**cuando** se procesa,  
**entonces** la letra `C` forma parte del código.

### CA-05 — Ceros iniciales

**Dado** `MR001210`,  
**cuando** se normaliza,  
**entonces** el código conserva `001210`.

### CA-06 — Varias coincidencias

**Dado** una línea con dos referencias,  
**cuando** se procesa,  
**entonces** ambas aparecen en el mismo orden.

### CA-07 — Texto irrelevante

**Dado** una línea sin formato válido,  
**cuando** se procesa,  
**entonces** no genera una propuesta.

### CA-08 — Edición

**Dado** una propuesta incorrecta,  
**cuando** el usuario la edita con valores válidos,  
**entonces** la lista muestra el resultado normalizado.

### CA-09 — Eliminación

**Dado** un falso positivo,  
**cuando** el usuario lo elimina,  
**entonces** deja de formar parte de la lista confirmable.

### CA-10 — Adición manual

**Dado** que falta una referencia,  
**cuando** el usuario la añade correctamente,  
**entonces** aparece al final de la lista.

### CA-11 — Rechazo de entrada inválida

**Dado** una categoría o código inválidos,  
**cuando** se intenta guardar la edición,  
**entonces** se muestran errores y no se aplica.

### CA-12 — Deduplicación

**Dado** que la misma referencia aparece con espacios o mayúsculas diferentes,  
**cuando** se procesa,  
**entonces** solo permanece una referencia normalizada.

### CA-13 — Orden

**Dado** un conjunto de referencias OCR,  
**cuando** se revisa,  
**entonces** se conserva el orden de su primera aparición.

### CA-14 — Lista vacía

**Dado** que no existen referencias,  
**cuando** se muestra la pantalla,  
**entonces** Confirmar está deshabilitado y puede añadirse manualmente.

### CA-15 — Confirmación

**Dado** que todas las referencias son válidas y únicas,  
**cuando** el usuario confirma,  
**entonces** se produce una colección ordenada y normalizada.

### CA-16 — Rotación

**Dado** que el usuario modificó la lista,  
**cuando** la Activity se recrea,  
**entonces** los cambios permanecen.

### CA-17 — Room sin cambios

**Dado** cualquier operación de revisión,  
**cuando** finaliza HU-24,  
**entonces** Room no se consulta ni se modifica.

### CA-18 — Funcionamiento offline

**Dado** el dispositivo sin conexión,  
**cuando** se revisan y confirman referencias,  
**entonces** todo el flujo continúa operativo.

---

## 32. Diseño técnico propuesto

### Dominio

```text
domain/reference/
├── WarehouseReference.java
├── WarehouseReferenceParser.java
├── WarehouseReferenceValidator.java
└── WarehouseReferenceMatch.java
```

`WarehouseReferenceValidator` solo deberá existir separado si evita duplicación real.

Si parser y validación comparten una regla pequeña y cohesionada, podrán residir en una única clase.

### Feature

```text
feature/reference_list/review/
├── ReferenceListReviewActivity.java
├── ReferenceListReviewAdapter.java
├── ReferenceListReviewUiState.java
├── ReferenceListReviewViewModel.java
├── ReferenceListReviewViewModelFactory.java
└── ReferenceProposal.java
```

Los diálogos de edición podrán implementarse dentro de la Activity si no contienen lógica de negocio.

### Composición

`ReferenceListModule` podrá proporcionar:

- parser;
- Factory del ViewModel de revisión.

No deberá guardar estado mutable de una lista activa.

---

## 33. Archivos previstos

Archivos probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── domain/reference/
│   ├── WarehouseReference.java
│   ├── WarehouseReferenceParser.java
│   └── WarehouseReferenceMatch.java
└── feature/reference_list/review/
    ├── ReferenceListReviewActivity.java
    ├── ReferenceListReviewAdapter.java
    ├── ReferenceListReviewUiState.java
    ├── ReferenceListReviewViewModel.java
    ├── ReferenceListReviewViewModelFactory.java
    └── ReferenceProposal.java
```

Cambios probables:

```text
app/src/main/java/com/rndymi/almacentracker/
├── app/AppContainer.java
├── app/di/ReferenceListModule.java
└── feature/reference_list/capture/
    └── ReferenceListCaptureActivity.java
```

Recursos probables:

```text
app/src/main/res/
├── layout/activity_reference_list_review.xml
├── layout/item_reference_proposal.xml
├── layout/dialog_reference_editor.xml
├── values/strings.xml
└── drawable/
```

Pruebas probables:

```text
app/src/test/java/com/rndymi/almacentracker/
├── domain/reference/
│   ├── WarehouseReferenceTest.java
│   └── WarehouseReferenceParserTest.java
└── feature/reference_list/review/
    ├── ReferenceListReviewUiStateTest.java
    └── ReferenceListReviewViewModelTest.java

app/src/androidTest/java/com/rndymi/almacentracker/
└── feature/reference_list/review/
    └── ReferenceListReviewActivityContractTest.java
```

No se prevén cambios en:

- entidad Room;
- esquema Room;
- DAO;
- repositorio de mercancía;
- `ScannerActivity`;
- CameraX;
- ML Kit Barcode Scanning;
- ML Kit Text Recognition;
- permisos;
- `ItemFormActivity`.

---

## 34. Pruebas

### Dominio

- `MR1210`;
- `MR 1210`;
- `MR1210A`;
- `MR 1210 A`;
- minúsculas;
- espacios múltiples;
- espacios Unicode;
- ceros iniciales;
- una letra inicial;
- tres letras iniciales;
- ausencia de dígitos;
- dos letras finales;
- cadena alfanumérica envolvente;
- varias referencias por línea;
- título sin referencia;
- cantidad después de referencia;
- orden;
- igualdad por categoría y código.

### ViewModel

- aplica líneas iniciales una vez;
- genera propuestas;
- deduplica;
- conserva primera aparición;
- elimina;
- deshace cuando se implemente;
- añade manualmente;
- rechaza inválida;
- edita;
- edición crea duplicado;
- mantiene orden;
- estado vacío;
- `canConfirm`;
- confirmación única;
- rotación no reaplica líneas;
- no consulta repositorio.

### Adapter y Activity

- renderiza categoría y código;
- muestra texto de origen;
- Editar abre formulario;
- Cancelar edición no cambia;
- Guardar edición válida actualiza;
- error se asocia al campo;
- Eliminar quita la fila;
- Añadir agrega al final;
- Confirmar deshabilitado con lista vacía;
- contrato de entrada conserva orden;
- contrato de salida conserva orden;
- recreación conserva cambios;
- accesibilidad de acciones.

### Manuales

- lista impresa;
- lista manuscrita clara;
- falsos positivos;
- referencias omitidas;
- duplicados;
- código con letra final;
- ceros iniciales;
- más de una referencia por línea;
- lista sin referencias;
- fuente grande;
- modo oscuro;
- rotación;
- modo avión.

---

## 35. Tareas de implementación

1. Confirmar HU-23 integrada en `develop`.
2. Verificar CI de `develop`.
3. Crear `feature/hu-24-revisar-referencias-reconocidas`.
4. Crear `WarehouseReference`.
5. Implementar parser centralizado.
6. Crear pruebas exhaustivas del parser.
7. Crear modelos de propuesta.
8. Crear estado de revisión.
9. Crear ViewModel.
10. Implementar aplicación única de líneas.
11. Implementar extracción múltiple.
12. Implementar deduplicación ordenada.
13. Crear Factory y composición.
14. Crear Activity de revisión.
15. Crear RecyclerView y adapter.
16. Crear fila de propuesta.
17. Implementar edición.
18. Implementar eliminación.
19. Implementar adición manual.
20. Implementar validaciones.
21. Implementar estado vacío.
22. Implementar confirmación.
23. Definir contrato de entrada.
24. Definir contrato de salida.
25. Conectar HU-23 con HU-24.
26. Añadir strings y accesibilidad.
27. Crear pruebas del ViewModel.
28. Crear pruebas de contrato de Activity.
29. Ejecutar pruebas unitarias.
30. Ejecutar lint.
31. Ejecutar build debug.
32. Ejecutar pruebas instrumentadas.
33. Verificar funcionamiento offline.
34. Verificar Room sin cambios.
35. Verificar criterios de aceptación.
36. Integrar en `develop`.
37. Verificar CI de `develop`.
38. Eliminar la rama tras confirmar la integración.

---

## 36. Evidencias necesarias

- navegación desde texto reconocido;
- propuestas extraídas;
- regla de dos letras;
- letra final opcional;
- ceros iniciales;
- múltiples referencias en una línea;
- rechazo de prefijo de tres letras;
- texto irrelevante ignorado;
- contexto OCR visible;
- edición;
- eliminación;
- adición manual;
- error de validación;
- deduplicación;
- orden conservado;
- estado vacío;
- confirmación de lista;
- rotación;
- modo avión;
- Room sin consultas;
- pruebas del parser;
- pruebas del ViewModel;
- pruebas instrumentadas;
- lint satisfactorio;
- `assembleDebug` satisfactorio;
- CI satisfactoria.

---

## 37. Definición de terminado

HU-24 estará terminada cuando:

- las líneas de HU-23 lleguen a revisión;
- el parser esté centralizado;
- se reconozcan exactamente dos letras iniciales;
- se reconozcan uno o más dígitos;
- se conserve una letra final opcional;
- se conserven ceros iniciales;
- no se extraigan coincidencias dentro de cadenas mayores;
- se detecten varias referencias por línea;
- el texto irrelevante no cree propuestas;
- las propuestas se muestren en orden;
- el usuario pueda editar;
- el usuario pueda eliminar;
- el usuario pueda añadir;
- las entradas inválidas se rechacen;
- no se apliquen correcciones OCR automáticas ambiguas;
- los duplicados se consoliden;
- se conserve la primera aparición;
- una lista vacía no pueda confirmarse;
- la lista confirmada sea válida, única y normalizada;
- los cambios sobrevivan a recreación;
- la confirmación se emita una sola vez;
- Room no se consulte;
- Room no se modifique;
- no se cree historial;
- no se interpreten cantidades;
- no se modifique stock;
- funcione completamente sin conexión;
- HU-23 continúe operativa;
- las pruebas definidas sean satisfactorias;
- lint no presente errores;
- `assembleDebug` finalice correctamente;
- las pruebas instrumentadas necesarias finalicen correctamente;
- CI de la rama sea satisfactoria;
- la integración en `develop` sea satisfactoria.

---

## 38. Validación técnica final

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

- extracción;
- corrección;
- eliminación;
- adición;
- deduplicación;
- lista vacía;
- confirmación;
- rotación;
- modo avión.

---

## 39. Resultado esperado

Al cerrar HU-24:

```text
texto OCR
    ↓
referencias propuestas
    ↓
revisión manual
    ↓
lista válida, normalizada y sin duplicados
    ↓
Room permanece sin cambios
```

La siguiente historia será:

```text
HU-25 — Mostrar ubicaciones de una lista reconocida
```
