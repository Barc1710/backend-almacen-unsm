package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.unsm.almacen.entity.TipoMovimiento;

public record KardexMovimientoResponse(
        Long id,
        Integer idArticulo,
        TipoMovimiento tipoMovimiento,
        String documentoTipo,
        Integer documentoId,
        BigDecimal cantidadEntrada,
        BigDecimal cantidadSalida,
        BigDecimal saldoResultante,
        Integer idUsuario,
        LocalDateTime fechaHora) {
}
