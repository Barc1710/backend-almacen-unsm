package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record DetalleItemRequest(
        @NotNull(message = "El ID del artículo es obligatorio")
        Integer idArticulo,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        @Digits(integer = 10, fraction = 2, message = "La cantidad debe tener como máximo 2 decimales")
        BigDecimal cantidad,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a cero")
        @Digits(integer = 10, fraction = 2, message = "El precio debe tener como máximo 2 decimales")
        BigDecimal precio
) {
}
