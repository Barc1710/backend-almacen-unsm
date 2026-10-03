-- =============================================================================
-- MIGRACIÓN REPETIBLE: R__01_semillas_modulos_y_permisos.sql
-- Catálogo oficial de 9 módulos y matriz de permisos base RBAC
-- Solo la familia 'Seguridad' agrupa subrutas (/seguridad/usuarios, /seguridad/perfiles)
-- =============================================================================

-- 1. CATÁLOGO OFICIAL DE 9 MÓDULOS
INSERT INTO modulo (id_modulo, codigo, nombre, url, icono, orden, estado) VALUES
    (1, 'DASHBOARD',   'Dashboard',           '/dashboard',          'layout-dashboard', 1, 1),
    (2, 'INVENTARIO',  'Inventario',          '/inventario',         'boxes',            2, 1),
    (3, 'INGRESOS',    'Ingresos',            '/ingresos',           'arrow-down-left',  3, 1),
    (4, 'EGRESOS',     'Egresos',             '/egresos',            'arrow-up-right',   4, 1),
    (5, 'KARDEX',      'Kardex',              '/kardex',             'clipboard-list',   5, 1),
    (6, 'PROVEEDORES', 'Proveedores',         '/proveedores',        'truck',            6, 1),
    (7, 'CLIENTES',    'Clientes',            '/clientes',           'users',            7, 1),
    (8, 'USUARIOS',    'Usuarios',            '/seguridad/usuarios', 'user-cog',         8, 1),
    (9, 'PERFILES',    'Perfiles y Permisos', '/seguridad/perfiles', 'shield-check',     9, 1)
ON DUPLICATE KEY UPDATE
    codigo = VALUES(codigo),
    nombre = VALUES(nombre),
    url    = VALUES(url),
    icono  = VALUES(icono),
    orden  = VALUES(orden),
    estado = VALUES(estado);

-- 2. PERFILES BASE GARANTIZADOS
INSERT INTO perfil (id_perfil, nombreperfil, estadoperfil) VALUES
    (1, 'ADMINISTRADOR', 1),
    (2, 'USUARIO', 1)
ON DUPLICATE KEY UPDATE
    nombreperfil = VALUES(nombreperfil),
    estadoperfil = 1;

-- 3. PERMISOS PARA ADMINISTRADOR (Acceso total a los 9 módulos)
INSERT INTO permiso (idperfil, idmodulo, estadopermiso)
SELECT p.id_perfil, m.id_modulo, 1
FROM perfil p
CROSS JOIN modulo m
WHERE UPPER(TRIM(p.nombreperfil)) LIKE '%ADMIN%' OR p.id_perfil = 1
ON DUPLICATE KEY UPDATE
    estadopermiso = VALUES(estadopermiso);

-- 4. PERMISOS PARA USUARIO (7 módulos operativos, excluye familia seguridad)
INSERT INTO permiso (idperfil, idmodulo, estadopermiso)
SELECT p.id_perfil, m.id_modulo, 1
FROM perfil p
CROSS JOIN modulo m
WHERE (UPPER(TRIM(p.nombreperfil)) LIKE '%USUARIO%' 
    OR UPPER(TRIM(p.nombreperfil)) LIKE '%OPERADOR%' 
    OR p.id_perfil = 2)
  AND UPPER(TRIM(p.nombreperfil)) NOT LIKE '%ADMIN%'
  AND p.id_perfil != 1
  AND m.url NOT LIKE '/seguridad%'
ON DUPLICATE KEY UPDATE
    estadopermiso = VALUES(estadopermiso);
