package pe.edu.unsm.almacen.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.repository.PermisoRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;

class ModuloAccessTest {

    private final ModuloRepository modulos = mock(ModuloRepository.class);
    private final PermisoRepository permisos = mock(PermisoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final ModuloAccess access = new ModuloAccess(modulos, permisos, usuarios);

    @Test
    void permisoActivoAutorizaYSuRevocacionNiegaLaMismaSesion() {
        Authentication sesion = sesion(7, "ALMACENERO", "ROLE_PERFIL");
        usuario(7, "ALMACENERO");
        when(modulos.existsByCodigoAndEstado("INGRESOS", 1)).thenReturn(true);
        when(permisos.existsActiveByPerfilAndCodigo(7, "INGRESOS")).thenReturn(true, false);

        assertTrue(access.hasAccess(sesion, "INGRESOS"));
        assertFalse(access.hasAccess(sesion, "INGRESOS"));
    }

    @Test
    void moduloInactivoNiegaAccesoAunqueElPerfilTengaPermiso() {
        Authentication sesion = sesion(7, "ALMACENERO", "ROLE_PERFIL");

        assertFalse(access.hasAccess(sesion, "INGRESOS"));
        verifyNoInteractions(permisos, usuarios);
    }

    @Test
    void moduloDeLecturaInactivoBloqueaTambienElAccesoDesdeOtroModulo() {
        Authentication sesion = sesion(7, "ALMACENERO", "ROLE_PERFIL");

        assertFalse(access.canRead(sesion, "ARTICULOS", "INGRESOS"));
        verifyNoInteractions(permisos, usuarios);
    }

    @Test
    void moduloOperativoPermiteLeerSuCatalogoAuxiliarActivo() {
        Authentication sesion = sesion(7, "ALMACENERO", "ROLE_PERFIL");
        usuario(7, "ALMACENERO");
        when(modulos.existsByCodigoAndEstado("ARTICULOS", 1)).thenReturn(true);
        when(modulos.existsByCodigoAndEstado("INGRESOS", 1)).thenReturn(true);
        when(permisos.existsActiveByPerfilAndCodigo(7, "INGRESOS")).thenReturn(true);

        assertTrue(access.canRead(sesion, "ARTICULOS", "INGRESOS"));
    }

    @Test
    void administradorSoloOmiteLaAsignacionYRespetaElEstadoDelModulo() {
        Authentication sesion = sesion(1, "ADMINISTRADOR", "ROLE_ADMINISTRADOR");
        usuario(1, "ADMINISTRADOR");
        when(modulos.existsByCodigoAndEstado("EGRESOS", 1)).thenReturn(true);

        assertTrue(access.hasAccess(sesion, "EGRESOS"));
        assertFalse(access.hasAccess(sesion, "INGRESOS"));
        verifyNoInteractions(permisos);
    }

    @Test
    void nombreDeAdministradorEnOtroPerfilNoOtorgaPrivilegios() {
        Authentication sesion = sesion(7, "ADMINISTRADOR", "ROLE_PERFIL");
        usuario(7, "ADMINISTRADOR");
        when(modulos.existsByCodigoAndEstado("EGRESOS", 1)).thenReturn(true);

        assertFalse(access.hasAccess(sesion, "EGRESOS"));
    }

    private void usuario(int idPerfil, String nombrePerfil) {
        Perfil perfil = Perfil.builder().idPerfil(idPerfil).nombrePerfil(nombrePerfil).estadoPerfil(1).build();
        Usuario usuario = Usuario.builder().idUsuario(7).usuario("ana").estado("1").perfil(perfil).build();
        if (idPerfil == 1) {
            usuario.setIdUsuario(1);
        }
        when(usuarios.findById(usuario.getIdUsuario())).thenReturn(Optional.of(usuario));
    }

    private Authentication sesion(int id, String perfil, String rol) {
        UserDetailsImpl principal = new UserDetailsImpl(id, "Ana", "Pérez", "ana", "", perfil, false,
                true, List.of(new SimpleGrantedAuthority(rol)));
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }
}
