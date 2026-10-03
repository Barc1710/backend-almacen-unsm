package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.entity.Modulo;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Permiso;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.repository.PermisoRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.jwt.JwtProvider;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;
import pe.edu.unsm.almacen.service.IAuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final PermisoRepository permisoRepository;

    @Override
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.clave())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtProvider.generateToken(userDetails);

        log.info("Usuario '{}' autenticado con perfil '{}'", userDetails.getUsername(), userDetails.getPerfil());

        return new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getNombreCompleto(),
                userDetails.getPerfil(),
                userDetails.getDebeCambiarClave()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuloResponse> obtenerMisModulos(String username) {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));

        Perfil perfil = usuario.getPerfil();
        if (!"1".equals(usuario.getEstado()) || perfil == null
                || !Integer.valueOf(1).equals(perfil.getEstado())) {
            return List.of();
        }

        String nombrePerfil = perfil.getNombrePerfil();
        if (perfil.getIdPerfil() == 1 && nombrePerfil != null
                && "ADMINISTRADOR".equalsIgnoreCase(nombrePerfil.trim())) {
            return moduloRepository.findByEstadoOrderByOrdenAsc(1).stream()
                    .map(this::mapModuloToResponse)
                    .toList();
        }

        return permisoRepository.findByPerfil_IdAndEstado(perfil.getIdPerfil(), 1).stream()
                .map(Permiso::getModulo)
                .filter(m -> m != null && m.getCodigo() != null && Integer.valueOf(1).equals(m.getEstado()))
                .map(this::mapModuloToResponse)
                .toList();
    }

    private ModuloResponse mapModuloToResponse(Modulo m) {
        return new ModuloResponse(
                m.getIdModulo(),
                m.getCodigo(),
                m.getNombre(),
                m.getUrl(),
                m.getIcono(),
                m.getOrden()
        );
    }
}
