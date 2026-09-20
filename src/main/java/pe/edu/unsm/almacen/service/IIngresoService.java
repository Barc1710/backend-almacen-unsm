package pe.edu.unsm.almacen.service;

import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.IngresoRequest;
import pe.edu.unsm.almacen.dto.response.IngresoResponse;

public interface IIngresoService {

    PageResponse<IngresoResponse> listar(Pageable pageable);

    IngresoResponse obtenerPorId(Integer id);

    IngresoResponse registrar(IngresoRequest request);
}
