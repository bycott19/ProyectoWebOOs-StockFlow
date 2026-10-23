# StockFlow - Sistema de Gestión de Inventario y Pedidos

> **Proyecto Universitario:** Web Orientada a Objetos (WEB_OO)  
> **Caso de Estudio:** GlobalRetail S.A.  
> **Stack:** Java 17, Spring Boot 4.1.1, Spring Data JPA, PostgreSQL 16, Thymeleaf, Swagger/OpenAPI, Docker Compose, Gradle 9.

---

## 📋 Descripción del Sistema

**StockFlow** es una plataforma integral de gestión de inventario, clientes y pedidos en bodega diseñada para resolver los desafíos de quiebre de stock, concurrencia en ventas y trazabilidad de despachos.

El sistema cuenta con una **triple capa de acceso y documentación**:
1. **Interfaz Web Responsiva (Thymeleaf + CSS Neomórfico/Retro):** Diseñada según los mockups del proyecto para el equipo de bodega y administración.
2. **API RESTful Completa:** Para integración con aplicaciones móviles, clientes frontend o servicios externos.
3. **Documentación Interactiva Swagger / OpenAPI:** Pruebas interactivas de todos los endpoints REST directamente desde el navegador.

---

## 🚀 Módulos del Sistema

| Módulo | Ruta Web | Endpoints REST | Descripción |
| :--- | :--- | :--- | :--- |
| **1. Dashboard** | `GET /` | - | KPIs en tiempo real (productos activos, pedidos pendientes, clientes, alertas), últimos pedidos registrados y panel de quiebres inminentes. |
| **2. Catálogo de Productos** | `GET /productos` | CRUD `/api/productos` | Catálogo de artículos con barras visuales proporcionales de stock, buscador en vivo y modal interactivo para creación y edición. |
| **3. Alertas de Stock Bajo (RF6)** | `GET /stock-bajo` | `GET /api/productos/stock-bajo`<br>`GET /stock-bajo/exportar-csv` | Monitoreo en tiempo real de artículos bajo el umbral mínimo con cálculo de unidades faltantes, semáforo de urgencia y **exportación a reporte CSV (Excel)**. |
| **4. Gestión de Clientes** | `GET /clientes` | CRUD `/api/clientes` | Registro de clientes con **validación estricta de RUT chileno (Módulo 11)**, avatares dinámicos de iniciales, buscador interactivo, modal de creación/edición y acceso a historial de compras. |
| **5. Registro de Nuevo Pedido** | `GET /pedidos/nuevo` | `POST /api/pedidos` | Formulario interactivo con selector de cliente, selector de productos con semáforo de disponibilidad en tiempo real, resumen de compra y procesamiento atómico transaccional (`@Transactional`). |
| **6. Historial de Pedidos** | `GET /pedidos`<br>`GET /pedidos/cliente/{id}` | `GET /api/pedidos`<br>`GET /pedidos/exportar-csv` | Historial general y filtrado por cliente con ficha individual del cliente, etiquetas de estado y **exportación a reporte CSV**. |
| **7. Detalle de Pedido y Flujo de Estados** | `GET /pedidos/{id}` | `PUT /api/pedidos/{id}/estado` | Ficha detallada con línea de tiempo (`PENDIENTE` $\rightarrow$ `PAGADO` $\rightarrow$ `ENVIADO` / `CANCELADO`), desglose con precios históricos y **restitución automática de stock** si la orden se cancela. |
| **8. Swagger UI** | `GET /swagger-ui.html` | `GET /v3/api-docs` | Documentación interactiva OpenAPI de toda la API REST. |

---

## 🏗️ Arquitectura por Capas

El proyecto implementa una arquitectura limpia y desacoplada:

```
cl.baeza_munoz.stockflow/
├── config/            # Configuración OpenAPI / Swagger (OpenApiConfig.java)
├── controller/        # WebController (Thymeleaf) y Controllers REST (@RestController)
├── service/           # Capa de Lógica de Negocio y Transacciones (@Transactional)
│   ├── ClienteService.java
│   ├── ProductoService.java
│   └── PedidoService.java
├── repository/        # Repositorios Spring Data JPA
│   ├── ClienteRepository.java
│   ├── ProductoRepository.java
│   └── PedidoRepository.java
├── model/             # Entidades JPA (@Entity)
│   ├── Cliente.java
│   ├── Producto.java
│   ├── Pedido.java
│   └── DetallePedido.java
├── dto/               # Objetos de Transferencia de Datos para Pedidos
│   ├── PedidoRequestDTO.java
│   └── DetallePedidoDTO.java
├── util/              # Utilidades de Validación (RutValidator.java - Módulo 11)
└── exception/         # Manejador Global de Excepciones (@RestControllerAdvice)
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── StockInsuficienteException.java
```

---

## ⚙️ Puesta en Marcha

### Prerrequisitos
* Java JDK 17 o superior.
* Docker Desktop (para la base de datos PostgreSQL en contenedor).

### 1. Variables de Entorno
El proyecto lee automáticamente el archivo `.env` en la raíz (basado en `.env.example`):
```env
DB_HOST=localhost
DB_PORT=5433
DB_NAME=stockflow
DB_USER=postgres
DB_PASSWORD=postgres
SERVER_PORT=8081
```

### 2. Iniciar la Base de Datos con Docker
```bash
docker compose up -d
```

### 3. Ejecutar la Aplicación
```powershell
.\gradlew.bat bootRun
```

La aplicación estará accesible en:
* **Aplicación Web:** [http://localhost:8081](http://localhost:8081)
* **Documentación Swagger UI:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## 🧪 Pruebas Automatizadas

El proyecto incluye una suite de pruebas con base de datos **H2 en memoria** (`application.properties` en `src/test/resources/`), lo que permite ejecutar las pruebas sin necesidad de tener el contenedor de PostgreSQL activo:

```powershell
.\gradlew.bat test
```
Las pruebas cubren:
* Carga del contexto Spring Boot.
* Algoritmo de validación y formateo de RUT chileno (Módulo 11).
* Descuento atómico de inventario en compras exitosas.
* Rollback transaccional ante intento de compra con stock insuficiente.
* Restitución automática de unidades al inventario cuando un pedido es cancelado.
