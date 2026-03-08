CREATE TABLE rol
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50) NOT NULL UNIQUE,
    descripcion    VARCHAR(255),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  DATETIME NULL
);

CREATE TABLE usuario
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    activo         BOOLEAN  DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  DATETIME NULL
);

CREATE TABLE usuario_rol
(
    usuario_id     BIGINT NOT NULL,
    rol_id         BIGINT NOT NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  DATETIME NULL,

    PRIMARY KEY (usuario_id, rol_id),

    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (rol_id) REFERENCES rol (id)
);

CREATE TABLE documento
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre                    VARCHAR(255) NOT NULL,
    descripcion               TEXT,
    tipo                      VARCHAR(100),
    ruta_archivo              VARCHAR(500) NOT NULL,
    hash                      VARCHAR(64)  NOT NULL,
    estado                    VARCHAR(30)  NOT NULL,
    fecha_creacion            DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_registro_blockchain DATETIME NULL,
    transaction_hash          VARCHAR(100) NULL,
    fecha_borrado             DATETIME NULL,

    emisor_id                 BIGINT       NOT NULL,

    FOREIGN KEY (emisor_id) REFERENCES usuario (id)
);

CREATE TABLE documento
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre                    VARCHAR(255) NOT NULL,
    descripcion               TEXT,
    tipo                      VARCHAR(100),
    ruta_archivo              VARCHAR(500) NOT NULL,
    hash                      VARCHAR(64)  NOT NULL,
    estado                    VARCHAR(30)  NOT NULL,
    fecha_creacion            DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_registro_blockchain DATETIME NULL,
    transaction_hash          VARCHAR(100) NULL,
    fecha_borrado             DATETIME NULL,

    emisor_id                 BIGINT       NOT NULL,

    FOREIGN KEY (emisor_id) REFERENCES usuario (id)
);

CREATE TABLE registro_blockchain
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    documento_id       BIGINT       NOT NULL,
    hash_documento     VARCHAR(64)  NOT NULL,
    direccion_contrato VARCHAR(255) NOT NULL,
    transaction_hash   VARCHAR(100) NOT NULL,
    bloque_number      BIGINT,
    fecha_creacion     DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado      DATETIME NULL,

    FOREIGN KEY (documento_id) REFERENCES documento (id)
);

CREATE TABLE auditoria
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id    BIGINT,
    accion        VARCHAR(100) NOT NULL,
    descripcion   TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado DATETIME NULL,

    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE TABLE api_key
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    clave          VARCHAR(255) NOT NULL UNIQUE,
    activa         BOOLEAN  DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    descripcion    VARCHAR(255),
    fecha_borrado  DATETIME NULL
);