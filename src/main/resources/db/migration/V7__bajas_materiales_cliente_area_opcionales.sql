-- =============================================================================
-- MIGRACIÓN V7: Flexibilización de Cliente y Área para Bajas de Materiales
-- =============================================================================
-- En los despachos ordinarios, cliente y área son obligatorios a nivel de negocio.
-- En las bajas por deterioro o vencimiento, no existe un receptor externo, por lo que
-- las columnas id_cliente e id_area permiten valores nulos.
ALTER TABLE egreso 
    MODIFY COLUMN id_cliente INT NULL,
    MODIFY COLUMN id_area INT NULL,
    ADD COLUMN motivo_baja VARCHAR(255) NULL AFTER tipo_egreso;
