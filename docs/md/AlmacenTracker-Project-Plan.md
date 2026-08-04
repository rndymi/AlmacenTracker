# AlmacenTracker — Plan general del proyecto

> Aplicación Android para registrar, consultar y localizar mercadería dentro de un almacén.

---

## 1. Identificación del proyecto

**Nombre principal:** AlmacenTracker  
**Nombre alternativo en inglés:** WarehouseLocator  
**Nombre recomendado del repositorio:** `almacen-tracker`

AlmacenTracker es un proyecto independiente inspirado en una necesidad real observada en un entorno de almacén. No representa una aplicación oficial de ninguna empresa ni utilizará datos, nombres, logotipos o información privada de terceros.

---

## 2. Descripción general

AlmacenTracker es una aplicación Android desarrollada con Java para gestionar la ubicación física de mercadería dentro de un almacén.

La aplicación permitirá registrar elementos mediante una categoría, un código y una ubicación formada por un sitio y, cuando corresponda, una posición. Posteriormente, los usuarios podrán consultar, buscar, modificar y eliminar los registros.

El proyecto comenzará como una aplicación local y evolucionará progresivamente hacia un sistema con importación y exportación de datos, lectura de códigos, trazabilidad y sincronización remota.

---

## 3. Problema que resuelve

En almacenes pequeños o con procesos parcialmente manuales, la ubicación de la mercadería puede registrarse en hojas, formularios o archivos compartidos. Esto puede provocar:

- búsquedas lentas;
- datos incompletos;
- combinaciones duplicadas;
- dificultad para conocer la ubicación actual;
- dependencia de anotaciones manuales;
- ausencia de trazabilidad;
- errores cuando varias personas trabajan con la misma información.

AlmacenTracker centraliza estos datos y facilita su consulta desde un dispositivo Android.

---

## 4. Objetivo general

Desarrollar una aplicación Android que permita registrar, consultar, modificar y eliminar ubicaciones de mercadería, aplicando una arquitectura organizada y preparada para evolucionar hacia un funcionamiento offline-first con sincronización remota.

---

## 5. Objetivos específicos

- Crear una aplicación Android funcional, estable y demostrable.
- Implementar un CRUD completo de registros de almacén.
- Permitir búsquedas por categoría, código, sitio y posición.
- Aplicar filtros sobre la información almacenada.
- Persistir los datos localmente mediante Room sobre SQLite.
- Validar los campos obligatorios.
- Evitar duplicados mediante una restricción compuesta.
- Separar la interfaz, la lógica de aplicación y la persistencia.
- Incorporar pruebas automatizadas desde las primeras versiones.
- Permitir localizar mercadería mediante escaneo individual de códigos.
- Permitir reconocer varias referencias desde fotografías, imágenes o capturas de listas.
- Permitir corregir manualmente la orientación de una imagen antes de ejecutar el OCR.
- Permitir reconstruir listas con una, dos o varias columnas cuando exista evidencia espacial suficiente.
- Preparar reglas documentales para referencias especiales, cantidades abreviadas y destinos de reparto.
- Preparar un historial documental de mercadería sacada sin gestionar ni descontar stock.
- Configurar integración continua desde el inicio.
- Configurar entrega continua de artefactos desde `master`.
- Aplicar una estrategia de ramas basada en `develop`, ramas por historia y `master` para producción.
- Documentar cada versión y cada historia de usuario.
- Preparar la aplicación para futuras capacidades de sincronización.

---

## 6. Conceptos principales del dominio

### 6.1. Elemento de almacén

Cada registro representa una mercadería o referencia localizada físicamente dentro del almacén.

Un elemento se identifica funcionalmente mediante la combinación:

```text
categoría + código
```

El código puede repetirse siempre que pertenezca a una categoría diferente.

Ejemplo válido:

| Categoría | Código |
|---|---|
| MR | 1050 |
| MD | 1050 |

Ejemplo no válido:

| Categoría | Código |
|---|---|
| MR | 1050 |
| MR | 1050 |

### 6.2. Sitio

El sitio representa el área general del almacén.

Ejemplos:

```text
A1
A2
B1
C3
```

El sitio es obligatorio.

### 6.3. Posición

La posición representa el nivel o fila concreta dentro del sitio.

Ejemplos:

