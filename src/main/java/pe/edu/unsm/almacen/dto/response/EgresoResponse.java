package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EgresoResponse(
        Integer id,
        Integer idCliente,
        String nombreCliente,
        Integer idEncargado,
        String nombreEncargado,
        String nombreEncargadoLibre,
        Integer idArea,
        String nombreArea,
        Integer idEncargadoAlmacen,
        String nombreEncargadoAlmacen,
        Integer idUsuario,
        String nombreUsuario,
        String ambiente,
        String prefijo,
        Integer correlativo,
        String numeroCompleto,
        String tipoEgreso,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        List<DetalleEgresoResponse> detalles
) {
}
