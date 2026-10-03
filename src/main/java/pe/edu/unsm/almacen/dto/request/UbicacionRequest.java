package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbicacionRequest(
        @NotBlank(message = "El nombre de la ubicación es obligatorio")
        @Size(max = 100, message = "El nombre de la ubicación no debe exceder 100 caracteres")
        String nombre,

        @Size(max = 150, message = "La descripción no debe exceder 150 caracteres")
        String descripcion
) {
}