```text
Nivel 1
Nivel 2
Nivel 3
Nivel 4
```

La posición es opcional porque algunos sitios disponen de un único espacio lineal y no se dividen en varios niveles.

---

## 7. Modelo conceptual

```java
public class WarehouseItem {
    private long id;
    private String categoria;
    private String codigo;
    private String sitio;
    private String posicion;
    private String observaciones;
    private long fechaCreacion;
    private long fechaActualizacion;
}
```

### Significado de los campos

| Campo | Tipo conceptual | Obligatorio | Descripción |
|---|---|---:|---|
| id | Long | Automático | Identificador interno generado por la aplicación |
| categoria | String | Sí | Categoría que diferencia códigos iguales |
| codigo | String | Sí | Código de la mercadería |
| sitio | String | Sí | Área general del almacén, por ejemplo A1 |
| posicion | String | No | Nivel o fila dentro del sitio |
| observaciones | String | No | Información adicional |
| fechaCreacion | Long | Automático | Fecha de creación del registro |
| fechaActualizacion | Long | Automático | Fecha de la última modificación |

---

## 8. Reglas generales de negocio

- La categoría es obligatoria.
- El código es obligatorio.
- El sitio es obligatorio.
- La posición es opcional.
- Las observaciones son opcionales.
- El código puede repetirse en categorías diferentes.
- La combinación categoría + código debe ser única.
- Los datos deben normalizarse antes de almacenarse.
- Las búsquedas no distinguirán mayúsculas y minúsculas.
- Las búsquedas admitirán coincidencias parciales.
- Cada modificación actualizará la fecha de actualización.
- La eliminación requerirá confirmación.
- No se utilizarán datos reales de ninguna empresa.

### Normalización inicial

- Eliminar espacios al inicio y al final.
- Convertir categoría y código a mayúsculas.
- Impedir que un campo obligatorio contenga únicamente espacios.
- Mantener sitio y posición en un formato coherente para su presentación.

---

## 9. Arquitectura evolutiva

AlmacenTracker se planteará inicialmente mediante una **arquitectura hexagonal pragmática**.

La intención será mantener una separación clara entre:

- dominio;
- casos de uso;
- puertos de entrada;
- puertos de salida;
- adaptadores de interfaz;
- adaptadores de persistencia;
- configuración de dependencias.

La estructura inicial prevista será:

```text
<package-root>/
├── domain/
│   └── model/
├── application/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── service/
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       ├── adapter/
│   │       └── state/
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               ├── database/
│               ├── entity/
│               ├── mapper/
│               └── repository/
└── configuration/
```

### 9.1. Responsabilidades iniciales

- `domain`: modelos y reglas independientes de Android y Room.
- `application.port.in`: operaciones ofrecidas por la aplicación.
- `application.port.out`: contratos requeridos por persistencia u otros servicios.
- `application.service`: implementación de casos de uso.
- `adapter.in`: Activities, adapters de RecyclerView y componentes de entrada.
- `adapter.out`: implementaciones técnicas, inicialmente Room.
- `configuration`: creación y conexión explícita de dependencias.

### 9.2. Regla de dependencias

```text
adapter.in
    ↓
application.port.in
    ↓
application.service
    ↓
application.port.out
    ↑
adapter.out
```

El dominio no deberá importar clases de Android, Room, Activities o componentes de infraestructura.

Los casos de uso se crearán únicamente cuando representen operaciones reales.

No se introducirán interfaces, servicios, métodos, pruebas o clases adicionales únicamente para mantener una estructura hexagonal formal.

### 9.3. Evaluación de un refactor arquitectónico

La arquitectura inicial deberá revisarse cuando el crecimiento del proyecto provoque alguno de estos problemas:

- proliferación de archivos con responsabilidades mínimas;
- interfaces con una única implementación sin valor de sustitución;
- casos de uso que solo delegan una llamada;
- métodos creados únicamente para atravesar capas;
- pruebas repetidas sobre delegaciones triviales;
- dificultad para localizar todos los componentes de una funcionalidad;
- exceso de separación horizontal;
- aumento de complejidad al incorporar cámara, escáner, OCR u otros componentes Android;
- coste de mantenimiento superior al desacoplamiento obtenido.

