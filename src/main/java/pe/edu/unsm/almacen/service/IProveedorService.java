package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ProveedorRequest;
import pe.edu.unsm.almacen.dto.response.ProveedorResponse;

public interface IProveedorService {

    PageResponse<ProveedorResponse> listarPaginado(String filtro, Pageable pageable);

    List<ProveedorResponse> listarActivos();

    ProveedorResponse obtenerPorId(Integer id);

    ProveedorResponse crear(ProveedorRequest request);

    ProveedorResponse actualizar(Integer id, ProveedorRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
