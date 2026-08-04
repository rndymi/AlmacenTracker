# AlmacenTracker — Plan de la versión 1.0

> Primera versión profesional: gestión local de mercancía mediante Android, Java y Room.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Nombre de la versión:** CRUD local  
**Estado inicial:** Planificada

---

## 2. Objetivo de la versión

Desarrollar una aplicación Android local que permita consultar, registrar, visualizar, buscar, filtrar, editar y eliminar mercancía almacenada en el dispositivo.

La versión 1.0 debe ser estable, demostrable y completamente funcional sin conexión a Internet.

---

## 3. Alcance incluido

La versión 1.0 incluirá:

- listado principal de mercancía;
- alta de registros;
- vista de detalle;
- edición;
- eliminación individual;
- búsqueda parcial;
- filtros por categoría, sitio y posición;
- validación visual de campos;
- normalización de datos;
- restricción única para categoría + código;
- persistencia mediante Room;
- estados de interfaz;
- confirmaciones y mensajes de error;
- funcionamiento sin conexión;
- pruebas automatizadas básicas;
- integración continua;
- README y capturas;
- release ejecutable.

La eliminación múltiple se considera una mejora de prioridad media. Se implementará al final de la versión únicamente si el núcleo obligatorio ya se encuentra estable.

---

## 4. Alcance excluido

La versión 1.0 no incluirá:

- autenticación;
- usuarios y roles;
- sincronización remota;
- backend;
- Firebase o Supabase;
- importación o exportación CSV;
- copias de seguridad externas;
- escaneo de códigos de barras;
- escaneo de códigos QR;
- historial de cambios de ubicación;
- varios almacenes;
- estadísticas;
- auditoría;
- resolución de conflictos;
- cantidades o gestión de stock.

---

## 5. Definición del registro

Cada registro representa una mercancía o referencia localizada dentro del almacén.

El identificador interno será generado automáticamente por Room.

La identidad funcional será:

```text
categoría + código
```

El código puede repetirse en categorías distintas.

Ejemplo permitido:

```text
MR + 1050
MD + 1050
```

Ejemplo no permitido:

```text
MR + 1050
MR + 1050
```

---

## 6. Modelo de datos de la versión

| Campo | Tipo Room previsto | Obligatorio | Descripción |
|---|---|---:|---|
| id | long | Automático | Clave primaria autogenerada |
| categoria | String | Sí | Categoría que diferencia códigos iguales |
| codigo | String | Sí | Código de la mercancía |
| sitio | String | Sí | Área general, por ejemplo A1 |
| posicion | String | No | Nivel o fila dentro del sitio |
| observaciones | String | No | Información adicional |
| fechaCreacion | long | Automático | Marca temporal de creación |
| fechaActualizacion | long | Automático | Marca temporal de última modificación |

### Restricción compuesta prevista

```java
@Entity(
    tableName = "warehouse_items",
    indices = {
        @Index(
            value = {"categoria", "codigo"},
            unique = true
        )
    }
)
```

---

## 7. Reglas de negocio

- Categoría obligatoria.
- Código obligatorio.
- Sitio obligatorio.
- Posición opcional.
- Observaciones opcionales.
- El código puede repetirse en categorías diferentes.
- Categoría + código deben formar una combinación única.
- Categoría y código se guardarán normalizados.
- Los campos no podrán contener únicamente espacios.
- Las búsquedas no distinguirán mayúsculas y minúsculas.
- Las búsquedas admitirán coincidencias parciales.
- La fecha de creación se asignará al insertar.
- La fecha de actualización se renovará al editar.
- Toda eliminación individual requerirá confirmación.
- La aplicación debe mantener sus operaciones principales sin conexión.

### Definición de ubicación

```text
Sitio:
área general del almacén, por ejemplo A1.

Posición:
nivel o fila dentro del sitio, por ejemplo Nivel 2.
Es opcional cuando el sitio solo tiene un espacio lineal.
```

