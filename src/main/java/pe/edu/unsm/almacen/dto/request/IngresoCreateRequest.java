package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record IngresoCreateRequest(
        @NotNull(message = "El proveedor es obligatorio")
        Integer idProveedor,

        @Size(max = 255, message = "La descripción no debe superar los 255 caracteres")
        String descripcion,

        @NotEmpty(message = "El documento debe incluir al menos una línea de detalle")
        @Size(max = 100, message = "No se pueden procesar más de 100 líneas por transacción")
        List<@NotNull(message = "La línea de detalle no puede ser nula") @Valid DetalleItemRequest> detalles
) {
}
