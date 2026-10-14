package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.CrearUsuarioEncargadoRequest;
import pe.edu.unsm.almacen.dto.request.EncargadoRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoResponse;
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
import pe.edu.unsm.almacen.repository.PerfilRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.service.IEncargadoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncargadoServiceImpl implements IEncargadoService {

    private final EncargadoRepository encargadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncargadoResponse> listarPaginado(String filtro, String estado, Pageable pageable) {
        String filtroLimpio = (filtro != null && !filtro.trim().isEmpty()) ? filtro.trim() : null;
        String estadoLimpio = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        Page<Encargado> page = encargadoRepository.buscar(filtroLimpio, estadoLimpio, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EncargadoResponse> listarActivos() {
        return encargadoRepository.findByEstadoOrderByApellidosAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EncargadoResponse obtenerPorId(Integer id) {
        return encargadoRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public EncargadoResponse crear(EncargadoRequest request) {
        Encargado encargado = Encargado.builder()
                .siglaProfesion(request.siglaProfesion() != null ? request.siglaProfesion().trim() : null)
                .nombres(request.nombres().trim())
                .apellidos(request.apellidos().trim())
                .dni(request.dni() != null ? request.dni().trim() : null)
                .cargo(request.cargo() != null ? request.cargo().trim() : null)
                .estado("1")
                .build();
        return mapToResponse(encargadoRepository.save(encargado));
    }

    @Override
    @Transactional
    public EncargadoResponse actualizar(Integer id, EncargadoRequest request) {
        Encargado encargado = encargadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con id: " + id));

        encargado.setSiglaProfesion(request.siglaProfesion() != null ? request.siglaProfesion().trim() : null);
        encargado.setNombres(request.nombres().trim());
        encargado.setApellidos(request.apellidos().trim());
        encargado.setDni(request.dni() != null ? request.dni().trim() : null);
        encargado.setCargo(request.cargo() != null ? request.cargo().trim() : null);

        return mapToResponse(encargadoRepository.save(encargado));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Encargado encargado = encargadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con id: " + id));
        encargado.setEstado(nuevoEstado);
        // Si se desactiva y tiene usuario vinculado, se suspende la cuenta de acceso
        if ("0".equals(nuevoEstado) && encargado.getUsuario() != null) {
            Usuario usuario = encargado.getUsuario();
            usuario.setEstado("0");
            usuarioRepository.save(usuario);
            log.info("Usuario '{}' suspendido al desactivar encargado id: {}", usuario.getUsuario(), id);
        }
        encargadoRepository.save(encargado);
    }

    @Override
    @Transactional
    public EncargadoResponse crearUsuario(Integer id, CrearUsuarioEncargadoRequest request) {
        Encargado encargado = encargadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con id: " + id));

        if ("0".equals(encargado.getEstado())) {
            throw new BusinessException("No se puede crear un usuario para un encargado inactivo");
        }

        if (encargado.getUsuario() != null) {
            throw new BusinessException("El encargado ya tiene una cuenta de usuario asignada (" + encargado.getUsuario().getUsuario() + ")");
        }

        validarAsignacionPerfilAdmin(request.idPerfil());

        String username = request.usuario().trim();
        if (usuarioRepository.findByUsuario(username).isPresent()) {
            throw new DuplicateResourceException("El nombre de usuario '" + username + "' ya está en uso");
        }

        Perfil perfil = perfilRepository.findById(request.idPerfil())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + request.idPerfil()));

        if (perfil.getEstado() != null && !Integer.valueOf(1).equals(perfil.getEstado())) {
            throw new BusinessException("No se puede asignar un perfil inactivo a un usuario");
        }

        Usuario usuario = Usuario.builder()
                .usuario(username)
                .clave(passwordEncoder.encode(request.clave()))
                .nombre(encargado.getNombres().trim())
                .apellido(encargado.getApellidos().trim())
                .dni(encargado.getDni() != null && !encargado.getDni().isBlank() ? encargado.getDni().trim() : null)
                .telefono(request.telefono() != null && !request.telefono().isBlank() ? request.telefono().trim() : null)
                .correo(request.correo() != null && !request.correo().isBlank() ? request.correo().trim() : null)
                .perfil(perfil)
                .estado("1")
                .debeCambiarClave(true)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        encargado.setUsuario(guardado);
        Encargado actualizado = encargadoRepository.save(encargado);

        log.info("Usuario '{}' creado y vinculado con éxito al encargado id: {}", username, id);
        return mapToResponse(actualizado);
    }

    private void validarAsignacionPerfilAdmin(Integer idPerfil) {
        if (Integer.valueOf(1).equals(idPerfil)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                throw new BusinessException("No tiene permisos para asignar el perfil de Administrador");
            }
            String username = auth.getName();
            boolean esAdmin = usuarioRepository.findByUsuario(username)
                    .filter(u -> "1".equals(u.getEstado()))
                    .map(u -> u.getPerfil() != null && Integer.valueOf(1).equals(u.getPerfil().getIdPerfil()))
                    .orElse(false);
            if (!esAdmin) {
                throw new BusinessException("No tiene permisos para asignar el perfil de Administrador");
            }
        }
    }

    private EncargadoResponse mapToResponse(Encargado encargado) {
        return new EncargadoResponse(
                encargado.getId(),
                encargado.getSiglaProfesion(),
                encargado.getNombres(),
                encargado.getApellidos(),
                encargado.getNombreCompleto(),
                encargado.getDni(),
                encargado.getCargo(),
                encargado.getEstado(),
                encargado.getIdUsuario(),
                encargado.getLoginUsuario(),
                encargado.tieneUsuario()
        );
    }
}