---

## 8. Pantallas

### 8.1. Listado principal

Debe mostrar:

- listado de registros;
- categoría;
- código;
- sitio;
- posición cuando exista;
- estado vacío;
- estado sin resultados;
- acceso al formulario de alta;
- acceso al detalle;
- barra de búsqueda;
- filtros.

### 8.2. Formulario de alta y edición

Campos:

- categoría;
- código;
- sitio;
- posición;
- observaciones.

Acciones:

- guardar;
- cancelar.

Debe mostrar errores visuales junto a los campos correspondientes.

### 8.3. Detalle

Debe mostrar:

- categoría;
- código;
- sitio;
- posición;
- observaciones;
- fecha de creación;
- fecha de actualización;
- acción editar;
- acción eliminar.

---

## 9. Navegación propuesta

La primera versión utilizará una navegación sencilla basada en Activities.

```text
MainActivity
├── listado principal
├── ItemFormActivity
└── ItemDetailActivity
```

`ItemFormActivity` podrá reutilizarse tanto para crear como para editar mediante un identificador opcional.

---

## 10. Arquitectura aplicada

La versión 1.0 utilizará arquitectura hexagonal pragmática, con dependencias dirigidas hacia el dominio y la aplicación.

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
│   │       ├── state/
│   │       └── viewmodel/
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

### Flujo general

```text
Activity / ViewModel
        ↓
application.port.in
        ↓
application.service
        ↓
application.port.out
        ↑
Room repository adapter
        ↓
DAO / Room
```

El modelo de dominio será Java puro. Las entidades Room permanecerán dentro del adaptador de salida y se convertirán mediante mappers.

No se crearán capas sin responsabilidad real. Cada puerto de entrada representará una operación de aplicación y cada puerto de salida una dependencia externa concreta.

## 11. Estados de interfaz

El listado debe diferenciar, como mínimo:

### Carga inicial

La aplicación está obteniendo los registros locales.

### Contenido

Existen registros y se muestran correctamente.

### Lista vacía

No existe mercancía registrada.

Mensaje orientativo:

```text
Todavía no hay mercancía registrada.
```

### Sin resultados

Existen registros, pero ninguno coincide con la búsqueda o filtros.

Mensaje orientativo:

```text
No se encontraron resultados.
```

### Error

No fue posible recuperar o procesar los datos.

La interfaz no debe bloquearse durante operaciones de persistencia.

---

# 12. Historias de usuario de la versión 1.0

La versión 1.0 se divide en las siguientes historias de usuario.

Cada historia tendrá posteriormente su propio documento individual. Inicialmente solo se desarrolla y documenta en detalle la HU-01. Las historias siguientes se documentarán al finalizar la historia anterior.

---

## HU-01 — Consultar listado de mercancía

### Historia

Como usuario, quiero visualizar los registros de mercancía almacenados para conocer rápidamente su categoría, código y ubicación actual.

### Objetivo

Construir la pantalla inicial y el flujo vertical de consulta:

```text
Room
  ↓
DAO
  ↓
Repository
  ↓
ViewModel
  ↓
MainActivity
  ↓
RecyclerView
```

### Alcance principal

- consultar todos los registros;
- mostrar categoría, código, sitio y posición;
- ordenar por categoría y código;
- mostrar carga, contenido, vacío y error;
- actualizar el listado mediante datos observables;
- conservar el estado esencial ante rotación.

### Criterios de cierre resumidos

- la aplicación inicia en el listado;
- los registros se recuperan desde Room;
- la posición se oculta cuando no existe;
- la lista vacía no se trata como error;
- el listado se actualiza automáticamente;
- la interfaz no accede directamente al DAO.

**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Documento individual:** `user-stories/HU-01-consultar-mercancia.md`

---

## HU-02 — Registrar mercancía

### Historia

Como usuario, quiero registrar una mercancía indicando su categoría, código y ubicación para poder localizarla posteriormente.

