package pe.edu.unsm.almacen.dto.request;

import java.math.BigDecimal;

public record DetalleIngresoRequest(
        Integer idArticulo,
        BigDecimal cantidad,
        BigDecimal precio) {
}
