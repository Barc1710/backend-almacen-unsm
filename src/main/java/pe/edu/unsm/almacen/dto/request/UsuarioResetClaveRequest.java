package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioResetClaveRequest(
        @NotBlank(message = "La nueva clave no puede estar vacía")
        @Size(min = 6, max = 100, message = "La clave debe tener entre 6 y 100 caracteres")
        String nuevaClave
) {
}
