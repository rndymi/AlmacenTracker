# AlmacenTracker — Plan de la versión 1.2

> Tercera entrega funcional: escaneo individual y reconocimiento de listas para localizar mercancía.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión funcional:** 1.2  
**Versión Android prevista:** 1.2.0  
**Nombre de la versión:** Escaneo y localización asistida  
**Estado:** En desarrollo  
**Rama de integración:** `develop`  
**Versión estable de partida:** `v1.1.0`

---

## 2. Contexto de partida

AlmacenTracker v1.1.0 ya permite:

- listar, registrar, consultar, buscar, filtrar, editar y eliminar mercancía;
- impedir combinaciones duplicadas de categoría y código;
- importar, exportar, compartir, respaldar y restaurar información mediante CSV;
- mantener Room como fuente de verdad;
- funcionar completamente sin conexión.

Antes de iniciar v1.2, el proyecto fue migrado a una arquitectura MVVM organizada por funcionalidades:

```text
app
core
domain
data
feature
```

La versión 1.2 se organizará en historias consecutivas.

Las primeras historias establecerán el escaneo individual y su integración con el inventario. Las historias posteriores reutilizarán esa base para capturar imágenes de listas, reconocer referencias y mostrar sus ubicaciones.

Cada historia mantendrá un alcance independiente y deberá dejar la aplicación funcional antes de iniciar la siguiente.

---

## 3. Objetivo de la versión

Permitir localizar mercancía mediante dos formas complementarias:

### Escaneo individual

```text
código de barras o QR
        ↓
valor textual
        ↓
búsqueda o formulario
```

### Reconocimiento de listas

```text
fotografía, imagen o captura
        ↓
OCR local
        ↓
referencias reconocidas
        ↓
revisión del usuario
        ↓
consulta de ubicaciones en Room
```

La versión cumplirá su objetivo cuando ayude al usuario a encontrar físicamente los productos.

La versión 1.2 no registrará todavía un historial persistente de mercancía sacada. Esa capacidad pertenecerá a v1.3.

---

## 4. Regla de oro de la versión

> AlmacenTracker debe identificar referencias y mostrar su ubicación.

La aplicación no utilizará las cantidades de una lista para gestionar stock.

Aunque una línea contenga:

```text
MR 1210 A - 3 paquetes - tallas S, M y L
```

v1.2 utilizará únicamente:

```text
categoría = MR
código = 1210A
```

para consultar:

```text
sitio
posición
```

La aplicación ignorará para la localización:

- piezas;
- unidades;
- paquetes;
- cajas;
- tallas;
- clientes;
- cantidades;
- instrucciones de reposición.

Estas informaciones podrán utilizarse posteriormente como datos documentales del historial de v1.3, pero nunca reducirán stock automáticamente.

---

## 5. Alcance incluido

La versión 1.2 incluirá:

- escaneo individual de códigos de barras;
- escaneo individual de códigos QR;
- búsqueda de mercancía mediante un código escaneado;
- uso del escáner en creación;
- uso del escáner en edición;
- permisos y errores de cámara;
- captura de una lista mediante cámara;
- selección de una imagen mediante el selector de fotos de Android;
- procesamiento de capturas de pantalla como imágenes seleccionadas;
- OCR local de listas impresas;
- OCR de escritura manual clara como capacidad condicionada a la precisión;
- extracción de referencias completas;
- revisión y corrección antes de consultar Room;
- eliminación de falsos positivos;
- adición manual de referencias omitidas;
- consolidación de referencias repetidas;
- consulta conjunta de ubicaciones;
- conservación del orden de aparición;
- identificación de referencias no encontradas;
- acceso al detalle de la mercancía encontrada;
- funcionamiento completamente sin conexión;
- privacidad de imágenes;
- pruebas unitarias, Room e instrumentadas;
- CI;
- release `v1.2.0`.

---

## 6. Alcance excluido

La versión 1.2 no incluirá:

- historial persistente de mercancía sacada;
- descuento o gestión de stock;
- actualización de cantidades almacenadas;
- historial de pedidos;
- asociación persistente con clientes;
- persistencia de piezas, paquetes o cajas;
- persistencia de tallas solicitadas;
- generación de códigos;
- impresión de etiquetas;
- escaneo masivo continuo en tiempo real;
- OCR genérico de documentos arbitrarios;
- almacenamiento permanente de fotografías;
- apertura automática de URLs de un QR;
- sincronización remota;
- autenticación;
- backend.

