-- 1. PERFILES BASE
INSERT INTO perfil (id_perfil, nombreperfil, estadoperfil) VALUES 
(1, 'ADMINISTRADOR', 1),
(2, 'USUARIO', 1);

-- 2. MÓDULOS DEL SISTEMA
INSERT INTO modulo (id_modulo, codigo, nombre, url, icono, orden, estado) VALUES
(1,  'DASHBOARD',            'Dashboard',            '/dashboard',            'layout-dashboard', 1, 1),
(2,  'INVENTARIO_ARTICULOS', 'Artículos',            '/inventario/articulos', 'package',          2, 1),
(3,  'INVENTARIO_FAMILIAS',  'Familias',             '/inventario/familias',  'layers',           3, 1),
(4,  'INVENTARIO_MARCAS',    'Marcas',               '/inventario/marcas',    'tags',             4, 1),
(5,  'INGRESOS',             'Ingresos',             '/ingresos',             'arrow-down-left',  5, 1),
(6,  'EGRESOS',              'Egresos',              '/egresos',              'arrow-up-right',   6, 1),
(7,  'KARDEX',               'Kardex',               '/kardex',               'clipboard-list',   7, 1),
(8,  'PROVEEDORES',          'Proveedores',          '/proveedores',          'truck',            8, 1),
(9,  'CLIENTES',             'Clientes',             '/clientes',             'building-2',       9, 1),
(10, 'SEGURIDAD_USUARIOS',   'Usuarios',             '/seguridad/usuarios',   'users',            10, 1),
(11, 'SEGURIDAD_PERFILES',   'Perfiles y Permisos',  '/seguridad/perfiles',   'shield-check',     11, 1),
(12, 'ENCARGADOS',           'Encargados',           '/encargados',           'user-check',       12, 1);

-- 3. PERMISOS
INSERT INTO permiso (id_permiso, idperfil, idmodulo, estadopermiso) VALUES
(1,  1, 1, 1),
(2,  1, 2, 1),
(3,  1, 3, 1),
(4,  1, 4, 1),
(5,  1, 5, 1),
(6,  1, 6, 1),
(7,  1, 7, 1),
(8,  1, 8, 1),
(9,  1, 9, 1),
(10, 1, 10, 1),
(11, 1, 11, 1),
(12, 1, 12, 1),
(13, 2, 1, 1),
(14, 2, 2, 1),
(15, 2, 3, 1),
(16, 2, 4, 1),
(17, 2, 5, 1),
(18, 2, 6, 1),
(19, 2, 7, 1),
(20, 2, 8, 1),
(21, 2, 9, 1);

-- 4. ÁREAS BASE
INSERT INTO area (id, nombre, estado) VALUES 
(1,  'LIMPIEZA',      '1'),
(2,  'REFRIGERACION', '1'),
(3,  'ELECTRICIDAD',  '1'),
(4,  'ALBAÑILERIA',   '1'),
(5,  'GASFITERIA',    '1'),
(6,  'CARPINTERIA',   '1'),
(7,  'MECANICA',      '1'),
(8,  'JARDINERIA',    '1'),
(9,  'SOLDADURA',     '1'),
(10, 'TRANSPORTE',    '1'),
(11, 'ALMACEN',       '1'),
(12, 'SECRETARIA',    '1'),
(13, 'VIGILANCIA',    '1');

-- 5. UBICACIONES FÍSICAS
INSERT INTO ubicacion (id, nombre, descripcion, estado) VALUES 
(1, 'Almacén 1', 'Almacén 1', '1'),
(2, 'Almacén 2', 'Almacén 2', '1'),
(3, 'Almacén 3', 'Almacén 3', '1');

-- 6. UNIDADES DE MEDIDA
INSERT INTO unidad_medida (id, codigo_sunat, nombre, simbolo, permite_decimales, estado) VALUES
(1,  'NIU', 'Unidad',         'und',  0, '1'),
(2,  'KGM', 'Kilogramo',      'kg',   1, '1'),
(3,  'LTR', 'Litro',          'L',    1, '1'),
(4,  'GLL', 'Galón',          'gal',  1, '1'),
(5,  'MTR', 'Metro',          'm',    1, '1'),
(6,  'MTK', 'Metro Cuadrado', 'm²',   1, '1'),
(7,  'P2',  'Pie Cuadrado',   'p²',   1, '1'),
(8,  'BX',  'Caja',           'caja', 0, '1'),
(9,  'PK',  'Paquete',        'paq',  0, '1'),
(10, 'BG',  'Bolsa',          'bls',  0, '1'),
(11, 'SET', 'Juego',          'jgo',  0, '1'),
(12, 'DZN', 'Docena',         'doc',  0, '1'),
(13, 'MIL', 'Millar',         'mil',  0, '1'),
(14, 'RLL', 'Rollo',          'rll',  0, '1');