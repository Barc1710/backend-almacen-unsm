package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DetalleEgresoRequest(
        @NotNull(message = "El ID del artículo es obligatorio")
        Integer idArticulo,

        @NotNull(message = "La cantidad a despachar es obligatoria")
        @Positive(message = "La cantidad a despachar debe ser mayor a cero")
        BigDecimal cantidad
) {
}
