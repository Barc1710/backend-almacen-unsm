package pe.edu.unsm.almacen.dto.request;

import java.math.BigDecimal;

public record DetalleEgresoRequest(
        Integer idArticulo,
        BigDecimal cantidad) {
}
