package pe.edu.unsm.almacen.dto.response.dashboard;

import java.math.BigDecimal;

public record StockFamiliaResponse(
        Integer idFamilia,
        String nombre,
        long totalArticulos,
        BigDecimal saldoTotal
) {}
