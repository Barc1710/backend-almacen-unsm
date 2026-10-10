package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EncargadoAlmacenRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoAlmacenResponse;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.EncargadoAlmacenRepository;
import pe.edu.unsm.almacen.service.IEncargadoAlmacenService;

@Service
@RequiredArgsConstructor
public class EncargadoAlmacenServiceImpl implements IEncargadoAlmacenService {

    private final EncargadoAlmacenRepository encargadoAlmacenRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncargadoAlmacenResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<EncargadoAlmacen> page = encargadoAlmacenRepository.buscar(filtro, pageable);
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
        EncargadoAlmacen encargadoAlmacen = EncargadoAlmacen.builder()
                .nombres(request.nombres().trim())
                .apellidos(request.apellidos().trim())
                .dni(request.dni() != null && !request.dni().isBlank() ? request.dni().trim() : null)
                .esTitular(Boolean.TRUE.equals(request.esTitular()))
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
        encargadoAlmacenRepository.save(encargadoAlmacen);
    }

    private EncargadoAlmacenResponse mapToResponse(EncargadoAlmacen encargadoAlmacen) {
        return new EncargadoAlmacenResponse(
                encargadoAlmacen.getId(),
                encargadoAlmacen.getNombres(),
                encargadoAlmacen.getApellidos(),
                encargadoAlmacen.getNombreCompleto(),
                encargadoAlmacen.getDni(),
                encargadoAlmacen.getEstado(),
                encargadoAlmacen.getEsTitular()
        );
    }
}
