package pe.edu.unsm.almacen.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;
import pe.edu.unsm.almacen.service.IEgresoService;

@Service
public class EgresoServiceImpl implements IEgresoService {

    @Override
    public PageResponse<EgresoResponse> listar(Pageable pageable) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public EgresoResponse obtenerPorId(Integer id) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public EgresoResponse registrar(EgresoRequest request) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