### Alcance principal

- abrir el formulario desde el listado;
- introducir categoría, código y sitio;
- introducir posición y observaciones opcionales;
- validar campos obligatorios;
- normalizar los datos;
- guardar el registro;
- regresar al listado;
- mostrar confirmación o error.

### Criterios de aceptación resumidos

- un formulario válido crea el registro;
- un campo obligatorio vacío impide guardar;
- los espacios externos se eliminan;
- posición y observaciones pueden quedar vacías;
- el nuevo registro aparece en el listado;
- los errores se muestran junto al campo correspondiente.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-01.

---

## HU-03 — Consultar detalle de mercancía

### Historia

Como usuario, quiero consultar toda la información de una mercancía para conocer su ubicación y sus datos completos.

### Alcance principal

- abrir un registro desde el listado;
- recuperar el elemento por su id interno;
- mostrar categoría, código, sitio y posición;
- mostrar observaciones y fechas;
- mostrar accesos para editar y eliminar;
- controlar el caso de registro inexistente.

### Criterios de aceptación resumidos

- al pulsar un elemento se abre el registro correcto;
- todos sus datos se presentan correctamente;
- los campos opcionales no muestran `null`;
- si el registro deja de existir se muestra un estado controlado;
- la Activity no consulta directamente el DAO.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-02.

---

## HU-04 — Buscar mercancía

### Historia

Como usuario, quiero buscar mercancía mediante texto para encontrar rápidamente un registro concreto.

### Alcance principal

- barra de búsqueda;
- coincidencias parciales;
- búsqueda sobre categoría, código, sitio y posición;
- búsqueda sin distinguir mayúsculas;
- actualización del listado;
- estado sin resultados;
- restauración del texto tras rotación.

### Criterios de aceptación resumidos

- una coincidencia parcial devuelve registros;
- la búsqueda no distingue mayúsculas;
- una cadena vacía vuelve a mostrar todos los registros;
- una búsqueda sin coincidencias muestra un estado específico;
- la base de datos vacía se diferencia de una búsqueda sin resultados.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-03.

---

## HU-05 — Filtrar mercancía

### Historia

Como usuario, quiero filtrar la mercancía por categoría, sitio o posición para reducir el conjunto de resultados.

### Alcance principal

- filtro por categoría;
- filtro por sitio;
- filtro por posición;
- opción de limpiar filtros;
- combinación entre búsqueda y filtros;
- indicación visual de filtros activos;
- conservación ante rotación.

### Criterios de aceptación resumidos

- seleccionar un filtro reduce correctamente el listado;
- varios filtros pueden combinarse;
- búsqueda y filtros pueden aplicarse al mismo tiempo;
- limpiar filtros restaura la lista;
- los filtros activos permanecen tras un cambio de configuración.

**Prioridad:** Media  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-04.

---

## HU-06 — Editar mercancía

### Historia

Como usuario, quiero modificar los datos y la ubicación de una mercancía cuando cambien.

### Alcance principal

- abrir edición desde el detalle;
- cargar los datos actuales;
- modificar los campos;
- validar y normalizar;
- controlar duplicados;
- actualizar la fecha de modificación;
- guardar el registro correcto;
- cancelar sin cambios.

### Criterios de aceptación resumidos

- se muestran los valores actuales;
- los cambios válidos se guardan;
- cancelar no altera el registro;
- una combinación duplicada se rechaza;
- el listado y detalle reflejan los cambios;
- la fecha de actualización cambia;
- la fecha de creación permanece.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-05.

---

## HU-07 — Eliminar mercancía

### Historia

Como usuario, quiero eliminar un registro incorrecto u obsoleto para mantener la información actualizada.

### Alcance principal

- iniciar eliminación desde el detalle;
- mostrar confirmación;
- cancelar eliminación;
- eliminar el registro seleccionado;
- volver al listado;
- actualizar automáticamente la lista;
- mostrar confirmación o error.

