package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AreaRequest(
        @NotBlank(message = "El nombre del área es obligatorio")
        @Size(max = 100, message = "El nombre del área no debe exceder 100 caracteres")
        String nombre
) {
}
