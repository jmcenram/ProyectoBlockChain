-- CREACION DE LA TABLA DE ROLES
CREATE TABLE rol
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- CREACION DE LA TABLA DE USUARIO
CREATE TABLE usuario
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    rol_id              BIGINT       NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (rol_id)
            REFERENCES rol (id)
);