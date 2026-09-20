package pe.edu.unsm.almacen.service;

import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;

public interface IEgresoService {

    PageResponse<EgresoResponse> listar(Pageable pageable);

    EgresoResponse obtenerPorId(Integer id);

    EgresoResponse registrar(EgresoRequest request);
}
