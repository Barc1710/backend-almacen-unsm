package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.MarcaRequest;
import pe.edu.unsm.almacen.dto.response.MarcaResponse;
import pe.edu.unsm.almacen.entity.Marca;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.MarcaRepository;
import pe.edu.unsm.almacen.service.IMarcaService;

@Service
@RequiredArgsConstructor
public class MarcaServiceImpl implements IMarcaService {

    private final MarcaRepository marcaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MarcaResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Marca> page = marcaRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaResponse> listarActivos() {
        return marcaRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaResponse obtenerPorId(Integer id) {
        return marcaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public MarcaResponse crear(MarcaRequest request) {
        Marca marca = Marca.builder()
                .nombre(request.nombre().trim())
                .estado("1")
                .build();
        return mapToResponse(marcaRepository.save(marca));
    }

    @Override
    @Transactional
    public MarcaResponse actualizar(Integer id, MarcaRequest request) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));

        marca.setNombre(request.nombre().trim());
        return mapToResponse(marcaRepository.save(marca));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
        marca.setEstado(nuevoEstado);
        marcaRepository.save(marca);
    }

    private MarcaResponse mapToResponse(Marca marca) {
        return new MarcaResponse(marca.getId(), marca.getNombre(), marca.getEstado());
    }
}
