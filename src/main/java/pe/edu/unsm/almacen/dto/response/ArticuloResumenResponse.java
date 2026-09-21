package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;

public record ArticuloResumenResponse(
        Integer id,
        String codigo,
        String descripcion,
        BigDecimal saldo,
        BigDecimal precio,
        String nombreFamilia,
        String nombreMarca,
        String nombreUbicacion,
        Boolean activo
) {
}
