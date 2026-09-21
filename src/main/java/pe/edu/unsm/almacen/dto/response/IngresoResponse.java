package pe.edu.unsm.almacen.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record IngresoResponse(
        Integer id,
        Integer idProveedor,
        String razonSocialProveedor,
        String rucProveedor,
        String descripcion,
        LocalDateTime fecha,
        String estado,
        List<DetalleIngresoResponse> detalles
) {
}
