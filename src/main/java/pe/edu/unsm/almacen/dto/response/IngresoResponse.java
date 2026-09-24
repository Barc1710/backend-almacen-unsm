package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IngresoResponse(
        Integer id,
        Integer idProveedor,
        String razonSocialProveedor,
        String rucProveedor,
        String numeroOrdenCompra,
        String descripcion,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        List<DetalleIngresoResponse> detalles
) {
}
