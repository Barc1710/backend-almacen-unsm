package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IngresoResponse(
        Integer id,
        Integer idProveedor,
        String razonSocialProveedor,
        String rucProveedor,
        Integer idUsuario,
        String nombreUsuario,
        Integer idEncargadoAlmacen,
        String nombreEncargadoAlmacen,
        Integer idJefe,
        String nombreJefe,
        String prefijo,
        Integer correlativo,
        String numeroCompleto,
        String numeroOrdenCompra,
        String descripcion,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        Integer totalItems,
        List<DetalleIngresoResponse> detalles
) {
}
