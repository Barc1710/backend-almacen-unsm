package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ArticuloResponse(
        Integer id,
        String codigo,
        String descripcion,
        Integer idUnidadMedida,
        String nombreUnidadMedida,
        String simboloUnidadMedida,
        Boolean permiteDecimales,
        Integer idFamilia,
        String nombreFamilia,
        Integer idMarca,
        String nombreMarca,
        Integer idUbicacion,
        String nombreUbicacion,
        BigDecimal saldo,
        BigDecimal cantidadMinima,
        BigDecimal precio,
        Boolean activo,
        String estado,
        String detalle,
        LocalDateTime fecha
) {
}
