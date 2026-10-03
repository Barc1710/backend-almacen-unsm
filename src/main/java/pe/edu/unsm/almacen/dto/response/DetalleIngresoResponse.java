package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleIngresoResponse(
        Integer id,
        Integer idArticulo,
        String codigoArticulo,
        String descripcionArticulo,
        String simboloUnidadMedida,
        Boolean permiteDecimales,
        BigDecimal cantidad,
        BigDecimal precio,
        BigDecimal saldo,
        LocalDateTime fecha,
        String tipo
) {
}
