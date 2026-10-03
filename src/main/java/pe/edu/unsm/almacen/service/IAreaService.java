package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.AreaRequest;
import pe.edu.unsm.almacen.dto.response.AreaResponse;

public interface IAreaService {

    PageResponse<AreaResponse> listarPaginado(String filtro, Pageable pageable);

    List<AreaResponse> listarActivos();

    AreaResponse obtenerPorId(Integer id);

    AreaResponse crear(AreaRequest request);

    AreaResponse actualizar(Integer id, AreaRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
