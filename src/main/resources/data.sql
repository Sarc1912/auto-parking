-- Datos iniciales. Se insertan solo la primera vez (BD en blanco).
-- Tarifa base
INSERT INTO tariff_configs (name, rate_per_hour, minimum_minutes, max_daily_rate, active)
SELECT 'Tarifa Estándar', 2.00, 30, 15.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM tariff_configs LIMIT 1);

-- Tasa de cambio BCV / paralela
INSERT INTO exchange_rates (bcv_rate, parallel_rate, updated_at)
SELECT 36.50, 38.20, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM exchange_rates LIMIT 1);

-- Tickets de demostración (varias horas de antigüedad, activos)
INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100001', DATEADD('HOUR', -1, CURRENT_TIMESTAMP), 'AB123CD', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100001');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100002', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 'XY789ZZ', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100002');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100003', DATEADD('HOUR', -8, CURRENT_TIMESTAMP), 'JKL456MN', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100003');

-- ---------------------------------------------------------------
-- SEEDS DE DEMOSTRACIÓN (prueba del rediseño y los reportes)
-- ---------------------------------------------------------------

-- Tarifa inactiva de ejemplo (para probar la edición en el panel)
INSERT INTO tariff_configs (name, rate_per_hour, minimum_minutes, max_daily_rate, active)
SELECT 'Tarifa Premium', 3.50, 15, 25.00, FALSE
WHERE NOT EXISTS (SELECT 1 FROM tariff_configs WHERE name = 'Tarifa Premium');

-- Historial de tasas de cambio (los últimos días)
INSERT INTO exchange_rates (bcv_rate, parallel_rate, updated_at)
SELECT 35.80, 37.40, DATEADD('DAY', -1, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM exchange_rates WHERE bcv_rate = 35.80 AND parallel_rate = 37.40);

INSERT INTO exchange_rates (bcv_rate, parallel_rate, updated_at)
SELECT 34.90, 36.60, DATEADD('DAY', -2, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM exchange_rates WHERE bcv_rate = 34.90 AND parallel_rate = 36.60);

INSERT INTO exchange_rates (bcv_rate, parallel_rate, updated_at)
SELECT 34.10, 35.70, DATEADD('DAY', -3, CURRENT_TIMESTAMP)
WHERE NOT EXISTS (SELECT 1 FROM exchange_rates WHERE bcv_rate = 34.10 AND parallel_rate = 35.70);

-- Ticket activo con más de un día: demuestra el tope diario (15.00 Bs)
INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100004', DATEADD('HOUR', -26, CURRENT_TIMESTAMP), 'GH567JK', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100004');

-- Tickets ya pagados (población de los reportes)
INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100005', DATEADD('HOUR', -75, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -72, CURRENT_TIMESTAMP), 'AA111BB', 6.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100005');

INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100006', DATEADD('HOUR', -73, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -72, CURRENT_TIMESTAMP), 'CC222DD', 2.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100006');

INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100007', DATEADD('HOUR', -50, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -48, CURRENT_TIMESTAMP), 'EE333FF', 4.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100007');

INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100008', DATEADD('HOUR', -53, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -48, CURRENT_TIMESTAMP), 'GG444HH', 10.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100008');

INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100010', DATEADD('HOUR', -28, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'II555JJ', 8.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100010');

INSERT INTO parking_tickets (code, entry_time, exit_time, license_plate, amount_due, status)
SELECT 'TKT-100011', DATEADD('HOUR', -26, CURRENT_TIMESTAMP),
       DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'KK666LL', 4.00, 'PAID'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100011');

-- Ticket cancelado (dato de control para el panel)
INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100009', DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'MM777NN', NULL, 'CANCELLED'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100009');

-- Tickets activos adicionales (para probar el flujo de pago del kiosco)
INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100012', DATEADD('HOUR', -2, CURRENT_TIMESTAMP), 'PQ101RS', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100012');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100013', DATEADD('HOUR', -5, CURRENT_TIMESTAMP), 'ST202TU', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100013');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100014', DATEADD('HOUR', -7, CURRENT_TIMESTAMP), 'UV303VW', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100014');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100015', DATEADD('HOUR', -12, CURRENT_TIMESTAMP), 'WX404XY', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100015');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100016', DATEADD('HOUR', -16, CURRENT_TIMESTAMP), 'YZ505ZA', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100016');

INSERT INTO parking_tickets (code, entry_time, license_plate, amount_due, status)
SELECT 'TKT-100017', DATEADD('HOUR', -20, CURRENT_TIMESTAMP), 'AB606BC', NULL, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM parking_tickets WHERE code = 'TKT-100017');

-- Registros de pago (reportes con métodos y monedas variadas)
INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'MOBILE_PAY', 6.00, 'VES', 34.90, DATEADD('HOUR', -72, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100005'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100005'));

INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'CARD_POS', 2.00, 'VES', 34.90, DATEADD('HOUR', -72, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100006'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100006'));

INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'CASH_VES', 4.00, 'VES', 35.80, DATEADD('HOUR', -48, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100007'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100007'));

INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'CASH_VES', 10.00, 'VES', 35.80, DATEADD('HOUR', -48, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100008'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100008'));

INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'MOBILE_PAY', 8.00, 'VES', 36.50, DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100010'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100010'));

INSERT INTO payment_records (ticket_id, payment_method, amount, currency, exchange_rate_used, timestamp, status)
SELECT id, 'CASH_USD', 0.11, 'USD', 36.50, DATEADD('HOUR', -24, CURRENT_TIMESTAMP), 'COMPLETED'
FROM parking_tickets WHERE code = 'TKT-100011'
AND NOT EXISTS (SELECT 1 FROM payment_records pr WHERE pr.ticket_id =
    (SELECT id FROM parking_tickets WHERE code = 'TKT-100011'));
