# Plan: Sistema de Autopago para Estacionamientos

## Estado actual

- **Directorio vacío** — el proyecto parte de cero
- **Java 25** disponible (excede el mínimo 17+)
- **Maven y Gradle NO instalados** — se usa el **Maven Wrapper** (mvnw), autocontenido
- **Node/npm/npx** disponibles (para skills Impeccable y Caveman)

## ✅ Estado del entregable (implementado)

- Estructura Maven completa con **Maven Wrapper** (`./mvnw`)
- Entidades JPA + repositorios desacoplados (contratos `*Store`)
- H2 embebida (archivo local `data/autopayparking`, offline-first)
- Pantallas JavaFX: Escanear → Confirmar → Pagar → Ticket+QR
- **Panel de administración web** (navegador, no la ventana del kiosco) con login
  (`admin` / `admin123`, hash PBKDF2 en BD)
- Datos maestros (tarifas, tasa de cambio) desde la BD, editables desde la web
- Indicador de pasos 1-4 en el flujo del kiosco
- UI del kiosco optimizada para **pantalla táctil** (botones ≥46px, campos y listas grandes)
- QR en pantalla con ZXing (transferencia offline al teléfono)
- Mock de pagos venezolanos (Pago Móvil, Transferencia, Efectivo Bs/USD)
- Seeds de demostración: 11 tickets, 6 pagos, tarifas y tasas históricas
- 24 pruebas (unitarias, contexto, autenticación, REST web y smoke de pantallas)
- **Verificado**: `mvnw.cmd clean verify` → BUILD SUCCESS; arranque con kiosco + web

### Cómo ejecutar

```bash
# Windows
mvnw.cmd javafx:run     # abre el kiosco Y la web admin (http://localhost:8080/admin/index.html)

# macOS / Linux
./mvnw javafx:run

# Pruebas
mvnw.cmd test

# Empaquetado (jar)
mvnw.cmd clean verify      # target/parking-0.1.0-SNAPSHOT.jar
```

### Acceso al panel de administración (web)

El kiosco abre el navegador en la URL de administración (botón **"Administración web"**).
También se accede desde cualquier dispositivo de la red:

| Dirección | Usuario | Contraseña | Rol |
|---|---|---|---|
| `http://localhost:8080/admin/index.html` | `admin` | `admin123` | Administrador (cuenta demo creada al primer arranque) |

La contraseña se guarda como hash **PBKDF2-HMAC-SHA256 con sal**; la web pide
login y guarda una sesión con token (`X-Admin-Token`).

### Tickets de prueba (sembrados en data.sql)

| Código | Entrada hace | Estado | Uso |
|---|---|---|---|
| `TKT-100001` | 1 hora | Activo | Pago rápido |
| `TKT-100002` | 3 horas | Activo | Tarifa normal |
| `TKT-100003` | 8 horas | Activo | Tarifa extendida |
| `TKT-100004` | 26 horas | Activo | **Tope diario** (descuento) |
| `TKT-100005..100008` | 1–3 días | Pagado | Reportes / resumen |
| `TKT-100009` | 1 día | Cancelado | Control |
| `TKT-100010` | 1 día | Pagado | Reportes (Pago Móvil) |
| `TKT-100011` | 1 día | Pagado | Reportes (pago en USD) |

El escáner USB (modo teclado) funciona escribiendo el código en la pantalla de escaneo; también hay campo de ingreso manual. `TKT-100004` sigue activo para probar el tope diario del cálculo de tarifa.

---

## Paso 0 — Instalar Skills

```bash
npx impeccable install        # detecta harness y escribe archivos de skill
/impeccable init              # entrevista → genera PRODUCT.md
/impeccable document          # genera DESIGN.md (colores, tipografía, componentes)
npx skills add JuliusBrussee/caveman  # eficiencia de tokens
```

> Nota: Impeccable y Caveman son skills del harness de desarrollo (Claude Code/Cursor). Si el harness actual no las soporta, se pueden usar como guía de referencia para decisiones de UI y compresión de contexto manualmente.

---

## Fase 1 — Estructura del Proyecto (Maven)

### 1.1 Inicializar Maven Wrapper

```bash
mvn wrapper:wrapper -Dmaven=3.9.9
```

Esto crea `mvnw` / `mvnw.cmd` y `.mvn/wrapper/` para que el proyecto sea autocontenido sin Maven global.

### 1.2 pom.xml — Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-parent` | 3.4.x | Parent POM y gestión de versiones |
| `spring-boot-starter-data-jpa` | — | Persistencia con JPA/Hibernate |
| `h2` | runtime | BD embebida (file-based para persistencia) |
| `javafx-controls` | 21.0.12 | Controles UI |
| `javafx-fxml` | 21.0.12 | FXML para diseño de pantallas |
| `zxing-core` + `zxing-javase` | 3.5.3 | Generación de códigos QR |
| `spring-boot-starter-test` | test | Pruebas unitarias |
| `lombok` | — | Reducir boilerplate (opcional) |

