package pe.edu.unsm.almacen.service;

import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;

public interface IKardexService {

    PageResponse<KardexMovimientoResponse> listarPorArticulo(Integer idArticulo, Pageable pageable);
}
