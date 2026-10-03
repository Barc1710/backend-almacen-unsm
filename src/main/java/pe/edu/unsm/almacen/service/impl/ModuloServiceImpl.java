package pe.edu.unsm.almacen.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.entity.Modulo;
import pe.edu.unsm.almacen.repository.ModuloRepository;
import pe.edu.unsm.almacen.service.IModuloService;

@Service
@RequiredArgsConstructor
public class ModuloServiceImpl implements IModuloService {

    private final ModuloRepository moduloRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModuloResponse> listarModulosActivos() {
        return moduloRepository.findByEstadoOrderByOrdenAsc(1)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ModuloResponse mapToResponse(Modulo m) {
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
