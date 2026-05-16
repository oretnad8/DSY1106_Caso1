# 📦 SmartLogix: Sistema Integral de Gestión Logística
## Desarrollo Fullstack III (DSY1106) Evaluación Parcial N°2 - Duoc UC

### 📝 Descripción del Proyecto
SmartLogix es una solución de software de extremo a extremo (E2E) diseñada para optimizar la cadena de suministro, gestión de inventarios, picking y despacho. El sistema utiliza una arquitectura moderna basada en microservicios, un orquestador BFF y múltiples interfaces de usuario (Web y Móvil) para cubrir las necesidades de operarios, jefes de bodega y administradores.

### 🏗️ Arquitectura del Sistema
La solución se basa en una arquitectura desacoplada:
* **Backend (Microservicios):** 6 servicios independientes construidos en Java Spring Boot que manejan la lógica de negocio central.
* **BFF (Backend For Frontend):** Un middleware en Node.js que unifica las peticiones del frontend web hacia los microservicios.
* **Frontend Web:** Consola administrativa desarrollada en React y TypeScript.
* **Frontend Móvil:** Aplicación nativa en Kotlin para terminales de alto cómputo en bodega.

### 📁 Estructura del Repositorio
El proyecto está organizado de la siguiente manera:

```text
.
├── 📂 backend               # Microservicios Java Spring Boot (Maven)
│   ├── authservice          # Gestión de sesiones y seguridad JWT
│   ├── usuariosservice      # Administración de usuarios y roles
│   ├── productosservice     # Catálogo maestro y stock
│   ├── ubicacionesservice   # Layout de bodega y racks
│   ├── pedidosservice       # Flujo de órdenes y picking
│   └── aprobacionesservice  # Auditoría y flujos críticos
│
├── 📂 front web             # Aplicación Administrativa Web
│   ├── 📂 backend           # Componente BFF (Node.js Express)
│   └── 📂 frontend          # Frontend React (NPM)
│
└── 📂 front kotlin          # Aplicación Móvil (Android Native)
    └── 📂 app               # Lógica móvil y Jetpack Compose
```

### 🛠️ Stack Tecnológico
* **Lenguajes:** Java 17+, Kotlin, JavaScript/TypeScript.
* **Frameworks Backend:** Spring Boot, Express.js.
* **Frameworks Frontend:** React 18+, Jetpack Compose (Android).
* **Base de Datos:** PostgreSQL (Microservicios), Room SQL (Móvil).
* **Versionamiento:** Git con estrategia Feature Branching.

### 🚀 Guía Rápida de Inicio
Cada componente tiene sus propias instrucciones detalladas de configuración en su respectiva carpeta. Por favor, consulte los archivos `README.md` internos:1.  **Backend:** Consulte `backend/README.md` para levantar los servicios Maven.
2.  **Web & BFF:** Consulte `front web/README.md` para iniciar el orquestador y el dashboard.
3.  **Móvil:** Consulte `front kotlin/README.md` para compilar la APK en Android Studio.

### 🌿 Estrategia de Branching
Se implementó un flujo de trabajo basado en GitFlow simplificado:
* `main`: Código estable y entregas finales.
* `develop`: Integración continua de nuevas características.
* `feature/*`: Ramas de desarrollo por componente (ej. `feature/backend-microservices`).

### 👨‍💻 Autores
Este proyecto fue desarrollado por el equipo SmartLogix como parte del programa de Ingeniería en Informática de Duoc UC.