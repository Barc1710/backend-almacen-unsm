package pe.edu.unsm.almacen.service.impl;

import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.request.AsignarPermisosRequest;
import pe.edu.unsm.almacen.dto.request.PerfilRequest;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.dto.response.PerfilPermisosResponse;
import pe.edu.unsm.almacen.dto.response.PerfilResponse;
import pe.edu.unsm.almacen.entity.Modulo;
import pe.edu.unsm.almacen.entity.Perfil;
import pe.edu.unsm.almacen.entity.Permiso;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.repository.PerfilRepository;
import pe.edu.unsm.almacen.repository.PermisoRepository;
import pe.edu.unsm.almacen.service.IPerfilService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerfilServiceImpl implements IPerfilService {

    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PerfilResponse> listarActivos() {
        return perfilRepository.findByEstadoOrderByNombreAsc(1)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilResponse obtenerPorId(Integer id) {
        return perfilRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public PerfilResponse crear(PerfilRequest request) {
        String nombre = request.nombre().trim();
        if (perfilRepository.existsByNombre(nombre)) {
            throw new DuplicateResourceException("Perfil duplicado: " + nombre);
        }

        Perfil perfil = Perfil.builder()
                .nombrePerfil(nombre)
                .estadoPerfil(1)
                .build();

        Perfil guardado = perfilRepository.save(perfil);
        log.info("Perfil '{}' creado con id: {}", guardado.getNombrePerfil(), guardado.getIdPerfil());
        return mapToResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilPermisosResponse obtenerModulosPorPerfil(Integer idPerfil) {
        Perfil perfil = perfilRepository.findById(idPerfil)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + idPerfil));

        if (esAdministrador(perfil)) {
            List<ModuloResponse> modulos = moduloRepository.findByEstadoOrderByOrdenAsc(1).stream()
                    .map(this::mapModuloToResponse)
                    .toList();
            return new PerfilPermisosResponse(perfil.getIdPerfil(), perfil.getNombrePerfil(), modulos);
        }

        List<ModuloResponse> modulos = permisoRepository.findByPerfil_IdAndEstado(idPerfil, 1)
                .stream()
                .map(Permiso::getModulo)
                .filter(m -> m != null && m.getCodigo() != null && Integer.valueOf(1).equals(m.getEstado()))
                .map(this::mapModuloToResponse)
                .toList();

        return new PerfilPermisosResponse(perfil.getIdPerfil(), perfil.getNombrePerfil(), modulos);
    }

    @Override
    @Transactional
    public void actualizarPermisos(Integer idPerfil, AsignarPermisosRequest request) {
        Perfil perfil = perfilRepository.findById(idPerfil)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil no encontrado con id: " + idPerfil));

        if (esAdministrador(perfil)) {
            throw new BusinessException("El perfil administrador tiene acceso a todos los módulos activos");
        }

        if (request.idsModulos() != null && !request.idsModulos().isEmpty()) {
            if (new HashSet<>(request.idsModulos()).size() != request.idsModulos().size()) {
                throw new BusinessException("La lista de módulos contiene identificadores repetidos");
            }
            List<Modulo> modulos = moduloRepository.findAllById(request.idsModulos());
            if (modulos.size() != request.idsModulos().size()
                    || modulos.stream().anyMatch(m -> m.getCodigo() == null || !Integer.valueOf(1).equals(m.getEstado()))) {
                throw new BusinessException("Solo se pueden asignar módulos definidos y activos");
            }
            permisoRepository.deleteByPerfil_Id(idPerfil);
            List<Permiso> nuevosPermisos = modulos.stream()
                    .map(m -> Permiso.builder()
                            .perfil(perfil)
                            .modulo(m)
                            .estadoPermiso(1)
                            .build())
                    .toList();

            permisoRepository.saveAll(nuevosPermisos);
            log.info("Asignados {} módulos al perfil '{}' (id: {})", nuevosPermisos.size(), perfil.getNombrePerfil(), idPerfil);
        } else {
            permisoRepository.deleteByPerfil_Id(idPerfil);
            log.info("Permisos eliminados del perfil '{}' (id: {})", perfil.getNombrePerfil(), idPerfil);
        }
    }

    private PerfilResponse mapToResponse(Perfil p) {
        return new PerfilResponse(
                p.getIdPerfil(),
                p.getNombrePerfil(),
                p.getEstado()
        );
    }

    private boolean esAdministrador(Perfil perfil) {
        return Integer.valueOf(1).equals(perfil.getIdPerfil())
                && "ADMINISTRADOR".equalsIgnoreCase(perfil.getNombrePerfil().trim());
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
