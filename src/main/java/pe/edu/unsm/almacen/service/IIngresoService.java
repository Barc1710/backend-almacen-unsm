package pe.edu.unsm.almacen.service;

import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.IngresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.IngresoResponse;

public interface IIngresoService {

    PageResponse<IngresoResponse> listar(Integer idProveedor, LocalDate desde, LocalDate hasta, Pageable pageable);

    IngresoResponse obtenerPorId(Integer id);

    IngresoResponse registrar(IngresoCreateRequest request);
}
