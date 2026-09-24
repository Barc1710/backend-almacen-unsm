package pe.edu.unsm.almacen.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @NotBlank(message = "El nombre del perfil es obligatorio")
        @Size(max = 100, message = "El nombre del perfil no puede exceder 100 caracteres")
        String nombre
) {
}