Si estos síntomas aparecen y la arquitectura deja de ser adecuada para el tamaño y naturaleza del proyecto, se podrá realizar un refactor hacia una **arquitectura MVVM pragmática organizada por funcionalidades**.

Este cambio no deberá considerarse obligatorio desde el inicio ni vincularse de antemano a una versión concreta.

La decisión deberá basarse en:

- complejidad real;
- cantidad de funcionalidades;
- volumen de clases;
- facilidad de navegación;
- mantenibilidad;
- capacidad de prueba;
- adecuación al ecosistema Android.

### 9.4. Estructura posible después del refactor

Si se aprueba el cambio hacia MVVM pragmática, la organización podrá evolucionar hacia:

```text
com.rndymi.almacentracker/
├── app/
│   ├── AlmacenTrackerApplication
│   ├── AppContainer
│   └── di/
│
├── core/
│   ├── common/
│   ├── csv/
│   ├── document/
│   └── scanner/
│
├── domain/
│   ├── model/
│   ├── reference/
│   ├── repository/
│   └── rule/
│
├── data/
│   ├── document/
│   ├── local/
│   │   └── room/
│   └── scanner/
│
└── feature/
    ├── inventory/
    │   ├── detail/
    │   ├── form/
    │   └── list/
    ├── data_management/
    ├── scanner/
    └── reference_list/
        ├── capture/
        ├── review/
        └── location/
```

### 9.5. Responsabilidades en MVVM pragmática

- `app`: configuración global y composición explícita de dependencias.
- `core`: contratos y modelos técnicos reutilizables.
- `domain`: modelos, reglas, normalización, validaciones y contratos de repositorio.
- `data`: Room, archivos, escáner, OCR y demás implementaciones técnicas.
- `feature`: Activities, ViewModels, estados, adapters y servicios asociados a cada flujo funcional.

La dirección preferente de dependencias será:

```text
Activity
    ↓
ViewModel
    ↓
servicio o repositorio
    ↓
contrato de dominio
    ↑
implementación técnica en data
```

La UI no deberá conocer DAO, entidades Room, consultas SQL, clases internas de ML Kit ni futuros modelos ONNX.

### 9.6. Conservación de límites útiles

Un eventual refactor hacia MVVM no deberá eliminar las separaciones que sigan aportando valor.

Podrán mantenerse o recuperarse puertos y adaptadores en límites concretos cuando existan varias implementaciones reales.

Ejemplo:

```text
DocumentTextRecognizer
        ├── MlKitDocumentTextRecognizer
        └── OnnxPaddleDocumentTextRecognizer
```

También podrá justificarse un puerto explícito cuando se incorporen:

- Room y una fuente remota;
- sincronización offline-first;
- varios motores OCR;
- varios proveedores de almacenamiento;
- servicios externos sustituibles.

La arquitectura resultante deberá priorizar la claridad y la mantenibilidad, no el cumplimiento rígido de una etiqueta arquitectónica.

## 10. Tecnologías generales

### Aplicación Android

- Java
- Android SDK
- Android Views
- RecyclerView
- Material Components
- ViewModel
- LiveData
- Repository Pattern
- Room
- SQLite
- Gradle

### Calidad y colaboración

- Git
- GitHub
- GitHub Actions
- JUnit
- AndroidX Test
- Espresso cuando sea necesario
- Markdown para documentación

### Evolución remota prevista

La evolución preferente será:

```text
Android Java
    ↓
API REST
    ↓
Spring Boot
    ↓
PostgreSQL o MySQL
```

Firebase o Supabase podrán evaluarse como alternativas, pero el backend propio ofrece mayor valor arquitectónico para portfolio.

---

## 11. Estrategia de persistencia y sincronización

### Fase local

Las primeras versiones utilizarán Room como única fuente de datos.

La aplicación podrá funcionar completamente sin conexión porque todas las operaciones se realizarán sobre la base de datos local.

### Fase offline-first

Cuando se incorpore una fuente remota:

1. Room conservará los datos locales.
2. Las operaciones se guardarán primero en el dispositivo.
3. Los cambios se sincronizarán cuando exista conexión.
4. La aplicación seguirá funcionando sin Internet.
5. Se definirán reglas de resolución de conflictos.

Room no será eliminado al incorporar conectividad.

---

## 12. Roadmap de versiones

