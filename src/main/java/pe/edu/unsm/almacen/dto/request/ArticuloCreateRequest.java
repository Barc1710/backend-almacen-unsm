package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ArticuloCreateRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20, message = "El código no debe exceder 20 caracteres")
        String codigo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no debe exceder 255 caracteres")
        String descripcion,

        @NotNull(message = "El ID de familia es obligatorio")
        Integer idFamilia,

        @NotNull(message = "El ID de marca es obligatorio")
        Integer idMarca,

        @NotNull(message = "El ID de ubicación es obligatorio")
        Integer idUbicacion,

        @NotNull(message = "La cantidad mínima es obligatoria")
        @PositiveOrZero(message = "La cantidad mínima debe ser mayor o igual a 0")
        @Digits(integer = 10, fraction = 2, message = "El stock mínimo admite como máximo 2 decimales")
        BigDecimal cantidadMinima,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a 0")
        @Digits(integer = 10, fraction = 2, message = "El precio admite como máximo 2 decimales")
        BigDecimal precio,

        @Size(max = 255, message = "El detalle no debe exceder 255 caracteres")
        String detalle
) {
}
