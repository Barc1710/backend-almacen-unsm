CREATE TABLE area (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100)     NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ubicacion (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)     NOT NULL,
    descripcion VARCHAR(150)     NULL,
    estado      CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE unidad_medida (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    codigo_sunat      VARCHAR(10)            NOT NULL,
    nombre            VARCHAR(100)           NOT NULL,
    simbolo           VARCHAR(10)            NOT NULL,
    permite_decimales TINYINT(1) DEFAULT 0   NOT NULL,
    estado            CHAR(1)    DEFAULT '1' NOT NULL,
    CONSTRAINT uq_unidad_medida_codigo UNIQUE (codigo_sunat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE familia (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100)     NOT NULL,
    inicial     VARCHAR(10)      NOT NULL,
    correlativo INT DEFAULT 1    NOT NULL,
    estado      CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE marca (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100)     NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE encargado (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    sigla_profesion VARCHAR(20)      NULL,
    nombres         VARCHAR(100)     NOT NULL,
    apellidos       VARCHAR(100)     NOT NULL,
    dni             CHAR(8)          NULL,
    cargo           VARCHAR(100)     NULL,
    estado          CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE encargado_almacen (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(150)           NOT NULL,
    estado     CHAR(1)    DEFAULT '1' NOT NULL,
    es_titular TINYINT(1) DEFAULT 0   NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cliente (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(150)     NOT NULL,
    dni       VARCHAR(15)      NULL,
    telefono  VARCHAR(50)      NULL,
    correo    VARCHAR(100)     NULL,
    direccion VARCHAR(255)     NULL,
    estado    CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE proveedor (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    ruc              VARCHAR(20)      NULL,
    razon_social     VARCHAR(150)     NOT NULL,
    telefono         VARCHAR(50)      NULL,
    correo           VARCHAR(100)     NULL,
    direccion        VARCHAR(255)     NULL,
    contacto         VARCHAR(100)     NULL,
    banco            VARCHAR(50)      NULL,
    cuenta_corriente VARCHAR(50)      NULL,
    estado           CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;