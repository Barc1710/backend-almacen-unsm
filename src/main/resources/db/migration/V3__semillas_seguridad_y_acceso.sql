-- =============================================================================
-- MIGRACIÓN V3: Semilla inicial de perfil y usuario administrador
-- =============================================================================

-- 1. Perfil administrativo base con ID estable
INSERT INTO perfil (id_perfil, nombreperfil, estadoperfil)
VALUES (1, 'ADMINISTRADOR', 1)
ON DUPLICATE KEY UPDATE
    nombreperfil = 'ADMINISTRADOR',
    estadoperfil = 1;

-- 2. Usuario administrador inicial: admin / admin123
INSERT INTO usuario (
    nombre, apellido, usuario, clave, estado, dni, telefono,
    idperfil_usuario, correo, direccion, debe_cambiar_clave
)
VALUES (
    'Administrador', 'Inicial', 'admin',
    '$2b$10$5qErEtr7YFNcilcjpOzSFewrs04QPI6RtOVxEcfl2Gmhfr4MEkenO',
    '1', NULL, NULL, 1, NULL, NULL, TRUE
)
ON DUPLICATE KEY UPDATE
    idperfil_usuario = 1,
    estado = '1',
    debe_cambiar_clave = TRUE;
