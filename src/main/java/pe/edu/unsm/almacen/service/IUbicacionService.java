package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UbicacionRequest;
import pe.edu.unsm.almacen.dto.response.UbicacionResponse;

public interface IUbicacionService {

    PageResponse<UbicacionResponse> listarPaginado(String filtro, Pageable pageable);

    List<UbicacionResponse> listarActivos();

    UbicacionResponse obtenerPorId(Integer id);

    UbicacionResponse crear(UbicacionRequest request);

    UbicacionResponse actualizar(Integer id, UbicacionRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
