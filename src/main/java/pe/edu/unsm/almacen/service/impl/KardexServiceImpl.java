package pe.edu.unsm.almacen.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;
import pe.edu.unsm.almacen.service.IKardexService;

@Service
public class KardexServiceImpl implements IKardexService {

    @Override
    public PageResponse<KardexMovimientoResponse> listarPorArticulo(Integer idArticulo, Pageable pageable) {
        // TODO: implementar este caso de uso.
        throw new UnsupportedOperationException("Pendiente de implementación");
    }
}
