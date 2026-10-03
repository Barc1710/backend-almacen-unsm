-- =============================================================================
-- MIGRACIÓN V6: Asignación formal de Encargado de Almacén y Jefe a Ingreso
-- =============================================================================

-- 1. Agregar columnas de relación y snapshot inmutable a la tabla ingreso
ALTER TABLE ingreso 
    ADD COLUMN id_encargado_almacen INT NULL AFTER id_usuario,
    ADD COLUMN id_jefe INT NULL AFTER id_encargado_almacen,
    ADD COLUMN nombre_encargado_almacen VARCHAR(150) NULL AFTER id_jefe,
    ADD COLUMN nombre_jefe VARCHAR(150) NULL AFTER nombre_encargado_almacen;

-- 2. Restricciones de clave foránea hacia encargado_almacen y encargado
ALTER TABLE ingreso
    ADD CONSTRAINT fk_ingreso_encargado_almacen 
        FOREIGN KEY (id_encargado_almacen) REFERENCES encargado_almacen (id) 
        ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_ingreso_jefe 
        FOREIGN KEY (id_jefe) REFERENCES encargado (id) 
        ON DELETE RESTRICT ON UPDATE CASCADE;
