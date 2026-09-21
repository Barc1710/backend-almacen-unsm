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

        @NotEmpty(message = "Debe registrar al menos un artículo en el ingreso")
        @Valid
        List<DetalleItemRequest> detalles
) {
}
