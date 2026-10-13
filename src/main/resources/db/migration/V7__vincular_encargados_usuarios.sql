-- ==============================================================================
-- V7: Vincular Encargados y Encargados de Almacén con Cuentas de Usuario
-- ==============================================================================

ALTER TABLE encargado
    ADD COLUMN id_usuario INT NULL,
    ADD CONSTRAINT uq_encargado_usuario UNIQUE (id_usuario),
    ADD CONSTRAINT fk_encargado_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE encargado_almacen
    ADD COLUMN id_usuario INT NULL,
    ADD CONSTRAINT uq_encargado_almacen_usuario UNIQUE (id_usuario),
    ADD CONSTRAINT fk_encargado_almacen_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL;
