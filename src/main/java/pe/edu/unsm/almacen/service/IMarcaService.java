package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.MarcaRequest;
import pe.edu.unsm.almacen.dto.response.MarcaResponse;

public interface IMarcaService {

    PageResponse<MarcaResponse> listarPaginado(String filtro, Pageable pageable);

    List<MarcaResponse> listarActivos();

    MarcaResponse obtenerPorId(Integer id);

    MarcaResponse crear(MarcaRequest request);

    MarcaResponse actualizar(Integer id, MarcaRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