### Criterios de aceptación resumidos

- eliminar siempre requiere confirmación;
- cancelar conserva el registro;
- confirmar elimina el elemento correcto;
- el registro desaparece del listado;
- la lista vacía aparece si era el último elemento;
- los errores no cierran la aplicación.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-06.

---

## HU-08 — Evitar combinaciones duplicadas

### Historia

Como usuario, quiero recibir un aviso cuando intento registrar una combinación de categoría y código que ya existe para evitar registros duplicados.

### Alcance principal

- restricción única en Room;
- validación previa al guardar;
- control al insertar y editar;
- mensaje comprensible;
- permitir el mismo código en categorías diferentes;
- impedir la misma categoría con el mismo código.

### Criterios de aceptación resumidos

- un código repetido en otra categoría se acepta;
- categoría + código repetidos se rechazan;
- editar un registro sin cambiar su identidad se permite;
- editarlo hacia una combinación existente se rechaza;
- Room conserva la restricción;
- el usuario recibe un mensaje comprensible.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-07.

---

## HU-09 — Mostrar estados vacíos y sin resultados

### Historia

Como usuario, quiero recibir mensajes claros cuando no haya datos o no existan coincidencias para entender el estado de la aplicación.

### Alcance principal

- estado de base de datos vacía;
- estado de búsqueda sin resultados;
- estado de filtros sin resultados;
- mensajes diferenciados;
- acciones contextuales;
- restauración del contenido al limpiar criterios.

### Criterios de aceptación resumidos

- base vacía muestra un mensaje específico;
- búsqueda sin resultados no se confunde con base vacía;
- filtros sin coincidencias muestran un mensaje apropiado;
- limpiar búsqueda o filtros restaura el contenido;
- no aparecen simultáneamente listado y estado vacío.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-08.

---

## HU-10 — Eliminar varios registros

### Historia

Como usuario, quiero seleccionar y eliminar varios registros para limpiar información obsoleta de manera eficiente.

### Alcance principal

- activar modo selección;
- seleccionar y deseleccionar elementos;
- mostrar contador;
- cancelar selección;
- solicitar confirmación;
- eliminar los seleccionados;
- actualizar la lista;
- controlar la interacción con búsqueda y filtros.

### Criterios de aceptación resumidos

- el modo selección no se activa accidentalmente;
- el contador coincide con la selección;
- cancelar no elimina registros;
- confirmar elimina únicamente los seleccionados;
- la lista se actualiza;
- el estado vacío aparece si se eliminan todos.

**Prioridad:** Media  
**Estado inicial:** No iniciada  
**Condición:** Puede aplazarse si el CRUD individual aún no está estable.  
**Documento individual:** Se generará al cerrar la HU-09.

---

## HU-11 — Utilizar la aplicación sin conexión

### Historia

Como usuario, quiero consultar y gestionar la mercancía sin conexión a Internet para poder trabajar desde cualquier zona del almacén.

### Alcance principal

- consultar sin conexión;
- registrar sin conexión;
- editar sin conexión;
- eliminar sin conexión;
- buscar y filtrar sin conexión;
- mantener persistencia al cerrar;
- no depender de servicios remotos.

### Criterios de aceptación resumidos

- todas las funciones principales operan sin Internet;
- los datos persisten tras cerrar y abrir;
- la aplicación no exige autenticación;
- ninguna operación depende de red;
- Room actúa como única fuente de datos.

**Prioridad:** Alta  
**Estado inicial:** No iniciada  
**Documento individual:** Se generará al cerrar la HU-10 o se verificará transversalmente al final.

---

## 13. Relación y orden de implementación

```text
HU-01 Consultar listado
   ↓
HU-02 Registrar
   ↓
HU-03 Consultar detalle
   ↓
HU-04 Buscar
   ↓
HU-05 Filtrar
   ↓
HU-06 Editar
   ↓
HU-07 Eliminar
   ↓
HU-08 Evitar duplicados
   ↓
HU-09 Estados vacíos y sin resultados
   ↓
HU-10 Eliminación múltiple
   ↓
HU-11 Verificación offline
```

