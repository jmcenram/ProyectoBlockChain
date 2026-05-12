PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS entidad_emisora
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT    NOT NULL,
    private_key    TEXT    NOT NULL UNIQUE,
    address        TEXT    NOT NULL UNIQUE,
    activo         INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion TEXT    DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  TEXT
);

CREATE TABLE IF NOT EXISTS rol
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre         TEXT NOT NULL UNIQUE,
    descripcion    TEXT,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  TEXT
);

CREATE TABLE IF NOT EXISTS usuario
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre             TEXT    NOT NULL,
    email              TEXT    NOT NULL UNIQUE,
    password           TEXT    NOT NULL,
    entidad_emisora_id INTEGER,
    activo             INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1)),
    fecha_creacion     TEXT    DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado      TEXT,

    FOREIGN KEY (entidad_emisora_id) REFERENCES entidad_emisora (id)
);

CREATE TABLE IF NOT EXISTS usuario_rol
(
    usuario_id     INTEGER NOT NULL,
    rol_id         INTEGER NOT NULL,
    fecha_creacion TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  TEXT,

    PRIMARY KEY (usuario_id, rol_id),

    FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    FOREIGN KEY (rol_id) REFERENCES rol (id)
);

CREATE TABLE IF NOT EXISTS documento
(
    id                        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre                    TEXT NOT NULL,
    descripcion               TEXT,
    tipo                      TEXT,
    ruta_archivo              TEXT NOT NULL,
    hash                      TEXT,
    estado                    TEXT NOT NULL,
    contenido                 BLOB,
    fecha_creacion            TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_registro_blockchain TEXT,
    transaction_hash          TEXT,
    fecha_borrado             TEXT,
    emisor_id                 INTEGER NOT NULL,

    FOREIGN KEY (emisor_id) REFERENCES usuario (id)
);

CREATE TABLE IF NOT EXISTS registro_blockchain
(
    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
    documento_id       INTEGER NOT NULL,
    hash_documento     TEXT    NOT NULL,
    direccion_contrato TEXT,
    transaction_hash   TEXT,
    estado             TEXT    NOT NULL,
    bloque_number      INTEGER,
    fecha_creacion     TEXT    DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado      TEXT,

    FOREIGN KEY (documento_id) REFERENCES documento (id)
);

CREATE TABLE IF NOT EXISTS auditoria
(
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id     INTEGER,
    accion         TEXT NOT NULL,
    descripcion    TEXT,
    fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP,
    fecha_borrado  TEXT,

    FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
