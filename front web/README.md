# 🌐 SmartLogix - Web Dashboard & BFF

Este repositorio contiene la consola central de administración web de **SmartLogix**, dividida estrictamente en dos capas: un Frontend SPA y un servidor BFF (Backend For Frontend).

## 📁 Estructura del Directorio
- `/backend`: Servidor **BFF** desarrollado en **Node.js Express**. Centraliza el consumo de los microservicios Java, realiza agregación de datos y expone endpoints simplificados hacia la capa visual.
- `/frontend`: Aplicación cliente SPA construida utilizando **React**, **TypeScript**, **Vite** y estandarizada bajo el ecosistema **NPM**.

## 🛠️ Requisitos Previos
- **Node.js**: Versión 18.x o superior.
- **NPM**: Versión 9.x o superior.

## 🚀 Instrucciones de Ejecución

### 1. Configuración e Inicio del BFF (Backend)
El BFF actúa como proxy inverso y orquestador para el cliente web.
```bash
cd backend
npm install
# Configure las variables en un archivo .env si es necesario (ej. puertos de microservicios)
node server.js
El servidor BFF iniciará comúnmente en el puerto configurado (ej: http://localhost:5000).

2. Configuración e Inicio del Frontend Web
```Bash
cd ../frontend
npm install
npm run dev
La consola de desarrollo se desplegará localmente, típicamente en http://localhost:5173.

## 📑 Scripts NPM Disponibles en Frontend
npm run dev: Lanza el servidor local de desarrollo con Hot Module Replacement (HMR).

npm run build: Compila y optimiza la aplicación para producción en la carpeta /dist.

npm run lint: Ejecuta el análisis estático de código mediante ESLint