El registro e historial de mercancía sacada se desarrollará en v1.3.

---

## 7. Referencias de mercancía

### 7.1. Formato inicial

Una referencia reconocida desde una lista deberá tener:

```text
exactamente dos letras iniciales
+
uno o más dígitos
+
una letra final opcional
```

Espacios opcionales podrán existir entre las partes.

Ejemplos válidos:

```text
MR1210
MR 1210
MR1210A
MR 1210 A
MI1300 C
MZ 900
```

Ejemplos no admitidos inicialmente:

```text
M1210
MRA1210
1210
MR
```

### 7.2. Separación de identidad

Ejemplo:

```text
MR 1210 A
```

Resultado normalizado:

```text
category = MR
code = 1210A
```

La búsqueda deberá utilizar:

```text
category + code
```

y no únicamente la parte numérica.

### 7.3. Patrón conceptual

Validación completa:

```regex
^[A-Z]{2}\s*[0-9]+\s*[A-Z]?$
```

Extracción dentro de una línea:

```regex
\b([A-Z]{2})\s*([0-9]+)\s*([A-Z]?)\b
```

La implementación deberá encapsular esta regla en un componente de dominio y no dispersarla entre Activities.

### 7.4. Evolución futura

Si en el futuro se admiten tres letras iniciales, deberá modificarse explícitamente la regla de dominio y mantenerse coherente con cualquier backend posterior.

---

## 8. Entradas admitidas para listas

### 8.1. Cámara

El usuario podrá tomar una fotografía desde la aplicación.

La captura será explícita. No se guardará automáticamente en la galería.

### 8.2. Selector de fotos

El usuario podrá seleccionar:

- fotografía;
- imagen descargada;
- captura de pantalla;
- documento visual guardado como imagen.

Se utilizará el selector moderno de Android cuando sea compatible.

### 8.3. Impresión y escritura manual

La primera implementación priorizará:

- listas impresas;
- texto con contraste suficiente;
- una referencia por línea;
- aproximadamente hasta 15 referencias por hoja.

La escritura manual se considerará reconocible cuando sea clara, pero la revisión del usuario será obligatoria.

No se prometerá precisión absoluta sobre:

- letra irregular;
- tachaduras;
- texto superpuesto;
- imágenes desenfocadas;
- perspectiva extrema;
- iluminación insuficiente.

---

## 9. Arquitectura prevista

La versión reutilizará la arquitectura MVVM existente.

```text
feature.scanner
    → escaneo individual

feature.reference_list
    ├── capture
    ├── review
    └── location
```

Componentes de dominio orientativos:

```text
domain/reference/
├── WarehouseReference.java
├── WarehouseReferenceParser.java
└── WarehouseReferenceValidationResult.java
```

Infraestructura orientativa:

```text
data/document/
└── MlKitDocumentTextRecognizer.java
```

Flujo:

```text
Activity
    ↓
ViewModel
    ↓
servicio / parser
    ↓
repositorio
    ↓
Room
```

La implementación concreta podrá ajustar nombres, pero deberá mantener:

- OCR fuera de Activities;
- reglas de referencia fuera de infraestructura;
- Room fuera de la feature;
- imágenes fuera del dominio;
- revisión previa antes de consultar varias referencias.

---

## 10. Historias de usuario de la versión 1.2

### HU-18 — Escanear códigos de barras y códigos QR

Como usuario, quiero escanear un código mediante la cámara para obtener su valor sin escribirlo manualmente.

Incluye:

- CameraX;
- ML Kit Barcode Scanning;
- formatos lineales y QR;
- permiso contextual;
- preview;
- resultado único;
- cancelación;
- errores;
- funcionamiento offline.

HU-18 no buscará Room ni procesará listas.

---

### HU-19 — Buscar mercancía mediante un código escaneado

Como usuario, quiero buscar una mercancía con el valor escaneado para abrir rápidamente su ubicación.

Incluye:

