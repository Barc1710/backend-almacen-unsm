package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetalleEgresoResponse(
        Integer id,
        Integer idArticulo,
        String codigoArticulo,
        String descripcionArticulo,
        BigDecimal cantidad,
        BigDecimal precio,
        BigDecimal subtotal,
        BigDecimal saldo,
        LocalDateTime fecha,
        String tipo
) {
}
