package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record DetalleItemRequest(
        @NotNull(message = "El ID del artículo es obligatorio")
        Integer idArticulo,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        BigDecimal cantidad,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
        BigDecimal precio
) {
}
