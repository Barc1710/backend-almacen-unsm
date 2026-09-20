package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        String usuario,

        @NotBlank(message = "La clave es obligatoria")
        String clave
) {
}
