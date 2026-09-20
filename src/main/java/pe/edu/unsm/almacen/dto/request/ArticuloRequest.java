package pe.edu.unsm.almacen.dto.request;

import java.math.BigDecimal;

public record ArticuloRequest(
        String codigo,
        String descripcion,
        Integer idFamilia,
        Integer idMarca,
        Integer idUbicacion,
        BigDecimal cantidadMinima,
        BigDecimal precio,
        String detalle) {
}
