package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.AreaRequest;
import pe.edu.unsm.almacen.dto.response.AreaResponse;
import pe.edu.unsm.almacen.entity.Area;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.AreaRepository;
import pe.edu.unsm.almacen.service.IAreaService;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements IAreaService {

    private final AreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AreaResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Area> page = areaRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> listarActivos() {
        return areaRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AreaResponse obtenerPorId(Integer id) {
        return areaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public AreaResponse crear(AreaRequest request) {
        Area area = Area.builder()
                .nombre(request.nombre().trim())
                .estado("1")
                .build();
        return mapToResponse(areaRepository.save(area));
    }

    @Override
    @Transactional
    public AreaResponse actualizar(Integer id, AreaRequest request) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con id: " + id));

        area.setNombre(request.nombre().trim());
        return mapToResponse(areaRepository.save(area));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con id: " + id));
        area.setEstado(nuevoEstado);
        areaRepository.save(area);
    }

    private AreaResponse mapToResponse(Area area) {
        return new AreaResponse(area.getId(), area.getNombre(), area.getEstado());
    }
}