| Versión | Objetivo principal |
|---|---|
| 1.0 | Gestión local mediante CRUD, Room, búsqueda, filtros y validaciones |
| 1.1 | Importación, exportación, copia de seguridad y compartición de archivos CSV |
| 1.2 | Escaneo individual, OCR y reconocimiento de listas para localizar mercadería |
| 1.3 | Registro e historial de mercadería sacada a partir de listas procesadas |
| 1.4 | Evolución del OCR local con orientación manual, varias columnas, referencias especiales, evaluación y optimización |
| 1.5 | Historial de ubicaciones, estados y categorías configurables |
| 1.6 | Procesamiento de listas de varias páginas y documentos más complejos |
| 2.0 | Autenticación y sincronización remota offline-first |
| 2.1 | Usuarios, roles, auditoría, actividad, estadísticas y varios almacenes |

Cada versión tendrá su propio documento de alcance y sus propias historias de usuario.

### 12.1. Separación entre localización e historial de sacado

La versión 1.2 permitirá reconocer referencias individuales o incluidas en listas y mostrar la ubicación de cada mercadería.

Las fuentes de entrada previstas serán:

- códigos de barras;
- códigos QR;
- fotografías tomadas desde la aplicación;
- imágenes seleccionadas desde el dispositivo;
- capturas de pantalla;
- listas impresas;
- listas manuscritas cuando la legibilidad permita un reconocimiento suficiente;
- imágenes recibidas con una orientación incorrecta, que podrán corregirse antes del procesamiento;
- documentos con una, dos o varias columnas cuando su estructura pueda reconstruirse de forma estable.

La versión 1.3 utilizará listas revisadas y confirmadas para registrar un historial de mercadería sacada.

Ese historial podrá conservar:

- título opcional;
- fecha y hora;
- categoría y código;
- cantidad y unidad cuando estén disponibles;
- ubicación existente en el momento de procesar la lista.

El historial tendrá carácter documental. No reducirá stock ni transformará AlmacenTracker en un sistema de inventario cuantitativo.

### 12.2. Regla principal del producto

La prioridad funcional de AlmacenTracker será:

```text
identificar una mercadería
        ↓
mostrar dónde se encuentra
```

Las cantidades, piezas, paquetes, cajas, tallas, clientes o títulos podrán formar parte de una lista, pero no modificarán el registro principal de mercadería ni su ubicación.

### 12.3. Formato evolutivo de referencias

Las referencias reconocibles desde una lista deberán mantener como identidad funcional:

```text
categoría + código
```

La categoría prevista continuará formada por letras y deberá validarse de acuerdo con las reglas vigentes del inventario.

El código podrá incluir:

- una parte numérica principal;
- un calificador alfabético opcional;
- una extensión documental separada por guion cuando la referencia exista de esa forma en Room.

Ejemplos conceptuales:

```text
MR 21570
MA 900 A
ML 4170 BLACK
M873-1
M873-12
```

La aplicación no deberá codificar reglas particulares para una categoría concreta ni asumir que todos los códigos especiales pertenecen a una única familia.

Cuando una línea contenga una estructura ambigua, Room deberá actuar como fuente de verdad. La estrategia preferida será conservar la referencia conocida más larga que coincida al inicio de la línea y dejar el resto como datos documentales.

Ejemplo:

```text
M873-1 - 1P - ①②
```

Interpretación prevista:

```text
reference = M873-1
quantity = 1
unit = P
destinations = ①, ②
```

Si la referencia no existe en Room y la separación no es inequívoca, la aplicación deberá conservarla para revisión manual en lugar de inventar una identidad.

### 12.4. Orientación, columnas y destinos documentales

La evolución OCR de v1.4 deberá contemplar progresivamente:

- corrección automática de la orientación EXIF cuando esté disponible;
- controles manuales para girar la imagen en pasos de 90 grados;
- reconstrucción de una, dos o varias columnas;
- lectura por columnas completas cuando el documento lo requiera;
- títulos opcionales en alfabetos compatibles con el modelo y el diccionario;
- unidades abreviadas como `P`, sin obligar a convertirlas silenciosamente en otra unidad;
- indicadores de destino como `①`, `②`, `③` o equivalentes;
- varios destinos asociados a una misma referencia cuando el documento lo exprese.

