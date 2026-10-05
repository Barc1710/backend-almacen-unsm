CREATE TABLE kardex_movimiento (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_articulo      INT                                                                                                 NOT NULL,
    tipo_movimiento  ENUM('SALDO_INICIAL', 'INGRESO', 'EGRESO', 'REVERSO_EGRESO', 'REVERSO_INGRESO', 'AJUSTE', 'BAJA') NOT NULL,
    documento_tipo   VARCHAR(20)                                                                                         NOT NULL,
    documento_id     INT                                                                                                 NULL,
    cantidad_entrada DECIMAL(12, 2) DEFAULT 0.00                                                                         NOT NULL,
    cantidad_salida  DECIMAL(12, 2) DEFAULT 0.00                                                                         NOT NULL,
    saldo_resultante DECIMAL(12, 2)                                                                                      NOT NULL,
    id_usuario       INT                                                                                                 NOT NULL,
    fecha_hora       DATETIME       DEFAULT CURRENT_TIMESTAMP                                                            NOT NULL,
    CONSTRAINT fk_kardex_articulo FOREIGN KEY (id_articulo) REFERENCES articulo (id) ON UPDATE CASCADE,
    CONSTRAINT fk_kardex_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_kardex_articulo_fecha ON kardex_movimiento (id_articulo, fecha_hora);