La HU-08 tendrá validaciones parciales desde la HU-02, pero se cerrará cuando la regla se haya verificado en alta, edición y Room.

La HU-09 tendrá una implementación inicial desde la HU-01, pero se cerrará cuando contemple también búsqueda y filtros.

La HU-11 es transversal y se verificará durante toda la versión.

---

## 14. Matriz de cobertura funcional

| Funcionalidad | HU responsable |
|---|---|
| Listado principal | HU-01 |
| Estado de carga | HU-01 |
| Estado vacío inicial | HU-01 / HU-09 |
| Alta | HU-02 |
| Detalle | HU-03 |
| Búsqueda | HU-04 |
| Filtros | HU-05 |
| Edición | HU-06 |
| Eliminación individual | HU-07 |
| Duplicado categoría + código | HU-08 |
| Sin resultados | HU-09 |
| Eliminación múltiple | HU-10 |
| Funcionamiento sin conexión | HU-11 |

---

## 15. Tareas técnicas iniciales

Estas tareas no se tratarán como historias de usuario:

1. Crear proyecto Android Java.
2. Crear repositorio GitHub.
3. Configurar `.gitignore`.
4. Definir estrategia de ramas.
5. Configurar GitHub Actions.
6. Incorporar dependencias base.
7. Crear estructura de paquetes.
8. Configurar Room.
9. Crear tema y recursos visuales básicos.
10. Preparar datos ficticios de desarrollo.
11. Configurar pruebas.
12. Crear README inicial.

---

## 16. Backlog técnico de la versión

| Orden | Tarea | Prioridad |
|---:|---|---|
| 1 | Crear proyecto Android Java | Alta |
| 2 | Crear repositorio y ramas | Alta |
| 3 | Configurar README inicial, licencia y `.gitignore` | Alta |
| 4 | Configurar CI | Alta |
| 5 | Definir modelo y reglas | Alta |
| 6 | Crear estructura por capas | Alta |
| 7 | Configurar Room | Alta |
| 8 | Implementar entidad, DAO y base de datos | Alta |
| 9 | Implementar repositorio | Alta |
| 10 | Implementar validación y normalización | Alta |
| 11 | Implementar ViewModel del listado | Alta |
| 12 | Implementar RecyclerView | Alta |
| 13 | Implementar formulario de alta | Alta |
| 14 | Implementar detalle | Alta |
| 15 | Implementar búsqueda | Alta |
| 16 | Implementar filtros | Media |
| 17 | Implementar edición | Alta |
| 18 | Implementar eliminación individual | Alta |
| 19 | Completar control de duplicados | Alta |
| 20 | Completar estados visuales | Alta |
| 21 | Implementar eliminación múltiple | Media |
| 22 | Crear pruebas unitarias | Alta |
| 23 | Crear pruebas instrumentadas de Room | Media |
| 24 | Revisar rotación y ciclo de vida | Alta |
| 25 | Verificar funcionamiento offline | Alta |
| 26 | Completar README y capturas | Alta |
| 27 | Crear release 1.0 | Alta |

---

## 17. Estrategia de pruebas

### Pruebas unitarias

- categoría vacía;
- código vacío;
- sitio vacío;
- posición opcional;
- normalización;
- duplicado categoría + código;
- búsqueda parcial;
- estados de ViewModel;
- filtros combinados.

### Pruebas de Room

- insertar;
- consultar todos;
- consultar por id;
- actualizar;
- eliminar;
- eliminar varios;
- persistir;
- rechazar duplicado compuesto;
- permitir mismo código en distinta categoría.

### Pruebas manuales

- inicio con base vacía;
- inicio con registros;
- rotación;
- textos largos;
- campos opcionales;
- búsqueda sin resultados;
- filtros combinados;
- edición;
- eliminación;
- selección múltiple;
- funcionamiento sin conexión.

