-- =============================================================================
-- MIGRACIÓN V1: Esquema Base Saneado para Almacén UNSM 
-- =============================================================================

-- 1. Catálogos Básicos
CREATE TABLE area (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ubicacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(150) NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE marca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE familia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    inicial VARCHAR(10) NOT NULL,
    correlativo INT DEFAULT 1 NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE correlativo_familia (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_familia INT NOT NULL,
    correlativo INT DEFAULT 1 NOT NULL,
    CONSTRAINT fk_correlativo_familia FOREIGN KEY (id_familia) 
        REFERENCES familia (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Actores y Personas
CREATE TABLE encargado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sigla_profesion VARCHAR(20) NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni CHAR(8) NULL,
    ambiente VARCHAR(100) NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE encargado_almacen (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    dni VARCHAR(15) NULL,
    telefono VARCHAR(50) NULL,
    celular VARCHAR(50) NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE proveedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(20) NULL,
    razon_social VARCHAR(150) NOT NULL,
    telefono VARCHAR(50) NULL,
    celular VARCHAR(50) NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    contacto VARCHAR(100) NULL,
    banco VARCHAR(50) NULL,
    cuenta_corriente VARCHAR(50) NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Seguridad y Usuarios
CREATE TABLE perfil (
    id_perfil INT AUTO_INCREMENT PRIMARY KEY,
    nombreperfil VARCHAR(100) NOT NULL,
    estadoperfil TINYINT DEFAULT 1 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE modulo (
    id_modulo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    url VARCHAR(150) NULL,
    icono VARCHAR(50) NULL,
    orden INT DEFAULT 0,
    estado TINYINT DEFAULT 1 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permiso (
    id_permiso INT AUTO_INCREMENT PRIMARY KEY,
    idperfil INT NOT NULL,
    idmodulo INT NOT NULL,
    estadopermiso TINYINT DEFAULT 1 NOT NULL,
    CONSTRAINT fk_permiso_perfil FOREIGN KEY (idperfil) REFERENCES perfil (id_perfil) ON DELETE CASCADE,
    CONSTRAINT fk_permiso_modulo FOREIGN KEY (idmodulo) REFERENCES modulo (id_modulo) ON DELETE CASCADE,
    UNIQUE KEY uq_perfil_modulo (idperfil, idmodulo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL,
    dni CHAR(8) NULL,
    telefono VARCHAR(20) NULL,
    idperfil_usuario INT NOT NULL,
    correo VARCHAR(100) NULL,
    direccion VARCHAR(255) NULL,
    debe_cambiar_clave BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (idperfil_usuario) 
        REFERENCES perfil (id_perfil) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Artículos 
CREATE TABLE articulo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    id_familia INT NOT NULL,
    id_marca INT NOT NULL,
    id_ubicacion INT NOT NULL,
    saldo DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    cantidad_minima DECIMAL(12, 2) DEFAULT 10.00 NOT NULL,
    precio DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    activo TINYINT(1) DEFAULT 1 NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL,
    detalle VARCHAR(255) DEFAULT '',
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_articulo_codigo UNIQUE (codigo),
    CONSTRAINT fk_articulo_familia FOREIGN KEY (id_familia) REFERENCES familia (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_articulo_marca FOREIGN KEY (id_marca) REFERENCES marca (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_articulo_ubicacion FOREIGN KEY (id_ubicacion) REFERENCES ubicacion (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Transacciones de Ingreso y Egreso 
CREATE TABLE ingreso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT NOT NULL,
    descripcion VARCHAR(255) NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL,
    CONSTRAINT fk_ingreso_proveedor FOREIGN KEY (id_proveedor) 
        REFERENCES proveedor (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_ingreso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_ingreso INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad DECIMAL(12, 2) NOT NULL,
    precio DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    saldo DECIMAL(12, 2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo CHAR(1) DEFAULT 'i' NOT NULL,
    CONSTRAINT fk_det_ingreso_cabecera FOREIGN KEY (id_ingreso) REFERENCES ingreso (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_det_ingreso_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE egreso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_encargado INT NOT NULL,
    id_area INT NOT NULL,
    id_encargado_almacen INT NOT NULL,
    ambiente VARCHAR(100) NULL,
    prefijo VARCHAR(10) DEFAULT '' NOT NULL,
    correlativo INT DEFAULT 0 NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL,
    CONSTRAINT fk_egreso_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_encargado FOREIGN KEY (id_encargado) REFERENCES encargado (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_area FOREIGN KEY (id_area) REFERENCES area (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_enc_almacen FOREIGN KEY (id_encargado_almacen) REFERENCES encargado_almacen (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_egreso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_egreso INT NOT NULL,
    id_articulo INT NOT NULL,
    cantidad DECIMAL(12, 2) NOT NULL,
    precio DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    saldo DECIMAL(12, 2) NOT NULL,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo CHAR(1) DEFAULT 'e' NOT NULL,
    CONSTRAINT fk_det_egreso_cabecera FOREIGN KEY (id_egreso) REFERENCES egreso (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_det_egreso_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Kardex Unificado 
CREATE TABLE kardex_movimiento (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_articulo INT NOT NULL,
    tipo_movimiento ENUM('SALDO_INICIAL', 'INGRESO', 'EGRESO', 'REVERSO_EGRESO', 'AJUSTE') NOT NULL,
    documento_tipo VARCHAR(20) NOT NULL,
    documento_id INT NULL,
    cantidad_entrada DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    cantidad_salida DECIMAL(12, 2) DEFAULT 0.00 NOT NULL,
    saldo_resultante DECIMAL(12, 2) NOT NULL,
    id_usuario INT NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_kardex_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_kardex_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kardex_articulo_fecha ON kardex_movimiento (id_articulo, fecha_hora);
CREATE INDEX idx_egreso_numeracion ON egreso (prefijo, correlativo);