package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UbicacionRequest;
import pe.edu.unsm.almacen.dto.response.UbicacionResponse;
import pe.edu.unsm.almacen.entity.Ubicacion;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.UbicacionRepository;
import pe.edu.unsm.almacen.service.IUbicacionService;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements IUbicacionService {

    private final UbicacionRepository ubicacionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UbicacionResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Ubicacion> page = ubicacionRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listarActivos() {
        return ubicacionRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionResponse obtenerPorId(Integer id) {
        return ubicacionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public UbicacionResponse crear(UbicacionRequest request) {
        Ubicacion ubicacion = Ubicacion.builder()
                .nombre(request.nombre().trim())
                .descripcion(request.descripcion() != null ? request.descripcion().trim() : null)
                .estado("1")
                .build();
        return mapToResponse(ubicacionRepository.save(ubicacion));
    }

    @Override
    @Transactional
    public UbicacionResponse actualizar(Integer id, UbicacionRequest request) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con id: " + id));

        ubicacion.setNombre(request.nombre().trim());
        ubicacion.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : null);
        return mapToResponse(ubicacionRepository.save(ubicacion));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Ubicacion ubicacion = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada con id: " + id));
        ubicacion.setEstado(nuevoEstado);
        ubicacionRepository.save(ubicacion);
    }

    private UbicacionResponse mapToResponse(Ubicacion ubicacion) {
        return new UbicacionResponse(ubicacion.getId(), ubicacion.getNombre(), ubicacion.getDescripcion(), ubicacion.getEstado());
    }
}