Los destinos tendrán carácter documental. No deberán modificar la identidad de la mercadería, su ubicación ni el stock.


## 13. Estrategia documental

La documentación se dividirá en tres niveles.

### 13.1. Documento general

Archivo recomendado:

```text
docs/project/project-plan.md
```

Describe:

- propósito del proyecto;
- problema;
- dominio;
- reglas generales;
- arquitectura evolutiva;
- tecnologías;
- roadmap;
- riesgos.

### 13.2. Documento por versión

Ejemplo:

```text
docs/versions/v1.0/version-plan.md
```

Describe:

- objetivo de la versión;
- alcance incluido y excluido;
- historias de usuario;
- modelo aplicable;
- backlog;
- pruebas;
- definición de terminado.

### 13.3. Documento por historia de usuario

Ejemplo:

```text
docs/versions/v1.0/user-stories/HU-01-consultar-mercaderia.md
```

Describe:

- necesidad;
- alcance;
- criterios de aceptación;
- flujo;
- estados;
- cambios técnicos;
- pruebas;
- definición de terminado.

Las historias posteriores se documentarán cuando la historia actual haya sido implementada y cerrada.

---

## 14. Estrategia de calidad

El proyecto incorporará calidad desde el inicio.

### Pruebas unitarias

- validaciones;
- normalización;
- reglas de campos obligatorios;
- comprobación de combinaciones duplicadas;
- filtros y búsquedas.

### Pruebas instrumentadas

- inserción en Room;
- consulta;
- actualización;
- eliminación;
- persistencia;
- restricción única compuesta.

### Pruebas manuales

- rotación de pantalla;
- lista vacía;
- búsqueda sin resultados;
- datos largos;
- campos opcionales;
- funcionamiento sin conexión;
- confirmaciones y errores.

### Integración continua

La CI deberá ejecutar, como mínimo:

```text
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

---

## 15. Estrategia de repositorio, ramas y automatización

### 15.1. Repositorio

Estructura prevista del repositorio público:

```text
almacen-tracker/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── cd-android-artifact.yml
├── app/
├── build.gradle
├── settings.gradle
├── gradle/
├── gradlew
├── gradlew.bat
├── .gitignore
├── LICENSE
└── README.md
```

Los documentos internos de planificación se mantendrán en “Fuentes” y no se versionarán en GitHub. La política del repositorio ignorará cualquier archivo Markdown salvo el `README.md` ubicado en la raíz.

El proyecto tendrá un único propietario y desarrollador. No dependerá de contribuciones externas ni utilizará Pull Requests como requisito del flujo inicial. Si GitHub recibe una propuesta externa, el propietario decidirá si la ignora o la cierra.

### 15.2. Estrategia de ramas

```text
feature/hu-XX-descripcion
          ↓ merge local
       develop
          ↓
   release/vX.Y.Z
          ↓ merge local
        master
          ↓
       tag vX.Y.Z
