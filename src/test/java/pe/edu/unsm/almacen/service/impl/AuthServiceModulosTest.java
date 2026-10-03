package pe.edu.unsm.almacen.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import pe.edu.unsm.almacen.entity.Modulo;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Permiso;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.repository.PermisoRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.jwt.JwtProvider;

class AuthServiceModulosTest {

    @Test
    void menuSoloIncluyePermisosYModulosActivos() {
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        ModuloRepository modulos = mock(ModuloRepository.class);
        PermisoRepository permisos = mock(PermisoRepository.class);
        AuthServiceImpl servicio = new AuthServiceImpl(mock(AuthenticationManager.class),
                mock(JwtProvider.class), usuarios, modulos, permisos);
        Perfil perfil = Perfil.builder().idPerfil(7).nombrePerfil("ALMACENERO").estadoPerfil(1).build();
        Usuario usuario = Usuario.builder().usuario("ana").estado("1").perfil(perfil).build();
        Modulo ingreso = Modulo.builder().idModulo(2).codigo("INGRESOS").nombre("Ingresos")
                .url("/ingresos").estado(1).build();
        Modulo egresoInactivo = Modulo.builder().idModulo(3).codigo("EGRESOS").nombre("Egresos")
                .url("/egresos").estado(0).build();
        when(usuarios.findByUsuario("ana")).thenReturn(Optional.of(usuario));
        when(permisos.findByPerfil_IdAndEstado(7, 1)).thenReturn(List.of(
                Permiso.builder().modulo(ingreso).estadoPermiso(1).build(),
                Permiso.builder().modulo(egresoInactivo).estadoPermiso(1).build()));

        var respuesta = servicio.obtenerMisModulos("ana");

        assertEquals(1, respuesta.size());
        assertEquals("INGRESOS", respuesta.getFirst().codigo());
    }
}
