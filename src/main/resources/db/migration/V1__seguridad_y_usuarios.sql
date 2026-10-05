CREATE TABLE perfil (
    id_perfil    INT AUTO_INCREMENT PRIMARY KEY,
    nombreperfil VARCHAR(100)      NOT NULL,
    estadoperfil TINYINT DEFAULT 1 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE modulo (
    id_modulo INT AUTO_INCREMENT PRIMARY KEY,
    codigo    VARCHAR(50)       NOT NULL,
    nombre    VARCHAR(100)      NOT NULL,
    url       VARCHAR(150)      NULL,
    icono     VARCHAR(50)       NULL,
    orden     INT     DEFAULT 0 NULL,
    estado    TINYINT DEFAULT 1 NOT NULL,
    CONSTRAINT uq_modulo_codigo UNIQUE (codigo),
    CONSTRAINT uq_modulo_url UNIQUE (url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE permiso (
    id_permiso    INT AUTO_INCREMENT PRIMARY KEY,
    idperfil      INT               NOT NULL,
    idmodulo      INT               NOT NULL,
    estadopermiso TINYINT DEFAULT 1 NOT NULL,
    CONSTRAINT uq_perfil_modulo UNIQUE (idperfil, idmodulo),
    CONSTRAINT fk_permiso_modulo FOREIGN KEY (idmodulo) REFERENCES modulo (id_modulo) ON DELETE CASCADE,
    CONSTRAINT fk_permiso_perfil FOREIGN KEY (idperfil) REFERENCES perfil (id_perfil) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100)           NOT NULL,
    apellido         VARCHAR(100)           NOT NULL,
    usuario          VARCHAR(50)            NOT NULL,
    clave            VARCHAR(255)           NOT NULL,
    estado           CHAR(1)    DEFAULT '1' NOT NULL,
    dni              CHAR(8)                NULL,
    telefono         VARCHAR(20)            NULL,
    idperfil_usuario INT                    NOT NULL,
    correo           VARCHAR(100)           NULL,
    direccion        VARCHAR(255)           NULL,
    CONSTRAINT uq_usuario_login UNIQUE (usuario),
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (idperfil_usuario) REFERENCES perfil (id_perfil) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;