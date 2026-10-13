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
import pe.edu.unsm.almacen.dto.request.EncargadoAlmacenRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoAlmacenResponse;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.EncargadoAlmacenRepository;
import pe.edu.unsm.almacen.repository.PerfilRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.service.IEncargadoAlmacenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncargadoAlmacenServiceImpl implements IEncargadoAlmacenService {

    private final EncargadoAlmacenRepository encargadoAlmacenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncargadoAlmacenResponse> listarPaginado(String filtro, String estado, Pageable pageable) {
        String filtroLimpio = (filtro != null && !filtro.trim().isEmpty()) ? filtro.trim() : null;
        String estadoLimpio = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        Page<EncargadoAlmacen> page = encargadoAlmacenRepository.buscar(filtroLimpio, estadoLimpio, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EncargadoAlmacenResponse> listarActivos() {
        return encargadoAlmacenRepository.findByEstadoOrderByApellidosAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EncargadoAlmacenResponse obtenerPorId(Integer id) {
        return encargadoAlmacenRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public EncargadoAlmacenResponse crear(EncargadoAlmacenRequest request) {
        boolean esTitular = Boolean.TRUE.equals(request.esTitular());
        if (esTitular) {
            encargadoAlmacenRepository.desmarcarTodosTitulares();
        }
        EncargadoAlmacen encargadoAlmacen = EncargadoAlmacen.builder()
                .nombres(request.nombres().trim())
                .apellidos(request.apellidos().trim())
                .dni(request.dni() != null && !request.dni().isBlank() ? request.dni().trim() : null)
                .esTitular(esTitular)
                .estado("1")
                .build();
        return mapToResponse(encargadoAlmacenRepository.save(encargadoAlmacen));
    }

    @Override
    @Transactional
    public EncargadoAlmacenResponse actualizar(Integer id, EncargadoAlmacenRequest request) {
        EncargadoAlmacen encargadoAlmacen = encargadoAlmacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con id: " + id));

        encargadoAlmacen.setNombres(request.nombres().trim());
        encargadoAlmacen.setApellidos(request.apellidos().trim());
        encargadoAlmacen.setDni(request.dni() != null && !request.dni().isBlank() ? request.dni().trim() : null);
        if (request.esTitular() != null) {
            if (Boolean.TRUE.equals(request.esTitular()) && "0".equals(encargadoAlmacen.getEstado())) {
                throw new BusinessException("Un encargado inactivo no puede ser asignado como titular de turno");
            }
            if (Boolean.TRUE.equals(request.esTitular())) {
                encargadoAlmacenRepository.desmarcarOtrosTitulares(id);
            }
            encargadoAlmacen.setEsTitular(request.esTitular());
        }
        return mapToResponse(encargadoAlmacenRepository.save(encargadoAlmacen));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        EncargadoAlmacen encargadoAlmacen = encargadoAlmacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con id: " + id));
        encargadoAlmacen.setEstado(nuevoEstado);
        // Si se desactiva y es titular, se le revoca la titularidad
        if ("0".equals(nuevoEstado) && Boolean.TRUE.equals(encargadoAlmacen.getEsTitular())) {
            encargadoAlmacen.setEsTitular(false);
            log.info("Titularidad revocada al desactivar encargado de almacén id: {}", id);
        }
        // Si se desactiva y tiene usuario vinculado, se suspende la cuenta de acceso
        if ("0".equals(nuevoEstado) && encargadoAlmacen.getUsuario() != null) {
            Usuario usuario = encargadoAlmacen.getUsuario();
            usuario.setEstado("0");
            usuarioRepository.save(usuario);
            log.info("Usuario '{}' suspendido al desactivar encargado de almacén id: {}", usuario.getUsuario(), id);
        }
        encargadoAlmacenRepository.save(encargadoAlmacen);
    }

    @Override
    @Transactional
    public EncargadoAlmacenResponse alternarTitular(Integer id) {
        EncargadoAlmacen encargado = encargadoAlmacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con id: " + id));

        if ("0".equals(encargado.getEstado())) {
            throw new BusinessException("Un encargado inactivo no puede ser asignado como titular de turno");
        }

        boolean nuevoEstado = !Boolean.TRUE.equals(encargado.getEsTitular());
        if (nuevoEstado) {
            encargadoAlmacenRepository.desmarcarOtrosTitulares(id);
        }
        encargado.setEsTitular(nuevoEstado);
        log.info("Titularidad alternada a {} para encargado de almacén id: {}", nuevoEstado, id);
        return mapToResponse(encargadoAlmacenRepository.save(encargado));
    }

    @Override
    @Transactional
    public EncargadoAlmacenResponse crearUsuario(Integer id, CrearUsuarioEncargadoRequest request) {
        EncargadoAlmacen encargado = encargadoAlmacenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con id: " + id));

        if ("0".equals(encargado.getEstado())) {
            throw new BusinessException("No se puede crear un usuario para un encargado inactivo");
        }

        if (encargado.getUsuario() != null) {
            throw new BusinessException("El encargado de almacén ya tiene una cuenta de usuario asignada (" + encargado.getUsuario().getUsuario() + ")");
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
        EncargadoAlmacen actualizado = encargadoAlmacenRepository.save(encargado);

        log.info("Usuario '{}' creado y vinculado con éxito al encargado de almacén id: {}", username, id);
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

    private EncargadoAlmacenResponse mapToResponse(EncargadoAlmacen encargadoAlmacen) {
        return new EncargadoAlmacenResponse(
                encargadoAlmacen.getId(),
                encargadoAlmacen.getNombres(),
                encargadoAlmacen.getApellidos(),
                encargadoAlmacen.getNombreCompleto(),
                encargadoAlmacen.getDni(),
                encargadoAlmacen.getEstado(),
                encargadoAlmacen.getEsTitular(),
                encargadoAlmacen.getIdUsuario(),
                encargadoAlmacen.getLoginUsuario(),
                encargadoAlmacen.tieneUsuario()
        );
    }
}
