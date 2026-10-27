-- Datos de ejemplo para Rinconcito Marino
-- Inserta 50 reservas y 50 opiniones.
-- Compatible con MySQL 8+.

INSERT INTO reservas (
    nombres,
    apellidos,
    dni,
    edad,
    email,
    telefono,
    personas,
    fecha,
    hora,
    estado,
    fecha_registro
)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT
    ELT(MOD(n - 1, 20) + 1,
        'Luis', 'Maria', 'Carlos', 'Ana', 'Jorge',
        'Lucia', 'Pedro', 'Rosa', 'Miguel', 'Valeria',
        'Diego', 'Camila', 'Fernando', 'Daniela', 'Ricardo',
        'Sofia', 'Alonso', 'Paola', 'Hector', 'Andrea'
    ) AS nombres,
    ELT(MOD(n - 1, 20) + 1,
        'Ramirez', 'Torres', 'Flores', 'Castillo', 'Mendoza',
        'Vargas', 'Rojas', 'Silva', 'Chavez', 'Morales',
        'Herrera', 'Campos', 'Navarro', 'Paredes', 'Salazar',
        'Cruz', 'Reyes', 'Aguilar', 'Leon', 'Mejia'
    ) AS apellidos,
    LPAD(70000000 + n, 8, '0') AS dni,
    18 + MOD(n, 38) AS edad,
    CONCAT('cliente', LPAD(n, 2, '0'), '@demo.com') AS email,
    CONCAT('9', LPAD(10000000 + n, 8, '0')) AS telefono,
    1 + MOD(n, 8) AS personas,
    DATE_ADD(CURDATE(), INTERVAL MOD(n, 21) DAY) AS fecha,
    ELT(MOD(n - 1, 8) + 1, '12:00', '12:30', '13:00', '13:30', '19:00', '19:30', '20:00', '20:30') AS hora,
    ELT(MOD(n - 1, 7) + 1, 'Pendiente', 'Confirmado', 'Atendido', 'No asistio', 'Cancelado por cliente', 'Rechazado', 'Expirado') AS estado,
    DATE_SUB(NOW(), INTERVAL n HOUR) AS fecha_registro
FROM seq;

INSERT INTO opiniones (
    nombres,
    correo,
    comentario,
    visible,
    destacado,
    fecha_registro
)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT
    ELT(MOD(n - 1, 20) + 1,
        'Luis Ramirez', 'Maria Torres', 'Carlos Flores', 'Ana Castillo', 'Jorge Mendoza',
        'Lucia Vargas', 'Pedro Rojas', 'Rosa Silva', 'Miguel Chavez', 'Valeria Morales',
        'Diego Herrera', 'Camila Campos', 'Fernando Navarro', 'Daniela Paredes', 'Ricardo Salazar',
        'Sofia Cruz', 'Alonso Reyes', 'Paola Aguilar', 'Hector Leon', 'Andrea Mejia'
    ) AS nombres,
    CONCAT('opinion', LPAD(n, 2, '0'), '@demo.com') AS correo,
    ELT(MOD(n - 1, 10) + 1,
        'Excelente atencion y comida muy fresca. Volveria con mi familia.',
        'La reserva fue rapida y el personal atendio con mucha amabilidad.',
        'Muy buen ceviche, buen ambiente y precios claros.',
        'El chicharron estuvo excelente y el servicio fue ordenado.',
        'Me gusto la rapidez de la atencion y la limpieza del local.',
        'Buena experiencia, la comida llego caliente y bien servida.',
        'El equipo fue amable y resolvio mis dudas sobre la reserva.',
        'Recomiendo el restaurante por la calidad y el trato al cliente.',
        'La carta es clara, el local agradable y la atencion puntual.',
        'Todo estuvo correcto, desde la reserva hasta la atencion en mesa.'
    ) AS comentario,
    CASE WHEN MOD(n, 9) = 0 THEN FALSE ELSE TRUE END AS visible,
    CASE WHEN MOD(n, 10) = 0 THEN TRUE ELSE FALSE END AS destacado,
    DATE_SUB(NOW(), INTERVAL n DAY) AS fecha_registro
FROM seq;
