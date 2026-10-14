package pe.edu.unsm.almacen.dto.response.dashboard;

import java.math.BigDecimal;

public record ArticuloStockCriticoResponse(
        Integer id,
        String codigo,
        String descripcion,
        String familia,
        BigDecimal saldo,
        BigDecimal cantidadMinima,
        double porcentaje,
        String unidadMedida
) {}
