# Tickets en el sistema

Generado desde la base de datos `data/autopayparking` (H2) el **09/09/2026**.

## Resumen

| Concepto | Cantidad |
|---|---|
| Tickets activos | 4 |
| Tickets pagados | 6 |
| Tickets cancelados | 1 |
| **Total de tickets** | **11** |
| Pagos registrados | 6 |
| Total recaudado (normalizado a Bs.) | Bs. 34.02 |

## Tickets

| Código | Entrada | Salida | Placa | Monto (Bs.) | Estado |
|---|---|---|---|---|---|
| `TKT-100001` | 01/09/2026 20:59 | — | AB123CD | — | Activo |
| `TKT-100002` | 01/09/2026 18:59 | — | XY789ZZ | — | Activo |
| `TKT-100003` | 01/09/2026 13:59 | — | JKL456MN | — | Activo |
| `TKT-100004` | 31/08/2026 21:15 | — | GH567JK | — | Activo |
| `TKT-100005` | 29/08/2026 20:15 | 29/08/2026 23:15 | AA111BB | 6,00 | Pagado |
| `TKT-100006` | 29/08/2026 22:15 | 29/08/2026 23:15 | CC222DD | 2,00 | Pagado |
| `TKT-100007` | 30/08/2026 21:15 | 30/08/2026 23:15 | EE333FF | 4,00 | Pagado |
| `TKT-100008` | 30/08/2026 18:15 | 30/08/2026 23:15 | GG444HH | 10,00 | Pagado |
| `TKT-100009` | 31/08/2026 23:15 | — | MM777NN | — | Cancelado |
| `TKT-100010` | 31/08/2026 19:15 | 31/08/2026 23:15 | II555JJ | 8,00 | Pagado |
| `TKT-100011` | 31/08/2026 21:15 | 31/08/2026 23:15 | KK666LL | 4,00 | Pagado |

## Pagos asociados

| Ticket | Método | Monto | Moneda | Tasa usada | Fecha/hora | Estado |
|---|---|---|---|---|---|---|
| `TKT-100005` | Pago Móvil | 6,00 | Bs. | 34,90 | 29/08/2026 23:15 | Completado |
| `TKT-100006` | Transferencia | 2,00 | Bs. | 34,90 | 29/08/2026 23:15 | Completado |
| `TKT-100007` | Efectivo Bs | 4,00 | Bs. | 35,80 | 30/08/2026 23:15 | Completado |
| `TKT-100008` | Efectivo Bs | 10,00 | Bs. | 35,80 | 30/08/2026 23:15 | Completado |
| `TKT-100010` | Pago Móvil | 8,00 | Bs. | 36,50 | 31/08/2026 23:15 | Completado |
| `TKT-100011` | Efectivo USD | 0,11 | USD | 36,50 | 31/08/2026 23:15 | Completado |