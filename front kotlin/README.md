# 📱 SmartLogix - Android Mobile Client (Kotlin)

Cliente móvil nativo para terminales de alto cómputo y captura de datos en bodega, desarrollado íntegramente en **Android Kotlin** utilizando arquitectura limpia y **Jetpack Compose** para la interfaz de usuario.

## 🏗️ Decisiones de Arquitectura e Implementación
- **Patrón Arquitectónico**: MVVM (Model-View-ViewModel) para un desacoplamiento estricto entre la vista nativa y la lógica de datos.
- **Capa de Conectividad**: Implementación de **Retrofit** y **Gson** para el consumo asíncrono de las APIs expuestas por el entorno distribuido.
- **Manejo de Estados**: Uso exhaustivo de componentes asíncronos nativos (`StateFlow` y unificación en objetos `UiState`) para reaccionar fluidamente a los cambios en el backend.
- **Persistencia Local**: Base de datos **Room SQL** integrada para escenarios de almacenamiento en caché distribuidos e inicio de sesión offline.

## 🛠️ Requisitos Previos
- **Android Studio**: Ladybug (2024.2.1) o superior.
- **Gradle**: Versión compatible con las especificaciones internas del wrapper.
- **SDK mínimo**: Android SDK 26 (Android 8.0).

## 🚀 Compilación y Ejecución
1. Abra **Android Studio**.
2. Seleccione `Open an Existing Project` y apunte al directorio `front kotlin/`.
3. Sincronice los archivos de configuración de Gradle (`build.gradle.kts`).
4. Configure las direcciones IP locales de sus microservicios en la clase de configuración de red móvil (`RetrofitClient.kt`).
5. Ejecute la aplicación en un dispositivo físico o emulador Android mediante el botón **Run** (`Shift + F10`).

## 🧪 Pruebas Unitarias Móviles
El proyecto incluye pruebas unitarias de ViewModels implementadas con JUnit para asegurar el flujo correcto de los estados de la interfaz móvil:
```bash
./gradlew test