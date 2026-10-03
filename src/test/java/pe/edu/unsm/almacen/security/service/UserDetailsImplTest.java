package pe.edu.unsm.almacen.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;

class UserDetailsImplTest {

    @Test
    void soloElPerfilAdministrativoReservadoRecibeElRolPrivilegiado() {
        Perfil perfilCreado = Perfil.builder().idPerfil(7).nombrePerfil("ROLE_ADMINISTRADOR").estadoPerfil(1).build();
        Usuario usuario = Usuario.builder().idUsuario(10).usuario("ana").estado("1").perfil(perfilCreado).build();

        assertEquals("ROLE_PERFIL", UserDetailsImpl.build(usuario).getAuthorities().iterator().next().getAuthority());

        usuario.setPerfil(Perfil.builder().idPerfil(1).nombrePerfil("ADMINISTRADOR").estadoPerfil(1).build());
        assertEquals("ROLE_ADMINISTRADOR", UserDetailsImpl.build(usuario).getAuthorities().iterator().next().getAuthority());
    }
}
