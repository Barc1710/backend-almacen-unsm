package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UnidadMedidaRequest;
import pe.edu.unsm.almacen.dto.response.UnidadMedidaResponse;
import pe.edu.unsm.almacen.entity.UnidadMedida;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.UnidadMedidaRepository;
import pe.edu.unsm.almacen.service.IUnidadMedidaService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnidadMedidaServiceImpl implements IUnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UnidadMedidaResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<UnidadMedida> page = unidadMedidaRepository.buscar(filtro, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnidadMedidaResponse> listarActivos() {
        return unidadMedidaRepository.findByEstadoOrderByNombreAsc("1")
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnidadMedidaResponse obtenerPorId(Integer id) {
        return unidadMedidaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public UnidadMedidaResponse crear(UnidadMedidaRequest request) {
        String codigoLimpio = request.codigoSunat().trim().toUpperCase();

        if (unidadMedidaRepository.existsByCodigoSunat(codigoLimpio)) {
            throw new DuplicateResourceException("Código SUNAT duplicado: " + codigoLimpio);
        }

        UnidadMedida unidad = UnidadMedida.builder()
                .codigoSunat(codigoLimpio)
                .nombre(request.nombre().trim())
                .simbolo(request.simbolo().trim())
                .permiteDecimales(Boolean.TRUE.equals(request.permiteDecimales()))
                .estado("1")
                .build();

        UnidadMedida guardada = unidadMedidaRepository.save(unidad);
        log.info("Unidad de medida creada: ID={}, Código={}", guardada.getId(), guardada.getCodigoSunat());
        return mapToResponse(guardada);
    }

    @Override
    @Transactional
    public UnidadMedidaResponse actualizar(Integer id, UnidadMedidaRequest request) {
        UnidadMedida unidad = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + id));

        String codigoLimpio = request.codigoSunat().trim().toUpperCase();
        if (unidadMedidaRepository.existsByCodigoSunatAndIdNot(codigoLimpio, id)) {
            throw new DuplicateResourceException("Código SUNAT duplicado: " + codigoLimpio);
        }

        unidad.setCodigoSunat(codigoLimpio);
        unidad.setNombre(request.nombre().trim());
        unidad.setSimbolo(request.simbolo().trim());
        unidad.setPermiteDecimales(Boolean.TRUE.equals(request.permiteDecimales()));

        UnidadMedida actualizada = unidadMedidaRepository.save(unidad);
        log.info("Unidad de medida actualizada: ID={}", id);
        return mapToResponse(actualizada);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        UnidadMedida unidad = unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unidad de medida no encontrada con ID: " + id));
        unidad.setEstado(nuevoEstado);
        unidadMedidaRepository.save(unidad);
        log.info("Estado de unidad de medida ID={} cambiado a {}", id, nuevoEstado);
    }

    private UnidadMedidaResponse mapToResponse(UnidadMedida u) {
        return new UnidadMedidaResponse(
                u.getId(),
                u.getCodigoSunat(),
                u.getNombre(),
                u.getSimbolo(),
                u.getPermiteDecimales(),
                u.getEstado()
        );
    }
}