- búsqueda exacta;
- una coincidencia;
- varias coincidencias cuando corresponda;
- ninguna coincidencia;
- apertura del detalle;
- posibilidad de registrar.

**Dependencia:** HU-18.

---

### HU-20 — Registrar mercancía con un código escaneado

Como usuario, quiero rellenar el código del formulario de alta mediante el escáner.

Incluye:

- integración con modo CREATE;
- conservación de campos;
- edición manual posterior;
- validación y duplicados existentes.

**Dependencia:** HU-18.

---

### HU-21 — Sustituir el código durante la edición

Como usuario, quiero escanear un nuevo código durante la edición para reducir errores de escritura.

Incluye:

- integración con modo EDIT;
- confirmación de reemplazo;
- conservación de id y fechas;
- validación normal al guardar.

**Dependencias:** HU-18 y HU-20.

---

### HU-22 — Consolidar permisos y errores del escáner

Como usuario, quiero recibir indicaciones claras cuando la cámara o el reconocimiento no puedan utilizarse.

Incluye:

- permiso denegado;
- permiso permanente;
- acceso voluntario a Ajustes;
- cámara no disponible;
- reintento;
- accesibilidad;
- entrada manual.

**Dependencia:** HU-18.

---

### HU-23 — Capturar o seleccionar una lista de referencias

Como usuario, quiero tomar una fotografía o seleccionar una imagen de una lista para reconocer las referencias que contiene.

Incluye:

- cámara documental;
- selección desde fotos;
- capturas de pantalla;
- OCR local;
- extracción inicial de líneas;
- no guardar la imagen permanentemente;
- no consultar todavía Room.

**Dependencias:** HU-18 y HU-22.

---

### HU-24 — Revisar y corregir referencias reconocidas

Como usuario, quiero revisar las referencias extraídas para corregir errores antes de localizar mercancía.

Incluye:

- separación categoría/código;
- regla de dos letras iniciales;
- letra final opcional;
- normalización de espacios;
- eliminación de falsos positivos;
- adición manual;
- corrección;
- deduplicación;
- confirmación de la lista.

Ignora cantidades, unidades, tallas, títulos y clientes para la búsqueda de ubicación.

**Dependencia:** HU-23.

---

### HU-25 — Mostrar ubicaciones de una lista reconocida

Como usuario, quiero consultar conjuntamente las referencias confirmadas para saber dónde se encuentra cada producto.

Incluye:

- consulta por categoría + código;
- orden de la hoja;
- sitio;
- posición;
- referencias no encontradas;
- referencias repetidas mostradas una sola vez;
- acceso al detalle;
- Room como fuente de verdad.

No crea historial persistente.

**Dependencia:** HU-24.

---

## 11. Orden de implementación

```text
HU-18 — Escáner base
        ↓
HU-19 — Búsqueda individual
        ↓
HU-20 — Alta mediante escaneo
        ↓
HU-21 — Edición mediante escaneo
        ↓
HU-22 — Permisos y errores
        ↓
HU-23 — Captura o selección de lista
        ↓
HU-24 — Revisión de referencias
        ↓
HU-25 — Ubicaciones de la lista
```

HU-18 mantendrá un alcance específico de escaneo individual.

Las capacidades de captura documental y OCR se desarrollarán en las historias asignadas a esos flujos.

---

## 12. Estrategia de OCR

La biblioteca deberá:

- funcionar localmente;
- reconocer texto latino;
- aceptar imágenes de cámara y selector;
- proporcionar bloques, líneas y elementos;
- permitir obtener coordenadas cuando aporte valor;
- no subir imágenes;
- ser compatible con Java y el `minSdk`.

Se evaluará ML Kit Text Recognition con modelo incluido u otra alternativa mantenida.

La primera versión mostrará resultados en una lista de ubicaciones.

La superposición de ubicaciones directamente sobre la fotografía quedará como mejora posterior, porque requiere:

- conservar coordenadas;
- transformar escala y rotación;
- corregir perspectiva;
- evitar solapamientos;
- asociar cada línea con su referencia.

---

## 13. Revisión obligatoria

El OCR no deberá consultar Room directamente sin intervención del usuario.

Flujo obligatorio:

