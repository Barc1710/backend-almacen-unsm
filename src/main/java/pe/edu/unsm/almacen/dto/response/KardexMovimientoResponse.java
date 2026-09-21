package pe.edu.unsm.almacen.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.edu.unsm.almacen.entity.TipoMovimiento;

public record KardexMovimientoResponse(
        Long id,
        Integer idArticulo,
        String codigoArticulo,
        String descripcionArticulo,
        LocalDateTime fecha,
        TipoMovimiento tipoMovimiento,
        String documentoTipo,
        Integer documentoId,
        String documentoReferencia,
        BigDecimal entrada,
        BigDecimal salida,
        BigDecimal saldoResultante,
        Integer idUsuario,
        String usuarioResponsable
) {
    public LocalDateTime fechaHora() {
        return fecha;
    }

    public BigDecimal cantidadEntrada() {
        return entrada;
    }

    public BigDecimal cantidadSalida() {
        return salida;
    }
}
