-- =============================================================================
-- MIGRACIÓN V5: Auditoría de usuario receptor y soporte para anulación de ingreso
-- =============================================================================

-- 1. Agregar columna id_usuario a la tabla ingreso (nullable para preservar histórico sin alterar datos previos)
ALTER TABLE ingreso 
    ADD COLUMN id_usuario INT NULL AFTER id_proveedor;

ALTER TABLE ingreso
    ADD CONSTRAINT fk_ingreso_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 2. Incorporar REVERSO_INGRESO al ENUM tipo_movimiento en kardex_movimiento
ALTER TABLE kardex_movimiento 
    MODIFY COLUMN tipo_movimiento ENUM('SALDO_INICIAL', 'INGRESO', 'EGRESO', 'REVERSO_EGRESO', 'REVERSO_INGRESO', 'AJUSTE', 'BAJA_DETERIORO', 'BAJA_VENCIMIENTO') NOT NULL;
