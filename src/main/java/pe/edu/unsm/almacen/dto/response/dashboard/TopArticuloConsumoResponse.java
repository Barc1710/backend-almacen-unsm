package pe.edu.unsm.almacen.dto.response.dashboard;

import java.math.BigDecimal;

public record TopArticuloConsumoResponse(
        Integer idArticulo,
        String descripcion,
        String unidadMedida,
        BigDecimal totalConsumido
) {}
