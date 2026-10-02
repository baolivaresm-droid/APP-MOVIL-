# App Móvil de Terreno - Induztek

Aplicación móvil desarrollada en **Jetpack Compose** (Android) para técnicos de terreno, permitiendo el registro, gestión y consulta de protocolos de ensayo e inspección de equipos eléctricos (transformadores, relés, celdas de media tensión, etc.).

---

## 🚀 Características Principales

* **Autenticación de Usuarios:** Sistema de inicio de sesión y registro con persistencia local mediante base de datos relacional.
* **Catálogo de Equipos Eléctricos:** Visualización en cuadrícula de los equipos críticos en terreno (con códigos de identificación, ubicaciones de clientes e historiales previos).
* **Registro de Protocolos:** Formulario de medición en terreno para guardar pruebas técnicas (valores de aislamiento, corrientes, voltajes, etc.).
* **Gestión de Protocolos (Carrito):** Pantalla de listado para revisar, seleccionar o eliminar los ensayos guardados antes de enviarlos.
* **Interfaz Adaptativa:** Soporte para cambio de tema (Modo Claro / Modo Oscuro) desde el menú contextual.
* **Navegación Optimizada:** Barras superiores (`TopAppBar`) personalizadas con accesos directos rápidos al inicio, listados y opción de cierre de sesión seguro.

---

## 🛠️ Tecnologías y Librerías Utilizadas

* **UI:** Jetpack Compose, Material Design 3.
* **Navegación:** Jetpack Navigation Compose.
* **Base de Datos & ORM:** Room (SQLite local para manejo de perfiles y entidades de protocolos).
* **Concurrencia:** Kotlin Coroutines y Flow.
* **Feedback Háptico:** Vibración de confirmación nativa para la interacción en terreno.

---

## 📱 Capturas de Pantalla / Arquitectura
El proyecto sigue los principios de arquitectura limpia de Android, separando responsabilidades en vistas (`ui`), modelos de datos/entidades (`models`), repositorios (`repository`) y lógica de negocio/viewmodels.

---

## ⚙️ Cómo Ejecutar el Proyecto

1. Clonar el repositorio en tu máquina local:
   ```bash
   git clone [https://github.com/baolivaresm-droid/APP-MOVIL-.git](https://github.com/baolivaresm-droid/APP-MOVIL-.git)

2. Abrir Android Studio (versión recomendada: Iguana o superior).

3. Seleccionar Open an Existing Project y elegir la carpeta del repositorio clonado.

4. Sincronizar las dependencias de Gradle (Sync Project with Gradle Files).

5. Ejecutar la aplicación en un Emulador de Android (API 24 o superior) o en un dispositivo físico mediante depuración USB.