**Plugins:**
- `javafx-maven-plugin` 0.0.8 — ejecutar con `mvn javafx:run`
- `spring-boot-maven-plugin` — NO usar `repackage` (incompatible con JavaFX)
- `jpackage-maven-plugin` — para generar instalador nativo (.exe/.msi) en producción

### 1.3 Estructura de Carpetas

```
auto-pay-parking/
├── pom.xml
├── mvnw / mvnw.cmd
├── .mvn/wrapper/
├── src/main/java/com/autopayparking/
│   ├── AutoPayParkingApp.java              # Entry point (extends Application)
│   ├── config/
│   │   └── AppConfig.java                  # Config Spring + JavaFX bridge
│   ├── model/
│   │   ├── ParkingTicket.java              # Entity: ticket de estacionamiento
│   │   ├── TariffConfig.java               # Entity: configuración de tarifas
│   │   ├── PaymentRecord.java              # Entity: registro de pagos
│   │   └── ExchangeRate.java               # Entity: tasa de cambio (BCV/paralela)
│   ├── repository/
│   │   ├── ParkingTicketRepository.java
│   │   ├── TariffConfigRepository.java
│   │   ├── PaymentRecordRepository.java
│   │   └── ExchangeRateRepository.java
│   ├── service/
│   │   ├── ParkingService.java             # Lógica de estacionamiento
│   │   ├── TariffService.java              # Cálculo de tarifas
│   │   ├── PaymentService.java             # Mock de pagos
│   │   ├── QRCodeService.java              # Generación de QR (ZXing)
│   │   └── ScannerService.java             # Manejo de escáner USB
│   └── ui/
│       ├── controller/
│       │   ├── MainController.java         # Navegación principal
│       │   ├── ScanScreenController.java   # Pantalla de escaneo
│       │   ├── ConfirmScreenController.java # Confirmación de pago
│       │   ├── PaymentScreenController.java # Selección de método de pago
│       │   ├── TicketScreenController.java  # Ticket pagado con QR
│       │   └── admin/
│       │       ├── AdminDashboardController.java
│       │       ├── TariffAdminController.java
│       │       └── ReportsController.java
│       └── util/
│           └── ScannerHandler.java         # Buffer de teclado para escáner
├── src/main/resources/
│   ├── application.properties
│   ├── fxml/
│   │   ├── main-view.fxml
│   │   ├── scan-screen.fxml
│   │   ├── confirm-screen.fxml
│   │   ├── payment-screen.fxml
│   │   ├── ticket-screen.fxml
│   │   └── admin/
│   │       ├── admin-dashboard.fxml
│   │       ├── tariff-admin.fxml
│   │       └── reports.fxml
│   ├── css/
│   │   └── styles.css                      # CSS moderno/premium
│   └── images/
│       └── logo.png
└── src/test/java/com/autopayparking/
    └── service/
        ├── TariffServiceTest.java
        └── ParkingServiceTest.java
```

---

## Fase 2 — Modelo de Datos (JPA + H2)

### 2.1 Entidades Principales

**ParkingTicket**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK, auto) | Identificador |
| `code` | String (unique) | Código de barras/QR escaneado |
| `entryTime` | LocalDateTime | Hora de entrada |
| `exitTime` | LocalDateTime (nullable) | Hora de salida (null = activo) |
| `licensePlate` | String (nullable) | Placa del vehículo |
| `amountDue` | BigDecimal | Monto a pagar |
| `status` | Enum (ACTIVE, PAID, CANCELLED) | Estado del ticket |

**TariffConfig**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK) | Identificador |
| `name` | String | Nombre de la tarifa |
| `ratePerHour` | BigDecimal | Costo por hora |
| `minimumMinutes` | int | Tiempo mínimo cobrado |
| `maxDailyRate` | BigDecimal | Tope diario máximo |
| `active` | boolean | Si está activa |

**PaymentRecord**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK) | Identificador |
| `ticket` | FK → ParkingTicket | Ticket asociado |
| `paymentMethod` | Enum (MOBILE_PAY, CARD_POS, CASH_VES, CASH_USD) | Método |
| `amount` | BigDecimal | Monto pagado |
| `currency` | Enum (VES, USD) | Moneda |
| `exchangeRateUsed` | BigDecimal | Tasa de cambio al momento del pago |
| `timestamp` | LocalDateTime | Fecha/hora del pago |
| `status` | Enum (COMPLETED, FAILED, PENDING) | Estado |