---

## 18. Integración, entrega y publicación de la versión

### CI

El workflow `.github/workflows/ci.yml` se ejecutará en pushes a `master`, `develop`, `feature/**`, `release/**` y `hotfix/**`.

Validaciones mínimas:

```text
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

La CI deberá finalizar correctamente antes de realizar cualquier merge local hacia `develop` o `master`. Los Pull Requests no serán obligatorios en este proyecto individual.

### CD inicial

El workflow `.github/workflows/cd-android-artifact.yml` se ejecutará en cada integración a `master` y de forma manual mediante `workflow_dispatch`.

Su objetivo inicial será:

- repetir pruebas y lint;
- compilar el APK debug;
- publicar el APK como artefacto descargable de GitHub Actions.

Este CD no constituye todavía una publicación firmada en Google Play. La firma de release se incorporará cuando existan keystore y secretos gestionados correctamente.

### Estrategia de ramas de la versión

```text
feature/hu-XX-descripcion
          ↓ merge local al cerrar la HU
       develop
          ↓ al completar la versión
   release/v1.0.0
          ↓ merge local validado
        master
          ↓
       tag v1.0.0
```

Cada historia se desarrollará en una rama creada desde `develop`. La HU-01 utilizará:

```text
feature/hu-01-consultar-mercancia
```

La rama `release/v1.0.0` se creará únicamente cuando todas las historias obligatorias de la versión estén integradas en `develop`. Durante su estabilización no se añadirán nuevas funcionalidades.

Después de fusionar la release en `master`, se creará el tag anotado `v1.0.0`. Las correcciones realizadas en la rama release deberán reintegrarse posteriormente en `develop`.

No se realizará push inicial hasta revisar el `.gitignore` y comprobar el contenido preparado para staging.

## 19. Riesgos de la versión

- Crear demasiadas capas para un CRUD pequeño.
- Mezclar historias antes de cerrar la actual.
- Implementar funciones de versiones futuras.
- Confundir sitio con posición.
- Aplicar unicidad solo al código.
- Ejecutar Room en el hilo principal.
- No mantener el estado ante rotación.
- Utilizar datos reales.
- Posponer pruebas y CI hasta el final.
- Incluir eliminación múltiple antes de estabilizar el CRUD individual.

---

## 20. Definición de terminado de la versión 1.0

La versión 1.0 estará terminada cuando:

- todas las historias obligatorias estén cerradas;
- el CRUD individual funcione;
- los datos se persistan mediante Room;
- categoría, código y sitio sean obligatorios;
- posición y observaciones sean opcionales;
- categoría + código sean únicos;
- exista búsqueda parcial;
- existan filtros básicos;
- se diferencie lista vacía de búsqueda sin resultados;
- las operaciones de datos no bloqueen la interfaz;
- la eliminación solicite confirmación;
- el estado esencial sobreviva a cambios de configuración;
- existan pruebas unitarias y de DAO;
- la CI compile, analice y ejecute pruebas;
- el repositorio pueda clonarse y ejecutarse;
- el README y las capturas estén completos;
- exista una release 1.0;
- no existan datos reales de terceros;
- se haya verificado el funcionamiento sin conexión.

La eliminación múltiple no impedirá cerrar la versión si se decide formalmente moverla a una versión posterior.

---

## 21. Entregables

- código fuente;
- APK o artefacto de release;
- documentación general;
- documento de la versión;
- documentos individuales de todas las historias cerradas;
- README;
- capturas;
- pruebas;
- workflow de CI;
- registro de decisiones relevantes.

---

## 22. Estado inicial

La versión 1.0 queda preparada para comenzar con:

```text
HU-01 — Consultar listado de mercancía
```

Las historias HU-02 a HU-11 ya quedan definidas dentro de este plan de versión. Sus documentos individuales se generarán progresivamente después de cerrar la historia anterior.
