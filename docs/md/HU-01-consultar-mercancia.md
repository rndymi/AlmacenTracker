# HU-01 — Consultar listado de mercancía

> Primera historia de usuario de AlmacenTracker v1.0.

---

## 1. Identificación

**Proyecto:** AlmacenTracker  
**Versión:** 1.0  
**Historia:** HU-01  
**Nombre:** Consultar listado de mercancía  
**Prioridad:** Alta  
**Estado inicial:** Pendiente  
**Rama de trabajo:** `feature/hu-01-consultar-mercancia`  
**Rama de integración:** `develop`

---

## 2. Historia de usuario

Como usuario,  
quiero visualizar los registros de mercancía almacenados,  
para conocer rápidamente su categoría, código y ubicación actual.

---

## 3. Objetivo

Implementar la pantalla inicial de la aplicación y el flujo necesario para recuperar y mostrar los registros existentes en la base de datos local.

Esta historia establecerá la primera integración vertical entre:

```text
Vista
  ↓
ViewModel
  ↓
Repositorio
  ↓
DAO
  ↓
Room
```

---

## 4. Alcance incluido

La HU-01 incluye:

- apertura de la aplicación;
- pantalla principal;
- consulta de todos los registros locales;
- representación de cada registro en un RecyclerView;
- visualización de categoría;
- visualización de código;
- visualización de sitio;
- visualización de posición cuando exista;
- orden estable del listado;
- estado de carga;
- estado con contenido;
- estado vacío;
- estado de error;
- actualización automática del listado cuando cambien los datos;
- conservación del estado esencial ante rotación;
- datos ficticios de desarrollo o pruebas;
- pruebas básicas del flujo de consulta.

---

## 5. Alcance excluido

La HU-01 no incluye:

- crear mercancía;
- editar mercancía;
- eliminar mercancía;
- abrir una vista de detalle funcional;
- búsqueda;
- filtros;
- selección múltiple;
- validación de formularios;
- control de duplicados;
- importación o exportación;
- sincronización remota;
- autenticación.

El botón o acceso para crear podrá aparecer visualmente si forma parte del diseño inicial, pero no deberá considerarse funcional dentro de esta historia.

---

## 6. Dependencias técnicas

Antes o durante la implementación de la HU-01 deberán existir:

- proyecto Android creado con Java;
- dependencias de Room;
- dependencias de Lifecycle;
- RecyclerView;
- Material Components;
- estructura básica de paquetes;
- entidad local;
- DAO;
- base de datos Room;
- repositorio;
- ViewModel;
- adapter;
- layout principal;
- layout de cada elemento del listado.

---

## 7. Modelo mínimo utilizado

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

Aunque la HU-01 no crea ni edita registros, utilizará el modelo completo previsto para la versión 1.0.

---

## 8. Información mostrada en cada elemento

Cada fila o tarjeta del listado deberá mostrar como mínimo:

- categoría;
- código;
- sitio;
- posición, únicamente cuando exista.

Ejemplo con posición:

```text
MR · 1050
Sitio A1 · Nivel 3
```

Ejemplo sin posición:

```text
MD · 1050
Sitio B2
```

Las observaciones y fechas no son obligatorias en el listado. Se reservarán para la futura pantalla de detalle.

---

## 9. Orden del listado

Para garantizar un resultado predecible, los registros se mostrarán inicialmente por:

1. categoría ascendente;
2. código ascendente.

Consulta conceptual:

```sql
ORDER BY categoria COLLATE NOCASE ASC,
         codigo COLLATE NOCASE ASC
```

Si durante la implementación se detecta una necesidad mejor, el cambio deberá documentarse.

---

## 10. Estados de interfaz

### 10.1. Carga

Mientras se obtiene la información local, la pantalla mostrará un indicador de carga discreto.

La carga no debe bloquear la interfaz.

### 10.2. Contenido

Cuando existan registros:

- se ocultará el estado vacío;
- se mostrará el RecyclerView;
- todos los registros recuperados aparecerán ordenados;
- la lista se actualizará cuando Room emita cambios.