**ExchangeRate**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | Long (PK) | Identificador |
| `bcvRate` | BigDecimal | Tasa oficial BCV |
| `parallelRate` | BigDecimal | Tasa paralela |
| `updatedAt` | LocalDateTime | Última actualización |

### 2.2 Persistencia

```properties
# application.properties
spring.datasource.url=jdbc:h2:file:./data/autopayparking;DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

La capa JPA queda desacoplada — cambiar a PostgreSQL/MySQL es solo cambiar properties.

---

## Fase 3 — Pantallas del Kiosco (JavaFX + FXML)

### 3.1 Flujo Principal del Usuario

```
┌──────────────┐    ┌───────────────┐    ┌──────────────┐    ┌──────────────┐
│   ESCANEAR   │───▶│   CONFIRMAR   │───▶│    PAGAR     │───▶│   TICKET     │
│  (escáner    │    │ (monto +      │    │ (selección   │    │ (QR en       │
│   USB)       │    │  tiempo)      │    │  método)     │    │  pantalla)   │
└──────────────┘    └───────────────┘    └──────────────┘    └──────────────┘
                                                   │
                                          ┌────────┴────────┐
                                          │   Pago Móvil    │
                                          │   Transferencia │
                                          │   Efectivo BS   │
                                          │   Efectivo USD  │
                                          └─────────────────┘