```

- `develop`: rama principal de desarrollo e integración.
- `feature/hu-XX-descripcion`: rama creada desde `develop` para implementar una historia de usuario completa.
- `release/vX.Y.Z`: rama temporal creada desde `develop` para estabilizar una versión, ajustar su número, completar evidencias y corregir defectos antes de producción.
- `master`: rama que representa la versión estable de producción.
- `hotfix/<descripcion>`: rama excepcional creada desde `master` para corregir un fallo urgente ya presente en producción; después deberá integrarse también en `develop`.
- `vX.Y.Z`: tag anotado e inmutable que identifica el commit exacto publicado.

No se realizará desarrollo funcional directamente sobre `master` ni sobre `develop`. Los cambios funcionales se realizarán en ramas `feature/*`. Los Pull Requests serán opcionales y no formarán parte del flujo habitual mientras el proyecto continúe siendo individual.

Cada historia se integrará una sola vez en `develop`, después de completar sus criterios, ejecutar pruebas y comprobar una CI satisfactoria.

### 15.3. Reglas de integración

Antes de fusionar localmente una rama:

- el árbol de trabajo deberá estar limpio;
- los commits deberán ser representativos y estar relacionados con la historia;
- la rama deberá estar actualizada cuando existan cambios relevantes en `develop`;
- la CI de la rama deberá finalizar correctamente;
- no se utilizará `force push` sobre `develop` ni `master`;
- no se fusionará una historia incompleta para continuarla posteriormente desde la misma rama.

Las ramas integradas podrán eliminarse localmente y en remoto una vez verificado el resultado.

### 15.4. Integración continua

El workflow `.github/workflows/ci.yml` se ejecutará en pushes a:

- `master`;
- `develop`;
- `feature/**`;
- `release/**`;
- `hotfix/**`.

Validaciones mínimas:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Los reportes de pruebas y lint se publicarán como artefactos incluso cuando falle una validación, siempre que hayan sido generados.

La CI no realizará el merge automáticamente. El propietario revisará el resultado y solo integrará localmente las ramas con validaciones satisfactorias.

### 15.5. Entrega continua

El workflow `.github/workflows/cd-android-artifact.yml` se ejecutará cuando un cambio llegue a `master` y también permitirá ejecución manual mediante `workflow_dispatch`.

En la primera etapa:

1. repetirá pruebas unitarias y lint;
2. compilará el APK de depuración;
3. publicará el APK como artefacto de GitHub Actions.

Este artefacto será demostrable, pero no debe confundirse con una release Android firmada para distribución. La firma de producción se incorporará posteriormente mediante secretos de GitHub y nunca mediante archivos de claves versionados.

Cuando exista una release Android firmada, el CD podrá evolucionar para ejecutarse mediante tags `v*.*.*`, generar `assembleRelease` y publicar una GitHub Release.

### 15.6. Versiones, releases y tags

Al completar todas las historias obligatorias de una versión:

1. se creará `release/vX.Y.Z` desde `develop`;
2. se ajustarán `versionCode` y `versionName`;
3. se realizarán únicamente correcciones y tareas de estabilización;
4. la release validada se fusionará localmente en `master`;
5. se creará el tag anotado `vX.Y.Z` sobre el commit publicado;
6. las correcciones realizadas en la release se reintegrarán en `develop`;
7. la rama release podrá eliminarse.

No se creará una rama release por cada historia de usuario.

### 15.7. Política previa al primer push

Antes de publicar cualquier commit se verificará:

```text
git status --ignored
git check-ignore -v local.properties
git check-ignore -v AGENTS.md
git check-ignore -v AlmacenTracker-Project-Plan.md
```

También se comprobará que no haya APK, claves, configuración local, carpetas de compilación ni documentos Markdown internos en staging.

## 16. Riesgos y límites

- No utilizar datos reales de ninguna empresa.
- No usar nombres, logotipos o información privada de terceros.
- No presentar el proyecto como una aplicación oficial.
- No inventar métricas de mejora.
- No añadir funcionalidades futuras antes de cerrar la versión actual.
- Evitar una arquitectura excesivamente compleja.
- Mantener una terminología consistente.
- No introducir sincronización hasta que la versión local sea estable.
- Documentar las decisiones importantes antes de implementarlas.
- No evaluar la precisión de un documento antes de corregir su orientación visible.
- Evitar reglas rígidas para una cantidad fija de columnas.
- No hardcodear familias especiales de códigos que puedan resolverse mediante Room.
- Separar los destinos documentales de la identidad y la ubicación de la mercadería.

---

## 17. Presentación en GitHub

### Descripción corta

```text
Android app for registering, searching and tracking warehouse item locations. Built with Java, Room and an offline-first evolution roadmap.
```

### Topics recomendados

```text
android
java
room
sqlite
mvvm
inventory
warehouse
crud
offline-first
```

---

## 18. Presentación en el CV

### Primera etapa

```text
AlmacenTracker | Aplicación Android de localización de mercadería

Aplicación móvil desarrollada en Java para registrar, buscar y actualizar la ubicación de mercadería mediante un CRUD con persistencia local, validación de datos y arquitectura organizada.
```

### Con sincronización

```text
Aplicación Android offline-first para gestionar ubicaciones de mercadería, con persistencia local mediante Room y sincronización con un servicio remoto.
```

---

## 19. Visión final

AlmacenTracker será una aplicación pequeña pero completa, construida progresivamente a partir de una necesidad real.

La prioridad será entregar versiones estables, demostrables y fáciles de explicar. La evolución del proyecto se realizará de forma incremental, cerrando cada historia de usuario antes de documentar y desarrollar la siguiente.
