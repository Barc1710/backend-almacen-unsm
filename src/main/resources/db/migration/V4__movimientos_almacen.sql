CREATE TABLE egreso (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente           INT                                   NULL,
    id_encargado         INT                                   NULL,
    id_area              INT                                   NULL,
    id_encargado_almacen INT                                   NOT NULL,
    id_usuario           INT                                   NULL,
    ambiente             VARCHAR(100)                          NULL,
    prefijo              VARCHAR(10) DEFAULT ''                NOT NULL,
    correlativo          INT         DEFAULT 0                 NOT NULL,
    tipo_egreso          VARCHAR(30) DEFAULT 'DESPACHO_ORDINARIO'        NOT NULL,
    motivo_baja          VARCHAR(255)                          NULL,
    fecha                DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado               CHAR(1)     DEFAULT '1'               NOT NULL,
    CONSTRAINT uq_egreso_prefijo_correlativo UNIQUE (prefijo, correlativo),
    CONSTRAINT fk_egreso_area FOREIGN KEY (id_area) REFERENCES area (id) ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_cliente FOREIGN KEY (id_cliente) REFERENCES cliente (id) ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_enc_almacen FOREIGN KEY (id_encargado_almacen) REFERENCES encargado_almacen (id) ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_encargado FOREIGN KEY (id_encargado) REFERENCES encargado (id) ON UPDATE CASCADE,
    CONSTRAINT fk_egreso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_egreso_numeracion ON egreso (prefijo, correlativo);

CREATE TABLE detalle_egreso (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_egreso   INT                                      NOT NULL,
    id_articulo INT                                      NOT NULL,
    cantidad    DECIMAL(12, 2)                           NOT NULL,
    precio      DECIMAL(12, 2) DEFAULT 0.00              NOT NULL,
    saldo       DECIMAL(12, 2)                           NOT NULL,
    fecha       DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo        CHAR(1)        DEFAULT 'e'               NOT NULL,
    CONSTRAINT fk_det_egreso_cabecera FOREIGN KEY (id_egreso) REFERENCES egreso (id) ON UPDATE CASCADE,
    CONSTRAINT fk_det_egreso_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ingreso (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor         INT                                   NOT NULL,
    id_usuario           INT                                   NULL,
    id_encargado_almacen INT                                   NULL,
    id_encargado         INT                                   NULL,
    prefijo              VARCHAR(10) DEFAULT ''                NOT NULL,
    correlativo          INT         DEFAULT 0                 NOT NULL,
    numero_orden_compra  VARCHAR(50)                           NULL,
    descripcion          VARCHAR(255)                          NULL,
    fecha                DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado               CHAR(1)     DEFAULT '1'               NOT NULL,
    CONSTRAINT uq_ingreso_orden_compra UNIQUE (numero_orden_compra),
    CONSTRAINT uq_ingreso_prefijo_correlativo UNIQUE (prefijo, correlativo),
    CONSTRAINT fk_ingreso_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor (id) ON UPDATE CASCADE,
    CONSTRAINT fk_ingreso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON UPDATE CASCADE,
    CONSTRAINT fk_ingreso_encargado_almacen FOREIGN KEY (id_encargado_almacen) REFERENCES encargado_almacen (id) ON UPDATE CASCADE,
    CONSTRAINT fk_ingreso_encargado FOREIGN KEY (id_encargado) REFERENCES encargado (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE detalle_ingreso (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_ingreso  INT                                      NOT NULL,
    id_articulo INT                                      NOT NULL,
    cantidad    DECIMAL(12, 2)                           NOT NULL,
    precio      DECIMAL(12, 2) DEFAULT 0.00              NOT NULL,
    saldo       DECIMAL(12, 2)                           NOT NULL,
    fecha       DATETIME       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tipo        CHAR(1)        DEFAULT 'i'               NOT NULL,
    CONSTRAINT fk_det_ingreso_cabecera FOREIGN KEY (id_ingreso) REFERENCES ingreso (id) ON UPDATE CASCADE,
    CONSTRAINT fk_det_ingreso_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;