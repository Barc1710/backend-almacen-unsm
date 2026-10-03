-- =============================================================================
-- MIGRACIÓN V4: Numeración correlativa anual para Ingresos y Estandarización
-- =============================================================================

-- 1. Incorporar prefijo y correlativo a la tabla ingreso (estándar anual: I26-0001)
ALTER TABLE ingreso 
    ADD COLUMN prefijo VARCHAR(10) DEFAULT '' NOT NULL AFTER id_proveedor,
    ADD COLUMN correlativo INT DEFAULT 0 NOT NULL AFTER prefijo;

-- 2. Asignar prefijo anual y correlativo correlacionado a ingresos existentes
UPDATE ingreso 
SET prefijo = CONCAT('I', DATE_FORMAT(fecha, '%y')), 
    correlativo = id 
WHERE correlativo = 0;

-- 3. Crear índice y restricción de unicidad para la numeración de ingresos
ALTER TABLE ingreso
    ADD CONSTRAINT uq_ingreso_prefijo_correlativo UNIQUE (prefijo, correlativo);
