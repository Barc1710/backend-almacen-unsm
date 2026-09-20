package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FamiliaRequest(
        @NotBlank(message = "El nombre de la familia es obligatorio")
        @Size(max = 100, message = "El nombre de la familia no debe exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "La inicial es obligatoria")
        @Size(max = 10, message = "La inicial no debe exceder 10 caracteres")
        String inicial
) {
}