### 10.3. Lista vacía

Cuando no existan registros:

- el RecyclerView podrá ocultarse;
- se mostrará un mensaje comprensible;
- podrá mostrarse una ilustración o icono;
- podrá mostrarse una invitación a registrar el primer elemento.

Texto orientativo:

```text
Todavía no hay mercancía registrada.
```

### 10.4. Error

Si ocurre un error al consultar los datos:

- se mostrará un mensaje comprensible;
- no se expondrán excepciones técnicas al usuario;
- el error deberá quedar disponible para diagnóstico durante desarrollo.

Texto orientativo:

```text
No se pudo cargar la mercancía.
```

---

## 11. Flujo principal

1. El usuario abre AlmacenTracker.
2. `MainActivity` crea u obtiene el ViewModel.
3. El ViewModel solicita el listado al repositorio.
4. El repositorio obtiene los datos del DAO.
5. Room devuelve el listado observable.
6. El ViewModel transforma el resultado en estado de interfaz.
7. La Activity observa el estado.
8. El RecyclerView muestra los registros.
9. Si no existen registros, se muestra el estado vacío.

---

## 12. Flujos alternativos

### FA-01 — Base de datos vacía

1. El usuario abre la aplicación.
2. Room devuelve una colección vacía.
3. La interfaz muestra el estado vacío.
4. No se considera un error.

### FA-02 — Registro sin posición

1. Room devuelve un registro con posición nula o vacía.
2. El adapter muestra categoría, código y sitio.
3. No se muestra un separador vacío ni un texto incorrecto.

### FA-03 — Cambio de configuración

1. El usuario rota el dispositivo.
2. La Activity se recrea.
3. El ViewModel conserva el estado.
4. El listado vuelve a mostrarse sin una navegación incorrecta ni pérdida de datos.

### FA-04 — Error inesperado

1. La consulta produce un error.
2. El ViewModel emite un estado de error.
3. La interfaz muestra un mensaje comprensible.
4. La aplicación no se cierra.

---

## 13. Criterios de aceptación

### CA-01 — Inicio con registros

**Dado** que existen registros almacenados,  
**cuando** el usuario abre la aplicación,  
**entonces** la pantalla principal muestra el listado de mercancía.

### CA-02 — Información mínima

**Dado** un registro válido,  
**cuando** aparece en el listado,  
**entonces** se muestran su categoría, código y sitio.

### CA-03 — Posición existente

**Dado** un registro con posición,  
**cuando** aparece en el listado,  
**entonces** se muestra también su nivel o fila.

### CA-04 — Posición inexistente

**Dado** un registro sin posición,  
**cuando** aparece en el listado,  
**entonces** no se muestra un campo vacío, un separador sobrante ni el texto `null`.

### CA-05 — Estado vacío

**Dado** que no existen registros,  
**cuando** el usuario abre la aplicación,  
**entonces** se muestra un estado vacío comprensible.

### CA-06 — Orden del listado

**Dado** que existen varios registros,  
**cuando** se muestran,  
**entonces** aparecen ordenados por categoría y código de forma ascendente.

### CA-07 — Actualización observable

**Dado** que la pantalla principal está abierta,  
**cuando** cambia el contenido de Room,  
**entonces** el listado se actualiza sin necesidad de reiniciar la aplicación.

### CA-08 — Rotación

**Dado** que el listado está visible,  
**cuando** el usuario rota el dispositivo,  
**entonces** la pantalla mantiene un estado correcto y vuelve a mostrar los datos.

### CA-09 — Error controlado

**Dado** que ocurre un error inesperado en la consulta,  
**cuando** la interfaz recibe el error,  
**entonces** muestra un mensaje comprensible y la aplicación no se cierra.

### CA-10 — Operación no bloqueante

**Dado** que la aplicación consulta Room,  
**cuando** se realiza la operación,  
**entonces** no se ejecuta una consulta bloqueante en el hilo principal.

---

## 14. Diseño técnico propuesto

### Puerto de entrada

`ObserveWarehouseItemsUseCase` representa la operación ofrecida por la aplicación para observar el listado completo de mercancía.

