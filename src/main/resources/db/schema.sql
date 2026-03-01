-- INSERCION DE LOS DATOS INICIALES DE ROL
INSERT INTO rol (nombre, descripcion)
VALUES ('ADMIN', 'Administrador del sistema con acceso completo'),
       ('EMISOR', 'Usuario autorizado para emitir documentos académicos'),
       ('VERIFICADOR', 'Usuario que puede verificar documentos');