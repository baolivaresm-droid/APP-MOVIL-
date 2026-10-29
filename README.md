# App Móvil de Terreno - Induztek

Aplicación móvil desarrollada en **Jetpack Compose** (Android) para técnicos de terreno de **Induztek Ingeniería SpA**, que permite registrar y emitir los **protocolos de ensayo e inspección de equipos eléctricos** (transformadores, relés, celdas, baterías) directamente en la instalación del cliente, sin volver a la oficina a llenar el protocolo en Excel u Office.

> **Caso base:** "Digitalización del protocolo de pruebas eléctricas en terreno" — DSY1105 Desarrollo de Aplicaciones Móviles.

---

## Integrantes

| Nombre | Correo institucional |
|---|---|
| Bastian Olivares | ba.olivaresm@duocuc.cl |
| Pablo Pastenes | pa.pastenes@duocuc.cl |

---

##  Roles y flujo de trabajo

La app tiene dos perfiles con permisos distintos:

| Rol | Qué puede hacer |
|---|---|
| **Administrador** | Aprueba o rechaza las cuentas de los técnicos, registra las empresas clientes y sus sucursales, asigna a cada técnico las empresas y sucursales donde puede trabajar, registra la maquinaria disponible y valida la maquinaria que los técnicos proponen en terreno. Además revisa el historial de protocolos emitidos. |
| **Técnico** | Inicia sesión, ve solo las empresas y sucursales que le asignó el administrador, registra la medición de los equipos con foto de evidencia, propone maquinaria nueva y emite el protocolo PDF para enviarlo al cliente. |

**Flujo típico:** el técnico se registra → queda *pendiente de aprobación* → el administrador lo aprueba → el técnico entra → ve sus empresas asignadas → elige la sucursal → mide el equipo con foto → genera y comparte el protocolo PDF → el protocolo queda en el historial del administrador.

El acceso de administrador **no se puede registrar desde la app**: la cuenta se crea directamente en la base de datos al instalar, de modo que solo el personal autorizado accede al panel de gestión.

---

##  Funcionalidades implementadas

### Pantallas (10)

1. **Login** — selector de acceso (Técnico / Administrador) con validación de correo y contraseña. El ViewModel (con las validaciones del `Validaciones`) valida el rol **y** el estado de aprobación.
2. **Registro** — alta de cuenta de técnico con validaciones por campo (nombre, correo, teléfono, contraseña segura y confirmación). La cuenta queda *pendiente de aprobación*.
3. **Panel de Administración** — 4 secciones en pestañas:
   - *Técnicos*: listar, aprobar y rechazar cuentas; ver cuántas sucursales tiene asignadas cada técnico.
   - *Empresas*: alta de clientes y, al entrar a cada uno, sus sucursales con la opción de **asignar la empresa completa a un técnico** o sucursal por sucursal.
   - *Maquinaria*: validar la maquinaria propuesta por los técnicos y registrar equipos por sucursal.
   - *Informes*: historial de protocolos emitidos, **ordenado por empresa y sucursal**, con la opción de volver a compartir el PDF.
4. **Mis Empresas** (Home del técnico) — solo las empresas que el administrador le asignó.
5. **Sucursales de la empresa** — solo las sucursales asignadas a ese técnico, con dirección, comuna y teléfono.
6. **Detalle de Sucursal** — maquinaria agrupada por categoría (Transformadores, Relés, Celdas, Baterías) con la imagen de referencia de cada categoría. El técnico puede proponer equipos nuevos, que quedan *pendientes* hasta su validación.
7. **Formulario de Medición** — valores de la prueba, observaciones, **valor del trabajo** (opcional) y **foto del equipo al momento de la medición** (obligatoria). Muestra el historial de pruebas anteriores del equipo.
8. **Informe del Protocolo** — tabla comparativa entre la lectura actual y la histórica, datos del cliente, la sucursal y el técnico responsable, y generación del **PDF** para compartir por WhatsApp o correo.
9. **Perfil** — edición de la zona/base de operaciones y foto de perfil (cámara o galería).

### Otros detalles

- **Sin conexión**: todo el flujo funciona offline con base local.
- **Tema claro / oscuro** con persistencia en DataStore, disponible tanto para el técnico como para el administrador.
- **Mensajes de estado vacío** en cada listado.
- **Evidencia fotográfica**: la foto del equipo se toma en terreno y viaja **solo dentro del protocolo PDF**; la app no la guarda ni la muestra después.

---

##  Recursos nativos utilizados

