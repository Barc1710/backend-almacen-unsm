package pe.edu.unsm.almacen.security;

import java.util.List;
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

        List<String> codigosCandidatos = resolverCodigosEquivalentes(codigo);
        boolean algunoExiste = codigosCandidatos.stream()
                .anyMatch(c -> moduloRepository.existsByCodigoAndEstado(c, 1));
        if (!algunoExiste) {
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
                    return codigosCandidatos.stream()
                            .anyMatch(c -> permisoRepository.existsActiveByPerfilAndCodigo(perfil.getIdPerfil(), c));
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canRead(Authentication authentication, String codigoRecurso, String... codigosRelacionados) {
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

    private List<String> resolverCodigosEquivalentes(String codigo) {
        if (codigo == null) {
            return List.of();
        }
        String clean = codigo.trim().toUpperCase();
        if ("INVENTARIO".equals(clean)) {
            return List.of(clean, "INVENTARIO_ARTICULOS", "INVENTARIO_FAMILIAS", "INVENTARIO_MARCAS", "ARTICULOS");
        }
        if ("ARTICULOS".equals(clean) || "INVENTARIO_ARTICULOS".equals(clean)) {
            return List.of(clean, "INVENTARIO_ARTICULOS", "INVENTARIO", "ARTICULOS");
        }
        if ("FAMILIAS".equals(clean) || "INVENTARIO_FAMILIAS".equals(clean)) {
            return List.of(clean, "INVENTARIO_FAMILIAS", "FAMILIAS", "INVENTARIO");
        }
        if ("MARCAS".equals(clean) || "INVENTARIO_MARCAS".equals(clean)) {
            return List.of(clean, "INVENTARIO_MARCAS", "MARCAS", "INVENTARIO");
        }
        if ("PERFILES".equals(clean) || "SEGURIDAD_PERFILES".equals(clean)) {
            return List.of(clean, "SEGURIDAD_PERFILES", "PERFILES", "SEGURIDAD");
        }
        if ("USUARIOS".equals(clean) || "SEGURIDAD_USUARIOS".equals(clean)) {
            return List.of(clean, "SEGURIDAD_USUARIOS", "USUARIOS", "SEGURIDAD");
        }
        if ("SEGURIDAD".equals(clean)) {
            return List.of(clean, "SEGURIDAD_USUARIOS", "SEGURIDAD_PERFILES", "SEGURIDAD");
        }
        return List.of(clean);
    }

    private boolean esAdministrador(Usuario usuario) {
        Perfil perfil = usuario.getPerfil();
        return Integer.valueOf(1).equals(perfil.getIdPerfil())
                && "ADMINISTRADOR".equalsIgnoreCase(perfil.getNombrePerfil().trim());
    }
}
