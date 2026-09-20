package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ClienteRequest;
import pe.edu.unsm.almacen.dto.response.ClienteResponse;

public interface IClienteService {

    PageResponse<ClienteResponse> listarPaginado(String filtro, Pageable pageable);

    List<ClienteResponse> listarActivos();

    ClienteResponse obtenerPorId(Integer id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Integer id, ClienteRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
