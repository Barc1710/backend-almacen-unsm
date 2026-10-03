package pe.edu.unsm.almacen.dto.response;

import java.util.List;

public record PerfilPermisosResponse(
        Integer idPerfil,
        String nombrePerfil,
        List<ModuloResponse> modulosAutorizados
) {
}
