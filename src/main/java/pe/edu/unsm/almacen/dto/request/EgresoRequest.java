package pe.edu.unsm.almacen.dto.request;

import java.util.List;

public record EgresoRequest(
        Integer idCliente,
        Integer idEncargado,
        Integer idArea,
        Integer idEncargadoAlmacen,
        String ambiente,
        List<DetalleEgresoRequest> detalles) {
}
