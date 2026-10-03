package pe.edu.unsm.almacen.security;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.repository.PermisoRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;

@Component("moduloAccess")
@RequiredArgsConstructor
public class ModuloAccess {

    private final ModuloRepository moduloRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public boolean hasAccess(Authentication authentication, String codigo) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)
                || !principal.isEnabled()) {
            return false;
        }

        if (!moduloRepository.existsByCodigoAndEstado(codigo, 1)) {
            return false;
        }

        return usuarioRepository.findById(principal.getId())
                .filter(usuario -> Objects.equals(usuario.getUsuario(), principal.getUsername()))
                .filter(usuario -> "1".equals(usuario.getEstado()))
                .map(usuario -> {
                    Perfil perfil = usuario.getPerfil();
                    if (perfil == null || !Integer.valueOf(1).equals(perfil.getEstado())) {
                        return false;
                    }
                    if (esAdministrador(usuario)) {
                        return true;
                    }
                    return permisoRepository.existsActiveByPerfilAndCodigo(perfil.getIdPerfil(), codigo);
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canRead(Authentication authentication, String codigoRecurso, String... codigosRelacionados) {
        if (!moduloRepository.existsByCodigoAndEstado(codigoRecurso, 1)) {
            return false;
        }
        if (hasAccess(authentication, codigoRecurso)) {
            return true;
        }
        for (String codigo : codigosRelacionados) {
            if (hasAccess(authentication, codigo)) {
                return true;
            }
        }
        return false;
    }

    private boolean esAdministrador(Usuario usuario) {
        Perfil perfil = usuario.getPerfil();
        return Integer.valueOf(1).equals(perfil.getIdPerfil())
                && "ADMINISTRADOR".equalsIgnoreCase(perfil.getNombrePerfil().trim());
    }
}
