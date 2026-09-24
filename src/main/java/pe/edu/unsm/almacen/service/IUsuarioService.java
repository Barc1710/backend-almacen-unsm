package pe.edu.unsm.almacen.service;

import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UsuarioCreateRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioResetClaveRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioUpdateRequest;
import pe.edu.unsm.almacen.dto.response.UsuarioResponse;

public interface IUsuarioService {

    PageResponse<UsuarioResponse> listar(String filtro, Integer idPerfil, String estado, Pageable pageable);

    UsuarioResponse obtenerPorId(Integer id);

    UsuarioResponse crear(UsuarioCreateRequest request);

    UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest request);

    void resetearClave(Integer id, UsuarioResetClaveRequest request);
}
