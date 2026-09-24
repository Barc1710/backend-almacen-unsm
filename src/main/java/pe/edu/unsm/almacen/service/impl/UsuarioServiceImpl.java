package pe.edu.unsm.almacen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UsuarioCreateRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioResetClaveRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioUpdateRequest;
import pe.edu.unsm.almacen.dto.response.UsuarioResponse;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.PerfilRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.service.IUsuarioService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(String filtro, Integer idPerfil, String estado, Pageable pageable) {
        Page<Usuario> page = usuarioRepository.buscar(filtro, idPerfil, estado, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest request) {
        String username = request.usuario().trim();
        if (usuarioRepository.findByUsuario(username).isPresent()) {
            throw new DuplicateResourceException("Usuario duplicado: " + username);
        }

        Perfil perfil = perfilRepository.findById(request.idPerfil())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + request.idPerfil()));

        Usuario usuario = Usuario.builder()
                .usuario(username)
                .clave(passwordEncoder.encode(request.clave()))
                .nombre(request.nombre().trim())
                .apellido(request.apellido().trim())
                .dni(request.dni() != null ? request.dni().trim() : null)
                .telefono(request.telefono() != null ? request.telefono().trim() : null)
                .correo(request.correo() != null ? request.correo().trim() : null)
                .perfil(perfil)
                .estado("1")
                .debeCambiarClave(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario '{}' creado con id: {}", guardado.getUsuario(), guardado.getIdUsuario());
        return mapToResponse(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        Perfil perfil = perfilRepository.findById(request.idPerfil())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + request.idPerfil()));

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setDni(request.dni() != null ? request.dni().trim() : null);
        usuario.setTelefono(request.telefono() != null ? request.telefono().trim() : null);
        usuario.setCorreo(request.correo() != null ? request.correo().trim() : null);
        usuario.setPerfil(perfil);

        if (request.estado() != null && !request.estado().isBlank()) {
            usuario.setEstado(request.estado().trim());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario '{}' actualizado (id: {})", actualizado.getUsuario(), actualizado.getIdUsuario());
        return mapToResponse(actualizado);
    }

    @Override
    @Transactional
    public void resetearClave(Integer id, UsuarioResetClaveRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        usuario.setClave(passwordEncoder.encode(request.nuevaClave()));
        usuario.setDebeCambiarClave(true);
        usuarioRepository.save(usuario);
        log.info("Contraseña restablecida para el usuario '{}' (id: {})", usuario.getUsuario(), id);
    }

    private UsuarioResponse mapToResponse(Usuario u) {
        return new UsuarioResponse(
                u.getIdUsuario(),
                u.getUsuario(),
                u.getNombreCompleto(),
                u.getDni(),
                u.getCorreo(),
                u.getTelefono(),
                u.getPerfil() != null ? u.getPerfil().getIdPerfil() : null,
                u.getPerfil() != null ? u.getPerfil().getNombrePerfil() : null,
                u.getEstado(),
                u.getDebeCambiarClave()
        );
    }
}
