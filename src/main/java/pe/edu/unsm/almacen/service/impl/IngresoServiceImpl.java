package pe.edu.unsm.almacen.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.IngresoRequest;
import pe.edu.unsm.almacen.dto.response.IngresoResponse;
import pe.edu.unsm.almacen.service.IIngresoService;

@Service
public class IngresoServiceImpl implements IIngresoService {

    @Override
    public PageResponse<IngresoResponse> listar(Pageable pageable) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public IngresoResponse obtenerPorId(Integer id) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public IngresoResponse registrar(IngresoRequest request) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