La interfaz de usuario dependerá de este puerto, no del DAO ni de la implementación Room.

### Servicio de aplicación

`ObserveWarehouseItemsService` implementará el puerto de entrada y delegará la recuperación en el puerto de salida.

No añadirá lógica artificial. Su responsabilidad inicial será establecer el límite de aplicación y permitir que la UI permanezca desacoplada de la infraestructura.

### Puerto de salida

`WarehouseItemRepository` definirá el contrato necesario para observar los registros ordenados.

### Adaptador de salida Room

`RoomWarehouseItemRepository` implementará el puerto de salida mediante:

- `WarehouseItemDao`;
- `WarehouseItemEntity`;
- `WarehouseItemPersistenceMapper`;
- `AlmacenTrackerDatabase`.

### DAO

Responsabilidad:

- observar todos los registros;
- devolverlos ordenados por categoría y código.

Firma orientativa:

```java
@Query(
    "SELECT * FROM warehouse_items " +
    "ORDER BY categoria COLLATE NOCASE ASC, " +
    "codigo COLLATE NOCASE ASC"
)
LiveData<List<WarehouseItemEntity>> observeAll();
```

La consulta observable de Room no deberá ejecutarse mediante acceso directo desde la Activity.

### ViewModel

Responsabilidad:

- utilizar `ObserveWarehouseItemsUseCase`;
- transformar la respuesta en estado de interfaz;
- sobrevivir a cambios de configuración;
- evitar lógica de persistencia en la Activity.

Se utilizará un estado exclusivo para impedir combinaciones inconsistentes:

```text
LOADING
CONTENT
EMPTY
ERROR
```

El estado incluirá la lista y un mensaje de error opcional.

### Activity

Responsabilidad:

- configurar RecyclerView;
- observar el ViewModel;
- renderizar carga, contenido, vacío y error;
- no acceder a DAO, base de datos o repositorio concreto.

### Adapter de RecyclerView

Responsabilidad:

- representar registros;
- ocultar posición cuando sea nula o vacía;
- actualizar la lista eficientemente mediante `ListAdapter` y `DiffUtil`.

### Composición de dependencias

`AppContainer` creará y conectará base de datos, repositorio, servicio de aplicación y Factory del ViewModel. No se incorporará un framework de inyección de dependencias en esta historia.

## 15. Estructura de archivos orientativa

```text
<package-root>/
├── domain/
│   └── model/
│       └── WarehouseItem.java
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── ObserveWarehouseItemsUseCase.java
│   │   └── out/
│   │       └── WarehouseItemRepository.java
│   └── service/
│       └── ObserveWarehouseItemsService.java
├── adapter/
│   ├── in/
│   │   └── ui/
│   │       ├── activity/
│   │       │   └── MainActivity.java
│   │       ├── adapter/
│   │       │   └── WarehouseItemAdapter.java
│   │       ├── state/
│   │       │   └── WarehouseItemListUiState.java
│   │       └── viewmodel/
│   │           ├── WarehouseItemListViewModel.java
│   │           └── WarehouseItemListViewModelFactory.java
│   └── out/
│       └── persistence/
│           └── room/
│               ├── dao/
│               │   └── WarehouseItemDao.java
│               ├── database/
│               │   └── AlmacenTrackerDatabase.java
│               ├── entity/
│               │   └── WarehouseItemEntity.java
│               ├── mapper/
│               │   └── WarehouseItemPersistenceMapper.java
│               └── repository/
│                   └── RoomWarehouseItemRepository.java
└── configuration/
    └── AppContainer.java
```

La estructura podrá evolucionar, pero no deberá eliminar la dirección de dependencias ni trasladar clases Room al dominio.

## 16. Datos de prueba

Los datos utilizados deberán ser completamente ficticios.

Ejemplo:

| Categoría | Código | Sitio | Posición |
|---|---|---|---|
| MR | 1050 | A1 | Nivel 2 |
| MD | 1050 | B3 | |
| CA | 2048 | C1 | Nivel 4 |

