package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EncargadoRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoResponse;
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
import pe.edu.unsm.almacen.service.IEncargadoService;

@Service
@RequiredArgsConstructor
public class EncargadoServiceImpl implements IEncargadoService {

    private final EncargadoRepository encargadoRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncargadoResponse> listarPaginado(String filtro, Pageable pageable) {
        Page<Encargado> page = encargadoRepository.buscar(filtro, pageable);
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
                .ambiente(request.ambiente() != null ? request.ambiente().trim() : null)
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
        encargado.setAmbiente(request.ambiente() != null ? request.ambiente().trim() : null);

        return mapToResponse(encargadoRepository.save(encargado));
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        Encargado encargado = encargadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con id: " + id));
        encargado.setEstado(nuevoEstado);
        encargadoRepository.save(encargado);
    }

    private EncargadoResponse mapToResponse(Encargado encargado) {
        return new EncargadoResponse(
                encargado.getId(),
                encargado.getSiglaProfesion(),
                encargado.getNombres(),
                encargado.getApellidos(),
                encargado.getNombreCompleto(),
                encargado.getDni(),
                encargado.getAmbiente(),
                encargado.getEstado()
        );
    }
}