```

### 3.2 Descripción de Cada Pantalla

**Pantalla 1 — Escaneo (`scan-screen.fxml`)**
- Instrucción grande: "Escanee su ticket"
- Icono de escáner o código de barras
- Campo oculto que captura input del escáner USB (keyboard wedge)
- Feedback visual: escaneando... / ticket encontrado / error
- Timer para detectar fin de lectura (keystroke timing)

**Pantalla 2 — Confirmación (`confirm-screen.fxml`)**
- Código del ticket
- Hora de entrada
- Tiempo transcurrido (calculado)
- Monto a pagar (con desglose si aplica)
- Botón "Pagar" / "Cancelar"
- Mensajes de error: ticket no encontrado / ticket ya pagado

**Pantalla 3 — Selección de Método de Pago (`payment-screen.fxml`)**
- Resumen del monto (Bs. y USD si aplica)
- Botones grandes para cada método:
  - **Pago Móvil** → sub-pantalla: selección de banco, datos de cuenta destino, monto en Bs.
  - **Transferencia** → sub-pantalla: datos bancarios, referencia
  - **Efectivo Bolívares** → sub-pantalla: monto, billetes recibidos, vuelto
  - **Efectivo Dólares** → sub-pantalla: monto equivalente, tipo de cambio aplicado
- Espacio para agregar: Zelle, Binance Pay (futuro)
- Mockup: no procesa pago real, solo muestra UI y simula confirmación

**Pantalla 4 — Ticket Pagado (`ticket-screen.fxml`)**
- Mensaje: "¡Pago exitoso!"
- Resumen: código, tiempo, monto pagado, método
- **Código QR grande en pantalla** con datos del ticket (para que el usuario lo capture con su teléfono)
- Botón "Nuevo ticket"

### 3.3 Panel de Administración (interfaz web)

El panel **no vive en la ventana del kiosco**: Spring Boot sirve una aplicación
web (mismo proceso, puerto 8080) con las mismas credenciales de BD.

- **Login (`/admin/index.html`)** — usuario/contraseña; el contenido nunca se
  muestra antes de autenticarse (token de sesión `X-Admin-Token`).
- **Resumen** — tickets activos/pagados, pagos de hoy, total recaudado
  (todas las monedas normalizadas a Bs) y tasa BCV activa.
- **Tarifas y tasa** — datos maestros desde la BD: CRUD de tarifas y
  actualización de la tasa de cambio con feedback de guardado/error; el kiosco
  usa los cambios de inmediato.
- **Reportes** — historial de pagos (fecha, ticket, método, moneda, monto) y
  total recaudado, con actualización manual.

`GET /api/admin/dashboard`, `GET/POST /api/admin/tariffs`, `DELETE /api/admin/tariffs/{id}`,
`GET/PUT /api/admin/rates`, `GET /api/admin/reports`, `POST /api/admin/login|logout`.

---

## Fase 4 — Lógica de Negocio

### 4.1 Escáner USB (Keyboard Wedge)

```java
// ScannerHandler.java — maneja input del escáner
// Detecta fin de lectura por: Enter key + timing (<50ms entre teclas)
// No necesita librería externa, solo JavaFX KeyListener
```

### 4.2 Cálculo de Tarifas

```java
// TariffService.java
// Recibe: entryTime, exitTime
// Retorna: monto a pagar
// Lógica: minimumMinutes → ratePerHour → maxDailyRate (tope)
// Tarifas configurables desde BD (TariffConfig entity)
```

### 4.3 Generación de QR

```java
// QRCodeService.java — usa ZXing
// Genera BufferedImage con código QR
// Contenido: JSON con {ticketCode, paidAt, amount, method}
// Se muestra en pantalla con ImageView de JavaFX
```

### 4.4 Mock de Pagos

```java
// PaymentService.java
// Simula procesamiento de pago (delay de 1-2 segundos)
// Valida datos del mockup (monto, método)
// Registra en PaymentRecord
// No integra pasarela real
```

---

## Fase 5 — Diseño UI (Impeccable)

### 5.1 Princios de Diseño
- **Touch-friendly**: botones grandes (mínimo 48px de alto), áreas de toque amplias
- **Tipografía grande**: títulos 32-48px, cuerpo 18-24px (legible a distancia de kiosco)
- **Colores**: paleta coherente (azul oscuro primario, verde para éxito, rojo para error)
- **Espaciado generoso**: márgenes amplios, separación entre elementos
- **Animaciones sutiles**: transiciones entre pantallas, feedback visual

### 5.2 CSS Base (`styles.css`)
- Variables CSS para colores, tipografía, espaciado
- Estilos para botones, tarjetas, tablas
- Estados de interacción (hover, pressed, disabled)
- Responsive para diferentes resoluciones de kiosco

### 5.3 Iteración con `/impeccable polish`
- Refinar cada pantalla: alineación, espaciado, tipografía
- Estados de interacción (escaneando, procesando, error)
- Transiciones entre pantallas

---

## Fase 6 — Empaquetado y Distribución

### 6.1 Durante Desarrollo
```bash
./mvnw javafx:run          # Ejecutar directamente
```

### 6.2 Para Producción
```bash
./mvnw clean verify        # Genera instalador con jpackage
# Windows: .exe o .msi
# macOS: .app
# Linux: .deb o .rpm
```

---

## Fase 7 — Pruebas

- **Unitarias**: TariffService (cálculo de montos), PaymentService (mock), QRCodeService
- **Integración**: Flujo completo escaneo → pago → ticket (con H2 embebida)
- **Manuales**: Prueba con escáner USB real, verificación de UI en pantalla táctil

---

## Orden de Implementación Recomendado

| # | Tarea | Dependencias |
|---|---|---|
| 1 | Instalar Maven Wrapper + crear pom.xml | Ninguna |
| 2 | Crear estructura de paquetes + entity base | #1 |
| 3 | Implementar modelos JPA + repositorios | #2 |
| 4 | Crear `application.properties` + H2 config | #3 |
| 5 | Entry point JavaFX + Spring Boot bridge | #1 |
| 6 | FXML + CSS base (pantalla de escaneo) | #5 |
| 7 | ScannerHandler (input de escáner USB) | #6 |
| 8 | Lógica de tarifas (TariffService) | #3 |
| 9 | Pantalla de confirmación | #6, #8 |
| 10 | Mock de pago + pantalla de selección de método | #9 |
| 11 | QRCodeService + pantalla de ticket | #10 |
| 12 | Panel administrativo (tarifas + reportes) | #3, #6 |
| 13 | CSS premium + iteración con Impeccable | #6-#12 |
| 14 | Pruebas unitarias | #3, #8 |
| 15 | Empaquetado con jpackage | Todos |

---

## Riesgos y Mitigaciones

| Riesgo | Mitigación |
|---|---|
| JavaFX + Spring Boot compatibilidad | No usar `module-info.java`, entry point en `Application` |
| jpackage requiere WiX (Windows) | Instalar WiX Toolset, o usar .jar ejecutable como alternativa |
| Escáner USB no detectado | Verificar modo keyboard wedge, fallback a input manual |
| H2 rendimiento con mucho tráfico | Cambiar a PostgreSQL/MySQL via properties (diseño desacoplado) |
| Impeccable/Caveman no disponibles | Seguir guías de diseño manualmente |

---

## Entregable de esta Etapa

✅ Estructura del proyecto completa (Maven, paquetes, dependencias)
✅ Modelos de datos JPA + esquema H2 (incl. `app_users`)
✅ Kiosco en JavaFX: Escanear → Confirmar → Pagar → Ticket con QR (pasos 1-4)
✅ Panel de administración web (login + resumen + tarifas/tasa + reportes)
✅ Datos maestros (tarifas, tasa) editables desde la web
✅ CSS del kiosco optimizado para pantalla táctil
✅ Pruebas (unitarias, autenticación, REST web y smoke de pantallas)
✅ Empaquetado ejecutable

**NO incluido en esta etapa:** pasarela de pago real, impresora térmica, multi-idioma.
