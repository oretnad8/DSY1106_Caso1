# 🚀 SmartLogix - Backend Microservices

Este directorio contiene el ecosistema de microservicios distribuidos para la plataforma **SmartLogix**, construidos sobre el ecosistema de **Java Spring Boot** y gestionados mediante **Maven**.

## 🧱 Estructura de Módulos (Arquetipos Maven)
- **authservice**: Microservicio encargado del cifrado y validación de sesiones JWT.
- **usuariosservice**: Gestión de datos maestros de operarios, jefes de bodega y administradores.
- **productosservice**: Control del catálogo maestro de SKUs y stock consolidado.
- **ubicacionesservice**: Administración del layout físico del almacén (Layout de Pasillos y Racks).
- **pedidosservice**: Orquestación transaccional del flujo de órdenes de compra y picking.
- **aprobacionesservice**: Motor de auditoría y flujos de aprobación para movimientos de inventario excepcionales.

## 🛠️ Requisitos Previos
- **Java JDK**: Versión 17 o superior.
- **Apache Maven**: Versión 3.8+.
- **Motor de Base de Datos**: PostgreSQL / MySQL configurado según el perfil de entorno.

## 🚀 Instalación y Ejecución
Para compilar y ejecutar cualquiera de los microservicios de forma independiente, navega al directorio del módulo específico y ejecuta:

```bash
# Cambiar al directorio del microservicio deseado, por ejemplo:
cd pedidosservice

# Compilar el proyecto saltando las pruebas unitarias si es necesario
./mvnw clean package -DskipTests

# Ejecutar la aplicación Spring Boot
./mvnw spring-boot:run