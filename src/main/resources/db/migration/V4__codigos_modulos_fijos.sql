-- Los códigos vinculan las pantallas existentes con la autorización de sus API.
-- Se conservan los IDs de módulos ya cargados para no perder permisos asignados.
ALTER TABLE modulo ADD COLUMN codigo VARCHAR(50) NULL;

UPDATE modulo
SET codigo = CASE
    WHEN LOWER(TRIM(url)) IN ('/dashboard', '/modulo/dashboard') OR UPPER(TRIM(nombre)) = 'DASHBOARD' THEN 'DASHBOARD'
    WHEN LOWER(TRIM(url)) IN ('/articulos', '/modulo/articulos') OR UPPER(TRIM(nombre)) IN ('ARTICULOS', 'ARTÍCULOS') THEN 'ARTICULOS'
    WHEN LOWER(TRIM(url)) IN ('/ingresos', '/modulo/ingresos') OR UPPER(TRIM(nombre)) = 'INGRESOS' THEN 'INGRESOS'
    WHEN LOWER(TRIM(url)) IN ('/egresos', '/modulo/egresos') OR UPPER(TRIM(nombre)) = 'EGRESOS' THEN 'EGRESOS'
    WHEN LOWER(TRIM(url)) IN ('/kardex', '/kardex/general', '/modulo/kardex') OR UPPER(TRIM(nombre)) = 'KARDEX' THEN 'KARDEX'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/proveedores', '/modulo/proveedores') OR UPPER(TRIM(nombre)) = 'PROVEEDORES' THEN 'PROVEEDORES'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/clientes', '/modulo/clientes') OR UPPER(TRIM(nombre)) = 'CLIENTES' THEN 'CLIENTES'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/areas', '/modulo/areas') OR UPPER(TRIM(nombre)) IN ('AREAS', 'ÁREAS') THEN 'AREAS'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/familias', '/modulo/familias') OR UPPER(TRIM(nombre)) = 'FAMILIAS' THEN 'FAMILIAS'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/marcas', '/modulo/marcas') OR UPPER(TRIM(nombre)) = 'MARCAS' THEN 'MARCAS'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/ubicaciones', '/modulo/ubicaciones') OR UPPER(TRIM(nombre)) = 'UBICACIONES' THEN 'UBICACIONES'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/encargados', '/modulo/encargados') OR UPPER(TRIM(nombre)) = 'ENCARGADOS' THEN 'ENCARGADOS'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/encargados-almacen', '/modulo/encargados-almacen') OR UPPER(TRIM(nombre)) IN ('ENCARGADOS ALMACÉN', 'ENCARGADOS ALMACEN') THEN 'ENCARGADOS_ALMACEN'
    WHEN LOWER(TRIM(url)) IN ('/mantenimiento/unidades-medida', '/modulo/unidades-medida') OR UPPER(TRIM(nombre)) IN ('UNIDADES DE MEDIDA', 'UNIDADES MEDIDA') THEN 'UNIDADES_MEDIDA'
    ELSE NULL
END;

ALTER TABLE modulo ADD CONSTRAINT uq_modulo_codigo UNIQUE (codigo);

INSERT INTO modulo (codigo, nombre, url, icono, orden, estado) VALUES
    ('DASHBOARD', 'Dashboard', '/dashboard', 'dashboard', 1, 1),
    ('ARTICULOS', 'Artículos', '/articulos', 'inventory', 2, 1),
    ('INGRESOS', 'Ingresos', '/ingresos', 'input', 3, 1),
    ('EGRESOS', 'Egresos', '/egresos', 'output', 4, 1),
    ('KARDEX', 'Kardex', '/kardex/general', 'history', 5, 1),
    ('PROVEEDORES', 'Proveedores', '/mantenimiento/proveedores', 'business', 6, 1),
    ('CLIENTES', 'Clientes', '/mantenimiento/clientes', 'people', 7, 1),
    ('AREAS', 'Áreas', '/mantenimiento/areas', 'account_tree', 8, 1),
    ('FAMILIAS', 'Familias', '/mantenimiento/familias', 'category', 9, 1),
    ('MARCAS', 'Marcas', '/mantenimiento/marcas', 'sell', 10, 1),
    ('UBICACIONES', 'Ubicaciones', '/mantenimiento/ubicaciones', 'warehouse', 11, 1),
    ('ENCARGADOS', 'Encargados', '/mantenimiento/encargados', 'badge', 12, 1),
    ('ENCARGADOS_ALMACEN', 'Encargados de almacén', '/mantenimiento/encargados-almacen', 'manage_accounts', 13, 1),
    ('UNIDADES_MEDIDA', 'Unidades de medida', '/mantenimiento/unidades-medida', 'straighten', 14, 1)
ON DUPLICATE KEY UPDATE codigo = VALUES(codigo);
