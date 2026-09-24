package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.FamiliaRequest;
import pe.edu.unsm.almacen.dto.response.FamiliaResponse;
import pe.edu.unsm.almacen.entity.Familia;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.FamiliaRepository;
import pe.edu.unsm.almacen.service.IFamiliaService;

@Service
@RequiredArgsConstructor
public class FamiliaServiceImpl implements IFamiliaService {

    private final FamiliaRepository familiaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FamiliaResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Familia> page = familiaRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamiliaResponse> listarActivos() {
        return familiaRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FamiliaResponse obtenerPorId(Integer id) {
        return familiaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public FamiliaResponse crear(FamiliaRequest request) {
        Familia familia = Familia.builder()
                .nombre(request.nombre().trim())
                .inicial(request.inicial().trim().toUpperCase())
                .correlativo(1)
                .estado("1")
                .build();
        Familia familiaGuardada = familiaRepository.save(familia);

        return mapToResponse(familiaGuardada);
    }

    @Override
    @Transactional
    public FamiliaResponse actualizar(Integer id, FamiliaRequest request) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));

        familia.setNombre(request.nombre().trim());
        familia.setInicial(request.inicial().trim().toUpperCase());
        return mapToResponse(familiaRepository.save(familia));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Familia familia = familiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Familia no encontrada con id: " + id));
        familia.setEstado(nuevoEstado);
        familiaRepository.save(familia);
    }

    private FamiliaResponse mapToResponse(Familia familia) {
        return new FamiliaResponse(
                familia.getId(),
                familia.getNombre(),
                familia.getInicial(),
                familia.getCorrelativo(),
                familia.getEstado()
        );
    }
}