Estos datos demuestran que el código puede repetirse en categorías diferentes.

No se incluirán datos reales de empresas.

---

## 17. Pruebas recomendadas

### Pruebas unitarias

- el ViewModel expone contenido cuando recibe registros;
- el ViewModel expone estado vacío ante una lista vacía;
- el ViewModel expone error ante una excepción;
- el adapter representa correctamente una posición existente;
- el adapter omite correctamente una posición ausente.

### Pruebas de DAO

- `observeAll()` devuelve todos los registros;
- el resultado se encuentra ordenado;
- una base vacía devuelve una lista vacía;
- registros con el mismo código y categorías diferentes pueden coexistir.

### Pruebas manuales

- abrir con datos;
- abrir sin datos;
- rotar con contenido;
- rotar con lista vacía;
- verificar textos largos;
- verificar posición vacía;
- comprobar que no aparece `null`;
- verificar actualización automática;
- ejecutar sin conexión.

---

## 18. Tareas de implementación

1. Crear o confirmar el modelo de datos.
2. Crear la entidad Room.
3. Crear el índice único categoría + código.
4. Crear el DAO con consulta observable.
5. Crear la base de datos.
6. Crear el repositorio.
7. Crear el mapper si corresponde.
8. Crear el ViewModel.
9. Definir el estado de interfaz.
10. Diseñar `activity_main.xml`.
11. Diseñar el elemento del RecyclerView.
12. Crear `ListAdapter` y `DiffUtil`.
13. Observar el estado desde MainActivity.
14. Implementar carga.
15. Implementar contenido.
16. Implementar estado vacío.
17. Implementar estado de error.
18. Añadir datos ficticios para desarrollo o pruebas.
19. Crear pruebas.
20. Crear la rama `feature/hu-01-consultar-mercancia` desde `develop` antes de implementar.
21. Publicar los commits de la rama y ejecutar CI.
22. Documentar evidencias.
23. Revisar criterios de aceptación.
24. Fusionar localmente la rama terminada en `develop`.
25. Verificar nuevamente CI en `develop`.
26. Eliminar la rama local y remota una vez confirmada la integración.

---

## 19. Evidencias necesarias para cerrar la HU

- captura del listado con varios registros;
- captura del estado vacío;
- evidencia de un registro sin posición;
- evidencia de orden correcto;
- resultado de pruebas;
- resultado satisfactorio de CI en `feature/hu-01-consultar-mercancia`;
- evidencia del merge local hacia `develop`;
- resultado satisfactorio de CI en `develop` después de la integración;
- confirmación de que MainActivity no accede directamente al DAO;
- confirmación de que la lista sobrevive correctamente a una rotación.

---

## 20. Definición de terminado

La HU-01 estará terminada cuando:

- la aplicación inicie en la pantalla principal;
- Room proporcione el listado observable;
- el listado muestre categoría, código y sitio;
- la posición aparezca únicamente cuando exista;
- los registros se ordenen por categoría y código;
- exista estado de carga;
- exista estado con contenido;
- exista estado vacío;
- exista estado de error;
- la interfaz no consulte directamente el DAO;
- el ViewModel conserve el estado esencial;
- el listado se actualice automáticamente;
- no se utilicen datos reales;
- las pruebas definidas se ejecuten correctamente;
- la CI finalice correctamente en la rama de la HU;
- todos los criterios de aceptación estén verificados;
- la documentación quede actualizada;
- la rama se fusione localmente en `develop` sin Pull Request obligatorio;
- la CI de `develop` finalice correctamente después del merge;
- la rama de la HU se elimine cuando la integración haya sido comprobada.

---

## 21. Resultado esperado

Al cerrar la HU-01, AlmacenTracker tendrá una primera funcionalidad vertical completa:

```text
usuario abre la aplicación
        ↓
la aplicación consulta Room
        ↓
el ViewModel recibe el resultado
        ↓
la pantalla muestra la mercancía
```

Una vez cerrada esta historia, se podrá documentar e iniciar la siguiente historia de usuario de la versión 1.0.
