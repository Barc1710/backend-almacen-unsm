package pe.edu.unsm.almacen.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ArticuloRequest;
import pe.edu.unsm.almacen.dto.response.ArticuloResponse;
import pe.edu.unsm.almacen.service.IArticuloService;

@Service
public class ArticuloServiceImpl implements IArticuloService {

    @Override
    public PageResponse<ArticuloResponse> listar(Pageable pageable) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public ArticuloResponse obtenerPorId(Integer id) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public ArticuloResponse crear(ArticuloRequest request) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }

    @Override
    public ArticuloResponse actualizar(Integer id, ArticuloRequest request) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
