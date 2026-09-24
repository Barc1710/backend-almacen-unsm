-- =============================================================================
-- MIGRACIÓN V2: Unidades de Medida, Trazabilidad de Compras, Egresos y Kardex
-- =============================================================================

-- 1. Catálogo de Unidades de Medida (Estándar SUNAT / OSCE Catálogo 03)
CREATE TABLE unidad_medida (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_sunat VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    simbolo VARCHAR(10) NOT NULL,
    permite_decimales BOOLEAN DEFAULT FALSE NOT NULL,
    estado CHAR(1) DEFAULT '1' NOT NULL,
    CONSTRAINT uq_unidad_medida_codigo UNIQUE (codigo_sunat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Precarga de magnitudes físicas y de conteo estándar formal
INSERT INTO unidad_medida (codigo_sunat, nombre, simbolo, permite_decimales, estado) VALUES
('NIU', 'Unidad', 'und', FALSE, '1'),
('KGM', 'Kilogramo', 'kg', TRUE, '1'),
('LTR', 'Litro', 'L', TRUE, '1'),
('GLL', 'Galón', 'gal', TRUE, '1'),
('MTR', 'Metro', 'm', TRUE, '1'),
('MTK', 'Metro Cuadrado', 'm²', TRUE, '1'),
('P2', 'Pie Cuadrado', 'p²', TRUE, '1');

-- Enlace de unidad de medida en artículo (nullable)
ALTER TABLE articulo 
    ADD COLUMN id_unidad_medida INT NULL AFTER descripcion,
    ADD CONSTRAINT fk_articulo_unidad_medida FOREIGN KEY (id_unidad_medida) 
        REFERENCES unidad_medida (id) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 2. Trazabilidad de Compras y Anti-duplicidad en Ingreso
ALTER TABLE ingreso 
    ADD COLUMN numero_orden_compra VARCHAR(50) NULL AFTER id_proveedor,
    ADD CONSTRAINT uq_ingreso_orden_compra UNIQUE (numero_orden_compra);

-- 3. Auditoría de Usuario, Responsables y Numeración Única en Egreso
ALTER TABLE egreso 
    MODIFY COLUMN id_encargado INT NULL,
    ADD COLUMN nombre_encargado_libre VARCHAR(150) NULL AFTER id_encargado,
    ADD COLUMN tipo_egreso VARCHAR(30) DEFAULT 'DESPACHO_ORDINARIO' NOT NULL AFTER correlativo,
    ADD COLUMN id_usuario INT NULL AFTER id_encargado_almacen,
    ADD CONSTRAINT fk_egreso_usuario FOREIGN KEY (id_usuario) 
        REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT uq_egreso_prefijo_correlativo UNIQUE (prefijo, correlativo);

-- 4. Soporte para Encargado Titular en Encargado de Almacén
ALTER TABLE encargado_almacen 
    ADD COLUMN es_titular BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN cargo VARCHAR(100) DEFAULT 'Encargado de Almacén' NULL;

-- 5. Ampliación del Enum de Kardex para Bajas y Mermas
ALTER TABLE kardex_movimiento 
    MODIFY COLUMN tipo_movimiento ENUM('SALDO_INICIAL', 'INGRESO', 'EGRESO', 'REVERSO_EGRESO', 'AJUSTE', 'BAJA_DETERIORO', 'BAJA_VENCIMIENTO') NOT NULL;

-- 6. Limpieza Estructural de Tablas Obsoletas
DROP TABLE IF EXISTS correlativo_familia;