```text
imagen
    ↓
OCR
    ↓
referencias propuestas
    ↓
revisión y corrección
    ↓
confirmación
    ↓
consulta de ubicaciones
```

La revisión deberá poder resolver confusiones como:

```text
0 ↔ O
1 ↔ I
5 ↔ S
8 ↔ B
```

---

## 14. Duplicados dentro de una lista

Si una referencia aparece más de una vez:

```text
MR1210A
MR1210A
```

v1.2 la mostrará una sola vez en el resultado de ubicaciones.

La aplicación no sumará ni interpretará cantidades.

La deduplicación se realizará por:

```text
categoría normalizada + código normalizado
```

---

## 15. Privacidad

La versión deberá:

- procesar imágenes localmente;
- no enviar fotografías;
- no guardar fotogramas;
- no conservar imágenes después del procesamiento por defecto;
- no registrar contenido completo en logs de producción;
- no ejecutar contenido QR;
- no abrir URLs automáticamente;
- solicitar permisos solo cuando sean necesarios.

La selección de imágenes deberá utilizar permisos modernos y limitar el acceso a los elementos elegidos por el usuario.

---

## 16. Pruebas principales

### Escaneo individual

- formatos admitidos;
- resultado único;
- ceros iniciales;
- cancelación;
- permisos;
- funcionamiento offline.

### Referencias

- `MR1210`;
- `MR 1210`;
- `MR1210A`;
- `MR 1210 A`;
- prefijo de una letra;
- prefijo de tres letras;
- parte numérica ausente;
- letra final;
- deduplicación;
- espacios.

### OCR

- lista impresa;
- captura de pantalla;
- fotografía;
- imagen rotada;
- baja iluminación;
- texto manual claro;
- línea no reconocida;
- varios códigos;
- hasta aproximadamente 15 referencias.

### Room

- todas encontradas;
- algunas no encontradas;
- orden conservado;
- búsqueda por categoría + código;
- mismo número en categorías distintas.

---

## 17. Riesgos

### Precisión del OCR

**Mitigación:** revisión obligatoria y edición manual.

### Escritura manuscrita

**Mitigación:** soporte condicionado a legibilidad; no prometer precisión absoluta.

### Falsos positivos

**Mitigación:** patrón estricto de dos letras, parte numérica y letra final opcional.

### Códigos omitidos

**Mitigación:** adición manual antes de confirmar.

### Imágenes grandes

**Mitigación:** decodificación y escalado controlados sin bloquear la UI.

### Pérdida de privacidad

**Mitigación:** procesamiento local y eliminación de temporales.

### Crecimiento excesivo de la versión

**Mitigación:** el historial persistente se traslada a v1.3.

---

## 18. Fuera del objetivo de v1.2

Aunque una lista contenga información adicional, v1.2 no persistirá:

```text
title
quantity
unit
sizes
customer
```

Tampoco modificará:

```text
stock
warehouse item quantity
```

La futura v1.3 podrá conservar título, cantidad, unidad y ubicación como snapshot histórico, sin descontar stock.

---

## 19. Definición de terminado

La versión 1.2 estará terminada cuando:

- el escáner individual funcione;
- la búsqueda individual funcione;
- alta y edición integren escaneo;
- permisos y errores estén completos;
- se pueda tomar una foto de una lista;
- se pueda seleccionar una imagen;
- las capturas puedan procesarse;
- el OCR extraiga referencias propuestas;
- la regla de dos letras se aplique;
- la letra final opcional se conserve;
- el usuario pueda revisar y corregir;
- los duplicados se consoliden;
- Room se consulte por categoría + código;
- se muestren sitio y posición;
- se identifiquen referencias no encontradas;
- no se gestionen cantidades ni stock;
- no se cree historial persistente;
- el procesamiento funcione offline;
- las imágenes no se envíen;
- pruebas, lint y build sean satisfactorios;
- CI sea satisfactoria;
- se publique `v1.2.0`.

---

## 20. Resultado esperado

Al cerrar v1.2.0:

```text
Escaneo individual
        ↓
localización rápida

Fotografía o imagen de una lista
        ↓
OCR
        ↓
revisión
        ↓
lista de referencias con ubicaciones
```

La siguiente versión será:

```text
v1.3.0 — Registro e historial de mercancía sacada
```
