package pe.edu.unsm.almacen.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record EgresoResponse(
        Integer id,
        Integer idCliente,
        Integer idEncargado,
        Integer idArea,
        Integer idEncargadoAlmacen,
        String ambiente,
        String prefijo,
        Integer correlativo,
        LocalDateTime fecha,
        String estado,
        List<DetalleEgresoResponse> detalles) {
}