| Recurso | Uso | Permiso |
|---|---|---|
| **Cámara (CameraX embebida)** | Captura de la foto de evidencia del equipo y de la foto de perfil sin salir de la app | `CAMERA` (runtime) |
| **Galería / PhotoPicker** | Selección de imágenes sin acceso amplio al almacenamiento | — |
| **PDF nativo** (`PdfDocument`) | Generación del protocolo formal en el dispositivo | — |
| **Compartir** (`FileProvider` + `ACTION_SEND`) | Envío del protocolo por WhatsApp, correo u otra app | — |
| **Vibración háptica** | Confirmación háptica al guardar una medición | `VIBRATE` |
| **Almacenamiento local** | Room (SQLite) para empresas, sucursales, maquinaria, mediciones, asignaciones e informes | — |
| **DataStore** | Persistencia del tema claro/oscuro | — |

---

##  Arquitectura

MVVM con ViewModels que acceden directamente a los DAO de Room (sin capa Repository ni Factory), igual que el proyecto de referencia:

```
app/src/main/java/com/example/appmovil/
├── MainActivity.kt        Activity + AppTheme (tema claro/oscuro desde DataStore)
├── repository/
│   ├── data/              AppDatabase (singleton + seed) y los 7 DAOs
│   │                      (Usuario, Empresa, Sucursal, Maquina, Medicion,
│   │                       Asignacion e Informe)
│   └── models/            Usuario, Empresa, Sucursal, Maquina, Medicion,
│                          Asignacion e Informe (entidades Room) + roles y estados
├── viewmodels/            Login, Registro, Admin, Asignacion, Empresa, Sucursal,
│                          Maquina, Medicion, Informe y Tema (AndroidViewModel)
│   └── Validaciones.kt    Validaciones centralizadas
├── navegations/
│   └── AppNavigation.kt   Routes, drawer por rol y NavHost con animaciones
└── view/
    ├── theme/             Tema de Material 3
    ├── utils/             CameraCapture (CameraX), PhotoBox, Animations,
    │                      InformePdf y EstadoDataStore
    └── ui/                Login, Registro, Home, Admin, AdminEmpresa,
                           TecnicoEmpresa, SucursalDetail, MedicionForm,
                           Informe y Perfil
```

**Modelo de datos (Room v9):**

```
Usuario (rol + estado)
   └─ Asignacion ──> Sucursal ──> Maquina ──> Medicion
                        │             (estado)     (precio)
                        └──> Empresa
   Informe (protocolo emitido, con empresa, sucursal, equipo y PDF)
```

- El `Usuario` tiene `rol` (ADMIN / TECNICO) y `estado` (PENDIENTE / APROBADO / RECHAZADO).
- La tabla `asignaciones` relaciona usuarios con sucursales: de ahí se deduce qué empresas ve cada técnico.
- La `Maquina` tiene `estado` (PENDIENTE cuando la propone un técnico) y `creadoPor`.
- La `Medicion` guarda el `precio` opcional del trabajo.
- La `Informe` registra cada protocolo emitido, con su archivo PDF en el dispositivo.

**Navegación por eventos** (patrón del módulo): `MainViewModel` emite `NavigationEvent` y un único colector en `NavGraph` los ejecuta sobre el `NavHostController`.

---

##  Stack tecnológico

- Kotlin 2.0.21 · Jetpack Compose (BOM 2026.02.01) · Material Design 3
- Navigation Compose · ViewModel + StateFlow · Kotlin Coroutines
- Room 2.6.1 (KSP) · DataStore Preferences · Coil (carga de imágenes)

---

##  Credenciales de prueba

| Perfil | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@induztek.cl` | `Induztek2026!` |
| Técnico (demo) | `tecnico@induztek.cl` | `Induztek123!` |

La base local se crea con datos de ejemplo:

- Empresas **Jumbo** (Jumbo La Florida, Jumbo Las Condes) y **Líder** (Líder Quilpué).
- Maquinaria de ejemplo: TRA-001, REL-001, CEL-001 y BAT-001.
- Las tres sucursales están asignadas al Técnico Demo, que por eso ve las dos empresas.

> Para entrar como administrador, en la pantalla de login hay que cambiar el selector de **Técnico** a **Administrador**.

---

##  Cómo ejecutar el proyecto

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/baolivaresm-droid/APP-MOVIL-.git
   ```

2. Abrir el proyecto en **Android Studio** (versión Iguana o superior).

3. Sincronizar las dependencias de Gradle (`Sync Project with Gradle Files`).

4. Verificar la ruta del SDK en `local.properties` (`sdk.dir=...`).

5. Ejecutar en un emulador (API 24 o superior) o en un dispositivo físico con depuración USB:
   ```bash
   ./gradlew installDebug
   ```

6. Iniciar sesión con cualquiera de las credenciales de prueba.

---

##  Requisitos

- Android Studio Iguana o superior
- JDK 17 o superior
- Android SDK con `compileSdk 35`, `minSdk 24`
- Emulador o dispositivo Android (API 24+)

---

> Los datos de la app son **ficticios y sintéticos**, conforme a las restricciones del caso.
